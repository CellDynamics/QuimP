/**
 * @file FakeSegmentation.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ShapeRoi;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.geom.TrackOutline;

/**
 * @author p.baniukiewicz
 * @date 27 Jun 2016
 *
 */
public class FakeSegmentation {

    private static final Logger LOGGER = LogManager.getLogger(FakeSegmentation.class.getName());

    private int nextID = 0;
    private ImagePlus iP;
    private TrackOutline[] trackers;

    public FakeSegmentation(ImagePlus iP) {
        this.iP = iP.duplicate();
        LOGGER.debug("Got " + iP.getImageStackSize() + " slices");
        trackers = new TrackOutline[this.iP.getImageStackSize()];
        ImageStack iPs = this.iP.getStack();
        for (int i = 0; i < trackers.length; i++)
            trackers[i] = new TrackOutline(iPs.getProcessor(i + 1), 0);
    }

    /**
     * 
     * @param r1
     * @param r2
     * @return
     * @warning Modify \a r1 parameter
     */
    private boolean testIntersect(ShapeRoi r1, ShapeRoi r2) {
        if (r1 == null || r2 == null)
            return false;
        ShapeRoi intersect = r1.and(r2);
        if (intersect.getFloatWidth() == 0 || intersect.getFloatHeight() == 0) {
            LOGGER.debug(r1 + " and " + r2 + " does not intersect");
            return false;
        } else {
            LOGGER.debug(r1 + " and " + r2 + " does intersect");
            return true;
        }
    }

    /**
     * 
     * @param sR
     * @param sRa
     * @return 
     */
    private void testIntersect(SegmentedShapeRoi sR, ArrayList<SegmentedShapeRoi> sRa) {
        if (sR.id == SegmentedShapeRoi.NOT_COUNTED) { // root - first outline
            sR.id = nextID++; // if not counted start new chain assigning new id
        }
        for (SegmentedShapeRoi s : sRa)
            if (testIntersect((ShapeRoi) sR.clone(), s) == true) {
                s.id = sR.id; // next outline has the same id
                break; // do not look more on this set (this frame)
            }
    }

    public void trackObjects() {
        for (int f = 0; f < trackers.length - 1; f++) {
            ArrayList<SegmentedShapeRoi> o1 = trackers[f].outlines;
            ArrayList<SegmentedShapeRoi> o2 = trackers[f + 1].outlines;
            for (SegmentedShapeRoi sR : o1) {
                testIntersect(sR, o2);
            }
        }
    }

    public ArrayList<ArrayList<ShapeRoi>> getChains() {
        ArrayList<ArrayList<ShapeRoi>> ret = new ArrayList<>(nextID);
        for (int i = 0; i < nextID; i++)
            ret.add(new ArrayList<>());
        for (TrackOutline tO : trackers) { // go through all Outlines and sort them for ID
            for (SegmentedShapeRoi sS : tO.outlines) {
                ret.get(sS.id).add(sS);
            }
        }
        return ret;
    }
}
