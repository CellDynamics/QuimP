package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import java.util.Arrays;

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
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Main implementation of ImageJ plugin.
 * 
 * Currently supports only 8bit images and stacks.
 * 
 * @author p.baniukiewicz
 * @see LidReconstructor
 */
public class DICLIDReconstruction_ implements PlugInFilter {
    static final Logger LOGGER = LoggerFactory.getLogger(DICLIDReconstruction_.class.getName());
    private LidReconstructor dic;
    private ImagePlus imp;
    /**
     * Filled by {@link #showDialog()}.
     */
    private double angle, decay;
    /**
     * Filled by {@link #showDialog()}.
     */
    private int masksize;
    /**
     * Angle of filtering.
     * 
     * Filled by {@link #showDialog()} as {@link #angle}+90.
     */
    private String prefilterangle;

    /*
     * (non-Javadoc)
     * 
     * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G + NO_CHANGES;
    }

    /**
     * This method is run when current image was accepted and input data were correct.
     * 
     * @param ip is the current slice
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
            dic = new LidReconstructor(imp.getProcessor(), decay, angle, prefilterangle, masksize);
            if (imp.getStack().getSize() == 1) {// if there is no stack we can avoid additional rotation
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
                    IJ.showProgress(s / ((double) stack.getSize() + 1));
                    dic.setIp(stack.getProcessor(s));
                    ret = dic.reconstructionDicLid();
                    resultstack.addSlice(ret);
                }
                IJ.showProgress(1.0);
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
     * @return true if user clicked OK and input data are correct (they are numbers) or return false
     *         otherwise
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("DIC reconstruction");
        gd.addMessage("Reconstruction of DIC image by Line Integrals.\n");
        gd.addNumericField("Shear angle (measured counterclockwise)", 45.0, 0, 6, "[deg]");
        gd.addNumericField("Decay factor (>0)", 0.0, 2, 6, "[-]");
        // gd.addChoice("Angle perpendicular to shear", new String[] { "0", "45", "90", "135" },
        // "45");
        gd.addNumericField("Filter mask size (odd, 0 to switch filtering off)", 0, 0);

        gd.setResizable(false);
        gd.showDialog();
        if (gd.wasCanceled()) // check if user clicked OK or CANCEL
            return false;
        // read GUI elements and store results in private fields order as these
        // methods are called should match to GUI build order
        angle = Math.abs(gd.getNextNumber());
        if (angle >= 360)
            angle -= 360;
        decay = gd.getNextNumber();
        prefilterangle = roundtofull(angle + 90); // filtering angle
        masksize = (int) gd.getNextNumber();

        if (gd.invalidNumber()) { // check if numbers in fields were correct
            IJ.error("One of the numbers in dialog box is not valid");
            return false;
        }
        if (masksize != 0 && masksize % 2 == 0) {
            IJ.error("Mask size must be uneven");
            return false;
        }

        return true;
    }

    /**
     * Round given angle to closest full angle: 0, 45, 90, 135
     * 
     * @param angle input angle in range <0;360)
     * @return full anlge as string. Can be 0, 45, 90, 135
     */
    String roundtofull(double angle) {
        int anglei = (int) angle; // cut fractions
        if (anglei >= 180)
            anglei -= 180;
        // calculate distances between 0, 45, 90, 135, 180
        Integer d[] = new Integer[5];
        d[0] = Math.abs(0 - anglei);
        d[1] = Math.abs(45 - anglei);
        d[2] = Math.abs(90 - anglei);
        d[3] = Math.abs(135 - anglei);
        d[4] = Math.abs(180 - anglei);
        // min distance
        int i = QuimPArrayUtils.minListIndex(Arrays.asList(d));
        i = i > 3 ? 0 : i; // deal with 180 that should be 0
        return Integer.toString(i * 45);
    }

}
