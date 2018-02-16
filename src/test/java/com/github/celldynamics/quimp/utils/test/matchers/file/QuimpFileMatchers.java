package com.github.celldynamics.quimp.utils.test.matchers.file;

import com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

// TODO: Auto-generated Javadoc
/**
 * Extend FileMatcher for QuimP data related matchers.
 * 
 * @author p.baniukiewicz
 * @see com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers
 */
public class QuimpFileMatchers extends FileMatchers {

  /**
   * Compare two serializable objects through their JSon keys. Strictly related to QuimP.
   * 
   * <pre>
   * <code>
   * assertThat(compared, haveSameKeys(expected);
   * </code>
   * </pre>
   *
   * @param <T> the generic type
   * @param expected QuimP object that gives expected keys in json
   * @return instance
   */
  public static <T extends IQuimpSerialize> QuimpKeysMatcherObject<T> haveSameKeys(T expected) {
    return new QuimpKeysMatcherObject<T>(expected);
  }

  /**
   * Compare two serializable objects through their JSon output. Strictly related to QuimP.
   * 
   * <p>Dump both jsons to tmp if test failed.
   * 
   * <pre>
   * <code>
   * assertThat(compared, givesSameJson(expected);
   * </code>
   * </pre>
   *
   * @param <T> the generic type
   * @param expected file that gives expected json
   * @return instance
   */
  public static <T extends IQuimpSerialize> QuimpObjectMatcher<T> givesSameJson(T expected) {
    return new QuimpObjectMatcher<T>(expected);
  }
}
