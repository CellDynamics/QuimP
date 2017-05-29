package com.github.celldynamics.quimp.utils.test.matchers.imageprocessor;

import static com.github.celldynamics.quimp.utils.test.matchers.imageprocessor.ImageProcessorMatchers.hasLessDifferentPixelsThan;
import static com.github.celldynamics.quimp.utils.test.matchers.imageprocessor.ImageProcessorMatchers.hasSamePixels;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class ImageProcessorMatchersTest {

  /**
   * The same processors.
   * 
   * @throws Exception on error
   */
  @Test
  public void testHasSamePixels_same() throws Exception {
    ImageProcessor expected = new ByteProcessor(5, 5);
    expected.putPixel(3, 2, 120);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    assertThat(test, is(hasSamePixels(expected)));
  }

  /**
   * The same processors but other values.
   * 
   * @throws Exception on error
   */
  @Test
  public void testHasSamePixels_otherval() throws Exception {
    ImageProcessor expected = new ByteProcessor(5, 5);
    expected.putPixel(3, 2, 120);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 121);
    assertThat(test, is(not(hasSamePixels(expected))));
  }

  /**
   * The same processors but other sizes.
   * 
   * @throws Exception on error
   */
  @Test
  public void testHasSamePixels_othersize() throws Exception {
    ImageProcessor expected = new ByteProcessor(5, 6);
    expected.putPixel(3, 2, 120);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    assertThat(test, is(not(hasSamePixels(expected))));
  }

  /**
   * The other processors but same values.
   * 
   * @throws Exception on error
   */
  @Test
  public void testHasSamePixels_othertypes() throws Exception {
    ImageProcessor expected = new FloatProcessor(5, 5);
    expected.putPixelValue(3, 2, 120);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    assertThat(test, is(hasSamePixels(expected)));
  }

  /**
   * The other processors same values.
   * 
   * @throws Exception on error
   */
  @Test
  public void testHasSamePixels_othertypes1() throws Exception {
    ImageProcessor expected = new FloatProcessor(5, 5);
    expected.putPixelValue(3, 2, 120);
    ImageProcessor test = new ShortProcessor(5, 5);
    test.putPixel(3, 2, 120);
    assertThat(test, is(hasSamePixels(expected)));
  }

  // -----------------------------------------------

  /**
   * The same processors.
   * 
   * @throws Exception on error
   */
  @Test
  public void testhasLessDifferentPixelsThan_same() throws Exception {
    ImageProcessor expected = new ByteProcessor(5, 5);
    expected.putPixel(3, 2, 120);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    assertThat(test, is(hasLessDifferentPixelsThan(expected, 3)));
  }

  /**
   * The different processors, more different pixels than threshold.
   * 
   * @throws Exception on error
   */
  @Test
  public void testhasLessDifferentPixelsThan_different1() throws Exception {
    ImageProcessor expected = new FloatProcessor(5, 5);
    expected.putPixelValue(3, 2, 120);
    expected.putPixelValue(1, 1, 12);
    expected.putPixelValue(2, 2, 1);
    expected.putPixelValue(3, 3, 10);
    expected.putPixelValue(4, 4, 1.5);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    expected.putPixelValue(3, 2, 120);
    expected.putPixelValue(1, 1, 120);
    expected.putPixelValue(2, 2, 10);
    expected.putPixelValue(3, 3, 100);
    expected.putPixelValue(4, 4, 1);
    assertThat(test, is(not(hasLessDifferentPixelsThan(expected, 3))));
  }

  /**
   * The different processors, equal different pixels to threshold.
   * 
   * @throws Exception on error
   */
  @Test
  public void testhasLessDifferentPixelsThan_different2() throws Exception {
    ImageProcessor expected = new FloatProcessor(5, 5);
    expected.putPixelValue(3, 2, 120);
    expected.putPixelValue(1, 1, 12);
    expected.putPixelValue(2, 2, 1);
    expected.putPixelValue(3, 3, 10);
    ImageProcessor test = new ByteProcessor(5, 5);
    test.putPixel(3, 2, 120);
    expected.putPixelValue(3, 2, 120);
    expected.putPixelValue(1, 1, 120);
    expected.putPixelValue(2, 2, 10);
    expected.putPixelValue(3, 3, 100);
    assertThat(test, is(hasLessDifferentPixelsThan(expected, 3)));
  }

}
