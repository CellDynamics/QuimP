/**
 * @file Prot_Analysis_run.java
 * @date 13 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.nio.file.Paths;

import org.apache.logging.log4j.core.config.Configurator;

import ij.ImageJ;

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
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        ImageJ ij = new ImageJ();
        // new Prot_Analysis(
        // Paths.get("/home/baniuk/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
        new Prot_Analysis(Paths
                .get("src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF"));

    }

}
