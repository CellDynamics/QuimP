package com.github.celldynamics.quimp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Represent collection of SnakeHandlers.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 */
public class Nest implements IQuimpSerialize {

  private static final Logger LOGGER = LoggerFactory.getLogger(Nest.class.getName());

  /**
   * List of SnakeHandlers.
   * 
   * <p>SnakeHandlers get subsequent IDs therefore index in this array matches SnakeHandler's ID.
   * This relation can be broken when any cell is deleted using
   * {@link #removeHandler(SnakeHandler)} method that simply removes it from array shifting other
   * objects down.
   * 
   * <p>This array should be accessed by {@link #getHandler(int)} when one needs access handler on
   * certain index but does not care about its ID or {@link #getHandlerofId(int)} when ID is
   * crucial.
   */
  private ArrayList<SnakeHandler> sHs;
  /**
   * Number of stored snakes in nest.
   */
  private int NSNAKES;
  /**
   * Number of live stored snakes in nest.
   */
  private int ALIVE;
  /**
   * Next free ID.
   */
  private int nextID;

  /**
   * Default constructor.
   */
  public Nest() {
    NSNAKES = 0;
    ALIVE = 0;
    nextID = 0;
    sHs = new ArrayList<SnakeHandler>();
  }

  /**
   * Convert array of SegmentedShapeRoi to SnakeHandlers.
   * 
   * <p>Conversion within one SnakeHandler is stopped when there is defective Snake.
   * 
   * @param roiArray First level stands for objects (SnakeHandlers(, second for Snakes within one
   *        chain
   */
  public void addHandlers(ArrayList<ArrayList<SegmentedShapeRoi>> roiArray) {
    LOGGER.trace("Adding " + roiArray.size() + "SnakeHandlers");
    for (List<SegmentedShapeRoi> lsS : roiArray) {
      try {
        sHs.add(new SnakeHandler(lsS, nextID));
        nextID++;
        NSNAKES++;
        ALIVE++;
      } catch (Exception e) {
        LOGGER.error("A snake on frame " + lsS.get(0).getFrame() + " failed to initilise "
                + e.getMessage());
      }
    }
  }

  /**
   * Add rois to Nest - convert them to Snake and store in SnakeHandler.
   * 
   * @param roiArray roiArray
   * @param startFrame start frame, rois from roiArray are added to subsequent frames.
   */
  public void addHandlers(Roi[] roiArray, int startFrame) {
    int i = 0;
    for (; i < roiArray.length; i++) {
      try {
        sHs.add(new SnakeHandler(roiArray[i], startFrame, nextID));
        nextID++;
        NSNAKES++;
        ALIVE++;
      } catch (Exception e) {
        BOA_.log("A snake failed to initilise: " + e.getMessage());
      }
    }
    BOA_.log("Added " + roiArray.length + " cells at frame " + startFrame);
    BOA_.log("Cells being tracked: " + NSNAKES);
  }

  /**
   * Add roi to Nest - convert them to Snake and store in SnakeHandler.
   * 
   * @param r ROI object that contain image object to be segmented
   * @param startFrame Current frame
   * @return SnakeHandler object that is also stored in Nest
   */
  public SnakeHandler addHandler(final Roi r, int startFrame) {
    SnakeHandler sh;
    try {
      sh = new SnakeHandler(r, startFrame, nextID);
      sHs.add(sh);
      nextID++;
      NSNAKES++;
      ALIVE++;
      BOA_.log("Added one cell, begining frame " + startFrame);
    } catch (Exception e) {
      BOA_.log("Added cell failed to initilise");
      LOGGER.debug(e.getMessage(), e);
      return null;
    }
    BOA_.log("Cells being tracked: " + NSNAKES);
    return sh;
  }

  /**
   * Gets SnakeHandler.
   * 
   * @param s Index of SnakeHandler to get.
   * @return SnakeHandler stored on index s. It may refer to SnakeHandler ID but may break when
   *         any cell has been deleted.
   * @see #getHandlerofId(int)
   */
  public SnakeHandler getHandler(int s) {
    return sHs.get(s);
  }

  /**
   * Return list of handlers stored in Nest.
   * 
   * @return unmodifiable list of handlers.
   */
  public List<SnakeHandler> getHandlers() {
    return Collections.unmodifiableList(sHs);
  }

  /**
   * Get SnakeHandler of given id.
   * 
   * @param id ID of SnakeHandler to find in Nest.
   * @return SnakeHandler with demanded ID. Throw exception if not found.
   * @see #getHandler(int)
   */
  public SnakeHandler getHandlerofId(int id) {
    int ret = -1;
    for (int i = 0; i < sHs.size(); i++) {
      if (sHs.get(i) != null && sHs.get(i).getID() == id) {
        ret = i;
        break;
      }
    }
    if (ret < 0) {
      throw new IllegalArgumentException("SnakeHandler of index " + id + " not found in nest");
    } else {
      return getHandler(ret);
    }
  }

