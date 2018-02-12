package com.github.celldynamics.quimp.plugin.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class ImageProcessorPlus_Test {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  private ImagePlus image;

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessorPlus_Test.class.getName());
  private ImageProcessorPlus ipp;

  /**
   * Dummy constructor
   */
  public ImageProcessorPlus_Test() {
  }

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    image = IJ.openImage("src/test/Resources-static/testObject.tif"); // opens test
    // image
    ipp = new ImageProcessorPlus();
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    if (image.changes) { // check if source was modified
      image.changes = false; // set flag to false to prevent save dialog
      image.close(); // close image
      throw new Exception("Image has been modified"); // throw exception if source image modified
    }
    image.close();
    image = null;
  }

  /**
   * Test method for utils.ImageProcessorPlus#rotate(ImageProcessor,double, boolean).
   * 
   * <p>Post: Rotated image should have bas-reliefs oriented horizontally. Saves rotated image to
   * /tmp/testrotateImage.tif.
   */
  @Test
  public void test_Rotate() {
    double angle = 135;
    ImageProcessor ret = ipp.rotate(image.getProcessor(), angle, true);
    IJ.saveAsTiff(new ImagePlus("", ret), tmpdir + "testrotateImage.tif");
    LOGGER.info("Check /tmp/testrotateImage.tif to see results of rotation");
  }

  /**
   * Test method for utils.ImageProcessorPlus.rotate(ImageProcessor,double, boolean) with
   * background settings.
   * 
   * <p>Post:Rotated image should have bas-reliefs oriented horizontally. Saves rotated image to
   * /tmp/testrotateImage.tif. There should be 0 background
   */
  @Test
  public void test_Rotate_0background() {
    double angle = 135;
    image.getProcessor().setBackgroundValue(0);
    ImageProcessor ret = ipp.rotate(image.getProcessor(), angle, true);
    IJ.saveAsTiff(new ImagePlus("", ret), tmpdir + "testrotateImage_0background.tif");
    LOGGER.info("Check /tmp/testrotateImage_0background.tif to see results of rotation");
  }

  /**
   * Test method for extendImageBeforeRotation(ImageProcessor,double).
   * 
   * <p>Post: Saves extended image to /tmp/testextendImage_0s.tif.
   */
  @Test
  public void test_ExtendImageToRotation_0s() {
    double angle = 0;
    ImageProcessor ret;
    ret = ipp.extendImageBeforeRotation(image.getProcessor(), angle);
    assertEquals(513, ret.getWidth()); // size of the image
    assertEquals(513, ret.getHeight());
    IJ.saveAsTiff(new ImagePlus("extended", ret), tmpdir + "testextendImage_0s.tif");
    LOGGER.info("Check /tmp/testextendImage_0s.tif to see results");
  }

  /**
   * Test method for extendImageBeforeRotation(ImageProcessor,double).
   * 
   * <p>Post: Saves extended image to /tmp/testextendImage_45s.tif.
   */
  @Test
  public void test_ExtendImageToRotation_45s() {
    double angle = 45;
    ImageProcessor ret;
    ret = ipp.extendImageBeforeRotation(image.getProcessor(), angle);
    assertEquals(725, ret.getWidth()); // size of the image
    assertEquals(725, ret.getHeight());
    IJ.saveAsTiff(new ImagePlus("extended", ret), tmpdir + "testextendImage_45s.tif");
    LOGGER.info("Check /tmp/testextendImage_45s.tif to see results");
  }

  /**
   * 
   */
  @Test
  public void test_crop() {
    ImageProcessor ret;
    ret = ipp.crop(image.getProcessor(), 10, 10, 200, 200);
    assertEquals(200, ret.getWidth()); // size of the image
    assertEquals(200, ret.getHeight());
    IJ.saveAsTiff(new ImagePlus("extended", ret), tmpdir + "testcrop.tif");
    LOGGER.info("Check /tmp/testcrop.tif to see results");
  }

  /**
   * Test of RectangleBox for square image and angle 0 deg
   */
  @Test
  public void test_RectangleBox_0s() {
    int width = 512;
    int height = 512;
    double angle = 0;

    RectangleBox r = new RectangleBox(width, height);
    r.rotateBoundingBox(angle);

    assertEquals(512, r.getWidth(), 0);
    assertEquals(512, r.getHeight(), 0);
  }

  /**
   * Test of RectangleBox for square image and angle 0 deg for input vectors
   * 
   * @throws Exception
   * 
   */
  @Test
  public void test_RectangleBoxVector_0s() throws Exception {

    Vector<Double> x = new Vector<Double>();
    Vector<Double> y = new Vector<Double>();

    x.add(-10.0);
    x.add(10.0);
    x.add(10.0);
    x.add(-10.0);
    y.add(10.0);
    y.add(10.0);
    y.add(-10.0);
    y.add(-10.0);

    double angle = 0;

    RectangleBox r = new RectangleBox(x, y);
    r.rotateBoundingBox(angle);

    assertEquals(20, r.getWidth(), 0);
    assertEquals(20, r.getHeight(), 0);
  }

  /**
   * Test of RectangleBox for square image and angle 45 deg for input vectors.
   * 
   * @throws Exception
   */
  @Test
  public void test_RectangleBoxVector_45s() throws Exception {

    Vector<Double> x = new Vector<Double>();
    Vector<Double> y = new Vector<Double>();

    x.add(-10.0);
    x.add(10.0);
    x.add(10.0);
    x.add(-10.0);
    y.add(10.0);
    y.add(10.0);
    y.add(-10.0);
    y.add(-10.0);

    double angle = 45;

    RectangleBox r = new RectangleBox(x, y);
    r.rotateBoundingBox(angle);

    assertEquals(28, Math.round(r.getWidth()), 0);
    assertEquals(28, Math.round(r.getHeight()), 0);
  }

  /**
   * Test of RectangleBox for square image and angle 90 deg.
   */
  @Test
  public void test_RectangleBox_90s() {
    int width = 512;
    int height = 512;
    double angle = 90;

    RectangleBox r = new RectangleBox(width, height);
    r.rotateBoundingBox(angle);

    assertEquals(512, r.getWidth(), 0);
    assertEquals(512, r.getHeight(), 0);
  }

  /**
   * Test of RectangleBox for non square image and angle 90 deg.
   */
  @Test
  public void test_RectangleBox_90ns() {
    int width = 512;
    int height = 1024;
    double angle = 90;

    RectangleBox r = new RectangleBox(width, height);
    r.rotateBoundingBox(angle);

    assertEquals(1024, Math.round(r.getWidth()), 0);
    assertEquals(512, Math.round(r.getHeight()), 0);
  }

  /**
   * Test of RectangleBox for square image and angle 45 deg
   */
  @Test
  public void test_RectangleBox_45s() {
    int width = 512;
    int height = 512;
    double angle = 45;

    RectangleBox r = new RectangleBox(width, height);
    r.rotateBoundingBox(angle);

    assertEquals(724, Math.round(r.getWidth()), 0);
    assertEquals(724, Math.round(r.getHeight()), 0);
  }

  /**
   * Test of RectangleBox for square image and angle 30 deg
   */
  @Test
  public void test_RectangleBox_30s() {
    int width = 512;
    int height = 512;
    double angle = 30;

    RectangleBox r = new RectangleBox(width, height);
    r.rotateBoundingBox(angle);

    assertEquals(699, Math.round(r.getWidth()), 0);
    assertEquals(699, Math.round(r.getHeight()), 0);
  }

}
