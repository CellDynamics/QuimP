package uk.ac.warwick.wsbc.QuimP.plugin.snakes;

import java.util.List;

import javax.vecmath.Point2d;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on points in euclidean space
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.QuimP.PluginFactory
 * @todo TODO Replace in future by IQuimpSnakeFilter
 */
public interface IQuimpPoint2dFilter extends IQuimpPlugin {

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
    List<Point2d> runPlugin() throws QuimpPluginException;

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
    void attachData(final List<Point2d> data);
}
