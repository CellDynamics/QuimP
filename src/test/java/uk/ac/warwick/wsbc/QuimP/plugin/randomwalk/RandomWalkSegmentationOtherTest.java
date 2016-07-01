/**
 * @file RandomWalkSegmentationOtherTest.java
 * @date 30 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentation.Point;

/**
 * @author p.baniukiewicz
 * @date 30 Jun 2016
 *
 */
public class RandomWalkSegmentationOtherTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER =
            LogManager.getLogger(RandomWalkSegmentationOtherTest.class.getName());

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
    Params p;

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
        testImage1seed = IJ.openImage("src/test/resources/segtest_small_seed.tif");
        testImage1 = IJ.openImage("src/test/resources/segtest_small.tif");
        testImage2seed = IJ.openImage("src/test/resources/segmented_color.tif");
        testImage2 = IJ.openImage("src/test/resources/segmented_frame_1.tif");
        testImage1rgb = IJ.openImage("src/test/resources/segtest_small_rgb_test.tif");
        p = new Params(400, 50, 100, 300, 40, 0.1, 8e-3);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        testImage1.close();
        testImage1 = null;
        testImage1seed.close();
        testImage1seed = null;
        testImage1rgb.close();
        testImage1rgb = null;
    }

    /**
     * @test Test of main runner
     * @post segmented image comparable to %% data from Repos/Prot_counting/fromMail - testcase for java
     * @throws Exception
     */
    @Test
    public void testRun_1() throws Exception {
        RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage1.getProcessor(), p);
        Map<Integer, List<Point>> seeds = obj.decodeSeeds(testImage1seed, Color.RED, Color.GREEN);
        LOGGER.debug("FG seeds: " + seeds.get(RandomWalkSegmentation.FOREGROUND).size());
        LOGGER.debug("BG seeds: " + seeds.get(RandomWalkSegmentation.BACKGROUND).size());
        // number of seed correct with matlab file
        ImageProcessor ret = obj.run(seeds);
        ImagePlus results = new ImagePlus("cmp", ret);
        /**
         * Compare to results from 
         * /home/baniuk/Documents/Repos/QUIMP-Matlab/Matlab/Segmentation/main.m section
         * %% data from Repos/Prot_counting/fromMail - testcase for java small
         * with this file:
         * @code
         * plotdiff(imread('/tmp/testRun_cmp.tif'),outj)
         * @endcode
         */
        IJ.saveAsTiff(results, "/tmp/testRun_cmp.tif");
    }

    /**
     * @test Test of main runner
     * @post segmented image comparable to %% data from Repos/Prot_counting/fromMail - testcase for java
     * @throws Exception
     */
    @Test
    public void testRun_2() throws Exception {
        RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage2.getProcessor(), p);
        Map<Integer, List<Point>> seeds = obj.decodeSeeds(testImage2seed, Color.RED, Color.GREEN);
        LOGGER.debug("FG seeds: " + seeds.get(RandomWalkSegmentation.FOREGROUND).size());
        LOGGER.debug("BG seeds: " + seeds.get(RandomWalkSegmentation.BACKGROUND).size());
        // number of seed correct with matlab file
        ImageProcessor ret = obj.run(seeds);
        ImagePlus results = new ImagePlus("cmp2", ret);
        /**
         * Compare to results from 
         * /home/baniuk/Documents/Repos/QUIMP-Matlab/Matlab/Segmentation/main.m section
         * %% data from Repos/Prot_counting/fromMail - testcase for java big
         * with this file:
         * @code
         * plotdiff(imread('/tmp/testRun_cmp2.tif'),out)
         * @endcode
         */
        IJ.saveAsTiff(results, "/tmp/testRun_cmp2.tif");
    }

    /**
     * @test Test precomputed values
     * @pre outputs are saved as tiff
     * @post results are compared with matlab \a java_output_ver.m using breakstop on
     * \a  rw_laplace4_java_base.m
     * @throws Exception
     */
    @Test
    public void testPrecompute() throws Exception {
        RandomWalkSegmentation obj = new RandomWalkSegmentation(testImage1.getProcessor(), p);
        RealMatrix[] ret =
                (RealMatrix[]) accessPrivate("precompute", obj, new Object[0], new Class[0]);

        IJ.saveAsTiff(
                new ImagePlus("gRight2", RandomWalkSegmentation.RealMatrix2ImageProcessor(ret[0])),
                "/tmp/testPrecompute_gRight2.tif");

        IJ.saveAsTiff(
                new ImagePlus("gTop2", RandomWalkSegmentation.RealMatrix2ImageProcessor(ret[1])),
                "/tmp/testPrecompute_gTop2.tif");

        IJ.saveAsTiff(
                new ImagePlus("gLeft2", RandomWalkSegmentation.RealMatrix2ImageProcessor(ret[2])),
                "/tmp/testPrecompute_gLeft2.tif");

        IJ.saveAsTiff(
                new ImagePlus("gBottom2", RandomWalkSegmentation.RealMatrix2ImageProcessor(ret[3])),
                "/tmp/testPrecompute_gBottom2.tif");
    }

    /**
     * @test Export to RealMatrix and then to tiff
     */
    @Test
    public void testConversion() {
        RealMatrix image =
                RandomWalkSegmentation.ImageProcessor2RealMatrix(testImage1.getProcessor());

        IJ.saveAsTiff(
                new ImagePlus("orimage", RandomWalkSegmentation.RealMatrix2ImageProcessor(image)),
                "/tmp/testConversion_image.tif");

    }

}
