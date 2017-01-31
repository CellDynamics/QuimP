package uk.ac.warwick.wsbc.QuimP.plugin.qanalysis;

import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * Test runner for Q_Analysis
 * 
 * @author p.baniukiewicz
 *
 */
public class Q_Analysis_run {
    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        ImageJ ij = new ImageJ();
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
