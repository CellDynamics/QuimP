package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author p.baniukiewicz
 *
 */
public class StatsHandlers implements IQuimpSerialize {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(StatsHandlers.class.getName());

    public ArrayList<StatsHandler> sHs;

    public StatsHandlers(int size) {
        sHs = new ArrayList<>(size);
    }

    public StatsHandlers() {
        sHs = new ArrayList<>();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }

}
