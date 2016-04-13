package uk.ac.warwick.wsbc.QuimP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.SaveDialog;

/**
 * Store all the snakes computed for one cell across frames and it is responsible
 * for writing them to file.
 * 
 * @author rtyson
 *
 */
public class SnakeHandler {
    private static final Logger LOGGER = LogManager.getLogger(SnakeHandler.class.getName());
    private Roi roi; // inital ROI
    private int startFrame;
    private int endFrame;
    private Snake liveSnake;
    private Snake[] finalSnakes; /*!< series of snakes, result of cell segm. and plugin processing*/
    private Snake[] segSnakes; /*!< series of snakes, result of cell segmentation only  */
    private int ID;

    /**
     * Constructor of SnakeHandler. Stores ROI with object for segmentation
     * 
     * @param r ROI with selected object
     * @param frame Current frame for which the ROI is taken
     * @param id Unique Snake ID controlled by Nest object
     * @throws Exception
     */
    public SnakeHandler(final Roi r, int frame, int id) throws Exception {
        startFrame = frame;
        endFrame = BOA_.boap.FRAMES;
        roi = r;
        // snakes array keeps snakes across frames from current to end. Current
        // is that one for which cell has been added
        finalSnakes = new Snake[BOA_.boap.FRAMES - startFrame + 1]; // stored snakes
        segSnakes = new Snake[BOA_.boap.FRAMES - startFrame + 1]; // stored snakes
        ID = id;
        liveSnake = new Snake(r, ID, false);
        backupLiveSnake(frame);
    }

    /**
     * Make copy of \c liveSnake into \c final \c snakes array
     * 
     * @param frame Frame for which \c liveSnake will be copied to
     * @throws BoaException
     */
    public void storeLiveSnake(int frame) throws BoaException {
        // BOA_.log("Store snake " + ID + " at frame " + frame);
        finalSnakes[frame - startFrame] = null; // delete at current frame

        finalSnakes[frame - startFrame] = new Snake(liveSnake, ID);
    }

    /**
     * Stores \c liveSnake (currently processed) in \c segSnakes array. 
     * 
     * For one SnakeHandler there is only one \c liveSnake which is processed "in place" by
     * segmentation methods. It is virtually moved from frame to frame and copied to final snakes
     * after segmentation on current frame and processing by plugins. 
     * It must be backed up for every frame to make possible restoring  original snakes when 
     * active plugin has been deselected. 
     *  
     * @param frame current frame
     * @throws BoaException
     */
    public void backupLiveSnake(int frame) throws BoaException {

        LOGGER.debug("Stored live snake in frame " + frame + " ID " + ID);
        segSnakes[frame - startFrame] = null; // delete at current frame

        segSnakes[frame - startFrame] = new Snake(liveSnake, ID);
    }

    /**
     * Makes copy of \c snake and store it as final snake.
     * 
     * @param snake Snake to store
     * @param frame Frame for which \c liveSnake will be copied to
     * @throws BoaException
     */
    public void storeThisSnake(Snake snake, int frame) throws BoaException {
        // BOA_.log("Store snake " + ID + " at frame " + frame);
        finalSnakes[frame - startFrame] = null; // delete at current frame

        finalSnakes[frame - startFrame] = new Snake(snake, ID);

    }

    public boolean writeSnakes() throws Exception {

        String saveIn = BOA_.boap.orgFile.getParent();
        // System.out.println(boap.orgFile.getParent());
        // if (!boap.orgFile.exists()) {
        // BOA_.log("image is not saved to disk!");
        // saveIn = OpenDialog.getLastDirectory();
        // }

        if (!BOA_.boap.savedOne) {

            SaveDialog sd =
                    new SaveDialog("Save segmentation data...", saveIn, BOA_.boap.fileName, "");

            if (sd.getFileName() == null) {
                BOA_.log("Save canceled");
                return false;
            }
            BOA_.boap.outFile = new File(sd.getDirectory(), sd.getFileName() + "_" + ID + ".snQP");
            BOA_.boap.fileName = sd.getFileName();
            BOA_.boap.savedOne = true;
        } else {
            BOA_.boap.outFile = new File(BOA_.boap.outFile.getParent(),
                    BOA_.boap.fileName + "_" + ID + ".snQP");
        }

        PrintWriter pw = new PrintWriter(new FileWriter(BOA_.boap.outFile), true); // auto
        // flush

        pw.write("#QuimP11 Node data");
        pw.write("\n#Node Position\tX-coord\tY-coord\tOrigin\tG-Origin\tSpeed");
        pw.write("\tFluor_Ch1\tCh1_x\tCh1_y\tFluor_Ch2\tCh2_x\tCh2_y\tFluor_CH3\tCH3_x\tCh3_y\n#");

        Snake s;
        for (int i = startFrame; i <= endFrame; i++) {
            s = getStoredSnake(i);
            s.setPositions(); //
            pw.write("\n#Frame " + i);
            write(pw, i + 1, s.getNODES(), s.getHead());
        }
        pw.close();
        BOA_.boap.writeParams(ID, startFrame, endFrame);

        if (BOA_.boap.oldFormat) {
            writeOldFormats();
        }
        return true;
    }

