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
	 * Runs filter and return filtered points as ordered collection
	 * 
	 * @return Filtered points
	 * @throws QuimpPluginException
	 */
	public List<E> runPlugin() throws QuimpPluginException;
}
