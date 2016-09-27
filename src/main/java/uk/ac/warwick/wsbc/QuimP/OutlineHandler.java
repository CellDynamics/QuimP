package uk.ac.warwick.wsbc.QuimP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Collection of outlines for subsequent frames (<it>f1</it> and <it>f2</it>) for one cell.
 * 
 * @author tyson
 */
public class OutlineHandler extends ShapeHandler<Outline> implements IQuimpSerialize {
    private static final Logger LOGGER = LogManager.getLogger(OutlineHandler.class.getName());
    /**
     * Array of given cell outlines found for frames (<tt>startFrame</tt> and <tt>endFrame</tt>) 
     */
    private Outline[] outlines;
    transient private QParams qp;
    // all transient fields are rebuild in afterSerialzie findStatLimits()
    transient private int size;
    transient public ExtendedVector2d maxCoor;
    transient public ExtendedVector2d minCoor;
    // min and max limits
    transient public double[] migLimits;
    transient public double[][] fluLims;
    // public double[] convLimits;
    transient public double[] curvLimits;
    transient public double maxLength = 0; //!< longest outline in outlines
    transient public boolean readSuccess;

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
     * Copy constructor
     * 
     * @param src to copy from
     */
    public OutlineHandler(final OutlineHandler src) {
        super(src);
        this.outlines = new Outline[src.outlines.length];
        for (int o = 0; o < this.outlines.length; o++)
            this.outlines[o] = new Outline(src.outlines[o]);
        size = src.size;
        /*// this is calculated by findStatLimits()
        maxCoor = src.maxCoor;
        minCoor = src.minCoor;
        migLimits = new double[src.migLimits.length];
        System.arraycopy(src.migLimits, 0, migLimits, 0, src.migLimits.length);
        fluLims = new double[src.fluLims.length][];
        for (int i = 0; i < src.fluLims.length; i++) {
            fluLims[i] = new double[src.fluLims[i].length];
            System.arraycopy(src.fluLims[i], 0, fluLims[i], 0, src.fluLims[i].length);
        }
        curvLimits = new double[src.curvLimits.length];
        System.arraycopy(src.curvLimits, 0, curvLimits, 0, src.curvLimits.length);
        */
        for (Outline o : outlines)
            if (o.getLength() > maxLength)
                maxLength = o.getLength();
        findStatLimits(); // fill maxCoor, minCoor, migLimits, fluLims, curvLimits
    }

    /**
     * Conversion constructor
     * 
     * Converts SnakeHandler to OutlineHandler. Converted are only Snakes and their range
     * @param snake source SnakeHandler 
     */
    public OutlineHandler(final SnakeHandler snake) {
        this(snake.startFrame, snake.endFrame); // create array and set ranges
        for (int f = startFrame; f <= endFrame; f++) { // copy all snakes
            Snake s = snake.getStoredSnake(f); // get original
            if (s != null)
                setOutline(f, new Outline(s)); // convert to Outline
        }
        findStatLimits();
    }

    public OutlineHandler(int s, int e) {
        size = e - s + 1;
        outlines = new Outline[size];
        startFrame = s;
        endFrame = e;

    }

