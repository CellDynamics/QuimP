/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 *
 * @author tyson
 */
public final class Outline extends Shape<Vert> implements Cloneable {
    private static final Logger LOGGER = LogManager.getLogger(Outline.class.getName());
    QColor color; /*!< Color of the Outline */

    /**
     * Create a Outline from existing linked list
     * 
     * @param h head node of linked list
     * @param N number of nodes in list
     * @warning head node \c h is deleted from list. List \c h should have dummy head node
     * @code{.java}
     *  index = 0;
     *  head = new Vert(index); // dummy head node
     *  head.setHead(true);
     *  prevn = head;
     *  index++;
     *  // insert next nodes here
     * @endcode 
     * @see uk.ac.warwick.wsbc.QuimP.OutlineHandler.readOutlines(final File) for example of use
     */
    public Outline(final Vert h, int N) {
        super(h, N);
        removeVert(head);
        this.updateCurvature();
        // calcCentroid(); It was introduced after 6819719a but apparently it causes wrong ECMM

        color = new QColor(0.5, 0, 1);
    }

    /**
     * Blank outline
     * 
     * @param h Initial Vert
     */
    public Outline(final Vert h) {
        super(h);
        this.updateCurvature();
        // calcCentroid(); It was introduced after 6819719a but apparently it causes wrong ECMM

        color = new QColor(0.5, 0, 1);
    }

    /**
     * Copy constructor. Copy properties of Outline
     * 
     * Previous or next points are not copied
     * 
     * @param src Source Outline
     * @todo TODO To implement and replace clone()
     */
    public Outline(final Outline src) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Create an outline from an Roi
     * 
     * @param roi Initial ROI
     */
    public Outline(final Roi roi) {
        Polygon p = roi.getPolygon();

        head = new Vert(0); // make a dummy head node
        POINTS = 1;
        head.setPrev(head); // link head to itself
        head.setNext(head);
        head.setHead(true);

        Vert v = head;
        // Vert lastInsert = head;
        // int j, nn;
        // double x, y, spacing;
        // Vect2d a, b, u;

        for (int i = 0; i < p.npoints; i++) {
            v = insertVert(v);
            v.setX(p.xpoints[i]);
            v.setY(p.ypoints[i]);
        }
        removeVert(head); // remove dummy head node
        updateNormales(false);
        this.updateCurvature();
        // calcCentroid(); It was introduced after 6819719a but apparently it causes wrong ECMM
    }

    public void print() {
        IJ.log("Print verts (" + POINTS + ")");
        int i = 0;
        Vert v = head;
        do {
            // int x = (int) v.getPoint().getX();
            // int y = (int) v.getPoint().getY();
            double x = v.getPoint().getX();
            double y = v.getPoint().getY();
            double c = v.coord;
            double f = v.fCoord;

            String xx = IJ.d2s(x, 8);
            String yy = IJ.d2s(y, 8);
            String cc = IJ.d2s(c, 3);
            String ff = IJ.d2s(f, 3);

            String sH = "";
            String sI = "\t";
            if (v.isHead()) {
                sH = " isHead";
            }
            if (v.isIntPoint()) {
                sI = "\t isIntPoint(" + v.intsectID + ")";
            }

            IJ.log("Vert " + v.getTrackNum() + " (" + cc + ")(" + ff + "), x:" + xx + ", y:" + yy
                    + sI + sH);
            v = v.getNext();
            i++;
        } while (!v.isHead());
        if (i != POINTS) {
            IJ.log("VERTS and linked list dont tally!!");
        }
    }

    public void plotOutline(String title, boolean per) {
        double[] xArr = new double[POINTS]; // arrays to hold x and y co-ords
        // (duplicate last to join)
        double[] yArr = new double[POINTS];

        Vert v = head;
        int i = 0;
        do {
            xArr[i] = v.getX();
            yArr[i] = v.getY();

            if (per && v.isIntPoint()) {
                xArr[i] = xArr[i] - 4;
                yArr[i] = yArr[i] + 3;
            }
            i++;
            v = v.getNext();
        } while (!v.isHead());

        // xArr[i] = head.getX(); // duplicate last vert
        // yArr[i] = head.getY();

        // Tool.plotXY(xArr, yArr, title);
    }

