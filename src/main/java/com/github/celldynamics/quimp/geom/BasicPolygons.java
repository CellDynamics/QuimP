package com.github.celldynamics.quimp.geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Tuple2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates basic geometry on polygons defined as list of point in specified direction.
 * 
 * @author p.baniukiewicz
 * @see <a href= "link">http://www.mathopenref.com/coordpolygonarea.html</a>
 */
public class BasicPolygons {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BasicPolygons.class.getName());

  /**
   * Default constructor.
   */
  public BasicPolygons() {

  }

  /**
   * Calculates area of polygon.
   * 
   * <p>Supports triangles, regular and irregular polygons, convex or concave polygons
   * 
   * <p><b>Warning</b>
   * 
   * <p>Polygon can not intersect itself.
   * 
   * @param p Vertices of polygon in specified order
   * @return Area
   * 
   */
  public double getPolyArea(final List<? extends Tuple2d> p) {
    int i;
    int j;
    double area = 0;

    for (i = 0; i < p.size(); i++) {
      j = (i + 1) % p.size(); // will round pointer to 0 for last point
      Tuple2d pi = p.get(i);
      Tuple2d pj = p.get(j);
      area += pi.getX() * pj.getY();
      area -= pi.getY() * pj.getX();
    }
    area /= 2.0;
    return (Math.abs(area));
  }

  /**
   * Calculates perimeter of polygon.
   * 
   * @param p Vertices of polygon in specified order
   * @return Perimeter
   */
  public double getPolyPerim(final List<? extends Tuple2d> p) {
    int i;
    int j;
    double len = 0;
    ArrayList<Vector2d> tmpV = new ArrayList<>();
    // get vectors between points
    for (i = 0; i < p.size(); i++) {
      j = (i + 1) % p.size(); // will round pointer to 0 for last point
      Tuple2d first = p.get(i);
      Tuple2d second = p.get(j);
      Vector2d tmp = new Vector2d(second.getX() - first.getX(), second.getY() - first.getY());
      tmpV.add(tmp);
    }
    for (Vector2d v : tmpV) {
      len += v.length();
    }
    return len;
  }

  /**
   * Test whether <tt>Ptest</tt> is inside polygon <tt>P</tt>
   * 
   * @param p Vertices of polygon in specified order
   * @param testP Point to be tested
   * @return true if <tt>Ptest</tt> is inside <tt>P</tt>, false otherwise
   * @see <a href=
   *      "link">http://www.shodor.org/~jmorrell/interactivate/org/shodor/util11/PolygonUtils.java</a>
   */
  public boolean isPointInside(final List<? extends Tuple2d> p, final Tuple2d testP) {
    double angle = 0;
    Point2d p1 = null;
    Point2d p2 = null;
    for (int i = 0; i < p.size(); i++) {
      p1 = new Point2d(p.get(i).getX() - testP.getX(), p.get(i).getY() - testP.getY());
      p2 = new Point2d(p.get((i + 1) % p.size()).getX() - testP.getX(),
              p.get((i + 1) % p.size()).getY() - testP.getY());
      angle += angle2D(p1, p2);
    }
    return Math.abs(angle) >= Math.PI;
  }

  /**
   * Helper method.
   * 
   * @param p1 first point
   * @param p2 second point
   * @return angle between points.
   */
  private double angle2D(Point2d p1, Point2d p2) {
    double dtheta = Math.atan2(p2.y, p2.x) - Math.atan2(p1.y, p1.x);
    while (dtheta > Math.PI) {
      dtheta -= 2.0 * Math.PI;
    }
    while (dtheta < -1.0 * Math.PI) {
      dtheta += 2.0 * Math.PI;
    }
    return dtheta;
  }

  /**
   * Test if all points <tt>Ptest</tt> are inside polygon <tt>P</tt>.
   * 
   * @param polygon polygon to test points with
   * @param testP points to test
   * @return true if all points are inside polygon
   * 
   * @see #isPointInside(List, Tuple2d)
   */
  public boolean areAllPointsInside(final List<? extends Tuple2d> polygon,
          final List<? extends Tuple2d> testP) {
    boolean result = true;
    Iterator<? extends Tuple2d> it = testP.iterator();
    while (it.hasNext() && result) {
      result = isPointInside(polygon, it.next());
    }
    return result;
  }

  /**
   * Test if all points from <tt>Ptest</tt> are outside of <tt>P</tt>.
   * 
   * @param polygon polygon to test points with
   * @param testP points to test
   * @return true if all points are outside polygon
   * 
   * @see #isPointInside(List, Tuple2d)
   */
  public boolean areAllPointOutside(final List<? extends Tuple2d> polygon,
          final List<? extends Tuple2d> testP) {
    boolean result = false;
    Iterator<? extends Tuple2d> it = testP.iterator();
    while (it.hasNext() && !result) {
      result = isPointInside(polygon, it.next());
    }
    return !result;
  }

  /**
   * Get center of mass of polygon.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Require correct polygon with non crossing edges.
   * 
   * @param polygon Vertices of polygon in specified order
   * @return Point of center of mass
   * @throws IllegalArgumentException when defective polygon is given (area equals 0)
   * @see <a href=
   *      "link">http://stackoverflow.com/questions/5271583/center-of-gravity-of-a-polygon</a>
   */
  public Point2d polygonCenterOfMass(final List<? extends Tuple2d> polygon) {

    int n = polygon.size();
    Point2d[] polygonTmp = new Point2d[n];

    for (int q = 0; q < n; q++) {
      polygonTmp[q] = new Point2d(polygon.get(q));
    }

    double cx = 0;
    double cy = 0;
    double a = getPolyArea(polygon);
    if (a == 0) {
      throw new IllegalArgumentException("Defective polygon, area is 0");
    }
    int i;
    int j;

    double factor = 0;
    for (i = 0; i < n; i++) {
      j = (i + 1) % n;
      factor = (polygonTmp[i].x * polygonTmp[j].y - polygonTmp[j].x * polygonTmp[i].y);
      cx += (polygonTmp[i].x + polygonTmp[j].x) * factor;
      cy += (polygonTmp[i].y + polygonTmp[j].y) * factor;
    }
    factor = 1.0 / (6.0 * a);
    cx *= factor;
    cy *= factor;
    return new Point2d(Math.abs(cx), Math.abs((cy)));
  }

}
