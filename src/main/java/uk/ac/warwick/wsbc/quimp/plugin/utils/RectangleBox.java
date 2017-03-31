package uk.ac.warwick.wsbc.quimp.plugin.utils;

import java.util.Collections;
import java.util.Vector;

import org.scijava.vecmath.Matrix3d;
import org.scijava.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents rectangle bounding box.
 * 
 * <p>Bounding box is defined by four corners (in contrary to javafx.geometry.BoundingBox) that can
 * be
 * rotated by any angle.
 * 
 * @author p.baniukiewicz
 */
class RectangleBox {

  static final Logger LOGGER = LoggerFactory.getLogger(RectangleBox.class.getName());

  private Vector<Double> px; // stores px coordinates of bounding box in clockwise order
  private Vector<Double> py; // stores py coordinates of bounding box in clockwise order

  /**
   * Creates bounding box object from px py vectors.
   * 
   * <p>Vectors define corners in clockwise direction. Vectors are referenced only, not copied. They
   * are modified during rotation.
   * 
   * @param x coordinates of bounding box in clockwise order
   * @param y coordinates of bounding box in clockwise order
   * @throws IllegalArgumentException When empty vectors are passed to constructor or input
   *         vectors have different length
   */
  @SuppressWarnings("unchecked")
  public RectangleBox(Vector<Double> x, Vector<Double> y) throws IllegalArgumentException {
    this.px = (Vector<Double>) x.clone();
    this.py = (Vector<Double>) y.clone();
    // get average of px and py
    if (x.isEmpty() || y.isEmpty()) {
      throw new IllegalArgumentException("Input vectors are empty");
    }
    if (x.size() != y.size()) {
      throw new IllegalArgumentException("Input vectors are not equal");
    }
    double centerX = getAverage(x); // centre of mass
    double centerY = getAverage(y); // centre of mass
    // move input points to (0,0)
    for (int i = 0; i < x.size(); i++) {
      this.px.set(i, x.get(i) - centerX);
      this.py.set(i, y.get(i) - centerY);
    }
  }

  /**
   * Specifies bounding box centred at (0,0).
   * 
   * @param width Width of bounding box
   * @param height Height of bounding box
   */
  public RectangleBox(double width, double height) {
    px = new Vector<Double>();
    py = new Vector<Double>();

    // generate artificial rectangle centered at (0,0)
    px.add(-width / 2); // left top
    px.add(width / 2); // right top
    px.add(width / 2); // right down
    px.add(-width / 2); // left down

    py.add(height / 2); // left top
    py.add(height / 2); // right top
    py.add(-height / 2); // right down
    py.add(-height / 2); // left down
  }

  /**
   * Rotates bounding box.
   * 
   * @param angle Rotation angle
   */
  public void rotateBoundingBox(double angle) {

    // assume that image is centered at (0,0)
    // convert to rad
    double angleRad = angle * Math.PI / 180.0;

    // rotation matrix
    Matrix3d rot = new Matrix3d();
    // rotation with - because shear is defined in anti-clockwise and rotZ
    // require counterclockwise (the same)
    rot.rotZ(-angleRad); // generate rotation matrix of angle - bring input image to horizontal
    // position

    // define corner points of image
    Point3d[] cornerTable = new Point3d[4];
    cornerTable[0] = new Point3d(px.get(0), py.get(0), 0); // left up
    cornerTable[1] = new Point3d(px.get(1), py.get(1), 0); // right up
    cornerTable[2] = new Point3d(px.get(2), py.get(2), 0); // right down
    cornerTable[3] = new Point3d(px.get(3), py.get(3), 0); // right up

    int i = 0;
    // rotate virtual image by angle
    for (Point3d p : cornerTable) {
      rot.transform(p); // multiply ROT*P and return result to P
      px.set(i, p.x);
      py.set(i, p.y);
      i++;
    }
  }

  /**
   * Gets width of bounding box as distance over \b px between outermost corners.
   * 
   * @return Width of bounding box
   */
  public double getWidth() {
    return Math.abs(Collections.max(px) - Collections.min(px));
  }

  /**
   * Gets height of bounding box as distance over \b py between outermost corners.
   * 
   * @return Height of bounding box
   */
  public double getHeight() {
    return Math.abs(Collections.max(py) - Collections.min(py));
  }

  /**
   * Gets mean value of input vector.
   * 
   * @param x Vector of to calculate mean
   * @return Mean value of x
   */
  private double getAverage(Vector<Double> x) {
    double sum = 0;
    for (Double val : x) {
      sum += val;
    }
    return sum / x.size();
  }
}