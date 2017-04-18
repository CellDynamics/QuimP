package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import ij.IJ;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

/**
 * ODE Solver.
 * 
 * @author rtyson
 *
 */
public class ODEsolver {

  private static boolean inside;

  /**
   * Default constructor.
   */
  public ODEsolver() {
  }

  /**
   * Euler solver.
   * 
   * @param v vertex
   * @param s sector
   * @return ?
   */
  public static ExtendedVector2d euler(Vert v, Sector s) {
    // Vect2d[] history = new Vect2d[ECMp.maxIter];
    int x;
    int y;
    int lastSampleX = -1;
    int lastSampleY = -1; // store where last sample was
    double dist = 0; // distance migrated
    double tempFlu;
    Vert edge;
    ExtendedVector2d p;
    ExtendedVector2d pp;

    v.snapped = false;

    if (ECMp.ANA) { // sample at boundary
      p = v.getPoint();
      x = (int) Math.round(p.getX());
      y = (int) Math.round(p.getY());
      lastSampleX = x;
      lastSampleY = y;
      tempFlu = ODEsolver.sampleFluo(ECMp.image, x, y);
      v.fluores[0].intensity = tempFlu;
      v.fluores[0].x = x; // store in first slot
      v.fluores[0].y = y;
    }

    if (ECMp.plot) {
      ECMM_Mapping.plot.setColor(0, 0, 0);
    }

    p = new ExtendedVector2d(v.getX(), v.getY());
    pp = new ExtendedVector2d(v.getX(), v.getY()); // previouse position

    // history[0] = new Vect2d(p.getX(), p.getY());

    boolean maxHit = false;
    int i = 1;
    ExtendedVector2d k;

    for (; i < ECMp.maxIter - 1; i++) {
      // IJ.log("\tIt " + i); //debug
      if (ODEsolver.proximity(p, s) || (ECMp.ANA && dist >= (ECMp.anaMigDist)) || maxHit) {
        // stop when within d of the target segment or
        // if migrated more than the ana set cortex width (in pixels)
        pp.setX(p.getX());
        pp.setY(p.getY());

        // if(!ECMp.ANA) { // no need to snap ana result. landing coord
        // not needed
        edge = ODEsolver.snap(p, s);
        dist += ExtendedVector2d.lengthP2P(pp, p);
        v.distance = QuimpToolsCollection.speedToScale(dist, ECMp.scale, ECMp.frameInterval);
        // if (s.expanding && !ECMp.ANA) {
        v.setLandingCoord(p, edge);
        // }
        // }

        if (ECMp.plot && ECMp.drawPaths) {
          ECMM_Mapping.plot.setColor(0, 0, 0);
          ECMM_Mapping.plot.drawLine(pp, p);
        }

        v.snapped = true;
        // System.out.println("iterations: " + i);
        break;
      }

      k = ODEsolver.dydt(p, s);
      k.multiply(ECMp.h);

      pp.setX(p.getX());
      pp.setY(p.getY());
      p.setX(p.getX() + k.getX());
      p.setY(p.getY() + k.getY());
      dist += ExtendedVector2d.lengthP2P(pp, p);

      if (ECMp.plot && ECMp.drawPaths) {
        // ECMM_Mapping.plot.setColor(1, 0, 0);
        ECMM_Mapping.plot.drawLine(pp, p);
      }
      // history[i] = new Vect2d(p.getX(), p.getY());

      if (ECMp.ANA) { // sample
        x = (int) Math.round(p.getX());
        y = (int) Math.round(p.getY());
        if (!(x == lastSampleX && y == lastSampleY)) { // on sample new locations
          lastSampleX = x;
          lastSampleY = y;
          tempFlu = ODEsolver.sampleFluo(ECMp.image, x, y);

          if (tempFlu > v.fluores[0].intensity) { // store first one
            // if((tempFlu / v.fluores[0].intensity)<1.1){
            // maxHit = true;
            // }
            v.fluores[0].intensity = tempFlu;
            v.fluores[0].x = x;
            v.fluores[0].y = y;

          }
        }
      }

      ECMp.its++;
    }

    if (ECMp.plot && !v.snapped && ECMp.drawFails) { // mark the start point of failed nodes
      ECMM_Mapping.plot.setColor(1, 0, 0);
      // p.print(v.getTrackNum() + "p: ");
      // pp.print(v.getTrackNum() + "pp: ");
      ECMM_Mapping.plot.drawCircle(v.getPoint(), 5);
    }

    return p;
  }

