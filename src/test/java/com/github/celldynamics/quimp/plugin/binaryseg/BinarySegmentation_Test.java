package com.github.celldynamics.quimp.plugin.binaryseg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.ParamList;

import ij.ImageJ;

/**
 * BinarySegmentation_Test.
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BinarySegmentation_Test {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Mock
  private BinarySegmentationView bsp;

  @InjectMocks
  private BinarySegmentation_ bs;

  @Spy
  private BinarySegmentation_ bspy;

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
   * Test method for {@link BinarySegmentation_#BinarySegmentation_()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentation_() throws Exception {
    bs.run("");
    verify(bsp).showWindow(true);
    verify(bsp, never()).setValues(any(ParamList.class));
    bspy.run("");
    verify(bspy).showUi(true);
  }

  /**
   * Test method for {@link BinarySegmentation_#BinarySegmentation_()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentation_1() throws Exception {
    bspy.run("");
    verify(bsp, times(0)).setValues(any(ParamList.class));
  }

  /**
   * Test macro.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentationRun() throws Exception {
    BinarySegmentation_ obj = new BinarySegmentation_();
    //!>
    obj.run("opts={"
            + "options:{"
            + "select_image:NONE,"
            + "step:4.0,"
            + "smoothing:false,"
            + "clear_nest:true,"
            + "restore_snake:true"
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
  }
}
