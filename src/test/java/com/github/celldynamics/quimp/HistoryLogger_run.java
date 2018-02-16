package com.github.celldynamics.quimp;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoryLogger_run.
 *
 * @author p.baniukiewicz
 */
public class HistoryLogger_run {

  /**
   * The hs.
   */
  HistoryLogger hs;

  /**
   * Instantiates a new history logger run.
   */
  public HistoryLogger_run() {
    hs = new HistoryLogger();
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    HistoryLogger_run hsr = new HistoryLogger_run();
    hsr.hs.openHistory();

    hsr.hs.addEntry("Test 1", null);
    hsr.hs.addEntry("Test 2", null);

  }

}
