package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Test class for StringParser
 * 
 * @author p.baniukiewicz
 * @date 24 Feb 2016
 *
 */
public class StringParserTest {

    private static final Logger LOGGER =
            LogManager.getLogger(StringParserTest.class.getName());

    @Test
    public void testGetNumofParam() throws Exception {
        String s = "1, 2,3.1";
        assertEquals(3, StringParser.getNumofParam(s));
    }

    /**
     * @test Test method for GetNumofParam
     * @pre valid list of parameters, more spaces
     * @post number of parameters in list
     */
    @Test
    public void testGetNumofParam_1() throws Exception {
        String s = "1,   2, 3.1";
        assertEquals(3, StringParser.getNumofParam(s));
    }

    /**
     * @test Test method for GetNumofParam
     * @pre valid list of parameters, one element
     * @post number of parameters in list
     */
    @Test
    public void testGetNumofParam_2() throws Exception {
        String s = "1";
        assertEquals(1, StringParser.getNumofParam(s));
    }

    /**
     * @test Test method for GetNumofParam
     * @pre invalid list of parameters
     * @post 0
     */
    @Test
    public void testGetNumofParam_3() throws Exception {
        String s = "";
        assertEquals(0, StringParser.getNumofParam(s));
    }

    /**
     * @test Test method for GetParams
     * @pre invalid list of parameters
     * @post empty array
     */
    @Test
    public void testGetParams() throws Exception {
        String s = "";
        String ret[] = StringParser.getParams(s);
        assertTrue(ret.length == 0);
    }

    /**
     * @test Test method for GetParams
     * @pre valid list of parameters
     * @post array of substrings
     */
    @Test
    public void testGetParams_1() throws Exception {
        String s = "1,   2,"
                + " 3.1";
        String[] ret = StringParser.getParams(s);
        String[] exp = { "1", "2", "3.1" };
        assertArrayEquals(exp, ret);
    }

    /**
     * @test Test method for GetParams
     * @pre invalid list of parameters
     * @post print of array, one field is empty
     */
    @Test
    public void testGetParams_2() throws Exception {
        String s = "1,,  2,"
                + " 3.1";
        String[] ret = StringParser.getParams(s);
        String[] exp = { "1", "2", "3.1" };
        LOGGER.debug(Arrays.toString(ret));
    }

}
