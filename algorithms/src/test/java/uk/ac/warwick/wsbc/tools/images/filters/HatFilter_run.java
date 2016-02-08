/**
 * @file HatFilter_run.java
 * @date 8 Feb 2016
 */
package uk.ac.warwick.wsbc.tools.images.filters;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.vecmath.Vector2d;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;

/**
 * Test class for HatFilter UI
 * 
 * Shows UI for HAtFilter
 * 
 * @author p.baniukiewicz
 * @date 8 Feb 2016
 *
 */
public class HatFilter_run {

	@SuppressWarnings("serial")
	public static void main(String[] args) throws QuimpPluginException, InterruptedException {
		List<Vector2d> input;
		// test data
		input = new ArrayList<>();
		for(int i = 0; i<40; i++)
			input.add(new Vector2d(i,0));
		input.set(18, new Vector2d(18,1));
		input.set(19, new Vector2d(19,1));
		input.set(20, new Vector2d(20,1));
		
		// create instance of hatfilter
		HatFilterInst hf = new HatFilterInst();
		hf.attachData(input);
		hf.setPluginConfig(new HashMap<String,Object>() {{	put("window",5.0);
															put("crown",3.0);
															put("sigma",1.0);}});
		CountDownLatch startSignal = new CountDownLatch(1);
		hf.pluginWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		hf.pluginWnd.addWindowListener(new WindowAdapter() {
			@Override
			// This method will be called when BOA_ window is closed
			public void windowClosing(WindowEvent arg0) {
				startSignal.countDown(); // decrease latch by 1
			}
		});
		hf.ToggleWindow(); // show window
		// main thread waits here until Latch reaches 0
		startSignal.await();
	}

}

/**
 * Wrapper for HatFilter that allows to access private member \c pluginWnd
 *    
 * @author p.baniukiewicz
 * @date 8 Feb 2016
 *
 */
class HatFilterInst extends HatFilter {
	
	public JFrame pluginWnd;
	
	public HatFilterInst() {
		super();
		this.pluginWnd = super.pluginWnd;
	}

	
	
}