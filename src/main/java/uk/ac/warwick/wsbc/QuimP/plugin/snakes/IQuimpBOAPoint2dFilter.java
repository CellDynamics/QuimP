package uk.ac.warwick.wsbc.QuimP.plugin.snakes;

import java.util.List;

import javax.vecmath.Point2d;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * General interface that defines filter run on points in euclidean space.
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.QuimP.PluginFactory
 */
public interface IQuimpBOAPoint2dFilter extends IQuimpCorePlugin {

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
    List<Point2d> runPlugin() throws QuimpPluginException;

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
    void attachData(final List<Point2d> data);
}
