package uk.ac.warwick.wsbc.quimp.plugin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.process.ImageProcessor;

/**
 * Class implementing extra functionalities for ij.ImageProcessor
 *
 * <p>Check {@link #extendImageBeforeRotation(ImageProcessor, double)} for possible problems
 * 
 * @author p.baniukiewicz
 */
public class ImageProcessorPlus {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessorPlus.class.getName());

  /**
   * Main constructor.
   */
  public ImageProcessorPlus() {
  }

  /**
   * Add borders around image to prevent cropping during rotating.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Replaces original image and may not preserve all its attributes
   * 
   * @param ip ImageProcessor to be extended
   * @param angle Angle to be image rotated
   * @return copy of ip extended to size that allows to rotate it by angle without clipping
   */
  public ImageProcessor extendImageBeforeRotation(ImageProcessor ip, double angle) {
    ImageProcessor ret;
    int width = ip.getWidth();
    int height = ip.getHeight();
    // get bounding box after rotation
    RectangleBox rb = new RectangleBox(width, height);
    rb.rotateBoundingBox(angle);
    int newWidth = (int) Math.round(rb.getWidth());
    int newHeight = (int) Math.round(rb.getHeight());
    if (newWidth < width) {
      newWidth = width;
    }
    if (newHeight < height) {
      newHeight = height;
    }
    // create new array resized
    ret = ip.createProcessor(newWidth, newHeight);
    // get current background - borders will have the same value
    ret.setValue(ip.getBackgroundValue()); // set current fill value for extended image
    ret.setBackgroundValue(ip.getBackgroundValue()); // set the same background as in original image
    ret.fill(); // change color of extended image
    ret.setInterpolationMethod(ip.getInterpolationMethod());
    // insert original image into extended
    ret.insert(ip, (newWidth - ip.getWidth()) / 2, (newHeight - ip.getHeight()) / 2);
    ret.resetRoi();
    return ret; // assign extended into current
  }

  /**
   * Rotate image by specified angle keeping correct rotation direction.
   * 
   * @param ip ImageProcessor to be rotated
   * @param angle Angle of rotation in anti-clockwise direction
   * @param addBorders if true rotates with extension, false use standard rotation with clipping
   * @return rotated ip that is a copy of ip whenaddBorders is true or reference when addBorders
   *         is false
   */
  public ImageProcessor rotate(ImageProcessor ip, double angle, boolean addBorders) {
    ImageProcessor ret;
    if (addBorders) {
      ret = extendImageBeforeRotation(ip, angle);
    } else {
      ret = ip;
    }
    ret.rotate(angle);
    return ret;
  }

  /**
   * Crop image.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Modifies current object
   * 
   * @param ip ImageProcessor to be cropped
   * @param luX Left upper corner x coordinate
   * @param luY Left upper corner y coordinate
   * @param width Width of clipped area
   * @param height Height of clipped area
   * @return Clipped image
   */
  public ImageProcessor crop(ImageProcessor ip, int luX, int luY, int width, int height) {
    ip.setRoi(luX, luY, width, height);
    ip = ip.crop();
    ip.resetRoi();
    return ip;
  }

  /**
   * Crop image.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Modifies current object Designed to use with cooperation with
   * extendImageBeforeRotation(ImageProcessor,double).
   * 
   * <p>Assumes that cropping area is centered in source image
   * 
   * @param ip ImageProcessor to be cropped
   * @param width Width of clipped area
   * @param height Height of clipped area
   * @return Clipped image
   */
  public ImageProcessor cropImageAfterRotation(ImageProcessor ip, int width, int height) {
    int sw = (ip.getWidth() - width) / 2;
    int sh = (ip.getHeight() - height) / 2;
    if (sw < 0) {
      sw = 0;
    }
    if (sh < 0) {
      sh = 0;
    }
    ip.setRoi(sw, sh, width, height);
    ip = ip.crop();
    ip.resetRoi();
    return ip;
  }

  /**
   * Perform running mean on image using convolution.
   * 
   * @param ip ImageProcessor
   * @param prefilterangle angle as k*45
   * @param masksize odd size, 0 will skip processing
   * @see GenerateKernel
   */
  public void runningMean(ImageProcessor ip, String prefilterangle, int masksize) {
    LOGGER.debug("Convolving: " + prefilterangle + " " + masksize);
    if (masksize == 0) {
      return;
    }
    float[] kernel = new GenerateKernel(masksize).generateKernel(prefilterangle);
    ip.convolve(kernel, masksize, masksize);
  }

  /**
   * Support generating kernels for running mean.
   * 
   * @author p.baniukiewicz
   *
   */
  class GenerateKernel {
    private int size;

    /**
     * 
     * @param size Size of the kernel assuming its rectangularity.
     */
    public GenerateKernel(int size) {
      this.size = size;
    }

    /**
     * Generate convolution kernel.
     * 
     * <p>Returned kernel is compatible with ij.process.ImageProcessor.convolve(float[], int, int)
     * 
     * @param option Option can be 0, 45, 90, 135 as string.
     * @return 1D array as row ordered matrix. The kernel contains 1 on diagonal and it is
     *         normalised.
     */
    public float[] generateKernel(String option) {
      float[] ret = new float[size * size];
      int mid = size / 2; // middle element (0 indexed)
      switch (option) {
        case "0": // row in middle
          for (int i = 0; i < size; i++) {
            ret[sub2lin(mid, i)] = 1.0f;
          }
          break;
        case "135":
          for (int i = 0; i < size; i++) {
            ret[sub2lin(i, i)] = 1.0f;
          }
          break;
        case "90":
          for (int i = 0; i < size; i++) {
            ret[sub2lin(i, mid)] = 1.0f;
          }
          break;
        case "45":
          for (int i = 0; i < size; i++) {
            ret[sub2lin(i, size - 1 - i)] = 1.0f;
          }
          break;
        default:
          throw new UnsupportedOperationException("Unsupported mask angle.");
      }

      return normalise(ret);
    }

    /**
     * Convert subscript indexes to linear.
     * 
     * @param row row counted from 0
     * @param col column counted from 0
     * @return Linear index based on row and col position
     */
    private int sub2lin(int row, int col) {
      return row * size + col;
    }

    /**
     * Normalise the kernel.
     * 
     * <p>Divide every element by sum of elements.
     * 
     * @param kernel kernel to normalise
     * @return normalised kernel (copy)
     */
    private float[] normalise(float[] kernel) {
      float s = 0;
      for (int i = 0; i < kernel.length; i++) {
        s += kernel[i];
      }
      float[] ret = new float[kernel.length];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = kernel[i] / s;
      }
      return ret;
    }
  }
}
