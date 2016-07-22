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
 * @author p.baniukiewicz
 * @date 27 May 2016
 *
 */
public class OutlineHandlers implements IQuimpSerialize {
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

    @Override
    public void afterSerialize() throws Exception {
        LOGGER.error("Not implemented");
    }
}
