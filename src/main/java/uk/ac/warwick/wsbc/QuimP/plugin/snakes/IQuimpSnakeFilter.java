/**
 * @file IQuimpSnakeFilter.java
 * @date 4 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.snakes;

import uk.ac.warwick.wsbc.QuimP.Snake;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on Snakes directly
 * 
 * Modification of Node object that form Snake should be done with carefulness. Read documentation 
 * for Snake 
 * @author p.baniukiewicz
 * @date 4 Apr 2016
 * @see uk.ac.warwick.wsbc.QuimP.PluginFactory
 * @see uk.ac.warwick.wsbc.QuimP.Snake
 */
public interface IQuimpSnakeFilter extends IQuimpPlugin {

    /**
     * Runs filter and return filtered points in the same order as input points
     * 
     * Number of returned points can be different.
     * 
     * @return Filtered points
     * @throws QuimpPluginException on any problems during filter execution
     * @warning Plugin may be run without attached data. Plugin must deal with
     * this
     */
    Snake runPlugin() throws QuimpPluginException;

    /**
     * Attach processed data to plugin
     * 
     * This method allows to process data by plugin without running it what is important e.g. 
     * for visualizing. 
     * 
     * @param data
     * @warning \c data may be passed as \c null from QuimP. Plugin must deal with
     * this
     */
    void attachData(final Snake data);
}
