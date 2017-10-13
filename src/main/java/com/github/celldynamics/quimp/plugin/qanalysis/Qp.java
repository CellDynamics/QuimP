package com.github.celldynamics.quimp.plugin.qanalysis;

import java.io.File;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * Configuration class for Q_Analysis.
 * 
 * @author rtyson
 *
 */
public class Qp {

  /**
   * snQP file.
   */
  public static File snQPfile;
  /**
   * stQP file.
   */
  public static File stQPfile;

  /**
   * Full path to output file.
   * 
   * <p>Include {@value #filename}
   */
  public static File outFile;

  /** The core of filename. */
  public static String filename;

  /** Pixel size in microns. */
  public static double scale = 1;

  /** The frame interval. */
  public static double frameInterval = 1;

  /** The start frame. */
  static int startFrame;

  /** The end frame. */
  static int endFrame;

  /**
   * Frames per second.
   * 
   * <p>1/{@value #frameInterval}
   */
  public static double fps = 1;

  /** The increment. */
  public static int increment = 1;

  /** The track color. */
  public static String trackColor;

  /** The outline plots. */
  public static String[] outlinePlots = { "Speed", "Fluorescence", "Convexity" };

  /** The outline plot. UI element. */
  public static String outlinePlot;

  /** The sum cov. UI element. */
  public static double sumCov = 1;

  /** The avg cov. UI element. */
  public static double avgCov = 0;

  /** The map resolution. */
  public static int mapRes = 400;

  /** The channel. */
  public static int channel = 0; // TODO Remove

  /** The single image. */
  static boolean singleImage = false;

  /** If use dialog. */
  static boolean useDialog = true;

  /** The Constant Build3D. */
  static final boolean Build3D = false;

  /**
   * Convexity to pixels.
   */
  static void convexityToPixels() {
    avgCov /= scale; // convert to pixels
    sumCov /= scale;
  }

  /**
   * Convexity to units.
   */
  static void convexityToUnits() {
    avgCov *= scale; // convert to pixels
    sumCov *= scale;
  }

  /**
   * Instantiates a new qp.
   */
  public Qp() {
  }

  /**
   * Copies selected data from QParams to this object.
   * 
   * @param qp General QuimP parameters object
   */
  public static void setup(QParams qp) {
    Qp.snQPfile = qp.getSnakeQP();
    Qp.scale = qp.getImageScale();
    Qp.frameInterval = qp.getFrameInterval();
    Qp.filename = QuimpToolsCollection.removeExtension(Qp.snQPfile.getName());
    Qp.outFile = new File(Qp.snQPfile.getParent() + File.separator + Qp.filename);
    Qp.startFrame = qp.getStartFrame();
    Qp.endFrame = qp.getEndFrame();
    // File p = qp.paramFile;
    fps = 1d / frameInterval;
    singleImage = false;
    useDialog = true;
  }
}