package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Contain various methods for converting labelled images to Seeds.
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
    int pixelsNum = 0;
    int pixelHistNum = 0;
    // iterate over foregrounds
    for (ImageProcessor i : out.get(RandomWalkSegmentation.SeedTypes.FOREGROUNDS)) {
      int[] histfg = i.getHistogram();
      pixelHistNum += histfg[0]; // sum number of background pixels for each map
      pixelsNum += i.getPixelCount(); // sum number of all pixels
    }
    int[] histbg = background.getHistogram();
    // if number of all pixels is same as number of hist[0] - no other pixels than background
    if (pixelHistNum == pixelsNum || histbg[0] == background.getPixelCount()) {
      throw new RandomWalkException(
              "Seed pixels are empty, check if:\n- correct colors were used\n- all slices have"
                      + " been seeded (if stacked seed is used)\n"
                      + "- Shrink/expand parameters are not too big.");
    }

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
   */
  public static Seeds decodeSeedsRoi(List<Roi> rois, String fgName, String bgName, int width,
          int height) {
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
    ret.put(SeedTypes.BACKGROUND, bg);

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

}
