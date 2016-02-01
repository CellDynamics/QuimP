/**
 * @file QWindowBuilder_Test.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

/**
 * Test class for QWindowBuilder
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class QWindowBuilder_Test extends QWindowBuilder {

	/**
	 * @test BuildWindow - builds and displays window
	 * @pre window is defined by Map structure
	 * @post Window is displayed on screen
	 * @throws Exception
	 */
	@Test
	public void BuildWindow_test() throws Exception {
		CountDownLatch startSignal = new CountDownLatch(1);
		
		HashMap<String,String[]> def1 = new HashMap<String, String[]>();
		def1.put("name", new String[] {"test"});
		def1.put("window", new String[] {"spinner", "-0.5","0.5","0.1"});
		def1.put("smooth", new String[] {"spinner", "-1", "10", "1"});
		def1.put("help", new String[] {"FlowLayout is the default layout manager for every JPanel. It simply lays out components in a single row, starting a new row if its container is not sufficiently wide. Both panels in CardLayoutDemo, shown previously, use FlowLayout. For further details, see How to Use FlowLayout."});

		BuildWindow(def1);
		pluginWnd.addWindowListener(new WindowAdapter() {
			@Override
			// This method will be called when BOA_ window is closed
			public void windowClosed(WindowEvent arg0) {
				startSignal.countDown(); // decrease latch by 1
			}
		});
		ToggleWindow();
		// main thread waits here until Latch reaches 0
		startSignal.await();
		
		
	}

}
