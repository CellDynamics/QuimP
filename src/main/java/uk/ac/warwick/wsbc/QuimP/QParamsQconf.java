/**
 * @file QParamsQconf.java
 * @date 26 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class override most of methods from super class QParams. 
 * The goal of this class is rather not to extend QParams but to use polymorphism to provide 
 * requested data to callers keeping compatibility with old quimP architecture. 
 * The QuimP uses QParams to keep parameters read from configuration files (\a paQP, \a snQP) and 
 * then to provide some of parameters stored in these files  to local configuration classes such as 
 * @ref uk.ac.warwick.wsbc.QuimP.ECMp "ECMp" or @ref uk.ac.warwick.wsbc.QuimP.Qp "Qp".
 * QuimP supports two independent file formats:
 * <ol>
 * <li> based on separate files (old QuimP) such as \a case_cellno.paQP
 * <li> compound \a case.QCONF that contains data for all cells
 * </ol>
 * Many of parameters in underlying class QParams are set to be private and they are accessible
 * by setters and getters. Every setter/getter is overrode in this class and contains simple logic
 * to provide requested and expected data even if the source file was \a QCONF. 
 * Appropriate object either QParam or QParamsQconf is created upon configuration file type. 
 * Owing to Java late binding, always correct method is called even if the object is casted to QParams 
 * 
 * @todo TODO These two classes should be in separate package to have better control over fields by 
 * setters and getters
 * 
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 */
public class QParamsQconf extends QParams {

    private static final Logger LOGGER = LogManager.getLogger(QParamsQconf.class.getName());

    private Serializer<DataContainer> loaded; // instance of loaded data

    /**
     * Set default values for superclass, also prefix and path for files
     * 
     * @param p \c QCONF file
     */
    public QParamsQconf(File p) {
        super(p);
        paramFormat = QParams.NEW_QUIMP;
    }

    /**
     * Extract DataContainer from Serializer super class
     * 
     * @return the loadedDataContainer
     */
    @Override
    public DataContainer getLoadedDataContainer() {
        return loaded.obj;
    }

