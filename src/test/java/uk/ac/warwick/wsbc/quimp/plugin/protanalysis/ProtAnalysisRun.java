package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtAnalysisRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Runner.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {

    ImageJ ij = new ImageJ();
    // new Prot_Analysis(
    // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
    Prot_Analysis pa =
            new Prot_Analysis("src/test/resources/280/July14ABD_GFP_actin_twoCells.QCONF");
    // new Prot_Analysis();

  }

}