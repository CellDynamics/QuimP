package com.github.celldynamics.quimp.plugin.randomwalk;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.linear.RealMatrix;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.ImageStack;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * Store probability maps computed for foreground and background objects.
 * 
 * <p>Allow to keep many maps for FG and BG, e.g. when there are more than one object that should be
 * handled separately. For given {@link SeedTypes} key maps are stored in ArrayList.
 * 
 * @author p.baniukiewicz
 * @see Seeds
 */
@SuppressWarnings("serial")
public class ProbabilityMaps extends ListMap<RealMatrix> {

  /**
   * Default constructor.
   */
  public ProbabilityMaps() {
    super();
  }

  /**
   * Convert list of maps under specified key to 3d array of doubles.
   * 
   * <p>Data in output array are references. Require the same size of all maps within specified key.
   * 
   * @param key which map to convert
   * @return 3d array [map][width][height] or null if there is no maps under specified key
   * @throws IllegalArgumentException if size of maps is not equal
   */
  public double[][][] convertTo3dMatrix(Object key) {
    List<RealMatrix> maps = get(key);
    // Can be null if points not found
    if (maps == null || maps.isEmpty()) {
      return null;
    }
    // assume all maps have the same resolution
    int width = maps.get(0).getColumnDimension();
    int height = maps.get(0).getRowDimension();
    int depth = maps.size();
    for (int i = 1; i < depth; i++) {
      RealMatrix tmp = maps.get(i);
      if (tmp.getRowDimension() != height || tmp.getColumnDimension() != width) {
        throw new IllegalArgumentException("All maps must have the same resoultion");
      }
    }
    double[][][] ret = new double[depth][][];
    ListIterator<RealMatrix> it = maps.listIterator();
    while (it.hasNext()) {
      ret[it.nextIndex()] = it.next().getData();
    }

    return ret;
  }

  /**
   * Convert probability maps to ImageStack.
   * 
   * @param key which map to convert
   * @return Float image stack or null if specified key does not exists
   * @throws IllegalArgumentException if size of maps is not equal
   */
  public ImageStack convertToImageStack(Object key) {
    List<RealMatrix> maps = get(key);
    // Can be null if points not found
    if (maps == null || maps.isEmpty()) {
      return null;
    }
    // assume all maps have the same resolution
    int width = maps.get(0).getColumnDimension();
    int height = maps.get(0).getRowDimension();
    int depth = maps.size();
    for (int i = 1; i < depth; i++) {
      RealMatrix tmp = maps.get(i);
      if (tmp.getRowDimension() != height || tmp.getColumnDimension() != width) {
        throw new IllegalArgumentException("All maps must have the same resoultion");
      }
    }
    ImageStack is = new ImageStack(width, height);
    for (RealMatrix m : maps) {
      ImageProcessor fp = QuimPArrayUtils.realMatrix2ImageProcessor(m); // returns float
      is.addSlice(fp);
    }
    return is;
  }
}
