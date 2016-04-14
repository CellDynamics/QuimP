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
public class Node extends PointListNode<Node> {
    private ExtendedVector2d vel; // velocity of the nodes
    private ExtendedVector2d F_total; // total force at node
    private ExtendedVector2d prelimPoint; // point to move node to after all new
                                          // node positions have been calc
    double position = -1; // position value.

    public Node() {
        super();
        F_total = new ExtendedVector2d();
        vel = new ExtendedVector2d();
        prelimPoint = new ExtendedVector2d();
    }

    public Node(int t) {
        super(t);
        F_total = new ExtendedVector2d();
        vel = new ExtendedVector2d();
        prelimPoint = new ExtendedVector2d();
    }

    Node(double xx, double yy, int t) {
        super(xx, yy, t);
        F_total = new ExtendedVector2d();
        vel = new ExtendedVector2d();
        prelimPoint = new ExtendedVector2d();
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

    public boolean isFrozen() {
        return frozen;
    }

    public ExtendedVector2d getF_total() {
        return F_total;
    }

    public ExtendedVector2d getVel() {
        return vel;
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

}