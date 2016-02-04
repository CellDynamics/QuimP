/**
 * @file IQuimpPlugin.java
 * @date 2 Feb 2016
 */
package uk.ac.warwick.wsbc.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * General definition of plugin interface for QuimP
 * 
 * Contain also flags understood by plugins
 *  
 * @author p.baniukiewicz
 * @date 2 Feb 2016
 * @todo finish documentation and add UML graphs
 */
public interface IQuimpPlugin {
	
	public int GENERAL = 0; ///< Type of plugin not defined
	public int DOES_SNAKES = 1; ///< Plugin process snakes only
	
	/**
	 * Provide basic information to QuimP about plugin
	 * 
	 * @warning
	 * It must return at least type of plugin
	 * @return Combination of flags specifying:
	 * -# type of plugin (obligatory)
	 */
	public int setup();
	
	/**
	 * Pass to plugin its configuration data as pairs <key,value>
	 * 
	 * @param par
	 * @throws QuimpPluginException on problems with understanding parameters by plugin
	 * e.g. \b key is not understood or casting from \c Object \b value to other type
	 * has not been successful. 
	 */
	public void setPluginConfig(HashMap<String,Object> par) throws QuimpPluginException;
	
	/**
	 * Retrieve plugin configuration data as pairs <key,value>
	 * 
	 * This configuration is not used by QuimP but it may be stored in QuimP configuration
	 * 
	 * @return
	 */
	public Map<String,Object> getPluginConfig();
	
	/**
	 * Show or hide plugin UI
	 * 
	 * UI is not obligatory. This function must be implemented but may do nothing.
	 * 
	 * @param val
	 */
	public void showUI(boolean val);
}
