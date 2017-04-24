package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.Converter;
import ij.plugin.tool.BrushTool;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.BoaException;
import uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.quimp.geom.filters.HatSnakeFilter;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentation;
import uk.ac.warwick.wsbc.quimp.plugin.generatemask.GenerateMask_;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;
import uk.ac.warwick.wsbc.quimp.registration.Registration;
import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

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
public class RandomWalkSegmentationPlugin_ implements IQuimpPlugin {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationPlugin_.class.getName());

  RandomWalkModel model;
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
    if (IJ.getInstance() != null) {
      lastTool = IJ.getToolName(); // remember selected tool
    }
    isCanceled = false;
    isRun = false;
    model = new RandomWalkModel();
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
  }

  /**
   * Updates view from model.
   */
  public void writeUI() {
    if (model.originalImage != null) {
      view.setCbOrginalImage(new String[] { model.originalImage.getTitle() }, "");
    }

    view.setSeedSource(model.seedSource);
    if (model.seedImage != null) {
      view.setCbRgbSeedImage(new String[] { model.seedImage.getTitle() }, "");
      view.setCbCreatedSeedImage(new String[] { model.seedImage.getTitle() }, "");
      view.setCbMaskSeedImage(new String[] { model.seedImage.getTitle() }, "");
    }

    view.setSrAlpha(model.params.alpha);
    view.setSrBeta(model.params.beta);
    view.setSrGamma0(model.params.gamma[0]);
    view.setSrGamma1(model.params.gamma[1]);
    view.setSrIter(model.params.iter);

    view.setShrinkMethod(model.getShrinkMethods(), model.getselectedShrinkMethod().name());
    view.setSrShrinkPower(model.shrinkPower);
    view.setSrExpandPower(model.expandPower);
    view.setFilteringMethod(model.getFilteringMethods(), model.getSelectedFilteringMethod().name());
    view.setChLocalMean(model.params.useLocalMean);
    view.setSrLocalMeanWindow(model.params.localMeanMaskSize);

    view.setChHatFilter(model.hatFilter);
    view.setSrAlev(model.alev);
    view.setSrNum(model.num);
    view.setSrWindow(model.window);
    view.setFilteringPostMethod(model.getFilteringMethods(),
            model.getSelectedFilteringPostMethod().name());

    view.setChShowSeed(model.showPreview);
    view.setChShowPreview(model.showPreview);

  }

  /**
   * Updates model from view.
   * 
   * @return updated model. It is reference of model stored in this class.
   */
  public RandomWalkModel readUI() {
    model.originalImage = WindowManager.getImage(view.getCbOrginalImage());
    model.seedSource = view.getSeedSource();
    switch (model.seedSource) {
      case RGBImage:
        model.seedImage = WindowManager.getImage(view.getCbRgbSeedImage());
        break;
      case CreatedImage:
        model.seedImage = WindowManager.getImage(view.getCbCreatedSeedImage());
        break;
      case MaskImage:
        model.seedImage = WindowManager.getImage(view.getCbMaskSeedImage());
        break;
      case QconfFile:
        break; // no control to display or read from for this case
      default:
        throw new IllegalArgumentException("Unknown seed source");
    }

    model.params.alpha = view.getSrAlpha();
    model.params.beta = view.getSrBeta();
    model.params.gamma[0] = view.getSrGamma0();
    model.params.gamma[1] = view.getSrGamma1();
    model.params.iter = view.getSrIter();

    model.setselectedShrinkMethod(view.getShrinkMethod());
    model.shrinkPower = view.getSrShrinkPower();
    model.expandPower = view.getSrExpandPower();
    model.setSelectedFilteringMethod(view.getFilteringMethod());
    model.params.useLocalMean = view.getChLocalMean();
    model.params.localMeanMaskSize = view.getSrLocalMeanWindow();

    model.hatFilter = view.getChHatFilter();
    model.alev = view.getSrAlev();
    model.num = view.getSrNum();
    model.window = view.getSrWindow();
    model.setSelectedFilteringPostMethod(view.getFilteringPostMethod());

    model.showSeeds = view.getChShowSeed();
    model.showPreview = view.getChShowPreview();

    return model;
  }

  /**
   * Build main dialog<br>
   * <img src="doc-files/RandomWalkSegmentationPlugin_1_UML.png"/><br>
   * State diagram <br>
   * <img src="doc-files/RandomWalkSegmentationPlugin_2_UML.png"/><br>
   * 
   * @return always 0, not used here.
   */
  @Override
  public int showUi(boolean val) {
    view.show();
    return 0;
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
      // list of open windows
      String[] images = WindowManager.getImageTitles();
      String selection = "";
      // try to find that stored in model in list of opened windows and select it
      if (model.originalImage != null) {
        selection = model.originalImage.getTitle();
      }
      // select that found (if any, first position otherwise)
      view.setCbOrginalImage(images, selection);
      selection = "";
      // the same with seeds
      if (model.seedImage != null) {
        selection = model.seedImage.getTitle();
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
      model.originalImage = WindowManager.getImage(view.getCbOrginalImage());
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
      switch (view.getSeedSource()) {
        case RGBImage:
          model.seedImage = WindowManager.getImage(view.getCbRgbSeedImage());
          break;
        case MaskImage:
          model.seedImage = WindowManager.getImage(view.getCbMaskSeedImage());
          break;
        case CreatedImage:
          model.seedImage = WindowManager.getImage(view.getCbCreatedSeedImage());
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
      startSlice = 1;// segment from first
      oneSlice = false;
      readUI();
      RWWorker rww = new RWWorker();
      rww.execute();
      LOGGER.trace("model: " + model.toString());

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
      readUI();
      startSlice = model.originalImage.getCurrentSlice();
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
      model.qconfFile = Paths.get(directory, filename).toString();
      view.setLqconfFile(filename);
    }
  }

  /**
   * Plugin runner.
   * 
   * <p>Shows UI and perform segmentation after validating UI
   * 
   * @param arg support only false to not show ui
   */
  @Override
  public void run(String arg) {
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    if (arg != null && arg.equals("false")) {
      showUi(false);
    } else {
      showUi(true);
    }
  }

  /**
   * Run segmentation.
   * TODO Should be available to run this method from code. segmented cannot be shown from this code
   * 
   * @return Segmented image(s)
   * @see RandomWalkModel
   */
  public ImagePlus runPlugin() {
    boolean useSeedStack; // true if seed has the same size as image, slices are seeds
    ImagePlus prev = null; // preview window, null if not opened
    isCanceled = false; // erase any previous state
    Color foreColor; // color of seed image foreground pixels
    Color backColor; // color of seed image background pixels
    ImagePlus image = model.originalImage;
    ImagePlus seedImage = model.seedImage;
    ImagePlus segmented = null; // result of segmentation
    ImageStack ret; // all images treated as stacks
    Map<Seeds, ImageProcessor> seeds;
    PropagateSeeds propagateSeeds;
    isRun = true; // segmentation started
    ImageStack is = image.getStack(); // get current stack (size 1 for one image)
    // local mean should not be applied for first slice if seeds are rgb - remember status here
    // to temporarily disable it and enable before processing second slice
    boolean localMeanUserStatus = model.params.useLocalMean;
    // if preview selected - prepare image
    if (model.showPreview) {
      prev = new ImagePlus();
    }
    try {
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
              new RandomWalkSegmentation(is.getProcessor(startSlice), model.params);
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
          if (model.params.useLocalMean) {
            LOGGER.warn("LocalMean is not used for first frame when seeds are scribbled images");
          }
          model.params.useLocalMean = false; // do not use LM on first frame (reenable it later)
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
      model.params.useLocalMean = localMeanUserStatus; // restore status after 1st frame
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
        obj = new RandomWalkSegmentation(is.getProcessor(s), model.params);
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
      segmented.show();
      segmented.updateAndDraw();
      // show seeds if selected and not stack seeds
      if (model.showSeeds) {
        if (useSeedStack == true) {
          switch (model.seedSource) {
            case QconfFile:
            case MaskImage:
              propagateSeeds.getCompositeSeed(image.duplicate()).show();
              break;
            default:
              LOGGER.warn("Effective seeds are not displayed if"
                      + " initial seeds are provided as stack");
          }
        } else {
          propagateSeeds.getCompositeSeed(image.duplicate()).show();
        }
      }
    } catch (BoaException | QuimpPluginException rwe) {
      rwe.handleException(view.getWnd(), "Segmentation problem:");
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Random Walk Segmentation error: " + e.getMessage(), e);
    } finally {
      isRun = false; // segmentation stopped
      IJ.showProgress(is.getSize() + 1, is.getSize()); // erase progress bar
      if (prev != null) {
        prev.close();
      }
      model.params.useLocalMean = localMeanUserStatus; // restore status
    }
    return segmented;
  }

  /**
   * Helper method, applies HSF.
   * 
   * @param retIp image to filter (mask)
   * @param orIp original image
   * @return Filtered processor
   * @throws BoaException Thrown when HatSnakeFilter can not shrink contour
   * @throws QuimpPluginException on problem with HatSnakeFilter
   */
  private ImageProcessor applyHatSnakeFilter(ImageProcessor retIp, ImageProcessor orIp)
          throws QuimpPluginException, BoaException {
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
    List<Point2d> retf = hsf.runPlugin(ssR.getOutlineasPoints(), orIp);
    Roi ssRF = new QuimpDataConverter(retf).getSnake(0).asFloatRoi();
    ImageProcessor retIptmp = new ByteProcessor(orIp.getWidth(), orIp.getHeight());
    retIptmp.setColor(Color.WHITE);
    retIptmp.fill(ssRF);
    return retIptmp;
  }

  @Override
  public int setup() {
    return 0;
  }

  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
  }

  @Override
  public ParamList getPluginConfig() {
    return null;
  }

  @Override
  public String getVersion() {
    return "See QuimP version";
  }

  @Override
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

}
