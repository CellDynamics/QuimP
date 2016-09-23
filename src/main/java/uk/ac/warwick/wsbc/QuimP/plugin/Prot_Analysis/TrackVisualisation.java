package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.tools.javac.util.Pair;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.utils.graphics.GraphicsElements;

/**
 * Support various methods of visualising protrusion data.
 * <p>
 * In general all plots are added to image used to construct this object as overlay layer.
 * <p>
 * This super class contains methods for creating static plots in [x,y] domain from coordinates
 * in [outline,frame] system (native for maps generated by Qanalysis).
 * <p>
 * <p><b>Warning</b><p>
 * It is assumed that plotted maps have frames on x-axis and indexes on y-axis
 * @author p.baniukiewicz
 *
 */
public abstract class TrackVisualisation {
    private static final Logger LOGGER = LogManager.getLogger(TrackVisualisation.class.getName());
    /**
     * Color for maxima points.
     */
    static public Color MAXIMA_COLOR = Color.MAGENTA;
    /**
     * Definition of colors used to plot tracks:
     * <ol>
     * <li> index 0 - backtracked position of point
     * <li> index 1 - forwardtracked position of point.
     * <li> index 2 - other
     * </ol>
     */
    static public Color[] color = { Color.YELLOW, Color.GREEN, Color.WHITE };
    protected ImagePlus originalImage; // reference of image to be plotted on
    protected Overlay overlay;

