package com.github.celldynamics.quimp.geom;

import java.awt.geom.Line2D;

import org.scijava.vecmath.Tuple2d;
import org.scijava.vecmath.Tuple2f;
import org.scijava.vecmath.Vector2d;
import org.scijava.vecmath.Vector2f;

/**
 * Defines 2D vector and performs operations on vectors and lines.
 *
 * <p>This definition of vector is often used in QuimP for expressing points as well
 *
 * @author Richard
 */
public class ExtendedVector2d extends Vector2d {

  /**
   * Constructs and initializes a Vector2d from the specified array.
   * 
   * @param v initial array
   */
  public ExtendedVector2d(double[] v) {
    super(v);
  }

  /**
   * Constructs and initializes a Vector2d from the specified Tuple.
   * 
   * @param t1 initial Tuple
   */
  public ExtendedVector2d(Tuple2d t1) {
    super(t1);
  }

  /**
   * Constructs and initializes a Vector2d from the specified Tuple.
   * 
   * @param t1 initial Tuple
   */
  public ExtendedVector2d(Tuple2f t1) {
    super(t1);
  }

  /**
   * Constructs and initializes a Vector2d from the specified Vector.
   * 
   * @param v1 initial vestor
   */
  public ExtendedVector2d(Vector2d v1) {
    super(v1);
  }

  /**
   * Constructs and initializes a Vector2d from the specified Vector.
   * 
   * @param v1 initial vestor
   */
  public ExtendedVector2d(Vector2f v1) {
    super(v1);
  }

  /**
   * Constructs and initializes a Vector2d to (0,0).
   */
  public ExtendedVector2d() {
    super();
  }

  /**
   * Simple constructor.
   * 
   * <p>Creates vector mounted at (0,0).
   * 
   * @param xx \c x coordinate of vector
   * @param yy \c y coordinate of vector
   */
  public ExtendedVector2d(double xx, double yy) {
    super(xx, yy);
  }

  private static final long serialVersionUID = -7238793665995665600L;

  /**
   * Make versor from vector.
   */
  public void makeUnit() {

    double length = length();
    if (length != 0) {
      x = x / length;
      y = y / length;
    }
  }

  /**
   * Set vector coordinates.
   * 
   * @param nx x coordinate
   * @param ny y coordinate
   */
  public void setXY(double nx, double ny) {
    y = ny;
    x = nx;

  }

  /**
   * Add vector to this.
   * 
   * @param v vector
   */
  public void addVec(ExtendedVector2d v) {
    x += v.getX();
    y += v.getY();
  }

  /**
   * Multiply this vector.
   * 
   * @param d multiplier
   */
  public void multiply(double d) {
    x *= d;
    y *= d;
  }

  /**
   * Power this vector.
   * 
   * @param p power
   */
  public void power(double p) {
    x = Math.pow(x, p);
    y = Math.pow(y, p);

  }

  /**
   * Unit vector to target.
   * 
   * @param a a
   * @param b b
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
   * Create vector between points.
   * 
   * @param a start point
   * @param b end point
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
   * Get length of the vector between points.
   * 
   * @param a initial point
   * @param b end point
   * @return length of the vector
   */
  public static double lengthP2P(ExtendedVector2d a, ExtendedVector2d b) {
    ExtendedVector2d v = vecP2P(a, b);
    return v.length();
  }

