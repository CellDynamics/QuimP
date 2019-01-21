package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapCoordConverter;
import com.github.celldynamics.quimp.plugin.protanalysis.Prot_Analysis.PointHashSet;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

/**
 * Action for ROI to/from transfer.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionToRoi extends ProtAnalysisAbstractAction {

  /**
   * Create action.
   * 
   * @param name name
   * @param desc tooltip
   * @param ui reference to manager
   */
  public ActionToRoi(String name, String desc, CustomStackWindow ui) {
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
    PointHashSet points = ui.getModel().selected;
    RoiManager rm = RoiManager.getRoiManager();
    rm.reset();
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    for (PointCoords p : points) {
      int tmpIndex = MapCoordConverter.findPointIndex(stMap[p.cellNo].getxMap()[p.frame],
              stMap[p.cellNo].getyMap()[p.frame], p.point.getX(), p.point.getY(), Double.MAX_VALUE);
      Roi roi = new PointRoi(tmpIndex,
              (double) p.frame / (stMap[p.cellNo].getT() - 1) * stMap[p.cellNo].getRes());
      roi.setName(ProtAnalysisOptions.roiPrefix + p.cellNo);
      rm.addRoi(roi);
    }

  }

}
