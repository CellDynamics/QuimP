package com.github.celldynamics.quimp.plugin.binaryseg;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.ParamList;

import ij.ImageJ;

/**
 * BinarySegmentation_Test. High level API.
 * 
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentation_Test {

  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * setUp.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    new ImageJ();
  }

  /**
   * Test macro - API use method 1.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentationRun() throws Exception {
    BinarySegmentation_ obj = new BinarySegmentation_();
    //!>
    obj.run("opts={"
            + "options:{"
            + "select_mask:NONE,"
            + "step:4.0,"
            + "smoothing:false,"
            + "clear_nest:true,"
            + "restore_snake:true,"
            + "select_original:src/test/Resources-static/Segmented_Stack-30.tif"
            + "},"
            + "maskFileName:(src/test/Resources-static/Segmented_Stack-30.tif),"
            + "outputPath:(" + temp.getRoot().toString() + "/Segmented_Stack-30.QCONF),"
            + "paramFile:(null)}");
    //!<
    assertThat(Paths.get(temp.getRoot().getPath(), "Segmented_Stack-30.QCONF").toFile().exists(),
            is(true));

    QconfLoader qcl = new QconfLoader(
            Paths.get(temp.getRoot().getPath(), "Segmented_Stack-30.QCONF").toFile());
    assertThat(qcl.getBOA().nest.size(), is(2));
    assertThat(qcl.getBOA().nest.getHandler(0).getStartFrame(), is(1));
    assertThat(qcl.getBOA().nest.getHandler(0).getEndFrame(), is(30));
    assertThat(qcl.getBOA().nest.getHandler(1).getStartFrame(), is(1));
    assertThat(qcl.getBOA().nest.getHandler(1).getEndFrame(), is(30));

    assertThat(qcl.getBOA().boap.getOrgFile().toString(),
            is("src/test/Resources-static/Segmented_Stack-30.tif"));
    // qconfloader overrides these fields
    // assertThat(qcl.getBOA().boap.getOutputFileCore().toString(),
    // is(temp.getRoot().toString() + "/ORIGINAL_Stack-30"));
    // assertThat(qcl.getBOA().boap.getFileName(), is("ORIGINAL_Stack-30"));
  }

  /**
   * Test method for
   * {@link BinarySegmentation_#BinarySegmentation_(AbstractPluginOptions)}.
   * 
   * <p>API use = method 2
   * 
   * @throws Exception on error
   */
  @Test
  public void testBinarySegmentation_AbstractPluginOptions() throws Exception {
    BinarySegmentationOptions options = new BinarySegmentationOptions();
    // need to wrap QWindowBuilder data structure
    options.options = new ParamList();
    options.options.put(BinarySegmentationView.STEP2, "4.0");
    options.options.put(BinarySegmentationView.SMOOTHING2, "false");
    options.options.put(BinarySegmentationView.SELECT_ORIGINAL_IMAGE,
            "src/test/Resources-static/Segmented_Stack-30.tif");
    options.maskFileName = "src/test/Resources-static/Segmented_Stack-30.tif";
    options.outputPath = temp.getRoot().toString() + "/Segmented_Stack-30.QCONF";
    BinarySegmentation_ obj = new BinarySegmentation_(options);
    obj.runPlugin();

    assertThat(Paths.get(temp.getRoot().getPath(), "Segmented_Stack-30.QCONF").toFile().exists(),
            is(true));

    QconfLoader qcl = new QconfLoader(
            Paths.get(temp.getRoot().getPath(), "Segmented_Stack-30.QCONF").toFile());
    assertThat(qcl.getBOA().nest.size(), is(2));
    assertThat(qcl.getBOA().nest.getHandler(0).getStartFrame(), is(1));
    assertThat(qcl.getBOA().nest.getHandler(0).getEndFrame(), is(30));
    assertThat(qcl.getBOA().nest.getHandler(1).getStartFrame(), is(1));
    assertThat(qcl.getBOA().nest.getHandler(1).getEndFrame(), is(30));
  }

  /**
   * About test.
   */
  @Test
  public void testAbout() {
    BinarySegmentation_ mask;
    mask = new BinarySegmentation_();
    assertThat(mask.about(), is(any(String.class)));
  }
}
