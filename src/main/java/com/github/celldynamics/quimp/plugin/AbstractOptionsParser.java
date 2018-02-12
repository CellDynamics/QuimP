package com.github.celldynamics.quimp.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.Macro;
import ij.plugin.frame.Recorder;

/**
 * This is template allows for serialize/deserialize plugin options to/from macro string.
 *
 * @author p.baniukiewicz
 * @see AbstractPluginOptions
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
   * Constructor that allows to provide own parameter string.
   * 
   * <p>Intended to run from API. Set {@link #apiCall} to true and {@link #errorSink} to
   * {@link MessageSinkTypes#CONSOLE}.
   * {@link AbstractPluginOptions} is initialised from specified string and assigned to this
   * instance.
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
   * Analyse and parse parameter string passed from IJ, macro or API.
   * 
   * <p>If arg is empty or null, {@link #parseArgumentString(String)} tries to get it from Macro, if
   * succeeds it parses it and returns true. Otherwise returns false. If arg is non-empty it assumes
   * Macro call, sets {@link #errorSink} to {@link MessageSinkTypes#IJERROR} and parses arg
   * returning true. {@link #apiCall} is set to false.
   * 
   * <p>If parser succeeded, internal {@link AbstractPluginOptions} object is properly initalised
   * and deserialised.
   * 
   * @param arg parameter string
   * @return return true if something has been parsed, false if input was empty or null
   * @throws QuimpPluginException when parsing failed
   * @see AbstractPluginOptions
   * @see #getOptions()
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
   * Helper, show macro parameters string if recorder is active.
   * 
   * <p>Perform serialisation of {@link AbstractPluginOptions} object composed with this
   * class.
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
