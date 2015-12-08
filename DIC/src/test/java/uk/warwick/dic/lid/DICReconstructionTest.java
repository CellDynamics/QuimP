/**
 * 
 */
package uk.warwick.dic.lid;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;

/**
 * @author baniuk
 *
 */
public class DICReconstructionTest extends DICReconstruction {
	
	private ImagePlus image;
	private static final Logger logger = LogManager.getLogger(DICReconstructionTest.class.getName());

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
	 * @warning May not detect changes done on image (e.g. rotation)
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
	 * @test Test method for {@link uk.warwick.dic.lid.DICReconstruction#DICReconstruction()}.
	 */
	@Test
	public void testDICReconstruction() {
		// Empty constructor - nothing to do here
	}

	/**
	 * @test
	 * Test method for {@link uk.warwick.dic.lid.DICReconstruction#uk.warwick.dic.lid.DICReconstruction.rotateImage(ImageProcessor, double)}.
	 * Saves rotated image to /tmp/testrotateImage.tif. 
	 * @post
	 * Rotated image should have bas-reliefs oriented horizontally
	 */
	@Test
	public void testrotateImage() {
		rotateImage(image.getProcessor(), 135f);
		IJ.saveAsTiff(image, "/tmp/testrotateImage.tif");
		logger.info("Check /tmp/testrotateImage.tif to see results of rotation");
	}
	/**
	 * @ test Test method for {@link uk.warwick.dic.lid.DICReconstruction#reconstructionDicLid(ij.ImagePlus, double, double)}.
	 * Saves output image at \c /tmp/testDicReconstructionLidMatrix.tif
	 * @pre
	 * Input image is square
	 * @post
	 * Output image should be properly reconstructed
	 */
	@Test
	public void testreconstructionDicLid() {
		ImagePlus ret;
		ret = reconstructionDicLid(image,0.04,135f);
		IJ.saveAsTiff(ret, "/tmp/testDicReconstructionLidMatrix.tif"); 
		logger.info("Check /tmp/testDicReconstructionLidMatrix.tif to see results");
	}

}
