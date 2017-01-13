package uk.ac.warwick.wsbc.QuimP;

// import ij.process.PolygonFiller;
import java.awt.Polygon;
// import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Calculate statistics for whole stack (all cells).
 * 
 * Stats are written on disk after calling constructor. Additionally there is separate list
 * maintained with the same data. They can be collected calling {@link #getStatH()}. This is due to
 * compatibility with old QuimP.
 * 
 * @author tyson
 * @author p.baniukiewicz
 */
public class CellStatsEval implements Measurements {
    /**
     * Hold all stats for cell. the same data are written to disk as csv file.
     */
    private CellStats statH;
    OutlineHandler outputH;
    File OUTFILE;
    ImagePlus iPlus;
    ImageProcessor iProc;
    ImageStatistics is;
    double scale;
    double frameInterval;

    /**
     * Create and run the object.
     * 
     * After creating the object, file with stats is written and stats are avaiable by calling
     * {@link #getStatH()} method.
     * 
     * @param oH
     * @param ip image associated with OutlineHandler
     * @param f file name to write stats
     * @param s image scale
     * @param fI frame interval
     */
    public CellStatsEval(OutlineHandler oH, ImagePlus ip, File f, double s, double fI) {
        IJ.showStatus("BOA-Calculating Cell stats");
        outputH = oH;
        OUTFILE = f;
        iPlus = ip;
        iProc = ip.getProcessor();
        scale = s;
        frameInterval = fI;

        FrameStatistics[] stats = record();
        iPlus.setSlice(1);
        iPlus.killRoi();
        write(stats, outputH.getStartFrame());
    }

    /**
     * Only create the object. Stats file is not created but results are available by calling
     * {@link #getStatH()} method.
     * 
     * @param oH
     * @param ip image associated with OutlineHandler
     * @param f file name to write stats
     * @param s image scale
     * @param fI frame interval
     */
    public CellStatsEval(OutlineHandler oH, ImagePlus ip, double s, double fI) {
        IJ.showStatus("BOA-Calculating Cell stats");
        outputH = oH;
        OUTFILE = null;
        iPlus = ip;
        iProc = ip.getProcessor();
        scale = s;
        frameInterval = fI;

        FrameStatistics[] stats = record();
        iPlus.setSlice(1);
        iPlus.killRoi();
        buildData(stats);
    }

    /**
     * Calculate stats.
     * 
     * <p>
     * <b>Warning</b>
     * <p>
     * Number of calculated stats must be reflected in {@link buildData(FrameStat[])}
     * 
     * @return Array with stats for every frame for one cell.
     */
    private FrameStatistics[] record() {
        // ImageStack orgStack = orgIpl.getStack();
        FrameStatistics[] stats = new FrameStatistics[outputH.getSize()];

        double distance = 0;
        Outline o;
        PolygonRoi roi;
        int store;

        for (int f = outputH.getStartFrame(); f <= outputH.getEndFrame(); f++) {
            IJ.showProgress(f, outputH.getEndFrame());
            store = f - outputH.getStartFrame();

            o = outputH.getOutline(f);
            iPlus.setSlice(f); // also updates the processor
            stats[store] = new FrameStatistics();

            Polygon oPoly = o.asPolygon();
            roi = new PolygonRoi(oPoly, Roi.POLYGON);

            iPlus.setRoi(roi);
            is = iPlus.getStatistics(AREA + CENTROID + ELLIPSE + SHAPE_DESCRIPTORS); // this does
                                                                                     // scale to
                                                                                     // image

            // all theses already to scale
            stats[store].frame = f;
            stats[store].area = is.area;
            stats[store].centroid.setX(is.xCentroid);
            stats[store].centroid.setY(is.yCentroid);

            stats[store].elongation = is.major / is.minor; // include both axis
                                                           // plus elongation
            stats[store].perimiter = roi.getLength(); // o.getLength();
            stats[store].circularity = 4 * Math.PI
                    * (stats[store].area / (stats[store].perimiter * stats[store].perimiter));
            stats[store].displacement =
                    ExtendedVector2d.lengthP2P(stats[0].centroid, stats[store].centroid);

            if (store != 0) {
                stats[store].speed = ExtendedVector2d.lengthP2P(stats[store - 1].centroid,
                        stats[store].centroid);
                distance += ExtendedVector2d.lengthP2P(stats[store - 1].centroid,
                        stats[store].centroid);
                stats[store].dist = distance;
            } else {
                stats[store].dist = 0;
                stats[store].speed = 0;
            }

            if (distance != 0) {
                stats[store].persistance = stats[store].displacement / distance;
            } else {
                stats[store].persistance = 0;
            }

        }

        // convert centroid to pixels
        for (int f = outputH.getStartFrame(); f <= outputH.getEndFrame(); f++) {
            store = f - outputH.getStartFrame();
            stats[store].centroidToPixels(scale);
        }

        return stats;
    }

    private void write(FrameStatistics[] s, int startFrame) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(OUTFILE), true); // auto flush
            // IJ.log("Writing to file");
            pw.print("#p2\n#QuimP output - " + OUTFILE.getAbsolutePath() + "\n");
            pw.print(
                    "# Centroids are given in pixels.  Distance & speed & area measurements are scaled to micro meters\n");
            pw.print("# Scale: " + scale + " micro meter per pixel | Frame interval: "
                    + frameInterval + " sec\n");
            pw.print("# Frame,X-Centroid,Y-Centroid,Displacement,Dist. Traveled,"
                    + "Directionality,Speed,Perimeter,Elongation,Circularity,Area");

            for (int i = 0; i < s.length; i++) {
                pw.print("\n" + (i + startFrame) + "," + IJ.d2s(s[i].centroid.getX(), 2) + ","
                        + IJ.d2s(s[i].centroid.getY(), 2) + "," + IJ.d2s(s[i].displacement) + ","
                        + IJ.d2s(s[i].dist) + "," + IJ.d2s(s[i].persistance) + ","
                        + IJ.d2s(s[i].speed) + "," + IJ.d2s(s[i].perimiter) + ","
                        + IJ.d2s(s[i].elongation) + "," + IJ.d2s(s[i].circularity, 3) + ","
                        + IJ.d2s(s[i].area));

            }

            pw.print("\n#\n# Fluorescence measurements");
            writeDummyFluo(pw, 1, startFrame, s.length);
            writeDummyFluo(pw, 2, startFrame, s.length);
            writeDummyFluo(pw, 3, startFrame, s.length);

            pw.close();
            buildData(s);
        } catch (Exception e) {
            IJ.error("could not open out file");
            return;
        }
    }

    /**
     * Complementary to write method. Create the same data as write but in form of arrays. For
     * compatible reasons.
     * 
     * @param s Frame statistics calculated by
     *        {@link uk.ac.warwick.wsbc.QuimP.CellStatsEval#record()}
     */
    private void buildData(FrameStatistics[] s) {
        statH = new CellStats(s.length, 11, 11);
        // duplicate from write method
        statH.framestat = new ArrayList<FrameStatistics>(Arrays.asList(s));
    }

    /**
     * @return the statH
     */
    public CellStats getStatH() {
        return statH;
    }

    private void writeDummyFluo(PrintWriter pw, int channel, int startFrame, int size)
            throws Exception {
        pw.print("\n#\n# Channel " + channel
                + ";Frame, Total Fluo.,Mean Fluo.,Cortex Width, Cyto. Area,Total Cyto. Fluo., Mean Cyto. Fluo.,"
                + "Cortex Area,Total Cortex Fluo., Mean Cortex Fluo., %age Cortex Fluo.");
        for (int i = 0; i < size; i++) {
            pw.print("\n" + (i + startFrame) + ",-1,-1,-1,-1,-1,-1,-1,-1,-1,-1");
        }
    }
}
