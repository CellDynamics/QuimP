package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.plugin.ZProjector;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.quimp.utils.graphics.GraphicsElements;

/**
 * Support various methods of visualising protrusion data.
 * 
 * <p>In general all plots are added to image used to construct this object as overlay layer.
 * 
 * <p>This super class contains methods for creating static plots in [x,y] domain from coordinates
 * in [outline,frame] system (native for maps generated by Qanalysis).
 * 
 * <p><b>Warning</b>
 * 
 * <p>It is assumed that plotted maps have frames on x-axis and indexes on y-axis
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class TrackVisualisation {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(TrackVisualisation.class.getName());
  /**
   * Color for maxima points.
   */
  public static Color MAXIMA_COLOR = Color.MAGENTA;
  /**
   * Definition of colors used to plot tracks:
   * <ol>
   * <li>index 0 - backtracked position of point
   * <li>index 1 - forwardtracked position of point.
   * <li>index 2 - other
   * </ol>
   */
  public static Color[] color = { Color.YELLOW, Color.GREEN, Color.WHITE };

  /**
   * The original image.
   */
  protected ImagePlus originalImage; // reference of image to be plotted on

  /**
   * The overlay.
   */
  protected Overlay overlay;

  /**
   * Create correct object.
   * 
   * <p>If input image contains any overlay data, they will be extended by new plots.
   * 
   * @param originalImage Image to be plotted on.
   */
  public TrackVisualisation(ImagePlus originalImage) {
    this.originalImage = originalImage;
    LOGGER.trace("Num of slices: " + originalImage.getStackSize());
    overlay = originalImage.getOverlay(); // check for existing overlay
    if (overlay == null) {
      overlay = new Overlay();
    }

  }

  /**
   * Construct object from raw ImageProcessor.
   * 
   * @param name Name of the image
   * @param imp ImageProcessor
   */
  public TrackVisualisation(String name, ImageProcessor imp) {
    this(new ImagePlus(name, imp));
  }

  /**
   * Plot filled circle on overlay.
   * 
   * @param x center
   * @param y center
   * @param color color
   * @param radius radius
   */
  public void plotCircle(double x, double y, Color color, double radius) {
    // create ROI
    PolygonRoi or = GraphicsElements.getCircle(x, y, color, radius);
    overlay.add(or); // add to collection of overlays
  }

  /**
   * getOriginalImage.
   * 
   * @return the originalImage
   */
  public ImagePlus getOriginalImage() {
    return originalImage;
  }

  /**
   * Helper method.
   * 
   * <p>Allows to convert enum to index of array of Colors.
   * 
   * @param track track
   * @return Color from color array
   */
  protected Color getColor(Track track) {
    Color c;
    Track.TrackType type = track.type;
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
   * Flatten stack according to given type.
   * 
   * <p>Output has the same resolution (x,y,t) as input. For stacks, slices are duplicated. Refer to
   * ij.plugin.ZProjector.setMethod(int)
   * 
   * @param method How to flatten - ZProjector methods.
   * @param preserveStack - if <b>true</b> size of output stack is preserved (slices are
   *        duplicated to form stack with the same number of slices as original one). Otherwise
   *        only one slice is built
   */
  public void flatten(int method, boolean preserveStack) {
    ImageStack is = originalImage.getStack();
    is = is.convertToFloat(); // for better averaging

    ZProjector zp = new ZProjector(new ImagePlus(originalImage.getTitle(), is));
    zp.setStartSlice(1);
    zp.setStopSlice(originalImage.getStackSize());
    zp.setMethod(method);
    zp.doProjection();
    ImagePlus ret = zp.getProjection();
    // recreate stack if needed
    if (originalImage.getStackSize() > 1 && preserveStack) {
      ImageStack imS = new ImageStack(ret.getWidth(), ret.getHeight());
      for (int s = 0; s < originalImage.getStackSize(); s++) {
        imS.addSlice(ret.getProcessor().convertToByte(true));
      }
      originalImage = new ImagePlus(originalImage.getTitle(), imS);
    } else { // return only one slice (due to input format or preserveStack flag status)
      originalImage =
              new ImagePlus(originalImage.getTitle(), ret.getProcessor().convertToByte(true));
    }

  }

  /**
   * Subclass for plotting on single image in coord space [outline,frame]
   * 
   * @author p.baniukiewicz
   *
   */
  static class Map extends TrackVisualisation {

    /**
     * Instantiates a new map.
     *
     * @param originalImage the original image
     */
    public Map(ImagePlus originalImage) {
      super(originalImage);
    }

    /**
     * Instantiates a new map.
     *
     * @param name the name
     * @param imp the imp
     */
    public Map(String name, ImageProcessor imp) {
      super(name, imp);
    }

    /**
     * Create object from raw data like e.g. motility map.
     * 
     * @param name Name of the image
     * @param data 2D data.
     */
    public Map(String name, float[][] data) {
      super(name, new FloatProcessor(data));
      // ImageProcessor imp = originalImage.getProcessor();
      // can not be rotated here!!
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
        if (frames[n] < 0 || indexes[n] < 0) {
          continue;
        }
        plotCircle(frames[n], indexes[n], color, radius);
      }
      originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot maxima found by {@link MaximaFinder} on current image.
     * 
     * @param maxF properly initialized {@link MaximaFinder} object.
     */
    public void addMaximaToImage(MaximaFinder maxF) {
      Polygon max = maxF.getMaxima();
      PointRoi pr =
              GraphicsElements.getPoint(max.xpoints, max.ypoints, TrackVisualisation.MAXIMA_COLOR);
      overlay.add(pr);
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
        PolygonRoi pr =
                GraphicsElements.getLine(pair.getLeft().asPolygon(), getColor(pair.getLeft())); // b
        overlay.add(pr);
        pr = GraphicsElements.getLine(pair.getRight().asPolygon(), getColor(pair.getRight())); // fw
        overlay.add(pr);
      }
      originalImage.setOverlay(overlay);
    }

  }

  /**
   * Class for plotting on [x,y] image
   * 
   * @author p.baniukiewicz
   *
   */
  static class Image extends TrackVisualisation {

    /**
     * Instantiates a new image.
     *
     * @param originalImage the original image
     */
    public Image(ImagePlus originalImage) {
      super(originalImage);
    }

    /**
     * Instantiates a new image.
     *
     * @param name the name
     * @param imp the imp
     */
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
      double[][] x = mapCell.getxMap();
      double[][] y = mapCell.getyMap();
      int[] indexes = points.ypoints;
      int[] frames = points.xpoints;
      for (int n = 0; n < points.npoints; n++) {
        // decode frame,outline to screen coordinates
        if (frames[n] < 0 || indexes[n] < 0) {
          continue;
        }
        double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
        double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
        plotCircle(xcoord, ycoord, color, radius);
      }
      originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot static elements on image if they are not null.
     * 
     * @param mapCell STmap
     * @param trackCollection initialised TrackCollection object
     * @param mf maxima according to Prot_Analysis.MaximaFinder
     */
    public void addElementsToImage(STmap mapCell, TrackCollection trackCollection,
            MaximaFinder mf) {
      if (mf != null) {
        Polygon max = mf.getMaxima();
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
     * @param trackCollection
     * 
     */
    public void addTrackingLinesToImage(STmap mapCell, TrackCollection trackCollection) {
      double[][] x = mapCell.getxMap(); // temporary x and y coordinates for given cell
      double[][] y = mapCell.getyMap();
      // these are raw coordinates of tracking lines extracted from List<PolygonRoi> pL
      ArrayList<float[]> xcoorda = new ArrayList<>();
      ArrayList<float[]> ycoorda = new ArrayList<>();
      int al = 0;
      // iterate over tracks
      Iterator<Track> it = trackCollection.iteratorTrack();
      while (it.hasNext()) {
        Track track = it.next();
        Polygon pr = track.asPolygon();
        // create store for tracking line coordinates
        xcoorda.add(new float[pr.npoints]);
        ycoorda.add(new float[pr.npoints]);
        // counter of invalid vertexes. According to TrackMap#trackForward last points can
        // be -1 when user provided longer time span than available. (last in term of time)
        int invalidVertex = 0;
        // decode frame,outline to x,y
        for (int f = 0; f < pr.npoints; f++) {
          // -1 stands for points that are outside of range - assured by TrackMap.class
          if (pr.ypoints[f] < 0 || pr.xpoints[f] < 0) {
            invalidVertex++; // count bad points
            continue;
          }
          xcoorda.get(al)[f] = (float) x[pr.xpoints[f]][pr.ypoints[f]];
          ycoorda.get(al)[f] = (float) y[pr.xpoints[f]][pr.ypoints[f]];
        }
        PolygonRoi polyRoi = GraphicsElements.getLine(xcoorda.get(al), ycoorda.get(al),
                pr.npoints - invalidVertex, getColor(track));
        overlay.add(polyRoi);
        al++;
      }
      originalImage.setOverlay(overlay); // add to image
    }
  }

  /**
   * Subclass for plotting on stacks in coord space [x,y,f].
   * 
   * @author p.baniukiewicz
   *
   */
  static class Stack extends TrackVisualisation {

    /**
     * Instantiates a new stack.
     *
     * @param originalImage the original image
     */
    public Stack(ImagePlus originalImage) {
      super(originalImage);
    }

    /**
     * Constructor.
     * 
     * @param name name
     * @param imp base ImageProcessor
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
      double[][] x = mapCell.getxMap();
      double[][] y = mapCell.getyMap();
      int[] indexes = points.ypoints;
      int[] frames = points.xpoints;

      // LOGGER.trace("Frames:" + Arrays.toString(frames));
      // LOGGER.trace("Indexe:" + Arrays.toString(indexes));
      for (int n = 0; n < points.npoints; n++) {
        // decode frame,outline to screen coordinates
        if (frames[n] < 0 || indexes[n] < 0) {
          continue;
        }
        double xcoord = x[frames[n]][indexes[n]]; // screen coordinate of
        double ycoord = y[frames[n]][indexes[n]]; // (frame,index) point
        plotCircle(xcoord, ycoord, frames[n] + 1, color, radius);
      }
      originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot unrelated points on image (stack). Input compatible with
     * {@link TrackMapAnalyser#getIntersectionParents(List, int)}.
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
        x[l] = p.getRight().x;
        y[l] = p.getRight().y;
        l++;
      }
      Polygon poly = new Polygon(x, y, points.size());
      addCirclesToImage(mapCell, poly, color, radius);
    }

    /**
     * Plot tracking lines before and after maxima points (in term of frames).
     * 
     * <p>First backward tracking lines are plotted then forward in two different colors. For given
     * maximum first is plotted backward tracking frame by frame, then forward tracking.
     * Backward tracking is visible as long as forward tracking is plotted. Then both disappear.
     * 
     * @param mapCell map related to given cell.
     * @param trackCollection initialized TrackCollection object TODO This method uses old
     *        approach assuming that back and forw tracks are repeating.
     */
    public void addTrackingLinesToImage(STmap mapCell, TrackCollection trackCollection) {
      double[][] x = mapCell.getxMap(); // temporary x and y coordinates for given cell
      double[][] y = mapCell.getyMap();
      // these are raw coordinates of tracking lines extracted from List<PolygonRoi> pL
      ArrayList<float[]> xcoorda = new ArrayList<>();
      ArrayList<float[]> ycoorda = new ArrayList<>();
      int al = 0;
      // iterate over tracks
      Iterator<Track> it = trackCollection.iteratorTrack();
      while (it.hasNext()) {
        Track track = it.next();
        Polygon polyR = track.asPolygon();
        // we need to sort tracking line points according to frames where they appear in
        // first convert poygon to list of Point2i object
        List<Point> plR =
                TrackMapAnalyser.polygon2Point2i(new ArrayList<Polygon>(Arrays.asList(polyR)));
        // then sort this list according y-coordinate (frame)
        Collections.sort(plR, new ListPoint2iComparator());
        // convert to polygon again but now it is sorted along frames
        Polygon plRsorted = TrackMapAnalyser.point2i2Polygon(plR);
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
          xcoorda.get(al)[f] = (float) x[plRsorted.xpoints[f]][plRsorted.ypoints[f]];
          ycoorda.get(al)[f] = (float) y[plRsorted.xpoints[f]][plRsorted.ypoints[f]];
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
          PolygonRoi polyRoi =
                  GraphicsElements.getLine(xcoorda.get(al), ycoorda.get(al), f + 1, color[al % 2]);
          // set where we want plot f+1 points from x/ycoorda
          polyRoi.setPosition((int) plRsorted.xpoints[f] + 1);
          overlay.add(polyRoi);
          // If there is maximum on x frame and we plotted backward line from x-n to x, we
          // wont to keep it during plotting forward tracking from x to x+z frames. So
          // this whole line is plotted on every x-x+z frame
          if (al % 2 == 1) {
            PolygonRoi polyRoi1 = GraphicsElements.getLine(xcoorda.get(al - 1), ycoorda.get(al - 1),
                    xcoorda.get(al - 1).length, color[al % 2 - 1]);
            polyRoi1.setPosition((int) plRsorted.xpoints[f] + 1);
            overlay.add(polyRoi1);
          }
        }
        al++;
      }
      originalImage.setOverlay(overlay); // add to image
    }

    /**
     * Plot maxima found by {@link MaximaFinder} on current image.
     * 
     * @param mapCell map related to given cell.
     * @param mf properly initialized {@link MaximaFinder} object.
     */
    public void addMaximaToImage(STmap mapCell, MaximaFinder mf) {
      Polygon max = mf.getMaxima();
      addCirclesToImage(mapCell, max, TrackVisualisation.MAXIMA_COLOR, 7);
    }

    /**
     * Plot filled circle on overlay on given frame.
     * 
     * @param x center
     * @param y center
     * @param frame frame
     * @param color color
     * @param radius radius
     */
    public void plotCircle(double x, double y, int frame, Color color, double radius) {
      // create ROI
      PolygonRoi or = GraphicsElements.getCircle(x, y, color, radius);
      // set z-position of ROI!!!
      or.setPosition(frame);
      overlay.add(or); // add to collection of overlays
    }

    /**
     * Plot outline around cell on image.
     * 
     * @param mapCell map related to given cell.
     * @param config configuration object defining colors, type of plot, etc.
     * @see ProtAnalysisConfig
     */
    public void addOutlinesToImage(STmap mapCell, ProtAnalysisConfig config) {
      double[][] mm = mapCell.getMotMap();
      double[][] cm = mapCell.getConvMap();

      switch (config.outlinesToImage.plotType) {
        case MOTILITY:
          plotOutline(mapCell.getxMap(), mapCell.getyMap(),
                  new Color[] { config.outlinesToImage.motColor, config.outlinesToImage.defColor },
                  new double[] { config.outlinesToImage.motThreshold }, mapCell.getMotMap());
          break;
        case CONVEXITY:
          plotOutline(mapCell.getxMap(), mapCell.getyMap(),
                  new Color[] { config.outlinesToImage.convColor, config.outlinesToImage.defColor },
                  new double[] { config.outlinesToImage.convThreshold }, mapCell.getConvMap());
          break;
        case CONVANDEXP: {
          // prepare fake map
          double[][] tmpMap = new double[mm.length][];
          for (int f = 0; f < tmpMap.length; f++) {
            tmpMap[f] = new double[mm[f].length];
            for (int r = 0; r < mm[f].length; r++) {
              tmpMap[f][r] = ((mm[f][r] > 0 && cm[f][r] > 0)) ? 1.0 : -1.0;
            }
          }
          plotOutline(mapCell.getxMap(), mapCell.getyMap(),
                  new Color[] { config.outlinesToImage.convColor, config.outlinesToImage.defColor },
                  new double[] { 0 }, tmpMap);
        }
          break;
        case CONCANDRETR: {
          // prepare fake map
          double[][] tmpMap = new double[mm.length][];
          for (int f = 0; f < tmpMap.length; f++) {
            tmpMap[f] = new double[mm[f].length];
            for (int r = 0; r < mm[f].length; r++) {
              tmpMap[f][r] = (mm[f][r] < 0 && cm[f][r] < 0) ? 1.0 : -1.0;
            }
          }
          plotOutline(mapCell.getxMap(), mapCell.getyMap(),
                  new Color[] { config.outlinesToImage.motColor, config.outlinesToImage.defColor },
                  new double[] { 0 }, tmpMap);
        }
          break;
        case BOTH: {
          double[][] tmpMap = new double[mm.length][];
          for (int f = 0; f < tmpMap.length; f++) {
            tmpMap[f] = new double[mm[f].length];
            for (int r = 0; r < mm[f].length; r++) {
              tmpMap[f][r] = (mm[f][r] > 0 && cm[f][r] > 0) ? 1.0 : -1.0;
            }
          }
          double[][] tmpMap1 = new double[mm.length][];
          for (int f = 0; f < tmpMap.length; f++) {
            tmpMap1[f] = new double[mm[f].length];
            for (int r = 0; r < mm[f].length; r++) {
              tmpMap1[f][r] = (mm[f][r] < 0 && cm[f][r] < 0) ? 1.0 : -1.0;
            }
          }

          plotOutline(mapCell.getxMap(),
                  mapCell.getyMap(), new Color[] { config.outlinesToImage.motColor,
                      config.outlinesToImage.convColor, config.outlinesToImage.defColor },
                  new double[] { 0, 0 }, tmpMap, tmpMap1);

        }
          break;
        default:
          throw new IllegalArgumentException("Plot Type not supported");
      }

      originalImage.setOverlay(overlay);
    }

    /**
     * Helper method for plotting outlines.
     * 
     * <p>Plot outline according to given maps and thresholds with specified colour. Points that
     * does not meet criterion are plotted in default colour.
     * 
     * @param x x-coordinates map
     * @param y y-coordinates map
     * @param color array of colours used for plotting maps given in <tt>map</tt>. The array
     *        must contain default colour on its last position. Its size is usually greater by 1
     *        than number of maps.
     * @param threshold array of threshold that applies to maps. Must be size of maps.
     * @param map list of maps to be plotted with colour <tt>color</tt> if they meet criterion
     *        <tt>threshold</tt>
     */
    private void plotOutline(double[][] x, double[][] y, Color[] color, double[] threshold,
            double[][]... map) {
      Polygon[] lines = new Polygon[map.length + 1]; // last line is default one
      for (int i = 0; i < lines.length; i++) {
        lines[i] = new Polygon();
      }
      // plot map lines (those points that are consecutive for given map and its criterion)
      // and default lines (those consecutive points that do no belong to any map)
      for (int f = 0; f < x.length; f++) { // over frames
        for (int r = 0; r < x[0].length; r++) { // over indexes of outline
          int l = 0;
          boolean added = false; // indicate whether given point is plotted as map
          for (Object item : map) { // over maps
            double[][] tmpmap = (double[][]) item;
            // check threshold. If point is accepted, any started default line is
            // finished here and new map line is started.
            if (tmpmap[f][r] >= threshold[l]) {
              // store this point as l-th line
              lines[l].addPoint((int) Math.round(x[f][r]), (int) Math.round(y[f][r]));
              added = true; // mark that this point is plotted already within any map
              // plot default no-map points if any stored (stored in else part)
              if (lines[lines.length - 1].npoints > 0) {
                PolygonRoi polyR = GraphicsElements.getLine(lines[lines.length - 1],
                        color[color.length - 1], f + 1);
                overlay.add(polyR);
                lines[lines.length - 1] = new Polygon();
              }
            } else { // if not meet criterion plot all lines and initialise them again
              if (lines[l].npoints > 0) {
                PolygonRoi polyR = GraphicsElements.getLine(lines[l], color[l], f + 1);
                overlay.add(polyR);
                lines[l] = new Polygon();
              }
              // and and add this point to default if not plotted yet
              if (!added) {
                lines[lines.length - 1].addPoint((int) Math.round(x[f][r]),
                        (int) Math.round(y[f][r]));
              }
            }
            l++;
          }
        }
        // plot if else-if above does not fire (if fire all lines are initialized again)
        PolygonRoi polyR;
        for (int i = 0; i < lines.length; i++) {
          if (lines[i].npoints > 0) {
            polyR = GraphicsElements.getLine(lines[i], color[i], f + 1);
            overlay.add(polyR);
            lines[i] = new Polygon();
          }
        }
      }
    }

    /**
     * Compare Point2i objects along frames (x-coordinate).
     * 
     * @author p.baniukiewicz
     *
     */
    class ListPoint2iComparator implements Comparator<Point> {

      /*
       * (non-Javadoc)
       * 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(Point o1, Point o2) {
        if (o1.x < o2.x) {
          return -1;
        }
        if (o1.x > o2.x) {
          return 1;
        } else {
          return 0;
        }
      }

    }
  }
}
