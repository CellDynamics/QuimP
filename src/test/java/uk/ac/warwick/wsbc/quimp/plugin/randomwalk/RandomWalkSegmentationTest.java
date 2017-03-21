package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
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
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * Test protected methods.
 * 
 * <p>See: src/test/resources/Matlab/RW_java_tests.m
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
    super(testImage1.getProcessor(), new Params());
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
   * @throws java.lang.Exception on error
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testImage1rgb = IJ.openImage("src/test/resources/segtest_small_rgb_test.tif");
    testImage1 = IJ.openImage("src/test/resources/segtest_small.tif");
  }

  /**
   * @throws java.lang.Exception on error
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    testImage1rgb.close();
    testImage1rgb = null;
    testImage1.close();
    testImage1 = null;
  }

  /**
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception on error
   */
  @After
  public void tearDown() throws Exception {

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
   * Test of decodeSeeds(ImagePlus, Color, Color).
   * 
   * <p>Pre: Image with green/red seed with known positions (segtest_small.rgb.tif)
   * 
   * <p>Post: Two lists with positions of seeds
   * 
   * @throws Exception on error
   */
  @Test
  public void testDecodeSeeds() throws Exception {
    Set<Point> expectedForeground = new HashSet<Point>();
    expectedForeground.add(new Point(70, 70));
    expectedForeground.add(new Point(71, 70));
    expectedForeground.add(new Point(72, 70));
    expectedForeground.add(new Point(100, 20));
    expectedForeground.add(new Point(172, 97));

    Set<Point> expectedBackground = new HashSet<Point>();
    expectedBackground.add(new Point(20, 20));
    expectedBackground.add(new Point(40, 40));
    expectedBackground.add(new Point(60, 60));

    Map<Seeds, ImageProcessor> ret = decodeSeeds(testImage1rgb, Color.RED, Color.GREEN);
    Map<Seeds, List<Point>> list = RandomWalkSegmentation.convertToList(ret);

    Set<Point> fseeds = new HashSet<>(list.get(Seeds.FOREGROUND));
    Set<Point> bseeds = new HashSet<>(list.get(Seeds.BACKGROUND));

    assertThat(fseeds, is(expectedForeground));
    assertThat(bseeds, is(expectedBackground));
  }

  /**
   * Test of decodeSeeds(ImagePlus, Color, Color).
   * 
   * <p>Pre: Mask image. Test approach if input is binary mask converted to rgb
   * 
   * <p>Post: Two lists with positions of seeds
   * 
   * @throws Exception on error
   */
  @Test
  public void testDecodeSeedsBW() throws Exception {
    Set<Point> expectedForeground = new HashSet<Point>();
    expectedForeground.add(new Point(218, 120));
    expectedForeground.add(new Point(233, 118));
    expectedForeground.add(new Point(239, 132));
    expectedForeground.add(new Point(249, 131));
    expectedForeground.add(new Point(322, 225));

    Set<Point> expectedBackground = new HashSet<Point>();
    expectedBackground.add(new Point(334, 321));
    expectedBackground.add(new Point(238, 81));
    expectedBackground.add(new Point(319, 246));

    ImagePlus testImage = IJ.openImage("src/test/resources/GMask.tif");
    new ImageConverter(testImage).convertToRGB(); // convert to rgb

    Map<Seeds, ImageProcessor> ret = decodeSeeds(testImage, Color.WHITE, Color.BLACK);
    Map<Seeds, List<Point>> list = RandomWalkSegmentation.convertToList(ret);

    Set<Point> fseeds = new HashSet<>(list.get(Seeds.FOREGROUND));
    Set<Point> bseeds = new HashSet<>(list.get(Seeds.BACKGROUND));

    for (Point p : expectedForeground) {
      assertTrue(fseeds.contains(p));
    }
    for (Point p : expectedBackground) {
      assertTrue(bseeds.contains(p));
    }
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
   * {@link RandomWalkSegmentation#realMatrix2ImageProcessor(RealMatrix)}.
   */
  @Test
  public void testRealMatrix2ImageProcessor() {
    int rows = 10; // y
    int cols = 20; // x
    RealMatrix test = new Array2DRowRealMatrix(rows, cols);
    test.setEntry(3, 7, 50); // row, col
    test.setEntry(6, 19, 25); // row, col
    ImageProcessor out = realMatrix2ImageProcessor(test);
    assertThat(out.getHeight(), is(rows));
    assertThat(out.getWidth(), is(cols));
    assertThat(out.getPixelValue(7, 3), is(50f));
    assertThat(out.getPixelValue(19, 6), is(25f)); // x y
  }

  /**
   * Test method for
   * {@link RandomWalkSegmentation#imageProcessor2RealMatrix(ImageProcessor)}.
   */
  @Test
  public void testImageProcessor2RealMatrix() {
    int rows = 10; // y
    int cols = 20; // x
    ImageProcessor test = new FloatProcessor(cols, rows); // width, height
    test.putPixelValue(7, 3, 50); // x y
    test.putPixelValue(19, 6, 25); // x y
    RealMatrix out = imageProcessor2RealMatrix(test);
    assertThat(out.getRowDimension(), is(rows));
    assertThat(out.getColumnDimension(), is(cols));
    assertThat(out.getEntry(3, 7), is(50.0));
    assertThat(out.getEntry(6, 19), is(25.0)); // row column

  }

}
