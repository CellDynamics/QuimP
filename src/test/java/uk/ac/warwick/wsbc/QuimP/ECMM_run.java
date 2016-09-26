/**
 */
package uk.ac.warwick.wsbc.QuimP;

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
