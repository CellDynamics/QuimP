package uk.ac.warwick.wsbc.quimp.geom;

import java.awt.Shape;
import java.util.List;

import org.scijava.vecmath.Point2d;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

/**
 * Add some fields indicating that this Shape has been included already in any Snake chain.
 * 
 * <p>Shapes among one chain have the same id. Chain id is set when following shape overlap current
 * one
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.quimp.geom.TrackOutline
 * @see uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentation
 *
 */
public class SegmentedShapeRoi extends ShapeRoi {

  /**
   * The Constant NOT_COUNTED.
   * 
   * <p>Code for not counted yet shape.
   */
  public static final int NOT_COUNTED = -1;

  /**
   * The id of the shape.
   * 
   * <p>positive if has any id assigned (thus it has been counted already)
   */
  protected int id = NOT_COUNTED;

  /**
   * Frame number where this outline was found.
   */
  protected int frame = 0;
  /**
   * Step during conversion outline to points. For 1 every point from outline is included in
   * output list
   */
  protected double step = 1;
  /**
   * true for using smoothing during interpolation.
   */
  protected boolean smooth = false;
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Construct object from ij.gui.Roi.
   * 
   * @param r Roi
   */
  public SegmentedShapeRoi(Roi r) {
    super(r);
  }

  /**
   * Construct object from java.awt.Shape.
   * 
   * @param s Shape
   */
  public SegmentedShapeRoi(Shape s) {
    super(s);
  }

  /**
   * Construct object from array.
   * 
   * @param shapeArray shapeArray
   * @see ij.gui.ShapeRoi#ShapeRoi(float[])
   */
  public SegmentedShapeRoi(float[] shapeArray) {
    super(shapeArray);
  }

  /**
   * Constructs a ShapeRoi from a Shape.
   * 
   * @param x x
   * @param y y
   * @param s s
   * @see ij.gui.ShapeRoi#ShapeRoi(int x, int y, Shape s)
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
   * Set Roi id.
   * 
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get Roi frame.
   * 
   * @return the frame
   */
  public int getFrame() {
    return frame;
  }

  /**
   * Set Roi frame.
   * 
   * @param frame the frame to set
   */
  public void setFrame(int frame) {
    this.frame = frame;
  }

  /**
   * Convert this ROI to list of points using smoothing and step.
   * 
   * <p>Use object parameters step, smooth that should be set before call this method.
   * 
   * @return List of List of ROIs
   * @see #getOutlineasRawPoints()
   */
  public List<Point2d> getOutlineasPoints() {
    List<Point2d> ret;
    FloatPolygon fp;
    PolygonRoi pr;
    // convert to PolygonRoi as it supports spline fitting
    pr = new PolygonRoi(getInterpolatedPolygon(step, false), Roi.FREEROI);
    if (smooth == true) { // fit spline
      pr.fitSpline();
      fp = pr.getFloatPolygon(); // get FloatPolygon to have access to x[],y[]
      ret = new QuimpDataConverter(fp.xpoints, fp.ypoints).getList(); // x[],y[] are fitted
    } else {
      fp = pr.getFloatPolygon();
      ret = new QuimpDataConverter(fp.xpoints, fp.ypoints).getList(); // x[],y[] not fitted
    }
    return ret;
  }

  /**
   * Convert this ROI to list of points without any interpolation.
   * 
   * @return List of List of ROIs
   * @see #getOutlineasPoints()
   */
  public List<Point2d> getOutlineasRawPoints() {
    FloatPolygon fp = this.getFloatPolygon();
    if (fp.xpoints != null && fp.ypoints != null) {
      return new QuimpDataConverter(fp.xpoints, fp.ypoints).getList();
    } else {
      return null;
    }
  }

  /**
   * Allow to set non-standard parameters used during conversion from outline (ROI) to list of
   * points.
   * 
   * <pre>
   * <code>
   * for (ArrayList&lt;SegmentedShapeRoi&gt; asS : ret) {
   *  for (SegmentedShapeRoi ss : asS) {
   *    ss.setInterpolationParameters(1, false);
   *  }
   * }
   * </code>
   * </pre>
   * 
   * @param step interpolation step
   * @param smooth true to use smoothing
   * 
   * @see #getOutlineasPoints()
   */
  public void setInterpolationParameters(double step, boolean smooth) {
    this.step = step;
    this.smooth = smooth;
  }

}
