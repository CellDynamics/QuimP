package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.ImageStack;

// TODO: Auto-generated Javadoc
/**
 * ProbabilityMapsTest.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProbabilityMapsTest {

  /**
   * Test of {@link ProbabilityMaps#put(SeedTypes, RealMatrix)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testPut() throws Exception {
    ProbabilityMaps obj = new ProbabilityMaps();
    RealMatrix imp = new BlockRealMatrix(150, 150);
    RealMatrix imp1 = new BlockRealMatrix(151, 151);
    obj.put(SeedTypes.FOREGROUNDS, imp);
    obj.put(SeedTypes.FOREGROUNDS, imp1);

    List<RealMatrix> ret = obj.get(SeedTypes.FOREGROUNDS);
    assertThat(ret.size(), is(2));
    assertThat(ret.get(0), is(imp));
    assertThat(ret.get(1), is(imp1));
  }

  /**
   * Test of {@link ProbabilityMaps#convertTo3dMatrix(Object)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConvertTo3dMatrix() throws Exception {
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    double[][] m2d = { { 7, 8, 9 }, { 10, 11, 12 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);

    double[][][] ret = obj.convertTo3dMatrix(SeedTypes.BACKGROUND);
    assertThat(ret, is(nullValue()));

    ret = obj.convertTo3dMatrix(SeedTypes.FOREGROUNDS);
    assertThat(ret.length, is(2));
    assertThat(ret[0][0][2], is(3.0));
    assertThat(ret[1][0][2], is(9.0));
  }

  /**
   * Test of {@link ProbabilityMaps#convertTo3dMatrix(Object)}.
   * 
   * @throws Exception IllegalArgumentException
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertTo3dMatrix_1() throws Exception {
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    double[][] m2d = { { 7, 8, 9, 10 }, { 10, 11, 12, 10 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);

    obj.convertTo3dMatrix(SeedTypes.FOREGROUNDS); // throws
  }

  /**
   * Test of {@link ProbabilityMaps#convertToImageStack(Object)}.
   *
   * @throws Exception IllegalArgumentException
   */
  @Test
  public void testConvertToImageStack() throws Exception {
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    double[][] m2d = { { 7, 8, 9 }, { 10, 11, 12 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);

    ImageStack ret = obj.convertToImageStack(SeedTypes.BACKGROUND);
    assertThat(ret, is(nullValue()));

    ret = obj.convertToImageStack(SeedTypes.FOREGROUNDS);
    assertThat(ret.getBitDepth(), is(32));
    assertThat(ret.getSize(), is(2));
    assertThat(ret.getVoxel(1, 0, 0), is(2.0));
    assertThat(ret.getVoxel(2, 1, 1), is(12.0));
  }

  /**
   * Test of {@link ProbabilityMaps#convertToImageStack(Object)}.
   *
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertToImageStack_1() throws Exception {
    double[][] m1d = { { 1, 2, 3 }, { 4, 5, 6 } }; // [2][3]
    double[][] m2d = { { 7, 8, 9, 10 }, { 10, 11, 12, 10 } };

    RealMatrix m1 = new Array2DRowRealMatrix(m1d);
    RealMatrix m2 = new Array2DRowRealMatrix(m2d);

    ProbabilityMaps obj = new ProbabilityMaps();
    obj.put(SeedTypes.FOREGROUNDS, m1);
    obj.put(SeedTypes.FOREGROUNDS, m2);

    obj.convertToImageStack(SeedTypes.FOREGROUNDS); // throws
  }

}
