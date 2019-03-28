package com.github.celldynamics.quimp.omero;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class OmeroClientRun {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new ImageJ();
    new OmeroClient_();

  }

}
