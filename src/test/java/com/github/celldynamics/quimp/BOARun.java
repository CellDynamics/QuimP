package com.github.celldynamics.quimp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Toolbar;

/**
 * Main runner for BOA_ plugin. Show main window and wait for its closing and then ends.
 * 
 * <p>The window is run in separate thread therefore when control is returned to main thread (main)
 * (immediately after the window has been created) then the main thread ends that results in closing
 * of the program To prevent this behaviour thread synchronization is used. Window listener is added
 * to BOA_ window. window is public field of BOA_ class representing CustomStackWindow internally
 * extending Frame class.
 * 
 * <pre>
 * <code>ps -aux | grep Run | awk '{print $2}' | xargs kill</code>
 * </pre>
 * 
 * @see com.github.celldynamics.quimp.BOA_
 * 
 * @author p.baniukiewicz
 */
@SuppressWarnings("unused")
public class BOARun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   * @throws InterruptedException Runner for BOA plugin
   */
  public static void main(String[] args) throws InterruptedException {
    QuimP.newFileFormat.set(true);
    ImagePlus img;
    ImageJ ij = new ImageJ();
    // create synchronization latch
    CountDownLatch startSignal = new CountDownLatch(1);
    // img = IJ.openImage("src/test/Resources-static/movie03_8bit_10slices.tif");
    // img = IJ.openImage("src/test/Resources-static/movie03_8bit.tif");
    img = IJ.openImage("/home/baniuk/Documents/NEUBIAS/RandomWalk/Touching_Cells/Stack-30.tif");
    // img = IJ.openImage("src/test/Resources-static/Stack_cut.tif");
    // img = IJ.openImage("src/test/Resources-static/ticket199/fluoreszenz-test.tif");
    // img = IJ.openImage("/home/baniuk/Desktop/Tests/283/fill.tif");
    // img = IJ.openImage("/home/baniuk/Documents/mibtp/Clipboard.tif");
    // img = IJ.openImage("C:/Users/baniu/Desktop/July14ABD_GFP_actin_1pctagar.tif");

    img.show(); // this is necessary for plugin as it uses getcurrentimage to work
    // fake toolbar to allow calls to static fields of this class inside boa
    Toolbar t = new Toolbar();
    BOA_ ob = new BOA_();

    ob.run("/home/baniuk/Documents/Repos/Fiji.app.test/plugins"); // run BOA, control is immediately
    // returned
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
