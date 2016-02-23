
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
 * Test runner for Interpolate class using parameters. Test only
 * getInterpolationLoess method using its own parameters
 * 
 * Use src/test/resources/Interpolate_Test_Analyzer.m for plotting results
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Parameterized.class)
public class InterpolateLoess_Param_Test {

    private List<Vector2d> testcase;
    private Double smoothing;
    private Path testfileName;
    private static final Logger LOGGER = LogManager
            .getLogger(InterpolateLoess_Param_Test.class.getName());

    /**
     * Parameterized constructor.
     * 
     * Each parameter should be placed as an argument here Every time runner
     * triggers, it will pass the arguments from parameters we defined to this
     * method
     * 
     * @param testFileName test file name
     * @param smooth smoothing value
     * @see DataLoader
     */
    public InterpolateLoess_Param_Test(String testFileName, Double smooth) {
        this.testfileName = Paths.get(testFileName);
        this.smoothing = smooth;
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
                { "../src/test/resources/testData_75.dat", 0.015 },
                { "../src/test/resources/testData_75.dat", 0.04 },
                { "../src/test/resources/testData_75.dat", 0.06 },
                { "../src/test/resources/testData_75.dat", 0.08 },
                { "../src/test/resources/testData_75.dat", 0.1 },

                { "../src/test/resources/testData_125.dat", 0.015 },
                { "../src/test/resources/testData_125.dat", 0.04 },
                { "../src/test/resources/testData_125.dat", 0.06 },
                { "../src/test/resources/testData_125.dat", 0.08 },
                { "../src/test/resources/testData_125.dat", 0.1 },

                { "../src/test/resources/testData_137.dat", 0.015 },
                { "../src/test/resources/testData_137.dat", 0.04 },
                { "../src/test/resources/testData_137.dat", 0.06 },
                { "../src/test/resources/testData_137.dat", 0.08 },
                { "../src/test/resources/testData_137.dat", 0.1 },

                { "../src/test/resources/testData_1.dat", 0.015 },
                { "../src/test/resources/testData_1.dat", 0.04 },
                { "../src/test/resources/testData_1.dat", 0.06 },
                { "../src/test/resources/testData_1.dat", 0.08 },
                { "../src/test/resources/testData_1.dat", 0.1 }, });
    }

    /**
     * @throws QuimpPluginException
     * @test Test of getInterpolationLoess method
     * @post Save image test_getInterpolationLoess_* in /tmp/
     * @see QuimP-toolbox/algorithms/src/test/resources/
     * Interpolate_Test_Analyzer.m for plotting results
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating
     * *.dat files
     */
    @SuppressWarnings("serial")
    @Test
    public void test_getInterpolationLoess() throws QuimpPluginException {
        ArrayList<Vector2d> out;
        LoessSnakeFilter_ i = new LoessSnakeFilter_();
        i.attachData(testcase);
        i.setPluginConfig(new HashMap<String, String>() {
            {
                put("smooth", String.valueOf(smoothing));
            }
        });
        out = (ArrayList<Vector2d>) i.runPlugin();
        RoiSaver.saveROI(
                "/tmp/test_getInterpolationLoess_" + testfileName.getFileName()
                        + "_" + smoothing.toString() + ".tif",
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
    public void test_roiSaver() {
        RoiSaver.saveROI("/tmp/test_roiSaver_" + testfileName.getFileName()
                + "_" + smoothing.toString() + ".tif",
                testcase);
    }

}