package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Color;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

/**
 * Hold all configuration for Protrusion Analysis Module
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisConfig implements IQuimpSerialize {

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
    public OutlinesToImage outlinesToImage;

    public ProtAnalysisConfig() {
        outlinesToImage = new OutlinesToImage();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }

}
