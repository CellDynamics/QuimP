package com.github.celldynamics.quimp.plugin.binaryseg;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Plugin runner.
 * 
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentationRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Main.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    new ImageJ();
    ImagePlus img = IJ.openImage("/home/baniuk/Desktop/Tests/291/Segmented_Stack-30.tif");
    img.show();
    BinarySegmentation_ obj = new BinarySegmentation_();
    obj.run("");
  }

}
