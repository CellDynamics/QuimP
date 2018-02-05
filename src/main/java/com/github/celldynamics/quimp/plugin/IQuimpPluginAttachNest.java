package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.Nest;

/**
 * Allow to attach Nest to filter.
 * 
 * @author p.baniukiewicz
 * @see Nest
 */
public interface IQuimpPluginAttachNest {

  /**
   * Attach processed data to plugin.
   * 
   * <p>This method allows to process data by plugin without running it what is important e.g. for
   * visualizing.
   * 
   * <p><b>warning</b>
   * 
   * <p><tt>data</tt> may be passed as <tt>null</tt> from QuimP. Plugin must deal with this.
   * 
   * @param data nest to connect
   */
  void attachNest(final Nest data);
}
