package uk.ac.warwick.wsbc.quimp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

// TODO: Auto-generated Javadoc
/**
 * Represents node of bidirectional list of points in Cartesian coordinates.
 * 
 * <p>This abstract class contains basic properties of points and provides method for moving across
 * the
 * list. Points in list are numbered from <b>1</b> and list can be looped. There is one special node
 * called <b>head</b> that indicates beginning of the list (and its end if the list is looped)
 * 
 * @author p.baniukiewicz
 *
 * @param <T> Type of point, currently can be Node or Vert
 */
public abstract class PointsList<T extends PointsList<T>> {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(PointsList.class.getName());

  /**
   * The prev.
   */
  protected transient T prev; /*!< previous point in list, \c null if no other point */

  /**
   * The next.
   */
  protected transient T next; /*!< next point in list, \c null if no other point */
  /**
   * x,y co-ordinates of the point.
   */
  protected ExtendedVector2d point;
  /**
   * Normal vector. Calculated by
   * {@link uk.ac.warwick.wsbc.quimp.PointsList#updateNormale(boolean)} and implicitly by
   * {@link uk.ac.warwick.wsbc.quimp.Shape#updateNormales(boolean)} from Shape during
   * serialization and deserialization and changing the shape of Shape
   */
  protected ExtendedVector2d normal;
  /**
   * tangent vector. Calculated by uk.ac.warwick.wsbc.quimp.PointsList.calcTan(). Implicitly
   * during calculating normals (see \c normal)
   */
  protected ExtendedVector2d tan;

  /**
   * The head.
   */
  protected boolean head = false; /*!< Indicate if this point is \b head */

  /**
   * The clockwise.
   */
  protected static boolean clockwise = true; /*!< access clockwise if true */
  /**
   * ID number of point, unique across list. Given during adding point to list, controlled by
   * Shape
   */
  protected int tracknumber = 1;
  /**
   * normalized position on list.
   * 
   * <p>0 - beginning , 1 - end of the list according to Shape perimeter. Set by
   * uk.ac.warwick.wsbc.quimp.Shape.setPositions() and called before and after serialise and on
   * Shape writing.
   */
  double position = -1;
  /**
   * flag which is set when the velocity is below the critical velocity.
   */
  public boolean frozen = false;

  /**
   * Default constructor, assumes that first point is created on list with ID = 1.
   */
  public PointsList() {
    point = new ExtendedVector2d();
    normal = new ExtendedVector2d();
    tan = new ExtendedVector2d();
  }

  /**
   * Create point with given ID. New point is not linked to any other yet.
   * 
   * <p>Caller should care about correct numbering of points
   * 
   * @param t ID of point
   */
  public PointsList(int t) {
    this();
    setTrackNum(t);
  }

  /**
   * Copy constructor. Make copy of properties of passed point.
   * 
   * <p>Previous or next points are not copied
   * 
   * @param src Source Point
   */
  public PointsList(final PointsList<?> src) {
    this.point = new ExtendedVector2d(src.point);
    this.normal = new ExtendedVector2d(src.normal);
    this.tan = new ExtendedVector2d(src.tan);
    this.head = src.head;
    this.tracknumber = src.tracknumber;
    this.position = src.position;
  }

