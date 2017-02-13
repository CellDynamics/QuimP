package uk.ac.warwick.wsbc.QuimP.plugin.ana;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * Plugin runner for in-place tests.
 * 
 * @author p.baniukiewicz
 */
public class ANA_run {
    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }

    /**
     * 
     */
    public ANA_run() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        ImageJ ij = new ImageJ();
        ImagePlus im = IJ.openImage("C:/Users/baniu/Desktop/attachments/Example_1.tif");
        im.show();
        ANA_ ana = new ANA_();
        ana.setup(new String(), im);
        // load paQP and QCONF file related to tiff pointed above
        ana.run(im.getProcessor());

    }

}
