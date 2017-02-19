package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class Prot_Analysis_UI_run {
    static {
        System.setProperty("quimp.debugLevel", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Prot_Analysis(
                "src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF").gui
                        .showUI(true);
        // ImagePlus ip =
        // IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif");
        // new Prot_AnalysisUI(new ProtAnalysisConfig(), null).getGradient(ip);

    }

}
