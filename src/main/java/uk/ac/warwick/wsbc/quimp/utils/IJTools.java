package uk.ac.warwick.wsbc.quimp.utils;

import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.process.ColorProcessor;
import ij.process.LUT;

// TODO: Auto-generated Javadoc
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

    /**
     * 
     * @return 8-bit grayscale LUT
     */
    public static LUT getGrayLut() {
        byte l[] = new byte[256];
        for (int i = 0; i < 256; i++)
            l[i] = (byte) i;
        return new LUT(l, l, l);
    }

}
