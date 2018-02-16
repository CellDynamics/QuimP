package com.github.celldynamics.quimp.plugin;

import ij.plugin.filter.PlugInFilter;

/**
 * Interface for plugin filter used in QuimP Bar.
 * 
 * <p>Those plugins are stand alone instances that could be run outside the QuimP.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpFilter extends PlugInFilter {

  /**
   * Return plugin description.
   * 
   * @return Plugin description
   */
  public String about();
}
