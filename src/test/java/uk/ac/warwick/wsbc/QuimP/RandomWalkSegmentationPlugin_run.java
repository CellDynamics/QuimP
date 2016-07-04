/**
 * @file RandomWalkSegmentationPlugin_run.java
 * @date 4 Jul 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import ij.ImageJ;

/**
 * @author p.baniukiewicz
 * @date 4 Jul 2016
 *
 */
public class RandomWalkSegmentationPlugin_run {
    /**
     * @param args
     */
    public static void main(String[] args) {
        RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
        ImageJ ij = new ImageJ();
        IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
                "open(\"/home/baniuk/Documents/Repos/QuimP/src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif\")");
        IJ.runMacro(
                "open(\"/home/baniuk/Documents/Repos/QuimP/src/test/resources/segmented_color.tif\")");
        obj.run("");

    }

}
