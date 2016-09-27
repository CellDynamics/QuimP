package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.CellStatsEval;
import uk.ac.warwick.wsbc.QuimP.CellStats;
import uk.ac.warwick.wsbc.QuimP.IQuimpSerialize;

/**
 * Keep statistics for cells.
 * 
 * This class is used as storage of frame statistics in
 * {@link uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer}.
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsCollection implements IQuimpSerialize {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(StatsCollection.class.getName());

    /**
     * List of statistic objects for separate cells.
     */
    public ArrayList<CellStats> sHs;

    /**
     * 
     * @param size Number of cells
     */
    public StatsCollection(int size) {
        sHs = new ArrayList<>(size);
    }

    public StatsCollection() {
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
    public void copyFromCellStat(List<CellStatsEval> in) {
        for (CellStatsEval cl : in) {
            sHs.add(cl.getStatH());
        }
    }

}
