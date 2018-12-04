package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.scijava.vecmath.Point3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.plugin.AbstractPluginQconf;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;
import com.github.celldynamics.quimp.utils.graphics.PolarPlot;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;

/*
 * !>
 * @startuml doc-files/Prot_Analysis_1_UML.png
 * Prot_Analysis *-- "1" ProtAnalysisOptions
 * Prot_Analysis *-- "1" ProtAnalysisUI
 * ProtAnalysisUI o-- "1" ProtAnalysisOptions
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
  private CustomStackWindow gui;

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
    gui = new CustomStackWindow(this, image);
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
    gui = new CustomStackWindow(this, getImage()); // need to be called after QCONF is loaded
    rt = createCellResultTable();
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
    if (gui != null) {
      gui.showUI(true);
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
    if (config.plotDynamicmax) {
      visStackDynamic = new TrackVisualisation.Stack(im1static.duplicate());
      visStackDynamic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Dynamic tracking"));
    }

    // static plot - all maxima on stack or flatten stack
    if (config.plotStaticmax) {
      visStackStatic = new TrackVisualisation.Image(im1static.duplicate());
      visStackStatic.getOriginalImage().setTitle(WindowManager.makeUniqueName("Static points"));
      if (config.staticPlot.averimage) {
        visStackStatic.flatten(ZProjector.AVG_METHOD, false);
      }
    }

    // outlines plot
    if (config.plotOutline) {
      visStackOutline = new TrackVisualisation.Stack(im1static.duplicate());
      visStackOutline.getOriginalImage().setTitle(WindowManager.makeUniqueName("Outlines"));
    }

    logger.trace("Cells in database: " + stMap.length);
    for (STmap mapCell : stMap) { // iterate through cells
      // convert binary 2D array to ImageJ
      TrackVisualisation.Map visSingle =
              new TrackVisualisation.Map(WindowManager.makeUniqueName("motility_map_cell_" + h),
                      QuimPArrayUtils.double2dfloat(mapCell.getMotMap()));
      // compute maxima
      MaximaFinder mf = new MaximaFinder(visSingle.getOriginalImage().getProcessor());
      mf.computeMaximaIJ(config.noiseTolerance); // 1.5
      // track maxima across motility map
      TrackMapAnalyser pt = new TrackMapAnalyser();
      pt.trackMaxima(mapCell, config.dropValue, mf);
      TrackCollection trackCollection = pt.getTrackCollection();

      // plot motility map with maxima nad tracking lines
      if (config.plotMotmapmax) {
        visSingle.addMaximaToImage(mf);
        visSingle.addTrackingLinesToImage(trackCollection);
        visSingle.getOriginalImage().show();
      }
      // plot motility map only
      if (config.plotMotmap) {
        ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("motility_map"),
                "rwb", mapCell.getMotMap(), ohs.oHs.get(h).migLimits[0],
                ohs.oHs.get(h).migLimits[1]);
        mm.setTitle(WindowManager.makeUniqueName("MotilityMap_cell_" + h));
        mm.show();
      }
      // plot convexity map only
      if (config.plotConmap) {
        ImagePlus mm = mapCell.map2ColorImagePlus(WindowManager.makeUniqueName("convexity_map"),
                "rbb", mapCell.getConvMap(), ohs.oHs.get(h).curvLimits[0],
                ohs.oHs.get(h).curvLimits[1]);
        mm.setTitle(WindowManager.makeUniqueName("ConvexityMap_cell_" + h));
        mm.show();
      }

      // plot static lines/or maxi
      if (config.plotStaticmax && config.staticPlot.plotmax && config.staticPlot.plottrack) {
        visStackStatic.addElementsToImage(mapCell, trackCollection, mf);
      } else if (config.plotStaticmax && config.staticPlot.plotmax
              && config.staticPlot.plottrack == false) {
        visStackStatic.addElementsToImage(mapCell, null, mf);
      } else if (config.plotStaticmax && config.staticPlot.plotmax == false
              && config.staticPlot.plottrack) {
        visStackStatic.addElementsToImage(mapCell, trackCollection, null);
      }

      // plot dynamic stack
      if (config.plotDynamicmax) {
        if (config.dynamicPlot.plotmax) {
          visStackDynamic.addMaximaToImage(mapCell, mf);
        }
        if (config.dynamicPlot.plottrack) {
          visStackDynamic.addTrackingLinesToImage(mapCell, trackCollection);
        }
      }

      if (config.plotOutline) {
        visStackOutline.addOutlinesToImage(mapCell, config);
      }

      // write svg plots
      try {
        if (config.polarPlot.plotpolar && config.polarPlot.useGradient) {
          PolarPlot pp = new PolarPlot(mapCell, config.polarPlot.gradientPoint);
          pp.labels = true;
          pp.generatePlot(Paths.get(qconfLoader.getQp().getPath(),
                  qconfLoader.getQp().getFileName() + "_" + h + FileExtensions.polarPlotSuffix)
                  .toString());
        }
        // write stats, and add to table
        writeStats(h, mapCell, mf, trackCollection).cellStatistics.addCellToCellTable(rt);
      } catch (IOException e) {
        throw new QuimpException(e);
      }

      // update static fields in gui
      // gui.lbMaxnum.setText(Integer.toString(mf.getMaximaNumber()));
      // gui.lbMaxval
      // .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMax(mapCell.getMotMap())));
      // gui.lbMinval
      // .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMin(mapCell.getMotMap())));
      h++;
    }

    if (config.plotStaticmax) {
      visStackStatic.getOriginalImage().show();
    }
    if (config.plotDynamicmax) {
      visStackDynamic.getOriginalImage().show();
    }
    if (config.plotOutline) {
      visStackOutline.getOriginalImage().show();
    }
    rt.show("Cumulated cell statistics");

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
    return gui;
  }

  /**
   * Keep list of selected points.
   * 
   * <p>Reason of this class is that {@link CustomStackWindow} and {@link CustomCanvas} operate on
   * 2D
   * images without knowledge about frame, which is needed. They also use java.awt.Point as main
   * class. Therefore the point selected in the image by user is added to the list with
   * {@link PointHashSet#add(Point)},
   * which incorporates the frame number.
   * 
   * <p>Field {@link Prot_Analysis#currentFrame} is updated by {@link CustomStackWindow} whereas
   * point operations happen in {@link CustomCanvas}. {@link Prot_Analysis} integrates all
   * informations.
   * 
   * @author p.baniukiewicz
   *
   */
  @SuppressWarnings("serial")
  class PointHashSet extends HashSet<Pair<Point3i, Integer>> {

    /**
     * Add information about current frame to point.
     * 
     * @param e Pair of 2D point which will be changed to {@link Point3i} with current frame at z
     *        position and cell number (will not be touched).
     * @return true if point exists in set.
     */
    public boolean add(Pair<? extends Point, Integer> e) {
      // convert from java.awt.Point to Point3i and add frame information
      Point3i tmpP = new Point3i((int) Math.round(e.getLeft().getX()),
              (int) Math.round(e.getLeft().getY()), currentFrame);
      // repack in Pair together with cell number that come from e
      ImmutablePair<Point3i, Integer> toAdd =
              new ImmutablePair<Point3i, Integer>(tmpP, e.getRight());
      LOGGER.debug("Added point: " + toAdd);
      return add(toAdd);
    }

    /**
     * Remove point from set.
     * 
     * @param e point to remove
     * @return true if exist
     */
    public boolean remove(Pair<? extends Point, Integer> e) {
      Point3i tmpP = new Point3i((int) Math.round(e.getLeft().getX()),
              (int) Math.round(e.getLeft().getY()), currentFrame);
      return remove(new ImmutablePair<Point3i, Integer>(tmpP, e.getRight()));
    }

  }

}
