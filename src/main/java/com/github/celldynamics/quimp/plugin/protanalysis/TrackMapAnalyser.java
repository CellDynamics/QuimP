package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.geom.MapTracker;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

/**
 * Track point using tracking map generated by
 * {@link com.github.celldynamics.quimp.geom.MapTracker}.
 * 
 * <p>All methods in this class are safe either for empty tracks returned by MapTracker or
 * trackMaxima.
 * Tracking point coordinates may contain invalid values (negative).
 * 
 * @author p.baniukiewicz
 *
 */
public class TrackMapAnalyser {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(TrackMapAnalyser.class.getName());
  /**
   * Allow detection common points in backward and forward tracks generated for the same starting
   * point.
   * 
   * @see com.github.celldynamics.quimp.geom.MapTracker#includeFirst
   */
  public static final int WITH_SELFCROSSING = 2;
  /**
   * Disallow detection common points in backward and forward tracks generated for the same
   * starting point.
   * 
   * @see com.github.celldynamics.quimp.geom.MapTracker#includeFirst
   */
  public static final int WITHOUT_SELFCROSSING = 4;
  /**
   * Maximum point (source of tracks) is included in tracks (if <tt>true</tt>).
   * 
   * <p>It should be changed carefully as many other procedures can assume that first point is
   * included in Tracks.
   */
  private static boolean INCLUDE_INITIAL = true;

  /**
   * Hold result of Map generation and analysis.
   */
  private TrackCollection trackCollection;

  /**
   * getTrackCollection.
   * 
   * @return the trackCollection
   */
  public TrackCollection getTrackCollection() {
    return trackCollection;
  }

  /**
   * Instantiates a new track map analyser. Assumes that Maximum point (source of tracks) is
   * included in tracks.
   * 
   * @see #INCLUDE_INITIAL
   */
  public TrackMapAnalyser() {
    trackCollection = new TrackCollection(INCLUDE_INITIAL);
  }

  /**
   * Track maxima across motility map as long as they fulfil criterion of amplitude.
   * 
   * <p>Return (as internal field {@link TrackCollection}) list of points tracked from every maximum
   * point as long as they meet criterion. Maximum point can be included in this list depending on
   * setting of {@link com.github.celldynamics.quimp.geom.MapTracker#includeFirst} flag. First
   * points
   * in tracks are initial points. Forward track is sorted within increasing frames from starting
   * point, backward according to decreasing frames.
   * 
   * @param mapCell holds all maps generated and saved by QuimP
   * @param drop the value (in x/100) while velocity remains above of the peak speed. E.g for
   *        drop=1 all tracked points are considered (along positive motility), drop=0.5 stands
   *        for points that are above 0.5*peakval, where peakval is the value of found maximum.
   * @param maximaFinder properly initialized object that holds maxima of motility map. All maxima
   *        are tracked.
   * 
   */
  public void trackMaxima(final STmap mapCell, double drop, final MaximaFinder maximaFinder) {

    int numFrames = mapCell.getMotMap().length;
    // int[] indexes = new int[numFrames];
    Polygon maxi = maximaFinder.getMaxima(); // restore computed maxima
    double[] maxValues = maximaFinder.getMaxValues(); // max values in order of maxi
    // build tracking map
    MapTracker trackMap = new MapTracker(mapCell.getOriginMap(), mapCell.getCoordMap());
    trackMap.includeFirst = INCLUDE_INITIAL; // include also initial point
    ArrayList<Point> trackForward = null;
    ArrayList<Point> trackBackward = null;
    // end indexes of accepted elements after checking criterion
    int nb = 0;
    int nf = 0;
    // iterate through all maxima - take only indexes (x)
    for (int i = 0; i < maxi.npoints; i++) {
      int index = maxi.ypoints[i]; // considered index
      int frame = maxi.xpoints[i]; // considered frame
      LOGGER.trace("Max = [" + frame + "," + index + "]");
      // trace forward every index until end of time
      trackForward = (ArrayList<Point>) trackMap.trackForwardValid(frame, index, numFrames - frame);
      // trace backward every index until end of time
      trackBackward = (ArrayList<Point>) trackMap.trackBackwardValid(frame, index, frame);
      Collections.reverse(trackBackward);
      // check where is drop off - index that has velocity below drop
      double dropValue = maxValues[i] - maxValues[i] * drop;
      for (nb = 0; nb < trackBackward.size() && trackBackward.get(nb).y >= 0; nb++) {
        double val = (mapCell.getMotMap()[trackBackward.get(nb).x][trackBackward.get(nb).y]);
        if (val < dropValue) {
          break;
        }
      }
      LOGGER.trace("tBackward: " + trackBackward);
      LOGGER.trace("Accepted:" + nb);

      for (nf = 0; nf < trackForward.size() && trackForward.get(nf).y >= 0; nf++) {
        double val = (mapCell.getMotMap()[trackForward.get(nf).x][trackForward.get(nf).y]);
        if (val < dropValue) {
          break;
        }
      }
      LOGGER.trace("tForward: " + trackForward);
      LOGGER.trace("Accepted:" + nf);
      // store tracking lines
      // Nb and Nf are pointer AFTER last valid point
      trackCollection.addPair(trackBackward.subList(0, nb), trackForward.subList(0, nf));
    }
  }

