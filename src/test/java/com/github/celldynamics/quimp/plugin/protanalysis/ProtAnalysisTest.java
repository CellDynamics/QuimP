package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapTracker;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ProtAnalysisTest.class.getName());

  /**
   * The q L 1.
   */
  static QconfLoader qL1;
  private STmap[] stMap;
  private ImageProcessor imp;

  /**
   * @throws java.lang.Exception Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    qL1 = new QconfLoader(
            Paths.get("src/test/Resources-static/TrackMapTests/fluoreszenz-test_eq_smooth.QCONF")
                    .toFile());
  }

  /**
   * @throws java.lang.Exception Exception
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
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Check tracking for found maxima.
   * 
   * <p>See: src/test/Resources-static/ProtAnalysisTest/main.m
   * 
   * @throws Exception Exception
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testTracking() throws Exception {
    int[] expectedI = { 167 - 1, 150 - 1, 139 - 1, 147 - 1, 138 - 1, 136 - 1, 134 - 1, 135 - 1,
        141 - 1, 139 - 1 };
    int[] expectedF =
            { 22 - 1, 23 - 1, 24 - 1, 25 - 1, 26 - 1, 27 - 1, 28 - 1, 29 - 1, 30 - 1, 31 - 1 };
    MapTracker testM = new MapTracker(stMap[0].getOriginMap(), stMap[0].getCoordMap());
    int frame = 20; // frame from found maxima [0]
    int index = 160; // index from found maxima [0]
    int[] ret = testM.trackForward(frame, index, 10);
    int[] retF = testM.getForwardFrames(frame, 10);
    assertThat(ret, is(expectedI));
    assertThat(retF, is(expectedF));
  }

  /**
   * Test common point.
   */
  @Test
  public void testCommonPoint() {

  }

}
