/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.bar;

import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * Bar displayer
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimP_Bar_run {

    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        ImageJ ij = new ImageJ();
        QuimP_Bar bar = new QuimP_Bar();
        bar.run("");

    }

}
