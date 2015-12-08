/**
 * 
 */
package uk.warwick.dic.lid;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

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
	 * @remarks
	 * Returned lengths of bounding box are rounded to up
	 * @param width
	 * @param height
	 * @param angle
	 * @return
	 */
	protected BoundingBox getBoundingBox(int width, int height, double angle) {
		
		// assume that image is centered at (0,0)
		// convert to rad
		double angleRad = angle*Math.PI/180.0;
		
		// rotation matrix
		Matrix3d rot = new Matrix3d();
		// rotation with - because shear is defined in anti-clockwise and rotZ require counterclockwise (the same)
		rot.rotZ(-angleRad); // generate rotation matrix of angle - bring input image to horizontal position
		logger.debug("Rotation matrix\n"+rot.toString());
		
		// define corner points of image
		Point3d[] cornerTable = new Point3d[4];
		cornerTable[0] = new Point3d(-width/2, height/2, 0);  // left up
		cornerTable[1] = new Point3d(width/2, height/2, 0);  // right up
		cornerTable[2] = new Point3d(width/2, -height/2, 0);  // right down
		cornerTable[3] = new Point3d(-width/2, -height/2, 0);  // right up
		for(Point3d p: cornerTable)
			logger.debug("Corner: " + p.toString());
		
		// rotate virtual image by angle
		for(Point3d p: cornerTable) {
			rot.transform(p); // multiply ROT*P and return result to P
			logger.debug("Rotated corner: " + p.toString());
		}
		
		// get ranges of x and y
		// collect all x coords
		Vector<Double> x = new Vector<Double>();
		for(Point3d p: cornerTable)
			x.add(p.x);
		// collect all y
		Vector<Double> y = new Vector<Double>();
		for(Point3d p: cornerTable)
			y.add(p.y);
		
		return new BoundingBox(x, y);
	}
	
	/**
	 * Add borders around image to prevent cropping during scaling.
	 * 
	 * @param srcImage
	 * @param newWidth
	 * @param newHeight
	 * @retval 
	 * @return
	 */
	protected ImageProcessor extendImage(ImageProcessor srcImage, int newWidth, int newHeight) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		double diagonal = Math.ceil(Math.sqrt(height*height + width*width));
		// TODO not finished
		return srcImage;
	}
	
	/**
	 * Rotate image by specified angle keeping correct rotation direction
	 * It is assumed that user counts angle in anti-clockwise direction and the shear angle 
	 * must be specified in this way as well.  
	 * @param ip ImageProcessor of image to be rotated
	 * @param angle Shear angle counted from x axis in anti-clockwise direction
	 * @warning This method modifies input image
	 * @remarks The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally 
	 */
	protected void rotateImage(ImageProcessor ip, double angle) {
		ip.setInterpolationMethod(ImageProcessor.BICUBIC);
		// rotate rotates in clockwise direction thus shear angle should not be negated if it has been counted in proper mathematical way
		ip.rotate(angle);
	}
	/**
	 * Reconstruct DIC image by LID method using LID method
	 * Make copy of original image to not change it.
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
		ImagePlus srcImagelCopy = srcImage.duplicate(); // make a copy of original
		ByteProcessor srcImagelCopyProcessor = (ByteProcessor) srcImagelCopy.getProcessor(); // get ByteProcessor to access pixels
		// create output image of size of input
		ImagePlus outputImage = IJ.createImage("DIC", 
				srcImage.getWidth(), 
				srcImage.getHeight(), 
				srcImage.getImageStackSize(), 
				srcImage.getBitDepth());
		// create array for storing results - 32bit float as imageprocessor
		FloatProcessor outputArrayProcessor = new FloatProcessor(srcImagelCopyProcessor.getWidth(), srcImagelCopyProcessor.getHeight());
		float[] outputPixelArray = (float[]) outputArrayProcessor.getPixels();
		
		// Rotating image
		rotateImage(srcImagelCopyProcessor, angle);

		// do for every row
		for(r=0; r<srcImagelCopyProcessor.getHeight(); r++) {
			// for every point apply KAM formula
			for(c=0; c<srcImagelCopyProcessor.getWidth(); c++) { // TODO variable for getHeight
				// up
				cumsumup = 0;
				for(u=c; u>=0; u--)
					cumsumup += (srcImagelCopyProcessor.getPixel(u, r) & 0xff)*Math.exp(-decay*Math.abs(u-c));		// TODO change for get as faster version
				// down
				cumsumdown = 0; // cumulative sum from point r to the end of column
				for(d=c; d<srcImagelCopyProcessor.getWidth(); d++)
					cumsumdown += (srcImagelCopyProcessor.getPixel(d,r) & 0xff)*Math.exp(-decay*Math.abs(d-c));
				// integral
				I = (float)cumsumup - (float)cumsumdown;
				outputPixelArray[linindex++] = I; // linear indexing is in row-order
			}
		}
		// rotate back output processor
		rotateImage(outputArrayProcessor, -angle);
		// replace outputImage processor with result array with scaling conversion
		outputImage.setProcessor(outputArrayProcessor.convertToByte(true));

		return outputImage; // return reconstruction
	}
	
	/**
	 * @remarks
	 * Returned lengths of bounding box are rounded to up
	 * @author baniuk
	 *
	 */
	class BoundingBox {
		private Vector<Double> x;
		private Vector<Double> y;
		public BoundingBox(Vector<Double> x, Vector<Double> y) {
			this.x = x;
			this.y = y;
		}
		public int getWidthInt() {return (int)(Math.round(Math.abs(Collections.max(x) - Collections.min(x))));}
		public int getHeightInt() {return (int)(Math.round(Math.abs(Collections.max(y) - Collections.min(y))));}
		public double getWidth() {return Collections.max(x) - Collections.min(x);}
		public double getHeight() {return Collections.max(y) - Collections.min(y);}
	}


}
