package com.github.celldynamics.quimp.registration;

import ij.IJ;

// TODO: Auto-generated Javadoc
/**
 * The Class RegistrationRun.
 *
 * @author p.baniukiewicz
 */
public class RegistrationRun {
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
    // ImageJ im = new ImageJ();
    Registration reg = new Registration(IJ.getInstance(), "Registration");
  }

}
