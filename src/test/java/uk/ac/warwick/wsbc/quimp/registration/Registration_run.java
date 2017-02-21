package uk.ac.warwick.wsbc.quimp.registration;

import ij.IJ;
import uk.ac.warwick.wsbc.quimp.registration.Registration;

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