  /**
   * Creates point with given ID and coordinates. New point is not linked to any other yet.
   * 
   * <p>Caller should care about correct numbering of points
   * 
   * @param xx x coordinate of point
   * @param yy y coordinate of point
   * @param t ID of point
   */
  public PointsList(double xx, double yy, int t) {
    this(t);
    point = new ExtendedVector2d(xx, yy);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (frozen ? 1231 : 1237);
    result = prime * result + (head ? 1231 : 1237);
    result = prime * result + ((normal == null) ? 0 : normal.hashCode());
    result = prime * result + ((point == null) ? 0 : point.hashCode());
    long temp;
    temp = Double.doubleToLongBits(position);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((tan == null) ? 0 : tan.hashCode());
    result = prime * result + tracknumber;
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PointsList)) {
      return false;
    }
    PointsList<?> other = (PointsList<?>) obj;
    if (frozen != other.frozen) {
      return false;
    }
    if (head != other.head) {
      return false;
    }
    if (normal == null) {
      if (other.normal != null) {
        return false;
      }
    } else if (!normal.equals(other.normal)) {
      return false;
    }
    if (point == null) {
      if (other.point != null) {
        return false;
      }
    } else if (!point.equals(other.point)) {
      return false;
    }
    if (Double.doubleToLongBits(position) != Double.doubleToLongBits(other.position)) {
      return false;
    }
    if (tan == null) {
      if (other.tan != null) {
        return false;
      }
    } else if (!tan.equals(other.tan)) {
      return false;
    }
    if (tracknumber != other.tracknumber) {
      return false;
    }
    return true;
  }

  /**
   * point getter.
   * 
   * @return X space co-ordinate
   */
  public double getX() {
    return point.getX();
  }

  /**
   * point getter.
   * 
   * @return Y space co-ordinate
   */
  public double getY() {
    return point.getY();
  }

  /**
   * Set X space co-ordinate.
   * 
   * @param x coordinate
   */
  public void setX(double x) {
    point.setX(x);
  }

  /**
   * Set Y space co-ordinate.
   * 
   * @param y coordinate
   */
  public void setY(double y) {
    point.setY(y);
  }

  /**
   * Sets the clockwise.
   *
   * @param b the new clockwise
   */
  public static void setClockwise(boolean b) {
    PointsList.clockwise = b;
  }

  /**
   * Gets the point.
   *
   * @return the point
   */
  public ExtendedVector2d getPoint() {
    return point;
  }

  /**
   * Gets the track num.
   *
   * @return the track num
   */
  public int getTrackNum() {
    return tracknumber;
  }

  /**
   * Gets the normal.
   *
   * @return the normal
   */
  public ExtendedVector2d getNormal() {
    return normal;
  }

  /**
   * Gets the tangent.
   *
   * @return the tangent
   */
  public ExtendedVector2d getTangent() {
    return tan;
  }

  /**
   * Checks if is head.
   *
   * @return true, if is head
   */
  public boolean isHead() {
    return head;
  }

  /**
   * Sets the normal.
   *
   * @param x the x
   * @param y the y
   */
  public void setNormal(double x, double y) {
    normal.setX(x);
    normal.setY(y);
  }

  /**
   * Sets the track num.
   *
   * @param b the new track num
   */
  public void setTrackNum(int b) {
    tracknumber = b;
  }

  /**
   * Set head marker to current node.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Only one Node in Snake can be head
   * 
   * @param t true if current node is head, false otherwise
   * @see uk.ac.warwick.wsbc.quimp.Snake#setNewHead(int)
   * @see uk.ac.warwick.wsbc.quimp.Snake
   */
  public void setHead(boolean t) {
    head = t;
  }

  /**
   * Get previous node in chain (next if not clockwise).
   * 
   * @return next or previous Node from list
   */
  public T getPrev() {
    if (clockwise) {
      return prev;
    } else {
      return next;
    }
  }

  /**
   * Get next node in chain (previous if not clockwise).
   * 
   * @return previous or next Node from list
   */
  public T getNext() {
    if (clockwise) {
      return next;
    } else {
      return prev;
    }
  }

  /**
   * Adds previous (or next if not clockwise) Node to list.
   * 
   * @param n Node to add
   */
  public void setPrev(T n) {
    if (clockwise) {
      prev = n;
    } else {
      next = n;
    }
  }

  /**
   * Adds next (or previous if not clockwise) Node to list.
   * 
   * @param n Node to add
   */
  public void setNext(T n) {
    if (clockwise) {
      next = n;
    } else {
      prev = n;
    }
  }

  /**
   * Updates the normal (must point inwards).
   * 
   * @param inner inner
   */
  public void updateNormale(boolean inner) {
    boolean c = clockwise;
    clockwise = true; // just in case
    tan = calcTan(); // tangent

    if (!inner) { // switch around if expanding snake
      normal.setX(-tan.getY());
      normal.setY(tan.getX());
    } else {
      normal.setX(tan.getY());
      normal.setY(-tan.getX());
    }
    clockwise = c;

  }

  /**
   * Calculate tangent at current point (i.e. unit vector between neighbours).
   *
   * <p>Calculate a unit vector towards neighbouring nodes and then a unit vector between their
   * ends.
   * direction important for normale calculation. Always calculate tan as if clockwise.
   *
   * @return Tangent at point
   */
  private ExtendedVector2d calcTan() {

    ExtendedVector2d unitVecLeft = ExtendedVector2d.unitVector(point, prev.getPoint());
    ExtendedVector2d pointLeft = new ExtendedVector2d();
    pointLeft.setX(getX());
    pointLeft.setY(getY());
    pointLeft.addVec(unitVecLeft);

    ExtendedVector2d unitVecRight = ExtendedVector2d.unitVector(point, next.getPoint());
    ExtendedVector2d pointRight = new ExtendedVector2d();
    pointRight.setX(getX());
    pointRight.setY(getY());
    pointRight.addVec(unitVecRight);

    return ExtendedVector2d.unitVector(pointLeft, pointRight);
  }

  /**
   * Set direction of <tt>this</tt> list.
   */
  public static void randDirection() {
    if (Math.random() < 0.5) {
      clockwise = true;
    } else {
      clockwise = false;
    }
  }

  /**
   * Current Point as String.
   * 
   * @return String representation of Node
   */
  public String toString() {
    String str;
    // str = "[" + this.getX() + "," + this.getY() + "] " + "head is " + head + " next:"
    // + getNext() + " prev: " + getPrev();
    str = "[" + this.getX() + "," + this.getY() + "] " + "tracknumber " + tracknumber;
    return str;
  }

  /**
   * Freeze Point.
   */
  public void freeze() {
    frozen = true;
  }

  /**
   * Unfreeze Point.
   */
  public void unfreeze() {
    frozen = false;
  }

  /**
   * Evaluate local curvature of T related to previous, this and next T.
   * 
   * @return Local curvature for this node in degrees
   */
  public double getCurvatureLocal() {
    ExtendedVector2d edge1 = ExtendedVector2d.vecP2P(this.getPoint(), this.getPrev().getPoint());
    ExtendedVector2d edge2 = ExtendedVector2d.vecP2P(this.getPoint(), this.getNext().getPoint());

    double angle = ExtendedVector2d.angle(edge1, edge2) * (180 / Math.PI);

    if (angle > 360 || angle < -360) {
      LOGGER.warn("Warning-angle out of range (Vert l:320)");
    }

    if (angle < 0) {
      angle = 360 + angle;
    }

    double curvatureLocal = 0;
    if (angle == 180) {
      curvatureLocal = 0;
    } else if (angle < 180) {
      curvatureLocal = -1 * (1 - (angle / 180));
    } else {
      curvatureLocal = (angle - 180) / 180;
    }
    return curvatureLocal;
  }

}
