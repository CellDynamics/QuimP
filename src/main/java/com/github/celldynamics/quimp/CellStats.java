package com.github.celldynamics.quimp;

import java.util.ArrayList;

import ij.measure.ResultsTable;

/**
 * Keep statistics (outline and fluoro) for one cell along frames. This is format used by new QCONF
 * file.
 * 
 * @author p.baniukiewicz
 *
 */
public class CellStats {

  /**
   * Number of parameters stored for cell statistic. This is like number of columns in output
   * array.
   */
  @Deprecated
  private int statsElements;
  /**
   * Number of parameters stored for fluoro statistic. This is like number of columns in output
   * array.
   */
  @Deprecated
  private int fluoElements;

  /**
   * List of statistic calculated for subsequent frames for the same object.
   */
  public ArrayList<FrameStatistics> framestat;

  /**
   * Initialises empty container.
   */
  public CellStats() {
    framestat = new ArrayList<>();
    statsElements = 0;
    fluoElements = 0;
  }

  /**
   * Initialises container.
   * 
   * @param framestat stats for subsequent frames for one cell
   */
  public CellStats(ArrayList<FrameStatistics> framestat) {
    this.framestat = framestat;
  }

  /**
   * Initialises container.
   * 
   * @param frames number of frames that cell appears on
   * @param statsElements number of stats to be calculated
   * @param fluoElements number of stats to be calculated
   * @deprecated statsElements not used
   */
  public CellStats(int frames, int statsElements, int fluoElements) {
    this.statsElements = statsElements;
    this.fluoElements = fluoElements;
    framestat = new ArrayList<>(frames);
  }

  /**
   * Get number of frames.
   * 
   * @return the frames
   */
  public int getNumStoredFrames() {
    return framestat.size();
  }

  /**
   * Get number of statsElements.
   * 
   * @return the statsElements
   * @deprecated Not used
   */
  public int getStatsElements() {
    return statsElements;
  }

  /**
   * Get number of fluoElements.
   * 
   * @return the fluoElements
   * @deprecated Not used
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
