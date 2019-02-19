package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.plugin.AbstractPluginQconf;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/*
 * !>
 * @startuml
 * salt
 * {
 *  {^"Visual tracking"
 *    {+
 *    Text field with help
 *    ...
 *    // Select points with   // 
 *    // CTRL key//
 *    }
 *    **Selected:**             4
 *    {
 *    [to ROI] | [from ROI]
 *    }
 *    [Clear all points]
 *    { (X) Static | () Dynamic}
 *    [X] Show tracked point
 *    [X] Smooth tracks
 *    ^Outline color  ^
 *    [ ] Open in new image
 *    [Track           ]
 *    [Clear Overlay   ] 
 *  }
 *  {^"Maps"
 *  ^Select cell     ^
 *    { [Mot ] | [Convex] | [Fluo] }
 *  }
 *  {^"Tables and plots"
 *    [X] Plot selected
 *    ^Select cell     ^
 *    { (X) Ch1 | ( ) Ch2 | ( ) Ch3}
 *    ...
 *    {
 *    [X] X-Centr | [ ] Y-Centr
 *    [ ] Displ | [ ] Distance 
 *    [ ] Direct | [ ] Speed
 *    [ ] Perim | [ ] Elong
 *    [ ] Circ | [ ] Area
 *    ==== | ===
 *    [ ] Total fl | [ ] Mean fl
 *    [ ] Cortex wd | [ ] Cyto ar
 *    [ ] Total ctf | [ ] Mean ctf
 *    [ ] Cortex ar | [ ] Total ctf
 *    [ ] Mean ctf |     
 *   }
 *  ===
 *  [Generate           ]
 *  }
 *  {^"Ploar plots"
 *  [Click point        ]
 *  [Get from ROI       ]
 *  Selected point:     127,45
 *  ^Select relative to^
 *  [Show plots         ]
 *  }
 * }
 * @enduml
 * 
 * @startuml
 * 
 * usecase UC0 as "**Load QCONF**
 * --
 * Open QCONF file
 * ..UC0.."
 * 
 * usecase UC1 as "**Select points on contour**
 * --
 * Use can click and select multiple
 * points in cell contour.
 * ==
 * This works within all frames
 * ..UC1..
 * "
 * 
 * usecase UC2 as "**Transfer points to ROI**
 * -- 
 * Selected points can be
 * copied to ROI manager
 * ..UC2..
 * "
 * 
 * usecase UC3 as "**Transfer points from ROI**
 * --
 * Copy points from ROI
 * manager and show them
 * in contour.
 * ==
 * * Delete old points
 * * Deal with different
 * frames
 * ..UC3..
 * "
 * 
 * usecase UC4 as "**Show selected points**
 * --
 * Show points for each frame
 * as user slide slider
 * ..UC4..
 * "
 * 
 * usecase UC5 as "**Clear points**
 * --
 * Remove all points
 * ..UC5.."
 * 
 * usecase UC6 as "**Track points**
 * --
 * Perform tracking for
 * selected points
 * ==
 * Regards static or dynamic
 * ..UC6..
 * "
 * 
 * usecase UC7 as "**Display tracking**
 * --
 * Show results on screen
 * ==
 * * Depending on settings show
 * in original window or separate
 * * Show dynamic or static
 * * Color outline
 * * Smooth if option selected
 * ..UC7..
 * "
 * 
 * usecase UC8 as "**Color outline**
 * --
 * Show outline in selected
 * color
 * ==
 * Colorscale scaled to range
 * ..UC8..
 * "
 * 
 * usecase UC9 as "**Clear overlay**
 * --
 * Clear tracking
 * ==
 * * Clear original window
 * * Remove points
 * ..UC9.."
 * 
 * usecase UC10 as "**Plot maps**
 * --
 * Show selected maps
 * ==
 * Together with transferring
 * ROI allows to select
 * maxim on the map and
 * track them
 * ..UC10.."
 * 
 * usecase UC11 as "**Plot 2d**
 * --
 * Plot selected metrics as
 * 2D plot in function of
 * frames
 * ==
 * * Can open many plots at
 * once
 * * Should allow to select
 * cell and channel
 * ..UC11.."
 * 
 * usecase UC12 as "**Copy to table**
 * --
 * Copy selected metrics to
 * IJ table.
 * ==
 * * Should allow to select
 * cell and channel
 * ..UC12..
 * "
 * 
 * usecase UC13 as "**Polar plots**
 * --
 * Generate polar plots
 * ==
 * * Save or show (depending on IJ
 * features in showing vector files)
 * * Show in log if saved
 * ..UC13..
 * "
 * 
 * usecase UC14 as "**Select origin point**
 * --
 * Allow to select origin
 * point for polar plots
 * ==
 * * click on screen
 * * Relative to screen
 * * Relative to cell
 * ..UC14..
 * "
 * 
 * usecase UC15 as "**Predefined trackings**
 * --
 * Allow to track points
 * from predefined settings
 * ==
 * Like:
 * * Max from motility map
 * ..UC15..
 * "
 * 
 * usecase UC16 as "**Smooth tracks**
 * --
 * Apply smoothing to tracks
 * ==
 * * If option selected
 * ..UC16..
 * "
 * 
 * note bottom of (UC12) : Decide how to deal\nwith many tables
 * 
 * note right of (UC8)
 * Decide if standalone
 * Now dependend from UC7
 * Tracking must be done first and 
 * tracking map shown but this can
 * be a standalone option as well
 * end note
 * 
 * note right of (UC13) : For all cells\nor add selector?
 * 
 * User -> (UC0)
 * User -> (UC1)
 * (UC1) ..> (UC4) : <<include>>
 * User -> (UC12)
 * User -> (UC5)
 * User -> (UC3)
 * (UC3) ..> (UC5) : <<extend>>
 * User -> (UC2)
 * User -> (UC6)
 * (UC6) ..> (UC7) : <<include>>
 * (UC6) ..> (UC15) : <<include>>
 * (UC7) ..> (UC8) : <<include>>
 * (UC7) ..> (UC16) : <<include>>
 * User -> (UC9)
 * (UC9) ..> (UC5) : <<extend>>
 * User --> (UC10)
 * User --> (UC11)
 * User --> (UC13)
 * (UC13) ..> (UC14) : <<include>>
 * @enduml
 * !<
 */
