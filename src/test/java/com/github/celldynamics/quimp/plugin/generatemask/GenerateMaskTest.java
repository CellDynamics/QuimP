package com.github.celldynamics.quimp.plugin.generatemask;

import org.junit.Ignore;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;

import ij.ImagePlus;

/**
 * Test class and use example for GenerateMask plugin.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMaskTest {

  /**
   * Example of use of GenerateMask plugin from API - IJ way.
   */
  @Test
  @Ignore
  public void testGenerateMask_1() {
    GenerateMask_ mask;
    mask = new GenerateMask_();
    mask.run(""); // no parameters - assumes menu call and display GUI (hehe will ask for file)
  }

  /**
   * Example of use of GenerateMask plugin from API - IJ way.
   */
  @Test
  public void testGenerateMask_2() {
    GenerateMask_ mask;
    mask = new GenerateMask_();
    // parameter string like in macro - errors redirected to IJ.error, no visual output
    mask.run("opts={paramFile:[src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_fine."
            + "QCONF]}");
    // mask.run("opts={paramFile:[/home/baniuk/Desktop/Tests/17.10/test.QCONF]}");
  }

  /**
   * Example of use of GenerateMask plugin from API - API way. All exceptions handled by user, logs
   * go to logger.
   * 
   * @throws QuimpPluginException on error
   */
  @Test
  public void testGenerateMask_3() throws QuimpPluginException {
    GenerateMask_ pa = new GenerateMask_(
            "opts={paramFile:[src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7"
                    + "%agar_FLU_fine.QCONF]}");

    ImagePlus ret = pa.getRes();
    if (ret != null) {
      ret.show();
    }

  }
}
