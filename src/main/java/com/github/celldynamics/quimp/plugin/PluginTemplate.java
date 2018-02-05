package com.github.celldynamics.quimp.plugin;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.generatemask.GenerateMask_;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.Macro;

/*
 * !>
 * @startuml doc-files/PluginTemplate_1_UML.png
 * User->(Run from IJ) : From macro\nor IJ menu
 * User->(Run from API) : Run from code
 * @enduml
 * !<
 * 
 * !>
 * @startuml doc-files/PluginTemplate_2_UML.png
 * actor User
 * actor IJ
 * User-->IJ : Run plugin
 * IJ-->ConcretePlugin : create instance
 * note left: Default constructor
 * ConcretePlugin-->PluginTemplate : PluginTemplate(AbstractPluginOptions)
 * activate PluginTemplate
 * PluginTemplate->PluginTemplate : apiCall=false
 * IJ-->PluginTemplate : run(String)
 * activate PluginTemplate
 * PluginTemplate->Registration
 * Registration-->PluginTemplate
 * PluginTemplate->PluginTemplate : parseArgumentString(String)
 * note left 
 * Fill concrete instance of
 * AbstractPluginOptions passed 
 * to constructor. runFromXX()
 * can then use Options object.
 * end note 
 * alt returned true
 * PluginTemplate->PluginTemplate : loadFile
 * activate PluginTemplate
 * PluginTemplate->PluginTemplate : validate
 * deactivate PluginTemplate
 * alt loaded QCONF
 * PluginTemplate->ConcretePlugin : runFromQconf()
 * else loaded paQP
 * PluginTemplate->ConcretePlugin : runFromPaQp()
 * end
 * note left: Detect File type(QCONF/paQP)
 * else returned false
 * PluginTemplate->ConcretePlugin : showUI
 * note right: It means no parameters provided\nperhaps menu call
 * end
 * @enduml
 * !<
 * 
 * !>
 * @startuml doc-files/PluginTemplate_3_UML.png
 * actor User
 * User-->ConcretePlugin : create instance(String)
 * note left: Default constructor
 * ConcretePlugin-->PluginTemplate : PluginTemplate(String, AbstractPluginOptions)
 * activate PluginTemplate
 * PluginTemplate->PluginTemplate : apiCall=true
 * PluginTemplate->deserialize2Macro
 * deserialize2Macro-->PluginTemplate : options object
 * PluginTemplate->PluginTemplate : loadFile
 * activate PluginTemplate
 * PluginTemplate->PluginTemplate : validate
 * deactivate PluginTemplate
 * alt loaded QCONF
 * PluginTemplate->ConcretePlugin : runFromQconf()
 * else loaded paQP
 * PluginTemplate->ConcretePlugin : runFromPaQp()
 * end
 * note left: Detect File type(QCONF/paQP)
 * @enduml
 * !<
 */

