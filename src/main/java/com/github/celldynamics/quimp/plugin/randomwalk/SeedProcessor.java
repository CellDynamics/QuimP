package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * Contain various methods for converting labelled images to Seeds.
 * 
 * TODO consider to return each FG seed in different grayscale, even if they are at separate image
 * 
 * @author p.baniukiewicz
 *
 */
public class SeedProcessor {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SeedProcessor.class.getName());

  /**
   * Decode RGB seed images to separate binary images. Support multiple foregrounds labels.
   * 
   * <p>Seeded RGB image is decomposed to separated binary images that contain only seeds.
   * E.g. <b>FOREGROUND</b> image will have only pixels that were labelled as foreground.
   * Decomposition is performed with respect to provided label colours.
   * 
   * @param rgb original image
   * @param fseed foreground seed image
   * @param bseed background seed image
   * @return Seed structure with separated seeds
   * @throws RandomWalkException on problems with decoding, unsupported image or empty list.
   *         Exception is thrown only when all seed images for key are empty
   * @see SeedProcessor#decodeSeedsfromRgb(ImagePlus, List, Color)
   */
  public static Seeds decodeSeedsfromRgb(final ImageProcessor rgb, final List<Color> fseed,
          final Color bseed) throws RandomWalkException {
    // output map integrating two lists of points
    Seeds out = new Seeds(2);
    // output lists of points. Can be null if points not found
    ImageProcessor background = new ByteProcessor(rgb.getWidth(), rgb.getHeight());
    // verify input condition
    if (rgb.getBitDepth() != 24) {
      throw new RandomWalkException("Unsupported seed image type");
    }
    List<Color> fseeds = new ArrayList<>(fseed);
    fseeds.add(bseed); // integrate in the same list
    // find marked pixels
    ColorProcessor cp = (ColorProcessor) rgb; // can cast here because of type checking
    for (Color color : fseeds) { // iterate over all fg (and one bg) seeds
      // foreground for current color
      ImageProcessor foreground = new ByteProcessor(rgb.getWidth(), rgb.getHeight());
      for (int x = 0; x < cp.getWidth(); x++) {
        for (int y = 0; y < cp.getHeight(); y++) {
          Color c = cp.getColor(x, y); // get color for pixel
          if (c.equals(color)) { // if current pixel has "our" color
            if (color.equals(bseed)) { // and it is from background seed
              background.putPixel(x, y, 255); // add it to background map
            } else { // otherwise store in foreground
              foreground.putPixel(x, y, 255); // remember foreground coords
            }
          }
        }
      }
      if (color.equals(bseed)) {
        out.put(RandomWalkSegmentation.SeedTypes.BACKGROUND, background); // only one background
      } else {
        // many possible foregrounds
        out.put(RandomWalkSegmentation.SeedTypes.FOREGROUNDS, foreground);
      }
    }
    // check if there is at least one seed pixel in any seed map
    validateSeeds(out, SeedTypes.FOREGROUNDS);
    validateSeeds(out, SeedTypes.BACKGROUND);

    return out;
  }

  /**
   * Decode RGB seed images to separate binary images. Support multiple foregrounds labels.
   * 
   * <p>Seeded RGB image is decomposed to separated binary images that contain only seeds.
   * E.g. <b>FOREGROUND</b> image will have only pixels that were labelled as foreground.
   * Decomposition is performed with respect to provided label colours.
   * 
   * @param rgb RGB seed image
   * @param fseed color of marker for foreground pixels
   * @param bseed color of marker for background pixels
   * @return Map containing extracted seed pixels from input RGB image that belong to foreground and
   *         background. Map is addressed by two enums: <i>FOREGROUND</i> and <i>BACKGROUND</i>
   * @throws RandomWalkException When image other that RGB provided
   * @see #decodeSeedsfromRgb(ImageProcessor, List, Color)
   */
  public static Seeds decodeSeedsfromRgb(final ImagePlus rgb, final List<Color> fseed,
          final Color bseed) throws RandomWalkException {
    if (rgb.getType() != ImagePlus.COLOR_RGB) {
      throw new RandomWalkException("Unsupported image type");
    }
    return decodeSeedsfromRgb(rgb.getProcessor(), fseed, bseed);
  }

  /**
   * Validate if at least one map in specified seed type contains non-zero pixel.
   * 
   * @param seeds seeds to verify
   * @param type seed type
   * @throws RandomWalkException when all maps under specified key are empty (black). Allows empty
   *         or nonexisting keys
   */
  public static void validateSeeds(Seeds seeds, SeedTypes type) throws RandomWalkException {
    if (seeds.get(type) == null || seeds.get(type).isEmpty()) {
      return;
    }
    // check if there is at least one seed pixel in any seed map
    int pixelsNum = 0;
    int pixelHistNum = 0;
    // iterate over foregrounds
    for (ImageProcessor i : seeds.get(type)) {
      int[] histfg = i.getHistogram();
      pixelHistNum += histfg[0]; // sum number of background pixels for each map
      pixelsNum += i.getPixelCount(); // sum number of all pixels
    }
    if (pixelHistNum == pixelsNum) {
      throw new RandomWalkException(
              "Seed pixels are empty, check if:\n- correct colors were used\n- all slices have"
                      + " been seeded (if stacked seed is used)\n"
                      + "- Shrink/expand parameters are not too big.");
    }
  }

  /**
   * Decode seeds from list of ROIs objects.
   * 
   * <p>ROIs naming must comply with the following pattern: coreID_NO, where core is different for
   * FG and BG objects, ID is the id of object and NO is its number unique within ID (one object can
   * ba labelled by several separated ROIs). All ROIs must have name set otherwise
   * NullPointException is thrown.
   * 
   * @param rois list of ROIs with names
   * @param fgName core for FG ROI name
   * @param bgName core for BG core name
   * @param width width of output map
   * @param height height of output map
   * @return Seed structure with FG and BG maps.
   * @throws RandomWalkException when all seeds are empty (but maps exist)
   */
  public static Seeds decodeSeedsRoi(List<Roi> rois, String fgName, String bgName, int width,
          int height) throws RandomWalkException {
    Seeds ret = new Seeds();

    List<Roi> fglist = rois.stream().filter(roi -> roi.getName().startsWith(fgName))
            .collect(Collectors.toList());

    List<Roi> bglist = rois.stream().filter(roi -> roi.getName().startsWith(bgName))
            .collect(Collectors.toList());

    // process backgrounds - all rois as one ID
    ImageProcessor bg = new ByteProcessor(width, height);
    bg.setColor(Color.WHITE);
    for (Roi r : bglist) {
      r.setFillColor(Color.WHITE);
      r.setStrokeColor(Color.WHITE);
      bg.draw(r);
    }
    if (!bglist.isEmpty()) {
      ret.put(SeedTypes.BACKGROUND, bg);
    }

    ArrayList<Integer> ind = new ArrayList<>(); // array of cell numbers
    // init color for roi and collect unique cell id from name fgNameId_no
    for (Roi r : fglist) {
      r.setFillColor(Color.WHITE);
      r.setStrokeColor(Color.WHITE);
      String name = r.getName();
      String i = name.substring(fgName.length(), name.length());
      Integer n = Integer.parseInt(i.substring(0, i.indexOf("_")));
      ind.add(n);
    }
    // remove duplicates from ids
    List<Integer> norepeat = ind.stream().distinct().collect(Collectors.toList());
    Collections.sort(norepeat); // unique ROIs
    for (Integer i : norepeat) { // over unique
      ImageProcessor fg = new ByteProcessor(width, height);
      fg.setColor(Color.WHITE);
      // find all with the same No within Id and add to image
      fglist.stream().filter(roi -> roi.getName().startsWith(fgName + i))
              .forEach(roi -> fg.draw(roi));
      ret.put(SeedTypes.FOREGROUNDS, fg);
    }

    // check if there is at least one seed pixel in any seed map
    validateSeeds(ret, SeedTypes.FOREGROUNDS);
    validateSeeds(ret, SeedTypes.BACKGROUND);
    // LOGGER.debug(norepeat.toString());
    // new ImagePlus("", ret.get(0).get(SeedTypes.BACKGROUND, 1)).show();
    // List<ImageProcessor> tmp = ret.get(0).get(SeedTypes.FOREGROUNDS);
    // for (ImageProcessor ip : tmp) {
    // new ImagePlus("", ip).show();
    // }
    LOGGER.debug("Found " + norepeat.size() + " FG objects (" + fglist.size() + " total) and "
            + bglist.size() + " BG seeds");
    return ret;
  }

  /**
   * Convert {@link Seeds} object into one-slice grayscale image. Background map have maximum
   * intensity.
   * 
   * <p>If there is more than one BG map it is not possible to find which of last colours are they.
   * 
   * @param seeds seeds to convert
   * @return Image with seeds in gray scale. Background is last (brightest). Null if there is no FG.
   *         Empty BG is allowed.
   */
  public static ImageProcessor getSeedsAsGrayscale(Seeds seeds) {
    if (seeds.get(SeedTypes.FOREGROUNDS) == null) {
      return null;
    }
    ImageProcessor fg = flatten(seeds, SeedTypes.FOREGROUNDS, 1);
    ImageStatistics stat = fg.getStats();
    ImageProcessor bg = flatten(seeds, SeedTypes.BACKGROUND, (int) stat.max + 1);
    ImageStack stack = new ImageStack(fg.getWidth(), fg.getHeight());
    stack.addSlice(fg);
    stack.addSlice(bg);

    ImagePlus im = new ImagePlus("", stack);
    ZProjector z = new ZProjector(im);
    z.setImage(im);
    z.setMethod(ZProjector.MAX_METHOD);
    z.doProjection();
    ImageProcessor ret = z.getProjection().getProcessor();
    return ret;
  }

  /**
   * Flatten seeds of specified type ad output grayscale image.
   * 
   * @param seeds seeds to flatten
   * @param type which map
   * @param initialValue brightness value to start from (typically 1)
   * @return Image with seeds in gray scale or null if input does not contain specified map
   */
  public static ImageProcessor flatten(Seeds seeds, SeedTypes type, int initialValue) {
    if (seeds.get(type) == null) {
      return null;
    }
    int currentVal = initialValue;
    // assume same sizes of seeds
    ImageStack stack = seeds.convertToStack(type).duplicate();
    for (int s = 1; s <= stack.size(); s++) {
      stack.getProcessor(s).multiply(1.0 * currentVal / 255);
      currentVal++;
    }
    ImagePlus im = new ImagePlus("", stack);
    ZProjector z = new ZProjector(im);
    z.setImage(im);
    z.setMethod(ZProjector.MAX_METHOD);
    z.doProjection();
    ImageProcessor ret = z.getProjection().getProcessor();
    return ret;
  }

  /**
   * Convert grayscale image to {@link Seeds}.
   * 
   * <p>Pixels with the same intensity are collected in one map at {@link Seeds} structure under
   * {@link SeedTypes#FOREGROUNDS} key. Works for separated objects as
   * well. Collected maps are binary.
   * 
   * @param im 8-bit grayscale image, 0 is background
   * @return Seeds with {@link SeedTypes#FOREGROUNDS} filled. No {@link SeedTypes#BACKGROUND}
   * @throws RandomWalkException when all output FG seed maps are empty
   */
  public static Seeds getGrayscaleAsSeeds(ImageProcessor im) throws RandomWalkException {
    Seeds ret = new Seeds(2);
    ImageStatistics stats = im.getStats();
    int max = (int) stats.max; // max value
    // list of all possible theoretical values of labels in image processor except background
    // 1...max(im)
    List<Integer> lin = IntStream.rangeClosed(1, max).boxed().collect(Collectors.toList());
    ImageProcessor tmp = new ByteProcessor(im.getWidth(), im.getHeight());
    // create requested number of foreground maps
    for (int i = 0; i < max; i++) {
      ret.put(SeedTypes.FOREGROUNDS, tmp.duplicate());
    }
    // fill each map with corresponding values
    for (int r = 0; r < im.getHeight(); r++) {
      for (int c = 0; c < im.getWidth(); c++) {
        int pixel = (int) im.getPixelValue(r, c); // color of the pixel = slice in FG maps
        if (pixel > 0) { // skip background
          ret.get(SeedTypes.FOREGROUNDS).get(pixel - 1).set(r, c, Color.WHITE.getBlue());
          // remove this map number from list - for further detection gaps in grayscale seeds that
          // impose empty FG
          lin.remove(new Integer(pixel));
        }
      }
    }
    // now lin should be empty, if not it means that there are gaps and some maps were not touched
    if (!lin.isEmpty()) {
      // remove starting from end
      Collections.reverse(lin);
      for (Integer i : lin) {
        ret.get(SeedTypes.FOREGROUNDS).remove(i.intValue() - 1);
      }
    }
    validateSeeds(ret, SeedTypes.FOREGROUNDS);
    return ret;
  }

  /**
   * Convert list of ROIs to binary images separately for each ROI.
   * 
   * <p>Assumes that ROIs are named: fgNameID_NO, where ID belongs to the same object and NO are
   * different scribbles for it. Similar to {@link #decodeSeedsRoi(List, String, String, int, int)}
   * but process each slice separatelly.
   * 
   * @param rois rois to process.
   * @param width width of output map
   * @param height height of output map
   * @param slices number of slices
   * @param fgName core for FG ROI name
   * @param bgName core for BG core name
   * @return List of Seeds for each slice
   * @throws RandomWalkException
   * @see #decodeSeedsRoi(List, String, String, int, int)
   */
  public static List<Seeds> decodeSeedsRoiStack(List<Roi> rois, String fgName, String bgName,
          int width, int height, int slices) throws RandomWalkException {
    ArrayList<Seeds> ret = new ArrayList<>();
    // find nonassigned ROIs - according to DOC getPosition() can return 0 as well (stacks start
    // from 1)
    List<Roi> col0 =
            rois.stream().filter(roi -> roi.getPosition() == 0).collect(Collectors.toList());
    // find ROIS on each slice
    for (int s = 1; s <= slices; s++) {
      final int w = s;
      List<Roi> col =
              rois.stream().filter(roi -> roi.getPosition() == w).collect(Collectors.toList());
      // merge those nonassigned to slice 1
      if (s == 1) {
        col.addAll(col0);
      }
      // produce Seeds
      Seeds tmpSeed = SeedProcessor.decodeSeedsRoi(col, fgName, bgName, width, height);
      ret.add(tmpSeed);
    }

    // new ImagePlus("", ret.get(0).get(SeedTypes.BACKGROUND, 1)).show();
    // List<ImageProcessor> tmp = ret.get(0).get(SeedTypes.FOREGROUNDS);
    // for (ImageProcessor ip : tmp) {
    // new ImagePlus("", ip).show();
    // }

    return ret;

  }
}
