package uk.ac.warwick.wsbc.quimp.geom.filters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Tuple2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.BoaException;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.geom.BasicPolygons;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

/**
 * Implementation of HatFilter for removing convexes from polygon.
 * 
 * <h3>List of user parameters:</h3>
 * <ol>
 * <li><i>window</i> - Size of window in pixels. It is responsible for sensitivity to protrusions of
 * given size. Larger window can eliminate small and large protrusions whereas smaller window is
 * sensitive only to small protrusions. Window size should be from 3 to number of outline points.
 * <li><i>pnum</i> - Number of protrusions/cavities that will be removed from outline. If not
 * limited by <i>alev</i> parameter the algorithm will eliminate <i>pnum</i> features
 * (convex/concave parts) from outline. Feature is removed if its rank is bigger than <i>alev</i>
 * threshold. If <i>pnum</i> is set to 0 algorithm will remove all candidates with rank higher than
 * <i>alev</i> regardless their number. <i>pnum</i> should be from 0 to any value. Algorithm stops
 * searching when there is no candidates to remove.
 * <li><i>alev</i> - Threshold value, if circularity computed for given window position is lower
 * than threshold this window is not eliminated regarding <i>pnum</i> or its rank in circularities.
 * <i>alev</i> should be in range form 0 to 1, where 0 stands for accepting every candidate
 * </ol>
 * 
 * <p><h3>General description of algorithm:</h3> The window slides over the wrapped contour. Points
 * inside window for its position <i>p</i> are considered as candidates to removal from contour if
 * they meet the following criterion:
 * <ol>
 * <li>The window has achieved for position p circularity parameter <i>c</i> larger than
 * <i>alev</i>
 * <li>The window on position <i>p</i> does not touch any other previously found window.
 * <li>Points of window <i>p</i> are convex/concave (can be configured by {@link #setMode(int)}).
 * </ol>
 * 
 * <p>Every window <i>p</i> has assigned a <i>rank</i>. Bigger <i>rank</i> stands for better
 * candidate
 * to remove. Algorithm tries to remove first <i>pnum</i> windows (those with biggest ranks) that
 * meet above rules. If <i>pnum</i> is set to 0 all protrusions with rank > <i>alev</i> are deleted.
 * 
 * <p><H3>Detailed description of algorithm</H3> The algorithm comprises of three main steps:
 * <ol>
 * <li>Preparing <i>rank</i> table of candidates to remove
 * <li>Iterating over <i>rank</i> table to find <i>pnum</i> such candidates who meet rules and store
 * their coordinates in <i>ind2rem</i> array. By candidates it is understood sets of polygon indexes
 * that is covered by window on given position. For simplification those vertexes are identified by
 * lover and upper index of window in outline array (input). <i>pnum</i> can be 0, see note above.
 * <li>Forming output table without protrusions.
 * </ol>
 * 
 * <p><H2>First step</H2> The window of size <i>window</i> slides over looped data. Looping is
 * // * performed by {@link Collections#rotate(List, int)} method that shift data left copying
 * falling out indexes
 * to end of the set (finally the window is settled in constant position between indexes
 * <0;window-1>). For each its position <i>r</i> the candidate points are deleted from original
 * contour and circularity is computed (see {@link #getCircularity(List)}). Then candidate points
 * are passed to {@link #getWeighting(List)} method where weight is evaluated. The role of weight is
 * to promote in <i>rank</i> candidate points that are cumulated in small area over distributed
 * sets. Thus weight should give larger values for that latter distribution than for cumulated one.
 * Currently weights are calculated as squared standard deviation of distances of all candidate
 * points to
 * center of mass of these points (or mean point if polygon is invalid). There is also mean
 * intensity calculated here. It is sampled along shrunk outline produced from input points. Finally
 * circularity(r) is divided by weight (<i>r</i>) and intensity and stored in <i>circ</i> array.
 * Additionally in this step the convex/concave is checked. All candidate points are tested whether
 * <b>all</b> they are inside the contour made without them (for concave option) or whether
 * <b>any</b> of
 * them is inside the modified contour (for convex option). Convex.concave sensitivity option can be
 * modified by {@link #setMode(int)}
 * This information is stored in <i>convex</i> array. Finally rank array <i>circ</i> is normalised
 * to maximum element.
 * Mean contour intensity can be switched off using {@link #runPlugin(List)} method.
 * 
 * <p><H2>Second step</H2> In second step array of ranks <i>circ</i> is sorted in descending order.
 * For every rank in sorted table the real position of window is retrieved (that gave this rank).
 * The window position is defined here by two numbers - lover and upper range of indexes
 * covered by it. The candidate points from this window are validated for criterion:
 * <ol>
 * <li><i>rank</i> must be greater than <i>alev</i>
 * <li>lower and upper index of window (index means here number of polygon vertex in array) must not
 * be included in any previously found window. This checking is done by deriving own class
 * WindowIndRange with overwritten {@link HatSnakeFilter.WindowIndRange#compareTo(Object)} method
 * that defines
 * rules of equality and non relations between ranges. Basically any overlapping range or included
 * is considered as equal and rejected from storing in <i>ind2rem</i> array.
 * <li>candidate points must be convex/concave.
 * <li>current <i>rank</i> (<i>circ</i>) is greater than <i>alev</i>
 * </ol>
 * If all above criterion are meet the window (l;u) is stored in <i>ind2rem</i>. Windows on the end
 * of data are wrapped by dividing them for two sub-windows: (w;end) and (0;c) otherwise they may
 * cover the whole range (e.g. <10;3> does not stand for window from 10 wrapped to 3 but window from
 * 3 to 10).
 * 
 * <p>The second step is repeated until <i>pnum</i> object will be found or end of candidates will
 * be
 * reached.
 * 
 * <p><H2>Third step</H2> In third step every point from original contour is tested for including in
 * array <i>ind2rem</i> that contains ranges of indexes to remove. Points on index that is not
 * included in any of ranges stored in <i>ind2rem</i> are copied to output.
 * 
 * @author p.baniukiewicz
 */
