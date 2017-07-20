package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.Nest;

/**
 * This core plugin accepts Nest as input and can modify it.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpNestPlugin extends IQuimpCorePlugin {

  /**
   * Runs filter and modify Nest object.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Plugin may be run without attached data. Plugin must deal with this
   * 
   * @throws QuimpPluginException on any problems during filter execution
   */
  void runPlugin() throws QuimpPluginException;

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
  void attachData(final Nest data);

}
