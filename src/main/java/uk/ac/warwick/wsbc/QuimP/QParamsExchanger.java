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
 * Compatibility layer.
 * 
 * @remarks In future this class and should only deliver data from dump file on request.
 * 
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 */
public class QParamsExchanger extends QParams {

    private static final Logger LOGGER = LogManager.getLogger(QParamsExchanger.class.getName());

    private Serializer<DataContainer> loaded; // instance of loaded data

    /**
     * Set default values for superclass, also prefix and path for files
     * @param p
     */
    QParamsExchanger(File p) {
        super(p);
        currentHandler = 0;
        paramFormat = QParams.NEW_QUIMP;
    }

    /**
     * Extract DataContainer from Serialzier super class
     * 
     * @return the loadedDataContainer
     */
    @Override
    public DataContainer getLoadedDataContainer() {
        return loaded.obj;
    }

    /**
     * Read composite file
     * @throws QuimpException 
     */
    @Override
    void readParams() throws QuimpException {
        SerializerNoPluginSupport<DataContainer> s =
                new SerializerNoPluginSupport<>(DataContainer.class);
        try {
            loaded = s.load(paramFile); // try to load (skip afterSerialzie)
        } catch (Exception e) { // stop on fail (file or json error)
            LOGGER.error(e.getMessage());
            throw new QuimpException(e);
        }
        // check if there is no null on main parts
        if (loaded.obj.BOAState == null || loaded.version[2] == null || loaded.className == null
                || !loaded.className.equals("DataContainer")) {
            LOGGER.error("Not QuimP file?");
            throw new QuimpException("Not QuimP file?");
        }
        // check if this is quimp config - compare string in version header (see #151)
        if (!loaded.version[2].equals("QuimP")
                && !loaded.version[2].equals("name not found in jar")) {
            LOGGER.error("Not QuimP file?");
            throw new QuimpException("Not QuimP file?");
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
            Serializer.Dump(loaded, paramFile); // "loaded" is already packed by Serializer
        } catch (FileNotFoundException e) {
            LOGGER.error("File " + paramFile + " could not be saved. " + e.getMessage());
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

}

/**
 * Blocks execution of afterSerialize() in Serializer. 
 * 
 * This method is not necessary now because one does not want to restore full plugin state.
 * Other data do not need any additional operations.
 * 
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 * @param <T>
 */
class SerializerNoPluginSupport<T extends IQuimpSerialize> extends Serializer<T> {

    /**
     * @param obj
     * @param version
     */
    public SerializerNoPluginSupport(T obj, String[] version) {
        super(obj, version);
        doAfterSerialize = false; // block afterSerialzie()
    }

    /**
     * @param t
     */
    public SerializerNoPluginSupport(Type t) {
        super(t);
        doAfterSerialize = false; // block afterSerialzie()
    }

}
