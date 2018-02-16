package com.github.celldynamics.quimp.plugin.dic;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * Gui checker for DICLIDReconstruction.
 */
public class DicLidReconstructionRun {

  // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   * @throws InterruptedException Gui checker for DICLIDReconstruction
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws InterruptedException {
    ImageJ ij = new ImageJ();
    // load images #272
    ImagePlus i = IJ.openImage("src/test/Resources-static/movie03_8bit_10slices.tif");
    i.show();
    DicLidReconstruction_ dic = new DicLidReconstruction_();
    dic.setup("", i);
    dic.run(i.getProcessor());
  }
}
