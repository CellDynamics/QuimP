package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Gui checker for DICLIDReconstruction
 */
public class DICLIDReconstruction_run {

    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * @param args
     * @throws InterruptedException Gui checker for DICLIDReconstruction
     */
    public static void main(String[] args) throws InterruptedException {
        ImageJ ij = new ImageJ();
        ImagePlus i = IJ.openImage( // load images #272
                "/home/baniuk/Downloads/C2-bleb_Image4.tif");
        i.show();
        DICLIDReconstruction_ dic = new DICLIDReconstruction_();
        dic.setup("", i);
        dic.run(i.getProcessor());
    }
}
