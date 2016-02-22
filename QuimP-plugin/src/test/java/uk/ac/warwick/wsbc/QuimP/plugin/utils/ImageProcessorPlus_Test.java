package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import static org.junit.Assert.*;

import java.util.Vector;

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

public class ImageProcessorPlus_Test {

    private ImagePlus image;
    private static final Logger LOGGER = LogManager.getLogger(ImageProcessorPlus_Test.class.getName());
    private ImageProcessorPlus ipp;

    /**
     * Dummy constructor
     */
    public ImageProcessorPlus_Test() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        image = IJ.openImage("src/test/resources/testObject.tif"); // opens test
                                                                   // image
        ipp = new ImageProcessorPlus();
    }

    @After
    public void tearDown() throws Exception {
        if (image.changes) { // check if source was modified
            image.changes = false; // set flag to false to prevent save dialog
            image.close(); // close image
            throw new Exception("Image has been modified"); // throw exception
                                                            // if source image
                                                            // was modified
        }
        image.close();
        image = null;
    }

    /**
     * @test Test method for
     *       {@link uk.ac.warwick.wsbc.tools.images.ImageProcessorPlus#rotate(ImageProcessor, double, boolean)}
     * @post Rotated image should have bas-reliefs oriented horizontally. Saves
     *       rotated image to /tmp/testrotateImage.tif.
     */
    @Test
    public void test_Rotate() {
        double angle = 135;
        ImageProcessor ret = ipp.rotate(image.getProcessor(), angle, true);
        IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testrotateImage.tif");
        LOGGER.info("Check /tmp/testrotateImage.tif to see results of rotation");
    }

    /**
     * @test Test method for
     *       {@link uk.ac.warwick.wsbc.tools.images.ImageProcessorPlus#rotate(ImageProcessor, double, boolean)}
     *       with background settings
     * @post Rotated image should have bas-reliefs oriented horizontally. Saves
     *       rotated image to /tmp/testrotateImage.tif. There should be 0
     *       background
     */
    @Test
    public void test_Rotate_0background() {
        double angle = 135;
        image.getProcessor().setBackgroundValue(0);
        ImageProcessor ret = ipp.rotate(image.getProcessor(), angle, true);
        IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testrotateImage_0background.tif");
        LOGGER.info("Check /tmp/testrotateImage_0background.tif to see results of rotation");
    }

    /**
     * @test Test method for
     *       {@link uk.ac.warwick.wsbc.tools.images.ImageProcessorPlus#extendImageBeforeRotation(ImageProcessor, double)}
     * @post Saves extended image to /tmp/testextendImage_0s.tif.
     */
    @Test
    public void test_ExtendImageToRotation_0s() {
        double angle = 0;
        ImageProcessor ret;
        ret = ipp.extendImageBeforeRotation(image.getProcessor(), angle);
        assertEquals(513, ret.getWidth()); // size of the image
        assertEquals(513, ret.getHeight());
        IJ.saveAsTiff(new ImagePlus("extended", ret), "/tmp/testextendImage_0s.tif");
        LOGGER.info("Check /tmp/testextendImage_0s.tif to see results");
    }

    /**
     * @test Test method for
     *       {@link uk.ac.warwick.wsbc.tools.images.ImageProcessorPlus#extendImageBeforeRotation(ImageProcessor, double)}
     * @post Saves extended image to /tmp/testextendImage_45s.tif.
     */
    @Test
    public void test_ExtendImageToRotation_45s() {
        double angle = 45;
        ImageProcessor ret;
        ret = ipp.extendImageBeforeRotation(image.getProcessor(), angle);
        assertEquals(725, ret.getWidth()); // size of the image
        assertEquals(725, ret.getHeight());
        IJ.saveAsTiff(new ImagePlus("extended", ret), "/tmp/testextendImage_45s.tif");
        LOGGER.info("Check /tmp/testextendImage_45s.tif to see results");
    }

    @Test
    public void test_crop() {
        ImageProcessor ret;
        ret = ipp.crop(image.getProcessor(), 10, 10, 200, 200);
        assertEquals(200, ret.getWidth()); // size of the image
        assertEquals(200, ret.getHeight());
        IJ.saveAsTiff(new ImagePlus("extended", ret), "/tmp/testcrop.tif");
        LOGGER.info("Check /tmp/testcrop.tif to see results");
    }

    /**
     * @test Test of RectangleBox for square image and angle 0 deg
     */
    @Test
    public void test_RectangleBox_0s() {
        int width = 512;
        int height = 512;
        double angle = 0;

        RectangleBox r = new RectangleBox(width, height);
        r.rotateBoundingBox(angle);

        assertEquals(512, r.getWidth(), 0);
        assertEquals(512, r.getHeight(), 0);
    }

    /**
     * @throws Exception
     * @test Test of RectangleBox for square image and angle 0 deg for input
     *       vectors
     */
    @Test
    public void test_RectangleBoxVector_0s() throws Exception {

        Vector<Double> x = new Vector<Double>();
        Vector<Double> y = new Vector<Double>();

        x.add(-10.0);
        x.add(10.0);
        x.add(10.0);
        x.add(-10.0);
        y.add(10.0);
        y.add(10.0);
        y.add(-10.0);
        y.add(-10.0);

        double angle = 0;

        RectangleBox r = new RectangleBox(x, y);
        r.rotateBoundingBox(angle);

        assertEquals(20, r.getWidth(), 0);
        assertEquals(20, r.getHeight(), 0);
    }

    /**
     * @throws Exception
     * @test Test of RectangleBox for square image and angle 45 deg for input
     *       vectors
     */
    @Test
    public void test_RectangleBoxVector_45s() throws Exception {

        Vector<Double> x = new Vector<Double>();
        Vector<Double> y = new Vector<Double>();

        x.add(-10.0);
        x.add(10.0);
        x.add(10.0);
        x.add(-10.0);
        y.add(10.0);
        y.add(10.0);
        y.add(-10.0);
        y.add(-10.0);

        double angle = 45;

        RectangleBox r = new RectangleBox(x, y);
        r.rotateBoundingBox(angle);

        assertEquals(28, Math.round(r.getWidth()), 0);
        assertEquals(28, Math.round(r.getHeight()), 0);
    }

    /**
     * @test Test of RectangleBox for square image and angle 90 deg
     */
    @Test
    public void test_RectangleBox_90s() {
        int width = 512;
        int height = 512;
        double angle = 90;

        RectangleBox r = new RectangleBox(width, height);
        r.rotateBoundingBox(angle);

        assertEquals(512, r.getWidth(), 0);
        assertEquals(512, r.getHeight(), 0);
    }

    /**
     * @test Test of RectangleBox for non square image and angle 90 deg
     */
    @Test
    public void test_RectangleBox_90ns() {
        int width = 512;
        int height = 1024;
        double angle = 90;

        RectangleBox r = new RectangleBox(width, height);
        r.rotateBoundingBox(angle);

        assertEquals(1024, Math.round(r.getWidth()), 0);
        assertEquals(512, Math.round(r.getHeight()), 0);
    }

    /**
     * @test Test of RectangleBox for square image and angle 45 deg
     */
    @Test
    public void test_RectangleBox_45s() {
        int width = 512;
        int height = 512;
        double angle = 45;

        RectangleBox r = new RectangleBox(width, height);
        r.rotateBoundingBox(angle);

        assertEquals(724, Math.round(r.getWidth()), 0);
        assertEquals(724, Math.round(r.getHeight()), 0);
    }

    /**
     * @test Test of RectangleBox for square image and angle 30 deg
     */
    @Test
    public void test_RectangleBox_30s() {
        int width = 512;
        int height = 512;
        double angle = 30;

        RectangleBox r = new RectangleBox(width, height);
        r.rotateBoundingBox(angle);

        assertEquals(699, Math.round(r.getWidth()), 0);
        assertEquals(699, Math.round(r.getHeight()), 0);
    }

}
