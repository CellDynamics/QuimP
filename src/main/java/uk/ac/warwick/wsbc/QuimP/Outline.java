/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Polygon;

import ij.IJ;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 *
 * @author tyson
 */
public final class Outline extends Shape<Vert> implements Cloneable {

    QColor color;

    public Outline(Vert h, int N) {
        super(h, N);
        removeVert(head);
        this.updateCurvature();

        color = new QColor(0.5, 0, 1);
    }

    public Outline(Vert h) {
        // blank outline
        super(h);
        this.updateCurvature();

        color = new QColor(0.5, 0, 1);
    }

    public Outline(Roi roi) {
        // Create an outline from an Roi
        // int[] xCoords = ((PolygonRoi)roi).getXCoordinates();
        // int[] yCoords = ((PolygonRoi)roi).getYCoordinates();
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
    }

    public void print() {
        IJ.log("Print verts (" + super.getNumPoints() + ")");
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
        if (i != super.getNumPoints()) {
            IJ.log("VERTS and linked list dont tally!!");
        }
    }

    public void plotOutline(String title, boolean per) {
        double[] xArr = new double[super.getNumPoints()]; // arrays to hold x and y co-ords
        // (duplicate last to join)
        double[] yArr = new double[super.getNumPoints()];

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

    public int getVerts() {
        return super.getNumPoints();
    }

    public int countVERTS() {
        Vert v = head;
        int c = 0;
        do {
            c++;
            v = v.getNext();
        } while (!v.isHead());

        return c;

    }

    public void removeVert(Vert v) {
        // removes node n and links neighbours together
        if (super.getNumPoints() <= 3) {
            System.out.println("Outline. 175. Can't remove node. less than 3 would remain");
            return;
        }

        super.removePoint(v, true);
        if (super.getNumPoints() < 3) {
            IJ.error("Outline.199. WARNING! Nodes less then 3");
        }

    }

    public void updateNormales(boolean inner) {
        // update all node normales
        Vert v = head;
        do {
            v.updateNormale(inner);
            v = v.getNext();
        } while (!v.isHead());
    }

    public void updateCurvature() {
        Vert v = head;
        do {
            v.calcCurvatureLocal();
            v = v.getNext();
        } while (!v.isHead());
    }

    public Vert insertVert(Vert v) {
        return insertPoint(v, new Vert());
    }

    public Vert insertInterpolatedVert(Vert v) {
        // insert a vert in-between v and v.next with interpolated values
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

    public Polygon asPolygon() {
        Polygon pol = new Polygon();
        Vert v = head;

        do {
            pol.addPoint((int) v.getX(), (int) v.getY());
            v = v.getNext();
        } while (!v.isHead());

        return pol;
    }

    public double[] XasArr() {
        double[] arry = new double[super.getNumPoints()];

        Vert v = head;
        int i = 0;
        do {
            arry[i] = v.getX();

            i++;
            v = v.getNext();
        } while (!v.isHead());
        return arry;
    }

    public double[] YasArr() {
        double[] arry = new double[super.getNumPoints()];

        Vert v = head;
        int i = 0;
        do {

            arry[i] = v.getY();
            i++;
            v = v.getNext();
        } while (!v.isHead());
        return arry;
    }

    public void setResolution(double density) {
        // evenly spaces a new vertices around the ouline by following vectors
        // between verts
        double length = getLength();
        int numVerts = (int) Math.round((length / density)); // must be round
                                                             // number of verts
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

    public ExtendedVector2d getCentroid() {
        ExtendedVector2d centroid = new ExtendedVector2d(0, 0);
        Vert v = head;
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
        return centroid;
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
                // IJ.write("removed Vert, dist: " + dist);
            } else if (dist > max) {
                this.insertInterpolatedVert(v);
                // v = v.getNext();
            } else {
                v = v.getNext();
            }
        } while (!v.isHead());
    }

    public boolean cutSelfIntersects() {
        // done once at the end of each frame to cut out any parts of the
        // contour
        // that self intersect.
        // Simular to cutLoops, but cheack all edges (interval up to NODES/2)
        // and cuts out the smallest section

        boolean iCut = false;
        int interval, state;// , c;

        Vert nA, nB;
        double[] intersect = new double[2];
        Vert newN;

        boolean cutHead;

        nA = head;
        do {
            cutHead = (nA.getNext().isHead()) ? true : false;
            nB = nA.getNext().getNext(); // don't check the next one along! they
                                         // touch, not overlap
            interval = (super.getNumPoints() > 6) ? super.getNumPoints() / 2 : 2; // always leave 3
                                                                                  // nodes, at
            // least. Check half way
            // around

            for (int i = 2; i < interval; i++) {

                if (nB.isHead()) {
                    cutHead = true;
                }
                intersect = new double[2];
                state = ExtendedVector2d.segmentIntersection(nA.getPoint().getX(),
                        nA.getPoint().getY(), nA.getNext().getPoint().getX(),
                        nA.getNext().getPoint().getY(), nB.getPoint().getX(), nB.getPoint().getY(),
                        nB.getNext().getPoint().getX(), nB.getNext().getPoint().getY(), intersect);
                /*
                 * if (state == -1) { System.out.println("\nLines parallel");
                 * System.out.println("close all;plot([" + nA.getX() + "," +
                 * nA.getNext().getX() + "],[" + nA.getY() + "," +
                 * nA.getNext().getY() + "],'-ob');"); // matlab output
                 * System.out.println("hold on; plot([" + nB.getX() + "," +
                 * nB.getNext().getX() + "],[" + nB.getY() + "," +
                 * nB.getNext().getY() + "],'-or');");
                 * 
                 * } else if (state == -2) { System.out.println(
                 * "\nLines parallel and overlap"); System.out.println(
                 * "close all;plot([" + nA.getX() + "," + nA.getNext().getX() +
                 * "],[" + nA.getY() + "," + nA.getNext().getY() + "],'-ob');");
                 * // matlab output System.out.println("hold on; plot([" +
                 * nB.getX() + "," + nB.getNext().getX() + "],[" + nB.getY() +
                 * "," + nB.getNext().getY() + "],'-or');");
                 * System.out.println("plot(" + intersect[0] + "," +
                 * intersect[1] + ", 'og');");
                 *
                 */
                if (state == 1) {
                    // c= countVERTS();
                    // if(c !=VERTS){
                    // System.out.println("A -VERTS NOT CORREECT. VERTS:
                    // "+VERTS+", Count: " + c);
                    // }else{
                    // System.out.println("A -verts correct. VERTS: "+VERTS+",
                    // Count: " + c);
                    // }
                    // System.out.println("\nLines intersect at " + intersect[0]
                    // + ", " + intersect[1]);
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
                    if (super.getNumPoints() - (i) < 3) {
                        System.out.println("OUTLINE 594_VERTS WILL BE than 3. i = " + i + ", VERT="
                                + super.getNumPoints());
                    }

                    POINTS -= (i);
                    // if(VERTS<3) System.out.println("OUTLINE 596_VERTS LESS
                    // than 3");

                    // c = countVERTS();
                    // if(c !=VERTS){
                    // System.out.println("B - VERTS NOT CORREECT. VERTS:
                    // "+VERTS+", Count: " + c+ ", i: "+i);
                    // VERTS = c;
                    // }else{
                    // System.out.println("B- verts correct");
                    // }
                    break;
                }

                nB = nB.getNext();
            }
            nA = nA.getNext();
        } while (!nA.isHead());

        return iCut;
    }

    public boolean removeNanoEdges() {
        // remove really small edges that cause numerical inaccuracy when
        // checking for self intersects.
        // tends to happen when migrating edges get pushed together
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

    public double getLength() {
        // add up lengths between all verts
        Vert v = head;
        double length = 0.0;
        do {
            length += ExtendedVector2d.lengthP2P(v.getPoint(), v.getNext().getPoint());
            v = v.getNext();
        } while (!v.isHead());
        return length;
    }

    public void coordReset() {
        // set head node coord to zero. Make closest landing to zero the head
        // node
        // prevents circulating of the zero coord

        Vert vFirst = findFirstNode('g'); // get first node in terms of fcoord
                                          // (origin)
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

        return n;
    }

    public void unfreeze() {
        Vert v = head;
        do {
            v.frozen = false;
            v = v.getNext();
        } while (!v.isHead());
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

    Roi asRoi() {
        Polygon p = asPolygon();
        Roi r = new PolygonRoi(p, PolygonRoi.POLYGON);
        return r;
    }

    Roi asFloatRoi() {

        float[] x = new float[super.getNumPoints()];
        float[] y = new float[super.getNumPoints()];

        Vert n = head;
        int i = 0;
        do {
            x[i] = (float) n.getX();
            y[i] = (float) n.getY();
            i++;
            n = n.getNext();
        } while (!n.isHead());
        return new PolygonRoi(x, y, super.getNumPoints(), Roi.POLYGON);
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

    public void makeAntiClockwise() {

        double sum = 0;
        Vert v = head;
        do {
            sum += (v.getNext().getX() - v.getX()) * (v.getNext().getY() + v.getY());
            v = v.getNext();
        } while (!v.isHead());
        if (sum > 0) {
            // System.out.println("Warning. Was clockwise, reversed");
            this.reverseSnake();
            this.updateNormales(true);
        } else {
        }
    }

    public void reverseSnake() {
        // turn it back anti clockwise
        Vert tmp;
        Vert v = head;
        do {
            tmp = v.getNext();
            v.setNext(v.getPrev());
            v.setPrev(tmp);
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

    public void scale(double amount, double stepSize) {
        // make sure snake access is clockwise
        Node.setClockwise(true);
        // scale the snake by 'amount', in increments of 'stepsize'
        if (amount > 0) {
            stepSize *= -1; // scale down if amount negative
        }
        double steps = Math.abs(amount / stepSize);
        // IJ.log(""+steps);
        Vert n;
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
        }
    }
}
