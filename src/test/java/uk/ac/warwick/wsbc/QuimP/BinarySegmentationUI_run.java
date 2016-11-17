/**
 */
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import ij.ImageJ;
import uk.ac.warwick.wsbc.QuimP.plugin.binaryseg.BinarySegmentationPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentationPlugin_;

/**
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentationUI_run {

    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * 
     */
    public BinarySegmentationUI_run() {
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        ImageJ ij = new ImageJ();
        RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
        IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
                "open(\"/home/p.baniukiewicz/Documents/Repos/QuimP/src/test/resources/BW_seg_5_slices.tif\")");
        IJ.runMacro(
                "open(\"/home/p.baniukiewicz/Documents/Repos/QuimP/src/test/resources/segmented_color.tif\")");
        BinarySegmentationPlugin fp = new BinarySegmentationPlugin();
        fp.attachData(null);
        fp.showUI(true);

    }

}
