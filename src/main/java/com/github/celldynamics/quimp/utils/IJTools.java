package com.github.celldynamics.quimp.utils;

import java.util.concurrent.TimeUnit;

import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.RGBStackMerge;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

/**
 * Contain IJ based procedures.
 * 
 * @author p.baniukiewicz
 *
 */
public class IJTools {

  /**
   * Time to wait after exit.
   * 
   * @see #exitIj(ImageJ)
   */
  public static int WAIT_TIME = 2;

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
   * Return RGB image created from R, G, B 8-bit images.
   * 
   * <p>Slices can be also grayscale images serve as background.
   * 
   * @param red red slice
   * @param green green slice
   * @param blue blue slice
   * @return Composite image
   */
  public static ColorProcessor getRGB(ImagePlus red, ImagePlus green, ImagePlus blue) {
    return getRGB(red.getProcessor(), green.getProcessor(), blue.getProcessor());
  }

  /**
   * Return RGB image created from R, G, B 8-bit images.
   * 
   * <p>Slices can be also grayscale images serve as background.
   * 
   * @param red red slice
   * @param green green slice
   * @param blue blue slice
   * @return Composite image
   */
  public static ColorProcessor getRGB(ImageProcessor red, ImageProcessor green,
          ImageProcessor blue) {
    ColorProcessor ret = new ColorProcessor(red.getWidth(), red.getHeight());

    ret.setChannel(1, red.convertToByteProcessor());
    ret.setChannel(2, green.convertToByteProcessor());
    ret.setChannel(3, blue.convertToByteProcessor());
    return ret;
  }

  /**
   * Convert LUT to grayscale.
   * 
   * @return 8-bit grayscale LUT
   */
  public static LUT getGrayLut() {
    byte[] l = new byte[256];
    for (int i = 0; i < 256; i++) {
      l[i] = (byte) i;
    }
    return new LUT(l, l, l);
  }

  /**
   * Close all images without saving.
   */
  public static void closeAllImages() {
    int[] img = WindowManager.getIDList();
    if (img != null) {
      for (int s : img) {
        ImagePlus id = WindowManager.getImage(s);
        if (id != null) {
          id.changes = false;
        }
      }
    }
    WindowManager.closeAllWindows();
  }

  /**
   * Exit IJ and wait time to accomplish.
   * 
   * @param ij ImageJ app object
   * 
   * @throws InterruptedException InterruptedException
   */
  public static void exitIj(ImageJ ij) throws InterruptedException {
    if (ij != null) {
      ij.quit();
      TimeUnit.SECONDS.sleep(WAIT_TIME);
    }
  }

}
