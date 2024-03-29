package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
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
public class ActionFromRoi extends ProtAnalysisAbstractAction {

  /**
   * Create action.
   * 
   * @param name name
   * @param desc tooltip
   * @param ui reference to gui manager
   */
  public ActionFromRoi(String name, String desc, ProtAnalysisUi ui) {
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
    int modifiers = e.getModifiers();
    PointHashSet points = ui.getModel().selected;
    points.clear(); // Here we clear points!
    RoiManager rm = RoiManager.getRoiManager();
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    List<Roi> rois = Arrays.asList(rm.getRoisAsArray());
    int cellNo = 0;

    if ((modifiers & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
      cellNo = options.selActiveCellMap.getValue();
      logger.debug("CTRL detected. Import all to current cell: " + cellNo);
    }
    for (Roi roi : rois) { // over ROIs in RoiManager
      if (!(roi instanceof PointRoi)) {
        logger.warn("ROI " + roi.getName() + " is not PointRoi");
      }
      // if NO ctrl get cell number from each ROI
      if ((modifiers & ActionEvent.CTRL_MASK) != ActionEvent.CTRL_MASK) {
        if (roi.getName().startsWith(ProtAnalysisOptions.roiPrefix)) { // our roi
          cellNo = stripCellNo(roi.getName());
          if (cellNo > stMap.length) {
            continue;
          }
          if (cellNo < 0) {
            logger.warn(
                    "Can not obtain cell index from ROI name: " + roi.getName() + " Assume cell 0");
            cellNo = 0;
          }
        }
      }
      // now each roi can contain some points (e.g. two point form one ROI object in ROI Manager)
      for (Point p : roi) {
        int tmpIndex = (int) p.getX();
        int frame = (int) Math.round(
                p.getY() * (stMap[cellNo].getT() - 1) / stMap[cellNo].getVerticalResolution());
        // get screen coordinates (ints)
        // FIXME Why p.getY is larger than 400?? This is on tiffs as well.
        // see com.github.celldynamics.quimp.plugin.qanalysis.STmap.resize(ImagePlus),
        // (int) Math.ceil((double) res / (double) T)*T = vertRes
        try {
          int x = (int) Math.round(stMap[cellNo].getxMap()[frame][tmpIndex]);
          int y = (int) Math.round(stMap[cellNo].getyMap()[frame][tmpIndex]);
          logger.trace("name: " + roi.getName() + " point: " + p + " tmpIndex: " + tmpIndex
                  + " frame: " + frame + " cell: " + cellNo + " x: " + x + " y: " + y);
          PointCoords point = new PointCoords(new Point(x, y), cellNo, frame);
          points.addRaw(point); // add without overwriting frame number
        } catch (ArrayIndexOutOfBoundsException ex) {
          logger.error(
                  "Point " + p + " can not be mapped to array (ArrayIndexOutOfBoundsException)");
        }
      }

    }
    updateCurrentView();
  }

  /**
   * Extract cell number from ROI name.
   * 
   * @param name roi name in format set in {@link ActionToRoi}
   * @return only cell number without additional roi number added by Fiji or -1 if number could not
   *         be retrieved.
   */
  int stripCellNo(String name) {
    // contain index and optional roi subnumber -0
    String tmp = name.substring(ProtAnalysisOptions.roiPrefix.length());
    Pattern p = Pattern.compile("^[\\d]*");
    Matcher m = p.matcher(tmp);
    if (m.find()) {
      return Integer.parseInt(m.group(0));
    }
    return -1;
  }

}
