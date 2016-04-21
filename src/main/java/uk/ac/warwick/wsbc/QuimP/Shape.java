package uk.ac.warwick.wsbc.QuimP;

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
 */
public abstract class Shape<T extends PointsList<T>> {
    protected int nextTrackNumber = 1; /*!< next node ID's */
    protected T head; /*!< first node in double linked list, always maintained */
    protected int POINTS; /*!< number of points */
    double position = -1; // position value. TODO move to Snake as it is referenced only there
    protected ExtendedVector2d centroid = null; /*!< centroid point of the Shape */

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
     * @param h head point of the list
     * @param N number of points in the list 
     */
    public Shape(T h, int N) {
        head = h;
        POINTS = N;
        nextTrackNumber = N + 1;
    }

    /**
     * Create Shape from one point, created Shape is looped
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
     * @return length of snake
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

}
