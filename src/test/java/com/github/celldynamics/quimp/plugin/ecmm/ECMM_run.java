package com.github.celldynamics.quimp.plugin.ecmm;

import com.github.celldynamics.quimp.plugin.ecmm.ECMM_Mapping;

import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class ECMM_run {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    ImageJ ij = new ImageJ();
    new ECMM_Mapping();

  }

}