    /**
     * Read composite \a QCONF file
     * 
     * @throws QuimpException when problem with loading/parsing JSON
     */
    @Override
    public void readParams() throws QuimpException {
        Serializer<DataContainer> s = new Serializer<>(DataContainer.class);
        try {
            // load file and make first check of correctness
            loaded = s.load(getParamFile()); // try to load (skip afterSerialzie)
            BOA_.qState = loaded.obj.BOAState; // restore qstate because some methods still need it
        } catch (Exception e) { // stop on fail (file or json error)
            LOGGER.error(e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throw new QuimpException(
                    "Loading or processing of " + getParamFile().getAbsolutePath() + " failed", e);
        }
        // second check of basic logic
        if (loaded.obj.BOAState == null)
            throw new QuimpException("Loaded file " + getParamFile().getAbsolutePath()
                    + " does not contain BOA data");
        if (!loaded.className.equals("DataContainer")
                || !loaded.version[2].equals("QuimP") && !loaded.version[2].equals(Tool.defNote)) {
            LOGGER.error("Not QuimP file?");
            throw new QuimpException(
                    "Loaded file " + getParamFile().getAbsolutePath() + " is not QuimP file");
        }
        // TODO Check config version here - more precisely (see #151)
        String[] ver = new Tool().getQuimPBuildInfo();
        if (!loaded.version[0].equals(ver[0])) {
            LOGGER.warn("Loaded config file is in diferent version than current QuimP (" + ver[0]
                    + " vs " + loaded.version[0]);
        }
        compatibilityLayer();
    }

    /**
     * Write all parameters in new format. 
     * 
     * Makes pure dump what means that object is already packed with QuimP format. Used when
     * original data has been loaded, modified and then they must be saved again under the same
     * name.
     * @throws QuimpException When file can not be saved
     */
    @Override
    public void writeParams() throws QuimpException {
        LOGGER.debug("New file format: Updating data " + getParamFile());
        try {
            loaded.obj.beforeSerialize(); // call explicitly beforeSerialize because Dump doesn't do
            Serializer.Dump(loaded, getParamFile(), BOA_.qState.boap.savePretty); // "loaded" is
                                                                                  // already
                                                                                  // packed by
                                                                                  // Serializer
        } catch (FileNotFoundException e) {
            LOGGER.error("File " + getParamFile() + " could not be saved. " + e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throw new QuimpException("File " + getParamFile() + " could not be saved. ", e);
        }
    }

    /**
     * Fill some underlying fields to assure compatibility between new and old formats.
     */
    private void compatibilityLayer() {
        // fill underlying parameters
        super.setSegImageFile(loaded.obj.BOAState.boap.getOrgFile());
        super.setSnakeQP(getSnakeQP());
        super.setStatsQP(getStatsQP());
        if (loaded.obj.BOAState != null) {
            super.setImageScale(loaded.obj.BOAState.boap.getImageScale());
            super.setFrameInterval(loaded.obj.BOAState.boap.getImageFrameInterval());
            super.NMAX = loaded.obj.BOAState.boap.NMAX;
            super.delta_t = loaded.obj.BOAState.boap.delta_t;
            super.max_iterations = loaded.obj.BOAState.segParam.max_iterations;
            super.setNodeRes(loaded.obj.BOAState.segParam.getNodeRes());
            super.setBlowup(loaded.obj.BOAState.segParam.blowup);
            super.sample_tan = loaded.obj.BOAState.segParam.sample_tan;
            super.sample_norm = loaded.obj.BOAState.segParam.sample_norm;
            super.vel_crit = loaded.obj.BOAState.segParam.vel_crit;
            super.f_central = loaded.obj.BOAState.segParam.f_central;
            super.f_contract = loaded.obj.BOAState.segParam.f_contract;
            super.f_friction = loaded.obj.BOAState.boap.f_friction;
            super.f_image = loaded.obj.BOAState.segParam.f_image;
            super.sensitivity = loaded.obj.BOAState.boap.sensitivity;
            // fill only if ANA has been run
            if (loaded.obj.ANAState != null) {
                super.setStartFrame(loaded.obj.ECMMState.oHs.get(currentHandler).getStartFrame());
                super.setEndFrame(loaded.obj.ECMMState.oHs.get(currentHandler).getEndFrame());
                super.finalShrink = loaded.obj.BOAState.segParam.finalShrink;
            }
        }

        // super.cortexWidth = loaded.obj.BOAState.boap.cor
    }

    /**
     * Write parameter file paQP in old format (QuimP11).
     * 
     * @throws QuimpException 
     * 
     */
    public void writeOldParams() throws QuimpException {
        super.writeParams();
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getStartFrame()
     * @warning In old way this was related always to loaded file that was separate for every
     * snake. In new way this field should not exist stand alone
     */
    @Override
    public int getStartFrame() {
        return getLoadedDataContainer().BOAState.nest.getHandler(currentHandler).startFrame;
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setStartFrame(int)
     * @warning In old way this was related always to loaded file that was separate for every
     * snake. In new way this field should not exist stand alone
     */
    @Override
    public void setStartFrame(int startFrame) {
        super.setStartFrame(startFrame); // backward compatibility
        // loadedDataContainer.BOAState.nest.getHandler(currentHandler).startFrame = startFrame;
        throw new UnsupportedOperationException("Not finished yet");
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getEndFrame()
     * @warning In old way this was related always to loaded file that was separate for every
     * snake. In new way this field should not exist stand alone
     */
    @Override
    public int getEndFrame() {
        return getLoadedDataContainer().BOAState.nest.getHandler(currentHandler).endFrame;
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setEndFrame(int)
     * @warning In old way this was related always to loaded file that was separate for every
     * snake. In new way this field should not exist stand alone
     */
    @Override
    public void setEndFrame(int endFrame) {
        super.setEndFrame(endFrame);
        throw new UnsupportedOperationException("Not finished yet");
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getImageScale()
     */
    @Override
    public double getImageScale() {
        return getLoadedDataContainer().BOAState.boap.getImageScale();
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setImageScale(double)
     */
    @Override
    public void setImageScale(double imageScale) {
        super.setImageScale(imageScale);
        throw new UnsupportedOperationException("Not finished yet");
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getFrameInterval()
     */
    @Override
    public double getFrameInterval() {
        return getLoadedDataContainer().BOAState.boap.getImageFrameInterval();
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setFrameInterval(double)
     */
    @Override
    public void setFrameInterval(double frameInterval) {
        super.setFrameInterval(frameInterval);
        throw new UnsupportedOperationException("Not finished yet");
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getNest()
     */
    @Override
    public Nest getNest() {
        if (getLoadedDataContainer() != null)
            return getLoadedDataContainer().BOAState.nest;
        else
            return super.getNest();
    }

    /** (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getBlowup()
     */
    @Override
    public int getBlowup() {
        throw new UnsupportedOperationException("Not finished yet");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setBlowup(int)
     */
    @Override
    public void setBlowup(int blowup) {
        getLoadedDataContainer().BOAState.segParam.blowup = blowup;
        throw new UnsupportedOperationException("Not finished yet");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getNodeRes()
     */
    @Override
    public double getNodeRes() {
        throw new UnsupportedOperationException("Not finished yet");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setNodeRes(int)
     */
    @Override
    public void setNodeRes(double nodeRes) {
        getLoadedDataContainer().BOAState.segParam.setNodeRes(nodeRes);
        throw new UnsupportedOperationException("Not finished yet");
    }

    /** 
     * For new file format it redirects call to super class searching for old files (paQP).
     * 
     * Finally old files can be processed together with new one.
     * 
     * @return
     * @see uk.ac.warwick.wsbc.QuimP.QParams.findParamFiles()
     */
    @Override
    public File[] findParamFiles() {
        return super.findParamFiles();
    }

    /** 
     * Create fake snQP name, for compatibility reasons
     * 
     * @return theoretical name of snQP file which is used then to estimate names of map files by
     * uk.ac.warwick.wsbc.QuimP.Qp class. This name contains \a suffix already  
     * @see uk.ac.warwick.wsbc.QuimP.QParams.getSnakeQP()
     */
    @Override
    public File getSnakeQP() {
        String path = getParamFile().getParent();
        String file = Tool.removeExtension(getParamFile().getName());
        return new File(path + File.separator + file + "_" + currentHandler + ".snQP");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getStatsQP()
     * @see uk.ac.warwick.wsbc.QuimP.QParamsQconf.getSnakeQP()
     */
    @Override
    public File getStatsQP() {
        String path = getParamFile().getParent();
        String file = Tool.removeExtension(getParamFile().getName());
        return new File(path + File.separator + file + "_" + currentHandler + ".stQP.csv");
    }

}

/**
 * Blocks execution of afterSerialize() in Serializer. 
 * 
 * This method is not necessary now because one does not want to restore full plugin state.
 * Other data do not need any additional operations. (
 * 
 * @warning currently not used as loaded BOAState must be deserialized to restore Snakes from
 * Elements arrays
 * 
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 * @param <T>
 * @deprecated But left here as example how to tackle the problem
 */
@Deprecated
class SerializerNoPluginSupport<T extends IQuimpSerialize> extends Serializer<T> {

    /**
     * @param obj
     * @param version
     */
    public SerializerNoPluginSupport(T obj, String[] version) {
        super(obj, version);
        doAfterSerialize = true; // false blocks afterSerialzie()
    }

    /**
     * @param t
     */
    public SerializerNoPluginSupport(Type t) {
        super(t);
        doAfterSerialize = true; // false blocks afterSerialzie()
    }

}
