package com.github.celldynamics.quimp.plugin.protanalysis;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;

import ij.ImageJ;

/**
 * The Class ProtAnalysisUIRun.
 *
 * @author p.baniukiewicz
 */
public class ProtAnalysisUIRun {
  static {
    System.setProperty("quimp.debugLevel", "qlog4j2.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   * @throws QuimpPluginException on error
   */
  public static void main(String[] args) throws QuimpPluginException {
    ImageJ ij = new ImageJ();
    new Prot_Analysis(null).gui.showUI(true);
    // ImagePlus ip =
    // IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frames_1-5.tif");
    // new ProtAnalysisUI(new ProtAnalysisConfig(), null).getGradient(ip);

  }

}
