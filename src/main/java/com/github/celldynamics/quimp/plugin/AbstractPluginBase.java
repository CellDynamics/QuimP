package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.registration.Registration;

import ij.IJ;
import ij.plugin.PlugIn;

/**
 * Base template of QuimP (legacy Fiji compatible) plugins.
 * 
 * <p>This template provides template of {@link #run(String)} method specified in
 * {@link PlugIn#run(String)} interface that detects context of caller by testing if there is valid
 * parameter string specified and then sets proper {@link AbstractOptionsParser#errorSink} and
 * {@link AbstractOptionsParser#apiCall}. Thus depending on context plugin will report errors in
 * correct place.
 * 
 * @author p.baniukiewicz
 * @see QuimpException#handleException(java.awt.Frame, String)
 */
public abstract class AbstractPluginBase extends AbstractOptionsParser implements IQuimpPlugin {

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to
   * {@link AbstractOptionsParser#AbstractOptionsParser(AbstractPluginOptions)}.
   */
  public AbstractPluginBase() {
    super();
  }

  /**
   * Default constructor.
   * 
   * <p>Set api call to false and assign provided options to object.
   * 
   * @param options Reference to plugin configuration container.
   */
  public AbstractPluginBase(AbstractPluginOptions options) {
    super(options);
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
  public AbstractPluginBase(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    super(argString, options);
  }

  /**
   * Called on plugin run by ImageJ or from API.
   * 
   * <p>Overrides {@link PlugIn#run(String)}. If input string is null or empty it sets
   * {@link AbstractOptionsParser#errorSink} to
   * {@link MessageSinkTypes#GUI}. Note that {@link AbstractOptionsParser#apiCall} is set by
   * choosing proper constructor. Then it tries to parse specified parameter string, if it succeeds,
   * {@link AbstractOptionsParser#options} is set and deserialised and {@link #executer()} method is
   * executed. If parsing fails, {@link #showUi(boolean)} is called with option true.
   * 
   * <p>Finally, macro string is published to ImageJ that represents current state of
   * {@link AbstractOptionsParser#options}.
   * 
   * <p>All exceptions thrown by plugin logic (from {@link #executer()}) are handled here depending
   * on {@link AbstractOptionsParser#errorSink} value.
   * 
   * @see AbstractOptionsParser#parseArgumentString(String)
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
        executer();
      } else {
        showUi(true);
      }

    } catch (QuimpException qe) {
      qe.setMessageSinkType(errorSink);
      qe.handleException(IJ.getInstance(), this.getClass().getSimpleName());
    } catch (Exception e) { // catch all exceptions here
      logger.debug(e.getMessage(), e);
      logger.error("Problem with running plugin: " + e.getMessage() + " (" + e.toString() + ")");
    } finally {
      publishMacroString();
    }
  }

  /**
   * Open plugin UI.
   * 
   * <p>Executed if {@link #run(String)} could not parse parameters.
   * 
   * @param val true to show UI
   * @throws Exception on any error. Handled by {@link #run(String)}
   */
  public abstract void showUi(boolean val) throws Exception;

  /**
   * Executed if {@link #run(String)} got parsable parameter string.
   * 
   * @throws QuimpException on any error. Exception is handled depending on
   *         {@link AbstractOptionsParser#errorSink} set by {@link #run(String)}
   */
  protected abstract void executer() throws QuimpException;

}
