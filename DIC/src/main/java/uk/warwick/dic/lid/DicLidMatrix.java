/**
 * 
 */
package uk.warwick.dic.lid;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implementation of Kam algorithm with use of matrix approach
 * @author baniuk
 *
 */
public class DicLidMatrix {
	
	private static final Logger logger = LogManager.getLogger(DicLidMatrix.class.getName());
	/**
	 * Empty default constructor
	 */
	public DicLidMatrix() {

	}
	
	/**
	 * Reconstruct DIC image by LID method using matrix approach
	 * Make copy of original image to not change it.
	 * @param srcImage
	 * @param s0
	 * @return
	 */
	public ImagePlus DicReconstructionLidMatrix(ImagePlus srcImage, double s0) {
		logger.debug("Height of image = " + String.valueOf(srcImage.getHeight()));
		ImagePlus localCopy = srcImage.duplicate(); // make a copy of original
		ByteProcessor tmpProcessor = (ByteProcessor) localCopy.getProcessor(); // get ByteProcessor to access pixels
		
		tmpProcessor.rotate(90);
		
		return localCopy; // return modified image
	}

}
