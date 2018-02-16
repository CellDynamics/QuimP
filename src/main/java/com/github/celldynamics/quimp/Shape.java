package com.github.celldynamics.quimp;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Tuple2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Form abstract shape from bidirectional list of points.
 * 
 * <p>This abstract class keeps head point of Shape and control number of points in Shape, allows
 * for inserting points to the Shape. Generally assumes that Shape is closed, so PointsList is
 * looped
 * 
 * @author p.baniukiewicz
 *
 * @param <T> Type of point, currently can be Node or Vert
 */
public abstract class Shape<T extends PointsList<T>> implements IQuimpSerialize, Iterable<T> {

  /**
   * Threshold value for choosing new head as next or previous element on list if old head is
   * deleted.
   * 
   * <p>This should be used for determining the new head node only in tests, accessed by reflection.
   * 
   * <pre>
   * <code>
   * Field f = Shape.class.getDeclaredField("threshold");
   * f.setAccessible(true);
   * f.setDouble(Shape.class, 1.0);
   * </code>
   * </pre>
   * 
   * @see #removePoint(PointsList, boolean)
   */
  private static double threshold = 0.5;
  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(Shape.class.getName());
  /**
   * Next node ID's. Initialised in constructor, changed during modification of shape.
   */
  protected int nextTrackNumber = 1;
  /**
   * first node in double linked list, always maintained, initialised in constructor.
   */
  protected T head;
  /**
   * number of points. Initialised in constructor, changed on Shape modification.
   */
  protected int POINTS;
  /**
   * Centroid point of the Shape. Updated by {@link #calcCentroid()} called. after change of the
   * Shape and also in {@link #afterSerialize()} and {@link #beforeSerialize()}
   */
  protected ExtendedVector2d centroid = null;

  /**
   * Max number of nodes allowed in Shape.
   */
  public static final int MAX_NODES = 10000;
  /**
   * List is correct: has head, looped.
   * 
   * @see #validateShape()
   */
  public static final int LIST_OK = 0;
  /**
   * List contains node marked as head but without proper flag or null.
   * 
   * @see #validateShape()
   */
  public static final int BAD_HEAD = 1;
  /**
   * No head found among first {@value #MAX_NODES}.
   * {@link #validateShape()}
   */
  public static final int NO_HEAD = 2;
  /**
   * List is not closed. At least one nod has not previous or next neighbour.
   * {@link #validateShape()}
   */
  public static final int BAD_LINKING = 4;
  /**
   * Number of list elements does match to the number stored in {@link #POINTS}.
   * 
   * <p>This can happen when {@link #Shape(PointsList, int)} is used. See {@link #validateShape()}.
   */
  public static final int BAD_NUM_POINTS = 8;
  /**
   * Elements of Shape as List. Initialised on Serialise. Temporary array to store linked list as
   * array to allow serialisation
   */
  private ArrayList<T> Elements = null;

  /**
   * Default constructor, creates empty Shape.
   */
  public Shape() {
    POINTS = 0;
    head = null;
  }

  /**
   * Create Shape from existing list of points (can be one point as well).
   * 
   * <p>List of points must be looped.
   * 
   * @param h head point of the list
   * @param n number of points in the list
   * @see #validateShape()
   */
  public Shape(T h, int n) {
    head = h;
    T he = checkIsHead(); // can override head
    if (he == null) {
      LOGGER.info("No head in list. Selecting " + h + "as head");
      h.setHead(true);
      head = h;
    } else {
      if (he != h) {
        LOGGER.info("Head at different position than given initial element. Selecting " + he
                + "as head");
      }
      head = he;
    }

    POINTS = n;
    nextTrackNumber = n + 1;
    calcCentroid();
    setPositions();
  }

