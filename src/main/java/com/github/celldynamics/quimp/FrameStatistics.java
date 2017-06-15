package com.github.celldynamics.quimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.plugin.ana.ANAp;
import com.github.celldynamics.quimp.plugin.ana.ChannelStat;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.measure.ResultsTable;

/**
 * Hold statistic evaluated for one frame and one outline, both geometric and fluorescence.
 * 
 * @author p.baniukiewicz
 * @see CellStatsEval
 * @see ChannelStat
 */
public class FrameStatistics {
  /**
   * Frame number.
   */
  public int frame;
  /**
   * Area of outline.
   */
  public double area;
  /**
   * Centroid of outline.
   */
  public ExtendedVector2d centroid;
  /**
   * Elongation of outline. An ellipse is fitted to the cell outline and the major/minor axis used
   * to compute the elongation of the cell’s shape. Elongation = major axis∕minor axis. Note that a
   * value of 1 does not necessarily represent a perfectly circular cell, only a circular fitted
   * ellipse.
   */
  public double elongation;
  /**
   * Circularity of outline. A measure of circularity defined by the following equation:
   * 4*PI*Area/(Perimeter*Perimeter). A value of 1 reveals the cell’s outline to be perfectly
   * circular.
   */
  public double circularity;
  /**
   * Perimeter of outline. Length of the cell perimeter (segmented outline).
   */
  public double perimiter;
  /**
   * Displacement of outline. Distance the cell centroid has moved from its position in the first
   * recorded frame.
   */
  public double displacement;
  /**
   * Cumulated distance that cell moved from the first recorded frame. (sum of distances between i-1
   * and i frame)
   */
  public double dist;
  /**
   * Persistence of outline. Persistence in direction, calculated as Displacement∕Dist.Travelled
   * (chemotaxis index). A value of 1 reveals that a cell has moved in a straight line. Decreasing
   * values denote a cell moving increasingly erratically.
   */
  public double persistance;
  /**
   * Speed at which the centroid moved between the current and previous frame.
   */
  public double speed;
  /**
   * Unknown.
   * 
   * @deprecated Not used
   */
  public double persistanceToSource;
  /**
   * Dispersion of outline.
   * 
   * @deprecated Not used
   */
  public double dispersion;
  /**
   * Extension of outline.
   * 
   * @deprecated Not used
   */
  public double extension;
  /**
   * Fluorescence stats added by ANA module.
   */
  public ChannelStat[] channels;

  /**
   * Default constructor, create empty container.
   */
  public FrameStatistics() {
    centroid = new ExtendedVector2d();
    channels = new ChannelStat[3];
    channels[0] = new ChannelStat();
    channels[1] = new ChannelStat();
    channels[2] = new ChannelStat();
  }

  /**
   * Rescale scalable parameters (area, perimeter,etc) to SI units.
   * 
   * @param scale scale
   * @param frameInterval frame interval
   */
  public void toScale(double scale, double frameInterval) {
    area = QuimpToolsCollection.areaToScale(area, scale);
    perimiter = QuimpToolsCollection.distanceToScale(perimiter, scale);
    displacement = QuimpToolsCollection.distanceToScale(displacement, scale);
    dist = QuimpToolsCollection.distanceToScale(dist, scale);
    speed = QuimpToolsCollection.speedToScale(speed, scale, frameInterval); // over 1 frame
  }

  /**
   * Re-scale centroid to pixels (from SI).
   * 
   * @param scale scale
   */
  void centroidToPixels(double scale) {
    centroid.setXY(centroid.getX() / scale, centroid.getY() / scale);
  }

  /**
   * Clear channel statistics.
   * 
   * @see ChannelStat
   */
  public void clearFluo() {
    this.channels[0] = new ChannelStat();
    this.channels[1] = new ChannelStat();
    this.channels[2] = new ChannelStat();
  }

