package com.github.celldynamics.quimp.geom.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.PointsList;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.plugin.ana.ANA_;
import com.github.celldynamics.quimp.plugin.utils.IPadArray;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

/**
 * Support algorithms for processing outlines.
 * 
 * @author p.baniukiewicz
 * @param <T> Outline or Snake class
 * @see ANA_
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
   * Compute running mean on <tt>Outline</tt>.
   * 
   * @param window Window size
   * @deprecated Will not be used after implementeing HatSnakeFilter for getting weights.
   */
  private void runningmeanfilter(int window) {
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    double[] curv;
    int l;
    Vert n;
    curv = getCurvatureLocal();
    runningMean(curv, window);
    // copy back to outline
    n = (Vert) outline.getHead();
    l = 0;
    do {
      n.curvatureLocal = curv[l];
      n = n.getNext();
      l++;
    } while (!n.isHead());
  }

  /**
   * Shrink the outline nonlinearly.
   * 
   * <p>TODO Nonlinearlity not implemented.
   * 
   * @param steps number of steps - integer
   * @param stepRes length of the step
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   * @see Outline#scale(double, double, double, double)
   */
  public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh) {
    // later drop any local feature like curvature and use rather snakehatfileter to get proper
    // weighting
    if (!(outline instanceof Outline)) {
      throw new IllegalArgumentException("This method applies to Outline only");
    }
    LOGGER.debug("Steps: " + steps);
    LOGGER.debug("Original res: " + outline.getNumPoints());
    int meanmasksize = 5;
    // System.out.println("steps: " + steps + ", step size: " +
    // ANAp.stepRes);
    Vert n;
    int j;
    int max = 10000;
    double d = outline.getLength() / outline.getNumPoints();

    for (j = 0; j < steps; j++) {
      runningmeanfilter(meanmasksize);
      if (outline.getNumPoints() <= 3) {
        break;
      }
      n = (Vert) outline.getHead();
      do {
        if (!n.isFrozen()) {
          n.setX(n.getX() - stepRes * 1.0 * n.getNormal().getX());
          n.setY(n.getY() - stepRes * 1.0 * n.getNormal().getY());
        }
        n = n.getNext();
      } while (!n.isHead());

      ((Outline) outline).removeProx(1.5, 1.5);
      ((Outline) outline).freezeProx(angleTh, freezeTh);
      // double d = outline.getLength() / outline.getNumVerts();
      ((Outline) outline).correctDensity(d, d / 2);
      ((Outline) outline).updateNormales(true);
      ((Outline) outline).updateCurvature();

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

    // LOGGER.debug("Shrank Verts: " + outline.getNumVerts());
    // LOGGER.debug("Verts after density correction: " + outline.getNumVerts());
    // LOGGER.debug("Density " + d + " [" + d / 4 + "," + d / 2 + "]");
  }

  /**
   * Apply mean filter to Shape. Recalculate centroid and normalised coords. Set normales inwards.
   * 
   * @param window size of mean window
   * @see OutlineProcessor#runningMean(double[], int)
   */
  public void smooth(int window) {
    QuimpDataConverter dt = new QuimpDataConverter(outline);
    double[] xcoords = dt.getX();
    double[] ycoords = dt.getY();
    runningMean(xcoords, window);
    runningMean(ycoords, window);
    PointsList<?> n = outline.getHead();
    int count = 0;
    // do not create new object, just replace coords
    do {
      n.setX(xcoords[count]);
      n.setY(ycoords[count]);
      n = n.getNext();
      count++;
    } while (!n.isHead());
    outline.calcCentroid();
    outline.setPositions();
    outline.updateNormales(true);
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

  /**
   * Return local curvature as array. Applies to Outline.class, returns array of zeros for other
   * classes.
   * 
   * @return local curvature values or aray of zeros
   * @see Vert#curvatureLocal
   */
  public double[] getCurvatureLocal() {
    double[] ret = new double[outline.getNumPoints()];
    if (outline instanceof Outline) {
      int l = 0;
      Vert n = (Vert) outline.getHead();
      do {
        ret[l] = n.curvatureLocal;
        n = n.getNext();
        l++;
      } while (!n.isHead());
    }
    return ret;
  }

  private double fcn(double curv) {
    double ret;
    if (curv >= 0) {
      ret = 1.0;
    } else {
      ret = 1 + 10 * (Math.exp(-curv * 0.5) / Math.exp(0.5));
    }
    return ret;
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
