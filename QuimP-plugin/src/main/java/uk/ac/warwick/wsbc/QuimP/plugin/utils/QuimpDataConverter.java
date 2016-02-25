package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.util.List;

import javax.vecmath.Vector2d;

/**
 * Convert Vector2d ordered list into separate X, Y arrays.
 * 
 * Usually each vector coordinate is filtered separately. QuimP by defaults
 * store coordinates in linked list that is converted to ordered ArrayList and
 * passed to plugins. This class may help in accessing X and Y coordinates
 * separately as regular arrays.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimpDataConverter {

    private List<Vector2d> points; //!< reference to input coordinates
    private double[] X; //!< extracted x coords from Vec2d
    private double[] Y; //!< extracted y coords from Vec2d

    /**
     * Default constructor
     * 
     * @param input list of vertices
     */
    public QuimpDataConverter(final List<Vector2d> input) {
        this.points = input;
        toArrays();
    }

    /**
     * Converts Vector2d to \c X and \c Y arrays storing \a x and \a y
     * coordinates of Vector2d separately
     */
    private void toArrays() {
        int i = 0;
        X = new double[points.size()];
        Y = new double[points.size()];
        for (Vector2d el : points) {
            X[i] = el.getX();
            Y[i] = el.getY();
            i++;
        }
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a X coordinates of input list
     */
    public double[] getX() {
        return X;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a Y coordinates of input list
     */
    public double[] getY() {
        return Y;
    }

    /**
     * Data accessor
     * 
     * @return Length of input list
     */
    public int size() {
        return points.size();
    }

    /**
     * Data accessor
     * 
     * @return List of vertices as list
     * @todo consider creating copy here
     */
    public List<Vector2d> getList() {
        return points;
    }

}
