package uk.ac.warwick.wsbc.quimp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpPluginSynchro;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;

/*
 * //!>
 * @startuml doc-files/SnakePluginList_1_UML.png
 * left to right direction
 * :User: as user
 * user--(//Initialize//)
 * user--(setInstance)
 * user--(Serialize)
 * user--(delete plugin)
 * user--(set active)
 * (Serialize)<|--(Handle\nconfiguration):<<extend>>
 * @enduml
 * 
 * @startuml doc-files/SnakePluginList_2_UML.png
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin : external instance\nof plugin
 * User->slist : <<create>>\nPluginFactory\nData\nViewUpdater
 * activate slist
 * loop all slots
 * slist->plugin : <<create>>
 * activate plugin
 * note left
 * Create empty instances of plugins
 * end note
 * end
 * @enduml
 * 
 * @startuml doc-files/SnakePluginList_3_UML.png
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
 * @startuml doc-files/SnakePluginList_4_UML.png
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
 * == before Serialization ==
 * User --/ slist : save loop all slots 
 * slist->plugin : ""downloadPluginConfig()""
 * plugin -> iPlugin : ""getPluginConfig()""
 * iPlugin --> plugin: config
 * plugin -> iPlugin : ""getVersion()""
 * iPlugin --> plugin : version
 * == after serialization ==
 * User --/ slist : load
 * note left
 * On load fields of SnakePluginList
 * and Plugin are restored except plugin
 * instances
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
 * note left: See setInstance UseCase ...
 * slist->plugin : ""uploadPluginConfig(config)"" 
 * plugin -> iPlugin : ""setPluginConfig(config)""
 * end
 * @enduml
 * 
 * @startuml doc-files/SnakePluginList_5_UML.png
 * actor User
 * participant SnakePluginList as slist
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin: Internal representation\nof plugin instance
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
 * @startuml doc-files/SnakePluginList_6_UML.png
 * actor User
 * participant SnakePluginList as slist 
 * participant Plugin as plugin
 * participant PluginFactory as pfact
 * participant IQuimpPlugin as iPlugin
 * note over plugin : Internal representation\nof plugin instance
 * note over iPlugin :external instance\nof plugin
 * activate slist
 * activate plugin
 * activate pfact
 * activate iPlugin
 * User --/ slist : setActive(i,Activity)
 * slist->plugin : set isActive
 * @enduml
 * 
 * 
 * //!<
 */
/**
 * Ordered list of plugins related to snake processing.
 * 
 * <p>Related to GUI, first plugin is at index 0, etc. Keeps also UI settings activating or
 * deactivating plugins. Produces plugins from their names using provided
 * {@link uk.ac.warwick.wsbc.quimp.PluginFactory} The sPluginList is serialized (saved as JSON
 * object). Because serialization does not touch plugins (understood as jars) directly, their
 * configuration and state must be copied locally to Plugin objects. This is done during preparation
 * to serialization and then after deserialization.
 * 
 * <p>This class is serializable and it is part of QuimP config.
 * 
 * <p>The most important use cases are:<br>
 * <img src="doc-files/SnakePluginList_1_UML.png"/><br>
 * 
 * <p>During initialization basic structures are created. Note that plugins are kept in intermediate
 * class Plugin that holds current state of plugin:
 * <ol>
 * <li>reference to jar (obtained from PluginFactory)
 * <li>name of plugin (name uniquely defines the plugin)
 * <li>version of plugin (read from jar)
 * <li>status of plugin (active or inactive, related to QuimP UI)
 * <li>configuration (for saving on disk)
 * </ol>
 * <br>
 * <img src="doc-files/SnakePluginList_2_UML.png"/><br>
 * 
 * <p>During setInstance the instance of plugin is created and assign to Plugin object <br>
 * <img src="doc-files/SnakePluginList_3_UML.png"/><br>
 * 
 * <p>During Serialize plugins are prepared for serialization what means saving current state of
 * plugins like:
 * <ol>
 * <li>Loaded plugins (those kept in SnakePluginList only, selected by user in UI)
 * <li>Their configuration
 * </ol>
 * <br>
 * <img src="doc-files/SnakePluginList_4_UML.png"/><br>
 * 
 * <p>During Deletion of plugin the new empty plugin is created in place of old one <br>
 * <img src="doc-files/SnakePluginList_5_UML.png"/><br>
 * 
 * <p>During Set Active state, the internal state of plugin is set to active. This is important for
 * method {@link uk.ac.warwick.wsbc.quimp.SnakePluginList.Plugin#isExecutable()}. <br>
 * <img src="doc-files/SnakePluginList_6_UML.png"/><br>
 * 
 * @see uk.ac.warwick.wsbc.quimp.BOA_#run(String)
 * @author p.baniukiewicz
 */
