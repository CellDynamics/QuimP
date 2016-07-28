/**
 * @file QParamsExchanger.java
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
    QParamsQconf(File p) {
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
    void readParams() throws QuimpException {
        Serializer<DataContainer> s = new Serializer<>(DataContainer.class);
        try {
            // load file and make first check of correctness
            loaded = s.load(paramFile); // try to load (skip afterSerialzie)
            BOA_.qState = loaded.obj.BOAState; // restore qstate because some methods still need it
        } catch (Exception e) { // stop on fail (file or json error)
            LOGGER.error(e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throw new QuimpException(
                    "Loading or processing of " + paramFile.getAbsolutePath() + " failed", e);
        }
        // second check of basic logic
        if (loaded.obj.BOAState == null || !loaded.className.equals("DataContainer")
                || !loaded.version[2].equals("QuimP") && !loaded.version[2].equals(Tool.defNote)) {
            LOGGER.error("Not QuimP file?");
            throw new QuimpException(
                    "Loaded file " + paramFile.getAbsolutePath() + " is not QuimP file");
        }
        // TODO Check config version here - more precisely (see #151)
        String[] ver = new Tool().getQuimPBuildInfo();
        if (!loaded.version[0].equals(ver[0])) {
            LOGGER.warn("Loaded config file is in diferent version than current QuimP (" + ver[0]
                    + " vs " + loaded.version[0]);
        }
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
    void writeParams() throws QuimpException {
        LOGGER.debug("New file format: Updating data " + paramFile);
        try {
            loaded.obj.beforeSerialize(); // call explicitly beforeSerialize because Dump doesn't do
            Serializer.Dump(loaded, paramFile, BOA_.qState.boap.savePretty); // "loaded" is already
                                                                             // packed by Serializer
        } catch (FileNotFoundException e) {
            LOGGER.error("File " + paramFile + " could not be saved. " + e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throw new QuimpException("File " + paramFile + " could not be saved. ", e);
        }
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
     * For new config file there is no need to check for presence other files (for old method other 
     * files were those with the same case name but for different cells than loaded NAME_0.paQP,
     * NAME_1.paQP, etc)
     * @return empty array to maintain compatibility with super class - it means that no files were
     * found 
     * @see uk.ac.warwick.wsbc.QuimP.QParams#findParamFiles()
     */
    @Override
    public File[] findParamFiles() {
        return new File[0];
    }

    /** 
     * Create fake snQP name, for compatibility reasons
     * 
     * @return theoretical name of snQP file which is used then to estimate names of map files by
     * uk.ac.warwick.wsbc.QuimP.Qp class. This name contains \a suffix already  
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getSnakeQP()
     */
    @Override
    public File getSnakeQP() {
        String path = getLoadedDataContainer().BOAState.boap.outFile.getParent();
        String file = getLoadedDataContainer().BOAState.boap.fileName;
        return new File(path + File.separator + file + "_" + currentHandler + ".snQP");
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