  /**
   * Get first derivative.
   * 
   * @param p ExtendedVector2d
   * @param s Sector
   * @return dy/dt
   */
  public static ExtendedVector2d dydt(ExtendedVector2d p, Sector s) {
    ExtendedVector2d result = fieldAt(p, s);
    result.multiply(ECMp.mobileQ);

    if (true) { // Math.abs(result.length()) > ECMp.maxVertF) {
      // IJ.log("!WARNING-max force exceeded: " +
      // Math.abs(result.length()));
      result.makeUnit();
      result.multiply(ECMp.maxVertF);
    }
    return result;
  }

  /**
   * Proximity.
   * 
   * @param p ExtendedVector2d
   * @param s Sector
   * @return ?
   */
  public static boolean proximity(ExtendedVector2d p, Sector s) {
    // could test against the chrages or the actual contour.
    // if using polar lines can use actual contour
    // Vert v = s.getTarStart();
    // if(true) return false;
    // Vert v = s.tarCharges.getHead();
    Vert v = s.getTarStart();
    do {
      double d = ExtendedVector2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
      // IJ.log("\t\tprox to: " + d); //debug
      if (d <= ECMp.d) {
        return true;
      }
      v = v.getNext();
    } while (!v.isIntPoint());
    return false;
  }

  private static Vert snap(ExtendedVector2d p, Sector s) {
    // snap p to the closest segment of target contour
    ExtendedVector2d current;
    Vert closestEdge;
    double distance;// = ECMp.d + 1; // must be closer then d+1, good starting value
    double tempDis;

    Vert v = s.getTarStart().getPrev(); // include the edge to the starting intersect pount
    distance = ExtendedVector2d.distPointToSegment(p, v.getPoint(), v.getNext().getPoint());
    v = v.getNext();
    closestEdge = v;
    do {
      current = ExtendedVector2d.PointToSegment(p, v.getPoint(), v.getNext().getPoint());
      tempDis = ExtendedVector2d.lengthP2P(p, current);

      if (tempDis < distance) {
        closestEdge = v;
        distance = tempDis;
      }
      v = v.getNext();
    } while (!v.isIntPoint());

    // p.setX(closest.getX());
    // p.setY(closest.getY());

    return closestEdge;
  }

  private static ExtendedVector2d fieldAt(ExtendedVector2d p, Sector s) {

    // Use line charges or point charges. remove if for speed
    // return fieldAtLines(p, s);
    if (ECMp.lineCharges) {
      return fieldAtLines(p, s);
    } else {
      return fieldAtPoints(p, s);
    }
  }

  private static ExtendedVector2d fieldAtPoints(ExtendedVector2d p, Sector s) {
    // calc the field size at p according to to migrating and target charges
    ExtendedVector2d field = new ExtendedVector2d(0, 0);
    ExtendedVector2d totalF = new ExtendedVector2d(0, 0);

    Vert v = s.migCharges.getHead();
    do {

      forceP(field, p, v.getPoint(), ECMp.migQ, ECMp.migPower);
      totalF.addVec(field);
      // totalF.print("\ttotlaF = ");
      v = v.getNext();
    } while (!v.getPrev().isIntPoint() || v.getPrev().isHead());

    v = s.tarCharges.getHead();
    do {
      forceP(field, p, v.getPoint(), ECMp.tarQ, ECMp.tarPower);
      totalF.addVec(field);
      v = v.getNext();
    } while (!v.getPrev().isIntPoint() || v.getPrev().isHead());

    return totalF;
  }

