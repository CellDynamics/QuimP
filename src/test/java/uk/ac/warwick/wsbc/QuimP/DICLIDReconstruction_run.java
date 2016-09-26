/**
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * Gui checker for DICLIDReconstruction
 */
public class DICLIDReconstruction_run {

    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }

    /**
     * @param args
     * @throws InterruptedException
     * @test Gui checker for DICLIDReconstruction
     */
    public static void main(String[] args) throws InterruptedException {
        DICLIDReconstruction_ dic = new DICLIDReconstruction_();
        dic.showDialog();
    }
}
