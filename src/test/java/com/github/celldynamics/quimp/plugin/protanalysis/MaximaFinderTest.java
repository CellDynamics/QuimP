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

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

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
    int[] expectedX = { 8, 13, 40, 70, 73, 79, 110, 127, 134, 147, 160, 212, 236, 266, 270, 273,
        288, 331, 359, 368 };
    int[] expectedY =
            { 42, 40, 63, 15, 57, 50, 89, 132, 129, 31, 20, 80, 77, 62, 102, 126, 31, 58, 97, 103 };
    // these values have been read from matlab using above points
    double[] expectedVal = { 11.75, 10.43, 14.58, 11.08, 11.83, 14.48, 15.15, 12.44, 15.06, 10.90,
        21.00, 13.03, 14.34, 15.05, 11.07, 13.28, 14.14, 11.55, 14.52, 10.64 };
    MaximaFinder mf = new MaximaFinder(imp);
    new ImagePlus("", imp).show();
    mf.computeMaximaIJ(10);
    Polygon ret = mf.getMaxima();
    double[] val = mf.getMaxValues();
    LOGGER.debug(Arrays.toString(val));
    assertThat(ret.npoints, is(20));
    for (int i = 0; i < 20; i++) {
      assertThat(ret.xpoints[i], is(expectedX[i]));
      assertThat(ret.ypoints[i], is(expectedY[i]));
    }
    assertArrayEquals(expectedVal, val, 1e-2);
  }

}
