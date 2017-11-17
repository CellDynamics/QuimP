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
   * The Constant logger.
   */
  protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  protected AbstractPluginOptions options;

  /**
   * Indicate that plugin is run as macro from script. Blocks all UIs.
   * 
   * <p>Use this variable in child class together with {@link #apiCall} to decide whether to show
   * message in UI or console. the following rules applies:
   * <ol>
   * <li>errorSink = MessageSinkTypes.GUI - plugin called from IJ menu
   * <li>errorSink = MessageSinkTypes.IJERROR - plugin called from macro
   * <li>errorSink = MessageSinkTypes.CONSOLE - plugin called from API (but exceptions are
   * re-thrown to be handled in caller code)
   * </ol>
   * 
   * <p>Here assume GUI output for parameterless call from IJ (e.g. menu). Override this setting in
   * {@link #run(String)} method.
   */
  protected MessageSinkTypes errorSink = MessageSinkTypes.GUI;

  /**
   * Loaded configuration file.
   */
  protected QconfLoader qconfLoader; // main object representing loaded configuration file

  /**
   * If true plugin is run from parametrised constructor what usually mean API.
   */
  protected boolean apiCall;

  /**
   * Extension of file plugin asks for after execution in IJ mode.
   */
  protected String fileExt = FileExtensions.newConfigFileExt;

  /**
   * Default constructor, should not run plugin. It is called mostly by IJ. All exceptions are
   * handled in place.
   * 
   * @param options Reference to plugin configuration container.
   */
  public PluginTemplate(AbstractPluginOptions options) {
    apiCall = false;
    this.options = options;
  }

  /**
   * Constructor that allows to provide own parameters. Intended to run from API. In this mode all
   * exceptions are re-thrown outside and plugin is executed.
   * 
   * @param argString it can be null to ask user for file or it can be parameters string like that
   *        passed in macro.
   * @param options Reference to plugin configuration container.
   * @throws QuimpPluginException on any error in plugin execution.
   * @see #loadFile(String)
   */
  public PluginTemplate(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    apiCall = true;
    this.options = options;
    this.options = AbstractPluginOptions.deserialize2Macro(argString, options);
    errorSink = MessageSinkTypes.CONSOLE;
    try {
      loadFile(this.options.paramFile); // load configuration file and verify it
    } catch (Exception qe) {
      throw new QuimpPluginException(qe);
    }
  }

  /**
   * Load configuration file and execute plugin depending on file type.
   * 
   * <p>If file is QCONF then {@link #runFromQconf()} is executed, if ti is paQP then
   * {@link #runFromPaqp()}.
   * 
   * @param paramFile path to the file. It can be null or empty string to allow user pick the file.
   * 
   * @throws QuimpException When configuration file could not be loaded or it does not meet
   *         requirements.
   * @see #run(String)
   * @see #showUi(boolean)
   */
  protected void loadFile(String paramFile) throws QuimpException {
    File pf;
    if (paramFile == null || paramFile.isEmpty()) {
      pf = null;
    } else {
      pf = new File(paramFile);
    }
    if (qconfLoader == null || qconfLoader.getQp() == null) {
      // load new file
      qconfLoader = new QconfLoader(pf, fileExt);
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      if (qconfLoader.isFileLoaded() == QParams.QUIMP_11) { // old path
        runFromPaqp();
      } else if (qconfLoader.isFileLoaded() == QParams.NEW_QUIMP) { // new path
        qconfLoader.getBOA();
        runFromQconf();
      } else {
        qconfLoader = null; // failed load or checking
        throw new QuimpPluginException(
                "QconfLoader returned unsupported version of QuimP or error.");
      }
    }
  }

  /**
   * Parse parameters string passed from IJ, macro or API.
   * 
   * <p>This method assign also all internal fields of {@link AbstractPluginOptions} class to values
   * read from option string.
   * 
   * <p>String arg can be passed here from three sources: macro, IJProp.txt or from
   * {@link #PluginTemplate(String, AbstractPluginOptions)}. Generally, in arg is empty or null,
   * {@link #parseArgumentString(String)} tries to get it from Macro, if succeed it parses it and
   * returns true. Otherwise returns false. If arg is non-empty it assumes Macro call, sets proper
   * {@link #errorSink} and parses arg returning true.
   * 
   * @param arg arguments passed to {@link #run(String)} or
   *        #{@link #PluginTemplate(String, AbstractPluginOptions)}.
   * @return return true if something has been parsed
   * @throws QuimpPluginException when parsin failed
   */
  protected boolean parseArgumentString(String arg) throws QuimpPluginException {
    String argString;
    IJ.log(new QuimpToolsCollection().getQuimPversion());
    // decode possible params passed in macro or from constructor
    if (arg == null || arg.isEmpty()) { // no options passed directly to method
      argString = Macro.getOptions(); // check if there are any in macro
    } else {
      argString = arg; // options passed here - they must be in the same format as in macro
    }
    if (argString != null && !argString.isEmpty()) { // something passed
      errorSink = MessageSinkTypes.IJERROR; // set errors to ij, we are in macro mode
      options = AbstractPluginOptions.deserialize2Macro(argString, options);
      return true;
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.plugin.PlugIn#run(java.lang.String)
   */
  /**
   * Runner if plugin is called from IJ. Depending on {@link #errorSink} exceptions are redirected
   * to GUI, Console or IJ.
   */
  @Override
  public void run(String arg) {
    if (arg == null || arg.isEmpty()) {
      errorSink = MessageSinkTypes.GUI; // no parameters - assume menu call
    } else {
      errorSink = MessageSinkTypes.IJERROR; // parameters available - macro call
    }
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    try {
      if (parseArgumentString(arg)) { // process options passed to this method
        loadFile(options.paramFile); // load configuration file and verify
      } else {
        showUi(true);
      }

    } catch (QuimpException qe) {
      qe.setMessageSinkType(errorSink);
      qe.handleException(IJ.getInstance(), this.getClass().getSimpleName());
    } catch (Exception e) { // catch all exceptions here
      logger.debug(e.getMessage(), e);
      logger.error("Problem with running plugin: " + e.getMessage());
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

  protected abstract void runFromPaqp() throws QuimpException;

  /**
   * Open plugin UI. Called when there is no parameters to parse.
   * 
   * <p>If plugin can handle null {@link AbstractPluginOptions#paramFile} this method can simply
   * repeat {@link #loadFile(String)}
   * 
   * @param val true to show UI
   * @throws Exception on any error. Handled by {@link #run(String)}
   */
  protected abstract void showUi(boolean val) throws Exception;

}
