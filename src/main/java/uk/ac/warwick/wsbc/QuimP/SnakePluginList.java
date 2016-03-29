/**
 * @file SnakePluginList.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.InstanceCreator;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;

/**
 * Ordered list of plugins related to snake processing. 
 * 
 * Related to GUI, first plugin is at index 0, etc. Keeps also UI settings activating or
 * deactivating plugins. Produces plugins from their names using provided 
 * uk.ac.warwick.wsbc.QuimP.PluginFactory
 * The \c sPluginList is serialized (saved as JSON object). Because serialization does not touch
 * plugins (understood as jars) directly, their configuration and state must be copied locally to
 * \c Plugin objects. This is done during preparation to serialization and then after 
 * deserialization.
 * 
 * @remarks This class is serializable and it is part of QuimP config.  
 * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 */
class SnakePluginList {
    private static final Logger LOGGER = LogManager.getLogger(SnakePluginList.class.getName());
    // all other data that are necessary for plugins
    private transient PluginFactory pluginFactory;
    private transient List<Point2d> dataToProcess;
    private transient ViewUpdater viewUpdater;

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
        private transient IQuimpPlugin ref; /*!< Reference to plugin instance */
        private boolean isActive;/*!< Is activate in GUI?*/
        private String name; /*!< Name of plugin delivered from PluginFactory */
        private ParamList config; /*!< Configuration read from plugin on save operation */
        private String ver; /*!< Version read from plugin on save operation */

        /**
         * Initializes empty default plugin
         */
        public Plugin() {
            ref = null;
            isActive = true; /*!< Default value */
            name = "";
            config = null; // no config or not supported by plugin
            ver = ""; // no version or not supported
        }

        /**
         * Main constructor. Creates instance of plugin \c name if \c name is known to provided
         * PluginFactory.
         * 
         * @remarks If \c name is not found in registered names of plugins in provided 
         * PluginFactory \c pf, the reference \c ref will be \c null
         * @param name Name of plugin to be instanced
         * @param isActive 
         * @param pf PluginFactory that provides plugin objects
         * @throws QuimpPluginException 
         */
        public Plugin(final String name, boolean isActive, final PluginFactory pf)
                throws QuimpPluginException {
            this.isActive = isActive;
            ref = pf.getInstance(name); // create instance of plugin
            if (ref == null)
                throw new QuimpPluginException("Plugin initialization failed. Plugin " + name
                        + " can not be loaded or instanced");
            this.name = name;
        }

