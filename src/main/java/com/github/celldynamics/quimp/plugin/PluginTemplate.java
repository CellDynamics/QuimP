package com.github.celldynamics.quimp.plugin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.Macro;

/*
 * !>
 * @startuml doc-files/PluginTemplate_1_UML.png
 * actor User
 * User -> PluginTemplate : <<create>>
 * activate PluginTemplate
 * PluginTemplate -> PluginTemplate : apiCall=false
 * User -> PluginTemplate : run(arg)
 * alt arg == null || arg.isEmpty()
 * PluginTemplate -> Macro : getOptions()
 * Macro --> PluginTemplate : options
 * else
 * PluginTemplate -> PluginTemplate : options = arg
 * end
 * alt options == null || options.isEmpty()
 * PluginTemplate->PluginTemplate : showUI
 * else
 * PluginTemplate->PluginTemplate : runAsMacro = MessageSinkTypes.IJERROR
 * end
 * PluginTemplate->PluginTemplate : parseOptions(options)
 * PluginTemplate->Registration : <<create>>
 * PluginTemplate -> PluginTemplate : loadFile()
 * PluginTemplate -> PluginTemplate : runFromQCONF()
 * PluginTemplate --> User
 * deactivate PluginTemplate
 * @enduml
 * !<
 */

/**
 * This is template for general purpose plugin based on QCONF file exchange platform.
 * 
 * <p>Should not be used for standard IJ plugins. There are two ways to initiate the plugin: 1) from
 * constructor, 2) from {@link #run(String)} method. The latter is default one whereas the
 * constructor should just call {@link #run(String)}. Note, that plugin architecture assumes that
 * default constructor (and any other) does not run the plugins. Parametrised constructor can be
 * used for tests or for using plugin from API. In latter case all exceptions are repacked to
 * {@link QuimpPluginException} and re-thrown. Here is sequence of actions for IJ or macro run:<br>
 * <img src="doc-files/PluginTemplate_1_UML.png"/><br>
 * 
 * <p>For API call constructor {@link PluginTemplate#PluginTemplate(String)} run plugin.
 * 
 * <p>For API calls the parametrised constructor should be used.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class PluginTemplate implements IQuimpPlugin {

  /**
   * Loaded QCONF file.
   * 
   * <p>Must be overridden by {@link #parseOptions(String)} or left null to force
   * {@link QconfLoader} to show file selector.
   */
  protected File paramFile = null;

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(PluginTemplate.class.getName());

  /**
   * Indicate that plugin is run as macro from script. Blocks all UIs.
   * 
   * <p>Use this variable in child class together with {@link #apiCall} to decide whether to show
   * message in UI or console. the following rules applies:
   * <ol>
   * <li>runAsMacro = MessageSinkTypes.GUI - plugin called from IJ menu
   * <li>runAsMacro = MessageSinkTypes.IJERROR - plugin called from macro
   * <li>runAsMacro = MessageSinkTypes.CONSOLE - plugin called from API (but exceptions are
   * re-thrown to be handled in caller code)
   * </ol>
   */
  protected MessageSinkTypes runAsMacro = MessageSinkTypes.GUI;

  /**
   * Loaded QCONF file.
   * 
   * <p>Initialised by {@link #loadFile(File)} through this constructor.
   */
  protected QconfLoader qconfLoader; // main object representing loaded configuration file

  /**
   * If true plugin is run from parametrised constructor what usually mean API..
   * 
   */
  protected boolean apiCall;

  /**
   * Default constructor, should not run plugin. It is called mostly by IJ. All exceptions are
   * handled in place.
   */
  public PluginTemplate() {
    apiCall = false;
  }

  /**
   * Constructor that allows to provide own parameters. Intended to run from API. In this mode all
   * exceptions are re-thrown outside.
   * 
   * @param params it can be null to ask user for file or it can be parameters string like that
   *        passed in macro.
   * @throws QuimpPluginException on any error in plugin execution.
   */
  public PluginTemplate(String params) throws QuimpPluginException {
    apiCall = true;
    prepareToRun(params);
    runAsMacro = MessageSinkTypes.CONSOLE;
    try {
      loadFile(paramFile); // load configuration file given by paramFile and verify it
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      runFromQconf(); // run plugin
    } catch (Exception qe) {
      throw new QuimpPluginException(qe);
    }
  }

  /**
   * Load configuration file. (only if not loaded before).
   * 
   * <p>Validates also all necessary datafields in loaded QCONF file. Set <tt>qconfLoader</tt> field
   * on success or set it to <tt>null</tt>.
   * 
   * @param paramFile
   * 
   * @throws QuimpException When QCONF could not be loaded or it does not meet requirements.
   */
  private void loadFile(File paramFile) throws QuimpException {
    if (qconfLoader == null || qconfLoader.getQp() == null) {
      // load new file
      qconfLoader = new QconfLoader(paramFile, FileExtensions.newConfigFileExt);
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      if (qconfLoader.isFileLoaded() == QParams.NEW_QUIMP) { // new path
        // validate in case new format
        qconfLoader.getBOA(); // will throw exception if not present
      } else {
        qconfLoader = null; // failed load or checking
        throw new QuimpPluginException("QconfLoader returned unsupported version of QuimP or error."
                + " Only new format can be loaded");
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#setup()
   */
  @Override
  public abstract int setup();

  /*
   * (non-Javadoc)
   * 
   * @see
   * IQuimpCorePlugin#setPluginConfig(com.github.celldynamics.quimp.plugin.ParamList)
   */
  @Override
  public abstract void setPluginConfig(ParamList par) throws QuimpPluginException;

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#getPluginConfig()
   */
  @Override
  public abstract ParamList getPluginConfig();

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#showUI(boolean)
   */
  @Override
  public abstract int showUi(boolean val);

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#getVersion()
   */
  @Override
  public abstract String getVersion();

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#about()
   */
  @Override
  public abstract String about();

  /**
   * This method should assign all internal variables of child class to values read from options.
   * 
   * <p>It should also deal with null and must set {@link #paramFile} variable. It also provides
   * simple syntax checking. In case of problems, missing variables etc the {@link #about()}
   * should be called and displayed to user (rather in console as wrong syntax happens only when
   * called from macro or code)
   * 
   * @param options string in form key=val key1=val1 etc or null
   */
  protected abstract void parseOptions(String options);

  /**
   * Helper - set correct sing depending on assumed caller - macro, IJ.
   * 
   * @param arg arguments passed to {@link #run(String)}.
   */
  private void prepareToRun(String arg) {
    String options;
    IJ.log(new QuimpToolsCollection().getQuimPversion());
    // decode possible params passed in macro or from constructor
    if (arg == null || arg.isEmpty()) { // no options passed directly to method
      options = Macro.getOptions(); // check if there are any in macro
    } else {
      options = arg; // options passed here - they must be in the same format as in macro
    }
    if (options == null || options.isEmpty()) { // nothing passed let user decide about defaults
      showUi(true); // and in UI
    } else { // there is something, parse it
      runAsMacro = MessageSinkTypes.IJERROR; // set errors to ij, we are in macro mode
      parseOptions(options); // parse whatever it is
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.plugin.PlugIn#run(java.lang.String)
   */
  /**
   * That method shows version in console, checks registration and calls {@link #runFromQconf()}
   * which is main runner for plugin. Catches all exceptions.
   */
  @Override
  public void run(String arg) {
    prepareToRun(arg);
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    // check whether config file name is provided or ask user for it
    try {
      loadFile(paramFile); // load configuration file given by paramFile and verify it
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      runFromQconf(); // run plugin
    } catch (QuimpException qe) {
      qe.setMessageSinkType(runAsMacro);
      qe.handleException(IJ.getInstance(), "GenerateMask:");
    } catch (Exception e) { // catch all exceptions here
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Problem with running GenerateMask plugin: " + e.getMessage());
    }

  }

  /**
   * Main runner.
   * 
   * <p>This method expects that {@link #qconfLoader} is already set ({@link #run(String)}. In macro
   * or IJ mode exceptions will be handled in place and displayed as IJERROR or GUI message. For API
   * call (only if initialised by {@link #PluginTemplate(String)} exceptions are re-thrown.
   * 
   * @throws QuimpException on error
   */
  protected abstract void runFromQconf() throws QuimpException;

}
