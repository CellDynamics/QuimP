package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImageJ;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentationPlugin_;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPlugin_run {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * @param args
   * @throws InterruptedException
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch startSignal = new CountDownLatch(1);
    ImageJ ij = new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    // IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
    // "open(\"src/test/resources/ticket209gh/fluoreszenz-test.tif\")");
    // IJ.runMacro("open(\"src/test/resources/ticket209gh/segmented_color.tif\")");
    IJ.openImage("src/test/resources/ticket209gh/fluoreszenz-test.tif").show();
    IJ.openImage("src/test/resources/ticket209gh/segmented_color.tif").show();
    obj.run("");
    obj.wnd.addWindowListener(new WindowAdapter() {

      @Override
      // This method will be called when BOA_ window is closed
      public void windowClosed(WindowEvent arg0) {
        startSignal.countDown(); // decrease latch by 1
      }
    });
    // main thread waits here until Latch reaches 0
    startSignal.await();

  }

}