/**
 * @file SnakePluginList.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * Ordered list of plugins related to snake processing. 
 * 
 * Related to GUI, first plugin is at index 0, etc. Keeps also UI settings activating or
 * deactivating plugins. Produces plugins from their names using provided 
 * uk.ac.warwick.wsbc.QuimP.PluginFactory
 * 
 * @remarks This class is serializable and it is part of QuimP config.  
 * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 */
class SnakePluginList {
    private static final Logger LOGGER = LogManager.getLogger(SnakePluginList.class.getName());
    private transient PluginFactory pluginFactory;

    /**
     * Keeps all Plugin related information and produces plugin instance using PluginFactory
     * 
     * Fields like \c config and \c ver are used during saving so they are initialized in this
     * time only and they are not valid during object lifetime.
     *  
     * @author p.baniukiewicz
     * @date 21 Mar 2016
     *
     */
    class Plugin {
        public transient IQuimpPlugin ref; // !< Reference to plugin instance
        public boolean isActive; // !< Is activate in GUI?
        public String name; // !< Name of plugin delivered from PluginFactory, used to create plugin
        public ParamList config; // !< Configuration read from plugin on save operation
        public String ver; // !< Version read from plugin on save operation

        /**
         * Initializes empty default plugin
         */
        public Plugin() {
            ref = null;
            isActive = true; // !< Default value
            name = "";
            config = null; // no config or not supported by plugin
            ver = ""; // no version or not supported
        }

        /**
         * Main constructor
         * 
         * @remarks If \c name is not found in registered names of plugins in provided 
         * PluginFactory \c pf, the reference \c ref will be \c null
         * @param name Name of plugin to be instanced
         * @param isActive 
         * @param pf PluginFactory that provides plugin objects
         */
        public Plugin(String name, boolean isActive, PluginFactory pf) {
            this.isActive = isActive;
            ref = pf.getInstance(name); // create instance of plugin
            this.name = name;
        }

        /**
         * Check if all execution conditions are met
         * 
         * These conditions are:
         * -# Plugin exist
         * -# Plugin is activated in UI
         * 
         * @return \c true if plugin can be executed
         */
        public boolean isExecutable() {
            if (ref == null)
                return false;
            else
                return isActive;
        }

        /**
         * Load configuration stored in \c config into plugin. 
         * 
         * Used during restoring state of plugin on load. 
         * @return
         * @throws QuimpPluginException When configuration has not been accepted
         */
        public void uploadPluginConfig() throws QuimpPluginException {
            if (ref != null && config != null) {
                ref.setPluginConfig(config);
            } else
                LOGGER.warn(
                        "Config can not be loaded to plugin. Reference is null or config is null");

        }

        public void downloadPluginConfig() {
            if (ref != null) {
                config = ref.getPluginConfig();
                ver = ref.getVersion();
            }
        }

        /**
         * Build new instance of plugin using name field. Used to reinitialize plugin after
         * loading config. 
         * 
         * @param pf Deliverer of plugins
         * @throws SnakePluginException When:
         * -# Plugin can not be loaded (can not be delivered by PluginFactory)
         * -# Loaded plugin is different version than saved
         * -# Config can not be restored
         */
        public void reinitialize(PluginFactory pf) throws SnakePluginException {
            // Skip on empty slot
            // Empty name is not NONE because on NONE SnakePluginList.setInstance is not called
            // and this slot has its default values initialized in Plugin constructor. On delete
            // plugin from slot (selecting NONE) new default Plugin is created as well
            if (name == "") { // This is default name of empty slot.
                ref = null;
                return;
            }
            ref = pf.getInstance(name);
            if (ref == null)
                throw new SnakePluginException("Plugin initialization failed. Plugin " + name
                        + " can not be loaded or instanced");
            // restore config
            try {
                ref.setPluginConfig(config);
            } catch (QuimpPluginException e) {
                throw new SnakePluginException(e);
            }
            // check version compatibility but do nothing on lack. Just try to load
            if (!ver.equals(ref.getVersion())) {
                throw new SnakePluginException("Loaded plugin is in different version than saved ("
                        + ref.getVersion() + " vs. " + ver + ")");
            }
        }
    }

    /**
     * Holds list of plugins up to max allowed. This list always contains valid \c Plugin objects
     * but they can point to \c null reference (\c Plugin.ref) when there is no plugin on i-th slot  
     */
    private ArrayList<Plugin> sPluginList;

    /**
     * Default constructor
     */
    public SnakePluginList() {
        sPluginList = new ArrayList<Plugin>();
        pluginFactory = null;
    }

    /**
     * Main constructor
     * 
     * @param s Number of supported plugins
     * @param pf Deliverer of plugins
     */
    public SnakePluginList(int s, PluginFactory pf) {
        this();
        for (int i = 0; i < s; i++)
            sPluginList.add(new Plugin());
        pluginFactory = pf;
    }

    /**
     * Returns unmodifiable list of plugins
     * @return unmodifiable list of plugins
     */
    public List<Plugin> getList() {
        return Collections.unmodifiableList(sPluginList);
    }

    /**
     * Return i-th instance of plugin
     * 
     * @param i Number of plugin to return
     */
    public IQuimpPlugin getInstance(int i) {
        return sPluginList.get(i).ref;
    }

    /**
     * Return \c bool if i-th plugin is active or not
     * 
     * @param i Number of plugin to check
     */
    public boolean isActive(int i) {
        return sPluginList.get(i).isActive;
    }

    /**
     * Sets instance of plugin on slot \c i
     * 
     * If there is other plugin there, it replaces instance keeping its selection
     * 
     * @param i Slot to be set
     * @param name Name of plugin - must be registered in PluginFactory or ref will be \c null
     * @param act \c true for active plugin, \c false for inactive
     */
    public void setInstance(int i, String name, boolean act) {
        sPluginList.set(i, new Plugin(name, act, pluginFactory));
    }

    /**
     * Activate or deactivate plugin
     * 
     * @param i Slot to be set
     * @param act \c true for active plugin, \c false for inactive
     */
    public void setActive(int i, boolean act) {
        sPluginList.get(i).isActive = act;
    }

    /**
     * Deletes plugin from memory
     * 
     * @param i Number of slot to delete
     */
    public void deletePlugin(int i) {
        sPluginList.set(i, new Plugin());
    }

    /**
     * Check if list of references contains all null elements.
     * 
     * 
     * @return \c true if list does not contain any valid plugin, \c false otherwise
     */
    public boolean isRefListEmpty() {
        for (Plugin i : sPluginList)
            if (i.ref != null)
                return false;
        return true;
    }

    /**
     * Fills fields in Plugin class related to configuration and version. These fields are 
     * serialized then
     * 
     * @remarks This method should be called directly before saving.  
     */
    public void beforeSerialize() {
        for (Plugin i : sPluginList)
            i.downloadPluginConfig();
    }

    /**
     * Restore plugins instances after deserialziation
     */
    public void afterdeSerialize() {
        for (Plugin i : sPluginList)
            try {
                i.reinitialize(pluginFactory);
            } catch (SnakePluginException e) {
                LOGGER.warn(e);
            }
    }
}

/**
 * Local class derived from Exception for purposes of SnakePluginList
 * 
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 */
class SnakePluginException extends Exception {

    private static final long serialVersionUID = 1L;

    public SnakePluginException() {
        super();
    }

    public SnakePluginException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SnakePluginException(Throwable cause) {
        super(cause);
    }

    public SnakePluginException(String arg0) {
        super(arg0);
    }

    public SnakePluginException(String arg0, Throwable cause) {
        super(arg0, cause);
    }
}
