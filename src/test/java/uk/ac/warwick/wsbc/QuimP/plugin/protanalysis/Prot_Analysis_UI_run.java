package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import ij.IJ;
import ij.ImagePlus;

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
        // new Prot_Analysis(new File("src/test/resources/Stack_cut.QCONF")).gui.showUI(true);
        ImagePlus ip = IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif");
        new Prot_AnalysisUI(new ProtAnalysisConfig(), null).getGradient(ip);

    }

}
