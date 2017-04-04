package uk.ac.warwick.wsbc.quimp;

import ij.IJ;
import ij.ImageJ;
import uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentationPlugin;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentationPlugin_;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class BinarySegmentationUI_run {

  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * 
   */
  public BinarySegmentationUI_run() {
  }

  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) {

    ImageJ ij = new ImageJ();
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    // load images that are called from RandomWalkSegmentationPlugin
    IJ.runMacro("open(\"/home/p.baniukiewicz/Documents/"
            + "Repos/QuimP/src/test/Resources-static/BW_seg_5_slices.tif\")");
    IJ.runMacro("open(\"/home/p.baniukiewicz/Documents/Repos/"
            + "QuimP/src/test/Resources-static/segmented_color.tif\")");
    BinarySegmentationPlugin fp = new BinarySegmentationPlugin();
    fp.attachData(null);
    fp.showUi(true);

  }

}
