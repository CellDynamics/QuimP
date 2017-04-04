package uk.ac.warwick.wsbc.quimp.plugin.ana;

import java.io.File;

import uk.ac.warwick.wsbc.quimp.FormatConverter;
import uk.ac.warwick.wsbc.quimp.QParams;
import uk.ac.warwick.wsbc.quimp.geom.filters.OutlineProcessor;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * Container class for parameters concerned with ANA analysis.
 * 
 * This class is serialized through {@link uk.ac.warwick.wsbc.quimp.filesystem.ANAParamCollection}.
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
   * snQP file if run from paQP.
   */
  public transient File INFILE;
  /**
   * Output snQP file reference (the same as {@link #INFILE}
   */
  public transient File OUTFILE;
  /**
   * Reference to Stats file.
   * 
   * USed in QCONF/paQP.
   */
  public transient File STATSFILE;
  /**
   * Atomic step during contour shrinking.
   */
  public final transient double stepRes = 0.04; // step size in pixels
  /**
   * Angle to freeze neighbouring vertexes.
   */
  public final transient double freezeTh = 1;
  /**
   * @see OutlineProcessor#shrink(double, double, double, double)
   */
  public final transient double angleTh = 0.1;
  /**
   * @see uk.ac.warwick.wsbc.quimp.Outline#setResolution(double)
   */
  public final transient double oneFrameRes = 1;
  /**
   * Image scale.
   * 
   * Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient double scale = 1.0;
  /**
   * Frame interval.
   * 
   * Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient double frameInterval;
  /**
   * Frame range.
   * 
   * Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
   */
  public transient int startFrame;
  /**
   * Frame range.
   * 
   * Initialised by {@link #setup(QParams)} from loaded QCONF/paQP.
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
   * UI setting. Show results in IJ table at the end
   */
  transient boolean fluoResultTable = false;

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
   * @param src
   */
  public ANAp(ANAp src) {
    this.INFILE = new File(src.INFILE.getAbsolutePath());
    this.OUTFILE = new File(src.OUTFILE.getAbsolutePath());
    this.STATSFILE = new File(src.STATSFILE.getAbsolutePath());
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
   * Initiates ANAp class with parameters copied from BOA analysis
   * 
   * @param qp reference to QParams container (master file and BOA params)
   */
  void setup(QParams qp) {
    channel = 0;
    INFILE = qp.getSnakeQP();
    OUTFILE = new File(INFILE.getAbsolutePath()); // output file (.snQP) file
    STATSFILE = new File(qp.getStatsQP().getAbsolutePath()); // output file
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
   * 
   * @param c the scale
   */
  public void setCortextWidthScale(double c) {
    cortexWidthScale = c;
    cortexWidthPixel = QuimpToolsCollection.distanceFromScale(cortexWidthScale, scale);
  }

  /**
   * @return the cortexWidthPixel
   */
  public double getCortexWidthPixel() {
    return cortexWidthPixel;
  }

  /**
   * 
   * @return cortexWidthScale
   */
  public double getCortexWidthScale() {
    return cortexWidthScale;
  }

  /**
   * @param cortexWidthPixel the cortexWidthPixel to set
   */
  public void setCortexWidthPixel(double cortexWidthPixel) {
    this.cortexWidthPixel = cortexWidthPixel;
  }
}