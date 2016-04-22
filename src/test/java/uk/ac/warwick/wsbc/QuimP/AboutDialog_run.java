/**
 * @file AboutDialog_run.java
 * @date 22 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Runner for AboutDialog
 * @author p.baniukiewicz
 * @date 22 Apr 2016
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
        ad.append("Hello\n");
        ad.append("ff");
        ad.aboutWnd.setVisible(true);

    }

}