/**
 * Main class for Protrusion Analysis module.
 * 
 * <p>Contain business logic for protrusion analysis. The UI is built by
 * {@link com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisUI}. The communication
 * between
 * these modules is through
 * {@link com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions}
 * <br>
 * <img src="doc-files/Prot_Analysis_1_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 */
public class Prot_Analysis extends AbstractPluginQconf {

  static final Logger LOGGER = LoggerFactory.getLogger(Prot_Analysis.class.getName());

  private static String thisPluginName = "Protrusion Analysis";

  private boolean uiCancelled = false;
  ImagePlus image = null;
  // points selected by user for current frame, cleared on each slice shift. In image coordinates
  PointHashSet selected = new PointHashSet();
  // updated on each slice, outlines for current frame
  ArrayList<Outline> outlines = new ArrayList<>();
  /**
   * Instance of module UI.
   * 
   * <p>Initialised by this constructor.
   */
  CustomStackWindow frameGui;

  /**
   * Instance of ResultTable.
   * 
   * <p>Initialised by this constructor, filled by {@link #runFromQconf()}, shown by
   * {@link ProtAnalysisUI#actionPerformed(ActionEvent)}
   */
  ResultsTable rt;

  /**
   * Current frame, 0-based.
   */
  int currentFrame = 0;

  /**
   * Default constructor.
   * 
   */
  public Prot_Analysis() {
    super(new ProtAnalysisOptions(), thisPluginName);
    ImagePlus image = getImage();
    LOGGER.trace("Attached image " + image.toString());
    frameGui = new CustomStackWindow(this, image);
    // gui.writeUI(); // fill UI controls with default options
    rt = createCellResultTable();
  }

  /**
   * Constructor that allows to provide own configuration parameters.
   * 
   * <p>Immediately executed all computations.
   * 
   * @param paramString parameter string.
   * @throws QuimpPluginException on error
   */
  public Prot_Analysis(String paramString) throws QuimpPluginException {
    // would start computations so we overrode runFromQconf (called by loadFile)
    super(paramString, new ProtAnalysisOptions(), thisPluginName);
    selected = new PointHashSet();
    outlines = new ArrayList<>();
    ImagePlus im = getImage();
    frameGui = new CustomStackWindow(this, im); // need to be called after QCONF is loaded
    rt = createCellResultTable();
  }

