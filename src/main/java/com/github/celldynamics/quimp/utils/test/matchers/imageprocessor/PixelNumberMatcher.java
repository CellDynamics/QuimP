package com.github.celldynamics.quimp.utils.test.matchers.imageprocessor;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.process.ImageProcessor;

/**
 * Compares pixels in ImageProcessors, trigger if given number of them is different.
 * 
 * @author p.baniukiewicz
 *
 */
public class PixelNumberMatcher extends TypeSafeDiagnosingMatcher<ImageProcessor> {

  private final ImageProcessor expected;
  private final int numOfDifferent;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   * @param numOfDifferent number of pixels that must differ to trigger
   */
  public PixelNumberMatcher(ImageProcessor expected, int numOfDifferent) {
    this.expected = expected;
    this.numOfDifferent = numOfDifferent;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("Number of different pixels is less than.");

  }

  @Override
  protected boolean matchesSafely(ImageProcessor compared, Description mismatchDescription) {
    Double[] refCompared = QuimPArrayUtils.object2double(QuimPArrayUtils.castToNumber(compared));
    Double[] refExpected = QuimPArrayUtils.object2double(QuimPArrayUtils.castToNumber(expected));

    int different = 0;
    if (refCompared.length != refExpected.length) {
      mismatchDescription.appendText("was: ").appendText("Differenr number of pixels");
      return false;
    }
    for (int i = 0; i < refCompared.length; i++) {
      if (refCompared[i].compareTo(refExpected[i]) != 0) {
        different++;
      }
      if (different > numOfDifferent) {
        mismatchDescription.appendText("was: ")
                .appendText("Differenr number of pixels exceeded level of " + numOfDifferent);
        return false;
      }
    }

    return true;
  }

}
