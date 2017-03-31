package uk.ac.warwick.wsbc.quimp.utils.graphics;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.QColor;
import uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.quimp.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter;

/**
 * Generate polar plots of motility speed along cell perimeter.
 * 
 * <p>Use basic mapping - location of point on polar plot depends on its position on cell outline.
 * 
 * @author p.baniukiewicz
 *
 */
public class PolarPlot {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(PolarPlot.class.getName());
  private STmap mapCell;
  private Point2d gradientcoord;
  /**
   * Size and position of left upper corner of plot area. Most code below assumes that area is
   * square and centered in 0,0.
   */
  protected Rectangle plotArea;
  /**
   * Distance of polar plot from 0. Rescaling factor.
   */
  public double kscale;
  /**
   * Distance of plot from edge. Rescaling factor.
   */
  public double uscale;

  /**
   * Create PolarPlot object with default plot size.
   * 
   * @param mapCell mapCell
   * @param gradientcoord coordinates of gradient point. Gradient is a feeding of cell put into
   *        cell environment.
   */
  public PolarPlot(STmap mapCell, Point2d gradientcoord) {
    this.mapCell = mapCell;
    this.gradientcoord = gradientcoord;
    plotArea = new Rectangle(-3, -3, 6, 6);
    kscale = 0.1;
    uscale = 0.05;
  }

  /**
   * Compute shift for every frame. Shift value indicates which index of outline point should be
   * first in maps. This point is closest to <tt>gradientcoord</tt>.
   * 
   * @return Indexes of first points (x-coordinate) for map for every frame (y-cordinate)
   */
  int[] getShift() {
    int[] ret = new int[mapCell.getT()]; // shift for every frame
    for (int f = 0; f < mapCell.getT(); f++) { // along frames
      double dist = Double.MAX_VALUE; // closest point for current frame
      for (int i = 0; i < mapCell.getRes(); i++) { // along points
        Point2d p = new Point2d(mapCell.getxMap()[f][i], mapCell.getyMap()[f][i]); // outline point
        double disttmp = p.distance(gradientcoord); // distance from gradinet point
        if (disttmp < dist) { // we have closer point
          dist = disttmp;
          ret[f] = i; // remember index of closer point
        }
      }
    }
    return ret;
  }

  /**
   * Compute mass centres for every frame.
   * 
   * @return Vector of mass centers for every frame.
   */
  Point2d[] getMassCentre() {
    Point2d[] ret = new Point2d[mapCell.getT()];
    double[] xmeans = QuimPArrayUtils.getMeanR(mapCell.getxMap());
    double[] ymeans = QuimPArrayUtils.getMeanR(mapCell.getyMap());
    for (int f = 0; f < mapCell.getT(); f++) {
      ret[f] = new Point2d(xmeans[f], ymeans[f]);
    }
    return ret;
  }

  /**
   * Compute vectors for one frame between mass centre and outline point. Vectors are in order
   * starting from closest point. This is representation of outline as vectors.
   * 
   * @param f
   * @param mass
   * @param shift
   * 
   * @return List of vectors starting from closes to gradientcoord.
   */
  Vector2d[] getVectors(int f, Point2d[] mass, int[] shift) {
    Vector2d[] ret = new Vector2d[mapCell.getRes()];
    int start = shift[f];
    Point2d mc = mass[f];
    int l = 0; // output index
    for (int i = start; i < mapCell.getRes() + start; i++) { // first point is that shifted
      // true array index
      int index = IPadArray.getIndex(mapCell.getRes(), i, IPadArray.CIRCULARPAD);
      // outline point
      Point2d p = new Point2d(mapCell.getxMap()[f][index], mapCell.getyMap()[f][index]);
      p.sub(mc); // p = p-mc - vector from centre to point
      ret[l++] = new Vector2d(p); // put [index] as first
    }
    return ret;
  }

