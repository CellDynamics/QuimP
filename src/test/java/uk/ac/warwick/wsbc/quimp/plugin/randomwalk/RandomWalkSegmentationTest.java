/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.Params;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.Point;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentation;

// TODO: Auto-generated Javadoc
/**
 * Test protected methods
 * 
 * See: src/test/resources/Matlab/RW_java_tests.m
 * 
 * @author p.baniukiewicz
 * @see <a href="./examples.html">Examples</a>
 */
public class RandomWalkSegmentationTest extends RandomWalkSegmentation {

    /**
     * Instantiates a new random walk segmentation test.
     */
    public RandomWalkSegmentationTest() {
        super(testImage1.getProcessor(), new Params());
    }

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER =
            LoggerFactory.getLogger(RandomWalkSegmentationTest.class.getName());

    /**
     * The test image 1 rgb.
     */
    static ImagePlus testImage1rgb; // contains rgb image with test seed points
    
    /**
     * The test image 1.
     */
    static ImagePlus testImage1;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testImage1rgb = IJ.openImage("src/test/resources/segtest_small_rgb_test.tif");
        testImage1 = IJ.openImage("src/test/resources/segtest_small.tif");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        testImage1rgb.close();
        testImage1rgb = null;
        testImage1.close();
        testImage1 = null;
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

