/**
 * 
 */
package uk.warwick.dic.lid;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;

/**
 * @author baniuk
 *
 */
public class DicLidMatrixTest extends DicLidMatrix {
	
	private ImagePlus image;
	private static final Logger logger = LogManager.getLogger(DicLidMatrixTest.class.getName());
	public DicLidMatrixTest() {
		
	}

	/**
	 * Load test image
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		image = IJ.openImage("src/test/java/uk/warwick/dic/lid/testObject.tif"); // opens test image
	}

	/**
	 * @throws java.lang.Exception
	 * @warning May not detect changes done on image
	 */
	@After
	public void tearDown() throws Exception {
		if(image.changes) { // check if source was modified
			image.changes = false; // set flag to false to prevent save dialog
			image.close(); // close image
			throw new Exception("Image has been modified"); // throw exception if source image was modified
		}
		image.close();
	}

	/**
	 * @test Test method for {@link uk.warwick.dic.lid.DicLidMatrix#DicLidMatrix()}.
	 */
	@Test
	public void testDicLidMatrix() {
		// Empty constructor - nothing to do here
	}

	/**
	 * @ test Test method for {@link uk.warwick.dic.lid.DicLidMatrix#DicReconstructionLidMatrix(ij.ImagePlus, double)}.
	 * Saves output image at \c /tmp/testDicReconstructionLidMatrix.tif
	 */
	@Test
	public void testDicReconstructionLidMatrix() {
		ImagePlus ret;
		ret = DicReconstructionLidMatrix(image,0);
		IJ.saveAsTiff(ret, "/tmp/testDicReconstructionLidMatrix.tif"); 
		logger.info("Check /tmp/testDicReconstructionLidMatrix.tif to see results");
	}

}