  /**
   * Write all Snakes to file.
   * 
   * <p>File names are deducted in called functions.
   * 
   * @return true if write operation has been successful
   * @throws IOException when the file exists but is a directory rather than a regular file, does
   *         not exist but cannot be created, or cannot be opened for any other reason
   */
  public boolean writeSnakes() throws IOException {
    Iterator<SnakeHandler> shitr = sHs.iterator();
    ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
    SnakeHandler sh;
    while (shitr.hasNext()) {
      sh = (SnakeHandler) shitr.next(); // get SnakeHandler from Nest
      sh.findLastFrame(); // find its last frame (frame with valid contour)
      if (sh.getStartFrame() > sh.getEndFrame()) {
        IJ.error("Snake " + sh.getID() + " not written as its empty. Deleting it.");
        toRemove.add(sh);
        continue;
      }
      if (!sh.writeSnakes()) {
        return false;
      }
    }
    // removing from list (after iterator based loop)
    for (int i = 0; i < toRemove.size(); i++) {
      removeHandler(toRemove.get(i));
    }
    return true;
  }

  /**
   * Remove SnakeHandler from Nest.
   * 
   * @param sh handler to remove.
   */
  public void kill(final SnakeHandler sh) {
    sh.kill();
    ALIVE--;
  }

  /**
   * Make all Snakes live in nest.
   */
  public void reviveNest() {
    Iterator<SnakeHandler> shitr = sHs.iterator();
    while (shitr.hasNext()) {
      SnakeHandler sh = (SnakeHandler) shitr.next();
      sh.revive();
    }
    ALIVE = NSNAKES;
  }

  /**
   * Check if Nest is empty.
   * 
   * @return true if Nest is empty
   */
  public boolean isVacant() {
    if (NSNAKES == 0) {
      return true;
    }
    return false;
  }

  /**
   * Check if there is live Snake in Nest.
   * 
   * @return true if all snakes in Nest are dead
   */
  public boolean allDead() {
    if (ALIVE == 0 || NSNAKES == 0) {
      return true;
    }
    return false;
  }

  /**
   * Write <i>stQP</i> file using current Snakes
   * 
   * <p><b>Warning</b>
   * 
   * <p>It can set current slice in ImagePlus (modifies the object state).
   * 
   * @param oi instance of current ImagePlus (required by CellStat that extends
   *        ij.measure.Measurements
   * @param saveFile if true stQP file is saved in disk, false stats are evaluated only and
   *        returned
   * @return CellStat objects with calculated statistics for every cell.
   */
  public List<CellStatsEval> analyse(final ImagePlus oi, boolean saveFile) {
    OutlineHandler outputH;
    SnakeHandler sh;
    ArrayList<CellStatsEval> ret = new ArrayList<>();
    Iterator<SnakeHandler> shitr = sHs.iterator();
    while (shitr.hasNext()) {
      sh = (SnakeHandler) shitr.next();

      File statsFile;
      if (saveFile == true) { // compatibility with old (#263), reread snakes from snQP
        outputH = new OutlineHandler(sh);
        statsFile = new File(BOA_.qState.boap.deductStatsFileName(sh.getID()));
      } else { // new approach use conversion constructor
        statsFile = null;
        outputH = new OutlineHandler(sh);
      }
      CellStatsEval tmp = new CellStatsEval(outputH, oi, statsFile,
              BOA_.qState.boap.getImageScale(), BOA_.qState.boap.getImageFrameInterval());
      ret.add(tmp);
    }
    return ret;
  }

  /**
   * Reset live snakes to ROI's.
   */
  public void resetNest() {
    reviveNest();
    Iterator<SnakeHandler> shitr = sHs.iterator();
    ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
    while (shitr.hasNext()) {
      SnakeHandler sh = (SnakeHandler) shitr.next();
      try {
        sh.reset();
      } catch (Exception e) {
        LOGGER.error("Could not reset snake " + e.getMessage(), e);
        BOA_.log("Could not reset snake " + sh.getID());
        BOA_.log("Removing snake " + sh.getID());
        // collect handler to remove. It will be removed later to avoid list modification in
        // iterator (#186)
        toRemove.add(sh);
      }
    }
    // removing from list (after iterator based loop)
    for (int i = 0; i < toRemove.size(); i++) {
      removeHandler(toRemove.get(i));
    }
  }

  /**
   * Remove {@link SnakeHandler} from Nest.
   * 
   * @param sh SnakeHandler to remove.
   */
  public void removeHandler(final SnakeHandler sh) {
    if (sh.isLive()) {
      ALIVE--;
    }
    sHs.remove(sh);
    NSNAKES--;
  }

