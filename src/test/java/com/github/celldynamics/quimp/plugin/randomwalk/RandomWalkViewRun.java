package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkView;

// TODO: Auto-generated Javadoc
/**
 * View runner.
 * 
 * @author p.baniukiewicz
 * @see <a href="https://examples.javacodegeeks.com/core-java/java-swing-mvc-example">MVC</a>
 */
public class RandomWalkViewRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /** The status. */
  static boolean status = true;

  /**
   * View preview.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    RandomWalkView rwv = new RandomWalkView();
    rwv.show();

    // test of disabling/enabling ui
    rwv.addCancelController(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        rwv.enableUI(status);
        status = !status;

      }
    });
  }

}
