/**
 * @file RandomWalkSegmentationTest.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;

/**
 * @author p.baniukiewicz
 * @date 22 Jun 2016
 *
 */
public class RandomWalkSegmentationTest extends RandomWalkSegmentation {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER =
            LogManager.getLogger(RandomWalkSegmentationTest.class.getName());

    static ImagePlus testImage1; // original 8bit grayscale
    static ImagePlus testImage1seed; // contains rgb image with seed

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testImage1seed = IJ.openImage("src/test/resources/segtest_small_seed.tif");
        testImage1 = IJ.openImage("src/test/resources/segtest_small.tif");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        testImage1.close();
        testImage1 = null;
        testImage1seed.close();
        testImage1seed = null;
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
        if (testImage1.changes) { // check if source was modified
            testImage1.changes = false; // set flag to false to prevent save dialog
            throw new Exception("Image has been modified"); // throw exception if source image
                                                            // was modified
        }
    }

    @Test
    public void x() {

    }

}
