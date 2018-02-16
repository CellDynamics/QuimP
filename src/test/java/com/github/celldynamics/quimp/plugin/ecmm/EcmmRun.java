package com.github.celldynamics.quimp.plugin.ecmm;

import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * Test runner.
 * 
 * @author p.baniukiewicz
 *
 */
public class EcmmRun {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Main.
   * 
   * @param args args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    ECMM_Mapping ob = new ECMM_Mapping();
    ob.run("");

  }

}
