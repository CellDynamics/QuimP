package uk.ac.warwick.wsbc.tools.images.filters;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import uk.ac.warwick.wsbc.tools.images.FilterException;

/**
 * Test class for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
public class HatFilter_test {
	
	private static final Logger logger = LogManager.getLogger(HatFilter_test.class.getName());
	List<Vector2d> input;

	/**
	 * Allow to get tested method name (called at setUp())
	 */
	@Rule
	public TestName name = new TestName();
	
	/**
	 * Create line with nodes in every 1 unit. 
	 * 
	 * Three middle nodes are moved to y=1:
	 * @code
	 *                  ---
	 * -----------------   --------------------
	 * @endcode
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		input = new ArrayList<>();
		for(int i = 0; i<40; i++)
			input.add(new Vector2d(i,0));
		input.set(18, new Vector2d(18,1));
		input.set(19, new Vector2d(19,1));
		input.set(20, new Vector2d(20,1));
		logger.info("Entering "+name.getMethodName());
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @test test of HatFilter method
	 * @pre vector line defined in setUp()
	 * @post all nodes accepted. input==output
	 * @throws FilterException
	 */
	@Test
	public void test_HatFilter_case1() throws FilterException {
		logger.debug("input: "+input.toString());
		HatFilter hf = new HatFilter(input, 5, 3, 1);
		ArrayList<Vector2d> out = (ArrayList<Vector2d>) hf.RunFilter();
		logger.debug("  out: "+out.toString());
		assertEquals(input, out);
	}
	
	/**
	 * @test test of HatFilter method
	 * @pre vector line defined in setUp()
	 * @post nodes 0, 1, 2, 37, 38, 39, 15, 16, 17, 18, 19, 20, 21, 22, 23 removed
	 * @throws FilterException
	 */
	@Test
	public void test_HatFilter_case2() throws FilterException {
		logger.debug("input: "+input.toString());
		HatFilter hf = new HatFilter(input, 5, 3, 0.05);
		ArrayList<Vector2d> out = (ArrayList<Vector2d>) hf.RunFilter();
		logger.debug("  out: "+out.toString());
		
		// remove precalculated indexes from input array (see Matlab test code)
		int removed[] = {0, 1, 2, 37, 38, 39, 15, 16, 17, 18, 19, 20, 21, 22, 23};
		Arrays.sort(removed);
		int lr = 0;
		for(int el : removed)
			input.remove(el-lr++);
		logger.debug(input.toString());	
		assertEquals(input, out);	
	}
	
	/**
	 * @test Input condition for HatFilter
	 * @pre Various bad combinations of inputs
	 * @post Exception FilterException
	 */
	@Test
	public void test_HatFilter_case3() {
		try {
			HatFilter hf = new HatFilter(input, 6, 3, 1); // even window
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 5, 4, 1); // even crown
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 5, 5, 1); // crown>window
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 5, 0, 1); // bad crown
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 0, 3, 1); // bad crown
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 0, -3, 1); // bad crown
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
		try {
			HatFilter hf = new HatFilter(input, 1, 1, 1); // bad crown
			hf.RunFilter();
			fail("Exception not thrown");
		} catch (FilterException e) {
			assertTrue(e!=null);
			logger.debug(e.getMessage());
		}
	}

}
