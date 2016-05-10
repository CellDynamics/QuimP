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
 * returned data will affect future conversions done by calling accessor methods.
 * 
 * The base format are two arrays \c X and \c Y. All other inputs are converted to arrays first.
 * Conversion e.g. Snake->Snake causes that output Snake is not reference of input one because
 * input has been converted to arrays first.
 * 
 * @todo TODO Add optimization here. E.g counter that check if any method for accessing X,Y
 * was called. If yes new Snake must be created if not reference to old can be returned (but this
 * breaks consistency because one time one has new object, other time reference to old one)
 * 
 * @author p.baniukiewicz
 * @date 11 Apr 2016
 *
 */
public class QuimpDataConverter {

    private double[] X; /*!< extracted x coords from Vec2d */
    private double[] Y; /*!< extracted y coords from Vec2d */

    /**
     * Default constructor
     */
    public QuimpDataConverter() {
        X = new double[0];
        Y = new double[0];
    }

    /**
     * Default constructor if Node list is in form of List
     * 
     * @param input list of vertices. If \c input is \c null \c X and \c Y are set to 0 length
     * arrays, Snake is \c null then
     */
    public QuimpDataConverter(final List<? extends Tuple2d> input) {
        this();
        if (input != null)
            toArrays(input);
    }

    /**
     * Default if Node list is in form of two arrays with coordinates
     * 
     * @param X input list of vertices
     * @param Y input list of vertices
     */
    public QuimpDataConverter(final double X[], final double Y[]) {
        this();
        if (X.length != Y.length)
            throw new IllegalArgumentException("Arrays have different lengths");
        this.X = X;
        this.Y = Y;
    }

    /**
     * Default constructor if Node list is in form of Snake object
     * 
     * @param s Snake to be converted. If \c null \c X and \c Y are set to 0 length arrays, List is
     * also 0 length.
     */
    public QuimpDataConverter(final Snake s) {
        this();
        if (s != null)
            toArrays(s.asList());
    }

    /**
     * Converts Vector2d to \c X and \c Y arrays storing \a x and \a y
     * coordinates of Vector2d separately
     * 
     * @param input List to be converted to arrays
     */
    private void toArrays(final List<? extends Tuple2d> input) {
        int i = 0;
        if (input != null) {
            X = new double[input.size()];
            Y = new double[input.size()];
            for (Tuple2d el : input) {
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
     * @return Array with ordered \a X coordinates of input list. Array can have 0 length.
     */
    public double[] getX() {
        return X;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a Y coordinates of input list. Array can have 0 length.
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
        return list;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a X coordinates of input list as float
     */
    public float[] getFloatX() {
        float Xf[] = new float[X.length];
        for (int i = 0; i < X.length; i++)
            Xf[i] = (float) X[i];
        return Xf;
    }

    /**
     * Data accessor
     * 
     * @return Array with ordered \a Y coordinates of input list as float
     */
    public float[] getFloatY() {
        float Yf[] = new float[Y.length];
        for (int i = 0; i < Y.length; i++)
            Yf[i] = (float) Y[i];
        return Yf;
    }

    /**
     * Data accessor
     * 
     * @return Length of input list
     */
    public int size() {
        return X.length;
    }

    /**
     * Return Snake created from stored data
     * 
     * @param id Id of snake
     * @return Snake object with Nodes in order of data given on input. Can be \c null
     * @throws Exception on Snake creation
     */
    public Snake getSnake(int id) throws Exception {
        if (X.length == 0 || Y.length == 0)
            return null;
        else
            return new Snake(X, Y, id);
    }

}