    public int getStartFrame() {
        return startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    public Outline getOutline(int f) {
        if (f - startFrame < 0 || f - startFrame > outlines.length) {
            IJ.log("Tried to access OOR frame store\n\t...frame:" + f);
            return null;
        }
        return outlines[f - startFrame];
    }

    public boolean isOutlineAt(int f) {
        if (f - startFrame < 0)
            return false;
        else if (f - startFrame >= outlines.length)
            return false;
        else if (outlines[f - startFrame] == null)
            return false;
        else
            return true;
    }

    public Outline indexGetOutline(int i) {
        return outlines[i];
    }

    public void setOutline(int f, Outline o) {
        outlines[f - startFrame] = o;
        double length = o.getLength();
        if (length > maxLength) {
            maxLength = length;
        }

    }

    private boolean readOutlines(final File f) {
        if (!f.exists()) {
            IJ.error("Cannot locate snake file (snQP)\n'" + f.getAbsolutePath() + "'");
            return false;
        }
        if (qp == null)
            throw new InvalidParameterException(
                    "QParams is null. This object has not been created (loaded) from QParams data");

        String thisLine;

        maxLength = 0;
        // maxFlu = 0.;
        int N;
        int index;
        double length;
        Vert head, n, prevn;
        size = 0;

        try {
            // first count the outlines
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.startsWith("#")) {
                    continue;
                }
                N = (int) Tool.s2d(thisLine);
                for (int i = 0; i < N; i++) {
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

                N = (int) Tool.s2d(thisLine);

                for (int i = 0; i < N; i++) {
                    thisLine = br.readLine();
                    String[] split = thisLine.split("\t");
                    n = new Vert(index);

                    n.coord = Tool.s2d(split[0]);
                    n.setX(Tool.s2d(split[1]));
                    n.setY(Tool.s2d(split[2]));

                    n.fCoord = Tool.s2d(split[3]); // Origon
                    n.gCoord = Tool.s2d(split[4]); // G-Origon
                    n.distance = Tool.s2d(split[5]); // speed

                    // store flu measurements
                    n.fluores[0].intensity = Tool.s2d(split[6]);

                    if (qp.paramFormat == QParams.QUIMP_11) {
                        // has other channels and x and y
                        n.fluores[0].x = Tool.s2d(split[7]);
                        n.fluores[0].y = Tool.s2d(split[8]);

                        n.fluores[1].intensity = Tool.s2d(split[9]);
                        n.fluores[1].x = Tool.s2d(split[10]);
                        n.fluores[1].y = Tool.s2d(split[11]);

                        n.fluores[2].intensity = Tool.s2d(split[12]);
                        n.fluores[2].x = Tool.s2d(split[13]);
                        n.fluores[2].y = Tool.s2d(split[14]);
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

                Outline tmp = new Outline(head, N + 1); // dont forget the head node
                tmp.removeVert(head); // WARN potential incompatibility with old code.
                                      // old constructor made copy of this list and deleted
                                      // first dummy node. Now it just covers this list
                // make deep copy of this list
                outlines[s] = new Outline(tmp);
                outlines[s].updateNormales(true);
                outlines[s].makeAntiClockwise();
                length = outlines[s].getLength();
                if (length > maxLength) {
                    maxLength = length;
                }
                s++;
                LOGGER.trace("Outline: " + s + " head =[" + outlines[s - 1].getHead().getX() + ","
                        + outlines[s - 1].getHead().getY() + "]");
            } // end while
            br.close();

            if (qp.paramFormat == QParams.OLD_QUIMP) { // TODO is this always true?
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
        }
    }

    /**
     * Evaluate <tt>maxCoor</tt>, <tt>minCoor</tt>, <tt>migLimits</tt>, <tt>fluLims</tt>,
     * <tt>curvLimits</tt>.
     * 
     * Initialize arrays as well
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
            if (outline == null)
                continue;
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

                if (n.distance < migLimits[0])
                    migLimits[0] = n.distance;
                if (n.distance > migLimits[1])
                    migLimits[1] = n.distance;

                // if(n.convexity < convLimits[0]) convLimits[0] = n.convexity;
                // if(n.convexity > convLimits[1]) convLimits[1] = n.convexity;

                for (int j = 0; j < n.fluores.length; j++) {
                    if (n.fluores[j].intensity < fluLims[j][0])
                        fluLims[j][0] = n.fluores[j].intensity;
                    if (n.fluores[j].intensity > fluLims[j][1])
                        fluLims[j][1] = n.fluores[j].intensity;
                }

                n = n.getNext();
            } while (!n.isHead());
        }

        // Set limits to equal positive and negative
        migLimits = Tool.setLimitsEqual(migLimits);
    }

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
     */
    public void writeOutlines(File outFile, boolean ECMMrun) {
        LOGGER.debug("Write outline at: " + outFile);
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto flush
            pw.write("#QuimP11 node data");
            if (ECMMrun) {
                pw.print("-ECMM");
            }
            pw.write("\n#Node Position\tX-coord\tY-coord\tOrigin\tG-Origin\tSpeed");
            pw.write(
                    "\tFluor_Ch1\tCh1_x\tCh1_y\tFluor_Ch2\tCh2_x\tCh2_y\tFluor_CH3\tCH3_x\tCh3_y\n#");

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

    static public void writeSingle(String f, Outline o) {
        try {
            File outFile = new File("/Users/rtyson/Documents/phd/tmp/" + f);
            if (outFile.exists()) {
                outFile.delete();
            }
            PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto flush
            pw.print("#");
            OutlineHandler.write(pw, o.getNumVerts(), o.getHead());
            pw.close();

        } catch (Exception e) {
            IJ.log("could not open out file " + f);
            return;
        }
    }

    static private void write(PrintWriter pw, int VERTS, Vert v) {
        pw.print("\n" + VERTS);
        // !< off formatting tag
        do {
            pw.print("\n" 
                    + IJ.d2s(v.coord, 6) + "\t" // Perimeter coord
                    + IJ.d2s(v.getX(), 2) + "\t" // X coord
                    + IJ.d2s(v.getY(), 2) + "\t" // Y coord
                    + IJ.d2s(v.fCoord, 6) + "\t" // Origin
                    + IJ.d2s(v.gCoord, 6) + "\t" // G-Origin
                    + IJ.d2s(v.distance, 6) + "\t" // Speed
                    + IJ.d2s(v.fluores[0].intensity, 6) + "\t" // Fluor_Ch1
                    + IJ.d2s(v.fluores[0].x, 0) + "\t"  // Ch1_x
                    + IJ.d2s(v.fluores[0].y, 0) + "\t" // Ch1_y
                    + IJ.d2s(v.fluores[1].intensity, 6) + "\t" // Fluor_Ch2
                    + IJ.d2s(v.fluores[1].x, 0) + "\t" // Ch2_x
                    + IJ.d2s(v.fluores[1].y, 0) + "\t" // Ch2_y
                    + IJ.d2s(v.fluores[2].intensity, 6) + "\t" // Fluor_CH3
                    + IJ.d2s(v.fluores[2].x, 0) + "\t" // CH3_x
                    + IJ.d2s(v.fluores[2].y, 0)); // CH3_y
            // on formatting */
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Prepare all Outline stored in this OutlineHandler for loading.
     */
    @Override
    public void beforeSerialize() {
        for (Outline o : outlines)
            if (o != null)
                o.beforeSerialize(); // convert outlines to array

    }

    /**
     * Call afterSerialzie() for other objects and restoer transient fields where possible
     */
    @Override
    public void afterSerialize() throws Exception {
        for (Outline o : outlines)
            if (o != null)
                o.afterSerialize(); // convert array to outlines
        // restore other fields
        size = outlines.length;
        for (Outline o : outlines)
            if (o.getLength() > maxLength)
                maxLength = o.getLength();
        findStatLimits(); // fill maxCoor, minCoor, migLimits, fluLims, curvLimits

    }
}