  /**
   * getCommonPoints.
   * 
   * @return All common points among tracks without self crossings (forward-backward for the same
   *         starting point)
   */
  public Polygon getCommonPoints() {
    ArrayList<Point> tmpRet = new ArrayList<>();
    List<Pair<Track, Track>> tracks = trackCollection.getBf();
    for (int i = 0; i < tracks.size() - 1; i++) {
      for (int j = i + 1; j < tracks.size(); j++) {
        Track b1 = tracks.get(i).getLeft();
        Track b2 = tracks.get(j).getLeft();
        Track f1 = tracks.get(i).getRight();
        Track f2 = tracks.get(j).getRight();
        // check b1-b2, b1-f2, b2-f1, f1-f2
        // b1-b2
        {
          Track copy = new Track(b1);
          copy.retainAll(b2);
          tmpRet.addAll(copy);
        }
        // b1-f2
        {
          Track copy = new Track(b1);
          copy.retainAll(f2);
          tmpRet.addAll(copy);
        }
        // b2-f1
        {
          Track copy = new Track(b2);
          copy.retainAll(f1);
          tmpRet.addAll(copy);
        }
        // f1-f2
        {
          Track copy = new Track(f1);
          copy.retainAll(f2);
          tmpRet.addAll(copy);
        }
      }
    }
    LOGGER.debug("Common points found:" + tmpRet.size());
    return point2i2Polygon(QuimPArrayUtils.removeDuplicates(tmpRet));
  }

  /**
   * Find common points among polygons.
   * 
   * <p>Check whether there are common points among polygons stored in List.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Polygon of size 0 may contain x,y, arrays of size 4, only number of points is 0
   * 
   * @param tracks List of polygons.
   * @return Polygon of size 0 when no intersection or polygons whose vertices are common for
   *         polygons in <tt>tracks</tt>. If there are vertexes shared among more than two
   *         polygons, they appear only once in returned polygon.
   */
  public Polygon getIntersectionPoints(List<Polygon> tracks) {
    List<Polygon> tmpRet = new ArrayList<>();
    for (int i = 0; i < tracks.size() - 1; i++) {
      for (int j = i + 1; j < tracks.size(); j++) {
        Polygon retPol = getIntersectionPoints(tracks.get(i), tracks.get(j));
        if (retPol.npoints != 0) {
          tmpRet.add(retPol); // add retained elements (common with p2)
        }
      }
    }
    // remove repeating vertexes
    List<Point> retP2i = QuimPArrayUtils.removeDuplicates(polygon2Point2i(tmpRet));
    // convert from list of polygons to one polygon
    return point2i2Polygon(retP2i);
  }

