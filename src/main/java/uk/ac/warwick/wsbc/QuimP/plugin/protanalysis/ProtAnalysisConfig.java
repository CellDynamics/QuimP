package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Color;

import org.scijava.vecmath.Point2d;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

// TODO: Auto-generated Javadoc
/**
 * Hold all configuration for Protrusion Analysis Module.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisConfig implements IQuimpSerialize {

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
    public boolean plotStaticmax = false;
    /**
     * Type of plots to show.
     */
    public boolean plotDynamicmax = false;

    /**
     * Plot types supported by
     * {@link TrackVisualisation.Stack#addOutlinesToImage(uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap, ProtAnalysisConfig)}.
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
    public enum outlinePlotTypes {

        /**
         * The motility.
         */
        MOTILITY,
        /**
         * The convexity.
         */
        CONVEXITY,
        /**
         * The convandexp.
         */
        CONVANDEXP,
        /**
         * The concandretr.
         */
        CONCANDRETR,
        /**
         * The both.
         */
        BOTH
    };

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
    public enum gradientType {

        /**
         * The screenpoint.
         */
        SCREENPOINT,
        /**
         * The outlinepoint.
         */
        OUTLINEPOINT,
        /**
         * The notdefined.
         */
        NOTDEFINED
    };

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
         * Indicate whether to plot polar plots
         */
        public boolean plotpolar = false;
        /**
         * Type of gradient point.
         * 
         * @see gradientType
         */
        public gradientType type = gradientType.SCREENPOINT;
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
     * @see TrackVisualisation.Stack#addOutlinesToImage(uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap,
     *      ProtAnalysisConfig)
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
        public outlinePlotTypes plotType;

        /**
         * Set default values.
         */
        public OutlinesToImage() {
            motThreshold = 0;
            convThreshold = 0;
            plotType = outlinePlotTypes.MOTILITY;
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
    public ProtAnalysisConfig() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {

    }

}
