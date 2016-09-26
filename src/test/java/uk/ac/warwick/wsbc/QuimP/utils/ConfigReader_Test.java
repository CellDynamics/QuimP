/**
 */
package uk.ac.warwick.wsbc.QuimP.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ConfigReader
 * 
 * @author p.baniukiewicz
 *
 */
public class ConfigReader_Test {

    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    @SuppressWarnings("deprecation")
    private ConfigReader cR;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        cR = new ConfigReader("src/test/resources/test.config");
    }

    /**
     * @test Test of reading of \c String
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test_ConfigReader_case1() throws ConfigReaderException {
        String w;
        w = cR.getRawParam("MeanFilter", "window");
        assertEquals("11", w);
    }

    /**
     * @test Test of wrong plugin name
     * @pre Name of filter with small letter
     * @post expected ConfigReaderException
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = ConfigReaderException.class)
    public void test_ConfigReader_case2() throws ConfigReaderException {
        cR.getRawParam("meanFilter", "window");
    }

    /**
     * @test Test of wrong parameter name
     * @pre Name of parameter ends with capital letter
     * @post expected ConfigReaderException
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = ConfigReaderException.class)
    public void test_ConfigReader_case3() throws ConfigReaderException {
        cR.getRawParam("MeanFilter", "windoW");
    }

    /**
     * @test Test of reading of \c String
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test_ConfigReader_case4() throws ConfigReaderException {
        String w;
        w = cR.getRawParam("LoesFilter", "smooth");
        assertEquals("1.1e-3", w);
    }

    /**
     * @test Test of reading of \c int
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test_getIntParam_case1() throws ConfigReaderException {
        int w;
        w = cR.getIntParam("MeanFilter", "window");
        assertEquals(11, w);
    }

    /**
     * @test Test of reading \c double as \c int
     * @pre pointed to double value but read it as int
     * @post expected ConfigReaderException
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = ConfigReaderException.class)
    public void test_getIntParam_case2() throws ConfigReaderException {
        int w;
        w = cR.getIntParam("LoesFilter", "smooth");
        assertEquals(11, w);
    }

    /**
     * @test Test of reading of \c double
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test_getDoubleParam_case1() throws ConfigReaderException {
        double w;
        w = cR.getDoubleParam("LoesFilter", "smooth");
        assertEquals(1.1e-3, w, 1e-5);
    }

    /**
     * @test Test of reading \c int as \c double
     * @pre pointed to int value but read it as double
     * @post expected nothing
     * @throws ConfigReaderException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test_getDoubleParam_case2() throws ConfigReaderException {
        double w;
        w = cR.getDoubleParam("MeanFilter", "window");
        assertEquals(11, w, 1e-5);
    }

}
