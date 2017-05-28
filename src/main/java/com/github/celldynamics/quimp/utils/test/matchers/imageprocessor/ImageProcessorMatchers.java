package com.github.celldynamics.quimp.utils.test.matchers.imageprocessor;

import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class ImageProcessorMatchers {

  /**
   * Compare pixels in ImageProcessors.
   * 
   * <p>Pixels are casted do double before comparison so it is possible to compare ImageProcessors
   * from different classes.
   * 
   * <pre>
   * <code>
   * assertThat(compared, hasSamePixels(expected);
   * </code>
   * </pre>
   * 
   * @param expected expected file
   * @return instance
   */
  public static PixelMatcher hasSamePixels(ImageProcessor expected) {
    return new PixelMatcher(expected);
  }

  /**
   * Compare pixels in ImageProcessors.
   * 
   * <p>Pixels are casted do double before comparison so it is possible to compare ImageProcessors
   * from different classes. It trigger if number of different pixels is larger than given
   * threshold.
   * 
   * <pre>
   * <code>
   * assertThat(compared, hasLessDifferentPixelsThan(expected);
   * </code>
   * </pre>
   * 
   * @param expected expected file
   * @param maxDifferent maximal number of pixels that may differ
   * @return instance
   */
  public static PixelNumberMatcher hasLessDifferentPixelsThan(ImageProcessor expected,
          int maxDifferent) {
    return new PixelNumberMatcher(expected, maxDifferent);
  }

}
