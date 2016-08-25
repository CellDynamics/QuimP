/**
 * @file ProtrusionVis.java
 * @date 19 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point2i;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import uk.ac.warwick.wsbc.QuimP.GraphicsElements;
import uk.ac.warwick.wsbc.QuimP.STmap;

/**
 * @author p.baniukiewicz
 *
 */
public class ProtrusionVis {
    private static final Logger LOGGER = LogManager.getLogger(ProtrusionVis.class.getName());
    private ImagePlus originalImage;
    private Overlay overlay;

    public ProtrusionVis() {
        originalImage = null;
    }

    /**
     * @param qP 
     * 
     */
    public ProtrusionVis(ImagePlus originalImage) {
        this.originalImage = originalImage;
        overlay = originalImage.getOverlay(); // check for existing overlay
        if (overlay == null) // create new if no present
            overlay = new Overlay();

    }

    /**
     * Plot maxima found by {@link MaximaFinder} on current image.
     * 
     * @param mapCell 
     * @param mF properly initialized {@link MaximaFinder} object.
     */
    public void addMaximaToImage(STmap mapCell, MaximaFinder mF) {

        LOGGER.trace("Num of slices: " + originalImage.getStackSize());
        LOGGER.trace(mF.getMaxima());
        LOGGER.trace(mapCell);
        Polygon max = mF.getMaxima();
        double x[][] = mapCell.getxMap();
        double y[][] = mapCell.getyMap();
        int[] indexes = max.xpoints;
        int[] frames = max.ypoints;

        LOGGER.trace("Frames:" + Arrays.toString(frames));
        LOGGER.trace("Indexe:" + Arrays.toString(indexes));
        for (int n = 0; n < max.npoints; n++) {

            double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
            double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
            plotCircle(xcoord, ycoord, frames[n] + 1, Color.MAGENTA, 10);
        }
        originalImage.setOverlay(overlay); // add to image
    }

    /**
     * 
     * @param mapCell
     * @param pL
     */
    public void addTrackingLinesToImage(STmap mapCell, List<PolygonRoi> pL) {
        double x[][] = mapCell.getxMap();
        double y[][] = mapCell.getyMap();
        for (PolygonRoi pR : pL) {
            List<Point2i> plR = PolygonRoi2Point2i(new ArrayList<PolygonRoi>(Arrays.asList(pR)));
            Collections.sort(plR, new ListPoint2iComparator());
            List<PolygonRoi> plRsorted = Point2i2PolygonRoi(plR); // again polygon but sorted along
                                                                  // frames
            float xcoord[] = new float[plRsorted.get(0).getNCoordinates()];
            float ycoord[] = new float[plRsorted.get(0).getNCoordinates()];
            for (int f = 0; f < plRsorted.get(0).getNCoordinates(); f++) {
                if (plRsorted.get(0).getPolygon().ypoints[f] < 0
                        || plRsorted.get(0).getPolygon().xpoints[f] < 0)
                    continue;
                xcoord[f] = (float) x[plRsorted.get(0).getPolygon().ypoints[f]][plRsorted.get(0)
                        .getPolygon().xpoints[f]];
                ycoord[f] = (float) y[plRsorted.get(0).getPolygon().ypoints[f]][plRsorted.get(0)
                        .getPolygon().xpoints[f]];
            }
            for (int f = 0; f < plRsorted.get(0).getNCoordinates(); f++) {
                PolygonRoi pRoi = new PolygonRoi(xcoord, ycoord, f + 1, Roi.FREELINE);
                pRoi.setPosition((int) plRsorted.get(0).getPolygon().ypoints[f] + 1);
                pRoi.setStrokeColor(Color.GREEN);
                pRoi.setFillColor(Color.GREEN);
                overlay.add(pRoi);
            }

        }
        // List<Point2i> plL = PolygonRoi2Point2i(pL); // convert to flat list of points
        // // go through all frames and check whether there is any point to draw
        // for (int f = 1; f <= originalImage.getStackSize(); f++) {
        // final int c = f;
        // List<Point2i> result =
        // plL.stream().filter(e -> e.getX() == c).collect(Collectors.toList());
        // if (result.isEmpty())
        // continue;
        // for (Point2i p : result) {
        // double xcoord = x[p.x][p.y]; // screen coordinate of
        // double ycoord = y[p.x][p.y]; // (frame,index) point
        // plotPoint(xcoord, ycoord, p.y, Color.GREEN);
        // }
        //
        // }
        originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot filled circle on overlay.
     * 
     * @param x
     * @param y
     * @param frame
     * @param color
     * @param radius
     */
    private void plotCircle(double x, double y, int frame, Color color, double radius) {
        // create ROI
        PolygonRoi oR = new PolygonRoi(
                GraphicsElements.plotCircle(new Point2d(x, y), (float) radius), Roi.POLYGON);
        // set z-position of ROI!!!
        oR.setPosition(frame);
        oR.setStrokeColor(color);
        oR.setFillColor(color);
        overlay.add(oR); // add to collection of overlays
    }

    /**
     * 
     * @param x
     * @param y
     * @param frame
     * @param color
     */
    private void plotPoint(double x, double y, int frame, Color color) {
        PointRoi pR = new PointRoi(x, y);
        pR.setPosition(frame);
        pR.setStrokeColor(color);
        pR.setFillColor(color);
        overlay.add(pR); // add to collection of overlays
    }

    /**
     * Convert list of Polygons to list of Points.
     * <p>
     * The difference is that for polygons points are kept in 1d arrays, whereas for Point2i they
     * are as separate points that allows object comparison.
     *  
     * @param list List of polygons to convert
     * @return List of points constructed from all polygons.
     */
    private List<Point2i> PolygonRoi2Point2i(List<PolygonRoi> list) {
        List<Point2i> ret = new ArrayList<>();
        for (PolygonRoi p : list) { // every polygon
            Polygon pl = p.getPolygon();
            for (int i = 0; i < pl.npoints; i++) // every point in it
                ret.add(new Point2i(pl.xpoints[i], pl.ypoints[i]));
        }
        return ret;
    }

    /**
     * Convert list of Points to list of Polygons.
     * <p>
     * The difference is that for polygons points are kept in 1d arrays, whereas for Point2i they
     * are as separate points that allows object comparison.
     *  
     * @param list List of points to convert
     * @return Polygon constructed from all points. This is 1-element list.
     */
    private List<PolygonRoi> Point2i2PolygonRoi(List<Point2i> list) {
        List<PolygonRoi> ret = new ArrayList<>();
        Polygon pl = new Polygon();
        for (Point2i p : list) { // every point
            pl.addPoint(p.getX(), p.getY());
        }
        ret.add(new PolygonRoi(pl, Roi.POLYLINE));
        return ret;

    }

    /**
     * @return the originalImage
     */
    public ImagePlus getOriginalImage() {
        return originalImage;
    }

}

/**
 * Compare Point2i objects along frames (y-coordinate).
 * 
 * @author p.baniukiewicz
 *
 */
class ListPoint2iComparator implements Comparator<Point2i> {

    @Override
    public int compare(Point2i o1, Point2i o2) {
        if (o1.getY() < o2.getY())
            return -1;
        if (o1.getY() > o2.getY())
            return 1;
        else
            return 0;
    }

}
