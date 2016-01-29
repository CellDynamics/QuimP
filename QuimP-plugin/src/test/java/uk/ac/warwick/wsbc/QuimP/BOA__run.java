/**
 * @file BOA__run.java
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.BOA_;

/**
 * Main runner for BOA_ plugin. Show main window and wait for its closing and then ends.
 * 
 * @author baniuk
 * @note
 * The window is run in separate thread therefore when control is returned to main thread (\c main)
 * (immediately after the window has been created) and the main thread ends everything is removed from memory
 * and the window closes. To prevent this behavior thread synchronization is used. Window listener is added to BOA_ window.
 * \c window is \c public field of BOA_ class representing \c CustomStackWindow internally extending \c Frame class.
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
		// create synchronization latch
		CountDownLatch startSignal = new CountDownLatch(1);
		img = IJ.openImage("src/test/resources/movie03_8bit.tif");
		img.show(); // this is necessary for plugin as it uses getcurrentimage to work
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
