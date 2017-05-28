package com.github.celldynamics.quimp.utils.test.matchers.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import com.google.gson.Gson;

/**
 * Hamcrest extension.
 * 
 * <p>Compare keys in json output. Values are ignored.
 * 
 * @author p.baniukiewicz
 * @see QuimpKeysMatcherObject
 */
public class QuimpKeysMatcherJson extends TypeSafeDiagnosingMatcher<String> {

  private final String expected;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   */
  public QuimpKeysMatcherJson(String expected) {
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
  protected boolean matchesSafely(String compared, Description mismatchDescription) {
    List<String> keysTest;
    List<String> keysRef;

    try {
      keysTest = getKeysFromJson(expected);
      keysRef = getKeysFromJson(compared);
    } catch (Exception e) {
      mismatchDescription.appendText("can not validate json: " + e.getMessage());
      return false;
    }

    Matcher<?> equalsMatcher = IsIterableContainingInAnyOrder.containsInAnyOrder(keysRef.toArray());
    if (!equalsMatcher.matches(keysTest)) {
      mismatchDescription.appendText("was: ").appendDescriptionOf(equalsMatcher);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Extract keys from json.
   * 
   * @param jsoString json string
   * @return list of keys
   * @throws Exception Exception
   */
  public static List<String> getKeysFromJson(String jsoString) throws Exception {
    Object things = new Gson().fromJson(jsoString, Object.class);
    List<String> keys = new ArrayList<String>();
    collectAllTheKeys(keys, things);
    return keys;
  }

  /**
   * Iterate through json levels and gather keys.
   * 
   * @param keys levels
   * @param o values on levels
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void collectAllTheKeys(List keys, Object o) {
    Collection values = null;
    if (o instanceof Map) {
      Map map = (Map) o;
      keys.addAll(map.keySet()); // collect keys at current level in hierarchy
      values = map.values();
    } else if (o instanceof Collection) {
      values = (Collection) o;
    } else {
      return;
    }

    for (Object value : values) {
      collectAllTheKeys(keys, value);
    }
  }

}
