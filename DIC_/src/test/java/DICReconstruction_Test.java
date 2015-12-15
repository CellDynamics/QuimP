import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DICReconstruction_Test {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @test Test of GUI builder for DICReconstruction_
	 * @post Shows GUI and expect correct inputs (numbers) and OK
	 */
	@Test
	public void testBuildGUI() {
		DICReconstruction_ dic = new DICReconstruction_();
		assertTrue(dic.showDialog());
	}

}
