package com.github.celldynamics.quimp.geom.filters;

import java.util.List;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Tuple2d;

import com.github.celldynamics.quimp.plugin.utils.IPadArray;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

/**
 * Perform filtering on basic list of coordinates.
 * 
 * @author p.baniukiewicz
 * @see OutlineProcessor
 */
public class PointListProcessor {

  private QuimpDataConverter dt;

  /**
   * Initialize object with list of points.
   * 
   * @param list to process
   */
  public PointListProcessor(List<? extends Tuple2d> list) {
    dt = new QuimpDataConverter(list);
  }

  /**
   * Apply mean filter to list.
   * 
   * @param window size of mean window
   * @param iters number of iterations
   * @return Reference to this object
   */
  public PointListProcessor smooth(int window, int iters) {
    double[] x = dt.getX();
    double[] y = dt.getY();

    for (int i = 0; i < iters; i++) {
      runningMean(y, window);
      runningMean(x, window);
    }
    dt = new QuimpDataConverter(x, y);
    return this;
  }

  /**
   * Return modified list.
   * 
   * @return list after processing
   */
  public List<Point2d> getList() {
    return dt.getList();
  }

  /**
   * Return underlying DataConverter instance.
   * 
   * @return DataConverter
   */
  public QuimpDataConverter getDataConverterInstance() {
    return dt;
  }

  /**
   * Running mean on input array.
   * 
   * @param data data to filter, can be empty
   * @param windowSize odd window size
   */
  public static void runningMean(double[] data, int windowSize) {
    if (windowSize % 2 == 0) {
      throw new IllegalArgumentException("Window must be odd");
    }
    double[] ret = new double[data.length];
    int cp = windowSize / 2; // left and right range of window

    for (int c = 0; c < data.length; c++) { // for every point in data
      double mean = 0;
      for (int cc = c - cp; cc <= c + cp; cc++) { // points in range c-2 - c+2 (for window=5)
        int indexTmp = IPadArray.getIndex(data.length, cc, IPadArray.CIRCULARPAD);
        mean += data[indexTmp];
      }
      mean = mean / windowSize;
      ret[c] = mean; // remember result
    }
    // replace input array
    System.arraycopy(ret, 0, data, 0, data.length);
  }
}
