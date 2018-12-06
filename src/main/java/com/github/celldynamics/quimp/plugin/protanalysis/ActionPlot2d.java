package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.github.celldynamics.quimp.FrameStatistics;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.filesystem.StatsCollection;

import ij.gui.Plot;
import ij.measure.ResultsTable;

/**
 * Plot selected 2d data.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionPlot2d extends ProtAnalysisAbstractAction {

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionPlot2d(String name, String desc, CustomStackWindow ui) {
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
    plot(qconfLoader);
  }

  void plot(QconfLoader qconfLoader) {
    StatsCollection stats =
            ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getStats();

    ResultsTable rt = ResultsTable.getResultsTable();
    stats.getStatCollection().get(0).addFluosToResultTable(rt, 0);
    rt.show("Results");

    ResultsTable rt1 = new ResultsTable();
    stats.getStatCollection().get(0).addStatsToResultTable(rt1);
    rt1.show("Results1");

    Plot plot = new Plot("Displacement", "x", "y");
    ArrayList<Double> ar = new ArrayList<>();
    ArrayList<Double> f = new ArrayList<>();
    // iterate overl all cells
    for (FrameStatistics fs : stats.getStatCollection().get(0).getFramestat()) {
      ar.add(fs.displacement);
      f.add((double) fs.frame);
    }
    plot.addPoints(f, ar, Plot.CONNECTED_CIRCLES);
    plot.show();

  }

}
