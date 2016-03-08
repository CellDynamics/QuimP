
/**
 * @file HatFilter_run.java
 * @date 8 Feb 2016
 */

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.vecmath.Point2d;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * Test class for HatFilter UI
 * 
 * Shows UI for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 8 Feb 2016
 *
 */
public class HatFilter_run {

    @SuppressWarnings("serial")
    public static void main(String[] args) throws QuimpPluginException, InterruptedException {
        List<Point2d> input;
        // test data
        input = new ArrayList<>();
        input.add(new Point2d(923, 700));
        input.add(new Point2d(577.5, 1175));
        input.add(new Point2d(18, 993));
        input.add(new Point2d(18, 406));
        input.add(new Point2d(577, 224));
        // input.add(new Vector2d( 428, -4.87));
        // input.add(new Vector2d( 3.11, -3.9));

        // create instance of hatfilter
        HatFilterInst hf = new HatFilterInst();
        hf.attachData(input);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", "15");
                put("pnum", "1");
                put("alev", "0.0"); // case insensitive
            }
        });
        CountDownLatch startSignal = new CountDownLatch(1);
        hf.pluginWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hf.pluginWnd.addWindowListener(new WindowAdapter() {
            @Override
            // This method will be called when BOA_ window is closed
            public void windowClosing(WindowEvent arg0) {
                startSignal.countDown(); // decrease latch by 1
            }
        });
        hf.toggleWindow(true); // show window
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
class HatFilterInst extends HatSnakeFilter_ {

    public JFrame pluginWnd;

    public HatFilterInst() {
        super();
        this.pluginWnd = super.pluginWnd;
    }

}