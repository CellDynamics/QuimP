package com.github.celldynamics.quimp.plugin.generatemask;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;

/**
 * Keep parameters for {@link GenerateMask_} plugin.
 * 
 * @author p.baniukiewicz
 * @see AbstractPluginOptions
 */
public class GenerateMaskOptions extends AbstractPluginOptions {
  // paramFile is defined already

  /**
   * If true generate binary image, grayscales otherwise (each separated object at different shade
   * starting from 1).
   */
  public boolean binary = true;

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    GenerateMaskOptions cp = new GenerateMaskOptions();
    cp.binary = this.binary;
    cp.paramFile = this.paramFile;
    return cp;
  }
}
