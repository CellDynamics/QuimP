package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.Collection;

import uk.ac.warwick.wsbc.tools.images.FilterException;

/**
 * General interface that define filter run on points in euclidean space
 *  
 * @author p.baniukiewicz
 *
 */
public interface IPoint2dFilter<E> {
	
	/**
	 * Runs filter and return filtered points
	 * 
	 * @return Filtered points
	 * @throws FilterException
	 */
	Collection<E>  RunFilter() throws FilterException;

}
