package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BoaException;
import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpException.MessageSinkTypes;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.geom.filters.HatSnakeFilter;
import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;
import com.github.celldynamics.quimp.plugin.PluginTemplate;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.binaryseg.BinarySegmentation;
import com.github.celldynamics.quimp.plugin.generatemask.GenerateMask_;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkModel.SeedSource;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ch.qos.logback.core.status.OnConsoleStatusListener;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.ContrastEnhancer;
import ij.plugin.Converter;
import ij.plugin.frame.Recorder;
import ij.plugin.tool.BrushTool;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;

/*
 * !>
 * 
 * @startuml doc-files/RandomWalkSegmentationPlugin_3_UML.png
 * actor "IJ plugin runner"
 * actor User
 * "IJ plugin runner" -> RandomWalkSegmentationPlugin_ : <<create>>
 * activate RandomWalkSegmentationPlugin_
 * RandomWalkSegmentationPlugin_ -> RandomWalkSegmentationPlugin_ : <<model>>
 * RandomWalkSegmentationPlugin_ -> RandomWalkSegmentationPlugin_ : <<view>>
 * RandomWalkSegmentationPlugin_ -> RandomWalkSegmentationPlugin_ : writeUI()
 * "IJ plugin runner" -> RandomWalkSegmentationPlugin_ : run()
 * RandomWalkSegmentationPlugin_ -> RandomWalkSegmentationPlugin_ :showUI(true)
 * ...
 * User -> Dialog : click Apply 
 * Dialog -> Dialog : readUI()
 * Dialog -> RWWorker : <<create>>
 * activate RWWorker
 * RWWorker -> Dialog : enableUI(false)
 * RWWorker -> Dialog : rename button
 * RWWorker -> RandomWalkSegmentationPlugin_ : runPlugin()
 * RandomWalkSegmentationPlugin_ --> IJ : update progress
 * RandomWalkSegmentationPlugin_ --> RWWorker : done
 * RWWorker -> Dialog : enableUI(true)
 * RWWorker -> Dialog : rename button
 * RWWorker --> Dialog : <<end>>
 * deactivate RWWorker
 * @enduml
 * !<
 */
