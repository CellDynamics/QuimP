package uk.ac.warwick.wsbc.quimp.plugin.qanalysis;

import java.io.File;

import uk.ac.warwick.wsbc.quimp.QParams;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/**
 * Configuration class for Q_Analysis.
 * 
 * @author rtyson
 *
 */
class Qp {

  public static File snQPfile;
  public static File stQPfile;
  public static File outFile;
  public static String filename;
  public static double scale = 1; // pixel size in microns
  public static double frameInterval = 1; // frames per second
  static int startFrame;
  static int endFrame;
  public static double fps = 1; // frames per second
  public static int increment = 1;
  public static String trackColor;
  public static String[] outlinePlots = { "Speed", "Fluorescence", "Convexity" };
  public static String outlinePlot;
  public static double sumCov = 1;
  public static double avgCov = 0;
  public static int mapRes = 400;
  public static int channel = 0;
  static boolean singleImage = false;
  static boolean useDialog = true;
  static final boolean Build3D = false;

  static void convexityToPixels() {
    avgCov /= scale; // convert to pixels
    sumCov /= scale;
  }

  static void convexityToUnits() {
    avgCov *= scale; // convert to pixels
    sumCov *= scale;
  }

  public Qp() {
  }

  /**
   * Copies selected data from QParams to this object.
   * 
   * @param qp General QuimP parameters object
   */
  static void setup(QParams qp) {
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