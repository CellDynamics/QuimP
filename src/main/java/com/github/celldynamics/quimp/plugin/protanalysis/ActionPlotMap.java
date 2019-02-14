package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ByteProcessor;

/**
 * Plot selected map.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionPlotMap extends ProtAnalysisAbstractAction {

  private String map;

  /**
   * Action for plotting maps.
   * 
   * <p>Plot new map under new name each time. Read {@link ProtAnalysisOptions#activeCellMap}
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class
   * @param map map code to plot: MOT, CONV, FLU
   */
  public ActionPlotMap(String name, String desc, CustomStackWindow ui, String map) {
    super(name, desc, ui);
    this.map = map;
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
    int h = options.activeCellMap.getValue();
    STmap[] stMap =
            ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer().getQState();
    STmap mapCell = stMap[h];
    OutlinesCollection ohs =
            ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer().getEcmmState();
    switch (map) {
      case "MOT": {
        ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("motility_map"),
                "rwb", mapCell.getMotMap(), ohs.oHs.get(h).migLimits[0],
                ohs.oHs.get(h).migLimits[1]);
        mm.setTitle(WindowManager.makeUniqueName("MotilityMap_cell_" + h));
        mm.show();
        break;
      }
      case "CONV": {
        ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("convexity_map"),
                "rbb", mapCell.getConvMap(), ohs.oHs.get(h).curvLimits[0],
                ohs.oHs.get(h).curvLimits[1]);
        mm.setTitle(WindowManager.makeUniqueName("ConvexityMap_cell_" + h));
        mm.show();
        break;
      }
      case "FLU":
        for (int i = 0; i < 3; i++) {
          if (!mapCell.getFluMaps()[i].isEnabled()) {
            continue;
          }
          ImagePlus fluImP = mapCell.map2ImagePlus(
                  WindowManager.makeUniqueName(
                          "fluo_map_cell_" + h + "_fluoCH" + mapCell.getFluMaps()[i].getChannel()),
                  new ByteProcessor(mapCell.getRes(), mapCell.getT(),
                          mapCell.getFluMaps()[i].getColours()));
          fluImP.show();
          IJ.doCommand("Red");
        }
        break;
      default:
        throw new RuntimeException("Wrong map code!");
    }

  }

}
