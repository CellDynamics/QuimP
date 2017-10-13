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
   * List of statistic calculated for subsequent frames for the same object.
   */
  public ArrayList<FrameStatistics> framestat;

  /**
   * Return list of statistic calculated for subsequent frames for the same object.
   * 
   * @return the framestat
   */
  public ArrayList<FrameStatistics> getFramestat() {
    return framestat;
  }

  /**
   * Initialises empty container.
   */
  public CellStats() {
    framestat = new ArrayList<>();
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
   * Get number of frames.
   * 
   * @return the frames
   */
  public int getNumStoredFrames() {
    return framestat.size();
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
