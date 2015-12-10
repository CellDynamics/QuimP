package uk.warwick.tools.images;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.warwick.tools.general.RectangleBox;

/**
 * Wrapper class implementing extra functionalities for ij.ImageProcessor
 * This class covers ImageProcessor object and may change it or release
 * @warning
 * Currently this class holds reference to passed ImageProcessor object
 * @author p.baniukiewicz
 * @date 9 Dec 2015
 *
 */
public class ExtraImageProcessor {
		
	private ImageProcessor ip;
	
	/**
	 * Main constructor. Connect ImageProcessor reference to object
	 * @param ip ImageProcessor object
	 */
	public ExtraImageProcessor(ImageProcessor ip) {
		setIP(ip);
	}
	
	/**
	 * Default constructor. Creates empty image of sie 100, 100
	 * @remarks
	 * Should not be used except tests
	 */
	public ExtraImageProcessor() {
		this.ip = new ByteProcessor(100,100);
	}
	
	/**
	 * Connects ImageProcessor to object, releasing old one.
	 * @param ip
	 */
	public void setIP(ImageProcessor ip) {
		this.ip = ip;
	}
	
	/**
	 * Returns covered ImageProcessor object.
	 * @return ImageProcessor object
	 */
	public ImageProcessor getIP() {
		return ip;
	}
	
	/**
	 * Add borders around image to prevent cropping during scaling.
	 * @warning Replaces original image and may not preserve all its attributes 
	 * @param angle Angle to be image rotated
	 */
	protected void extendImageBeforeRotation(double angle) {
		ImageProcessor ret;
		int width = ip.getWidth(); 
		int height = ip.getHeight(); 
		// get bounding box after rotation
		RectangleBox rb = new RectangleBox(width,height);
		rb.rotateBoundingBox(angle);
		int newWidth = (int)Math.round(rb.getWidth());
		int newHeight = (int)Math.round(rb.getHeight());
		// create new array resized
		ret = ip.createProcessor(newWidth, newHeight);
		// get current background - borders will have the same value
		ret.setValue(ip.getBackgroundValue()); // set current fill value for extended image
		ret.setBackgroundValue(ip.getBackgroundValue()); // set the same background as in original image
		ret.fill(); // change color of extended image
		ret.insert(ip,
				(int)Math.round( (newWidth - ip.getWidth())/2 ),
				(int)Math.round( (newHeight - ip.getHeight())/2 )
				); // insert original image into extended
		this.ip = ret; // assign extended into current
	}
	
	/**
	 * Rotate image by specified angle keeping correct rotation direction
	 * @param angle Angle of rotation in anti-clockwise direction
	 * @param addBorders if \a true rotates with extension, \a false use standard rotation with clipping
	 */
	public ImageProcessor rotate(double angle, boolean addBorders) {
		if(addBorders)
			extendImageBeforeRotation(angle);
		ip.rotate(angle);		
		return ip;
	}
	
	/**
	 * Crop image
	 * 
	 * @param luX Left upper corner \a x coordinate
	 * @param luY Left upper corner \a y coordinate
	 * @param width Width of clipped area
	 * @param height Height of clipped area
	 * @remarks Modifies current object
	 * @retval ImageProcessor
	 * @return Clipped image
	 */
	public ImageProcessor crop(int luX, int luY, int width, int height) {
		ip.setRoi(luX, luY, width, height);
		ip = ip.crop();
		ip.resetRoi();
		return ip;
	}
	
	/**
	 * Crop image
	 * Designed to use with cooperation with extendImageBeforeRotation(double). Assumes that cropping area is centered 
	 * in source image
	 * @param width Width of clipped area
	 * @param height Height of clipped area
	 * @remarks Modifies current object
	 * @retval ImageProcessor
	 * @return Clipped image
	 */
	public ImageProcessor cropImageAfterRotation(int width, int height) {
		ip.setRoi((int)Math.round( (ip.getWidth() - width)/2 ),
				(int)Math.round( (ip.getHeight() - height)/2 ),
				width,
				height);
		ip = ip.crop();
		ip.resetRoi();
		return ip;
	}

}
