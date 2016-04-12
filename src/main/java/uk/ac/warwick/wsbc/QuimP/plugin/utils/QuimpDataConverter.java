package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;

import uk.ac.warwick.wsbc.QuimP.Snake;

/**
 * Perform conversions among Snake, List and X, Y arrays
 * 
 * As this object returns references to arrays and list, any modification done "in place" on 
 * returned data will affect future conversions done by calling accessor methods (except getList())
 * 
 * @author p.baniukiewicz
 * @date 11 Apr 2016
 *
 */
public class QuimpDataConverter {

    private List<? extends Tuple2d> points; /*!< reference to input coordinates */
    private double[] X; /*!< extracted x coords from Vec2d */
    private double[] Y; /*!< extracted y coords from Vec2d */

    public QuimpDataConverter() {
        points = new ArrayList<Point2d>();
        X = new double[0];
        Y = new double[0];
    }

    /**
     * Default constructor if Node list is in form of List
     * 
     * @param input list of vertices
     */
    public QuimpDataConverter(final List<? extends Tuple2d> input) {
        this();
        if (input != null) {
            this.points = input;
            toArrays();
        }
    }

    /**
     * Default if Node list is in form of two arrays with coordinates
     * 
     * @param X input list of vertices
     * @param Y input list of vertices
     */
    public QuimpDataConverter(final double X[], final double Y[]) {
        this.X = X;
        this.Y = Y;
        points = null;
    }

    /**
     * Default constructor if Node list is in form of Snake object
     * 
     * @param s Snake to be converted
     */
    public QuimpDataConverter(final Snake s) {
        this();
        if (s != null) {
            points = s.asList();
            toArrays();
        }
    }

    /**
     * Converts Vector2d to \c X and \c Y arrays storing \a x and \a y
     * coordinates of Vector2d separately
     */
    private void toArrays() {
        int i = 0;
        if (points != null) {
            X = new double[points.size()];
            Y = new double[points.size()];
            for (Tuple2d el : points) {
                X[i] = el.getX();
                Y[i] = el.getY();
                i++;
            }
        } else {
            X = new double[0];
            Y = new double[0];
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
     * @return List of Point2d from stored objects
     * @warning If user modifies this list this object loses its consistency 
     */
    public List<Point2d> getList() {
        ArrayList<Point2d> list = new ArrayList<>();
        for (int i = 0; i < X.length; i++)
            list.add(new Point2d(X[i], Y[i]));
        points = list;
        return list;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a X coordinates of input list as float
     */
    public float[] getFloatX() {
        float Xf[] = new float[points.size()];
        for (int i = 0; i < points.size(); i++)
            Xf[i] = (float) X[i];
        return Xf;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a Y coordinates of input list as float
     */
    public float[] getFloatY() {
        float Yf[] = new float[points.size()];
        for (int i = 0; i < points.size(); i++)
            Yf[i] = (float) Y[i];
        return Yf;
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
     * Return Snake created from stored data
     * 
     * @param id Id of snake
     * @return Snake object with Nodes in order of data given on input
     * @throws Exception
     */
    public Snake getSnake(int id) throws Exception {
        if (X.length == 0 || Y.length == 0)
            return null;
        else
            return new Snake(X, Y, id);
    }

}
