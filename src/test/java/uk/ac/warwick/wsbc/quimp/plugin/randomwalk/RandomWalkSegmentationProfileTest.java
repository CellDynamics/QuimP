package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;

/**
 * Normal test but used for profiling
 * 
 * @author p.baniukiewicz
 *
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

  /**
   * Access private.
   *
   * @param name the name
   * @param obj the obj
   * @param param the param
   * @param paramtype the paramtype
   * @return the object
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  static Object accessPrivate(String name, RandomWalkSegmentation obj, Object[] param,
          Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  private ImagePlus testImage2seed;
  private ImagePlus fluoreszenz1;
  private ImagePlus fluoreszenz2;

  /**
   * The p.
   */
  Params params;

  /**
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
    testImage2seed = IJ.openImage("src/test/Resources-static/segmented_color.tif");

    fluoreszenz1 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_1.tif");
    fluoreszenz2 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_2.tif");

    params = new Params(400, 50, 100, 300, 80, 0.1, new double[] { 8e-3, 1e-3 }, false, 25);
    // Thread.sleep(10000);
  }

  /**
   * @throws java.lang.Exception on error
   */
  @After
  public void tearDown() throws Exception {

    fluoreszenz1.close();
    fluoreszenz2.close();
  }

  /**
   * Test of main runner use propagateseed.
   * 
   * <p>pre: two frames
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_4() throws Exception {
    long startTime = System.nanoTime();

    RandomWalkSegmentation obj = new RandomWalkSegmentation(fluoreszenz1.getProcessor(), params);
    Map<Seeds, ImageProcessor> seeds =
            RandomWalkSegmentation.decodeSeeds(testImage2seed, Color.RED, Color.GREEN);
    ImageProcessor retFrame1 = obj.run(seeds);
    Map<Seeds, ImageProcessor> nextseed =
            new PropagateSeeds.Morphological().propagateSeed(retFrame1, 3, 4.5);
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
