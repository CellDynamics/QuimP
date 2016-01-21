package uk.ac.warwick.wsbc.tools.images;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

/**
 * Test runner for Interpolate class using parameters. Test only getInterpolationMean method using its own parameters
 * 
 * Use src/test/resources/Interpolate_Test_Analyzer.m for plotting results
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Parameterized.class)
public class InterpolateMean_testParam {
	private List<Vector2d> testcase;
	private Integer window;
	private Path testfileName;
	private static final Logger logger = LogManager.getLogger(InterpolateMean_testParam.class.getName());

	/**
	 * Parameterized constructor.
	 * 
	 * Each parameter should be placed as an argument here
     * Every time runner triggers, it will pass the arguments
     * from parameters we defined in primeNumbers() method
     * 
	 * @param testFileName test file name
	 * @param window averaging window size
	 * @see DataLoader
	 */
	public InterpolateMean_testParam(String testFileName, Integer window) {
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
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> testFiles() {
		return Arrays.asList(new Object[][] {
			{"src/test/resources/testData_75.dat",1},
			{"src/test/resources/testData_75.dat",3},
			{"src/test/resources/testData_75.dat",5},
			{"src/test/resources/testData_75.dat",9},
			{"src/test/resources/testData_75.dat",15},
			
			{"src/test/resources/testData_125.dat",1},
			{"src/test/resources/testData_125.dat",3},
			{"src/test/resources/testData_125.dat",5},
			{"src/test/resources/testData_125.dat",9},
			{"src/test/resources/testData_125.dat",15},
			
			{"src/test/resources/testData_137.dat",1},
			{"src/test/resources/testData_137.dat",3},
			{"src/test/resources/testData_137.dat",5},
			{"src/test/resources/testData_137.dat",9},
			{"src/test/resources/testData_137.dat",15},
			
			{"src/test/resources/testData_1.dat",1},
			{"src/test/resources/testData_1.dat",3},
			{"src/test/resources/testData_1.dat",5},
			{"src/test/resources/testData_1.dat",9},
			{"src/test/resources/testData_1.dat",15},
		});
	}
	
	/**
	 * @throws InterpolateException 
	 * @test Test of getInterpolationMean method
	 * @pre original images saved as test_roiSaver_
	 * @post Save image test_getInterpolationMean_* in /tmp/
	 */
	@Test
	public void test_getInterpolationMean() throws InterpolateException {
		ArrayList<Vector2d> out;
		Interpolate i = new Interpolate(testcase);
		out = (ArrayList<Vector2d>) i.getInterpolationMean(window.intValue());
		RoiSaver.saveROI("/tmp/test_getInterpolationMean_"+testfileName.getFileName()+"_"+window.toString()+".tif", out);
		logger.debug("setUp: "+testcase.toString());
		if(out.size()<100)
			logger.debug("testInterpolate: "+out.toString());
	}
	
	/**
	 * @test Simple test of roiSaver class, create reference images without processing
	 * @post Save image /tmp/testroiSaver_*.tif
	 */
	@Test
	@Ignore
	public void test_roiSaver() {
		RoiSaver.saveROI("/tmp/test_roiSaver_"+testfileName.getFileName()+"_"+window.toString()+".tif", testcase);
	}

}
