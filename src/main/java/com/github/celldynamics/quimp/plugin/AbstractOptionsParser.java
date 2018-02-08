package com.github.celldynamics.quimp.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.plugin.generatemask.GenerateMask_;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.Macro;
import ij.plugin.frame.Recorder;

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
 * This model assumes that plugin is run by {@link AbstractOptionsParser#run(String)} method where
 * string
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
 * {@link AbstractOptionsParser#validate()} to provide his own validators.
 * 
 * <p><h2>Run from API</h2>
 * For API calls plugin can be instanced from
 * {@link AbstractOptionsParser#PluginTemplate(String, AbstractPluginOptions)}, where String is a
 * parameter
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
 * <p>For this path {@link AbstractOptionsParser#apiCall} is set to true (can be used in overridden
 * {@link AbstractOptionsParser#runFromQconf()} for specific behaviour) and all exceptions are
 * passed to
 * caller with sink set to Console. This path does not support GUI.
 * 
 * <br><img src="doc-files/PluginTemplate_3_UML.png"/><br>
 * 
 * <p>By default QCONF is checked against BOA module. User can override
 * {@link AbstractOptionsParser#validate()} to provide his own validators.
 * 
 * <p>Executors {@link AbstractOptionsParser#runFromQconf()} and
 * {@link AbstractOptionsParser#runFromPaqp()} are
 * executed from {@link AbstractOptionsParser#loadFile(String)} which is default activity (load
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
public abstract class AbstractOptionsParser {

  /**
   * If true plugin is run from parametrised constructor what usually means API.
   */
  public boolean apiCall;
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
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to {@link #AbstractOptionsParser(AbstractPluginOptions)}.
   */
  protected AbstractOptionsParser() {
    throw new NoSuchMethodError();
  }

  /**
   * Default constructor.
   * 
   * <p>Set {@link #apiCall} to false and assign provided options to object.
   * 
   * @param options Reference to plugin configuration container.
   */
  protected AbstractOptionsParser(AbstractPluginOptions options) {
    apiCall = false;
    this.options = options;
  }

  /**
   * Constructor that allows to provide own parameters.
   * 
   * <p>Intended to run from API. In this mode all exceptions are re-thrown outside and plugin is
   * executed. Redirect messages to console. Set {@link #apiCall} to true.
   * 
   * @param argString parameters string like that passed in macro. If it is empty string or null
   *        constructor exits before deserialisation.
   * @param options Reference to plugin configuration container.
   * @throws QuimpPluginException on any error in plugin execution.
   */
  protected AbstractOptionsParser(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    apiCall = true;
    this.options = options;
    errorSink = MessageSinkTypes.CONSOLE;
    if (argString == null || argString.isEmpty()) {
      return;
    }
    this.options = AbstractPluginOptions.deserialize2Macro(argString, options);
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

  /**
   * Return {@link #options}.
   * 
   * @return the options
   */
  public AbstractPluginOptions getOptions() {
    return options;
  }

  /**
   * Helper, show macro string if recorder is active.
   */
  protected void publishMacroString() {
    // check whether config file name is provided or ask user for it
    logger.debug("Internal options " + options.serialize2Macro());
    if (Recorder.record) {
      Recorder.setCommand(this.getClass().getSimpleName());
      Recorder.recordOption(AbstractPluginOptions.KEY, options.serialize2Macro());
      Recorder.saveCommand();
    }
  }

}