        public Plugin(final String name, boolean isActive, final PluginFactory pf,
                final ParamList config) throws QuimpPluginException {
            this(name, isActive, pf);
            ref.setPluginConfig(config);
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
         * Copies plugin configuration to local object. Local copy of configuration
         * is necessary for saving/loading. Should be called before saving to make sure that 
         * latest settings are stored.
         */
        public void downloadPluginConfig() {
            if (ref != null) {
                config = ref.getPluginConfig();
                ver = ref.getVersion();
            }
        }

        /**
         * Upload provided configuration to plugin
         * 
         * @param config Configuration to upload
         * @throws QuimpPluginException when \c config can not be uploaded to plugin
         */
        public void uploadPluginConfig(final ParamList config) throws QuimpPluginException {
            if (ref != null)
                ref.setPluginConfig(config);
        }

        /**
         * Return reference to plugin loaded from jar
         * 
         * @return reference to jar
         */
        public IQuimpPlugin getRef() {
            return ref;
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
        dataToProcess = null;
        viewUpdater = null;
    }

    /**
     * Main constructor
     * 
     * @param s Number of supported plugins
     * @param pf Deliverer of plugins
     * @param dataToProcess data to be connected to plugin (not obligatory)
     * @param vu ViewUpdater to be connected to plugin
     */
    public SnakePluginList(int s, final PluginFactory pf, final List<Point2d> dataToProcess,
            final ViewUpdater vu) {
        this();
        for (int i = 0; i < s; i++)
            sPluginList.add(new Plugin());
        this.pluginFactory = pf;
        this.dataToProcess = dataToProcess;
        this.viewUpdater = vu;
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
     * @return Instance of plugin
     */
    public IQuimpPlugin getInstance(int i) {
        return sPluginList.get(i).ref;
    }

    /**
     * Return i-th plugin name
     * 
     * @param i Number of plugin to return
     * @return Name of plugin
     */
    public String getName(int i) {
        return sPluginList.get(i).name;
    }

    /**
     * Return i-th plugin version
     * 
     * @param i Number of plugin to return
     * @return Version of plugin
     */
    public String getVer(int i) {
        return sPluginList.get(i).ver;
    }

    /**
     * Return i-th plugin configuration
     * 
     * @param i Number of plugin to return
     * @return \b Copy of configuration of plugin
     */
    public ParamList getConfig(int i) {
        if (sPluginList.get(i).config != null)
            return new ParamList(sPluginList.get(i).config); // makes copy of plugin configuration
        else
            return null;
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
     * If there is other plugin there, it replaces instance keeping its selection state. 
     * Connects also ViewUpdater and data to plugin if necessary.
     * 
     * @param i Slot to be set
     * @param name Name of plugin - must be registered in PluginFactory or ref will be \c null
     * @param act \c true for active plugin, \c false for inactive
     * @throws QuimpPluginException 
     */
    public void setInstance(int i, final String name, boolean act) throws QuimpPluginException {

        if (name.isEmpty()) {
            sPluginList.set(i, new Plugin()); // just create new empty plugin with no instance
            return;
        }
        sPluginList.set(i, new Plugin(name, act, pluginFactory)); // create new Plugin using
                                                                  // name and PluginFactory

        IQuimpPlugin ref = getInstance(i);
        // connects all goods to created plugin
        if (ref != null) {
            if (ref instanceof IQuimpPluginSynchro) // if it support backward synchronization
                ((IQuimpPluginSynchro) ref).attachContext(viewUpdater); // attach BOA context
            if (ref instanceof IQuimpPoint2dFilter)
                ((IQuimpPoint2dFilter) ref).attachData(dataToProcess);
        }
    }

    /**
     * Sets instance of plugin on slot \c i
     * 
     * If there is other plugin there, it replaces instance keeping its selection. 
     * Connects also ViewUpdater and data to plugin if necessary
     * 
     * @param i Slot to be set
     * @param name Name of plugin - must be registered in PluginFactory or ref will be \c null
     * @param act \c true for active plugin, \c false for inactive
     * @param config Configuration to connect to plugin
     * @throws QuimpPluginException When \c config is not compatible
     */
    private void setInstance(int i, final String name, boolean act, final ParamList config)
            throws QuimpPluginException {
        setInstance(i, name, act);
        sPluginList.get(i).uploadPluginConfig(config);

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
     * @remarks This method should be called directly before saving to have most recent options.  
     */
    public void beforeSerialize() {
        for (Plugin i : sPluginList)
            i.downloadPluginConfig();
    }

    /**
     * Restore plugins instances after deserialization
     * 
     * On load all fields of Plugin object are restored from JSON file except plugin instance. In
     * this step this instance is created using those fields loaded from disk.
     * 
     * @throws QuimpPluginException 
     */
    public void afterdeSerialize() throws QuimpPluginException {
        // go through list and create new Plugin using old values that were restored after loading
        for (int i = 0; i < sPluginList.size(); i++) {
            String ver = sPluginList.get(i).ver;
            // sets new instance of plugin using old configuration loaded
            setInstance(i, getName(i), isActive(i), sPluginList.get(i).config);
            if (getInstance(i) != null) {
                if (!ver.equals(sPluginList.get(i).ref.getVersion()))
                    LOGGER.warn("Loaded plugin (" + sPluginList.get(i).name
                            + ") is in different version than saved ("
                            + sPluginList.get(i).ref.getVersion() + " vs. " + ver + ")");
            }
        }
    }

    /**
     * Close all opened plugins windows
     */
    public void closeAllWindows() {
        for (int i = 0; i < sPluginList.size(); i++)
            if (getInstance(i) != null)
                getInstance(i).showUI(false);
    }
}

/**
 * Object builder for GSon and SnakePluginList class
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 *
 */
class SnakePluginListInstanceCreator implements InstanceCreator<SnakePluginList> {

    private int size;
    private PluginFactory pf;
    private List<Point2d> dt;
    private ViewUpdater vu;

    public SnakePluginListInstanceCreator(int size, final PluginFactory pf,
            final List<Point2d> dataToProcess, final ViewUpdater vu) {
        this.size = size;
        this.pf = pf;
        this.dt = dataToProcess;
        this.vu = vu;
    }

    @Override
    public SnakePluginList createInstance(Type arg0) {
        return new SnakePluginList(size, pf, dt, vu);
    }

}