    /**
     * Create correct object.
     * 
     * If input image contains any overlay data, they will be extended by new plots.
     * 
     * @param qP Image to be plotted on. 
     */
    public TrackVisualisation(ImagePlus originalImage) {
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
    public TrackVisualisation(String name, ImageProcessor imp) {
        this(new ImagePlus(name, imp));
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
    public void plotCircle(double x, double y, Color color, double radius) {
        // create ROI
        PolygonRoi oR = GraphicsElements.getCircle(x, y, color, radius);
        overlay.add(oR); // add to collection of overlays
    }

    /**
     * @return the originalImage
     */
    public ImagePlus getOriginalImage() {
        return originalImage;
    }

    /**
     * Helper method.
     * 
     * Allows to convert enum to index of array of Colors.
     * 
     * @param type
     * @return Color from color array
     */
    protected Color getColor(Track track) {
        Color c;
        Track.Type type = track.type;
        switch (type) {
            case FORWARD:
                c = color[1];
                break;
            case BACKWARD:
                c = color[0];
                break;
            case OTHER:
                c = color[2];
                break;
            default:
                throw new IllegalArgumentException("Color not supported");
        }
        return c;
    }

    /**
     * Subclass for plotting on single image in coord space [outline,frame]
     * 
     * @author baniuk
     *
     */
    static class Map extends TrackVisualisation {

        public Map(ImagePlus originalImage) {
            super(originalImage);
        }

        public Map(String name, ImageProcessor imp) {
            super(name, imp);
        }

        /**
         * Create object from raw data like e.g. motility map. 
         * 
         * @param name Name of the image
         * @param data 2D data. They will be rotated to match Matlab representation.
         * @ see uk.ac.warwick.wsbc.QuimP.STmap
         */
        public Map(String name, float[][] data) {
            super(name, new FloatProcessor(data));
            ImageProcessor imp = originalImage.getProcessor();
            // can no be rotated here!!
            // imp = imp.rotateRight();
            // imp.flipHorizontal();
            // imp.resetRoi();
            // originalImage.setProcessor(imp);
        }

        /**
         * Plot unrelated points on image (static).
         * 
         * @param points list of points to plot in coordinates (index,frame)
         * @param color color of point
         * @param radius radius of point
         */
        public void addCirclesToImage(Polygon points, Color color, double radius) {
            int[] indexes = points.ypoints;
            int[] frames = points.xpoints;
            for (int n = 0; n < points.npoints; n++) {
                // decode frame,outline to screen coordinates
                if (frames[n] < 0 || indexes[n] < 0)
                    continue;
                plotCircle(frames[n], indexes[n], color, radius);
            }
            originalImage.setOverlay(overlay); // add to image
        }

        /**
         * Plot maxima found by {@link MaximaFinder} on current image.
         * 
         * @param mF properly initialized {@link MaximaFinder} object.
         */
        public void addMaximaToImage(MaximaFinder mF) {
            Polygon max = mF.getMaxima();
            PointRoi pR = GraphicsElements.getPoint(max.xpoints, max.ypoints,
                    TrackVisualisation.MAXIMA_COLOR);
            overlay.add(pR);
            originalImage.setOverlay(overlay);
        }

        /**
         * Add lines defined as polygons to image.
         * 
         * @param trackCollection initialised TrackCollection object
         * 
         */
        public void addTrackingLinesToImage(TrackCollection trackCollection) {
            Iterator<Pair<Track, Track>> it = trackCollection.iterator();
            while (it.hasNext()) {
                Pair<Track, Track> pair = it.next();
                PolygonRoi pR = GraphicsElements.getLine(pair.fst.asPolygon(), getColor(pair.fst)); // back
                overlay.add(pR);
                pR = GraphicsElements.getLine(pair.snd.asPolygon(), getColor(pair.snd)); // forward
                overlay.add(pR);
            }
            originalImage.setOverlay(overlay);
        }

    }

    /**
     * Class for plotting on [x,y] image
     * @author baniuk
     *
     */
    static class Image extends TrackVisualisation {
        public Image(ImagePlus originalImage) {
            super(originalImage);
        }

        public Image(String name, ImageProcessor imp) {
            super(name, imp);
        }

        /**
         * Plot unrelated points on image (static).
         * 
         * @param mapCell source of coordinate maps
         * @param points list of points to plot in coordinates (index,frame)
         * @param color color of point
         * @param radius radius of point
         */
        public void addCirclesToImage(STmap mapCell, Polygon points, Color color, double radius) {
            double x[][] = mapCell.getxMap();
            double y[][] = mapCell.getyMap();
            int[] indexes = points.ypoints;
            int[] frames = points.xpoints;
            for (int n = 0; n < points.npoints; n++) {
                // decode frame,outline to screen coordinates
                if (frames[n] < 0 || indexes[n] < 0)
                    continue;
                double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
                double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
                plotCircle(xcoord, ycoord, color, radius);
            }
            originalImage.setOverlay(overlay); // add to image
        }

        /**
         * Plot static elements on image if they are not null.
         * 
         * @param trackCollection initialised TrackCollection object
         * @param mF maxima according to Prot_Analysis.MaximaFinder
         */
        public void addElementsToImage(STmap mapCell, TrackCollection trackCollection,
                MaximaFinder mF) {
            if (mF != null) {
                Polygon max = mF.getMaxima();
                addCirclesToImage(mapCell, max, TrackVisualisation.MAXIMA_COLOR, 7);
            }
            if (trackCollection != null) {
                addTrackingLinesToImage(mapCell, trackCollection);
            }

        }

        /**
         * Plot tracking lines before and after maxima points (static).
         *  
         * @param mapCell map related to given cell.
         * @param pL List of polygons that keep coordinates of points of backward and forward tracks.
         * The polygons in <tt>pL</tt> list must be in alternating order: BM1,FM1,BM2,FM2,..., where
         * BMx is backward track for maximum point no.x and FMx is the forward track for maximum point 
         * no.x. This order is respected by {@link Prot_Analysis.trackMaxima(STmap, double, MaximaFinder)} 
         */
        public void addTrackingLinesToImage(STmap mapCell, TrackCollection trackCollection) {
            double x[][] = mapCell.getxMap(); // temporary x and y coordinates for given cell
            double y[][] = mapCell.getyMap();
            // these are raw coordinates of tracking lines extracted from List<PolygonRoi> pL
            ArrayList<float[]> xcoorda = new ArrayList<>();
            ArrayList<float[]> ycoorda = new ArrayList<>();
            int aL = 0;
            // iterate over tracks
            Iterator<Track> it = trackCollection.iteratorTrack();
            while (it.hasNext()) {
                Track track = it.next();
                Polygon pR = track.asPolygon();
                // create store for tracking line coordinates
                xcoorda.add(new float[pR.npoints]);
                ycoorda.add(new float[pR.npoints]);
                // counter of invalid vertexes. According to TrackMap#trackForward last points can
                // be -1 when user provided longer time span than available. (last in term of time)
                int invalidVertex = 0;
                // decode frame,outline to x,y
                for (int f = 0; f < pR.npoints; f++) {
                    // -1 stands for points that are outside of range - assured by TrackMap.class
                    if (pR.ypoints[f] < 0 || pR.xpoints[f] < 0) {
                        invalidVertex++; // count bad points
                        continue;
                    }
                    xcoorda.get(aL)[f] = (float) x[pR.xpoints[f]][pR.ypoints[f]];
                    ycoorda.get(aL)[f] = (float) y[pR.xpoints[f]][pR.ypoints[f]];
                }
                PolygonRoi pRoi = GraphicsElements.getLine(xcoorda.get(aL), ycoorda.get(aL),
                        pR.npoints - invalidVertex, getColor(track));
                overlay.add(pRoi);
                aL++;
            }
            originalImage.setOverlay(overlay); // add to image
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
            int[] indexes = points.ypoints;
            int[] frames = points.xpoints;

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
         * @param trackCollection initialized TrackCollection object
         * TODO This method uses old approach assuming that back and forw tracks are repeating. 
         */
        public void addTrackingLinesToImage(STmap mapCell, TrackCollection trackCollection) {
            double x[][] = mapCell.getxMap(); // temporary x and y coordinates for given cell
            double y[][] = mapCell.getyMap();
            // these are raw coordinates of tracking lines extracted from List<PolygonRoi> pL
            ArrayList<float[]> xcoorda = new ArrayList<>();
            ArrayList<float[]> ycoorda = new ArrayList<>();
            int aL = 0;
            // iterate over tracks
            Iterator<Track> it = trackCollection.iteratorTrack();
            while (it.hasNext()) {
                Track track = it.next();
                Polygon pR = track.asPolygon();
                // we need to sort tracking line points according to frames where they appear in
                // first convert poygon to list of Point2i object
                List<Point> plR =
                        TrackMapAnalyser.Polygon2Point2i(new ArrayList<Polygon>(Arrays.asList(pR)));
                // then sort this list according y-coordinate (frame)
                Collections.sort(plR, new ListPoint2iComparator());
                // convert to polygon again but now it is sorted along frames
                Polygon plRsorted = TrackMapAnalyser.Point2i2Polygon(plR);
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
                    xcoorda.get(aL)[f] = (float) x[plRsorted.xpoints[f]][plRsorted.ypoints[f]];
                    ycoorda.get(aL)[f] = (float) y[plRsorted.xpoints[f]][plRsorted.ypoints[f]];
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
                    // set colors (remember about backward/forward order)
                    PolygonRoi pRoi = GraphicsElements.getLine(xcoorda.get(aL), ycoorda.get(aL),
                            f + 1, color[aL % 2]);
                    // set where we want plot f+1 points from x/ycoorda
                    pRoi.setPosition((int) plRsorted.xpoints[f] + 1);
                    overlay.add(pRoi);
                    // If there is maximum on x frame and we plotted backward line from x-n to x, we
                    // wont to keep it during plotting forward tracking from x to x+z frames. So
                    // this whole line is plotted on every x-x+z frame
                    if (aL % 2 == 1) {
                        PolygonRoi pRoi1 = GraphicsElements.getLine(xcoorda.get(aL - 1),
                                ycoorda.get(aL - 1), xcoorda.get(aL - 1).length, color[aL % 2 - 1]);
                        pRoi1.setPosition((int) plRsorted.xpoints[f] + 1);
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
            addCirclesToImage(mapCell, max, TrackVisualisation.MAXIMA_COLOR, 7);
        }

        /**
         * Plot filled circle on overlay on given frame.
         * 
         * @param x
         * @param y
         * @param frame
         * @param color
         * @param radius
         */
        public void plotCircle(double x, double y, int frame, Color color, double radius) {
            // create ROI
            PolygonRoi oR = GraphicsElements.getCircle(x, y, color, radius);
            // set z-position of ROI!!!
            oR.setPosition(frame);
            overlay.add(oR); // add to collection of overlays
        }

        /**
         * Compare Point2i objects along frames (x-coordinate).
         * 
         * @author p.baniukiewicz
         *
         */
        class ListPoint2iComparator implements Comparator<Point> {

            @Override
            public int compare(Point o1, Point o2) {
                if (o1.x < o2.x)
                    return -1;
                if (o1.x > o2.x)
                    return 1;
                else
                    return 0;
            }

        }
    }
}
