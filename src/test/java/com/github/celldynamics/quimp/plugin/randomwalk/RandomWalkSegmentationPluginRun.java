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

    // IJ.openImage("src/test/Resources-static/PropagateSeeds/stack.tif").show();
    // IJ.openImage("src/test/Resources-static/PropagateSeeds/stack-mask.tif").show();

    IJ.openImage("src/test/Resources-static/Stack_cut.tif").show();

    // obj.run("{algOptions:{alpha:401.0,beta:50.0,gamma:[100.0,300.0],"
    // + "iter:10000,dt:0.1,relim:[0.008,0.01],useLocalMean:true,localMeanMaskSize:25,"
    // + "maskLimit:false},originalImageName:(stack.tif)," + "seedSource:MaskImage,"
    // + "seedImageName:(stack-mask.tif),"
    // + "selectedShrinkMethod:NONE,shrinkPower:10.0,expandPower:15.0,"
    // + "selectedFilteringMethod:NONE,hatFilter:false,alev:0.9,num:1,window:15,"
    // + "selectedFilteringPostMethod:MEDIAN,showSeeds:false,showPreview:false,"
    // + "paramFile:(null)}");
    obj.run("");

  }

}
