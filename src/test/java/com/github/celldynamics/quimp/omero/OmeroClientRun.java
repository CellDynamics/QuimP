package com.github.celldynamics.quimp.omero;

import ij.ImageJ;

/**
 * Dummy runner.
 * 
 * @author p.baniukiewicz
 *
 */
public class OmeroClientRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * Main.
   * 
   * @param args args
   */
  public static void main(String[] args) {
    new ImageJ();
    new OmeroClient_();

  }

}