  private static void forceP(ExtendedVector2d force, ExtendedVector2d p, ExtendedVector2d pq,
          double q, double power) {
    double r = ExtendedVector2d.lengthP2P(pq, p);
    // System.out.println("\t r = " + r);
    if (r == 0) {
      force.setX(250);
      force.setY(250);
      IJ.log("!WARNING-FORCE INFINITE");
      return;
    }
    r = Math.abs(Math.pow(r, power));
    ExtendedVector2d unitV = ExtendedVector2d.unitVector(pq, p);
    double multiplier = (ECMp.k * (q / r));
    force.setX(unitV.getX() * multiplier);
    force.setY(unitV.getY() * multiplier);
  }

  private static ExtendedVector2d fieldAtLines(ExtendedVector2d p, Sector s) {
    // calc the field size at p according to to migrating and target charges
    ExtendedVector2d field = new ExtendedVector2d(0, 0);
    ExtendedVector2d totalF = new ExtendedVector2d(0, 0);
    double polarDir;

    // inside or outside sector?
    inside = s.insideCharges(p);

    if (!inside) {
      polarDir = -1;
      // System.out.println("switched");
    } else {
      polarDir = 1;
    }

    Vert v = s.migCharges.getHead();
    do {

      // forceL(field, p, v.getPoint(), v.getNext().getPoint(),
      // ECMp.migQ);

      /*
       * //times by the outerDirection to make lines polar sideDis =
       * Vect2d.distPoinToInfLine(p, v.getPoint(), v.getNext().getPoint()); if (sideDis < 0) {
       * polarDir = s.outerDirection * -1; } else { polarDir = s.outerDirection; }
       *
       */

      forceLpolar(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.migQ, ECMp.migPower,
              polarDir);

      totalF.addVec(field);
      v = v.getNext();
    } while (!v.isIntPoint() || v.isHead());

    v = s.tarCharges.getHead();
    do {
      // forceL(field, p, v.getPoint(), v.getNext().getPoint(),
      // ECMp.tarQ);

      /*
       * sideDis = Vect2d.distPoinToInfLine(p, v.getPoint(), v.getNext().getPoint()); if
       * (sideDis < 0) { polarDir = s.outerDirection ; } else { polarDir = s.outerDirection *
       * -1; }
       *
       */

      forceLpolar(field, p, v.getPoint(), v.getNext().getPoint(), ECMp.tarQ, ECMp.tarPower,
              polarDir);

      totalF.addVec(field);
      v = v.getNext();
    } while (!v.isIntPoint() || v.isHead());

    return totalF;
  }

  private static void forceLpolar(ExtendedVector2d force, ExtendedVector2d p, ExtendedVector2d s1,
          ExtendedVector2d s2, double q, double power, double orientation) {
    double l = ExtendedVector2d.lengthP2P(s1, s2);
    ExtendedVector2d ru = ExtendedVector2d.unitVector(s2, p);
    double r = ExtendedVector2d.lengthP2P(s2, p);
    ExtendedVector2d rpU = ExtendedVector2d.unitVector(s1, p);
    double rp = ExtendedVector2d.lengthP2P(s1, p);

    double d = (((rp + r) * (rp + r)) - (l * l)) / (2 * l);
    // double d = ( Math.pow((rp + r), power) - (L * L)) / (2 * L);
    double multiplier = ((ECMp.k * q) / d);
    rpU.addVec(ru);

    force.setX(rpU.getX() * multiplier * orientation);
    force.setY(rpU.getY() * multiplier * orientation);
  }

  /**
   * Get intensity value for given coordinates.
   * 
   * @param ip image to sample intensities
   * @param x screen coordinate
   * @param y screen coordinate
   * @return mean intensity within 9-point stencil located at x,y
   */
  public static double sampleFluo(ImageProcessor ip, int x, int y) {
    double tempFlu = ip.getPixelValue(x, y) + ip.getPixelValue(x - 1, y)
            + ip.getPixelValue(x + 1, y) + ip.getPixelValue(x, y - 1) + ip.getPixelValue(x, y + 1)
            + ip.getPixelValue(x - 1, y - 1) + ip.getPixelValue(x + 1, y + 1)
            + ip.getPixelValue(x + 1, y - 1) + ip.getPixelValue(x - 1, y + 1);
    tempFlu = tempFlu / 9d;
    return tempFlu;
  }
}