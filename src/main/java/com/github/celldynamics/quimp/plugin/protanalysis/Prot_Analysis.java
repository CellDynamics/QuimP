package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.IQuimpPlugin;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;
import com.github.celldynamics.quimp.utils.graphics.PolarPlot;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;

/*
 * !>
 * @startuml doc-files/Prot_Analysis_1_UML.png
 * Prot_Analysis *-- "1" ProtAnalysisConfig
 * Prot_Analysis *-- "1" ProtAnalysisUI
 * ProtAnalysisUI o-- "1" ProtAnalysisConfig
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
 * {@link com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisConfig}
 * <br>
 * <img src="doc-files/Prot_Analysis_1_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 */
public class Prot_Analysis implements IQuimpPlugin {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(Prot_Analysis.class.getName());

  /**
   * Loaded QCONF file.
   * 
   * <p>Initialised by {@link #loadFile(File)} through this constructor.
   */
  QconfLoader qconfLoader = null; // main object representing loaded configuration file
  private boolean uiCancelled = false;
  /**
   * Instance of module UI.
   * 
   * <p>Initialised by this constructor.
   */
  public ProtAnalysisUI gui;
  /**
   * Indicate that plugin is run as macro from script. Blocks all UIs.
   */
  private MessageSinkTypes runAsMacro = MessageSinkTypes.GUI;

  /**
   * The param list.
   */
  // default configuration parameters, for future using
  ParamList paramList = new ParamList();
  /**
   * Keep overall configuration.
   * 
   * <p>This object is filled in GUI and passed to runPlugin, where it is read out. Initialised by
   * this constructor.
   */
  ProtAnalysisConfig config;

  /**
   * Instance of ResultTable.
   * 
   * <p>Initialised by this constructor, filled by {@link #runFromQconf()}, shown by
   * {@link ProtAnalysisUI#actionPerformed(ActionEvent)}
   */
  ResultsTable rt;

  /**
   * Default constructor.
   * 
   */
  public Prot_Analysis() {
  }

  /**
   * Constructor that allows to provide own file.
   * 
   * @param paramFile File to process.
   */
  public Prot_Analysis(String paramFile) {
    run(paramFile);
  }

  /**
   * Load configuration file. (only if not loaded before).
   * 
   * <p>Validates also all necessary datafields in loaded QCONF file. Set <tt>qconfLoader</tt> field
   * on success or set it to <tt>null</tt>.
   * 
   * @param paramFile
   * 
   * @throws QuimpException When QCONF could not be loaded or it does not meet requirements.
   */
  private void loadFile(File paramFile) throws QuimpException {
    if (qconfLoader == null || qconfLoader.getQp() == null) {
      // load new file
      qconfLoader = new QconfLoader(paramFile, FileExtensions.newConfigFileExt);
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      if (qconfLoader.isFileLoaded() == QParams.NEW_QUIMP) { // new path
        // validate in case new format
        qconfLoader.getBOA(); // will throw exception if not present
        qconfLoader.getEcmm();
        qconfLoader.getQ();
      } else {
        qconfLoader = null; // failed load or checking
        throw new QuimpPluginException("QconfLoader returned unsupported version of QuimP or error."
                + " Only new format can be loaded");
      }
    }
  }

  /**
   * Main runner.
   * 
   * <p>Keeps logic of ECMM, ANA, Q plugins.
   * 
   * <p>This method reads entries in
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisConfig} and performs
   * selected
   * actions. Additionally it collects all results for all cells in one common table.
   * 
   * @throws QuimpException on problem with plugin
   * @throws IOException on problem with saving svg plots
   */
  private void runFromQconf() throws QuimpException, IOException {
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
      visStackDynamic.getOriginalImage().setTitle("Dynamic tracking");
    }

    // static plot - all maxima on stack or flatten stack
    if (config.plotStaticmax) {
      visStackStatic = new TrackVisualisation.Image(im1static.duplicate());
      visStackStatic.getOriginalImage().setTitle("Static points");
      if (config.staticPlot.averimage) {
        visStackStatic.flatten(ZProjector.AVG_METHOD, false);
      }
    }

    // outlines plot
    if (config.plotOutline) {
      visStackOutline = new TrackVisualisation.Stack(im1static.duplicate());
      visStackOutline.getOriginalImage().setTitle("Outlines");
    }

