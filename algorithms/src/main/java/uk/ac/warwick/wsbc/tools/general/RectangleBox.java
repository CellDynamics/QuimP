package uk.ac.warwick.wsbc.tools.general;

import java.util.Collections;
import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents rectangle bounding box
 * 
 * Bounding box is defined by four corners (in contrary to javafx.geometry.BoundingBox) that can be rotated
 * by any angle.
 * 
 * @author p.baniukiewicz
 * @date 09 Dec 2015
 * @todo integrate with DIC and ExtraImageProcessor
 */
public class RectangleBox {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(RectangleBox.class.getName());
	
	private Vector<Double> x; // stores x coordinates of bounding box in clockwise order
	private Vector<Double> y; // stores y coordinates of bounding box in clockwise order
	
	/**
	 * Creates bounding box object from \a x \a y vectors.
	 * 
	 * Vectors define corners in clockwise direction.
	 * 
	 * @warning Vectors are referenced only, not copied. They are
	 * modified during rotation
	 * @param x \a x coordinates of bounding box in clockwise order
	 * @param y \a y coordinates of bounding box in clockwise order
	 * @throws IllegalArgumentException When empty vectors are passed to constructor or input vectors have different length
	 */
	@SuppressWarnings("unchecked")
	public RectangleBox(Vector<Double> x, Vector<Double> y) throws Exception {
		this.x = (Vector<Double>) x.clone();
		this.y = (Vector<Double>) y.clone();
		// get average of x and y
		if(x.isEmpty() || y.isEmpty())
			throw new IllegalArgumentException("Input vectors are empty");
		if(x.size() != y.size())
			throw new IllegalArgumentException("Input vectors are not equal");
		double centerX = getAverage(x); // centre of mass
		double centerY = getAverage(y); // centre of mass
		// move input points to (0,0)
		for(int i=0;i<x.size();i++) {
			this.x.set(i, x.get(i)-centerX);
			this.y.set(i, y.get(i)-centerY);
		}
	}

	/**
	 * Specifies bounding box centred at (0,0)
	 * 
	 * @param width Width of bounding box
	 * @param height Height of bounding box
	 */
	public RectangleBox(double width, double height) {
		x = new Vector<Double>();
		y = new Vector<Double>();
		
		// generate artificial rectangle centered at (0,0)
		x.add(-width/2); // left top
		x.add(width/2); //right top
		x.add(width/2); //right down
		x.add(-width/2); // left down
		
		y.add(height/2); //left top
		y.add(height/2); // right top
		y.add(-height/2); //right down
		y.add(-height/2); // left down
	}
	
	/**
	 * Rotates bounding box.
	 * 
	 * @param angle Rotation angle
	 */
	public void rotateBoundingBox(double angle) {
		
		// assume that image is centered at (0,0)
		// convert to rad
		double angleRad = angle*Math.PI/180.0;
		
		// rotation matrix
		Matrix3d rot = new Matrix3d();
		// rotation with - because shear is defined in anti-clockwise and rotZ require counterclockwise (the same)
		rot.rotZ(-angleRad); // generate rotation matrix of angle - bring input image to horizontal position
		// logger.debug("Rotation matrix\n"+rot.toString());
		
		// define corner points of image
		Point3d[] cornerTable = new Point3d[4];
		cornerTable[0] = new Point3d(x.get(0), y.get(0), 0);  // left up
		cornerTable[1] = new Point3d(x.get(1), y.get(1), 0);  // right up
		cornerTable[2] = new Point3d(x.get(2), y.get(2), 0);  // right down
		cornerTable[3] = new Point3d(x.get(3), y.get(3), 0);  // right up
		//for(Point3d p: cornerTable)
		//	logger.debug("Corner: " + p.toString());
		
		int i=0;
		// rotate virtual image by angle
		for(Point3d p: cornerTable) {
			rot.transform(p); // multiply ROT*P and return result to P
			x.set(i, p.x);
			y.set(i, p.y);
			i++;
			// logger.debug("Rotated corner: " + p.toString());
		}
	}
	
	/**
	 * Gets width of bounding box as distance over \b x between outermost corners 
	 * 
	 * @return Width of bounding box
	 */
	public double getWidth() {return Math.abs(Collections.max(x) - Collections.min(x));}
	
	/**
	 * Gets height of bounding box as distance over \b y between outermost corners
	 * 
	 * @return Height of bounding box
	 */
	public double getHeight() {return Math.abs(Collections.max(y) - Collections.min(y));}
	
	/**
	 * Gets mean value of input vector
	 * 
	 * @param x Vector of to calculate mean
	 * @return Mean value of \a x
	 */
	private double getAverage(Vector<Double> x) {
		double sum = 0;
		for(Double val : x)
			sum += val;
		return sum/x.size();		
	}
}