  /**
   * Get values from selected map shifting it according to shift.
   * 
   * @param f Frame to get.
   * @param shift Shift value
   * @param map
   * @return Vector of map values with first value closest to gradientcoord
   */
  double[] getRadius(int f, int shift, double[][] map) {
    double[] ret = new double[map[f].length];
    int l = 0; // output index
    for (int i = shift; i < mapCell.getRes() + shift; i++) {
      int index = IPadArray.getIndex(map[f].length, i, IPadArray.CIRCULARPAD);
      ret[l++] = map[f][index];
    }
    return ret;
  }

  /**
   * Compute angles between reference vector and vectors.
   * 
   * @param vectors array of vectors (in correct order)
   * @param ref reference vector
   * @return angles between reference vector and all <tt>vectors</tt>
   */
  double[] getAngles(Vector2d[] vectors, Vector2d ref) {
    double[] ret = new double[vectors.length];
    for (int i = 0; i < vectors.length; i++) {
      double a1 = Math.atan2(vectors[i].y, vectors[i].x);
      double a2 = Math.atan2(ref.y, ref.x);
      ret[i] = -a1 + a2;
      // convert to 4-squares angle (left comment to comp with matlab plotPolarPlot)
      // ret[i] = (ret[i] < 0) ? (ret[i] + 2 * Math.PI) : ret[i];
    }
    return ret;

  }

  /**
   * Polar plot of one frame.
   * 
   * @param filename filename
   * @param frame frame
   * @throws IOException on file error
   * @see #generatePlot
   */
  public void generatePlotFrame(String filename, int frame) throws IOException {
    int[] shifts = getShift(); // calculate shifts of points according to gradientcoord
    // shift motility
    double[] magn = getRadius(frame, shifts[frame], mapCell.getMotMap());
    // generate vector of arguments
    double[] angles = getUniformAngles(magn.length);
    // remove negative values (shift)
    double min = QuimPArrayUtils.arrayMin(magn);
    for (int i = 0; i < magn.length; i++) {
      magn[i] -= (min + kscale * min); // k*min - distance from 0
    }

    polarPlotPoints(filename, angles, magn); // generate plot

  }

  /**
   * Polar plot of mean of motility along frames.
   * 
   * <p>The position of point on polar plot depends on its position on cell outline, First point
   * after shifting is that closest to selected gradient. It is plotted
   * on angle 0.
   * 
   * @param filename Name of the svg file.
   * @throws IOException on file save
   */
  public void generatePlot(String filename) throws IOException {
    int[] shifts = getShift(); // calculate shifts of points according to gradientcoord
    // contains magnitudes of polar plot (e.g. motility) shifted, so first point is that
    // related to gradientcoord, for every frame [frames][outline points]
    double[][] magnF = new double[mapCell.getT()][];
    for (int f = 0; f < mapCell.getT(); f++) {
      // shift motility for every frame to have gradientcoord related point on 0 index
      magnF[f] = getRadius(f, shifts[f], mapCell.getMotMap());
    }
    // generate vector of arguments
    double[] angles = getUniformAngles(magnF[0].length);

    double[] magn = QuimPArrayUtils.getMeanC(magnF); // compute mean for map on frames
    // rescale to remove negative values and make distance from 0 point
    double min = QuimPArrayUtils.arrayMin(magn);
    for (int i = 0; i < magn.length; i++) {
      magn[i] -= (min + kscale * min); // k*min - distance from 0
    }
    polarPlotPoints(filename, angles, magn); // generate plot
    LOGGER.debug("Polar plot saved: " + filename);

  }

