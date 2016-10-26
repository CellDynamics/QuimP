package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import uk.ac.warwick.wsbc.QuimP.registration.Registration;

/**
 * Main implementation of ImageJ plugin
 * 
 * Currently supports only 8bit images and stacks.
 * 
 * @author p.baniukiewicz
 * @see LidReconstructor for algorithm details *
 */
public class DICLIDReconstruction_ implements PlugInFilter {
    static final Logger LOGGER = LoggerFactory.getLogger(DICLIDReconstruction_.class.getName());
    private LidReconstructor dic;
    private ImagePlus imp;
    private double angle, decay;

    /**
     * This method gets called by ImageJ/Fiji to determine whether the current image is of an
     * appropriate type.
     * 
     * @param arg can be specified in plugins.config
     * @param imp is the currently opened image
     * @return Combination of flags determining supported formats: \li DOES_8G - plugin supports
     *         8bit grayscale images
     * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G + NO_CHANGES;
    }

    /**
     * This method is run when current image was accepted and input data were correct
     * 
     * @param ip is the current slice
     * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
     */
    @Override
    public void run(ImageProcessor ip) {
        // validate registered user
        new Registration(IJ.getInstance(), "QuimP Registration");
        ImageProcessor ret;
        if (!showDialog())
            return; // if user clicked Cancel or data were not valid
        try {
            // create result as separate 16bit image
            ImagePlus result = new ImagePlus("DIC_" + imp.getTitle(),
                    new ShortProcessor(ip.getWidth(), ip.getHeight()));
            dic = new LidReconstructor(imp, decay, angle);
            if (imp.getNSlices() == 1) {// if there is no stack we can avoid additional rotation
                                        // here (see DICReconstruction documentation)
                IJ.showProgress(0.0);
                ret = dic.reconstructionDicLid();
                result.getProcessor().setPixels(ret.getPixels());
                IJ.showProgress(1.0);
                result.show();
            } else { // we have stack. Process slice by slice
                // create result stack
                ImageStack resultstack = new ImageStack(imp.getWidth(), imp.getHeight());
                ImageStack stack = imp.getStack();
                for (int s = 1; s <= stack.getSize(); s++) {
                    IJ.showProgress(s / (double) stack.getSize());
                    dic.setIp(stack.getProcessor(s));
                    ret = dic.reconstructionDicLid();
                    resultstack.addSlice(ret);
                }
                // pack in ImagePlus
                new ImagePlus("DIC_" + imp.getTitle(), resultstack).show();
            }
        } catch (DicException e) { // exception can be thrown if input image is 16-bit and saturated
            IJ.log(e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with DIC reconstruction: " + e.getMessage());
        } finally {
            imp.updateAndDraw();
        }
    }

    /**
     * Shows user dialog and check conditions.
     * 
     * @return \c true if user clicked \b OK and input data are correct (they are numbers) or return
     *         \c false otherwise
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
