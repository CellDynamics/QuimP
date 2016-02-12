package uk.ac.warwick.wsbc.plugin.snakes;

import java.util.List;

import uk.ac.warwick.wsbc.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on points in euclidean space
 * 
 * @author p.baniukiewicz
 * @todo TODO Finish documentation
 * @todo TODO General specification E may not be useful here as QuimP always
 * call it as \c Vector2d. Remove if not useful
 */
public interface IQuimpPoint2dFilter<E> extends IQuimpPlugin {

    /**
     * Runs filter and return filtered points in the same order as input points
     * 
     * Number of returned points can be different.
     * 
     * @return Filtered points
     * @throws QuimpPluginException on any problems during filter execution
     */
    public List<E> runPlugin() throws QuimpPluginException;

    /**
     * Attach data to process to plugin
     * 
     * @param data
     */
    public void attachData(List<E> data);
}