    private void write(final PrintWriter pw, int frame, int NODES, Node n) {
        pw.print("\n" + NODES);

        do {
            // fluo values (x,y, itensity)
            pw.print("\n" + IJ.d2s(n.position, 6) + "\t" + IJ.d2s(n.getX(), 2) + "\t"
                    + IJ.d2s(n.getY(), 2) + "\t0\t0\t0" + "\t-2\t-2\t-2\t-2\t-2\t-2\t-2\t-2\t-2");
            n = n.getNext();
        } while (!n.isHead());

    }

    private void writeOldFormats() throws Exception {
        // create file to outpurt old format
        File OLD = new File(BOA_.boap.outFile.getParent(), BOA_.boap.fileName + ".dat");
        PrintWriter pw = new PrintWriter(new FileWriter(OLD), true); // auto
                                                                     // flush

        for (int i = 0; i < finalSnakes.length; i++) {
            if (finalSnakes[i] == null) {
                break;
            }
            if (i != 0) {
                pw.print("\n");
            } // no new line at top
            pw.print(finalSnakes[i].getNODES());

            Node n = finalSnakes[i].getHead();
            do {
                pw.print("\n" + IJ.d2s(n.getX(), 6));
                pw.print("\n" + IJ.d2s(n.getY(), 6));
                n = n.getNext();
            } while (!n.isHead());
        }
        pw.close();

        OLD = new File(BOA_.boap.outFile.getParent(), BOA_.boap.fileName + ".dat_tn");
        pw = new PrintWriter(new FileWriter(OLD), true); // auto flush

        for (int i = 0; i < finalSnakes.length; i++) {
            if (finalSnakes[i] == null) {
                break;
            }
            if (i != 0) {
                pw.print("\n");
            } // no new line at top
            pw.print(finalSnakes[i].getNODES());

            Node n = finalSnakes[i].getHead();
            do {
                pw.print("\n" + IJ.d2s(n.getX(), 6));
                pw.print("\n" + IJ.d2s(n.getY(), 6));
                pw.print("\n" + n.getTrackNum());
                n = n.getNext();
            } while (!n.isHead());
        }
        pw.close();

        OLD = new File(BOA_.boap.outFile.getParent(), BOA_.boap.fileName + ".dat1");
        pw = new PrintWriter(new FileWriter(OLD), true); // auto flush

        pw.print(IJ.d2s(BOA_.boap.NMAX, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.delta_t, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.max_iterations, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.getMin_dist(), 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.getMax_dist(), 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.blowup, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.sample_tan, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.sample_norm, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.vel_crit, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.f_central, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.f_contract, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.f_friction, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.segParam.f_image, 6) + "\n");
        pw.print(IJ.d2s(1.0, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.sensitivity, 6) + "\n");
        pw.print(IJ.d2s(BOA_.boap.cut_every, 6) + "\n");
        pw.print("100");

        pw.close();
    }

    public Snake getLiveSnake() {
        return liveSnake;
    }

    public Snake getBackupSnake(int f) {
        LOGGER.debug("Asked for backup snake at frame " + f + " ID " + ID);
        if (f - startFrame < 0) {
            LOGGER.warn("Tried to access negative frame store");
            return null;
        }
        return segSnakes[f - startFrame];
    }

