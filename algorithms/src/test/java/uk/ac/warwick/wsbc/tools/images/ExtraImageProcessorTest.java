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
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class ExtraImageProcessorTest extends ExtraImageProcessor {

	private ImagePlus image;
	private static final Logger logger = LogManager.getLogger(ExtraImageProcessorTest.class.getName());
	
	/**
	 * Dummy constructor filling underlying class with unused object replaced in setUp() method
	 */
	public ExtraImageProcessorTest() {
		super(new ByteProcessor(100,100));

	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		image = IJ.openImage("src/test/resources/testObject.tif"); // opens test image
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
	public void TestRotate() {
		double angle = 135;
		ImageProcessor ret = rotate(angle,true);
		IJ.saveAsTiff(new ImagePlus("",ret), "/tmp/testrotateImage.tif");
		logger.info("Check /tmp/testrotateImage.tif to see results of rotation");
	}
	
	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#rotate(double, boolean)}. with background settings
	 * @post
	 * Rotated image should have bas-reliefs oriented horizontally. Saves rotated image to /tmp/testrotateImage.tif. 
	 * There should be 0 background
	 */
	@Test
	public void TestRotate_0background() {
		double angle = 135;
		getIP().setBackgroundValue(0);
		ImageProcessor ret = rotate(angle,true);
		IJ.saveAsTiff(new ImagePlus("",ret), "/tmp/testrotateImage_0background.tif");
		logger.info("Check /tmp/testrotateImage_0background.tif to see results of rotation");
	}
	
	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#extendImageBeforeRotation(double)}.
	 * @post
	 * Saves extended image to /tmp/testextendImage_0s.tif. 
	 */
	@Test
	public void TestExtendImageToRotation_0s() {
		double angle = 0;
		ImageProcessor ret;
		extendImageBeforeRotation(angle);
		ret = getIP();
		assertEquals(513,ret.getWidth()); // size of the image
		assertEquals(513,ret.getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",ret), "/tmp/testextendImage_0s.tif"); 
		logger.info("Check /tmp/testextendImage_0s.tif to see results");		
	}
	
	/**
	 * @test
	 * Test method for {@link uk.warwick.tools.images.ExtraImageProcessor#extendImageBeforeRotation(double)}.
	 * @post
	 * Saves extended image to /tmp/testextendImage_45s.tif. 
	 */
	@Test
	public void TestExtendImageToRotation_45s() {
		double angle = 45;
		ImageProcessor ret;
		extendImageBeforeRotation(angle);
		ret = getIP();
		assertEquals(725,ret.getWidth()); // size of the image
		assertEquals(725,ret.getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",ret), "/tmp/testextendImage_45s.tif"); 
		logger.info("Check /tmp/testextendImage_45s.tif to see results");		
	}
	
	@Test
	public void Testcrop() {
		crop(10,10,200,200);
		assertEquals(200,getIP().getWidth()); // size of the image
		assertEquals(200,getIP().getHeight());
		IJ.saveAsTiff(new ImagePlus("extended",getIP()), "/tmp/testcrop.tif"); 
		logger.info("Check /tmp/testcrop.tif to see results");		
	}

}
