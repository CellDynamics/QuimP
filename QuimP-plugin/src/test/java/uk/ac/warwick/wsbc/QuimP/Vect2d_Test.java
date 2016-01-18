package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple testing class for Vec2d class
 * 
 * @remarks
 * Will be removed in final version
 * @todo
 * Remove this class in future
 * @author baniuk
 *
 */
public class Vect2d_Test {

	private static final Logger logger = LogManager.getLogger(Vect2d_Test.class.getName());
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @test test toString() method
	 * @post Content of List in log file
	 */
	@Test
	public void testToString() {
		ArrayList<Vect2d> v = new ArrayList<Vect2d>();
		Vect2d vv = new Vect2d(3.14,-5.6);
		v.add(new Vect2d(0,0));
		v.add(new Vect2d(10,10));
		v.add(new Vect2d(3.14,-4.56));
		logger.debug("vector "+vv.toString());
		logger.debug("V1 vector: "+v.toString());
	}

}
