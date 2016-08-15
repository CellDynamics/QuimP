/**
 * @file Prot_Analysis_run.java
 * @date 13 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.nio.file.Paths;

import org.apache.logging.log4j.core.config.Configurator;

/**
 * @author p.baniukiewicz
 *
 */
public class Prot_Analysis_run {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        new Prot_Analysis(
                Paths.get("/home/baniuk/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));

    }

}
