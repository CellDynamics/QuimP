/**
 * @file Q_Analysis_run.java
 * @date 28 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.nio.file.Paths;

/**
 * Test runner for Q_Analysis
 * 
 * @author p.baniukiewicz
 * @date 28 Apr 2016
 *
 */
public class Q_Analysis_run {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // if default constructor is used Q_Analysis will ask for paQP file
        new Q_Analysis(
                Paths.get("/home/baniuk/Desktop/Tests/ticket61/fluoreszenz-test_BOA_0.paQP"));
        // source of data: http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ProtrusionTracking
        // These data came from Repos/Prot_counting/fromMail directory and were used in Matlab
        // experiments

    }

}
