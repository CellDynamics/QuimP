/**
 * @file SegmentedShapeRoi.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import java.awt.Shape;
import java.util.List;

import javax.vecmath.Point2d;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter;

/**
 * Add some fields indicating that this Shape has been included already in any Snake chain 
 * 
 * Shapes among one chain have the same id. Chain id is set when following shape overlap current
 * one 
 * 
 * @author p.baniukiewicz
 * @date 27 Jun 2016
 * @see uk.ac.warwick.wsbc.QuimP.geom.TrackOutline
 * @see uk.ac.warwick.wsbc.QuimP.BinarySegmentation
 *
 */
public class SegmentedShapeRoi extends ShapeRoi {
    public static final int NOT_COUNTED = -1; //!< Code for not counted yet shape

    protected int id = NOT_COUNTED; //!< positive if has any id assigned (thus it has been counted already)
    protected int frame = 0; //!< frame number where this outline was found
    /**
     * step during conversion outline to points. For 1 every point from outline
     * is included in output list
     */
    protected double step = 1;
    /**
     * \a true for using smoothing during interpolation
     */
    protected boolean smooth = false;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param r
     */
    public SegmentedShapeRoi(Roi r) {
        super(r);
    }

    /**
     * @param s
     */
    public SegmentedShapeRoi(Shape s) {
        super(s);
    }

    /**
     * @param shapeArray
     */
    public SegmentedShapeRoi(float[] shapeArray) {
        super(shapeArray);
    }

    /**
     * @param x
     * @param y
     * @param s
     */
    public SegmentedShapeRoi(int x, int y, Shape s) {
        super(x, y, s);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the frame
     */
    public int getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(int frame) {
        this.frame = frame;
    }

    /**
     * Convert this ROI to list of points using smoothing and step
     * 
     * Use object parameters \a step, \a smooth that should be set before call this method
     *  
     * @return List of List of ROIs
     */
    public List<Point2d> getOutlineasPoints() {
        List<Point2d> ret;
        FloatPolygon fp;
        PolygonRoi pR;
        // convert to PolygonRoi as it supports spline fitting
        pR = new PolygonRoi(getInterpolatedPolygon(step, false), Roi.FREEROI);
        if (smooth == true) { // fit spline
            pR.fitSpline();
            fp = pR.getFloatPolygon(); // get FloatPolygon to have access to x[],y[]
            ret = new QuimpDataConverter(fp.xpoints, fp.ypoints).getList(); // x[],y[] are fitted
        } else {
            fp = pR.getFloatPolygon();
            ret = new QuimpDataConverter(fp.xpoints, fp.ypoints).getList(); // x[],y[] not fitted
        }
        return ret;
    }

    /**
     * Allow to set non-standard parameters used during conversion from outline (ROI) to list of
     * points
     * 
     * @param step
     * @param smooth
     * 
     * @see getOutlineasPoints()
     */
    public void setInterpolationParameters(double step, boolean smooth) {
        this.step = step;
        this.smooth = smooth;
    }

}
