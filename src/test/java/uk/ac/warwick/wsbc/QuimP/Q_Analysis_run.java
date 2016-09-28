package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.Q_Analysis;

/**
 * Test runner for Q_Analysis
 * 
 * @author p.baniukiewicz
 *
 */
public class Q_Analysis_run {
    static {
        System.setProperty("quimp.debugLevel", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // if default constructor is used Q_Analysis will ask for paQP file
        // new Q_Analysis(
        // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));

        /**
         * source of data: http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ProtrusionTracking
         * These data came from Repos/Prot_counting/fromMail directory and were used in Matlab
         * experiments
         */
        // new Q_Analysis(Paths
        // .get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth_0.paQP"));

        new Q_Analysis();

    }

}
