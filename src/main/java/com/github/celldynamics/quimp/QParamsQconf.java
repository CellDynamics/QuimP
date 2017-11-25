package com.github.celldynamics.quimp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState.BOAp;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.versions.Converter170202;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

/**
 * This class override most of methods from super class QParams. The goal of this class is rather
 * not to extend QParams but to use polymorphism to provide requested data to callers keeping
 * compatibility with old QuimP architecture. The QuimP uses QParams to keep parameters read from
 * configuration files (<i>paQP</i>, <i>snQP</i>) and then to provide some of parameters stored in
 * these files to local configuration classes such as e.g.
 * {@link com.github.celldynamics.quimp.plugin.ecmm.ECMp},
 * {@link com.github.celldynamics.quimp.plugin.qanalysis.Q_Analysis},
 * {@link com.github.celldynamics.quimp.plugin.ana.ANAp}. QuimP supports two independent file
 * formats:
 * <ol>
 * <li>based on separate files (old QuimP) such as case_cellno.paQP
 * <li>compound <i>case.QCONF</i> that contains data for all cells
 * </ol>
 * Many of parameters in underlying class QParams are set to be private and they are accessible by
 * setters and getters. Many setter/getter are overridden in this class and contains simple logic to
 * provide requested and expected data even if the source file was <i>QCONF</i>. There is also
 * method that convert parameters read from QCONF and fills underlying fields in QParams.
 * Appropriate object either QParam or QParamsQconf is created upon configuration file type. Owing
 * to Java late binding, always correct method is called even if the object is casted to QParams
 * 
 * @author p.baniukiewicz
 *
 */
