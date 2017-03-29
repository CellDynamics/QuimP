package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisUIRun {
  static {
    System.setProperty("quimp.debugLevel", "qlog4j2.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    new Prot_Analysis(
            "src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF").gui
                    .showUI(true);
    // ImagePlus ip =
    // IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif");
    // new ProtAnalysisUI(new ProtAnalysisConfig(), null).getGradient(ip);

  }

}
