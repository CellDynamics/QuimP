/**
 * @file HatFilter_testParam.java
 * @date 25 Jan 2016
 */
package uk.ac.warwick.wsbc.tools.images.filters;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.warwick.wsbc.tools.images.FilterException;

/**
 * Parameterized test for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
@RunWith(Parameterized.class)
public class HatFilter_testParam {
	private static final Logger logger = LogManager.getLogger(HatFilter_testParam.class.getName());
	private Integer window;
	private Integer crown;
	private Double sig;
	private List<Vector2d> testcase;
	private Path testfileName;

	/**
	 * Parameterized constructor.
	 * 
	 * Each parameter should be placed as an argument here
	 * Every time runner triggers, it will pass the arguments
	 * from parameters we defined to this method
	 * @param testFileName test file name
	 * @param window filter window size
	 * @param crown filter crown size
	 * @param sig sigma value
	 * @see DataLoader
	 * @see HatFilter
	 */
	public HatFilter_testParam(String testFileName, Integer window, Integer crown, Double sig) {
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
	 * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating *.dat files
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> testFiles() {
		return Arrays.asList(new Object[][] {
			{"src/test/resources/testData_137.dat",23,13,0.3},
			{"src/test/resources/testData_1.dat",23,13,0.3},
			{"src/test/resources/testData_125.dat",23,13,0.3},
			{"src/test/resources/testData_75.dat",23,13,0.3}
		});
	}
	

	/**
	 * @test Test of getInterpolationLoess method
	 * @pre Real cases extrcted from 
	 * @post Save image test_HatFilter_* in /tmp/
	 * @throws FilterException 
	 * @see QuimP-toolbox/algorithms/src/test/resources/HatFilter.m for verification of logs (ratios, indexes, etc)
	 * @see QuimP-toolbox/algorithms/src/test/resources/Interpolate_Test_Analyzer.m for plotting results
	 * @see QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating *.dat files
	 */
	@Test
	public void test_HatFilter() throws FilterException {
		ArrayList<Vector2d> out;
		HatFilter hf = new HatFilter(testcase, window, crown, sig);
		out = (ArrayList<Vector2d>) hf.RunFilter();
		RoiSaver.saveROI("/tmp/test_HatFilter_"+testfileName.getFileName()+"_"+window.toString()+"_"+crown.toString()+"_"+sig.toString()+".tif", out);
		logger.debug("setUp: "+testcase.toString());
	}
	
	/**
	 * @test Simple test of RoiSaver class, create reference images without processing but with the same name scheme
	 * @post Save image /tmp/testroiSaver_*.tif
	 */
	@Test
	public void test_roiSaver() {
		RoiSaver.saveROI("/tmp/ref_HatFilter_"+testfileName.getFileName()+"_"+window.toString()+"_"+crown.toString()+"_"+sig.toString()+".tif", testcase);
	}
}
