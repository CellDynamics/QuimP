
package uk.ac.warwick.wsbc.QuimP;

import java.util.Arrays;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Represents a vertex in the outline. Contains several methods that operate on
 * vertexes and vectors.
 * Properties defined for Vertex are updated by different QuimP modules (ECMM, QA)
 * 
 * @author rtyson
 * @author p.baniukiewicz
 */
public class Vert extends PointsList<Vert> {
    public double charge; /*!< charge on the vertex */
    /**
     * distance vert migrated (actually converted to speed by Tool.speedToScale
     */
    public double distance;
    final FluoMeasurement[] fluores = new FluoMeasurement[3]; /*!< fluorescence channels 1-3 */
    /**
     * curvature local to a node
     * Updated by uk.ac.warwick.wsbc.QuimP.Vert.calcCurvatureLocal() and implicitly by 
     * uk.ac.warwick.wsbc.QuimP.Vert.calcCurvatureLocal() called during creation and serialization.
     */
    public double curvatureLocal;
    /**
     * smoothed curvature
     * Updated during map generation (Q Analysis) by uk.ac.warwick.wsbc.QuimP.STmap.calcCurvature()
     * or uk.ac.warwick.wsbc.QuimP.STmap.averageCurvature(Outline)
     */
    public double curvatureSmoothed;
    /**
     * summed curvature over x microns this is the value recorded into maps
     * Updated during map generation (Q Analysis) by uk.ac.warwick.wsbc.QuimP.STmap.calcCurvature()
     */
    public double curvatureSum;
    /**
     * coord relative to head node on current frame
     * Set during ECMM
     */
    public double coord;
    /**
     * coord relative to coord on previous frame
     * Set by uk.ac.warwick.wsbc.QuimP.Mapping.migrate() and during changing resolution in ECMM
     */
    public double fCoord;
    /**
     * landing relative to previous frame
     * Set by uk.ac.warwick.wsbc.QuimP.Vert.setLandingCoord(ExtendedVector2d, Vert) called on solving
     * ECMM equations
     */
    public double fLandCoord;
    /**
     * global coord relative to head node on frame 1
     * Set by uk.ac.warwick.wsbc.QuimP.Mapping.migrate() and during changing resolution  in ECMM
     */
    public double gCoord;
    /**
     * landing coord relative to head node on frame 1
     * Set by uk.ac.warwick.wsbc.QuimP.Vert.setLandingCoord(ExtendedVector2d, Vert) called on solving
     * ECMM equations
     */
    public double gLandCoord;
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
     * -# 4 - inverted and loose
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
     */
    public Vert(final Vert src) {
        super(src);
        charge = src.charge;
        distance = src.distance;
        for (int i = 0; i < 3; i++)
            fluores[i] = new FluoMeasurement(src.fluores[i]);
        curvatureLocal = src.curvatureLocal;
        curvatureSmoothed = src.curvatureSmoothed;
        curvatureSum = src.curvatureSum;
        coord = src.coord;
        fCoord = src.fCoord;
        fLandCoord = src.fLandCoord;
        gCoord = src.gCoord;
        gLandCoord = src.gLandCoord;
        tarLandingCoord = src.tarLandingCoord;
        color = new QColor(src.color);
        intPoint = src.intPoint;
        snapped = src.snapped;
        intsectID = src.intsectID;
        intState = src.intState;
    }

    /**
     * Conversion constructor
     * @param src Node to convert to Vert
     */
    public Vert(final Node src) {
        super(src);
        vertInitializer(); // default params that are not passed from Vert
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

    /**
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(charge);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        temp = Double.doubleToLongBits(coord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(curvatureLocal);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(curvatureSmoothed);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(curvatureSum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(fCoord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(fLandCoord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Arrays.hashCode(fluores);
        temp = Double.doubleToLongBits(gCoord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(gLandCoord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (intPoint ? 1231 : 1237);
        result = prime * result + intState;
        result = prime * result + intsectID;
        result = prime * result + (snapped ? 1231 : 1237);
        temp = Double.doubleToLongBits(tarLandingCoord);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Vert))
            return false;
        Vert other = (Vert) obj;
        if (Double.doubleToLongBits(charge) != Double.doubleToLongBits(other.charge))
            return false;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (Double.doubleToLongBits(coord) != Double.doubleToLongBits(other.coord))
            return false;
        if (Double.doubleToLongBits(curvatureLocal) != Double
                .doubleToLongBits(other.curvatureLocal))
            return false;
        if (Double.doubleToLongBits(curvatureSmoothed) != Double
                .doubleToLongBits(other.curvatureSmoothed))
            return false;
        if (Double.doubleToLongBits(curvatureSum) != Double.doubleToLongBits(other.curvatureSum))
            return false;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
            return false;
        if (Double.doubleToLongBits(fCoord) != Double.doubleToLongBits(other.fCoord))
            return false;
        if (Double.doubleToLongBits(fLandCoord) != Double.doubleToLongBits(other.fLandCoord))
            return false;
        if (!Arrays.equals(fluores, other.fluores))
            return false;
        if (Double.doubleToLongBits(gCoord) != Double.doubleToLongBits(other.gCoord))
            return false;
        if (Double.doubleToLongBits(gLandCoord) != Double.doubleToLongBits(other.gLandCoord))
            return false;
        if (intPoint != other.intPoint)
            return false;
        if (intState != other.intState)
            return false;
        if (intsectID != other.intsectID)
            return false;
        if (snapped != other.snapped)
            return false;
        if (Double.doubleToLongBits(tarLandingCoord) != Double
                .doubleToLongBits(other.tarLandingCoord))
            return false;
        return true;
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

    /**
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Vert [charge=" + charge + ", distance=" + distance + ", fluores="
                + Arrays.toString(fluores) + ", curvatureLocal=" + curvatureLocal
                + ", curvatureSmoothed=" + curvatureSmoothed + ", curvatureSum=" + curvatureSum
                + ", coord=" + coord + ", fCoord=" + fCoord + ", fLandCoord=" + fLandCoord
                + ", gCoord=" + gCoord + ", gLandCoord=" + gLandCoord + ", tarLandingCoord="
                + tarLandingCoord + ", color=" + color + ", intPoint=" + intPoint + ", snapped="
                + snapped + ", intsectID=" + intsectID + ", intState=" + intState + ", point="
                + point + ", tracknumber=" + tracknumber + ", position=" + position + "]";
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