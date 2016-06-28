/**
 * @file SegmentedShapeRoi.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import java.awt.Shape;
import java.util.List;

import javax.vecmath.Point2d;

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
 * @see uk.ac.warwick.wsbc.QuimP.FakeSegmentation
 *
 */
public class SegmentedShapeRoi extends ShapeRoi {
    public static final int NOT_COUNTED = -1; //!< Code for not counted yet shape

    protected int id = NOT_COUNTED; //!< positive if has any id assigned (thus it has been counted already)
    protected int frame = 0; //!< frame number where this outline was found
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param r
     */
    public SegmentedShapeRoi(Roi r) {
        super(r);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param s
     */
    public SegmentedShapeRoi(Shape s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param shapeArray
     */
    public SegmentedShapeRoi(float[] shapeArray) {
        super(shapeArray);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param x
     * @param y
     * @param s
     */
    public SegmentedShapeRoi(int x, int y, Shape s) {
        super(x, y, s);
        // TODO Auto-generated constructor stub
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
     * @param step step - step during conversion outline to points. For 1 every point from outline
     * is included in output list
     * @param smooth \a true for using smoothing during interpolation 
     * @return List of List of ROIs
     */
    public List<Point2d> getOutlineasPoints(double step, boolean smooth) {
        List<Point2d> ret;
        FloatPolygon fp;
        fp = getInterpolatedPolygon(step, smooth);
        ret = new QuimpDataConverter(fp.xpoints, fp.ypoints).getList();
        return ret;
    }

}
