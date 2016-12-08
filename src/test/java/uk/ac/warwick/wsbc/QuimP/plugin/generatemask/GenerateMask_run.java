package uk.ac.warwick.wsbc.QuimP.plugin.generatemask;

import java.io.File;

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
        GenerateMask pa = new GenerateMask(
                new File("/home/baniuk/Desktop/Tests/280/July14ABD_GFP_actin_twoCells.QCONF"));

    }

}
