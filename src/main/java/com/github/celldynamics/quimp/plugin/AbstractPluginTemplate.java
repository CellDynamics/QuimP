package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.QuimpException;

/**
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractPluginTemplate extends AbstractPluginBase {

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to {@link #AbstractPluginTemplate(AbstractPluginOptions)}.
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
   */
  protected AbstractPluginTemplate(AbstractPluginOptions options) {
    super(options);
  }

  /**
   * Constructor that allows to provide own parameters.
   * 
   * <p>Intended to run from API. In this mode all exceptions are re-thrown outside and plugin is
   * executed. Redirect messages to console. Set {@link AbstractOptionsParser#apiCall} to true.
   * 
   * @param argString parameters string like that passed in macro. If it is empty string or null
   *        constructor exits before deserialisation.
   * @param options Reference to plugin configuration container.
   * @throws QuimpPluginException on any error in plugin execution.
   */
  public AbstractPluginTemplate(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    super(argString, options);
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
