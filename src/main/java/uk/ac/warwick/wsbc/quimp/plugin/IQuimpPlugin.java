package uk.ac.warwick.wsbc.quimp.plugin;

import ij.plugin.PlugIn;

/**
 * This interface supports plugins used in QuimP Bar.
 * <p>
 * Those plugins are stand alone instances that could be run outside other QuimP modules.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPlugin extends IQuimpCorePlugin, PlugIn {

}
