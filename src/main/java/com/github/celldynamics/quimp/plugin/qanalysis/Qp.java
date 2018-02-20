package com.github.celldynamics.quimp.plugin.qanalysis;

import java.io.File;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

/**
 * Configuration class for Q_Analysis.
 * 
 * @author rtyson
 *
 */
public class Qp extends AbstractPluginOptions {

  /**
   * snQP file.
   */
  public transient File snQPfile;
  /**
   * stQP file.
   */
  public transient File stQPfile;

  /**
   * Full path to output file.
   * 
   * <p>Include {@link #filename}
   */
  public transient File outFile;

  /** The core of filename. */
  public transient String filename;

  /** Pixel size in microns. */
  public transient double scale = 1;

  /** The frame interval. */
  public transient double frameInterval = 1;

  /** The start frame. */
  transient int startFrame;

  /** The end frame. */
  transient int endFrame;

  /**
   * Frames per second.
   * 
   * <p>1/{@link #frameInterval}
   */
  public transient double fps = 1;

  /** The increment. */
  public transient int increment = 1;

  /** The track color. */
  public transient String trackColor;

  /** The outline plots. */
  public transient String[] outlinePlots = { "Speed", "Fluorescence", "Convexity" };

  /** The outline plot. UI element. */
  public transient String outlinePlot;

  /** The sum cov. UI element. */
  public double sumCov = 1;

  /** The avg cov. UI element. */
  public double avgCov = 0;

  /** The map resolution. */
  public int mapRes = 400;

  /** The channel. */
  public transient int channel = 0; // TODO Remove

  /** The single image. */
  transient boolean singleImage = false;

  /** If use dialog. */
  transient boolean useDialog = true;

  /** The Constant Build3D. */
  final transient boolean Build3D = false;

  /**
   * Convexity to pixels.
   */
  void convexityToPixels() {
    avgCov /= scale; // convert to pixels
    sumCov /= scale;
  }

  /**
   * Convexity to units.
   */
  void convexityToUnits() {
    avgCov *= scale; // convert to pixels
    sumCov *= scale;
  }

  /**
   * Instantiates a new qp.
   */
  public Qp() {
  }

  /**
   * Allow to add file name to options.
   * 
   * <p>For convenient creation of {@link Q_Analysis} for API.
   * 
   * @param file file to add
   */
  public Qp(File file) {
    paramFile = file.getPath();
  }

  /**
   * Copies selected data from QParams to this object.
   * 
   * @param qp General QuimP parameters object
   */
  public void setup(QParams qp) {
    snQPfile = qp.getSnakeQP();
    scale = qp.getImageScale();
    frameInterval = qp.getFrameInterval();
    filename = QuimpToolsCollection.removeExtension(snQPfile.getName());
    outFile = new File(snQPfile.getParent() + File.separator + filename);
    startFrame = qp.getStartFrame();
    endFrame = qp.getEndFrame();
    // File p = qp.paramFile;
    fps = 1d / frameInterval;
    singleImage = false;
    useDialog = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    Qp cp = new Qp();
    cp.paramFile = this.paramFile;
    if (this.snQPfile != null && this.snQPfile.getPath() != null) {
      cp.snQPfile = new File(this.snQPfile.getPath());
    }
    if (this.stQPfile != null && this.stQPfile.getPath() != null) {
      cp.stQPfile = new File(this.stQPfile.getPath());
    }
    if (this.outFile != null && this.outFile.getPath() != null) {
      cp.outFile = new File(this.outFile.getPath());
    }
    cp.filename = this.filename;
    cp.scale = this.scale;
    cp.frameInterval = this.frameInterval;
    cp.startFrame = this.startFrame;
    cp.endFrame = this.endFrame;
    cp.fps = this.fps;
    cp.increment = this.increment;
    cp.trackColor = this.trackColor;
    cp.outlinePlot = this.outlinePlot;
    cp.sumCov = this.sumCov;
    cp.avgCov = this.avgCov;
    cp.mapRes = this.mapRes;
    cp.channel = this.channel;
    cp.singleImage = this.singleImage;
    cp.useDialog = this.useDialog;

    return cp;
  }

}