package uk.ac.warwick.wsbc.quimp.plugin.utils;
/**
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Point2d;

import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;

// TODO: Auto-generated Javadoc
/**
 * Test class for ROI saver
 * 
 * @author p.baniukiewicz
 *
 */
public class RoiSaver_test {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Test method for tools.images.filters.RoiSaver.saveROI(String, List<Vector2d>)
   * 
   * pre: Simple polygon
   * 
   * post: Image /tmp/test_SaveROI_case1.tif
   */
  @Test
  public void test_SaveROI_case1() {
    ArrayList<Point2d> testcase = new ArrayList<>();
    testcase.add(new Point2d(0, 0));
    testcase.add(new Point2d(50, 100));
    testcase.add(new Point2d(100, 0));
    RoiSaver.saveROI(tmpdir + "test_SaveROI_case1.tif", testcase);
  }

  /**
   * Test method for tools.images.filters.RoiSaver.saveROI(String,List<Vector2d>)
   * 
   * pre: Empty list
   * 
   * post: Uniform \b red Image /tmp/test_SaveROI_case2.tif
   */
  @Test
  public void test_SaveROI_case2() {
    ArrayList<Point2d> testcase = new ArrayList<>();
    RoiSaver.saveROI(tmpdir + "test_SaveROI_case2.tif", testcase);
  }

  /**
   * Test method for uk.ac.warwick.wsbc.tools.images.filters.RoiSaver.saveROI(String,
   * List<Vector2d>)
   * 
   * pre: null pointer
   * 
   * post: Uniform \b red Image /tmp/test_SaveROI_case3.tif
   */
  @Test
  public void test_SaveROI_case3() {
    RoiSaver.saveROI(tmpdir + "test_SaveROI_case3.tif", (List<Point2d>) null);
  }

}
