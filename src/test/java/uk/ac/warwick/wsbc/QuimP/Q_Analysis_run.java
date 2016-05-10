/**
 * @file Q_Analysis_run.java
 * @date 28 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 * @date 28 Apr 2016
 *
 */
public class Q_Analysis_run {

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Q_Analysis();

    }

}
