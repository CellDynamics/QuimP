package uk.ac.warwick.wsbc.quimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/**
 * Collection of outlines for subsequent frames (<it>f1</it> and <it>f2</it>) for one cell.
 * 
 * @author tyson
 * @author p.baniukiewicz
 */
public class OutlineHandler extends ShapeHandler<Outline> implements IQuimpSerialize {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(OutlineHandler.class.getName());
  /**
   * Array of given cell outlines found for frames (<tt>startFrame</tt> and <tt>endFrame</tt>).
   */
  private Outline[] outlines;
  private transient QParams qp;
  // all transient fields are rebuild in afterSerialzie findStatLimits()
  private transient int size;

  /**
   * The max coor.
   */
  public transient ExtendedVector2d maxCoor;

  /**
   * The min coor.
   */
  public transient ExtendedVector2d minCoor;

  /**
   * The mig limits.
   */
  // min and max limits
  public transient double[] migLimits;

  /**
   * The flu lims.
   */
  public transient double[][] fluLims;

  /**
   * The curv limits.
   */
  public transient double[] curvLimits;
  /**
   * longest outline in outlines.
   */
  public transient double maxLength = 0;

  /**
   * The read success.
   */
  public transient boolean readSuccess;

  /**
   * Instantiates a new outline handler.
   *
   * @param params the params
   */
  public OutlineHandler(QParams params) {
    qp = params;
    startFrame = qp.getStartFrame();
    endFrame = qp.getEndFrame();

    // System.out.println("start frame: " + startFrame + ", endframe: " +
    // endFrame);

    if (!readOutlines(qp.getSnakeQP())) { // initialize also arrays by findStatsLimits()
      IJ.error("Failed to read in snakQP (OutlineHandler:36)");
      readSuccess = false;
      size = 0;
    } else {
      size = outlines.length;
      readSuccess = true;
    }
  }

  /**
   * Copy constructor.
   * 
   * @param src to copy from
   */
  public OutlineHandler(final OutlineHandler src) {
    super(src);
    this.outlines = new Outline[src.outlines.length];
    for (int o = 0; o < this.outlines.length; o++) {
      this.outlines[o] = new Outline(src.outlines[o]);
    }
    size = src.size;
    /*
     * // this is calculated by findStatLimits() maxCoor = src.maxCoor; minCoor = src.minCoor;
     * migLimits = new double[src.migLimits.length]; System.arraycopy(src.migLimits, 0,
     * migLimits, 0, src.migLimits.length); fluLims = new double[src.fluLims.length][]; for (int
     * i = 0; i < src.fluLims.length; i++) { fluLims[i] = new double[src.fluLims[i].length];
     * System.arraycopy(src.fluLims[i], 0, fluLims[i], 0, src.fluLims[i].length); } curvLimits =
     * new double[src.curvLimits.length]; System.arraycopy(src.curvLimits, 0, curvLimits, 0,
     * src.curvLimits.length);
     */
    for (Outline o : outlines) {
      if (o.getLength() > maxLength) {
        maxLength = o.getLength();
      }
    }
    findStatLimits(); // fill maxCoor, minCoor, migLimits, fluLims, curvLimits
  }

  /**
   * Conversion constructor.
   * 
   * <p>Converts SnakeHandler to OutlineHandler. Converted are only Snakes and their range
   * 
   * @param snake source SnakeHandler
   */
  public OutlineHandler(final SnakeHandler snake) {
    this(snake.startFrame, snake.endFrame); // create array and set ranges
    for (int f = startFrame; f <= endFrame; f++) { // copy all snakes
      Snake s = snake.getStoredSnake(f); // get original
      if (s != null) {
        setOutline(f, new Outline(s)); // convert to Outline
      }
    }
    findStatLimits();
  }

  /**
   * Instantiates a new outline handler.
   *
   * @param s start frame
   * @param e end frame
   */
  public OutlineHandler(int s, int e) {
    size = e - s + 1;
    outlines = new Outline[size];
    startFrame = s;
    endFrame = e;

  }

  /**
   * Gets the start frame.
   *
   * @return the start frame
   */
  public int getStartFrame() {
    return startFrame;
  }

  /**
   * Gets the end frame.
   *
   * @return the end frame
   */
  public int getEndFrame() {
    return endFrame;
  }

