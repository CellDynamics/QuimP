package uk.ac.warwick.wsbc.quimp.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.CellStats;
import uk.ac.warwick.wsbc.quimp.CellStatsEval;

// TODO: Auto-generated Javadoc
/**
 * Keep statistics for cells.
 * 
 * This class is used as storage of frame statistics in
 * {@link uk.ac.warwick.wsbc.quimp.filesystem.DataContainer}.
 * 
 * @author p.baniukiewicz
 *
 */
public class StatsCollection implements IQuimpSerialize {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(StatsCollection.class.getName());

    /**
     * List of statistic objects for separate cells.
     */
    public ArrayList<CellStats> sHs;

    /**
     * Create <tt>size</tt> elements in store for {@link CellStats} objects.
     * 
     * Size of the store usually equals to the number of cells in the image.
     * 
     * @param size Number of cells
     */
    public StatsCollection(int size) {
        sHs = new ArrayList<>(size);
    }

    /**
     * Default constructor.
     * 
     * Create empty store for {@link CellStats} objects.
     */
    public StatsCollection() {
        sHs = new ArrayList<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.IQuimpSerialize#afterSerialize()
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

    /**
     * @return the sHs
     */
    public ArrayList<CellStats> getStatCollection() {
        return sHs;
    }

}
