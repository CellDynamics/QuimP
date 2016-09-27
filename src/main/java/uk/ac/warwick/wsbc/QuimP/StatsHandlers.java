package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Keep statistics for cells.
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsHandlers implements IQuimpSerialize {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(StatsHandlers.class.getName());

    /**
     * List of statistic objects for separate cells.
     */
    public ArrayList<StatsHandler> sHs;

    /**
     * 
     * @param size Number of cells
     */
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

    /**
     * Bridge method to maintain compatibility with old code.
     * Copies statistics from CellStat objects into internal fields of StatsHandler.
     * @param in List of CellStat objects - size of this list equals to number of cells.
     */
    public void copyFromCellStat(List<CellStat> in) {
        for (CellStat cl : in) {
            sHs.add(cl.getStatH());
        }
    }

}
