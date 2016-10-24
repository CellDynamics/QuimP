package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.OutlineHandler;

/**
 * Represent collection of OutlineHandlers for cells.
 * 
 * This class is used as storage of OutlineHandlers (results of continuous segmentation) in
 * {@link uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer}.
 * 
 * @author p.baniukiewicz
 *
 */
public class OutlinesCollection implements IQuimpSerialize {
    @SuppressWarnings("unused")
    static final Logger LOGGER = LoggerFactory.getLogger(OutlinesCollection.class.getName());
    /**
     * Contain {@link OutlineHandler} objects.
     * 
     * Each object ({@link OutlineHandler} represents segmented cell (outline) between frame
     * <it>f1</it> and <it>f2</it> but only if segmentation process run continuously between these
     * frames. Outlines are returned by of ECMM module and they are almost the same as Snakes but
     * may differ in node order or number.
     * 
     */
    public ArrayList<OutlineHandler> oHs;

    public OutlinesCollection(int size) {
        oHs = new ArrayList<>(size);
    }

    public OutlinesCollection() {
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
