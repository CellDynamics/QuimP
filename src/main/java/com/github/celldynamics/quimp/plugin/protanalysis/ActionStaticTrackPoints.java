package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapCoordConverter;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

/**
 * Action for track button.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
public class ActionStaticTrackPoints extends AbstractActionTrackPoints {
  /**
   * Action creator.
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionStaticTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  @Override
  void track(QconfLoader qconfLoader) {
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    // wrap original image into TrackVisualisation object. All changes will modify the image
    // Note that TrackVisualisation keeps only reference and adds overlay object to it.
    TrackVisualisation.Stack visStackStatic = new TrackVisualisation.Stack(image);
    visStackStatic.circleRadius = options.circleRadius;

    // order data by cell numbers. For each key (cell number) collect all users points
    HashMap<Integer, List<Point2D>> tmpSelected = new HashMap<>();
    for (PointCoords p : ui.getModel().selected) {
      int tmpIndex = MapCoordConverter.findPointIndex(stMap[p.cellNo].getxMap()[p.frame],
              stMap[p.cellNo].getyMap()[p.frame], p.point.getX(), p.point.getY(), Double.MAX_VALUE);
      if (tmpIndex >= 0) {
        // if no cell in HashMap - create
        if (tmpSelected.get(p.cellNo) == null) {
          tmpSelected.put(p.cellNo, new ArrayList<Point2D>());
        }
        // add point to the cell
        tmpSelected.get(p.cellNo).add(new Point2D.Double(p.frame, tmpIndex));
      }
    }
    logger.trace("Added " + tmpSelected.size() + " points");
    // plot - iterate over cells (keys) and plot all points
    for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
      Integer map = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points
      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);

      TrackMapAnalyser pt = new TrackMapAnalyser();
      pt.trackMaxima(stMap[map], -1.0, mf); // TODO Add as parameter
      TrackCollection trackCollection = pt.getTrackCollection();
      visStackStatic.addTrackingMaximaToImage(stMap[map], trackCollection);

      // TODO Config option to select outline type
      visStackStatic.addOutlinesToImage(stMap[map], options);

      // TODO optional plot
      TrackVisualisation.Image vis = new TrackVisualisation.Image(image);
      vis.addElementsToImage(stMap[map], trackCollection, mf);

    }
  }

}