public class QParamsQconf extends QParams {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QParamsQconf.class.getName());
  private Serializer<DataContainer> loaded; // instance of loaded data
  private File newParamFile;
  /**
   * Currently processed handler.
   * 
   * <p>This is compatibility parameter. Old QuimP uses separated files for every snake thus QParams
   * contained always correct values as given snake has been loaded. New QuimP uses composed file
   * and this field points to currently processed Handler and it must be controlled from outside.
   * For compatibility reasons all setters and getters assumes that there is only one Handler (as
   * in old QuimP). This field allow to set current Handler if QParamsEschange instance is used.
   */
  private int currentHandler;

  /**
   * Instantiates a new q params qconf.
   */
  public QParamsQconf() {

  }

  /**
   * Set default values for superclass, also prefix and path for files.
   * 
   * @param p <i>QCONF</i> file with extension
   */
  public QParamsQconf(File p) {
    super(p);
    currentHandler = 0;
    newParamFile = p;
    // prepare correct name for old parameters
    super.setParamFile(new File(QuimpToolsCollection
            .removeExtension(newParamFile.getParent() + File.separator + newParamFile.getName())
            + "_" + currentHandler + FileExtensions.configFileExt));
    paramFormat = QParams.NEW_QUIMP;
  }

  /**
   * Get configuration file (with path).
   * 
   * @return the newParamFile
   */
  @Override
  public File getParamFile() {
    return newParamFile;
  }

  /**
   * Get name of configuration file.
   * 
   * @return the prefix. Without any cell number in contrary to super.getFileName(). Only filename
   *         without path and extension.
   */
  @Override
  public String getFileName() {
    return QuimpToolsCollection.removeExtension(newParamFile.getName());
  }

  /**
   * Extract DataContainer from Serializer super class.
   * 
   * @return the loadedDataContainer
   */
  public DataContainer getLoadedDataContainer() {
    return loaded.obj;
  }

  /**
   * Return file creation date and other parameters.
   * 
   * @return QuimpVersion structure
   */
  public QuimpVersion getFileVersion() {
    return loaded.timeStamp;
  }

  /**
   * Read composite <i>QCONF</i> file.
   * 
   * <p>Update <tt>outputFileCore</tt> in {@link BOAp} to current QCONF.
   * 
   * @throws QuimpException when problem with loading/parsing JSON
   */
  @Override
  public void readParams() throws QuimpException {
    Serializer<DataContainer> s = new Serializer<>(DataContainer.class, QuimP.TOOL_VERSION);
    s.registerConverter(new Converter170202<>(QuimP.TOOL_VERSION));
    try {
      // load file and make first check of correctness
      loaded = s.load(getParamFile()); // try to load
      // restore qstate because some methods still need it
      BOA_.qState = getLoadedDataContainer().getBOAState();
      // update path and file core name
      if (getLoadedDataContainer().getBOAState() != null) {
        getLoadedDataContainer().getBOAState().boap
                .setOutputFileCore(newParamFile.getAbsolutePath());
      }
    } catch (Exception e) { // stop on fail (file or json error)
      LOGGER.debug(e.getMessage(), e);
      throw new QuimpException(
              "Loading or processing of " + getParamFile().getAbsolutePath() + " failed", e);
    }
    // second check of basic logic
    // checking against nulls is in Serializer
    if (!loaded.className.equals("DataContainer") || !loaded.timeStamp.getName().equals("QuimP")
            && !loaded.timeStamp.getName().equals(QuimpToolsCollection.defNote)) {
      LOGGER.debug("Not QuimP file?");
      throw new QuimpException(
              "Loaded file " + getParamFile().getAbsolutePath() + " is not QuimP file");
    }
    compatibilityLayer(); // fill underlying data (paQP) from QCONF
  }

  /**
   * Sets the active handler.
   *
   * @param num the new active handler
   */
  public void setActiveHandler(int num) {
    currentHandler = num;
    compatibilityLayer();
  }

  /**
   * Gets the active handler.
   *
   * @return the active handler
   */
  public int getActiveHandler() {
    return currentHandler;
  }

  /**
   * Write all parameters in new format.
   * 
   * <p>Makes pure dump what means that object is already packed with QuimP format. Used when
   * original data has been loaded, modified and then they must be saved again under the same
   * name.
   * 
   * @throws IOException When file can not be saved
   */
  @Override
  public void writeParams() throws IOException {
    LOGGER.debug("New file format: Updating data " + getParamFile());
    try {
      // loaded.obj.beforeSerialize(); // call explicitly beforeSerialize because Dump doesn't
      // do
      // Serializer.Dump(loaded, getParamFile(), BOA_.qState.boap.savePretty); // "loaded" is
      // already
      // packed by
      // Serializer
      Serializer<DataContainer> n;
      n = new Serializer<>(getLoadedDataContainer(), QuimP.TOOL_VERSION);
      if (getLoadedDataContainer().BOAState.boap.savePretty) {
        // configured
        n.setPretty();
      }
      n.save(getParamFile().getAbsolutePath());
      n = null;
    } catch (FileNotFoundException e) {
      LOGGER.error("File " + getParamFile() + " could not be saved. " + e.getMessage());
      LOGGER.debug(e.getMessage(), e);
      throw new IOException("File " + getParamFile() + " could not be saved. ", e);
    }
  }

  /**
   * Fill some underlying fields to assure compatibility between new and old formats.
   * 
   * <p><b>Warning</b>
   * 
   * <p>Some data depend on status of <tt>currentHandler</tt> that points to current outline. This
   * is
   * due to differences in file handling between old format (separate paQP for every cell) and new
   * (one file).
   */
  private void compatibilityLayer() {
    // fill underlying parameters
    super.setParamFile(new File(QuimpToolsCollection.removeExtension(newParamFile.getAbsolutePath())
            + "_" + currentHandler + FileExtensions.configFileExt));
    super.guessOtherFileNames();
    super.setSnakeQP(getSnakeQP());
    super.setStatsQP(getStatsQP());
    if (getLoadedDataContainer().getBOAState() != null) {
      super.setSegImageFile(getLoadedDataContainer().getBOAState().boap.getOrgFile());
      super.setImageScale(getLoadedDataContainer().getBOAState().boap.getImageScale());
      super.setFrameInterval(getLoadedDataContainer().getBOAState().boap.getImageFrameInterval());
      super.nmax = getLoadedDataContainer().getBOAState().boap.NMAX;
      super.deltaT = getLoadedDataContainer().getBOAState().boap.delta_t;
      super.maxIterations = getLoadedDataContainer().getBOAState().segParam.max_iterations;
      super.setNodeRes(getLoadedDataContainer().getBOAState().segParam.getNodeRes());
      super.setBlowup(getLoadedDataContainer().getBOAState().segParam.blowup);
      super.sampleTan = getLoadedDataContainer().getBOAState().segParam.sample_tan;
      super.sampleNorm = getLoadedDataContainer().getBOAState().segParam.sample_norm;
      super.velCrit = getLoadedDataContainer().getBOAState().segParam.vel_crit;
      super.centralForce = getLoadedDataContainer().getBOAState().segParam.f_central;
      super.contractForce = getLoadedDataContainer().getBOAState().segParam.f_contract;
      super.frictionForce = getLoadedDataContainer().getBOAState().boap.f_friction;
      super.imageForce = getLoadedDataContainer().getBOAState().segParam.f_image;
      super.sensitivity = getLoadedDataContainer().getBOAState().boap.sensitivity;
      super.finalShrink = getLoadedDataContainer().getBOAState().segParam.finalShrink;
      // set frames from snakes
      super.setStartFrame(getLoadedDataContainer().getBOAState().nest.getHandler(currentHandler)
              .getStartFrame());
      super.setEndFrame(
              getLoadedDataContainer().getBOAState().nest.getHandler(currentHandler).getEndFrame());
      if (getLoadedDataContainer().getEcmmState() != null) {
        super.setStartFrame(
                getLoadedDataContainer().getEcmmState().oHs.get(currentHandler).getStartFrame());
        super.setEndFrame(
                getLoadedDataContainer().getEcmmState().oHs.get(currentHandler).getEndFrame());
      }
      // fill only if ANA has been run
      if (getLoadedDataContainer().getANAState() != null) {
        super.cortexWidth =
                getLoadedDataContainer().getANAState().aS.get(currentHandler).getCortexWidthScale();

        // copy here is due to #204 - when new tiff is added to old loaded fluTiffs,
        // previous absolute paths / are extended to full: /xxx/yyy/Quimp
        File[] lf = getLoadedDataContainer().getANAState().aS.get(currentHandler).fluTiffs;
        this.fluTiffs = new File[lf.length];
        fluTiffs[0] = new File(lf[0].getPath());
        fluTiffs[1] = new File(lf[1].getPath());
        fluTiffs[2] = new File(lf[2].getPath());
      }

    }
  }

  /**
   * Write parameter file paQP in old format (QuimP11).
   * 
   * @throws IOException
   * 
   */
  public void writeOldParams() throws IOException {
    super.writeParams();
  }

  /*
   * (non-Javadoc)
   * 
   * In old way this was related always to loaded file that was separate for every snake. In new
   * way this field should not exist stand alone
   * 
   * @see com.github.celldynamics.quimp.QParams#getStartFrame()
   * 
   */
  @Override
  public int getStartFrame() {
    return super.getStartFrame();
  }

  /*
   * (non-Javadoc)
   * 
   * In old way this was related always to loaded file that was separate for every snake. In new
   * way this field should not exist stand alone
   * 
   * @see com.github.celldynamics.quimp.QParams#setStartFrame(int)
   * 
   */
  @Override
  public void setStartFrame(int startFrame) {
    super.setStartFrame(startFrame); // backward compatibility
    getLoadedDataContainer().getBOAState().nest.getHandler(currentHandler).startFrame = startFrame;
  }

  /*
   * (non-Javadoc)
   * 
   * In old way this was related always to loaded file that was separate for every snake. In new
   * way this field should not exist stand alone
   * 
   * @see com.github.celldynamics.quimp.QParams#getEndFrame()
   */
  @Override
  public int getEndFrame() {
    return super.getEndFrame();
  }

  /*
   * (non-Javadoc)
   * 
   * In old way this was related always to loaded file that was separate for every snake. In new
   * way this field should not exist stand alone
   * 
   * @see com.github.celldynamics.quimp.QParams#setEndFrame(int)
   */
  @Override
  public void setEndFrame(int endFrame) {
    super.setEndFrame(endFrame);
    getLoadedDataContainer().getBOAState().nest.getHandler(currentHandler).endFrame = endFrame;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#getImageScale()
   */
  @Override
  public double getImageScale() {
    return super.getImageScale();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#setImageScale(double)
   */
  @Override
  public void setImageScale(double imageScale) {
    getLoadedDataContainer().getBOAState().boap.setImageScale(imageScale);
    super.setImageScale(imageScale);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#getFrameInterval()
   */
  @Override
  public double getFrameInterval() {
    return super.getFrameInterval();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#setFrameInterval(double)
   */
  @Override
  public void setFrameInterval(double frameInterval) {
    getLoadedDataContainer().getBOAState().boap.setImageFrameInterval(frameInterval);
    super.setFrameInterval(frameInterval);
  }

  /**
   * 
   * @return {@link Nest} object from loaded dataset.
   */
  public Nest getNest() {
    if (getLoadedDataContainer() != null) {
      return getLoadedDataContainer().getBOAState().nest;
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#getBlowup()
   */
  @Override
  public int getBlowup() {
    return super.getBlowup();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#setBlowup(int)
   */
  @Override
  public void setBlowup(int blowup) {
    getLoadedDataContainer().getBOAState().segParam.blowup = blowup;
    super.setBlowup(blowup);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#getNodeRes()
   */
  @Override
  public double getNodeRes() {
    return super.getNodeRes();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#setNodeRes(int)
   */
  @Override
  public void setNodeRes(double nodeRes) {
    getLoadedDataContainer().getBOAState().segParam.setNodeRes(nodeRes);
    super.setNodeRes(nodeRes);
  }

  /**
   * For new file format it redirects call to super class searching for old files (paQP).
   * 
   * <p>Finally old files can be processed together with new one.
   * 
   * @return Array of found files.
   * @see com.github.celldynamics.quimp.QParams#findParamFiles()
   */
  @Override
  public File[] findParamFiles() {
    return super.findParamFiles();
  }

  /**
   * Create fake snQP name, for compatibility reasons.
   * 
   * @return theoretical name of snQP file which is used then to estimate names of map files by
   *         com.github.celldynamics.quimp.Qp class. This name contains \a suffix already
   * @see com.github.celldynamics.quimp.QParams#getSnakeQP()
   */
  @Override
  public File getSnakeQP() {
    String path = getParamFile().getParent();
    String file = QuimpToolsCollection.removeExtension(getParamFile().getName());
    return new File(
            path + File.separator + file + "_" + currentHandler + FileExtensions.snakeFileExt);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.QParams#getStatsQP()
   * 
   * @see com.github.celldynamics.quimp.QParamsQconf.getSnakeQP()
   */
  @Override
  public File getStatsQP() {
    String path = getParamFile().getParent();
    String file = QuimpToolsCollection.removeExtension(getParamFile().getName());
    return new File(
            path + File.separator + file + "_" + currentHandler + FileExtensions.statsFileExt);
  }

}
