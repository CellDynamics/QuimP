package com.github.celldynamics.quimp.plugin.qanalysis;

import com.github.celldynamics.quimp.QuimpException;

import ij.ImageJ;

/**
 * Test runner for Q_Analysis.
 *
 * @author p.baniukiewicz
 */
public class QAnalysisRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   * @throws QuimpException
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws QuimpException {
    ImageJ ij = new ImageJ();
    // API
    // Q_Analysis qa = new Q_Analysis(new File("C:/Users/baniu/Desktop/FullAnalysis/Stack.QCONF"));
    // qa.executer();

    /**
     * source of data: http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ProtrusionTracking
     * These data came from Repos/Prot_counting/fromMail directory and were used in Matlab
     * experiments
     */
    // new Q_Analysis(Paths
    // .get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth_0.paQP"));

    Q_Analysis qa = new Q_Analysis();
    qa.run("");

    // qa.run("opts={trackColor:Summer,outlinePlot:Speed,sumCov:1.0,avgCov:0.0,mapRes:400,"
    // + "paramFile:(C:/Users/baniu/Desktop/FullAnalysis/Stack.QCONF)}");

  }

}
