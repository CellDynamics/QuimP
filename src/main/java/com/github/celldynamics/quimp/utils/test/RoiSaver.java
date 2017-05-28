package com.github.celldynamics.quimp.utils.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QColor;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * Helper class to export shapes as *.tif images
 * 
 * @author p.baniukiewicz
 *
 */
public class RoiSaver {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(RoiSaver.class.getName());

  /**
   * Dummy constructor.
   */
  public RoiSaver() {
  }

  /**
   * Save ROI as image
   * 
   * <p>Get ListArray with vertices and create fileName.tif image with ROI For non-valid input list
   * it creates red image of size 100 x 100
   * 
   * @param fileName file to save image with path
   * @param vert list of vertices
   */
  public static void saveRoi(String fileName, List<Point2d> vert) {
    try {
      double[] bb;
      float[] x = new float[vert.size()];
      float[] y = new float[vert.size()];
      int l = 0;
      // copy to arrays
      for (Point2d el : vert) {
        x[l] = (float) el.getX();
        y[l] = (float) el.getY();
        l++;
      }
      bb = getBoundingBox(vert); // get size of output image
      PolygonRoi pp = new PolygonRoi(x, y, Roi.POLYGON); // create polygon object
      LOGGER.debug("Creating image of size [" + (int) Math.round(bb[0]) + ","
              + (int) Math.round(bb[1]) + "]");
      ImagePlus outputImage = IJ.createImage("", (int) Math.round(bb[0] + 0.2 * bb[0]),
              (int) Math.round(bb[1] + 0.2 * bb[1]), 1, 8); // output // margins
      ImageProcessor ip = outputImage.getProcessor(); // get processor required later
      ip.setColor(Color.WHITE); // set pen
      pp.setLocation(0.1 * bb[0], 0.1 * bb[1]); // move slightly ROI to center
      pp.drawPixels(ip); // draw roi
      IJ.saveAsTiff(outputImage, fileName); // save image
      LOGGER.debug("Saved as: " + fileName);
    } catch (Exception e) {
      ImagePlus outputImage = IJ.createImage("", 100, 100, 1, 24);
      ImageProcessor ip = outputImage.getProcessor();
      ip.setColor(Color.RED);
      ip.fill();
      IJ.saveAsTiff(outputImage, fileName); // save image
      LOGGER.error(e.getMessage());
    }

  }

  /**
   * Save ROI as image.
   * 
   * @param fileName fileName
   * @param roi roi
   * @see com.github.celldynamics.quimp.utils.test.RoiSaver#saveRois(ImagePlus, String, ArrayList)
   */
  public static void saveRoi(String fileName, Roi roi) {
    if (roi == null) {
      saveRoi(fileName, (List<Point2d>) null);
      return;
    }
    FloatPolygon fp;
    fp = roi.getFloatPolygon(); // save common part
    saveRoi(fileName, new QuimpDataConverter(fp.xpoints, fp.ypoints).getList());
  }

  /**
   * Create stack from List of Rois
   * 
   * @param image Image where rois will be plotted. Number of slices must be equal to rois.size();
   * @param fileName File to save
   * @param ret List of Lists of Rois. First level of rois is plotted on slices, second contains
   *        rois to plot. Rois along second level are plotted with the same color across slices
   *        e.g. First roi in second level in red, second roi in second level ble etc
   */
  public static void saveRois(ImagePlus image, String fileName,
          ArrayList<ArrayList<SegmentedShapeRoi>> ret) {
    ImagePlus cp = image.duplicate();
    new ImageConverter(cp).convertToRGB();
    for (ArrayList<? extends ShapeRoi> al : ret) {
      QColor qcolor = QColor.lightColor();
      Color color = new Color(qcolor.getColorInt());
      for (int i = 0; i < al.size(); i++) {
        ImageProcessor currentP = cp.getImageStack().getProcessor(i + 1);
        currentP.setColor(color);
        currentP.setLineWidth(2);
        al.get(i).drawPixels(currentP); // TODO catch OutOfBounds exception to skip missing slices
      }
    }
    IJ.saveAsTiff(cp, fileName); // save image
  }

  /**
   * Calculates width and height of bounding box for shape defined as List of Vector2d
   * elements.
   * 
   * @param vert List of vertexes of shape
   * @return two elements array where [width height]
   */
  private static double[] getBoundingBox(List<Point2d> vert) {
    double minx = vert.get(0).getX();
    double maxx = minx;
    double miny = vert.get(0).getY();
    double maxy = miny;
    double[] out = new double[2];
    for (Point2d el : vert) {
      if (el.getX() > maxx) {
        maxx = el.getX();
      }
      if (el.getX() < minx) {
        minx = el.getX();
      }
      if (el.getY() > maxy) {
        maxy = el.getY();
      }
      if (el.getY() < miny) {
        miny = el.getY();
      }
    }
    out[0] = Math.abs(maxx - minx);
    out[1] = Math.abs(maxy - miny);
    return out;
  }
}