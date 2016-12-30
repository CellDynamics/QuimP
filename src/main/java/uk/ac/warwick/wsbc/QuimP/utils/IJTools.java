package uk.ac.warwick.wsbc.QuimP.utils;

import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.MacroRunner;

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
     */
    public static ImagePlus getComposite(ImagePlus org, ImagePlus small, ImagePlus big) {
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

}
