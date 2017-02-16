package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
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
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/C1-talA_mNeon_bleb_0pt7%agar_FLU_snakemask.tif");
        ImagePlus org = IJ.openImage(
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/C1-talA_mNeon_bleb_0pt7%agar_FLU.tif");
        mask.show();
        org.show();
        ImageStack is = mask.getStack();
        PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true);
        for (int i = 1; i <= is.getSize(); i++) {
            cc.propagateSeed(mask.getStack().getProcessor(i), 20, 10);
        }
        CompositeImage ret = (CompositeImage) cc.getCompositeSeed(org);
        ret.show();
        ImageStack resultstack_red = new ImageStack(mask.getWidth(), mask.getHeight());
        ImageStack resultstack_green = new ImageStack(mask.getWidth(), mask.getHeight());
        for (int i = 1; i <= is.getSize(); i++) {
            ret.setZ(i);
            ImageProcessor red = ret.getProcessor(1);
            ImageProcessor green = ret.getProcessor(2);
            resultstack_red.addSlice(red);
            resultstack_green.addSlice(green);
        }
        IJ.saveAsTiff(new ImagePlus("red", resultstack_red),
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/rough_snakemask_shrink_stack.tif");
        IJ.saveAsTiff(new ImagePlus("green", resultstack_green),
                "/home/baniuk/baniuk1@gmail.com/Warwick/Abstract/rough_snakemask_expand_stack.tif");

        ij.quit();
    }

}
