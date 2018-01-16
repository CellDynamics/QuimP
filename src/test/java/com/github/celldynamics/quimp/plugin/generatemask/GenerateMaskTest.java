package com.github.celldynamics.quimp.plugin.generatemask;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

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
    mask.run("opts={paramFile:(src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_fine."
            + "QCONF)}");
    // mask.run("opts={paramFile:(/home/baniuk/Desktop/Tests/17.10/test.QCONF)}");
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
            "opts={paramFile:(src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7"
                    + "%agar_FLU_fine.QCONF)}");

    ImagePlus ret = pa.getRes();
    if (ret != null) {
      ret.show();
    }
  }

  /**
   * Example of use of GenerateMask plugin from API - API way + many cells. All exceptions handled
   * by user, logs go to logger.
   * 
   * @throws QuimpPluginException on error
   */
  @Test
  public void testGenerateMask_4() throws QuimpPluginException {
    GenerateMask_ pa =
            new GenerateMask_("opts={paramFile:(src/test/Resources-static/Stack_cut_1.QCONF)}");

    ImagePlus ret = pa.getRes();
    assertThat(ret, is(not(nullValue())));
    // normal binary output
    ImageProcessor i = ret.getStack().getProcessor(5);
    assertThat(i, is(instanceOf(ByteProcessor.class)));
    assertThat(i.get(273, 255), is(255));
    assertThat(i.get(194, 301), is(255));
    assertThat(i.get(218, 404), is(255));
  }

  /**
   * Example of use of GenerateMask plugin from API - API way + many cells + grayscale. All
   * exceptions handled by user, logs go to logger.
   * 
   * @throws QuimpPluginException on error
   */
  @Test
  public void testGenerateMask_5() throws QuimpPluginException {
    GenerateMask_ pa = new GenerateMask_(
            "opts={paramFile:(src/test/Resources-static/Stack_cut_1.QCONF),binary:false}");

    ImagePlus ret = pa.getRes();
    assertThat(ret, is(not(nullValue())));
    // normal binary output
    ImageProcessor i = ret.getStack().getProcessor(5);
    assertThat(i, is(instanceOf(ShortProcessor.class)));
    assertThat(i.get(273, 255), is(1));
    assertThat(i.get(194, 301), is(2));
    assertThat(i.get(218, 404), is(3));
  }
}
