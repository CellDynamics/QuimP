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

  /**
   * Color of selected point on the outline when hoover the mouse.
   */
  public static Color pointColor = Color.CYAN; // box color
  /**
   * Color of selected point.
   */
  public static Color staticPointColor = Color.YELLOW; // box color
  /**
   * Size of the point when selected.
   */
  public static int staticPointSize = 20;
  /**
   * Size of the point when hoover.
   */
  public static int pointSize = 10; // box size
  /**
   * Prefix added to Roi in ROI Manager.
   */
  public static final String roiPrefix = "pa_cell_";
  /**
   * Radius of circles plotted by tool.
   */
  public double circleRadius = 10;
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
  public MutableBoolean chbNewImage = new MutableBoolean(true);
  /**
   * Whether to flatten static track. VisualTracking UI Option.
   */
  public MutableBoolean chbFlattenStaticTrackImage = new MutableBoolean(false);
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
  public MutableBoolean chbSmoothTracks = new MutableBoolean(false);
  /**
   * Show tracked point on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean chbShowPoint = new MutableBoolean(true);
  /**
   * Show track on dynamic track. VisualTracking UI Option.
   */
  public MutableBoolean chbShowTrack = new MutableBoolean(true);
  /**
   * Show tracks on motility map. VisualTracking UI Option.
   */
  public MutableBoolean chbShowTrackMotility = new MutableBoolean(true);
  /**
   * Cell used for generating plots. VisualTracking UI Option.
   */
  public MutableInt selActiveCellPlot = new MutableInt(0);
  /**
   * Cell used for generating maps. VisualTracking UI Option.
   */
  public MutableInt selActiveCellMap = new MutableInt(0);
  /**
   * Active channel for plotting maps. VisualTracking UI Option.
   */
  public MutableInt selActiveChannel = new MutableInt(CH1);
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
  public MutableBoolean chbPersistencePlot = new MutableBoolean(false);
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
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbTotFluPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbMeanFluPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbCortexWidthPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbCytoAreaPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbTotalCytoPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbMeanCtfPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbMeanCytoPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbCortexAreaPlot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbTotalCtf2Plot = new MutableBoolean(false);
  /**
   * Plot 2D. VisualTracking UI Option.
   */
  public MutableBoolean chbManCtfPlot = new MutableBoolean(false);
  /**
   * Hold configuration for plotting outlines of cells on stack of images.
   * 
   * @see ActionTrackPoints
   */
  public OutlinesToImage selOutlineColoring = new OutlinesToImage();
  /**
   * Gradient point for polar plot.
   */
  public Point2d gradientPoint = new Point2d(0, 0);
  /**
   * Indicate that pick point mode is on.
   * 
   * @see CustomCanvas#mouseClicked(java.awt.event.MouseEvent)
   */
  public MutableBoolean bnGradientPickActive = new MutableBoolean(false);
  /**
   * Save tracks to csv file.
   * 
   * <p>Currently not available from UI. Maps are saved always under fixed name.
   * 
   * @see ActionTrackPoints
   */
  public MutableBoolean saveTracks = new MutableBoolean(true);

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
