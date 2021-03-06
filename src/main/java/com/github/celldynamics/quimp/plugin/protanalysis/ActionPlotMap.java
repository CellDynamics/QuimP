package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
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
  public ActionPlotMap(String name, String desc, ProtAnalysisUi ui, String map) {
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
          mm = getUnscaledMap(mapCell, map, 0, uname);
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
          mm = getUnscaledMap(mapCell, map, 0, uname);
        } else {
          if (ohs.oHs.get(h).curvLimits[0] == ohs.oHs.get(h).curvLimits[1]) {
            if (ui.getModel().getSink() == MessageSinkTypes.GUI
                    || ui.getModel().getSink() == MessageSinkTypes.IJERROR) {
              IJ.log("Min and Max limits are equal. Try to re-run Q-Analysis!");
            } else {
              logger.error("Min and Max limits are equal. Try to re-run Q-Analysis!");
            }
          }
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
            mm = getUnscaledMap(mapCell, map, i, uname);
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
   * @param mapCell reference to STmap (holds resolution)
   * @param map map code to plot: "MOT", "CONV", "FLU"
   * @param i index of FLU map, ignored for "MOT" and "CONV"
   * @param uname name of the window with map
   * @return ImagePlus with unscaled image
   */
  static ImagePlus getUnscaledMap(STmap mapCell, String map, int i, String uname) {
    double[][] mapArray = null;
    switch (map) {
      case "MOT":
        mapArray = mapCell.getMotMap();
        break;
      case "CONV":
        mapArray = mapCell.getConvMap();
        break;
      case "FLU":
        mapArray = mapCell.getFluMaps()[i].getMap();
        break;
      default:
        throw new RuntimeException("Wrong map code!");
    }
    ImageProcessor imp = new FloatProcessor(QuimPArrayUtils.double2dfloat(mapArray)).rotateRight();
    imp.flipHorizontal();
    ImagePlus mm = mapCell.map2ImagePlus(uname, imp);
    return mm;
  }
}
