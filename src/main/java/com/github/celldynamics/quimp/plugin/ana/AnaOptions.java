package com.github.celldynamics.quimp.plugin.ana;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;

/**
 * Hold ANA options.
 * 
 * <p>Note that {@link ANAp} class hold options related to algorithm and stored in QCONF. Some
 * settings are duplicated.
 * 
 * @author p.baniukiewicz
 * @see ANAp
 */
public class AnaOptions extends AbstractPluginOptions {

  /**
   * UI setting. Plot outlines on the end.
   */
  public boolean plotOutlines = false;
  /**
   * UI setting. Show results in IJ table at the end
   */
  public boolean fluoResultTable = false;
  /**
   * UI setting. Append result to IJ table.
   */
  public boolean fluoResultTableAppend = false;
  /**
   * The channel. UI setting
   */
  public int channel = 0;
  /**
   * The normalise. UI setting
   */
  public boolean normalise = true;
  /**
   * The sample at same. UI setting
   */
  public boolean sampleAtSame = false;
  /**
   * Whether to clear Fluo stats before.
   */
  public boolean clearFlu = false;
  /**
   * Scale typed in UI.
   * 
   * @see ANAp#setCortextWidthScale(double)
   */
  public double userScale = 1;

  /**
   * Default constructor.
   */
  public AnaOptions() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    AnaOptions cp = new AnaOptions();
    cp.paramFile = this.paramFile;
    cp.plotOutlines = this.plotOutlines;
    cp.fluoResultTableAppend = this.fluoResultTableAppend;
    cp.fluoResultTable = this.fluoResultTable;
    cp.channel = this.channel;
    cp.normalise = this.normalise;
    cp.sampleAtSame = this.sampleAtSame;
    cp.clearFlu = this.clearFlu;
    cp.userScale = this.userScale;
    return cp;
  }

}