  /**
   * Remove all handlers from Nest. Make Nest empty.
   */
  public void cleanNest() {
    sHs.clear();
    NSNAKES = 0;
    ALIVE = 0;
    nextID = 0;
  }

  /**
   * Get NEst size.
   * 
   * @return Get number of SnakeHandlers (snakes) in nest
   */
  public int size() {
    return NSNAKES;
  }

  /**
   * Prepare for segmentation from frame f.
   * 
   * @param f current frame under segmentation
   */
  void resetForFrame(int f) {
    reviveNest();
    Iterator<SnakeHandler> shitr = sHs.iterator();
    ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
    while (shitr.hasNext()) {
      SnakeHandler sh = (SnakeHandler) shitr.next();
      try {
        if (f <= sh.getStartFrame()) {
          // BOA_.log("Reset snake " + sH.getID() + " as Roi");
          sh.reset();
        } else {
          // BOA_.log("Reset snake " + sH.getID() + " as prev snake");
          sh.resetForFrame(f);
        }
      } catch (Exception e) {
        LOGGER.debug("Could not reset snake " + e.getMessage(), e);
        BOA_.log("Could not reset snake " + sh.getID());
        BOA_.log("Removing snake " + sh.getID());
        // collect handler to remove. It will be removed later to avoid list modification in
        // iterator (#186)
        toRemove.add(sh);
      }
    }
    // removing from list (after iterator based loop)
    for (int i = 0; i < toRemove.size(); i++) {
      removeHandler(toRemove.get(i));
    }
  }

  /**
   * Get list of snakes (its IDs) that are on frame frame.
   * 
   * @param frame Frame find snakes in
   * @return List of Snake id on frame
   */
  public List<Integer> getSnakesforFrame(int frame) {
    ArrayList<Integer> ret = new ArrayList<Integer>();
    Iterator<SnakeHandler> shiter = sHs.iterator();
    while (shiter.hasNext()) { // over whole nest
      SnakeHandler sh = shiter.next(); // for every SnakeHandler
      if (sh.getStartFrame() > frame || sh.getEndFrame() < frame) {
        continue; // no snake in frame
      }
      if (sh.isStoredAt(frame)) { // if limits are ok check if this particular snake exist
        // it is not deleted by user on this particular frame after successful creating as
        // series of Snakes
        Snake s = sh.getStoredSnake(frame);
        ret.add(s.getSnakeID()); // if yes get its id
      }
    }
    return ret;
  }

  /**
   * Count the snakes that exist at, or after, frame.
   * 
   * @param frame frame
   * @return number of snakes
   */
  int nbSnakesAt(int frame) {
    int n = 0;
    for (int i = 0; i < NSNAKES; i++) {
      if (sHs.get(i).getStartFrame() >= frame) {
        n++;
      }
    }
    return n;
  }

  /**
   * Store OutlineHandler as finalSnake.
   * 
   * <p>Use {@link com.github.celldynamics.quimp.SnakeHandler#copyFromFinalToSeg()} to populate
   * snake
   * over
   * segSnakes.
   * 
   * <p>Conversion to Snakes is through Rois
   * 
   * @param oh OutlineHandler to convert from.
   */
  public void addOutlinehandler(final OutlineHandler oh) {
    SnakeHandler sh = addHandler(oh.indexGetOutline(0).asFloatRoi(), oh.getStartFrame());

    Outline o;
    for (int i = oh.getStartFrame(); i <= oh.getEndFrame(); i++) {
      o = oh.getOutline(i);
      sh.storeRoi((PolygonRoi) o.asFloatRoi(), i);
    }
    sh.copyFromFinalToSeg();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
    Iterator<SnakeHandler> shitr = sHs.iterator();
    ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
    SnakeHandler sh;
    // sanity operation - delete defective snakes
    while (shitr.hasNext()) {
      sh = (SnakeHandler) shitr.next(); // get SnakeHandler from Nest
      sh.findLastFrame(); // find its last frame (frame with valid contour)
      if (sh.getStartFrame() > sh.getEndFrame()) {
        IJ.error("Snake " + sh.getID() + " not written as its empty. Deleting it.");
        toRemove.add(sh);
        continue;
      }
      sh.beforeSerialize();
    }
    // removing from list (after iterator based loop)
    for (int i = 0; i < toRemove.size(); i++) {
      removeHandler(toRemove.get(i));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    Iterator<SnakeHandler> shitr = sHs.iterator();
    SnakeHandler sh;
    while (shitr.hasNext()) {
      sh = (SnakeHandler) shitr.next(); // get SnakeHandler from Nest
      sh.afterSerialize();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Nest [sHs=" + sHs + ", NSNAKES=" + NSNAKES + ", ALIVE=" + ALIVE + ", nextID=" + nextID
            + "]";
  }
}