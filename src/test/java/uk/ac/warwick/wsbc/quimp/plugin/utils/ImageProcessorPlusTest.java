package uk.ac.warwick.wsbc.quimp.plugin.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.quimp.plugin.utils.ImageProcessorPlus;
import uk.ac.warwick.wsbc.quimp.plugin.utils.ImageProcessorPlus.GenerateKernel;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class ImageProcessorPlusTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  private ImagePlus image;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    image = IJ.openImage("src/test/resources/AX3 stack1_slice_1.tif");
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    image.close();
  }

  /**
   * @throws Exception
   */
  @Test
  public void test_GenerateKernel() throws Exception {
    {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.2f, 0.2f, 0.2f, 0.2f, 0.2f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f
                    };
         //!<   
      GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
      assertThat(gk.generateKernel("0"), is(exp));
    }
    {//!>
            float[] exp = {
                    0.2f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.2f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.2f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.2f
                    };
         //!<   
      GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
      assertThat(gk.generateKernel("135"), is(exp));
    }
    {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f
                    };
         //!<   
      GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
      assertThat(gk.generateKernel("90"), is(exp));
    }
    {//!>
            float[] exp = {
                    0.0f, 0.0f, 0.0f, 0.0f, 0.2f,
                    0.0f, 0.0f, 0.0f, 0.2f, 0.0f,
                    0.0f, 0.0f, 0.2f, 0.0f, 0.0f,
                    0.0f, 0.2f, 0.0f, 0.0f, 0.0f,
                    0.2f, 0.0f, 0.0f, 0.0f, 0.0f
                    };
         //!<   
      GenerateKernel gk = new ImageProcessorPlus().new GenerateKernel(5);
      assertThat(gk.generateKernel("45"), is(exp));
    }
  }

  /**
   * Test method for
   * {@link uk.ac.warwick.wsbc.quimp.plugin.utils.ImageProcessorPlus#runningMean(ij.process.ImageProcessor, java.lang.String, int)}.
   * 
   * @throws Exception
   */
  @Test
  public void testRunningMean() throws Exception {
    new ImageProcessorPlus().runningMean(image.getProcessor(), "45", 9);
    IJ.saveAsTiff(image, tmpdir + "testRunningMean.tif");
  }
}
