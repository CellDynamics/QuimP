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
import uk.ac.warwick.wsbc.QuimP.QParams;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtrusionVis {
    private static final Logger LOGGER = LogManager.getLogger(ProtrusionVis.class.getName());
    private QParams qP;
    private MaximaFinder mF;
    ImagePlus originalImage;

    public ProtrusionVis() {
        qP = null;
        mF = null;
        originalImage = null;
    }

    /**
     * 
     */
    public ProtrusionVis(QParams qp, MaximaFinder mF) {
        this.qP = qP;
        this.mF = mF;

    }

    public void addPointsToImage() {
        LOGGER.trace(originalImage);
        LOGGER.trace(mF.getMaxima());
        LOGGER.trace(qP.getLoadedDataContainer());
        Overlay overaly = new Overlay();
        LOGGER.trace(Arrays.toString(qP.getLoadedDataContainer().getQState()[0].getxMap()));

    }

    /**
     * @return the originalImage
     */
    public ImagePlus getOriginalImage() {
        return originalImage;
    }

}
