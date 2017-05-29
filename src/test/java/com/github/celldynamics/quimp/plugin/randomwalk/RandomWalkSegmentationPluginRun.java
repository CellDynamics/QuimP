package com.github.celldynamics.quimp.plugin.randomwalk;

import java.util.concurrent.CountDownLatch;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentationPlugin_;

import ij.IJ;
import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPluginRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
    System.setProperty("quimpconfig.superDebug", "false");
  }

  /**
   * Run plugin.
   * 
   * @param args args
   * @throws InterruptedException on error
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch startSignal = new CountDownLatch(1);
    ImageJ ij = new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    // IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
    // "open(\"src/test/Resources-static/ticket209gh/fluoreszenz-test.tif\")");
    // IJ.runMacro("open(\"src/test/Resources-static/ticket209gh/segmented_color.tif\")");
    IJ.openImage("src/test/Resources-static/RW/" + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif")
            .show();
    IJ.openImage(
            "src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_manualseed.png")
            .show();
    obj.showUi(true);

  }

}
