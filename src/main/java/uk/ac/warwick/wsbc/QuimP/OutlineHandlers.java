package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represent collection of OutlineHandlers.
 * 
 * This class is used as storage of OutlineHandlers (results of continuous segmentation) in
 * {@link uk.ac.warwick.wsbc.QuimP.DataContainer}.
 * 
 * @author p.baniukiewicz
 *
 */
public class OutlineHandlers implements IQuimpSerialize {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(OutlineHandlers.class.getName());
    /**
     * Contain {@link OutlineHandler} objects. 
     * 
     * Each object ({@link OutlineHandler} represents segmented cell (outline) between 
     * frame <it>f1</it> and <it>f2</it>  but only if segmentation process run continuously 
     * between these frames. Outlines are returned by of ECMM module and they are almost the same
     * as Snakes but may differ in node order or number.
     * 
     */
    public ArrayList<OutlineHandler> oHs;

    public OutlineHandlers(int size) {
        oHs = new ArrayList<>(size);
    }

    public OutlineHandlers() {
        oHs = new ArrayList<>();
    }

    /**
     * Prepare Outlines for serialization. Build arrays from Outline objects stored in
     * OutlineHandlers
     */
    @Override
    public void beforeSerialize() {
        if (oHs != null)
            for (OutlineHandler oH : oHs)
                oH.beforeSerialize();
    }

    /**
     * Rebuild every Outline from temporary array
     */
    @Override
    public void afterSerialize() throws Exception {
        if (oHs != null)
            for (OutlineHandler oH : oHs)
                oH.afterSerialize();
    }
}
