package uk.ac.warwick.wsbc.quimp.geom.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.geom.filters.HatSnakeFilter.WindowIndRange;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;

/**
 * Test class for HatFilter.
 * 
 * @author p.baniukiewicz
 *
 */
public class HatSnakeFilterTest {

  static final Logger LOGGER = LoggerFactory.getLogger(HatSnakeFilterTest.class.getName());
  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  private List<Point2d> input;
  private List<Point2d> lininput; // line at 45 deg
  private List<Point2d> circ; // circular object <EM>../src/test/resources/HatFilter.m</EM>
  /**
   * simulated protrusions
   * 
   * % protrusions - generate test data from ../src/test/resources/HatFilter.m
   */
  private List<Point2d> prot;

  /**
   * 
   */
  @Rule
  public TestName name = new TestName(); // Allow to get tested method name (called at setUp())

  /**
   * Load all data
   * 
   * @throws Exception
   * @see <a href="../src/test/resources/HatFilter.m">../src/test/resources/HatFilter.m</a>
   */
  @Before
  public void setUp() throws Exception {
    input = new ArrayList<>();
    for (int i = 0; i < 40; i++)
      input.add(new Point2d(i, 0));
    input.set(18, new Point2d(18, 1));
    input.set(19, new Point2d(19, 1));
    input.set(20, new Point2d(20, 1));
    LOGGER.info("Entering " + name.getMethodName());

    lininput = new ArrayList<>();
    for (int i = 0; i < 20; i++)
      lininput.add(new Point2d(i, i));

    circ = new DataLoader("src/test/resources/testData_circle.dat").getData();
    prot = new DataLoader("src/test/resources/testData_prot.dat").getData();
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test of HatSnakeFilter_.runPlugin()
   * 
   * Pre: Ideally circular object
   * 
   * Post: In logs: 1) Weighting the same, 2)circularity the same
   * 
   * @throws QuimpPluginException
   */
  @SuppressWarnings("serial")
  @Test
  public void test_HatFilter_run() throws QuimpPluginException {
    LOGGER.debug("input: " + circ.toString());
    HatSnakeFilter hf = new HatSnakeFilter(5, 1, 0);
    hf.runPlugin(circ);
  }

  /**
   * Test of HatSnakeFilter_.runPlugin().
   * 
   * Pre: Simulated protrusions post Logs are comparable with script
   * ../src/test/resources/HatFilter.m After run go to folder mentioned above and run %%
   * protrusions - load java results and compare with matlab to verify results
   * 
   * This matlab code is not fully compatible with java. Some results differ Matlab dont accept
   * windows lying on beginning because they have indexes 1-max.
   * 
   * @throws QuimpPluginException
   */
  @SuppressWarnings("serial")
  @Test
  public void test_HatFilter_run_2() throws QuimpPluginException {
    LOGGER.debug("input: " + prot.toString());
    HatSnakeFilter hf = new HatSnakeFilter(9, 3, 0);
    List<Point2d> out = hf.runPlugin(prot);
    RoiSaver.saveROI(tmpdir + "test_HatFilter_run_2.tif", out);
  }

  /**
   * Test of HatSnakeFilter_.runPlugin()
   * 
   * Pre: Linear object
   * 
   * Post: In logs: 1) Weighting differ at end, 2) circularity differ at end -# Window is moving
   * and has circular padding
   * 
   * @throws QuimpPluginException
   */
  @SuppressWarnings("serial")
  @Test
  public void test_HatFilter_run_1() throws QuimpPluginException {
    LOGGER.debug("input: " + lininput.toString());
    HatSnakeFilter hf = new HatSnakeFilter(5, 1, 0);
    hf.runPlugin(lininput);
  }

  /**
   * Input condition for HatFilter
   * 
   * Pre: Various bad combinations of inputs
   * 
   * Post: Exception FilterException
   */
  @SuppressWarnings("serial")
  @Test
  public void test_HatFilter_case3() {
    try {
      HatSnakeFilter hf = new HatSnakeFilter(6, 3, 1); // even window
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(-5, 3, 1); // neg window
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(600, 3, 1); // too long win
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(1, 3, 1); // to small window
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(5, 0, 1); // bad protrusions
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(5, 3, -1); // bad acceptance
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
    try {
      HatSnakeFilter hf = new HatSnakeFilter(6, -4, 1); // bad crown
      hf.runPlugin(input);
      fail("Exception not thrown");
    } catch (QuimpPluginException e) {
      assertTrue(e != null);
      LOGGER.debug(e.getMessage());
    }
  }

  /**
   * Test of WindowIndRange class
   * 
   * Pre: Separated ranges of indexes
   * 
   * Post: All ranges are added to list
   */
  @Test
  public void testWindowIndRange_1() {
    TreeSet<WindowIndRange> p = new TreeSet<>();
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(1, 5)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(6, 10)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(-5, 0)));
    LOGGER.debug(p.toString());
  }

  /**
   * Test of WindowIndRange class
   * 
   * Pre: Overlap ranges of indexes
   * 
   * Post: Overlap ranges are not added to list
   */
  @Test
  public void testWindowIndRange_2() {
    TreeSet<WindowIndRange> p = new TreeSet<>();
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(1, 5)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(7, 10)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(-5, 0)));

    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(7, 8)));
    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(10, 12)));
    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(9, 12)));
    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(4, 7)));
    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(4, 6)));
    assertFalse(p.add(new HatSnakeFilter().new WindowIndRange(-5, 0)));
    LOGGER.debug(p.toString());
  }

  /**
   * Test of WindowIndRange class Test if particular point is included in any range stored in
   * TreeSet
   */
  @Test
  public void testWindowIndRange_3() {
    TreeSet<WindowIndRange> p = new TreeSet<>();
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(1, 5)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(6, 10)));
    assertTrue(p.add(new HatSnakeFilter().new WindowIndRange(-5, 0)));

    assertTrue(p.contains(new HatSnakeFilter().new WindowIndRange(2, 2)));
    assertTrue(p.contains(new HatSnakeFilter().new WindowIndRange(6, 6)));
    assertFalse(p.contains(new HatSnakeFilter().new WindowIndRange(11, 11)));

    LOGGER.debug(p.toString());
  }

}
