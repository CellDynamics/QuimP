/**
 * 
 */
package uk.warwick.dic.lid;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import uk.warwick.tools.images.ExtraImageProcessor;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;
/**
 * Implementation of Kam algorithm with use of matrix approach
 * @author baniuk
 *
 */
public class DICReconstruction {
	
	private static final Logger logger = LogManager.getLogger(DICReconstruction.class.getName());
	private final int shift = 1; // shift added to original image to eliminate 0 values
	
	private ImagePlus srcImage;
	private double decay;
	private double angle;
	private double[] decays; // reference to preallocated decay data
	private int maxWidth;
	private int[][] ranges;
	private ImageStatistics is;
	/**
	 * Default constructor
	 * @throws DicException Throws exception after generateRanges()
	 */
	public DICReconstruction(ImagePlus srcImage, double decay, double angle) throws DicException {
		this.srcImage = srcImage;
		this.angle = angle;
		this.decay = decay;
		is = srcImage.getProcessor().getStatistics();
		// get mean value
		logger.debug("Mean value is " + is.mean);
		recalculate(); // ranges calculated above
	}
	

	/**
	 * Sets new reconstruction parameters for current object
	 * @param decay
	 * @param angle
	 * @throws DicException Rethrow exception after generateRanges()
	 */
	public void setParams(double decay, double angle) throws DicException {
		this.angle = angle;
		this.decay = decay;
		recalculate();
	}
	
	/**
	 * Setup private fields.
	 * TODO should accept slice number?
	 * @throws DicException when input image is close to saturation e.g. has values of 65536-shift. This is due to applied algorithm 
	 * of detection image pixels after rotation.
	 */
	private void generateRanges() throws DicException {
		double minpixel, maxpixel; // minimal pixel value
		int r; // loop indexes
		int firstpixel, lastpixel; // first and last pixel of image in line
		
		ExtraImageProcessor srcImageCopyProcessor;
		// make copy of original image to not modify it - converting to 16bit
		srcImageCopyProcessor = new ExtraImageProcessor(srcImage.getProcessor().convertToShort(false));
		logger.debug("Type of image " + srcImageCopyProcessor.getIP().getBitDepth() + " bit");
		// check condition for removing 0 value from image
		srcImageCopyProcessor.getIP().resetMinAndMax();	// ensure that minmax will be recalculated (usually they are stored in class field)
		minpixel = srcImageCopyProcessor.getIP().getMin();
		maxpixel = srcImageCopyProcessor.getIP().getMax();
		logger.debug("Pixel range is " + minpixel + " " + maxpixel);
		if(maxpixel > 65535-shift) {
			logger.warn("Possible image clipping - check if image is saturated");
			throw new DicException(String.format("Possible image clipping - input image has at leas one pixel with value %d",65535-shift));
		}
		// set interpolation
		srcImageCopyProcessor.getIP().setInterpolationMethod(ImageProcessor.BICUBIC);
		// Rotating image - set 0 background
		srcImageCopyProcessor.getIP().setBackgroundValue(0.0);
				// scale pixels by adding 1 - we remove any 0 value from source image
		srcImageCopyProcessor.getIP().add(shift); 	
		srcImageCopyProcessor.getIP().resetMinAndMax(); logger.debug("Pixel range after shift is " + srcImageCopyProcessor.getIP().getMin() + " " + srcImageCopyProcessor.getIP().getMax());
		// rotate image with extending it. borders have the same value as background
		srcImageCopyProcessor.rotate(angle,true); // WARN May happen that after interpolation pixels gets 0 again ?
		int newWidth = srcImageCopyProcessor.getIP().getWidth();
		int newHeight = srcImageCopyProcessor.getIP().getHeight();
		ImageProcessor srcImageProcessorUnwrapped = srcImageCopyProcessor.getIP();
		maxWidth = newWidth;
		ranges = new int[newHeight][2];
		for(r=0; r<newHeight; r++) {
			// to not process whole line, detect where starts and ends pixels of image (reject background added during rotation)
			for(firstpixel=0; firstpixel<newWidth && srcImageProcessorUnwrapped.get(firstpixel,r)==0;firstpixel++);
			for(lastpixel=newWidth-1;lastpixel>=0 && srcImageProcessorUnwrapped.get(lastpixel,r)==0;lastpixel--);
			ranges[r][0] = firstpixel;
			ranges[r][1] = lastpixel;
		}
		
	}
	
	/**
	 * Recalculates tables on demand
	 * @throws DicException Rethrow exception after generateRanges()
	 */
	private void recalculate() throws DicException {
		// calculate preallocated decay data
		// generateRanges() must be called first as it initializes fields used by generateDecay()
		generateRanges();
		generateDeacy(decay, maxWidth);
	}
	
