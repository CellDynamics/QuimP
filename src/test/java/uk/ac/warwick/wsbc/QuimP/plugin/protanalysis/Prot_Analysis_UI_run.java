package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.io.File;

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
        new Prot_Analysis(new File("src/test/resources/Stack_cut.QCONF")).gui.showUI(true);

    }

}
