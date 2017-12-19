package com.github.celldynamics.quimp.geom.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.PointsList;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.plugin.ana.ANA_;

/**
 * Support algorithms for processing outlines using their specific properties.
 * 
 * @author p.baniukiewicz
 * @param <T> Outline or Snake class
 * @see ANA_
 * @see PointListProcessor
 */
public class OutlineProcessor<T extends Shape<?>> {
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(OutlineProcessor.class.getName());

  private T outline;

  /**
   * Assign outline to be processed to object.
   * 
   * @param outline Reference to Outline to be processed
   */
  public OutlineProcessor(T outline) {
    this.outline = outline;
  }

  /**
   * Apply running mean filter to Shape.
   * 
   * <p>Do not create new Shape but modify nodes of existing one. Compute
   * {@link Shape#calcCentroid()}, {@link Shape#updateNormals(boolean)} and
   * {@link Shape#setPositions()}. Normals are updated inwards.
   * 
   * @param window size of filter window, must be uneven
   * @param iters number of smoothing iterations
   * @return reference to this object. Allows chaining
   */
  public OutlineProcessor<T> runningMean(int window, int iters) {
    PointListProcessor pp = new PointListProcessor(outline.asList());
    pp.smoothMean(window, iters);
    List<Point2d> p = pp.getList();
    Iterator<?> it = outline.iterator();
    Iterator<Point2d> itl = p.iterator();
    while (it.hasNext() && itl.hasNext()) {
      PointsList<?> n = (PointsList<?>) it.next();
      ExtendedVector2d v = n.getPoint();
      Point2d pr = itl.next();
      v.x = pr.x;
      v.y = pr.y;
    }
    outline.calcCentroid();
    outline.updateNormals(true);
    outline.setPositions();
    return this;
  }

  /**
   * Apply running median filter to Shape.
   * 
   * <p>Do not create new Shape but modify nodes of existing one. Compute
   * {@link Shape#calcCentroid()}, {@link Shape#updateNormals(boolean)} and
   * {@link Shape#setPositions()}. Normals are updated inwards.
   * 
   * @param window size of filter window, must be uneven
   * @param iters number of smoothing iterations
   * @return reference to this object. Allows chaining
   */
  public OutlineProcessor<T> runningMedian(int window, int iters) {
    PointListProcessor pp = new PointListProcessor(outline.asList());
    pp.smoothMedian(window, iters);
    List<Point2d> p = pp.getList();
    Iterator<?> it = outline.iterator();
    Iterator<Point2d> itl = p.iterator();
    while (it.hasNext() && itl.hasNext()) {
      PointsList<?> n = (PointsList<?>) it.next();
      ExtendedVector2d v = n.getPoint();
      Point2d pr = itl.next();
      v.x = pr.x;
      v.y = pr.y;
    }
    outline.calcCentroid();
    outline.updateNormals(true);
    outline.setPositions();
    return this;
  }

