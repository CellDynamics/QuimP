package uk.ac.warwick.wsbc.quimp.geom.filters;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.scijava.vecmath.Tuple2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.plugin.ana.ANA_;

/**
 * Support algorithms for processing outlines.
 * 
 * @author p.baniukiewicz
 * @see ANA_
 */
public class OutlineProcessor {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(OutlineProcessor.class.getName());

  private Outline outline;

  /**
   * Assign outline to be processed to object.
   * 
   * @param outline Reference to Outline to be processed
   */
  public OutlineProcessor(Outline outline) {
    this.outline = outline;
  }

  /**
   * Compute running mean on <tt>curvatureLocal</tt>.
   * 
   * @param window Window size
   * @return array of filtered coefficients in order of vertexes.
   */
  public double[] runningmeanfilter(int window) {
    // FIXME There is no looping, first and last vertexes are skipped.
    int half = window / 2;
    // copy to array
    double[] curv = new double[outline.getNumVerts()];
    double[] curvf = new double[outline.getNumVerts()];
    Vert n;
    int l = 0;
    n = outline.getHead();
    do {
      curv[l] = n.curvatureLocal;
      n = n.getNext();
      l++;
    } while (!n.isHead());
    // LOGGER.debug(
    // "Min=" + QuimPArrayUtils.arrayMin(curv) + " Max=" + QuimPArrayUtils.arrayMax(curv));

    for (int i = half; i < curv.length - 1 - half; i++) {
      double min = 0;
      for (int inner = i - half; inner <= i + half; inner++) {
        // if (curv[inner] < min)
        min += curv[inner];
      }
      curvf[i] = min / window;
    }

    n = outline.getHead();
    l = 0;
    do {
      n.curvatureLocal = curvf[l];
      n = n.getNext();
      l++;
    } while (!n.isHead());

    return curvf;

  }

