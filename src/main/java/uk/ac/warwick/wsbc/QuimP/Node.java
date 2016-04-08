package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Represents a node in the snake - its basic component In fact this class
 * stands for bidirectional list containing Nodes. Every node has assigned 2D
 * position and several additional properties such as:
 * <ul>
 * <li>velocity of Node</li>
 * <li>total force of Node</li>
 * <li>normal vector</li>
 * </ul>
 * 
 * @author rtyson
 *
 */
public class Node {
    private ExtendedVector2d point; // x,y co-ordinates of the node
    private ExtendedVector2d normal; // normals
    private ExtendedVector2d tan;
    private ExtendedVector2d vel; // velocity of the nodes
    private ExtendedVector2d F_total; // total force at node
    private ExtendedVector2d prelimPoint; // point to move node to after all new
                                          // node positions have been calc
    private boolean frozen; // flag which is set when the velocity is below the
                            // critical velocity
    private int tracknumber;
    double position = -1; // position value.
    private Node prev; // predecessor to current node
    private Node next; // successor to current node
    private boolean head;
    private static boolean clockwise = true; // access clockwise if true
    // public QColor colour;

    public Node(int t) {
        // t = tracking number
        point = new ExtendedVector2d();
        F_total = new ExtendedVector2d();
        vel = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
        prelimPoint = new ExtendedVector2d();
        frozen = false;
        head = false;
        tracknumber = t;
        // colour = QColor.lightColor();
    }

    Node(double xx, double yy, int t) {
        point = new ExtendedVector2d(xx, yy);
        F_total = new ExtendedVector2d();
        vel = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
        prelimPoint = new ExtendedVector2d();
        frozen = false;
        head = false;
        tracknumber = t;
        // colour = QColor.lightColor();
    }

    public double getX() {
        // get X space co-ordinate
        return point.getX();
    }

    public double getY() {
        // get X space co-ordinate
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

    /**
     * Update point and force with preliminary values, and reset.
     */
    public void update() {
        setX(getX() + prelimPoint.getX());
        setY(getY() + prelimPoint.getY());
        prelimPoint.setX(0);
        prelimPoint.setY(0);
    }

    /**
     * Get previous node in chain (next if not clockwise)
     * 
     * @return next or previous Node from list
     */
    public Node getPrev() {
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
    public Node getNext() {
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
    public void setPrev(Node n) {
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
    public void setNext(Node n) {
        if (clockwise) {
            next = n;
        } else {
            prev = n;
        }
    }

    public static void setClockwise(boolean b) {
        Node.clockwise = b;
    }

    public ExtendedVector2d getPoint() {
        return point;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public ExtendedVector2d getF_total() {
        return F_total;
    }

    public ExtendedVector2d getVel() {
        return vel;
    }

    public ExtendedVector2d getNormal() {
        return normal;
    }

    public ExtendedVector2d getTangent() {
        return tan;
    }

    public int getTrackNum() {
        return tracknumber;
    }

    /**
     * Sets total force for Node
     * 
     * @param f vector of force to assign to Node force
     */
    public void setF_total(ExtendedVector2d f) {
        F_total.setX(f.getX());
        F_total.setY(f.getY());
    }

    /**
     * Sets velocity for Node
     * 
     * @param v vector of velocity to assign to Node force
     */
    public void setVel(ExtendedVector2d v) {
        vel.setX(v.getX());
        vel.setY(v.getY());
    }

    /**
     * Updates total force for Node
     * 
     * @param f vector of force to add to Node force
     */
    public void addF_total(ExtendedVector2d f) {
        // add the xy values in f to xy F_total i.e updates total Force
        F_total.setX(F_total.getX() + f.getX());
        F_total.setY(F_total.getY() + f.getY());
    }

    /**
     * Updates velocity for Node
     * 
     * @param v vector of velocity to add to Node force
     */
    public void addVel(ExtendedVector2d v) {
        // adds the xy values in v to Vel i.e. updates velocity
        vel.setX(vel.getX() + v.getX());
        vel.setY(vel.getY() + v.getY());
    }

    public void setPrelim(ExtendedVector2d v) {
        prelimPoint.setX(v.getX());
        prelimPoint.setY(v.getY());
    }

    public void freeze() {
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean t) {
        head = t;
    }

    /**
     * Updates the normal (must point inwards)
     */
    public void updateNormale() {
        boolean c = clockwise;
        clockwise = true; // just in case
        tan = calcTan(); // tangent

        /*
         * // calc local orientation matrix double xa, ya, xb, yb, xc, yc; xa = prev.getX(); ya =
         * prev.getY(); xb = getX(); yb = getY(); xc = next.getX(); yc = next.getY();
         * 
         * double localO = (xb*yc + xa*yb + ya*xc) - (ya*xb + yb*xc + xa*yc); //determinant of
         * orientation if(localO==0){ IJ.log( "orientation is flat!"); normal.setX(-tan.getY());
         * normal.setY(tan.getX()); }else if(localO*detO > 0){ normal.setX(-tan.getY());
         * normal.setY(tan.getX()); }else{ normal.setX(tan.getY()); normal.setY(-tan.getX()); }
         * 
         * if (p.expandSnake) { // switch around if expanding snake normal.setX(-normal.getY());
         * normal.setY(-normal.getX()); }
         */

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
     * 
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

        tan = ExtendedVector2d.unitVector(pointLeft, pointRight);

        return tan;
    }

    public static void randDirection() {
        if (Math.random() < 0.5) {
            clockwise = true;
        } else {
            clockwise = false;
        }
    }

    public double getCurvatureLocal() {

        ExtendedVector2d edge1 =
                ExtendedVector2d.vecP2P(this.getPoint(), this.getPrev().getPoint());
        ExtendedVector2d edge2 =
                ExtendedVector2d.vecP2P(this.getPoint(), this.getNext().getPoint());

        double angle = ExtendedVector2d.angle(edge1, edge2) * (180 / Math.PI);

        if (angle > 360 || angle < -360) {
            System.out.println("Warning-angle out of range (Vert l:320)");
        }

        if (angle < 0)
            angle = 360 + angle;

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

    public String toString() {
        String str;
        // str = "[" + this.getX() + "," + this.getY() + "] " + "head is " + head + " next:"
        // + getNext() + " prev: " + getPrev();
        str = "[" + this.getX() + "," + this.getY() + "] " + "track " + tracknumber;
        return str;

    }

}