    LOGGER.trace("Cells in database: " + stMap.length);
    for (STmap mapCell : stMap) { // iterate through cells
      // convert binary 2D array to ImageJ
      TrackVisualisation.Map visSingle = new TrackVisualisation.Map("motility_map_cell_" + h,
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
        ImagePlus mm = mapCell.map2ColorImagePlus("motility_map", "rwb", mapCell.getMotMap(),
                ohs.oHs.get(h).migLimits[0], ohs.oHs.get(h).migLimits[1]);
        mm.setTitle("MotilityMap_cell_" + h);
        mm.show();
      }
      // plot convexity map only
      if (config.plotConmap) {
        ImagePlus mm = mapCell.map2ColorImagePlus("convexity_map", "rbb", mapCell.getConvMap(),
                ohs.oHs.get(h).curvLimits[0], ohs.oHs.get(h).curvLimits[1]);
        mm.setTitle("ConvexityMap_cell_" + h);
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
      if (config.polarPlot.plotpolar && config.polarPlot.useGradient) {
        PolarPlot pp = new PolarPlot(mapCell, config.polarPlot.gradientPoint);
        pp.labels = true;
        pp.generatePlot(Paths.get(qconfLoader.getQp().getPath(),
                qconfLoader.getQp().getFileName() + "_" + h + FileExtensions.polarPlotSuffix)
                .toString());
      }
      // write stats, and add to table
      writeStats(h, mapCell, mf, trackCollection).cellStatistics.addCellToCellTable(rt);

      // update static fields in gui
      gui.lbMaxnum.setText(Integer.toString(mf.getMaximaNumber()));
      gui.lbMaxval
              .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMax(mapCell.getMotMap())));
      gui.lbMinval
              .setText(String.format("%1$.3f", QuimPArrayUtils.array2dMin(mapCell.getMotMap())));
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
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#setup()
   */
  @Override
  public int setup() {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#setPluginConfig(com.github.celldynamics.
   * quimp.
   * plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    paramList = new ParamList(par);
    // TODO restore config from json

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#getPluginConfig()
   */
  @Override
  public ParamList getPluginConfig() {
    // TODO convert config to json one liner and add to paramlist
    // paramList.put("config", json)
    return paramList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#showUI(boolean)
   */
  @Override
  public int showUi(boolean val) {
    gui.showUI(val);
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#getVersion()
   */
  @Override
  public String getVersion() {
    return "See QuimP version";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin#about()
   */
  @Override
  public String about() {
    return "Protrusion Analysis Plugin.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }

  void runPlugin() throws QuimpPluginException {
    try {
      IJ.showStatus("Protrusion Analysis");
      runFromQconf();
      IJ.log("Protrusion Analysis complete");
      IJ.showStatus("Finished");
    } catch (Exception e) { // catch all here and convert to expected type
      throw new QuimpPluginException(e);
    }
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

  /*
   * (non-Javadoc)
   * 
   * @see ij.plugin.PlugIn#run(java.lang.String)
   */
  @Override
  public void run(String arg) {
    // set file name or null if no file provided
    File paramFile;
    if (arg == null || arg.isEmpty()) {
      paramFile = null;
    } else {
      paramFile = new File(arg);
    }
    IJ.log(new QuimpToolsCollection().getQuimPversion());
    config = new ProtAnalysisConfig();
    gui = new ProtAnalysisUI(this);
    rt = createCellResultTable();
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    // check whether config file name is provided or ask user for it
    try {
      IJ.showStatus("Protrusion Analysis");
      loadFile(paramFile); // load configuration file given by paramFile and verify it
      if (qconfLoader.getQp() == null) {
        return; // not loaded
      }
      gui.writeUI(); // set ui
      showUi(true); // show it and wait for user action. Plugin is run from Apply button
      if (uiCancelled) {
        return;
      }
    } catch (QuimpException qe) { // catch QuimpPluginException and QuimpException
      qe.setMessageSinkType(runAsMacro);
      qe.handleException(IJ.getInstance(), "Protrusion Analysis:");
    } catch (Exception e) { // catch all exceptions here
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Problem with running Protrusion Analysis mapping: " + e.getMessage());
    }
  }
}
