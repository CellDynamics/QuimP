/**
 * @file SnakePluginList.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.InstanceCreator;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

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
 * The most important use cases are:
 * 
 * @startuml
 * left to right direction
 * 
 * :User: as user
 * user--(//Initialize//)
 * user--(setInstance)
 * user--(Serialize)
 * user--(delete plugin)
 * user--(set active)
 * (Serialize)<|--(Handle\nconfiguration):<<extend>>
 * @enduml
 * 
 * During \a initialization basic structures are created. Note that plugins are kept in intermediate 
 * class Plugin that holds current state of plugin :
 * -# reference to jar (obtained from PluginFactory)
 * -# name of plugin (\a name uniquely defines the plugin)  
 * -# version of plugin (read from jar)
 * -# status of plugin (active or inactive, related to QuimP UI)
 * -# configuration (for saving on disk)
 * 
 * @startuml
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin

 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * 
 * User->slist : <<create>>\nPluginFactory\nData\nViewUpdater
 * activate slist
 * loop all slots
 *  slist->plugin : <<create>>
 *  activate plugin
 *  note left
 *  Create empty instances
 *  of plugins
 *  end note
 * end
 * @enduml
 * 
 * During \a setInstance the instance of plugin is created and assign to Plugin object
 * @startuml
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * activate slist
 * activate plugin
 * activate pfact
 * User->slist : ""setInstance""\ni\nName\nActivity
 * slist-->plugin : <<destroy>>
 * destroy plugin
 * slist -> plugin : <<create>>\nName\nActivity\nPluginFactory
 * activate plugin
 * plugin -> pfact : ""getInstance(Name)""
 * pfact -> iPlugin : <<create>>
 * activate iPlugin
 * pfact --> plugin : instance
 * note left: instance is stored in\nPlugin
 * plugin --> slist
 * slist->plugin : ""getInstance(i)""
 * plugin-->slist : instance
 * slist -> iPlugin : ""attachContext(ViewUpdater)""
 * @enduml
 * 
 * During \a Serialize plugins are prepared for serialization what means saving current state of 
 * plugins like:
 * -# Loaded plugins (those kept in SnakePluginList only, selected by user in UI)
 * -# Their configuration
 * 
 * @startuml
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * activate slist
 * activate plugin
 * activate pfact
 * activate iPlugin
 * 
 * == before Serialization ==
 * User --/ slist : save
 * loop all slots
 *  slist->plugin : ""downloadPluginConfig()""
 *  plugin -> iPlugin : ""getPluginConfig()""
 *  iPlugin --> plugin : config
 *  plugin -> iPlugin : ""getVersion()""
 *  iPlugin --> plugin : version
 * end
 * 
 * == after serialization ==
 * User --/ slist : load
 * note left
 * On load fields of SnakePluginList
 * and Plugin are restored except 
 * plugin instances
 * end note
 * loop all slots
 * slist->plugin : get version
 * plugin --> slist: version
 * slist->plugin : get Config
 * plugin --> slist: config
 * slist->plugin : get Name
 * plugin --> slist: name
 * note left: Those restored from load
 * slist->slist : ""setInstance(i,Name,Activity,Config)""
 * activate slist
 * destroy plugin
 * destroy iPlugin
 * slist->slist : ""setInstance(i,Name,Activity)""
 * activate slist
 * activate plugin
 * activate iPlugin
 * activate slist
 * ... See setInstance Use Case ...
 * slist->plugin : ""uploadPluginConfig(config)""
 * plugin -> iPlugin : ""setPluginConfig(config)""
 * end
 * @enduml
 * 
 * During \a Deletion of plugin the new empty plugin is created in place of old one
 * 
 * @startuml
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * activate slist
 * activate plugin
 * activate pfact
 * activate iPlugin
 * User --/ slist : Delete plugin
 * destroy plugin
 * destroy iPlugin
 * slist->plugin : <<create>>
 * activate plugin
 * @enduml
 * 
 * During \a Set \a Active state, the internal state of plugin is set to active. This is important
 * for method uk.ac.warwick.wsbc.QuimP.SnakePluginList.Plugin.isExecutable().
 * 
 * @startuml
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * 
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * activate slist
 * activate plugin
 * activate pfact
 * activate iPlugin
 * User --/ slist : setActive(i,Activity)
 * slist->plugin : set isActive
 * @enduml
 *  
 * @remarks This class is serializable and it is part of QuimP config.  
 * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 */
class SnakePluginList implements IQuimpSerialize {
    private static final Logger LOGGER = LogManager.getLogger(SnakePluginList.class.getName());
    // all other data that are necessary for plugins
    private transient PluginFactory pluginFactory;
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
        private transient IQuimpPlugin ref; //!< Reference to plugin instance 
        private boolean isActive;//!< Is activate in GUI?
        private String name; //!< Name of plugin delivered from PluginFactory 
        private ParamList config; //!< Configuration read from plugin on save operation 
        private String ver; //!< Version read from plugin on save operation 

