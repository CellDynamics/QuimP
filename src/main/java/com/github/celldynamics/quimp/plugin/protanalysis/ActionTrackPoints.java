package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.apache.commons.lang3.tuple.Pair;
import org.scijava.vecmath.Point3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapCoordConverter;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.WindowManager;

/**
 * Action for track button.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public class ActionTrackPoints extends ProtAnalysisAbstractAction implements Action {
  static final Logger LOGGER = LoggerFactory.getLogger(ActionTrackPoints.class.getName());

  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    track(qconfLoader);
  }

  void track(QconfLoader qconfLoader) {
    // TODO Finish
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    TrackVisualisation.Image visStackStatic =
            new TrackVisualisation.Image(ui.getImagePlus().duplicate()); // FIXME no duplicate
    visStackStatic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Static tracking"));

    // order data by cell numbers. For each key (cell number) collect all users points
    HashMap<Integer, List<Point2D>> tmpSelected = new HashMap<>();
    for (Pair<Point3i, Integer> p : ui.getModel().selected) {
      int tmpIndex =
              MapCoordConverter.findPointIndex(stMap[p.getRight()].getxMap()[p.getLeft().getZ()],
                      stMap[p.getRight()].getyMap()[p.getLeft().getZ()], p.getLeft().getX(),
                      p.getLeft().getY(), Double.MAX_VALUE);
      if (tmpIndex >= 0) {
        // if no cell in HashMap - create
        if (tmpSelected.get(p.getRight()) == null) {
          tmpSelected.put(p.getRight(), new ArrayList<Point2D>());
        }
        // add point to the cell
        tmpSelected.get(p.getRight()).add(new Point2D.Double(0, tmpIndex));
      }
    }
    LOGGER.trace("Added " + tmpSelected.size() + " points");
    // plot - iterate over cells (keys) and plot all points
    for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
      Integer map = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points
      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);
      visStackStatic.addElementsToImage(stMap[map], null, mf);

    }
    visStackStatic.getOriginalImage().show();
  }

}
