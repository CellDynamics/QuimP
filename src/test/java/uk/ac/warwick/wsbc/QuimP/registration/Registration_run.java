package uk.ac.warwick.wsbc.QuimP.registration;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class Registration_run {
    static {
        System.setProperty("quimp.debugLevel", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ImageJ im = new ImageJ();
        Registration reg = new Registration(im.getOwner(), "Registration");

    }

}
