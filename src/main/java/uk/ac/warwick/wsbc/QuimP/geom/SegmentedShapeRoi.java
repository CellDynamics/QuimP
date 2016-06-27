/**
 * @file SegmentedShapeRoi.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import java.awt.Shape;

import ij.gui.Roi;
import ij.gui.ShapeRoi;

/**
 * Add one field indicating that this Shape has been included already in any Snake chain and it can be
 * skipped
 * 
 * Shapes among one chain have the same id. Chain id defined when following shape overlap current
 * one 
 * 
 * @author p.baniukiewicz
 * @date 27 Jun 2016
 *
 */
public class SegmentedShapeRoi extends ShapeRoi {
    public static final int NOT_COUNTED = -1; //!< Code for not counted yet shape

    public int id = NOT_COUNTED; //!< positive if has any id assigned (thus it has been counted already)
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

}
