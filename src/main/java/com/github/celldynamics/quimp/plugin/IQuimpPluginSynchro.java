package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.SnakePluginList;
import com.github.celldynamics.quimp.ViewUpdater;

/**
 * The interface that add synchronisation between plugin and QuimP.
 * 
 * <p>Plugin can refresh main QuimP screen and redraw its content. Every outline is processed by all
 * active plugins and then redrawn on QuimP main screen. Additionally current plugin configuration
 * is transferred and stored in QuimP.
 * 
 * @author p.baniukiewicz
 * @see SnakePluginList
 */
public interface IQuimpPluginSynchro {

  /**
   * Pass to plugin ViewUpdater object which is accessor to selected methods from QuimP interface.
   * 
   * <p>The main role of ViewUpdater is to limit methods that plugin can call to avoid accidental
   * data destruction.
   * 
   * @param b Reference to ViewUpdater that holds selected methods from main QuimP object
   * @see ViewUpdater to check what methods are exposed to plugin.
   */
  void attachContext(final ViewUpdater b);

}
