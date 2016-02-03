package uk.ac.warwick.wsbc.plugin.snakefilter;

import java.util.List;

import uk.ac.warwick.wsbc.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on points in euclidean space
 *  
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPoint2dFilter<E> extends IQuimpPlugin {
	
	/**
	 * Runs filter and return filtered points in the same order as input points
	 * 
	 * Number of points can be different.
	 * 
	 * @return Filtered points
	 * @throws QuimpPluginException on any problems during filter execution
	 */
	public List<E> runPlugin() throws QuimpPluginException;
	
	/**
	 * Attach data to process to plugin
	 * 
	 * @todo attached data can be separate class type like QuimpDataAccessor?
	 * general base class + some derived to cover transformations from other types 
	 * or one class with several methods that return X,Y,image,etc. If any data
	 * not available method throw exception.
	 * @param data
	 */
	public void attachData(List<E> data);
}
