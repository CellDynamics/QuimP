/**
 * @file ProtrusionVis.java
 * @date 19 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.ImagePlus;
import ij.gui.Overlay;
import uk.ac.warwick.wsbc.QuimP.STmap;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtrusionVis {
    private static final Logger LOGGER = LogManager.getLogger(ProtrusionVis.class.getName());
    private STmap mapCell;
    private MaximaFinder mF;
    ImagePlus originalImage;

    public ProtrusionVis() {
        mapCell = null;
        mF = null;
        originalImage = null;
    }

    /**
     * @param qP 
     * 
     */
    public ProtrusionVis(MaximaFinder mF, STmap mapCell) {
        this.mapCell = mapCell;
        this.mF = mF;

    }

    public void addPointsToImage() {
        LOGGER.trace(originalImage);
        LOGGER.trace(mF.getMaxima());
        LOGGER.trace(mapCell);
        Overlay overaly = new Overlay();
        double x[][] = mapCell.getxMap();
        double y[][] = mapCell.getyMap();

        LOGGER.trace(Arrays.toString(mapCell.getxMap()));

    }

    /**
     * @return the originalImage
     */
    public ImagePlus getOriginalImage() {
        return originalImage;
    }

}
