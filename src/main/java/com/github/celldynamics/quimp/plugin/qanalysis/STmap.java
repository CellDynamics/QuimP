package com.github.celldynamics.quimp.plugin.qanalysis;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.QColor;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Create spatial temporal maps from ECMM and ANA data.
 * 
 * <p>This class can be serialized but only as container of maps. Data required for creation of
 * those maps are not serialized, thus restored object is not fully functional. As this is last step
 * in QuimP workflow it may not be necessary to load this json anymore.
 * 
 * @author rtyson
 * @author baniuk
 */
public class STmap implements IQuimpSerialize {

  /**
   * The Constant LOGGER. It is public because {@link FormatConverter#saveMaps(int)} can replace it
   */
  public static Logger LOGGER = LoggerFactory.getLogger(STmap.class.getName());

  /**
   * Motility map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int MOTILITY = 1;
  /**
   * Convexity map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int CONVEXITY = 2;
  /**
   * Origin map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int ORIGIN = 4;
  /**
   * Coordinates map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int COORD = 8;
  /**
   * X coordinates map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int XMAP = 16;
  /**
   * Y coordinates map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int YMAP = 32;
  /**
   * Fluorescence channel 1 map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int FLU1 = 64;
  /**
   * Fluorescence channel 2 map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int FLU2 = 128;
  /**
   * Fluorescence channel 3 map code.
   * 
   * @see #saveMaps(int)
   */
  public static final int FLU3 = 256;
  /**
   * Combine all FLU maps.
   * 
   * @see #saveMaps(int)
   */
  public static final int ALLFLU = FLU1 | FLU2 | FLU3;
  /**
   * Combine all maps.
   */
  public static final int ALLMAPS = MOTILITY | CONVEXITY | ORIGIN | COORD | XMAP | YMAP | ALLFLU;

  /**
   * Coordinates map.
   * 
   * <p>Each node has an associated position. The co-ordinate map, rather than contain values
   * regarding motility, fluorescence or convexity, instead contains the position values of nodes.
   * The main purpose of the co-ordinate map, along with the origin map, is for tracking positions
   * through time. Size is Map[getT()][getRes()]. Setter is recommended for proper initialisation.
   * 
   * @see <a href=
   *      "http://pilip.lnx.warwick.ac.uk/docs/develop/QuimP_Guide.html#x1-320005">Manual</a>
   */
  private double[][] coordMap;
  /**
   * Each node has an origin, the position a node originated from on the previous frame. The
   * origin map contains origin values and can be used, along with the co-ordinate map, to track
   * positions through time. Size is Map[getT()][getRes()]. Setter is recommended for proper
   * initialisation.
   * 
   * @see <a href=
   *      "http://pilip.lnx.warwick.ac.uk/docs/develop/QuimP_Guide.html#x1-320005">Manual</a>
   */
  private double[][] originMap;
  /**
   * Contains horizontal image pixel co-ordinates (those on the image used for segmentation)
   * relating to map pixels. Size is Map[getT()][getRes()]. Setter is recommended for proper
   * initialisation.
   * 
   * @see <a href=
   *      "http://pilip.lnx.warwick.ac.uk/docs/develop/QuimP_Guide.html#x1-320005">Manual</a>
   */
  private double[][] xMap;
  /**
   * Contains vertical image pixel co-ordinates (those on the image used for segmentation)
   * relating to map pixels. Size is Map[getT()][getRes()]. Setter is recommended for proper
   * initialisation.
   * 
   * @see <a href=
   *      "http://pilip.lnx.warwick.ac.uk/docs/develop/QuimP_Guide.html#x1-320005">Manual</a>
   */
  private double[][] yMap;
  /**
   * Motility map.
   * 
   * <p>Pixels are coloured according to node speed, as calculated by ECMM. Red shades represent
   * expanding regions, blue shades contracting regions. Pixel values within the tiff image are
   * scaled to fill the colour spectrum. The map file extended _motilityMap.maPQ contains
   * un-scaled values, in microns per second. Size is Map[getT()][getRes()]. Setter is recommended
   * for proper initialisation.
   * 
   * @see <a href=
   *      "http://pilip.lnx.warwick.ac.uk/docs/develop/QuimP_Guide.html#x1-320005">Manual</a>
   */
  private double[][] motMap;

  /**
   * The mig color.
   */
  transient int[] migColor;