/**
 * This is template for general purpose plugin based on QCONF file exchange platform. It provided
 * parsing parameters string, loading QuimP datafile and setting correct backend for exception
 * handling.
 * 
 * <p>There are two main use cases: <br><img src="doc-files/PluginTemplate_1_UML.png"/><br>
 * 
 * <ul>
 * <li>Run from IJ. It cover either macro or execution from IJ menu.
 * <li>Run from API.
 * </ul>
 * 
 * <h2>Run from IJ</h2>
 * This model assumes that plugin is run by {@link PluginTemplate#run(String)} method where string
 * parameter can be passed from Macro or IJProps file. It can be empty if plugin is run from IJ
 * menu. All exceptions are redirected to IJERROR stream, except the latter case which assumes
 * interaction with plugin and redirects exceptions to GUI (see
 * {@link QuimpException#handleException(java.awt.Frame, String)}. Note that IJ expects default
 * constructor in concrete class there fore recommended way of plugin initialisation is:
 * 
 * <pre>
 * <code>
 * class Concrete extends PluginTemplate {
 *    public Concrete() {
 *      super(new ConcreteOptions());
 *    }
 * }
 * </code>
 * </pre>
 * 
 * <br><img src="doc-files/PluginTemplate_2_UML.png"/><br>
 * 
 * <p>By default QCONF is checked against BOA module. User can override
 * {@link PluginTemplate#validate()} to provide his own validators.
 * 
 * <p><h2>Run from API</h2>
 * For API calls plugin can be instanced from
 * {@link PluginTemplate#PluginTemplate(String, AbstractPluginOptions)}, where String is a parameter
 * string in format required by plugin. From concrete class it can be done by:
 * 
 * <pre>
 * <code>
 * class Concrete extends PluginTemplate {
 *    public Concrete(String paramString) {
 *      super(paramString, new ConcreteOptions());
 *    }
 * }
 * </code>
 * </pre>
 * 
 * <p>For this path {@link PluginTemplate#apiCall} is set to true (can be used in overridden
 * {@link PluginTemplate#runFromQconf()} for specific behaviour) and all exceptions are passed to
 * caller with sink set to Console. This path does not support GUI.
 * 
 * <br><img src="doc-files/PluginTemplate_3_UML.png"/><br>
 * 
 * <p>By default QCONF is checked against BOA module. User can override
 * {@link PluginTemplate#validate()} to provide his own validators.
 * 
 * <p>Executors {@link PluginTemplate#runFromQconf()} and {@link PluginTemplate#runFromPaqp()} are
 * executed from {@link PluginTemplate#loadFile(String)} which is default activity (load
 * configuration and execute plugin). If plugin supports GUI, they should be called from
 * {@link #showUi(boolean)}.
 * 
 * <p>If plugin does not load configuration file on start (by {@link #run(String)}, concrete class
 * should override {@link #loadFile(String)} to call {@link #runFromQconf()} directly.
 * 
 * <p>Default constructor would always need {@link #run(String)} method.
 * 
 * @author p.baniukiewicz
 * @see GenerateMask_
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
   * If true plugin is run from parametrised constructor what usually means API.
   */
  public boolean apiCall;

  /**
   * Extension of file plugin asks for after execution in IJ mode.
   */
  protected String fileExt = FileExtensions.newConfigFileExt;

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to {@link #PluginTemplate(AbstractPluginOptions)}.
   */
  protected PluginTemplate() {
    throw new NoSuchMethodError();
  }

  /**
   * Default constructor, should be encapsulated by default constructor of child class. Used when
   * instance is created by IJ.
   * 
   * @param options Reference to plugin configuration container.
   */
  protected PluginTemplate(AbstractPluginOptions options) {
    apiCall = false;
    this.options = options;
  }

  /**
   * Constructor that allows to provide own parameters. Intended to run from API. In this mode all
   * exceptions are re-thrown outside and plugin is executed.
   * 
   * @param argString parameters string like that passed in macro. If it is empty string or null
   *        constructor exits before deserialisation.
   * @param options Reference to plugin configuration container.
   * @throws QuimpPluginException on any error in plugin execution.
   * @see #loadFile(String)
   */
  protected PluginTemplate(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    apiCall = true;
    this.options = options;
    errorSink = MessageSinkTypes.CONSOLE;
    if (argString == null || argString.isEmpty()) {
      return;
    }
    this.options = AbstractPluginOptions.deserialize2Macro(argString, options);
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
        validate();
        runFromQconf();
      } else {
        qconfLoader = null; // failed load or checking
        throw new QuimpPluginException(
                "QconfLoader returned unsupported version of QuimP or error.");
      }
    }
  }

  /**
   * OVerride this file to pass your own validation of QCONF structure.
   * 
   * @throws QuimpException if file can not be validated.
   */
  protected void validate() throws QuimpException {
    qconfLoader.getBOA();

  }

  /**
   * Parse parameters string passed from IJ, macro or API.
   * 
   * <p>This method assign also all internal fields of {@link AbstractPluginOptions} class to values
   * read from option string.
   * 
   * <p>String arg can be passed here from three sources: macro, IJProp.txt or from
   * {@link #PluginTemplate(String, AbstractPluginOptions)}. Generally, if arg is empty or null,
   * {@link #parseArgumentString(String)} tries to get it from Macro, if succeeds it parses it and
   * returns true. Otherwise returns false. If arg is non-empty it assumes Macro call, sets proper
   * {@link #errorSink} and parses arg returning true.
   * 
   * @param arg arguments passed to {@link #run(String)} or
   *        #{@link #PluginTemplate(String, AbstractPluginOptions)}.
   * @return return true if something has been parsed
   * @throws QuimpPluginException when parsing failed
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
   * <p>This method expects that {@link #qconfLoader} is already set up ({@link #run(String)}. In
   * macro or IJ mode exceptions will be handled in place and displayed as IJERROR or GUI message.
   * For API call (only if initialised by {@link #PluginTemplate(String, AbstractPluginOptions)})
   * exceptions are re-thrown.
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
  public abstract void showUi(boolean val) throws Exception;

  /**
   * Return {@link QconfLoader} object.
   * 
   * @return the qconfLoader
   */
  public QconfLoader getQconfLoader() {
    return qconfLoader;
  }

  /**
   * Return {@link #options}.
   * 
   * @return the options
   */
  public AbstractPluginOptions getOptions() {
    return options;
  }

}