  /**
   * Calculate the intersect between two edges A and B. Edge is defined as vector AB,
   * where A and B stand for initial and terminal points given as vectors mounted at (0,0).
   * 
   * @param na1 initial point of A edge
   * @param na2 terminal point of A edge
   * @param nb1 initial point of B edge
   * @param nb2 terminal point of B edge
   * @return Intersect point
   * @deprecated Actually not used in this version of QuimP
   */
  public static ExtendedVector2d lineIntersectionOLD(ExtendedVector2d na1, ExtendedVector2d na2,
          ExtendedVector2d nb1, ExtendedVector2d nb2) {
    double aa;
    double bb;
    double ab;
    double ba;
    double denom;
    aa = na2.getY() - na1.getY();
    ba = na1.getX() - na2.getX();

    ab = nb2.getY() - nb1.getY();
    bb = nb1.getX() - nb2.getX();

    denom = aa * bb - ab * ba;

    if (denom == 0) { // lines are parallel
      // System.out.println("parrellel lines");
      return null;
    }

    double ca;
    double cb;

    ca = na2.getX() * na1.getY() - na1.getX() * na2.getY();
    cb = nb2.getX() * nb1.getY() - nb1.getX() * nb2.getY();

    ExtendedVector2d cp = null;
    double da2;
    double da1;
    double ea1;
    double ea2;

    // intersection point
    cp = new ExtendedVector2d((ba * cb - bb * ca) / denom, (ab * ca - aa * cb) / denom);

    // System.out.println("Pos Intersect at x:" + cp.getX() + ", y:" +
    // cp.getY());

    // check in bounds of line1
    da1 = cp.getX() - na1.getX(); // line1.getX1();
    da2 = cp.getX() - na2.getX(); // line1.getX2();
    ea1 = cp.getY() - na1.getY(); // line1.getY1();
    ea2 = cp.getY() - na2.getY(); // line1.getY2();

    double db2;
    double db1;
    double eb1;
    double eb2;
    db1 = cp.getX() - nb1.getX(); // line2.getX1();
    db2 = cp.getX() - nb2.getX(); // line2.getX2();
    eb1 = cp.getY() - nb1.getY(); // line2.getY1();
    eb2 = cp.getY() - nb2.getY(); // line2.getY2();

    if ((Math.abs(ba) >= (Math.abs(da1) + Math.abs(da2)))
            && (Math.abs(aa) >= (Math.abs(ea1) + Math.abs(ea2)))) {

      if ((Math.abs(bb) >= (Math.abs(db1) + Math.abs(db2)))
              && (Math.abs(ab) >= (Math.abs(eb1) + Math.abs(eb2)))) {

        // System.out.println("Intersect at x:" + cp.getX() + ", y:" +
        // cp.getY());
        return cp;
      }
    }
    return null;
  }

