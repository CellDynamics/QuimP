package uk.ac.warwick.wsbc.QuimP;

import java.awt.Polygon;
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
public class Snake extends Shape<Node> {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(Snake.class.getName());
    public boolean alive; // snake is alive
    private int snakeID;
    public double startingNnodes; // how many nodes at start of segmentation
    // used as a reference for node limit
    private int FROZEN; // number of nodes frozen
    private double minX, minY, maxX, maxY;
    private Rectangle bounds = new Rectangle(); // snake bounds
    private ExtendedVector2d centroid;

    /**
     * Create a snake from existing linked list (at least one head node)
     * 
     * @param h Node of list
     * @param N Number of nodes
     * @param id Unique snake ID related to object being segmented.
     * @throws Exception
     */
    public Snake(final Node h, int N, int id) throws BoaException {
        super(h, N);
        snakeID = id;
        FROZEN = N;
        // colour = QColor.lightColor();
        centroid = new ExtendedVector2d(0d, 0d);
        this.calcCentroid();

        removeNode(head);
        this.makeAntiClockwise();
        this.updateNormales();
        alive = true;
        startingNnodes = super.getNumPoints() / 100.; // as 1%. limit to X%
        // calcOrientation();
    }

    /**
     * Copy constructor
     * 
     * @param snake Snake to be duplicated
     * @param id New id
     * @throws BoaException 
     */
    public Snake(final Snake snake, int id) throws BoaException {

        head = new Node(0); // dummy head node
        head.setHead(true);

        Node prev = head;
        Node nn;
        Node sn = snake.getHead();
        do {
            nn = new Node(sn.getTrackNum());
            nn.setX(sn.getX());
            nn.setY(sn.getY());

            nn.setPrev(prev);
            prev.setNext(nn);

            prev = nn;
            sn = sn.getNext();
        } while (!sn.isHead());
        nn.setNext(head); // link round tail
        head.setPrev(nn);

        snakeID = id;
        POINTS = snake.getNODES() + 1;
        FROZEN = super.getNumPoints();
        nextTrackNumber = super.getNumPoints() + 1;
        centroid = new ExtendedVector2d(0d, 0d);
        removeNode(head);
        this.makeAntiClockwise();
        this.updateNormales();
        alive = snake.alive;
        startingNnodes = snake.startingNnodes;
        this.calcCentroid();
        /*
         * from initializearraylist head = new Node(0); NODES = 1; FROZEN = 0; head.setPrev(head);
         * head.setNext(head); head.setHead(true);
         * 
         * Node node; for (Point2d el : p) { node = new Node(el.getX(), el.getY(),
         * nextTrackNumber++); addNode(node); }
         * 
         * removeNode(head); this.makeAntiClockwise(); updateNormales();
         */
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
        startingNnodes = super.getNumPoints() / 100.; // as 1%. limit to X%
        alive = true;
        // colour = QColor.lightColor();
        // calcOrientation();
        this.calcCentroid();
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
        startingNnodes = super.getNumPoints() / 100.; // as 1%. limit to X%
        alive = true;
        // colour = QColor.lightColor();
        // calcOrientation();
        this.calcCentroid();
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
        startingNnodes = super.getNumPoints() / 100;
        alive = true;
        this.calcCentroid();
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
        startingNnodes = super.getNumPoints() / 100;
        alive = true;
        this.calcCentroid();
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
            addNode(node);
        }
        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales();
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
                addNode(node);
            }
        }
        removeNode(head); // remove dummy head node new head will be set
        this.makeAntiClockwise();
        updateNormales();
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
            addNode(node);
        }

        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales();
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
            addNode(node);
        }

        removeNode(head); // remove dummy head node
        this.makeAntiClockwise();
        updateNormales();
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
            addNode(node);
        }

        removeNode(head);
        this.makeAntiClockwise();
        updateNormales();
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
            addNode(node);
        }

        removeNode(head);
        this.makeAntiClockwise();
        updateNormales();
    }

    public void printSnake() {
        System.out.println("Print Nodes (" + super.getNumPoints() + ")");
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
        if (i != super.getNumPoints()) {
            System.out.println("NODES and linked list dont tally!!");
        }
    }

    /**
     * Assign head to node \c nodeIndex.
     * 
     * Do not change head if \c nodeIndex is not found
     * 
     * @param nodeIndex Index of node of new head
     */
    public void setNewHead(int nodeIndex) {
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
    public int getNODES() {
        return super.getNumPoints();
    }

    /**
     * Unfreeze all nodes
     */
    public void defreeze() {
        Node n = head;
        do {
            n.unfreeze();
            n = n.getNext();
        } while (!n.isHead());
        FROZEN = 0;
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
        if (FROZEN == super.getNumPoints()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add node before head node assuring that list has closed loop. If initial
     * list condition is defined in such way:
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
     * @param newNode Node to be added to list
     * 
     * @remarks For initialization only
     */
    private void addNode(final Node newNode) {
        Node prevNode = head.getPrev();
        newNode.setPrev(prevNode);
        newNode.setNext(head);

        head.setPrev(newNode);
        prevNode.setNext(newNode);
        POINTS++;
    }

    /**
     * Remove selected node from list Check if removed node was head and if it
     * was, the new head is randomly selected
     * 
     * @param n Node to remove
     * 
     * @throws Exception
     */
    final public void removeNode(Node n) throws BoaException {
        if (super.getNumPoints() <= 3) {
            throw new BoaException("removeNode: Did not remove node. " + super.getNumPoints()
                    + " nodes remaining.", 0, 2);
        }
        if (n.isFrozen()) {
            FROZEN--;
        }
        super.removePoint(n, BOA_.boap.segParam.expandSnake);
    }

    /**
     * Update all node normals Called after modification of Snake nodes
     */
    public void updateNormales() {
        Node n = head;
        do {
            n.updateNormale(BOA_.boap.segParam.expandSnake);
            n = n.getNext();
        } while (!n.isHead());
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
        cx = cx / super.getNumPoints();
        cy = cy / super.getNumPoints();

        intializeOval(nextTrackNumber, (int) cx, (int) cy, 4, 4, 1);
    }

    private double calcArea() {
        double area, sum;
        sum = 0.0;
        Node n = head;
        Node np1 = n.getNext();
        do {
            sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
            n = n.getNext();
            np1 = n.getNext(); // note: n is reset on prev line

        } while (!n.isHead());
        area = 0.5 * sum;
        return area;
    }

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
            updateNormales();
        }
    }

    /**
     * Cut out a loop Insert a new node at cut point
     */
    public void cutLoops() {
        // System.out.println("cutting loops");
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
            interval = (super.getNumPoints() > MAXINTERVAL + 3) ? MAXINTERVAL
                    : (super.getNumPoints() - 3);

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

        // this.checkNodeNumber();
        // System.out.println("done cutting loops");
    }

    /**
     * Cut out intersects. Done once at the end of each frame to cut out any
     * parts of the contour that self intersect. Similar to cutLoops, but check
     * all edges (NODES / 2) and cuts out the smallest section
     * 
     * @see cutLoops()
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
            nB = nA.getNext().getNext();// don't check next edge as they can't
                                        // cross, but do touch
            interval = (super.getNumPoints() > 6) ? super.getNumPoints() / 2 : 2; // always leave 3
                                                                                  // nodes, at
            // least

            for (int i = 2; i < interval; i++) {
                if (nB.isHead()) {
                    cutHead = true;
                }
                state = ExtendedVector2d.segmentIntersection(nA.getX(), nA.getY(),
                        nA.getNext().getX(), nA.getNext().getY(), nB.getX(), nB.getY(),
                        nB.getNext().getX(), nB.getNext().getY(), intersect);
                if (state == 1) {
                    // System.out.println("CutIntersect: cut out an intersect:
                    // x0: " +
                    // nA.getX() + ", y0:" + nA.getY()+ ", x1 :"
                    // +nA.getNext().getX()+ ", y1: " +nA.getNext().getY() +
                    // ", x2: "+nB.getX()+ ", y2: " + nB.getY()+ ", x3: "
                    // +nB.getNext().getX()+ ", y3: " + nB.getNext().getY());

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

                if ((super.getNumPoints() - (i + 1)) < 4) {
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
     * @deprecated Old version of correctDistance(boolean)
     * @throws Exception
     */
    public void correctDistanceOLD() throws Exception {
        // ensure nodes are between maxDist and minDist apart, add remove nodes
        // as required

        double Di, avg_dist, InsX, InsY, InsNormX, InsNormY, rand;
        ExtendedVector2d tan;

        // choose a random direction to process the chain
        Node.randDirection();

        avg_dist = 0.5 * (BOA_.boap.getMin_dist() + BOA_.boap.getMax_dist()); // compute
        // average
        // distance

        Node n = head;
        Node n_neigh = n.getNext(); // either the left or right neighbour
        do {
            // compute tangent
            tan = ExtendedVector2d.vecP2P(n.getPoint(), n_neigh.getPoint());

            // compute Distance
            Di = tan.length();

            if (Di > 2. * avg_dist) { // distance greater than DistMax: add in
                                      // node
                Node nIns = insertNode(n);
                nIns.setVel(n.getVel());
                nIns.getVel().makeUnit();
                nIns.getVel().multiply(BOA_.boap.segParam.vel_crit * 2);

                // V2. random postion on average normale
                InsNormX = 0.5 * (n.getNormal().getX() + n_neigh.getNormal().getX());
                InsNormY = 0.5 * (n.getNormal().getY() + n_neigh.getNormal().getY());
                // move along -ve normale rand amount at least 0.05)
                rand = 0.05 + (-2. * Math.random());
                InsX = (rand * InsNormX) + (0.5 * (n.getX() + n_neigh.getX()));
                InsY = (rand * InsNormY) + (0.5 * (n.getY() + n_neigh.getY()));

                nIns.getPoint().setX(InsX);
                nIns.getPoint().setY(InsY);

                // update normals of those nodes effected
                nIns.updateNormale(BOA_.boap.segParam.expandSnake);
                n.updateNormale(BOA_.boap.segParam.expandSnake);
                n.getNext().updateNormale(BOA_.boap.segParam.expandSnake);
                n.getNext().getNext().updateNormale(BOA_.boap.segParam.expandSnake);
                n = nIns;

            } else if (Di < BOA_.boap.getMin_dist() && super.getNumPoints() >= 4) { // Minimum Nodes
                // is 3
                removeNode(n_neigh); // removes Node n_neigh
                n_neigh = n.getNext();
            }

            n = n.getNext();
            n_neigh = n_neigh.getNext();
        } while (!n.isHead());

        Node.setClockwise(true); // reset to clockwise (although shouldnt effect
                                 // things??)
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
     * Insert node after node \c n
     */
    public Node insertNode(final Node n) {
        return insertPoint(n, new Node());
    }

    /**
     * Return current \c snake as polygon
     */
    public Polygon asPolygon() {
        Polygon pol = new Polygon();
        Node n = head;

        do {
            pol.addPoint((int) Math.floor(n.getX() + 0.5), (int) Math.floor(n.getY() + 0.5));
            n = n.getNext();
        } while (!n.isHead());

        return pol;
    }

    public void setPositions() {
        double length = getLength();
        double d = 0.;

        Node v = head;
        do {
            v.position = d / length;
            d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Add up lengths between all verts
     * 
     * @return length of snake
     */
    public double getLength() {
        Node v = head;
        double length = 0.0;
        do {
            length += ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            v = v.getNext();
        } while (!v.isHead());
        return length;
    }

    Roi asIntRoi() {
        Polygon p = asPolygon();
        Roi r = new PolygonRoi(p, PolygonRoi.POLYGON);
        return r;
    }

    Roi asFloatRoi() {

        float[] x = new float[super.getNumPoints()];
        float[] y = new float[super.getNumPoints()];

        Node n = head;
        int i = 0;
        do {
            x[i] = (float) n.getX();
            y[i] = (float) n.getY();
            i++;
            n = n.getNext();
        } while (!n.isHead());
        return new PolygonRoi(x, y, super.getNumPoints(), Roi.POLYGON);
    }

    Roi asPolyLine() {
        float[] x = new float[super.getNumPoints()];
        float[] y = new float[super.getNumPoints()];

        Node n = head;
        int i = 0;
        do {
            x[i] = (float) n.getX();
            y[i] = (float) n.getY();
            i++;
            n = n.getNext();
        } while (!n.isHead());
        return new PolygonRoi(x, y, super.getNumPoints(), Roi.POLYLINE);
    }

    /**
     * Returns current Snake as list of Nodes (copy)
     * 
     * @return List of Vector2d objects representing coordinates of Snake Nodes
     */
    public List<Point2d> asList() {
        List<Point2d> al = new ArrayList<Point2d>(super.getNumPoints());
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
     * Gets bounds of snake
     * 
     * @return Bounding box of current Snake object as Double
     */
    public Rectangle2D.Double getDoubleBounds() {
        // change tp asPolygon, and get bounds
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
     * Count the nodes and check that NODES matches
     * 
     * @return \c true if counted nodes matches \c NODES
     */
    public boolean checkNodeNumber() {
        Node n = head;
        int count = 0;
        do {
            count++;
            n = n.getNext();
        } while (!n.isHead());

        if (count != super.getNumPoints()) {
            System.out.println(
                    "Node number wrong. NODES:" + super.getNumPoints() + " .actual: " + count);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if there is a head node
     * 
     * @return \c true if there is head of snake
     */
    public boolean checkIsHead() {
        // make sure there is a head node
        Node n = head;
        int count = 0;
        do {
            if (count++ > 10000) {
                System.out.println("Head lost!!!!");
                return false;
            }
            n = n.getNext();
        } while (!n.isHead());
        return true;
    }

    public void editSnake() {
        System.out.println("Editing a snake");
    }

    public ExtendedVector2d getCentroid() {
        return centroid;
    }

    /**
     * Calculate centroid of Snake
     */
    public void calcCentroid() {
        centroid = new ExtendedVector2d(0, 0);
        Node v = this.head;
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

    public void makeAntiClockwise() {
        // BOA_.log("Checking if clockwise...");
        double sum = 0;
        Node v = head;
        do {
            sum += (v.getNext().getX() - v.getX()) * (v.getNext().getY() + v.getY());
            v = v.getNext();
        } while (!v.isHead());
        if (sum > 0) {
            // BOA_.log("\tclockwise, reversed");
            this.reverseSnake();
        }
    }

    /**
     * Turn Snake back anti clockwise
     */
    public void reverseSnake() {
        Node tmp;
        Node v = head;
        do {
            tmp = v.getNext();
            v.setNext(v.getPrev());
            v.setPrev(tmp);
            v = v.getNext();
        } while (!v.isHead());
    }

    public String toString() {
        throw new UnsupportedOperationException("Not imnplemented");
    }

}