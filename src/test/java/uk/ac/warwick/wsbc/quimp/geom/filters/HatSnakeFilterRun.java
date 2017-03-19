package uk.ac.warwick.wsbc.quimp.geom.filters;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentation;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

/**
 * @author p.baniukiewicz
 *
 */
public class HatSnakeFilterRun {
  static {
    // System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(HatSnakeFilterRun.class.getName());

  /**
   * Runner.
   * 
   * @param args args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String folder = "/home/baniuk/Documents/BioinformaticsPaper/" + "Piotr_test_data_src/TestSeg";
    String casename = "RW1_C1-talA_GFP_rnd_motility_FLU";

    final int step = 1;

    String filename = casename.substring(4);
    Path maskimage = Paths.get(folder, casename, filename + "_snakemask.tif");

    int i = 56; // fram counted from 1

    ImageJ ij = new ImageJ();
    ImagePlus mask = IJ.openImage(maskimage.toString());
    mask.setSlice(i);
    mask.show();

    BinarySegmentation obj = new BinarySegmentation(mask);
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains();
    for (ArrayList<SegmentedShapeRoi> asS : ret) {
      for (SegmentedShapeRoi sS : asS) {
        sS.setInterpolationParameters(step, false);
      }
    }

    // filter
    final int window = 15;
    final int pnum = 1;
    final double alev = 0.71;

    ImagePlus filtered = NewImage.createByteImage("filt", mask.getWidth(), mask.getHeight(),
            mask.getStackSize(), NewImage.GRAY8);
    ImagePlus org = NewImage.createByteImage("org", mask.getWidth(), mask.getHeight(),
            mask.getStackSize(), NewImage.GRAY8);
    for (i = 1; i <= 100; i++) {// !!
      LOGGER.info("--Frame " + i);
      SegmentedShapeRoi ssR = ret.get(0).get(i - 1);
      org.getStack().getProcessor(i).setColor(Color.WHITE);
      org.getStack().getProcessor(i).draw(ssR);
      org.getStack().getProcessor(i).invert();

      HatSnakeFilter hsf = new HatSnakeFilter(window, pnum, alev);
      List<Point2d> retf = hsf.runPlugin(ssR.getOutlineasPoints());
      Roi ssRF = new QuimpDataConverter(retf).getSnake(0).asFloatRoi();

      // plot test

      filtered.getStack().getProcessor(i).setColor(Color.WHITE);
      filtered.getStack().getProcessor(i).draw(ssRF);
      filtered.getStack().getProcessor(i).invert();
      IJ.showProgress(i, mask.getStackSize());
      LOGGER.info("------");
    }
    filtered.setSlice(i);
    filtered.show();
    org.setSlice(i);
    org.show();
    // both original and result image can be combined as composite to check what parts were removed.
    // By default program logs alev value computed for every frame for removed parts.

  }

}
