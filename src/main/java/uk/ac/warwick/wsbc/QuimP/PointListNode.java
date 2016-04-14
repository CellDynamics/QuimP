package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Represents node of bidirectional list of points in Cartesian coordinates.
 * 
 * This abstract class contains basic properties of points and provides method for moving across 
 * the list. Points in list are numbered from \b 1 and list can be looped. There is one special
 * node called \b head that indicates beginning of the list (and its end if the list is looped) 
 * 
 * @author p.baniukiewicz
 * @date 14 Apr 2016
 *
 * @param <T> Type of point, currently can be Node or Vert
 */
public abstract class PointListNode<T extends PointListNode<T>> {
    protected T prev; /*!< previous point in list, \c null if no other point */
    protected T next; /*!< next point in list, \c null if no other point */
    protected ExtendedVector2d point; /*!< x,y co-ordinates of the point */
    protected ExtendedVector2d normal; /*!< normal vector */
    protected ExtendedVector2d tan; /*!< tangent vector */
    protected boolean head = false; /*!< Indicate if this point is \b head */
    protected static boolean clockwise = true; /*!< access clockwise if true */
    protected int tracknumber = 1; /*!< ID number of point, unique across list */
    /**
     * flag which is set when the velocity is below the critical velocity
     */
    protected boolean frozen = false;

    /**
     * Default constructor, assumes that first point is created on list with ID = 1
     */
    public PointListNode() {
        point = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
    }

    /**
     * Creates point with given ID. New point is not linked to any other yet.
     * 
     * Caller should care about correct numbering of points
     * 
     * @param t ID of point
     */
    public PointListNode(int t) {
        this();
        setTrackNum(t);
    }

    /**
     * Creates point with given ID and coordinates. New point is not linked to any other yet.
     * 
     * Caller should care about correct numbering of points
     * 
     * @param xx x coordinate of point
     * @param yy y coordinate of point
     * @param t ID of point
     */
    public PointListNode(double xx, double yy, int t) {
        this(t);
        point = new ExtendedVector2d(xx, yy);
    }

    /**
     * \c point getter
     * 
     * @return X space co-ordinate
     */
    public double getX() {
        return point.getX();
    }

    /**
     * \c point getter
     * 
     * @return Y space co-ordinate
     */
    public double getY() {
        return point.getY();
    }

    /**
     * Set \c X space co-ordinate
     * 
     * @param x coordinate
     */
    public void setX(double x) {
        point.setX(x);
    }

    /**
     * Set \c Y space co-ordinate
     * 
     * @param y coordinate
     */
    public void setY(double y) {
        point.setY(y);
    }

    public static void setClockwise(boolean b) {
        PointListNode.clockwise = b;
    }

    public ExtendedVector2d getPoint() {
        return point;
    }

    public int getTrackNum() {
        return tracknumber;
    }

    public ExtendedVector2d getNormal() {
        return normal;
    }

    public ExtendedVector2d getTangent() {
        return tan;
    }

    public boolean isHead() {
        return head;
    }

    public void setNormal(double x, double y) {
        normal.setX(x);
        normal.setY(y);
    }

    public void setTrackNum(int b) {
        tracknumber = b;
    }

    /**
     * Set head marker to current node
     * 
     * @param t \c true if current node is head, \c false otherwise
     * @warning Only one Node in Snake can be head
     * @see uk.ac.warwick.wsbc.QuimP.Snake.setNewHead(int)
     * @see uk.ac.warwick.wsbc.QuimP.Snake
     */
    public void setHead(boolean t) {
        head = t;
    }

    /**
     * Get previous node in chain (next if not clockwise)
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
     * Get next node in chain (previous if not clockwise)
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
     * Adds previous (or next if not clockwise) Node to list
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
     * Adds next (or previous if not clockwise) Node to list
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
     * Updates the normal (must point inwards)
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
     * Calculate tangent at Node n (i.e. unit vector between neighbors)
     * extends BidirectionalList<Vertex>
     * Calculate a unit vector towards neighboring nodes and then a unit vector
     * between their ends. direction important for normale calculation. Always
     * calculate tan as if clockwise
     *
     * @return Tangent at node
     */
    private ExtendedVector2d calcTan() {

        ExtendedVector2d unitVecLeft = ExtendedVector2d.unitVector(point, prev.getPoint());
        ExtendedVector2d unitVecRight = ExtendedVector2d.unitVector(point, next.getPoint());

        ExtendedVector2d pointLeft = new ExtendedVector2d();
        pointLeft.setX(getX());
        pointLeft.setY(getY());
        pointLeft.addVec(unitVecLeft);

        ExtendedVector2d pointRight = new ExtendedVector2d();
        pointRight.setX(getX());
        pointRight.setY(getY());
        pointRight.addVec(unitVecRight);

        return ExtendedVector2d.unitVector(pointLeft, pointRight);
    }

    public static void randDirection() {
        if (Math.random() < 0.5) {
            clockwise = true;
        } else {
            clockwise = false;
        }
    }

    /**
     * Current Point as String
     * 
     * @return String representation of Node
     */
    public String toString() {
        String str;
        // str = "[" + this.getX() + "," + this.getY() + "] " + "head is " + head + " next:"
        // + getNext() + " prev: " + getPrev();
        str = "[" + this.getX() + "," + this.getY() + "] " + "track " + tracknumber;
        return str;
    }

}
