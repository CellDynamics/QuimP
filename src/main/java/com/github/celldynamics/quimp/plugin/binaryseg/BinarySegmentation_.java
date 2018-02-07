package com.github.celldynamics.quimp.plugin.binaryseg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.BOA_;
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
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.IQuimpPluginAttachImagePlus;
import com.github.celldynamics.quimp.plugin.IQuimpPluginAttachNest;
import com.github.celldynamics.quimp.plugin.IQuimpPluginExchangeData;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.PluginTemplate;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.frame.Recorder;
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
public class BinarySegmentation_ extends PluginTemplate implements IQuimpPluginSynchro,
        IQuimpPluginAttachNest, IQuimpPluginExchangeData, IQuimpPluginAttachImagePlus {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentation_.class.getName());

  private Nest nest = null; // reference to Nest object, can be null
  private ViewUpdater vu = null; // BOA context for updating it
  private BinarySegmentationView bsp = new BinarySegmentationView();
  private boolean wasNest = false; // true if attached nest from outside, used to save file or not

  private ImagePlus ip = null;

  /**
   * Default constructor.
   */
  public BinarySegmentation_() {
    super(new BinarySegmentationOptions());
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
   * Called on plugin run.
   * 
   * <p>Overrides {@link PluginTemplate#run(String)} to avoid loading QCONF file which is not used
   * here.
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#run(java.lang.String)
   */
  @Override
  public void run(String arg) {
    if (arg == null || arg.isEmpty()) {
      errorSink = MessageSinkTypes.GUI; // no parameters - assume menu call
    } else {
      errorSink = MessageSinkTypes.IJERROR; // parameters available - macro call
    }
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    try {
      if (parseArgumentString(arg)) { // process options passed to this method
        bsp.setValues(((BinarySegmentationOptions) options).options); // update GUI
        runPlugin();
      } else {
        showUi(true);
      }

    } catch (QuimpException qe) {
      qe.setMessageSinkType(errorSink);
      qe.handleException(IJ.getInstance(), this.getClass().getSimpleName());
    } catch (Exception e) { // catch all exceptions here
      logger.debug(e.getMessage(), e);
      logger.error("Problem with running plugin: " + e.getMessage());
    }
  }

  /**
   * Main plugin logic.
   * 
   * @throws QuimpPluginException on any error, handled by {@link #run(String)}
   */
  private void runPlugin() throws QuimpPluginException {
    DataContainer dt = null;
    BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
    LOGGER.debug(opts.toString());
    // try to open images selected mask override loaded one (if both specified)
    String selectedImage = opts.options.get(BinarySegmentationView.SELECT_IMAGE);
    ImagePlus maskFile = null;
    if (!selectedImage.equals(BOA_.NONE)) {
      maskFile = WindowManager.getImage(selectedImage);
    } else { // try file if exists
      if (opts.maskFileName != null && !opts.maskFileName.isEmpty()) {
        maskFile = IJ.openImage(opts.maskFileName);
      }
    }
    if (maskFile == null) {
      throw new QuimpPluginException("Image can not be loaded or found.");
    }
    // here we have maskFile filled
    FileInfo fileinfo = maskFile.getFileInfo();

    if (wasNest == false) {
      nest = new Nest(); // run as plugin outside BOA
      // initialise static fields in BOAState, required for nest.addHandlers(ret)
      BOA_.qState = new BOAState(maskFile);
      dt = new DataContainer();
      dt.BOAState = BOA_.qState;
      dt.BOAState.nest = nest;
      dt.BOAState.binarySegmentationPlugin = this;
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
      if (opts.outputPath == null || opts.outputPath.isEmpty()) { // ask for path
        String folder = fileinfo.directory;
        if (folder == null || folder.isEmpty()) {
          folder = IJ.getDirectory("current");
        }
        String name = QuimpToolsCollection.removeExtension(maskFile.getTitle());
        SaveDialog sd = new SaveDialog("Save QCONF", folder, name, FileExtensions.newConfigFileExt);
        String selPath = sd.getDirectory();
        String selName = sd.getFileName();
        if (selPath != null && selName != null && !selPath.isEmpty() && !selName.isEmpty()) {
          opts.outputPath = Paths.get(selPath, selName).toString();
        }
      }
      // save
      try {
        n.save(opts.outputPath);
      } catch (FileNotFoundException e) {
        throw new QuimpPluginException(e);
      }

    }

    publishMacroString();
  }

  /**
   * Helper, show macro string if recorder is active.
   */
  private void publishMacroString() {
    // check whether config file name is provided or ask user for it
    BinarySegmentationOptions opts = (BinarySegmentationOptions) options;
    logger.debug("Internal options " + options.serialize2Macro());
    if (Recorder.record) {
      Recorder.setCommand("BinarySegmentation");
      Recorder.recordOption(AbstractPluginOptions.KEY, opts.serialize2Macro());
      Recorder.saveCommand();
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
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#runFromQconf()
   */
  @Override
  protected void runFromQconf() throws QuimpException {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.plugin.PluginTemplate#runFromPaqp()
   */
  @Override
  protected void runFromPaqp() throws QuimpException {
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
}