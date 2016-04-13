
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Represents a vertex in the outline Contains several methods that operate on
 * vertexes and vectors.
 * 
 * @author rtyson
 */
public class Vert {
    private ExtendedVector2d point; // x,y co-ordinates of the node
    private ExtendedVector2d normal; // normals
    private ExtendedVector2d tan;
    public double charge; // charge on the vertex
    public double distance; // distance vert migrated (actually converted to speed by
                            // Tool.speedToScale
    final FluoMeasurement[] fluores; // fluorescence channels 1-3. Intensity and location
    // public double curvatureOLD;
    // public double convexityOLD; // curvature, but may be a smoothed value
    public double curvatureLocal; // curvature local to a node
    public double curvatureSmoothed; // smoothed curvature
    public double curvatureSum; // summed curvature over x microns
                                // this is the value recorded into maps

    private int tracknumber;
    public boolean frozen;

    public double coord; // co-ord relative to head node on current frame
    public double fCoord; // coor relative to coord on previous frame
    public double fLandCoord; // landing relative to previous frame
    public double gCoord; // global co-ord relative to head node on frame 1;
    public double gLandCoord; // landing co-cord relative to head node on frame
                              // 1;
    public double tarLandingCoord;

    public QColor color;

    private Vert prev;
    private Vert next;
    private boolean head;
    private boolean intPoint; // vert represents an intersect point and is
                              // temporary. Mark start end of sectors
    public boolean snapped; // the vert has been snapped to an edge

    public int intsectID;
    public int intState;; // 0 - undetermined; 1-forms valid sector;
                          // 2-LOOSE sector; 3-forms inverted sector; 4-inverted
                          // and loose

    private static boolean clockwise = true; // access clockwise if true

    public Vert(int t) {
        // t = tracking number
        point = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
        head = false;
        intPoint = false;
        intState = 0;
        tracknumber = t;
        frozen = false;
        fCoord = -1;
        gCoord = -1;

        color = new QColor(1, 0, 0);

        fluores = new FluoMeasurement[3];
        for (int i = 0; i < 3; i++) {
            fluores[i] = new FluoMeasurement(-2, -2, -2);
        }
    }

    public Vert(double xx, double yy, int t) {
        point = new ExtendedVector2d(xx, yy);
        tracknumber = t;
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
        head = false;
        intPoint = false;
        intState = 0;
        gCoord = -1;
        fCoord = -1;

        color = new QColor(1, 0, 0);

        fluores = new FluoMeasurement[3];
        for (int i = 0; i < 3; i++) {
            fluores[i] = new FluoMeasurement(-2, -2, -2);
        }
    }

    public void print(String s) {
        System.out.print(s + "vert: " + tracknumber + ", x:" + getX() + ", y:" + getY()
                + ", coord: " + coord + ", fCoord: " + fCoord + ", gCoord: " + gCoord);
        if (intPoint)
            System.out.print(", isIntPoint (" + intsectID + ")");
        if (head)
            System.out.print(", Head");
        System.out.println("");
    }

    public double getX() {
        // get X space co-ordinate
        return point.getX();
    }

    public double getY() {
        // get X space co-ordinate
        return point.getY();
    }

    public void setX(double x) {
        // set X space co-ordinate
        point.setX(x);
    }

    public void setY(double y) {
        // set X space co-ordinate
        point.setY(y);
    }

    public Vert getPrev() {
        // get next node in chain (prev if not clockwise)
        if (clockwise) {
            return prev;
        } else {
            return next;
        }
    }

    public Vert getNext() {
        // get prev node in chain (next if not clockwise)
        if (clockwise) {
            return next;
        } else {
            return prev;
        }
    }

    public void setPrev(Vert n) {
        if (clockwise) {
            prev = n;
        } else {
            next = n;
        }
    }

    public void setNext(Vert n) {
        if (clockwise) {
            next = n;
        } else {
            prev = n;
        }
    }

    public static void setClockwise(boolean b) {
        Vert.clockwise = b;
    }

    public ExtendedVector2d getPoint() {
        return point;
    }

    public ExtendedVector2d getNormal() {
        return normal;
    }

    public void setNormal(double x, double y) {
        normal.setX(x);
        normal.setY(y);
    }
    //

    public ExtendedVector2d getTangent() {
        return tan;
    }

    public int getTrackNum() {
        return tracknumber;
    }

