package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.io.File;

import ij.ImageJ;
import uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.Prot_Analysis;

/**
 * @author p.baniukiewicz
 *
 */
public class Prot_Analysis_run {
    static {
        System.setProperty("quimp.debugLevel", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        ImageJ ij = new ImageJ();
        // new Prot_Analysis(
        // Paths.get("/home/p.baniukiewicz/Desktop/Tests/ticket150/fluoreszenz-test_eq_smooth.QCONF"));
        new Prot_Analysis(new File(
                "src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF"));
        // new Prot_Analysis();

    }

}
