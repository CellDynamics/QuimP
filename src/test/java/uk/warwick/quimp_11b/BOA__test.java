package uk.warwick.quimp_11b;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import uk.warwick.quimp_11b.BOA_.*;

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
		img = IJ.openImage("../../Tests/uk/warwick/quimp_11b/movie03.tif");
	}

	@After
	public void tearDown() throws Exception {
		img = null;
	}

	@Test
	/**
	 * Runs BOA_ class and displays plugin window
	 * @author baniuk
	 * @test RunBOA()
	 */
	public void RunBOA() {
		img.show(); // this is necessary for plugin as it uses getcurrentimage to work
		BOA_ ob = new BOA_();
		ob.run(null);
		new WaitForUserDialog("Do something, then click OK.").show();		
	}

}
