package uk.ac.warwick.wsbc.QuimP;

import java.awt.Polygon;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Form abstract shape from bidirectional list of points.
 * 
 * This abstract class keeps head point of Shape and control number of points in Shape, allows for
 * inserting points to the Shape
 *   
 * @author p.baniukiewicz
 * @date 14 Apr 2016
 *
 * @param <T> Type of point, currently can be Node or Vert
 * @remarks Generally assumes that Shape is closed, so PointsList is looped
 */
public abstract class Shape<T extends PointsList<T>> {
    private static final Logger LOGGER = LogManager.getLogger(Shape.class.getName());
    protected int nextTrackNumber = 1; /*!< next node ID's */
    protected T head; /*!< first node in double linked list, always maintained */
    protected int POINTS; /*!< number of points */
    double position = -1; // position value. TODO move to Snake as it is referenced only there
    protected ExtendedVector2d centroid = null; /*!< centroid point of the Shape */
    public static final int MAX_NODES = 10000; //!< Max number of nodes allowed in Shape 

	/**
     * Default constructor, create empty Shape
     */
    public Shape() {
        POINTS = 0;
        head = null;
    }

    /**
     * Create Shape from existing list of points (can be one point as well)
     * 
     * @warning List of points must be looped
     * @param h head point of the list
     * @param N number of points in the list 
     */
    public Shape(T h, int N) {
        head = h;
        POINTS = N;
        nextTrackNumber = N + 1;
    }

    /**
     * Create Shape from one point, created Shape is looped. 
     * If \c h is a list, only \c h will be maintained and list will be unlinked.
     * 
     * @param h head point of the list
     */
    public Shape(final T h) {
        this(h, 1);
        head.setHead(true);
        head.setNext(head);
        head.setPrev(head);
        nextTrackNumber = head.getTrackNum() + 1;
    }

