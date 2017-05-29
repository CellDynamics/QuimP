package com.github.celldynamics.quimp.registration;

import com.github.celldynamics.quimp.registration.Registration;

import ij.IJ;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class Registration_run {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {
    // ImageJ im = new ImageJ();
    Registration reg = new Registration(IJ.getInstance(), "Registration");
  }

}
