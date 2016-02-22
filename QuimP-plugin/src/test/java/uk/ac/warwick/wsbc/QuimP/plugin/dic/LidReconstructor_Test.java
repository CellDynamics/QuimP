/**
 * @file LidReconstructor_Test.java
 */
package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * @author baniuk
 *
 */
public class LidReconstructor_Test {

    private ImagePlus image;
    private ImagePlus stack;
    private static final Logger LOGGER =
            LogManager.getLogger(LidReconstructor_Test.class.getName());

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
     * @test Test method for
     * {@link LidReconstructor.wsbc.dic.lid.DICReconstruction#reconstructionDicLid()}
     * Saves output image at \c /tmp/testDicReconstructionLidMatrix.tif
     * @pre Input image is square
     * @post Output image should be properly reconstructed and have correct size
     * of input image
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
            IJ.saveAsTiff(outputImage,
                    "/tmp/testDicReconstructionLidMatrix.tif");
            LOGGER.trace(
                    "Check /tmp/testDicReconstructionLidMatrix.tif"
                            + " to see results");
        } catch (DicException e) {
            LOGGER.error(e);
        }

    }

    /**
     * @test Test method for
     * {@link LidReconstructor.wsbc.dic.lid.DICReconstruction#reconstructionDicLid()}
     * Saves output image at \c /tmp/testDicReconstructionLidMatrix_sat.tif
     * @pre Input image is square and saturated
     * @post Throws exception DicException because of saturated image
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
            IJ.saveAsTiff(outputImage,
                    "/tmp/testDicReconstructionLidMatrix_sat.tif");
            LOGGER.trace(
                    "Check /tmp/testDicReconstructionLidMatrix_sat.tif"
                            + " to see results");
        } catch (DicException e) {
            throw e;
        }

    }

    /**
     * @test Test method for
     * {@link LidReconstructor.dic.lid.DICReconstruction#setIp(final
     * ImageProcessor)}
     * Saves output image at \c /tmp/testDicReconstructionLidMatrix_Stack.tif
     * @pre Input stack is square
     * @post Reconstructed stack
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
            IJ.saveAsTiff(outputImage,
                    "/tmp/testDicReconstructionLidMatrix_stack.tif");
            LOGGER.trace(
                    "Check /tmp/testDicReconstructionLidMatrix_stack.tif to"
                            + " see results");
        } catch (DicException e) {
            LOGGER.error(e);
        }

    }

}
