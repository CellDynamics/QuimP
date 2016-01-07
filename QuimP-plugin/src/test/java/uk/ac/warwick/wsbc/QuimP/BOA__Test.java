package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;

/**
 * Test class for BOA_ plugin
 * @author baniuk
 *
 */
public class BOA__Test {

	ImagePlus img;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		img = IJ.openImage("src/test/resources/movie03.tif");
	}

	@After
	public void tearDown() throws Exception {
		img = null;
	}
	
	@Test
	public void testExample() {
		assertEquals(10,10);
	}

	@Test
	public void testExample1() {
		assertEquals(10,10);
	}
}
