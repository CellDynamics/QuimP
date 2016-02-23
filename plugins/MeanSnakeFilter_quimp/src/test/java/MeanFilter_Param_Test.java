
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * Test runner for Interpolate class using parameters. Test only
 * getInterpolationMean method using its own parameters
 * 
 * Use src/test/resources/Interpolate_Test_Analyzer.m for plotting results
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Parameterized.class)
public class MeanFilter_Param_Test {
    private List<Vector2d> testcase;
    private Integer window;
    private Path testfileName;
    private static final Logger LOGGER = LogManager
            .getLogger(MeanFilter_Param_Test.class.getName());

    /**
     * Parameterized constructor.
     * 
     * Each parameter should be placed as an argument here Every time runner
     * triggers, it will pass the arguments from parameters we defined to this
     * method
     * 
     * @param testFileName test file name
     * @param window averaging window size
     * @see DataLoader
     */
    public MeanFilter_Param_Test(String testFileName, Integer window) {
        this.testfileName = Paths.get(testFileName);
        this.window = window;
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
        return Arrays.asList(
                new Object[][] { { "../src/test/resources/testData_75.dat", 1 },
                        { "../src/test/resources/testData_75.dat", 3 },
                        { "../src/test/resources/testData_75.dat", 5 },
                        { "../src/test/resources/testData_75.dat", 9 },
                        { "../src/test/resources/testData_75.dat", 15 },

                        { "../src/test/resources/testData_125.dat", 1 },
                        { "../src/test/resources/testData_125.dat", 3 },
                        { "../src/test/resources/testData_125.dat", 5 },
                        { "../src/test/resources/testData_125.dat", 9 },
                        { "../src/test/resources/testData_125.dat", 15 },

                        { "../src/test/resources/testData_137.dat", 1 },
                        { "../src/test/resources/testData_137.dat", 3 },
                        { "../src/test/resources/testData_137.dat", 5 },
                        { "../src/test/resources/testData_137.dat", 9 },
                        { "../src/test/resources/testData_137.dat", 15 },

                        { "../src/test/resources/testData_1.dat", 1 },
                        { "../src/test/resources/testData_1.dat", 3 },
                        { "../src/test/resources/testData_1.dat", 5 },
                        { "../src/test/resources/testData_1.dat", 9 },
                        { "../src/test/resources/testData_1.dat", 15 }, });
    }

    /**
     * @throws QuimpPluginException
     * @test Test of getInterpolationMean method
     * @pre original images saved as test_roiSaver_
     * @post Save image test_getInterpolationMean_* in /tmp/
     * @see QuimP-toolbox/algorithms/src/test/resources/
     * Interpolate_Test_Analyzer.m for plotting results
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating
     * *.dat files
     */
    @SuppressWarnings("serial")
    @Test
    public void test_getInterpolationMean() throws QuimpPluginException {
        ArrayList<Vector2d> out;
        MeanSnakeFilter i = new MeanSnakeFilter();
        i.attachData(testcase);
        i.setPluginConfig(new HashMap<String, String>() {
            {
                put("window", String.valueOf(window));
            }
        });
        out = (ArrayList<Vector2d>) i.runPlugin();
        RoiSaver.saveROI(
                "/tmp/test_getInterpolationMean_" + testfileName.getFileName()
                        + "_" + window.toString() + ".tif",
                out);
        LOGGER.debug("setUp: " + testcase.toString());
        if (out.size() < 100)
            LOGGER.debug("testInterpolate: " + out.toString());
    }

    /**
     * @test Simple test of RoiSaver class, create reference images without
     * processing
     * @post Save image /tmp/testroiSaver_*.tif
     */
    @Test
    @Ignore
    public void test_roiSaver() {
        RoiSaver.saveROI("/tmp/test_roiSaver_" + testfileName.getFileName()
                + "_" + window.toString() + ".tif",
                testcase);
    }

}
