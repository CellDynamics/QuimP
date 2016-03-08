package uk.ac.warwick.wsbc.QuimP;

import ij.*;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

import java.io.*;

/**
 *
 * @author tyson
 */
public class OutlineHandler {

    private Outline[] outlines;
    private int size;
    private int startFrame;
    private int endFrame;
    private QParams qp;
    public ExtendedVector2d maxCoor;
    public ExtendedVector2d minCoor;
    // min and max limits
    public double[] migLimits;
    public double[][] fluLims;
    // public double[] convLimits;
    public double[] curvLimits;
    public double maxLength = 0;
    public boolean readSuccess;

    public OutlineHandler(QParams params) {
        qp = params;
        startFrame = qp.startFrame;
        endFrame = qp.endFrame;

        // System.out.println("start frame: " + startFrame + ", endframe: " +
        // endFrame);

        if (!readOutlines(qp.snakeQP)) {
            IJ.error("Failed to read in snakQP (OutlineHandler:36)");
            readSuccess = false;
            size = 0;
        } else {
            size = outlines.length;
            readSuccess = true;
        }
    }

    public OutlineHandler(int s, int e) {
        size = e - s + 1;
        outlines = new Outline[size];
        startFrame = s;
        endFrame = e;

    }

    int getStartFrame() {
        return startFrame;
    }

    int getEndFrame() {
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
        if (f < startFrame || f > endFrame) {
            return false;
        } else {
            return true;
        }
    }

    Outline indexGetOutline(int i) {
        return outlines[i];
    }

    public void setOutline(int f, Outline o) {
        outlines[f - startFrame] = o;
        double length = o.getLength();
        if (length > maxLength) {
            maxLength = length;
        }

    }

    private boolean readOutlines(File f) {
        if (!f.exists()) {
            IJ.error("Cannot locate snake file (snQP)\n'" + f.getAbsolutePath() + "'");
            return false;
        }

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

            while ((thisLine = br.readLine()) != null) { // while loop begins
                                                         // here
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

                    if (qp.newFormat) {
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

                outlines[s] = new Outline(head, N + 1); // dont forget the head
                                                        // node
                outlines[s].updateNormales(true);
                outlines[s].makeAntiClockwise();
                length = outlines[s].getLength();
                if (length > maxLength) {
                    maxLength = length;
                }

                // int c = outlines[s].countVERTS();
                // if(c !=outlines[s].getVerts()){
                // System.out.println("OH.234.VERTS NOT CORREECT. VERTS:
                // "+outlines[s].getVerts()+", Count: " + c);
                // }

                s++;
            } // end while
            br.close();

            if (!qp.newFormat) {
                qp.startFrame = 1;
                qp.endFrame = size;
                this.endFrame = size;
                this.startFrame = 1;
                qp.writeParams(); // replace the old format parameter file
            }
            this.findStatLimits();

            return true;
        } catch (IOException e) {
            System.err.println("Could not read file: " + e);
            IJ.error("Could not read file: " + e);
            return false;
        }
    }

    private void findStatLimits() {
        maxCoor = new ExtendedVector2d();
        minCoor = new ExtendedVector2d();
        fluLims = new double[3][2];
        migLimits = new double[2];
        // convLimits = new double[2];
        curvLimits = new double[2]; // not filled until Q_Analsis run. smoothed
                                    // curvature

        // cycle through all frames and find the min and max for all data
        // store min and max coor\migration\flu for plotting
        Outline outline;
        Vert n;
        for (int i = 0; i < outlines.length; i++) {
            outline = outlines[i];
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

    public void save(Outline o, int frame) {
        // basically clone snake into memory
        Vert oV = o.getHead();

        Vert nV = new Vert(oV.getX(), oV.getY(), oV.getTrackNum()); // head node
        nV.coord = oV.coord;
        nV.fCoord = oV.fCoord;
        nV.gCoord = oV.gCoord;
        nV.distance = oV.distance;
        // nV.fluores = oV.fluores;
        nV.setFluores(oV.fluores);

        Outline n = new Outline(nV);

        oV = oV.getNext();
        do {
            nV = n.insertVert(nV);

            nV.setX(oV.getX());
            nV.setY(oV.getY());
            nV.coord = oV.coord;
            nV.fCoord = oV.fCoord;
            nV.gCoord = oV.gCoord;
            nV.distance = oV.distance;
            // nV.fluores = oV.cloneFluo();
            nV.setFluores(oV.fluores);
            nV.setTrackNum(oV.getTrackNum());

            oV = oV.getNext();
        } while (!oV.isHead());
        outlines[frame - startFrame] = n;
    }

    public void writeOutlines(File outFile, boolean ECMMrun) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto
                                                                             // flush

            pw.write("#QuimP11 node data");
            if (ECMMrun) {
                pw.print("-ECMM");
            }
            pw.write("\n#Node Position\tX-coord\tY-coord\tOrigin\tG-Origin\tSpeed");
            pw.write("\tFluor_Ch1\tCh1_x\tCh1_y\tFluor_Ch2\tCh2_x\tCh2_y\tFluor_CH3\tCH3_x\tCh3_y\n#");

            Outline o;
            for (int i = startFrame; i <= endFrame; i++) {
                o = getOutline(i);
                pw.write("\n#Frame " + i);
                write(pw, o.getVerts(), o.getHead());
            }

            // for (int i = 0; i < outlines.length; i++) {
            // pw.write("\n#Frame "+(i+1));
            // if (outlines[i] == null) {
            // System.out.println("DEBUG writeOutlines: frame " + i + " is NULL,
            // not writting");
            // continue;
            // }

            // OutlineHandler.write(pw, i + 1, outlines[i].getVerts(),
            // outlines[i].getHead());
            // }
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
            PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto
                                                                             // flush
            pw.print("#");
            OutlineHandler.write(pw, o.getVerts(), o.getHead());
            pw.close();

        } catch (Exception e) {
            IJ.log("could not open out file " + f);
            return;
        }
    }

    static private void write(PrintWriter pw, int VERTS, Vert v) {
        pw.print("\n" + VERTS);

        do {
            pw.print("\n" + IJ.d2s(v.coord, 6) + "\t" + IJ.d2s(v.getX(), 2) + "\t" + IJ.d2s(v.getY(), 2) + "\t"
                    + IJ.d2s(v.fCoord, 6) + "\t" + IJ.d2s(v.gCoord, 6) + "\t" + IJ.d2s(v.distance, 6) + "\t"
                    + IJ.d2s(v.fluores[0].intensity, 6) + "\t" + IJ.d2s(v.fluores[0].x, 0) + "\t"
                    + IJ.d2s(v.fluores[0].y, 0) + "\t" + IJ.d2s(v.fluores[1].intensity, 6) + "\t"
                    + IJ.d2s(v.fluores[1].x, 0) + "\t" + IJ.d2s(v.fluores[1].y, 0) + "\t"
                    + IJ.d2s(v.fluores[2].intensity, 6) + "\t" + IJ.d2s(v.fluores[2].x, 0) + "\t"
                    + IJ.d2s(v.fluores[2].y, 0));
            v = v.getNext();
        } while (!v.isHead());
    }
}
