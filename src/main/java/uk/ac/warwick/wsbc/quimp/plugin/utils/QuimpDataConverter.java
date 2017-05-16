package uk.ac.warwick.wsbc.quimp.plugin.utils;

import java.util.ArrayList;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Tuple2d;

import ij.util.Tools;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.Shape;
import uk.ac.warwick.wsbc.quimp.Snake;

/**
 * Perform conversions among Snake, List and xc, yc arrays.
 * 
 * <p>As this object returns references to arrays and list, any modification done "in place" on
 * returned data will affect future conversions done by calling accessor methods.
 * 
 * <p>The base format are two arrays xc and yc. All other inputs are converted to arrays
 * first.
 * Conversion e.g. Snake->Snake causes that output Snake is not reference of input one because input
 * has been converted to arrays first.
 * 
 * @author p.baniukiewicz
 * 
 */
public class QuimpDataConverter {

  private double[] xc; // extracted x coords from Vec2d
  private double[] yc; // extracted y coords from Vec2d

  /**
   * Default constructor.
   */
  public QuimpDataConverter() {
    xc = new double[0];
    yc = new double[0];
  }

  /**
   * Default constructor if Node list is in form of List.
   * 
   * @param input list of vertices. If input is null xc and yc are set to 0 length arrays,
   *        Snake is
   *        null then
   */
  public QuimpDataConverter(final List<? extends Tuple2d> input) {
    this();
    if (input != null) {
      toArrays(input);
    }
  }

  /**
   * Default if Node list is in form of two arrays with coordinates.
   * 
   * @param x input list of vertices
   * @param y input list of vertices
   */
  public QuimpDataConverter(final double[] x, final double[] y) {
    this();
    if (x.length != y.length) {
      throw new IllegalArgumentException("Arrays have different lengths");
    }
    this.xc = x;
    this.yc = y;
  }

  /**
   * Default if Node list is in form of two arrays with coordinates.
   * 
   * @param x input list of vertices
   * @param y input list of vertices
   */
  public QuimpDataConverter(final float[] x, final float[] y) {
    this();
    if (x.length != y.length) {
      throw new IllegalArgumentException("Arrays have different lengths");
    }
    this.xc = Tools.toDouble(x);
    this.yc = Tools.toDouble(y);
  }

  /**
   * Default constructor if Node list is in form of Snake object.
   * 
   * @param s Shape to be converted. If null xc and yc are set to 0 length arrays, List is
   *        also 0 length.
   */
  public QuimpDataConverter(final Shape<?> s) {
    this();
    if (s != null) {
      toArrays(s.asList());
    }
  }

  /**
   * Converts Vector2d to xc and yc arrays storing xc and yc coordinates of
   * Vector2d separately.
   * 
   * @param input List to be converted to arrays
   */
  private void toArrays(final List<? extends Tuple2d> input) {
    int i = 0;
    if (input != null) {
      xc = new double[input.size()];
      yc = new double[input.size()];
      for (Tuple2d el : input) {
        xc[i] = el.getX();
        yc[i] = el.getY();
        i++;
      }
    } else {
      xc = new double[0];
      yc = new double[0];
    }
  }

  /**
   * Data accessor.
   * 
   * @return Array with ordered xc coordinates of input list. Array can have 0 length.
   */
  public double[] getX() {
    return xc;
  }

  /**
   * Data accessor.
   * 
   * @return Array with ordered yc coordinates of input list. Array can have 0 length.
   */
  public double[] getY() {
    return yc;
  }

  /**
   * Data accessor.
   * 
   * <p><b>Warning</b>
   * 
   * <p>If user modifies this list this object loses its consistency
   * 
   * <p>To convert {@link Shape} to list use {@link Shape#asList()}.
   * 
   * @return List of Point2d from stored objects
   */
  public List<Point2d> getList() {
    ArrayList<Point2d> list = new ArrayList<>();
    for (int i = 0; i < xc.length; i++) {
      list.add(new Point2d(xc[i], yc[i]));
    }
    return list;
  }

  /**
   * Data accessor.
   * 
   * @return Array with ordered xc coordinates of input list as float
   */
  public float[] getFloatX() {
    float[] xf = new float[xc.length];
    for (int i = 0; i < xc.length; i++) {
      xf[i] = (float) xc[i];
    }
    return xf;
  }

  /**
   * Data accessor.
   * 
   * @return Array with ordered yc coordinates of input list as float
   */
  public float[] getFloatY() {
    float[] yf = new float[yc.length];
    for (int i = 0; i < yc.length; i++) {
      yf[i] = (float) yc[i];
    }
    return yf;
  }

  /**
   * Data accessor.
   * 
   * @return Length of input list
   */
  public int size() {
    return xc.length;
  }

  /**
   * Return Snake created from stored data.
   * 
   * <p>Head node is first point from list.
   * 
   * @param id Id of snake
   * @return Snake object with Nodes in order of data given on input. Can be null
   * @throws QuimpException on Snake creation
   * @see uk.ac.warwick.wsbc.quimp.Snake#Snake(double[], double[], int)
   * @see uk.ac.warwick.wsbc.quimp.Snake#removeNode(uk.ac.warwick.wsbc.quimp.Node)
   */
  public Snake getSnake(int id) throws QuimpException {
    Snake ret = null;
    if (xc.length == 0 || yc.length == 0) {
      return ret;
    } else {
      ret = new Snake(xc, yc, id);
      return ret;
    }
  }

  /**
   * Return Outline created from stored data.
   * 
   * @param id Id of snake
   * @return Outline object with Nodes in order of data given on input. Can be null. Normales are
   *         set outwards. Head node is first point from list.
   * @throws QuimpException on Outline creation
   * @see uk.ac.warwick.wsbc.quimp.Snake#Snake(double[], double[], int)
   * @see uk.ac.warwick.wsbc.quimp.Snake#removeNode(uk.ac.warwick.wsbc.quimp.Node)
   */
  public Outline getOutline(int id) throws QuimpException {
    Snake stmp = getSnake(id);
    if (stmp == null) {
      return null;
    } else {
      return new Outline(stmp);
    }
  }

}
