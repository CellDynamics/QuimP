package com.github.celldynamics.quimp.utils.test.matchers.file;

import java.io.File;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

// TODO: Auto-generated Javadoc
/**
 * Hamcrest extension.
 * 
 * <p>Compare json output of two QuimP data objects.
 *
 * @author p.baniukiewicz
 * @param <T> the generic type
 */
public class QuimpObjectMatcher<T extends IQuimpSerialize> extends TypeSafeDiagnosingMatcher<T> {

  /** The expected. */
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
      try {
        String tmpfile = File.createTempFile("QuimpObjectMatcher_cmp", "", null).toString();
        comparedSer.setPretty();
        comparedSer.save(tmpfile);

        String expfile = File.createTempFile("QuimpObjectMatcher_exp", "", null).toString();
        expectedSer.setPretty();
        expectedSer.save(expfile);
        mismatchDescription.appendText("Saved in " + tmpfile + " and " + expfile);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    } else {
      return true;
    }
  }

}
