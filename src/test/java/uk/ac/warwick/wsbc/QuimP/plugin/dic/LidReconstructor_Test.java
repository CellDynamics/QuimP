/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class LidReconstructor_Test {

    private ImagePlus image;
    private ImagePlus stack;
    static final Logger LOGGER = LoggerFactory.getLogger(LidReconstructor_Test.class.getName());

    /**
     * Load test image
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        image = IJ.openImage("src/test/resources/testObject.tif"); // opens test
                                                                   // image

        // opens test image
        stack = IJ.openImage("src/test/resources/testObject_4slices.tif");
    }

    /**
     * @throws java.lang.Exception
     * @warning May not detect changes done on image (e.g. rotation)
     */
    @After
    public void tearDown() throws Exception {
        if (image.changes || stack.changes) { // check if source was modified
            image.changes = false; // set flag to false to prevent save dialog
            stack.changes = false;
            image.close(); // close image
            stack.close();
            throw new Exception("Image has been modified"); // throw exception
                                                            // if source image
                                                            // was modified
        }
        image.close();
        stack.close();
    }

    /**
     * Test method for wsbc.QuimP.plugin.dic.LidReconstructor.reconstructionDicLid() Saves output
     * image at \c /tmp/testDicReconstructionLidMatrix.tif
     * 
     * Input image is square Output image should be properly reconstructed and have correct size of
     * input image
     */
    @Test
    public void test_ReconstructionDicLid() {
        ImageProcessor ret;
        LidReconstructor dcr;
        try {
            dcr = new LidReconstructor(image, 0.04, 135f);
            // replace outputImage processor with result array with scaling
            // conversion
            ret = dcr.reconstructionDicLid();
            ImagePlus outputImage = new ImagePlus("", ret);

            assertEquals(513, outputImage.getWidth()); // size of the image
            assertEquals(513, outputImage.getHeight());
            IJ.saveAsTiff(outputImage, "/tmp/testDicReconstructionLidMatrix.tif");
            LOGGER.trace("Check /tmp/testDicReconstructionLidMatrix.tif" + " to see results");
        } catch (DicException e) {
            LOGGER.error(e.toString());
        }

    }

    @Test
    public void test_ReconstructionDicLid_filt() {
        ImageProcessor ret;
        LidReconstructor dcr;
        try {
            dcr = new LidReconstructor(image.getProcessor(), 0.04, 135f, "45", 9);
            // replace outputImage processor with result array with scaling
            // conversion
            ret = dcr.reconstructionDicLid();
            ImagePlus outputImage = new ImagePlus("", ret);

            assertEquals(513, outputImage.getWidth()); // size of the image
            assertEquals(513, outputImage.getHeight());
            IJ.saveAsTiff(outputImage, "/tmp/testDicReconstructionLidMatrix_filt.tif");
            LOGGER.trace("Check /tmp/testDicReconstructionLidMatrix_filt.tif" + " to see results");
        } catch (DicException e) {
            LOGGER.error(e.toString());
        }

    }

    /**
     * Test method for wsbc.QuimP.plugin.dic.LidReconstructor.reconstructionDicLid() Saves output
     * image at \c /tmp/testDicReconstructionLidMatrix_sat.tif
     * 
     * Input image is square and saturated Throws exception DicException because of saturated image
     */
    @Test(expected = DicException.class)
    public void test_ReconstructionDicLid_saturated() throws DicException {
        ImageProcessor ret;
        LidReconstructor dcr;
        ImageConverter.setDoScaling(true);
        ImageConverter image16 = new ImageConverter(image);
        image16.convertToGray16();

        image.getProcessor().putPixel(100, 100, 65535);

        try {
            dcr = new LidReconstructor(image, 0.04, 135f);
            ret = dcr.reconstructionDicLid();
            ImagePlus outputImage = new ImagePlus("", ret);
            assertEquals(513, outputImage.getWidth()); // size of the image
            assertEquals(513, outputImage.getHeight());
            IJ.saveAsTiff(outputImage, "/tmp/testDicReconstructionLidMatrix_sat.tif");
            LOGGER.trace("Check /tmp/testDicReconstructionLidMatrix_sat.tif" + " to see results");
        } catch (DicException e) {
            throw e;
        }

    }

    /**
     * Test method for warwick.wsbc.QuimP.plugin.dic.LidReconstructor.setIp(ImageProcessor) Saves
     * output image at \c /tmp/testDicReconstructionLidMatrix_Stack.tif
     * 
     * Input stack is square Reconstructed stack
     */
    @Test()
    public void test_ReconstructionDicLid_stack() {
        ImageProcessor ret;
        LidReconstructor dcr;
        try {
            dcr = new LidReconstructor(stack, 0.04, 135f);
            ImageStack is = stack.getStack();
            for (int s = 1; s <= is.getSize(); s++) {
                dcr.setIp(is.getProcessor(s));
                ret = dcr.reconstructionDicLid();
                is.setPixels(ret.getPixels(), s);
            }

            ImagePlus outputImage = new ImagePlus("", is);

            assertEquals(513, outputImage.getWidth()); // size of the image
            assertEquals(513, outputImage.getHeight());
            IJ.saveAsTiff(outputImage, "/tmp/testDicReconstructionLidMatrix_stack.tif");
            LOGGER.trace("Check /tmp/testDicReconstructionLidMatrix_stack.tif to" + " see results");
        } catch (DicException e) {
            LOGGER.error(e.toString());
        }
    }
}
