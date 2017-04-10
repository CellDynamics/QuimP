package uk.ac.warwick.wsbc.quimp.plugin;

import ij.plugin.filter.PlugInFilter;

/**
 * This interface supports plugins used in QuimP Bar.
 * 
 * <p>Those plugins are stand alone instances that could be run outside the QuimP.
 * 
 * <p>This interface is for future use. It combines IJ plugins and QuimP plugins. If user
 * implemented
 * all methods from parent interfaces, the plugin would be used in both IJ and QuimP.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPluginFilter extends IQuimpCorePlugin, PlugInFilter {

}
