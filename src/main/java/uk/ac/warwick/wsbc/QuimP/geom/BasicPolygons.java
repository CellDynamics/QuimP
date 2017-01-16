/**
 */
package uk.ac.warwick.wsbc.QuimP.geom;

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

    static final Logger LOGGER = LoggerFactory.getLogger(BasicPolygons.class.getName());

    public BasicPolygons() {

    }

    /**
     * Calculates area of polygon.
     * 
     * Supports triangles, regular and irregular polygons, convex or concave polygons
     * <p>
     * <b>Warning</b>
     * <p>
     * Polygon can not intersect itself.
     * 
     * @param P Vertices of polygon in specified order
     * @return Area
     * 
     */
    public double getPolyArea(final List<? extends Tuple2d> P) {
        int i, j;
        double area = 0;

        for (i = 0; i < P.size(); i++) {
            j = (i + 1) % P.size(); // will round pointer to 0 for last point
            Tuple2d pi = P.get(i);
            Tuple2d pj = P.get(j);
            area += pi.getX() * pj.getY();
            area -= pi.getY() * pj.getX();
        }
        area /= 2.0;
        return (Math.abs(area));
    }

    /**
     * Calculates perimeter of polygon.
     * 
     * @param P Vertices of polygon in specified order
     * @return Perimeter
     */
    public double getPolyPerim(final List<? extends Tuple2d> P) {
        int i, j;
        double len = 0;
        ArrayList<Vector2d> V = new ArrayList<>();
        // get vectors between points
        for (i = 0; i < P.size(); i++) {
            j = (i + 1) % P.size(); // will round pointer to 0 for last point
            Tuple2d first = P.get(i);
            Tuple2d second = P.get(j);
            Vector2d tmp = new Vector2d(second.getX() - first.getX(), second.getY() - first.getY());
            V.add(tmp);
        }
        for (Vector2d v : V)
            len += v.length();
        return len;
    }

    /**
     * Test whether <tt>Ptest</tt> is inside polygon <tt>P</tt>
     * 
     * @param P Vertices of polygon in specified order
     * @param Ptest Point to be tested
     * @return true if <tt>Ptest</tt> is inside <tt>P</tt>, false otherwise
     * @see <a href=
     *      "link">http://www.shodor.org/~jmorrell/interactivate/org/shodor/util11/PolygonUtils.java</a>
     */
    public boolean isPointInside(final List<? extends Tuple2d> P, final Tuple2d Ptest) {
        double angle = 0;
        Point2d p1 = null, p2 = null;
        for (int i = 0; i < P.size(); i++) {
            p1 = new Point2d(P.get(i).getX() - Ptest.getX(), P.get(i).getY() - Ptest.getY());
            p2 = new Point2d(P.get((i + 1) % P.size()).getX() - Ptest.getX(),
                    P.get((i + 1) % P.size()).getY() - Ptest.getY());
            angle += angle2D(p1, p2);
        }
        return Math.abs(angle) >= Math.PI;
    }

    /**
     * Helper method.
     * 
     * @param p1
     * @param p2
     * @return angle between points.
     */
    private double angle2D(Point2d p1, Point2d p2) {
        double dtheta = Math.atan2(p2.y, p2.x) - Math.atan2(p1.y, p1.x);
        while (dtheta > Math.PI)
            dtheta -= 2.0 * Math.PI;
        while (dtheta < -1.0 * Math.PI)
            dtheta += 2.0 * Math.PI;
        return dtheta;
    }

    /**
     * Test if all points <tt>Ptest</tt> are inside polygon <tt>P</tt>.
     * 
     * @param P
     * @param Ptest
     * @return {@link #isPointInside(List, Tuple2d)}
     * 
     * @see #isPointInside(List, Tuple2d)
     */
    public boolean arePointsInside(final List<? extends Tuple2d> P,
            final List<? extends Tuple2d> Ptest) {
        boolean result = true;
        Iterator<? extends Tuple2d> it = Ptest.iterator();
        while (it.hasNext() && result) {
            result = isPointInside(P, it.next());
        }
        return result;
    }

    /**
     * Test if any point from <tt>Ptest</tt> is inside of <tt>P</tt>.
     * 
     * @param P
     * @param Ptest
     * @return {@link #isPointInside(List, Tuple2d)}
     * 
     * @see #isPointInside(List, Tuple2d)
     */
    public boolean isanyPointInside(final List<? extends Tuple2d> P,
            final List<? extends Tuple2d> Ptest) {
        boolean result = false;
        Iterator<? extends Tuple2d> it = Ptest.iterator();
        while (it.hasNext() && !result) {
            result = isPointInside(P, it.next());
        }
        return result;
    }

    /**
     * Get center of mass of polygon.
     * <p>
     * <b>Warning</b>
     * <p>
     * Require correct polygon with non crossing edges.
     * 
     * @param P Vertices of polygon in specified order
     * @return Point of center of mass
     * @throws IllegalArgumentException when defective polygon is given (area equals 0)
     * @see <a href=
     *      "link">http://stackoverflow.com/questions/5271583/center-of-gravity-of-a-polygon</a>
     */
    public Point2d polygonCenterOfMass(final List<? extends Tuple2d> P) {

        int N = P.size();
        Point2d[] polygon = new Point2d[N];

        for (int q = 0; q < N; q++)
            polygon[q] = new Point2d(P.get(q));

        double cx = 0, cy = 0;
        double A = getPolyArea(P);
        if (A == 0) // defective polygon
            throw new IllegalArgumentException("Defective polygon, area is 0");
        int i, j;

        double factor = 0;
        for (i = 0; i < N; i++) {
            j = (i + 1) % N;
            factor = (polygon[i].x * polygon[j].y - polygon[j].x * polygon[i].y);
            cx += (polygon[i].x + polygon[j].x) * factor;
            cy += (polygon[i].y + polygon[j].y) * factor;
        }
        factor = 1.0 / (6.0 * A);
        cx *= factor;
        cy *= factor;
        return new Point2d(Math.abs(cx), Math.abs((cy)));
    }

}