    /**
     * circshift(RealMatrix, int)
     * 
     * Pre: any 2D matrix
     * 
     * Post: this matrix shifted to UP (matlab code issue)
     * 
     * @throws Exception
     */
    @Test
    public void testCircshift_right() throws Exception {
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { 2, 3, 4, 1 }, { 3, 4, 5, 2 }, { 7, 8, 9, 6 } };
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.RIGHT);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * circshift(RealMatrix, int)
     * 
     * Pre: any 2D matrix
     * 
     * Post: this matrix shifted to BOTTOM
     * 
     * @throws Exception
     */
    @Test
    public void testCircshift_left() throws Exception {
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { 4, 1, 2, 3 }, { 5, 2, 3, 4 }, { 9, 6, 7, 8 } };
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.LEFT);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * circshift(RealMatrix, int)
     * 
     * Pre: any 2D matrix
     * 
     * Post: this matrix shifted to LEFT
     * 
     * @throws Exception
     */
    @Test
    public void testCircshift_top() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };

        double[][] expected = { { 2, 3, 4, 5 }, { 6, 7, 8, 9 }, { 1, 2, 3, 4 } };
        /**/
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.TOP);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * Test of circshift(RealMatrix, int)
     * 
     * Pre: any 2D matrix
     * 
     * Post: this matrix shifted to BOTTOM
     * 
     * @throws Exception
     */
    @Test
    public void testCircshift_bottom() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };

        double[][] expected = { { 6, 7, 8, 9 }, { 1, 2, 3, 4 }, { 2, 3, 4, 5 } };
        /**/
        RealMatrix testrm = MatrixUtils.createRealMatrix(test);
        RealMatrix shift = circshift(testrm, RandomWalkSegmentation.BOTTOM);
        assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * Test of getSqrdDiffIntensity(RealMatrix, RealMatrix)
     * 
     * @throws Exception
     */
    @Test
    public void testGetSqrdDiffIntensity() throws Exception {
        //!<
        double[][] a = { { 1, 2 }, { 2, 3 } };

        double[][] b = { { 3, 4 }, { 6, 2 } };

        double[][] expected = { { 4, 4 }, { 16, 1 } };
        /**/
        RealMatrix out = getSqrdDiffIntensity(MatrixUtils.createRealMatrix(a),
                MatrixUtils.createRealMatrix(b));
        assertThat(out, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * Test of getMin(RealMatrix)
     * 
     * @throws Exception
     */
    @Test
    public void testGetMin() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        /**/
        assertThat(getMax(MatrixUtils.createRealMatrix(test)), is(40.0));
    }

    /**
     * Analysis of getSubMatrix from Apache
     */
    @Test
    public void testGetSubMatrix() {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        /**/
        int r[] = { 0, 1, 0 };
        int c[] = { 0, 2, 3 };

        RealMatrix in = MatrixUtils.createRealMatrix(test);
        RealMatrix out = in.getSubMatrix(r, c);
        LOGGER.debug(out.toString());
    }

    /**
     * Test of decodeSeeds(ImagePlus, Color, Color)
     * 
     * Pre: Image with gree/red seed with known positions (\a segtest_small.rgb.tif)
     * 
     * Post: Two lists with positions ot seeds
     * 
     * @throws Exception
     */
    @Test
    public void testDecodeSeeds() throws Exception {
        Set<Point> expectedForeground = new HashSet<Point>();
        expectedForeground.add(new Point(70, 70));
        expectedForeground.add(new Point(70, 71));
        expectedForeground.add(new Point(70, 72));
        expectedForeground.add(new Point(20, 100));
        expectedForeground.add(new Point(97, 172));

        Set<Point> expectedBackground = new HashSet<Point>();
        expectedBackground.add(new Point(20, 20));
        expectedBackground.add(new Point(40, 40));
        expectedBackground.add(new Point(60, 60));

        Map<Integer, List<Point>> ret = decodeSeeds(testImage1rgb, Color.RED, Color.GREEN);

        Set<Point> fseeds = new HashSet<>(ret.get(RandomWalkSegmentation.FOREGROUND));
        Set<Point> bseeds = new HashSet<>(ret.get(RandomWalkSegmentation.BACKGROUND));

        assertThat(fseeds, is(expectedForeground));
        assertThat(bseeds, is(expectedBackground));

    }

    /**
     * Test of getValues(RealMatrix, List<Point>)
     * 
     * @throws Exception
     */
    @Test
    public void testGetValues() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        /**/
        double[] expected = { 1, 6, 40 };
        RealMatrix in = MatrixUtils.createRealMatrix(test);
        LOGGER.debug(Double.toString(in.getEntry(1, 2))); // row,col
        List<Point> ind = new ArrayList<>();
        ind.add(new Point(0, 0));
        ind.add(new Point(0, 2));
        ind.add(new Point(2, 1));
        LOGGER.debug(ind.toString());
        ArrayRealVector ret = getValues(in, ind);
        assertThat(ret.getDataRef(), is(expected));
    }

    /**
     * of setValues(RealMatrix, List<Point>, ArrayRealVector)
     * 
     * Pre: number of values equals number of indexes
     * 
     * @throws Exception
     */
    @Test
    public void testSetValues() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { -1, 2, 3, 4 }, { 2, 3, -3, 5 }, { -2, 7, 8, 9 } };
        /**/
        RealMatrix in = MatrixUtils.createRealMatrix(test);
        List<Point> ind = new ArrayList<>();
        ind.add(new Point(0, 0)); // col,row
        ind.add(new Point(0, 2));
        ind.add(new Point(2, 1));
        double[] toSet = { -1, -2, -3 }; // values to set into indexes ind
        setValues(in, ind, new ArrayRealVector(toSet)); // input is modified
        assertThat(in, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * of setValues(RealMatrix, List<Point>, ArrayRealVector)
     * 
     * Pre: one value many indexes
     * 
     * Post: the same value in every provided index
     * 
     * @throws Exception
     */
    @Test
    public void testSetValues_1() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { -1, 2, 3, 4 }, { 2, 3, -1, 5 }, { -1, 7, 8, 9 } };
        //!>
        RealMatrix in = MatrixUtils.createRealMatrix(test);
        List<Point> ind = new ArrayList<>();
        ind.add(new Point(0, 0)); // col,row
        ind.add(new Point(0, 2));
        ind.add(new Point(2, 1));
        double[] toSet = { -1 }; // values to set into indexes ind
        setValues(in, ind, new ArrayRealVector(toSet)); // input is modified
        assertThat(in, is(MatrixUtils.createRealMatrix(expected)));
    }

    /**
     * setValues(RealMatrix, List<Point>, ArrayRealVector)
     * 
     * Pre: number of values is not 1 and does not equal to the number of indexes
     * 
     * Post: exception
     * 
     * @throws Exception
     */
    @Test(expected = InvalidParameterException.class)
    public void testSetValues_2() throws Exception {
        //!<
        double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
        double[][] expected = { { -1, 2, 3, 4 }, { 2, 3, -1, 5 }, { -1, 7, 8, 9 } };
        /**/
        RealMatrix in = MatrixUtils.createRealMatrix(test);
        List<Point> ind = new ArrayList<>();
        ind.add(new Point(0, 0)); // col,row
        ind.add(new Point(0, 2));
        ind.add(new Point(2, 1));
        double[] toSet = { -1, -2 }; // values to set into indexes ind
        setValues(in, ind, new ArrayRealVector(toSet)); // input is modified
        assertThat(in, is(MatrixUtils.createRealMatrix(expected)));
    }

}
