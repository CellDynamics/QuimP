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
 * maintained with the same stats. They can be collected calling {@link getStatH()}
 * @author tyson
 * @author p.baniukiewicz
 */
public class CellStatsEval implements Measurements {
    /**
     * Hold all stats for cell.
     */
    private CellStats statH;
    OutlineHandler outputH;
    File OUTFILE;
    ImagePlus iPlus;
    ImageProcessor iProc;
    ImageStatistics is;
    // Analyzer analyser;
    // ResultsTable results;
    double scale;
    double frameInterval;
    // private static final int m = Measurements.AREA +
    // Measurements.INTEGRATED_DENSITY + Measurements.MEAN +
    // Measurements.MEDIAN + Measurements.STD_DEV+
    // Measurements.CENTROID;
    // private static final int m = Measurements.AREA +
    // Measurements.CIRCULARITY +
    // Measurements.CENTROID + Measurements.SHAPE_DESCRIPTORS +
    // Measurements.PERIMETER + Measurements.ELLIPSE;

    public CellStatsEval(OutlineHandler oH, ImagePlus ip, File f, double s, double fI) {
        IJ.showStatus("BOA-Calculating Cell stats");
        outputH = oH;
        OUTFILE = f;
        iPlus = ip;
        iProc = ip.getProcessor();
        scale = s;
        frameInterval = fI;

        // Analyzer.setMeasurements(m);
        // results = new ResultsTable();
        // analyser = new Analyzer(ip,m,results);
        // results.setDefaultHeadings();
        // analyser.setMeasurements(m);

        FrameStatistics[] stats = record();
        iPlus.setSlice(1);
        iPlus.killRoi();
        write(stats, outputH.getStartFrame());
    }

    /**
     * Calculate stats.
     * 
     * <p><b>Warning</b><p>
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
            PrintWriter pw = new PrintWriter(new FileWriter(OUTFILE), true); // auto
                                                                             // flush
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
     * Complementary to write method.
     * Create the same data as write but in form of arrays.
     * For compatible reasons.
     * 
     * @param s Frame statistics calculated by {@link uk.ac.warwick.wsbc.QuimP.CellStat.record()}
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

    // private int cellAge(ImageStatistics is) {
    // double x, y, a, b, angle;
    // x = is.xCentroid;
    // y = is.yCentroid;
    // a = is.major;
    // b = is.minor;
    // angle = is.angle;
    //
    // Line.setWidth(10);
    // Line l = majorAxis(x, y, a / 2, b / 2, angle);
    //
    // iPlus.setRoi(l);
    // ProfilePlot pp = new ProfilePlot(iPlus, false);
    // double[] profile = pp.getProfile();
    // return Tool.findNumPeaks(profile, 5) - 1;
    // }

    // private Line majorAxis(double x, double y, double a, double b, double
    // angle) {
    // double beta = -angle * (Math.PI / 180);
    // double alpha, X,Y;
    // double ax1 = -1.;
    // double ay1 = -1.;
    // double bx1 = -1.;
    // double by1 = -1.;
    // double ax2 = -1.;
    // double ay2 = -1.;
    // double bx2 = -1.;
    // double by2 = -1.;
    //
    // for (int i = 0; i <= 360; i += 2) {
    // alpha = i * (Math.PI / 180);
    // X = x + a * Math.cos(alpha) * Math.cos(beta) - b * Math.sin(alpha) *
    // Math.sin(beta);
    // Y = y + a * Math.cos(alpha) * Math.sin(beta) + b * Math.sin(alpha) *
    // Math.cos(beta);
    //
    // if (i == 0) {
    // ax1 = X;
    // ay1 = Y;
    // }
    // if (i == 90) {
    // bx1 = X;
    // by1 = Y;
    // }
    // if (i == 180) {
    // ax2 = X;
    // ay2 = Y;
    // }
    // if (i == 270) {
    // bx2 = X;
    // by2 = Y;
    // }
    // }
    // return new Line(ax1, ay1, ax2, ay2);
    // }
}
