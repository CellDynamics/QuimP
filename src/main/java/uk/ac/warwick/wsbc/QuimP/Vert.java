
package uk.ac.warwick.wsbc.QuimP;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Represents a vertex in the outline Contains several methods that operate on
 * vertexes and vectors.
 * 
 * @author rtyson
 * @author p.baniukiewicz
 */
public class Vert extends PointsList<Vert> {
    public double charge; /*!< charge on the vertex */
    public double distance; /*!< distance vert migrated (actually converted to speed by Tool.speedToScale */
    final FluoMeasurement[] fluores = new FluoMeasurement[3]; /*!< fluorescence channels 1-3 */
    public double curvatureLocal; /*!< curvature local to a node */
    public double curvatureSmoothed; /*!< smoothed curvature */
    public double curvatureSum; /*!< summed curvature over x microns this is the value recorded into maps */
    public double coord; /*!< co-ord relative to head node on current frame */
    public double fCoord; /*!< coor relative to coord on previous frame */
    public double fLandCoord; /*!< landing relative to previous frame */
    public double gCoord; /*!< global co-ord relative to head node on frame 1; */
    public double gLandCoord; /*!< landing co-cord relative to head node on frame 1; */
    public double tarLandingCoord;
    public QColor color; /*!< color of Vert */
    private boolean intPoint; /*!< vert represents an intersect point and is temporary. Mark start end of sectors */
    public boolean snapped; /*!< the vert has been snapped to an edge */

    public int intsectID;

    /**
     * Internal state
     * -# 0 - undetermined
     * -# 1 - forms valid sector
     * -# 2 - LOOSE sector
     * -# 3 - forms inverted sector
     * -# 4 inverted and loose
     */
    public int intState;

    /**
     * Default constructor, creates Vert element with ID=1
     */
    public Vert() {
        super();
        vertInitializer();
    }

    /**
     * Create Vert element with given ID
     * 
     * @param t ID of Vert
     */
    public Vert(int t) {
        super(t);
        vertInitializer();
    }

    /**
     * Create Vert from {x,y} coordinates
     * 
     * @param xx x-axis coordinate
     * @param yy y-axis coordinate
     * @param t id of Vert
     * @see uk.ac.warwick.wsbc.QuimP.PointListNode.PointListNode(double, double, int)
     */
    public Vert(double xx, double yy, int t) {
        super(xx, yy, t);
        vertInitializer();
    }

    /**
     * Copy constructor. Copy properties of Vert
     * 
     * Previous or next points are not copied
     * 
     * @param src Source Vert
     * @todo TODO To implement
     */
    public Vert(final Vert src) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Initializers for Vert object
     */
    private void vertInitializer() {
        intPoint = false;
        intState = 0;
        fCoord = -1;
        gCoord = -1;
        color = new QColor(1, 0, 0);
        for (int i = 0; i < 3; i++)
            fluores[i] = new FluoMeasurement(-2, -2, -2);
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

    public boolean isIntPoint() {
        return intPoint;
    }

    public void setIntPoint(boolean t, int i) {
        intPoint = t;
        intsectID = i;
        tracknumber = -1;
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