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
    ImagePlus img = IJ.openImage("C:/Users/baniu/Desktop/Segmentation.tif");
    img.show();
    ImagePlus img1 = IJ.openImage("C:/Users/baniu/Desktop/QW_channel_1_actin.tif");
    img1.show();
    BinarySegmentation_ obj = new BinarySegmentation_();
    obj.run("");
  }

}