public class HatSnakeFilter implements IPadArray {

  static final Logger LOGGER = LoggerFactory.getLogger(HatSnakeFilter.class.getName());
  /**
   * Algorithm will look for cavities.
   * 
   * @see #setMode(int)
   */
  public static final int CAVITIES = 1;
  /**
   * Algorithm will look for protrusions.
   * 
   * @see #setMode(int)
   */
  public static final int PROTRUSIONS = 2;
  /**
   * Algorithm will look for cavities and protrusions.
   * 
   * @see #setMode(int)
   */
  public static final int ALL = 3;

  /**
   * Number of steps used for outline shrinking for intensity sampling.
   */
  public int shrinkSteps = 10;

  /**
   * Set it to 1 to look for inclusions, 2 for protrusions, 3 for all.
   */
  private int lookFor = PROTRUSIONS; // default for compatibility with old code

  private boolean lookForb;
  private int window; // filter's window size
  private int pnum; // how many protrusions to remove
  private double alev; // minimal acceptance level
  private List<Point2d> points; // original contour passed from QuimP

  /**
   * Construct HatFilter Input array with data is virtually circularly padded.
   */
  public HatSnakeFilter() {
    this.window = 15;
    this.pnum = 1;
    this.alev = 0;
    LOGGER.debug("Set default parameter: window=" + window + " pnum=" + pnum + " alev=" + alev);
  }

  /**
   * Constructor passing algorithm parameters.
   * 
   * @param window size of the window
   * @param pnum number of protrusions to find
   * @param alev threshold
   * @see HatSnakeFilter
   */
  public HatSnakeFilter(int window, int pnum, double alev) {
    super();
    this.window = window;
    this.pnum = pnum;
    this.alev = alev;
  }

