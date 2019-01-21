package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

/**
 * Hold all configuration for Protrusion Analysis Module.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisOptions extends AbstractPluginOptions implements IQuimpSerialize {

  public static Color pointColor = Color.CYAN; // box color
  public static Color staticPointColor = Color.YELLOW; // box color
  public static int staticPointSize = 20;
  public static int pointSize = 10; // box size
  /**
   * Prefix added to Roi in ROI Manager.
   */
  public static final String roiPrefix = "pa_cell_";

  /**
   * Available relative positions of reference point for polar plots.
   * 
   * @see #selrelativePolar
   */
  public static final String[] relativePolar = { "LEFT", "TOP", "SOMETHING" };
  /**
   * Vale for static plot.
   * 
   * @see #plotStaticDynamic
   */
  public static final int PLOT_STATIC = 0;
  /**
   * Vale for dynamic plot.
   * 
   * @see #plotStaticDynamic
   */
  public static final int PLOT_DYNAMIC = 1;
  /**
   * Channel for tables.
   */
  public static final int CH1 = 0;
  /**
   * Channel for tables.
   */
  public static final int CH2 = 1;
  /**
   * Channel for tables.
   */
  public static final int CH3 = 2;
  /**
   * Whether to show tracks in new plot or in embedded in GUI. VisualTracking UI Option.
   */
  public MutableBoolean guiNewImage = new MutableBoolean(true);
  /**
   * Whether to flatten static track. VisualTracking UI Option.
   */
  public MutableBoolean guiFlattenStaticTrackImage = new MutableBoolean(false);
  /**
   * Radius of circles plotted by tool.
   */
  double circleRadius = 10;
  /**
   * Plot dynamic or static plots. VisualTracking UI Option.
   * 
   * <p>0 - static, 1 - dynamic
   * 
   * @see #PLOT_DYNAMIC
   * @see #PLOT_STATIC
   */
  public MutableInt plotStaticDynamic = new MutableInt(PLOT_STATIC);
  /**
   * Apply track smoothing. VisualTracking UI Option.
   */
  public MutableBoolean guiSmoothTracks = new MutableBoolean(false);
  /**
   * Selected relative point for polar plot.
   * 
   * @see #relativePolar
   */
  public MutableInt selrelativePolar = new MutableInt(0);
  /**
   * Show tracked point on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean guiShowPoint = new MutableBoolean(true);
  /**
   * Show track on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean guiShowTrack = new MutableBoolean(true);
  /**
   * Cell used for generating plots. VisualTracking UI Option.
   */
  public MutableInt activeCellPlot = new MutableInt(0);
  /**
   * Cell used for generating maps. VisualTracking UI Option.
   */
  public MutableInt activeCellMap = new MutableInt(0);
  /**
   * Active channel for plotting maps. VisualTracking UI Option.
   */
  public MutableInt activeChannel = new MutableInt(CH1);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbXcentrPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbYcentrPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbDisplPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbDistPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbDirectPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbSpeedPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbPerimPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbElongPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbCircPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbAreaPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbTotFluPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbMeanFluPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbCortexWidthPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbCytoAreaPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbTotalCtfPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbMeanCtfPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbCortexAreaPlot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbTotalCtf2Plot = new MutableBoolean(false);
  /**
   * Plot map. VisualTracking UI Option.
   */
  public MutableBoolean chbManCtfPlot = new MutableBoolean(false);
  /**
   * Hold configuration for plotting outlines of cells on stack of images.
   */
  public OutlinesToImage selOutlineColoring = new OutlinesToImage();

  /**
   * Sensitivity of maximum detection.
   */
  public double noiseTolerance = 1.5;
  /**
   * Percentage of drop from maximum of motility map to consider point in tracking line.
   */
  public double dropValue = 1;

  /**
   * Type of plots to show.
   */
  public boolean plotMotmap = false;
  /**
   * Type of plots to show.
   */
  public boolean plotMotmapmax = true;
  /**
   * Type of plots to show.
   */
  public boolean plotConmap = false;
  /**
   * Type of plots to show.
   */
  public boolean plotOutline = false;
  /**
   * Type of plots to show.
   */
  public boolean plotStaticmax = true;
  /**
   * Type of plots to show.
   */
  public boolean plotDynamicmax = false;

  /**
   * Plot types supported by
   * {@link TrackVisualisation.Stack#addOutlinesToImage(STmap, ProtAnalysisOptions)}.
   * <ol>
   * <li>MOTILITY - only motility based outline.
   * <li>CONVEXITY - only convexity based outline.
   * <li>CONVANDEXP - parts that are convex and expanding.
   * <li>CONCANDRETR - parts that are concave and retracting.
   * <li>BOTH - combines CONVANDEXP and CONCANDRETR
   * </ol>
   * 
   * @author p.baniukiewicz
   *
   */
  public enum OutlinePlotTypes {

    /**
     * Just pure outline.
     */
    UNIFORM(0),
    /**
     * The motility only.
     */
    MOTILITY(1),
    /**
     * The convexity only.
     */
    CONVEXITY(2),
    /**
     * Convex and expanding parts.
     */
    CONVANDEXP(3),
    /**
     * Concave and retracting parts.
     */
    CONCANDRETR(4),
    /**
     * CONCANDRETR + CONVANDEXP.
     */
    BOTH(5);

    private final int value;

    private OutlinePlotTypes(int value) {
      this.value = value;
    }

    /**
     * Get enum value.
     * 
     * @return value
     */
    public int getValue() {
      return value;
    }
  }

  /**
   * Types of gradient points.
   * <ol>
   * <li>SCREENPOINT - any point clicked on image. Given as {x,y} coordinates
   * <li>OUTLINEPOINT - point on outline. Given as number of this point on perimeter.
   * <li>NOTDEFINED - not defined or selected.
   * </ol>
   * 
   * @author p.baniukiewicz
   *
   */
  public enum GradientType {

    /**
     * Point picked from screen.
     */
    SCREENPOINT,
    /**
     * Point on the outline.
     */
    OUTLINEPOINT,
    /**
     * The notdefined.
     */
    NOTDEFINED
  }

  /**
   * Keep position of gradient, a point on image or a point on outline.
   * 
   * @author p.baniukiewicz
   *
   */
  class PolarPlot {
    /**
     * Indicate whether to use selected gradient or not.
     */
    public boolean useGradient = false;
    /**
     * Indicate whether to plot polar plots.
     */
    public boolean plotpolar = false;
    /**
     * Type of gradient point.
     * 
     * @see GradientType
     */
    public GradientType type = GradientType.SCREENPOINT;
    /**
     * Coordinates of gradient point if type is SCREENPOINT.
     */
    public Point2d gradientPoint = new Point2d(0, 0);
    /**
     * Number of outline point chosen as gradient if type is OUTLINEPOINT.
     */
    public int gradientOutline;
  }

  /**
   * Configuration for static plot.
   * 
   * @author p.baniukiewicz
   *
   */
  class StaticPlot {
    /**
     * Indicate whether to plot maxima point on image.
     */
    public boolean plotmax = true;
    /**
     * Indicate whether to plot track lines on image.
     */
    public boolean plottrack = true;
    /**
     * Indicate whether to average image.
     */
    public boolean averimage;
  }

  /**
   * Configuration for dynamic plot.
   * 
   * @author p.baniukiewicz
   *
   */
  class DynamicPlot {
    /**
     * Indicate whether to plot maxima point on image.
     */
    public boolean plotmax = true;
    /**
     * Indicate whether to plot track lines on image.
     */
    public boolean plottrack = true;
  }

  /**
   * Configuration of plotting outlines of cells on stack of images.
   * 
   * @author p.baniukiewicz
   * @see TrackVisualisation.Stack#addOutlinesToImage(STmap,ProtAnalysisOptions)
   */
  class OutlinesToImage implements IEnumDataType {
    /**
     * Default color of motility outline.
     */
    public Color motColor = Color.BLUE;
    /**
     * Default color of convexity outline.
     */
    public Color convColor = Color.RED;
    /**
     * Default color of outline.
     */
    public Color defColor = Color.GRAY;
    /**
     * Threshold above to which plot motility on outline.
     */
    public double motThreshold;
    /**
     * Threshold above to which plot convexity on outline.
     */
    public double convThreshold;
    /**
     * Define type of plot of outline.
     */
    public OutlinePlotTypes plotType;

    /**
     * Set default values.
     */
    public OutlinesToImage() {
      motThreshold = 0;
      convThreshold = 0;
      plotType = OutlinePlotTypes.UNIFORM;
    }

    @Override
    public void setCurrent(Enum<?> val) {
      plotType = (OutlinePlotTypes) val;
    }
  }

  /**
   * Base Interface used by {@link ActionUpdateOptionsEnum}.
   * 
   * <p>Every filed that uses Enum should implement this.
   * 
   * @author p.baniukiewicz
   *
   */
  interface IEnumDataType {
    public void setCurrent(Enum<?> val);
  }

  /**
   * Hold configuration for plotting static images.
   */
  public StaticPlot staticPlot = new StaticPlot();
  /**
   * Hold configuration for plotting dynamic images.
   */
  public DynamicPlot dynamicPlot = new DynamicPlot();
  /**
   * Hold configuration for gradient position.
   */
  public PolarPlot polarPlot = new PolarPlot();

  /**
   * Instantiates a new prot analysis config.
   */
  public ProtAnalysisOptions() {
  }

  @Override
  public void beforeSerialize() {
  }

  @Override
  public void afterSerialize() throws Exception {
  }

  /**
   * Convert enums to string array for filling selectors.
   * 
   * @param e enum class
   * @return enum names
   */
  public static String[] getNames(Class<? extends Enum<?>> e) {
    return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
  }

}
