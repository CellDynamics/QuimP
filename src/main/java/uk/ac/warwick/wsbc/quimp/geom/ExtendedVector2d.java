package uk.ac.warwick.wsbc.quimp.geom;

import java.awt.geom.Line2D;

import org.scijava.vecmath.Tuple2d;
import org.scijava.vecmath.Tuple2f;
import org.scijava.vecmath.Vector2d;
import org.scijava.vecmath.Vector2f;

// TODO: Auto-generated Javadoc
/**
 * Defines 2D vector and performs operations on vectors and lines.
 *
 * This definition of vector is often used in QuimP for expressing points as well
 *
 * @author Richard
 */
public class ExtendedVector2d extends Vector2d {

  /**
   * @param v
   */
  public ExtendedVector2d(double[] v) {
    super(v);
  }

  /**
   * @param t1
   */
  public ExtendedVector2d(Tuple2d t1) {
    super(t1);
  }

  /**
   * @param t1
   */
  public ExtendedVector2d(Tuple2f t1) {
    super(t1);
  }

  /**
   * @param v1
   */
  public ExtendedVector2d(Vector2d v1) {
    super(v1);
  }

  /**
   * @param v1
   */
  public ExtendedVector2d(Vector2f v1) {
    super(v1);
  }

  /**
   * 
   */
  public ExtendedVector2d() {
    super();
  }

  /**
   * Simple constructor.
   * 
   * Creates vector mounted at (0,0).
   * 
   * @param xx \c x coordinate of vector
   * @param yy \c y coordinate of vector
   */
  public ExtendedVector2d(double xx, double yy) {
    super(xx, yy);
  }

  private static final long serialVersionUID = -7238793665995665600L;

  /**
   * 
   */
  public void makeUnit() {

    double length = length();
    if (length != 0) {
      x = x / length;
      y = y / length;
      // vec.setX(vec.getX() / length);
      // vec.setY(vec.getY() / length);
    }
  }

  /**
   * @param nx
   * @param ny
   */
  public void setXY(double nx, double ny) {
    y = ny;
    x = nx;

  }

  /**
   * @param v
   */
  public void addVec(ExtendedVector2d v) {
    x += v.getX();
    y += v.getY();
  }

  /**
   * @param d
   */
  public void multiply(double d) {
    x *= d;
    y *= d;
  }

  /**
   * @param p
   */
  public void power(double p) {
    x = Math.pow(x, p);
    y = Math.pow(y, p);

  }

  /**
   * @param a
   * @param b
   * @return calc unit vector to target
   */
  public static ExtendedVector2d unitVector(ExtendedVector2d a, ExtendedVector2d b) {
    ExtendedVector2d vec = ExtendedVector2d.vecP2P(a, b);

    double length = vec.length();
    if (length != 0) {
      vec.setX(vec.getX() / length);
      vec.setY(vec.getY() / length);
    }
    return vec;
  }

  /**
   * @param a
   * @param b
   * @return vector between points
   */
  public static ExtendedVector2d vecP2P(ExtendedVector2d a, ExtendedVector2d b) {
    // calc a vector between two points
    ExtendedVector2d vec = new ExtendedVector2d();

    vec.setX(b.getX() - a.getX());
    vec.setY(b.getY() - a.getY());
    return vec;
  }

  /**
   * @param a
   * @param b
   * @return length of the vector
   */
  public static double lengthP2P(ExtendedVector2d a, ExtendedVector2d b) {
    ExtendedVector2d v = vecP2P(a, b);
    return v.length();
  }

