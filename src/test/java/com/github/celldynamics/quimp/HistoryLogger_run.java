package com.github.celldynamics.quimp;

import com.github.celldynamics.quimp.HistoryLogger;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class HistoryLogger_run {

  /**
   * The hs.
   */
  HistoryLogger hs;

  /**
   * 
   */
  public HistoryLogger_run() {
    hs = new HistoryLogger();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    HistoryLogger_run hsr = new HistoryLogger_run();
    hsr.hs.openHistory();

    hsr.hs.addEntry("Test 1", null);
    hsr.hs.addEntry("Test 2", null);

  }

}