  /**
   * In contrary to {@link #runPlugin(List, ImageProcessor)} this method does not use intensity
   * weighting.
   * 
   * @param data contour to process
   * @return Processed input list, size of output list may be different than input. Empty output
   *         is also allowed.
   * @throws QuimpPluginException on wrong input data
   * @see #runPlugin(List, ImageProcessor)
   */
  public List<Point2d> runPlugin(List<Point2d> data) throws QuimpPluginException {
    List<Point2d> ret = new ArrayList<>();
    try {
      ret = runPlugin(data, null);
    } catch (BoaException e) {
      // for null==orgIp will be newer thrown, but in case
      throw new IllegalStateException(e);
    }
    return ret;
  }

  /**
   * Main filter runner.
   * 
   * <p>This version assumes that user clicked Apply button to populate data from UI to plugin or
   * any other ui element. User can expect that points will be always valid but they optionally may
   * have 0 length.
   * 
   * @param data contour to process
   * @param orgIp Original image used for sampling intensity
   * 
   * @return Processed input list, size of output list may be different than input. Empty output
   *         is also allowed.
   * @throws QuimpPluginException on wrong input data
   * @throws BoaException on problem with creating outline from points
   */
  public List<Point2d> runPlugin(List<Point2d> data, ImageProcessor orgIp)
          throws QuimpPluginException, BoaException {
    points = data;

    List<Point2d> shCont = new ArrayList<>();
    Outline outline = null;
    if (orgIp != null) {
      // create shrunk outline to sample intensity - one of the parameters used for candidate rank
      outline = new QuimpDataConverter(data).getOutline(0); // FIXME What if more contours?
      new OutlineProcessor(outline).shrink(shrinkSteps, 0.3, 0.1, 0.01);
      outline.unfreezeAll();
      shCont = outline.asList();
    }
    // internal parameters are not updated here but when user click apply
    LOGGER.debug(String.format("Run plugin with params: window %d, pnum %d, alev %f", window, pnum,
            alev));

    BasicPolygons bp = new BasicPolygons(); // provides geometry processing
    List<Point2d> out = new ArrayList<Point2d>(); // output table for plotting temporary results
    // check input conditions
    if (window % 2 == 0 || window < 0) {
      throw new QuimpPluginException("Window must be uneven, positive and larger than 0");
    }
    if (window >= points.size()) {
      throw new QuimpPluginException("Processing window to long");
    }
    if (window < 3) {
      throw new QuimpPluginException("Window should be larger than 2");
    }
    if (pnum < 0) {
      throw new QuimpPluginException("Number of protrusions should be larger than 0");
    }
    if (alev < 0) {
      throw new QuimpPluginException("Acceptacne level should be positive");
    }

    // temporary variable for keeping window currently tested for containing in ind2rem
    WindowIndRange indexTest = new WindowIndRange();
    // Step 1 - Build circularity table
    // array to store circularity for window positions. Index is related to window position
    // (negative shift in rotate)
    ArrayList<Double> circ = new ArrayList<Double>();
    // store information if points for window at r position are convex compared to shape without
    // these points
    ArrayList<Boolean> convex = new ArrayList<Boolean>();

    double tmpCirc;
    double tmpInt; // mean intensity along contour in window
    for (int r = 0; r < points.size(); r++) {
      LOGGER.trace("------- Iter: " + r + "-------");
      LOGGER.trace("points: " + points.toString());
      LOGGER.trace("shCont: " + shCont.toString());
      // get all points except window. Window has constant position 0 - (window-1)
      List<Point2d> pointsnowindow = points.subList(window, points.size());
      LOGGER.trace("windowPoints: " + pointsnowindow.toString());
      tmpCirc = getCircularity(pointsnowindow);
      LOGGER.trace("circ " + tmpCirc);
      // calculate weighting for circularity
      List<Point2d> pointswindow = points.subList(0, window); // get points for window only
      List<Point2d> shContnowindow = shCont.subList(0, window);
      LOGGER.trace("win         : " + pointswindow.toString());
      LOGGER.trace("windowshCont: " + shContnowindow.toString());
      tmpInt = getIntensity(shContnowindow, orgIp);
      LOGGER.trace("mInt  :" + tmpInt);
      tmpCirc /= (getWeighting(pointswindow) * tmpInt); // calculate weighting for window content
      LOGGER.trace("Wcirc :" + tmpCirc);
      circ.add(tmpCirc); // store weighted circularity for shape without window
      // check if points of window are convex according to shape without these points
      if (lookForb == true) {
        convex.add(bp.arePointsInside(pointsnowindow, pointswindow)); // true if concave
      } else {
        convex.add(bp.isanyPointInside(pointsnowindow, pointswindow)); // old code
      }
      LOGGER.trace("con: " + convex.get(convex.size() - 1));
      // move window to next position
      // rotates by -1 what means that on first n positions
      // of points there are different values simulate window
      // first iter 0 1 2 3 4 5... (w=[0 1 2])
      // second itr 1 2 3 4 5 0... (w=[1 2 3])
      // last itera 5 0 1 2 3 4... (w=[5 0 1])
      Collections.rotate(points, -1);
      Collections.rotate(shCont, -1);
    }

    if (QuimP.SUPER_DEBUG && orgIp != null) {
      // add contour to input original image
      orgIp.setColor(Color.WHITE);
      if (outline != null) {
        outline.asFloatRoi().drawPixels(orgIp);
      }
    }
    // normalize circularity to 1
    double maxCirc = Collections.max(circ);
    LOGGER.trace("Max circ=" + maxCirc);
    for (int r = 0; r < circ.size(); r++) {
      circ.set(r, circ.get(r) / maxCirc);
    }

    // Step 2 - Check criterion for all windows
    TreeSet<WindowIndRange> ind2rem = new TreeSet<>(); // <l;u> range of indexes to remove
    // need sorted but the old one as well to identify windows positions
    ArrayList<Double> circsorted = new ArrayList<>(circ);
    circsorted.sort(Collections.reverseOrder()); // sort in descending order
    LOGGER.trace("cirs: " + circsorted.toString());
    LOGGER.trace("circ: " + circ.toString());

    if (circsorted.get(0) < alev) {
      return points; // just return non-modified data; TODO does it matter if circ is normalised?
    }

    int found = 0; // how many protrusions we have found already
    // current index in circsorted - number of window to analyze. Real position of
    // window on data can be retrieved by finding value from sorted array circularity
    // in non sorted, where order of data is related to window position starting
    // from 0 for most left point of window
    int i = 0;
    boolean contains; // temporary result of test if current window is included in any prev
    // do as long as we find pnum protrusions (or to end of candidates, does not apply if pnum==0
    // when pnum is ignored)
    while (found < pnum || pnum == 0) {
      if (i >= circsorted.size()) { // no more data to check, probably we have less prot. pnum
        LOGGER.warn("Can find next candidate. Use smaller window");
        break;
      }
      if (circsorted.get(i) < alev) {
        break; // stop searching because all i+n are smaller as well
      }
      if (found > 0) {
        // find where it was before sorting and store in window positions
        int startpos = circ.indexOf(circsorted.get(i));
        // check if we already have this index in list indexes to remove
        if (startpos + window - 1 >= points.size()) { // if at end, we must turn to begin
          indexTest.setRange(startpos, points.size() - 1); // to end
          contains = ind2rem.contains(indexTest); // beginning of window at the end of dat
          indexTest.setRange(0, window - (points.size() - startpos) - 1); // turn to start
          contains &= ind2rem.contains(indexTest); // check rotated part at beginning
        } else {
          indexTest.setRange(startpos, startpos + window - 1);
          contains = ind2rem.contains(indexTest);
        }
        // this window doesn't overlap with those found already and it is convex
        if (!contains && (convex.get(startpos) == lookForb || lookFor == ALL)) {
          // store range of indexes that belongs to window
          if (startpos + window - 1 >= points.size()) { // as prev split to two windows
            // if we are on the end of data
            ind2rem.add(new WindowIndRange(startpos, points.size() - 1));
            // turn window to beginning
            ind2rem.add(new WindowIndRange(0, window - (points.size() - startpos) - 1));
          } else {
            ind2rem.add(new WindowIndRange(startpos, startpos + window - 1));
          }
          LOGGER.trace("added win for i=" + i + " startpos=" + startpos + " coord:"
                  + points.get(startpos).toString() + "alev=" + circsorted.get(i));
          found++;
          i++;
        } else { // go to next candidate in sorted circularities
          i++;
        }
      } else { // first candidate always accepted
        // find where it was before sorting and store in window positions
        int startpos = circ.indexOf(circsorted.get(i));
        if (lookFor != ALL && convex.get(startpos) == !lookForb) { // do not check for mode 3
          i++;
          continue;
        }
        // store range of indexes that belongs to window
        if (startpos + window - 1 >= points.size()) { // as prev split to two windows
          // if we are on the end of data
          ind2rem.add(new WindowIndRange(startpos, points.size() - 1));
          // turn window to beginning
          ind2rem.add(new WindowIndRange(0, window - (points.size() - startpos) - 1));
        } else {
          ind2rem.add(new WindowIndRange(startpos, startpos + window - 1));
        }
        LOGGER.info("added win for i=" + i + " startpos=" + startpos + " coord:"
                + points.get(startpos).toString() + "alev=" + circsorted.get(i));
        i++;
        found++;
      }
    }
    LOGGER.trace("winpos: " + ind2rem.toString());
    LOGGER.trace("Found :" + found + " accepted windows");
    // Step 3 - remove selected windows from input data
    // array will be copied to new one skipping points to remove
    for (i = 0; i < points.size(); i++) {
      // set upper and lower index to the same value - allows to test particular index for its
      // presence in any defined range
      indexTest.setSame(i);
      if (!ind2rem.contains(indexTest)) { // check if any window position (l and u bound)
        out.add(new Point2d(points.get(i)));
      } else { // include tested point. Copy it to new array if not
        LOGGER.trace("winpos: " + ind2rem.toString() + " " + points.get(i));
      }
    }
    return out;
  }