    public void plotRegion(Vert v, int z, String title) {
        double[] xArr = new double[z];
        double[] yArr = new double[z];

        int i = 0;
        do {
            xArr[i] = v.getX();
            yArr[i] = v.getY();
            if (i == z - 1) {
                break;
            }
            i++;
            v = v.getNext();
        } while (!v.isHead());

        // Tool.plotXY(xArr, yArr, title);
    }

    /**
     * Get number of Vert objects forming current Outline
     * 
     * @return number of Vert in current Outline
     */
    public int getNumVerts() {
        return POINTS;
    }

    /**
     * Remove selected Vert from list.
     * 
     * Perform check if removed Vert was head and if it was, the new head is randomly selected.
     * Neighbors are linked together
     * 
     * @param v Vert to remove
     */
    public void removeVert(final Vert v) {
        if (POINTS <= 3) {
            LOGGER.error("Outline. 175. Can't remove node. less than 3 would remain");
            return;
        }
        super.removePoint(v, true);
        if (POINTS < 3) {
            IJ.error("Outline.199. WARNING! Nodes less then 3");
        }
    }

    /**
     * Update curvature for all Vert in Outline
     */
    public void updateCurvature() {
        Vert v = head;
        do {
            v.calcCurvatureLocal();
            v = v.getNext();
        } while (!v.isHead());
    }

    /**
     * Insert default Vert after Vert \c v
     *  
     * @param v Vert to insert new Vert after
     * @return Inserted Vert
     */
    public Vert insertVert(final Vert v) {
        return insertPoint(v, new Vert());
    }

    /**
     * Insert a vertex in-between \c v and \c v.next with interpolated values
     * 
     * Modify current Outline
     * 
     * @param v 
     * @return Vertex created between \c v and \c v.next
     */
    public Vert insertInterpolatedVert(final Vert v) {
        Vert newVert = insertVert(v);
        newVert.setX((newVert.getPrev().getX() + newVert.getNext().getX()) / 2);
        newVert.setY((newVert.getPrev().getY() + newVert.getNext().getY()) / 2);

        newVert.updateNormale(true);
        newVert.distance = (newVert.getPrev().distance + newVert.getNext().distance) / 2;
        newVert.gCoord = interpolateCoord(newVert.getPrev().gCoord, newVert.getNext().gCoord);
        newVert.fCoord = interpolateCoord(newVert.getPrev().fCoord, newVert.getNext().fCoord);

        return newVert;
    }

    public double interpolateCoord(double a, double b) {
        // interpolate between co-ordinates (that run 0-1)
        if (a > b) {
            b = b + 1;
        }
        double dis = a + ((b - a) / 2);

        if (dis >= 1) {
            dis += -1; // passed zero
        }
        return dis;
    }

    /**
     * Convert coordinate of Outline to array
     * 
     * @return x-coordinates of Outline as array
     */
    public double[] XasArr() {
        double[] arry = new double[POINTS];

        Vert v = head;
        int i = 0;
        do {
            arry[i] = v.getX();
            i++;
            v = v.getNext();
        } while (!v.isHead());
        return arry;
    }

    /**
     * Convert coordinate of Outline to array
     * 
     * @return y-coordinates of Outline as array
     */
    public double[] YasArr() {
        double[] arry = new double[POINTS];

        Vert v = head;
        int i = 0;
        do {
            arry[i] = v.getY();
            i++;
            v = v.getNext();
        } while (!v.isHead());
        return arry;
    }

