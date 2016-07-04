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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Params;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.Point;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkException;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentation;

/**
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

    private ImagePlus image;
    private ImagePlus seedImage;
    Params params;

    /**
     * 
     */
    public RandomWalkSegmentationPlugin_() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Shows user dialog and check conditions.
     * 
     * @return \c true if user clicked \b OK and input data are correct or 
     * return \c false otherwise
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("Random Walk segmentation");
        gd.addMessage("Random Walk segmentation");
        gd.addChoice("Image", WindowManager.getImageTitles(), ""); // image to be segmented
        gd.addChoice("Seed", WindowManager.getImageTitles(), ""); // seed image
        gd.addNumericField("alpha", 400, 0, 6, ""); // alpha
        gd.addNumericField("beta", 50, 2, 6, ""); // beta
        gd.addNumericField("gamma", 100, 2, 6, ""); // gamma
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
        // read GUI elements and store results in private fields order as these
        // methods are called should match to GUI build order
        //!<
        params = new Params(gd.getNextNumber(), // alpha
                gd.getNextNumber(), // beta
                gd.getNextNumber(), // gamma1
                0, // not used gamma 2
                80, // iterations
                0.1, // dt
                8e-3 // error
                );
        /**/
        if (gd.invalidNumber()) { // check if numbers in fields were correct
            IJ.error("Not valid number");
            LOGGER.error("One of the numbers in dialog box is not valid");
            return false;
        }
        return true; // all correct
    }

    @Override
    public void run(String arg) {
        ImageProcessor ret;
        if (showDialog()) { // returned true - all fields ok and initialized correctly
            RandomWalkSegmentation obj = new RandomWalkSegmentation(image.getProcessor(), params);
            Map<Integer, List<Point>> seeds;
            try {
                seeds = obj.decodeSeeds(seedImage, Color.RED, Color.GREEN);
                ret = obj.run(seeds);
                ImagePlus segmented =
                        new ImagePlus("Segmented_" + image.getTitle(), ret.convertToByte(true));
                segmented.show();
                segmented.updateAndDraw();
            } catch (RandomWalkException e) {
                LOGGER.error("Segmentation failed because: " + e.getMessage());
            }

        }

    }

}
