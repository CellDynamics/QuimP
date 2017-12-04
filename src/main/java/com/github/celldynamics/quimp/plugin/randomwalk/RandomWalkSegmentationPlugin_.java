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
import java.util.List;
import java.util.Map;

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
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.Converter;
import ij.plugin.frame.Recorder;
import ij.plugin.tool.BrushTool;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

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
    view.addRunActiveController(new RunActiveBtnController());
    view.addHelpController(new HelpBtnController());
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

    view.setSeedSource(model.seedSource);
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

    view.setShrinkMethod(model.getShrinkMethods(), model.getselectedShrinkMethod().name());
    view.setSrShrinkPower(model.shrinkPower);
    view.setSrExpandPower(model.expandPower);
    view.setFilteringMethod(model.getFilteringMethods(), model.getSelectedFilteringMethod().name());
    view.setChLocalMean(model.algOptions.useLocalMean);
    view.setSrLocalMeanWindow(model.algOptions.localMeanMaskSize);

    view.setChHatFilter(model.hatFilter);
    view.setSrAlev(model.alev);
    view.setSrNum(model.num);
    view.setSrWindow(model.window);
    view.setFilteringPostMethod(model.getFilteringMethods(),
            model.getSelectedFilteringPostMethod().name());
    view.setChMaskCut(model.algOptions.maskLimit);

    view.setChShowSeed(model.showPreview);
    view.setChShowPreview(model.showPreview);

  }

  /**
   * Updates model from view.
   * 
   * @return updated model. It is reference of model stored in this class.
   */
  public RandomWalkModel readUI() {
    RandomWalkModel model = (RandomWalkModel) options;
    model.setOriginalImage(WindowManager.getImage(view.getCbOrginalImage()));
    model.seedSource = view.getSeedSource();
    switch (model.seedSource) {
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
      default:
        throw new IllegalArgumentException("Unknown seed source");
    }

    model.algOptions.alpha = view.getSrAlpha();
    model.algOptions.beta = view.getSrBeta();
    model.algOptions.gamma[0] = view.getSrGamma0();
    model.algOptions.gamma[1] = view.getSrGamma1();
    model.algOptions.iter = view.getSrIter();

    model.setselectedShrinkMethod(view.getShrinkMethod());
    model.shrinkPower = view.getSrShrinkPower();
    model.expandPower = view.getSrExpandPower();
    model.setSelectedFilteringMethod(view.getFilteringMethod());
    model.algOptions.useLocalMean = view.getChLocalMean();
    model.algOptions.localMeanMaskSize = view.getSrLocalMeanWindow();

    model.hatFilter = view.getChHatFilter();
    model.alev = view.getSrAlev();
    model.num = view.getSrNum();
    model.window = view.getSrWindow();
    model.setSelectedFilteringPostMethod(view.getFilteringPostMethod());
    model.algOptions.maskLimit = view.getChMaskCut();

    model.showSeeds = view.getChShowSeed();
    model.showPreview = view.getChShowPreview();

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
   * Detect change on seeds JComboBoxs, these change can be due to user action adding new images
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
      switch (view.getSeedSource()) {
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
      Object[] options = { "Whole stack", "Current slice", "Cancel" };
      int ret = JOptionPane.showOptionDialog(view.getWnd(),
              QuimpToolsCollection
                      .stringWrap("Do you want to duplicte the whole stack or only current slice?"),
              "Duplicate stack", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, options, null);
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
          return;
      }
      duplicatedImage.show();
      new Converter().run("RGB Color");
      duplicatedImage.setTitle("SEED_" + tmpImage.getTitle());
      view.setCbCreatedSeedImage(WindowManager.getImageTitles(), duplicatedImage.getTitle());
    }
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
    Map<Seeds, ImageProcessor> seeds;
    PropagateSeeds propagateSeeds;
    isRun = true; // segmentation started
    try {
      ImagePlus image = model.getOriginalImage();
      ImagePlus seedImage = model.getSeedImage();
      if (image == null || seedImage == null) {
        throw new QuimpPluginException("Input image or seed image can not be opened.");
      }
      ImageStack is = image.getStack(); // get current stack (size 1 for one image)
      // if preview selected - prepare image
      if (model.showPreview) {
        prev = new ImagePlus();
      }

      if (seedImage.getStackSize() == 1) {
        useSeedStack = false; // use propagateSeed for generating next frame seed from prev
      } else if (seedImage.getStackSize() == image.getStackSize()) {
        useSeedStack = true; // use slices as seeds
      } else {
        throw new RandomWalkException("Seed stack and image stack must have the same z dimension");
      }
      // create seeding object with or without storing the history of configured type
      propagateSeeds = PropagateSeeds.getPropagator(model.selectedShrinkMethod, model.showSeeds);
      ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
      // create segmentation engine
      RandomWalkSegmentation obj =
              new RandomWalkSegmentation(is.getProcessor(startSlice), model.algOptions);
      // decode provided seeds depending on selected option
      switch (model.seedSource) {
        case RGBImage: // use seeds as they are
        case CreatedImage:
          foreColor = Color.RED;
          backColor = Color.GREEN;
          if (seedImage.getNSlices() >= startSlice) {
            seeds = RandomWalkSegmentation.decodeSeeds(
                    seedImage.getStack().getProcessor(startSlice), foreColor, backColor);
          } else {
            seeds = RandomWalkSegmentation.decodeSeeds(seedImage.getStack().getProcessor(1),
                    foreColor, backColor);
          }
          if (model.algOptions.useLocalMean) {
            LOGGER.warn("LocalMean is not used for first frame when seed is RGB image");
          }
          model.algOptions.useLocalMean = false; // do not use LM on first frame (reenable it later)
          break;
        case QconfFile:
          seedImage = new GenerateMask_("filename=[" + model.qconfFile + "]").getRes();
          if (seedImage == null) {
            throw new RandomWalkException("Mask image is not loaded");
          }
          // and continue to the next case
        case MaskImage:
          foreColor = Color.WHITE;
          backColor = Color.BLACK;
          new ImageConverter(seedImage).convertToRGB(); // convert to rgb
          // get seeds split to FG and BG
          Map<Seeds, ImageProcessor> seedsTmp = RandomWalkSegmentation
                  .decodeSeeds(seedImage.getStack().getProcessor(startSlice), foreColor, backColor);
          // this is mask (bigger) so produce seeds, overwrite seeds
          seeds = propagateSeeds.propagateSeed(seedsTmp.get(Seeds.FOREGROUND), model.shrinkPower,
                  model.expandPower);
          // mask to local mean
          seeds.put(Seeds.ROUGHMASK,
                  seedImage.getStack().getProcessor(startSlice).convertToByte(false));
          break;
        default:
          throw new IllegalArgumentException("Unsupported seed source");
      }
      // segment first slice (or image if it is not stack)
      ImageProcessor retIp = obj.run(seeds);
      model.algOptions.useLocalMean = localMeanUserStatus; // restore status after 1st frame
      if (model.hatFilter) {
        retIp = applyHatSnakeFilter(retIp, is.getProcessor(startSlice));
      }
      ret.addSlice(retIp.convertToByte(true)); // store output in new stack
      if (model.showPreview) { // display first slice
        prev.setProcessor(retIp);
        prev.setTitle("Previev - frame: " + 1);
        prev.show();
        prev.updateAndDraw();
      }
      // iterate over all slices after first (may not run for one image and for current image seg)
      for (int s = 2; s <= is.getSize() && isCanceled == false && oneSlice == false; s++) {
        Map<Seeds, ImageProcessor> nextseed;
        obj = new RandomWalkSegmentation(is.getProcessor(s), model.algOptions);
        // get seeds from previous result
        if (useSeedStack) { // true - use slices
          nextseed = RandomWalkSegmentation.decodeSeeds(seedImage.getStack().getProcessor(s),
                  foreColor, backColor);
          switch (model.seedSource) {
            case QconfFile:
            case MaskImage:
              nextseed = propagateSeeds.propagateSeed(nextseed.get(Seeds.FOREGROUND),
                      model.shrinkPower, model.expandPower);
              nextseed.put(Seeds.ROUGHMASK,
                      seedImage.getStack().getProcessor(s).convertToByte(false));
              break;
            default:
          }
        } else { // false - use previous frame
          // modify masks and convert to lists
          nextseed = propagateSeeds.propagateSeed(retIp, model.shrinkPower, model.expandPower);
          nextseed.put(Seeds.ROUGHMASK, retIp);
        }
        // segmentation and results stored for next seeding
        retIp = obj.run(nextseed);
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
      // show seeds if selected and not stack seeds
      if (model.showSeeds) {
        if (useSeedStack == true) {
          switch (model.seedSource) {
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
      rwe.handleException(view.getWnd(), "Segmentation problem");
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
      IJ.error("Random Walk Segmentation error", e.getMessage());
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
      return null;
    }
  }

  @Override
  protected void runFromQconf() throws QuimpException {
  }

  @Override
  protected void runFromPaqp() throws QuimpException {
  }

}