    /**
     * Evenly spaces a new vertices around the ouline by following vectors between verts
     * 
     * @param density
     */
    public void setResolution(double density) {
        double length = getLength();
        int numVerts = (int) Math.round((length / density)); // must be round number of verts
        density = length / numVerts; // re-cal the density

        double remaining = 0.;
        double lastPlacement, currentDis;

        Vert oldHead = head;
        Vert v1 = oldHead;

        // new, higher res outline
        head = new Vert(v1.getX(), v1.getY(), v1.getTrackNum());
        head.setHead(true);
        head.setIntPoint(oldHead.isIntPoint(), oldHead.intsectID);
        head.setNext(head);
        head.setPrev(head);

        head.gCoord = 0.;
        head.coord = 0.;

        nextTrackNumber = oldHead.getTrackNum() + 1;
        POINTS = 1;

        double CoorSpaceing = 1.0 / numVerts;
        // System.out.println("coord: " + CoorSpaceing);
        double currentCoor = CoorSpaceing;

        Vert temp, newV;
        newV = head;

        int numVertsInserted = 1; // the head
        ExtendedVector2d uEdge, edge, placementVector;
        do {
            Vert v2 = v1.getNext();
            lastPlacement = 0.0;
            currentDis = 0.0;
            edge = ExtendedVector2d.vecP2P(v1.getPoint(), v2.getPoint());
            // edge.print("edge");
            uEdge = ExtendedVector2d.unitVector(v1.getPoint(), v2.getPoint());
            // uEdge.print("uEdge");
            if (edge.length() == 0) { // points on top of one another, move on
                v1 = v1.getNext();
                continue;
            }

            while (true) {
                placementVector = new ExtendedVector2d(uEdge.getX(), uEdge.getY());
                // double mult = (density + currentDis) - remaining;
                // System.out.println("mult "+mult);
                // placementVector.print("before Mult");
                placementVector.multiply((density + currentDis) - remaining);

                // placementVector.print("\tafter Mult");

                if (placementVector.length() <= edge.length() && numVertsInserted < numVerts) { // if
                    currentDis = placementVector.length();
                    lastPlacement = currentDis;
                    remaining = 0;

                    placementVector.addVec(v1.getPoint());

                    temp = insertVert(newV);
                    temp.setX(placementVector.getX());
                    temp.setY(placementVector.getY());
                    temp.gCoord = currentCoor;
                    temp.coord = currentCoor;

                    newV = temp;

                    numVertsInserted++;
                    currentCoor += CoorSpaceing;

                } else {
                    if (v2.isHead()) {
                        break; // stop it douplicating the head node if it also
                               // an intpoint
                    }
                    if (v2.isIntPoint()) {
                        temp = insertVert(newV);
                        temp.setX(v2.getX());
                        temp.setY(v2.getY());
                        temp.setIntPoint(true, v2.intsectID);
                        newV = temp;
                    }
                    remaining = edge.length() - lastPlacement + remaining;
                    break;
                }
            }

            v1 = v1.getNext();
        } while (!v1.isHead());
        oldHead = null;
        // LOGGER.trace("head =[" + getHead().getX() + "," + getHead().getY() + "]");
    }

