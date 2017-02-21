package uk.ac.warwick.wsbc.quimp.plugin.generatemask;

import ij.ImageJ;
import uk.ac.warwick.wsbc.quimp.plugin.generatemask.GenerateMask_;

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
