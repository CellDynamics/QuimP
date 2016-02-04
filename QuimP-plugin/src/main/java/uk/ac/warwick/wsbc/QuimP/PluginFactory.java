/**
 * @file PluginFactory.java
 * @date 4 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.tools.images.filters.HatFilter_test;

/**
 * 
 * 
 * @author p.baniukiewicz
 * @date 4 Feb 2016
 *
 */
public class PluginFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PluginFactory.class.getName()); 
	private HashMap<String, Path> snakePlugins;
	
	/**
	 * 
	 */
	public PluginFactory(Path path) {
		logger.debug("Attached " + path.toString());
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 */
	private void scanDirectory() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Return list of plugins of given types.
	 * 
	 * @param type Type defined in uk.ac.warwick.wsbc.plugin.IQuimpPlugin
	 * @return List of names of plugins of type \c type
	 */
	List<String> getPluginNames(int type) {
		return null;
		// TODO Auto-generated constructor stub
	}
	
	
	IQuimpPlugin getInstance(String name) {
		return null;
		// TODO Auto-generated constructor stub
	}

}