    public Snake getStoredSnake(int f) {
        if (f - startFrame < 0) {
            BOA_.log("Tried to access negative frame store\n\tframe:" + f + "\n\tsnakeID:" + ID);
            return null;
        }
        // BOA_.log("Fetch stored snake " + ID + " frame " + f);
        return finalSnakes[f - startFrame];
    }

    boolean isStoredAt(int f) {
        if (f - startFrame < 0) {
            return false;
        } else if (finalSnakes[f - startFrame] == null) {
            return false;
        } else {
            return true;
        }
    }

    public int snakeReader(final File inFile) throws Exception {
        String thisLine;
        int N;
        int index;
        double x, y;
        Node head, n, prevn;
        int s = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(inFile));

            while ((thisLine = br.readLine()) != null) {
                index = 0;
                head = new Node(index); // dummy head node
                head.setHead(true);
                prevn = head;
                index++;

                N = (int) Tool.s2d(thisLine);

                for (int i = 0; i < N; i++) {
                    x = Tool.s2d(br.readLine());
                    y = Tool.s2d(br.readLine());

                    n = new Node(index);
                    n.setX(x);
                    n.setY(y);
                    index++;

                    prevn.setNext(n);
                    n.setPrev(prevn);

                    prevn = n;

                }
                // link tail to head
                prevn.setNext(head);
                head.setPrev(prevn);

                finalSnakes[s] = new Snake(head, N + 1, ID); // dont forget the head
                // node
                s++;
            } // end while
        } catch (IOException e) {
            System.err.println("Error: " + e);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        return 1;
    }

    public void revive() {
        liveSnake.alive = true;
    }

    public void kill() {
        liveSnake.alive = false;
    }

    public void reset() throws Exception {
        liveSnake = new Snake(roi, ID, false);
        // snakes = new Snake[boap.FRAMES - startFrame + 1]; // stored snakes
    }

    public int getID() {
        return ID;
    }

    public boolean isLive() {
        return liveSnake.alive;
    }

    void deleteStoreAt(int frame) {
        if (frame - startFrame < 0) {
            BOA_.log(
                    "Tried to delete negative frame store\n\tframe:" + frame + "\n\tsnakeID:" + ID);
        } else {
            finalSnakes[frame - startFrame] = null;
        }
    }

    void deleteStoreFrom(int frame) {
        for (int i = frame; i <= BOA_.boap.FRAMES; i++) {
            deleteStoreAt(i);
        }
    }

    void storeAt(final Snake s, int frame) {
        s.calcCentroid();
        if (frame - startFrame < 0) {
            BOA_.log("Tried to store at negative frame\n\tframe:" + frame + "\n\tsnakeID:" + ID);
        } else {
            // BOA_.log("Storing snake " + ID + " frame " + frame);
            finalSnakes[frame - startFrame] = s;
        }
    }

    int getStartframe() {
        return startFrame;
    }

    int getEndFrame() {
        return endFrame;
    }

    /**
     * Prepare current frame \c for segmentation
     * 
     * Create \c liveSnake using final snake stored in previous frame
     * 
     * @param f Current segmented frame
     */
    void resetForFrame(int f) {
        try {
            if (BOA_.boap.segParam.use_previous_snake) {
                // set to last segmentation ready for blowup
                liveSnake = new Snake((PolygonRoi) this.getStoredSnake(f - 1).asFloatRoi(), ID);
            } else {
                liveSnake = new Snake(roi, ID, false);
            }
        } catch (Exception e) {
            BOA_.log("Could not reset live snake form frame" + f);
        }
    }

    void storeRoi(final PolygonRoi r, int f) {
        try {
            Snake snake = new Snake(r, ID);
            snake.calcCentroid();
            this.deleteStoreAt(f);
            storeAt(snake, f);
            // BOA_.log("Storing ROI snake " + ID + " frame " + f);
        } catch (Exception e) {
            BOA_.log("Could not stor ROI");
            e.printStackTrace();
        }
    }

    void setEndFrame() {
        // find the first missing contour and set end frame to the previous one

        for (int i = startFrame; i <= BOA_.boap.FRAMES; i++) {
            if (!isStoredAt(i)) {
                endFrame = i - 1;
                return;
            }
        }
        endFrame = BOA_.boap.FRAMES;
    }
}