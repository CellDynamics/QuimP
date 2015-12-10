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
	
	/**
	 * Empty default constructor
	 */
	public DICReconstruction() {

	}
	
   /**
	 * Reconstruct DIC image by LID method using LID method
	 * Make copy of original image to not change it.
	 * It is assumed that user counts angle in anti-clockwise direction and the shear angle 
	 * must be specified in this way as well.
	 * The algorithm process only correct pixels of rotated image to prevent artifacts on edges. This
	 * pixels are detected in less-computational way. First image is converted to 16bit and the value of \c shift is added
	 * to each pixel. In this way original image does not contain 0. Then image is rotated with 0 padding. Thus any 0 on 
	 * rotated and prepared to reconstruction image does not belong to right pixels. 
	 * @remarks The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally 
	 * @param srcImage DIC image to be reconstructed
	 * @param decay Decay factor (positive) defined as exponent
	 * @param angle Shear angle counted from x axis in anti-clockwise direction (mathematically)
	 * @retval ImagePlus
	 * @return Return reconstruction of \c srcImage as 8-bit image
	 */
	public ImagePlus reconstructionDicLid(ImagePlus srcImage, double decay, double angle) {
		logger.debug("Input image: "+ String.valueOf(srcImage.getWidth()) + " " + 
				String.valueOf(srcImage.getHeight()) + " " + 
				String.valueOf(srcImage.getImageStackSize()) + " " +  
				String.valueOf(srcImage.getBitDepth()));
		
		double cumsumup, cumsumdown;
		int c,u,d,r; // loop indexes
		int linindex = 0;
		double minpixel, maxpixel; // minimal pixel value
		int firstpixel, lastpixel; // first and last pixel of image in line
		double meanofimage; // mean value of source image
		// make copy of original image to not modify it - converting to 16bit
		ExtraImageProcessor srcImageCopyProcessor = new ExtraImageProcessor(srcImage.getProcessor().convertToShort(false));
		logger.debug("Type of image " + srcImageCopyProcessor.getIP().getBitDepth() + " bit");
		// set interpolation
		srcImageCopyProcessor.getIP().setInterpolationMethod(ImageProcessor.BICUBIC);
		// get mean value
		ImageStatistics is = srcImageCopyProcessor.getIP().getStatistics();
		meanofimage = is.mean;
		logger.debug("Mean value is " + is.mean);
		// check condition for removing 0 value from image
		srcImageCopyProcessor.getIP().resetMinAndMax();	// ensure that minmax will be recalculated (usually they are stored in class field)
		minpixel = srcImageCopyProcessor.getIP().getMin();
		maxpixel = srcImageCopyProcessor.getIP().getMax();
		logger.debug("Pixel range is " + minpixel + " " + maxpixel);
		if(maxpixel > 65535-shift)
			logger.warn("Possible image clipping - check if image is saturated");
		// scale pixels by adding 1 - we remove any 0 value from source image
		srcImageCopyProcessor.getIP().add(shift); 	
		// Rotating image - set 0 background
		srcImageCopyProcessor.getIP().setBackgroundValue(0.0);
		srcImageCopyProcessor.getIP().resetMinAndMax(); logger.debug("Pixel range after shift is " + srcImageCopyProcessor.getIP().getMin() + " " + srcImageCopyProcessor.getIP().getMax());
		// rotate image with extending it. borders have the same value as background
		srcImageCopyProcessor.rotate(angle,true); // WARN May happen that after interpolation pixels gets 0 again ?
		
		// get new sizes for optimisation purposes
		int newWidth = srcImageCopyProcessor.getIP().getWidth();
		int newHeight = srcImageCopyProcessor.getIP().getHeight();
		ImageProcessor srcImageProcessorUnwrapped = srcImageCopyProcessor.getIP();
		// -------------------- end of optimisation
		
		// create array for storing results - 32bit float as imageprocessor		
		ExtraImageProcessor outputArrayProcessor = new ExtraImageProcessor(new FloatProcessor(newWidth, newHeight));
		float[] outputPixelArray = (float[]) outputArrayProcessor.getIP().getPixels();
		
		// do for every row
		logger.debug("Image size " + newWidth + " " + newHeight);
		for(r=0; r<newHeight; r++) {
			for(firstpixel=0; firstpixel<newWidth && srcImageProcessorUnwrapped.getPixel(firstpixel,r)==0;firstpixel++);
			for(lastpixel=newWidth-1;lastpixel>=0 && srcImageProcessorUnwrapped.getPixel(lastpixel,r)==0;lastpixel--);
			//logger.debug("First last " + firstpixel + " " + lastpixel);
			linindex = linindex + firstpixel;
			// for every point apply KAM formula
			for(c=firstpixel; c<=lastpixel; c++) {
				// up
				cumsumup = 0;
				for(u=c; u>=firstpixel; u--) {
					cumsumup += (srcImageProcessorUnwrapped.getPixel(u, r)-shift-meanofimage)*Math.exp(-decay*Math.abs(u-c));		// TODO change for get as faster version
				}
				// down
				cumsumdown = 0; // cumulative sum from point r to the end of column
				for(d=c; d<=lastpixel; d++) {
					cumsumdown += (srcImageProcessorUnwrapped.getPixel(d,r)-shift-meanofimage)*Math.exp(-decay*Math.abs(d-c));
				}
				// integral
				outputPixelArray[linindex] = (float)(cumsumup - cumsumdown); // linear indexing is in row-order
				linindex++;
			}
			linindex = linindex + newWidth-lastpixel-1;
		}
		// rotate back output processor
		outputArrayProcessor.getIP().setBackgroundValue(0.0);
		outputArrayProcessor.getIP().rotate(-angle);
		// crop it back
		outputArrayProcessor.cropImageAfterRotation(srcImage.getWidth(), srcImage.getHeight());

		// replace outputImage processor with result array with scaling conversion
		ImagePlus outputImage = new ImagePlus("", outputArrayProcessor.getIP().convertToByte(true));

		return outputImage; // return reconstruction
	}
}
