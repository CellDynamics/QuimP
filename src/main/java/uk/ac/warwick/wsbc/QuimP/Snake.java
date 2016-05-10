package uk.ac.warwick.wsbc.QuimP;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Low level snake definition. Form snake from Node objects. Snake is defined by
 * first \c head node. Remaining nodes are in bidirectional linked list.
 * 
 * @remarks Node list may be modified externally but then method such as findNode(),
 * updateNormales(), calcCentroid() should be called to update internal fields of Snake. If number
 * of nodes changes it is recommended to create \b new object.
 * 
 * @author rtyson
 *
 */
public class Snake extends Shape<Node> implements IQuimpSerialize {
    private static final Logger LOGGER = LogManager.getLogger(Snake.class.getName());
    public boolean alive; //!< \c true if snake is alive
    private int snakeID; //!< unique ID of snake
    public double startingNnodes; //!< how many nodes at start of segmentation
    private int FROZEN; //!< number of nodes frozen
    private Rectangle bounds = new Rectangle(); //!< snake bounds
    
    /**
     * Create a snake from existing linked list (at least one head node)
     * 
     * @param h Node of list
     * @param N Number of nodes
     * @param id Unique snake ID related to object being segmented.
     * @throws Exception
     * @warning List is referenced only not copied
     * Behavior of this method was changed. Now it does not make copy of Node. In old 
     * approach there was dummy node deleted in this constructor.
     * @code{.java}
     *  index = 0;
     *  head = new Vert(index); // dummy head node
     *  head.setHead(true);
     *  prevn = head;
     *  index++;
     *  // insert next nodes here
     * @endcode 
     */
    public Snake(final Node h, int N, int id) throws BoaException {
        super(h, N);
        snakeID = id;
        centroid = new ExtendedVector2d(0d, 0d);
        calcCentroid();

        // removeNode(head);
        this.makeAntiClockwise();
        this.updateNormales(BOA_.boap.segParam.expandSnake);
        alive = true;
        startingNnodes = POINTS / 100.; // as 1%. limit to X%
        countFrozen(); // set FROZEN
        // calcOrientation();
    }

    /**
     * Copy constructor
     * 
     * @param src Snake to be duplicated
     * @param id New id
     */
    public Snake(final Snake src, int id) {
        super(src);
        alive = src.alive;
        snakeID = id;
        startingNnodes = src.startingNnodes;
        countFrozen();
        bounds = new Rectangle(src.bounds);
        calcCentroid();
    }

    /**
     * Create snake from ROI
     * 
     * @param R ROI with object to be segmented
     * @param id Unique ID of snake related to object being segmented.
     * @param direct
     * @throws Exception
     */
    public Snake(final Roi R, int id, boolean direct) throws Exception {
        // place nodes in a circle
        snakeID = id;
        if (R.getType() == Roi.RECTANGLE || R.getType() == Roi.POLYGON) {
            if (direct) {
                intializePolygonDirect(R.getFloatPolygon());
            } else {
                intializePolygon(R.getFloatPolygon());
            }
        } else {
            Rectangle Rect = R.getBounds();
            int xc = Rect.x + Rect.width / 2;
            int yc = Rect.y + Rect.height / 2;
            int Rx = Rect.width / 2;
            int Ry = Rect.height / 2;

            intializeOval(0, xc, yc, Rx, Ry, BOA_.boap.segParam.getNodeRes() / 2);
        }
        startingNnodes = POINTS / 100.; // as 1%. limit to X%
        alive = true;
        // colour = QColor.lightColor();
        // calcOrientation();
        calcCentroid();
    }

    /**
     * @see Snake(Roi, int, boolean)
     * @param R
     * @param id
     * @throws Exception
     */
    public Snake(final PolygonRoi R, int id) throws Exception {
        snakeID = id;
        intializeFloat(R.getFloatPolygon());
        startingNnodes = POINTS / 100.; // as 1%. limit to X%
        alive = true;
        // colour = QColor.lightColor();
        // calcOrientation();
        calcCentroid();
    }

    /**
     * Construct Snake object from list of nodes
     * 
     * @param list list of nodes as Vector2d
     * @param id id of Snake
     * @throws Exception
     */
    public Snake(final List<? extends Tuple2d> list, int id) throws Exception {
        snakeID = id;
        initializeArrayList(list);
        startingNnodes = POINTS / 100;
        alive = true;
        calcCentroid();
    }

