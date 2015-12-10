package uk.warwick.tools.images;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class ExtraImageProcessorTest extends ExtraImageProcessor {

	private ImagePlus image;
	private static final Logger logger = LogManager.getLogger(ExtraImageProcessorTest.class.getName());
	
	public ExtraImageProcessorTest() {
		// TODO Auto-generated constructor stub
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		image = IJ.openImage("src/test/java/uk/warwick/dic/lid/testObject.tif"); // opens test image
		setIP(image.getProcessor());
	}

	@After
	public void tearDown() throws Exception {
		if(image.changes) { // check if source was modified
			image.changes = false; // set flag to false to prevent save dialog
			image.close(); // close image
			throw new Exception("Image has been modified"); // throw exception if source image was modified
		}
		image.close();
		image = null;
	}

	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#rotate(double, boolean)}.
	 * @post
	 * Rotated image should have bas-reliefs oriented horizontally. Saves rotated image to /tmp/testrotateImage.tif. 
	 */
	@Test
	public void testRotate() {
		double angle = 135;
		ImageProcessor ret = rotate(angle,true);
		IJ.saveAsTiff(new ImagePlus("",ret), "/tmp/testrotateImage.tif");
		logger.info("Check /tmp/testrotateImage.tif to see results of rotation");
	}
	
	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#extendImageToRotation(double)}.
	 * @post
	 * Saves extended image to /tmp/testextendImage_0s.tif. 
	 */
	@Test
	public void testExtendImageToRotation_0s() {
		double angle = 0;
		ImageProcessor ret;
		extendImageToRotation(angle);
		ret = getIP();
		assertEquals(513,ret.getWidth()); // size of the image
		assertEquals(513,ret.getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",ret), "/tmp/testextendImage_0s.tif"); 
		logger.info("Check /tmp/testextendImage_0s.tif to see results");		
	}
	
	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#extendImageToRotation(double)}.
	 * @post
	 * Saves extended image to /tmp/testextendImage_45s.tif. 
	 */
	@Test
	public void testExtendImageToRotation_45s() {
		double angle = 45;
		ImageProcessor ret;
		extendImageToRotation(angle);
		ret = getIP();
		assertEquals(725,ret.getWidth()); // size of the image
		assertEquals(725,ret.getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",ret), "/tmp/testextendImage_45s.tif"); 
		logger.info("Check /tmp/testextendImage_45s.tif to see results");		
	}
	
	@Test
	public void testcrop() {
		crop(10,10,200,200);
		assertEquals(200,getImageProcessor().getWidth()); // size of the image
		assertEquals(200,getImageProcessor().getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",getImageProcessor()), "/tmp/testcrop.tif"); 
		logger.info("Check /tmp/testcrop.tif to see results");		
	}

}