  /**
   * Set detection mode.
   * 
   * @param mode can be CAVITIES, PROTRUSIONS, BOTH
   */
  public void setMode(int mode) {
    lookFor = mode;
    lookForb = (lookFor == CAVITIES ? true : false);
  }

  double getIntensity(List<Point2d> shContnowindow, ImageProcessor orgIp) {
    double meanI = 0.0;
    if (orgIp == null) {
      return 1.0; // case where intensity is not used
    }
    for (Point2d p : shContnowindow) {
      meanI += orgIp.getPixelValue((int) p.x, (int) p.y);
    }
    meanI /= shContnowindow.size();
    if (meanI == 0.0) {
      meanI = 1;
    }
    return meanI;
  }

  /**
   * Calculate circularity of polygon.
   * 
   * <p>Circularity is computed as: \f[ circ=\frac{4*\pi*A}{P^2} \f] where \f$A\f$ is polygon area
   * and \f$P\f$ is its perimeter
   * 
   * @param p Polygon vertices
   * @return circularity
   */
  double getCircularity(final List<? extends Tuple2d> p) {
    double area;
    double perim;
    BasicPolygons b = new BasicPolygons();
    area = b.getPolyArea(p);
    perim = b.getPolyPerim(p);

    return (4 * Math.PI * area) / (perim * perim);
  }

