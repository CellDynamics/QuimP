package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.CellStats;
import uk.ac.warwick.wsbc.QuimP.CellStatsEval;

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
    static final Logger LOGGER = LoggerFactory.getLogger(StatsCollection.class.getName());

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

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {
    }

    /**
     * Bridge method to maintain compatibility with old code. Copies statistics from CellStat
     * objects into internal fields of StatsHandler.
     * 
     * @param in List of CellStat objects - size of this list equals to number of cells.
     */
    public void copyFromCellStat(List<CellStatsEval> in) {
        for (CellStatsEval cl : in) {
            sHs.add(cl.getStatH());
        }
    }

}
