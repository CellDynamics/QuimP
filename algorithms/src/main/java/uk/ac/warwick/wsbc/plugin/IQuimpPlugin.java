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
 * @author p.baniukiewicz
 * @date 2 Feb 2016
 *
 */
public interface IQuimpPlugin {
	
	/**
	 * 
	 * @return
	 */
	public int setup();
	
	/**
	 * Pass to plugin its configuration data as pairs <key,value>
	 * 
	 * @param par
	 */
	public void setPluginConfig(HashMap<String,Object> par);
	
	/**
	 * Retrieve plugin configuration data as pairs <key,value>
	 * 
	 * @return
	 */
	public Map<String,Object> getPluginConfig();
	
	/**
	 * Attach data to process to plugin
	 * 
	 * @todo attached data can be separate class type like QuimpDataAccessor?
	 * @param data
	 */
	public void attachData(Object data);
	
	/**
	 * Run plugin, returned is copy of processed data attached by attachData(Object)
	 * 
	 * @return
	 * @throws QuimpPluginException
	 */
	public Object runPlugin() throws QuimpPluginException;
}
