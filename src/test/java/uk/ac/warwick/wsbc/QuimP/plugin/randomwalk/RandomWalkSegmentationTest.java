/**
 * @file RandomWalkSegmentationTest.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.math3.linear.MatrixUtils;
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

    /**
     * @Test of circshift(RealMatrix, int)
     * @pre any 2D matrix
     * @post this matrix shifted to RIGHT
     */
    @Test
    public void testCircshift_right() throws Exception {
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { 4, 1, 2, 3 }, { 5, 2, 3, 4 }, { 9, 6, 7, 8 } };
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.RIGHT);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * @Test of circshift(RealMatrix, int)
     * @pre any 2D matrix
     * @post this matrix shifted to LEFT
     */
    @Test
    public void testCircshift_left() throws Exception {
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { 2, 3, 4, 1 }, { 3, 4, 5, 2 }, { 7, 8, 9, 6 } };
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.LEFT);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * @Test of circshift(RealMatrix, int)
     * @pre any 2D matrix
     * @post this matrix shifted to TOP
     */
    @Test
    public void testCircshift_top() throws Exception {
        //!<
        double[][] test =     { { 1, 2, 3, 4 },
                                { 2, 3, 4, 5 },
                                { 6, 7, 8, 9 } };
        
        double[][] expected = { { 2, 3, 4, 5 },
                                { 6, 7, 8, 9 },
                                { 1, 2, 3, 4 }};
        /**/
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.TOP);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * @Test of circshift(RealMatrix, int)
     * @pre any 2D matrix
     * @post this matrix shifted to BOTTOM
     */
    @Test
    public void testCircshift_bottom() throws Exception {
        //!<
        double[][] test =     { { 1, 2, 3, 4 },
                                { 2, 3, 4, 5 },
                                { 6, 7, 8, 9 } };
        
        double[][] expected = { { 6, 7, 8, 9 },
                                { 1, 2, 3, 4 },
                                { 2, 3, 4, 5 } };
        /**/
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.BOTTOM);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

}
