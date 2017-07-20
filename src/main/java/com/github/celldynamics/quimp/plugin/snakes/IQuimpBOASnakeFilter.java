package com.github.celldynamics.quimp.plugin.snakes;

import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.plugin.IQuimpCorePlugin;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on Snakes directly.
 * 
 * <p>Modification of Node object that form Snake should be done with carefulness. Read
 * documentation
 * for Snake
 * 
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.plugin.engine.PluginFactory
 * @see com.github.celldynamics.quimp.Snake
 */
public interface IQuimpBOASnakeFilter extends IQuimpCorePlugin {

  /**
   * Runs filter and return filtered points in the same order as input points.
   * 
   * <p>Number of returned points can be different.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Plugin may be run without attached data. Plugin must deal with this
   * 
   * @return Filtered points
   * @throws QuimpPluginException on any problems during filter execution
   */
  Snake runPlugin() throws QuimpPluginException;

  /**
   * Attach processed data to plugin.
   * 
   * <p>This method allows to process data by plugin without running it what is important e.g. for
   * visualizing.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Plugin may be run without attached data. Plugin must deal with this
   * 
   * @param data snake to connect
   */
  void attachData(final Snake data);
}
