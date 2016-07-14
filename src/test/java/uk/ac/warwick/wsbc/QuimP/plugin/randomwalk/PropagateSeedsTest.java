/**
 * @file PropagateSeedsTest.java
 * @date 1 Jul 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.core.config.Configurator;
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

    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
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
        // IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testPropagateSeed_20.tif");
    }

    /**
     * @test of eroding
     * @post eroded image on disk
     * @throws Exception
     */
    @Test
    public void testIterateMorphological() throws Exception {
        ImagePlus ip = testImage2.duplicate();
        BinaryProcessor rete =
                new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
        BinaryProcessor retd =
                new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
        PropagateSeeds.iterateMorphological(rete, PropagateSeeds.ERODE, 3);
        IJ.saveAsTiff(new ImagePlus("", rete), "/tmp/testIterateMorphological_erode3.tif");

        PropagateSeeds.iterateMorphological(retd, PropagateSeeds.DILATE, 5);
        IJ.saveAsTiff(new ImagePlus("", retd), "/tmp/testIterateMorphological_dilate5.tif");

    }

}
