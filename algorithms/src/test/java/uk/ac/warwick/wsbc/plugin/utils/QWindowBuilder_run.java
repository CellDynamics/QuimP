package uk.ac.warwick.wsbc.plugin.utils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple test class showing window from QWindowBuilder
 * 
 * @author p.baniukiewicz
 * @date 8 Feb 2016
 *
 */
public class QWindowBuilder_run {

    public static void main(String[] args) throws InterruptedException {
        final Logger logger = LogManager.getLogger(QWindowBuilder_run.class.getName());
        HashMap<String, String[]> def1;
        QWindowBuilderInst inst;

        def1 = new HashMap<String, String[]>(); // setup window params
        def1.put("name", new String[] { "test" });
        def1.put("window", new String[] { "spinner", "-0.5", "0.5", "0.1", "0" });
        def1.put("smooth", new String[] { "spinner", "-1", "10", "1", "-1" });
        def1.put("help", new String[] {
                "FlowLayout is the default layout manager for every JPanel. It simply lays out components in a single row, starting a new row if its container is not sufficiently wide. Both panels in CardLayoutDemo, shown previously, use FlowLayout. For further details, see How to Use FlowLayout." });
        inst = new QWindowBuilderInst(); // create window object

        HashMap<String, Object> set = new HashMap<>();
        HashMap<String, Object> ret;
        set.put("window", 0.32);
        set.put("smooth", 8.0);
        CountDownLatch startSignal = new CountDownLatch(1);
        inst.BuildWindow(def1); // main window builder
        inst.setValues(set);
        inst.pluginWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inst.pluginWnd.addWindowListener(new WindowAdapter() {
            @Override
            // This method will be called when BOA_ window is closed
            public void windowClosing(WindowEvent arg0) {
                logger.debug("Listener activated ");
                startSignal.countDown(); // decrease latch by 1
            }
        });
        inst.ToggleWindow(); // show window
        // main thread waits here until Latch reaches 0
        startSignal.await();
        ret = (HashMap<String, Object>) inst.getValues();
        logger.trace("Finishing ");
        logger.debug("window=" + ret.get("window") + " smooth=" + ret.get("smooth"));
        inst = null;
    }

}

class QWindowBuilderInst extends QWindowBuilder {

}