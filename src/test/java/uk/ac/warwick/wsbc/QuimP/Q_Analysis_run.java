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

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // if default constructor is used Q_Analysis will ask for paQP file
        new Q_Analysis(Paths.get("/home/baniuk/Downloads/Composite-after-macro_cut_4.paQP"));

    }

}
