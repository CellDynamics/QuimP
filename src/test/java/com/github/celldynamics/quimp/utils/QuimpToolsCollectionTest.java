package com.github.celldynamics.quimp.utils;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimP;

/**
 * The Class QuimpToolsCollectionTest.
 *
 * @author p.baniukiewicz
 */
public class QuimpToolsCollectionTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpToolsCollectionTest.class.getName());

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.utils.QuimpToolsCollection#getQuimPversion()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetFormattedQuimPversion() throws Exception {
    LOGGER.debug(new QuimpToolsCollection().getQuimPversion());
  }

  /**
   * Test method for
   * {@link QuimpToolsCollection#implementationDateConverter(java.lang.String)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testImplementationDateConverter() throws Exception {
    String date = "2017-02-24T08:55:44+0000";
    String ret = QuimpToolsCollection.implementationDateConverter(date);
    assertEquals("2017-02-24 08:55:44", ret);
  }

  /**
   * Test of {@link QuimpToolsCollection#stringWrap(String, int, String)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testStringWrapStringIntString() throws Exception {
    String str = "Long string with \\home\\baniuk\\Documents\\"
            + "Repos/QuimP-env\\QuimP\\src\\test\\Resources-static\\"
            + "FormatConverter/templates\\Paqp-Q to QCONF missing maps\\"
            + "test_0.paQP should be broken";
    String ret = QuimpToolsCollection.stringWrap(str, 20);
    LOGGER.debug(ret);
    assertEquals(10, StringUtils.countMatches(ret, '\n'));

    str = "Long string with /home/baniuk/Documents/"
            + "Repos/QuimP-env/QuimP/src/test/Resources-static/"
            + "FormatConverter/templates\\Paqp-Q to QCONF missing maps/"
            + "test_0.paQP should be broken";
    ret = QuimpToolsCollection.stringWrap(str, 20);
    assertEquals(10, StringUtils.countMatches(ret, '\n'));

    str = "Long string with /home/baniuk/Documents/\n"
            + "Repos/QuimP-env/QuimP/src/test/Resources-static/\n"
            + "FormatConverter/templates\\Paqp-Q to QCONF missing maps/\n"
            + "test_0.paQP should be broken";
    ret = QuimpToolsCollection.stringWrap(str, 20);
    LOGGER.debug(ret);
    assertEquals(10, StringUtils.countMatches(ret, '\n'));
  }

  /**
   * Test of {@link QuimpToolsCollection#stringWrap(String, int, String)}.
   * 
   * <p>Test if new line symbols are preserved.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testStringWrapStringInt() throws Exception {
    String str = "Seed pixels are empty, check if:\n"
            + "- correct colors were used\n- all slices have been seeded"
            + " (if stacked seed is used)\n" + "- Shrink/expand parameters are not too big.";
    String ret = QuimpToolsCollection.stringWrap(str, QuimP.LINE_WRAP);
    LOGGER.debug(ret);
    assertEquals(3, StringUtils.countMatches(ret, '\n'));
  }

}