    public void setTrackNum(int b) {
        tracknumber = b;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isIntPoint() {
        return intPoint;
    }

    public void setHead(boolean t) {
        head = t;
    }

    public void setIntPoint(boolean t, int i) {
        intPoint = t;
        intsectID = i;
        tracknumber = -1;
    }

    public void updateNormale(boolean inner) {
        // updates the normal (must point inwards)
        clockwise = true; // just in case
        tan = calcTan(); // tangent

        // inner norma X = -ve Y, Y = +ve X
        // inner norma X = +ve Y, Y = -ve X //

        if (!inner) { // switch around if expanding snake
            normal.setX(-tan.getY());
            normal.setY(tan.getX());
        } else {
            normal.setX(tan.getY());
            normal.setY(-tan.getX());
        }
    }

    private ExtendedVector2d calcTan() {
        // calulate tangent at Vert n (i.e. unit vector between neighbours)
        // calc a unit vector towards neighbouring nodes and then a unit vec
        // between their ends
        // direction important for normale calculation. Always calc tan as if
        // clockwise

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

    public void setLandingCoord(ExtendedVector2d p, Vert edge) {
        Vert edge2 = edge.getNext(); // 'edge' is the 1st vert of an edge

        while (edge.isIntPoint()) {
            edge = edge.getPrev();
        }
        while (edge2.isIntPoint()) {
            edge2 = edge2.getNext();
        }

        // relative position of landing
        double d1 = ExtendedVector2d.lengthP2P(edge.point, edge2.point);
        double d2 = ExtendedVector2d.lengthP2P(edge.point, p);
        double prop = d2 / d1;

        gLandCoord = calcLanding(edge.gCoord, edge2.gCoord, prop);
        fLandCoord = calcLanding(edge.coord, edge2.coord, prop);

        if (gLandCoord >= 1 || gLandCoord < 0 || fLandCoord >= 1 || fLandCoord < 0) {
            System.out.println("Vert253:setLandingCoord-Error in landing coord\n\t" + "gLandCoord= "
                    + gLandCoord + ", fLandCoord = " + fLandCoord);

        }

        /*
         * double d1 = Vec2d.lengthP2P(edge.point, edge2.point); double d2 =
         * Vec2d.lengthP2P(edge.point, p); double prop = d2 / d1;
         * 
         * double edge2coord = edge2.coord; // if wrapping around if
         * (edge2.coord < edge.coord) { edge2coord += 1; }
         * 
         * double coordDist = edge2coord - edge.coord; landingCoor = edge.coord
         * + (prop * coordDist);
         * 
         * if (landingCoor >= 1) { //if wrapped around and passed 1 landingCoor
         * -= 1; }
         * 
         * if (landingCoor < 0 || landingCoor > 1 || Double.isNaN(landingCoor))
         * { IJ.write("ERROR 1: NAN or < landing coord ("+IJ.d2s(landingCoor)+
         * ")< 0; e1=" + edge.coord + ", e2=" + edge2.coord + ", prop" + prop);
         * }
         */
    }

    private double calcLanding(double coordA, double coordB, double prop) {
        double landing;

        if (coordB < coordA) {
            coordB += 1;
        }

        double coordDist = coordB - coordA;
        landing = coordA + (prop * coordDist);

        if (landing >= 1) { // if wrapped around and passed 1
            landing -= 1;
        }

        if (landing < 0 || landing >= 1 || Double.isNaN(landing)) {
            System.out.println(
                    "Vert295:calcLanding ERROR in landing coord:\n\t" + "landing:" + IJ.d2s(landing)
                            + ", coordA=" + coordA + ", coorB=" + coordB + ", proportion=" + prop);
        }
        return landing;
    }

    public void calcCurvatureLocal() {

        ExtendedVector2d edge1 =
                ExtendedVector2d.vecP2P(this.getPoint(), this.getPrev().getPoint());
        ExtendedVector2d edge2 =
                ExtendedVector2d.vecP2P(this.getPoint(), this.getNext().getPoint());

        double angle = ExtendedVector2d.angle(edge1, edge2) * (180 / Math.PI); // convert
                                                                               // to
                                                                               // degrees

        if (angle > 360 || angle < -360) {
            System.out.println("Warning-angle out of range (Vert l:320)");
        }

        if (angle < 0)
            angle = 360 + angle;

        if (angle == 180) {
            curvatureLocal = 0;
        } else if (angle < 180) {
            curvatureLocal = -1 * (1 - (angle / 180));
        } else {
            curvatureLocal = (angle - 180) / 180;
        }

    }

    // FluoMeasurement[] cloneFluo(){
    // FluoMeasurement[] fluoNew = new FluoMeasurement[3];
    // for(int i = 0; i < 3; i++){
    // fluoNew[i] = fluores[i].copy();
    // }
    // return fluoNew;
    // }

    void setFluoresChannel(FluoMeasurement m, int channel) {
        fluores[channel].intensity = m.intensity;
        fluores[channel].x = m.x;
        fluores[channel].y = m.y;
    }

    void setFluoresChannel(int x, int y, int i, int channel) {
        fluores[channel].intensity = i;
        fluores[channel].x = x;
        fluores[channel].y = y;
    }

    void setFluores(FluoMeasurement[] m) {

        for (int i = 0; i < 3; i++) {
            fluores[i].intensity = m[i].intensity;
            fluores[i].x = m[i].x;
            fluores[i].y = m[i].y;
        }
    }

    static double disCoord2Coord(double a, double b) {
        if (a < b)
            return b - a;
        if (b < a)
            return (1 - a) + b;
        else
            return 0;
    }

    static double addCoords(double a, double b) {
        double r = a + b;
        if (r >= 1)
            r = r - 1;
        return r;
    }

}