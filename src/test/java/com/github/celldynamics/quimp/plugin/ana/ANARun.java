package com.github.celldynamics.quimp.plugin.ana;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

// TODO: Auto-generated Javadoc
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
   * Runner.
   */
  public ANARun() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Main.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    IJ.runMacro("run(\"Record...\");");
    ImagePlus im = IJ.openImage("/home/baniuk/Desktop/Tests/175/test.tif");
    im.show();
    ANA_ ana = new ANA_();
    ana.setup(new String(), im);
    // load paQP and QCONF file related to tiff pointed above
    ana.run(im.getProcessor());

  }

}
