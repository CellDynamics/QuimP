package com.github.celldynamics.quimp.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.CellStats;
import com.github.celldynamics.quimp.CellStatsEval;

/**
 * Keep statistics for cells.
 * 
 * <p>This class is used as storage of frame statistics in
 * {@link com.github.celldynamics.quimp.filesystem.DataContainer}.
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
   * <p>Size of the store usually equals to the number of cells in the image.
   * 
   * @param size Number of cells
   */
  public StatsCollection(int size) {
    sHs = new ArrayList<>(size);
  }

  /**
   * Default constructor.
   * 
   * <p>Create empty store for {@link CellStats} objects.
   */
  public StatsCollection() {
    sHs = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
  }

  /**
   * Bridge method to maintain compatibility with old code. Copies statistics from CellStat
   * objects into internal fields of StatsHandler.
   * 
   * @param in List of CellStat objects - size of this list equals to number of cells. Each
   *        {@link CellStats} object maintain statistic for one cell along all frames.
   */
  public void copyFromCellStat(List<CellStatsEval> in) {
    in.forEach(cl -> sHs.add(cl.getStatH()));
  }

  /**
   * Get stats collection object wrapped by this class.
   * 
   * @return the sHs
   */
  public ArrayList<CellStats> getStatCollection() {
    return sHs;
  }

  /**
   * Set stats collection object wrapped by this class.
   * 
   * @param shs the sHs to set
   */
  public void setStatCollection(ArrayList<CellStats> shs) {
    this.sHs = shs;
  }

}
