package com.github.celldynamics.quimp.plugin.generatemask;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.celldynamics.quimp.plugin.QuimpPluginException;

import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

// TODO: Auto-generated Javadoc
/**
 * Test class and use example for GenerateMask plugin.
 * 
 * @author p.baniukiewicz
 *
 */
public class GenerateMaskTest {

  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * Example of use of GenerateMask plugin from API - IJ way.
   */
  @Test
  @Ignore
  public void testGenerateMask_1() {
    GenerateMask_ mask;
    mask = new GenerateMask_();
    mask.run(""); // no parameters - assumes menu call and display GUI (here will ask for file)
  }

  /**
   * Example of use of GenerateMask plugin from API - IJ way.
   * 
   * @throws IOException on copy error
   */
  @Test
  public void testGenerateMask_2() throws IOException {
    // important to get IJ_Props file (MUST contain registration data if test is run with
    // installation profile)
    Path target = Paths.get(temp.getRoot().toString(), "test.QCONF");
    new ImageJ();
    FileUtils.copyFile(
            Paths.get("src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_fine.QCONF")
                    .toFile(),
            target.toFile());
    GenerateMask_ mask;
    mask = new GenerateMask_();
    // parameter string like in macro - errors redirected to IJ.error, shows and save
    mask.run("opts={paramFile:(" + target.toString() + ")}");
    Path expectedTiff = Paths.get(temp.getRoot().toString(), "test_snakemask.tif");
    // check is image saved
    assertThat(expectedTiff.toFile().exists(), is(true));
    // check if shown
    assertThat(WindowManager.getCurrentImage(), is(notNullValue()));
    assertThat(WindowManager.getCurrentImage().getShortTitle(), equalTo("test_snakemask"));
    // check file props
    Opener opener = new Opener();
    assertThat(opener.openImage(expectedTiff.toString()).getBitDepth(), is(8));
    assertThat(opener.openImage(expectedTiff.toString()).getNSlices(), is(30));
    // mask.run("opts={paramFile:(/home/baniuk/Desktop/Tests/17.10/test.QCONF)}");
  }

  /**
   * Example of use of GenerateMask plugin from API - API way. All exceptions handled by user, logs
   * go to logger, image not shown nor saved
   * 
   * @throws QuimpPluginException on error
   */
  @Test
  public void testGenerateMask_3() throws QuimpPluginException {
    GenerateMask_ pa = new GenerateMask_(
            "opts={paramFile:(src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7"
                    + "%agar_FLU_fine.QCONF)}");

    ImagePlus ret = pa.getRes();
    assertThat(ret.getBitDepth(), is(8));
    assertThat(ret.getNSlices(), is(30));
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

  /**
   * About test.
   */
  @Test
  public void testAbout() {
    GenerateMask_ mask;
    mask = new GenerateMask_();
    assertThat(mask.about(), is(any(String.class)));
  }
}
