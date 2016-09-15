/**
 * @file ECMM_run.java
 * @date 19 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 * @date 19 Apr 2016
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
