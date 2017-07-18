package com.github.celldynamics.quimp.plugin.ana;

import java.io.File;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

/**
 * Container class for parameters concerned with ANA analysis.
 * 
 * <p>This class is serialized through
 * {@link com.github.celldynamics.quimp.filesystem.ANAParamCollection}.
 * The structure of transient and non-transient fields must be reflected in FormatConverter.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 * @see FormatConverter#doConversion()
 */
public class ANAp {

  /**
   * Array of fluorescent channels.
   */
  public File[] fluTiffs;
  /**
   * Input file reference.
   * 
   * <p>snQP file if run from paQP.
   */
  public transient File inFile;
  /**
   * Output snQP file reference (the same as {@link #inFile}.
   */
  public transient File outFile;
  /**
   * Reference to Stats file.
   * 
   * <p>Used in QCONF/paQP.
   */
  public transient File statFile;
  /**
   * Atomic step during contour shrinking.
   */
  public final transient double stepRes = 0.04; // step size in pixels
  /**
   * Angle to freeze neighbouring vertexes.
   */
  public final transient double freezeTh = 1;
  /**
   * Angle threshold.
   * 
   * @see Outline#scale(double, double, double, double)
   */
  public final transient double angleTh = 0.1;
  /**
   * @see com.github.celldynamics.quimp.Outline#setResolution(double)
   */
  public final transient double oneFrameRes = 1;
  /**
   * Image scale.
   * 
   * <p>Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient double scale = 1.0;
  /**
   * Frame interval.
   * 
   * <p>Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient double frameInterval;
  /**
   * Frame range.
   * 
   * <p>Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient int startFrame;
  /**
   * Frame range.
   * 
   * <p>Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient int endFrame;

  /**
   * The normalise. UI setting
   */
  transient boolean normalise = true;

  /**
   * The sample at same. UI setting
   */
  transient boolean sampleAtSame = false;

  /**
   * UI setting, plot outlines on new image.
   */
  transient boolean plotOutlines = false;

  /**
   * The present data.
   */
  transient int[] presentData;

  /**
   * The cleared.
   */
  transient boolean cleared;

  /**
   * The no data.
   */
  transient boolean noData;

  /**
   * The channel. UI setting
   */
  transient int channel = 0;

  /**
   * The use loc from ch.UI setting
   */
  transient int useLocFromCh;

  private double cortexWidthPixel; // in pixels
  private double cortexWidthScale; // at scale

  /**
   * Default constructor.
   */
  public ANAp() {
    fluTiffs = new File[3];
    fluTiffs[0] = new File("/");
    fluTiffs[1] = new File("/");
    fluTiffs[2] = new File("/");
    presentData = new int[3];
    setCortextWidthScale(0.7); // default value
  }

  /**
   * Copy constructor.
   * 
   * @param src source to copy from
   */
  public ANAp(ANAp src) {
    this.inFile = new File(src.inFile.getAbsolutePath());
    this.outFile = new File(src.outFile.getAbsolutePath());
    this.statFile = new File(src.statFile.getAbsolutePath());
    this.cortexWidthPixel = src.cortexWidthPixel;
    this.cortexWidthScale = src.cortexWidthScale;
    this.scale = src.scale;
    this.frameInterval = src.frameInterval;
    this.startFrame = src.startFrame;
    this.endFrame = src.endFrame;
    this.normalise = src.normalise;
    this.sampleAtSame = src.sampleAtSame;
    this.presentData = new int[src.presentData.length];
    System.arraycopy(src.presentData, 0, this.presentData, 0, src.presentData.length);
    this.cleared = src.cleared;
    this.noData = src.noData;
    this.channel = src.channel;
    this.useLocFromCh = src.useLocFromCh;
    this.plotOutlines = src.plotOutlines;

    this.fluTiffs = new File[src.fluTiffs.length];
    for (int i = 0; i < fluTiffs.length; i++) {
      fluTiffs[i] = new File(src.fluTiffs[i].getPath());
    }

  }

  /**
   * Initiates ANAp class with parameters copied from BOA analysis.
   * 
   * @param qp reference to QParams container (master file and BOA params)
   */
  void setup(QParams qp) {
    channel = 0;
    inFile = qp.getSnakeQP();
    outFile = new File(inFile.getAbsolutePath()); // output file (.snQP) file
    statFile = new File(qp.getStatsQP().getAbsolutePath()); // output file
    // (.stQP.csv) file
    scale = qp.getImageScale();
    frameInterval = qp.getFrameInterval();
    setCortextWidthScale(qp.cortexWidth);
    startFrame = qp.getStartFrame();
    endFrame = qp.getEndFrame();
    cleared = false;
    noData = true;
  }

  /**
   * Set cortex scale.
   * 
   * @param c the scale
   */
  public void setCortextWidthScale(double c) {
    cortexWidthScale = c;
    cortexWidthPixel = QuimpToolsCollection.distanceFromScale(cortexWidthScale, scale);
  }

  /**
   * Get cortex width in pixels.
   * 
   * @return the cortexWidthPixel
   */
  public double getCortexWidthPixel() {
    return cortexWidthPixel;
  }

  /**
   * Get cortex scale.
   * 
   * @return cortexWidthScale
   */
  public double getCortexWidthScale() {
    return cortexWidthScale;
  }

  /**
   * Set cortex widh in pixels.
   * 
   * @param cortexWidthPixel the cortexWidthPixel to set
   */
  public void setCortexWidthPixel(double cortexWidthPixel) {
    this.cortexWidthPixel = cortexWidthPixel;
  }
}