package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

public abstract class BidirectionalList<T extends BidirectionalList<?>> {
    protected T prev;
    protected T next;
    protected ExtendedVector2d point; // x,y co-ordinates of the node
    protected ExtendedVector2d normal; // normals
    protected ExtendedVector2d tan;
    protected boolean head = false;
    protected static boolean clockwise = true; // access clockwise if true
    protected int tracknumber;

    public BidirectionalList(int t) {
        // t = tracking number
        point = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
        tracknumber = t;
    }

    public BidirectionalList(double xx, double yy, int t) {
        this(t);
        point = new ExtendedVector2d(xx, yy);
    }

    /**
     * \c point getter
     * @return X space co-ordinate
     */
    public double getX() {
        return point.getX();
    }

    /**
     * \c point getter
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
        BidirectionalList.clockwise = b;
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
    public void updateNormale() {
        boolean c = clockwise;
        clockwise = true; // just in case
        tan = calcTan(); // tangent

        if (!BOA_.boap.segParam.expandSnake) { // switch around if expanding snake
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

}