    public void setResolutionN(double numVerts) {
        // evenly spaces a new vertices around the ouline by following vectors
        // between verts
        double length = getLength();
        // int numVerts = (int) Math.round((length / density)); // must be round
        // number of verts
        double density = length / numVerts; // re-cal the density

        double remaining = 0.;
        double lastPlacement, currentDis;

        Vert oldHead = head;
        Vert v1 = oldHead;

        // new, higher res outline
        head = new Vert(v1.getX(), v1.getY(), v1.getTrackNum());
        head.setHead(true);
        head.setIntPoint(oldHead.isIntPoint(), oldHead.intsectID);
        head.setNext(head);
        head.setPrev(head);

        head.gCoord = 0.;
        head.coord = 0.;

        nextTrackNumber = oldHead.getTrackNum() + 1;
        POINTS = 1;

        double CoorSpaceing = 1.0 / numVerts;
        // System.out.println("coord: " + CoorSpaceing);
        double currentCoor = CoorSpaceing;

        Vert temp, newV;
        newV = head;

        int numVertsInserted = 1; // the head
        ExtendedVector2d uEdge, edge, placementVector;
        do {
            Vert v2 = v1.getNext();
            lastPlacement = 0.0;
            currentDis = 0.0;
            edge = ExtendedVector2d.vecP2P(v1.getPoint(), v2.getPoint());
            // edge.print("edge");
            uEdge = ExtendedVector2d.unitVector(v1.getPoint(), v2.getPoint());
            // uEdge.print("uEdge");
            if (edge.length() == 0) { // points on top of one another, move on
                v1 = v1.getNext();
                continue;
            }

            while (true) {
                placementVector = new ExtendedVector2d(uEdge.getX(), uEdge.getY());
                // double mult = (density + currentDis) - remaining;
                // System.out.println("mult "+mult);
                // placementVector.print("before Mult");
                placementVector.multiply((density + currentDis) - remaining);

                // placementVector.print("\tafter Mult");

                if (placementVector.length() <= edge.length() && numVertsInserted < numVerts) { // if
                    currentDis = placementVector.length();
                    lastPlacement = currentDis;
                    remaining = 0;

                    placementVector.addVec(v1.getPoint());

                    temp = insertVert(newV);
                    temp.setX(placementVector.getX());
                    temp.setY(placementVector.getY());
                    temp.gCoord = currentCoor;
                    temp.coord = currentCoor;

                    newV = temp;

                    numVertsInserted++;
                    currentCoor += CoorSpaceing;

                } else {
                    if (v2.isHead()) {
                        break; // stop it douplicating the head node if it also
                               // an intpoint
                    }
                    if (v2.isIntPoint()) {
                        temp = insertVert(newV);
                        temp.setX(v2.getX());
                        temp.setY(v2.getY());
                        temp.setIntPoint(true, v2.intsectID);
                        newV = temp;
                    }
                    remaining = edge.length() - lastPlacement + remaining;
                    break;
                }
            }

            v1 = v1.getNext();
        } while (!v1.isHead());
        oldHead = null;
    }

    public double calcVolume() {
        double sum;
        sum = 0.0;
        Vert n = head;
        Vert np1 = n.getNext();
        do {
            sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
            n = n.getNext();
            np1 = n.getNext(); // note: n is reset on prev line

        } while (!n.isHead());
        return 0.5 * sum;
    }

    public double calcArea() {
        double area, sum;
        sum = 0.0;
        Vert n = head;
        Vert np1 = n.getNext();
        do {
            sum += (n.getX() * np1.getY()) - (np1.getX() * n.getY());
            n = n.getNext();
            np1 = n.getNext(); // note: n is reset on prev line

        } while (!n.isHead());
        area = 0.5 * sum;
        return area;
    }

    public static Vert getNextIntersect(Vert v) {
        do {
            v = v.getNext();
        } while (!v.isIntPoint()); // move to next int point
        return v;
    }

    public static Vert findIntersect(Vert v, int id) {
        int count = 0; // debug
        do {
            if (v.isIntPoint() && v.intsectID == id) {
                break;
            }
            v = v.getNext();
            if (count++ > 2000) {
                System.out.println("Outline->findIntersect - search exceeded 2000");
                break;
            }
        } while (true);

        return v;
    }

    public static int distBetweenInts(Vert intA, Vert intB) {
        int d = 0;
        do {
            d++;
            intA = intA.getNext();
            if (d > 2000) { // debug
                System.out.println("Outline:distBetween->search exceeded 2000");
                break;
            }
        } while (intA.intsectID != intB.intsectID);
        return d;
    }

    public static int invertsBetween(Vert intA, Vert intB) {
        int i = 0;
        int count = 0;
        do {
            intA = intA.getNext();
            if (intA.isIntPoint() && intA.intState > 2) {
                i++;
            }
            if (intA.isIntPoint() && intA.intState == 1) {
                i--;
            }
            if (count++ > 2000) { // debug
                System.out.println("Outline:invertsBetween. search exceeded 2000");
                break;
            }
        } while (intA.intsectID != intB.intsectID);
        return i;
    }

