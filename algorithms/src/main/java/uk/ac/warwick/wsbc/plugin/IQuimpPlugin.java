/**
 * @file IQuimpPlugin.java
 * @date 2 Feb 2016
 */
package uk.ac.warwick.wsbc.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * General definition of plugin interface for QuimP
 * 
 * Contain also flags understood by plugins
 * 
 * @author p.baniukiewicz
 * @date 2 Feb 2016
 * @todo TODO finish documentation and add UML graphs
 */
public interface IQuimpPlugin {

    int GENERAL = 0; ///< Type of plugin not defined
    int DOES_SNAKES = 1; ///< Plugin process snakes only
    int CHANGE_SIZE = 32; ///< Plugin change size of input data

    /**
     * Provide basic information to QuimP about plugin
     * 
     * @warning It must return at least type of plugin
     * @return Combination of flags specifying:
     * -# type of plugin (obligatory)
     * -# modification of input size (e.g. reduction of polygon points)
     */
    int setup();

    /**
     * Pass to plugin its configuration data as pairs <key,value>
     * 
     * This method is used for restoring configuration from config files
     * maintained by caller. Caller do not modify these values and only stores
     * them as the were returned by getPluginConfig() method.
     * 
     * @remarks Numerical values should be passed as Double
     * @warning setPluginConfig(HashMap<String,Object>) and getPluginConfig()
     * should use the same convention of key naming and parameters
     * casting
     * @param par
     * @throws QuimpPluginException
     * on problems with understanding parameters by plugin e.g. \b
     * key is not understood or casting from \c Object \b value to
     * other type has not been successful.
     */
    void setPluginConfig(HashMap<String, Object> par)
            throws QuimpPluginException;

    /**
     * Retrieve plugin configuration data as pairs <key,value>
     * 
     * This configuration is not used by QuimP but it may be stored in QuimP
     * configuration
     * 
     * @return
     * @see setPluginConfig(HashMap<String,Object>)
     */
    Map<String, Object> getPluginConfig();

    /**
     * Show or hide plugin UI
     * 
     * UI is not obligatory. This function must be implemented but may do
     * nothing.
     * 
     * @param val
     */
    void showUI(boolean val);

    /**
     * Get version of plugin
     * 
     * Versioning may be used for detecting incompatibilities between
     * configurations. Plugin version is saved in QuimP config files, and then
     * passed to plugin by setPluginConfig(HashMap<String,Object>) as \a version
     * key. This key is not available if plugin has not provided its version.
     * The plugin is responsible for parsing this parameter.
     * 
     * @return String with version (any format) or \c null if not supported
     */
    String getVersion();
}
