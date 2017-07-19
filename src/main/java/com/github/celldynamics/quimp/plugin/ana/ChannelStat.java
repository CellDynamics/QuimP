package com.github.celldynamics.quimp.plugin.ana;

/**
 * Hold statistic of fluorescence for one channel.
 * 
 * @author p.baniukiewicz
 *
 */
public class ChannelStat {

  /**
   * Area of inner contour - <b>scaled</b>.
   */
  public double innerArea = -1;
  /**
   * Mean intensity in outer contour * outer contour area. This is <b>not scaled</b> to any unit.
   */
  public double totalFluor = -1;
  /**
   * Scaled width of the cortex.
   */
  public double cortexWidth = -1;
  /**
   * Mean intensity within outer contour.
   */
  public double meanFluor = -1;
  /**
   * Mean intensity of inner contour.
   */
  public double meanInnerFluor = -1;
  /**
   * Mean intensity within inner contour * <b>unscaled</b> area of inner contour.
   */
  public double totalInnerFluor = -1;
  /**
   * Scaled outer area - scaled inner area.
   * 
   * <p>Outer area is read from <tt>stQP</tt> file and it is computed by BOA on exit:
   * ({@link com.github.celldynamics.quimp.Nest#analyse(ij.ImagePlus, boolean)} and then
   * {@link com.github.celldynamics.quimp.CellStatsEval})
   */
  public double cortexArea = -1;
  /**
   * Mean intensity in outer contour - mean intensity within inner contour.
   */
  public double totalCorFluo = -1;
  /**
   * totalCorFluo / (outer contour area (not scaled) - inner contour area (not scaled)).
   */
  public double meanCorFluo = -1;
  /**
   * totalCorFluo / totalFluor * 100.
   */
  public double percCortexFluo = -1;

  /**
   * Default constructor. Does nothing.
   */
  public ChannelStat() {
  }

}