    public void correctDensity(double max, double min) {
        double dist;
        Vert v = head;

        do {
            dist = ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());

            if (dist < min) {
                removeVert(v.getNext());
            } else if (dist > max) {
                this.insertInterpolatedVert(v);
            } else {
                v = v.getNext();
            }
        } while (!v.isHead());
        // LOGGER.trace("head =[" + getHead().getX() + "," + getHead().getY() + "]");
    }

    /**
     * Done once at the end of each frame to cut out any parts of the contour that self intersect.
     * 
     * Similar to cutLoops, but check all edges (interval up to NODES/2) and cuts out the smallest
     * section
     * 
     * @return \c true if cut something
     * @see uk.ac.warwick.wsbc.QuimP.Snake.cutLoops()
     * @see uk.ac.warwick.wsbc.QuimP.Snake.cutIntersects()
     */
    public boolean cutSelfIntersects() {
        boolean iCut = false;
        int interval, state;// , c;

        Vert nA, nB;
        double[] intersect = new double[2];
        Vert newN;

        boolean cutHead;

        nA = head;
        do {
            cutHead = (nA.getNext().isHead()) ? true : false;
            nB = nA.getNext().getNext(); // don't check the next one along! they touch, not overlap
            interval = (POINTS > 6) ? POINTS / 2 : 2; // always leave 3 nodes, at least.
                                                      // Check half way around
            for (int i = 2; i < interval; i++) {
                if (nB.isHead()) {
                    cutHead = true;
                }
                intersect = new double[2];
                state = ExtendedVector2d.segmentIntersection(nA.getX(), nA.getY(),
                        nA.getNext().getX(), nA.getNext().getY(), nB.getX(), nB.getY(),
                        nB.getNext().getX(), nB.getNext().getY(), intersect);

                if (state == 1) {
                    iCut = true;
                    newN = this.insertInterpolatedVert(nA);
                    newN.setX(intersect[0]);
                    newN.setY(intersect[1]);

                    newN.setNext(nB.getNext());
                    nB.getNext().setPrev(newN);

                    newN.updateNormale(true);
                    nB.getNext().updateNormale(true);

                    if (cutHead) {
                        // System.out.println("cut the head");
                        newN.setHead(true); // put a new head in
                        head = newN;
                    }

                    // newN.print("inserted node: ");
                    // System.out.println("C - VERTS : " + VERTS);
                    if (POINTS - (i) < 3)
                        LOGGER.warn(
                                "OUTLINE 594_VERTS WILL BE than 3. i = " + i + ", VERT=" + POINTS);
                    POINTS -= (i);
                    break;
                }
                nB = nB.getNext();
            }
            nA = nA.getNext();
        } while (!nA.isHead());

        return iCut;
    }

    /**
     * Remove really small edges that cause numerical inaccuracy when checking for self intersects.
     * 
     * Tends to happen when migrating edges get pushed together
     * 
     * @return \c true if deleted something
     */
    public boolean removeNanoEdges() {
        double nano = 0.1;
        double length;
        boolean deleted = false;

        Vert nA, nB;

        nA = head;
        do {
            do {
                nB = nA.getNext();
                length = ExtendedVector2d.lengthP2P(nA.getPoint(), nB.getPoint());
                if (length < nano) {
                    this.removeVert(nB);
                    deleted = true;
                }
            } while (length < nano);

            nA = nA.getNext();
        } while (!nA.isHead());

        return deleted;
    }

    /**
     * Set head node coord to zero. Make closest landing to zero the head node
     * 
     * Prevents circulating of the zero coord
     */
    public void coordReset() {
        Vert vFirst = findFirstNode('g'); // get first node in terms of fcoord (origin)
        head.setHead(false);
        head = vFirst;
        head.setHead(true);

        double length = getLength();
        double d = 0.;

        Vert v = head;
        do {
            v.coord = d / length;
            d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            v = v.getNext();
        } while (!v.isHead());
    }

    public void resetAllCoords() {
        double length = getLength();
        double d = 0.;

        Vert v = head;
        do {
            v.coord = d / length;
            v.gCoord = v.coord;
            v.fCoord = v.coord;
            d = d + ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            v = v.getNext();
        } while (!v.isHead());
        // LOGGER.trace("head =[" + getHead().getX() + "," + getHead().getY() + "]");
    }

    public Object clone() {
        // clone the outline
        Vert oV = head;

        Vert nV = new Vert(oV.getX(), oV.getY(), oV.getTrackNum()); // head node
        nV.coord = oV.coord;
        nV.fCoord = oV.fCoord;
        nV.gCoord = oV.gCoord;
        nV.distance = oV.distance;
        // nV.fluores = oV.cloneFluo();
        nV.setFluores(oV.fluores);

        Outline n = new Outline(nV);

        oV = oV.getNext();
        do {
            nV = n.insertVert(nV);
            nV.setX(oV.getX());
            nV.setY(oV.getY());
            nV.coord = oV.coord;
            nV.fCoord = oV.fCoord;
            nV.gCoord = oV.gCoord;
            nV.distance = oV.distance;
            // nV.fluores = oV.cloneFluo();
            nV.setFluores(oV.fluores);
            nV.setTrackNum(oV.getTrackNum());

            oV = oV.getNext();
        } while (!oV.isHead());
        n.updateNormales(true);
        n.calcCentroid();
        n.updateCurvature();

        return n;
    }

    public boolean checkCoordErrors() {
        Vert v = head;

        do {
            // check for errors in gCoord and fCoord
            if (v.gCoord >= 1 || v.gCoord < 0 || v.fCoord >= 1 || v.fCoord < 0 || v.coord >= 1
                    || v.coord < 0) {
                System.out.println("Outline587: Errors in tracking Coordinates\n\t" + "coord="
                        + v.coord + ", gCoord= " + v.gCoord + ", fCoord = " + v.fCoord);
                return true;
            }

            v = v.getNext();
        } while (!v.isHead());

        return false;

    }

    public Vert findFirstNode(char c) {
        // find the first node in terms of coord (c) or fcoord (f)
        // ie closest to zero

        Vert v = head;
        Vert vFirst = v;

        double coord, coordPrev;
        double dis, disFirst = 0;

        do {
            // coord = (c == 'f') ? v.fCoord : v.coord;
            // coordPrev = (c == 'f') ? v.getPrev().fCoord : v.getPrev().coord;
            if (c == 'f') {
                coord = v.fCoord;
                coordPrev = v.getPrev().fCoord;
            } else if (c == 'g') {
                coord = v.gCoord;
                coordPrev = v.getPrev().gCoord;
            } else {
                coord = v.coord;
                coordPrev = v.getPrev().coord;
            }

            dis = Math.abs(coord - coordPrev);
            // System.out.println("abs( " + coord + "-"+coordPrev+") = " + dis);

            if (dis > disFirst) {
                vFirst = v;
                disFirst = dis;
                // System.out.println("\tchoosen ");
                // vFirst.print();
            }

            v = v.getNext();
        } while (!v.isHead());

        // System.out.println("First "+c+"Coord vert: ");
        // vFirst.print();

        return vFirst;

    }

    void clearFluores() {
        Vert v = head;
        do {
            v.setFluoresChannel(-2, -2, -2, 0);
            v.setFluoresChannel(-2, -2, -2, 1);
            v.setFluoresChannel(-2, -2, -2, 2);
            v = v.getNext();
        } while (!v.isHead());
    }

    void setHeadclosest(ExtendedVector2d pHead) {
        double dis, curDis;
        Vert v = head;
        Vert closestV = head;
        curDis = ExtendedVector2d.lengthP2P(pHead, v.getPoint());

        do {
            dis = ExtendedVector2d.lengthP2P(pHead, v.getPoint());
            if (dis < curDis) {
                curDis = dis;
                closestV = v;
            }
            v = v.getNext();
        } while (!v.isHead());

        if (closestV.isHead())
            return;

        head.setHead(false);
        closestV.setHead(true);
        head = closestV;
    }

    // Vert findCoordPoint(double addAt) {

    // Vert v = findCoordEdge(addAt);

    // }

    Vert findCoordEdge(double a) {
        Vert v = head;
        do {

            if (v.coord < a && v.coord > a) {
                return v;
            }

            if (v.coord > a && v.getPrev().coord > a && v.getNext().coord > a
                    && v.getNext().getNext().coord > a) {
                return v;
            }

            if (v.coord < a && v.getPrev().coord < a && v.getNext().coord < a
                    && v.getNext().getNext().coord < a) {
                return v;
            }

            v = v.getNext();
        } while (!v.isHead());
        return head;
    }
}