/**
 * Run RandomWalkSegmentation in IJ environment.
 * 
 * <p>Implements common PlugIn interface as both images are provided after run. The seed can be one
 * image - in this case seed propagation is used to generate seed for subsequent frames, or it can
 * be stack of the same size as image. In latter case every slice from seed is used for seeding
 * related slice from image.
 * 
 * <p>Principles of working:<br>
 * <img src="doc-files/RandomWalkSegmentationPlugin_3_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPlugin_ extends PluginTemplate {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationPlugin_.class.getName());

  RandomWalkView view;
  private SeedPicker seedPickerWnd = null;

  private BrushTool br = new BrushTool();
  private String lastTool; // tool selected in IJ
  private boolean isCanceled; // true if user click Cancel, false if clicked Apply
  private boolean isRun; // true if segmentation is running
  private boolean oneSlice = false; // true if only current slice is segmented
  private int startSlice = 1; // number of slice to segment If oneSlice == false, segment all from 1

  /**
   * Default constructor.
   */
  public RandomWalkSegmentationPlugin_() {
    super(new RandomWalkModel());
    if (IJ.getInstance() != null) {
      lastTool = IJ.getToolName(); // remember selected tool
    }
    isCanceled = false;
    isRun = false;
    view = new RandomWalkView();
    seedPickerWnd = new SeedPicker(false);
    writeUI();
    view.addWindowController(new ActivateWindowController());
    view.addImageController(new ImageController());
    view.addSeedController(new SeedController());
    view.addRunController(new RunBtnController());
    view.addCancelController(new CancelBtnController());
    view.addBgController(new BgController());
    view.addFgController(new FgController());
    view.addCloneController(new CloneController());
    view.addLoadQconfController(new LoadQconfController());
    view.addQconfShowSeedImageController(new QconfShowSeedImageController());
    view.addRunActiveController(new RunActiveBtnController());
    view.addHelpController(new HelpBtnController());
    view.addSeedRoiController(new SeedRoiController());
    seedPickerWnd.addFinishController(new FinishControllerSeedPicker());
  }

  /**
   * Constructor that allows to provide own configuration parameters.
   * 
   * @param paramString parameter string.
   * @throws QuimpPluginException on error
   */
  public RandomWalkSegmentationPlugin_(String paramString) throws QuimpPluginException {
    super(paramString, new RandomWalkModel());
    view = new RandomWalkView();
    writeUI(); // fill UI controls with default options
  }

  /**
   * Updates view from model.
   */
  public void writeUI() {
    RandomWalkModel model = (RandomWalkModel) options;
    if (model.getOriginalImage() != null) {
      view.setCbOrginalImage(new String[] { model.getOriginalImage().getTitle() }, "");
    }

    view.setSeedSource(model.getSeedSources(), model.getSelectedSeedSource().name());
    if (model.getSeedImage() != null) {
      view.setCbRgbSeedImage(new String[] { model.getSeedImage().getTitle() }, "");
      view.setCbCreatedSeedImage(new String[] { model.getSeedImage().getTitle() }, "");
      view.setCbMaskSeedImage(new String[] { model.getSeedImage().getTitle() }, "");
    }

    view.setSrAlpha(model.algOptions.alpha);
    view.setSrBeta(model.algOptions.beta);
    view.setSrGamma0(model.algOptions.gamma[0]);
    view.setSrGamma1(model.algOptions.gamma[1]);
    view.setSrIter(model.algOptions.iter);
    view.setSrRelerr(model.algOptions.relim[0]);

    view.setShrinkMethod(model.getShrinkMethods(), model.getselectedShrinkMethod().name());
    view.setSrShrinkPower(model.shrinkPower);
    view.setSrExpandPower(model.expandPower);
    view.setSrScaleSigma(model.scaleSigma);
    view.setSrScaleMagn(model.scaleMagn);
    view.setSrScaleCurvDistDist(model.scaleCurvDistDist);
    view.setSrScaleEqNormalsDist(model.scaleEqNormalsDist);
    view.setFilteringMethod(model.getFilteringMethods(), model.getSelectedFilteringMethod().name());
    view.setChLocalMean(model.algOptions.useLocalMean);
    view.setSrLocalMeanWindow(model.algOptions.localMeanMaskSize);
    view.setChTrueBackground(model.estimateBackground);

    view.setChHatFilter(model.hatFilter);
    view.setSrAlev(model.alev);
    view.setSrNum(model.num);
    view.setSrWindow(model.window);
    view.setFilteringPostMethod(model.getFilteringMethods(),
            model.getSelectedFilteringPostMethod().name());
    view.setChMaskCut(model.algOptions.maskLimit);

    view.setChShowSeed(model.showPreview);
    view.setChShowPreview(model.showPreview);
    view.setChShowProbMaps(model.showProbMaps);

  }

  /**
   * Updates model from view.
   * 
   * @return updated model. It is reference of model stored in this class.
   */
  public RandomWalkModel readUI() {
    RandomWalkModel model = (RandomWalkModel) options;
    model.setOriginalImage(WindowManager.getImage(view.getCbOrginalImage()));
    model.setSelectedSeedSource(view.getSeedSource());
    switch (model.getSelectedSeedSource()) {
      case RGBImage:
        model.setSeedImage(WindowManager.getImage(view.getCbRgbSeedImage()));
        break;
      case CreatedImage:
        model.setSeedImage(WindowManager.getImage(view.getCbCreatedSeedImage()));
        break;
      case MaskImage:
        model.setSeedImage(WindowManager.getImage(view.getCbMaskSeedImage()));
        break;
      case QconfFile:
        break; // no control to display or read from for this case
      case Rois:
        break;
      default:
        throw new IllegalArgumentException("Unknown seed source");
    }

    model.algOptions.alpha = view.getSrAlpha();
    model.algOptions.beta = view.getSrBeta();
    model.algOptions.gamma[0] = view.getSrGamma0();
    model.algOptions.gamma[1] = view.getSrGamma1();
    model.algOptions.iter = view.getSrIter();
    model.algOptions.relim[0] = view.getSrRelerr();
    model.algOptions.relim[1] = model.algOptions.relim[0] * 10;

    model.setselectedShrinkMethod(view.getShrinkMethod());
    model.shrinkPower = view.getSrShrinkPower();
    model.expandPower = view.getSrExpandPower();
    model.scaleSigma = view.getSrScaleSigma();
    model.scaleMagn = view.getSrScaleMagn();
    model.scaleCurvDistDist = view.getSrScaleCurvDistDist();
    model.scaleEqNormalsDist = view.getSrScaleEqNormalsDist();
    model.setSelectedFilteringMethod(view.getFilteringMethod());
    model.algOptions.useLocalMean = view.getChLocalMean();
    model.algOptions.localMeanMaskSize = view.getSrLocalMeanWindow();
    model.estimateBackground = view.getChTrueBackground();

    model.hatFilter = view.getChHatFilter();
    model.alev = view.getSrAlev();
    model.num = view.getSrNum();
    model.window = view.getSrWindow();
    model.setSelectedFilteringPostMethod(view.getFilteringPostMethod());
    model.algOptions.maskLimit = view.getChMaskCut();

    model.showSeeds = view.getChShowSeed();
    model.showPreview = view.getChShowPreview();
    model.showProbMaps = view.getChShowProbMaps();

    return model;
  }

  /**
   * Build main dialog.
   * <br>
   * <img src="doc-files/RandomWalkSegmentationPlugin_1_UML.png"/><br>
   * State diagram <br>
   * <img src="doc-files/RandomWalkSegmentationPlugin_2_UML.png"/><br>
   * 
   * @param val
   * 
   */
  public void showUi(boolean val) {
    view.show();
  }

  /**
   * Action {@link OnConsoleStatusListener} {@link SeedPicker}.
   * 
   * <p>Fill
   * 
   * @author p.baniukiewicz
   *
   */
  class FinishControllerSeedPicker implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (seedPickerWnd == null) {
        return;
      }
      RandomWalkModel model = (RandomWalkModel) options;
      model.getOriginalImage().deleteRoi(); // just in case if ROI tool left something
      List<Seeds> rois = seedPickerWnd.seedsRoi;
      if (rois != null) {
        view.setLroiSeedsInfo("Objects: " + rois.get(0).get(SeedTypes.FOREGROUNDS).size()
                + " FG and " + rois.get(0).get(SeedTypes.BACKGROUND).size() + " BG");
      }
    }

  }

  /**
   * Updates list of images in selector on activation or deactivation of window.
   * 
   * @author p.baniukiewicz
   *
   */
  class ActivateWindowController implements WindowFocusListener {

    @Override
    public void windowGainedFocus(WindowEvent e) {
      action();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
      action();
    }

    private void action() {
      if (isRun == true) {
        return;
      }
      RandomWalkModel model = (RandomWalkModel) options;
      // list of open windows
      String[] images = WindowManager.getImageTitles();
      String selection = "";
      // try to find that stored in model in list of opened windows and select it
      if (model.getOriginalImage() != null) {
        selection = model.getOriginalImage().getTitle();
      }
      // select that found (if any, first position otherwise)
      view.setCbOrginalImage(images, selection);
      selection = "";
      // the same with seeds
      if (model.getSeedImage() != null) {
        selection = model.getSeedImage().getTitle();
      }
      // all seeds selectors share the same image in model
      view.setCbCreatedSeedImage(images, selection);
      view.setCbRgbSeedImage(images, selection);
      view.setCbMaskSeedImage(images, selection);

    }
  }

  /**
   * Detect change on image JComboBox, this change can be due to user action adding new images to
   * list by {@link ActivateWindowController}.
   * 
   * <p>Stores selected image in model.
   * 
   * @author p.baniukiewicz
   *
   */
  class ImageController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      RandomWalkModel model = (RandomWalkModel) options;
      model.setOriginalImage(WindowManager.getImage(view.getCbOrginalImage()));
    }
  }

  /**
   * Detect change on seeds selector, these change can be due to user action adding new images
   * to list by {@link ActivateWindowController}.
   * 
   * <p>Stores selected image in model.
   * 
   * @author p.baniukiewicz
   *
   */
  class SeedController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      RandomWalkModel model = (RandomWalkModel) options;
      SeedSource src = SeedSource.valueOf(model.getSeedSources()[view.getSeedSource()]);
      switch (src) {
        case RGBImage:
          model.setSeedImage(WindowManager.getImage(view.getCbRgbSeedImage()));
          break;
        case MaskImage:
          model.setSeedImage(WindowManager.getImage(view.getCbMaskSeedImage()));
          break;
        case CreatedImage:
          model.setSeedImage(WindowManager.getImage(view.getCbCreatedSeedImage()));
          break;
        case QconfFile:
          break;
        case Rois:
          break;
        default:
          throw new IllegalArgumentException("Unknown seed source");
      }
    }
  }

  /**
   * Handle Run button.
   * 
   * <p>Copy view status to model and start processing.
   * 
   * @author p.baniukiewicz
   *
   */
  class RunBtnController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      RandomWalkModel model = (RandomWalkModel) options;
      startSlice = 1;// segment from first
      oneSlice = false;
      readUI();
      RWWorker rww = new RWWorker();
      rww.execute();
      LOGGER.trace("model: " + model.toString());
    }
  }

  /**
   * Handle Help button.
   * 
   * <p>Open browser.
   * 
   * @author p.baniukiewicz
   *
   */
  class HelpBtnController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
      try {
        java.awt.Desktop.getDesktop().browse(new URI(url));
      } catch (Exception e1) {
        LOGGER.error("Could not open help: " + e1.getMessage(), e1);
      }
    }
  }

  /**
   * Handle Run button from active.
   * 
   * <p>Copy view status to model and start processing.
   * 
   * @author p.baniukiewicz
   *
   */
  class RunActiveBtnController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      RandomWalkModel model = (RandomWalkModel) options;
      ImagePlus orgImg = model.getOriginalImage();
      if (orgImg == null) {
        return;
      }
      readUI();
      startSlice = orgImg.getCurrentSlice();
      oneSlice = true;
      RWWorker rww = new RWWorker();
      rww.execute();
      LOGGER.trace("model: " + model.toString());

    }
  }

  /**
   * Handle Cancel button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class CancelBtnController implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      isCanceled = true;
      if (isRun == false) {
        view.getWnd().dispose();
      }

    }
  }

  /**
   * Handle Clone button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class CloneController implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      ImagePlus tmpImage = WindowManager.getImage(view.getCbOrginalImage());
      if (tmpImage == null) {
        return;
      }
      ImagePlus duplicatedImage;
      Object[] options = { "Whole stack", "Current slice", "Cancel" };
      int ret = JOptionPane.showOptionDialog(view.getWnd(),
              QuimpToolsCollection
                      .stringWrap("Do you want to duplicte the whole stack or only current slice?"),
              "Duplicate stack", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, options, null);
      switch (ret) {
        case 0: // stack
          duplicatedImage = tmpImage.duplicate();
          break;
        case 1: // slice
          duplicatedImage = new ImagePlus("",
                  tmpImage.getStack().getProcessor(tmpImage.getCurrentSlice()).duplicate());
          break;
        case 2: // cancel
        default: // closed window
          return;
      }
      duplicatedImage.show();
      new Converter().run("RGB Color");
      duplicatedImage.setTitle("SEED_" + tmpImage.getTitle());
      view.setCbCreatedSeedImage(WindowManager.getImageTitles(), duplicatedImage.getTitle());
    }
  }

  /**
   * Handle seed ROI button. Open {@link SeedPicker}.
   * 
   * @author p.baniukiewicz
   *
   */
  public class SeedRoiController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      ImagePlus tmpImage = WindowManager.getCurrentImage();
      if (tmpImage == null) {
        return;
      }
      if (seedPickerWnd != null) {
        seedPickerWnd.image = tmpImage;
        seedPickerWnd.reset();
        seedPickerWnd.setVisible(true);
      }
    }

  }

  /**
   * Clone whole image or selected slice. Helper
   * 
   * @param tmpImage image to clone
   * @return cloned image
   */
  private ImagePlus cloneImageAndAsk(ImagePlus tmpImage) {
    Object[] options = { "Whole stack", "Current slice", "Cancel" };
    int ret = JOptionPane.showOptionDialog(view.getWnd(),
            QuimpToolsCollection
                    .stringWrap("Do you want to duplicte the whole stack or only current slice?"),
            "Duplicate stack", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            options, null);
    ImagePlus duplicatedImage;
    switch (ret) {
      case 0: // stack
        duplicatedImage = tmpImage.duplicate();
        break;
      case 1: // slice
        duplicatedImage = new ImagePlus("",
                tmpImage.getStack().getProcessor(tmpImage.getCurrentSlice()).duplicate());
        break;
      case 2: // cancel
      default: // closed window
        return null;
    }
    return duplicatedImage;
  }

  /**
   * Handle FG button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class FgController implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (view.getBnFore().isSelected()) {
        IJ.setForegroundColor(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue());
        BrushTool.setBrushWidth(10); // set brush width
        br.run(""); // run macro
      } else {
        IJ.setTool(lastTool); // if unselected just switch off BrushTool selecting other tool
      }
    }
  }

  /**
   * Handle BG button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class BgController implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (view.getBnBack().isSelected()) {
        IJ.setForegroundColor(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue());
        BrushTool.setBrushWidth(10); // set brush width
        br.run(""); // run macro
      } else {
        IJ.setTool(lastTool); // if unselected just switch off BrushTool selecting other tool
      }
    }
  }

  /**
   * Handle Load qconf button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class LoadQconfController implements ActionListener {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      FileDialog od = new FileDialog(IJ.getInstance(), "Open Qconf file");
      od.setFile("*.QCONF");
      od.setDirectory(OpenDialog.getLastDirectory());
      od.setMultipleMode(false);
      od.setMode(FileDialog.LOAD);
      od.setVisible(true);
      if (od.getFile() == null) {
        IJ.log("Cancelled - exiting...");
        return;
      }
      String directory = od.getDirectory();
      String filename = od.getFile();
      RandomWalkModel model = (RandomWalkModel) options;
      model.qconfFile = Paths.get(directory, filename).toString();
      view.setLqconfFile(filename);
    }
  }

  /**
   * Handle show loaded qconf file mask button.
   * 
   * @author p.baniukiewicz
   *
   */
  public class QconfShowSeedImageController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent arg0) {
      RandomWalkModel model = (RandomWalkModel) options;
      if (model.qconfFile == null || model.qconfFile.isEmpty()
              || Paths.get(model.qconfFile).getFileName() == null) {
        return;
      }
      try {
        ImagePlus seedImage;
        // temporary load, it is repeated in runPlugin
        seedImage = new GenerateMask_("opts={paramFile:(" + model.qconfFile + "),binary:false}")
                .getRes();
        seedImage.setTitle("Mask-" + Paths.get(model.qconfFile).getFileName().toString() + ".tif");
        new ContrastEnhancer().stretchHistogram(seedImage, 0.35);
        seedImage.show();
      } catch (QuimpPluginException e) {
        LOGGER.debug("Can not load QCONF"); // not important, error handled in runPlugin
      }

    }

  }

  /**
   * Called on plugin run.
   * 
   * <p>Overrides {@link PluginTemplate#run(String)} to avoid loading QCONF file which is not used
   * here.
   * 
   * @see PluginTemplate
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
   * Helper, show macro string if recorder is active.
   */
  private void publishMacroString() {
    // check whether config file name is provided or ask user for it
    RandomWalkModel opts = (RandomWalkModel) options;
    logger.debug("Internal options " + options.serialize2Macro());
    if (Recorder.record) {
      Recorder.setCommand("RandomWalk");
      Recorder.recordOption(AbstractPluginOptions.KEY, opts.serialize2Macro());
      Recorder.saveCommand();
    }
  }

  /**
   * Run segmentation.
   * 
   * <p>Set {@link PluginTemplate#apiCall} (this.apiCall) to true and
   * {@link RandomWalkModel#showPreview} to false to block visual output.
   * 
   * @return Segmented image(s)
   * @see RandomWalkModel
   * @see RandomWalkSegmentationPlugin_#RandomWalkSegmentationPlugin_(String)
   */
  public ImagePlus runPlugin() {
    ImagePlus segmented = null; // result of segmentation
    RandomWalkModel model = (RandomWalkModel) options;
    ImagePlus prev = null; // preview window, null if not opened
    // local mean should not be applied for first slice if seeds are rgb - remember status here
    // to temporarily disable it and enable before processing second slice
    boolean useSeedStack; // true if seed has the same size as image, slices are seeds
    isCanceled = false; // erase any previous state
    Color foreColor; // color of seed image foreground pixels
    Color backColor; // color of seed image background pixels
    boolean localMeanUserStatus = model.algOptions.useLocalMean;
    ImageStack ret; // all images treated as stacks
    ImageStack is = null;
    Seeds seeds;
    PropagateSeeds propagateSeeds;
    isRun = true; // segmentation started
    // if preview selected - prepare image
    if (model.showPreview) {
      prev = new ImagePlus();
    }
    AutoThresholder.Method thresholdBackground = null;
    if (model.estimateBackground == true) {
      thresholdBackground = AutoThresholder.Method.Otsu;
    }
    try {
      ImagePlus image = model.getOriginalImage();
      ImagePlus seedImage;
      // find if we have stack seeds or not
      if (model.getSelectedSeedSource() == SeedSource.Rois) {
        seedImage = seedPickerWnd.image; // stored image in seedpicker (at SeedRoiController)
      } else {
        seedImage = model.getSeedImage(); // stored in model (rgb, mask)
      }
      if (image == null || seedImage == null) {
        throw new QuimpPluginException("Input image or seed image can not be opened.");
      }
      is = image.getStack(); // get current stack (size 1 for one image)
      if (seedImage.getStackSize() == 1) {
        useSeedStack = false; // use propagateSeed for generating next frame seed from prev
      } else if (seedImage.getStackSize() == image.getStackSize()) {
        useSeedStack = true; // use slices as seeds
      } else {
        throw new RandomWalkException("Seed stack and image stack must have the same z dimension");
      }
      // create seeding object with or without storing the history of configured type
      propagateSeeds = PropagateSeeds.getPropagator(model.selectedShrinkMethod, model.showSeeds,
              thresholdBackground);
      if (propagateSeeds instanceof PropagateSeeds.Contour) {
        ((PropagateSeeds.Contour) propagateSeeds).scaleMagn = model.scaleMagn;
        ((PropagateSeeds.Contour) propagateSeeds).scaleSigma = model.scaleSigma;
        ((PropagateSeeds.Contour) propagateSeeds).averageNormalsDist = model.scaleEqNormalsDist;
        ((PropagateSeeds.Contour) propagateSeeds).averageCurvDist = model.scaleCurvDistDist;
      }

      ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
      // create segmentation engine
      RandomWalkSegmentation obj =
              new RandomWalkSegmentation(is.getProcessor(startSlice), model.algOptions);
      // decode provided seeds depending on selected option
      switch (model.getSelectedSeedSource()) {
        case RGBImage: // use seeds as they are
        case CreatedImage:
          foreColor = Color.RED;
          backColor = Color.GREEN;
          if (seedImage != null && seedImage.equals(image)) {
            throw new RandomWalkException("Seed image and segmented image are the same.");
          }
          if (seedImage.getNSlices() >= startSlice) {
            seeds = SeedProcessor.decodeSeedsfromRgb(seedImage.getStack().getProcessor(startSlice),
                    Arrays.asList(foreColor), backColor);
          } else {
            seeds = SeedProcessor.decodeSeedsfromRgb(seedImage.getStack().getProcessor(1),
                    Arrays.asList(foreColor), backColor);
          }
          if (model.algOptions.useLocalMean) {
            LOGGER.warn("LocalMean is not used for first frame when seed is RGB image");
          }
          model.algOptions.useLocalMean = false; // do not use LM on first frame (reenable it later)
          break;
        case Rois:
          if (seedPickerWnd.seedsRoi.isEmpty()) {
            throw new RandomWalkException("No ROIs processed, did you forget to click Finish?");
          }
          seeds = seedPickerWnd.seedsRoi.get(startSlice - 1);
          if (model.algOptions.useLocalMean) {
            LOGGER.warn("LocalMean is not used for first frame when seeds are ROIs.");
          }
          model.algOptions.useLocalMean = false; // do not use LM on first frame (reenable it later)
          break;
        case QconfFile:
          seedImage = new GenerateMask_("opts={paramFile:(" + model.qconfFile + "),binary:false}")
                  .getRes(); // it throws in case
          // and continue to the next case
        case MaskImage:
          if (seedImage != null && seedImage.equals(image)) {
            throw new RandomWalkException("Seed image and segmented image are the same.");
          }
          double max = new StackStatistics(seedImage).max;
          if (max > 255) {
            LOGGER.warn("There are more than 255 objects in loaded QCONF file. Only first"
                    + " 255 will be segmented");
          }
          // get seeds split to FG and BG
          // this is mask (bigger) so produce seeds, overwrite seeds
          // do no scale here as seedImage is 16bit and it would remove some colors. Assume clipping
          seeds = propagateSeeds.propagateSeed(
                  seedImage.getStack().getProcessor(startSlice).convertToByte(false),
                  is.getProcessor(startSlice), model.shrinkPower, model.expandPower);
          // mask to local mean
          seeds.put(SeedTypes.ROUGHMASK,
                  seedImage.getStack().getProcessor(startSlice).convertToByte(false));
          seeds.get(SeedTypes.ROUGHMASK, 1).threshold(0); // to have BW map in case
          break;
        default:
          throw new IllegalArgumentException("Unsupported seed source");
      }
      // segment first slice (or image if it is not stack)
      ImageProcessor retIp = obj.run(seeds);
      if (retIp == null) { // segmentation failed, return empty image
        LOGGER.error("Segmentation failed - no Foreground maps provided"); // not very important
        retIp = new ByteProcessor(image.getWidth(), image.getHeight());
      }
      model.algOptions.useLocalMean = localMeanUserStatus; // restore status after 1st frame
      if (model.hatFilter) {
        retIp = applyHatSnakeFilter(retIp, is.getProcessor(startSlice));
      }
      ret.addSlice(retIp.convertToByte(true)); // store output in new stack
      if (model.showPreview) { // display first
                               // slice
        prev.setProcessor(retIp);
        prev.setTitle("Previev - frame: " + 1);
        prev.show();
        prev.updateAndDraw();
      }
      // iterate over all slices after first (may not run for one image and for current image seg)
      for (int s = 2; s <= is.getSize() && isCanceled == false && oneSlice == false; s++) {
        LOGGER.info("----- Slice " + s + " -----");
        Seeds nextseed = new Seeds(); // just to remove null warning
        obj = new RandomWalkSegmentation(is.getProcessor(s), model.algOptions);
        // get seeds from previous result
        if (useSeedStack) { // true - use slices
          switch (model.getSelectedSeedSource()) {
            case RGBImage:
            case Rois:
              // TODO add support for multislice seeds
              throw new RandomWalkException(
                      "This combination is not supported - for ROI seeds should be selected in "
                              + "single image, " + "not a stack");
            case QconfFile:
            case MaskImage:
              // do no scale here as seedImage is 16bit and it would remove some colors. Assume
              // clipping
              nextseed = propagateSeeds.propagateSeed(
                      seedImage.getStack().getProcessor(s).convertToByte(false), is.getProcessor(s),
                      model.shrinkPower, model.expandPower);
              nextseed.put(SeedTypes.ROUGHMASK,
                      seedImage.getStack().getProcessor(s).convertToByte(false)); // this makes copy
              nextseed.get(SeedTypes.ROUGHMASK, 1).threshold(0); // to have BW map in case
              break;
            default:
          }
        } else { // false - use previous frame
          // modify masks and convert to lists
          // retIp can be grayscale but it does no matter, return from propagateSeed is BW, each
          // object separated
          nextseed = propagateSeeds.propagateSeed(retIp, is.getProcessor(s), model.shrinkPower,
                  model.expandPower);
          nextseed.put(SeedTypes.ROUGHMASK, retIp.duplicate());
          nextseed.get(SeedTypes.ROUGHMASK, 1).threshold(0); // to have BW map in case
        }
        // segmentation and results stored for next seeding
        retIp = obj.run(nextseed);
        if (retIp == null) { // segmentation failed, return empty image
          LOGGER.error("Segmentation failed - no Foreground maps provided"); // not very important
          retIp = new ByteProcessor(image.getWidth(), image.getHeight());
        }
        if (model.hatFilter) {
          retIp = applyHatSnakeFilter(retIp, is.getProcessor(s));
        }
        ret.addSlice(retIp); // add next slice
        if (model.showPreview) { // show preview remaining slices
          prev.setProcessor(retIp);
          prev.setTitle("Previev - frame: " + s);
          prev.setActivated();
          prev.updateAndDraw();
        }
        IJ.showProgress(s - 1, is.getSize());
      }
      // convert to ImagePlus and show
      segmented = new ImagePlus("Segmented_" + image.getTitle(), ret);
      if (!apiCall) {
        segmented.show();
        segmented.updateAndDraw();
      }
      // show maps (last used - not stack!)
      if (model.showProbMaps == true) {
        ProbabilityMaps pm = obj.getProbabilityMaps();
        if (pm != null) { // if seg run
          ImageStack pmstackfg = pm.convertToImageStack(SeedTypes.FOREGROUNDS);
          if (pmstackfg == null) { // if not successful seg
            logger.warn("showProbMaps is selected but segmentation returned empty FG maps.");
          } else { // if successful seg
            ImageStack pmstackbg = pm.convertToImageStack(SeedTypes.BACKGROUND);
            // add bg if exists
            if (pmstackbg != null) {
              for (int s = 1; s <= pmstackbg.size(); s++) {
                pmstackfg.addSlice(pmstackbg.getProcessor(s));
              }
              new ImagePlus("Last Probability maps", pmstackfg).show();
            }
          }
        }
      }
      // show seeds if selected and not stack seeds (throw must be last
      if (model.showSeeds) {
        if (useSeedStack == true) {
          switch (model.getSelectedSeedSource()) {
            case QconfFile:
            case MaskImage:
              if (oneSlice) { // have stack but want only one slice
                propagateSeeds.getCompositeSeed(image.duplicate(), startSlice).show();
              } else { // have stack and segmented stack
                propagateSeeds.getCompositeSeed(image.duplicate(), 0).show();
              }
              break;
            default:
              LOGGER.warn("Effective seeds are not displayed if"
                      + " initial seeds are provided as stack");
          }
        } else {
          propagateSeeds.getCompositeSeed(image.duplicate(), 0).show();
        }
      }
    } catch (QuimpPluginException rwe) {
      // if (!(rwe instanceof RandomWalkException)) { // RandomWalkException has set proper sink
      rwe.setMessageSinkType(errorSink);
      // }
      rwe.handleException(view.getWnd(), "Segmentation problem.");
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
      IJ.error("Random Walk Segmentation error",
              e.getClass().getSimpleName() + ": " + e.getMessage());
    } finally {
      isRun = false; // segmentation stopped
      IJ.showProgress(2, 1); // erase progress bar
      if (prev != null) {
        prev.close();
      }
      model.algOptions.useLocalMean = localMeanUserStatus; // restore status
      publishMacroString();
    }
    return segmented;
  }

  /**
   * Helper method, applies HSF.
   * 
   * @param retIp image to filter (mask)
   * @param orIp original image
   * @return Filtered processor
   * @throws QuimpPluginException on problem with HatSnakeFilter
   */
  private ImageProcessor applyHatSnakeFilter(ImageProcessor retIp, ImageProcessor orIp)
          throws QuimpPluginException {
    RandomWalkModel model = (RandomWalkModel) options;
    BinarySegmentation obj = new BinarySegmentation(new ImagePlus("", retIp));
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains();
    for (ArrayList<SegmentedShapeRoi> asS : ret) {
      for (SegmentedShapeRoi ss : asS) {
        ss.setInterpolationParameters(1, false);
      }
    }
    SegmentedShapeRoi ssR = ret.get(0).get(0); // FIXME possible trap for multi object images
    HatSnakeFilter hsf = new HatSnakeFilter(model.window, model.num, model.alev);
    hsf.setMode(HatSnakeFilter.CAVITIES);
    // dont use interpolation - provide list of points as they are on image
    List<Point2d> retf = hsf.runPlugin(ssR.getOutlineasRawPoints(), orIp);
    Roi ssRF;
    try {
      ssRF = new QuimpDataConverter(retf).getSnake(0).asFloatRoi();
    } catch (BoaException e) { // lesss than 3 points
      e.setMessageSinkType(MessageSinkTypes.IJERROR);
      e.handleException(null, "HatSnake Filter failed");
      return retIp;
    }
    ImageProcessor retIptmp = new ByteProcessor(orIp.getWidth(), orIp.getHeight());
    retIptmp.setColor(Color.WHITE);
    retIptmp.fill(ssRF);
    return retIptmp;
  }

  /**
   * About string.
   * 
   * @return About string
   */
  public String about() {
    return "Random Walk plugin.\n" + "Author: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk\n"
            + "This plugin does not supports macro parameters\n";
  }

  /**
   * Swing worker class.
   * 
   * <p>Run segmentation and take care about renaming/blocking UI elements.
   * 
   * @author p.baniukiewicz
   *
   */
  class RWWorker extends SwingWorker<Object, Object> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Object doInBackground() throws Exception {
      view.setCancelLabel("STOP"); // use cancel to stopping
      view.enableUI(false);
      runPlugin(); // will update IJ progress bar
      view.enableUI(true);
      view.setCancelLabel("Cancel");
      return true;
    }

    @Override
    protected void done() {
      try {
        get();
      } catch (ExecutionException e) {
        LOGGER.error(e.getMessage());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        view.enableUI(true);
        view.setCancelLabel("Cancel");
      }
    }
  }

  @Override
  protected void runFromQconf() throws QuimpException {
  }

  @Override
  protected void runFromPaqp() throws QuimpException {
  }

}
