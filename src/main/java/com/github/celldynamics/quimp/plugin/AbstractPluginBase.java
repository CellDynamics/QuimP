package com.github.celldynamics.quimp.plugin;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.registration.Registration;

import ij.IJ;

/**
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractPluginBase extends AbstractOptionsParser implements IQuimpPlugin {

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to {@link #AbstractPluginQconf(AbstractPluginOptions)}.
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
  public AbstractPluginBase(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    super(argString, options);
  }

  /**
   * Called on plugin run.
   * 
   * <p>Overrides {@link AbstractOptionsParser#run(String)} to avoid loading QCONF file which is not
   * used
   * here.
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractOptionsParser#run(java.lang.String)
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
      logger.error("Problem with running plugin: " + e.getMessage());
    } finally {
      publishMacroString();
    }
  }

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

  protected abstract void executer() throws QuimpException;

}
