package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;

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
     * SnakeHandlers get subsequent IDs therefore index in this array matches SnakeHandler's ID.
     * This relation can be broken when any cell is deleted using
     * {@link #removeHandler(SnakeHandler)} method that simply removes it from array shifting other
     * objects down.
     * 
     * This array should be accessed by {@link #getHandler(int)} when one needs access handler on
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

    public Nest() {
        NSNAKES = 0;
        ALIVE = 0;
        nextID = 0;
        sHs = new ArrayList<SnakeHandler>();
    }

    /**
     * Convert array of SegmentedShapeRoi to SnakeHandlers.
     * 
     * Conversion within one SnakeHandler is stopped when there is defective Snake.
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
     * Add ROI objects in Nest Snakes are stored in Nest object in form of SnakeHandler objects kept
     * in \c ArrayList<SnakeHandler> \c sHs field.
     * 
     * @param r ROI object that contain image object to be segmented
     * @param startFrame Current frame
     * @return SnakeHandler object that is also stored in Nest
     */
    public SnakeHandler addHandler(final Roi r, int startFrame) {
        SnakeHandler sH;
        try {
            sH = new SnakeHandler(r, startFrame, nextID);
            sHs.add(sH);
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
        return sH;
    }

    /**
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
     * 
     * @param id ID of SnakeHandler to find in Nest.
     * @return SnakeHandler with demanded ID. Throw exception if not found.
     * @see #getHandler(int)
     */
    public SnakeHandler getHandlerofId(int id) {
        int ret = -1;
        for (int i = 0; i < sHs.size(); i++)
            if (sHs.get(i) != null && sHs.get(i).getID() == id) {
                ret = i;
                break;
            }
        if (ret < 0)
            throw new IllegalArgumentException(
                    "SnakeHandler of index " + id + " not found in nest");
        else
            return getHandler(ret);
    }

    /**
     * Write all Snakes to file.
     * 
     * File names are deducted in called functions.
     * 
     * @return \c true if write operation has been successful
     * @throws IOException when the file exists but is a directory rather than a regular file, does
     *         not exist but cannot be created, or cannot be opened for any other reason
     */
    public boolean writeSnakes() throws IOException {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.findLastFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartFrame() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                toRemove.add(sH);
                continue;
            }
            if (!sH.writeSnakes()) {
                return false;
            }
        }
        // removing from list (after iterator based loop)
        for (int i = 0; i < toRemove.size(); i++)
            removeHandler(toRemove.get(i));
        return true;
    }

    public void kill(final SnakeHandler sH) {
        sH.kill();
        ALIVE--;
    }

    public void reviveNest() {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            sH.revive();
        }
        ALIVE = NSNAKES;
    }

    public boolean isVacant() {
        if (NSNAKES == 0) {
            return true;
        }
        return false;
    }

    public boolean allDead() {
        if (ALIVE == 0 || NSNAKES == 0) {
            return true;
        }
        return false;
    }

    /**
     * Write <i>stQP</i> file using current Snakes
     * 
     * <p>
     * <b>Warning</b>
     * <p>
     * It can set current slice in ImagePlus (modifies the object state).
     * 
     * @param oi instance of current ImagePlus (required by CellStat that extends
     *        ij.measure.Measurements
     * @return CellStat objects with calculated statistics for every cell.
     */
    public List<CellStatsEval> analyse(final ImagePlus oi) {
        OutlineHandler outputH;
        SnakeHandler sH;
        ArrayList<CellStatsEval> ret = new ArrayList<>();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        try {
            while (sHitr.hasNext()) {
                sH = (SnakeHandler) sHitr.next();

                File pFile = new File(BOA_.qState.boap.deductParamFileName(sH.getID()));
                QParams newQp = new QParams(pFile);
                newQp.readParams();
                outputH = new OutlineHandler(newQp);

                File statsFile = new File(BOA_.qState.boap.deductStatsFileName(sH.getID()));
                CellStatsEval tmp = new CellStatsEval(outputH, oi, statsFile,
                        BOA_.qState.boap.getImageScale(), BOA_.qState.boap.getImageFrameInterval());
                ret.add(tmp);
            }
        } catch (QuimpException e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error(e.getMessage());
        }
        return ret;
    }

    public void resetNest() {
        // Rset live snakes to ROI's
        reviveNest();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                sH.reset();
            } catch (Exception e) {
                LOGGER.error("Could not reset snake " + e.getMessage(), e);
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removing snake " + sH.getID());
                // collect handler to remove. It will be removed later to avoid list modification in
                // iterator (#186)
                toRemove.add(sH);
            }
        }
        // removing from list (after iterator based loop)
        for (int i = 0; i < toRemove.size(); i++)
            removeHandler(toRemove.get(i));
    }

    public void removeHandler(final SnakeHandler sH) {
        if (sH.isLive()) {
            ALIVE--;
        }
        sHs.remove(sH);
        NSNAKES--;
    }

    /**
     * Remove all handlers from Nest. Make Nest empty
     */
    public void cleanNest() {
        sHs.clear();
        NSNAKES = 0;
        ALIVE = 0;
        nextID = 0;
    }

    /**
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
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                if (f <= sH.getStartFrame()) {
                    // BOA_.log("Reset snake " + sH.getID() + " as Roi");
                    sH.reset();
                } else {
                    // BOA_.log("Reset snake " + sH.getID() + " as prev snake");
                    sH.resetForFrame(f);
                }
            } catch (Exception e) {
                LOGGER.debug("Could not reset snake " + e.getMessage(), e);
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removing snake " + sH.getID());
                // collect handler to remove. It will be removed later to avoid list modification in
                // iterator (#186)
                toRemove.add(sH);
            }
        }
        // removing from list (after iterator based loop)
        for (int i = 0; i < toRemove.size(); i++)
            removeHandler(toRemove.get(i));
    }

    /**
     * Get list of snakes (its IDs) that are on frame frame.
     * 
     * @param frame Frame find snakes in
     * @return List of Snake id on \c frame
     */
    public List<Integer> getSnakesforFrame(int frame) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        Iterator<SnakeHandler> sHiter = sHs.iterator();
        while (sHiter.hasNext()) { // over whole nest
            SnakeHandler sH = sHiter.next(); // for every SnakeHandler
            if (sH.getStartFrame() > frame || sH.getEndFrame() < frame) // check its limits
                continue; // no snake in frame
            if (sH.isStoredAt(frame)) { // if limits are ok check if this particular snake exist
                // it is not deleted by user on this particular frame after successful creating as
                // series of Snakes
                Snake s = sH.getStoredSnake(frame);
                ret.add(s.getSnakeID()); // if yes get its id
            }
        }
        return ret;
    }

    /**
     * Count the snakes that exist at, or after, frame.
     * 
     * @param frame
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
     * Use {@link uk.ac.warwick.wsbc.QuimP.SnakeHandler#copyFromFinalToSeg()} to populate snake over
     * segSnakes.
     * 
     * @param oH
     */
    void addOutlinehandler(final OutlineHandler oH) {
        SnakeHandler sH = addHandler(oH.indexGetOutline(0).asFloatRoi(), oH.getStartFrame());

        Outline o;
        for (int i = oH.getStartFrame(); i <= oH.getEndFrame(); i++) {
            o = oH.getOutline(i);
            sH.storeRoi((PolygonRoi) o.asFloatRoi(), i);
        }
        sH.copyFromFinalToSeg();
    }

    @Override
    public void beforeSerialize() {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        ArrayList<SnakeHandler> toRemove = new ArrayList<>(); // will keep handler to remove
        SnakeHandler sH;
        // sanity operation - delete defective snakes
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.findLastFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartFrame() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                toRemove.add(sH);
                continue;
            }
            sH.beforeSerialize();
        }
        // removing from list (after iterator based loop)
        for (int i = 0; i < toRemove.size(); i++)
            removeHandler(toRemove.get(i));
    }

    @Override
    public void afterSerialize() throws Exception {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.afterSerialize();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Nest [sHs=" + sHs + ", NSNAKES=" + NSNAKES + ", ALIVE=" + ALIVE + ", nextID="
                + nextID + "]";
    }
}