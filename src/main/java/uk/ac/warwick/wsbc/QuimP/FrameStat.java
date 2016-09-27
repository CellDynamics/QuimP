package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Hold statistic evaluated for one frame.
 * 
 * @author p.baniukiewicz
 * @see {@link uk.ac.warwick.wsbc.QuimP.CellStat} 
 */
public class FrameStat {
    public double area;
    // double totalFlour;
    // double meanFlour;
    public ExtendedVector2d centroid;
    public double elongation;
    public double circularity;
    public double perimiter;
    public double displacement;
    public double dist;
    public double persistance;
    public double speed; // over 1 frame
    public double persistanceToSource;
    public double dispersion;
    public double extension;
    // int cellAge;

    public FrameStat() {
        centroid = new ExtendedVector2d();
    }

    public void toScale(double scale, double frameInterval) {
        area = Tool.areaToScale(area, scale);
        perimiter = Tool.distanceToScale(perimiter, scale);
        displacement = Tool.distanceToScale(displacement, scale);
        dist = Tool.distanceToScale(dist, scale);
        speed = Tool.speedToScale(speed, scale, frameInterval); // over 1 frame
    }

    void centroidToPixels(double scale) {
        centroid.setXY(centroid.getX() / scale, centroid.getY() / scale);
    }
}