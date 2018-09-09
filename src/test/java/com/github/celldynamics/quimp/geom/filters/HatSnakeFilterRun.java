package com.github.celldynamics.quimp.geom.filters;

import java.awt.Color;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.binaryseg.BinarySegmentation;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;

/**
 * HatSnakeFilterRun.
 * 
 * @author p.baniukiewicz
 *
 */
public class HatSnakeFilterRun {
  static {
    // System.setProperty("logback.configurationFile", "quimp-logback.xml");
    // System.setProperty("quimpconfig.superDebug", "true");
  }

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(HatSnakeFilterRun.class.getName());

  /**
   * Runner.
   * 
   * @param args args
   * @throws Exception on error
   */
  public static void main(String[] args) throws Exception {
    String folder =
            "src/test/Resources-static/com.github.celldynamics.quimp.geom.filters.HatSnakeFilter/";
    String casename = "test_C1-talA_GFP_rnd_motility_FLU";
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 0.0); // head next

    final int step = 1;

    String filename = casename.substring(5);
    Path maskimage = Paths.get(folder, filename + "_snakemaskO.tif");
    Path orgimage = Paths.get(folder, filename + ".tif");

    int pp = 58; // fram counted from 1

    ImageJ ij = new ImageJ();
    ImagePlus mask = IJ.openImage(maskimage.toString());
    ImagePlus orgim = IJ.openImage(orgimage.toString());
    mask.setSlice(pp);
    mask.show();
    orgim.setSlice(pp);
    orgim.show();

    BinarySegmentation obj = new BinarySegmentation(mask);
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains();
    for (ArrayList<SegmentedShapeRoi> asS : ret) {
      for (SegmentedShapeRoi ss : asS) {
        ss.setInterpolationParameters(step, false);
      }
    }

    // filter
    final int window = 17;
    final int pnum = 0;
    final double alev = 0.008;

    ImagePlus filtered = NewImage.createByteImage("filt", mask.getWidth(), mask.getHeight(),
            mask.getStackSize(), NewImage.GRAY8);
    ImagePlus org = NewImage.createByteImage("org", mask.getWidth(), mask.getHeight(),
            mask.getStackSize(), NewImage.GRAY8);
    for (int i = 1; i <= 100; i++) { // !!
      LOGGER.info("--Frame " + i);
      SegmentedShapeRoi ssR = ret.get(0).get(i - 1);
      org.getStack().getProcessor(i).setColor(Color.WHITE);
      org.getStack().getProcessor(i).draw(ssR);
      org.getStack().getProcessor(i).invert();

      HatSnakeFilter hsf = new HatSnakeFilter(window, pnum, alev);
      hsf.setMode(HatSnakeFilter.CAVITIES);

      // List<Point2d> rr = ssR.getOutlineasPoints();
      // FileOutputStream fos = new FileOutputStream("/tmp/examplaryContour.tmp");
      // ObjectOutputStream oos = new ObjectOutputStream(fos);
      // oos.writeObject(rr);

      List<Point2d> cc = ssR.getOutlineasPoints();
      // Pair<ArrayList<Double>, ArrayList<Boolean>> rank =
      // hsf.calculateRank(cc, orgim.getStack().getProcessor(i));
      List<Point2d> retf = hsf.runPlugin(cc, orgim.getStack().getProcessor(i));
      Roi ssRF = new QuimpDataConverter(retf).getSnake(0).asFloatRoi();

      // plot test

      filtered.getStack().getProcessor(i).setColor(Color.WHITE);
      filtered.getStack().getProcessor(i).draw(ssRF);
      filtered.getStack().getProcessor(i).invert();
      IJ.showProgress(i, mask.getStackSize());
      LOGGER.info("------");
    }
    filtered.setSlice(pp);
    filtered.show();
    org.setSlice(pp);
    org.show();
    f.setDouble(Shape.class, 0.5); // restore
    // both original and result image can be combined as composite to check what parts were removed.
    // By default program logs alev value computed for every frame for removed parts.

  }

}
