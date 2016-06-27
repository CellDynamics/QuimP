package uk.ac.warwick.wsbc.QuimP.plugin.utils;

import java.awt.Color;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

/**
 * Helper class to export shapes as \a *.tif images
 * 
 * @author baniuk
 *
 */
public class RoiSaver {
    private static final Logger LOGGER = LogManager.getLogger(RoiSaver.class.getName());

    /**
     * Dummy constructor
     */
    public RoiSaver() {
    }

    /**
     * Save ROI as image
     * 
     * Get ListArray with vertices and create \a fileName.tif image with ROI For
     * non-valid input list it creates red image of size 100 x 100
     * 
     * @param fileName file to save image with path
     * @param vert list of vertices
     */
    public static void saveROI(String fileName, List<Point2d> vert) {
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
            PolygonRoi pR = new PolygonRoi(x, y, Roi.POLYGON); // create polygon
                                                               // object
            LOGGER.debug("Creating image of size [" + (int) Math.round(bb[0]) + ","
                    + (int) Math.round(bb[1]) + "]");
            ImagePlus outputImage = IJ.createImage("", (int) Math.round(bb[0] + 0.2 * bb[0]),
                    (int) Math.round(bb[1] + 0.2 * bb[1]), 1, 8); // output // margins
            ImageProcessor ip = outputImage.getProcessor(); // get processor
                                                            // required later
            ip.setColor(Color.WHITE); // set pen
            pR.setLocation(0.1 * bb[0], 0.1 * bb[1]); // move slightly ROI to
                                                      // center
            pR.drawPixels(ip); // draw roi
            IJ.saveAsTiff(outputImage, fileName); // save image
            LOGGER.debug("Saved as: " + fileName);
        } catch (Exception e) {
            ImagePlus outputImage = IJ.createImage("", 100, 100, 1, 24);
            ImageProcessor ip = outputImage.getProcessor();
            ip.setColor(Color.RED);
            ip.fill();
            IJ.saveAsTiff(outputImage, fileName); // save image
            LOGGER.error(e);
        }

    }

    /**
     * Save ROI as image
     * 
     * @param fileName
     * @param roi
     * @see uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver.saveROI(String, List<Point2d>) 
     */
    public static void saveROI(String fileName, Roi roi) {
        if (roi == null)
            saveROI(fileName, (List<Point2d>) null);
        FloatPolygon fp;
        fp = roi.getFloatPolygon(); // save common part
        saveROI(fileName, new QuimpDataConverter(fp.xpoints, fp.ypoints).getList());
    }

    /**
     * Calculates \b width and \b height of bounding box for shape defined as
     * List of Vector2d elements
     * 
     * @param vert
     * List of vertexes of shape
     * @return two elements array where [width height]
     * @retval double[2]
     */
    private static double[] getBoundingBox(List<Point2d> vert) {
        double minx = vert.get(0).getX();
        double maxx = minx;
        double miny = vert.get(0).getY();
        double maxy = miny;
        double[] out = new double[2];
        for (Point2d el : vert) {
            if (el.getX() > maxx)
                maxx = el.getX();
            if (el.getX() < minx)
                minx = el.getX();
            if (el.getY() > maxy)
                maxy = el.getY();
            if (el.getY() < miny)
                miny = el.getY();
        }
        out[0] = Math.abs(maxx - minx);
        out[1] = Math.abs(maxy - miny);
        return out;
    }
}