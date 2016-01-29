/**
 * @file ConfigReader_Test.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.helpers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ConfigReader
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class ConfigReader_Test {

	private ConfigReader cR;
	
	@Before
	public void setUp() throws Exception {
		cR = new ConfigReader("src/test/resources/test.config");
	}
	
	/**
	 * @test Test of reading of \c String
	 * @throws ConfigReaderException
	 */
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
	@Test(expected=ConfigReaderException.class)
	public void test_ConfigReader_case2() throws ConfigReaderException {
		cR.getRawParam("meanFilter", "window");
	}
	
	/**
	 * @test Test of wrong parameter name
	 * @pre Name of parameter ends with capital letter
	 * @post expected ConfigReaderException
	 * @throws ConfigReaderException
	 */
	@Test(expected=ConfigReaderException.class)
	public void test_ConfigReader_case3() throws ConfigReaderException {
		cR.getRawParam("MeanFilter", "windoW");
	}
	
	/**
	 * @test Test of reading of \c String
	 * @throws ConfigReaderException
	 */
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
	@Test(expected=ConfigReaderException.class)
	public void test_getIntParam_case2() throws ConfigReaderException {
		int w;
		w = cR.getIntParam("LoesFilter", "smooth");
		assertEquals(11, w);
	}
	
	/**
	 * @test Test of reading of \c double
	 * @throws ConfigReaderException
	 */
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
	@Test
	public void test_getDoubleParam_case2() throws ConfigReaderException {
		double w;
		w = cR.getDoubleParam("MeanFilter", "window");
		assertEquals(11, w, 1e-5);
	}

}
