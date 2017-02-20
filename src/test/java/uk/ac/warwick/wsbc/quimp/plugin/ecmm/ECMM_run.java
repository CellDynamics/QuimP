/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import ij.ImageJ;
import uk.ac.warwick.wsbc.quimp.plugin.ecmm.ECMM_Mapping;

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
