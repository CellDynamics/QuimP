package uk.ac.warwick.wsbc.quimp.geom.filters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;

/**
 * Parameterised test for HatFilter.
 * 
 * <p>Generates images of processed data as well as images of original data. Those can be viewed in
 * <EM>../src/test/resources/HatFilter.m</EM>
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Parameterized.class)
public class HatFilter_ParamTest {
  static final Logger LOGGER = LoggerFactory.getLogger(HatFilter_ParamTest.class.getName());
  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  private Integer window;
  private Integer pnum;
  private Double alev;
  private List<Point2d> testcase;
  private Path testfileName;

  /**
   * Parameterised constructor.
   * 
   * Each parameter should be placed as an argument here Every time runner triggers, it will pass
   * the arguments from parameters we defined to this method
   * 
   * @param testFileName test file name
   * @param window filter window size
   * @param pnum number of protrusions to find
   * @param alev acceptance level
   * @see DataLoader
   * @see HatSnakeFilter
   */
  public HatFilter_ParamTest(String testFileName, Integer window, Integer pnum, Double alev) {
    this.testfileName = Paths.get(testFileName);
    this.window = window;
    this.pnum = pnum;
    this.alev = alev;
  }

  /**
   * Called after construction but before tests
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    testcase = new DataLoader(testfileName.toString()).getData();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Set of parameters for tests.
   * 
   * @return List of strings with paths to testfiles and smooth parameter
   * @see <a href="creating *.dat files">QuimP-toolbox/Prototyping/59-Shape_filtering/main.m</a>
   */
  @Parameterized.Parameters
  public static Collection<Object[]> testFiles() {
    return Arrays.asList(new Object[][] { { "src/test/resources/testData_137.dat", 23, 1, 0.0 },
        { "src/test/resources/testData_1.dat", 23, 1, 0.0 },
        { "src/test/resources/testData_125.dat", 23, 1, 0.0 },
        { "src/test/resources/testData_75.dat", 23, 1, 0.0 },
        { "src/test/resources/testData_137.dat", 23, 2, 0.0 },
        { "src/test/resources/testData_1.dat", 23, 2, 0.0 },
        { "src/test/resources/testData_125.dat", 23, 2, 0.0 },
        { "src/test/resources/testData_75.dat", 23, 2, 0.0 } });
  }

  /**
   * Test of getInterpolationLoess method
   * 
   * Pre: Real cases extracted from
   * 
   * Post: Save image test_HatFilter_* in /tmp/
   * 
   * @throws QuimpPluginException
   * @see <a
   *      href="verification of logs (ratios, indexes, etc)">QuimP-toolbox/Prototyping/59-Shape_filtering/HatFilter.m</a>
   * @see <a href="resorces">/src/test/resources/HatFilter.m</a>
   * @see <a href="creating *.dat files">QuimP-toolbox/Prototyping/59-Shape_filtering/main.m</a>
   */
  @Test
  public void test_HatFilter() throws QuimpPluginException {
    List<Point2d> out;
    HatSnakeFilter hf = new HatSnakeFilter(window, pnum, alev);
    out = hf.runPlugin(testcase);
    RoiSaver.saveROI(tmpdir + "test_HatFilter_" + testfileName.getFileName() + "_"
            + window.toString() + "_" + pnum.toString() + "_" + alev.toString() + ".tif", out);
    LOGGER.debug("setUp: " + testcase.toString());
  }

  /**
   * Simple test of RoiSaver class, create reference images without processing but with the same
   * name scheme as processed data.
   * 
   * Post: Save image in /tmp
   * 
   * @see <a href="resorces">/src/test/resources/HatFilter.m</a>
   */
  @Test
  public void test_roiSaver() {
    RoiSaver.saveROI(tmpdir + "ref_HatFilter_" + testfileName.getFileName() + "_"
            + window.toString() + "_" + pnum.toString() + "_" + alev.toString() + ".tif", testcase);
  }
}
