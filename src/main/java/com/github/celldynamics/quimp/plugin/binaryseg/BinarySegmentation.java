package com.github.celldynamics.quimp.plugin.binaryseg;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.geom.TrackOutline;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ShapeRoi;

/*
 * //!>
 * @startuml doc-files/BinarySegmentation_1_UML.png
 * User-->(Create BinarySegmentation)
 * User->(run tracking)
 * User->(get chains)
 * (Create BinarySegmentation).->(create TrackOutline) : <<extend>>
 * @enduml
 * 
 * @startuml doc-files/BinarySegmentation_2_UML.png
 * actor User
 * User->BinarySegmentation : <<create>>\n""image""
 * loop for every frame
 * BinarySegmentation->TrackOutline : <<create>>\n""slice"",""frame""
 * activate TrackOutline
 * TrackOutline-->BinarySegmentation : //obj//
 * BinarySegmentation->BinarySegmentation : store //obj// in ""trackers""
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
 * //!<
 */
/**
 * Run Binary segmentation converting black-white masks to ordered ROIs.
 * 
 * <p>This class mainly join subsequent outlines to chains that contain outlines related by origin (
 * when next outline originates from previous - it means that next object overlap previous one) The
 * segmentation itself - generation of outlines for one slice is done in TrackOutline class
 * 
 * <p>The ROIs are grouped according to their origin and they have assigned frame number where they
 * appeared. The algorithm is as follows: The frames from input stack from first to before last are
 * processed. For every i-th frame the outlines are obtained and compared with i+1 frame. If any of
 * k-th outline from i+1 frame overlap l-th outline on i-th frame, the k-th outline gets the same id
 * as l-th but only if k-th does not have any ID yet. There for if there is outline that does not
 * have source on i-th frame, it will skipped now but it will be found in next iteration and because
 * it does not have ID, the new will be assigned to it.
 * 
 * <p>If there is break in chain (missing object), the object on the next frame will begin the new
 * chain. <br>
 * <img src="doc-files/BinarySegmentation_1_UML.png"/><br>
 * After creation of object user has to call trackObjects() to run tracking. Segmentation is run on
 * object creation. Finally, getChains() should be called to get results - chains of outlines. <br>
 * <img src="doc-files/BinarySegmentation_2_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.geom.TrackOutline
 *
 */
public class BinarySegmentation {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentation.class.getName());

  private int nextID = 0; // next free ID
  private ImagePlus ip; // image to process (stack)
  /**
   * Predefined background color.
   */
  int backgroundColor = 0;
  /**
   * Array of segmented slices. One TrackOutline object can have some outlines, depending how many
   * objects were on this slice
   */
  private TrackOutline[] trackers;

  /**
   * Constructor for segmentation of stack.
   * 
   * @param ip stack of images to segment
   * @throws QuimpPluginException when image is null or wrong type
   */
  public BinarySegmentation(final ImagePlus ip) throws QuimpPluginException {
    if (ip == null) {
      throw new QuimpPluginException("The image was null");
    }
    if (!ip.getProcessor().isGrayscale()) {
      throw new QuimpPluginException("Input image must be 8-bit");
    }

    this.ip = ip.duplicate();
    LOGGER.debug("Got " + ip.getImageStackSize() + " slices");
    trackers = new TrackOutline[this.ip.getImageStackSize()];
    ImageStack ips = this.ip.getStack();
    for (int i = 0; i < trackers.length; i++) {
      trackers[i] = new TrackOutline(ips.getProcessor(i + 1), backgroundColor, i + 1); // outlining
    }
  }

  /**
   * Test whether two ROIs overlap. Modify r1 parameter
   * 
   * @param r1 First ROI - it will be modified!
   * @param r2 Seconf ROI
   * @return true if r1 and r2 overlap
   */
  private boolean testIntersect(final ShapeRoi r1, final ShapeRoi r2) {
    if (r1 == null || r2 == null) {
      return false;
    }
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
   * <p>If any of sRa overlap sR, the roi from array gets the same ID as \a sR. If \a sR does
   * not have ID it get the new one
   * 
   * @param sr ROI to test (not modified)
   * @param sra Array of ROIs to test
   * 
   */
  private void testIntersect(final SegmentedShapeRoi sr, final ArrayList<SegmentedShapeRoi> sra) {
    if (sr.getId() == SegmentedShapeRoi.NOT_COUNTED) { // root - first outline
      sr.setId(nextID++); // if not counted start new chain assigning new id
    }
    for (SegmentedShapeRoi s : sra) {
      if (testIntersect((ShapeRoi) sr.clone(), s) == true) {
        s.setId(sr.getId()); // next outline has the same id
        break; // do not look more on this set (this frame)
      }
    }
  }

  /**
   * Main runner for tracking.
   * 
   * <p>In result of this method the ROIs kept in TrackOutline objects will be modified by giving
   * them IDs of their parent.
   */
  public void trackObjects() {
    if (trackers.length == 1) { // only one slice, use the same reference for testIntersect
      ArrayList<SegmentedShapeRoi> o1 = trackers[0].outlines; // get frame current
      ArrayList<SegmentedShapeRoi> o2 = trackers[0].outlines; // and next
      for (SegmentedShapeRoi sr : o1) { // iterate over all objects in current frame
        testIntersect(sr, o2); // and find its child if any on next frame
      }
    } // loop below does not fire for one slice
    for (int f = 0; f < trackers.length - 1; f++) { // iterate over frames
      ArrayList<SegmentedShapeRoi> o1 = trackers[f].outlines; // get frame current
      ArrayList<SegmentedShapeRoi> o2 = trackers[f + 1].outlines; // and next
      for (SegmentedShapeRoi sr : o1) { // iterate over all objects in current frame
        testIntersect(sr, o2); // and find its child if any on next frame
      }
    }
  }

  /**
   * Compose chains of object related to each others along frames.
   * 
   * <p>Relation means that previous object and next one overlap, thus their segmentations will be
   * assigned to the same group and they will be in correct order as they appeared in stack
   * 
   * @return List of Lists that contains outlines. First level of list is the chain (found related
   *         objects), the second level are outlines for this chain. Every outline has coded frame
   *         where it appeared.
   */
  public ArrayList<ArrayList<SegmentedShapeRoi>> getChains() {
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = new ArrayList<>(nextID);
    for (int i = 0; i < nextID; i++) {
      ret.add(new ArrayList<>());
    }
    for (TrackOutline to : trackers) { // go through all Outlines and sort them for ID
      for (SegmentedShapeRoi ss : to.outlines) {
        ret.get(ss.getId()).add(ss);
      }
    }
    return ret;
  }
}
