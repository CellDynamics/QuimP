package uk.ac.warwick.wsbc.QuimP.plugin.snakes;

import uk.ac.warwick.wsbc.QuimP.Snake;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on Snakes directly.
 * 
 * Modification of Node object that form Snake should be done with carefulness. Read documentation
 * for Snake
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.QuimP.PluginFactory
 * @see uk.ac.warwick.wsbc.QuimP.Snake
 */
public interface IQuimpBOASnakeFilter extends IQuimpCorePlugin {

    /**
     * Runs filter and return filtered points in the same order as input points.
     * 
     * Number of returned points can be different.
     * <p>
     * <b>Warning</b>
     * <p>
     * Plugin may be run without attached data. Plugin must deal with this
     * 
     * @return Filtered points
     * @throws QuimpPluginException on any problems during filter execution
     */
    Snake runPlugin() throws QuimpPluginException;

    /**
     * Attach processed data to plugin.
     * 
     * This method allows to process data by plugin without running it what is important e.g. for
     * visualizing.
     * <p>
     * <b>Warning</b>
     * <p>
     * Plugin may be run without attached data. Plugin must deal with this
     * 
     * @param data
     */
    void attachData(final Snake data);
}
