package com.github.celldynamics.quimp.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * The Class QuimPArrayUtilsTest.
 *
 * @author p.baniukiewicz
 */
public class QuimPArrayUtilsTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimPArrayUtilsTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.QuimPArrayUtils#float2ddouble(float[][])}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testFloat2Ddouble() throws Exception {
    float[][] in = { { 1.0f, 2.0f, 3.0f }, { 1.11f, 2.11f, 3.11f } };
    double[][] out = QuimPArrayUtils.float2ddouble(in);
    for (int r = 0; r < 2; r++) {
      for (int c = 0; c < 3; c++) {
        assertEquals(in[r][c], out[r][c], 1e-3);
      }
    }
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.QuimPArrayUtils#double2dfloat(double[][])}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testDouble2Float() throws Exception {
    double[][] in = { { 1.0, 2.0, 3.0 }, { 1.11, 2.11, 3.11 } };
    float[][] out = QuimPArrayUtils.double2dfloat(in);
    for (int r = 0; r < 2; r++) {
      for (int c = 0; c < 3; c++) {
        assertEquals(in[r][c], out[r][c], 1e-3);
      }
    }
  }

  /**
   * Test method of {@link QuimPArrayUtils#minListIndex(java.util.List)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testMinListIndex() throws Exception {
    ArrayList<Double> ar = new ArrayList<>();
    ar.add(34.0);
    ar.add(5.0);
    ar.add(-5.0);

    assertThat(QuimPArrayUtils.minListIndex(ar), equalTo(2));
  }

  /**
   * Test method for
   * {@link QuimPArrayUtils#file2Array(java.lang.String, java.io.File)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testFile2Array() throws Exception {
    //!>
    double[][] expected =
        { { 1, 2, 3, 4, 5 },
        { 1.1, 2.2, 3.3, 4.4, 5.5 },
        { 6, 7, 8, 9, Math.PI } };
    //!<
    QuimPArrayUtils.arrayToFile(expected, ",", new File(tmpdir + "testFile2Array.map"));

    double[][] test = QuimPArrayUtils.file2Array(",", new File(tmpdir + "testFile2Array.map"));

    assertThat(test, is(expected));

  }

  /**
   * Test method for
   * {@link QuimPArrayUtils#realMatrix2D2File(RealMatrix, java.lang.String)}.
   * 
   * @throws IOException on file problem
   */
  @Test
  public void testRealMatrix2D2File() throws IOException {
    int rows = 4;
    int cols = 3;
    RealMatrix test = new Array2DRowRealMatrix(rows, cols);
    int l = 0;
    for (int r = 0; r < rows; r++) {
      for (int k = 0; k < cols; k++) {
        test.setEntry(r, k, l++);
      }
    }
    QuimPArrayUtils.realMatrix2D2File(test, tmpdir + "testRealMatrix2D2File.txt");
  }

  /**
   * Test of getMax(RealMatrix).
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetMax() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    assertThat(QuimPArrayUtils.getMax(MatrixUtils.createRealMatrix(test)), is(40.0));
  }

  /**
   * Test of getMin(RealMatrix).
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetMin() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    assertThat(QuimPArrayUtils.getMin(MatrixUtils.createRealMatrix(test)), is(1.0));
  }

  /**
   * Test of getMeanR.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetMeanR() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    assertThat(QuimPArrayUtils.getMeanR(test), is(new double[] { 2.5, 12.5, 7.5 }));
  }

  /**
   * Test of getMeanC.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetMeanC() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    assertThat(QuimPArrayUtils.getMeanC(test), is(new double[] { 3, 4, 17, 6 }));
  }

  /**
   * Test of testCopy2darray.
   * 
   * @throws Exception on error
   */
  @Test
  public void testCopy2darray() throws Exception {
    double[][] test = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };
    double[][] ret = null;
    double[][] exp = { { 1, 2, 3, 4 }, { 2, 3, 40, 5 }, { 6, 7, 8, 9 } };

    assertThat(QuimPArrayUtils.copy2darray(test, ret), is(exp));

    ret = QuimPArrayUtils.initDouble2dArray(3, 4);
    assertThat(QuimPArrayUtils.copy2darray(test, ret), is(exp));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.utils.QuimPArrayUtils#arrayMax(int[])}.
   * 
   * @throws Exception omn error
   */
  @Test
  public void testArray2dMaxIntArray() throws Exception {
    int[] test = new int[] { 2, 4, 5, 6, 7, 8, 1, 76 };
    assertThat(QuimPArrayUtils.arrayMax(test), is(76));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.utils.QuimPArrayUtils#sumArray(int[])}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testSumArray() throws Exception {
    int[] test = new int[] { 2, 4, 5, 6, 7, 8, 1, 76 };
    assertThat(QuimPArrayUtils.sumArray(test), is(109));
  }

  /**
   * Test method for
   * {@link QuimPArrayUtils#realMatrix2ImageProcessor(RealMatrix)}.
   */
  @Test
  public void testRealMatrix2ImageProcessor() {
    int rows = 10; // y
    int cols = 20; // x
    RealMatrix test = new Array2DRowRealMatrix(rows, cols);
    test.setEntry(3, 7, 50); // row, col
    test.setEntry(6, 19, 25); // row, col
    ImageProcessor out = QuimPArrayUtils.realMatrix2ImageProcessor(test);
    assertThat(out.getHeight(), is(rows));
    assertThat(out.getWidth(), is(cols));
    assertThat(out.getPixelValue(7, 3), is(50f));
    assertThat(out.getPixelValue(19, 6), is(25f)); // x y
  }

  /**
   * Test method for
   * {@link QuimPArrayUtils#imageProcessor2RealMatrix(ImageProcessor)}.
   */
  @Test
  public void testImageProcessor2RealMatrix() {
    int rows = 10; // y
    int cols = 20; // x
    ImageProcessor test = new FloatProcessor(cols, rows); // width, height
    test.putPixelValue(7, 3, 50); // x y
    test.putPixelValue(19, 6, 25); // x y
    RealMatrix out = QuimPArrayUtils.imageProcessor2RealMatrix(test);
    assertThat(out.getRowDimension(), is(rows));
    assertThat(out.getColumnDimension(), is(cols));
    assertThat(out.getEntry(3, 7), is(50.0));
    assertThat(out.getEntry(6, 19), is(25.0)); // row column

  }

}
