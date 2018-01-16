package com.github.celldynamics.quimp.plugin.randomwalk;

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
    ImageJ ij = new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    // IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
    // "open(\"src/test/Resources-static/ticket209gh/fluoreszenz-test.tif\")");
    // IJ.runMacro("open(\"src/test/Resources-static/ticket209gh/segmented_color.tif\")");

    // IJ.openImage("src/test/Resources-static/PropagateSeeds/stack.tif").show();
    // IJ.openImage("src/test/Resources-static/PropagateSeeds/stack-mask.tif").show();

    // IJ.openImage("src/test/Resources-static/Stack_cut.tif").show();

    // obj.run("{algOptions:{alpha:401.0,beta:50.0,gamma:[100.0,300.0],"
    // + "iter:10000,dt:0.1,relim:[0.008,0.01],useLocalMean:true,localMeanMaskSize:25,"
    // + "maskLimit:false},originalImageName:(stack.tif)," + "seedSource:MaskImage,"
    // + "seedImageName:(stack-mask.tif),"
    // + "selectedShrinkMethod:NONE,shrinkPower:10.0,expandPower:15.0,"
    // + "selectedFilteringMethod:NONE,hatFilter:false,alev:0.9,num:1,window:15,"
    // + "selectedFilteringPostMethod:MEDIAN,showSeeds:false,showPreview:false,"
    // + "paramFile:(null)}");

    obj.run("");

    // ************** Run **************
    // example1();
    // example2();
  }

  /**
   * Super debug.
   * 
   * <p>Save selected number of fg and bg prob maps to stack for each iteration.
   */
  public static void example2() {
    System.setProperty("quimpconfig.superDebug", "true");
    new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    //!> example 1
    IJ.openImage(
            "/home/baniuk/Desktop/Tests/284/Stack_1frame.tif")
            .show();
    IJ.openImage("/home/baniuk/Desktop/Tests/284/SEED_Stack_1frame.tif")
            .show();
    obj.run("opts={"
            + "algOptions:{"
            + "alpha:400.0,"
            + "beta:50.0,"
            + "gamma:[100.0,0.0],"
            + "iter:10000," // !
            + "dt:0.1,"
            + "relim:[0.00200,0.02],"
            + "useLocalMean:false,"
            + "localMeanMaskSize:23,"
            + "maskLimit:false"
            + "},"
            + "originalImageName:(Stack_1frame.tif),"
            + "selectedSeedSource:RGBImage,"
            + "seedImageName:(SEED_Stack_1frame.tif),"
            + "qconfFile:(null),"
            + "selectedShrinkMethod:NONE,"
            + "shrinkPower:17.0,"
            + "expandPower:15.0,"
            + "scaleSigma:0.3,"
            + "scaleMagn:4.0,"
            + "scaleEqNormalsDist:12.0,"
            + "scaleCurvDistDist:12.0,"
            + "estimateBackground:false,"
            + "selectedFilteringMethod:NONE,"
            + "hatFilter:false,alev:0.9,num:1,window:15,"
            + "selectedFilteringPostMethod:NONE,"
            + "showSeeds:false,"
            + "showPreview:false,"
            + "showProbMaps:false,"
            + "paramFile:(null)}");
    IJ.runMacro("selectWindow(\"Segmented_Stack_1frame.tif\");");
    IJ.runMacro("run(\"Outline\");");
    IJ.runMacro("run(\"Merge Channels...\", \""
            + "c1=Segmented_Stack_1frame.tif "
            + "c4=Stack_1frame.tif create keep\");");
    //!<
  }

  /**
   * Example run for concave cell.
   */
  public static void example1() {
    new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    //!> example 1
    IJ.openImage(
            "src/test/Resources-static/RW/"
            + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif")
            .show();
    IJ.openImage("src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif")
            .show();
    obj.run("opts={algOptions:{alpha:900.0,beta:100.0,gamma:[100.0,0.0],iter:10000,"
            + "dt:0.1,relim:[0.002,0.02],useLocalMean:true,localMeanMaskSize:23,"
            + "maskLimit:false},"
            + "originalImageName:(C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif),"
            + "selectedSeedSource:MaskImage,"
            + "seedImageName:(C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif),"
            + "qconfFile:(null),"
            + "selectedShrinkMethod:CONTOUR,shrinkPower:17.0,expandPower:15.0,"
            + "scaleSigma:0.3,scaleMagn:4.0,"
            + "scaleEqNormalsDist:12.0,"
            + "scaleCurvDistDist:12.0,"
            + "estimateBackground:false,"
            + "selectedFilteringMethod:NONE,"
            + "hatFilter:false,alev:0.9,num:1,window:15,"
            + "selectedFilteringPostMethod:NONE,"
            + "showSeeds:false,showPreview:false,"
            + "showProbMaps:false,"
            + "paramFile:(null)}");
    IJ.runMacro("selectWindow(\"Segmented_C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif\");");
    IJ.runMacro("run(\"Outline\");");
    IJ.runMacro("run(\"Merge Channels...\", \""
            + "c1=Segmented_C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif "
            + "c4=C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif create keep\");");
    //!<
  }

}
