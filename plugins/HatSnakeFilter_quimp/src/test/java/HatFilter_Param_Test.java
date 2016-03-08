
/**
 * @file HatFilter_Param_Test.java
 * @date 25 Jan 2016
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * Parameterized test for HatFilter
 * 
 * Generates images of processed data as well as images of original data. Those can be viewed in 
 * <EM>../src/test/resources/HatFilter.m</EM>
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
@RunWith(Parameterized.class)
public class HatFilter_Param_Test {
    private static final Logger LOGGER = LogManager.getLogger(HatFilter_Param_Test.class.getName());
    private Integer window;
    private Integer pnum;
    private Double alev;
    private List<Point2d> testcase;
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
     * @param pnum number of protrusions to find
     * @param alev acceptance level
     * @see DataLoader
     * @see HatSnakeFilter_
     */
    public HatFilter_Param_Test(String testFileName, Integer window, Integer pnum, Double alev) {
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
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating
     * *.dat files
     */
    @Parameterized.Parameters
    public static Collection<Object[]> testFiles() {
        return Arrays
                .asList(new Object[][] { { "../src/test/resources/testData_137.dat", 23, 1, 0.0 },
                        { "../src/test/resources/testData_1.dat", 23, 1, 0.0 },
                        { "../src/test/resources/testData_125.dat", 23, 1, 0.0 },
                        { "../src/test/resources/testData_75.dat", 23, 1, 0.0 },
                        { "../src/test/resources/testData_137.dat", 23, 2, 0.0 },
                        { "../src/test/resources/testData_1.dat", 23, 2, 0.0 },
                        { "../src/test/resources/testData_125.dat", 23, 2, 0.0 },
                        { "../src/test/resources/testData_75.dat", 23, 2, 0.0 } });
    }

    /**
     * @test Test of getInterpolationLoess method
     * @pre Real cases extracted from
     * @post Save image test_HatFilter_* in /tmp/
     * @throws QuimpPluginException
     * @see QuimP-toolbox/algorithms/src/test/resources/HatFilter.m for verification of logs
     * (ratios, indexes, etc)
     * @see <EM>../src/test/resources/HatFilter.m</EM> for plotting results
     * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating *.dat files
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter() throws QuimpPluginException {
        List<Point2d> out;
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(testcase);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", String.valueOf(window));
                put("pnum", String.valueOf(pnum));
                put("alev", String.valueOf(alev));
            }
        });
        out = hf.runPlugin();
        RoiSaver.saveROI("/tmp/test_HatFilter_" + testfileName.getFileName() + "_"
                + window.toString() + "_" + pnum.toString() + "_" + alev.toString() + ".tif", out);
        LOGGER.debug("setUp: " + testcase.toString());
    }

    /**
     * @test Simple test of RoiSaver class, create reference images without processing but with the
     * same name scheme as processed data
     * @post Save image in /tmp
     * @see <EM>../src/test/resources/HatFilter.m</EM> for plotting results
     */
    @Test
    public void test_roiSaver() {
        RoiSaver.saveROI("/tmp/ref_HatFilter_" + testfileName.getFileName() + "_"
                + window.toString() + "_" + pnum.toString() + "_" + alev.toString() + ".tif",
                testcase);
    }
}
