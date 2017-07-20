package com.github.celldynamics.quimp;

import com.github.celldynamics.quimp.plugin.binaryseg.BinarySegmentationPlugin;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentationPlugin_;

import ij.IJ;
import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentationUiRun {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Default constructor.
   */
  public BinarySegmentationUiRun() {
  }

  /**
   * Runner.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {

    ImageJ ij = new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    // load images that are called from RandomWalkSegmentationPlugin
    IJ.runMacro("open(\"/home/p.baniukiewicz/Documents/"
            + "Repos/QuimP/src/test/Resources-static/BW_seg_5_slices.tif\")");
    IJ.runMacro("open(\"/home/p.baniukiewicz/Documents/Repos/"
            + "QuimP/src/test/Resources-static/segmented_color.tif\")");
    BinarySegmentationPlugin fp = new BinarySegmentationPlugin();
    fp.attachData(null);
    fp.showUi(true);

  }

}