    /**
     * Copy constructor
     * 
     * @param src source Shape to copy from
     * @throws RuntimeException when T does no have copy constructor
     */
    @SuppressWarnings("unchecked")
    public Shape(final Shape<T> src) {
        T tmpHead = src.getHead(); // get head as representative object
        Class<?> tClass = tmpHead.getClass(); // get class name under Shape (T)
        try { // Constructor of T as type can not be called directly, use reflection
              // get Constructor of T with one parameter of Type T (copy constructor)
            Constructor<?> ctor = tmpHead.getClass().getDeclaredConstructor(tClass);
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
            throw new RuntimeException(e); // change fo unchecked exception
        }
        // copy rest of params
        POINTS = src.POINTS;
        position = src.position;
        nextTrackNumber = src.nextTrackNumber;
        calcCentroid();
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + POINTS;
        result = prime * result + ((centroid == null) ? 0 : centroid.hashCode());
        if (head == null)
            result = prime * result + 0;
        else { // go through the whole list
            T n = head;
            do {
                result = prime * result + n.hashCode();
                n = n.getNext();
            } while (!n.isHead());
        }
        result = prime * result + nextTrackNumber;
        long temp;
        temp = Double.doubleToLongBits(position);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Shape))
            return false;
        @SuppressWarnings("unchecked")
        Shape<T> other = (Shape<T>) obj;
        if (POINTS != other.POINTS)
            return false;
        if (centroid == null) {
            if (other.centroid != null)
                return false;
        } else if (!centroid.equals(other.centroid))
            return false;
        if (head == null) {
            if (other.head != null)
                return false;
        } else {// iterate over list of nodes compare all
            T n = head;
            T nobj = other.getHead();
            boolean status = true;
            do {
                status &= n.equals(nobj);
                n = n.getNext();
                nobj = nobj.getNext();
            } while (!n.isHead());
            if (!status)
                return false;
        }
        if (nextTrackNumber != other.nextTrackNumber)
            return false;
        if (Double.doubleToLongBits(position) != Double.doubleToLongBits(other.position))
            return false;
        return true;
    }

    /**
     * Getter for \c centroid
     * 
     * @return centroid
     */
    public ExtendedVector2d getCentroid() {
        if (centroid == null)
            calcCentroid();
        return centroid;
    }

    /**
     * Calculate centroid of Snake
     * 
     * @warning This method modifies internal field \c centroid. In two classes that use this 
     * template (Snake and Outline) its application is different. In Snake it is called in
     * constructor and on any change of Snake, so this field every time holds correct value.
     * In Outline it is called on demand and putting this method in constructor leads to
     * unpredictable errors in ECMM
     */
    public void calcCentroid() {
        centroid = new ExtendedVector2d(0, 0);
        T v = head;
        double x, y, g;
        do {
            g = (v.getX() * v.getNext().getY()) - (v.getNext().getX() * v.getY());
            x = (v.getX() + v.getNext().getX()) * g;
            y = (v.getY() + v.getNext().getY()) * g;
            centroid.setX(centroid.getX() + x);
            centroid.setY(centroid.getY() + y);
            v = v.getNext();
        } while (!v.isHead());

        centroid.multiply(1d / (6 * this.calcArea()));
    }

    /**
     * Calculate area of the Shape
     * 
     * @return Area
     */
    private double calcArea() {
        double area, sum;
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
     * Add up lengths between all verts
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
     * Update all node normales
     * 
     * Called after modification of Shape nodes
     * 
     * @param inner Direction of the Shape
     */
    public void updateNormales(boolean inner) {
        T v = head;
        do {
            v.updateNormale(inner);
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Get head of current Shape
     * 
     * @return Point representing head of Shape
     */
    public T getHead() {
        return head;
    }

    /**
     * Add node before head node assuring that list has closed loop. 
     * 
     * If initial list condition is defined in such way:
     * 
     * @code
     * head = new Node(0); //make a dummy head node NODES = 1; FROZEN = 0;
     * head.setPrev(head); // link head to itself head.setNext(head);
     * head.setHead(true);
     * @endcode
     * 
     * The \c addNode will produce closed bidirectional linked list.
     * From first Node it is possible to reach last one by calling
     * Node::getNext() and from the last one, first should be accessible
     * by calling Node::getPrev()
     * 
     * @param n Node to be added to list
     * 
     * @remarks For initialization only
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
     * Insert point \c ne after point \c n
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
     * Check if removed point was head and if it was, the new head is randomly selected. 
     * Neighbors are linked together
     * 
     * @param n point to remove
     * @param inner direction of normal vectors of Shape
     * @warning There is no protection here against removing last node at all
     */
    public void removePoint(final T n, boolean inner) {
        n.getPrev().setNext(n.getNext());
        n.getNext().setPrev(n.getPrev());

        // if removing head randomly assign a neighbour as new head
        if (n.isHead()) {
            if (Math.random() <= 1.0) { // WARN Change to original code here!
                head = n.getNext();
            } else {
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
     * Get number of points in Shape
     * 
     * @return Number of points
     */
    public int getNumPoints() {
        return POINTS;
    }

    /**
     * Make Shape anti-clockwise
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
            this.reverseSnake();
            this.updateNormales(true); // WARN This was in Outline but not in Snake
        } else {
        }
    }

    /**
     * Turn Shape back anti clockwise
     */
    public void reverseSnake() {
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
     * Return current Shape as ImageJ float number polygon
     * 
     * @return current Shape as PolygonRoi
     */
    Roi asFloatRoi() {
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
     * Return current Shape as ImageJ ROI object
     * 
     * @return current Shape as ROI
     */
    Roi asIntRoi() {
        Polygon p = asPolygon();
        Roi r = new PolygonRoi(p, PolygonRoi.POLYGON);
        return r;
    }

    /**
     * Count number of Points in Shape
     * 
     * Number of Points is stored in local POINTS field as well. This method can verify if that
     * field contains correct value
     * 
     * @return number of Points in Shape
     */
    public int countPoints() {
        T v = head;
        int c = 0;
        do {
            c++;
            v = v.getNext();
        } while (!v.isHead());

        return c;
    }

    /**
     * Unfreeze all nodes in Shape
     */
    public void unfreezeAll() {
        T v = head;
        do {
            v.unfreeze();
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Freeze all nodes in Shape
     */
    public void freezeAll() {
        T v = head;
        do {
            v.freeze();
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Scale current Shape by \c amount in increments of \c stepSize
     * 
     * @param amount scale
     * @param stepSize increment
     */
    public void scale(double amount, double stepSize) {
        // make sure snake access is clockwise
        PointsList.setClockwise(true);
        if (amount > 0) {
            stepSize *= -1; // scale down if amount negative
        }
        double steps = Math.abs(amount / stepSize);
        // IJ.log(""+steps);
        T n;
        int j;
        for (j = 0; j < steps; j++) {
            n = head;
            do {

                n.setX(n.getX() + stepSize * n.getNormal().getX());
                n.setY(n.getY() + stepSize * n.getNormal().getY());
                n = n.getNext();
            } while (!n.isHead());
            // cutSelfIntersects();
            updateNormales(false);
            calcCentroid();
        }
    }

}
