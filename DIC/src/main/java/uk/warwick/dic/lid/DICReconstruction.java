/**
 * 
 */
package uk.warwick.dic.lid;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
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
	 * @remarks The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally 
	 * @param srcImage DIC image to be reconstructed
	 * @param decay Decay factor (positive) defined as exponent
	 * @param angle Shear angle counted from x axis in anti-clockwise direction (mathematically)
	 * @retval ImagePlus
	 * @return Return reconstruction of \a srcImage as 8-bit image
	 */
	public ImagePlus reconstructionDicLid(ImagePlus srcImage, double decay, double angle) {
		logger.debug("Input image: "+ String.valueOf(srcImage.getWidth()) + " " + 
				String.valueOf(srcImage.getHeight()) + " " + 
				String.valueOf(srcImage.getImageStackSize()) + " " +  
				String.valueOf(srcImage.getBitDepth()));
		
		int cumsumup, cumsumdown;
		int c,u,d,r; // loop indexes
		float I;
		int linindex = 0;
		// make copy of original image to not modify it
		ExtraImageProcessor srcImageCopyProcessor = new ExtraImageProcessor(srcImage.duplicate().getProcessor());
		// Rotating image
		srcImageCopyProcessor.rotate(angle,true);
		
		// get new sizes for optimisation purposes
		int newWidth = srcImageCopyProcessor.getIP().getWidth();
		int newHeight = srcImageCopyProcessor.getIP().getHeight();
		ImageProcessor srcImageProcessorUnwrapped = srcImageCopyProcessor.getIP();
		// -------------------- end of optimisation
		
		// create array for storing results - 32bit float as imageprocessor		
		FloatProcessor outputArrayProcessor = new FloatProcessor(newWidth, newHeight);
		float[] outputPixelArray = (float[]) outputArrayProcessor.getPixels();
		
		// do for every row
		for(r=0; r<newHeight; r++) {
			// for every point apply KAM formula
			for(c=0; c<newWidth; c++) { // TODO variable for getHeight
				// up
				cumsumup = 0;
				for(u=c; u>=0; u--)
					cumsumup += (srcImageProcessorUnwrapped.getPixel(u, r))*Math.exp(-decay*Math.abs(u-c));		// TODO change for get as faster version
				// down
				cumsumdown = 0; // cumulative sum from point r to the end of column
				for(d=c; d<srcImageProcessorUnwrapped.getWidth(); d++)
					cumsumdown += (srcImageProcessorUnwrapped.getPixel(d,r))*Math.exp(-decay*Math.abs(d-c));
				// integral
				I = (float)cumsumup - (float)cumsumdown;
				outputPixelArray[linindex++] = I; // linear indexing is in row-order
			}
		}
		// rotate back output processor
		outputArrayProcessor.rotate(-angle);
		// replace outputImage processor with result array with scaling conversion
		ImagePlus outputImage = new ImagePlus("", outputArrayProcessor.convertToByte(true));

		return outputImage; // return reconstruction
	}
}
