package com.github.celldynamics.quimp.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Test class for StringParser
 * 
 * @author p.baniukiewicz
 *
 */
public class StringParserTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(StringParserTest.class.getName());

  /**
   * Test get numof param.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetNumofParam() throws Exception {
    String s = "1, 2,3.1";
    assertEquals(3, StringParser.getNumofParam(s, ','));
  }

  /**
   * Test method for GetNumofParam.
   * 
   * <p>Pre: valid list of parameters, more spaces
   * 
   * <p>Post: number of parameters in list
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetNumofParam_1() throws Exception {
    String s = "1,   2, 3.1";
    assertEquals(3, StringParser.getNumofParam(s, ','));
  }

  /**
   * Test method for GetNumofParam.
   * 
   * <p>Pre: valid list of parameters, one element
   * 
   * <p>Post: number of parameters in list
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetNumofParam_2() throws Exception {
    String s = "1";
    assertEquals(1, StringParser.getNumofParam(s, ','));
  }

  /**
   * Test method for GetNumofParam.
   * 
   * <p>Pre: pre invalid list of parameters
   * 
   * <p>Post: 0
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetNumofParam_3() throws Exception {
    String s = "";
    assertEquals(0, StringParser.getNumofParam(s, ','));
  }

  /**
   * Test method for GetParams.
   * 
   * <p>Pre:pre invalid list of parameters
   * 
   * <p>Post: empty array
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetParams() throws Exception {
    String s = "";
    String[] ret = StringParser.getParams(s, ',');
    assertTrue(ret.length == 0);
  }

  /**
   * Test method for GetParams.
   * 
   * <p>Pre: valid list of parameters
   * 
   * <p>Post: array of substrings
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetParams_1() throws Exception {
    String s = "1,   2," + " 3.1";
    String[] ret = StringParser.getParams(s, ',');
    String[] exp = { "1", "2", "3.1" };
    assertArrayEquals(exp, ret);
  }

  /**
   * Test method for GetParams.
   * 
   * <p>Pre: valid list of parameters
   * 
   * <p>Post: array of substrings
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetParams_3() throws Exception {
    String s = "1,   2," + " 3.1, With Space ";
    String[] ret = StringParser.getParams(s, ',');
    String[] exp = { "1", "2", "3.1", "With Space" };
    assertArrayEquals(exp, ret);
  }

  /**
   * Test method for GetParams.
   * 
   * <p>Pre: invalid list of parameters
   * 
   * <p>Post: print of array, one field is empty
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetParams_2() throws Exception {
    String s = "1,,  2," + " 3.1";
    String[] ret = StringParser.getParams(s, ',');
    String[] exp = { "1", "", "2", "3.1" };
    assertArrayEquals(exp, ret);
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.utils.StringParser#removeSpaces(java.lang.String)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRemoveSpaces() throws Exception {
    String test;
    String exp;

    test = " 2";
    exp = "2";
    assertEquals(exp, StringParser.removeSpaces(test));

    test = " word ";
    exp = "word";
    assertEquals(exp, StringParser.removeSpaces(test));

    test = " two words ";
    exp = "two words";
    assertEquals(exp, StringParser.removeSpaces(test));

  }

}
