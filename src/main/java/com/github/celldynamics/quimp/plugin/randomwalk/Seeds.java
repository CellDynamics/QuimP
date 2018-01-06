package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Keep information about seeds for different Seed types.
 * 
 * <p>Seed type specifies type of object scribbled by user ({@link SeedTypes}, seed map is an binary
 * image of size of scribbled (original) image where pixels selected by user are white.
 * 
 * <p>Each seed map is stored as binary ImageProcessor but it is assumed that for one key
 * ({@link SeedTypes}) there can be several separate seeds (e.g. for different objects that should
 * be treated separately). This is common case for e.g. {@value SeedTypes#FOREGROUNDS}.
 * 
 * <p>Internally each seed image is stored in {@link ImageStack} structure, note that <i>put</i>
 * method does not verify is there is the same slice in stack already. Seed types other than
 * {@value SeedTypes#FOREGROUNDS} usually have only one seed map associated, but it is also stored
 * in {@link ImageStack}. One needs to take care about proper unwrapping data if standard
 * {@link #get(Object)} is used.
 * 
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class Seeds extends ListMap<ImageProcessor> {

  /**
   * Default constructor.
   */
  public Seeds() {
    super();
  }

  /**
   * Allow to set initial capacity.
   * 
   * @param initialCapacity initial capacity
   */
  public Seeds(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Retrieve specified seed map from specified key.
   * 
   * @param key key to retrieve
   * @param slice seed map number (starts from 1)
   * @return specified map or null if not available (neither key nor slice number)
   */
  public ImageProcessor get(Object key, int slice) {
    slice--;
    List<ImageProcessor> tmp = super.get(key);
    if (tmp == null) {
      return null;
    }
    if (slice < 0 || slice >= tmp.size()) {
      return null;
    }
    return tmp.get(slice);

  }

  /**
   * Convert binary maps representing foregrounds and background seeds to lists of coordinates
   * (format accepted by {@link RandomWalkSegmentation#solver(Seeds, RealMatrix[])}).
   * 
   * <p>Assumes that there can be more labels for each key (e.g. few objects labelled as foreground
   * that will be treated separately). Each label for specified key is converted to list of points
   * separately as well.
   * 
   * @param key which map to convert
   * @return List of point coordinates accepted by RW algorithm for each label within specified key.
   *         If there is more labels for e.g. FOREGROUND key, each is converted to list of points
   *         separately. Null if key does not exist. If seed map is empty (black) or key does not
   *         exist empty list is returned.
   * @see SeedProcessor#decodeSeedsfromRgb(ImagePlus, List, Color)
   * @see SeedProcessor#decodeSeedsfromRgb(ImageProcessor, List, Color)
   */
  public List<List<Point>> convertToList(Object key) {
    List<ImageProcessor> seeds = get(key);
    // output map integrating two lists of points
    List<List<Point>> out = new ArrayList<>();
    // Can be empty if points not found
    if (seeds == null || seeds.isEmpty()) {
      return out;
    }

    for (int s = 0; s < seeds.size(); s++) {
      ImageProcessor slice = seeds.get(s);
      List<Point> points = new ArrayList<>();
      for (int x = 0; x < slice.getWidth(); x++) {
        for (int y = 0; y < slice.getHeight(); y++) {
          if (slice.get(x, y) > 0) {
            points.add(new Point(x, y)); // remember foreground coords
          }
        }
      }
      out.add(points);
    }
    return out;
  }

  /**
   * Convert seeds to ImageStack.
   * 
   * @param key which map to convert
   * @return Stack of seeds for selected key or null if key does not exist or there aren't maps
   *         under it.
   */
  public ImageStack convertToStack(Object key) {
    List<ImageProcessor> seeds = get(key);
    if (seeds == null || seeds.isEmpty()) {
      return null;
    }
    ImageProcessor tmp = seeds.get(0);
    ImageStack ret = new ImageStack(tmp.getWidth(), tmp.getHeight());
    for (ImageProcessor ip : seeds) {
      ret.addSlice(ip);
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString();
  }

}
