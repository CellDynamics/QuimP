package uk.ac.warwick.wsbc.QuimP.plugin.generatemask;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
public class GenerateMask_run {
    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        ImageJ ij = new ImageJ();
        // GenerateMask_ pa = new GenerateMask_(
        // "filename=[C:/Users/baniu/Google
        // Drive/Warwick/Abstract/C1-talA_mNeon_bleb_0pt7%agar_FLU_fine.QCONF]");

        GenerateMask_ pa = new GenerateMask_(null);

    }

}
