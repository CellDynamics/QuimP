package uk.warwick.tools.images;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.warwick.tools.general.RectangleBox;

public class ExtraImageProcessor {
		
	private ImageProcessor ip;
	
	public ExtraImageProcessor(ImageProcessor ip) {
		setImageProcessor(ip);
	}
	
	public ExtraImageProcessor() {
		this.ip = null;
	}
	
	public void setImageProcessor(ImageProcessor ip) {
		this.ip = ip;
	}
	
	public ImageProcessor getImageProcessor() {
		return ip;
	}
	/**
	 * Add borders around image to prevent cropping during scaling.
	 * 
	 * @param srcImage
	 * @param angle
	 * @retval ImageProcessor
	 * @return
	 * @throws Exception 
	 */
	protected void extendImageToRotation(double angle) throws Exception {
		ImageProcessor ret;
		int width = ip.getWidth(); 
		int height = ip.getHeight(); 
		// get bounding box after rotation
		RectangleBox rb = new RectangleBox(width,height);
		rb.rotateBoundingBox(angle);
		int newWidth = (int)Math.round(rb.getWidth());
		int newHeight = (int)Math.round(rb.getHeight());
		// create new array resized
		switch(ip.getBitDepth()) {
		case 8:
			ret = new ByteProcessor(newWidth, newHeight);
			break;
		case 32:
			ret = new FloatProcessor(newWidth, newHeight);
			break;		
		default:
			throw new Exception("Bitdepth not supported");		
		}
		ret.insert(ip,
				(int)Math.round( (newWidth - ip.getWidth())/2 ),
				(int)Math.round( (newHeight - ip.getHeight())/2 )
				);
		this.ip = ret;
	}
	
	/**
	 * Rotate image by specified angle keeping correct rotation direction
	 * It is assumed that user counts angle in anti-clockwise direction and the shear angle 
	 * must be specified in this way as well.  
	 * @param ip ImageProcessor of image to be rotated
	 * @param angle Shear angle counted from x axis in anti-clockwise direction
	 * @throws Exception 
	 * @warning This method modifies input image
	 * @remarks The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally 
	 */
	public ImageProcessor rotate(double angle, boolean addBorders) throws Exception {
		if(addBorders)
			extendImageToRotation(angle);
		ip.rotate(angle);		
		return ip;
	}

}
