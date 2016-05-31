/**
 * @file QParamsExchanger.java
 * @date 26 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.BOA_.BOAState;

/**
 * Compatibility layer.
 * 
 * @remarks In future this class and super should be only deliverer of binary object from data file.
 * 
 * @author p.baniukiewicz
 * @date 26 May 2016
 *
 */
public class QParamsExchanger extends QParams {

    private static final Logger LOGGER = LogManager.getLogger(QParamsExchanger.class.getName());
    private BOAState loadedBOAState;

    /**
     * Set default values for superclass, also prefix and path for files
     * @param p
     */
    QParamsExchanger(File p) {
        super(p);
        paramFormat = QParams.NEW_QUIMP;
    }

    /**
     * Read composite file
     */
    @Override
    boolean readParams() {
        Serializer<DataContainer> loaded; // instance of loaded data
        SerializerNoPluginSupport<DataContainer> s =
                new SerializerNoPluginSupport<>(DataContainer.class);
        try {
            loaded = s.load(paramFile); // try to load (skip afterSerialzie)
        } catch (Exception e) { // stop on fail (file or json error)
            LOGGER.error(e.getMessage());
            return false;
        }
        // check if this is quimp config - compare string in version header (see #151)
        if (!loaded.version[2].equals("QuimP")
                && !loaded.version[2].equals("name not found in jar")) {
            LOGGER.error("Not QuimP file?");
            return false;
        }
        // TODO Check config version here - more precisely (see #151)
        String[] ver = new Tool().getQuimPBuildInfo();
        if (!loaded.version[0].equals(ver[0])) {
            LOGGER.warn("Loaded config file is in diferent version than current QuimP (" + ver[0]
                    + " vs " + loaded.version[0]);
        }
        loadedBOAState = loaded.obj.BOAState;
        return true;
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getStartFrame()
     */
    @Override
    public int getStartFrame() {
        return loadedBOAState.nest.getHandler(0).startFrame;
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setStartFrame(int)
     */
    @Override
    public void setStartFrame(int startFrame) {
        // TODO Auto-generated method stub
        super.setStartFrame(startFrame);
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getEndFrame()
     */
    @Override
    public int getEndFrame() {
        return loadedBOAState.nest.getHandler(0).endFrame;
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setEndFrame(int)
     */
    @Override
    public void setEndFrame(int endFrame) {
        // TODO Auto-generated method stub
        super.setEndFrame(endFrame);
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getImageScale()
     */
    @Override
    public double getImageScale() {
        // TODO Auto-generated method stub
        return super.getImageScale();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setImageScale(double)
     */
    @Override
    public void setImageScale(double imageScale) {
        // TODO Auto-generated method stub
        super.setImageScale(imageScale);
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getFrameInterval()
     */
    @Override
    public double getFrameInterval() {
        // TODO Auto-generated method stub
        return super.getFrameInterval();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setFrameInterval(double)
     */
    @Override
    public void setFrameInterval(double frameInterval) {
        // TODO Auto-generated method stub
        super.setFrameInterval(frameInterval);
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getNest()
     */
    @Override
    public Nest getNest() {
        if (loadedBOAState != null)
            return loadedBOAState.nest;
        else
            return super.getNest();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getBlowup()
     */
    @Override
    public int getBlowup() {
        return loadedBOAState.segParam.blowup;
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setBlowup(int)
     */
    @Override
    public void setBlowup(int blowup) {
        loadedBOAState.segParam.blowup = blowup;
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#getNodeRes()
     */
    @Override
    public double getNodeRes() {
        return loadedBOAState.segParam.getNodeRes();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.QParams#setNodeRes(int)
     */
    @Override
    public void setNodeRes(double nodeRes) {
        loadedBOAState.segParam.setNodeRes(nodeRes);
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
