/**
 * @file Q_Analysis_run.java
 * @date 28 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * Test runner for Q_Analysis
 * 
 * @author p.baniukiewicz
 * @date 28 Apr 2016
 *
 */
@SuppressWarnings("unused")
public class Q_Analysis_run {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // if default constructor is used Q_Analysis will ask for paQP file
        // new Q_Analysis(
        // Paths.get("/home/baniuk/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));

        /**
         * source of data: http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ProtrusionTracking
         * These data came from Repos/Prot_counting/fromMail directory and were used in Matlab
         * experiments
         */
        // new Q_Analysis(Paths
        // .get("/home/baniuk/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth_0.paQP"));

        new Q_Analysis();

    }

}
