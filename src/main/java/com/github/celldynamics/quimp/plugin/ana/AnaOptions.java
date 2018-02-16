package com.github.celldynamics.quimp.plugin.ana;

import java.io.File;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.ecmm.ECMM_Mapping;

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
   * Allow to add file name to options.
   * 
   * <p>For convenient creation of {@link ECMM_Mapping} for API.
   * 
   * @param file file to add
   */
  public AnaOptions(File file) {
    super();
    paramFile = file.getPath();
  }

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
    return cp;
  }

}
