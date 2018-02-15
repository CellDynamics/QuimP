package com.github.celldynamics.quimp.plugin.binaryseg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
import com.github.celldynamics.quimp.CellStatsEval;
import com.github.celldynamics.quimp.Constrictor;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.SnakeHandler;
import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.StatsCollection;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.AbstractOptionsParser;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.AbstractPluginTemplate;
import com.github.celldynamics.quimp.plugin.IQuimpPluginAttachImagePlus;
import com.github.celldynamics.quimp.plugin.IQuimpPluginAttachNest;
import com.github.celldynamics.quimp.plugin.IQuimpPluginExchangeData;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.process.ImageProcessor;

/**
 * Binary segmentation plugin called from Fiji.
 * 
 * <p>Use {@link BinarySegmentation} for API calls or {@link BinarySegmentation_#run(String)}
 * 
 * <p>This is front-end of {@link BinarySegmentation} used as stand alone plugin and BOA component.
 * 
 * @author p.baniukiewicz
 * @see BinarySegmentationView
 * @See {@link BinarySegmentationOptions}
 *
 */
public class BinarySegmentation_ extends AbstractPluginTemplate implements IQuimpPluginSynchro,
        IQuimpPluginAttachNest, IQuimpPluginExchangeData, IQuimpPluginAttachImagePlus {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentation_.class.getName());

  private static String thisPluginName = "Generate Qconf";

  private Nest nest = null; // reference to Nest object, can be null
  private ViewUpdater vu = null; // BOA context for updating it
  private BinarySegmentationView bsp = new BinarySegmentationView();
  private boolean wasNest = false; // true if attached nest from outside, used to save file or not

  private ImagePlus ip = null;

  /**
   * Default constructor.
   */
  public BinarySegmentation_() {
    super(new BinarySegmentationOptions(), thisPluginName);
    BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
    bsp.addApplyListener(new ActionListener() {

      /**
       * Apply button.
       * 
       * <p>Run plugin in GUI mode, handle exceptions.
       * 
       * @param e event
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
        // update config for export, always handle current one
        opts.options = bsp.getValues();
        try {
          runPlugin(); // run after apply
          publishMacroString(thisPluginName);
        } catch (QuimpException qe) {
          qe.setMessageSinkType(errorSink);
          qe.handleException(IJ.getInstance(), BinarySegmentation_.class.getSimpleName());
        } catch (Exception ee) { // catch all exceptions here
          logger.debug(ee.getMessage(), ee);
          logger.error("Problem with running plugin: " + ee.getMessage());
        }
      }
    });

    bsp.addLoadMaskListener(new ActionListener() {

      /**
       * Load mask button.
       * 
       * <p>Grab path to image but technically does not load it yet.
       * 
       * @param e event
       */
      @Override
      public void actionPerformed(ActionEvent e) {
        opts.outputPath = ""; // clear path to ask again on new file
        OpenDialog od = new OpenDialog("Load mask file", "");
        if (od.getPath() != null) { // not canceled
          opts.maskFileName = od.getPath(); // not part of UI, store separately
        }
      }
    });

    bsp.addSelectImageListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.ITEM_STATE_CHANGED) {
          opts.outputPath = ""; // clear path to ask again on new file
        }
      }
    });

    opts.options = bsp.getValues(); // store initial values
  }

  /**
   * Constructor for API calls.
   * 
   * <p>Call {@link #runPlugin()} afterwards. Note that {@link AbstractOptionsParser#apiCall} is
   * set to true and sink to Console.
   * 
   * @param options configuration options
   */
  public BinarySegmentation_(AbstractPluginOptions options) {
    super(options, thisPluginName);
    apiCall = true;
    errorSink = MessageSinkTypes.CONSOLE;
  }

  /**
   * Main plugin logic.
   * 
   * @throws QuimpPluginException on any error, handled by {@link #run(String)}
   */
  @Override
  protected void runPlugin() throws QuimpPluginException {
    bsp.setValues(((BinarySegmentationOptions) options).options); // update GUI
    DataContainer dt = null;
    BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
    LOGGER.debug(opts.toString());
    // try to open images selected mask override loaded one (if both specified)
    String selectedImage = opts.options.get(BinarySegmentationView.SELECT_MASK);
    String selectedOriginalImage = opts.options.get(BinarySegmentationView.SELECT_ORIGINAL_IMAGE);

    ImagePlus maskFile = null;
    ImagePlus orgFile = null;
    Path orgFilePath = null; // depending on source will be read from fileinfo or provided path
    if (selectedImage != null && !selectedImage.equals(BOA_.NONE)) {
      maskFile = WindowManager.getImage(selectedImage);
    } else { // try file if exists
      if (opts.maskFileName != null && !opts.maskFileName.isEmpty()) {
        maskFile = IJ.openImage(opts.maskFileName);
      }
    }
    if (selectedOriginalImage != null && !selectedOriginalImage.equals(BOA_.NONE)) {
      orgFile = WindowManager.getImage(selectedOriginalImage);
      if (orgFile != null) { // if window failed try as path
        FileInfo orgfileinfo = orgFile.getFileInfo();
        orgFilePath = Paths.get(orgfileinfo.directory, orgFile.getTitle());
      } else {
        orgFile = IJ.openImage(selectedOriginalImage);
        orgFilePath = Paths.get(selectedOriginalImage);
      }
    } else {
      orgFilePath = Paths.get(""); // just to show something in exception
    }
    if (maskFile == null) {
      throw new QuimpPluginException("Mask can not be loaded or found.");
    }
    // here we have maskFile filled
    FileInfo maskfileinfo = maskFile.getFileInfo();

    // reconstruct BOA structures if called without it
    if (wasNest == false) {
      nest = new Nest(); // run as plugin outside BOA
      // initialise static fields in BOAState, required for nest.addHandlers(ret)
      // use mask file but replace to initialise sizes, etc but replace names to org
      BOA_.qState = new BOAState(maskFile);
      dt = new DataContainer();
      dt.BOAState = BOA_.qState;
      dt.BOAState.nest = nest;
      dt.BOAState.binarySegmentationPlugin = this;
      dt.Stats = new StatsCollection();

      // try if original image was provided and recalculate Stats, required for BOAfree mode
      if (orgFile == null) {
        throw new QuimpPluginException(
                "Original image " + orgFilePath.toString() + " can not be opened");
      }
      dt.BOAState.boap.setOrgFile(orgFilePath.toFile());
      dt.BOAState.boap.setOutputFileCore(opts.outputPath);

    }
    LOGGER.debug("Segmentation: " + (maskFile != null ? maskFile.toString() : "null") + "params: "
            + opts.toString());
    BinarySegmentation obj = new BinarySegmentation(maskFile); // create segmentation object
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    // set interpolation params for every tracker. They are used when converting from
    // SegmentedShapeRoi to points in SnakeHandler
    boolean smoothing = opts.options.getBooleanValue(BinarySegmentationView.SMOOTHING2);
    int step = opts.options.getIntValue(BinarySegmentationView.STEP2);
    LOGGER.debug("step: " + step + " smooth: " + smoothing);
    for (ArrayList<SegmentedShapeRoi> asS : ret) {
      for (SegmentedShapeRoi ss : asS) {
        ss.setInterpolationParameters(step, smoothing);
      }
    }
    if (opts.options.getBooleanValue(BinarySegmentationView.CLEAR_NEST)) {
      nest.cleanNest(); // remove old stuff
    }
    nest.addHandlers(ret); // convert from array of SegmentedShapeRoi to SnakeHandlers

    // if run as plugin, original image is not available, use mask instead
    if (ip == null) {
      ip = maskFile;
    }
    if (opts.options.getBooleanValue(BinarySegmentationView.RESTORE_SNAKE)) {
      Constrictor constrictor = new Constrictor();
      for (SnakeHandler sh : nest.getHandlers()) {
        for (int f = sh.getStartFrame(); f <= sh.getEndFrame(); f++) {
          sh.getBackupSnake(f).calcCentroid(); // actually this is calculated in Snake constr.
          sh.getBackupSnake(f).setPositions(); // actually this is calculated in Snake constr.
          sh.getBackupSnake(f).updateNormals(true); // calculated in Snake constr. but for other
          sh.getBackupSnake(f).getBounds(); // actually this is calculated in Snake constr.

          sh.getStoredSnake(f).calcCentroid();
          sh.getStoredSnake(f).setPositions();
          sh.getStoredSnake(f).updateNormals(true);
          sh.getStoredSnake(f).getBounds();

          constrictor.constrict(sh.getStoredSnake(f), ip.getStack().getProcessor(f));
          constrictor.constrict(sh.getBackupSnake(f), ip.getStack().getProcessor(f));
        }
        sh.getLiveSnake().calcCentroid();
        sh.getLiveSnake().setPositions();
        sh.getLiveSnake().updateNormals(true);
        sh.getLiveSnake().getBounds();
        constrictor.constrict(sh.getLiveSnake(), ip.getStack().getProcessor(sh.getStartFrame()));
      }
    }
    if (vu != null) {
      vu.updateView(); // update view if we can
    }
    // save file, assume if ViewUpdater is not attached we are in standalone mode
    Serializer<DataContainer> n = new Serializer<>(dt, QuimP.TOOL_VERSION);
    n.setPretty();
    if (wasNest == false && vu == null) { // will not execute if run from BOA

      List<CellStatsEval> retstat = nest.analyse(orgFile, true);
      dt.Stats.copyFromCellStat(retstat);

      if (opts.outputPath == null || opts.outputPath.isEmpty()) { // ask for path
        String folder = maskfileinfo.directory;
        if (folder == null || folder.isEmpty()) {
          folder = IJ.getDirectory("current");
        }
        String name = QuimpToolsCollection.removeExtension(maskFile.getTitle());
        SaveDialog sd = new SaveDialog("Save QCONF", folder, name, FileExtensions.newConfigFileExt);
        String selPath = sd.getDirectory();
        String selName = sd.getFileName();
        if (selPath != null && selName != null && !selPath.isEmpty() && !selName.isEmpty()) {
          opts.outputPath = Paths.get(selPath, selName).toString();
          dt.BOAState.boap.setOutputFileCore(opts.outputPath); // update in BOA
        }
      }
      // save
      try {
        n.save(opts.outputPath);
      } catch (FileNotFoundException e) {
        throw new QuimpPluginException(e);
      }

    }
  }

  /**
   * Return true if window is visible.
   * 
   * @return true if window is visible
   */
  public boolean isWindowVisible() {
    return bsp.isWindowVisible();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.IQuimpPluginAttachNest#attachNest(com.github.celldynamics.
   * quimp.Nest)
   */
  @Override
  public void attachNest(Nest data) {
    this.nest = data;
    wasNest = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro#attachContext(com.github.celldynamics.
   * quimp.ViewUpdater)
   */
  @Override
  public void attachContext(ViewUpdater b) {
    vu = b;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#showUi(boolean)
   */
  @Override
  public void showUi(boolean val) {
    bsp.setValues(((BinarySegmentationOptions) options).options); // update GUI
    bsp.showWindow(val);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpPluginExchangeData#getPluginConfig()
   */
  @Override
  public ParamList getPluginConfig() {
    BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
    ParamList tmp = bsp.getValues();
    if (opts.maskFileName != null) {
      tmp.put(BinarySegmentationView.LOADED_FILE, opts.maskFileName);
    }
    return tmp;
  }

  /*
   * (non-Javadoc)
   *
   * @see IQuimpCorePlugin#setPluginConfig(com.github.celldynamics.quimp.plugin.ParamList)
   */
  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    bsp.setValues(par); // will not update values kept outside UI in ParamList
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpPluginAttachImage#attachImage(ij.process.
   * ImageProcessor)
   */
  @Override
  public void attachImage(ImageProcessor img) {
    ip = new ImagePlus("", img);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.celldynamics.quimp.plugin.IQuimpPluginAttachImagePlus#attachImagePlus(ij.ImagePlus)
   */
  @Override
  public void attachImagePlus(ImagePlus img) {
    ip = img;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.IQuimpPlugin#about()
   */
  @Override
  public String about() {
    return "Binary segmentation.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }
}
