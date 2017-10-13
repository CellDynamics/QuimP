package com.github.celldynamics.quimp.filesystem;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.SnakePluginList;
import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.plugin.engine.PluginFactory;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

/**
 * Integrate outputs from every module of QuimP.
 * 
 * <p>All modules can add here their configuration sets. This structure is used for exchanging data
 * between modules. It can be dynamically modified.
 * 
 * @author p.baniukiewicz
 * @see FormatConverter
 * @see QParams
 * @see QParamsQconf
 */
public class DataContainer implements IQuimpSerialize {

  /**
   * Indicate that BOAState module is not null.
   */
  public static final int BOA_RUN = 1024;
  /**
   * Indicate that ECMM module is not null (it has been run already on those data).
   */
  public static final int ECMM_RUN = 2048;
  /**
   * Indicate that ANA module is not null (it has been run already on those data).
   */
  public static final int ANA_RUN = 4096;
  /**
   * Indicate that Q module is not null (it has been run already on those data).
   */
  public static final int Q_RUN = 8192;
  /**
   * Indicate that QCONF contains statistics.
   */
  public static final int STATS_AVAIL = 16384;

  /**
   * Object to store all BOA state. Can be <tt>null</tt> when module has not been run yet.
   */
  public BOAState BOAState;
  /**
   * Object to store ECCM output. Can be <tt>null</tt> when module has not been run yet.
   */
  public OutlinesCollection ECMMState;
  /**
   * Hold ANA state.
   */
  public ANAParamCollection ANAState;
  /**
   * Store maps generated by Q Analysis plugin for every OutlineHandler from ECMMState. Can be
   * <tt>null</tt> when module has not been run yet.
   * 
   * <p>The order of STmap object in this array is correlated with the order of OutlineHandlers in
   * <tt>ECMMState</tt>, thus <i>n-th</i> STmap has been calculated from <i>n-th</i> Outline by Q
   * Analysis module.
   */
  public STmap[] QState;
  /**
   * Store statistics for cells computed for outlines and fluorescence data.
   */
  public StatsCollection Stats;

  private transient PluginFactory pf;
  private transient ViewUpdater vu;

  /**
   * Default constructor. Create empty data containers.
   */
  public DataContainer() {
    BOAState = null;
    ECMMState = null;
    ANAState = null;
    QState = null;
  }

  /**
   * Create DataContainer with attached {@link PluginFactory PluginFactory} and {@link ViewUpdater
   * ViewUpdater}.
   * 
   * @param pf {@link PluginFactory PluginFactory} object reference
   * @param vu {@link ViewUpdater ViewUpdater} object reference
   */
  public DataContainer(final PluginFactory pf, final ViewUpdater vu) {
    this();
    this.pf = pf;
    this.vu = vu;
  }

  /**
   * getBOAState.
   * 
   * @return the bOAState
   */
  public BOAState getBOAState() {
    return BOAState;
  }

  /**
   * getECMMState.
   * 
   * @return the eCMMState
   */
  public OutlinesCollection getEcmmState() {
    return ECMMState;
  }

  /**
   * getQState.
   * 
   * @return the qState
   */
  public STmap[] getQState() {
    return QState;
  }

  /**
   * getANAState.
   * 
   * @return the aNAState
   */
  public ANAParamCollection getANAState() {
    return ANAState;
  }

  /**
   * getStats.
   * 
   * @return the stats
   */
  public StatsCollection getStats() {
    return Stats;
  }

  /**
   * Get information about available modules in object.
   * 
   * @return Flags according to loaded modules.
   * @see com.github.celldynamics.quimp.filesystem.QconfLoader
   */
  public int validateDataContainer() {
    int ret = QconfLoader.QCONF_INVALID;
    // check for all modules
    if (getBOAState() != null) {
      ret += DataContainer.BOA_RUN;
    }
    if (getEcmmState() != null) {
      ret += DataContainer.ECMM_RUN;
    }
    if (getANAState() != null) {
      ret += DataContainer.ANA_RUN;
    }
    if (getQState() != null) {
      if (getQState().length > 0) {
        int count = 0;
        for (STmap tmp : getQState()) {
          if (tmp != null && tmp.getT() != 0) {
            count++;
          }
        }
        if (count == getQState().length) { // qstate can be !null but contain invalid
          // data, e.g fluoro[3] array with initial default vales but empty. This is e,g, after
          // paQP-QCONF conversion. So check resolution T (array size)
          ret += DataContainer.Q_RUN;
        }
      }
    }
    if (getStats() != null) {
      ret += DataContainer.STATS_AVAIL;
    }
    return ret;
  }

  /**
   * Called before serialization.
   * 
   * <p>Call similar method for all stored object allowing them for self-preparation for normal
   * operations after loading
   */
  @Override
  public void beforeSerialize() {
    if (BOAState != null) {
      BOAState.beforeSerialize(); // serialize first stored data
    }
    if (ECMMState != null) {
      ECMMState.beforeSerialize(); // serialize second stored data
    }
    if (ANAState != null) {
      ANAState.beforeSerialize();
    }
    if (QState != null) {
      for (STmap stM : QState) {
        if (stM != null) {
          stM.beforeSerialize();
        }
      }
    }
    if (Stats != null) {
      Stats.beforeSerialize();
    }

  }

  /**
   * Called after serialization.
   * 
   * <p>Call similar method for all stored object allowing them for self-preparation for saving
   */
  @Override
  public void afterSerialize() throws Exception {
    if (BOAState != null) {
      BOAState.snakePluginList = new SnakePluginList(BOA_.NUM_SNAKE_PLUGINS, pf, vu);
      BOAState.afterSerialize();
      for (SnakePluginList sl : BOAState.snakePluginListSnapshots) {
        sl.updateRefs(pf, vu);
      }
    }
    if (ECMMState != null) {
      ECMMState.afterSerialize();
    }
    if (ANAState != null) {
      ANAState.afterSerialize();
    }
    if (QState != null) {
      for (STmap stM : QState) {
        if (stM != null) {
          stM.afterSerialize();
        }
      }
    }
    if (Stats != null) {
      Stats.afterSerialize();
    }
  }
}