public class SnakePluginList implements IQuimpSerialize {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SnakePluginList.class.getName());
  // all other data that are necessary for plugins
  private transient PluginFactory pluginFactory;
  private transient ViewUpdater viewUpdater;

  /**
   * Keeps all Plugin related information and produces plugin instance using PluginFactory.
   * 
   * <p>Fields like config and ver are used during saving so they are initialized in this time only
   * and they are not valid during object lifetime.
   * 
   * @author p.baniukiewicz
   *
   */
  class Plugin {
    /**
     * Reference to plugin instance.
     */
    private transient IQuimpCorePlugin ref;
    /**
     * Is activate in GUI?.
     */
    private boolean isActive;
    /**
     * Name of plugin delivered from PluginFactory.
     */
    private String name;
    /**
     * Configuration read from plugin on save operation.
     */
    private ParamList config;
    /**
     * Version read from plugin on save operation.
     */
    private String ver;

    /**
     * Initializes empty default plugin.
     */
    public Plugin() {
      ref = null;
      isActive = true; // Default value
      name = "";
      config = null; // no config or not supported by plugin
      ver = ""; // no version or not supported
    }

    /**
     * Main constructor. Creates instance of plugin name if name is known to provided
     * PluginFactory.
     * 
     * <p>If name is not found in registered names of plugins in provided PluginFactory pf, the
     * reference ref will be null
     * 
     * @param name Name of plugin to be instanced
     * @param isActive is plugin active?
     * @param pf PluginFactory that provides plugin objects
     * @throws QuimpPluginException if plugin can not be instanced
     */
    public Plugin(final String name, boolean isActive, final PluginFactory pf)
            throws QuimpPluginException {
      this.isActive = isActive;
      ref = pf.getInstance(name); // create instance of plugin
      if (ref == null) {
        throw new QuimpPluginException(
                "Plugin initialization failed. Plugin " + name + " can not be loaded or instanced");
      }
      this.name = name;
    }

    /**
     * Instantiates a new plugin.
     *
     * @param name the name
     * @param isActive the is active
     * @param pf the pf
     * @param config the config
     * @throws QuimpPluginException the quimp plugin exception
     */
    @Deprecated
    public Plugin(final String name, boolean isActive, final PluginFactory pf,
            final ParamList config) throws QuimpPluginException {
      this(name, isActive, pf);
      ref.setPluginConfig(config);
    }

    /**
     * Copy method.
     * 
     * <p>Returns copy of current object with some limitations. It does not copy loaded plugin
     * (ref). Should be called after {@link #downloadPluginConfig()} to make sure that config,
     * ver are filled correctly
     * 
     * @return Copy of current object
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
     * Copy method.
     * 
     * <p>Returns copy of current object. It does copy loaded plugin (ref). Should be called after
     * {@link #downloadPluginConfig()} to make sure that config, ver are filled correctly
     * 
     * @return Copy of current object
     */
    private Plugin getDeepCopy() {
      Plugin ret = getShallowCopy();
      ret.ref = this.ref;
      return ret;
    }

    /**
     * Check if all execution conditions are met.
     * 
     * <p>These conditions are: 1) Plugin exist 2) Plugin is activated in UI
     * 
     * @return true if plugin can be executed
     */
    public boolean isExecutable() {
      if (ref == null) {
        return false;
      } else {
        return isActive;
      }
    }

    /**
     * Copies plugin configuration to local object.
     * 
     * <p>Local copy of configuration is necessary for saving/loading. Should be called before
     * saving to make sure that latest settings are stored.
     */
    public void downloadPluginConfig() {
      if (ref != null) {
        config = ref.getPluginConfig();
        ver = ref.getVersion();
      }
    }

    /**
     * Upload provided configuration to plugin.
     * 
     * @param config Configuration to upload
     * @throws QuimpPluginException when config can not be uploaded to plugin
     */
    public void uploadPluginConfig(final ParamList config) throws QuimpPluginException {
      if (ref != null) {
        ref.setPluginConfig(config);
      }
    }

    /**
     * Return reference to plugin loaded from jar.
     * 
     * @return reference to jar
     */
    public IQuimpCorePlugin getRef() {
      return ref;
    }
  }

  /**
   * Holds list of plugins up to max allowed.
   * 
   * <p>This list always contains valid \c Plugin objects but they can point to null reference (
   * Plugin.ref) when there is no plugin on i-th slot
   */
  private ArrayList<Plugin> sPluginList;

  /**
   * Default constructor.
   * 
   * <p>Create empty Plugin object that refers to nothing
   */
  public SnakePluginList() {
    sPluginList = new ArrayList<Plugin>();
    updateRefs(null, null);
  }

  /**
   * Main constructor. Collect all external data necessary to use plugins.
   * 
   * @param s Number of supported plugins
   * @param pf Deliverer of plugins
   * @param vu ViewUpdater to be connected to plugin
   */
  public SnakePluginList(int s, final PluginFactory pf, final ViewUpdater vu) {
    this(); // initialize structures
    for (int i = 0; i < s; i++) {
      sPluginList.add(new Plugin()); // fill list with empty Plugins
    }
    // store plugin deliverer and external data that may be important for plugins
    updateRefs(pf, vu);
  }

  /**
   * Copy method.
   * 
   * <p>Returns copy of current object with some limitations. It does copy loaded plugin (ref).
   * Should be called after {@link SnakePluginList.Plugin#downloadPluginConfig()} to make sure
   * that config, ver are filled correctly
   * 
   * @return Copy of current object
   */
  public SnakePluginList getShallowCopy() {
    beforeSerialize(); // get plugin config from Plugins (jars->Plugin) to fill Plugin subclass
    SnakePluginList ret = new SnakePluginList();
    ret.updateRefs(pluginFactory, viewUpdater); // assign current external data
    // make deep copy of the list
    for (Plugin p : this.sPluginList) {
      ret.sPluginList.add(p.getShallowCopy());
    }
    return ret;
  }

  /**
   * Copy method.
   * 
   * <p>Returns copy of current object.It does copy loaded plugin (ref). Should be called after
   * {@link SnakePluginList.Plugin#downloadPluginConfig()} to make sure that config, ver are
   * filled correctly
   * 
   * @return Copy of current object
   */
  public SnakePluginList getDeepCopy() {
    beforeSerialize(); // get plugin config from Plugins (jars->Plugin) to fill Plugin subclass
    SnakePluginList ret = new SnakePluginList();
    ret.updateRefs(pluginFactory, viewUpdater); // assign current external data
    // make deep copy of the list
    for (Plugin p : this.sPluginList) {
      ret.sPluginList.add(p.getDeepCopy());
    }
    return ret;
  }

  /**
   * Updates references of external object connected in constructor.
   * 
   * <p>External references are not copied by {@link #getShallowCopy()} thus they should be
   * reinitialized after that operation
   * 
   * @param pf new PluginFactory
   * @param vu new ViewUpdater
   */
  public void updateRefs(PluginFactory pf, ViewUpdater vu) {
    this.pluginFactory = pf;
    this.viewUpdater = vu;
  }

  /**
   * Returns unmodifiable list of plugins.
   * 
   * <p>Particular fields in Plugin may not be valid unless beforeSerialize() is called.
   * 
   * @return unmodifiable list of plugins
   */
  public List<Plugin> getList() {
    return Collections.unmodifiableList(sPluginList);
  }

  /**
   * Return i-th instance of plugin.
   * 
   * @param i Number of plugin to return
   * @return Instance of plugin
   */
  public IQuimpCorePlugin getInstance(int i) {
    return sPluginList.get(i).ref;
  }

  /**
   * Return i-th plugin name.
   * 
   * @param i Number of plugin to return
   * @return Name of plugin
   */
  public String getName(int i) {
    return sPluginList.get(i).name;
  }

  /**
   * Return i-th plugin version.
   * 
   * @param i Number of plugin to return
   * @return Version of plugin
   */
  public String getVer(int i) {
    return sPluginList.get(i).ver;
  }

  /**
   * Return i-th plugin configuration.
   * 
   * @param i Number of plugin to return
   * @return Copy of configuration of plugin
   */
  public ParamList getConfig(int i) {
    if (sPluginList.get(i).config != null) {
      return new ParamList(sPluginList.get(i).config); // makes copy of plugin configuration
    } else {
      return null;
    }
  }

  /**
   * Check if plugin is active.
   * 
   * @param i Number of plugin to check
   * @return bool if i-th plugin is active or not
   */
  public boolean isActive(int i) {
    return sPluginList.get(i).isActive;
  }

  /**
   * Sets instance of plugin on slot i.
   * 
   * <p>If there is other plugin there, it replaces instance keeping its selection state. Connects
   * also ViewUpdater and data to plugin if necessary.
   * 
   * @param i Slot to be set
   * @param name Name of plugin - must be registered in PluginFactory or ref will be null
   * @param act true for active plugin, false for inactive
   * @throws QuimpPluginException When instance can not be created
   */
  public void setInstance(int i, final String name, boolean act) throws QuimpPluginException {

    if (name.isEmpty()) {
      sPluginList.set(i, new Plugin()); // just create new empty plugin with no instance
      return;
    }
    sPluginList.set(i, new Plugin(name, act, pluginFactory)); // create new Plugin

    IQuimpCorePlugin ref = getInstance(i);
    // connects all goods to created plugin
    if (ref != null) {
      if (ref instanceof IQuimpPluginSynchro) { // if it supports backward synchronization
        ((IQuimpPluginSynchro) ref).attachContext(viewUpdater); // attach BOA context
      }
    }
  }

  /**
   * Sets instance of plugin on slot i.
   * 
   * <p>If there is other plugin there, it replaces instance keeping its selection. Connects also
   * ViewUpdater and data to plugin if necessary.
   * 
   * @param i Slot to be set
   * @param name Name of plugin - must be registered in PluginFactory or ref will be null
   * @param act true for active plugin, false for inactive
   * @param config Configuration to connect to plugin
   * @throws QuimpPluginException When config is not compatible or instance can not be created
   */
  private void setInstance(int i, final String name, boolean act, final ParamList config)
          throws QuimpPluginException {
    setInstance(i, name, act);
    sPluginList.get(i).uploadPluginConfig(config);

  }

  /**
   * Activate or deactivate plugin.
   * 
   * @param i Slot to be set
   * @param act true for active plugin, false for inactive
   */
  public void setActive(int i, boolean act) {
    sPluginList.get(i).isActive = act;
  }

  /**
   * Deletes plugin from memory.
   * 
   * @param i Number of slot to delete
   */
  public void deletePlugin(int i) {
    sPluginList.set(i, new Plugin());
  }

  /**
   * Deletes all plugins from list and closes theirs windows.
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
   * @return true if list does not contain any valid plugin, false otherwise
   */
  public boolean isRefListEmpty() {
    for (Plugin i : sPluginList) {
      if (i.ref != null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Fills fields in Plugin class related to configuration and version. These fields are
   * serialized then.
   * 
   * <p>This method should be called directly before saving to have most recent options.
   */
  @Override
  public void beforeSerialize() {
    for (Plugin i : sPluginList) {
      i.downloadPluginConfig();
    }
  }

  /**
   * Restore plugins instances after deserialization.
   * 
   * <p>On load all fields of Plugin object are restored from JSON file except plugin instance. In
   * this step this instance is created using those fields loaded from disk.
   * 
   * <p>This method masks all QuimpPluginException exceptions that allows to skim defective plugin
   * and load all next plugins.
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
        if (!ver.equals(sPluginList.get(i).ref.getVersion())) {
          LOGGER.warn("Loaded plugin (" + sPluginList.get(i).name
                  + ") is in different version than saved (" + sPluginList.get(i).ref.getVersion()
                  + " vs. " + ver + ")");
        }
      }
    }
  }

  /**
   * Close all opened plugins windows.
   */
  public void closeAllWindows() {
    for (int i = 0; i < sPluginList.size(); i++) {
      if (getInstance(i) != null) {
        getInstance(i).showUi(false);
      }
    }
  }

  /**
   * Return names of plugins of given type registered in PluginFactory associated with this
   * object.
   * 
   * @param type requested plugin type {@link PluginFactory}
   * @return list of plugin names
   * 
   * @see uk.ac.warwick.wsbc.quimp.PluginFactory#getPluginNames(int)
   */
  public ArrayList<String> getPluginNames(int type) {
    return pluginFactory.getPluginNames(type);
  }
}

/**
 * Object builder for GSon and SnakePluginList class.
 * 
 * <p>This class is used on load JSon representation of SnakePluginList class.
 * 
 * @author p.baniukiewicz
 * @see Gson documentation
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
