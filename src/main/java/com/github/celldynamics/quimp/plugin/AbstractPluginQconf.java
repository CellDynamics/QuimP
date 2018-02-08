package com.github.celldynamics.quimp.plugin;

import java.io.File;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;

/**
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractPluginQconf extends AbstractPluginBase {

  /**
   * Loaded configuration file.
   */
  protected QconfLoader qconfLoader; // main object representing loaded configuration file

  /**
   * Extension of file plugin asks for after execution in IJ mode.
   */
  protected String fileExt = FileExtensions.newConfigFileExt;

  /**
   * This default constructor must be overridden in concrete class. It is called by IJ when plugin
   * instance is created. A concrete instance of {@link AbstractPluginOptions} class should be
   * created there and then passed to {@link #AbstractPluginQconf(AbstractPluginOptions)}.
   */
  protected AbstractPluginQconf() {
    super();
  }

  /**
   * Default constructor.
   * 
   * <p>Set api call to false and assign provided options to object.
   * 
   * @param options Reference to plugin configuration container.
   */
  protected AbstractPluginQconf(AbstractPluginOptions options) {
    super(options);
  }

  /**
   * Constructor that allows to provide own parameters.
   * 
   * <p>Intended to run from API. In this mode all exceptions are re-thrown outside and plugin is
   * executed. Redirect messages to console. It tries to laod QCONF file specified in
   * {@link AbstractPluginOptions#paramFile}. Set {@link AbstractOptionsParser#apiCall} to true.
   * 
   * @param argString parameters string like that passed in macro. If it is empty string or null
   *        constructor exits before deserialisation.
   * @param options Reference to plugin configuration container.
   * @throws QuimpPluginException on any error in plugin execution.
   * @see #loadFile(String)
   */
  protected AbstractPluginQconf(String argString, AbstractPluginOptions options)
          throws QuimpPluginException {
    super(argString, options);
    try {
      loadFile(this.options.paramFile); // load configuration file and verify it
    } catch (Exception qe) {
      throw new QuimpPluginException(qe);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractPluginBase#executer()
   */
  @Override
  protected void executer() throws QuimpException {
    loadFile(options.paramFile);
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
   * Override this file to pass your own validation of QCONF structure.
   * 
   * @throws QuimpException if file can not be validated.
   */
  protected void validate() throws QuimpException {
    qconfLoader.getBOA();
  }

  /**
   * Return {@link QconfLoader} object.
   * 
   * @return the qconfLoader
   */
  public QconfLoader getQconfLoader() {
    return qconfLoader;
  }

  /**
   * Main runner.
   * 
   * <p>This method expects that {@link #qconfLoader} is already set up ({@link #run(String)}. In
   * macro or IJ mode exceptions will be handled in place and displayed as IJERROR or GUI message.
   * For API call (only if initialised by
   * {@link #AbstractPluginQconf(String, AbstractPluginOptions)})
   * exceptions are re-thrown.
   * 
   * @throws QuimpException on error
   */
  protected abstract void runFromQconf() throws QuimpException;

  /**
   * Main runner.
   * 
   * <p>This method expects that {@link #qconfLoader} is already set up ({@link #run(String)}. In
   * macro or IJ mode exceptions will be handled in place and displayed as IJERROR or GUI message.
   * For API call (only if initialised by
   * {@link #AbstractPluginQconf(String, AbstractPluginOptions)})
   * exceptions are re-thrown.
   * 
   * @throws QuimpException on error
   */
  protected abstract void runFromPaqp() throws QuimpException;

}