  /**
   * The mig pixels.
   */
  transient float[] migPixels;
  /**
   * Fluoroscence maps for channels.
   */
  public FluoMap[] fluoMaps;
  /**
   * Convexity map.
   * 
   * <p>Size is Map[getT()][getRes()]. Setter is recommended for proper initialisation.
   */
  private double[][] convMap;

  /**
   * The conv color.
   */
  transient int[] convColor;

  /**
   * The conv im P.
   */
  transient ImagePlus migImP;
  transient ImagePlus fluImP;
  transient ImagePlus convImP;
  /**
   * Contain OutlineHandler used for generating maps.
   * 
   * <p><b>warning</b>
   * 
   * <p>It is not serialized
   */
  transient OutlineHandler oh;
  /**
   * Resolution of maps.
   * 
   * <p>This field together with <tt>T</tt> stands for the dimensions of 2D arrays for storing maps.
   * For this reason they are serialized. Map[T][res]
   */
  private int res;
  /**
   * Number of timeframes.
   */
  private int T;
  private double mapPixelHeight = 1;
  private double mapPixelWidth = 1;

  /**
   * Default constructor to satisfy GSon builder. Should not be used for proper object
   * initialization
   */
  public STmap() {
    this.fluoMaps = new FluoMap[3];
  }

  /**
   * Copy constructor.
   * 
   * <p><b>warning</b>
   * 
   * <p>Make a copy of serializable fields only
   * 
   * @param src source object
   */
  public STmap(final STmap src) {
    this();
    this.coordMap = QuimPArrayUtils.copy2darray(src.coordMap, null);
    this.originMap = QuimPArrayUtils.copy2darray(src.originMap, null);
    this.xMap = QuimPArrayUtils.copy2darray(src.xMap, null);
    this.yMap = QuimPArrayUtils.copy2darray(src.yMap, null);
    this.motMap = QuimPArrayUtils.copy2darray(src.motMap, null);
    this.convMap = QuimPArrayUtils.copy2darray(src.convMap, null);
    this.res = src.res;
    this.T = src.T;
    this.mapPixelHeight = src.mapPixelHeight;
    this.mapPixelWidth = src.mapPixelWidth;
    this.fluoMaps = new FluoMap[src.fluoMaps.length];
    for (int i = 0; i < src.fluoMaps.length; i++) {
      this.fluoMaps[i] = new FluoMap(src.fluoMaps[i]);
    }

  }

  /**
   * Build object for given:
   * 
   * @param o Outline from ECMM
   * @param r Map resolution in pixels
   * @see com.github.celldynamics.quimp.plugin.qanalysis.Qp
   */
  public STmap(OutlineHandler o, int r) {
    this();
    mapPixelHeight = 1;
    mapPixelWidth = 1.0d / r;
    res = r;
    oh = o;
    T = oh.getSize();

    coordMap = new double[T][res];
    originMap = new double[T][res];
    xMap = new double[T][res]; // interpolated pixel coordinates
    yMap = new double[T][res];

    motMap = new double[T][res];
    migColor = new int[T * res];
    migPixels = new float[T * res];

    // fluMap = new double[T][res];
    // fluColor = new byte[T * res];

    convMap = new double[T][res];
    convColor = new int[T * res];

    // flu maps
    Vert v = oh.indexGetOutline(0).getHead();
    for (int i = 0; i < 3; i++) {
      fluoMaps[i] = new FluoMap(T, res, i + 1);
      System.out.println("flou in v: " + v.fluores[i].intensity);
      if (v.fluores[i].intensity == -2) { // disable if no data
        IJ.log("No fluorescence data for channel " + (i + 1));
        fluoMaps[i].setEnabled(false);
      }
    }

    generate();
    saveConvMotImages(); // save images that are opened already
  }

