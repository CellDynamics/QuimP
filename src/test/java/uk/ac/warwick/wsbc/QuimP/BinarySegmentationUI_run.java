/**
 * @file BinarySegmentationUI_run.java
 * @date 28 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import ij.ImageJ;

/**
 * @author p.baniukiewicz
 * @date 28 Jun 2016
 *
 */
public class BinarySegmentationUI_run {

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * 
     */
    public BinarySegmentationUI_run() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        ImageJ ij = new ImageJ();
        RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
        IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
                "open(\"/home/baniuk/Documents/Repos/QuimP/src/test/resources/BW_seg_5_slices.tif\")");
        IJ.runMacro(
                "open(\"/home/baniuk/Documents/Repos/QuimP/src/test/resources/segmented_color.tif\")");
        BinarySegmentationPlugin fp = new BinarySegmentationPlugin();
        fp.attachData(null);
        fp.showUI(true);

    }

}