  /**
   * Check if p1 and p2 have common vertexes.
   * 
   * @param p1 Polygon
   * @param p2 Polygon
   * @return Polygon whose vertexes are those common for p1 and p2.
   */
  public Polygon getIntersectionPoints(Polygon p1, Polygon p2) {
    Polygon ret = new Polygon();
    List<Point> tmpRet = new ArrayList<>();
    List<Point> p1p = polygon2Point2i(Arrays.asList(p1)); // polygon as list of points
    List<Point> p2p = polygon2Point2i(Arrays.asList(p2)); // polygon as list of points
    // check if p1 and p2 have common elements
    p1p.retainAll(p2p);
    tmpRet.addAll(p1p); // add retained elements (common with p2)

    ret = point2i2Polygon(tmpRet);
    return ret;
  }

  /**
   * Find common points among polygons.
   * 
   * <p>This method provides also parents of every common point. Parents are given as indexes of
   * polygons in input list that have common vertex.
   * 
   * @param tracks List of polygons.
   * @param mode WITHOUT_SELFCROSSING | WITH_SELFCROSSING
   * @return List of common points together with their parents List(Pair(Parents,Point)). If there
   *         is no common points the list is empty
   */
  public List<Pair<Point, Point>> getIntersectionParents(List<Polygon> tracks, int mode) {
    ArrayList<Pair<Point, Point>> retTmp = new ArrayList<>();
    List<Pair<Point, Point>> ret;
    for (int i = 0; i < tracks.size() - 1; i++) {
      for (int j = i + 1; j < tracks.size(); j++) {
        Polygon retPol = getIntersectionPoints(tracks.get(i), tracks.get(j));
        for (int n = 0; n < retPol.npoints; n++) {
          Pair<Point, Point> pairTmp = new ImmutablePair<Point, Point>(new Point(i, j),
                  new Point(retPol.xpoints[n], retPol.ypoints[n]));
          retTmp.add(pairTmp);
        }
      }
    }
    ret = retTmp;
    if ((mode & WITHOUT_SELFCROSSING) == WITHOUT_SELFCROSSING) {
      ret = removeSelfCrossings(ret);
    }
    return ret;
  }

  /**
   * Removes the self repeatings.
   *
   * @param intersections the intersections
   * @param tracks the tracks
   * @return the list
   */
  public List<Pair<Point, Point>> removeSelfRepeatings(List<Pair<Point, Point>> intersections,
          List<Polygon> tracks) {
    HashMap<Integer, List<Pair<Point, Point>>> map = new HashMap<>();
    List<Pair<Point, Point>> ret = new ArrayList<>();
    // collect all intersections into separate maps according to parent (left only considered)
    for (Pair<Point, Point> p : intersections) {
      Integer parentleft = p.getLeft().x;
      if (map.get(parentleft) == null) {
        map.put(parentleft, new ArrayList<>()); // if no create
      }
      map.get(parentleft).add(p); // add crossection point to this key
    }
    // now, there are intersection points under keys which are their left parent.
    // go through every set and check which point is first along this parent
    Iterator<Integer> it = map.keySet().iterator();
    int minInd = Integer.MAX_VALUE;
    while (it.hasNext()) {
      Integer key = it.next();
      List<Pair<Point, Point>> values = map.get(key);
      Pair<Point, Point> minPoint = null; // will never be added to ret as it will be
      // Initialised or exception will be thrown
      for (Pair<Point, Point> p : values) { // iterate over intersections for given parent
        // get indexes of back and for tracks
        // This is strictly related to trackMaxima return order
        int back;
        int forw;
        if (p.getLeft().x % 2 == 0) { // if index is even it is back and forward is next one
          back = p.getLeft().x;
          forw = back + 1;
        } else { // if index is uneven this is forward and back is previous
          forw = p.getLeft().x;
          back = forw - 1;
        }
        int ind = enumeratePoint(tracks.get(back), tracks.get(forw), p.getRight());
        if (ind < 0) {
          throw new IllegalArgumentException("Point does not exist in track");
        }
        if (ind < minInd) {
          minInd = ind;
          minPoint = p;
        }
      }
      ret.add(minPoint);
    }
    return ret;

  }