  /**
   * Write stat file in old format.
   * 
   * @param s statistics to write
   * @param outfile file name
   * @param anap ANA configuration object
   * @throws IOException on file error
   * @see ANAp
   */
  public static void write(FrameStatistics[] s, File outfile, ANAp anap) throws IOException {
    PrintWriter pw = new PrintWriter(new FileWriter(outfile), true); // auto flush
    IJ.log("Writing to file");
    pw.print("#p2\n#QuimP ouput - " + outfile.getAbsolutePath() + "\n");
    pw.print("# Centroids are given in pixels.  Distance & speed & area measurements are scaled"
            + " to micro meters\n");
    pw.print("# Scale: " + anap.scale + " micro meter per pixel | Frame interval: "
            + anap.frameInterval + " sec\n");
    pw.print("# Frame,X-Centroid,Y-Centroid,Displacement,Dist. Traveled,"
            + "Directionality,Speed,Perimeter,Elongation,Circularity,Area");

    for (int i = 0; i < s.length; i++) {
      pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].centroid.getX(), 2) + ","
              + IJ.d2s(s[i].centroid.getY(), 2) + "," + IJ.d2s(s[i].displacement) + ","
              + IJ.d2s(s[i].dist) + "," + IJ.d2s(s[i].persistance) + "," + IJ.d2s(s[i].speed) + ","
              + IJ.d2s(s[i].perimiter) + "," + IJ.d2s(s[i].elongation) + ","
              + IJ.d2s(s[i].circularity, 3) + "," + IJ.d2s(s[i].area));
    }
    pw.print("\n#\n# Fluorescence measurements");
    writeFluo(s, pw, 0);
    writeFluo(s, pw, 1);
    writeFluo(s, pw, 2);
    pw.close();
  }

  private static void writeFluo(FrameStatistics[] s, PrintWriter pw, int c) {
    pw.print("\n#\n# Channel " + (c + 1)
            + ";Frame, Total Fluo.,Mean Fluo.,Cortex Width, Cyto. Area,Total Cyto. Fluo.,"
            + " Mean Cyto. Fluo.,"
            + "Cortex Area,Total Cortex Fluo., Mean Cortex Fluo., %age Cortex Fluo.");
    for (int i = 0; i < s.length; i++) {
      pw.print("\n" + s[i].frame + "," + IJ.d2s(s[i].channels[c].totalFluor) + ","
              + IJ.d2s(s[i].channels[c].meanFluor) + "," + IJ.d2s(s[i].channels[c].cortexWidth));
      pw.print("," + IJ.d2s(s[i].channels[c].innerArea) + ","
              + IJ.d2s(s[i].channels[c].totalInnerFluor) + ","
              + IJ.d2s(s[i].channels[c].meanInnerFluor));
      pw.print("," + IJ.d2s(s[i].channels[c].cortexArea) + ","
              + IJ.d2s(s[i].channels[c].totalCorFluo) + "," + IJ.d2s(s[i].channels[c].meanCorFluo)
              + "," + IJ.d2s(s[i].channels[c].percCortexFluo));
    }
  }

  /**
   * Load statistics from old file format.
   * 
   * @param infile file to read
   * @return Array of statistics for all object in frame
   * @throws IOException on file error
   */
  public static FrameStatistics[] read(File infile) throws IOException {

    BufferedReader br = new BufferedReader(new FileReader(infile));
    String thisLine;
    int i = 0;
    // count the number of frames in .scv file
    while ((thisLine = br.readLine()) != null) {
      if (thisLine.startsWith("# Fluorescence measurements")) {
        break;
      }
      if (thisLine.startsWith("#")) {
        continue;
      }
      // System.out.println(thisLine);
      i++;
    }
    br.close();
    FrameStatistics[] stats = new FrameStatistics[i];

    i = 0;
    String[] split;
    br = new BufferedReader(new FileReader(infile)); // re-open and read
    while ((thisLine = br.readLine()) != null) {
      if (thisLine.startsWith("# Channel")) { // reached fluo stats
        break;
      }
      if (thisLine.startsWith("#")) {
        continue;
      }
      // System.out.println(thisLine);

      split = thisLine.split(",");

      stats[i] = new FrameStatistics();
      stats[i].frame = (int) QuimpToolsCollection.s2d(split[0]);
      stats[i].centroid.setXY(QuimpToolsCollection.s2d(split[1]),
              QuimpToolsCollection.s2d(split[2]));
      stats[i].displacement = QuimpToolsCollection.s2d(split[3]);
      stats[i].dist = QuimpToolsCollection.s2d(split[4]);
      stats[i].persistance = QuimpToolsCollection.s2d(split[5]);
      stats[i].speed = QuimpToolsCollection.s2d(split[6]);
      stats[i].perimiter = QuimpToolsCollection.s2d(split[7]);
      stats[i].elongation = QuimpToolsCollection.s2d(split[8]);
      stats[i].circularity = QuimpToolsCollection.s2d(split[9]);
      stats[i].area = QuimpToolsCollection.s2d(split[10]);

      i++;
    }

    readChannel(0, stats, br);
    readChannel(1, stats, br);
    readChannel(2, stats, br);

    br.close();
    return stats;
  }

  private static void readChannel(int c, FrameStatistics[] stats, BufferedReader br)
          throws IOException {
    String thisLine;
    String[] split;
    int i = 0;
    while ((thisLine = br.readLine()) != null) {
      if (thisLine.startsWith("# Channel")) {
        break;
      }
      if (thisLine.startsWith("#")) {
        continue;
      }

      split = thisLine.split(",");
      // split[0] == frame
      stats[i].channels[c].totalFluor = QuimpToolsCollection.s2d(split[1]);
      stats[i].channels[c].meanFluor = QuimpToolsCollection.s2d(split[2]);
      stats[i].channels[c].cortexWidth = QuimpToolsCollection.s2d(split[3]);
      stats[i].channels[c].innerArea = QuimpToolsCollection.s2d(split[4]);
      stats[i].channels[c].totalInnerFluor = QuimpToolsCollection.s2d(split[5]);
      stats[i].channels[c].meanInnerFluor = QuimpToolsCollection.s2d(split[6]);
      stats[i].channels[c].cortexArea = QuimpToolsCollection.s2d(split[7]);
      stats[i].channels[c].totalCorFluo = QuimpToolsCollection.s2d(split[8]);
      stats[i].channels[c].meanCorFluo = QuimpToolsCollection.s2d(split[9]);
      stats[i].channels[c].percCortexFluo = QuimpToolsCollection.s2d(split[10]);

      i++;
    }
  }

  /**
   * Add channel statistic to given ResultsTable.
   * 
   * @param rt IJ result table
   * @param channelno channel number for fluoro stats
   */
  public void addFluoToResultTable(ResultsTable rt, int channelno) {
    // Those fields must be related to writeFluo
    ChannelStat cs = channels[channelno]; // reference to channel
    rt.incrementCounter();
    rt.addValue("frame", frame);
    rt.addValue("TotalFluo", cs.totalFluor);
    rt.addValue("MeanFluo", cs.meanFluor);
    rt.addValue("Cortex Width", cs.cortexWidth);
    rt.addValue("Cyto. Area", cs.innerArea);
    rt.addValue("Total Cyto. Fluo.", cs.totalInnerFluor);
    rt.addValue("Mean Cyto. Fluo.h", cs.meanInnerFluor);
    rt.addValue("Cortex Area", cs.cortexArea);
    rt.addValue("Total Cortex Fluo.", cs.totalCorFluo);
    rt.addValue("Mean Cortex Fluo.", cs.meanCorFluo);
    rt.addValue("%age Cortex Fluo.", cs.percCortexFluo);
  }

}