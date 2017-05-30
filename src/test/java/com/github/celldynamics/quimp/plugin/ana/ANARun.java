package com.github.celldynamics.quimp.plugin.ana;

import com.github.celldynamics.quimp.plugin.ana.ANA_;

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
    ImagePlus im = IJ.openImage("/home/baniuk/Desktop/Tests/Pabloemail/test.tif");
    im.show();
    ANA_ ana = new ANA_();
    ana.setup(new String(), im);
    // load paQP and QCONF file related to tiff pointed above
    ana.run(im.getProcessor());

  }

}