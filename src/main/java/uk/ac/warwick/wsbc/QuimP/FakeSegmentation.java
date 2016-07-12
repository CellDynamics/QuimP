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
 * Run fake segmentation converting black-white masks to ordered ROIs.
 * 
 * This class mainly join subsequent outlines to chains that contain outlines related by origin (
 * when next outline originates from previous - it means that next object overlap previous one)
 * The segmentation itself - generation of outlines for one slice is done in TrackOutline class 
 * 
 * The ROIs are grouped according to their origin and they have assigned frame number where they 
 * appeared. The algorithm is as follows:
 * The frames from input stack from first to before last are processed. For every i-th frame the
 * outlines are obtained and compared with i+1 frame. If any of k-th outline from i+1 frame overlap
 * l-th outline on i-th frame, the k-th outline gets the same id as l-th but only if k-th does not
 * have any ID yet. There for if there is outline that does not have source on i-th frame, it will 
 * skipped now but it will be found in next iteration and because it does not have ID,
 * the new will be assigned to it.
 * 
 * If there is break in chain (missing object), the object on the next frame will begin the new
 * chain. 
 * 
 * @startuml
 * User-->(Create FakeSegmentation)
 * User->(run tracking)
 * User->(get chains)
 * (Create FakeSegmentation).->(create TrackOutline) : <<extend>>
 * @enduml
 * 
 * After creation of object user has to call trackObjects()
 * to run tracking. Segmentation is run on object creation. Finally, getChains() should be called
 * to get results - chains of outlines.
 * 
 * @startuml
 * actor User
 * User->FakeSegmentation : <<create>>\n""image""
 * loop for every frame
 * FakeSegmentation->TrackOutline : <<create>>\n""slice"",""frame""
 * activate TrackOutline
 * TrackOutline-->FakeSegmentation : //obj//
 * FakeSegmentation->FakeSegmentation : store //obj// in ""trackers""
 * note left
 * See TrackOutline
 * trackers are ROIs for
 * one slice kept in TrackOutline
 * object
 * end note
 * end
 * User->trackObjects
 * loop for every tracker //o2//
 * loop for every object in tracker //sR//
 * trackObjects->TrackOutline : get ""outlines""
 * TrackOutline->trackObjects : ""outlines""
 * note left : references
 * trackObjects->testIntersect : ""sR"",""o2""
 * testIntersect->trackObjects : set ID to current outline
 * note right
 * Modify reference in TrackOutline
 * end note
 * testIntersect->trackObjects : set parent ID to next outline
 * note left
 * Test for current object and all
 * on next frame
 * end note
 * end
 * end
 * User->getChains
 * getChains->getChains : sort according to ID
 * getChains->User : return array
 * @enduml
 * 
 * @author p.baniukiewicz
 * @date 27 Jun 2016
 * @see uk.ac.warwick.wsbc.QuimP.geom.TrackOutline
 *
 */
public class FakeSegmentation {

    private static final Logger LOGGER = LogManager.getLogger(FakeSegmentation.class.getName());

    private int nextID = 0; //!< next free ID
    private ImagePlus iP; //!< image to process (stack)
    int backgroundColor = 0; //!< predefined background color
    /**
     * array of segmented slices. One TrackOutline object can have some outlines, depending how 
     * many objects were on this slice
     */
    private TrackOutline[] trackers;

    /**
     * Constructor for segmentation of stack
     * 
     * @param iP stack of images to segment
     * @throws IllegalArgumentException when wrong image is provided
     */
    public FakeSegmentation(final ImagePlus iP) {
        if (iP == null) // can not create from null image
            throw new IllegalArgumentException("The image was: null");
        this.iP = iP.duplicate();
        LOGGER.debug("Got " + iP.getImageStackSize() + " slices");
        trackers = new TrackOutline[this.iP.getImageStackSize()];
        ImageStack iPs = this.iP.getStack();
        for (int i = 0; i < trackers.length; i++)
            trackers[i] = new TrackOutline(iPs.getProcessor(i + 1), backgroundColor, i + 1); // slice
                                                                                             // outlining
    }

    /**
     * Test whether two ROIs overlap
     * 
     * @param r1 First ROI - it will be modified!
     * @param r2 Seconf ROI
     * @return \a true if \a r1 and \a r2 overlap
     * @warning Modify \a r1 parameter
     */
    private boolean testIntersect(final ShapeRoi r1, final ShapeRoi r2) {
        if (r1 == null || r2 == null)
            return false;
        ShapeRoi intersect = r1.and(r2);
        if (intersect.getFloatWidth() == 0 || intersect.getFloatHeight() == 0) {
            LOGGER.debug(r1 + " and " + r2 + " do not intersect");
            return false;
        } else {
            LOGGER.debug(r1 + " and " + r2 + " do intersect");
            return true;
        }
    }

    /**
     * Test whether given ROI overlap any of ROI in array and assign correct ID to ROIs
     * 
     * If any of \a sRa overlap \a sR, the roi from array gets the same ID as \a sR. If \a sR does
     * not have ID it get the new one
     * 
     * @param sR ROI to test (not modified)
     * @param sRa Array of ROIs to test
     * 
     */
    private void testIntersect(final SegmentedShapeRoi sR, final ArrayList<SegmentedShapeRoi> sRa) {
        if (sR.getId() == SegmentedShapeRoi.NOT_COUNTED) { // root - first outline
            sR.setId(nextID++); // if not counted start new chain assigning new id
        }
        for (SegmentedShapeRoi s : sRa)
            if (testIntersect((ShapeRoi) sR.clone(), s) == true) {
                s.setId(sR.getId()); // next outline has the same id
                break; // do not look more on this set (this frame)
            }
    }

    /**
     * Main runner for tracking.
     * 
     * In result of this method the ROIs kept in TrackOutline objects will be modified by giving them 
     * IDs of their parent. 
     */
    public void trackObjects() {
        if (trackers.length == 1) { // only one slice, use the same reference for testIntersect
            ArrayList<SegmentedShapeRoi> o1 = trackers[0].outlines; // get frame current
            ArrayList<SegmentedShapeRoi> o2 = trackers[0].outlines; // and next
            for (SegmentedShapeRoi sR : o1) { // iterate over all objects in current frame
                testIntersect(sR, o2); // and find its child if any on next frame
            }
        } // loop below does not fire for one slice
        for (int f = 0; f < trackers.length - 1; f++) { // iterate over frames
            ArrayList<SegmentedShapeRoi> o1 = trackers[f].outlines; // get frame current
            ArrayList<SegmentedShapeRoi> o2 = trackers[f + 1].outlines; // and next
            for (SegmentedShapeRoi sR : o1) { // iterate over all objects in current frame
                testIntersect(sR, o2); // and find its child if any on next frame
            }
        }
    }

    /**
     * Compose chains of object related to each others along frames. 
     * 
     * Relation means that previous object and next one overlap, thus their segmentations will be
     * assigned to the same group and they will be in correct order as they appeared in stack
     * 
     * @return List of Lists that contains outlines. First level of list is the chain (found
     * related objects), the second level are outlines for this chain. Every outline has coded
     * frame where it appeared. 
     */
    public ArrayList<ArrayList<SegmentedShapeRoi>> getChains() {
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = new ArrayList<>(nextID);
        for (int i = 0; i < nextID; i++)
            ret.add(new ArrayList<>());
        for (TrackOutline tO : trackers) { // go through all Outlines and sort them for ID
            for (SegmentedShapeRoi sS : tO.outlines) {
                ret.get(sS.getId()).add(sS);
            }
        }
        return ret;
    }
}
