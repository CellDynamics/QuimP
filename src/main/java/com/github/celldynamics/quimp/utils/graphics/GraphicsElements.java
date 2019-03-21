package com.github.celldynamics.quimp.utils.graphics;

import java.awt.Color;
import java.awt.Polygon;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;

import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

/**
 * This class contains static methods for creating simple graphics elements using IJ Roi API.
 * 
 * @author p.baniukiewicz
 *
 */
public class GraphicsElements {

  /**
   * Create an arrow.
   * 
   * <p><b>Warning</b><br>
   * input parameters can be modified
   * 
   * @param direction Directional vector
   * @param base base point
   * @param length Length of arrow
   * @param baselength length of base as percentage of \a length
   * @return FloatPolygon
   */
  public static FloatPolygon plotArrow(Vector2d direction, Point2d base, float length,
          float baselength) {
    direction.normalize();
    direction.scale(length);

    Vector2d v2 = new Vector2d(-direction.getY(), direction.getX()); // perpend. to direction
    Vector2d v3 = new Vector2d(-v2.getX(), -v2.getY()); // parallel to v3
    v2.scale(baselength);
    v3.scale(baselength);

    FloatPolygon fp = new FloatPolygon();

    fp.addPoint(base.getX() + direction.getX(), base.getY() + direction.getY());
    fp.addPoint(base.getX() + v2.getX(), base.getY() + v2.getY());
    fp.addPoint(base.getX() + v3.getX(), base.getY() + v3.getY());

    return fp;
  }

  /**
   * Return circle.
   * 
   * @param base center
   * @param radius radius
   * @return FloatPolygon
   */
  public static FloatPolygon getCircle(Point2d base, float radius) {
    OvalRoi or =
            new OvalRoi(base.getX() - radius / 4, base.getY() - radius / 4, radius / 2, radius / 2);
    return or.getFloatPolygon();

  }

  /**
   * Generate filled circle.
   * 
   * @param x x coordinate of center
   * @param y y coordinate of center
   * @param color color
   * @param radius radius
   * @return PolygonRoi
   */
  public static PolygonRoi getCircle(double x, double y, Color color, double radius) {
    // create ROI
    PolygonRoi or = new PolygonRoi(GraphicsElements.getCircle(new Point2d(x, y), (float) radius),
            Roi.POLYGON);
    or.setStrokeColor(color);
    or.setFillColor(color);
    return or;
  }

  /**
   * Generate point roi.
   * 
   * @param x coordinate
   * @param y coordinate
   * @param color color
   * @return PointRoi
   */
  public static PointRoi getPoint(double x, double y, Color color) {
    PointRoi pr = new PointRoi(x, y);
    pr.setStrokeColor(color);
    pr.setFillColor(color);
    return pr;
  }

  /**
   * Generate points roi.
   * 
   * @param x coordinate
   * @param y coordinate
   * @param color color
   * @return PointRoi
   */
  public static PointRoi getPoint(int[] x, int[] y, Color color) {
    if (x.length != y.length) {
      throw new IllegalArgumentException("Arras of different sizes");
    }
    PointRoi pr = new PointRoi(x, y, x.length);
    pr.setStrokeColor(color);
    pr.setFillColor(color);
    return pr;
  }

  /**
   * Generate points roi.
   * 
   * @param poly polygon
   * @param color color
   * @return PointRoi
   */
  public static PointRoi getPoint(Polygon poly, Color color) {
    PointRoi pr = new PointRoi(poly);
    pr.setStrokeColor(color);
    pr.setFillColor(color);
    return pr;
  }

  /**
   * Generate line.
   * 
   * @param points Coordinates of line defined in Polygon.
   * @param npoints Number of points in <tt>points</tt> to use.
   * @param color Color of the line
   * @return PolygonRoi
   */
  public static PolygonRoi getLine(Polygon points, int npoints, Color color) {
    PolygonRoi pr = new PolygonRoi(points, Roi.FREELINE);
    pr.setStrokeColor(color);
    pr.setFillColor(color);
    return pr;
  }

  /**
   * Generate line.
   * 
   * @param points Coordinates of line defined in Polygon.
   * @param color Color of the line
   * @return PolygonRoi
   */
  public static PolygonRoi getLine(Polygon points, Color color) {
    return GraphicsElements.getLine(points, points.npoints, color);
  }

  /**
   * Generate line on frame f.
   * 
   * @param points Coordinates of line defined in Polygon.
   * @param color Color of the line
   * @param frame Frame to set ROI at.
   * @return PolygonRoi
   */
  public static PolygonRoi getLine(Polygon points, Color color, int frame) {
    PolygonRoi pr = GraphicsElements.getLine(points, points.npoints, color);
    pr.setPosition(frame);
    return pr;
  }

  /**
   * Generate line.
   * 
   * @param x Array with x coordinates
   * @param y Array wit y coordinates.
   * @param npoints Number of points to consider
   * @param color color
   * @return PolygonRoi
   */
  public static PolygonRoi getLine(float[] x, float[] y, int npoints, Color color) {
    if (x.length != y.length) {
      throw new IllegalArgumentException("Arras of different sizes");
    }
    PolygonRoi proi = new PolygonRoi(x, y, npoints, Roi.FREELINE);
    proi.setStrokeColor(color);
    proi.setFillColor(color);
    return proi;
  }

}