  /**
   * Shrink the outline nonlinearly.
   * 
   * @param steps number of steps - integer
   * @param stepRes length of the step
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   * @see #shrink(double, double, double, double)
   */
  public void shrinknl(double steps, double stepRes, double angleTh, double freezeTh) {
    LOGGER.debug("Steps: " + steps);
    LOGGER.debug("Original res: " + outline.getNumVerts());
    int meanmasksize = 5;
    // System.out.println("steps: " + steps + ", step size: " +
    // ANAp.stepRes);
    Vert n;
    int j;
    int max = 10000;
    double d = outline.getLength() / outline.getNumVerts();

    for (j = 0; j < steps; j++) {
      runningmeanfilter(meanmasksize);
      if (outline.getNumVerts() <= 3) {
        break;
      }
      n = outline.getHead();
      do {
        if (!n.frozen) {
          n.setX(n.getX() - stepRes * 1.0 * n.getNormal().getX());
          n.setY(n.getY() - stepRes * 1.0 * n.getNormal().getY());
        }
        n = n.getNext();
      } while (!n.isHead());

      removeProx();
      freezeProx(angleTh, freezeTh);
      // double d = outline.getLength() / outline.getNumVerts();
      outline.correctDensity(d, d / 2);
      outline.updateNormales(true);
      outline.updateCurvature();

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

    if (outline.getNumVerts() < 3) {
      System.out.println("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
    }

    if (outline.cutSelfIntersects()) {
      System.out.println("ANA_(382)...fixed ana intersects");
    }

    if (outline.getNumVerts() < 3) {
      System.out.println("ANA 377_NODES LESS THAN 3");
    }

    // LOGGER.debug("Shrank Verts: " + outline.getNumVerts());
    // LOGGER.debug("Verts after density correction: " + outline.getNumVerts());
    // LOGGER.debug("Density " + d + " [" + d / 4 + "," + d / 2 + "]");
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
   * Shrink the outline linearly.
   * 
   * @param steps Number of steps
   * @param stepRes shift done in one step
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   */
  public void shrink(double steps, double stepRes, double angleTh, double freezeTh) {
    Vert n;
    int j;
    int max = 10000;
    for (j = 0; j < steps; j++) {
      if (outline.getNumVerts() <= 3) {
        break;
      }
      n = outline.getHead();
      do {
        if (!n.frozen) {
          n.setX(n.getX() - stepRes * n.getNormal().getX());
          n.setY(n.getY() - stepRes * n.getNormal().getY());
        }
        n = n.getNext();
      } while (!n.isHead());
      outline.updateNormales(true);
      removeProx();
      freezeProx(angleTh, freezeTh);
      if (j > max) {
        LOGGER.warn("shrink (336) hit max iterations!");
        break;
      }
    }

    if (outline.getNumVerts() < 3) {
      System.out.println("ANA 377_NODES LESS THAN 3 BEFORE CUTS");
    }

    if (outline.cutSelfIntersects()) {
      System.out.println("ANA_(382)...fixed ana intersects");
    }

    if (outline.getNumVerts() < 3) {
      System.out.println("ANA 377_NODES LESS THAN 3");
    }
  }

  /**
   * Remove close vertexes.
   */
  private void removeProx() {
    if (outline.getNumVerts() <= 3) {
      return;
    }
    Vert v;
    Vert vl;
    Vert vr;
    double d1;
    double d2;
    v = outline.getHead();
    vl = v.getPrev();
    vr = v.getNext();
    do {
      d1 = ExtendedVector2d.lengthP2P(v.getPoint(), vl.getPoint());
      d2 = ExtendedVector2d.lengthP2P(v.getPoint(), vr.getPoint());

      if ((d1 < 1.5 || d2 < 1.5) && !v.frozen) { // don't remove frozen. May alter angles
        outline.removeVert(v);
      }
      v = v.getNext().getNext();
      vl = v.getPrev();
      vr = v.getNext();
    } while (!v.isHead() && !vl.isHead());

  }

  /**
   * Freeze a node and corresponding edge if its to close && close to paralel.
   * 
   * @param angleTh angle threshold
   * @param freezeTh freeze threshold
   */
  private void freezeProx(double angleTh, double freezeTh) {
    Vert v;
    Vert vtmp;
    ExtendedVector2d closest;
    ExtendedVector2d edge;
    ExtendedVector2d link;
    double dis;
    double angle;

    v = outline.getHead();
    do {
      // if (!v.frozen) {
      vtmp = outline.getHead();
      do {
        if (vtmp.getTrackNum() == v.getTrackNum()
                || vtmp.getNext().getTrackNum() == v.getTrackNum()) {
          vtmp = vtmp.getNext();
          continue;
        }
        closest = ExtendedVector2d.PointToSegment(v.getPoint(), vtmp.getPoint(),
                vtmp.getNext().getPoint());
        dis = ExtendedVector2d.lengthP2P(v.getPoint(), closest);
        // System.out.println("dis: " + dis);
        // dis=1;
        if (dis < freezeTh) {
          edge = ExtendedVector2d.unitVector(vtmp.getPoint(), vtmp.getNext().getPoint());
          link = ExtendedVector2d.unitVector(v.getPoint(), closest);
          angle = Math.abs(ExtendedVector2d.angle(edge, link));
          if (angle > Math.PI) {
            angle = angle - Math.PI; // if > 180, shift back around
          }
          // 180
          angle = angle - 1.5708; // 90 degree shift to centre around zero
          // System.out.println("angle:" + angle);

          if (angle < angleTh && angle > -angleTh) {
            v.frozen = true;
            vtmp.frozen = true;
            vtmp.getNext().frozen = true;
          }

        }
        vtmp = vtmp.getNext();
      } while (!vtmp.isHead());
      // }
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Outline getter.
   * 
   * @return the outline
   */
  public Outline getO() {
    return outline;
  }

}

/**
 * Helper class supporting scaling and fitting polygon to DrawWindow
 * 
 * <p>This class is strictly TreeSet related. equals method does not assure correct comparison
 * 
 * @author p.baniukiewicz
 *
 */
class ExPolygon extends Polygon {
  static final Logger LOGGER = LoggerFactory.getLogger(ExPolygon.class.getName());
  private static final long serialVersionUID = 5870934217878285135L;
  public Rectangle initbounds; // initial size of polygon, before scaling
  public double scale; // current scale

  /**
   * Construct polygon from list of points.
   * 
   * @param data List of points
   */
  public ExPolygon(List<? extends Tuple2d> data) {
    // convert to polygon
    for (Tuple2d v : data) {
      addPoint((int) Math.round(v.getX()), (int) Math.round(v.getY()));
    }
    initbounds = new Rectangle(getBounds()); // remember original size
    scale = 1;
  }

  /**
   * Scale polygon to fit in rectangular window of size. Method changes internal polygon
   * representation. Fitting is done basing on bounding box area.
   * 
   * @param size Size of window to fit polygon
   */
  public void fitPolygon(double size) {
    // set in 0,0
    translate((int) Math.round(-initbounds.getCenterX()),
            (int) Math.round(-initbounds.getCenterY()));
    // get size of bounding box
    Rectangle2D bounds = getBounds2D();
    // set scale according to window size
    if (bounds.getWidth() > bounds.getHeight()) {
      scale = bounds.getWidth();
    } else {
      scale = bounds.getHeight();
    }
    scale = size / scale;
    scale *= 0.95; // little smaller than window
    for (int i = 0; i < npoints; i++) {
      xpoints[i] = (int) Math.round(xpoints[i] * scale);
      ypoints[i] = (int) Math.round(ypoints[i] * scale);
    }
    // center in window
    LOGGER.debug("Scale is: " + scale + " BoundsCenters: " + bounds.getCenterX() + " "
            + bounds.getCenterY());
    translate((int) Math.round(bounds.getCenterX()) + (int) (size / 2),
            (int) Math.round(bounds.getCenterY()) + (int) (size / 2));
  }

  /**
   * Scale polygon to fit in rectangular window of size using pre-computed bounding box and scale
   * 
   * <p>Use for setting next polygon on base of previous, when next has different shape but must be
   * centered with previous one.
   * 
   * @param size Size of window to fit polygon
   * @param init Bounding box to fit new polygon
   * @param scale Scale of new polygon
   */
  public void fitPolygon(double size, Rectangle2D init, double scale) {
    // set in 0,0
    this.scale = scale;
    LOGGER.debug("fitPolygon: Scale is: " + scale + " BoundsCenters: " + init.getCenterX() + " "
            + init.getCenterY());
    translate((int) Math.round(-init.getCenterX()), (int) Math.round(-init.getCenterY()));

    for (int i = 0; i < npoints; i++) {
      xpoints[i] = (int) Math.round(xpoints[i] * scale);
      ypoints[i] = (int) Math.round(ypoints[i] * scale);
    }
    translate((int) (size / 2), (int) (size / 2));
  }
}
