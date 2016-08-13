package uk.ac.warwick.wsbc.QuimP.plugin;

import uk.ac.warwick.wsbc.QuimP.Nest;

/**
 * This core plugin accepts Nest as input and can modify it
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpCorePlugin extends IQuimpPlugin {

    /**
     * Runs filter and modify Nest object.
     * 
     * @throws QuimpPluginException on any problems during filter execution
     * @warning Plugin may be run without attached data. Plugin must deal with
     * this
     */
    void runPlugin() throws QuimpPluginException;

    /**
     * Attach processed data to plugin.
     * <p>
     * This method allows to process data by plugin without running it what is important e.g. 
     * for visualizing. 
     * <p>
     * <b>warning</b><p>
     * <tt>data</tt> may be passed as <tt>null</tt> from QuimP. Plugin must deal with
     * this.
     * 
     * @param data
     */
    void attachData(final Nest data);

}
