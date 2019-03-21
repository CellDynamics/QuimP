package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.filesystem.StatsCollection;

import ij.measure.ResultsTable;

/**
 * Create table with fluorescence features for selected cell and channel.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionTableFluo extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionTableFluo(String name, String desc, ProtAnalysisUi ui) {
    super(name, desc, ui);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisAbstractAction#actionPerformed(
   * java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    int h = options.selActiveCellPlot.getValue();
    StatsCollection stats =
            ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getStats();
    ResultsTable rt = new ResultsTable();
    stats.getStatCollection().get(h).addFluosToResultTable(rt, options.selActiveChannel.intValue());
    rt.show("Results");
  }
}
