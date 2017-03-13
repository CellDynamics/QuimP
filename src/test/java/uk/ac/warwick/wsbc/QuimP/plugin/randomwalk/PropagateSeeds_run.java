package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.PropagateSeeds;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkException;

/**
 * @author p.baniukiewicz
 *
 */
public class PropagateSeeds_run {

  static {
    // System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }
  private static final Logger LOGGER = LoggerFactory.getLogger(PropagateSeeds_run.class.getName());

  /**
   * @param args
   * @throws RandomWalkException
   */
  public static void main(String[] args) throws RandomWalkException {
    String folder =
            "/home/baniuk/baniuk1@gmail.com/Warwick/BioinformaticsPaper/Piotr_test_data_src/TestSeg";
    String casename = "RM1_C1-talA_GFP_rnd_motility_FLU";

    int sh = 14;
    int ex = 10;

    String filename = casename.substring(4);
    Path orgimage = Paths.get(folder, casename, filename + ".tif");
    Path maskimage = Paths.get(folder, casename, filename + "_snakemask.tif");
    Path maskimageOutEx = Paths.get(folder, casename, filename + "_snakemask_expand.tif");
    Path maskimageOutSh = Paths.get(folder, casename, filename + "_snakemask_shrink.tif");
    Path compositeOut =
            Paths.get(folder, casename, filename + "_composite_sh" + sh + "_ex" + ex + ".tif");

    ImageJ ij = new ImageJ();
    ImagePlus mask = IJ.openImage(maskimage.toString());
    ImagePlus org = IJ.openImage(orgimage.toString());
    mask.show();
    org.show();

    ImageStack is = mask.getStack();
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true);
    for (int i = 1; i <= is.getSize(); i++) {
      cc.propagateSeed(mask.getStack().getProcessor(i), sh, ex);
      IJ.showProgress(i, is.getSize());
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
    IJ.saveAsTiff(new ImagePlus("red", resultstack_red), maskimageOutSh.toString());
    IJ.saveAsTiff(new ImagePlus("green", resultstack_green), maskimageOutEx.toString());
    IJ.saveAsTiff(ret, compositeOut.toString());

    // ij.quit();
  }

}
