package uk.ac.warwick.wsbc.QuimP.plugin.ecmm;

import java.io.File;

import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.QParams;

/**
 * Container class holding parameters related to ECMM analysis.
 * 
 * @author rtyson
 *
 */
public class ECMp {

    static public File INFILE; // snQP file
    static public File OUTFILE;
    static public double scale;
    static public double frameInterval;
    static public int startFrame, endFrame;
    static public ImageProcessor image;
    static public int numINTS;
    static public boolean ANA;
    static public boolean plot;
    static public boolean lineCharges;
    static public double markerRes; // resolution of outlines
    static public double chargeDensity;// field complexity (set to -1 to leave
                                       // as marker density)
    static public double maxVertF; // max force allowed on a marker (0.06)
    static public double migPower;
    static public double tarPower;
    static public double migQ; // was 0.4E-6
    static public double tarQ;
    static public double mobileQ;
    static public double d;// threshold distance to stop
    static public double w; // size of displacment of mig edge charges
    static public double h; // Euler time step, was 0.6
    static public int maxIter;
    static public double k;
    static public double anaMigDist;
    static public boolean forceNoSectors;
    static public boolean forceForwardMapping;
    static public boolean forceBackwardMapping;
    static public boolean disableDensityCorrections;
    static public int its; // total euler iterations
    static public int unSnapped; // number of nodes that failed to snap
    static public int visualRes;
    static public double maxCellSize;

    static boolean drawIntersects;
    static boolean drawInitialOutlines;
    static boolean drawSolutionOutlines;
    static boolean drawPaths;
    static boolean drawFails;
    static boolean saveTemp;
    static boolean inspectSectors;
    static boolean preserveHeads = false; //!< true if original head should be preserved

    public ECMp() {
    }

    /**
     * Defines default values for ECMM algorithm
     * 
     * @param maxCellLength Maximal length of cell
     */
    public static void setParams(double maxCellLength) {
        maxCellSize = maxCellLength / Math.PI; // guess cell diameter

        lineCharges = true;
        markerRes = 4; // resolution of outlines (set to 0 to not alter density,
                       // set negative to only alter at first frame)
        chargeDensity = -1; // field complexity (set to -1 to leave as marker
                            // density)
        maxVertF = 0.1; // max force allowed on a marker (0.06)
        migPower = 2;
        tarPower = 2;
        migQ = 0.5E-6; // was 0.4E-6
        tarQ = -0.5E-6; // was -2.5E-5
        mobileQ = 0.1E-5;
        d = 0.2; // threshold distance to stop
        w = 0.01; // size of displacment of mig edge charges (0.01)
        h = 0.3; // Euler time step, was 0.6
        maxIter = 4000;
        k = 8.987E9;
        // static public boolean plot = true;
        inspectSectors = true;
        forceNoSectors = false;
        forceForwardMapping = false;
        forceBackwardMapping = false; // takes priority
        disableDensityCorrections = false;
        its = 0; // total euler iterations
        unSnapped = 0; // number of nodes that failed to snap
        visualRes = 300; // set to 200! $

        saveTemp = false; // set to false!! $
        drawIntersects = true; // set to true!! $
        drawInitialOutlines = true; // set to true!! $
        drawSolutionOutlines = true; // set to true!! $
        drawPaths = true;
        drawFails = true;

    }

    /**
     * Fills ECMp fields with values from previous analysis (master paQP file)
     * 
     * @param qp  Master configuration file
     */
    static void setup(QParams qp) {
        INFILE = qp.getSnakeQP();
        OUTFILE = new File(ECMp.INFILE.getAbsolutePath()); // output file (.snQP) file
        scale = qp.getImageScale();
        frameInterval = qp.getFrameInterval();
        // markerRes = qp.nodeRes;
        startFrame = qp.getStartFrame();
        endFrame = qp.getEndFrame();
        ECMp.ANA = false;
        ECMp.plot = true;
    }
}