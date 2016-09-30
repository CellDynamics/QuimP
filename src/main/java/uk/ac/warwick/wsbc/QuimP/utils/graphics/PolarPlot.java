package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 *
 */
public class PolarPlot {

    private STmap mapCell;

    public PolarPlot(STmap mapCell) {
        this.mapCell = mapCell;
    }

    /**
     * Compute shift for every frame. Shift value indicates which index of outline point should be 
     * first in maps. This point is closest to <tt>gradientcoord</tt>.
     * 
     * @param gradientcoord
     * @return Indexes of first points (x-coordinate) for map for every frame (y-cordinate)
     */
    int[] getShift(Point2d gradientcoord) {
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
        double xmeans[] = QuimPArrayUtils.geMean(mapCell.getxMap());
        double ymeans[] = QuimPArrayUtils.geMean(mapCell.getyMap());
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
}
