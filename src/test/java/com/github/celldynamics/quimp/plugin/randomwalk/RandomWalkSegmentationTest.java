package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;

/**
 * Test API methods (mostly protected).
 * 
 * <p>See: src/test/Resources-static/Matlab/RW_java_tests.m
 * 
 * <p>See: Abstract/main.m
 * 
 * @author p.baniukiewicz
 * @see <a href="./examples.html">Examples</a>
 */
public class RandomWalkSegmentationTest extends RandomWalkSegmentation {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Instantiates a new random walk segmentation test.
   * 
   * @throws RandomWalkException on wrong image
   */
  public RandomWalkSegmentationTest() throws RandomWalkException {
    super(testImage1.getProcessor(), new RandomWalkOptions());
  }

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkSegmentationTest.class.getName());

  /**
   * The test image 1 rgb.
   */
  static ImagePlus testImage1rgb; // contains rgb image with test seed points

  /**
   * The test image 1.
   */
  static ImagePlus testImage1;

  /**
   * Load test data.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testImage1rgb = IJ.openImage("src/test/Resources-static/segtest_small_rgb_test.tif");
    testImage1 = IJ.openImage("src/test/Resources-static/segtest_small.tif");
  }

  /**
   * Clean.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    testImage1rgb.close();
    testImage1rgb = null;
    testImage1.close();
    testImage1 = null;
  }

  /**
   * circshift(RealMatrix, int).
   * 
   * <p>Pre: any 2D matrix
   * 
   * <p>Post: this matrix shifted to UP (matlab code issue)
   * 
   * @throws Exception on error
   */
  @Test
  public void testCircshift_right() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
    double[][] expected = { { 2, 3, 4, 1 }, { 3, 4, 5, 2 }, { 7, 8, 9, 6 } };
    RealMatrix testrm = MatrixUtils.createRealMatrix(test);
    RealMatrix shift = circshift(testrm, RandomWalkSegmentation.TOP);
    assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
  }

  /**
   * circshift(RealMatrix, int).
   * 
   * <p>Pre: any 2D matrix
   * 
   * <p>Post: this matrix shifted to BOTTOM
   * 
   * @throws Exception on error
   */
  @Test
  public void testCircshift_left() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };
    double[][] expected = { { 4, 1, 2, 3 }, { 5, 2, 3, 4 }, { 9, 6, 7, 8 } };
    RealMatrix testrm = MatrixUtils.createRealMatrix(test);
    RealMatrix shift = circshift(testrm, RandomWalkSegmentation.BOTTOM);
    assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
  }

  /**
   * circshift(RealMatrix, int).
   * 
   * <p>Pre: any 2D matrix
   * 
   * <p>Post: this matrix shifted to LEFT
   * 
   * @throws Exception on error
   */
  @Test
  public void testCircshift_top() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };

    double[][] expected = { { 2, 3, 4, 5 }, { 6, 7, 8, 9 }, { 1, 2, 3, 4 } };
    RealMatrix testrm = MatrixUtils.createRealMatrix(test);
    RealMatrix shift = circshift(testrm, RandomWalkSegmentation.RIGHT);
    assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
  }

  /**
   * Test of circshift(RealMatrix, int).
   * 
   * <p>Pre: any 2D matrix
   * 
   * <p>Post: this matrix shifted to BOTTOM
   * 
   * @throws Exception on error
   */
  @Test
  public void testCircshift_bottom() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 6, 7, 8, 9 } };

    double[][] expected = { { 6, 7, 8, 9 }, { 1, 2, 3, 4 }, { 2, 3, 4, 5 } };
    RealMatrix testrm = MatrixUtils.createRealMatrix(test);
    RealMatrix shift = circshift(testrm, RandomWalkSegmentation.LEFT);
    assertThat(shift, is(MatrixUtils.createRealMatrix(expected)));
  }

  /**
   * Test of getSqrdDiffIntensity(RealMatrix, RealMatrix).
   *
   * @throws Exception on error
   */
  @Test
  public void testGetSqrdDiffIntensity() throws Exception {
    double[][] a = { { 1, 2 }, { 2, 3 } };

    double[][] b = { { 3, 4 }, { 6, 2 } };

    double[][] expected = { { 4, 4 }, { 16, 1 } };
    RealMatrix out =
            getSqrdDiffIntensity(MatrixUtils.createRealMatrix(a), MatrixUtils.createRealMatrix(b));
    assertThat(out, is(MatrixUtils.createRealMatrix(expected)));
  }

  /**
   * Analysis of getSubMatrix from Apache.
   */
  @Test
  public void testGetSubMatrix() {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    int[] r = { 0, 1, 0 };
    int[] c = { 0, 2, 3 };

    RealMatrix in = MatrixUtils.createRealMatrix(test);
    RealMatrix out = in.getSubMatrix(r, c);
    LOGGER.debug(out.toString());
  }

  /**
   * Test of
   * {@link com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.MatrixDotDivN}.
   * 
   * @throws Exception on Error
   */
  @Test
  public void testMatrixDotDivN() throws Exception {
    RealMatrix a = MatrixUtils.createRealMatrix(new double[][] { { 6, 2 }, { 8, 12 } });

    RealMatrix b = MatrixUtils.createRealMatrix(new double[][] { { 3, 0 }, { 4, 2 } });

    RealMatrix expected = MatrixUtils.createRealMatrix(new double[][] { { 2, 0 }, { 2, 6 } });

    a.walkInOptimizedOrder(new MatrixDotDivN(b));

    assertThat(a, is(expected));

  }

  /**
   * Test of getValues(RealMatrix, List).
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetValues() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    RealMatrix in = MatrixUtils.createRealMatrix(test);
    LOGGER.debug(Double.toString(in.getEntry(1, 2))); // row,col
    List<Point> ind = new ArrayList<>();
    ind.add(new Point(0, 0));
    ind.add(new Point(0, 2));
    ind.add(new Point(2, 1));
    LOGGER.debug(ind.toString());
    ArrayRealVector ret = getValues(in, ind);

    double[] expected = { 1, 6, 40 };
    assertThat(ret.getDataRef(), is(expected));
  }

  /**
   * of setValues(RealMatrix, List, ArrayRealVector).
   * 
   * <p>Pre: number of values equals number of indexes
   * 
   * @throws Exception on error
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
   * of setValues(RealMatrix, List, ArrayRealVector).
   * 
   * <p>Pre: one value many indexes
   * 
   * <p>Post: the same value in every provided index
   * 
   * @throws Exception on error
   */
  @Test
  public void testSetValues_1() throws Exception {
    //!>
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    double[][] expected = { { -1, 2, 3, 4 }, { 2, 3, -1, 5 }, { -1, 7, 8, 9 } };
    //!<
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
   * setValues(RealMatrix, List, ArrayRealVector).
   * 
   * <p>Pre: number of values is not 1 and does not equal to the number of indexes
   * 
   * <p>Post: exception
   * 
   * @throws Exception on error
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

  /**
   * Test method for
   * {@link RandomWalkSegmentation#computeRelErr(double[][], double[][])}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testComputeRelErr() throws Exception {
    //!>
    double[][] fg = new double[][] {
      {8,1,6},
      {3,5,7},
      {4,9,2}
    };
    double[][] fglast = new double[][] {
      {8,3,4},
      {1,5,9},
      {6,7,2}
    };
    double expected = 0.36666666666666664;
    //!<

    RandomWalkSegmentation rws =
            new RandomWalkSegmentation(testImage1.getProcessor(), new RandomWalkOptions());
    double ret = rws.computeRelErr(fglast, fg);
    assertThat(ret, is(expected));
  }

  /**
   * Test of {@link RandomWalkSegmentation#compare(ProbabilityMaps)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testCompare() throws Exception {
    // probability for 1 obj
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    // probability for 2nd object
    double[][] m2d = { { 0.1, 0.8, 0.9 }, { 10, 11, 12 } };
    // background
    double[][] m3d = { { 0, 0, 0 }, { 0, 0, 15 } };
    // higher wins, returned indexes of object, 0 for bck
    double[][] exp = { { 1, 1, 1 }, { 2, 2, 0 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);
    RealMatrix m3 = new Array2DRowRealMatrix(m3d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);
    obj.put(SeedTypes.BACKGROUND, m3);

    RandomWalkSegmentation rws =
            new RandomWalkSegmentation(testImage1.getProcessor(), new RandomWalkOptions());
    RealMatrix ret = rws.compare(obj);
    assertThat(ret, is(MatrixUtils.createRealMatrix(exp)));
  }

  /**
   * Test of {@link RandomWalkSegmentation#flatten(ProbabilityMaps, SeedTypes)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testFlatten() throws Exception {
    // probability for 1 obj
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    // probability for 2nd object
    double[][] m2d = { { 0.1, 0.8, 0.9 }, { 10, 11, 12 } };
    // higher wins, returned indexes of object, 0 for bck
    double[][] exp = { { 1, 2, 3 }, { 10, 11, 12 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);

    RandomWalkSegmentation rws =
            new RandomWalkSegmentation(testImage1.getProcessor(), new RandomWalkOptions());
    double[][] ret = rws.flatten(obj, SeedTypes.FOREGROUNDS);
    assertThat(MatrixUtils.createRealMatrix(ret), is(MatrixUtils.createRealMatrix(exp)));
  }

}
