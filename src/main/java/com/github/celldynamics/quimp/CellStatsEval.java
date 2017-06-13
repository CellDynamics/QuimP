package com.github.celldynamics.quimp;

import java.awt.Polygon;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * Calculate statistics for whole stack (all cells).
 * 
 * <p>Stats are written on disk after calling constructor. Additionally there is separate list
 * maintained with the same data. They can be collected calling {@link #getStatH()}. This is due to
 * compatibility with old QuimP.
 * 
 * @author tyson
 * @author p.baniukiewicz
 */
public class CellStatsEval implements Measurements {
  /**
   * Hold all stats for cell. the same data are written to disk as csv file.
   */
  private CellStats statH;

  /**
   * The output H.
   */
  OutlineHandler outputH;

  /**
   * The outfile.
   */
  File outfile;

  /**
   * The i plus.
   */
  ImagePlus iplus;

  /**
   * The i proc.
   */
  ImageProcessor iproc;

  /**
   * The is.
   */
  ImageStatistics is;

  /**
   * The scale.
   */
  double scale;

  /**
   * The frame interval.
   */
  double frameInterval;

  /**
   * Create and run the object.
   * 
   * <p>After creating the object, file with stats is written and stats are available by calling
   * {@link #getStatH()} method.
   * 
   * @param oh OutlineHandler
   * @param ip image associated with OutlineHandler
   * @param f file name to write stats, if null file is not created
   * @param s image scale
   * @param fi frame interval
   */
  public CellStatsEval(OutlineHandler oh, ImagePlus ip, File f, double s, double fi) {
    IJ.showStatus("BOA-Calculating Cell stats");
    outputH = oh;
    outfile = f;
    iplus = ip;
    iproc = ip.getProcessor();
    scale = s;
    frameInterval = fi;

    FrameStatistics[] stats = record();
    iplus.setSlice(1);
    iplus.killRoi();
    if (f == null) {
      buildData(stats);
    } else {
      write(stats, outputH.getStartFrame()); // also call buildData
    }
  }

  /**
   * Only create the object. Stats file is not created but results are available by calling
   * {@link #getStatH()} method.
   * 
   * @param oh OutlineHandler
   * @param ip image associated with OutlineHandler
   * @param s image scale
   * @param fi frame interval
   */
  public CellStatsEval(OutlineHandler oh, ImagePlus ip, double s, double fi) {
    this(oh, ip, null, s, fi);
  }

  /**
   * Calculate stats.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Number of calculated stats must be reflected in {@link #buildData(FrameStatistics[])}.
   * 
   * <p>Scales stored in processed image are used. Even if {@link BOAState.BOAp} stores scale of
   * image, it is used scale from tiff (they are the same as user scale is copied to image in
   * initialisation stage).
   * 
   * 
   * @return Array with stats for every frame for one cell.
   */
  private FrameStatistics[] record() {
    // ImageStack orgStack = orgIpl.getStack();
    FrameStatistics[] stats = new FrameStatistics[outputH.getSize()];

    double distance = 0;
    Outline o;
    PolygonRoi roi;
    int store;

    for (int f = outputH.getStartFrame(); f <= outputH.getEndFrame(); f++) {
      IJ.showProgress(f, outputH.getEndFrame());
      store = f - outputH.getStartFrame();

      o = outputH.getOutline(f);
      iplus.setSlice(f); // also updates the processor
      stats[store] = new FrameStatistics();

      Polygon opoly = o.asPolygon();
      roi = new PolygonRoi(opoly, Roi.POLYGON);

      iplus.setRoi(roi);
      is = iplus.getStatistics(AREA + CENTROID + ELLIPSE + SHAPE_DESCRIPTORS); // this does scale to
      // image

      // all theses already to scale
      stats[store].frame = f;
      stats[store].area = is.area;
      stats[store].centroid.setX(is.xCentroid);
      stats[store].centroid.setY(is.yCentroid);

      stats[store].elongation = is.major / is.minor; // include both axis plus elongation
      stats[store].perimiter = roi.getLength(); // o.getLength();
      stats[store].circularity =
              4 * Math.PI * (stats[store].area / (stats[store].perimiter * stats[store].perimiter));
      stats[store].displacement =
              ExtendedVector2d.lengthP2P(stats[0].centroid, stats[store].centroid);

      if (store != 0) {
        stats[store].speed =
                ExtendedVector2d.lengthP2P(stats[store - 1].centroid, stats[store].centroid);
        distance += ExtendedVector2d.lengthP2P(stats[store - 1].centroid, stats[store].centroid);
        stats[store].dist = distance;
      } else {
        stats[store].dist = 0;
        stats[store].speed = 0;
      }

      if (distance != 0) {
        stats[store].persistance = stats[store].displacement / distance;
      } else {
        stats[store].persistance = 0;
      }

    }

    // convert centroid to pixels
    for (int f = outputH.getStartFrame(); f <= outputH.getEndFrame(); f++) {
      store = f - outputH.getStartFrame();
      stats[store].centroidToPixels(scale);
    }

    return stats;
  }

  private void write(FrameStatistics[] s, int startFrame) {
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(outfile), true); // auto flush
      // IJ.log("Writing to file");
      pw.print("#p2\n#QuimP output - " + outfile.getAbsolutePath() + "\n");
      pw.print("# Centroids are given in pixels.  Distance & speed & area measurements are"
              + " scaled to micro meters\n");
      pw.print("# Scale: " + scale + " micro meter per pixel | Frame interval: " + frameInterval
              + " sec\n");
      pw.print("# Frame,X-Centroid,Y-Centroid,Displacement,Dist. Traveled,"
              + "Directionality,Speed,Perimeter,Elongation,Circularity,Area");

      for (int i = 0; i < s.length; i++) {
        pw.print("\n" + (i + startFrame) + "," + IJ.d2s(s[i].centroid.getX(), 2) + ","
                + IJ.d2s(s[i].centroid.getY(), 2) + "," + IJ.d2s(s[i].displacement) + ","
                + IJ.d2s(s[i].dist) + "," + IJ.d2s(s[i].persistance) + "," + IJ.d2s(s[i].speed)
                + "," + IJ.d2s(s[i].perimiter) + "," + IJ.d2s(s[i].elongation) + ","
                + IJ.d2s(s[i].circularity, 3) + "," + IJ.d2s(s[i].area));

      }

      pw.print("\n#\n# Fluorescence measurements");
      writeDummyFluo(pw, 1, startFrame, s.length);
      writeDummyFluo(pw, 2, startFrame, s.length);
      writeDummyFluo(pw, 3, startFrame, s.length);

      pw.close();
      buildData(s);
    } catch (Exception e) {
      IJ.error("could not open out file");
      return;
    }
  }

  /**
   * Complementary to write method. Create the same data as write but in form of arrays. For
   * compatible reasons.
   * 
   * @param s Frame statistics calculated by
   *        {@link com.github.celldynamics.quimp.CellStatsEval#record()}
   */
  private void buildData(FrameStatistics[] s) {
    statH = new CellStats(s.length, 11, 11);
    // duplicate from write method
    statH.framestat = new ArrayList<FrameStatistics>(Arrays.asList(s));
  }

  /**
   * getStatH.
   * 
   * @return the statH
   */
  public CellStats getStatH() {
    return statH;
  }

  private void writeDummyFluo(PrintWriter pw, int channel, int startFrame, int size)
          throws Exception {
    pw.print("\n#\n# Channel " + channel
            + ";Frame, Total Fluo.,Mean Fluo.,Cortex Width, Cyto. Area,Total Cyto. Fluo.,"
            + " Mean Cyto. Fluo.,"
            + "Cortex Area,Total Cortex Fluo., Mean Cortex Fluo., %age Cortex Fluo.");
    for (int i = 0; i < size; i++) {
      pw.print("\n" + (i + startFrame) + ",-1,-1,-1,-1,-1,-1,-1,-1,-1,-1");
    }
  }
}
