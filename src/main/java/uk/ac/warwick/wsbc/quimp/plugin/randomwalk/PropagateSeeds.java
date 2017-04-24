package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.geom.TrackOutline;
import uk.ac.warwick.wsbc.quimp.geom.filters.OutlineProcessor;
import uk.ac.warwick.wsbc.quimp.plugin.ana.ANAp;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.BinaryFilters.MorphoOperations;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;
import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;
import uk.ac.warwick.wsbc.quimp.utils.IJTools;

/**
 * Generate new seeds for n+1 frame in stack using previous results of segmentation.
 * 
 * <p>This class supports two methods:
 * <ol>
 * <li>Based on morphological operations
 * <li>Based on contour shrinking (part of QuimP Outline framework)
 * </ol>
 * 
 * <p>In both cases the aim is to shrink the object (which is white) to prevent overlapping
 * foreground
 * and background in next frame (assuming that objects are moving). The same is for background.
 * Finally, the new seed should have set foreground pixels to area inside the object and background
 * pixels in remaining part of image. There should be unseeded strip of pixels around the object.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class PropagateSeeds {

  /**
   * Seed propagators available in this class.
   * 
   * @author p.baniukiewicz
   *
   */
  public enum Propagators {
    /**
     * Just copy input as output.
     */
    NONE,
    /**
     * Use contour shrinking.
     * 
     * @see Contour
     */
    CONTOUR,
    /**
     * Use morphological operations.
     * 
     * @see Morphological
     */
    MORPHOLOGICAL

  }

  /**
   * Default constructor.
   */
  public PropagateSeeds() {
  }

  /**
   * Allow to store seed history that can be later presented in form of composite image.
   * 
   * @param storeSeeds <tt>true</tt> to store seeds.
   * @see #getCompositeSeed(ImagePlus)
   */
  public PropagateSeeds(boolean storeSeeds) {
    this.storeSeeds = storeSeeds;
    if (storeSeeds) {
      this.seeds = new ArrayList<>();
    }
  }

  /**
   * Default resolution used during outlining objects.
   * 
   * @see Contour#getOutline(ImageProcessor)
   */
  public static final int STEPS = 4;
  /**
   * By default seed history is not stored.
   */
  protected boolean storeSeeds = false;
  /**
   * Container for FG and BG seeds pixels used for seed visualisation.
   * 
   * <p>Every imageProcessor in pair contains important bits set to WHITE. For example BG pixels are
   * white here as well as FG pixels.
   * 
   * @see #getCompositeSeed(ImagePlus)
   * @see PropagateSeeds#storeSeeds
   */
  protected List<Map<Seeds, ImageProcessor>> seeds;
  /**
   * Scale color values in composite preview.
   * 
   * <p>1.0 stand for opaque colors.
   * 
   * @see #getCompositeSeed(ImagePlus)
   */
  public static final double colorScaling = 0.5;

  /**
   * Return demanded propagator.
   * 
   * @param prop propagator to create
   * @param storeseeds true for storig seeds
   * @return the propagator
   */
  public static PropagateSeeds getPropagator(Propagators prop, boolean storeseeds) {
    switch (prop) {
      case NONE:
        return new PropagateSeeds.Dummy(storeseeds);
      case CONTOUR:
        return new PropagateSeeds.Contour(storeseeds);
      case MORPHOLOGICAL:
        return new PropagateSeeds.Morphological(storeseeds);
      default:
        throw new IllegalArgumentException("Unknown propagator");
    }
  }

  /**
   * Empty propagator. Do nothing.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Dummy extends PropagateSeeds {

    PropagateSeeds binary;

    /**
     * Default constructor without storing seed history.
     */
    public Dummy() {
      binary = new PropagateSeeds.Morphological();
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @see #getCompositeSeed(ImagePlus)
     */
    public Dummy(boolean storeSeeds) {
      binary = new PropagateSeeds.Morphological(storeSeeds);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.plugin.randomwalk.PropagateSeeds#propagateSeed(ij.process.
     * ImageProcessor, double, double)
     * 
     */
    @Override
    Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, double shrinkPower,
            double expandPower) {
      return binary.propagateSeed(previous, 0, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.plugin.randomwalk.PropagateSeeds#getCompositeSeed(ij.ImagePlus)
     */
    @Override
    public ImagePlus getCompositeSeed(ImagePlus org) throws RandomWalkException {
      // Need override as we have different object here (binary, not this).
      return binary.getCompositeSeed(org);
    }

  }

  /**
   * Contain methods for propagating seeds to the next frame using contour shrinking operations.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Contour extends PropagateSeeds {

    /**
     * Step size during object outline shrinking.
     * 
     * @see OutlineProcessor#shrink(double, double, double, double)
     * @see ANAp
     */
    public static final double stepSize = 0.04;

    /**
     * Default constructor without storing seed history.
     */
    public Contour() {
      this(false);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @see #getCompositeSeed(ImagePlus)
     */
    public Contour(boolean storeSeeds) {
      super(storeSeeds);
    }

    /**
     * Generate seeds for next frame using provided mask.
     * 
     * <p>The mask provided to this method is shrunk to get new seeds of object (that can move
     * meanwhile). The same mask is expanded and subtracted from image forming the background.
     * 
     * <p>Setting <tt>shrinkPower</tt> or <tt>expandPower</tt> to zero prevents contour
     * modifications.
     * 
     * @param previous Previous result of segmentation. BW mask with white object on black
     *        background.
     * @param shrinkPower Shrink size for objects in pixels.
     * @param expandPower Expand size used to generate background (object is expanded and then
     *        subtracted from background)
     * @return List of background and foreground coordinates.
     * @see PropagateSeeds.Morphological#propagateSeed(ImageProcessor, double, double)
     * @see OutlineProcessor#shrink(double, double, double, double)
     */
    @Override
    public Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, double shrinkPower,
            double expandPower) {
      ByteProcessor small = new ByteProcessor(previous.getWidth(), previous.getHeight());
      ByteProcessor big = new ByteProcessor(previous.getWidth(), previous.getHeight());
      small.setColor(Color.BLACK);
      small.fill();
      big.setColor(Color.BLACK);
      big.fill();
      double stepsshrink = shrinkPower / stepSize; // total shrink/step size
      double stepsexp = (expandPower) / stepSize; // total shrink/step size

      List<Outline> outlines = getOutline(previous);

      // save extra debug info if property set
      if (QuimP.SUPER_DEBUG) {
        String tmp = System.getProperty("java.io.tmpdir");
        for (Outline o : outlines) {
          long time = new Date().getTime();
          RoiSaver.saveROI(
                  tmp + File.separator + "propagateSeed_" + time + "_" + outlines.hashCode(),
                  o.asList());
        }
      }

      for (Outline o : outlines) {
        // shrink outline - copy as we want to expand it later
        Outline copy = new Outline(o);
        new OutlineProcessor(copy).shrinknl(stepsshrink, stepSize, 0.1, 1); // taken from anap
        copy.unfreezeAll();
        Roi fr = copy.asFloatRoi();
        fr.setFillColor(Color.WHITE);
        fr.setStrokeColor(Color.WHITE);
        small.drawRoi(fr);
      }

      for (Outline o : outlines) {
        // frezeTh influences artifacts that appear when concave regions are expanded
        // 0 prevent a liitle
        new OutlineProcessor(o).shrinknl(stepsexp, -stepSize, 0.1, 0); // taken from anap
        o.unfreezeAll();
        Roi fr = o.asFloatRoi();
        fr.setFillColor(Color.WHITE);
        fr.setStrokeColor(Color.WHITE);
        big.drawRoi(fr);
      }
      big.invert();
      // store seeds if option ticked
      Map<Seeds, ImageProcessor> ret = new HashMap<Seeds, ImageProcessor>(2);
      ret.put(Seeds.FOREGROUND, small);
      ret.put(Seeds.BACKGROUND, big);
      if (storeSeeds) {
        seeds.add(ret);
      }

      return ret;

    }

    /**
     * Convert mask to outline.
     * 
     * @param previous image to outline. White object on black background.
     * @return List of Outline for current frame
     * @see TrackOutline
     */
    private List<Outline> getOutline(ImageProcessor previous) {
      TrackOutline track = new TrackOutline(previous, 0);
      return track.getOutlines(STEPS, false);
    }

  }

  /**
   * Contain methods for propagating seeds to next frame using morphological operations.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Morphological extends PropagateSeeds {

    /**
     * Default constructor without storing seed history.
     */
    public Morphological() {
      this(false);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @see #getCompositeSeed(ImagePlus)
     */
    public Morphological(boolean storeSeeds) {
      super(storeSeeds);
    }

    /**
     * Generate new seeds using segmented image.
     * 
     * <p>Setting <tt>shrinkPower</tt> or <tt>expandPower</tt> to zero prevents contour
     * modifications.
     * 
     * @param previous segmented image, background on \b zero
     * @param shrinkPower number of erode iterations
     * @param expandPower number of dilate iterations
     * 
     * @return Map containing list of coordinates that belong to foreground and background. Map is
     *         addressed by two enums: <tt>FOREGROUND</tt> and <tt>BACKGROUND</tt>
     * @see RandomWalkSegmentation#decodeSeeds(ImagePlus, Color, Color)
     */
    @Override
    public Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, double shrinkPower,
            double expandPower) {
      ImageProcessor cp = previous;
      // object smaller than on frame n
      ImageProcessor small = cp.duplicate();
      // object bigger than on frame n
      ImageProcessor big = cp.duplicate();
      // make objects smaller
      small = BinaryFilters.iterateMorphological(small, MorphoOperations.ERODE, shrinkPower);
      // make background bigger
      big = BinaryFilters.iterateMorphological(big, MorphoOperations.DILATE, expandPower);

      // apply big to old background making object bigger and prevent covering objects on
      // frame
      // n+1
      // by previous background (make "empty" not seeded space around objects)
      // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_bigbef.tif");
      // IJ.saveAsTiff(new ImagePlus("", cp), "/tmp/testIterateMorphological_cp.tif");
      for (int x = 0; x < cp.getWidth(); x++) {
        for (int y = 0; y < cp.getHeight(); y++) {
          big.putPixel(x, y, big.getPixel(x, y) | cp.getPixel(x, y));
        }
      }

      big.invert(); // invert to have BG pixels white in seed. (required by convertToList)
      // store seeds if option ticked
      Map<Seeds, ImageProcessor> ret = new HashMap<Seeds, ImageProcessor>(2);
      ret.put(Seeds.FOREGROUND, small);
      ret.put(Seeds.BACKGROUND, big);
      if (storeSeeds) {
        seeds.add(ret);
      }

      return ret;
    }

  }

  /**
   * Produce composite image containing seeds generated during segmentation of particular frames.
   * 
   * <p>To have this method working, the Contour object must be created with storeSeeds==true.
   * 
   * @param org Original image (or stack) where composite layer will be added to.
   * @return Composite image with marked foreground and background.
   * @throws RandomWalkException When seeds were not collected.
   */
  public ImagePlus getCompositeSeed(ImagePlus org) throws RandomWalkException {
    ImagePlus ret;
    if (seeds == null || seeds.size() == 0) {
      throw new RandomWalkException(
              "Seeds were not stored. You need at least two time frames to collect one seed");
    }
    ImageStack smallstack = new ImageStack(seeds.get(0).get(Seeds.FOREGROUND).getWidth(),
            seeds.get(0).get(Seeds.FOREGROUND).getHeight());
    ImageStack bigstack = new ImageStack(seeds.get(0).get(Seeds.FOREGROUND).getWidth(),
            seeds.get(0).get(Seeds.FOREGROUND).getHeight());
    for (Map<Seeds, ImageProcessor> p : seeds) {
      // just in case convert to byte
      ImageProcessor fg = (ImageProcessor) p.get(Seeds.FOREGROUND).convertToByte(true);
      ImageProcessor bg = (ImageProcessor) p.get(Seeds.BACKGROUND).convertToByte(true);
      // make colors transparent
      bg.multiply(colorScaling);
      fg.multiply(colorScaling);
      // set gray lut just in case
      fg.setLut(IJTools.getGrayLut());
      bg.setLut(IJTools.getGrayLut());
      smallstack.addSlice((ImageProcessor) fg);
      bigstack.addSlice((ImageProcessor) bg);
    }
    // check if stack or not. getComposite requires the same type
    if (org.getStack().getSize() == 1) {
      ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack.getProcessor(1)),
              new ImagePlus("", bigstack.getProcessor(1)));
    } else {
      ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack),
              new ImagePlus("", bigstack));
    }
    return ret;
  }

  /**
   * Propagate seed.
   *
   * @param previous the previous
   * @param shrinkPower the shrink power
   * @param expandPower the expand power
   * @return the map
   */
  abstract Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, double shrinkPower,
          double expandPower);

}
