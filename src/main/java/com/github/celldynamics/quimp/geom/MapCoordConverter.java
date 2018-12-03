package com.github.celldynamics.quimp.geom;

import java.awt.geom.Point2D;

import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

/**
 * Convert between map coordinates (frame;outline pos) and image coordinates (x;y).
 * 
 * @author baniu
 *
 */
public class MapCoordConverter {

  /**
   * Convert (Frame;Outline) to cartesian (x;y).
   * 
   * @param mapCell structure with maps.
   * @param frame frame coordinate (from 0)
   * @param outlinePos position on the outline to convert
   * @param tol tolerance to match outlinePos to coordinates in mapCell defined as
   *        : |coordMap[frame] - outlinePos|
   * @return converted coordinate or null point if not found
   */
  public static Point2D.Double toCartesian(STmap mapCell, int frame, double outlinePos,
          double tol) {
    double[][] xmap = mapCell.getxMap();
    double[][] ymap = mapCell.getyMap();
    double[][] coordMap = mapCell.getCoordMap();
    // find node index in coord map for frame
    int index = findIndex(coordMap[frame], outlinePos, tol);
    if (index < 0) {
      return null;
    } else {
      return new Point2D.Double(xmap[frame][index], ymap[frame][index]);
    }
  }

  /**
   * Convert Cartesian coordinates for given frame to map coordinates (frame;outlinePos).
   * 
   * @param mapCell structure with maps
   * @param frame frame to look in
   * @param x Cartesian coordinate of point
   * @param y Cartesian coordinate of point
   * @param tol defined maximal distance between point
   * @return index of outline point which is closest to (x;y) and closer than tol
   */
  public static Point2D.Double toMap(STmap mapCell, int frame, double x, double y, double tol) {
    double[][] xmap = mapCell.getxMap();
    double[][] ymap = mapCell.getyMap();
    double[][] coordMap = mapCell.getCoordMap();
    int index = findPointIndex(xmap[frame], ymap[frame], x, y, tol);
    if (index < 0) {
      return null;
    } else {
      return new Point2D.Double(frame, coordMap[frame][index]);
    }

  }

  /**
   * Find point defined by xmap and ymap .for given frmae which is closest to another point (x;y)
   * within tolerance.
   * 
   * @param xmap map of x coordinates for one frame
   * @param ymap map of y coordinates for one frame
   * @param x x coordinate fo searched point
   * @param y y coordinate fo searched point
   * @param tol maximum distance between points.
   * @return index of point (xmap[i];ymap[i]) which is closest to (x;y) AND closer than tol, -1
   *         otherwise
   */
  public static int findPointIndex(double[] xmap, double[] ymap, double x, double y, double tol) {
    double dist = Double.MAX_VALUE;
    int index = -1;
    for (int i = 0; i < xmap.length; i++) {
      double _dist = Math.sqrt((xmap[i] - x) * (xmap[i] - x) + (ymap[i] - y) * (ymap[i] - y));
      if (_dist <= tol && _dist < dist) {
        dist = _dist;
        index = i;
      }
    }
    return index;
  }

  /**
   * Find index of element val in array with tolerance.
   * 
   * @param ar array to look for
   * @param val value to find
   * @param tol tolerance
   * 
   * @return index or negative number if not found
   */
  public static int findIndex(double[] ar, double val, double tol) {
    double dist = Double.MAX_VALUE;
    int index = -1;
    for (int i = 0; i < ar.length; i++) {
      double _dist = Math.abs(ar[i] - val);
      if (_dist <= tol && _dist < dist) {
        dist = _dist;
        index = i;
      }
    }
    return index;
  }
}
