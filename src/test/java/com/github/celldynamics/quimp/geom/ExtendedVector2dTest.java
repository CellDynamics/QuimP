package com.github.celldynamics.quimp.geom;

import static com.github.baniuk.ImageJTestSuite.matchers.arrays.ArrayMatchers.arrayCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Simple testing class for ExtendedVector2d class.
 * 
 * @author p.baniukiewicz
 *
 */
public class ExtendedVector2dTest {

  /**
   * The Constant LOGGER.
   */
  // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
  static final Logger LOGGER = LoggerFactory.getLogger(ExtendedVector2dTest.class.getName());

  /**
   * test toString() method.
   * 
   * <p>post: Content of List in log file
   */
  @Test
  public void test_ToString() {
    ArrayList<ExtendedVector2d> v = new ArrayList<ExtendedVector2d>();
    ExtendedVector2d vv = new ExtendedVector2d(3.14, -5.6);
    v.add(new ExtendedVector2d(0, 0));
    v.add(new ExtendedVector2d(10, 10));
    v.add(new ExtendedVector2d(3.14, -4.56));
    LOGGER.debug("vector " + vv.toString());
    LOGGER.debug("V1 vector: " + v.toString());
  }

  /**
   * casting of {@link ExtendedVector2d} to javax.vecmath.Vector2d
   */
  @Test
  public void test_Casting() {
    Vector2d v = new ExtendedVector2d(10, 10);
    LOGGER.debug("Casting: " + v.toString());

    Vector2d v1 = new Vector2d(5, 5);
    ExtendedVector2d ev1 = new ExtendedVector2d(v1);
    LOGGER.debug("Casting1: " + ev1.toString());
  }

  /**
   * Test method for {@link ExtendedVector2d#linspace(double, double, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testLinspace() throws Exception {
    {
      List<Double> ret = ExtendedVector2d.linspace(0, 10, 10);
      double[] exp = new double[] { 0, 1.1111, 2.2222, 3.3333, 4.4444, 5.5556, 6.6667, 7.7778,
          8.8889, 10.0000 };
      assertThat(ret.toArray(new Double[0]), is(arrayCloseTo(exp, 0.01)));
    }
    {
      List<Double> ret = ExtendedVector2d.linspace(10, 10, 3);
      double[] exp = new double[] { 10, 10, 10 };
      assertThat(ret.toArray(new Double[0]), is(arrayCloseTo(exp, 0.01)));
    }
    {
      List<Double> ret = ExtendedVector2d.linspace(10, 9, 3);
      double[] exp = new double[] { 10, 9.5, 9 };
      assertThat(ret.toArray(new Double[0]), is(arrayCloseTo(exp, 0.01)));
    }
  }
}
