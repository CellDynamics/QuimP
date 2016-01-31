/**
 * @file QWindowBuilder_Test.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

/**
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class QWindowBuilder_Test extends QWindowBuilder {

	@Test
	public void test() throws InterruptedException {
		CountDownLatch startSignal = new CountDownLatch(1);
		
		String def[][] = {
				{"Title", ""},
				{"spinner", "-0.5","0.5","0.1","Description"},
				{"spinner", "-1", "10", "1","Description1"}
		};
		BuildWindow(def);
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
