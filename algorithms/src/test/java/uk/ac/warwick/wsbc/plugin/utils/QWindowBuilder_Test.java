/**
 * @file QWindowBuilder_Test.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.plugin.utils;

import static org.junit.Assert.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import uk.ac.warwick.wsbc.plugin.utils.QWindowBuilder;

/**
 * Test class for QWindowBuilder
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class QWindowBuilder_Test {
	private static final Logger logger = LogManager.getLogger(QWindowBuilder_Test.class.getName());
	private HashMap<String,String[]> def1;
	QWindowBuilderInst inst;
	
	/**
	 * Instance private class for tested QWindowBuilder
	 * 
	 * @author p.baniukiewicz
	 * @date 2 Feb 2016
	 *
	 */
	class QWindowBuilderInst extends QWindowBuilder {		
	}

	@Rule
	public TestName name = new TestName(); ///< Allow to get tested method name (called at setUp())
	
	@Before
	public void setUp() throws Exception {
		def1 = new HashMap<String, String[]>(); // setup window params
		def1.put("name", new String[] {"test"});
		def1.put("window", new String[] {"spinner", "-0.5","0.5","0.1"});
		def1.put("smooth", new String[] {"spinner", "-1", "10", "1"});
		def1.put("help", new String[] {"FlowLayout is the default layout manager for every JPanel. It simply lays out components in a single row, starting a new row if its container is not sufficiently wide. Both panels in CardLayoutDemo, shown previously, use FlowLayout. For further details, see How to Use FlowLayout."});
		inst = new QWindowBuilderInst(); // create window object
	}
	
	@After
	public void tearDown() throws Exception {
		def1.clear();
		def1 = null;
		inst = null;
	}
	
	/**
	 * @test BuildWindow - builds and displays window
	 * @pre window is defined by Map structure and other than default values are set
	 * @post Window is displayed on screen with updated values
	 * @throws Exception
	 */
	@Test
	public void test_BuildWindow() throws Exception {
		HashMap<String,Object> set = new HashMap<>();
		HashMap<String,Object> ret;
		set.put("window", 0.32);
		set.put("smooth", 8.0);
		CountDownLatch startSignal = new CountDownLatch(1);
		inst.BuildWindow(def1); // main window builder
		inst.setValues(set);
		inst.pluginWnd.addWindowListener(new WindowAdapter() {
			@Override
			// This method will be called when BOA_ window is closed
			public void windowClosing(WindowEvent arg0) {
				logger.debug("Listener activated "+name.getMethodName());
				startSignal.countDown(); // decrease latch by 1
			}
		});
		inst.ToggleWindow(); // show window
		// main thread waits here until Latch reaches 0
		startSignal.await();
		ret = (HashMap<String, Object>) inst.getValues();
		logger.trace("Finishing "+name.getMethodName());
		logger.debug("window="+ret.get("window")+" smooth="+ret.get("smooth"));
	}
	
	/**
	 * @test getValues Get default values from window
	 * @pre \c def1 config string
	 * @post default values are lower bounds for defined ui controls
	 */
	@Test
	public void test_getValues() {
		HashMap<String,Object> ret;
		inst.BuildWindow(def1);
		ret = (HashMap<String, Object>) inst.getValues();
		assertEquals(-0.5, (Double)ret.get("window"),1e-4);
		assertEquals(-1, (Double)ret.get("smooth"),1e-4);
	}
	
	/**
	 * @test setgetValues Get previously set values from window
	 * @pre \c values for two ui are set
	 * @post set values are received
	 */
	@Test
	public void test_setgetValues() {
		HashMap<String,Object> ret;
		HashMap<String,Object> set = new HashMap<>();
		set.put("window", 0.32);
		set.put("smooth", 7.0);
		inst.BuildWindow(def1);
		inst.setValues(set);
		ret = (HashMap<String, Object>) inst.getValues();
		assertEquals(0.32, (Double)ret.get("window"),1e-4);
		assertEquals(7, (Double)ret.get("smooth"),1e-4);
	}
	
	

}
