package com.github.celldynamics.quimp.plugin.utils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.ParamList;

// TODO: Auto-generated Javadoc
/**
 * Simple test class showing window from QWindowBuilder.
 * 
 * @author p.baniukiewicz
 *
 */
public class QWindowBuilder_run {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws InterruptedException {
    final Logger LOGGER = LoggerFactory.getLogger(QWindowBuilder_run.class.getName());
    ParamList def1;
    QWindowBuilderInst inst;

    def1 = new ParamList(); // setup window params
    def1.put("NAME", "test");
    def1.put("window", "spinner: -0.5: 0.5: 0.1: 0");
    def1.put("SMOOTH", "spinner: -1: 10: 1: -1: Other help text");
    def1.put("Selector", "choice: option_1: option_2: option_3");
    def1.put("Selector1", "choiceh: option_1: option_2: option_3: Help text");
    def1.put("Button", "button: Two words: help text");
    def1.put("boolean", "checkbox: Two words: true: help text");
    def1.put("help",
            "FlowLayout is the default layout manager for"
                    + " every JPanel. It simply lays out components in a"
                    + " single row, starting a new row if its container "
                    + "is not sufficiently wide. Both panels in CardLayoutDemo, "
                    + "shown previously, use FlowLayout. For further "
                    + "details, see How to Use FlowLayout.");
    inst = new QWindowBuilderInst(); // create window object

    ParamList set = new ParamList();
    ParamList ret;
    set.setDoubleValue("Window", 0.32);
    set.setDoubleValue("SMOoTH", 8.0);
    set.setStringValue("Selector", "option_2");
    set.setStringValue("Selector1", "option_3");
    // set.setBooleanValue("boolean", false);
    CountDownLatch startSignal = new CountDownLatch(1);
    inst.buildWindow(def1); // main window builder
    inst.setValues(set);
    inst.pluginWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    inst.pluginWnd.addWindowListener(new WindowAdapter() {
      @Override
      // This method will be called when BOA_ window is closed
      public void windowClosing(WindowEvent arg0) {
        LOGGER.debug("Listener activated ");
        startSignal.countDown(); // decrease latch by 1
      }
    });
    inst.toggleWindow(true); // show window
    // main thread waits here until Latch reaches 0
    startSignal.await();
    ret = inst.getValues();
    LOGGER.trace("Finishing ");
    LOGGER.debug("window=" + ret.get("window") + " smooth=" + ret.get("smooth") + " selector="
            + ret.get("selector"));
    inst = null;
  }

}

class QWindowBuilderInst extends QWindowBuilder {

}