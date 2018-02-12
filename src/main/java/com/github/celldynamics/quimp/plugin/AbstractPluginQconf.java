package com.github.celldynamics.quimp.plugin;

import java.io.File;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.QconfLoader;

/**
 * Template of plugin focused at processing QCONF/paQP files.
 * 
 * <p>This type of plugin opens QCONF/paQP file immediately after run, process it and returns
 * results. User interface is not directly supported here unless you override {@link #executer()}.
 * For UI plugins use {@link AbstractPluginTemplate}. If {@link AbstractPluginOptions#paramFile} is
 * not null and not empty, it will be used, otherwise template displays file dialog.
 * 
 * <p>Following workflow specified in {@link AbstractPluginBase}, this implementation calls
 * {@link #loadFile(String)} from {@link #executer()}.
 * 
 * @author p.baniukiewicz
 * @see AbstractPluginTemplate
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
   * <p>Intended to run from API. Set {@link #apiCall} to true and {@link #errorSink} to
   * {@link MessageSinkTypes#CONSOLE}.
   * {@link AbstractPluginOptions} is initialised from specified string and assigned to this
   * instance.
   * 
   * <p>It loads QCONF.paQP file specified in {@link AbstractPluginOptions#paramFile}. If file is
   * not specified it shows load file window.
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
   * Load specified configuration file and execute plugin depending on file type.
   * 
   * <p>If file is QCONF then {@link #runFromQconf()} is executed, if it is paQP then
   * {@link #runFromPaqp()}. Validate loaded QCONF file by {@link #validate()}.
   * 
   * @param paramFile path to the file. It can be null or empty string to allow user pick the file.
   * 
   * @throws QuimpException When configuration file could not be loaded or it does not meet
   *         requirements.
   * @see #validate()
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
        return; // not loaded (cancelled)
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
   * Override this method to pass your own validation of QCONF structure.
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
   * Called if loaded file is QCONF.
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
   * Called if loaded file is paQP.
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
