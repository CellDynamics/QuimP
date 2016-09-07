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
 * Support various methods of visualizing protrusion data.
 * <p>
 * In general all plots are added to image used to construct this object as overlay layer.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtrusionVis {
    private static final Logger LOGGER = LogManager.getLogger(ProtrusionVis.class.getName());
    /**
     * Definition of colors used to plot:
     * <ol>
     * <li> index 0 - backtracked position of point
     * <li> index 1 - forwardtracked position of point.
     * </ol>
     */
    public Color[] color = { Color.YELLOW, Color.GREEN };
    private ImagePlus originalImage; // reference of image to be plotted on
    private Overlay overlay;

    /**
     * Create correct object.
     * 
     * If input image contains any overlay data, they will be extended by new plots.
     * 
     * @param qP Image to be plotted on. 
     */
    public ProtrusionVis(ImagePlus originalImage) {
        this.originalImage = originalImage;
        LOGGER.trace("Num of slices: " + originalImage.getStackSize());
        overlay = originalImage.getOverlay(); // check for existing overlay
        if (overlay == null) // create new if no present
            overlay = new Overlay();

    }

    /**
     * Plot maxima found by {@link MaximaFinder} on current image.
     * 
     * @param mapCell map related to given cell.
     * @param mF properly initialized {@link MaximaFinder} object.
     */
    public void addMaximaToImage(STmap mapCell, MaximaFinder mF) {
        Polygon max = mF.getMaxima();
        addPointsToImage(mapCell, max, Color.MAGENTA, 7);
    }

    /**
     * Plot tracking lines before and after maxima points (in term of frames).
     * <p>
     * First backward tracking lines are plotted then forward in two different colors. For given 
     * maximum first is plotted backward tracking frame by frame, then forward tracking. Backward
     * tracking is visible as long as forward tracking is plotted. Then both disappear.
     *  
     * @param mapCell map related to given cell.
     * @param pL List of polygons that keep coordinates of points of backward and forward tracks.
     * The polygons in <tt>pL</tt> list must be in alternating order: BM1,FM1,BM2,FM2,..., where
     * BMx is backward track for maximum point no.x and FMx is the forward track for maximum point 
     * no.x. This order is respected by {@link Prot_Analysis.trackMaxima(STmap, double, MaximaFinder)} 
     */
    public void addTrackingLinesToImage(STmap mapCell, List<Polygon> pL) {
        double x[][] = mapCell.getxMap(); // temporary x and y coordinates for given cell
        double y[][] = mapCell.getyMap();
        // these are raw coordinates of tracking lines extracted from List<PolygonRoi> pL
        ArrayList<float[]> xcoorda = new ArrayList<>();
        ArrayList<float[]> ycoorda = new ArrayList<>();
        int aL = 0;
        // iterate over polygons. A polygon stores one tracking line
        for (Polygon pR : pL) {
            // we need to sort tracking line points according to frames where they appear in
            // first convert poygon to list of Point2i object
            List<Point2i> plR = Polygon2Point2i(new ArrayList<Polygon>(Arrays.asList(pR)));
            // then sort this list according y-coordinate (frame)
            Collections.sort(plR, new ListPoint2iComparator());
            // convert to polygon again but now it is sorted along frames
            Polygon plRsorted = Point2i2Polygon(plR).get(0);
            // create store for tracking line coordinates
            xcoorda.add(new float[plRsorted.npoints]);
            ycoorda.add(new float[plRsorted.npoints]);
            // counter of invalid vertexes. According to TrackMap#trackForward last points can be -1
            // when user provided longer time span than available. (last in term of time)
            int invalidVertex = 0;
            // decode frame,outline to x,y
            for (int f = 0; f < plRsorted.npoints; f++) {
                // -1 stands for points that are outside of range - assured by TrackMap.class
                if (plRsorted.ypoints[f] < 0 || plRsorted.xpoints[f] < 0) {
                    invalidVertex++; // count bad points
                    continue;
                }
                xcoorda.get(aL)[f] = (float) x[plRsorted.ypoints[f]][plRsorted.xpoints[f]];
                ycoorda.get(aL)[f] = (float) y[plRsorted.ypoints[f]][plRsorted.xpoints[f]];
            }
            // now xcoorda,yccora keep coordinates of aL track, it is time to plot
            // iterate over points in sorted polygon (one track line)
            // even indexes stand for backward tracking, odd for forward tracking lines
            // Some last points can be skipped here (sorting does not influence this because
            // last points means last in term of time)
            for (int f = 0; f < plRsorted.npoints - invalidVertex; f++) {
                // x/ycoorda keep all points of tracking lines but PolygonRoi constructor allow
                // to define how many first of them we take. This allows us to add points together
                // with frames - in result the line grows as frames rise. After sorting, first
                // points are those on lower frames
                PolygonRoi pRoi =
                        new PolygonRoi(xcoorda.get(aL), ycoorda.get(aL), f + 1, Roi.FREELINE);
                // set where we want plot f+1 points from x/ycoorda
                pRoi.setPosition((int) plRsorted.ypoints[f] + 1);
                // set colors (remember about backward/forward order)
                pRoi.setStrokeColor(color[aL % 2]);
                pRoi.setFillColor(color[aL % 2]);
                overlay.add(pRoi);
                // If there is maximum on x frame and we plotted backward line from x-n to x, we
                // wont to keep it during plotting forward tracking from x to x+z frames. So this
                // whole line is plotted on every x-x+z frame
                if (aL % 2 == 1) {
                    PolygonRoi pRoi1 = new PolygonRoi(xcoorda.get(aL - 1), ycoorda.get(aL - 1),
                            xcoorda.get(aL - 1).length, Roi.FREELINE);
                    pRoi1.setPosition((int) plRsorted.ypoints[f] + 1);
                    pRoi1.setStrokeColor(color[aL % 2 - 1]);
                    pRoi1.setFillColor(color[aL % 2 - 1]);
                    overlay.add(pRoi1);
                }
            }
            aL++;
        }
        originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot unrelated points on image (stack).
     * 
     * @param mapCell source of coordinate maps
     * @param points list of points to plot in coordinates (index,frame)
     * @param color color of point
     * @param radius radius of point
     */
    public void addPointsToImage(STmap mapCell, Polygon points, Color color, double radius) {
        double x[][] = mapCell.getxMap();
        double y[][] = mapCell.getyMap();
        int[] indexes = points.xpoints;
        int[] frames = points.ypoints;

        LOGGER.trace("Frames:" + Arrays.toString(frames));
        LOGGER.trace("Indexe:" + Arrays.toString(indexes));
        for (int n = 0; n < points.npoints; n++) {
            // decode frame,outline to screen coordinates
            double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
            double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
            plotCircle(xcoord, ycoord, frames[n] + 1, color, radius);
        }
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
    private List<Point2i> Polygon2Point2i(List<Polygon> list) {
        List<Point2i> ret = new ArrayList<>();
        for (Polygon pl : list) { // every polygon
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
    private List<Polygon> Point2i2Polygon(List<Point2i> list) {
        List<Polygon> ret = new ArrayList<>();
        Polygon pl = new Polygon();
        for (Point2i p : list) { // every point
            pl.addPoint(p.getX(), p.getY());
        }
        ret.add(pl);
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
