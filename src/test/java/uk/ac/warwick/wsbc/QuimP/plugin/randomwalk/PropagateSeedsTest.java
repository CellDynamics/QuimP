/**
 * @file PropagateSeedsTest.java
 * @date 1 Jul 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.BinaryProcessor;

/**
 * @author p.baniukiewicz
 * @date 1 Jul 2016
 *
 */
public class PropagateSeedsTest {

    static ImagePlus testImage2;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testImage2 = IJ.openImage("src/test/resources/binary_1.tif");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        testImage2.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPropagateSeed() throws Exception {
        ImagePlus ip = testImage2.duplicate();
        BinaryProcessor ret = new BinaryProcessor(ip.getProcessor().convertToByteProcessor());
        Map<Integer, List<Point>> seed = PropagateSeeds.propagateSeed(ret, 20);
        IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testPropagateSeed_20.tif");
    }

    /**
     * @test of eroding
     * @post eroded image on disk
     * @throws Exception
     */
    @Test
    public void testIterateMorphological() throws Exception {
        ImagePlus ip = testImage2.duplicate();
        BinaryProcessor ret = new BinaryProcessor(ip.getProcessor().convertToByteProcessor());
        PropagateSeeds.iterateMorphological(ret, PropagateSeeds.ERODE, 20);
        IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testIterateMorphological_erode20.tif");

        PropagateSeeds.iterateMorphological(ret, PropagateSeeds.DILATE, 40);
        IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testIterateMorphological_dilate40.tif");

    }

}
