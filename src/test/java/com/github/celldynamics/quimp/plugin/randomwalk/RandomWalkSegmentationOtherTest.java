package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Run time tests of segmentation. Example of low-level API.
 * 
 * <p>See: src/test/Resources-static/Matlab/RW_java_tests.m This is source of good cases for
 * segmentation
 * 
 * <p>See: Abstract/main.m
 * 
 * <p>Comparator for results from debug from
 * com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentationTest
 * 
 * <p>See: src/test/Resources-static/Matlab/java_output_ver.m
 *
 * 
 * @author p.baniukiewicz
 * @see <a href="./examples.html">Examples</a>
 */
@SuppressWarnings("unused")
public class RandomWalkSegmentationOtherTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationOtherTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  static {
    System.setProperty("quimpconfig.superDebug", "false");
  }

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

  private ImagePlus testImage1; // original 8bit grayscale
  private ImagePlus testImage1seed; // contains rgb image with seed
  private ImagePlus testImage1rgb; // contains rgb image with test seed points
  private ImagePlus testImage2seed;
  private ImagePlus testImage2;
  private ImagePlus fluoreszenz1;
  private ImagePlus fluoreszenz2;

  RandomWalkOptions params;

  /**
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
    testImage1seed = IJ.openImage("src/test/Resources-static/segtest_small_seed.tif");
    testImage1 = IJ.openImage("src/test/Resources-static/segtest_small.tif");
    testImage2seed = IJ.openImage("src/test/Resources-static/segmented_color.tif");
    testImage2 = IJ.openImage("src/test/Resources-static/segmented_frame_1.tif");
    IJ.openImage("src/test/Resources-static/segmented_frame_2.tif");

    fluoreszenz1 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_1.tif");
    fluoreszenz2 = IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frame_2.tif");

    testImage1rgb = IJ.openImage("src/test/Resources-static/segtest_small_rgb_test.tif");
    params = new RandomWalkOptions(400.0, 50.0, 100.0, 300.0, 80, 0.1, new Double[] { 8e-3, 1e-3 },
            false, 25);
    params.intermediateFilter = new BinaryFilters.EmptyMorpho();
  }

  /**
   * @throws java.lang.Exception o error
   */
  @After
  public void tearDown() throws Exception {
    testImage1.close();
    testImage1 = null;
    testImage1seed.close();
    testImage1seed = null;
    testImage1rgb.close();
    testImage1rgb = null;
    testImage2 = null;
    fluoreszenz1.close();
    fluoreszenz2.close();
  }

  /**
   * Test of main runner.
   * 
   * <p>post: segmented image comparable to %% data from Repos/Prot_counting/fromMail - testcase for
   * java
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_1() throws Exception {
    RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage1.getProcessor(), params);
    Seeds seeds =
            SeedProcessor.decodeSeedsfromRgb(testImage1seed, Arrays.asList(Color.RED), Color.GREEN);
    IJ.saveAsTiff(new ImagePlus("", seeds.get(SeedTypes.FOREGROUNDS).get(0)),
            tmpdir + "foreSeeds_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", seeds.get(SeedTypes.BACKGROUND).get(0)),
            tmpdir + "backSeeds_QuimP.tif");
    // number of seed correct with matlab file
    ImageProcessor ret = obj.run(seeds);
    ImagePlus results = new ImagePlus("cmp", ret);
    /*
     * Compare to results from
     * /home/p.baniukiewicz/Documents/Repos/QUIMP-Matlab/Matlab/Segmentation/main.m section %%
     * data from Repos/Prot_counting/fromMail - testcase for java small with this file:
     * 
     * plotdiff(imread('/tmp/testRun_cmp.tif'),outj)
     */
    IJ.saveAsTiff(results, tmpdir + "testRun_1_QuimP.tif");
  }

  /**
   * Test of main runner.
   * 
   * <p>post: segmented image comparable to %% data from Repos/Prot_counting/fromMail - testcase for
   * java
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_2() throws Exception {
    RandomWalkSegmentation obj = new RandomWalkSegmentation(fluoreszenz1.getProcessor(), params);
    Seeds seeds =
            SeedProcessor.decodeSeedsfromRgb(testImage2seed, Arrays.asList(Color.RED), Color.GREEN);
    // number of seed correct with matlab file
    ImageProcessor ret = obj.run(seeds);
    ImagePlus results = new ImagePlus("cmp2", ret);
    /**
     * Compare to results from
     * /home/p.baniukiewicz/Documents/Repos/QUIMP-Matlab/Matlab/Segmentation/main.m section %%
     * data from Repos/Prot_counting/fromMail - testcase for java big with this file:
     * 
     * <code>
     * plotdiff(imread('/tmp/testRun_cmp2.tif'),out)
     * </code>
     */
    IJ.saveAsTiff(results, tmpdir + "testRun_2_QuimP.tif");
  }

  /**
   * Test of main runner - localMean.
   * 
   * <p>post: segmented image comparable to %% RW-shrink-mod-gamma_1 - one slice - good Java test
   * Example from Abstract
   * 
   * <pre>
   * <code>
   * ja=imread('/tmp/testRun_4_QuimP.tif');
   * figure;imshow(combineOutlines(org,mask_rough,ja,mask_shrink));
   * </code>
   * </pre>
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_4() throws Exception {
    params.useLocalMean = true;
    params.alpha = 900;
    params.beta = 25;
    params.iter = 300;
    params.gamma[0] = 100;
    params.gamma[1] = 300;
    params.intermediateFilter = new BinaryFilters.SimpleMorpho();
    params.finalFilter = new BinaryFilters.MedianMorpho();

    ImagePlus seed = IJ.openImage(
            "src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_seed.tif");
    ImagePlus org = IJ.openImage(
            "src/test/Resources-static/" + "RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif");
    RandomWalkSegmentation obj = new RandomWalkSegmentation(org.getProcessor(), params);
    Seeds seeds = SeedProcessor.decodeSeedsfromRgb(seed, Arrays.asList(Color.RED), Color.GREEN);
    ImagePlus rough = IJ.openImage("src/test/Resources-static/RW/"
            + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif");
    seeds.put(SeedTypes.ROUGHMASK, rough.getProcessor());
    // number of seed correct with matlab file
    ImageProcessor ret = obj.run(seeds);
    ImagePlus results = new ImagePlus("cmp2", ret);
    IJ.saveAsTiff(results, tmpdir + "testRun_4_QuimP.tif");
  }

  /**
   * Main runner, multiple seeds.
   * 
   * <p>Give wrong results if second sweep is used.
   * 
   * <p>Expected is division line splitting two circles.
   * 
   * @throws Exception on error
   */
  @Test
  public void testRun_5() throws Exception {
    params.gamma[1] = 0; // do not use 2nd sweep - give wrong results
    params.iter = 1000; // stop by error
    ImageProcessor ip = IJ.openImage("src/test/Resources-static/284/2uniform.tif").getProcessor();
    RandomWalkSegmentation obj = new RandomWalkSegmentation(ip, params);
    ImageProcessor seedsIp =
            IJ.openImage("src/test/Resources-static/284/CompositeRGB.tif").getProcessor();
    Seeds seeds = SeedProcessor.decodeSeedsfromRgb(seedsIp,
            Arrays.asList(new Color(244, 0, 0), new Color(0, 0, 244)), new Color(0, 244, 0));
    IJ.saveAsTiff(new ImagePlus("", seeds.get(SeedTypes.FOREGROUNDS).get(0)),
            tmpdir + "foreSeeds5_0_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", seeds.get(SeedTypes.FOREGROUNDS).get(1)),
            tmpdir + "foreSeeds5_1_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", seeds.get(SeedTypes.BACKGROUND).get(0)),
            tmpdir + "backSeeds5_QuimP.tif");

    ImageProcessor ret = obj.run(seeds);
    ImagePlus results = new ImagePlus("cmp", ret);

    // simple stats
    int[] hist = ret.getHistogram();
    // Expecting two circles in color 128 and 255 (after scaling to 8bit)
    // segmented area should be roughly similar
    double ratio = (double) hist[128] / hist[255]; // tested in IJ
    IJ.saveAsTiff(results, tmpdir + "testRun_5_QuimP.tif");
    assertThat(ratio, is(closeTo(0.96555436, 0.1)));

    // now the same if there is no background defined. For this case we expect exactly the same
    // result.
    seeds.remove(SeedTypes.BACKGROUND);
    ret = obj.run(seeds);
    hist = ret.getHistogram();
    double ratio1 = (double) hist[128] / hist[255]; // tested in IJ
    assertThat(ratio1, is(closeTo(ratio, 1e-5)));
  }

  /**
   * Test of main runner.
   * 
   * <p>pre: wrong colors used for seeding
   * 
   * <p>post: Exception
   * 
   * @throws Exception on error
   */
  @Test(expected = RandomWalkException.class)
  public void testRun_3() throws Exception {
    RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage2.getProcessor(), params);
    Seeds seeds = SeedProcessor.decodeSeedsfromRgb(testImage2seed, Arrays.asList(Color.CYAN),
            Color.GREEN);
    // number of seed correct with matlab file
    ImageProcessor ret = obj.run(seeds);
  }

  /**
   * Test precomputed values.
   * 
   * <p>pre: outputs are saved as tiff
   * 
   * <p>post: results are compared with matlab \a java_output_ver.m using breakstop on \a
   * rw_laplace4_java_base.m
   * 
   * @throws Exception on error
   */
  @Test
  public void testPrecomputeGradients() throws Exception {
    RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage1.getProcessor(), params);
    RealMatrix[] ret =
            (RealMatrix[]) accessPrivate("precomputeGradients", obj, new Object[0], new Class[0]);

    IJ.saveAsTiff(new ImagePlus("gRight2", QuimPArrayUtils.realMatrix2ImageProcessor(ret[0])),
            tmpdir + "testPrecompute_gRight2_QuimP.tif");

    IJ.saveAsTiff(new ImagePlus("gTop2", QuimPArrayUtils.realMatrix2ImageProcessor(ret[1])),
            tmpdir + "testPrecompute_gTop2_QuimP.tif");

    IJ.saveAsTiff(new ImagePlus("gLeft2", QuimPArrayUtils.realMatrix2ImageProcessor(ret[2])),
            tmpdir + "testPrecompute_gLeft2_QuimP.tif");

    IJ.saveAsTiff(new ImagePlus("gBottom2", QuimPArrayUtils.realMatrix2ImageProcessor(ret[3])),
            tmpdir + "testPrecompute_gBottom2_QuimP.tif");
  }

  /**
   * Export to RealMatrix and then to tiff.
   */
  @Test
  public void testConversion() {
    RealMatrix image = QuimPArrayUtils.imageProcessor2RealMatrix(testImage1.getProcessor());

    IJ.saveAsTiff(new ImagePlus("orimage", QuimPArrayUtils.realMatrix2ImageProcessor(image)),
            tmpdir + "testConversion_image_QuimP.tif");
  }

  /**
   * Test getMeanSeed.
   * 
   * @throws RandomWalkException one wrong image
   */
  @Test
  public void testGetMeanSeed() throws RandomWalkException {
    RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage2.getProcessor(), params);
    ImagePlus mask = IJ.openImage("src/test/Resources-static/RW/mask.tif");

    RealMatrix ret = obj.getMeanSeedLocal(mask.getProcessor(), 3);

    IJ.saveAsTiff(new ImagePlus("meanseed", QuimPArrayUtils.realMatrix2ImageProcessor(ret)),
            tmpdir + "testGetMeanSeed_QuimP.tif");

    assertThat(ret.hashCode(), is(1549840221)); // checked manually
  }

}
