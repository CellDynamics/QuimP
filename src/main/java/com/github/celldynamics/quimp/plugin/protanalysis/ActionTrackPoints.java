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
 * <p>Read:
 * <ol>
 * <li>{@link ProtAnalysisOptions#chbNewImage}
 * <li>{@link ProtAnalysisOptions#plotStaticDynamic}
 * <li>{@link ProtAnalysisOptions#chbFlattenStaticTrackImage}
 * <li>{@link ProtAnalysisOptions#chbShowTrackMotility}
 * <li>{@link ProtAnalysisOptions#chbShowPoint}
 * <li>{@link ProtAnalysisOptions#chbShowTrack}
 * <li>{@link ProtAnalysisOptions#circleRadius}
 * </ol>
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
   * @param name name
   * @param desc description
   * @param ui reference to outer class.
   */
  public ActionTrackPoints(String name, String desc, CustomStackWindow ui) {
    super(name, desc, ui);
  }

  /**
   * Initialise this action as ActionListener without properties.
   * 
   * @param ui reference to outer class.
   */
  public ActionTrackPoints(CustomStackWindow ui) {
    super(ui);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (options.chbNewImage.getValue()) { // if true = new image
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
        if (options.chbFlattenStaticTrackImage.booleanValue()) {
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
    if (options.chbShowTrackMotility.booleanValue()) {
      plotOnMap(qconfLoader);
    }
    if (options.chbNewImage.getValue()) {
      image.setTitle(WindowManager.makeUniqueName(image.getTitle() + " - tracking"));
      image.show(); // show new image
    } else { // clear user selection at the end if no new image
      new ActionClearPoints(ui).clear();
    }
  }

  /**
   * Plot tracking lines on map.
   *
   * @param qconfLoader qconfLoader
   * @see ProtAnalysisOptions#chbShowTrackMotility
   */
  void plotOnMap(QconfLoader qconfLoader) {
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    HashMap<Integer, List<Point2D>> tmpSelected = extractPoints(stMap);
    // plot - iterate over cells (keys) and plot all points
    for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
      Integer cellNo = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points

      // time - y
      // outline - x
      ImagePlus mm = ActionPlotMap.getUnscaledMap(stMap[cellNo], stMap[cellNo].getMotMap(),
              WindowManager.makeUniqueName("motility_map_cell_" + cellNo));

      // This is for unscaled map
      // TrackVisualisation.Map visSingle = new TrackVisualisation.Map(
      // WindowManager.makeUniqueName("motility_map_cell_" + cellNo),
      // QuimPArrayUtils.double2dfloat(stMap[cellNo].getMotMap()));
      TrackVisualisation.Map visSingle = new TrackVisualisation.Map(mm, true,
              (double) stMap[cellNo].getRes() / stMap[cellNo].getT(), 1.0);

      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);
      TrackCollection trackCollection = getTracks(stMap, cellNo, mf);
      visSingle.addMaximaToImage(mf);
      visSingle.addTrackingLinesToImage(trackCollection);
      visSingle.getOriginalImage().show();
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

    HashMap<Integer, List<Point2D>> tmpSelected = extractPoints(stMap);
    // plot - iterate over cells (keys) and plot all points
    for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
      Integer cellNo = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points
      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);

      TrackCollection trackCollection = getTracks(stMap, cellNo, mf);
      if (options.chbShowPoint.booleanValue()) {
        visStackDynamic.addMaximaToImage(stMap[cellNo], mf);
        visStackDynamic.addTrackingMaximaToImage(stMap[cellNo], trackCollection);
      }
      // tracking lines
      if (options.chbShowTrack.booleanValue()) {
        visStackDynamic.addTrackingLinesToImage(stMap[cellNo], trackCollection);
      }

    }
    addOutline(stMap, visStackDynamic);
  }

  /**
   * Order data by cell numbers. For each key (cell number) collect all users points.
   * 
   * @param stMap maps
   * @return Map(CellNo, ListOfPoints for this cellNo). Point has coordinate of Map [frame,index]
   */
  HashMap<Integer, List<Point2D>> extractPoints(STmap[] stMap) {
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
    return tmpSelected;
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

    HashMap<Integer, List<Point2D>> tmpSelected = extractPoints(stMap);
    // plot - iterate over cells (keys) and plot all points
    for (Map.Entry<Integer, List<Point2D>> entry : tmpSelected.entrySet()) {
      Integer cellNo = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points
      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);

      TrackCollection trackCollection = getTracks(stMap, cellNo, mf);
      if (options.chbShowPoint.booleanValue()) {
        visStackStatic.addTrackingMaximaToImage(stMap[cellNo], trackCollection);
      }

      // tracking lines
      TrackVisualisation.Image vis = new TrackVisualisation.Image(image);
      if (options.chbShowTrack.booleanValue()) {
        vis.addElementsToImage(stMap[cellNo], trackCollection, mf);
      }
    }
    // add outline always even if no points
    addOutline(stMap, visStackStatic);
  }

  /**
   * Get tracks.
   * 
   * @param stMap stMap
   * @param cellNo cellNo
   * @param mf mf
   * @return trackCollection
   */
  protected TrackCollection getTracks(STmap[] stMap, Integer cellNo, MaximaFinder mf) {
    TrackMapAnalyser pt = new TrackMapAnalyser();
    pt.trackMaxima(stMap[cellNo], -1.0, mf); // TODO Add as parameter
    TrackCollection trackCollection = pt.getTrackCollection();
    return trackCollection;
  }

  /**
   * Plot outlines.
   * 
   * @param stMap stMap
   * @param visStackStatic visStackStatic
   */
  private void addOutline(STmap[] stMap, TrackVisualisation.Stack visStackStatic) {
    for (STmap mapCell : stMap) {
      // color outlines
      visStackStatic.addOutlinesToImage(mapCell, options);
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
      Integer cellNo = entry.getKey(); // cell number
      List<Point2D> points = entry.getValue(); // users points
      MaximaFinder mf = new MaximaFinder(ui.getImagePlus().getProcessor());
      mf.setMaxima(points);

      TrackCollection trackCollection = getTracks(stMap, cellNo, mf);
      // tracking lines
      MaximaFinder mfTmp = null;
      TrackCollection tcTmp = null;
      if (options.chbShowTrack.booleanValue()) {
        tcTmp = trackCollection;
      }
      if (options.chbShowPoint.booleanValue()) {
        mfTmp = mf;
      }
      if (options.chbShowTrack.booleanValue()) {
        visStackStatic.addElementsToImage(stMap[cellNo], tcTmp, mfTmp);
      }

    }
    return visStackStatic.getOriginalImage();
  }

}
