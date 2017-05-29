package com.github.celldynamics.quimp.utils.test.matchers.imageprocessor;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.process.ImageProcessor;

/**
 * Compares pixels. Pixels are casted to double.
 * 
 * <p>Makes it possible to compare images from different classes.
 * 
 * @author p.baniukiewicz
 *
 */
class PixelMatcher extends TypeSafeDiagnosingMatcher<ImageProcessor> {

  private final ImageProcessor expected;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   */
  public PixelMatcher(ImageProcessor expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(Description arg0) {
    arg0.appendText("Same pixels intensities.");

  }

  @Override
  protected boolean matchesSafely(ImageProcessor compared, Description arg1) {
    Double[] refCompared = QuimPArrayUtils.object2double(QuimPArrayUtils.castToNumber(compared));
    Double[] refExpected = QuimPArrayUtils.object2double(QuimPArrayUtils.castToNumber(expected));
    Matcher<?> isMatcher = CoreMatchers.is(refExpected);
    if (!isMatcher.matches(refCompared)) {
      arg1.appendText("was: ").appendDescriptionOf(isMatcher);
      return false;
    }
    return true;
  }

}