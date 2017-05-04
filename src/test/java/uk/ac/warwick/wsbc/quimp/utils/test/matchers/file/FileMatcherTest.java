package uk.ac.warwick.wsbc.quimp.utils.test.matchers.file;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.containsExactText;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.givesSameJson;

import java.io.File;

import org.junit.Test;

import uk.ac.warwick.wsbc.quimp.filesystem.DataContainer;
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

}
