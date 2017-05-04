package uk.ac.warwick.wsbc.quimp.utils.test.matchers.file;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import uk.ac.warwick.wsbc.quimp.QuimpVersion;
import uk.ac.warwick.wsbc.quimp.Serializer;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;

/**
 * Hamcrest extension.
 * 
 * <p>Compare json output of two QuimP data objects.
 * 
 * @author p.baniukiewicz
 * @param <T>
 *
 */
public class QuimpObjectMatcher<T extends IQuimpSerialize> extends TypeSafeDiagnosingMatcher<T> {

  private final T expected;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   */
  public QuimpObjectMatcher(T expected) {
    this.expected = expected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
   */
  @Override
  public void describeTo(Description description) {
    description.appendText("Same JSon output after serialization.");

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.TypeSafeDiagnosingMatcher#matchesSafely(java.lang.Object,
   * org.hamcrest.Description)
   */
  @Override
  protected boolean matchesSafely(T compared, Description mismatchDescription) {
    // save tmp only compared objects - most objects do not have equals() and transient objects are
    // not restored on load of reference so comparison must be on json level.
    Serializer<T> expectedSer = new Serializer<T>(expected, new QuimpVersion());
    expectedSer.createdOn = ""; // erase
    String test = expectedSer.toString();
    Serializer<T> comparedSer = new Serializer<T>(compared, new QuimpVersion());
    comparedSer.createdOn = "";
    String ref = comparedSer.toString();

    Matcher<?> equalsMatcher = CoreMatchers.equalTo(ref);
    if (!equalsMatcher.matches(test)) {
      mismatchDescription.appendText("was: ").appendDescriptionOf(equalsMatcher);
      return false;
    } else {
      return true;
    }
  }

}
