package com.github.celldynamics.quimp.plugin.generatemask;

import org.junit.Ignore;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.generatemask.GenerateMask_;

import ij.ImagePlus;

/**
 * Test class and use example for GenerateMask plugin.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMaskTest {

  /**
   * Example of use of GenerateMask plugin from API.
   */
  @Test
  @Ignore
  public void testGenerateMask() {
    GenerateMask_ mask;
    try {
      // to solve exception handling API call must start from parametrised constructor.
      mask = new GenerateMask_("filename=[src/test/Resources-static/RW/"
              + "C1-talA_mNeon_bleb_0pt7%agar_FLU_fine.QCONF]");
      ImagePlus ret = mask.getRes();
      if (ret != null) {
        ret.show();
      }
    } catch (QuimpPluginException e) {
      e.handleException(null, "");
    }

  }
}
