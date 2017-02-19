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
        // GenerateMask pa = new GenerateMask(
        // new File("/home/baniuk/Desktop/Tests/280/July14ABD_GFP_actin_twoCells.QCONF"));
        GenerateMask_ pa = new GenerateMask_(null);

    }

}
