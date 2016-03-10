/**
 * @file BOA__run.java
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

/**
 * Main runner for BOA_ plugin. Show main window and wait for its closing and
 * then ends.
 * 
 * @author baniuk
 * @note The window is run in separate thread therefore when control is returned
 * to main thread (\c main) (immediately after the window has been
 * created) then the main thread ends that results in closing of the program To prevent this
 *  behavior thread synchronization is used. Window listener is added to BOA_ window. \c window is
 *  \c public field of BOA_ class representing \c CustomStackWindow internally
 * extending \c Frame class.
 * @see BOA_
 * @remarks This process will not finish because \c window default behavior is
 * to conceal itself not quit. Kill instances by
 * @code{.sh}
 * ps -aux | grep BOA__run | awk '{print $2}' | xargs kill
 * @endcode
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
        img = IJ.openImage("src/test/resources/movie03_8bit_10slices.tif");
        img.show(); // this is necessary for plugin as it uses getcurrentimage
                    // to work
        Toolbar t = new Toolbar(); // fake toolbar to allow calls to static
                                   // fields of this class inside boa
        BOA_ ob = new BOA_();

        ob.run("../plugins/target/"); // run BOA, control is immediately returned
        // ob.run("../plugins_test/target/"); // run BOA, control is immediately returned

        // add window listener to BOA_ window window is \public field of BOA_ class
        // representing CustomStackWindow internally extending Frame class
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
