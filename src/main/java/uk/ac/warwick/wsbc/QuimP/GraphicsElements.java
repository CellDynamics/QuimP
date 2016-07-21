/**
 * @file GraphicsElements.java
 * @date 5 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import ij.gui.OvalRoi;
import ij.process.FloatPolygon;

/**
 * This class contains static methods for creating simple graphics elements
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

    public static FloatPolygon plotCircle(Point2d base, float radius) {
        OvalRoi or = new OvalRoi(base.getX() - radius / 4, base.getY() - radius / 4, radius / 2,
                radius / 2);
        return or.getFloatPolygon();

    }

}