  /**
   * Calculates weighting based on distribution of window points.
   * 
   * <p>Calculates center of mass of window points and then standard deviations of lengths between
   * this point and every other point. Cumulated distributions like protrusions give smaller
   * values than elongated ones.
   * If input polygon <i>p</i> (which is only part of whole cell shape) is defective, i.e its
   * edges cross, the weight is calculated using middle vector defined as mean of coordinates.
   * 
   * @param p Polygon vertices
   * @return Weight
   */
  double getWeighting(final List<Point2d> p) {
    double[] len = new double[p.size()];
    BasicPolygons bp = new BasicPolygons();
    Vector2d middle;
    try { // check if input polygon is correct
      middle = new Vector2d(bp.polygonCenterOfMass(p));
    } catch (IllegalArgumentException e) { // if not get middle point as mean
      double mx = 0;
      double my = 0;
      for (Point2d v : p) {
        mx += v.x;
        my += v.y;
      }
      middle = new Vector2d(mx / p.size(), my / p.size());
    }
    int i = 0;
    // get lengths
    for (Point2d v : p) {
      Vector2d vec = new Vector2d(middle); // vector between px and middle
      vec.sub(v);
      len[i++] = vec.length();
    }
    // get mean
    double mean = 0;
    for (double d : len) {
      mean += d;
    }
    mean /= p.size();
    // get std
    double std = 0;
    for (double d : len) {
      std += Math.pow(d - mean, 2.0);
    }
    std /= points.size();
    std = Math.sqrt(std);

    // max of len
    // double maxLen = QuimPArrayUtils.arrayMax(len);
    LOGGER.debug("w " + std);
    return std * std;
  }

