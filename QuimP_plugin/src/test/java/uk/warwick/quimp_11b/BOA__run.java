/**
 * 
 */
package uk.warwick.quimp_11b;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImagePlus;

/**
 * Main runner for BOA plugin. Shows main window and waits for its closing and then ends.
 * @author baniuk
 * @note
 * The main problem is with ImageJ plugin is that it is run and then when it finish
 * all its windows being closed. Window is run in separate thread and when control
 * is returned to main thread all being killed and removed from memory
 * To prevent this behaviour thread synchronisation is used.
 * Window listener is added to BOA_ window.
 * \c window is \c public field of BOA_ class representing \c CustomStackWindow
 * internally extending \c Frame class
 * @see
 * BOA_
 */
public class BOA__run {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @test Runner for BOA plugin
	 */
	public static void main(String[] args) throws InterruptedException {
		ImagePlus img;
		// create synchronisation latch
		CountDownLatch startSignal = new CountDownLatch(1);
		img = IJ.openImage("src/test/java/uk/warwick/quimp_11b/movie03.tif");
		img.show(); // this is necessary for plugin as it uses \c getcurrentimage to work
		BOA_ ob = new BOA_(); 
		ob.run(null); // run BOA, control is immediately returned to main
		// add window listener to BOA_ window
		// window is \public field of BOA_ class representing CustomStackWindow
		// internally extending Frame class
		ob.window.addWindowListener(new WindowAdapter() {
			
			@Override
			// This method will be called when BOA_ window is closed
			public void windowClosed(WindowEvent arg0) {
				startSignal.countDown(); // decrease latch by 1
			}
		});
		// main thread waits here until Latch reaches 0
		startSignal.await();
		img.close();
		
	}

}
