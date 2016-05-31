package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Represents collection of Snakes
 * 
 * @author rtyson
 * @author p.baniukiewicz
 * @date 4 May 2016
 */
class Nest implements IQuimpSerialize {
    private ArrayList<SnakeHandler> sHs;
    private int NSNAKES; //!< Number of stored snakes in nest
    private int ALIVE;
    private int nextID; // handler ID's

    public Nest() {
        NSNAKES = 0;
        ALIVE = 0;
        nextID = 0;
        sHs = new ArrayList<SnakeHandler>();
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
                BOA_.log("A snake failed to initilise");
            }
        }
        BOA_.log("Added " + roiArray.length + " cells at frame " + startFrame);
        BOA_.log("Cells being tracked: " + NSNAKES);
    }

    /**
     * Add ROI objects in Nest Snakes are stored in Nest object in form of
     * SnakeHandler objects kept in \c ArrayList<SnakeHandler> \c sHs field.
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
            return null;
        }
        BOA_.log("Cells being tracked: " + NSNAKES);
        return sH;
    }

    public SnakeHandler getHandler(int s) {
        return sHs.get(s);
    }

    /**
     * Write all Snakes to file.
     * 
     * File names are deducted in called functions.
     * 
     * @return \c true if write operation has been successful
     * @throws IOException when the file exists but is a directory rather than a regular file, 
     * does not exist but cannot be created, or cannot be opened for any other reason
     */
    public boolean writeSnakes() throws IOException {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.setEndFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartframe() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                removeHandler(sH);
                continue;
            }
            if (!sH.writeSnakes()) {
                return false;
            }
        }
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
     * Write \a stQP file using current Snakes
     * 
     * @param oi instance of current ImagePlus (required by CellStat that extends 
     * ij.measure.Measurements
     * @remarks It can set current slice in ImagePlus (modifies the object state)
     */
    public void analyse(final ImagePlus oi) {
        OutlineHandler outputH;
        SnakeHandler sH;
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next();

            File pFile = new File(BOA_.qState.boap.outFile.getParent(),
                    BOA_.qState.boap.fileName + "_" + sH.getID() + ".paQP");
            QParams newQp = new QParams(pFile);
            newQp.readParams();
            outputH = new OutlineHandler(newQp);

            File statsFile = new File(BOA_.qState.boap.outFile.getParent() + File.separator
                    + BOA_.qState.boap.fileName + "_" + sH.getID() + ".stQP.csv");
            new CellStat(outputH, oi, statsFile, BOA_.qState.boap.getImageScale(),
                    BOA_.qState.boap.getImageFrameInterval());
        }
    }

    public void resetNest() {
        // Rset live snakes to ROI's
        reviveNest();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                sH.reset();
            } catch (Exception e) {
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removeing snake " + sH.getID());
                removeHandler(sH);
            }
        }
    }

    public void removeHandler(final SnakeHandler sH) {
        if (sH.isLive()) {
            ALIVE--;
        }
        sHs.remove(sH);
        NSNAKES--;
    }

    /**
     * @return Get number of SnakeHandlers (snakes) in nest
     */
    int size() {
        return NSNAKES;
    }

    /**
     * Prepare for segmentation from frame \c f
     * 
     * @param f current frame under segmentation
     */
    void resetForFrame(int f) {
        reviveNest();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        // BOA_.log("Reseting for frame " + f);
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                if (f <= sH.getStartframe()) {
                    // BOA_.log("Reset snake " + sH.getID() + " as Roi");
                    sH.reset();
                } else {
                    // BOA_.log("Reset snake " + sH.getID() + " as prev snake");
                    sH.resetForFrame(f);
                }
            } catch (Exception e) {
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removeing snake " + sH.getID());
                removeHandler(sH);
            }
        }
    }

    /**
     * Get list of snakes that are on frame  \c frame
     * 
     * @param frame Frame find snakes in
     * @return List of Snake id on \c frame
     */
    List<Integer> getSnakesforFrame(int frame) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        Iterator<SnakeHandler> sHiter = sHs.iterator();
        while (sHiter.hasNext()) { // over whole nest
            SnakeHandler sH = sHiter.next(); // for every SnakeHandler
            if (sH.getStartframe() > frame || sH.getEndFrame() < frame) // check its limits
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
     * Count the snakes that exist at, or after, frame
     * 
     * @param frame
     * @return
     */
    int nbSnakesAt(int frame) {
        int n = 0;
        for (int i = 0; i < NSNAKES; i++) {
            if (sHs.get(i).getStartframe() >= frame) {
                n++;
            }
        }
        return n;
    }

    // TODO use new feature of conversion between snakes and outlines
    void addOutlinehandler(final OutlineHandler oH) {
        SnakeHandler sH = addHandler(oH.indexGetOutline(0).asFloatRoi(), oH.getStartFrame());

        Outline o;
        for (int i = oH.getStartFrame(); i <= oH.getEndFrame(); i++) {
            o = oH.getOutline(i);
            sH.storeRoi((PolygonRoi) o.asFloatRoi(), i);
        }
    }

    @Override
    public void beforeSerialize() {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.setEndFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartframe() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                removeHandler(sH);
                continue;
            }
            sH.beforeSerialize();
        }
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
}