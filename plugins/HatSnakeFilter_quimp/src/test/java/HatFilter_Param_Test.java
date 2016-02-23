
/**
 * @file HatFilter_Param_Test.java
 * @date 25 Jan 2016
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * Parameterized test for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
@RunWith(Parameterized.class)
public class HatFilter_Param_Test {
    private static final Logger LOGGER = LogManager
            .getLogger(HatFilter_Param_Test.class.getName());
    private Integer window;
    private Integer crown;
    private Double sig;
    private List<Vector2d> testcase;
    private Path testfileName;

    /**
     * Parameterized constructor.
     * 
     * Each parameter should be placed as an argument here Every time runner
     * triggers, it will pass the arguments from parameters we defined to this
     * method
     * 
     * @param testFileName test file name
     * @param window filter window size
     * @param crown filter crown size
     * @param sig
     * sigma value
     * @see DataLoader
     * @see HatSnakeFilter_
     */
    public HatFilter_Param_Test(String testFileName, Integer window,
            Integer crown, Double sig) {
        this.testfileName = Paths.get(testFileName);
        this.window = window;
        this.crown = crown;
        this.sig = sig;
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
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating
     * *.dat files
     */
    @Parameterized.Parameters
    public static Collection<Object[]> testFiles() {
        return Arrays.asList(new Object[][] {
                { "../src/test/resources/testData_137.dat", 23, 13, 0.3 },
                { "../src/test/resources/testData_1.dat", 23, 13, 0.3 },
                { "../src/test/resources/testData_125.dat", 23, 13, 0.3 },
                { "../src/test/resources/testData_75.dat", 23, 13, 0.3 } });
    }

    /**
     * @test Test of getInterpolationLoess method
     * @pre Real cases extrcted from
     * @post Save image test_HatFilter_* in /tmp/
     * @throws QuimpPluginException
     * @see QuimP-toolbox/algorithms/src/test/resources/HatFilter.m for
     * verification of logs (ratios, indexes, etc)
     * @see QuimP-toolbox/algorithms/src/test/resources/
     * Interpolate_Test_Analyzer.m for plotting results
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating
     * *.dat files
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter() throws QuimpPluginException {
        ArrayList<Vector2d> out;
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(testcase);
        hf.setPluginConfig(new HashMap<String, String>() {
            {
                put("window", String.valueOf(window));
                put("crown", String.valueOf(crown));
                put("sigma", String.valueOf(sig));
            }
        });
        out = (ArrayList<Vector2d>) hf.runPlugin();
        RoiSaver.saveROI("/tmp/test_HatFilter_" + testfileName.getFileName()
                + "_" + window.toString() + "_"
                + crown.toString() + "_" + sig.toString() + ".tif", out);
        LOGGER.debug("setUp: " + testcase.toString());
    }

    /**
     * @test Simple test of RoiSaver class, create reference images without
     * processing but with the same name scheme
     * @post Save image /tmp/testroiSaver_*.tif
     */
    @Test
    public void test_roiSaver() {
        RoiSaver.saveROI("/tmp/ref_HatFilter_" + testfileName.getFileName()
                + "_" + window.toString() + "_"
                + crown.toString() + "_" + sig.toString() + ".tif", testcase);
    }
}
