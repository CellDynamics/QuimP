package uk.ac.warwick.wsbc.QuimP.plugin;

/**
 * This interface supports plugins used in QuimP Bar.
 * <p>
 * Those plugins are stand alone instances that could be run outside other QuimP modules.
 * 
 * @author p.baniukiewicz
 *
 */
public interface IQuimpPlugin extends IQuimpCorePlugin {
    /**
     * Run plugin.
     * 
     * This method should deal with data loading and processing
     * @throws QuimpPluginException on any problems during filter execution
     */
    void runPlugin() throws QuimpPluginException;

}