  /**
   * lineIntersectionOLD2.
   * 
   * @param na1 initial point of A edge
   * @param na2 terminal point of A edge
   * @param nb1 initial point of B edge
   * @param nb2 terminal point of B edge
   * @return line intersection point
   * @deprecated Actually not used in this version of QuimP
   */
  public static ExtendedVector2d lineIntersectionOLD2(ExtendedVector2d na1, ExtendedVector2d na2,
          ExtendedVector2d nb1, ExtendedVector2d nb2) {
    if (Line2D.linesIntersect(na1.getX(), na1.getY(), na2.getX(), na2.getY(), nb1.getX(),
            nb1.getY(), nb2.getX(), nb2.getY())) {

      double aa;
      double bb;
      double ab;
      double ba;
      double denom;
      aa = na2.getY() - na1.getY();
      ba = na1.getX() - na2.getX();

      ab = nb2.getY() - nb1.getY();
      bb = nb1.getX() - nb2.getX();

      denom = aa * bb - ab * ba;

      if (denom == 0) { // lines are parallel
        System.out.println("parrellel lines");
        return null;
      }

      double ca;
      double cb;

      ca = na2.getX() * na1.getY() - na1.getX() * na2.getY();
      cb = nb2.getX() * nb1.getY() - nb1.getX() * nb2.getY();

      ExtendedVector2d cp = null;
      // intersection point
      cp = new ExtendedVector2d((ba * cb - bb * ca) / denom, (ab * ca - aa * cb) / denom);

      // cp.print("Intersect at: ");

      System.out.println("plot([" + na1.getX() + "," + na2.getX() + "],[" + na1.getY() + ","
              + na2.getY() + "],'-ob');"); // matlab output
      System.out.println("hold on; plot([" + nb1.getX() + "," + nb2.getX() + "],[" + nb1.getY()
              + "," + nb2.getY() + "],'-or');");
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

    final double limit = 1e-5;
    final double infinity = 1e8;

    double x;
    double y;

    //
    // Convert the lines to the form y = ax + b
    //

    // Slope of the two lines
    double a0 = equals(x0, x1, limit) ? infinity : (y0 - y1) / (x0 - x1);
    double a1 = equals(x2, x3, limit) ? infinity : (y2 - y3) / (x2 - x3);

    double b0 = y0 - a0 * x0; // y intersects intersects
    double b1 = y2 - a1 * x2;

    // Check if lines are parallel (within tolloerance)
    if (equals(a0, a1, limit)) {
      if (!equals(b0, b1, limit)) {
        return -1; // Parallel non-overlapping

      } else {
        if (equals(x0, x1, limit)) {
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
    if (equals(a0, infinity, limit)) {
      x = x0;
      y = a1 * x + b1;
    } else if (equals(a1, infinity, limit)) {
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
    if (equals(x0, x1, limit)) {
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
    if (equals(x2, x3, limit)) {

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

    return equals(distanceFrom1, 0.0, limit) && equals(distanceFrom2, 0.0, limit) ? 1 : 0;
  }

  /**
   * Compute area of triangle (can be negative).
   * 
   * @param a a point
   * @param b b point
   * @param c c point
   * @return area of triangle (can be negative)
   */
  public static double triangleArea(ExtendedVector2d a, ExtendedVector2d b, ExtendedVector2d c) {
    // calc area of a triangle (can be negative)
    return (b.getX() - a.getX()) * (c.getY() - a.getY())
            - (c.getX() - a.getX()) * (b.getY() - a.getY());
  }

  /**
   * Calculate the closest point on a segment to point P.
   * 
   * @param p point to test
   * @param s0 first point of segment
   * @param s1 last point of segment
   * @return closest point on a segment to point P
   */
  public static ExtendedVector2d pointToSegment(ExtendedVector2d p, ExtendedVector2d s0,
          ExtendedVector2d s1) {
    ExtendedVector2d v = ExtendedVector2d.vecP2P(s0, s1);
    ExtendedVector2d w = ExtendedVector2d.vecP2P(s0, p);

    double c1 = dot(w, v);
    if (c1 <= 0) {
      return s0;
    }

    double c2 = dot(v, v);
    if (c2 <= c1) {
      return s1;
    }

    double b = c1 / c2;

    v.multiply(b);
    v.addVec(s0);

    return v;
  }

  /**
   * Compute distance between closest point and segment.
   * 
   * @param p point to test
   * @param s0 first point of segment
   * @param s1 last point of segment
   * @return distance between closest point and segment
   */
  public static double distPointToSegment(ExtendedVector2d p, ExtendedVector2d s0,
          ExtendedVector2d s1) {
    ExtendedVector2d closest = ExtendedVector2d.pointToSegment(p, s0, s1);
    return ExtendedVector2d.lengthP2P(p, closest);
  }

  /**
   * Compute scalar dot.
   * 
   * @param a left operand
   * @param b right operand
   * @return scalar dot
   */
  public static double dot(ExtendedVector2d a, ExtendedVector2d b) {
    double d = a.getX() * b.getX();
    d += a.getY() * b.getY();
    return d;
  }

  /**
   * Calculate non relative angle between 2 vectors.
   * 
   * @param aa vector
   * @param bb vector
   * @return angle between 2 vectors (non relative)
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
   * Calculate angle between 2 vectors.
   * 
   * @param aa vector
   * @param bb vector
   * @return angle between 2 vectors
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
   * Calculate distance of point to line given as two points.
   * 
   * @param p point to test
   * @param a line point
   * @param b line point
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