  /**
   * Compute average curvature of {@link Outline}.
   * 
   * <p>Does not apply to {@link Snake}. Require {@link Outline#updateCurvature()} to be called
   * first to fill {@link Vert#curvatureLocal} field.
   * 
   * @param averdistance Average distance in pixels to compute curvature. Nodes in half of distance
   *        on left and right from current node will be included in average curvature for this node.
   * @return instance of this object.
   */
  public OutlineProcessor<T> averageCurvature(double averdistance) {
    Vert v;
    Vert tmpV;
    double totalCur;
    double distance;
    int count;
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    // Average over curvatures
    if (averdistance > 0) {
      // System.out.println("new outline");
      v = (Vert) outline.getHead();
      do {
        // System.out.println("\tnew vert");
        totalCur = v.curvatureLocal; // reset
        count = 1;

        // add up curvatures of prev nodes
        // System.out.println("\t prev nodes");
        tmpV = v.getPrev();
        distance = 0;
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureLocal;
          count++;
          tmpV = tmpV.getPrev();
        } while (distance < averdistance / 2);

        // add up curvatures of next nodes
        distance = 0;
        tmpV = v.getNext();
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureLocal;
          count++;
          tmpV = tmpV.getNext();
        } while (distance < averdistance / 2);

        v.curvatureSmoothed = totalCur / count;

        v = v.getNext();
      } while (!v.isHead());
    }
    return this;
  }

  /**
   * Sum smoothed curvature over a region of the membrane. For {@link Outline} only.
   * 
   * <p><p>Does not apply to {@link Snake}. Require {@link #averageCurvature(double)} to be called
   * first to fill {@link Vert#curvatureSmoothed} field.
   * 
   * @param averdistance Average distance in pixels to compute curvature. Nodes in half of distance
   *        on left and right from current node will be included in average curvature for this node.
   * @return instance of this object.
   */
  public OutlineProcessor<T> sumCurvature(double averdistance) {
    Vert v;
    Vert tmpV;
    double totalCur;
    double distance;
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    // Average over curvatures
    if (averdistance > 0) {
      v = (Vert) outline.getHead();
      do {
        // System.out.println("\tnew vert");
        totalCur = v.curvatureSmoothed; // reset
        // add up curvatures of prev nodes
        // System.out.println("\t prev nodes");
        tmpV = v.getPrev();
        distance = 0;
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureSmoothed;
          tmpV = tmpV.getPrev();
        } while (distance < averdistance / 2);

        // add up curvatures of next nodes
        distance = 0;
        tmpV = v.getNext();
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureSmoothed;
          tmpV = tmpV.getNext();
        } while (distance < averdistance / 2);

        v.curvatureSum = totalCur;

        v = v.getNext();
      } while (!v.isHead());
    }
    return this;
  }

  // /**
  // * Compute running mean on <tt>Outline</tt>.
  // *
  // * @param window Window size
  // * @deprecated Will not be used after implementing HatSnakeFilter for getting weights.
  // */
  // private void runningmeanfilter(int window) {
  // if (!(outline instanceof Outline)) {
  // throw new IllegalArgumentException("This method applies to Outline only");
  // }
  // double[] curv;
  // int l;
  // Vert n;
  // curv = getCurvatureLocal();
  // PointListProcessor.runningMean(curv, window);
  // // copy back to outline
  // n = (Vert) outline.getHead();
  // l = 0;
  // do {
  // n.curvatureLocal = curv[l];
  // n = n.getNext();
  // l++;
  // } while (!n.isHead());
  // }

  // /**
  // * Shrink the outline nonlinearly.
  // *
  // * <p>TODO Nonlinearlity not implemented.
  // *
  // * @param steps number of steps - integer
  // * @param stepRes length of the step
  // * @param angleTh angle threshold
  // * @param freezeTh freeze threshold
  // * @see Outline#scaleOutline(double, double, double, double)
  // */
  // public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh) {
  // // later drop any local feature like curvature and use rather snakehatfileter to get proper
  // // weighting
  // if (!(outline instanceof Outline)) {
  // throw new IllegalArgumentException("This method applies to Outline only");
  // }
  // LOGGER.debug("Steps: " + steps);
  // LOGGER.debug("Original res: " + outline.getNumPoints());
  // int meanmasksize = 5;
  // // System.out.println("steps: " + steps + ", step size: " +
  // // ANAp.stepRes);
  // Vert n;
  // int j;
  // int max = 10000;
  // double d = outline.getLength() / outline.getNumPoints();
  //
  // for (j = 0; j < steps; j++) {
  // runningmeanfilter(meanmasksize);
  // if (outline.getNumPoints() <= 3) {
  // break;
  // }
  // n = (Vert) outline.getHead();
  // do {
  // if (!n.isFrozen()) {
  // n.setX(n.getX() - stepRes * 1.0 * n.getNormal().getX());
  // n.setY(n.getY() - stepRes * 1.0 * n.getNormal().getY());
  // }
  // n = n.getNext();
  // } while (!n.isHead());
  //
  // ((Outline) outline).removeProx(1.5, 1.5);
  // ((Outline) outline).freezeProx(angleTh, freezeTh);
  // // double d = outline.getLength() / outline.getNumVerts();
  // ((Outline) outline).correctDensity(d, d / 2);
  // ((Outline) outline).updateNormals(true);
  // ((Outline) outline).updateCurvature();
  //
  // // do not shrink if there are 4 nodes or less
  // if (outline.getNumPoints() <= 4) {
  // LOGGER.debug("Stopped iterations");
  // break;
  // }
  //
  // if (j > max) {
  // LOGGER.warn("shrink (336) hit max iterations!");
  // break;
  // }
  // }
  //
  // if (outline.getNumPoints() < 3) {
  // LOGGER.info("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
  // }
  //
  // if (((Outline) outline).cutSelfIntersects()) {
  // LOGGER.debug("ANA_(382)...fixed ana intersects");
  // }
  //
  // if (outline.getNumPoints() < 3) {
  // LOGGER.info("ANA 377_NODES LESS THAN 3");
  // }
  //
  // // LOGGER.debug("Shrank Verts: " + outline.getNumVerts());
  // // LOGGER.debug("Verts after density correction: " + outline.getNumVerts());
  // // LOGGER.debug("Density " + d + " [" + d / 4 + "," + d / 2 + "]");
  // }

  /**
   * Compatibility layer.
   * 
   * @param steps steps
   * @param stepRes stepRes
   * @param angleTh angleTh
   * @param freezeTh freezeTh
   * @see #shrinknl(double, double, double, double, double, double, int)
   */
  public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh) {
    final double defSigma = 0.3;
    final double defAmpl = 5;
    shrinknl(steps, stepRes, angleTh, freezeTh, defSigma, defAmpl, 0);
  }

  /**
   * Shrink the outline nonlinearly.
   * 
   * <p>This method uses {@link Vert#curvatureSum} computed by
   * {@link OutlineProcessor#sumCurvature(double)} to produce nonlinear scaling. Scaling factor
   * depends on {@link Vert#curvatureSum} and it is computed by
   * {@link #amplificationFactor(double, double, double)} method. Nodes are shifted inwards
   * according to normals ({@link Vert#getNormal()}). If <i>averageNormals</i> is positive, this
   * number of nodes on each side of current node will be analysed to find lowest (must be negative)
   * {@link Vert#curvatureSum} and then normal vector of this node will be copied to all other nodes
   * in current window.
   * 
   * <p>{@link #amplificationFactor(double, double, double)} increase step size used by
   * {@link #shrinknl(double, double, double, double)} maximally by factor of <t>magn</t>
   * ({@link #amplificationFactor(double, double, double)} option).
   * 
   * @param steps number of steps - integer
   * @param stepRes length of the step
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   * @param sigma sigma of Gaussian
   * @param magn maximum amplification (for curv<<0)
   * @param averageNormals number of neighbouring nodes to set normals the same (set 0 to disable)
   * @see Outline#scaleOutline(double, double, double, double)
   * @see OutlineProcessor#amplificationFactor(double, double, double)
   */
  public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh, double sigma,
          double magn, int averageNormals) {
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    LOGGER.debug("Steps: " + steps);
    LOGGER.debug("Original res: " + outline.getNumPoints());
    Vert n;
    int max = 10000;
    double d = outline.getLength() / outline.getNumPoints();
    ((Outline) outline).correctDensity(d, d / 2);
    ((Outline) outline).updateNormals(true);
    ((Outline) outline).updateCurvature();
    double d1 = outline.getLength() / outline.getNumPoints();
    averageCurvature(d1).sumCurvature(d1);
    constrainNormals(d1 * averageNormals);

    LOGGER.debug("Res after 1: " + outline.getNumPoints());

    for (int j = 0; j < steps; j++) {
      n = (Vert) outline.getHead();
      do {
        if (!n.isFrozen()) {
          n.setX(n.getX() - stepRes * amplificationFactor(n.curvatureSum, sigma, magn)
                  * n.getNormal().getX());
          n.setY(n.getY() - stepRes * amplificationFactor(n.curvatureSum, sigma, magn)
                  * n.getNormal().getY());
        }
        n = n.getNext();
      } while (!n.isHead());

      ((Outline) outline).removeProx(1.5, 1.5);
      ((Outline) outline).freezeProx(angleTh, freezeTh);
      ((Outline) outline).correctDensity(d, d / 2);
      // ((Outline) outline).updateNormals(true); // sometimes it is better
      ((Outline) outline).updateCurvature();
      d1 = outline.getLength() / outline.getNumPoints();
      averageCurvature(d1).sumCurvature(d1);
      // equalNormales(d1 * 5); // better if original normals stay. interpolate can add vertex but
      // ith will have updated normales

      // do not shrink if there are 4 nodes or less
      if (outline.getNumPoints() <= 4) {
        LOGGER.debug("Stopped iterations");
        break;
      }

      if (j > max) {
        LOGGER.warn("shrink (336) hit max iterations!");
        break;
      }
    }

    if (outline.getNumPoints() < 3) {
      LOGGER.info("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
    }

    if (((Outline) outline).cutSelfIntersects()) {
      LOGGER.debug("ANA_(382)...fixed ana intersects");
    }

    if (outline.getNumPoints() < 3) {
      LOGGER.info("ANA 377_NODES LESS THAN 3");
    }
  }

  /**
   * Set normals to the same direction according to local minimum of sumCurvature.
   * 
   * @param averdistance distance to look along. Set 0 to turn off any modification. Any value
   *        smaller than distance will cause that at least 3 nodes are taken.
   */
  private void constrainNormals(double averdistance) {
    Vert v;
    Vert tmpV;
    double distance;
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    if (averdistance == 0) {
      return;
    }
    List<Double> curvSums = new ArrayList<>(); // will keep local curvature sums in +-distance
    List<ExtendedVector2d> norms = new ArrayList<>(); // will keep normals in +-distance

    if (averdistance > 0) {
      v = (Vert) outline.getHead();
      do { // main loop
        curvSums.clear(); // new vertex
        norms.clear();
        curvSums.add(v.curvatureSum); // remember it
        norms.add(v.getNormal());
        tmpV = v.getPrev();
        distance = 0;
        do { // after current vert in +distance
          distance += ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
          curvSums.add(tmpV.curvatureSum); // Remember all verts in this window
          norms.add(tmpV.getNormal());
          tmpV = tmpV.getPrev();
        } while (distance < averdistance / 2);
        distance = 0;
        tmpV = v.getNext();
        do { // before current vert
          distance += ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
          curvSums.add(tmpV.curvatureSum); // Remember all verts in this window
          norms.add(tmpV.getNormal());
          tmpV = tmpV.getNext();
        } while (distance < averdistance / 2);
        // v is current vert
        // find min curv
        Double minCurv = Collections.min(curvSums);
        int indmin = curvSums.indexOf(minCurv); // min curvature index
        if (minCurv < 0) { // propagate only in curvature is negative
          ExtendedVector2d normmin = norms.get(indmin); // get its normale
          v.setNormal(normmin.getX(), normmin.getY()); // and propagate to current
        }
        v = v.getNext(); // go to next vert
      } while (!v.isHead());
    }
  }

  // /**
  // * Return local curvature as array. Applies to Outline.class, returns array of zeros for other
  // * classes.
  // *
  // * @return local curvature values or aray of zeros
  // * @see Vert#curvatureLocal
  // * @deprecated Will be removed after implementing nonproportional shrinking based on HSF
  // */
  // public double[] getCurvatureLocal() {
  // double[] ret = new double[outline.getNumPoints()];
  // if (outline instanceof Outline) {
  // int l = 0;
  // Vert n = (Vert) outline.getHead();
  // do {
  // ret[l] = n.curvatureLocal;
  // n = n.getNext();
  // l++;
  // } while (!n.isHead());
  // }
  // return ret;
  // }

  /**
   * Return amplification factor dependent on curvature.
   * 
   * <p>For positive curvature it returns 1, for negative a reversed Gaussian curve scaled between
   * 1.0 and specified magnitude is returned.
   * 
   * <pre>
   * <code>
   * x=linspace(-2,0,100);
   * sig = 0.3;
   * u = 0;
   * a = 4
   * y = (a*(1-exp(-0.5*((x-u)/sig).^2)))/(a/(a-1))+1;
   * plot(x,y)
   * </code>
   * </pre>
   * 
   * @param curv curvature value to compute magnification coefficient for
   * @param sigma sigma of Gaussian
   * @param magn maximum amplification (for curv<<0)
   * @return amplification coefficient
   */
  public double amplificationFactor(double curv, double sigma, double magn) {
    double ampl;
    double u = 0;
    if (magn == 1) {
      return 1.0;
    }
    if (curv >= 0) {
      ampl = 1.0;
    } else {
      ampl = (magn * (1 - Math.exp(-0.5 * (Math.pow((curv - u) / sigma, 2))))) / (magn / (magn - 1))
              + 1;
    }
    return ampl;
  }

  /**
   * Outline getter.
   * 
   * @return the outline
   */
  public T getO() {
    return outline;
  }

}
