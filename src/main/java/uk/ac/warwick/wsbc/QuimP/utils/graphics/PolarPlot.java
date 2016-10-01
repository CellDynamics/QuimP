package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.graphics.svg.SVGwritter;

/**
 * @author p.baniukiewicz
 *
 */
public class PolarPlot {
    private static final Logger LOGGER = LogManager.getLogger(PolarPlot.class.getName());
    private STmap mapCell;
    private Point2d gradientcoord;

    /**
     * 
     * @param mapCell
     * @param gradientcoord
     */
    public PolarPlot(STmap mapCell, Point2d gradientcoord) {
        this.mapCell = mapCell;
        this.gradientcoord = gradientcoord;
    }

    /**
     * Compute shift for every frame. Shift value indicates which index of outline point should be 
     * first in maps. This point is closest to <tt>gradientcoord</tt>.
     * 
     * @return Indexes of first points (x-coordinate) for map for every frame (y-cordinate)
     */
    int[] getShift() {
        int[] ret = new int[mapCell.getT()]; // shift for every frame
        for (int f = 0; f < mapCell.getT(); f++) {// along frames
            double dist = Double.MAX_VALUE; // closest point for current frame
            for (int i = 0; i < mapCell.getRes(); i++) {// along points
                Point2d p = new Point2d(mapCell.getxMap()[f][i], mapCell.getyMap()[f][i]); // outline
                                                                                           // point
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
        double xmeans[] = QuimPArrayUtils.getMeanR(mapCell.getxMap());
        double ymeans[] = QuimPArrayUtils.getMeanR(mapCell.getyMap());
        for (int f = 0; f < mapCell.getT(); f++)
            ret[f] = new Point2d(xmeans[f], ymeans[f]);
        return ret;
    }

    /**
     * Compute vectors for one frame between mass centre and outline point.
     * Vectors are in order starting from closest point. This is representation of outline as 
     * vectors.
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
     * Plot of one frame.
     * 
     * @param filename
     * @param frame
     */
    public void generatePlotFrame(String filename, int frame) {
        int[] shifts = getShift(); // calculate shifts of points according to gradientcoord
        Point2d[] mass = getMassCentre(); // calc mass centres for all frames

        Vector2d[] pv = getVectors(frame, mass, shifts); // converts outline points to vectors

        double angles[] = getAngles(pv, pv[0]); // first ic ref vector because they are shifted
        double magn[] = getRadius(frame, shifts[frame], mapCell.getMotMap());

        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filename));
            OutputStreamWriter osw = new OutputStreamWriter(out);
            SVGwritter.writeHeader(osw); // TODO add size of page here

            SVGwritter.Qcircle qc = new SVGwritter.Qcircle(0, 0, 0.02);
            qc.colour = new QColor(1, 0, 0);
            qc.draw(osw);
            for (int i = 0; i < angles.length; i++) {
                double x = Math.cos(angles[i]) * magn[i]; // TODO deal with negative values move to
                                                          // positive
                double y = Math.sin(angles[i]) * magn[i];
                double x1 =
                        Math.cos(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
                double y1 =
                        Math.sin(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
                LOGGER.trace("Point coords:" + x + " " + y + " Polar coords:" + angles[i] + " "
                        + magn[i]);
                SVGwritter.Qline ql = new SVGwritter.Qline(x, y, x1, y1);
                ql.thickness = 0.01;
                ql.draw(osw);
            }

            osw.write("</svg>\n");
            osw.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Plot of mean of frames.
     * @param filename
     */
    public void generatePlot(String filename) {
        int[] shifts = getShift(); // calculate shifts of points according to gradientcoord
        Point2d[] mass = getMassCentre(); // calc mass centres for all frames

        double anglesF[][] = new double[mapCell.getT()][];
        double magnF[][] = new double[mapCell.getT()][];
        for (int f = 0; f < mapCell.getT(); f++) {
            Vector2d[] pv = getVectors(f, mass, shifts); // converts outline points to vectors

            anglesF[f] = getAngles(pv, pv[0]); // first ic ref vector because they are shifted
            magnF[f] = getRadius(f, shifts[f], mapCell.getMotMap());
        }
        double angles[] = QuimPArrayUtils.getMeanC(anglesF);
        double magn[] = QuimPArrayUtils.getMeanC(magnF);

        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filename));
            OutputStreamWriter osw = new OutputStreamWriter(out);
            SVGwritter.writeHeader(osw); // TODO add size of page here

            SVGwritter.Qcircle qc = new SVGwritter.Qcircle(0, 0, 0.02);
            qc.colour = new QColor(1, 0, 0);
            qc.draw(osw);
            for (int i = 0; i < angles.length; i++) {
                double x = Math.cos(angles[i]) * magn[i];
                double y = Math.sin(angles[i]) * magn[i];
                double x1 =
                        Math.cos(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
                double y1 =
                        Math.sin(angles[(i + 1) % angles.length]) * magn[(i + 1) % angles.length];
                LOGGER.trace("Point coords:" + x + " " + y + " Polar coords:" + angles[i] + " "
                        + magn[i]);
                SVGwritter.Qline ql = new SVGwritter.Qline(x, y, x1, y1);
                ql.thickness = 0.01;
                ql.draw(osw);
            }

            osw.write("</svg>\n");
            osw.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * http://www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
     * @param x
     * @param y
     * @param xi
     * @return
     * @throws IllegalArgumentException
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