  /**
   * Calculate the intersect between two edges \b A and \b B. Edge is defined as vector \b AB,
   * where \b A and \b B stand for initial and terminal points given as vectors mounted at (0,0).
   * 
   * @param nA1 initial point of \b A edge
   * @param nA2 terminal point of \b A edge
   * @param nB1 initial point of \b B edge
   * @param nB2 terminal point of \b B edge
   * @return Intersect point
   * @deprecated Actually not used in this version of QuimP
   */
  @Deprecated
  public static ExtendedVector2d lineIntersectionOLD(ExtendedVector2d nA1, ExtendedVector2d nA2,
          ExtendedVector2d nB1, ExtendedVector2d nB2) {
    double aA, bB, aB, bA, denom;
    aA = nA2.getY() - nA1.getY();
    bA = nA1.getX() - nA2.getX();

    aB = nB2.getY() - nB1.getY();
    bB = nB1.getX() - nB2.getX();

    denom = aA * bB - aB * bA;

    if (denom == 0) { // lines are parallel
      // System.out.println("parrellel lines");
      return null;
    }

    double cA, cB;

    cA = nA2.getX() * nA1.getY() - nA1.getX() * nA2.getY();
    cB = nB2.getX() * nB1.getY() - nB1.getX() * nB2.getY();

    ExtendedVector2d cp = null;
    double dA2, dA1, eA1, eA2, dB2, dB1, eB1, eB2;

    // intersection point
    cp = new ExtendedVector2d((bA * cB - bB * cA) / denom, (aB * cA - aA * cB) / denom);

    // System.out.println("Pos Intersect at x:" + cp.getX() + ", y:" +
    // cp.getY());

    // check in bounds of line1
    dA1 = cp.getX() - nA1.getX(); // line1.getX1();
    dA2 = cp.getX() - nA2.getX(); // line1.getX2();
    eA1 = cp.getY() - nA1.getY(); // line1.getY1();
    eA2 = cp.getY() - nA2.getY(); // line1.getY2();

    dB1 = cp.getX() - nB1.getX(); // line2.getX1();
    dB2 = cp.getX() - nB2.getX(); // line2.getX2();
    eB1 = cp.getY() - nB1.getY(); // line2.getY1();
    eB2 = cp.getY() - nB2.getY(); // line2.getY2();

    if ((Math.abs(bA) >= (Math.abs(dA1) + Math.abs(dA2)))
            && (Math.abs(aA) >= (Math.abs(eA1) + Math.abs(eA2)))) {

      if ((Math.abs(bB) >= (Math.abs(dB1) + Math.abs(dB2)))
              && (Math.abs(aB) >= (Math.abs(eB1) + Math.abs(eB2)))) {

        // System.out.println("Intersect at x:" + cp.getX() + ", y:" +
        // cp.getY());
        return cp;
      }
    }
    return null;
  }

  /**
   * @param nA1
   * @param nA2
   * @param nB1
   * @param nB2
   * @return line intersection point
   */
  @Deprecated
  public static ExtendedVector2d lineIntersectionOLD2(ExtendedVector2d nA1, ExtendedVector2d nA2,
          ExtendedVector2d nB1, ExtendedVector2d nB2) {
    if (Line2D.linesIntersect(nA1.getX(), nA1.getY(), nA2.getX(), nA2.getY(), nB1.getX(),
            nB1.getY(), nB2.getX(), nB2.getY())) {

      double aA, bB, aB, bA, denom;
      aA = nA2.getY() - nA1.getY();
      bA = nA1.getX() - nA2.getX();

      aB = nB2.getY() - nB1.getY();
      bB = nB1.getX() - nB2.getX();

      denom = aA * bB - aB * bA;

      if (denom == 0) { // lines are parallel
        System.out.println("parrellel lines");
        return null;
      }

      double cA, cB;

      cA = nA2.getX() * nA1.getY() - nA1.getX() * nA2.getY();
      cB = nB2.getX() * nB1.getY() - nB1.getX() * nB2.getY();

      ExtendedVector2d cp = null;
      // intersection point
      cp = new ExtendedVector2d((bA * cB - bB * cA) / denom, (aB * cA - aA * cB) / denom);

      // cp.print("Intersect at: ");

      System.out.println("plot([" + nA1.getX() + "," + nA2.getX() + "],[" + nA1.getY() + ","
              + nA2.getY() + "],'-ob');"); // matlab
                                           // output
      System.out.println("hold on; plot([" + nB1.getX() + "," + nB2.getX() + "],[" + nB1.getY()
              + "," + nB2.getY() + "],'-or');");
      System.out.println("plot(" + cp.x + "," + cp.y + ", 'og');");

      return cp;
    } else {
      return null;
    }
  }

