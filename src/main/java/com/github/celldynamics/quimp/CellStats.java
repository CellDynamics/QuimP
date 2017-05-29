package com.github.celldynamics.quimp;

import java.util.ArrayList;

import ij.measure.ResultsTable;

// TODO: Auto-generated Javadoc
/**
 * Keep statistics (outline and fluoro) for one cell along frames.
 * 
 * @author p.baniukiewicz
 *
 */
public class CellStats {

  /**
   * Number of frames stored in.
   */
  private int frames;
  /**
   * Number of parameters stored for cell statistic. This is like number of columns in output
   * array.
   */
  private int statsElements;
  /**
   * Number of parameters stored for fluoro statistic. This is like number of columns in output
   * array.
   */
  private int fluoElements;

  /**
   * List of statistic calculated for subsequent frames for the same object.
   */
  public ArrayList<FrameStatistics> framestat;

  /**
   * 
   */
  public CellStats() {
    framestat = new ArrayList<>();
    frames = 0;
    statsElements = 0;
    fluoElements = 0;
  }

  /**
   * @param frames
   * @param statsElements
   * @param fluoElements
   */
  public CellStats(int frames, int statsElements, int fluoElements) {
    this.frames = frames;
    this.statsElements = statsElements;
    this.fluoElements = fluoElements;
    framestat = new ArrayList<>(frames);
  }

  /**
   * @return the frames
   */
  public int getFrames() {
    return frames;
  }

  /**
   * @return the statsElements
   */
  public int getStatsElements() {
    return statsElements;
  }

  /**
   * @return the fluoElements
   */
  public int getFluoElements() {
    return fluoElements;
  }

  /**
   * Add results stored in {@link FrameStatistics} object to ResultTable for all frames.
   * 
   * @param rt table to fill
   * @param channelno channel number
   * @see FrameStatistics#addFluoToResultTable(ResultsTable, int)
   */
  public void addFluosToResultTable(ResultsTable rt, int channelno) {
    for (FrameStatistics fs : framestat) {
      fs.addFluoToResultTable(rt, channelno);
    }

  }
}
