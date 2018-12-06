package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Color;

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
   * Available coloring schemes for outline.
   */
  public static final String[] outlineColoring = { "UNIFORM", "MOTILITY", "CURVATURE" };
  /**
   * Available relative positions of reference point for polar plots.
   */
  public static final String[] relativePolar = { "LEFT", "TOP", "SOMETHING" };
  /**
   * Whether to show tracks in new plot or in embedded in GUI. VisualTracking UI Option.
   */
  public boolean guiNewImage = false;
  /**
   * Radius of circles plotted by tool.
   */
  double circleRadius = 10;
  /**
   * Plot static or dynamic plots. VisualTracking UI Option.
   */
  public boolean plotStatic = true;
  /**
   * Apply track smoothing. VisualTracking UI Option.
   */
  public boolean guiSmoothTracks = false;
  /**
   * Selected outline colouring mode. VisualTracking UI Option.
   */
  public String selOutlineColoring = outlineColoring[0];
  /**
   * Selected relative point for polar plot.
   */
  public String selrelativePolar = relativePolar[0];
  /**
   * Show tracked point on dynamic track. VisualTracking UI Option.
   */
  public boolean guiShowPoint = true;
  /**
   * Cell used for generating maps. VisualTracking UI Option.
   */
  public Integer activeCellMap = 0;
  /**
   * Cell used for generating plots. VisualTracking UI Option.
   */
  public Integer activeCellPlot = 0;
  /**
   * Active channel for plotting maps. VisualTracking UI Option.
   */
  public int activeChannel = 0;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbXcentrPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbYcentrPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbDisplPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbDistPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbDirectPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbSpeedPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbPerimPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbElongPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbCircPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbAreaPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbTotFluPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbMeanFluPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbCortexWidthPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbCytoAreaPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbTotalCtfPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbMeanCtfPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbCortexAreaPlot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbTotalCtf2Plot = false;
  /**
   * Plot map. VisualTracking UI Option.
   */
  public boolean chbManCtfPlot = false;

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
    BOTH,
    /**
     * Just pure outline.
     */
    NONE
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
  class OutlinesToImage {
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
    public Color defColor = Color.WHITE;
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
      plotType = OutlinePlotTypes.NONE;
    }
  }

  /**
   * Hold configuration for plotting outlines of cells on stack of images.
   */
  public OutlinesToImage outlinesToImage = new OutlinesToImage();
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

}
