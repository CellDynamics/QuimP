package uk.ac.warwick.wsbc.quimp.utils.test.matchers.file;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.containsExactText;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.givesSameJson;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.haveSameKeys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.warwick.wsbc.quimp.filesystem.DataContainer;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.filesystem.OutlinesCollection;

/**
 * Test class for FileMatcher.
 * 
 * @author p.baniukiewicz
 *
 */
public class FileMatcherTest {

  /**
   * Test contains exact text different line.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_differentLine() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/differentLine.ijm");
    assertThat(test, is(not(containsExactText(orginal))));
  }

  /**
   * Test contains exact text same.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_same() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    assertThat(test, containsExactText(orginal));
  }

  /**
   * Test contains exact text both empty.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_bothEmpty() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/empty.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/empty.ijm");
    assertThat(test, containsExactText(orginal));
  }

  /**
   * Test contains exact text orgshort.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_orgshort() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/shorter.ijm");
    assertThat(test, is(not(containsExactText(orginal))));
  }

  /**
   * Test contains exact text shortorg.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_shortorg() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/shorter.ijm");
    assertThat(orginal, is(not(containsExactText(test))));
  }

  /**
   * Test contains exact text empty.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_empty() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/empty.ijm");
    assertThat(orginal, is(not(containsExactText(test))));
  }

  /**
   * Test contains exact text empty 1.
   *
   * @throws Exception the exception
   */
  @Test
  public void testContainsExactText_empty1() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginal.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/empty.ijm");
    assertThat(test, is(not(containsExactText(orginal))));
  }

  /**
   * Test contains exact text no file.
   *
   * @throws Exception the exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testContainsExactText_noFile() throws Exception {
    File orginal = new File("src/test/Resources-static/FileMatcherTest/orginallll.ijm");
    File test = new File("src/test/Resources-static/FileMatcherTest/empty.ijm");
    assertThat(test, is(not(containsExactText(orginal))));
  }

  // ------------------------- GivesSameJson

  /**
   * Test of json matching, same objects.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testGivesSameJson_same() throws Exception {
    DataContainer dt = new DataContainer();
    DataContainer dt1 = new DataContainer();

    assertThat(dt, givesSameJson(dt1));
  }

  /**
   * Test of json matching, other objects.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testGivesSameJson_other() throws Exception {
    DataContainer dt = new DataContainer();
    dt.ECMMState = new OutlinesCollection();
    DataContainer dt1 = new DataContainer();

    assertThat(dt, is(not(givesSameJson(dt1))));
  }

  // ------------------------- HaveSameKeys

  /**
   * Test of keys matching.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testHaveSameKeys_same() throws Exception {
    TestClass test = new TestClass();
    TestClass ref = new TestClass();
    ref.alpha = 13; // val difference
    assertThat(test, haveSameKeys(ref)); // but keys remain the same
    // but different json
    assertThat(test, is(not(givesSameJson(ref))));
    ref.alpha = 0; // set the same value
    assertThat(test, is(givesSameJson(ref))); // and have the same json
  }

  /**
   * Test of keys matching.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testHaveSameKeys_different() throws Exception {
    TestClass test = new TestClass();
    TestClass1 ref = new TestClass1(); // one letter different
    ref.alpha = 13; // val difference
    assertThat(test, is(not(haveSameKeys(ref))));
  }

  class TestClass implements IQuimpSerialize {

    int alpha;
    int beta = 10;
    Nested nest;

    public TestClass() {
      nest = new Nested();
    }

    @Override
    public void beforeSerialize() {
    }

    @Override
    public void afterSerialize() throws Exception {
    }

    class Nested {
      List<String> list = new ArrayList<>();
      double bravo = 3.14;
    }
  }

  class TestClass1 implements IQuimpSerialize {

    int alpha;
    int beta = 10;
    Nested nest;

    public TestClass1() {
      nest = new Nested();
    }

    @Override
    public void beforeSerialize() {
    }

    @Override
    public void afterSerialize() throws Exception {
    }

    class Nested {
      List<String> list = new ArrayList<>();
      double bravO = 3.14; // here
    }
  }
}