   /**
	 * Reconstruct DIC image by LID method using LID method
	 * Make copy of original image to not change it.
	 * It is assumed that user counts angle in anti-clockwise direction and the shear angle 
	 * must be specified in this way as well.
	 * The algorithm process only correct pixels of rotated image to prevent artifacts on edges. This
	 * pixels are detected in less-computational way. First the image is converted to 16bit and the value of \c shift is added
	 * to each pixel. In this way original image does not contain 0. Then image is rotated with 0 padding. Thus any 0 on 
	 * rotated and prepared to reconstruction image does not belong to right pixels. 
	 * @remarks The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally, thus correct \c angle should be provided
	 * @warning Used optimisation with detecting of image pixels based on their value may not be accurate when input image 
	 * will contain saturated pixels
	 * @param srcImage DIC image to be reconstructed
	 * @param decay Decay factor (positive) defined as exponent
	 * @param angle Shear angle counted from x axis in anti-clockwise direction (mathematically)
	 * @retval ImagePlus
	 * @return Return reconstruction of \c srcImage as 8-bit image
	 */
	public ImagePlus reconstructionDicLid() {
		logger.debug("Input image: "+ String.valueOf(srcImage.getWidth()) + " " + 
				String.valueOf(srcImage.getHeight()) + " " + 
				String.valueOf(srcImage.getImageStackSize()) + " " +  
				String.valueOf(srcImage.getBitDepth()));
		
		double cumsumup, cumsumdown;
		int c,u,d,r; // loop indexes
		int linindex = 0; // output table linear index
		ExtraImageProcessor srcImageCopyProcessor;
		// make copy of original image to not modify it - converting to 16bit
		srcImageCopyProcessor = new ExtraImageProcessor(srcImage.getProcessor().convertToShort(false));
		// set interpolation
		srcImageCopyProcessor.getIP().setInterpolationMethod(ImageProcessor.BICUBIC);
		// Rotating image - set 0 background
		srcImageCopyProcessor.getIP().setBackgroundValue(0.0);
		
		srcImageCopyProcessor.rotate(angle,true);
		// dereferencing for optimization purposes
		int newWidth = srcImageCopyProcessor.getIP().getWidth();
		int newHeight = srcImageCopyProcessor.getIP().getHeight();
		ImageProcessor srcImageProcessorUnwrapped = srcImageCopyProcessor.getIP();
		// create array for storing results - 32bit float as imageprocessor		
		ExtraImageProcessor outputArrayProcessor = new ExtraImageProcessor(new FloatProcessor(newWidth, newHeight));
		float[] outputPixelArray = (float[]) outputArrayProcessor.getIP().getPixels();
		
		// do for every row - bas-relief is oriented horizontally 
		for(r=0; r<newHeight; r++) {
			// ranges[r][0] - first image pixel in line r
			// ranges[r][1] - last image pixel in line r
			linindex = linindex + ranges[r][0];
			// for every point apply KAM formula
			for(c=ranges[r][0]; c<=ranges[r][1]; c++) {
				// up
				cumsumup = 0;
				for(u=c; u>=ranges[r][0]; u--) {
					cumsumup += (srcImageProcessorUnwrapped.get(u, r)-is.mean)*decays[Math.abs(u-c)];
				}
				// down
				cumsumdown = 0; // cumulative sum from point r to the end of column
				for(d=c; d<=ranges[r][1]; d++) {
					cumsumdown += (srcImageProcessorUnwrapped.get(d,r)-is.mean)*decays[Math.abs(d-c)];
				}
				// integral
				outputPixelArray[linindex] = (float)(cumsumup - cumsumdown); // linear indexing is in row-order
				linindex++;
			}
			linindex = linindex + newWidth-ranges[r][1]-1;
		}
		// rotate back output processor
		outputArrayProcessor.getIP().setBackgroundValue(0.0);
		outputArrayProcessor.getIP().rotate(-angle);
		// crop it back to original size
		outputArrayProcessor.cropImageAfterRotation(srcImage.getWidth(), srcImage.getHeight());

		// replace outputImage processor with result array with scaling conversion
		ImagePlus outputImage = new ImagePlus("", outputArrayProcessor.getIP().convertToByte(true));

		return outputImage; // return reconstruction
	}
	
	/**
	 * Generates decay table with exponential distances between pixels multiplied by decay coefficient
	 * @param decay The value of decay coefficient
	 * @param length Length of table, usually equals to longest processed line on image
	 * @retval double[]
	 * @return Table with decays coefficients 
	 */
	private void generateDeacy(double decay, int length) {
		decays = new double[length];
		
		for(int i=0;i<length;i++)
			decays[i] = Math.exp(-decay*i);
	}
}