  /**
   * Gets the outline.
   *
   * @param f the f
   * @return the outline
   */
  public Outline getOutline(int f) {
    if (f - startFrame < 0 || f - startFrame > outlines.length) {
      IJ.log("Tried to access OOR frame store\n\t...frame:" + f);
      return null;
    }
    return outlines[f - startFrame];
  }

  /**
   * Checks if is outline at.
   *
   * @param f the f
   * @return true, if is outline at
   */
  public boolean isOutlineAt(int f) {
    if (f - startFrame < 0) {
      return false;
    } else if (f - startFrame >= outlines.length) {
      return false;
    } else if (outlines[f - startFrame] == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Index get outline.
   *
   * @param i the i
   * @return the outline
   */
  public Outline indexGetOutline(int i) {
    return outlines[i];
  }

  /**
   * Sets the outline.
   *
   * @param f the f
   * @param o the o
   */
  public void setOutline(int f, Outline o) {
    outlines[f - startFrame] = o;
    double length = o.getLength();
    if (length > maxLength) {
      maxLength = length;
    }

  }

  private boolean readOutlines(final File f) {
    if (!f.exists()) {
      IJ.error("Cannot locate snake file (" + FileExtensions.snakeFileExt + ")\n'"
              + f.getAbsolutePath() + "'");
      return false;
    }
    if (qp == null) {
      throw new InvalidParameterException(
              "QParams is null. This object has not been created (loaded) from QParams data");
    }

    String thisLine;

    maxLength = 0;
    // maxFlu = 0.;
    int nn;
    int index;
    double length;
    Vert head;
    Vert n;
    Vert prevn;
    size = 0;

    try {
      // first count the outlines
      BufferedReader br = new BufferedReader(new FileReader(f));
      while ((thisLine = br.readLine()) != null) {
        if (thisLine.startsWith("#")) {
          continue;
        }
        nn = (int) QuimpToolsCollection.s2d(thisLine);
        for (int i = 0; i < nn; i++) {
          // System.out.println(br.readLine() + ", " + );
          br.readLine();
          // br.readLine();
        }
        size++;
      }
      br.close();
      // IJ.write("num outlines " + size);
      outlines = new Outline[size];

      int s = 0;
      // read outlines into memory
      br = new BufferedReader(new FileReader(f));

      while ((thisLine = br.readLine()) != null) { // while loop begins here
        // System.out.println(thisLine);
        if (thisLine.startsWith("#")) {
          continue; // skip comments
        }

        index = 0;
        head = new Vert(index); // dummy head node
        head.setHead(true);
        prevn = head;
        index++;

        nn = (int) QuimpToolsCollection.s2d(thisLine);

        for (int i = 0; i < nn; i++) {
          thisLine = br.readLine();
          String[] split = thisLine.split("\t");
          n = new Vert(index);

          n.coord = QuimpToolsCollection.s2d(split[0]);
          n.setX(QuimpToolsCollection.s2d(split[1]));
          n.setY(QuimpToolsCollection.s2d(split[2]));

          n.fCoord = QuimpToolsCollection.s2d(split[3]); // Origon
          n.gCoord = QuimpToolsCollection.s2d(split[4]); // G-Origon
          n.distance = QuimpToolsCollection.s2d(split[5]); // speed

          // store flu measurements
          n.fluores[0].intensity = QuimpToolsCollection.s2d(split[6]);

          if (qp.paramFormat == QParams.QUIMP_11) {
            // has other channels and x and y
            n.fluores[0].x = QuimpToolsCollection.s2d(split[7]);
            n.fluores[0].y = QuimpToolsCollection.s2d(split[8]);

            n.fluores[1].intensity = QuimpToolsCollection.s2d(split[9]);
            n.fluores[1].x = QuimpToolsCollection.s2d(split[10]);
            n.fluores[1].y = QuimpToolsCollection.s2d(split[11]);

            n.fluores[2].intensity = QuimpToolsCollection.s2d(split[12]);
            n.fluores[2].x = QuimpToolsCollection.s2d(split[13]);
            n.fluores[2].y = QuimpToolsCollection.s2d(split[14]);
          }

          n.frozen = false;
          index++;
          prevn.setNext(n);
          n.setPrev(prevn);
          prevn = n;

        }
        // link tail to head
        prevn.setNext(head);
        head.setPrev(prevn);

        // head is dummy element that will be removed. To deal with random selection of new head we
        // remember next element to it (which is first element from snQP)
        Vert newHead = head.getNext(); // this will be new head

        Outline tmp = new Outline(head, nn + 1); // dont forget the head node
        // WARN potential incompatibility with old code. see
        // 3784b9f1afb1dd317bd4740e17f02627fa89bc41 for original Outline and OutlineHandler
        tmp.removeVert(head); // new head is randomly selected
        tmp.setHead(newHead); // be sure to set head to first node on snQP list.

        outlines[s] = tmp;
        outlines[s].updateNormales(true);
        outlines[s].makeAntiClockwise();
        outlines[s].coordReset(); // there is no cord data in snQP file this set it as Position.
        length = outlines[s].getLength();
        if (length > maxLength) {
          maxLength = length;
        }
        s++;
        LOGGER.trace("Outline: " + s + " head =[" + outlines[s - 1].getHead().getX() + ","
                + outlines[s - 1].getHead().getY() + "]");
      } // end while
      br.close();

      if (qp.paramFormat == QParams.OLD_QUIMP) {
        qp.setStartFrame(1);
        qp.setEndFrame(size);
        this.endFrame = size;
        this.startFrame = 1;
        qp.writeParams(); // replace the old format parameter file
      }
      this.findStatLimits();

      return true;
    } catch (IOException e) {
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Could not read outlines", e.getMessage());
      return false;
    } catch (NullPointerException e1) {
      LOGGER.debug(e1.getMessage(), e1);
      LOGGER.error("Damaged snQP file", e1.getMessage());
      return false;
    }
  }

  /**
   * Evaluate <tt>maxCoor</tt>, <tt>minCoor</tt>, <tt>migLimits</tt>, <tt>fluLims</tt>,
   * <tt>curvLimits</tt>.
   * 
   * <p>Initialise arrays as well
   */
  private void findStatLimits() {
    maxCoor = new ExtendedVector2d();
    minCoor = new ExtendedVector2d();
    fluLims = new double[3][2];
    migLimits = new double[2];
    // convLimits = new double[2];
    curvLimits = new double[2]; // not filled until Q_Analsis run. smoothed curvature

    // cycle through all frames and find the min and max for all data
    // store min and max coor\migration\flu for plotting
    Outline outline;
    Vert n;
    for (int i = 0; i < outlines.length; i++) {
      outline = outlines[i];
      if (outline == null) {
        continue;
      }
      n = outline.getHead();
      if (i == 0) {
        minCoor.setXY(n.getX(), n.getY());
        maxCoor.setXY(n.getX(), n.getY());
        migLimits[0] = n.distance;
        migLimits[1] = n.distance;
        // convLimits[0] = n.convexity;
        // convLimits[1] = n.convexity;
        for (int j = 0; j < n.fluores.length; j++) {
          fluLims[j][0] = n.fluores[j].intensity;
          fluLims[j][1] = n.fluores[j].intensity;
        }
      }

      do {
        if (n.getX() > maxCoor.getX()) {
          maxCoor.setX(n.getX());
        }
        if (n.getY() > maxCoor.getY()) {
          maxCoor.setY(n.getY());
        }
        if (n.getX() < minCoor.getX()) {
          minCoor.setX(n.getX());
        }
        if (n.getY() < minCoor.getY()) {
          minCoor.setY(n.getY());
        }

        if (n.distance < migLimits[0]) {
          migLimits[0] = n.distance;
        }
        if (n.distance > migLimits[1]) {
          migLimits[1] = n.distance;
        }

        // if(n.convexity < convLimits[0]) convLimits[0] = n.convexity;
        // if(n.convexity > convLimits[1]) convLimits[1] = n.convexity;

        for (int j = 0; j < n.fluores.length; j++) {
          if (n.fluores[j].intensity < fluLims[j][0]) {
            fluLims[j][0] = n.fluores[j].intensity;
          }
          if (n.fluores[j].intensity > fluLims[j][1]) {
            fluLims[j][1] = n.fluores[j].intensity;
          }
        }

        n = n.getNext();
      } while (!n.isHead());
      // see uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap.calcCurvature()
      Vert v;
      for (int f = getStartFrame(); f <= getEndFrame(); f++) {
        Outline o = getOutline(f);
        if (o == null) {
          continue;
        }
        v = o.getHead();

        // find min and max of sum curvature
        v = o.getHead();
        if (f == getStartFrame()) {
          curvLimits[1] = v.curvatureSum;
          curvLimits[0] = v.curvatureSum;
        }
        do {
          if (v.curvatureSum > curvLimits[1]) {
            curvLimits[1] = v.curvatureSum;
          }
          if (v.curvatureSum < curvLimits[0]) {
            curvLimits[0] = v.curvatureSum;
          }
          v = v.getNext();
        } while (!v.isHead());
      }
    }

    // Set limits to equal positive and negative
    migLimits = QuimpToolsCollection.setLimitsEqual(migLimits);
    curvLimits = QuimpToolsCollection.setLimitsEqual(curvLimits);
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  public int getSize() {
    return size;
  }

  /**
   * Copy Outline into internal outlines array on correct position.
   * 
   * @param o Outline to copy.
   * @param frame Frame where copy Outline to.
   */
  public void save(Outline o, int frame) {
    outlines[frame - startFrame] = new Outline(o);
  }

  /**
   * Write <b>this</b> outline to disk.
   * 
   * @param outFile file to save
   * @param isEccmRun was ECMM run?
   */
  public void writeOutlines(File outFile, boolean isEccmRun) {
    LOGGER.debug("Write outline at: " + outFile);
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto flush
      pw.write("#QuimP11 node data");
      if (isEccmRun) {
        pw.print("-ECMM");
      }
      pw.write("\n#Node Position\tX-coord\tY-coord\tOrigin\tG-Origin\tSpeed");
      pw.write("\tFluor_Ch1\tCh1_x\tCh1_y\tFluor_Ch2\tCh2_x\tCh2_y\tFluor_CH3\tCH3_x\tCh3_y\n#");

      Outline o;
      for (int i = startFrame; i <= endFrame; i++) {
        o = getOutline(i);
        pw.write("\n#Frame " + i);
        write(pw, o.getNumVerts(), o.getHead());
      }
      pw.close();
    } catch (Exception e) {
      IJ.log("could not open out file " + outFile.getAbsolutePath());
      return;
    }
  }

  private static void write(PrintWriter pw, int verts, Vert v) {
    pw.print("\n" + verts);
    //!>
    do {
      pw.print("\n" + IJ.d2s(v.coord, 6) + "\t" // Perimeter coord
              + IJ.d2s(v.getX(), 2) + "\t" // X coord
              + IJ.d2s(v.getY(), 2) + "\t" // Y coord
              + IJ.d2s(v.fCoord, 6) + "\t" // Origin
              + IJ.d2s(v.gCoord, 6) + "\t" // G-Origin
              + IJ.d2s(v.distance, 6) + "\t" // Speed
              + IJ.d2s(v.fluores[0].intensity, 6) + "\t" // Fluor_Ch1
              + IJ.d2s(v.fluores[0].x, 0) + "\t" // Ch1_x
              + IJ.d2s(v.fluores[0].y, 0) + "\t" // Ch1_y
              + IJ.d2s(v.fluores[1].intensity, 6) + "\t" // Fluor_Ch2
              + IJ.d2s(v.fluores[1].x, 0) + "\t" // Ch2_x
              + IJ.d2s(v.fluores[1].y, 0) + "\t" // Ch2_y
              + IJ.d2s(v.fluores[2].intensity, 6) + "\t" // Fluor_CH3
              + IJ.d2s(v.fluores[2].x, 0) + "\t" // CH3_x
              + IJ.d2s(v.fluores[2].y, 0)); // CH3_y
      //!<
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Prepare all Outline stored in this OutlineHandler for loading.
   */
  @Override
  public void beforeSerialize() {
    for (Outline o : outlines) {
      if (o != null) {
        o.beforeSerialize(); // convert outlines to array
      }
    }
  }

  /**
   * Call afterSerialzie() for other objects and restore transient fields where possible.
   */
  @Override
  public void afterSerialize() throws Exception {
    for (Outline o : outlines) {
      if (o != null) {
        o.afterSerialize(); // convert array to outlines
      }
    }
    // restore other fields
    size = outlines.length;
    for (Outline o : outlines) {
      if (o.getLength() > maxLength) {
        maxLength = o.getLength();
      }
    }
    findStatLimits(); // fill maxCoor, minCoor, migLimits, fluLims, curvLimits

  }
}
