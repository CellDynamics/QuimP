/**
 * @file RandomWalkSegmentationPlugin.java
 * @date 4 Jul 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Params;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Point;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.PropagateSeeds;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkException;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentation;

/**
 * Run RandomWalkSegmentation in IJ environment.
 * 
 * Implements common PlugIn interface as both images are provided after run.
 * The seed can be one image - in this case seed propagation is used to generate seed for
 * subsequent frames, or it can be stack of the same size as image. In latter case every slice
 * from seed is used for seeding related slice from image.
 *  
 * @author p.baniukiewicz
 * @date 4 Jul 2016
 *
 */
public class RandomWalkSegmentationPlugin_ implements PlugIn {

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER =
            LogManager.getLogger(RandomWalkSegmentationPlugin_.class.getName());

    private ImagePlus image; //!< stack or image to segment
    private ImagePlus seedImage; //!< RGB seed image
    private Params params; // parameters
    private int erodeIter; //!< number of erosions for generating next seed from previous
    private boolean useSeedStack; //!< \a true if seed has the same size as image, slices are seeds 

    /**
     * Shows user dialog and check conditions.
     * 
     * @return \c true if user clicked \b OK and input data are correct or 
     * return \c false otherwise
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("Random Walk segmentation");
        gd.addChoice("Image", WindowManager.getImageTitles(), ""); // image to be segmented
        gd.addChoice("Seed", WindowManager.getImageTitles(), ""); // seed image
        gd.addNumericField("alpha", 400, 0, 6, ""); // alpha
        gd.addNumericField("beta", 50, 2, 6, ""); // beta
        gd.addNumericField("gamma", 100, 2, 6, ""); // gamma
        gd.addNumericField("Iterations", 80, 3);
        gd.addNumericField("erode iterations", 5, 0, 2, "");

        //!<
        gd.addMessage(
                "The erode iterations depend\n"
              + "on how fast cells move or how\n"
              + "big are differences betweenn\n"
              + "succeeding frames.");
        /**/
        gd.setResizable(false);
        gd.showDialog();
        // user response, return false on any error
        if (gd.wasCanceled()) // check if user clicked OK or CANCEL
            return false;
        image = WindowManager.getImage(gd.getNextChoice());
        seedImage = WindowManager.getImage(gd.getNextChoice());
        if (image.getBitDepth() != 8 && image.getBitDepth() != 16) {
            IJ.showMessage("Error", "Image must be 8 or 16 bit");
            return false; // wrong image type
        }
        if (seedImage.getBitDepth() != 24) {
            IJ.showMessage("Error", "Seed image must be 24 bit");
            return false; // wrong seed
        }
        if (seedImage.getStackSize() == 1)
            useSeedStack = false; // use propagateSeed for generating next frame seed from previous
        else if (seedImage.getStackSize() == image.getStackSize())
            useSeedStack = true; // use slices as seeds
        else {
            IJ.showMessage("Error", "Seed must be image or stack of the same size as image");
            return false; // wrong seed size
        }

        // read GUI elements and store results in private fields order as these
        // methods are called should match to GUI build order
        //!<
        params = new Params(gd.getNextNumber(), // alpha
                gd.getNextNumber(), // beta
                gd.getNextNumber(), // gamma1
                0, // not used gamma 2
                (int) gd.getNextNumber(), // iterations
                0.1, // dt
                8e-3 // error
                );
        /**/
        erodeIter = (int) Math.round(gd.getNextNumber()); // erosions
        if (gd.invalidNumber()) { // check if numbers in fields were correct
            IJ.error("Not valid number");
            LOGGER.error("One of the numbers in dialog box is not valid");
            return false;
        }
        return true; // all correct
    }

    /**
     * Plugin runner. 
     * 
     * Shows UI and perform segmentation after validating UI
     */
    @Override
    public void run(String arg) {
        ImageStack ret; // all images treated as stacks
        Map<Integer, List<Point>> seeds;
        if (showDialog()) { // returned true - all fields ok and initialized correctly
            if (image == null || seedImage == null) {
                IJ.showMessage("Error", "Select both images first");
            }
            try {
                ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
                ImageStack is = image.getStack(); // get current stack (size 1 for one image)
                // segment first slice (or image if it is not stack)
                RandomWalkSegmentation obj = new RandomWalkSegmentation(is.getProcessor(1), params);
                seeds = obj.decodeSeeds(seedImage.getStack().getProcessor(1), Color.RED,
                        Color.GREEN); // generate seeds
                ImageProcessor retIp = obj.run(seeds); // segmentation
                ret.addSlice(retIp.convertToByte(true)); // store output in new stack
                // iterate over all slices after first (may not run for one image)
                for (int s = 2; s <= is.getSize(); s++) {
                    Map<Integer, List<Point>> nextseed;
                    obj = new RandomWalkSegmentation(is.getProcessor(s), params);
                    // get seeds from previous result
                    if (useSeedStack) { // true - use slices
                        nextseed = obj.decodeSeeds(seedImage.getStack().getProcessor(s), Color.RED,
                                Color.GREEN);
                    } else // false - use previous frame
                        nextseed = PropagateSeeds.propagateSeed(retIp, erodeIter);
                    retIp = obj.run(nextseed); // segmentation and results stored for next seeding
                    ret.addSlice(retIp); // add next slice
                    IJ.showProgress(s - 1, is.getSize());
                }
                // convert to ImagePlus and show
                ImagePlus segmented = new ImagePlus("Segmented_" + image.getTitle(), ret);
                segmented.show();
                segmented.updateAndDraw();
            } catch (RandomWalkException e) {
                LOGGER.error("Segmentation failed because: " + e.getMessage());
            }

        }

    }

}