  /**
   * Create Shape from one point, created Shape will be looped. If <tt>h</tt> is a list, only
   * <tt>h</tt> will be maintained and list will be unlinked.
   * 
   * @param h head point of the list
   */
  public Shape(final T h) {
    head = h;
    head.setHead(true);
    head.setNext(head);
    head.setPrev(head);
    nextTrackNumber = head.getTrackNum() + 1;
    POINTS = 1;
    calcCentroid();
    setPositions();
  }

  /**
   * Copy constructor. Calculates centroid as well and positions.
   * 
   * @param src source Shape to copy from
   * @throws RuntimeException when T does no have copy constructor
   */
  public Shape(final Shape<T> src) {
    this(src, src.getHead());
  }

  /**
   * Conversion and copy constructor.
   * 
   * <p>Converts between different types of PointsList. <tt>src</tt> is source Shape of type T to
   * convert to other Shape based on <tt>PointsList</tt> other type (but in general extended from
   * PointsList) Typical approach is to convert Snake to Outline ({@link Node} to
   * {@link Vert}).
   * 
   * <p>Can be used as copy constructor.
   * 
   * @param src input Shape to convert.
   * @param destType object of base node that PointsList is composed from
   * @throws RuntimeException when T does no have copy constructor
   * 
   */
  @SuppressWarnings("unchecked")
  public Shape(final Shape<T> src, T destType) {
    T tmpHead = src.getHead(); // get head as representative object
    Class<?> templateClass = tmpHead.getClass(); // get class under Shape (T)
    LOGGER.trace("Src class: " + templateClass.getName());
    try {
      // Constructor of T as type can not be called directly, use reflection
      // get Constructor of T with one parameter of Type src (conversion constructor)
      Constructor<?> ctor = destType.getClass().getDeclaredConstructor(templateClass);
      // create copy of head
      head = (T) ctor.newInstance(src.getHead());
      T srcn = src.getHead();
      T n = head;
      // iterate over whole list making copies of T elements
      for (srcn = srcn.getNext(); !srcn.isHead(); srcn = srcn.getNext()) {
        T next = (T) ctor.newInstance(srcn);
        n.setNext(next);
        next.setPrev(n);
        n = next;
      }
      // loop list
      n.setNext(head);
      head.setPrev(n);
    } catch (SecurityException | NoSuchMethodException | InstantiationException
            | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e); // change to unchecked exception
    }
    // copy rest of params
    POINTS = src.POINTS;
    nextTrackNumber = src.nextTrackNumber;
    centroid = new ExtendedVector2d(src.centroid);
  }

  /**
   * Create Shape of elements <tt>elInst</tt> with coordinates <tt>p</tt>.
   * 
   * @param p list of coordinates
   * @param elInst instance of requested element type
   * @param inner direction of normales. For Outlines set to true, for snakes to
   *        BOA_.qState.segParam.expandSnake
   */
  @SuppressWarnings("unchecked")
  public Shape(final List<? extends Tuple2d> p, T elInst, boolean inner) {
    try {
      Constructor<?> ctor = elInst.getClass().getDeclaredConstructor(int.class);
      Constructor<?> ctorPoint =
              elInst.getClass().getDeclaredConstructor(double.class, double.class, int.class);
      head = (T) ctor.newInstance(0);
      POINTS = 1;
      head.setPrev(head);
      head.setNext(head);
      head.setHead(true);
      T node;
      for (Tuple2d el : p) {
        node = (T) ctorPoint.newInstance(el.getX(), el.getY(), nextTrackNumber++);
        addPoint(node);
      }
      removePoint(head, inner);
      setHeadClosestTo(new ExtendedVector2d(p.get(0))); // first point from list is head

    } catch (SecurityException | NoSuchMethodException | InstantiationException
            | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e); // change to unchecked exception
    }
    updateNormals(inner);
    calcCentroid();
    setPositions();
  }

  /**
   * Create Shape of elements <tt>elInst</tt> with coordinates <tt>x</tt> and <tt>y</tt>.
   * 
   * @param x coordinates
   * @param y coordinates
   * @param elInst instance of requested element type
   * @param inner direction of normals. For Outlines set to true, for snakes to
   *        BOA_.qState.segParam.expandSnake
   */
  @SuppressWarnings("unchecked")
  public Shape(final double[] x, final double[] y, T elInst, boolean inner) {
    try {
      Constructor<?> ctor = elInst.getClass().getDeclaredConstructor(int.class);
      Constructor<?> ctorPoint =
              elInst.getClass().getDeclaredConstructor(double.class, double.class, int.class);
      head = (T) ctor.newInstance(0);
      POINTS = 1;
      head.setPrev(head);
      head.setNext(head);
      head.setHead(true);
      T node;
      for (int i = 0; i < x.length; i++) {
        node = (T) ctorPoint.newInstance(x[i], y[i], nextTrackNumber++);
        addPoint(node);
      }
      removePoint(head, inner);
      setHeadClosestTo(new ExtendedVector2d(x[0], y[0])); // first point from list is head

    } catch (SecurityException | NoSuchMethodException | InstantiationException
            | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e); // change to unchecked exception
    }
    updateNormals(inner);
    calcCentroid();
    setPositions();
  }

  /**
   * Print Shape nodes.
   * 
   * @return String representation of Shape
   */
  @Override
  public String toString() {
    T v = this.head;
    String out = "Coords: ";
    do {
      out = out.concat(" {" + v.getX() + "," + v.getY() + "}");
      v = v.getNext();
    } while (!v.isHead());
    return out;
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
    result = prime * result + POINTS;
    result = prime * result + ((centroid == null) ? 0 : centroid.hashCode());
    if (head == null) {
      result = prime * result + 0;
    } else { // go through the whole list
      T n = head;
      do {
        result = prime * result + n.hashCode();
        n = n.getNext();
      } while (!n.isHead());
    }
    result = prime * result + nextTrackNumber;
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
    if (!(obj instanceof Shape)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    Shape<T> other = (Shape<T>) obj;
    if (POINTS != other.POINTS) {
      return false;
    }
    if (centroid == null) {
      if (other.centroid != null) {
        return false;
      }
    } else if (!centroid.equals(other.centroid)) {
      return false;
    }
    if (head == null) {
      if (other.head != null) {
        return false;
      }
    } else { // iterate over list of nodes compare all
      T n = head;
      T nobj = other.getHead();
      boolean status = true;
      do {
        status &= n.equals(nobj);
        n = n.getNext();
        nobj = nobj.getNext();
      } while (!n.isHead());
      if (!status) {
        return false;
      }
    }
    if (nextTrackNumber != other.nextTrackNumber) {
      return false;
    }
    return true;
  }

  /**
   * Getter for <tt>centroid</tt>.
   * 
   * @return centroid
   */
  public ExtendedVector2d getCentroid() {
    if (centroid == null) {
      calcCentroid();
    }
    return centroid;
  }

  /**
   * Calculate centroid of Shape.
   * 
   * <p>This method modifies internal field centroid.
   */
  public void calcCentroid() {
    if (POINTS == 0) {
      centroid = null;
      return;
    }
    if (POINTS == 1) {
      centroid = new ExtendedVector2d(head.getX(), head.getY());
      return;
    }
    centroid = new ExtendedVector2d(0, 0);
    T v = head;
    double x;
    double y;
    double g;
    do {
      g = (v.getX() * v.getNext().getY()) - (v.getNext().getX() * v.getY());
      x = (v.getX() + v.getNext().getX()) * g;
      y = (v.getY() + v.getNext().getY()) * g;
      centroid.setX(centroid.getX() + x);
      centroid.setY(centroid.getY() + y);
      v = v.getNext();
    } while (!v.isHead());

    double area = this.calcArea();
    if (area != 0) {
      centroid.multiply(1d / (6 * this.calcArea()));
    } else {
      centroid.setX(0);
      centroid.setY(0);
    }
  }

  /**
   * Get bounds of Shape.
   * 
   * @return Bounding box of current Snake object as Double
   */
  public Rectangle2D.Double getDoubleBounds() {
    double minX;
    double minY;
    double maxX;
    double maxY;
    T n = getHead();
    minX = n.getX();
    maxX = n.getX();
    minY = n.getY();
    maxY = n.getY();
    n = n.getNext();
    do {
      if (n.getX() > maxX) {
        maxX = n.getX();
      }
      if (n.getX() < minX) {
        minX = n.getX();
      }
      if (n.getY() > maxY) {
        maxY = n.getY();
      }
      if (n.getY() < minY) {
        minY = n.getY();
      }
      n = n.getNext();
    } while (!n.isHead());
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  /**
   * Calculate area of the Shape.
   * 
   * @return Area
   */
  private double calcArea() {
    double area;
    double sum;
    sum = 0.0;
    T n = head;
    T np1 = n.getNext();
    do {
      sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
      n = n.getNext();
      np1 = n.getNext(); // note: n is reset on prev line

    } while (!n.isHead());
    area = 0.5 * sum;
    return area;
  }

  /**
   * Add up lengths between all verts.
   * 
   * @return length of Shape
   */
  public double getLength() {
    T v = head;
    double length = 0.0;
    do {
      length += ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      v = v.getNext();
    } while (!v.isHead());
    return length;
  }

  /**
   * Calculate position of Shape element expressed as distance of element on Shape perimeter from
   * <b>head</b>.
   * 
   * <p>First element has position 0, last 1. Element at position 0.5 is in half length of perimeter
   */
  public void setPositions() {
    double length = getLength();
    double d = 0.;

    T v = head;
    do {
      v.position = d / length;
      d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Update all node normals. Called after modification of Shape nodes.
   * 
   * @param inner Direction of normals. If <tt>false</tt> they are set outwards the shape.
   */
  public void updateNormals(boolean inner) {
    T v = head;
    do {
      v.updateNormale(inner);
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Get head of current Shape.
   * 
   * @return Point representing head of Shape
   */
  public T getHead() {
    return head;
  }

  /**
   * Set head of the shape to given element of the list.
   * 
   * <p>Element must be referenced on list.
   * 
   * @param newHead reference of new head.
   */
  public void setHead(T newHead) {
    T oldHead = getHead();
    T tmp;
    T v = oldHead;
    boolean status = false;
    if (oldHead == newHead) {
      return;
    }
    do {
      tmp = v.getNext();
      if (tmp == newHead) {
        tmp.setHead(true);
        head = tmp;
        oldHead.setHead(false);
        status = true;
        break;
      }
      v = v.getNext();
    } while (!v.isHead());

    if (!status) {
      throw new IllegalArgumentException("Given element has not been found on list");
    }
  }

  /**
   * Assign head to node nodeIndex. Do not change head if nodeIndex is not found or there is no head
   * in list.
   * 
   * @param nodeIndex Index of node of new head
   */
  public void setHead(int nodeIndex) {
    if (checkIsHead() == null) {
      return;
    }
    T n = head;
    T oldhead = n;
    do {
      n = n.getNext();
    } while (n.getTrackNum() != nodeIndex && !n.isHead());
    n.setHead(true);
    if (oldhead != n) {
      oldhead.setHead(false);
    }
    head = n;
    LOGGER.debug("New head is: " + getHead().toString());
  }

  /**
   * Set head to element closest to given coordinates.
   * 
   * @param phead point to set head closest
   */
  public void setHeadClosestTo(ExtendedVector2d phead) {
    double dis;
    double curDis;
    T v = head;
    T closestV = head;
    curDis = ExtendedVector2d.lengthP2P(phead, v.getPoint());

    do {
      dis = ExtendedVector2d.lengthP2P(phead, v.getPoint());
      if (dis < curDis) {
        curDis = dis;
        closestV = v;
      }
      v = v.getNext();
    } while (!v.isHead());

    if (closestV.isHead()) {
      return;
    }

    head.setHead(false);
    closestV.setHead(true);
    head = closestV;
  }

  /**
   * Check if there is a head node.
   * 
   * <p>Traverse along first {@value #MAX_NODES} elements and check if any of them is head.
   * 
   * @return found head or null if not found
   */
  public T checkIsHead() {
    if (head == null) {
      return null;
    }
    T n = head;
    int count = 0;
    do {
      if (count++ > MAX_NODES) {
        LOGGER.warn("Head lost!!!!");
        return null; // list looped but no head node
      }
      T p = n.getPrev(); // prev to current
      n = n.getNext(); // next to current
      if (n == null || p == null) { // each current must have next and previous
        throw new IllegalArgumentException("List is not looped");
      }
    } while (!n.isHead());
    return n;
  }

  /**
   * Validate correctness of the Shape.
   * 
   * <p>Check if Shape has head, it is correct and all nodes have previous and next neighbour.
   * 
   * @return {@value #LIST_OK} or combination of {@value #NO_HEAD}, {@value #BAD_LINKING},
   *         {@value #BAD_HEAD}, {@value #BAD_NUM_POINTS}
   */
  public int validateShape() {
    int ret = LIST_OK;
    if (head == null || head.isHead() == false) {
      ret = ret | BAD_HEAD; // bad head
    }
    try {
      if (checkIsHead() == null) {
        ret = ret | NO_HEAD; // no head
      }
    } catch (IllegalArgumentException e) {
      ret = ret | BAD_LINKING; // bad linking
    }
    if (ret == 0) { // check number of points that can be different if Shape.Shape(T, int) was used
      if (countPoints() != POINTS) {
        ret = ret | BAD_NUM_POINTS;
      }
    }
    return ret;
  }

  /**
   * Add node before head node assuring that list is a closed loop.
   * 
   * <p>If initial list condition is defined in such way:
   * 
   * <pre>
   * {@code
   * head = new Node(0); //make a dummy head node NODES = 1; FROZEN = 0;
   * head.setPrev(head); // link head to itself head.setNext(head);
   * head.setHead(true);
   * }
   * </pre>
   * 
   * <p>The <tt>addPoint</tt> will produce closed bidirectional linked list. From first Node it is
   * possible to reach last one by calling
   * {@link com.github.celldynamics.quimp.PointsList#getNext()}
   * and from the last one, first should be accessible by calling
   * {@link com.github.celldynamics.quimp.PointsList#getPrev()}.
   * 
   * <p>For initialisation only.
   * 
   * @param n Node to be added to list
   * 
   */
  public void addPoint(final T n) {
    T prevNode = head.getPrev();
    n.setPrev(prevNode);
    n.setNext(head);

    head.setPrev(n);
    prevNode.setNext(n);
    POINTS++;
  }

  /**
   * Insert point ne after point n.
   * 
   * @param n reference point
   * @param ne point to be inserted after reference.
   * @return new node
   */
  public T insertPoint(final T n, final T ne) {
    T newNode = ne;
    ne.setTrackNum(nextTrackNumber++);
    newNode.setNext(n.getNext());
    newNode.setPrev(n);
    n.getNext().setPrev(newNode);
    n.setNext(newNode);
    POINTS++;
    return newNode;
  }

  /**
   * Remove selected point from list.
   * 
   * <p>Check if removed point was head and if it was, the new head is randomly selected. Neighbors
   * are linked together. There is no protection here against removing last node at all.
   * 
   * @param n point to remove
   * @param inner direction of normal vectors of Shape
   * @see #Shape(List, PointsList, boolean)
   */
  public void removePoint(final T n, boolean inner) {
    n.getPrev().setNext(n.getNext());
    n.getNext().setPrev(n.getPrev());

    // if removing head randomly assign a neighbour as new head
    if (n.isHead()) {
      if (Math.random() > threshold) {
        LOGGER.trace("removePoint - getNext");
        head = n.getNext();
      } else {
        LOGGER.trace("removePoint - getPrev");
        head = n.getPrev();
      }
      head.setHead(true);
    }

    POINTS--;

    n.getPrev().updateNormale(inner);
    n.getNext().updateNormale(inner);
    // WARN This may have influence to other parts of project. In original n was cleaned
    // n = null;
  }

  /**
   * Get number of points in Shape.
   * 
   * @return Number of points
   */
  public int getNumPoints() {
    return POINTS;
  }

  /**
   * Make Shape anti-clockwise.
   */
  public void makeAntiClockwise() {
    double sum = 0;
    T v = head;
    do {
      sum += (v.getNext().getX() - v.getX()) * (v.getNext().getY() + v.getY());
      v = v.getNext();
    } while (!v.isHead());
    if (sum > 0) {
      LOGGER.trace("Warning. Was clockwise, reversed");
      this.reverseShape();
      this.updateNormals(true); // WARN This was in Outline but not in Snake
    }
  }

  /**
   * Turn Shape back anti clockwise.
   */
  public void reverseShape() {
    T tmp;
    T v = head;
    do {
      tmp = v.getNext();
      v.setNext(v.getPrev());
      v.setPrev(tmp);
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Return current Shape as Java polygon
   * 
   * @return current Shape as java.awt.Polygon
   */
  public Polygon asPolygon() {
    Polygon pol = new Polygon();
    T n = head;
    do {
      pol.addPoint((int) Math.floor(n.getX() + 0.5), (int) Math.floor(n.getY() + 0.5));
      n = n.getNext();
    } while (!n.isHead());

    return pol;
  }

  /**
   * Returns current Shape as list of points (copy).
   * 
   * @return List of Point2d objects representing coordinates of T
   */
  public List<Point2d> asList() {
    List<Point2d> al = new ArrayList<Point2d>(POINTS);
    // iterate over nodes at Shape
    T n = head;
    do {
      al.add(new Point2d(n.getX(), n.getY()));
      n = n.getNext();
    } while (!n.isHead());
    return al;
  }

  /**
   * Return current Shape as ImageJ float number polygon.
   * 
   * @return current Shape as PolygonRoi
   */
  public Roi asFloatRoi() {
    float[] x = new float[POINTS];
    float[] y = new float[POINTS];

    T n = head;
    int i = 0;
    do {
      x[i] = (float) n.getX();
      y[i] = (float) n.getY();
      i++;
      n = n.getNext();
    } while (!n.isHead());
    return new PolygonRoi(x, y, POINTS, Roi.POLYGON);
  }

  /**
   * Return current Shape as ImageJ ROI object.
   * 
   * @return current Shape as ROI
   */
  public Roi asIntRoi() {
    Polygon p = asPolygon();
    Roi r = new PolygonRoi(p, PolygonRoi.POLYGON);
    return r;
  }

  /**
   * Count number of Points in Shape.
   * 
   * <p>Number of Points is stored in local POINTS field as well. This method can verify if that
   * field contains correct value.
   * 
   * @return number of Points in Shape or negative value if {@value #MAX_NODES} was reached
   */
  public int countPoints() {
    T v = head;
    int c = 0;
    do {
      c++;
      v = v.getNext();
    } while (!v.isHead() && c < MAX_NODES);

    if (c == MAX_NODES) {
      return -1;
    } else {
      return c;
    }
  }

  /**
   * Unfreeze all nodes in Shape.
   */
  public void unfreezeAll() {
    T v = head;
    do {
      v.unfreeze();
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Freeze all nodes in Shape.
   */
  public void freezeAll() {
    T v = head;
    do {
      v.freeze();
      v = v.getNext();
    } while (!v.isHead());
  }

  /**
   * Scale current Shape by <tt>stepSize</tt>. Centroid and normals need to be updated afterwards.
   * 
   * <p>Direction of scaling depends on direction of normals.
   * 
   * @param stepSize increment
   * @see PointsList#updateNormale(boolean)
   * @see #calcCentroid()
   * @see #setPositions()
   * @see PointsList#setClockwise(boolean)
   */
  public void scale(double stepSize) {
    T n;
    n = head;
    do {
      if (!n.isFrozen()) {
        n.setX(n.getX() + stepSize * n.getNormal().getX());
        n.setY(n.getY() + stepSize * n.getNormal().getY());
      }
      n = n.getNext();
    } while (!n.isHead());
  }

  /**
   * Convert coordinate of Shape to array.
   * 
   * @return x-coordinates of Outline as array
   */
  public double[] xtoArr() {
    double[] arry = new double[POINTS];

    T v = head;
    int i = 0;
    do {
      arry[i] = v.getX();
      i++;
      v = v.getNext();
    } while (!v.isHead());
    return arry;
  }

  /**
   * Convert coordinate of Shape to array.
   * 
   * @return y-coordinates of Outline as array
   */
  public double[] ytoArr() {
    double[] arry = new double[POINTS];

    T v = head;
    int i = 0;
    do {
      arry[i] = v.getY();
      i++;
      v = v.getNext();
    } while (!v.isHead());
    return arry;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<T> iterator() {
    int ret = validateShape();
    if (ret != 0) {
      LOGGER.warn("Shape is broken, iterator may be unpredicable. Reason: " + ret);
    }
    Iterator<T> it = new Iterator<T>() {
      private transient T nextToReturn = head; // element to return by next()
      private transient boolean traversed = false; // true if we started iterating, used for
      // detection that first head should be returned but second not (second - we approached head
      // from left side)

      @Override
      public boolean hasNext() {
        if (nextToReturn == null || nextToReturn.getNext() == null) {
          return false;
        }
        if (POINTS == 1 && nextToReturn.isHead()) {
          return true; // only head linked to itself
        }
        if (nextToReturn.isHead() && traversed == true) {
          return false; // is head and we started iterating, so approached it from left
        } else {
          return true; // iterating not started - return at least current element (head)
        }
      }

      @Override
      public T next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements");
        }
        T ret = nextToReturn;
        if (POINTS == 1) { // if only head
          nextToReturn = null;
        } else {
          nextToReturn = nextToReturn.getNext();
        }
        traversed = true;
        return ret;
      }
    };
    return it;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
    calcCentroid();
    setPositions();
    updateNormals(true);
    makeAntiClockwise();
    Elements = new ArrayList<>();
    T n = getHead().getNext(); // do not store head as it is stored in head variable
    do {
      Elements.add(n);
      n = n.getNext();
    } while (!n.isHead());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    if (Elements != null && Elements.size() > 0) {
      // head is saved as non-transitive field, so it is recreated on load and this object
      // exists already
      T first = head; // remember it
      Class<?> templateClass = head.getClass(); // get class name under Shape (T)
      try {
        Constructor<?> ctor = head.getClass().getDeclaredConstructor(templateClass);
        for (int i = 0; i < Elements.size(); i++) { // iterate over list from second position
          @SuppressWarnings("unchecked")
          T next = (T) ctor.newInstance(Elements.get(i));
          head.setNext(next);
          next.setPrev(head);
          head = next;
        }
        head.setNext(first);
        first.setPrev(head);
        head = first;
      } catch (SecurityException | NoSuchMethodException | InstantiationException
              | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e); // change to unchecked exception
      }
    }
    clearElements();
    calcCentroid(); // WARN Updating saved data - may be wrong
    setPositions();
    updateNormals(true);
    makeAntiClockwise();
  }

  /**
   * Clear <tt>Elements</tt> array that stores list of {Node, Snake} in ArrayList form. It is used
   * and initialized on Serialization. This method simply delete this array saving memory.
   * 
   * <p>It should be called after every serialisation.
   */
  public void clearElements() {
    if (Elements != null) {
      Elements.clear();
    }
    Elements = null;
  }

}
