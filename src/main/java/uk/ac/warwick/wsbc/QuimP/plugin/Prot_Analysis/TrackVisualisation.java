package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.tools.javac.util.Pair;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.GraphicsElements;
import uk.ac.warwick.wsbc.QuimP.STmap;

/**
 * Support various methods of visualising protrusion data.
 * <p>
 * In general all plots are added to image used to construct this object as overlay layer.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class TrackVisualisation {
    private static final Logger LOGGER = LogManager.getLogger(TrackVisualisation.class.getName());
    /**
     * Definition of colors used to plot:
     * <ol>
     * <li> index 0 - backtracked position of point
     * <li> index 1 - forwardtracked position of point.
     * </ol>
     */
    public Color[] color = { Color.YELLOW, Color.GREEN };
    protected ImagePlus originalImage; // reference of image to be plotted on
    protected Overlay overlay;

    /**
     * Create correct object.
     * 
     * If input image contains any overlay data, they will be extended by new plots.
     * 
     * @param qP Image to be plotted on. 
     */
    protected TrackVisualisation(ImagePlus originalImage) {
        this.originalImage = originalImage;
        LOGGER.trace("Num of slices: " + originalImage.getStackSize());
        overlay = originalImage.getOverlay(); // check for existing overlay
        if (overlay == null) // create new if no present
            overlay = new Overlay();

    }

    /**
     * Construct object from raw ImageProcessor.
     * 
     * @param name Name of the image
     * @param imp 
     */
    protected TrackVisualisation(String name, ImageProcessor imp) {
        this(new ImagePlus(name, imp));
    }

    /**
     * Generate filled circle.
     * 
     * @param x
     * @param y
     * @param frame
     * @param color
     * @param radius
     */
    public PolygonRoi getCircle(double x, double y, Color color, double radius) {
        // create ROI
        PolygonRoi oR = new PolygonRoi(
                GraphicsElements.plotCircle(new Point2d(x, y), (float) radius), Roi.POLYGON);
        // set z-position of ROI!!!
        oR.setStrokeColor(color);
        oR.setFillColor(color);
        return oR;
    }

    /**
     * 
     * @param x
     * @param y
     * @param frame
     * @param color
     */
    public PointRoi getPoint(double x, double y, Color color) {
        PointRoi pR = new PointRoi(x, y);
        pR.setStrokeColor(color);
        pR.setFillColor(color);
        return pR;
    }

    /**
     * @return the originalImage
     */
    public ImagePlus getOriginalImage() {
        return originalImage;
    }

    /**
     *  Subclass for plotting on single image in coord space [x,y] (or [outline,frame]
     * 
     * @author baniuk
     *
     */
    static class Single extends TrackVisualisation {

        public Single(ImagePlus originalImage) {
            super(originalImage);
        }

        public Single(String name, ImageProcessor imp) {
            super(name, imp);
        }

        /**
         * Create object from raw data like e.g. motility map. 
         * 
         * @param name Name of the image
         * @param data 2D data. They will be rotated to match Matlab representation.
         * @ see uk.ac.warwick.wsbc.QuimP.STmap
         */
        public Single(String name, float[][] data) {
            super(name, new FloatProcessor(data));
            ImageProcessor imp = originalImage.getProcessor();
            imp = imp.rotateRight();
            imp.flipHorizontal();
            originalImage.setProcessor(imp);
        }

        /**
         * Plot maxima found by {@link MaximaFinder} on current image.
         * 
         * @param mF properly initialized {@link MaximaFinder} object.
         */
        public void addMaximaToImage(MaximaFinder mF) {
            Polygon max = mF.getMaxima();
            PointRoi pR = new PointRoi(max.xpoints, max.ypoints, max.xpoints.length);
            overlay.add(pR);
            originalImage.setOverlay(overlay);
        }

        /**
         * Add lines defined as polygons to image.
         * 
         * @param pL Lines to add
         */
        public void addTrackingLinesToImage(List<Polygon> pL) {
            for (Polygon p : pL) {
                overlay.add(new PolygonRoi(p, Roi.FREELINE));
            }
            originalImage.setOverlay(overlay);
        }

    }

    /**
     * Subclass for plotting on stacks in coord space [x,y,f].
     * 
     * @author baniuk
     *
     */
    static class Stack extends TrackVisualisation {

        public Stack(ImagePlus originalImage) {
            super(originalImage);
        }

        /**
         * @param name
         * @param imp
         */
        public Stack(String name, ImageProcessor imp) {
            super(name, imp);
            // TODO Auto-generated constructor stub
        }

        /**
         * Plot unrelated points on image (stack).
         * 
         * @param mapCell source of coordinate maps
         * @param points list of points to plot in coordinates (index,frame)
         * @param color color of point
         * @param radius radius of point
         */
        public void addCirclesToImage(STmap mapCell, Polygon points, Color color, double radius) {
            double x[][] = mapCell.getxMap();
            double y[][] = mapCell.getyMap();
            int[] indexes = points.xpoints;
            int[] frames = points.ypoints;

            // LOGGER.trace("Frames:" + Arrays.toString(frames));
            // LOGGER.trace("Indexe:" + Arrays.toString(indexes));
            for (int n = 0; n < points.npoints; n++) {
                // decode frame,outline to screen coordinates
                if (frames[n] < 0 || indexes[n] < 0)
                    continue;
                double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
                double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
                plotCircle(xcoord, ycoord, frames[n] + 1, color, radius);
            }
            originalImage.setOverlay(overlay); // add to image
        }

        /**
         * Plot unrelated points on image (stack). Input compatible with
         * {@link PointTracker.getIntersectionParents(List<Polygon>, int)}.
         * 
         * @param mapCell source of coordinate maps
         * @param points list of points to plot in coordinates (index,frame)
         * @param color color of point
         * @param radius radius of point
         */
        public void addCirclesToImage(STmap mapCell, List<Pair<Point, Point>> points, Color color,
                double radius) {
            int[] x = new int[points.size()];
            int[] y = new int[points.size()];
            int l = 0;
            for (Pair<Point, Point> p : points) {
                x[l] = p.snd.x;
                y[l] = p.snd.y;
                l++;
            }
            Polygon poly = new Polygon(x, y, points.size());
            addCirclesToImage(mapCell, poly, color, radius);
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
                List<Point> plR =
                        PointTracker.Polygon2Point2i(new ArrayList<Polygon>(Arrays.asList(pR)));
                // then sort this list according y-coordinate (frame)
                Collections.sort(plR, new ListPoint2iComparator());
                // convert to polygon again but now it is sorted along frames
                Polygon plRsorted = PointTracker.Point2i2Polygon(plR);
                // create store for tracking line coordinates
                xcoorda.add(new float[plRsorted.npoints]);
                ycoorda.add(new float[plRsorted.npoints]);
                // counter of invalid vertexes. According to TrackMap#trackForward last points can
                // be -1 when user provided longer time span than available. (last in term of time)
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
                // iterate over points in sorted polygon (one track line) even indexes stand for
                // backward tracking, odd for forward tracking lines Some last points can be skipped
                // here (sorting does not influence this because last points means last in term of
                // time)
                for (int f = 0; f < plRsorted.npoints - invalidVertex; f++) {
                    // x/ycoorda keep all points of tracking lines but PolygonRoi constructor allow
                    // to define how many first of them we take. This allows us to add points
                    // together with frames - in result the line grows as frames rise. After
                    // sorting, first points are those on lower frames
                    PolygonRoi pRoi =
                            new PolygonRoi(xcoorda.get(aL), ycoorda.get(aL), f + 1, Roi.FREELINE);
                    // set where we want plot f+1 points from x/ycoorda
                    pRoi.setPosition((int) plRsorted.ypoints[f] + 1);
                    // set colors (remember about backward/forward order)
                    pRoi.setStrokeColor(color[aL % 2]);
                    pRoi.setFillColor(color[aL % 2]);
                    overlay.add(pRoi);
                    // If there is maximum on x frame and we plotted backward line from x-n to x, we
                    // wont to keep it during plotting forward tracking from x to x+z frames. So
                    // this whole line is plotted on every x-x+z frame
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
         * Plot maxima found by {@link MaximaFinder} on current image.
         * 
         * @param mapCell map related to given cell.
         * @param mF properly initialized {@link MaximaFinder} object.
         */
        public void addMaximaToImage(STmap mapCell, MaximaFinder mF) {
            Polygon max = mF.getMaxima();
            addCirclesToImage(mapCell, max, Color.MAGENTA, 7);
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
        public void plotCircle(double x, double y, int frame, Color color, double radius) {
            // create ROI
            PolygonRoi oR = getCircle(x, y, color, radius);
            // set z-position of ROI!!!
            oR.setPosition(frame);
            overlay.add(oR); // add to collection of overlays
        }
    }
}

/**
 * Compare Point2i objects along frames (y-coordinate).
 * 
 * @author p.baniukiewicz
 *
 */
class ListPoint2iComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.y < o2.y)
            return -1;
        if (o1.y > o2.y)
            return 1;
        else
            return 0;
    }

}
