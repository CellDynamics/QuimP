/**
 * 
 */
package uk.warwick.dic.lid;

import static org.junit.Assert.assertEquals;

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
public class DICReconstructionTest {
	
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
 	 * @ test Test method for {@link uk.warwick.dic.lid.DICReconstruction#reconstructionDicLid(ImagePlus, double, double)}.
	 * Saves output image at \c /tmp/testDicReconstructionLidMatrix.tif
	 * @pre
	 * Input image is square
	 * @post
	 * Output image should be properly reconstructed and have correct size of input image
	 */
	@Test
	public void testreconstructionDicLid() {
		ImagePlus ret;
		DICReconstruction dcr;
		try {
			dcr = new DICReconstruction(image, 0.04, 135f);
			ret = dcr.reconstructionDicLid();
			assertEquals(513,ret.getWidth()); // size of the image
			assertEquals(513,ret.getHeight());
			IJ.saveAsTiff(ret, "/tmp/testDicReconstructionLidMatrix.tif"); 
			logger.info("Check /tmp/testDicReconstructionLidMatrix.tif to see results");
		} catch (DicException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

}
