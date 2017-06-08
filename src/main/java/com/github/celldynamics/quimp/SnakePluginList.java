package com.github.celldynamics.quimp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.github.celldynamics.quimp.plugin.IQuimpCorePlugin;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.engine.PluginFactory;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

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
 * Ordered list of plugins related to snake processing for one frame.
 * 
 * <p>This class keeps configuration for all plugin slots but for one frame. If you look for global
 * Configuration check {@link BOAState#snakePluginListSnapshots}. Note
 * also that each plugin is instanced only once by {@link PluginFactory} so their configuration must
 * be updated for each frame separately.
 * 
 * <p>Related to GUI, first plugin is at index 0, etc. Keeps also UI settings activating or
 * deactivating plugins. Produces plugins from their names using provided
 * {@link com.github.celldynamics.quimp.plugin.engine.PluginFactory} The <tt>sPluginList</tt> is
 * serialized
 * (saved as JSON object). Because serialization does not touch plugins (understood as jars)
 * directly and moreover they reference the same object among frames, their
 * configuration and state must be stored locally in Plugin object. This task is
 * must be
 * accomplished on any change in plugin configuration, serialisation and deserialisation. Check
 * {@link #afterSerialize()}, {@link #beforeSerialize()}
 * {@link BOAState#restore(int)} and {@link BOAState#store(int)}. From plugin side it is enough to
 * use {@link com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro} interface to store its
 * current configuration locally.
 * For the whole stack of images, plugin stack is kept for each frame
 * ({@link BOAState#snakePluginListSnapshots}) together with updated configuration inside
 * {@link Plugin}. Browsing through frames upload configuration to plugin for each frame.
 * 
 * <p>This class is serializable and it is part of QuimP config.
 * 
 * <p>The most important use cases are:<br>
 * <img src="doc-files/SnakePluginList_1_UML.png"/><br>
 * 
 * <p>During initialization basic structures are created. Note that plugins are stored in
 * intermediate
 * class {@link Plugin} that holds current state of plugin:
 * <ol>
 * <li>reference to jar (obtained from PluginFactory)
 * <li>name of plugin (name uniquely defines the plugin)
 * <li>version of plugin (read from jar)
 * <li>status of plugin (active or inactive, related to QuimP UI)
 * <li>configuration - updated for each frame or on each action in plugin, always actual
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
 * method {@link com.github.celldynamics.quimp.SnakePluginList.Plugin#isExecutable()}. <br>
 * <img src="doc-files/SnakePluginList_6_UML.png"/><br>
 * 
 * @see com.github.celldynamics.quimp.BOA_#run(String)
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
     * This is local snapshot of current plugin configuration.
     * 
     * <p>Updated on each call
     * {@link com.github.celldynamics.quimp.ViewUpdater#updateView()} made by plugin (indirectly
     * {@link BOA_#recalculatePlugins()} and {@link BOAState#store(int)}. Configuration is pushed to
     * plugin on each change of displayed frame {@link BOAState#restore(int)} because all plugins
     * with the same name are references of one instance. This field should not be null.
     * 
     * @see #uploadPluginConfig(ParamList)
     * @see #uploadPluginConfig()
     * @see #downloadPluginConfig()
     * @see SnakePluginList#downloadPluginsConfig()
     * @see SnakePluginList#uploadPluginsConfig()
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
      config = new ParamList(); // no config or not supported by plugin
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
      ver = ref.getVersion();
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
        ParamList tmpconfig = ref.getPluginConfig();
        // if plugin returns null, create empty container
        config = tmpconfig != null ? tmpconfig : new ParamList();
        String tmpVer = ref.getVersion();
        ver = tmpVer != null ? tmpVer : "";
      }
    }

    /**
     * Upload provided configuration to plugin and set it as local.
     * 
     * @param config Configuration to upload
     * @throws QuimpPluginException when config can not be uploaded to plugin
     */
    public void uploadPluginConfig(final ParamList config) throws QuimpPluginException {
      if (ref != null) {
        ref.setPluginConfig(config);
      }
      this.config = config;
    }

    /**
     * Upload local configuration to plugin.
     * 
     * @throws QuimpPluginException when config can not be uploaded to plugin
     */
    public void uploadPluginConfig() throws QuimpPluginException {
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
   * Holds list of plugins up to max slots allowed.
   * 
   * <p>This list always contains valid Plugin objects but they can point to null reference (
   * Plugin.ref) when there is no plugin on i-th slot. Keep also plugin configuration for
   * serialisation purposes. Configuration can be exchanged with jar plugin.
   * 
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
   * <p>Returns copy of current object with some limitations. It does not copy loaded plugin (ref).
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
   * <p>All copied plugin will refer to the same jar thus they will share e.g. configuration
   * parameters. To have independent copies {@link #afterSerialize()} should be called on the list.
   * Refer to this code making full independent copies of plugins from current frame over all
   * frames:
   * 
   * <pre>
   * <code>
   * SnakePluginList tmp = qState.snakePluginList.getDeepCopy();
   *  for (int i = 0; i < qState.snakePluginListSnapshots.size(); i++) {
   *    // make a deep copy
   *    qState.snakePluginListSnapshots.set(i, tmp.getDeepCopy());
   *    // instance separate copy of jar for this plugin
   *    qState.snakePluginListSnapshots.get(i).afterSerialize();
   *  }
   *  int cf = qState.boap.frame;
   *  for (boap.frame = 1; boap.frame <= boap.getFrames(); qState.boap.frame++) {
   *    imageGroup.updateToFrame(boap.frame);
   *    recalculatePlugins();
   *  }
   *  qState.boap.frame = cf;
   *  imageGroup.updateToFrame(qState.boap.frame);
   * </code>
   * </pre>
   * 
   * @return Copy of current object
   */
  public SnakePluginList getDeepCopy() {
    downloadPluginsConfig(); // get plugin config from Plugins (jars->Plugin) to fill Plugin
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
      return new ParamList(sPluginList.get(i).config); // TODO makes copy? of plugin configuration
    } else {
      return new ParamList(); // return empty list
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
    try {
      sPluginList.get(i).uploadPluginConfig(config); // and set given config as local as well
    } catch (QuimpPluginException e) { // catch here if e.g. lack of backward comp.
      LOGGER.warn("Plugin " + sPluginList.get(i).name + " refused provided configuration");
    }

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
   * Download configuration from all opened plugins into internal structure. Performs transfer
   * jar->local object.
   * 
   * <p>Configuration is kept locally for serialisation purposes.
   * 
   * @see #beforeSerialize()
   */
  public void downloadPluginsConfig() {
    for (Plugin i : sPluginList) {
      i.downloadPluginConfig();
    }
  }

  /**
   * Transfer locally stored config to plugins instance for current list of plugin.
   * 
   * @see #downloadPluginsConfig()
   */
  public void uploadPluginsConfig() {
    for (Plugin i : sPluginList) {
      try {
        i.uploadPluginConfig();
      } catch (QuimpPluginException e) {
        LOGGER.warn("Plugin " + i.name
                + " refused provided configuration. Default values will be used on next run");
      }
    }
  }

  /**
   * Fills fields in Plugin class related to configuration and version. These fields are
   * serialized then.
   * 
   * <p>This method should be called directly before saving to have most recent options.
   */
  @Override
  public void beforeSerialize() {
    downloadPluginsConfig();
  }

  /*
   * Restore plugins instances after deserialization.
   * 
   * <p>On load all fields of {@link Plugin} object are restored from JSON automaticly except
   * plugin instance. In this step the instance is created using fields populated from JSON.
   * 
   * <p>This method masks all QuimpPluginException exceptions that allows to skip defective plugin
   * and load all next plugins.
   */
  @Override
  public void afterSerialize() {
    // go through list and create new Plugin using old values that were restored after loading
    for (int i = 0; i < sPluginList.size(); i++) {
      String savedVer = sPluginList.get(i).ver;
      String name = getName(i); // only for exception handling to know name before setInstance
      // sets new instance of plugin using old configuration loaded
      // skip plugin that cannot be loaded or with wrong configuration
      try {
        // try load jar according to data saved in JSON
        setInstance(i, getName(i), isActive(i), sPluginList.get(i).config);
      } catch (QuimpPluginException e) {
        deletePlugin(i); // delete plugin on any error
        LOGGER.warn("Plugin name: " + name + " " + e.getMessage());
      }
      // check version compatibility - only inform user
      if (getInstance(i) != null) {
        if (!savedVer.equals(sPluginList.get(i).ver)) {
          LOGGER.warn("Loaded plugin (" + sPluginList.get(i).name
                  + ") is in different version than saved (" + sPluginList.get(i).ver + " vs. "
                  + savedVer + ")");
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
   * @see com.github.celldynamics.quimp.plugin.engine.PluginFactory#getPluginNames(int)
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
