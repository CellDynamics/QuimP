package uk.ac.warwick.wsbc.QuimP.registration;

import ij.IJ;

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
