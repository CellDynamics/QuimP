package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;

/**
 * General purpose plugin template for UI plugins.
 * 
 * <p>This template handle typical plugin that shows UI if run without parameters or run in
 * background (without UI) if valid parameter string is passed to {@link #run(String)}.
 * 
 * <p>Following workflow specified in {@link AbstractPluginBase}, this implementation calls
 * {@link #runPlugin()} from {@link #executer()}.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractPluginTemplate extends AbstractPluginBase {

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to
   * {@link #AbstractPluginTemplate(AbstractPluginOptions, String)}.
   */
  protected AbstractPluginTemplate() {
    super();
  }

  /**
   * Default constructor.
   * 
   * <p>Set api call to false and assign provided options to object.
   * 
   * @param options Reference to plugin configuration container.
   * @param pluginName name of the plugin that will be displayed in Macro Recorder
   */
  protected AbstractPluginTemplate(AbstractPluginOptions options, String pluginName) {
    super(options, pluginName);
  }

  /**
   * Constructor that allows to provide own parameters.
   * 
   * <p>Intended to run from API. Set {@link #apiCall} to true and {@link #errorSink} to
   * {@link MessageSinkTypes#CONSOLE}.
   * {@link AbstractPluginOptions} is initialised from specified string and assigned to this
   * instance.
   * 
   * @param argString parameters string like that passed in macro. If it is empty string or null
   *        constructor exits before deserialisation.
   * @param options Reference to plugin configuration container.
   * @param pluginName name of the plugin that will be displayed in Macro Recorder
   * @throws QuimpPluginException on any error in plugin execution.
   */
  public AbstractPluginTemplate(String argString, AbstractPluginOptions options, String pluginName)
          throws QuimpPluginException {
    super(argString, options, pluginName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractPluginBase#executer()
   */
  @Override
  protected void executer() throws QuimpException {
    runPlugin();
  }

  /**
   * Main plugin logic.
   * 
   * @throws QuimpPluginException on any error, handled by {@link #run(String)}
   */
  protected abstract void runPlugin() throws QuimpPluginException;

}
