package uk.ac.warwick.wsbc.QuimP.utils;

import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.process.ColorProcessor;

/**
 * Contain IJ based procedures.
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
        ImagePlus ret = RGBStackMerge
                .mergeChannels(new ImagePlus[] { small, big, null, org, null, null, null }, false);
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
    public static ColorProcessor getCompositeRGB(ImagePlus org, ImagePlus small, ImagePlus big) {
        ColorProcessor ret = new ColorProcessor(org.getWidth(), org.getHeight());

        ret.setChannel(1, small.getProcessor().convertToByteProcessor());
        ret.setChannel(2, big.getProcessor().convertToByteProcessor());
        ret.setChannel(3, org.getProcessor().convertToByteProcessor());
        return ret;
    }

}