  /**
   * Generate all maps saved by Q Analysis Fill internal class fields.
   */
  private void generate() {

    this.calcCurvature();
    Vert zeroVert;
    Vert v;
    String migColorMap = "rwb";

    double fraction;
    double intMig;
    double intFlu;
    double intConv;
    double target;
    double actualTarget;
    QColor color;
    int pn;
    Vert fhead;
    Vert chead;

    double step = 1.0d / res;

    // ------debug----
    // System.out.println("210.in generate: min:"+ oh.migLimits[0]+",
    // max"+oh.migLimits[1]);
    // zeroVert = closestFloor(oh.getOutline(28), 0.9138373074502235, 'f');
    // fHead = findFirstNode(oh.getOutline(28),'f');
    // cHead = findFirstNode(oh.getOutline(28),'c');
    // if(true)return;
    // -------------------------

    double origin = 0; // co-ord for zeroVert to move to next
    int frame;

    for (int tt = 0; tt < T; tt++) {

      frame = tt + oh.getStartFrame();
      // System.out.println("frame " + t);
      pn = tt * res; // pixel index

      // find the first node in terms of coord and fcoord (not the head)
      fhead = oh.getStoredOutline(frame).findFirstNode('f');
      chead = oh.getStoredOutline(frame).findFirstNode('c');
      // fHead.print();
      // cHead.print();

      if (tt == 0) {
        // for the first time point the head coord node is our starting point
        zeroVert = chead;
        fraction = 0;
        origin = 0;
      } else {
        // vert closest below zero (zero being tracked over time from
        // frame 1!)
        zeroVert = closestFloor(oh.getStoredOutline(frame), origin, 'f', fhead);
        // System.out.println("zerovert: " + zeroVert.fCoord +", origin:
        // " + origin + ", fHead: " + fHead.fCoord);
        // position of origin between zeroVert and zeroVert.getNext
        fraction = ffraction(zeroVert, origin, fhead);
        // System.out.println("resulting fraction: " + fraction);

        // System.out.print("\nzeroVert.fCoord:"+zeroVert.coord+",
        // fraction:"+fraction +"\n");
        origin = interpCoord(zeroVert, fraction, chead); // the new origin
        // System.out.println("new origin: " + origin);
      }
      target = origin; // coord to fill in map next

      intMig = interpolate(zeroVert.distance, zeroVert.getNext().distance, fraction);
      motMap[tt][0] = intMig;
      color = QColor.erColorMap2(migColorMap, intMig, oh.migLimits[0], oh.migLimits[1]);
      migColor[pn] = color.getColorInt();
      migPixels[pn] = (float) intMig;

      // fill fluo maps
      for (int i = 0; i < 3; i++) {
        if (fluoMaps[i].isEnabled()) {
          if (zeroVert.fluores[i].intensity == -2) {
            IJ.log("ERROR: There are missing fluoresecne values! Run ANA");
            return;
          }
          intFlu = interpolate(zeroVert.fluores[i].intensity,
                  zeroVert.getNext().fluores[i].intensity, fraction);
          fluoMaps[i].fill(tt, 0, pn, intFlu, oh.fluLims[i][1]);
        }
      }

      /*
       * if (zeroVert.floures == -1) { fluMap[t][0] = 0; fluColor[pN] = (byte)
       * QColor.bwScale(0, 256, oh.maxFlu, 0); } else { intFlu = interpolate(zeroVert.floures,
       * zeroVert.getNext().floures, fraction); fluMap[t][0] = intFlu; fluColor[pN] = (byte)
       * QColor.bwScale(intFlu, 256, oh.maxFlu, 0); }
       */

      intConv = interpolate(zeroVert.curvatureSum, zeroVert.getNext().curvatureSum, fraction);
      convMap[tt][0] = intConv;
      color = QColor.erColorMap2("rbb", intConv, oh.curvLimits[0], oh.curvLimits[1]);
      convColor[pn] = color.getColorInt();

      coordMap[tt][0] = origin;
      originMap[tt][0] = interpFCoord(zeroVert, fraction, fhead);
      xMap[tt][0] = interpolate(zeroVert.getX(), zeroVert.getNext().getX(), fraction);
      yMap[tt][0] = interpolate(zeroVert.getY(), zeroVert.getNext().getY(), fraction);

      if (target >= 1 || target < 0) {
        System.out.println("target out of range: " + target);
      }

      for (int p = 1; p < res; p++) {
        pn = (tt * res) + p; // pixel index
        target += step;
        actualTarget = (target >= 1) ? target - 1 : target; // wraps around to zero
        // System.out.println("\tactualtarget:"+actualTarget);
        coordMap[tt][p] = actualTarget;

        v = closestFloor(oh.getStoredOutline(frame), actualTarget, 'c', chead); // should this be g
        fraction = cfraction(v, actualTarget, chead);

        originMap[tt][p] = interpFCoord(v, fraction, fhead);
        xMap[tt][p] = interpolate(v.getX(), v.getNext().getX(), fraction);
        yMap[tt][p] = interpolate(v.getY(), v.getNext().getY(), fraction);

        intMig = interpolate(v.distance, v.getNext().distance, fraction);
        motMap[tt][p] = intMig;
        color = QColor.erColorMap2(migColorMap, intMig, oh.migLimits[0], oh.migLimits[1]);
        migColor[pn] = color.getColorInt();
        migPixels[pn] = (float) intMig;

        for (int i = 0; i < 3; i++) {
          if (fluoMaps[i].isEnabled()) {
            intFlu = interpolate(v.fluores[i].intensity, v.getNext().fluores[i].intensity,
                    fraction);
            fluoMaps[i].fill(tt, p, pn, intFlu, oh.fluLims[i][1]);
          }
        }
        /*
         * if (zeroVert.floures == -1) { fluMap[t][p] = 0; fluColor[pN] = (byte)
         * QColor.bwScale(0, 256, oh.maxFlu, 0); } else { intFlu = interpolate(v.floures,
         * v.getNext().floures, fraction); fluMap[t][p] = intFlu; fluColor[pN] = (byte)
         * QColor.bwScale(intFlu, 256, oh.maxFlu, 0); }
         */

        intConv = interpolate(v.curvatureSum, v.getNext().curvatureSum, fraction);
        convMap[tt][p] = intConv;
        color = QColor.erColorMap2("rbb", intConv, oh.curvLimits[0], oh.curvLimits[1]);
        convColor[pn] = color.getColorInt();

      }

    }

    migImP = map2ImagePlus("motility_map", new ColorProcessor(res, T, migColor));
    convImP = map2ImagePlus("convexity_map", new ColorProcessor(res, T, convColor));
    migImP.show();
    convImP.show();
    // fluImP.show();
    // IJ.doCommand("Red");

    if (Qp.Build3D) {
      // create 3D of motility
      STMap3D map3d = new STMap3D(xMap, yMap, migColor);
      map3d.build();
      map3d.toOrigin(oh.indexGetOutline(0).getCentroid());
      map3d.scale(0.05f);
      map3d.write(new File("/tmp/cell_02.wrl"));

      // create 3D of curvature
      STMap3D map3dCur = new STMap3D(xMap, yMap, convColor);
      map3dCur.build();
      map3dCur.toOrigin(oh.indexGetOutline(0).getCentroid());
      map3dCur.scale(0.05f);
      map3dCur.write(new File("/tmp/cell_02_cur.wrl"));
    }

    // create fluo images

    for (int i = 0; i < 3; i++) {
      if (!fluoMaps[i].isEnabled()) {
        continue;
      }

      fluImP = IJ.createImage(Qp.filename + "_fluoCH" + fluoMaps[i].channel, "8-bit black", res, T,
              1);
      fluImP.getProcessor().setPixels(fluoMaps[i].getColours());
      resize(fluImP);
      setCalibration(fluImP);
      fluImP.show();

      try {
        Thread.sleep(500); // needed to let imageJ set the right colour maps
      } catch (Exception e) {
        ;
      }

      IJ.doCommand("Red"); // this don't always work. dun know why
      String tmpfilename = FileExtensions.fluomapFileExt.replaceFirst("%",
              Integer.toString(fluoMaps[i].channel));
      IJ.saveAs(fluImP, "tiff",
              Qp.outFile.getParent() + File.separator + Qp.filename + tmpfilename);
    }

    // saveMaps(); // save maQP files

    if (QuimPArrayUtils.sumArray(migColor) == 0) {
      IJ.showMessage("ECMM data is missing (or corrupt), and is needed for building accurate maps.+"
              + "\nPlease run ECMM (fluorescence data will be lost)");
    }
    // test making LUT images
    /*
     * ImagePlus migImPLut = IJ.createImage("mig_32", "32-bit", res, T,1); ImageProcessor
     * ipFloat = new FloatProcessor(res, T, migPixels, null); LUT lut = new LUT();
     * ipFloat.setLut(lut) migImPLut.setProcessor(ipFloat); resize(migImPLut); migImPLut.show();
     */
  }

