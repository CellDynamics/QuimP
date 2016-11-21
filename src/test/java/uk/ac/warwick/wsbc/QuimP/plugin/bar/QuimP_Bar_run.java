/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.bar;

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
    public static void main(String[] args) {
        QuimP_Bar bar = new QuimP_Bar();
        bar.run("");

    }

}
