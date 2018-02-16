package com.github.celldynamics.quimp.plugin.ana;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Plugin runner for in-place tests.
 * 
 * @author p.baniukiewicz
 */
public class ANARun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Main.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    ImagePlus im = IJ.openImage("/home/baniuk/Desktop/Tests/175/test.tif");
    im.show();
    ANA_ ana = new ANA_();
    // load paQP and QCONF file related to tiff pointed above
    // ana.run("opts={plotOutlines:true," + "fluoResultTable:false,fluoResultTableAppend:false,"
    // + "channel:0,normalise:true,sampleAtSame:false,clearFlu:false,"
    // + "paramFile:(/home/baniuk/Desktop/Tests/175/test.QCONF)}");
    ana.run("");

  }

}
