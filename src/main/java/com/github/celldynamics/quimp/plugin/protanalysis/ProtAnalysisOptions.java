package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Color;

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
   * Show tracked point on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean guiShowPoint = new MutableBoolean(true);
  /**
   * Show track on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean guiShowTrack = new MutableBoolean(true);
  /**
   * Show tracks on motility map. VisualTracking UI Option.
   */
  public MutableBoolean guiShowTrackMotility = new MutableBoolean(true);
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
   * 
   * @see ActionTrackPoints
   */
  public OutlinesToImage selOutlineColoring = new OutlinesToImage();
  /**
   * Type of gradinet point in polar plot.
   */
  public PolarPlot selrelativePolar = new PolarPlot();

  /**
   * Sensitivity of maximum detection.
   */
  public double noiseTolerance = 1.5;
  /**
   * Percentage of drop from maximum of motility map to consider point in tracking line.
   */
  public double dropValue = 1;

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
    UNIFORM,
    /**
     * The motility only.
     */
    MOTILITY,
    /**
     * The convexity only.
     */
    CONVEXITY,
    /**
     * Convex and expanding parts.
     */
    CONVANDEXP,
    /**
     * Concave and retracting parts.
     */
    CONCANDRETR,
    /**
     * CONCANDRETR + CONVANDEXP.
     */
    BOTH
  }

  /**
   * Types of gradient points.
   * <ol>
   * <li>SCREENPOINT - any point clicked on image. Given as {x,y} coordinates
   * <li>OUTLINEPOINT - point on outline. Given as number of this point on perimeter.
   * <li>....
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
     * Left bottom corner.
     */
    LB_CORNER,
    /**
     * Left upper corner.
     */
    LU_CORNER,
    /**
     * Right bottom corner.
     */
    RB_CORNER,
    /**
     * Right upper corner.
     */
    RU_CORNER
  }

  /**
   * Keep position of gradient, a point on image or a point on outline.
   * 
   * @author p.baniukiewicz
   *
   */
  class PolarPlot implements IEnumDataType {
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

    @Override
    public void setCurrent(Enum<?> val) {
      type = (GradientType) val;

    }
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

}