  /**
   * Remove self crossings that happen between backward and forward tracks for the same initial
   * point.
   * 
   * {@link #trackMaxima} returns alternating tracks tracks, therefore every pair i,i+1 is related
   * to the same starting points, for even i. If the flag
   * com.github.celldynamics.quimp.geom.TrackMap.includeFirst is set, those two tracks share one
   * point
   * that is also starting point.
   * 
   * <p>This method remove those Pairs that come from parent (even,uneven).
   * 
   * @param input input data
   * @return input list without common points between tracks that belong to the same starting
   *         point.
   * @see #trackMaxima(STmap, double, MaximaFinder)
   */
  private List<Pair<Point, Point>> removeSelfCrossings(List<Pair<Point, Point>> input) {
    ArrayList<Pair<Point, Point>> ret = new ArrayList<>(input);
    ListIterator<Pair<Point, Point>> it = ret.listIterator();
    while (it.hasNext()) {
      Pair<Point, Point> element = it.next();
      // remove because first parent is even and second is next track. <even,uneven> are
      // <backward,forward> according to trackMaxima.
      if (element.getLeft().x % 2 == 0 && element.getLeft().x + 1 == element.getLeft().y) {
        it.remove();
      }
    }
    return ret;
  }

  /**
   * Convert list of Polygons to list of Points.
   * 
   * <p>The difference is that for polygons points are kept in 1d arrays, whereas for Point2i they
   * are as separate points that allows object comparison.
   * 
   * @param list List of polygons to convert
   * @return List of points constructed from all polygons.
   */
  public static List<Point> polygon2Point2i(List<Polygon> list) {
    List<Point> ret = new ArrayList<>();
    for (Polygon pl : list) { // every polygon
      for (int i = 0; i < pl.npoints; i++) {
        ret.add(new Point(pl.xpoints[i], pl.ypoints[i]));
      }
    }
    return ret;
  }

  /**
   * Convert list of Points to list of Polygons.
   * 
   * <p>The difference is that for polygons points are kept in 1d arrays, whereas for Point2i they
   * are as separate points that allows object comparison.
   * 
   * @param list List of points to convert
   * @return Polygon constructed from all points. This is 1-element list.
   */
  public static Polygon point2i2Polygon(List<Point> list) {
    int[] x = new int[list.size()];
    int[] y = new int[list.size()];
    int l = 0;
    for (Point p : list) { // every point
      x[l] = p.x;
      y[l] = p.y;
      l++;
    }
    return new Polygon(x, y, list.size());
  }

  /**
   * Get index of point in the whole track line composed from backward+forward tracks.
   * 
   * <p>Assumes that order og points in tracks is correct, from first to last. (assured by
   * {@link #trackMaxima(STmap, double, MaximaFinder)}.
   * 
   * <p>Use {@link #INCLUDE_INITIAL} to check whether initial point is included in tracks. If it is
   * it means that it appears twice (for backward and forward tracks respectively). then it is
   * counted only one. For <tt>false</tt> state all points are counted.
   * 
   * @param backwardMap backwardMap
   * @param forwardMap forwardMap
   * @param point point to check
   * @return Total index of point or -1 if not found in these track maps.
   */
  static int enumeratePoint(Polygon backwardMap, Polygon forwardMap, Point point) {
    int i = 0;
    int delta = 0;
    // if maximum is included in tracks it appear there twice, for backward and forward
    // track
    if (INCLUDE_INITIAL && forwardMap.npoints > 0 && backwardMap.npoints > 0) {
      delta = 1;
    }
    // do no count last point (maximum) if it is there. It will be counted for forward track
    for (i = 0; i < backwardMap.npoints - delta; i++) {
      if (backwardMap.xpoints[i] == point.x && backwardMap.ypoints[i] == point.y) {
        return i;
      }
    }
    for (; i < forwardMap.npoints + backwardMap.npoints - delta; i++) {
      if (forwardMap.xpoints[i - backwardMap.npoints + delta] == point.x
              && forwardMap.ypoints[i - backwardMap.npoints + delta] == point.y) {
        return i;
      }
    }
    return -1;
  }

}
