/**
 * @file IQuimpCorePlugin.java
 * @date 29 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import uk.ac.warwick.wsbc.QuimP.Nest;

/**
 * This core plugin accepts Nest as input and can modify it
 * 
 * @author p.baniukiewicz
 * @date 29 Jun 2016
 *
 */
public interface IQuimpCorePlugin extends IQuimpPlugin {

    /**
     * Runs filter and modify Nest object
     * 
     * @throws QuimpPluginException on any problems during filter execution
     * @warning Plugin may be run without attached data. Plugin must deal with
     * this
     */
    void runPlugin() throws QuimpPluginException;

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
    void attachData(final Nest data);

}
