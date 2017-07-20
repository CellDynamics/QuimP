package com.github.celldynamics.quimp.plugin.ecmm;

import java.io.File;

import com.github.celldynamics.quimp.QParams;

import ij.process.ImageProcessor;

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
  public static File INFILE; // snQP file
  /**
   * 
   */
  public static File OUTFILE;
  /**
   * 
   */
  public static double scale;
  /**
   * 
   */
  public static double frameInterval;
  /**
   * 
   */
  public static int startFrame;
  /**
   * 
   */
  public static int endFrame;
  /**
   * 
   */
  public static ImageProcessor image;
  /**
   * 
   */
  public static int numINTS;
  /**
   * 
   */
  public static boolean ANA;
  /**
   * 
   */
  public static boolean plot;
  /**
   * 
   */
  public static boolean lineCharges;
  /**
   * resolution of outlines
   */
  public static double markerRes;
  /**
   * field complexity (set to -1 to leave as marker density)
   */
  public static double chargeDensity;
  /**
   * max force allowed on a marker (0.06)
   */
  public static double maxVertF;
  /**
   * 
   */
  public static double migPower;
  /**
   * 
   */
  public static double tarPower;
  /**
   * 
   */
  public static double migQ; // was 0.4E-6
  /**
   * 
   */
  public static double tarQ;
  /**
   * 
   */
  public static double mobileQ;
  /**
   * threshold distance to stop
   */
  public static double d;
  /**
   * size of displacment of mig edge charges
   */
  public static double w;
  /**
   * Euler time step, was 0.6
   */
  public static double h;
  /**
   * 
   */
  public static int maxIter;
  /**
   * 
   */
  public static double k;
  /**
   * 
   */
  public static double anaMigDist;
  /**
   * 
   */
  public static boolean forceNoSectors;
  /**
   * 
   */
  public static boolean forceForwardMapping;
  /**
   * 
   */
  public static boolean forceBackwardMapping;
  /**
   * 
   */
  public static boolean disableDensityCorrections;
  /**
   * total euler iterations
   */
  public static int its;
  /**
   * number of nodes that failed to snap
   */
  public static int unSnapped;
  /**
   * 
   */
  public static int visualRes;
  /**
   * 
   */
  public static double maxCellSize;

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
   * Default constructor.
   */
  public ECMp() {
  }

  /**
   * Defines default values for ECMM algorithm.
   * 
   * @param maxCellLength Maximal length of cell
   */
  public static void setParams(double maxCellLength) {
    maxCellSize = maxCellLength / Math.PI; // guess cell diameter

    lineCharges = true;
    // resolution of outlines (set to 0 to not alter density, set negative to only alter at first
    // frame)
    markerRes = 4;
    chargeDensity = -1; // field complexity (set to -1 to leave as marker density)
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
   * Fills ECMp fields with values from previous analysis (master paQP file).
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