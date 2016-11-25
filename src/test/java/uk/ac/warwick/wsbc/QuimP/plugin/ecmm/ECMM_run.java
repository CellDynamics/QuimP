/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.ecmm;

import ij.ImageJ;

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
    public static void main(String[] args) {
        ImageJ ij = new ImageJ();
        new ECMM_Mapping();

    }

}
