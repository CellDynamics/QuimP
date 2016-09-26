package uk.ac.warwick.wsbc.QuimP.plugin.utils;
/**
 */

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

/**
 * Test class for ROI saver
 * 
 * @author p.baniukiewicz
 *
 */
public class RoiSaver_test {

    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }

    /**
     * @test Test method for
     * tools.images.filters.RoiSaver.saveROI(String, List<Vector2d>)
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
     * @test Test method for
     * tools.images.filters.RoiSaver.saveROI(String,List<Vector2d>)
     * @pre Empty list
     * @post Uniform \b red Image /tmp/test_SaveROI_case2.tif
     */
    @Test
    public void test_SaveROI_case2() {
        ArrayList<Point2d> testcase = new ArrayList<>();
        RoiSaver.saveROI("/tmp/test_SaveROI_case2.tif", testcase);
    }

    /**
     * @test Test method for
     * uk.ac.warwick.wsbc.tools.images.filters.RoiSaver.saveROI(String,
     * List<Vector2d>)
     * @pre null pointer
     * @post Uniform \b red Image /tmp/test_SaveROI_case3.tif
     */
    @Test
    public void test_SaveROI_case3() {
        RoiSaver.saveROI("/tmp/test_SaveROI_case3.tif", (List<Point2d>) null);
    }

}
