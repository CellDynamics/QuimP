package uk.ac.warwick.wsbc.QuimP.plugin.utils;
/**
 */

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.scijava.vecmath.Point2d;

/**
 * Test class for ROI saver
 * 
 * @author p.baniukiewicz
 *
 */
public class RoiSaver_test {

    /**
     * @test Test method for tools.images.filters.RoiSaver.saveROI(String, List<Vector2d>)
     * @pre Simple polygon
     * @post Image /tmp/test_SaveROI_case1.tif
     */
    @Test
    public void test_SaveROI_case1() {
        ArrayList<Point2d> testcase = new ArrayList<>();
        testcase.add(new Point2d(0, 0));
        testcase.add(new Point2d(50, 100));
        testcase.add(new Point2d(100, 0));
        RoiSaver.saveROI("/tmp/test_SaveROI_case1.tif", testcase);
    }

    /**
     * @test Test method for tools.images.filters.RoiSaver.saveROI(String,List<Vector2d>)
     * @pre Empty list
     * @post Uniform \b red Image /tmp/test_SaveROI_case2.tif
     */
    @Test
    public void test_SaveROI_case2() {
        ArrayList<Point2d> testcase = new ArrayList<>();
        RoiSaver.saveROI("/tmp/test_SaveROI_case2.tif", testcase);
    }

    /**
     * @test Test method for uk.ac.warwick.wsbc.tools.images.filters.RoiSaver.saveROI(String,
     *       List<Vector2d>)
     * @pre null pointer
     * @post Uniform \b red Image /tmp/test_SaveROI_case3.tif
     */
    @Test
    public void test_SaveROI_case3() {
        RoiSaver.saveROI("/tmp/test_SaveROI_case3.tif", (List<Point2d>) null);
    }

}
