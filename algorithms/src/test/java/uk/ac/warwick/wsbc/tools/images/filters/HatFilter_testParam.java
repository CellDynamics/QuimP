/**
 * @file HatFilter_testParam.java
 * @date 25 Jan 2016
 */
package uk.ac.warwick.wsbc.tools.images.filters;

import static org.junit.Assert.*;

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
     * from parameters we defined in primeNumbers() method
     * 
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
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> testFiles() {
		return Arrays.asList(new Object[][] {
			{"src/test/resources/testData_137.dat",13,5,0.01},
		});
	}
	

	/**
	 * @throws FilterException 
	 * @test Test of getInterpolationLoess method
	 * @post Save image test_HatFilter_* in /tmp/
	 */
	@Test
	public void test_HatFilter() throws FilterException {
		ArrayList<Vector2d> out;
		HatFilter hf = new HatFilter(testcase, window, crown, sig);
		out = (ArrayList<Vector2d>) hf.RunFilter();
		RoiSaver.saveROI("/tmp/test_HatFilter_"+testfileName.getFileName()+"_"+window.toString()+"_"+crown.toString()+"_"+sig.toString()+".tif", out);
		logger.debug("setUp: "+testcase.toString());
	}
}