  /**
   * get current sink.
   * 
   * @return sink type
   */
  public MessageSinkTypes getSink() {
    return errorSink;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#validate()
   */
  @Override
  protected void validate() throws QuimpException {
    super.validate();
    qconfLoader.getEcmm();
    qconfLoader.getQ();
    qconfLoader.getStats();
  }

  /**
   * Write cell statistic and protrusion statistics to files.
   * 
   * @param h Cell number
   * @param mapCell cell map
   * @param mf maxima finder object
   * @param trackCollection track collection object
   * @return ProtStat instance of object that keeps cell statistics. Can be used to form e.g.
   *         table with results.
   * 
   * @throws FileNotFoundException if stats can not be written
   */
  private ProtStat writeStats(int h, STmap mapCell, MaximaFinder mf,
          TrackCollection trackCollection) throws FileNotFoundException {
    QParamsQconf qp = (QParamsQconf) qconfLoader.getQp();
    // Maps are correlated in order with Outlines in DataContainer.
    // write data
    PrintWriter cellStatFile = new PrintWriter(
            Paths.get(qp.getPath(), qp.getFileName() + "_" + h + FileExtensions.cellStatSuffix)
                    .toFile());
    PrintWriter protStatFile = new PrintWriter(
            Paths.get(qp.getPath(), qp.getFileName() + "_" + h + FileExtensions.protStatSuffix)
                    .toFile());
    new ProtStat(mf, trackCollection, qp.getLoadedDataContainer().getStats().sHs.get(h), mapCell)
            .writeProtrusion(protStatFile, h);

    ProtStat cellStat = new ProtStat(mf, trackCollection,
            qp.getLoadedDataContainer().getStats().sHs.get(h), mapCell);

    cellStat.writeCell(cellStatFile, h);
    protStatFile.close();
    cellStatFile.close();
    return cellStat;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractPluginBase#showUi(boolean)
   */
  @Override
  public void showUi(boolean val) throws Exception {
    if (frameGui != null) {
      frameGui.showUI(true);
      // gui.setVisible(true);
    } else {
      LOGGER.error("You need image (and QCONF) to see UI");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpPlugin#about()
   */
  @Override
  public String about() {
    return "Protrusion Analysis Plugin.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }

  /**
   * Called after Apply in contrary to #{@link Prot_Analysis#run(String)} called once on the
   * beginning.
   * 
   */
  @Deprecated
  void runPlugin() {
    try {
      IJ.showStatus("Protrusion Analysis");
      runFromQconf();
      IJ.log("Protrusion Analysis complete");
      IJ.showStatus("Finished");
      publishMacroString(thisPluginName);
    } catch (QuimpException qe) {
      qe.setMessageSinkType(errorSink);
      qe.handleException(IJ.getInstance(), "Error during execution of Protrusion Analysis");
    } catch (Exception e) { // catch all here and convert to expected type
      logger.debug(e.getMessage(), e);
      IJ.error("Problem with running plugin", e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.plugin.PlugIn#run(java.lang.String)
   */
  @Override
  public void run(String arg) {
    super.run(arg);
  }

  /**
   * Build cell statistic result table. It contains statistics for all cells.
   * 
   * @return Handle to ResultTable that can be displayed by show("Name"_ method.
   */
  public ResultsTable createCellResultTable() {
    ResultsTable rt = new ResultsTable();
    return rt;
  }

  /**
   * Main runner.
   * 
   * <p>Keeps logic of ECMM, ANA, Q plugins.
   * 
   * <p>This method reads entries in
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions} and performs
   * selected
   * actions. Additionally it collects all results for all cells in one common table.
   * 
   * @throws QuimpException on problem with plugin
   */
  @Deprecated
  protected void runFromQconfLocal() throws QuimpException {
    // need to be mapped locally, run will create new object after deserialisation
    ProtAnalysisOptions config = (ProtAnalysisOptions) options;
    STmap[] stMap = ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getQState();
    OutlinesCollection ohs =
            ((QParamsQconf) qconfLoader.getQp()).getLoadedDataContainer().getEcmmState();
    TrackVisualisation.Stack visStackDynamic = null;
    TrackVisualisation.Image visStackStatic = null;
    TrackVisualisation.Stack visStackOutline = null;
    int h = 0;

    ImagePlus im1static = qconfLoader.getImage();
    if (im1static == null) {
      return; // stop if no image
    }

    // dynamic stack
    // if (config.plotDynamicmax) {
    // visStackDynamic = new TrackVisualisation.Stack(im1static.duplicate());
    // visStackDynamic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Dynamic
    // tracking"));
    // }

    // static plot - all maxima on stack or flatten stack
    // if (config.plotStaticmax) {
    // visStackStatic = new TrackVisualisation.Image(im1static.duplicate());
    // visStackStatic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Static points"));
    // if (config.staticPlot.averimage) {
    // visStackStatic.flatten(ZProjector.AVG_METHOD, false);
    // }
    // }

    // outlines plot
    // if (config.plotOutline) {
    // visStackOutline = new TrackVisualisation.Stack(im1static.duplicate());
    // visStackOutline.getOriginalImage().setTitle(WindowManager.makeUniqueName("Outlines"));
    // }

    // logger.trace("Cells in database: " + stMap.length);
    // for (STmap mapCell : stMap) { // iterate through cells
    // // convert binary 2D array to ImageJ
    // TrackVisualisation.Map visSingle =
    // new TrackVisualisation.Map(WindowManager.makeUniqueName("motility_map_cell_" + h),
    // QuimPArrayUtils.double2dfloat(mapCell.getMotMap()));
    // // compute maxima
    // MaximaFinder mf = new MaximaFinder(visSingle.getOriginalImage().getProcessor());
    // mf.computeMaximaIJ(config.noiseTolerance); // 1.5
    // // track maxima across motility map
    // TrackMapAnalyser pt = new TrackMapAnalyser();
    // pt.trackMaxima(mapCell, config.dropValue, mf);
    // TrackCollection trackCollection = pt.getTrackCollection();
    //
    // // plot motility map with maxima nad tracking lines
    // if (config.plotMotmapmax) {
    // visSingle.addMaximaToImage(mf);
    // visSingle.addTrackingLinesToImage(trackCollection);
    // visSingle.getOriginalImage().show();
    // }
    // // plot motility map only
    // if (config.plotMotmap) {
    // ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("motility_map"),
    // "rwb", mapCell.getMotMap(), ohs.oHs.get(h).migLimits[0],
    // ohs.oHs.get(h).migLimits[1]);
    // mm.setTitle(WindowManager.makeUniqueName("MotilityMap_cell_" + h));
    // mm.show();
    // }
    // // plot convexity map only
    // if (config.plotConmap) {
    // ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("convexity_map"),
    // "rbb", mapCell.getConvMap(), ohs.oHs.get(h).curvLimits[0],
    // ohs.oHs.get(h).curvLimits[1]);
    // mm.setTitle(WindowManager.makeUniqueName("ConvexityMap_cell_" + h));
    // mm.show();
    // }

    // // plot static lines/or maxi
    // if (config.plotStaticmax && config.staticPlot.plotmax && config.staticPlot.plottrack) {
    // visStackStatic.addElementsToImage(mapCell, trackCollection, mf);
    // } else if (config.plotStaticmax && config.staticPlot.plotmax
    // && config.staticPlot.plottrack == false) {
    // visStackStatic.addElementsToImage(mapCell, null, mf);
    // } else if (config.plotStaticmax && config.staticPlot.plotmax == false
    // && config.staticPlot.plottrack) {
    // visStackStatic.addElementsToImage(mapCell, trackCollection, null);
    // }
    //
    // // plot dynamic stack
    // if (config.plotDynamicmax) {
    // if (config.dynamicPlot.plotmax) {
    // visStackDynamic.addMaximaToImage(mapCell, mf);
    // }
    // if (config.dynamicPlot.plottrack) {
    // visStackDynamic.addTrackingLinesToImage(mapCell, trackCollection);
    // }
    // }

    // if (config.plotOutline) {
    // visStackOutline.addOutlinesToImage(mapCell, config);
    // }
    //
    // // write svg plots
    // try {
    // if (config.polarPlot.plotpolar && config.polarPlot.useGradient) {
    // PolarPlot pp = new PolarPlot(mapCell, config.polarPlot.gradientPoint);
    // pp.labels = true;
    // pp.generatePlot(Paths.get(qconfLoader.getQp().getPath(),
    // qconfLoader.getQp().getFileName() + "_" + h + FileExtensions.polarPlotSuffix)
    // .toString());
    // }
    // // write stats, and add to table
    // writeStats(h, mapCell, mf, trackCollection).cellStatistics.addCellToCellTable(rt);
    // } catch (IOException e) {
    // throw new QuimpException(e);
    // }

    // update static fields in gui
    // gui.lbMaxnum.setText(Integer.toString(mf.getMaximaNumber()));
    // gui.lbMaxval
    // .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMax(mapCell.getMotMap())));
    // gui.lbMinval
    // .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMin(mapCell.getMotMap())));
    // h++;
    // }

    // if (config.plotStaticmax)
    //
    // {
    // visStackStatic.getOriginalImage().show();
    // }
    // if (config.plotDynamicmax) {
    // visStackDynamic.getOriginalImage().show();
    // }
    // if (config.plotOutline) {
    // visStackOutline.getOriginalImage().show();
    // }
    // rt.show("Cumulated cell statistics");

  }

  @Override
  protected void runFromPaqp() throws QuimpException {
    throw new QuimpException("This plugin does not support paQP files.");

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.AbstractPluginQconf#runFromQconf()
   */
  @Override
  protected void runFromQconf() throws QuimpException {
    // need to be overridden to block execution until we setup all UI elements (called by loadFile)
  }

  /**
   * Get image associated with loaded QCONF.
   * 
   * @return image or null if image could not be loaded
   */
  ImagePlus getImage() {
    if (image == null) {
      if (getQconfLoader() != null) {
        image = getQconfLoader().getImage();
      } else {
        throw new RuntimeException("Can not obtain image");
      }
    }
    return image;
  }

  /**
   * Get gui.
   * 
   * @return Main window class.
   */
  CustomStackWindow getGui() {
    return frameGui;
  }

  /**
   * Keep list of selected points.
   * 
   * <p>Reason of this class is that {@link CustomStackWindow} and {@link CustomCanvas} operate on
   * 2D
   * images without knowledge about frame, which is needed. They also use java.awt.Point as main
   * class. Therefore the point selected in the image by user in {@link CustomCanvas} contains only
   * x,y and cell number (all stored in {@link PointCoords}). Frame number is appended with
   * {@link PointHashSet#add(PointCoords)}, called in this context (frame is stored in
   * Prot_Analysis.currentFrame)
   * 
   * <p>Field {@link Prot_Analysis#currentFrame} is updated by {@link CustomStackWindow} whereas
   * point operations happen in {@link CustomCanvas}. {@link Prot_Analysis} integrates all
   * informations.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  class PointHashSet extends HashSet<PointCoords> {

    /**
     * Add information about current frame to point.
     * 
     * @param e 2D point from current frame. Field {@link PointCoords#frame} will be overwritten by
     *        current frame.
     * @return true if point exists in set.
     */
    @Override
    public boolean add(PointCoords e) {
      e.frame = currentFrame;
      LOGGER.debug("Added point: " + e);
      return super.add(e);
    }

    /**
     * Add {@link PointCoords} with frame number.
     * 
     * <p>In contrary to {@link #add(PointCoords)} this method does not override frame number in
     * specified {@link PointCoords}.
     * 
     * @param e 2D point from current frame.
     * @return true if point exists in set.
     */
    boolean addRaw(PointCoords e) {
      LOGGER.debug("Added raw point: " + e);
      return super.add(e);
    }

    /**
     * Remove point from set.
     * 
     * @param e point to remove
     * @return true if exist
     */
    public boolean remove(PointCoords e) {
      e.frame = currentFrame;
      return super.remove(e);
    }

  }

}
