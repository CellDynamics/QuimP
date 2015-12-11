package uk.warwick.quimp;

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
public class BOA__test {

	ImagePlus img;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		img = IJ.openImage("src/test/java/uk/warwick/quimp_11b/movie03.tif");
	}

	@After
	public void tearDown() throws Exception {
		img = null;
	}
}
