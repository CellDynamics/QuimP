package com.github.celldynamics.quimp.plugin.randomwalk;

import ij.IJ;
import ij.ImagePlus;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;

/**
 * Provide sets of binary filters.
 * 
 * <p>Returned image is converted to BinaryProcessor and it is copy of input image.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class BinaryFilters {

  /**
   * Filters available in this class.
   * 
   * @author p.baniukiewicz
   *
   */
  public static enum Filters {
    /**
     * Do nothing.
     */
    NONE,
    /**
     * Simple filtering.
     * 
     * @see SimpleMorpho
     */
    SIMPLE,
    /**
     * Small median filter.
     * 
     * @see MedianMorpho
     */
    MEDIAN
  }

  /**
   * Available binary operations.
   * 
   * @author p.baniukiewicz
   * @see BinaryFilters#iterateMorphological(ImageProcessor, MorphoOperations, double)
   */
  public static enum MorphoOperations {
    /**
     * Denote ERODE operation.
     */
    ERODE,
    /**
     * Denote DILATE operation.
     */
    DILATE,
    /**
     * Run IJ open macro.
     */
    IJOPEN
  }

  /**
   * Factory of filters supported by this class.
   * 
   * @param type type of demanded filter
   * @return filter instance
   * @see Filters
   */
  public static BinaryFilters getFilter(Filters type) {
    switch (type) {
      case NONE:
        return new MedianMorpho.EmptyMorpho();
      case SIMPLE:
        return new MedianMorpho.SimpleMorpho();
      case MEDIAN:
        return new MedianMorpho.MedianMorpho();
      default:
        throw new IllegalArgumentException("Unknown filter type");
    }
  }

  /**
   * Filter image using morphological operations.
   * 
   * <p>Input image is copied and converted to BinaryProcessor.
   * 
   * @param input image to process (not modified)
   * @return processed image
   */
  abstract ImageProcessor filter(ImageProcessor input);

  /**
   * Perform median filtering with radius 2 on image.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class MedianMorpho extends BinaryFilters {

    @Override
    ImageProcessor filter(ImageProcessor input) {
      BinaryProcessor bp = getBinaryProcessor(input);
      bp.medianFilter();
      return bp;
    }

  }

  /**
   * Filter image using 1 iteration opening.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class SimpleMorpho extends BinaryFilters {

    /*
     * (non-Javadoc)
     * 
     * @see BinaryFilters#filter(ij.process.ImageProcessor)
     */
    @Override
    ImageProcessor filter(ImageProcessor input) {
      return iterateMorphological(getBinaryProcessor(input), MorphoOperations.IJOPEN, 1);
    }

  }

  /**
   * Dummy filter that does nothing.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class EmptyMorpho extends BinaryFilters {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters#filter(ij.process.
     * ImageProcessor)
     */
    @Override
    ImageProcessor filter(ImageProcessor input) {
      BinaryProcessor ret = getBinaryProcessor(input);
      return ret;
    }

  }

  /**
   * Converts input image to BinaryProcessor duplicating it.
   * 
   * @param input image to convert
   * @return Image converted to BinaryProcessor
   */
  public static BinaryProcessor getBinaryProcessor(ImageProcessor input) {
    return new BinaryProcessor(input.duplicate().convertToByteProcessor());
  }

  /**
   * Run morphological operation on input image.
   * 
   * @param ip Image to process, must be IJ binary (8-bit-image with only 0 and 255)
   * @param oper Operator
   * @param iter number of iterations
   * @return Modified image (copy)
   */
  public static ImageProcessor iterateMorphological(ImageProcessor ip, MorphoOperations oper,
          double iter) {
    BinaryProcessor result;
    switch (oper) {
      case ERODE:
        result = getBinaryProcessor(ip);
        for (int i = 0; i < iter; i++) {
          result.erode(1, 0); // first param influence precision
        }
        break;
      case DILATE:
        result = getBinaryProcessor(ip);
        for (int i = 0; i < iter; i++) {
          result.dilate(1, 0);
        }
        break;
      case IJOPEN:
        // this approach duplicate image internally
        IJ.run("Options...", "iterations=" + iter + " count=1 black do=Nothing");
        IJ.run(new ImagePlus("before", ip), "Open", "");
        result = (BinaryProcessor) ip;
        break;
      default:
        throw new IllegalArgumentException("Binary operation not supported");
    }
    return result;
  }

}
