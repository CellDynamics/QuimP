package com.github.celldynamics.quimp.plugin.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.utils.ImageProcessorPlus.GenerateKernel;

import ij.IJ;
import ij.ImagePlus;

/**
 * The Class ImageProcessorPlusTest.
 *
 * @author p.baniukiewicz
 */
public class ImageProcessorPlusTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /** The image. */
  private ImagePlus image;

  /**
   * Sets the up before class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * Tear down after class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    image = IJ.openImage("src/test/Resources-static/AX3 stack1_slice_1.tif");
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    image.close();
  }

  /**
   * Test generate kernel.
   *
   * @throws Exception the exception
   */
  @Test
  public void test_GenerateKernel() throws Exception {
    { //!>
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
    { //!>
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
    { //!>
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
    { //!>
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
   * {@link ImageProcessorPlus#runningMean(ij.process.ImageProcessor, java.lang.String, int)}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRunningMean() throws Exception {
    new ImageProcessorPlus().runningMean(image.getProcessor(), "45", 9);
    IJ.saveAsTiff(image, tmpdir + "testRunningMean.tif");
  }
}
