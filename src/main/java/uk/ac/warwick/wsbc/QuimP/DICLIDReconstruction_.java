package uk.ac.warwick.wsbc.QuimP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.dic.DicException;
import uk.ac.warwick.wsbc.QuimP.plugin.dic.LidReconstructor;

/**
 * Main implementation of ImageJ plugin
 * 
 * Currently supports only 8bit images and stacks.
 * 
 * @author p.baniukiewicz
 * @date 14 Dec 2015
 * @see LidReconstructor for algorithm details *
 */
public class DICLIDReconstruction_ implements PlugInFilter {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER =
            LogManager.getLogger(DICLIDReconstruction_.class.getName());
    private LidReconstructor dic;
    private ImagePlus imp;
    private double angle, decay;

    /**
     * This method gets called by ImageJ/Fiji to determine whether the current
     * image is of an appropriate type.
     * 
     * @param arg can be specified in plugins.config
     * @param imp is the currently opened image
     * @return Combination of flags determining supported formats: 
     * \li DOES_8G - plugin supports 8bit grayscale images
     * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    /**
     * This method is run when current image was accepted and input data were
     * correct
     * 
     * @param ip is the current slice
     * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
     */
    @Override
    public void run(ImageProcessor ip) {
        ImageProcessor ret;
        if (!showDialog())
            return; // if user clicked Cancel or data were not valid
        try {
            dic = new LidReconstructor(imp, decay, angle);
            if (imp.getNSlices() == 1) {// if there is no stack we can avoid additional rotation
                                        // here (see DICReconstruction documentation)
                IJ.showProgress(0.0);
                ret = dic.reconstructionDicLid();
                ip.setPixels(ret.getPixels()); // DICReconstruction works with duplicates. Copy
                                               // resulting array to current image
                IJ.showProgress(1.0);
            } else { // we have stack. Process slice by slice
                ImageStack stack = imp.getStack();
                for (int s = 1; s <= stack.getSize(); s++) {
                    IJ.showProgress(s / (double) stack.getSize());
                    dic.setIp(stack.getProcessor(s));
                    ret = dic.reconstructionDicLid();
                    stack.setPixels(ret.getPixels(), s);
                }
            }
        } catch (DicException e) { // exception can be thrown if input image is 16-bit and saturated
            IJ.log(e.getMessage());
            LOGGER.error(e);
        } finally {
            imp.updateAndDraw();
        }
    }

    /**
     * Shows user dialog and check conditions.
     * 
     * @return \c true if user clicked \b OK and input data are correct (they are numbers) or 
     * return \c false otherwise
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("DIC reconstruction");
        gd.addMessage("Reconstruction of DIC image by Line Integrals\n\nShear angle"
                + " is measured counterclockwise\n"
                + "Decay factor is usually positive and smaller than 1");
        gd.addNumericField("Shear", 45.0, 0, 6, "[deg]");
        gd.addNumericField("Decay", 0.0, 2, 6, "[-]");
        gd.setResizable(false);
        gd.showDialog();
        if (gd.wasCanceled()) // check if user clicked OK or CANCEL
            return false;
        // read GUI elements and store results in private fields order as these
        // methods are called should match to GUI build order
        angle = gd.getNextNumber();
        decay = gd.getNextNumber();
        if (gd.invalidNumber()) { // check if numbers in fields were correct
            IJ.error("Not valid number");
            LOGGER.error("One of the numbers in dialog box is not valid");
            return false;
        }
        return true;
    }

}
