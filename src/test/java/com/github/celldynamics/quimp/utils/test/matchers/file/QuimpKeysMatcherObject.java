package com.github.celldynamics.quimp.utils.test.matchers.file;

import static com.github.baniuk.ImageJTestSuite.matchers.json.JsonMatchers.haveSameKeys;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

/**
 * Hamcrest extension.
 * 
 * <p>Compare keys in json output of two objects. Values are ignored.
 * 
 * @author p.baniukiewicz
 * @param <T> QuimP data object.
 *
 */
public class QuimpKeysMatcherObject<T extends IQuimpSerialize>
        extends TypeSafeDiagnosingMatcher<T> {

  private final T expected;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   */
  public QuimpKeysMatcherObject(T expected) {
    this.expected = expected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
   */
  @Override
  public void describeTo(Description description) {
    description.appendText("Same keys in JSon output after serialization.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.TypeSafeDiagnosingMatcher#matchesSafely(java.lang.Object,
   * org.hamcrest.Description)
   */
  @Override
  protected boolean matchesSafely(T compared, Description mismatchDescription) {
    Serializer<T> expectedSer = new Serializer<T>(expected, new QuimpVersion());
    String test = expectedSer.toString();
    Serializer<T> comparedSer = new Serializer<T>(compared, new QuimpVersion());
    String ref = comparedSer.toString();

    Matcher<?> equalsMatcher = haveSameKeys(ref);

    if (!equalsMatcher.matches(test)) {
      mismatchDescription.appendText("was: ").appendDescriptionOf(equalsMatcher);
      return false;
    } else {
      return true;
    }
  }
}