  /**
   * Compute the intersection between two line segments, or two lines of infinite length.
   *
   * @param x0 X coordinate first end point first line segment.
   * @param y0 Y coordinate first end point first line segment.
   * @param x1 X coordinate second end point first line segment.
   * @param y1 Y coordinate second end point first line segment.
   * @param x2 X coordinate first end point second line segment.
   * @param y2 Y coordinate first end point second line segment.
   * @param x3 X coordinate second end point second line segment.
   * @param y3 Y coordinate second end point second line segment.
   * @param intersection Preallocated by caller to double[2]
   * @return -1 if lines are parallel (x,y unset), -2 if lines are parallel and overlapping (x, y
   *         center) 0 if intersection outside segments (x,y set) +1 if segments intersect (x,y
   *         set)
   * @see <a href= "link">http://geosoft.no/software/geometry/Geometry.java.html</a>
   */
  public static int segmentIntersection(double x0, double y0, double x1, double y1, double x2,
          double y2, double x3, double y3, double[] intersection) {

    final double LIMIT = 1e-5;
    final double INFINITY = 1e8;

    double x, y;

    //
    // Convert the lines to the form y = ax + b
    //

    // Slope of the two lines
    double a0 = equals(x0, x1, LIMIT) ? INFINITY : (y0 - y1) / (x0 - x1);
    double a1 = equals(x2, x3, LIMIT) ? INFINITY : (y2 - y3) / (x2 - x3);

    double b0 = y0 - a0 * x0; // y intersects intersects
    double b1 = y2 - a1 * x2;

    // Check if lines are parallel (within tolloerance)
    if (equals(a0, a1, LIMIT)) {
      if (!equals(b0, b1, LIMIT)) {
        return -1; // Parallel non-overlapping

      } else {
        if (equals(x0, x1, LIMIT)) {
          if (Math.min(y0, y1) < Math.max(y2, y3) || Math.max(y0, y1) > Math.min(y2, y3)) {
            double twoMiddle = y0 + y1 + y2 + y3 - min(y0, y1, y2, y3) - max(y0, y1, y2, y3);
            y = (twoMiddle) / 2.0;
            x = (y - b0) / a0;
          } else {
            return -1;
          } // Parallel non-overlapping
        } else {
          if (Math.min(x0, x1) < Math.max(x2, x3) || Math.max(x0, x1) > Math.min(x2, x3)) {
            double twoMiddle = x0 + x1 + x2 + x3 - min(x0, x1, x2, x3) - max(x0, x1, x2, x3);
            x = (twoMiddle) / 2.0;
            y = a0 * x + b0;
          } else {
            return -1;
          }
        }

        intersection[0] = x;
        intersection[1] = y;
        return -2;
      }
    }

    // Find correct intersection point
    if (equals(a0, INFINITY, LIMIT)) {
      x = x0;
      y = a1 * x + b1;
    } else if (equals(a1, INFINITY, LIMIT)) {
      x = x2;
      y = a0 * x + b0;
    } else {
      x = -(b0 - b1) / (a0 - a1);
      y = a0 * x + b0;
    }

    intersection[0] = x;
    intersection[1] = y;

    // Then check if intersection is within line segments
    double distanceFrom1;
    if (equals(x0, x1, LIMIT)) {
      if (y0 < y1) {
        distanceFrom1 = y < y0 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x0, y0))
                : y > y1 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x1, y1))
                        : 0.0;
      } else {
        distanceFrom1 = y < y1 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x1, y1))
                : y > y0 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x0, y0))
                        : 0.0;
      }
    } else {
      if (x0 < x1) {
        distanceFrom1 = x < x0 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x0, y0))
                : x > x1 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x1, y1))
                        : 0.0;
      } else {
        distanceFrom1 = x < x1 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x1, y1))
                : x > x0 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x0, y0))
                        : 0.0;
      }
    }

    double distanceFrom2;
    if (equals(x2, x3, LIMIT)) {

      if (y2 < y3) {
        distanceFrom2 = y < y2 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x2, y2))
                : y > y3 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x3, y3))
                        : 0.0;
      } else {
        distanceFrom2 = y < y3 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x3, y3))
                : y > y2 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x2, y2))
                        : 0.0;
      }
    } else {
      if (x2 < x3) {
        distanceFrom2 = x < x2 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x2, y2))
                : x > x3 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x3, y3))
                        : 0.0;
      } else {
        distanceFrom2 = x < x3 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x3, y3))
                : x > x2 ? lengthP2P(new ExtendedVector2d(x, y), new ExtendedVector2d(x2, y2))
                        : 0.0;
      }
    }

    return equals(distanceFrom1, 0.0, LIMIT) && equals(distanceFrom2, 0.0, LIMIT) ? 1 : 0;
  }

  /**
   * @param a
   * @param b
   * @param c
   * @return area of triangle (can be negative)
   */
  public static double triangleArea(ExtendedVector2d a, ExtendedVector2d b, ExtendedVector2d c) {
    // calc area of a triangle (can be negative)
    return (b.getX() - a.getX()) * (c.getY() - a.getY())
            - (c.getX() - a.getX()) * (b.getY() - a.getY());
  }

  /**
   * @param P
   * @param S0
   * @param S1
   * @return calculate the closest point on a segment to point P
   */
  public static ExtendedVector2d PointToSegment(ExtendedVector2d P, ExtendedVector2d S0,
          ExtendedVector2d S1) {
    ExtendedVector2d v = ExtendedVector2d.vecP2P(S0, S1);
    ExtendedVector2d w = ExtendedVector2d.vecP2P(S0, P);

    double c1 = dot(w, v);
    if (c1 <= 0) {
      return S0;
    }

    double c2 = dot(v, v);
    if (c2 <= c1) {
      return S1;
    }

    double b = c1 / c2;

    v.multiply(b);
    v.addVec(S0);

    return v;
  }

  /**
   * @param P
   * @param S0
   * @param S1
   * @return distance between closest point and segment
   */
  public static double distPointToSegment(ExtendedVector2d P, ExtendedVector2d S0,
          ExtendedVector2d S1) {
    ExtendedVector2d closest = ExtendedVector2d.PointToSegment(P, S0, S1);
    return ExtendedVector2d.lengthP2P(P, closest);
  }

  /**
   * @param a
   * @param b
   * @return scalar dot
   */
  public static double dot(ExtendedVector2d a, ExtendedVector2d b) {
    double d = a.getX() * b.getX();
    d += a.getY() * b.getY();
    return d;
  }

  /**
   * @param aa
   * @param bb
   * @return calc angle between 2 vectors
   */
  public static double angleNonRelative(ExtendedVector2d aa, ExtendedVector2d bb) {
    ExtendedVector2d a;
    ExtendedVector2d b;
    a = new ExtendedVector2d(aa.getX(), aa.getY()); // make a copy
    b = new ExtendedVector2d(bb.getX(), bb.getY());
    a.makeUnit();
    b.makeUnit();

    return Math.acos(dot(a, b));
  }

  /**
   * @param aa
   * @param bb
   * @return calc angle between 2 vectors
   */
  public static double angle(ExtendedVector2d aa, ExtendedVector2d bb) {
    ExtendedVector2d a;
    ExtendedVector2d b;
    a = new ExtendedVector2d(aa.getX(), aa.getY()); // make a copy
    b = new ExtendedVector2d(bb.getX(), bb.getY());
    a.makeUnit();
    b.makeUnit();

    return Math.atan2(b.getY(), b.getX()) - Math.atan2(a.getY(), a.getX());
  }

  /**
   * @param p
   * @param a
   * @param b
   * @return distance of point to line given as two points
   */
  public static double distPoinToInfLine(ExtendedVector2d p, ExtendedVector2d a,
          ExtendedVector2d b) {
    return (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
  }

  /**
   * Check if two double precision numbers are "equal", i.e. close enough to a given limit.
   *
   * @param a First number to check
   * @param b Second number to check
   * @param limit The definition of "equal".
   * @return True if the two numbers are "equal", false otherwise
   * @see <a href= "link">http://geosoft.no/software/geometry/Geometry.java.htmll</a>
   */
  private static boolean equals(double a, double b, double limit) {
    return Math.abs(a - b) < limit;
  }

  /**
   * Return smallest of four numbers.
   *
   * @param a First number to find smallest among.
   * @param b Second number to find smallest among.
   * @param c Third number to find smallest among.
   * @param d Fourth number to find smallest among.
   * @return Smallest of a, b, c and d.
   * @see <a href= "link">http://geosoft.no/software/geometry/Geometry.java.htmll</a>
   */
  private static double min(double a, double b, double c, double d) {
    return Math.min(Math.min(a, b), Math.min(c, d));
  }

  /**
   * Return largest of four numbers.
   *
   * @param a First number to find largest among.
   * @param b Second number to find largest among.
   * @param c Third number to find largest among.
   * @param d Fourth number to find largest among.
   * @return Largest of a, b, c and d.
   * @see <a href= "link">http://geosoft.no/software/geometry/Geometry.java.htmll</a>
   */
  private static double max(double a, double b, double c, double d) {
    return Math.max(Math.max(a, b), Math.max(c, d));
  }

}
