package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import java.io.File;

import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.QParams;

// TODO: Auto-generated Javadoc
/**
 * Container class holding parameters related to ECMM analysis.
 * 
 * @author rtyson
 *
 */
public class ECMp {

  /**
   * 
   */
  static public File INFILE; // snQP file
  /**
   * 
   */
  static public File OUTFILE;
  /**
   * 
   */
  static public double scale;
  /**
   * 
   */
  static public double frameInterval;
  /**
   * 
   */
  static public int startFrame;
  /**
   * 
   */
  static public int endFrame;
  /**
   * 
   */
  static public ImageProcessor image;
  /**
   * 
   */
  static public int numINTS;
  /**
   * 
   */
  static public boolean ANA;
  /**
   * 
   */
  static public boolean plot;
  /**
   * 
   */
  static public boolean lineCharges;
  /**
   * resolution of outlines
   */
  static public double markerRes;
  /**
   * field complexity (set to -1 to leave as marker density)
   */
  static public double chargeDensity;
  /**
   * max force allowed on a marker (0.06)
   */
  static public double maxVertF;
  /**
   * 
   */
  static public double migPower;
  /**
   * 
   */
  static public double tarPower;
  /**
   * 
   */
  static public double migQ; // was 0.4E-6
  /**
   * 
   */
  static public double tarQ;
  /**
   * 
   */
  static public double mobileQ;
  /**
   * threshold distance to stop
   */
  static public double d;
  /**
   * size of displacment of mig edge charges
   */
  static public double w;
  /**
   * Euler time step, was 0.6
   */
  static public double h;
  /**
   * 
   */
  static public int maxIter;
  /**
   * 
   */
  static public double k;
  /**
   * 
   */
  static public double anaMigDist;
  /**
   * 
   */
  static public boolean forceNoSectors;
  /**
   * 
   */
  static public boolean forceForwardMapping;
  /**
   * 
   */
  static public boolean forceBackwardMapping;
  /**
   * 
   */
  static public boolean disableDensityCorrections;
  /**
   * total euler iterations
   */
  static public int its;
  /**
   * number of nodes that failed to snap
   */
  static public int unSnapped;
  /**
   * 
   */
  static public int visualRes;
  /**
   * 
   */
  static public double maxCellSize;

  /**
   * The draw intersects.
   */
  static boolean drawIntersects;

  /**
   * The draw initial outlines.
   */
  static boolean drawInitialOutlines;

  /**
   * The draw solution outlines.
   */
  static boolean drawSolutionOutlines;

  /**
   * The draw paths.
   */
  static boolean drawPaths;

  /**
   * The draw fails.
   */
  static boolean drawFails;

  /**
   * The save temp.
   */
  static boolean saveTemp;

  /**
   * The inspect sectors.
   */
  static boolean inspectSectors;

  /**
   * The preserve heads.
   */
  static boolean preserveHeads = false; //!< true if original head should be preserved

  /**
   * 
   */
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
   * @param qp Master configuration file
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