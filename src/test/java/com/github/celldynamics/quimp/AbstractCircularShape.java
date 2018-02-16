package com.github.celldynamics.quimp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

// TODO: Auto-generated Javadoc
/**
 * Hold predefined circular shape and precomputed geometric parameters.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractCircularShape {

  /**
   * Centroid.
   */
  public static final ExtendedVector2d CENTROID = new ExtendedVector2d(1, 2);
  /**
   * Number of verts.
   */
  public static final int NUMVERT = 36;
  /**
   * Euclidian distance between two nodes.
   */
  public static final double DISTANCE = 1.743114854953163;
  /**
   * Local curvature for three nodes.
   */
  public static final double CURVATURELOCAL = -0.055555555555;
  /**
   * Circle bounds.
   */
  public static final Rectangle BOUNDS = new Rectangle(-9, -8, 20, 20);

  /**
   * Circle from 36 points.
   * 
   * @return List of points that forms circle.
   */
  public static final List<Point2d> getCircle() {
    double a = 0;
    double d = 10;
    int steps = NUMVERT;
    double r = 10;
    double dx = CENTROID.getX();
    double dy = CENTROID.getY();
    ArrayList<Point2d> ret = new ArrayList<>();
    for (int i = 0; i < steps; i++) {
      double x = r * Math.sin(a * Math.PI / 180);
      double y = r * Math.cos(a * Math.PI / 180);
      ret.add(new Point2d(x + dx, y + dy));
      a += d;
    }
    return ret;
  }

  /**
   * Get shape as linked and closed node list.
   * 
   * @param reversed true if reversed
   * 
   * @return head node
   * @throws Exception on error
   */
  public static final Node getNodeList(boolean reversed) throws Exception {
    Snake s = new Snake();
    List<Point2d> p;
    if (reversed) {
      p = getCircleReversed();
    } else {
      p = getCircle();
    }
    int nextTrackNumber = 0;
    s.head = new Node(0); // make a dummy head node for list initialization
    s.head.setPrev(s.head); // link head to itself
    s.head.setNext(s.head);
    s.head.setHead(true);

    Node node;
    for (int i = 0; i < p.size(); i++) {
      node = new Node(p.get(i).getX(), p.get(i).getY(), nextTrackNumber++);
      s.addPoint(node);
    }
    s.removeNode(s.head); // remove dummy head node
    s.setHeadClosestTo(new ExtendedVector2d(p.get(0)));
    return s.getHead();
  }

  /**
   * Get shape as linked and closed node list.
   * 
   * @param reversed true if reversed
   * 
   * @return head node
   * @throws Exception on error
   */
  public static final Vert getVertList(boolean reversed) throws Exception {
    Outline s = new Outline();
    List<Point2d> p;
    if (reversed) {
      p = getCircleReversed();
    } else {
      p = getCircle();
    }
    int nextTrackNumber = 0;
    s.head = new Vert(0); // make a dummy head node for list initialization
    s.head.setPrev(s.head); // link head to itself
    s.head.setNext(s.head);
    s.head.setHead(true);

    Vert node;
    for (int i = 0; i < p.size(); i++) {
      node = new Vert(p.get(i).getX(), p.get(i).getY(), nextTrackNumber++);
      s.addPoint(node);
    }
    s.removeVert(s.head); // remove dummy head node
    s.setHeadClosestTo(new ExtendedVector2d(p.get(0)));
    return s.getHead();
  }

  /**
   * Return normals for {@link #getCircle()}.
   * 
   * @param reversed true if list reversed
   * @param inner true for inner
   * @return List of normals.
   */
  public static final List<Point2d> getNormals(boolean reversed, boolean inner) {
    List<Point2d> ret = new ArrayList<>();
    List<Point2d> p = null;
    if (reversed) {
      p = getCircleReversed();
    } else {
      p = getCircle();
    }
    int prev;
    int next;
    for (int i = 0; i < p.size(); i++) {
      if (i == 0) {
        prev = p.size() - 1;
      } else {
        prev = i - 1;
      }
      if (i == p.size() - 1) {
        next = 0;
      } else {
        next = i + 1;
      }
      ExtendedVector2d point = new ExtendedVector2d(p.get(i));
      ExtendedVector2d unitVecLeft =
              ExtendedVector2d.unitVector(point, new ExtendedVector2d(p.get(prev)));
      ExtendedVector2d pointLeft = new ExtendedVector2d();
      pointLeft.setX(p.get(i).getX());
      pointLeft.setY(p.get(i).getY());
      pointLeft.addVec(unitVecLeft);

      ExtendedVector2d unitVecRight =
              ExtendedVector2d.unitVector(point, new ExtendedVector2d(p.get(next)));
      ExtendedVector2d pointRight = new ExtendedVector2d();
      pointRight.setX(p.get(i).getX());
      pointRight.setY(p.get(i).getY());
      pointRight.addVec(unitVecRight);

      ExtendedVector2d tan = ExtendedVector2d.unitVector(pointLeft, pointRight);
      if (inner) {
        ret.add(new Point2d(tan.getY(), -tan.getX()));
      } else {
        ret.add(new Point2d(-tan.getY(), tan.getX()));
      }
    }

    return ret;
  }

  /**
   * Circle from 36 points, reversed direction.
   * 
   * @return List of points that forms circle.
   */
  public static final List<Point2d> getCircleReversed() {
    double a = 360;
    double d = -10;
    int steps = NUMVERT;
    double r = 10;
    double dx = CENTROID.getX();
    double dy = CENTROID.getY();
    ArrayList<Point2d> ret = new ArrayList<>();
    for (int i = 0; i < steps; i++) {
      double x = r * Math.sin(a * Math.PI / 180);
      double y = r * Math.cos(a * Math.PI / 180);
      ret.add(new Point2d(x + dx, y + dy));
      a += d;
    }
    return ret;
  }

  /**
   * Compute linear indices of nodes.
   * 
   * @param reversed true if reversed
   * 
   * @return linear indices
   */
  public static final List<Double> getLinearIndices(boolean reversed) {
    ArrayList<Double> ret = new ArrayList<>();
    double len = DISTANCE * NUMVERT;
    double v = 0;
    for (int i = 0; i < NUMVERT; i++) {
      ret.add(v / len);
      v += DISTANCE;
    }
    if (reversed) {
      Collections.reverse(ret);
      Collections.rotate(ret, 1);
    }
    return ret;
  }

  /**
   * Return X coordinates of shape.
   * 
   * @return list of coordinates
   */
  public static final double[] getX() {
    List<Point2d> p = getCircle();
    QuimpDataConverter dc = new QuimpDataConverter(p);
    return dc.getX();
  }

  /**
   * Return X coordinates of shape.
   * 
   * @return list of coordinates
   */
  public static final double[] getY() {
    List<Point2d> p = getCircle();
    QuimpDataConverter dc = new QuimpDataConverter(p);
    return dc.getY();
  }

  /**
   * Return X coordinates of shape.
   * 
   * @return list of coordinates
   */
  public static final float[] getXfloat() {
    List<Point2d> p = getCircle();
    QuimpDataConverter dc = new QuimpDataConverter(p);
    return dc.getFloatX();
  }

  /**
   * Return X coordinates of shape.
   * 
   * @return list of coordinates
   */
  public static final float[] getYfloat() {
    List<Point2d> p = getCircle();
    QuimpDataConverter dc = new QuimpDataConverter(p);
    return dc.getFloatY();
  }

  /**
   * Validate smoothed curvature.
   * 
   * <p>For circular object it should be the three times larger than for one node (for
   * {@value #DISTANCE} rounded up).
   * 
   * @param o Outline to validate.
   * @throws Exception on error
   */
  public static void validateCurvatureSum(Outline o) throws Exception {
    for (Vert v : o) { // all curvatures the same as it is circle
      // sum of curvatureSmoothed over 3 vertexes
      assertThat(v.curvatureSum, is(closeTo(CURVATURELOCAL * 3, 1e-5)));
      assertThat(v.curvatureSum, is(closeTo(v.curvatureLocal * 3, 1e-5)));
    }
  }

  /**
   * Validate curvature.
   * 
   * <p>For circular object it should be the the same for each node.
   * 
   * @param o Outline to validate.
   * @throws Exception on error
   */
  public static void validateCurvature(Outline o) throws Exception {
    for (Vert v : o) {
      assertThat(v.curvatureLocal, is(closeTo(CURVATURELOCAL, 1e-5)));
    }
  }

  /**
   * Validate summed curvature. For circular object it should be the same as for one node.
   * 
   * @param o Outline to validate.
   * @throws Exception on error
   */
  public static void validateCurvatureSmooth(Outline o) throws Exception {
    for (Vert v : o) { // all curvatures the same as it is circle
      // for vertex v curvature Smoothed is average from v-1 v+1 and v of curvatureLocal. This is
      // circle so all curvatures are the same so average is also the same as local curvature
      assertThat(v.curvatureSmoothed, is(closeTo(CURVATURELOCAL, 1e-5)));
      assertThat(v.curvatureSmoothed, is(closeTo(v.curvatureLocal, 1e-5)));
    }
  }

  /**
   * Validate if Shape has correct number of points.
   * 
   * @param s shape to validate.
   * @throws Exception on error
   */
  public static void validateNumOfPoints(Shape<?> s) throws Exception {
    assertThat(s.getNumPoints(), is(NUMVERT)); // not changed number of verts
  }

  /**
   * Validate linear indices.
   * 
   * @param sh shape to validate
   * @param reversed true if reversed
   * @throws Exception on error
   */
  public static void validateLinearIndices(Shape<?> sh, boolean reversed) throws Exception {
    List<Double> li = getLinearIndices(reversed);
    Iterator<Double> lit = li.iterator();
    Iterator<?> sit = sh.iterator();
    assertThat(sh.getNumPoints(), is(li.size()));
    while (sit.hasNext() && lit.hasNext()) {
      PointsList<?> p = (PointsList<?>) sit.next();
      double exp = lit.next();
      assertThat(p.getPosition(), is(closeTo(exp, 1e-5)));
    }
  }

  /**
   * Validate object bounds.
   * 
   * @param s snake to validate.
   * @throws Exception on error
   */
  public static void validateBounds(Snake s) throws Exception {
    assertThat(s.getBounds(), is(BOUNDS));
  }

  /**
   * Validate centroid.
   * 
   * @param sh Shape to validate
   * @throws Exception on error
   */
  public static void validateCentroid(Shape<?> sh) throws Exception {
    assertThat(sh.centroid.getX(), is(closeTo(CENTROID.getX(), 1e-5)));
    assertThat(sh.centroid.getY(), is(closeTo(CENTROID.getY(), 1e-5)));
  }

  /**
   * Validate coordinates of points.
   * 
   * @param sh shape to validate
   * @param reversed true for reversed direction
   * @throws Exception on error
   */
  public static void validatePoints(Shape<?> sh, boolean reversed) throws Exception {
    List<Point2d> exp = null;
    if (reversed) {
      exp = getCircleReversed();
    } else {
      exp = getCircle();
    }
    Iterator<Point2d> itexp = exp.iterator();
    Iterator<?> it = sh.iterator();
    assertThat(sh.getNumPoints(), is(exp.size()));
    while (it.hasNext() && itexp.hasNext()) {
      PointsList<?> p = (PointsList<?>) it.next();
      ExtendedVector2d point = p.getPoint();
      Point2d pointexp = itexp.next();
      assertThat(point.getX(), is(closeTo(pointexp.getX(), 1e-5)));
      assertThat(point.getY(), is(closeTo(pointexp.getY(), 1e-5)));
    }
  }

  /**
   * Validate coordinates of normals.
   * 
   * @param sh shape to validate
   * @param reversed true if shape reversed
   * @param inner true for inner
   * @throws Exception on error
   */
  public static void validateNormals(Shape<?> sh, boolean reversed, boolean inner)
          throws Exception {
    List<Point2d> exp = null;
    exp = getNormals(reversed, inner);
    Iterator<Point2d> itexp = exp.iterator();
    Iterator<?> it = sh.iterator();
    assertThat(sh.getNumPoints(), is(exp.size()));
    while (it.hasNext() && itexp.hasNext()) {
      PointsList<?> p = (PointsList<?>) it.next();
      ExtendedVector2d point = p.getNormal();
      Point2d pointexp = itexp.next();
      assertThat(point.getX(), is(closeTo(pointexp.getX(), 1e-5)));
      assertThat(point.getY(), is(closeTo(pointexp.getY(), 1e-5)));
    }
  }

  /**
   * Collection of geometric properties for Shapes.
   * 
   * <p>Remember about random selection of head node.
   * 
   * @param sh shape to validate
   * @param reversed true for reversed direction
   * @param inner true for inner normals
   * @throws Exception on error
   */
  public static void validateShapeGeomProperties(Shape<?> sh, boolean reversed, boolean inner)
          throws Exception {
    validateNumOfPoints(sh);
    validateCentroid(sh);
    validatePoints(sh, reversed);
    validateNormals(sh, reversed, inner);
    validateLinearIndices(sh, reversed);
  }

  /**
   * Collection of geometric properties for Snakes.
   * 
   * <p>Remember about random selection of head node.
   * 
   * @param s snake to validate.
   * @throws Exception on error
   */
  public static void validateSnakeGeomProperties(Snake s) throws Exception {
    validateShapeGeomProperties(s, true, BOA_.qState.segParam.expandSnake);
    validateBounds(s);
    validateFrozen(s);
  }

  /**
   * Collection of geometric properties for Outlines.
   * 
   * <p>Remember about random selection of head node.
   * 
   * @param o outline to validate.
   * @throws Exception on error
   */
  public static void validateOutlineGeomProperties(Outline o) throws Exception {
    validateShapeGeomProperties(o, false, false);
    validateCurvature(o);
  }

  /**
   * Check if Snake is unfrozen.
   * 
   * @param s snake to test
   */
  public static void validateFrozen(Snake s) {
    assertThat(s.isFrozen(), is(false));
  }
}
