package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Color;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

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
     * Types of plots to show.
     */
    public boolean plotMotmap = false, plotMotmapmax = true, plotConmap = false,
            plotOutline = false, plotStaticmax = false, plotDynamicmax = false;

    /**
     * Plot types supported by {@link addOutlinesToImage(STmap, ProtAnalysisConfig)}.
     * <p>
     * <ol>
     * <li> MOTILITY - only motility based outline.
     * <li> CONVEXITY - only convexity based outline.
     * <li> CONVANDEXP - parts that are convex and expanding.
     * <li> CONCANDRETR - parts that are concave and retracting.
     * <li> BOTH - combines CONVANDEXP and CONCANDRETR 
     * </ol>
     * 
     * @author p.baniukiewicz
     *
     */
    public enum outlinePlotTypes {
        MOTILITY, CONVEXITY, CONVANDEXP, CONCANDRETR, BOTH
    };

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
     * @see {@link addOutlinesToImage(STmap, ProtAnalysisConfig)}
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
     * Suffix for cell stats.
     */
    public final String cellStatSuffix = "_cellstat.csv";
    /**
     * Protrusion statistics file suffix. 
     */
    public final String protStatSuffix = "_protstat.csv";

    public ProtAnalysisConfig() {
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {

    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {

    }

}
