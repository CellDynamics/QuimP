package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.io.File;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class Prot_Analysis_run {
    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        ImageJ ij = new ImageJ();
        // new Prot_Analysis(
        // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
        Prot_Analysis pa = new Prot_Analysis(new File(
                "src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF"));
        // new Prot_Analysis();

    }

}
