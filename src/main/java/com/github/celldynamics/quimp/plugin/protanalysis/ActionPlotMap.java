package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

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
   * <p>Plot new map under new name each time. Read {@link ProtAnalysisOptions#selActiveCellMap}
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
    int modifiers = e.getModifiers();
    int h = options.selActiveCellMap.getValue();
    STmap[] stMap =
            ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer().getQState();
    STmap mapCell = stMap[h];
    OutlinesCollection ohs =
            ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer().getEcmmState();
    ImagePlus mm;
    switch (map) {
      case "MOT": {
        String uname = WindowManager.makeUniqueName("motility_map");
        if ((modifiers & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
          mm = getUnscaledMap(mapCell, mapCell.getMotMap(), uname);
        } else {
          mm = mapCell.map2ColorImagePlus(uname, "rwb", mapCell.getMotMap(),
                  ohs.oHs.get(h).migLimits[0], ohs.oHs.get(h).migLimits[1]);
        }
        mm.setTitle(WindowManager.makeUniqueName("MotilityMap_cell_" + h));
        mm.show();
        break;
      }
      case "CONV": {
        String uname = WindowManager.makeUniqueName("convexity_map");
        if ((modifiers & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
          mm = getUnscaledMap(mapCell, mapCell.getConvMap(), uname);
        } else {
          mm = mapCell.map2ColorImagePlus(uname, "rbb", mapCell.getConvMap(),
                  ohs.oHs.get(h).curvLimits[0], ohs.oHs.get(h).curvLimits[1]);
        }
        mm.setTitle(WindowManager.makeUniqueName("ConvexityMap_cell_" + h));
        mm.show();
        break;
      }
      case "FLU":
        for (int i = 0; i < 3; i++) {
          if (!mapCell.getFluMaps()[i].isEnabled()) {
            continue;
          }
          String uname = WindowManager.makeUniqueName(
                  "fluo_map_cell_" + h + "_fluoCH" + mapCell.getFluMaps()[i].getChannel());
          if ((modifiers & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
            mm = getUnscaledMap(mapCell, mapCell.getFluMaps()[i].getMap(), uname);
            mm.show();
          } else {
            mm = mapCell.map2ImagePlus(uname, new ByteProcessor(mapCell.getRes(), mapCell.getT(),
                    mapCell.getFluMaps()[i].getColours()));
            mm.show();
            IJ.doCommand("Red");
          }
        }
        break;
      default:
        throw new RuntimeException("Wrong map code!");
    }

  }

  /**
   * Produce image from map.
   * 
   * <p>Map is unscaled in contrary to
   * {@link STmap#map2ColorImagePlus(String, String, double[][], double, double)}
   * 
   * @param mapCell reference to STmap
   * @param map map to plot
   * @param uname name
   * @return ImagePlus with unscaled image
   */
  ImagePlus getUnscaledMap(STmap mapCell, double[][] map, String uname) {
    ImageProcessor imp =
            new FloatProcessor(QuimPArrayUtils.double2dfloat(mapCell.getMotMap())).rotateRight();
    imp.flipHorizontal();
    ImagePlus mm = mapCell.map2ImagePlus(uname, imp);
    return mm;
  }
}