        /**
         * Initializes empty default plugin
         */
        public Plugin() {
            ref = null;
            isActive = true; //!< Default value
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

        @Deprecated
        public Plugin(final String name, boolean isActive, final PluginFactory pf,
                final ParamList config) throws QuimpPluginException {
            this(name, isActive, pf);
            ref.setPluginConfig(config);
        }

        /**
         * Copy method
         * 
         * Returns copy of current object with some limitations
         * 
         * @return Copy of current object
         * @warning It does not copy loaded plugin (ref)
         * @remarks Should be called after SnakePluginList.Plugin.downloadPluginConfig() to make
         * sure that \c config, \c ver are filled correctly
         */
        private Plugin getShallowCopy() {
            Plugin ret = new Plugin();
            ret.isActive = this.isActive;
            ret.name = this.name;
            ret.config = new ParamList(this.config); // copy config
            ret.ver = this.ver;
            return ret;
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
     * 
     * Create empty Plugin object that refers to nothing
     */
    public SnakePluginList() {
        sPluginList = new ArrayList<Plugin>();
        pluginFactory = null;
        viewUpdater = null;
    }

    /**
     * Main constructor. Collect all external data necessary to use plugins
     * 
     * @param s Number of supported plugins
     * @param pf Deliverer of plugins
     * @param vu ViewUpdater to be connected to plugin
     */
    public SnakePluginList(int s, final PluginFactory pf, final ViewUpdater vu) {
        this(); // initialize structures
        for (int i = 0; i < s; i++)
            sPluginList.add(new Plugin()); // fill list with empty Plugins
        this.pluginFactory = pf; // store plugin deliverer
        // store external data that may be important for plugins
        this.viewUpdater = vu;
    }

    /**
     * Copy method
     * 
     * Returns copy of current object with some limitations
     * 
     * @return Copy of current object
     * @warning It does not copy loaded plugin (ref)
     * @remarks Should be called after SnakePluginList.Plugin.downloadPluginConfig() to make
     * sure that \c config, \c ver are filled correctly
     */
    public SnakePluginList getShallowCopy() {
        beforeSerialize(); // get plugin config from Plugins (jars->Plugin) to fill Plugin subclass
        SnakePluginList ret = new SnakePluginList();
        // make deep copy of the list
        for (Plugin p : this.sPluginList)
            ret.sPluginList.add(p.getShallowCopy());
        return ret;
    }

    /**
     * Returns unmodifiable list of plugins
     * @return unmodifiable list of plugins
     * @warning Particular fields in Plugin may not be valid unless beforeSerialize() is called.
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
     * @throws QuimpPluginException When instance can not be created
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
     * @throws QuimpPluginException When \c config is not compatible or instance can not be created
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
     * Deletes all plugins from list and closes theirs windows
     */
    public void clear() {
        closeAllWindows();
        for (int i = 0; i < sPluginList.size(); i++) {
            sPluginList.set(i, new Plugin());
        }
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
    @Override
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
     * This method masks all QuimpPluginException exceptions that allows to skim defective plugin
     * and load all next plugins
     */
    @Override
    public void afterSerialize() {
        // go through list and create new Plugin using old values that were restored after loading
        for (int i = 0; i < sPluginList.size(); i++) {
            String ver = sPluginList.get(i).ver;
            String name = getName(i); // only for exception handling to know name before setInstance
            // sets new instance of plugin using old configuration loaded
            // skip plugin that cannot be loaded or with wrong configuration
            try {
                setInstance(i, getName(i), isActive(i), sPluginList.get(i).config);
            } catch (QuimpPluginException e) {
                deletePlugin(i); // delete plugin on any error
                LOGGER.warn("Plugin name: " + name + " " + e.getMessage());
            }
            // check version compatibility - only inform user
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

    /**
     * Return names of plugins of given type registered in PluginFactory associated with this object
     * 
     * @see uk.ac.warwick.wsbc.QuimP.PluginFactory.getPluginNames(int)
     */
    public ArrayList<String> getPluginNames(int type) {
        return pluginFactory.getPluginNames(type);
    }
}

/**
 * Object builder for GSon and SnakePluginList class
 * 
 * This class is used on load JSon representation of SnakePluginList class
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 * @see GSon documentation
 */
class SnakePluginListInstanceCreator implements InstanceCreator<SnakePluginList> {

    private int size;
    private PluginFactory pf;
    private ViewUpdater vu;

    public SnakePluginListInstanceCreator(int size, final PluginFactory pf, final ViewUpdater vu) {
        this.size = size;
        this.pf = pf;
        this.vu = vu;
    }

    @Override
    public SnakePluginList createInstance(Type arg0) {
        return new SnakePluginList(size, pf, vu);
    }

}
