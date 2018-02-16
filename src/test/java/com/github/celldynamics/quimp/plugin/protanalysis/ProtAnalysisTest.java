package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapTracker;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.ImageJ;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * Tests and example of high level API.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisTest {

  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ProtAnalysisTest.class.getName());

  /**
   * The q L 1.
   */
  static QconfLoader qL1;
  
  /** The st map. */
  private STmap[] stMap;
  
  /** The imp. */
  private ImageProcessor imp;

  /**
   * Load qconf.
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
   * Prepare images.
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

    WindowManager.closeAllWindows();
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
   * Example of high level call.
   * 
   * @throws IOException on error
   */
  @Test
  public void testApi() throws IOException {
    new ImageJ();

    Path target = Paths.get(temp.getRoot().getPath(), "fluoreszenz-test.QCONF");
    FileUtils.copyFile(
            new File("src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF"),
            target.toFile());
    LOGGER.info(temp.getRoot().getPath().toString());
    Prot_Analysis obj = new Prot_Analysis();
    //!>
    obj.run("{"
            + "noiseTolerance:1.5,"
            + "dropValue:1.0,"
            + "plotMotmap:true,"
            + "plotMotmapmax:true,"
            + "plotConmap:true,"
            + "plotOutline:true,"
            + "plotStaticmax:true,"
            + "plotDynamicmax:true,"
            + "outlinesToImage:{motColor:{value:-16776961,falpha:0.0},"
            + "convColor:{value:-65536,falpha:0.0}," + "defColor:{value:-1,falpha:0.0},"
            + "motThreshold:0.0,"
            + "convThreshold:0.0,"
            + "plotType:CONCANDRETR},"
            + "staticPlot:{"
            + "plotmax:true,"
            + "plottrack:true,"
            + "averimage:true},"
            + "dynamicPlot:{"
            + "plotmax:true,"
            + "plottrack:true},"
            + "polarPlot:{useGradient:true,"
            + "plotpolar:true,type:SCREENPOINT,"
            + "gradientPoint:{x:0.0,y:0.0},"
            + "gradientOutline:0},"
            + "paramFile:(" + target.toString() + ")}"
            );
    //!<
    assertThat(Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_0_cellstat.csv").toFile()
            .exists(), is(true));
    assertThat(Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_1_cellstat.csv").toFile()
            .exists(), is(true));
    assertThat(Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_0_protstat.csv").toFile()
            .exists(), is(true));
    assertThat(Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_1_protstat.csv").toFile()
            .exists(), is(true));
    assertThat(
            Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_0_polar.svg").toFile().exists(),
            is(true));
    assertThat(
            Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_1_polar.svg").toFile().exists(),
            is(true));

    assertThat(WindowManager.getImageCount(), is(9));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles, hasItemInArray("Outlines"));
    assertThat(titles, hasItemInArray("ConvexityMap_cell_1"));
    assertThat(titles, hasItemInArray("ConvexityMap_cell_0"));
    assertThat(titles, hasItemInArray("MotilityMap_cell_1"));
    assertThat(titles, hasItemInArray("MotilityMap_cell_0"));
    assertThat(titles, hasItemInArray("Static points"));
    assertThat(titles, hasItemInArray("Dynamic tracking"));

    assertThat(titles, hasItemInArray("motility_map_cell_0"));
    assertThat(titles, hasItemInArray("motility_map_cell_1"));
  }

}
