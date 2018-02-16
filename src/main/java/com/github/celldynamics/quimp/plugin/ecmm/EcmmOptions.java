package com.github.celldynamics.quimp.plugin.ecmm;

import java.io.File;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;

/**
 * Hold ECMM options, currently only file name to process.
 * 
 * <p>Note that {@link ECMp} class hold options related to algorithm, not configurable by user.
 * 
 * @author p.baniukiewicz
 *
 */
public class EcmmOptions extends AbstractPluginOptions {

  /**
   * Allow to add file name to options.
   * 
   * <p>For convenient creation of {@link ECMM_Mapping} for API.
   * 
   * @param file file to add
   */
  public EcmmOptions(File file) {
    paramFile = file.getPath();
  }

  /**
   * Default constructor.
   */
  public EcmmOptions() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    EcmmOptions cp = new EcmmOptions();
    cp.paramFile = this.paramFile;
    return cp;
  }
}
