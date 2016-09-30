package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import javax.vecmath.Point2d;

import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;

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
     * first in maps. This point is closest to <tt>gradientcoord</tt>
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
}
