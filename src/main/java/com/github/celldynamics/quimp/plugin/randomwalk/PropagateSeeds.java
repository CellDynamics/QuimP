package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.geom.TrackOutline;
import com.github.celldynamics.quimp.geom.filters.OutlineProcessor;
import com.github.celldynamics.quimp.plugin.ana.ANAp;
import com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters.MorphoOperations;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;
import com.github.celldynamics.quimp.utils.IJTools;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.AutoThresholder;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

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
   * Default setting. Better do not change.
   */
  public boolean darkBackground = true;
  /**
   * Thresholding method used for estimating true background.
   * 
   * <p>If null background is not modified.
   * 
   */
  private AutoThresholder.Method thresholdMethod = null;

  /**
   * Default constructor.
   */
  public PropagateSeeds() {
  }

  /**
   * Allow to store seed history that can be later presented in form of composite image.
   * 
   * @param storeSeeds <tt>true</tt> to store seeds.
   * @param trueBackground if not null, selected method will be used for estimating true background
   *        - excluding bright objects from it
   * @see #getCompositeSeed(ImagePlus, int)
   */
  public PropagateSeeds(boolean storeSeeds, AutoThresholder.Method trueBackground) {
    this.storeSeeds = storeSeeds;
    if (storeSeeds) {
      this.seeds = new ArrayList<>();
    }
    thresholdMethod = trueBackground;
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
   * @see #getCompositeSeed(ImagePlus, int)
   * @see PropagateSeeds#storeSeeds
   */
  protected List<Map<Seeds, ImageProcessor>> seeds;
  /**
   * Scale color values in composite preview.
   * 
   * <p>1.0 stand for opaque colors.
   * 
   * @see #getCompositeSeed(ImagePlus, int)
   */
  public static final double colorScaling = 0.5;

  /**
   * Return demanded propagator.
   * 
   * @param prop propagator to create
   * @param storeseeds true for storing seeds
   * @param trueBackground if not null, selected method will be used for estimating true
   *        background - excluding bright objects from it
   * @return the propagator
   */
  public static PropagateSeeds getPropagator(Propagators prop, boolean storeseeds,
          AutoThresholder.Method trueBackground) {
    switch (prop) {
      case NONE:
        return new PropagateSeeds.Dummy(storeseeds);
      case CONTOUR:
        return new PropagateSeeds.Contour(storeseeds, trueBackground);
      case MORPHOLOGICAL:
        return new PropagateSeeds.Morphological(storeseeds, trueBackground);
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
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Dummy(boolean storeSeeds) {
      binary = new PropagateSeeds.Morphological(storeSeeds, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds#propagateSeed(ij.process.
     * ImageProcessor, double, double)
     * 
     */
    @Override
    public Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, ImageProcessor org,
            double shrinkPower, double expandPower) {
      return binary.propagateSeed(previous, org, 0, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds#getCompositeSeed(ij.ImagePlus)
     */
    @Override
    public ImagePlus getCompositeSeed(ImagePlus org, int offset) throws RandomWalkException {
      // Need override as we have different object here (binary, not this).
      return binary.getCompositeSeed(org, offset);
    }

  }

  /**
   * Contain methods for propagating seeds to the next frame using contour shrinking operations.
   * 
   * <p><t>ampl</t> and <t>shrinkPower</t> specified in
   * {@link #propagateSeed(ImageProcessor, ImageProcessor, double, double)} are related to each
   * other. Larger <t>shrinkPower</t> stands for larger number of steps during shrinking
   * ({@link OutlineProcessor#shrinknl(double, double, double, double, double, double, double)},
   * step
   * size is fixed {@link Contour#stepSize}). Therefore large <t>ampl</t> will move concave node too
   * fast because internally @link Contour#stepSize} is multiplied by factor returned by
   * {@link OutlineProcessor#amplificationFactor(double, double, double)} that ranges 1.0 for
   * positive curvatures to <t>amp</t> for negative with specified <t>sigma</t>. Generally
   * increasing <t>shrinkPower</t>, the <t>ampl</t> should be deceased.
   * 
   * <p>To increase effect of shifting nodes with negative curvature, normals locally can be
   * adjusted according to node with lowest curvature. See
   * {@link OutlineProcessor#shrinknl(double, double, double, double, double, double, double)}
   * 
   * @author p.baniukiewicz
   * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double)
   * @see OutlineProcessor#amplificationFactor(double, double, double)
   */
  public static class Contour extends PropagateSeeds {

    /**
     * Scale magnification. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double)
     */
    public double scaleMagn = 5;
    /**
     * Sigma. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double)
     */
    public double scaleSigma = 0.3;
    /**
     * Distance to set normals equal. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double)
     */
    public double scaleEqNormalsDist = 0;

    /**
     * Step size during object outline shrinking.
     * 
     * @see Outline#scaleOutline(double, double, double, double)
     * @see ANAp
     */
    public static final double stepSize = 0.04;

    /**
     * Default constructor without storing seed history.
     */
    public Contour() {
      this(false, null);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Contour(boolean storeSeeds, AutoThresholder.Method trueBackground) {
      super(storeSeeds, trueBackground);
    }

    /**
     * Allow additionally to set power of shrinking curved parts of the outline and window for
     * equalising normals.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @param sigma sigma of Gaussian
     * @param ampl maximum amplification (for curv<<0)
     * @param normWindow distance to look along for node with smallest curvature (negative) from
     *        current node. Set 0 to turn off this feature. Any value smaller than distance will
     *        cause that at least 3 nodes are taken. See {@link OutlineProcessor}
     * @see Contour#Contour(boolean, ij.process.AutoThresholder.Method)
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double)
     */
    public Contour(boolean storeSeeds, AutoThresholder.Method trueBackground, double sigma,
            double ampl, int normWindow) {
      super(storeSeeds, trueBackground);
      this.scaleMagn = ampl;
      this.scaleSigma = sigma;
      this.scaleEqNormalsDist = normWindow;
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
     * @param org original image that new seeds are computed for. Usually it is current image
     * @param shrinkPower Shrink size for objects in pixels.
     * @param expandPower Expand size used to generate background (object is expanded and then
     *        subtracted from background)
     * @return List of background and foreground coordinates.
     * @see PropagateSeeds.Morphological#propagateSeed(ImageProcessor, ImageProcessor, double,
     *      double)
     * @see Outline#scaleOutline(double, double, double, double)
     * @see #getTrueBackground(ImageProcessor, ImageProcessor)
     * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
     */
    @Override
    public Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, ImageProcessor org,
            double shrinkPower, double expandPower) {
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
          RoiSaver.saveRoi(
                  tmp + File.separator + "propagateSeed_" + time + "_" + outlines.hashCode(),
                  o.asList());
        }
      }
      small.setColor(Color.WHITE); // for fill(Roi)
      for (Outline o : outlines) {
        if (o.getNumPoints() < 4) {
          continue;
        }
        // shrink outline - copy as we want to expand it later
        Outline copy = new Outline(o);
        new OutlineProcessor<Outline>(copy).shrinknl(stepsshrink, stepSize, 0.1, 1.5, scaleSigma,
                scaleMagn, scaleEqNormalsDist);
        copy.unfreezeAll();
        Roi fr = copy.asIntRoi();
        fr.setFillColor(Color.WHITE);
        fr.setStrokeWidth(1.1);
        fr.setStrokeColor(Color.WHITE);
        small.fill(fr);
        small.drawRoi(fr);
        // small.resetRoi();
      }

      for (Outline o : outlines) {
        if (o.getNumPoints() < 4) {
          continue;
        }
        // frezeTh influences artifacts that appear when concave regions are expanded
        // 0 prevent a little
        // TODO check if expand shuold use normWindow
        new OutlineProcessor<Outline>(o).shrinknl(stepsexp, -stepSize, 0.1, 0, scaleSigma,
                scaleMagn, 0);
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
      ret.put(Seeds.BACKGROUND, getTrueBackground(big, org));
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
    public static List<Outline> getOutline(ImageProcessor previous) {
      TrackOutlineLocal track = new TrackOutlineLocal(previous, 0);
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
      this(false, null);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Morphological(boolean storeSeeds, AutoThresholder.Method trueBackground) {
      super(storeSeeds, trueBackground);
    }

    /**
     * Generate new seeds using segmented image.
     * 
     * <p>Setting <tt>shrinkPower</tt> or <tt>expandPower</tt> to zero prevents contour
     * modifications.
     * 
     * @param previous segmented image, background on zero
     * @param org original image that new seeds are computed for. Usually it is current image
     * @param shrinkPower number of erode iterations
     * @param expandPower number of dilate iterations
     * 
     * @return Map containing list of coordinates that belong to foreground and background. Map is
     *         addressed by two enums: <tt>FOREGROUND</tt> and <tt>BACKGROUND</tt>
     * @see RandomWalkSegmentation#decodeSeeds(ImagePlus, Color, Color)
     * @see #getTrueBackground(ImageProcessor, ImageProcessor)
     * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
     */
    @Override
    public Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous, ImageProcessor org,
            double shrinkPower, double expandPower) {
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
      ret.put(Seeds.BACKGROUND, getTrueBackground(big, org));
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
   * @param offset Slice number to display in composite if there is stack provided. Ignored if org
   *        is single image. Set it to 0 to dispplay whole stack.
   * @return Composite image with marked foreground and background.
   * @throws RandomWalkException When seeds were not collected.
   */
  public ImagePlus getCompositeSeed(ImagePlus org, int offset) throws RandomWalkException {
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
    if (org.getStack().getSize() == 1) { // single image
      ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack.getProcessor(1)),
              new ImagePlus("", bigstack.getProcessor(1)));
    } else {
      if (offset > 0) { // stack but show only one image
        ImageProcessor tmp = org.getStack().getProcessor(offset).duplicate();
        ret = IJTools.getComposite(new ImagePlus("", tmp), new ImagePlus("", smallstack),
                new ImagePlus("", bigstack));
      } else { // stack
        ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack),
                new ImagePlus("", bigstack));
      }
    }
    return ret;
  }

  /**
   * Propagate seed.
   *
   * @param previous the previous
   * @param org original image that new seeds are computed for. Usually it is current image
   * @param shrinkPower the shrink power
   * @param expandPower the expand power
   * @return the map
   * @see #getTrueBackground(ImageProcessor, ImageProcessor)
   * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
   */
  public abstract Map<Seeds, ImageProcessor> propagateSeed(ImageProcessor previous,
          ImageProcessor org, double shrinkPower, double expandPower);

  /**
   * Excludes objects from estimated background.
   * 
   * <p>If seed propagator is used, background is obtained by expanding initially segmented cell and
   * then negating the image. Thus background covers all area except cell. If there are other cell
   * there they can influence background mean. To avoid this, that background is thresholded to
   * detect objects that should be excluded from mean. This should be used when there are other
   * objects around.
   * 
   * @param bck Background (white) estimated from Propagator
   * @param org Original 8-bit image
   * @return Background without objects above threshold
   * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
   */
  ImageProcessor getTrueBackground(ImageProcessor bck, ImageProcessor org) {
    if (thresholdMethod == null) {
      return bck;
    }
    ImageProcessor orgD = org.duplicate();
    orgD.threshold(new AutoThresholder().getThreshold(thresholdMethod, orgD.getHistogram()));
    orgD.invert();
    orgD.copyBits(bck, 0, 0, Blitter.AND); // cut
    return orgD;
  }

  /**
   * Turn on processing background before using it as seed.
   * 
   * @param method Threshold method. null for turning off processing
   */
  public void setTrueBackgroundProcessing(AutoThresholder.Method method) {
    thresholdMethod = method;
  }

}

/**
 * In purpose of overriding {@link TrackOutline#prepare()} which in super class can remove this
 * lines.
 * 
 * @author p.baniukiewicz
 *
 */
class TrackOutlineLocal extends TrackOutline {

  /**
   * Default constructor here.
   * 
   * @param imp image to process
   * @param background background color
   */
  public TrackOutlineLocal(ImageProcessor imp, int background) {
    super(imp, background);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.geom.TrackOutline#prepare()
   */
  @Override
  public ImageProcessor prepare() {
    ImageProcessor filtered = imp.duplicate();
    return filtered;
  }

}
