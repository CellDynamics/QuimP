package uk.ac.warwick.wsbc.QuimP.plugin.utils;

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
        assertEquals(3, StringParser.getNumofParam(s));
    }

    /**
     * Test method for GetNumofParam
     * 
     * Pre: valid list of parameters, more spaces
     * 
     * Post: number of parameters in list
     * 
     * @throws Exception
     */
    @Test
    public void testGetNumofParam_1() throws Exception {
        String s = "1,   2, 3.1";
        assertEquals(3, StringParser.getNumofParam(s));
    }

    /**
     * Test method for GetNumofParam
     * 
     * Pre: valid list of parameters, one element
     * 
     * Post: number of parameters in list
     * 
     * @throws Exception
     */
    @Test
    public void testGetNumofParam_2() throws Exception {
        String s = "1";
        assertEquals(1, StringParser.getNumofParam(s));
    }

    /**
     * Test method for GetNumofParam
     * 
     * Pre: pre invalid list of parameters
     * 
     * Post: 0
     * 
     * @throws Exception
     */
    @Test
    public void testGetNumofParam_3() throws Exception {
        String s = "";
        assertEquals(0, StringParser.getNumofParam(s));
    }

    /**
     * Test method for GetParams
     * 
     * Pre:pre invalid list of parameters
     * 
     * Post: empty array
     * 
     * @throws Exception
     */
    @Test
    public void testGetParams() throws Exception {
        String s = "";
        String ret[] = StringParser.getParams(s);
        assertTrue(ret.length == 0);
    }

    /**
     * Test method for GetParams
     * 
     * Pre: valid list of parameters
     * 
     * Post: array of substrings
     * 
     * @throws Exception
     */
    @Test
    public void testGetParams_1() throws Exception {
        String s = "1,   2," + " 3.1";
        String[] ret = StringParser.getParams(s);
        String[] exp = { "1", "2", "3.1" };
        assertArrayEquals(exp, ret);
    }

    /**
     * Test method for GetParams
     * 
     * Pre: invalid list of parameters
     * 
     * Post: print of array, one field is empty
     * 
     * @throws Exception
     */
    @Test
    public void testGetParams_2() throws Exception {
        String s = "1,,  2," + " 3.1";
        String[] ret = StringParser.getParams(s);
        String[] exp = { "1", "", "2", "3.1" };
        assertArrayEquals(exp, ret);
    }

}
