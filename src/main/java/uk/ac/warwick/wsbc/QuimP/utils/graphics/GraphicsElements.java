package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import java.awt.Color;
import java.awt.Polygon;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

/**
 * This class contains static methods for creating simple graphics elements.
 * 
 * @author p.baniukiewicz
 * @date 5 Apr 2016
 *
 */
public class GraphicsElements {

    /**
     * Create an arrow 
     * 
     * @param direction Directional vector
     * @param base base point 
     * @param length Length of arrow
     * @param baselength length of base as percentage of \a length
     * @return FloatPolygon
     * @warning input parameters can be modified
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

    public static FloatPolygon getCircle(Point2d base, float radius) {
        OvalRoi or = new OvalRoi(base.getX() - radius / 4, base.getY() - radius / 4, radius / 2,
                radius / 2);
        return or.getFloatPolygon();

    }

    /**
     * Generate filled circle.
     * 
     * @param x
     * @param y
     * @param color
     * @param radius
     */
    public static PolygonRoi getCircle(double x, double y, Color color, double radius) {
        // create ROI
        PolygonRoi oR = new PolygonRoi(
                GraphicsElements.getCircle(new Point2d(x, y), (float) radius), Roi.POLYGON);
        oR.setStrokeColor(color);
        oR.setFillColor(color);
        return oR;
    }

    /**
     * Generate point roi.
     * 
     * @param x
     * @param y
     * @param color
     */
    public static PointRoi getPoint(double x, double y, Color color) {
        PointRoi pR = new PointRoi(x, y);
        pR.setStrokeColor(color);
        pR.setFillColor(color);
        return pR;
    }

    /**
     * Generate points roi.
     * 
     * @param x
     * @param y
     * @param color
     */
    public static PointRoi getPoint(int x[], int y[], Color color) {
        if (x.length != y.length)
            throw new IllegalArgumentException("Arras of different sizes");
        PointRoi pR = new PointRoi(x, y, x.length);
        pR.setStrokeColor(color);
        pR.setFillColor(color);
        return pR;
    }

    /**
     * Generate line.
     * 
     * @param points Coordinates of line defined in Polygon.
     * @param npoints Number of points in <tt>points</tt> to use.
     * @param color Color of the line
     * @return
     */
    public static PolygonRoi getLine(Polygon points, int npoints, Color color) {
        PolygonRoi pR = new PolygonRoi(points, Roi.FREELINE);
        pR.setStrokeColor(color);
        pR.setFillColor(color);
        return pR;
    }

    /**
     * Generate line.
     * 
     * @param points Coordinates of line defined in Polygon.
     * @param color Color of the line
     * @return
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
     * @return
     */
    public static PolygonRoi getLine(Polygon points, Color color, int frame) {
        PolygonRoi pR = GraphicsElements.getLine(points, points.npoints, color);
        pR.setPosition(frame);
        return pR;
    }

    /**
     * Generate line.
     * 
     * @param x Array with x coordinates
     * @param y Array wit y coordinates.
     * @param npoints Number of points to consider
     * @param color
     * @return
     */
    public static PolygonRoi getLine(float[] x, float[] y, int npoints, Color color) {
        if (x.length != y.length)
            throw new IllegalArgumentException("Arras of different sizes");
        PolygonRoi pRoi = new PolygonRoi(x, y, npoints, Roi.FREELINE);
        pRoi.setStrokeColor(color);
        pRoi.setFillColor(color);
        return pRoi;
    }

}
