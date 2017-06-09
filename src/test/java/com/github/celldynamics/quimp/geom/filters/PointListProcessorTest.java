package com.github.celldynamics.quimp.geom.filters;

import static com.github.baniuk.ImageJTestSuite.matchers.arrays.ArrayMatchers.arrayCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

/**
 * @author p.baniukiewicz
 *
 */
public class PointListProcessorTest {

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
   * Test method for
   * {@link PointListProcessor#runningMean(double[], int)}.
   * 
   * <pre>
   * <code>
   * x=1:10
   * xx=padarray(x',1,'circular')'
   * ret=movmean(xx,3)
   * ret(2:end-1)
   * </code>
   * </pre>
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRunningMean() throws Exception {
    double[] data = new double[10];
    IntStream.range(0, 10).forEach(i -> data[i] = i + 1);
    PointListProcessor.runningMean(data, 3);
    double[] expected =
            { 4.3333, 2.0000, 3.0000, 4.0000, 5.0000, 6.0000, 7.0000, 8.0000, 9.0000, 6.6667 };
    assertThat(ArrayUtils.toObject(data), arrayCloseTo(expected, 1e-4));

    // table size == window
    double[] data1 = new double[3];
    IntStream.range(0, 3).forEach(i -> data1[i] = i + 1);
    PointListProcessor.runningMean(data1, 3);
    double[] expected1 = { 2, 2, 2 };
    assertThat(ArrayUtils.toObject(data1), arrayCloseTo(expected1, 1e-4));

    // table size < window
    double[] data2 = new double[1];
    IntStream.range(0, 1).forEach(i -> data2[i] = i + 1);
    PointListProcessor.runningMean(data2, 3);
    double[] expected2 = { 1 };
    assertThat(ArrayUtils.toObject(data2), arrayCloseTo(expected2, 1e-4));

    // empty input
    double[] data3 = new double[0];
    PointListProcessor.runningMean(data3, 3);
    assertThat(data3.length, is(0));
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.geom.filters.PointListProcessor#smooth(int, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSmooth() throws Exception {
    List<Point2d> test = new ArrayList<>();
    IntStream.range(0, 10).forEach(i -> test.add(new Point2d(i + 1, 10 - i)));
    double[] expectedx =
            { 4.3333, 2.0000, 3.0000, 4.0000, 5.0000, 6.0000, 7.0000, 8.0000, 9.0000, 6.6667 };
    double[] expectedy = new double[expectedx.length];
    System.arraycopy(expectedx, 0, expectedy, 0, expectedx.length);
    ArrayUtils.reverse(expectedy);

    List<Point2d> ret = new PointListProcessor(test).smooth(3, 1).getList();

    assertThat(ArrayUtils.toObject(new QuimpDataConverter(ret).getX()),
            arrayCloseTo(expectedx, 1e-4));
    assertThat(ArrayUtils.toObject(new QuimpDataConverter(ret).getY()),
            arrayCloseTo(expectedy, 1e-4));

  }
}