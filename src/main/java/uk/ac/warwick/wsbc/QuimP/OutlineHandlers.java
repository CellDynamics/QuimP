/**
 * @file OutlineHandlers.java
 * @date 27 May 2016
 */

package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represent collection of OutlineHandlers
 * 
 * This class is used as storage of OutlineHandlers (results of continuous segmentation) in
 * uk.ac.warwick.wsbc.QuimP.DataContainer
 * 
 * @author p.baniukiewicz
 * @date 27 May 2016
 *
 */
public class OutlineHandlers implements IQuimpSerialize {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(OutlineHandlers.class.getName());
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
