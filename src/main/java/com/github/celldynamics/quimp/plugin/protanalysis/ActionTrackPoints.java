package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.MapCoordConverter;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.plugin.ZProjector;

/**
 * Generalisation of action for tracking points.
 * 
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("serial")
public class ActionTrackPoints extends ProtAnalysisAbstractAction {

  protected ImagePlus image; // image used for plotting overlay

  /**
   * Action creator.
   * 
   * <p>Read {@link ProtAnalysisOptions#guiNewImage}
   * 
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (options.guiNewImage.getValue()) { // if true = new image
      image = ui.getImagePlus().duplicate();
      Overlay overlay = image.getOverlay();
      if (overlay != null) {
        overlay.clear();
      }
    } else {
      image = ui.getImagePlus();
    }
    QconfLoader qconfLoader = ui.getModel().getQconfLoader();
    switch (options.plotStaticDynamic.shortValue()) {
      case 0:
        if (options.guiFlattenStaticTrackImage.booleanValue()) {
          image = trackStaticFlat(qconfLoader); // static flat image, return new copy of image
        } else {
          trackStatic(qconfLoader); // static but we have slices
        }
        break;
      case 1:
        trackDynamic(qconfLoader); // dynamic
        break;
      default:
        throw new IllegalArgumentException("Type of plot not supported.");
    }
    if (options.guiNewImage.getValue()) {
      image.setTitle(WindowManager.makeUniqueName(image.getTitle() + " - tracking"));
      image.show(); // show new image
    } else { // clear user selection at the end if no new image
      new ActionClearPoints(ui).clear();
    }
  }

  /**
   * Dynamic plot.
   * 
   * @param qconfLoader qconfLoader
   */
  void trackDynamic(QconfLoader qconfLoader) {
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    // wrap original image into TrackVisualisation object. All changes will modify the image
    // Note that TrackVisualisation keeps only reference and adds overlay object to it.
    TrackVisualisation.Stack visStackDynamic = new TrackVisualisation.Stack(image);
    visStackDynamic.circleRadius = options.circleRadius;

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
      if (options.guiShowPoint.booleanValue()) {
        visStackDynamic.addMaximaToImage(stMap[map], mf);
        visStackDynamic.addTrackingMaximaToImage(stMap[map], trackCollection);
      }
      // color outlines
      visStackDynamic.addOutlinesToImage(stMap[map], options);
      // tracking lines
      if (options.guiShowTrack.booleanValue()) {
        visStackDynamic.addTrackingLinesToImage(stMap[map], trackCollection);
      }

    }
  }

  /**
   * Combination of static and dynamic.
   * 
   * @param qconfLoader qconfLoader
   */
  void trackStatic(QconfLoader qconfLoader) {
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
      if (options.guiShowPoint.booleanValue()) {
        visStackStatic.addTrackingMaximaToImage(stMap[map], trackCollection);
      }
      // color outlines
      visStackStatic.addOutlinesToImage(stMap[map], options);
      // tracking lines
      TrackVisualisation.Image vis = new TrackVisualisation.Image(image);
      if (options.guiShowTrack.booleanValue()) {
        vis.addElementsToImage(stMap[map], trackCollection, mf);
      }

    }
  }

  /**
   * Fully static.
   * 
   * @param qconfLoader qconfLoader
   * @return flattened image
   */
  ImagePlus trackStaticFlat(QconfLoader qconfLoader) {
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    // wrap original image into TrackVisualisation object. All changes will modify the image
    // Note that TrackVisualisation keeps only reference and adds overlay object to it.
    TrackVisualisation.Image visStackStatic = new TrackVisualisation.Image(image);
    visStackStatic.circleRadius = options.circleRadius;
    visStackStatic.flatten(ZProjector.MAX_METHOD, false);

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
      // tracking lines
      MaximaFinder mfTmp = null;
      TrackCollection tcTmp = null;
      if (options.guiShowTrack.booleanValue()) {
        tcTmp = trackCollection;
      }
      if (options.guiShowPoint.booleanValue()) {
        mfTmp = mf;
      }
      if (options.guiShowTrack.booleanValue()) {
        visStackStatic.addElementsToImage(stMap[map], tcTmp, mfTmp);
      }

    }
    return visStackStatic.getOriginalImage();
  }

}
