/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import ij.IJ;
import ij.ImageJ;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.RandomWalkSegmentationPlugin_;

/**
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPlugin_run {
    /**
     * @param args
     * @throws InterruptedException 
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        ImageJ ij = new ImageJ();
        RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
        IJ.runMacro( // load images that are called from RandomWalkSegmentationPlugin
                "open(\"/home/p.baniukiewicz/Documents/Repos/QuimP/src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif\")");
        IJ.runMacro(
                "open(\"/home/p.baniukiewicz/Documents/Repos/QuimP/src/test/resources/segmented_color.tif\")");
        // obj.run("");
        obj.showDialog();

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
