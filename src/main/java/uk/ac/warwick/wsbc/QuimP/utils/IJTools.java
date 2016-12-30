package uk.ac.warwick.wsbc.QuimP.utils;

import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.MacroRunner;
import ij.process.ColorProcessor;

/**
 * Contain IJ based procedures
 * 
 * @author p.baniukiewicz
 *
 */
public class IJTools {

    /**
     * Return composite image created from background cell image and FG and BG pixels.
     * 
     * @param org Original image
     * @param small Foreground mask
     * @param big Background mask
     * @return Composite image
     * 
     *         TODO This does not work every time. Try to look in IJ code instead
     */
    @Deprecated
    public static ImagePlus getCompositeold(ImagePlus org, ImagePlus small, ImagePlus big) {
        big.setTitle("big");
        big.show();
        small.setTitle("small");
        small.show();
        org.setTitle("org");
        org.show();
        // using IJ macro directly
        new MacroRunner("run(\"Merge Channels...\", \"c1=big c3=small c4=org create\");").run();
        ImagePlus ret = WindowManager.getCurrentImage();
        ret.hide();
        return ret;
    }

    /**
     * Return composite image created from background cell image and FG and BG pixels.
     * 
     * @param org Original image
     * @param small Foreground mask
     * @param big Background mask
     * @return Composite image
     */
    public static ColorProcessor getComposite(ImagePlus org, ImagePlus small, ImagePlus big) {
        ColorProcessor ret = new ColorProcessor(org.getWidth(), org.getHeight());

        ret.setChannel(1, small.getProcessor().convertToByteProcessor());
        ret.setChannel(2, big.getProcessor().convertToByteProcessor());
        ret.setChannel(3, org.getProcessor().convertToByteProcessor());
        return ret;
    }

}
