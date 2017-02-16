package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class PropagateSeeds_run {

    static {
        System.setProperty("logback.configurationFile", "quimp-logback.xml");
    }
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PropagateSeeds_run.class.getName());

    /**
     * @param args
     * @throws RandomWalkException
     */
    public static void main(String[] args) throws RandomWalkException {
        ImageJ ij = new ImageJ();
        ImagePlus mask = IJ.openImage(
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif");
        ImagePlus org = IJ.openImage(
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif");
        mask.show();
        org.show();
        PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true);
        cc.propagateSeed(mask.getProcessor(), 20, 10);
        CompositeImage ret = (CompositeImage) cc.getCompositeSeed(org);
        ret.show();
        ImageProcessor r = ret.getProcessor(1);
        IJ.saveAsTiff(new ImagePlus("red", r),
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/rough_snakemask_shrink.tif");
        r = ret.getProcessor(2);
        IJ.saveAsTiff(new ImagePlus("green", r),
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/rough_snakemask_expand.tif");

    }

}
