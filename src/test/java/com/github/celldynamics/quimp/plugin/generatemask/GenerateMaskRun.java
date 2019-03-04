package com.github.celldynamics.quimp.plugin.generatemask;

import ij.ImageJ;

/**
 * The Class GenerateMaskRun.
 *
 * @author p.baniukiewicz
 */
public class GenerateMaskRun {
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
    // run with options

    // try {
    // GenerateMask_ pa = new GenerateMask_(
    // "opts={paramFile:(src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7"
    // + "%agar_FLU_fine.QCONF)}");
    //
    // ImagePlus ret = pa.getRes();
    // if (ret != null) {
    // ret.show();
    // }
    // } catch (QuimpPluginException e) {
    // e.handleException(null, "");
    // }

    // run as api
    GenerateMask_ pa = new GenerateMask_();
    pa.run("");

  }

}
