package uk.ac.warwick.wsbc.quimp.plugin.generatemask;

import ij.ImageJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;

/**
 * @author p.baniukiewicz
 *
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
    try {
      GenerateMask_ pa = new GenerateMask_("filename=[src/test/Resources-static/RW/"
              + "C1-talA_mNeon_bleb_0pt7%agar_FLU_fine.QCONF]");
      ImagePlus ret = pa.getRes();
      if (ret != null) {
        ret.show();
      }
    } catch (QuimpPluginException e) {
      e.handleException(null, "");
    }

    // GenerateMask_ pa = new GenerateMask_(null);

  }

}
