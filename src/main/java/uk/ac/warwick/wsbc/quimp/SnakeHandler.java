package uk.ac.warwick.wsbc.quimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * Store all the snakes computed for one cell across frames and it is responsible for writing them
 * to file.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 *
 */
public class SnakeHandler extends ShapeHandler<Snake> implements IQuimpSerialize {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SnakeHandler.class.getName());
  /**
   * initial ROI, not stored but rebuilt from snake on load
   */
  private transient Roi roi;
  /**
   * initial snake being currently processed
   */
  private Snake liveSnake;
  /**
   * series of snakes, result of cell segm. and plugin processing
   */
  private Snake[] finalSnakes;
  /**
   * series of snakes, result of cell segmentation only
   */
  private Snake[] segSnakes;
  /**
   * ID of Snakes stored in this SnakeHandler
   */
  private int ID;

  /**
   * Instantiates a new snake handler.
   */
  public SnakeHandler() {
    // endFrame = BOA_.qState.boap.FRAMES;
  }

  /**
   * Constructor of SnakeHandler. Stores ROI with object for segmentation.
   * 
   * @param r ROI with selected object
   * @param frame Current frame for which the ROI is taken
   * @param id Unique Snake ID controlled by Nest object
   * @throws Exception
   */
  public SnakeHandler(final Roi r, int frame, int id) throws Exception {
    this();
    startFrame = frame;
    endFrame = BOA_.qState.boap.getFRAMES();
    roi = r;
    // snakes array keeps snakes across frames from current to end. Current
    // is that one for which cell has been added
    finalSnakes = new Snake[BOA_.qState.boap.getFRAMES() - startFrame + 1]; // stored snakes
    segSnakes = new Snake[BOA_.qState.boap.getFRAMES() - startFrame + 1]; // stored snakes
    ID = id;
    liveSnake = new Snake(r, ID, false);
    backupLiveSnake(frame);
  }

  /**
   * Copy constructor. Create SnakeHandler from list of already prepared outlines.
   * 
   * For every frame it copies provided snake to all three arrays: finalSnakes, segSnakes,
   * liveSnake and sets first and last frame using data from SegmentedShapeRoi object
   * 
   * @param snakes List of outlines that will be propagated from first frame. First frame is wrote
   *        down in first element of this list
   * @param id Unique Snake ID controlled by Nest object
   * @throws BoaException
   * @see uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi
   */
  public SnakeHandler(List<SegmentedShapeRoi> snakes, int id) throws BoaException {
    this();
    startFrame = snakes.get(0).getFrame(); // get first frame from outline
    endFrame = BOA_.qState.boap.getFRAMES();
    finalSnakes = new Snake[BOA_.qState.boap.getFRAMES() - startFrame + 1]; // stored snakes
    segSnakes = new Snake[BOA_.qState.boap.getFRAMES() - startFrame + 1]; // stored snakes
    ID = id;
    roi = snakes.get(0); // set initial roi to first snake
    for (SegmentedShapeRoi sS : snakes) {
      liveSnake = new Snake(sS.getOutlineasPoints(), ID); // tmp for next two methods
      backupLiveSnake(sS.getFrame()); // fill segSnakes for frame
      storeLiveSnake(sS.getFrame()); // fill finalSnakes for frame
    }
    liveSnake = new Snake(snakes.get(0).getOutlineasPoints(), ID); // set live again for current
                                                                   // frame
    endFrame = snakes.get(snakes.size() - 1).getFrame(); // SegmentedShapeRoi contains number of
                                                         // frame that it came from. The are
                                                         // sorted as frames so last originates
                                                         // from last frame
    // LOGGER.trace("Added SnakeHandler: ID=" + id + " startFrame=" + startFrame + " endFrame="
    // + endFrame); // try toString
    LOGGER.debug("Added" + this.toString()); // try toString
  }

  /**
   * Make copy of \c liveSnake into \c final \c snakes array
   * 
   * @param frame Frame for which \c liveSnake will be copied to
   */
  public void storeLiveSnake(int frame) {
    finalSnakes[frame - startFrame] = null; // delete at current frame
    finalSnakes[frame - startFrame] = new Snake(liveSnake, ID);
  }

  /**
   * Stores \c liveSnake (currently processed) in \c segSnakes array.
   * 
   * For one SnakeHandler there is only one \c liveSnake which is processed "in place" by
   * segmentation methods. It is virtually moved from frame to frame and copied to final snakes
   * after segmentation on current frame and processing by plugins. It must be backed up for every
   * frame to make possible restoring original snakes when active plugin has been deselected.
   * 
   * @param frame current frame
   */
  public void backupLiveSnake(int frame) {

    LOGGER.trace("Stored live snake in frame " + frame + " ID " + ID);
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

  /**
   * Makes copy of \c snake and store it as segmented snake.
   * 
   * @param snake Snake to store
   * @param frame Frame for which \c liveSnake will be copied to
   */
  public void backupThisSnake(final Snake snake, int frame) {
    // BOA_.log("Store snake " + ID + " at frame " + frame);
    segSnakes[frame - startFrame] = null; // delete at current frame

    segSnakes[frame - startFrame] = new Snake(snake, ID);
  }

  /**
   * Copy all segSnakes to finalSnakes
   */
  public void copyFromSegToFinal() {
    for (int i = 0; i < segSnakes.length; i++) {
      if (segSnakes[i] == null)
        finalSnakes[i] = null;
      else
        finalSnakes[i] = new Snake(segSnakes[i]);
    }
  }

  /**
   * Copy all finalSnakes to segSnakes
   */
  public void copyFromFinalToSeg() {
    for (int i = 0; i < finalSnakes.length; i++) {
      if (finalSnakes[i] == null)
        segSnakes[i] = null;
      else
        segSnakes[i] = new Snake(finalSnakes[i]);
    }
  }

  /**
   * Write Snakes from this handler to \a *.snPQ file
   * 
   * Display also user interface
   * 
   * @return \c true if save has been successful or \c false if user canceled it
   * @throws IOException when the file exists but is a directory rather than a regular file, does
   *         not exist but cannot be created, or cannot be opened for any other reason
   */
  public boolean writeSnakes() throws IOException {
    // String saveIn = BOA_.qState.boap.getOrgFile().getParent();
    // System.out.println(boap.orgFile.getParent());
    // if (!boap.orgFile.exists()) {
    // BOA_.log("image is not saved to disk!");
    // saveIn = OpenDialog.getLastDirectory();
    // }

    // if (!BOA_.qState.boap.savedOne) {
    //
    // SaveDialog sd = new SaveDialog("Save segmentation data...", saveIn,
    // BOA_.qState.boap.fileName, "");
    //
    // if (sd.getFileName() == null) {
    // BOA_.log("Save canceled");
    // return false;
    // }
    // BOA_.qState.boap.outFile =
    // new File(sd.getDirectory(), sd.getFileName() + "_" + ID + ".snQP");
    // BOA_.qState.boap.fileName = sd.getFileName();
    // BOA_.qState.boap.savedOne = true;
    // } else {
    // BOA_.qState.boap.outFile = new File(BOA_.qState.boap.outFile.getParent(),
    // BOA_.qState.boap.fileName + "_" + ID + ".snQP");
    // }
    String snakeOutFile = BOA_.qState.boap.deductSnakeFileName(ID);
    LOGGER.debug("Write " + FileExtensions.snakeFileExt + " at: " + snakeOutFile);
    PrintWriter pw = new PrintWriter(new FileWriter(snakeOutFile), true); // auto flush
    pw.write("#QuimP11 Node data");
    pw.write("\n#Node Position\tX-coord\tY-coord\tOrigin\tG-Origin\tSpeed");
    pw.write("\tFluor_Ch1\tCh1_x\tCh1_y\tFluor_Ch2\tCh2_x\tCh2_y\tFluor_CH3\tCH3_x\tCh3_y\n#");

    Snake s;
    for (int i = startFrame; i <= endFrame; i++) {
      s = getStoredSnake(i);
      s.setPositions(); // calculate position field
      pw.write("\n#Frame " + i);
      write(pw, i + 1, s.getNumNodes(), s.getHead());
    }
    pw.close();
    BOA_.qState.writeParams(ID, startFrame, endFrame);

    if (BOA_.qState.boap.oldFormat) {
      writeOldFormats();
    }
    return true;
  }

  /**
   * Write one Node to disk (one line in \a snPQ file)
   * 
   * @param pw
   * @param frame
   * @param NODES
   * @param n
   */
  private void write(final PrintWriter pw, int frame, int NODES, Node n) {
    pw.print("\n" + NODES);

    do {
      // fluo values (x,y, itensity)
      pw.print("\n" + IJ.d2s(n.position, 6) + "\t" + IJ.d2s(n.getX(), 2) + "\t"
              + IJ.d2s(n.getY(), 2) + "\t0\t0\t0" + "\t-2\t-2\t-2\t-2\t-2\t-2\t-2\t-2\t-2");
      n = n.getNext();
    } while (!n.isHead());

  }

  private void writeOldFormats() throws IOException {
    // create file to outpurt old format
    File OLD = new File(BOA_.qState.boap.getOutputFileCore().getParent(),
            BOA_.qState.boap.getFileName() + ".dat");
    PrintWriter pw = new PrintWriter(new FileWriter(OLD), true); // auto
                                                                 // flush

    for (int i = 0; i < finalSnakes.length; i++) {
      if (finalSnakes[i] == null) {
        break;
      }
      if (i != 0) {
        pw.print("\n");
      } // no new line at top
      pw.print(finalSnakes[i].getNumNodes());

      Node n = finalSnakes[i].getHead();
      do {
        pw.print("\n" + IJ.d2s(n.getX(), 6));
        pw.print("\n" + IJ.d2s(n.getY(), 6));
        n = n.getNext();
      } while (!n.isHead());
    }
    pw.close();

    OLD = new File(BOA_.qState.boap.getOutputFileCore().getParent(),
            BOA_.qState.boap.getFileName() + ".dat_tn");
    pw = new PrintWriter(new FileWriter(OLD), true); // auto flush

    for (int i = 0; i < finalSnakes.length; i++) {
      if (finalSnakes[i] == null) {
        break;
      }
      if (i != 0) {
        pw.print("\n");
      } // no new line at top
      pw.print(finalSnakes[i].getNumNodes());

      Node n = finalSnakes[i].getHead();
      do {
        pw.print("\n" + IJ.d2s(n.getX(), 6));
        pw.print("\n" + IJ.d2s(n.getY(), 6));
        pw.print("\n" + n.getTrackNum());
        n = n.getNext();
      } while (!n.isHead());
    }
    pw.close();

    OLD = new File(BOA_.qState.boap.getOutputFileCore().getParent(),
            BOA_.qState.boap.getFileName() + ".dat1");
    pw = new PrintWriter(new FileWriter(OLD), true); // auto flush

    pw.print(IJ.d2s(BOA_.qState.boap.NMAX, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.boap.delta_t, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.max_iterations, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.getMin_dist(), 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.getMax_dist(), 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.blowup, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.sample_tan, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.sample_norm, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.vel_crit, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.f_central, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.f_contract, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.boap.f_friction, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.segParam.f_image, 6) + "\n");
    pw.print(IJ.d2s(1.0, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.boap.sensitivity, 6) + "\n");
    pw.print(IJ.d2s(BOA_.qState.boap.cut_every, 6) + "\n");
    pw.print("100");

    pw.close();
  }

  /**
   * Gets the live snake.
   *
   * @return the live snake
   */
  public Snake getLiveSnake() {
    return liveSnake;
  }

  /**
   * Gets the backup snake.
   *
   * @param f the f
   * @return the backup snake
   */
  public Snake getBackupSnake(int f) {
    LOGGER.trace("Asked for backup snake at frame " + f + " ID " + ID);
    if (f - startFrame < 0) {
      LOGGER.warn("Tried to access negative frame store");
      return null;
    }
    return segSnakes[f - startFrame];
  }

  /**
   * Return final Snake (after plugins) stored for frame \c f
   * 
   * @param f frame
   * @return Snake at frame \c f or \c null
   */
  public Snake getStoredSnake(int f) {
    if (f - startFrame < 0) {
      LOGGER.warn("Tried to access negative frame store\n\tframe:" + f + "\n\tsnakeID:" + ID);
      return null;
    }
    return finalSnakes[f - startFrame];
  }

  /**
   * Validate whether there is any Snake at frame \c f
   * 
   * @param f frame to validate
   * @return \c true if \c finalSnakes array contains valid Snake at frame \c f
   */
  boolean isStoredAt(int f) {
    if (f - startFrame < 0)
      return false;
    else if (f - startFrame >= finalSnakes.length)
      return false;
    else if (finalSnakes[f - startFrame] == null)
      return false;
    else
      return true;

  }

  /**
   * Read Snake from file
   *
   * May not be compatible wit old version due to changes in Snake constructor.
   *
   * @param inFile
   * @return value of 1
   * @throws Exception
   * @see <a href="link">uk.ac.warwick.wsbc.quimp.OutlineHandler.readOutlines(File)</a>
   */
  @Deprecated
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

        N = (int) QuimpToolsCollection.s2d(thisLine);

        for (int i = 0; i < N; i++) {
          x = QuimpToolsCollection.s2d(br.readLine());
          y = QuimpToolsCollection.s2d(br.readLine());

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
        finalSnakes[s].removeNode(head); // due to compatibility with code above.
                                         // old versions made copies of list
                                         // WARN potential uncompatibility with old code.
                                         // old constructor made copy of this list and
                                         // deleted first dummy node. Now it just covers
                                         // this list
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

  /**
   * Revive.
   */
  public void revive() {
    liveSnake.alive = true;
  }

  /**
   * Kill.
   */
  public void kill() {
    liveSnake.alive = false;
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  public void reset() throws Exception {
    liveSnake = new Snake(roi, ID, false);
    // snakes = new Snake[boap.FRAMES - startFrame + 1]; // stored snakes
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getID() {
    return ID;
  }

  /**
   * Checks if is live.
   *
   * @return true, if is live
   */
  public boolean isLive() {
    return liveSnake.alive;
  }

  /**
   * Delete store at.
   *
   * @param frame the frame
   */
  void deleteStoreAt(int frame) {
    if (frame - startFrame < 0) {
      BOA_.log("Tried to delete negative frame store\n\tframe:" + frame + "\n\tsnakeID:" + ID);
    } else {
      finalSnakes[frame - startFrame] = null;
      segSnakes[frame - startFrame] = null;
    }
  }

  /**
   * Delete store from.
   *
   * @param frame the frame
   */
  void deleteStoreFrom(int frame) {
    for (int i = frame; i <= BOA_.qState.boap.getFRAMES(); i++) {
      deleteStoreAt(i);
    }
  }

  /**
   * Store at.
   *
   * @param s the s
   * @param frame the frame
   */
  void storeAt(final Snake s, int frame) {
    s.calcCentroid();
    if (frame - startFrame < 0) {
      BOA_.log("Tried to store at negative frame\n\tframe:" + frame + "\n\tsnakeID:" + ID);
    } else {
      // BOA_.log("Storing snake " + ID + " frame " + frame);
      finalSnakes[frame - startFrame] = s;
    }
  }

  /**
   * Gets the start frame.
   *
   * @return the start frame
   */
  int getStartFrame() {
    return startFrame;
  }

  /**
   * Gets the end frame.
   *
   * @return the end frame
   */
  int getEndFrame() {
    return endFrame;
  }

  /**
   * Prepare current frame \c for segmentation
   * 
   * Create \c liveSnake using final snake stored in previous frame or use original ROI for
   * creating new Snake
   * 
   * @param f Current segmented frame
   */
  void resetForFrame(int f) {
    try {
      if (BOA_.qState.segParam.use_previous_snake) {
        // set to last segmentation ready for blowup
        liveSnake = new Snake((PolygonRoi) this.getStoredSnake(f - 1).asFloatRoi(), ID);
      } else {
        liveSnake = new Snake(roi, ID, false);
      }
    } catch (Exception e) {
      BOA_.log("Could not reset live snake form frame" + f);
      LOGGER.debug(e.getMessage(), e);
    }
  }

  /**
   * Store ROI as snake in finalSnakes.
   * 
   * @param r
   * @param f
   */
  void storeRoi(final PolygonRoi r, int f) {
    try {
      Snake snake = new Snake(r, ID);
      snake.calcCentroid();
      this.deleteStoreAt(f);
      storeAt(snake, f);
      // BOA_.log("Storing ROI snake " + ID + " frame " + f);
    } catch (Exception e) {
      BOA_.log("Could not store ROI");
      LOGGER.debug(e.getMessage(), e);
    }
  }

  /**
   * Find the first missing contour at series of frames and set end frame to the previous one
   */
  void findLastFrame() {
    for (int i = startFrame; i <= BOA_.qState.boap.getFRAMES(); i++) {
      if (!isStoredAt(i)) {
        endFrame = i - 1;
        return;
      }
    }
    endFrame = BOA_.qState.boap.getFRAMES();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SnakeHandler [liveSnake=" + liveSnake + ", finalSnakes=" + Arrays.toString(finalSnakes)
            + ", ID=" + ID + ", startFrame=" + startFrame + ", endFrame=" + endFrame + "]";
  }

  /**
   * Prepare all Snake stored in this SnakeHandler for saving.
   */
  @Override
  public void beforeSerialize() {
    if (liveSnake != null)
      liveSnake.beforeSerialize(); // convert liveSnake to array
    for (Snake s : finalSnakes)
      if (s != null)
        s.beforeSerialize(); // convert finalSnakes to array
    for (Snake s : segSnakes)
      if (s != null)
        s.beforeSerialize(); // convert segSnakes to array
  }

  /**
   * Prepare all Snake stored in this SnakeHandler for loading.
   */
  @Override
  public void afterSerialize() throws Exception {
    if (liveSnake != null)
      liveSnake.afterSerialize();
    for (Snake s : finalSnakes) {
      if (s != null)
        s.afterSerialize();
    }
    for (Snake s : segSnakes) {
      if (s != null)
        s.afterSerialize();
    }
    // restore roi as first snake from segmented snakes
    if (segSnakes != null && segSnakes.length > 0) {
      int i = 0;
      while (i < segSnakes.length && segSnakes[i++] == null)
        ; // find first not null snake
      QuimpDataConverter dC = new QuimpDataConverter(segSnakes[--i]);
      // rebuild roi from snake
      roi = new PolygonRoi(dC.getFloatX(), dC.getFloatY(), Roi.FREEROI);
    }

    /*
     * segSnakes = new Snake[finalSnakes.length]; for (int i = 0; i < segSnakes.length; i++) if
     * (finalSnakes[i] != null) segSnakes[i] = new Snake(finalSnakes[i],
     * finalSnakes[i].getSnakeID());
     */
  }
}