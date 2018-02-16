package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.awt.Polygon;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * Test class for {@link com.github.celldynamics.quimp.plugin.protanalysis.MaximaFinder}.
 * 
 * @author p.baniukiewicz
 *
 */
public class MaximaFinderTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(MaximaFinderTest.class.getName());

  /**
   * The q L 1.
   */
  static QconfLoader qL1;

  /** The st map. */
  private STmap[] stMap;

  /** The imp. */
  private ImageProcessor imp;

  /**
   * Sets the up before class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    qL1 = new QconfLoader(
            Paths.get("src/test/Resources-static/TrackMapTests/fluoreszenz-test_eq_smooth.QCONF")
                    .toFile());
  }

  /**
   * Tear down after class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    stMap = ((QParamsQconf) qL1.getQp()).getLoadedDataContainer().QState;
    float[][] motMap = QuimPArrayUtils.double2dfloat(stMap[0].getMotMap());
    // rotate and flip to match orientation of ColorProcessor (QuimP default)
    imp = new FloatProcessor(motMap).rotateRight();
    imp.flipHorizontal();
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
   * {@link MaximaFinder#MaximaFinder(ij.process.ImageProcessor)}.
   * 
   * <p>Results compared with those generated in IJ from
   * src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test_eq_smooth_0_motilityMap.maQP when
   * imported as text image.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testMaximaFinder() throws Exception {
    int[] expectedX = { 160, 110, 134, 266, 40, 359, 79, 236, 288, 273, 212, 127, 73, 8, 331, 70,
        270, 147, 368, 13 };
    int[] expectedY =
            { 20, 89, 129, 62, 63, 97, 50, 77, 31, 126, 80, 132, 57, 42, 58, 15, 102, 31, 103, 40 };
    // these values have been read from matlab using above points
    double[] expectedVal = { 21.00, 15.15, 15.06, 15.05, 14.58, 14.52, 14.48, 14.34, 14.14, 13.28,
        13.03, 12.44, 11.83, 11.75, 11.55, 11.08, 11.07, 10.90, 10.64, 10.43 };
    MaximaFinder mf = new MaximaFinder(imp);
    mf.computeMaximaIJ(10);
    Polygon ret = mf.getMaxima();
    double[] val = mf.getMaxValues();
    LOGGER.debug(Arrays.toString(val));
    assertThat(ret.xpoints, is(expectedX));
    assertThat(ret.ypoints, is(expectedY));
    assertArrayEquals(expectedVal, val, 1e-2);
  }

}