    /**
     * Construct Snake object from X and Y arrays
     * 
     * @param X x coordinates of nodes
     * @param Y y coordinates of nodes
     * @param id id of Snake
     * @throws Exception
     */
    public Snake(final double X[], final double Y[], int id) throws Exception {
        snakeID = id;
        initializeArray(X, Y);
        startingNnodes = POINTS / 100;
        alive = true;
        calcCentroid();
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + FROZEN;
        result = prime * result + (alive ? 1231 : 1237);
        result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
        result = prime * result + snakeID;
        long temp;
        temp = Double.doubleToLongBits(startingNnodes);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Snake))
            return false;
        Snake other = (Snake) obj;
        if (FROZEN != other.FROZEN)
            return false;
        if (alive != other.alive)
            return false;
        if (bounds == null) {
            if (other.bounds != null)
                return false;
        } else if (!bounds.equals(other.bounds))
            return false;
        if (snakeID != other.snakeID)
            return false;
        if (Double.doubleToLongBits(startingNnodes) != Double
                .doubleToLongBits(other.startingNnodes))
            return false;
        return true;
    }

    /**
     * @return the snakeID
     */
    public int getSnakeID() {
        return snakeID;
    }

    /**
     * @param snakeID the snakeID to set
     * @warning Should be used carefully
     */
    protected void setSnakeID(int snakeID) {
        this.snakeID = snakeID;
    }

    /**
     * Initializes \c Node list from ROIs other than polygons For non-polygon
     * ROIs ellipse is used as first approximation of segmented shape.
     * Parameters of ellipse are estimated usually using parameters of bounding
     * box of user ROI This method differs from other \c initialize* methods by
     * input data which do not contain nodes but the are defined analytically
     * 
     * @param t index of node
     * @param xc center of ellipse
     * @param yc center of ellipse
     * @param Rx ellipse diameter
     * @param Ry ellipse diameter
     * @param s number of nodes
     * 
     * @throws Exception
     */
    private void intializeOval(int t, int xc, int yc, int Rx, int Ry, double s) throws Exception {
        head = new Node(t); // make a dummy head node for list initialization
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head); // link head to itself
        head.setNext(head);
        head.setHead(true);

        double theta = 2.0 / (double) ((Rx + Ry) / 2);

        // nodes are added in behind the head node
        Node node;
        for (double a = 0.0; a < (2 * Math.PI); a += s * theta) {
            node = new Node(nextTrackNumber);
            nextTrackNumber++;
            node.getPoint().setX((int) (xc + Rx * Math.cos(a)));
            node.getPoint().setY((int) (yc + Ry * Math.sin(a)));
            addPoint(node);
        }
        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    /**
     * Initializes \c Node list from polygon Each edge of input polygon is
     * divided on uk.ac.warwick.wsbc.QuimP.boap.nodeRes nodes
     * 
     * @param p Polygon extracted from IJ ROI
     * @throws Exception
     */
    private void intializePolygon(final FloatPolygon p) throws Exception {
        // System.out.println("poly with node distance");
        head = new Node(0); // make a dummy head node for list initialization
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head); // link head to itself
        head.setNext(head);
        head.setHead(true);

        Node node;
        int j, nn;
        double x, y, spacing;
        ExtendedVector2d a, b, u;
        for (int i = 0; i < p.npoints; i++) {
            j = ((i + 1) % (p.npoints)); // for last i point we turn for first
                                         // one closing polygon
            a = new ExtendedVector2d(p.xpoints[i], p.ypoints[i]);// vectors ab
                                                                 // define edge
            b = new ExtendedVector2d(p.xpoints[j], p.ypoints[j]);

            nn = (int) Math
                    .ceil(ExtendedVector2d.lengthP2P(a, b) / BOA_.boap.segParam.getNodeRes());
            spacing = ExtendedVector2d.lengthP2P(a, b) / (double) nn;
            u = ExtendedVector2d.unitVector(a, b);
            u.multiply(spacing); // required distance between points

            for (int s = 0; s < nn; s++) { // place nodes along edge
                node = new Node(nextTrackNumber);
                nextTrackNumber++;
                x = a.getX() + (double) s * u.getX();
                y = a.getY() + (double) s * u.getY();
                node.setX(x);
                node.setY(y);
                addPoint(node);
            }
        }
        removeNode(head); // remove dummy head node new head will be set
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    /**
     * Initializes \c Node list from polygon Does not refine points. Use only
     * those nodes available in polygon
     * 
     * @param p Polygon extracted from IJ ROI
     * @throws Exception
     * @see intializePolygon(FloatPolygon)
     */
    private void intializePolygonDirect(final FloatPolygon p) throws Exception {
        // System.out.println("poly direct");
        head = new Node(0); // make a dummy head node for list initialization
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head); // link head to itself
        head.setNext(head);
        head.setHead(true);

        Node node;
        for (int i = 0; i < p.npoints; i++) {
            node = new Node((double) p.xpoints[i], (double) p.ypoints[i], nextTrackNumber++);
            addPoint(node);
        }

        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    /**
     * @see intializePolygonDirect(FloatPolygon)
     * @param p
     * @throws Exception
     * @todo This method is the same as intializePolygonDirect(FloatPolygon)
     */
    private void intializeFloat(final FloatPolygon p) throws Exception {
        // System.out.println("poly direct");
        head = new Node(0); // make a dummy head node
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head); // link head to itself
        head.setNext(head);
        head.setHead(true);

        Node node;
        for (int i = 0; i < p.npoints; i++) {
            node = new Node((double) p.xpoints[i], (double) p.ypoints[i], nextTrackNumber++);
            addPoint(node);
        }

        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    /**
     * Initialize snake from List of Vector2d objects
     * 
     * @param p List as initializer of Snake
     * @throws Exception
     */
    private void initializeArrayList(final List<? extends Tuple2d> p) throws Exception {
        head = new Node(0);
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head);
        head.setNext(head);
        head.setHead(true);

        Node node;
        for (Tuple2d el : p) {
            node = new Node(el.getX(), el.getY(), nextTrackNumber++);
            addPoint(node);
        }

        removeNode(head);
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    /**
     * Initialize snake from X, Y arrays
     * 
     * @param X x coordinates of nodes
     * @param Y y coordinates of nodes
     * @throws Exception
     */
    private void initializeArray(final double X[], final double Y[]) throws Exception {
        head = new Node(0);
        POINTS = 1;
        FROZEN = 0;
        head.setPrev(head);
        head.setNext(head);
        head.setHead(true);

        if (X.length != Y.length)
            throw new Exception("Lengths of X and Y arrays are not equal");

        Node node;
        for (int i = 0; i < X.length; i++) {
            node = new Node(X[i], Y[i], nextTrackNumber++);
            addPoint(node);
        }

        removeNode(head);
        this.makeAntiClockwise();
        updateNormales(BOA_.boap.segParam.expandSnake);
    }

    public void printSnake() {
        System.out.println("Print Nodes (" + POINTS + ")");
        int i = 0;
        Node n = head;
        do {
            int x = (int) n.getPoint().getX();
            int y = (int) n.getPoint().getY();
            System.out.println(i + " Node " + n.getTrackNum() + ", x:" + x + ", y:" + y + ", vel: "
                    + n.getVel().length());
            n = n.getNext();
            i++;
        } while (!n.isHead());
        if (i != POINTS) {
            System.out.println("NODES and linked list dont tally!!");
        }
    }

    /**
     * Assign head to node \c nodeIndex.
     * 
     * Do not change \b head if \c nodeIndex is not found or there is no \b head in list
     * 
     * @param nodeIndex Index of node of new head
     */
    public void setNewHead(int nodeIndex) {
        if (!checkIsHead())
            return;
        Node n = head;
        Node oldhead = n;
        do {
            n = n.getNext();
        } while (n.getTrackNum() != nodeIndex && !n.isHead());
        n.setHead(true);
        if (oldhead != n)
            oldhead.setHead(false);
        head = n;
        LOGGER.debug("New head is: " + getHead().toString());
    }

    /**
     * Get number of nodes forming current Snake
     * 
     * @return number of nodes in current Snake
     */
    public int getNumNodes() {
        return POINTS;
    }

    /**
     * Unfreeze all nodes
     */
    public void unfreezeAll() {
        super.unfreezeAll();
        FROZEN = 0;
    }

    /**
     * Go through whole list and count Nodes that are frozen.
     * 
     * Set \c FREEZE variable 
     */
    private void countFrozen() {
        Node n = head;
        FROZEN = 0;
        do {
            if (n.isFrozen())
                FROZEN++;
            n = n.getNext();
        } while (!n.isHead());
    }

    /**
     * Freeze a specific node
     * 
     * @param n Node to freeze
     */
    public void freezeNode(Node n) {
        if (!n.isFrozen()) {
            n.freeze();
            FROZEN++;
        }
    }

    /**
     * Unfreeze a specific node
     * 
     * @param n Node to unfreeze
     */
    public void unfreezeNode(Node n) {
        if (n.isFrozen()) {
            n.unfreeze();
            FROZEN--;
        }
    }

    /**
     * Check if all nodes are frozen
     * 
     * @return \c true if all nodes are frozen
     */
    public boolean isFrozen() {
        if (FROZEN == POINTS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove selected node from list.
     * 
     * Perform check if removed node was head and if it was, the new head is randomly selected.
     * Neighbors are linked together
     * 
     * @param n Node to remove
     * 
     * @throws BoaException on insufficient number of nodes 
     */
    final public void removeNode(Node n) throws BoaException {
        if (POINTS <= 3) {
            throw new BoaException(
                    "removeNode: Did not remove node. " + POINTS + " nodes remaining.", 0, 2);
        }
        if (n.isFrozen()) {
            FROZEN--;
        }
        super.removePoint(n, BOA_.boap.segParam.expandSnake);
    }

    public void blowup() throws Exception {
        scale(BOA_.boap.segParam.blowup, 4, true);
    }

    public void shrinkSnake() throws BoaException {
        scale(-BOA_.boap.segParam.finalShrink, 0.5, false);
    }

    public void implode() throws Exception {
        // calculate centroid
        double cx;
        double cy;
        cx = 0.0;
        cy = 0.0;
        Node n = head;
        do {
            cx += n.getX();
            cy += n.getY();
            n = n.getNext();
        } while (!n.isHead());
        cx = cx / POINTS;
        cy = cy / POINTS;

        intializeOval(nextTrackNumber, (int) cx, (int) cy, 4, 4, 1);
    }

    /**
     * Scale current Snake by \c amount in increments of \c stepSize
     * 
     * @param amount scale
     * @param stepSize increment
     * @param correct
     * @throws BoaException
     * @see uk.ac.warwick.wsbc.QuimP.Shape.scale(double, double)
     */
    public void scale(double amount, double stepSize, boolean correct) throws BoaException {
        if (amount == 0)
            return;
        // make sure snake access is clockwise
        Node.setClockwise(true);
        // scale the snake by 'amount', in increments of 'stepsize'
        if (amount > 0) {
            stepSize *= -1; // scale down if amount negative
        }
        double steps = Math.abs(amount / stepSize);
        // IJ.log(""+steps);
        Node n;
        int j;
        for (j = 0; j < steps; j++) {
            n = head;
            do {
                if (!n.isFrozen()) {
                    n.setX(n.getX() + stepSize * n.getNormal().getX());
                    n.setY(n.getY() + stepSize * n.getNormal().getY());
                }
                n = n.getNext();
            } while (!n.isHead());
            if (correct) {
                correctDistance(false);
            }
            cutLoops();
            updateNormales(BOA_.boap.segParam.expandSnake);
        }
    }

    /**
     * Cut out a loop Insert a new node at cut point
     */
    public void cutLoops() {
        int MAXINTERVAL = 12; // how far ahead do you check for a loop
        int interval, state;

        Node nA, nB;
        double[] intersect = new double[2];
        Node newN;

        boolean cutHead;

        nA = head;
        do {
            cutHead = (nA.getNext().isHead()) ? true : false;
            nB = nA.getNext().getNext(); // don't check next edge as they can't
                                         // cross, but do touch

            // always leave 3 nodes, at least
            interval = (POINTS > MAXINTERVAL + 3) ? MAXINTERVAL : (POINTS - 3);

            for (int i = 0; i < interval; i++) {
                if (nB.isHead()) {
                    cutHead = true;
                }
                state = ExtendedVector2d.segmentIntersection(nA.getX(), nA.getY(),
                        nA.getNext().getX(), nA.getNext().getY(), nB.getX(), nB.getY(),
                        nB.getNext().getX(), nB.getNext().getY(), intersect);
                if (state == 1) {
                    // System.out.println("CutLoops: cut out a loop");
                    newN = this.insertNode(nA);
                    newN.setX(intersect[0]);
                    newN.setY(intersect[1]);

                    newN.setNext(nB.getNext());
                    nB.getNext().setPrev(newN);

                    newN.updateNormale(BOA_.boap.segParam.expandSnake);
                    nB.getNext().updateNormale(BOA_.boap.segParam.expandSnake);

                    // set velocity
                    newN.setVel(nB.getVel());
                    if (newN.getVel().length() < BOA_.boap.segParam.vel_crit) {
                        newN.getVel().makeUnit();
                        newN.getVel().multiply(BOA_.boap.segParam.vel_crit * 1.5);
                    }

                    if (cutHead) {
                        newN.setHead(true); // put a new head in
                        head = newN;
                    }

                    POINTS -= (i + 2); // the one skipped and the current one
                    break;
                }
                nB = nB.getNext();
            }
            nA = nA.getNext();
        } while (!nA.isHead());
    }

    /**
     * Cut out intersects. Done once at the end of each frame to cut out any
     * parts of the contour that self intersect. Similar to cutLoops, but check
     * all edges (NODES / 2) and cuts out the smallest section
     * 
     * @see cutLoops()
     * @see uk.ac.warwick.wsbc.QuimP.Outline.cutSelfIntersects()
     */
    public void cutIntersects() {

        int interval, state;

        Node nA, nB;
        double[] intersect = new double[2];
        Node newN;

        boolean cutHead;

        nA = head;
        do {
            cutHead = (nA.getNext().isHead()) ? true : false;
            nB = nA.getNext().getNext();// don't check next edge as they can't cross, but do touch
            interval = (POINTS > 6) ? POINTS / 2 : 2; // always leave 3 nodes, at least

            for (int i = 2; i < interval; i++) {
                if (nB.isHead()) {
                    cutHead = true;
                }

                state = ExtendedVector2d.segmentIntersection(nA.getX(), nA.getY(),
                        nA.getNext().getX(), nA.getNext().getY(), nB.getX(), nB.getY(),
                        nB.getNext().getX(), nB.getNext().getY(), intersect);

                if (state == 1) {
                    newN = this.insertNode(nA);
                    newN.setX(intersect[0]);
                    newN.setY(intersect[1]);

                    newN.setNext(nB.getNext());
                    nB.getNext().setPrev(newN);

                    newN.updateNormale(BOA_.boap.segParam.expandSnake);
                    nB.getNext().updateNormale(BOA_.boap.segParam.expandSnake);

                    if (cutHead) {
                        newN.setHead(true); // put a new head in
                        head = newN;
                    }

                    POINTS -= (i);
                    break;
                }
                nB = nB.getNext();
            }

            nA = nA.getNext();
        } while (!nA.isHead());
    }

    /**
     * @deprecated Old version of cutLoops()
     */
    public void cutLoopsOLD() {

        int i;

        double diffX, diffY, diffXp, diffYp;
        Node node1, node2, right1, right2;
        boolean ishead; // look for head node in section to be cut
        // check the next INTERVALL nodes for cross-overs
        int INTERVALL = 10; // 8 //20

        node1 = head;
        do {
            ishead = false;
            right1 = node1.getNext();
            node2 = right1.getNext();
            right2 = node2.getNext();

            diffX = right1.getPoint().getX() - node1.getPoint().getX();
            diffY = right1.getPoint().getY() - node1.getPoint().getY();

            for (i = 1; i <= INTERVALL; ++i) {
                // see if the head node will be cut out
                if (node2.isHead() || right1.isHead()) {
                    ishead = true;
                }
                diffXp = right2.getPoint().getX() - node2.getPoint().getX();
                diffYp = right2.getPoint().getY() - node2.getPoint().getY();

                if ((POINTS - (i + 1)) < 4) {
                    break;
                }
                if (node1.getTrackNum() == right2.getTrackNum()) { // dont go
                                                                   // past node1
                    break;
                } else if (((diffX * node2.getY() - diffY * node2.getX()) < (diffX * node1.getY()
                        - diffY * node1.getX())
                        ^ (diffX * right2.getY() - diffY * right2.getX()) < (diffX * node1.getY()
                                - diffY * node1.getX()))
                        & ((diffXp * node1.getY() - diffYp * node1.getX()) < (diffXp * node2.getY()
                                - diffYp * node2.getX())
                                ^ (diffXp * right1.getY()
                                        - diffYp * right1.getX()) < (diffXp * node2.getY()
                                                - diffYp * node2.getX()))) {

                    // join node1 to right 2
                    // int node1index = Contour.getNodeIndex(node1); //debug
                    // int right2index = Contour.getNodeIndex(right2); //debug

                    // IJ.log("Cut Loop! cut from node1 " + node1index + " to
                    // right2 " + right2index + " interval " + i);
                    node1.setNext(right2);
                    right2.setPrev(node1);
                    node1.updateNormale(BOA_.boap.segParam.expandSnake);
                    right2.updateNormale(BOA_.boap.segParam.expandSnake);
                    POINTS -= i + 1; // set number of nodes

                    if (ishead) {
                        head = right2;
                        right2.setHead(true);
                    }
                    break;
                }
                node2 = node2.getNext();
                right2 = right2.getNext();
            }
            node1 = node1.getNext(); // next node will be right2 if it cut

        } while (!node1.isHead());
        // if (NODES < 4) {
        // // System.out.println("CutLoops. Nodes left after cuts: " + NODES);
        // }
    }

    /**
     * Ensure nodes are between \c maxDist and \c minDist apart, add remove
     * nodes as required
     * 
     * @param shiftNewNode
     * @throws Exception
     */
    public void correctDistance(boolean shiftNewNode) throws BoaException {
        Node.randDirection(); // choose a random direction to process the chain

        ExtendedVector2d tanL, tanR, tanLR, npos; //
        double dL, dR, dLR, tmp;

        Node nC = head;
        Node nL, nR; // neighbours

        do {

            nL = nC.getPrev(); // left neighbour
            nR = nC.getNext(); // left neighbour

            // compute tangent
            tanL = ExtendedVector2d.vecP2P(nL.getPoint(), nC.getPoint());
            tanR = ExtendedVector2d.vecP2P(nC.getPoint(), nR.getPoint());
            tanLR = ExtendedVector2d.vecP2P(nL.getPoint(), nR.getPoint());
            dL = tanL.length();
            dR = tanR.length();
            dLR = tanLR.length();

            if (dL < BOA_.boap.getMin_dist() || dR < BOA_.boap.getMin_dist()) {
                // nC is to close to a neigbour
                if (dLR > 2 * BOA_.boap.getMin_dist()) {

                    // move nC to middle
                    npos = new ExtendedVector2d(tanLR.getX(), tanLR.getY());
                    npos.multiply(0.501); // half
                    npos.addVec(nL.getPoint());

                    nC.setX(npos.getX());
                    nC.setY(npos.getY());

                    // tmp = Math.sqrt((dL*dL) - ((dLR/2.)*(dLR/2.)));
                    // System.out.println("too close, move to middle, tmp:
                    // "+tmp);

                    tmp = Math.sin(ExtendedVector2d.angle(tanL, tanLR)) * dL;
                    // tmp = Vec2d.distPointToSegment(nC.getPoint(),
                    // nL.getPoint(), nR.getPoint());
                    nC.getNormal().multiply(-tmp);
                    nC.getPoint().addVec(nC.getNormal());

                    nC.updateNormale(BOA_.boap.segParam.expandSnake);
                    nL.updateNormale(BOA_.boap.segParam.expandSnake);
                    nR.updateNormale(BOA_.boap.segParam.expandSnake);
                    this.unfreezeNode(nC);

                } else {
                    // delete nC
                    // System.out.println("delete node");
                    removeNode(nC);
                    nL.updateNormale(BOA_.boap.segParam.expandSnake);
                    nR.updateNormale(BOA_.boap.segParam.expandSnake);
                    if (nR.isHead())
                        break;
                    nC = nR.getNext();
                    continue;
                }
            }
            if (dL > BOA_.boap.getMax_dist()) {

                // System.out.println("1357-insert node");
                Node nIns = insertNode(nL);
                nIns.setVel(nL.getVel());
                nIns.getVel().addVec(nC.getVel());
                nIns.getVel().multiply(0.5);
                if (nIns.getVel().length() < BOA_.boap.segParam.vel_crit) {
                    nIns.getVel().makeUnit();
                    nIns.getVel().multiply(BOA_.boap.segParam.vel_crit * 1.5);
                }

                npos = new ExtendedVector2d(tanL.getX(), tanL.getY());
                npos.multiply(0.51);
                npos.addVec(nL.getPoint());

                nIns.setX(npos.getX());
                nIns.setY(npos.getY());
                nIns.updateNormale(BOA_.boap.segParam.expandSnake);
                if (shiftNewNode) {
                    nIns.getNormal().multiply(-2); // move out a bit
                    nIns.getPoint().addVec(nIns.getNormal());
                    nIns.updateNormale(BOA_.boap.segParam.expandSnake);
                }
                nL.updateNormale(BOA_.boap.segParam.expandSnake);
                nR.updateNormale(BOA_.boap.segParam.expandSnake);
                nC.updateNormale(BOA_.boap.segParam.expandSnake);

            }

            nC = nC.getNext();
        } while (!nC.isHead());

        Node.setClockwise(true); // reset to clockwise (although shouldnt effect
                                 // things??)
    }

    /**
     * Insert default Node after Node \c v
     *  
     * @param n Node to insert new Node after
     * @return Inserted Node
     */
    public Node insertNode(final Node n) {
        return insertPoint(n, new Node());
    }

    /**
     * Return current Snake as \b POLYLINE
     * 
     * @return ij.gui.PolygonRoi.PolygonRoi as \b POLYLINE type
     */
    Roi asPolyLine() {
        float[] x = new float[POINTS];
        float[] y = new float[POINTS];

        Node n = head;
        int i = 0;
        do {
            x[i] = (float) n.getX();
            y[i] = (float) n.getY();
            i++;
            n = n.getNext();
        } while (!n.isHead());
        return new PolygonRoi(x, y, POINTS, Roi.POLYLINE);
    }

    /**
     * Returns current Snake as list of Nodes (copy)
     * 
     * @return List of Vector2d objects representing coordinates of Snake Nodes
     */
    public List<Point2d> asList() {
        List<Point2d> al = new ArrayList<Point2d>(POINTS);
        // iterate over nodes at Snake
        Node n = head;
        do {
            al.add(new Point2d(n.getX(), n.getY()));
            n = n.getNext();
        } while (!n.isHead());
        return al;
    }

    /**
     * Gets bounds of snake
     * 
     * @return Bounding box of current Snake object
     */
    public Rectangle getBounds() {

        Rectangle2D.Double rect = getDoubleBounds();

        bounds.setBounds((int) rect.getMinX(), (int) rect.getMinY(), (int) rect.getWidth(),
                (int) rect.getHeight());
        return bounds;
    }

    /**
     * Get bounds of snake
     * 
     * @return Bounding box of current Snake object as Double
     */
    public Rectangle2D.Double getDoubleBounds() {
        double minX, minY, maxX, maxY;
        Node n = head;
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
     * Check if there is a head node
     * 
     * Traverse along first 10000 Node elements and check if any of them is \b head
     * 
     * @return \c true if there is head of snake
     */
    public boolean checkIsHead() {
        Node n = head;
        int count = 0;
        do {
            if (count++ > MAX_NODES) {
                LOGGER.error("Head lost!!!!");
                return false;
            }
            n = n.getNext();
        } while (!n.isHead());
        return true;
    }

    public void editSnake() {
        System.out.println("Editing a snake");
    }

    /**
     * Print Snake nodes
     * 
     * @return String representation of Snake
     */
    public String toString() {
        Node v = this.head;
        String out = "id=" + this.getSnakeID();
        do {
            out = out.concat(" {" + v.getX() + "," + v.getY() + "}");
            v = v.getNext();
        } while (!v.isHead());
        return out;
    }

}