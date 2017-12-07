package com.github.celldynamics.quimp.plugin.randomwalk;

import java.util.concurrent.CountDownLatch;

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
    IJ.openImage("/home/baniuk/Documents/Other_Images/from_Till/Stack.tif").show();
    IJ.openImage("/home/baniuk/Documents/SEED_Stack.tif").show();
    obj.showUi(true);

  }

}
