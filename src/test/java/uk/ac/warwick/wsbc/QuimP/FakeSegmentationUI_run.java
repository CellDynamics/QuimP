/**
 * @file FakeSegmentationUI_run.java
 * @date 28 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 * @date 28 Jun 2016
 *
 */
public class FakeSegmentationUI_run {

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * 
     */
    public FakeSegmentationUI_run() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        new FakeSegmentationUI(null).show();

    }

}