  /**
   * Save map files (maQP) on disk.
   * 
   * @param maps Map to be saved, defined in this class. Use {@value STmap#ALLMAPS} for save all
   * @throws QuimpException any error with saving maps (except IO)
   */
  public void saveMaps(int maps) throws QuimpException {
    try {
      if ((maps & MOTILITY) == MOTILITY) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.motmapFileExt);
        QuimPArrayUtils.arrayToFile(motMap, ",", f);
        LOGGER.info("\tSaved motility map at: " + f.getAbsolutePath());
      }
      if ((maps & CONVEXITY) == CONVEXITY) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.convmapFileExt);
        QuimPArrayUtils.arrayToFile(convMap, ",", f);
        LOGGER.info("\tSaved convexity map at: " + f.getAbsolutePath());
      }
      if ((maps & ORIGIN) == ORIGIN) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.originmapFileExt);
        QuimPArrayUtils.arrayToFile(originMap, ",", f);
        LOGGER.info("\tSaved origin map at: " + f.getAbsolutePath());
      }
      if ((maps & COORD) == COORD) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.coordmapFileExt);
        QuimPArrayUtils.arrayToFile(coordMap, ",", f);
        LOGGER.info("\tSaved coord map at: " + f.getAbsolutePath());
      }
      if ((maps & XMAP) == XMAP) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.xmapFileExt);
        QuimPArrayUtils.arrayToFile(xMap, ",", f);
        LOGGER.info("\tSaved x map at: " + f.getAbsolutePath());
      }
      if ((maps & YMAP) == YMAP) {
        File f = new File(Qp.outFile.getPath() + FileExtensions.ymapFileExt);
        QuimPArrayUtils.arrayToFile(yMap, ",", f);
        LOGGER.info("\tSaved y map at: " + f.getAbsolutePath());
      }
      if ((maps & FLU1) == FLU1) {
        saveFluoroMap(0);
      }
      if ((maps & FLU2) == FLU2) {
        saveFluoroMap(1);
      }
      if ((maps & FLU3) == FLU3) {
        saveFluoroMap(2);
      }
    } catch (NullPointerException np) {
      LOGGER.debug(np.getMessage(), np);
      throw new QuimpException("Can not save map. Input array does not exist: " + np.getMessage());
    } catch (IOException e1) {
      IJ.error("Could not write Map file:\n " + e1.getMessage());
      LOGGER.debug(e1.getMessage(), e1);
      throw new QuimpException(e1);
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
      throw new QuimpException(e);
    }
  }

  /**
   * Saves selected fluoro map.
   * 
   * @param index map index in {@link #fluoMaps} array.
   * 
   * @throws IOException on file save
   */
  private void saveFluoroMap(int index) throws IOException {
    if (!fluoMaps[index].isEnabled()) {
      LOGGER.debug("Selected map " + (index + 1) + " is not enabled");
    } else {
      String tmpfilename = FileExtensions.fluomapFileExt.replaceFirst("%",
              Integer.toString(fluoMaps[index].channel));
      File f = new File(Qp.outFile.getPath() + tmpfilename);
      QuimPArrayUtils.arrayToFile(fluoMaps[index].getMap(), ",", f);
      LOGGER.info("\tSaved fluoro map at: " + f.getAbsolutePath());
    }
  }

  /**
   * Saves already opened generated motility and convexity images.
   */
  private void saveConvMotImages() {
    // save images
    IJ.saveAs(migImP, "tiff",
            Qp.outFile.getParent() + File.separator + Qp.filename + FileExtensions.motimageFileExt);
    IJ.saveAs(convImP, "tiff", Qp.outFile.getParent() + File.separator + Qp.filename
            + FileExtensions.convimageFileExt);
  }

  /**
   * Convert ImageProcessor created from Map to ImagePlus.
   * 
   * <p>Usually maps are non-square and need to be rescaled for correct presentation, what this
   * method does.
   * 
   * <p><b>Note</b>
   * 
   * <p>Take care about what <tt>ImageProcessor</tt> is used. The <tt>ColorProcessor</tt> is created
   * from 1D array, whereas e.g. <tt>FloatProcessor</tt> from 2D arrays. This causes that the same
   * map will be displayed with different orientation. QuimP natively uses maps presented by
   * <tt>ColorProcessor</tt>, if one uses <tt>FloatProcessor</tt> it must be rotated and flip to
   * maintain correct orientation. See the following example:
   * 
   * <pre>
   * {
   *   &#64;code
   *   float[][] motMap = QuimPArrayUtils.double2float(mapCell.motMap);
   *   ImageProcessor imp = new FloatProcessor(motMap).rotateRight();
   *   imp.flipHorizontal();
   *   mapCell.map2ImagePlus("motility_map", imp).show();
   * }
   * </pre>
   * 
   * @param name Name of the ImagePlus window
   * @param imp Image processor
   * @return Created ImagePlus object
   * @see #map2ColorImagePlus(String, String, double[][], double, double)
   */
  public ImagePlus map2ImagePlus(String name, ImageProcessor imp) {
    ImagePlus ret = new ImagePlus(name, imp);
    resize(ret);
    setCalibration(ret);
    return ret;
  }

  /**
   * Convert raw map to colorscale.
   * 
   * <p><b>warning</b>
   * 
   * <p>Assumes that input 2D array map is regular and not column. This method can be used with data
   * restored from <i>QCONF</i> file in the following way:
   * 
   * <pre>
   * {@code
   * // Maps are correlated in order with Outlines in DataContainer.
   * mapCell.map2ColorImagePlus("motility_map", "rwb",mapCell.motMap, oHs.oHs.get(h).migLimits[0],
   *               oHs.oHs.get(h).migLimits[1]).show();
   * }
   * </pre>
   * 
   * @param name Name of the ImagePlus
   * @param map Map to convert
   * @param palette Palette code, can be "rww" or "rwb".
   * @param min Minimum value in Outline that was used for creation of this map
   * @param max Maximum value in Outline that was used for creation of this map
   * @return Created ImagePlus object
   * @see #map2ImagePlus(String, ImageProcessor)
   * @see QColor#erColorMap2(String, double, double, double) for palettes
   */
  public ImagePlus map2ColorImagePlus(String name, String palette, double[][] map, double min,
          double max) {
    int[] migColor = new int[map.length * map[0].length];
    int pn = 0;
    int t = map.length;
    int res = map[0].length;
    for (int r = 0; r < t; r++) {
      for (int c = 0; c < res; c++) {
        // pN = (c * mapCell.getT()) + r;
        QColor color = QColor.erColorMap2(palette, map[r][c], min, max);
        migColor[pn++] = color.getColorInt();
      }
    }
    return map2ImagePlus(name, new ColorProcessor(res, t, migColor));
  }

  private Vert closestFloor(Outline o, double target, char c, Vert head) {
    // find the vert with coor closest (floored) to target coordinate

    Vert v = head; // the fcoord or cCoord head
    double coord;
    double coordNext;
    do {
      coord = (c == 'f') ? v.fCoord : v.coord;
      coordNext = (c == 'f') ? v.getNext().fCoord : v.getNext().coord;

      if (coord == target) {
        break;
      }
      if (coordNext > target && coord < target) {
        break;
      }

      v = v.getNext();
    } while (v.getNext().getTrackNum() != head.getTrackNum());

    // System.out.println("found fcoord " + v.fCoord);
    return v;
  }

  private double cfraction(Vert v, double target, Vert head) {
    // calc fraction for iterpolation
    double v2coord;
    if (v.getNext().getTrackNum() == head.getTrackNum()) { // passed zero
      v2coord = v.getNext().coord + 1;
      target = (target > v.coord) ? target : target + 1; // not passed zero as all values are zero!
    } else {
      v2coord = v.getNext().coord;
    }

    double frac = (target - v.coord) / (v2coord - v.coord);
    // System.out.println("\tffraction:
    // |v:"+v.coord+"|v2:"+v2coord+"|tar:"+target+"|frac:"+frac);
    if (frac >= 1) {
      frac = frac - 1;
      LOGGER.warn("WARNING- frac corrected: " + frac);
    }
    if (frac > 1 || frac < 0) {
      LOGGER.warn("!WARNING, frac out of range:" + frac);
    }
    return frac;
  }

  private double ffraction(Vert v, double target, Vert head) {
    // calc fraction for iterpolation
    double v2coord;
    if (v.getNext().getTrackNum() == head.getTrackNum()) { // passed zero
      // System.out.println("\tffraction: pass zero. wrap");
      v2coord = v.getNext().fCoord + 1;
      target = (target > v.fCoord) ? target : target + 1; // not passed zero as all values are zero!
    } else {
      v2coord = v.getNext().fCoord;
    }
    double frac = (target - v.fCoord) / (v2coord - v.fCoord);
    // System.out.println("\tffraction:
    // |v:"+v.fCoord+"|v2:"+v2coord+"|tar:"+target+"|frac:"+frac);

    if (frac >= 1) {
      frac = frac - 1;
      LOGGER.warn("WARNING- frac corrected: " + frac);
    }
    if (frac > 1 || frac < 0) {
      LOGGER.warn("WARNING, frac out of range:" + frac);
    }

    if (Double.isNaN(frac)) {
      LOGGER.warn("WARNING, frac is nan:" + frac);
      System.out.println("\tffraction: |v:" + v.fCoord + "|v2:" + v2coord + "|tar:" + target
              + "|frac:" + frac);
      frac = 0.5;
    }
    return frac;
  }

  private double interpCoord(Vert v, double frac, Vert head) {
    double v2Coord = (v.getNext().getTrackNum() == head.getTrackNum()) ? v.getNext().coord + 1
            : v.getNext().coord; // pass zero
    double dis = v2Coord - v.coord;
    double targ = v.coord + (dis * frac);

    if (targ >= 1) {
      targ += -1; // passed zero
    }

    if (targ < 0) {
      LOGGER.error("ERROR: target less than zero");
    }

    return targ;
  }

  private double interpFCoord(Vert v, double frac, Vert head) {
    double v2Coord = (v.getNext().getTrackNum() == head.getTrackNum()) ? v.getNext().fCoord + 1
            : v.getNext().fCoord;
    double dis = v2Coord - v.fCoord;
    double targ = v.fCoord + (dis * frac);

    if (targ >= 1) {
      targ += -1; // passed zero
    }

    if (targ < 0) {
      LOGGER.error("ERROR: target less than zero");
    }

    return targ;
  }

  private double interpolate(double v1, double v2, double frac) {
    return v1 + ((v2 - v1) * frac);
  }

  /**
   * Calculates convexity by smoothing or averaging across nodes.
   */
  private void calcCurvature() {

    Outline o;
    Vert v;

    oh.curvLimits = new double[2];

    for (int f = oh.getStartFrame(); f <= oh.getEndFrame(); f++) {
      o = oh.getStoredOutline(f);
      if (o == null) {
        IJ.log("ERROR: Outline at frame " + f + " is empty");
        continue;
      }

      // update local curvature just in case
      o.updateCurvature();

      // set default curvatures
      v = o.getHead();
      do {
        v.curvatureSmoothed = v.curvatureLocal;
        v.curvatureSum = v.curvatureLocal;
        v = v.getNext();
      } while (!v.isHead());

      averageCurvature(o);
      sumCurvature(o);

      // find min and max of sum curvature

      v = o.getHead();
      if (f == oh.getStartFrame()) {
        oh.curvLimits[1] = v.curvatureSum;
        oh.curvLimits[0] = v.curvatureSum;
      }
      do {
        if (v.curvatureSum > oh.curvLimits[1]) {
          oh.curvLimits[1] = v.curvatureSum;
        }
        if (v.curvatureSum < oh.curvLimits[0]) {
          oh.curvLimits[0] = v.curvatureSum;
        }
        v = v.getNext();
      } while (!v.isHead());
      // System.out.println("Min curv: " + oh.curvLimits[0] + ", max curv:
      // " + oh.curvLimits[1]);

    }

    // System.out.println("curve limits before: " + oh.curvLimits[0] + ", "
    // + oh.curvLimits[1]);
    oh.curvLimits = QuimpToolsCollection.setLimitsEqual(oh.curvLimits);
    // System.out.println("curve limits after: " + oh.curvLimits[0] + ", " +
    // oh.curvLimits[1]);
  }

  private void averageCurvature(Outline o) {

    Vert v;
    Vert tmpV;
    double totalCur;
    double distance;
    int count;

    // avertage over curvatures
    if (Qp.avgCov > 0) {
      // System.out.println("new outline");
      v = o.getHead();
      do {
        // System.out.println("\tnew vert");
        totalCur = v.curvatureLocal; // reset
        count = 1;

        // add up curvatures of prev nodes
        // System.out.println("\t prev nodes");
        tmpV = v.getPrev();
        distance = 0;
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureLocal;
          count++;
          tmpV = tmpV.getPrev();
        } while (distance < Qp.avgCov / 2);

        // add up curvatures of next nodes
        distance = 0;
        tmpV = v.getNext();
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureLocal;
          count++;
          tmpV = tmpV.getNext();
        } while (distance < Qp.avgCov / 2);

        v.curvatureSmoothed = totalCur / count;

        v = v.getNext();
      } while (!v.isHead());
    }
  }

  /**
   * Sum smoothed curavture over a region of the membrane.
   * 
   * @param o the outline
   */
  private void sumCurvature(Outline o) {

    Vert v;
    Vert tmpV;
    double totalCur;
    double distance;
    // avertage over curvatures
    if (Qp.sumCov > 0) {
      LOGGER.trace("summing curv");
      v = o.getHead();
      do {
        // System.out.println("\tnew vert");
        totalCur = v.curvatureSmoothed; // reset
        // add up curvatures of prev nodes
        // System.out.println("\t prev nodes");
        tmpV = v.getPrev();
        distance = 0;
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getNext().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureSmoothed;
          tmpV = tmpV.getPrev();
        } while (distance < Qp.sumCov / 2);

        // add up curvatures of next nodes
        distance = 0;
        tmpV = v.getNext();
        do {
          distance += ExtendedVector2d.lengthP2P(tmpV.getPrev().getPoint(), tmpV.getPoint());
          totalCur += tmpV.curvatureSmoothed;
          tmpV = tmpV.getNext();
        } while (distance < Qp.sumCov / 2);

        v.curvatureSum = totalCur;

        v = v.getNext();
      } while (!v.isHead());
    }

  }

  private void resize(ImagePlus imp) {
    if (T >= res * 0.9) {
      return; // don't resize if its going to compress frames
    }
    ImageProcessor ip = imp.getProcessor();

    // if (Qp.singleImage) {
    ip.setInterpolationMethod(ImageProcessor.NONE);
    // } else {
    // ip.setInterpolationMethod(ImageProcessor.BILINEAR);
    // }

    double vertRes = Math.ceil((double) res / (double) T);
    // System.out.println("OH s: " + oh.getSize() + ",vertres: "+ vertRes);
    mapPixelHeight = 1.0d / vertRes;
    vertRes = T * vertRes;

    // System.out.println("OH s: " + oh.getSize() + ",vertres: "+ vertRes);

    ip = ip.resize(res, (int) vertRes);
    imp.setProcessor(ip);

  }

  private void setCalibration(ImagePlus imp) {
    imp.getCalibration().setUnit("frames");
    imp.getCalibration().pixelHeight = mapPixelHeight;
    imp.getCalibration().pixelWidth = mapPixelWidth;
    LOGGER.debug("PixelWidth=" + mapPixelWidth + " PixelHeight=" + mapPixelHeight);
  }

  /**
   * Return resolution of the map.
   * 
   * <p>This is value set by user in Q Analysis UI.
   * 
   * @return the res
   */
  public int getRes() {
    return res;
  }

  /**
   * Return number of outline points.
   * 
   * <p>This is value obtained after interpolation of Outlines.
   * 
   * @return the T
   */
  public int getT() {
    return T;
  }

  /**
   * getxMap.
   * 
   * @return the xMap
   */
  public double[][] getxMap() {
    return xMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param xmap the xMap to set
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setxMap(double[][] xmap) {
    this.xMap = xmap;
    T = xmap.length;
    res = xmap[0].length;
  }

  /**
   * getyMap.
   * 
   * @return the yMap
   */
  public double[][] getyMap() {
    return yMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param ymap the yMap to set
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setyMap(double[][] ymap) {
    this.yMap = ymap;
    T = ymap.length;
    res = ymap[0].length;
  }

  /**
   * Get coordinates map.
   * 
   * @return the coordMap
   */
  public double[][] getCoordMap() {
    return coordMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param coordMap the coordMap to set
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setCoordMap(double[][] coordMap) {
    this.coordMap = coordMap;
    T = coordMap.length;
    res = coordMap[0].length;
  }

  /**
   * Get origin map.
   * 
   * @return the originMap
   */
  public double[][] getOriginMap() {
    return originMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param originMap the originMap to set
   * 
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setOriginMap(double[][] originMap) {
    this.originMap = originMap;
    T = originMap.length;
    res = originMap[0].length;
  }

  /**
   * getMotMap.
   * 
   * @return the motMap
   */
  public double[][] getMotMap() {
    return motMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param motMap the motMap to set
   * 
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setMotMap(double[][] motMap) {
    this.motMap = motMap;
    T = motMap.length;
    res = motMap[0].length;
  }

  /**
   * getConvMap.
   * 
   * @return the convMap
   */
  public double[][] getConvMap() {
    return convMap;
  }

  /**
   * Set map. Initialise also resolution fields. All maps must have the same resolution.
   * 
   * @param convMap the convMap to set
   * @see STmap#getRes()
   * @see #getT()
   */
  public void setConvMap(double[][] convMap) {
    this.convMap = convMap;
    T = convMap.length;
    res = convMap[0].length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    LOGGER.debug("This class can not be deserialzied without assgning OutlineHndler");
  }
}