/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// TODO: Auto-generated Javadoc
/**
 * Runner for AboutDialog
 * @author p.baniukiewicz
 *
 */
public class AboutDialog_run {

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Frame window = new Frame("Base");
        window.add(new Panel());
        window.setSize(500, 500);
        window.setVisible(true);
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                window.dispose();
            }
        });
        AboutDialog ad = new AboutDialog(window); // create about dialog with parent 'window'
        ad.appendLine("Hello");
        ad.appendLine("ff");
        ad.appendDistance();
        ad.aboutWnd.setVisible(true);

    }

}