  /**
   * Class holding lower and upper index of window. Supports comparisons.
   * 
   * <p>Two ranges (l;u) and (l1;u1) are equal if any of these conditions is met:
   * <ol>
   * <li>they overlap
   * <li>they are the same
   * <li>one is included in second
   * </ol>
   * 
   * @author p.baniukiewicz
   * @see WindowIndRange#compareTo(Object)
   */
  class WindowIndRange implements Comparable<Object> {
    public int lower;
    public int upper;

    public WindowIndRange() {
      upper = 0;
      lower = 0;
    }

    /**
     * Create pair of indexes that define window.
     * 
     * @param l lower index
     * @param u upper index
     */
    WindowIndRange(int l, int u) {
      setRange(l, u);
    }

    @Override
    public String toString() {
      return "{" + lower + "," + upper + "}";
    }

    public int hashCode() {
      int result = 1;
      result = 31 * result + lower;
      result = 31 * result + upper;
      return result;
    }

    /**
     * Compare two WindowIndRange objects.
     * 
     * @param obj object to compare
     * @return true only if ranges does not overlap
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }

      final WindowIndRange other = (WindowIndRange) obj;
      if (upper < other.lower) {
        return true;
      } else if (lower > other.upper) {
        return true;
      } else {
        return false;
      }

    }

    /**
     * Compare two WindowIndRange objects.
     * 
     * <p>The following rules of comparison are used:
     * <ol>
     * <li>If range1 is below range2 they are not equal
     * <li>If range1 is above range2 they are not equal
     * </ol>
     * 
     * <p>They are equal in all other cases:
     * <ol>
     * <li>They are sticked
     * <li>One includes other
     * <li>They overlap
     * </ol>
     * 
     * @param obj Object to compare to this
     * @return -1,0,1 expressing relations in windows positions
     */
    @Override
    public int compareTo(Object obj) {
      final WindowIndRange other = (WindowIndRange) obj;
      if (this == obj) {
        return 0;
      }

      if (upper < other.lower) {
        return -1;
      } else if (lower > other.upper) {
        return 1;
      } else {
        return 0;
      }
    }

    /**
     * Sets upper and lower indexes to the same value.
     * 
     * @param i Value to set for u and l
     */
    public void setSame(int i) {
      lower = i;
      upper = i;
    }

    /**
     * Set pair of indexes that define window assuring that l < u.
     * 
     * @param l lower index, always smaller
     * @param u upper index
     */
    public void setRange(int l, int u) {
      if (l > u) {
        this.lower = u;
        this.upper = l;
      } else {
        this.lower = l;
        this.upper = u;
      }
    }

  }
}
