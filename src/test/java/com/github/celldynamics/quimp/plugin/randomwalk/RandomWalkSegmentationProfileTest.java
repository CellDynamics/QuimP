package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Test and example of end-user API (but still low level).
 * 
 * @author p.baniukiewicz
 * @see RandomWalkSegmentationPluginTest
 */
public class RandomWalkSegmentationProfileTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationProfileTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /** The test image 2 seed. */
  private ImagePlus testImage2seed;

  /** The fluoreszenz 1. */
  private ImagePlus fluoreszenz1;

  /** The fluoreszenz 2. */
  private ImagePlus fluoreszenz2;

  /**
   * The p.
   */
  RandomWalkOptions params;

  /**
   * Load test images.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    testImage2seed = IJ.openImage("src/test/Resources-static/segmented_color.tif");

    fluoreszenz1 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_1.tif");
    fluoreszenz2 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_2.tif");

    params = new RandomWalkOptions(400d, 50d, 100d, 300d, 80, 0.1, new Double[] { 8e-3, 1e-3 },
            false, 25);
    // Thread.sleep(10000);
  }

  /**
   * Clean.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {

    fluoreszenz1.close();
    fluoreszenz2.close();
  }

  /**
   * Test of main runner, example of use of seed propagation.
   * 
   * <p>pre: two frames
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_4() throws Exception {
    long startTime = System.nanoTime();

    RandomWalkSegmentation obj = new RandomWalkSegmentation(fluoreszenz1.getProcessor(), params);
    Seeds seeds =
            SeedProcessor.decodeSeedsfromRgb(testImage2seed, Arrays.asList(Color.RED), Color.GREEN);
    ImageProcessor retFrame1 = obj.run(seeds);
    Seeds nextseed = new PropagateSeeds.Morphological().propagateSeed(retFrame1,
            fluoreszenz1.getProcessor(), 3, 4.5);
    obj = new RandomWalkSegmentation(fluoreszenz2.getProcessor(), params);
    ImageProcessor retFrame2 = obj.run(nextseed);

    long stopTime = System.nanoTime();
    LOGGER.info("--Time used -- " + (double) (stopTime - startTime) / 1000000000.0 + " [s]");

    ImagePlus resultsFrame2 = new ImagePlus("cmp", retFrame2);
    IJ.saveAsTiff(resultsFrame2, tmpdir + "testRun_4_f2.tif");
    ImagePlus resultsFrame1 = new ImagePlus("cmp", retFrame1);
    IJ.saveAsTiff(resultsFrame1, tmpdir + "testRun_4_f1.tif");
  }

}
