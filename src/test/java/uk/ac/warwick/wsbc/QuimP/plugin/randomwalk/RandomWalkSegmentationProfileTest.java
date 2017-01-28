/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Normal test but used for profiling
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationProfileTest {

    static final Logger LOGGER =
            LoggerFactory.getLogger(RandomWalkSegmentationProfileTest.class.getName());

    static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

    static Object accessPrivate(String name, RandomWalkSegmentation obj, Object[] param,
            Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
        prv.setAccessible(true);
        return prv.invoke(obj, param);
    }

    private ImagePlus testImage2_1seed;
    private ImagePlus fluoreszenz_1, fluoreszenz_2;
    Params p;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testImage2_1seed = IJ.openImage("src/test/resources/segmented_color.tif");

        fluoreszenz_1 = IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frame_1.tif");
        fluoreszenz_2 = IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frame_2.tif");

        p = new Params(400, 50, 100, 300, 80, 0.1, 8e-3);
        // Thread.sleep(10000);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        fluoreszenz_1.close();
        fluoreszenz_2.close();
    }

    /**
     * Test of main runner use propagateseed
     * 
     * pre: two frames
     * 
     * @throws Exception
     */
    @Test
    public void testRun_4() throws Exception {
        long startTime = System.nanoTime();

        RandomWalkSegmentation obj = new RandomWalkSegmentation(fluoreszenz_1.getProcessor(), p);
        Map<Integer, List<Point>> seeds = obj.decodeSeeds(testImage2_1seed, Color.RED, Color.GREEN);
        ImageProcessor ret_frame_1 = obj.run(seeds);
        Map<Integer, List<Point>> nextseed =
                new PropagateSeeds.Morphological().propagateSeed(ret_frame_1, 3, 4.5);
        obj = new RandomWalkSegmentation(fluoreszenz_2.getProcessor(), p);
        ImageProcessor ret_frame_2 = obj.run(nextseed);

        long stopTime = System.nanoTime();
        LOGGER.info("--Time used -- " + (double) (stopTime - startTime) / 1000000000.0 + " [s]");

        ImagePlus results_frame_2 = new ImagePlus("cmp", ret_frame_2);
        IJ.saveAsTiff(results_frame_2, tmpdir + "testRun_4_f2.tif");
        ImagePlus results_frame_1 = new ImagePlus("cmp", ret_frame_1);
        IJ.saveAsTiff(results_frame_1, tmpdir + "testRun_4_f1.tif");
    }

}
