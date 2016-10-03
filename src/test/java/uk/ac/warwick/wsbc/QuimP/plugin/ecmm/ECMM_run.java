/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.ecmm;

import uk.ac.warwick.wsbc.QuimP.plugin.ecmm.ECMM_Mapping;

/**
 * @author p.baniukiewicz
 *
 */
public class ECMM_run {

    static {
        System.setProperty("quimp.debugLevel", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        new ECMM_Mapping();

    }

}