  /**
   * Generate svg plot of points.
   * 
   * @param filename name of svg file
   * @param angles vector of arguments (angles) generated {@link #getUniformAngles(int)}
   * @param magn vector of values related to <tt>angles</tt>
   * @throws IOException on file save
   */
  private void polarPlotPoints(String filename, double[] angles, double[] magn) throws IOException {
    // scale for plotting (6/2) - half of plot area size as plotted from centre)
    double plotscale = plotArea.getWidth() / 2 / QuimPArrayUtils.arrayMax(magn);
    plotscale -= plotscale * uscale; // move a little from edge
    // generate svg
    BufferedOutputStream out;
    out = new BufferedOutputStream(new FileOutputStream(filename));
    OutputStreamWriter osw = new OutputStreamWriter(out);
    SVGwritter.writeHeader(osw, plotArea); // write header with sizes
    // plot axes
    SVGwritter.QPolarAxes qaxes = new SVGwritter.QPolarAxes(plotArea);
    qaxes.draw(osw);
    // circle around
    SVGwritter.Qcircle qc = new SVGwritter.Qcircle(0, 0, 0.02);
    qc.colour = new QColor(1, 0, 0);
    qc.draw(osw);
    // plot points
    for (int i = 0; i < angles.length; i++) {
      // x1;y1, x2;y2 two points that define line segment
      double x = Math.cos(angles[i]) * magn[i];
      double y = Math.sin(angles[i]) * magn[i];
      double x1 = Math.cos(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
      double y1 = Math.sin(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
      // LOGGER.trace("Point coords:" + x + " " + y + " Polar coords:"
      // + Math.toDegrees(angles[i]) + " " + magn[i]);
      SVGwritter.Qline ql =
              new SVGwritter.Qline(x * plotscale, y * plotscale, x1 * plotscale, y1 * plotscale);
      ql.thickness = 0.01;
      ql.draw(osw);
    }

    osw.write("</svg>\n");
    osw.close();
  }

  /**
   * Generate uniformly distributed angles for given resolution.
   * 
   * <p>Generate angles for polar plot (related to plot, not for position of outline points) assume
   * basic mapping - first outline point at angle 0, second at angle delta, etc CCW system is
   * used, but angles are counted in CW. IV and III quarter are negative, then II and I are
   * positive.
   * 
   * @param res Number of angles to generate.
   * @return Array of angles in radians counted CW, IV quarter is first and negative, II quarter
   *         is positive, e.g. -1,-2,...-90,...-180,179,178...,90,....0
   */
  private double[] getUniformAngles(int res) {
    double[] angles = new double[res];
    double delta = (2 * Math.PI - 0) / (res - 1);
    for (int i = 0; i < angles.length; i++) {
      angles[i] = -(0 + delta * i);
      if (angles[i] < -Math.PI) {
        angles[i] = Math.PI + Math.PI + angles[i]; // negative in II q is changed to positv
      }
    }
    return angles;
  }

  /**
   * http://www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
   * 
   * @param x coordinates
   * @param y coordinates
   * @param xi value to interpolate yi
   * @return linear interpolation of y=f(xi)
   * @throws IllegalArgumentException wrong input arrays size
   */
  public static double[] interpLinear(double[] x, double[] y, double[] xi)
          throws IllegalArgumentException {

    if (x.length != y.length) {
      throw new IllegalArgumentException("X and Y must be the same length");
    }
    if (x.length == 1) {
      throw new IllegalArgumentException("X must contain more than one value");
    }
    double[] dx = new double[x.length - 1];
    double[] dy = new double[x.length - 1];
    double[] slope = new double[x.length - 1];
    double[] intercept = new double[x.length - 1];

    // Calculate the line equation (i.e. slope and intercept) between each point
    for (int i = 0; i < x.length - 1; i++) {
      dx[i] = x[i + 1] - x[i];
      if (dx[i] == 0) {
        throw new IllegalArgumentException(
                "X must be montotonic. A duplicate " + "x-value was found");
      }
      if (dx[i] < 0) {
        throw new IllegalArgumentException("X must be sorted");
      }
      dy[i] = y[i + 1] - y[i];
      slope[i] = dy[i] / dx[i];
      intercept[i] = y[i] - x[i] * slope[i];
    }
    // Perform the interpolation here
    double[] yi = new double[xi.length];
    for (int i = 0; i < xi.length; i++) {
      if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
        yi[i] = Double.NaN;
      } else {
        int loc = Arrays.binarySearch(x, xi[i]);
        if (loc < -1) {
          loc = -loc - 2;
          yi[i] = slope[loc] * xi[i] + intercept[loc];
        } else {
          yi[i] = y[loc];
        }
      }
    }

    return yi;
  }

}
