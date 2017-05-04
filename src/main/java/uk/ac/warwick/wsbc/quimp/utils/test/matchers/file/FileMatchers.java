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
}
