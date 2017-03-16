package uk.ac.warwick.wsbc.quimp.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class QuimpToolsCollectionTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QuimpToolsCollectionTest.class.getName());

  /**
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception on error
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection#getQuimPversion()}.
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

}
