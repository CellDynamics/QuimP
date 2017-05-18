package uk.ac.warwick.wsbc.quimp.utils.test.matchers.file;

import java.io.File;

import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;

/**
 * @author p.baniukiewicz
 *
 */
public class FileMatchers {

  /**
   * Deliver instance of this class.
   * 
   * <pre>
   * <code>
   * assertThat(compared, containsExactText(expected);
   * </code>
   * </pre>
   * 
   * @param expected expected file
   * @return instance
   */
  public static FileMatcher containsExactText(File expected) {
    return new FileMatcher(expected);
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
   * @param expected file that gives expected json
   * @return instance
   */
  public static <T extends IQuimpSerialize> QuimpObjectMatcher<T> givesSameJson(T expected) {
    return new QuimpObjectMatcher<T>(expected);
  }

  /**
   * Compare two serializable objects through their JSon keys. Strictly related to QuimP.
   * 
   * <pre>
   * <code>
   * assertThat(compared, haveSameKeys(expected);
   * </code>
   * </pre>
   * 
   * @param expected QuimP object that gives expected keys in json
   * @return instance
   */
  public static <T extends IQuimpSerialize> QuimpKeysMatcherObject<T> haveSameKeys(T expected) {
    return new QuimpKeysMatcherObject<T>(expected);
  }

  /**
   * Compare two serializable objects through their JSon keys. Strictly related to QuimP.
   * 
   * <pre>
   * <code>
   * assertThat(compared, haveSameKeys(expected);
   * </code>
   * </pre>
   * 
   * @param expected json string that gives expected keys in json
   * @return instance
   */
  public static QuimpKeysMatcherJson haveSameKeys(String expected) {
    return new QuimpKeysMatcherJson(expected);
  }
}
