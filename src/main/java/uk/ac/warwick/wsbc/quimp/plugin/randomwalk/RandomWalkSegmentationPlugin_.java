/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.Converter;
import ij.plugin.tool.BrushTool;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.PropertyReader;
import uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.quimp.plugin.ParamList;
import uk.ac.warwick.wsbc.quimp.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.quimp.registration.Registration;

/*
 * !>
 * @startuml doc-files/RandomWalkSegmentationPlugin_1_UML.png
 * salt
 *   {+
 *   Random Walk segmentation
 *   ~~
 *   {+
 *   Define images
 *   Open image:  |  ^Original image ^
 *   Open seed:   |  ^Seed image     ^
 *   }
 *   {+
 *   or create it:
 *   [  Clone  ] | [   FG   ] | [   BG   ]
 *   }
 *   ==
 *   {+
 *   Segmentation parameters
 *   alpha: | "400.0  "
 *   beta: | "50.0   "
 *   gamma: | "100.0  "
 *   iterations: | "80     "
 *   shrink power: | "5      "
 *   shrink algorithm: | ^OUTLINE^
 *   [] Show Seeds | [] Preview
 *   }
 *   ~~
 *   {
 *   [     OK     ] | [   Cancel   ]
 *   }
 *   }
 * @enduml
 * 
 * @startuml doc-files/RandomWalkSegmentationPlugin_2_UML.png
 *   [*] --> Default
 *   Default : selectors empty
 *   Default : **Clone**, **BG** and **FG** //inactive//
 *   Default --> ImageSelected : when **Image** selector not empty
 *   ImageSelected : **Clone** //active//
 *   Default --> SeedSelected : when **Seed** selector not empty and image tthere is valid
 *   SeedSelected : **Clone**, **BG** and **FG** //active//
 *   SeedSelected --> SeedCreation
 *   ImageSelected --> SeedCreation : Clicked **Clone**
 *   SeedCreation : Original image cloned and converted to RGB
 *   SeedCreation : **BG** and **FG** //active//
 *   SeedCreation : **SeedImage** selector filled with name of cloned image
 *   SeedCreation --> Sketch : **BG** or **FG** clicked
 *   Sketch : Draw tool selected in IJ
 *   Sketch : **BG** or **FG** changed to notify
 *   Default -> Run
 *   Run : Verify all fields
 *   Run : Run algorithm
 *   Run : Maintain UI state
 *   Sketch --> Run
 *   Sketch --> [*]
 *   SeedCreation --> Run
 *   SeedCreation --> [*]
 *   ImageSelected --> Run
 *   ImageSelected --> [*]
 *   Run --> [*]
 *   Default --> [*]
 * @enduml
 * 
 * @startuml doc-files/RandomWalkSegmentationPlugin_3_UML.png
 * actor "IJ plugin runner"
 * actor User
 * "IJ plugin runner" -> RandomWalkSegmentationPlugin_ : run()
 * RandomWalkSegmentationPlugin_ -> RandomWalkSegmentationPlugin_ :showUI(true)
 * ...
 * User -> Dialog : click Apply 
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
 * Implements common PlugIn interface as both images are provided after run. The seed can be one
 * image - in this case seed propagation is used to generate seed for subsequent frames, or it can
 * be stack of the same size as image. In latter case every slice from seed is used for seeding
 * related slice from image.
 * 
 * Principles of working:<br>
 * <img src="doc-files/RandomWalkSegmentationPlugin_3_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPlugin_ implements IQuimpPlugin, ActionListener, ChangeListener {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationPlugin_.class.getName());

  private ImagePlus image; // stack or image to segment.
  private ImagePlus seedImage; // RGB seed image
  private Params params; // All parameters
  private double shrinkPower; // Number of erosions for generating next seed from previous one, or
                              // number of pixels to shrink contour.
  private double expandPower; // Number of dilations for generating next seed from previous one,
                              // or number of pixels to expand contour.
  private boolean useSeedStack; // true if seed has the same size as image, slices are seeds

  private JComboBox<String> cImage, cSeed, cShrinkMethod;
  private JButton bClone;
  private JToggleButton bBack, bFore;
  private JSpinner sAlpha, sBeta, sGamma, sIter, sShrinkPower, sExpandPower;
  private JButton bCancel, bApply, bHelp;
  private BrushTool br = new BrushTool();
  private JCheckBox cShowSeed, cShowPreview;
  private String lastTool; // tool selected in IJ
  private boolean isCanceled; // true if user click Cancel, false if clicked Apply
  private boolean isRun; // true if segmentation is running

  /**
   * Define shrinking methods.
   * 
   * @see PropagateSeeds
   * @see #runPlugin()
   */
  private String shrinkMethods[] = new String[] { "OUTLINE", "MORPHO" };
  /**
   * Reference to underlying Frame object.
   */
  public JFrame wnd;

  /**
   * Default constructor
   */
  public RandomWalkSegmentationPlugin_() {
    lastTool = IJ.getToolName(); // remember selected tool
    isCanceled = false;
    isRun = false;
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
  public int showUI(boolean val) {
    wnd = new JFrame("Random Walker Segmentation");
    wnd.setResizable(false);
    JPanel panel = new JPanel(new BorderLayout());

    // Choices zone (upper)
    JPanel comboPanel = new JPanel();
    comboPanel.setBorder(BorderFactory.createTitledBorder("Image selection"));
    comboPanel.setLayout(new GridLayout(4, 1, 2, 2));
    cImage = new JComboBox<String>(WindowManager.getImageTitles());
    cImage.addActionListener(this);
    cSeed = new JComboBox<String>(WindowManager.getImageTitles());
    cSeed.addActionListener(this);
    comboPanel.add(new JLabel("Original image"));
    comboPanel.add(cImage);
    comboPanel.add(new JLabel("Seed image"));
    comboPanel.add(cSeed);

    // Seed build zone (middle)
    JPanel seedBuildPanels = new JPanel();
    seedBuildPanels.setBorder(BorderFactory.createTitledBorder("Seed build"));
    seedBuildPanels.setLayout(new BorderLayout());
    ((BorderLayout) seedBuildPanels.getLayout()).setVgap(2);
    // middle buttons
    JPanel seedBuildPanel = new JPanel();
    seedBuildPanel.setLayout(new GridLayout(1, 3, 2, 2));
    bClone = new JButton("Clone");
    bClone.setToolTipText("Clone selected original image and allow to seed it manually");
    bClone.addActionListener(this);
    bFore = new JToggleButton("FG");
    bFore.setToolTipText("Select Foreground pen");
    bFore.setBackground(Color.ORANGE);
    bFore.addActionListener(this);
    bBack = new JToggleButton("BG");
    bBack.setToolTipText("Select Background pen");
    bBack.setBackground(Color.GREEN);
    bBack.addActionListener(this);
    seedBuildPanel.add(bClone);
    seedBuildPanel.add(bFore);
    seedBuildPanel.add(bBack);
    seedBuildPanels.add(seedBuildPanel, BorderLayout.NORTH);
    // middle info area
    JTextPane helpArea = new JTextPane();
    helpArea.setContentType("text/html");
    helpArea.setEditable(false);
    //!>
        helpArea.setText("" + "<font size=\"3\">" + "<strong>Note</strong><br>"
                + "The seed image has always size of the segmented data."
                + "Thus, if one processes stack of images, he is "
                + "supposed to provide the stack of seeds as well. Otherwise, "
                + "one can cut (using ImageJ tools) only one slice and seed it, "
                + " then Random Walk plugin will generate seeds for subsequent "
                + "slices using <i>Shrink power</i> and <i>Expand power</i> parameters.<br>"
                + "<strong>Parameters</strong><br>"
                + "Good starting points:<br>"
                + "OUTLINE - <i>Shrink power</i>=10, <i>Expand power</i>=15<br>"
                + "MORPHO - <i>Shrink power</i>=3, <i>Expand power</i>=4<br>"
                + "</font>");
        //!<
    JScrollPane helpAreascroll = new JScrollPane(helpArea);
    helpAreascroll.setPreferredSize(new Dimension(200, 200));
    seedBuildPanels.add(helpAreascroll, BorderLayout.CENTER);

    // Options zone (middle)
    cShowSeed = new JCheckBox("Show seeds");
    cShowSeed.setSelected(false);
    cShowPreview = new JCheckBox("Show preview");
    cShowPreview.setSelected(false);
    cShrinkMethod = new JComboBox<String>(shrinkMethods);
    cShrinkMethod.addActionListener(this);
    JPanel optionsPanel = new JPanel();
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Segmentation options"));
    optionsPanel.setLayout(new GridLayout(8, 2, 2, 2));
    sAlpha = new JSpinner(new SpinnerNumberModel(400, 1, 100000, 1));
    sAlpha.addChangeListener(this);
    optionsPanel.add(new JLabel("Alpha"));
    optionsPanel.add(sAlpha);
    sBeta = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
    sBeta.addChangeListener(this);
    optionsPanel.add(new JLabel("Beta"));
    optionsPanel.add(sBeta);
    sGamma = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
    sGamma.addChangeListener(this);
    optionsPanel.add(new JLabel("Gamma"));
    optionsPanel.add(sGamma);
    sIter = new JSpinner(new SpinnerNumberModel(80, 1, 1000, 1));
    sIter.addChangeListener(this);
    optionsPanel.add(new JLabel("Iterations"));
    optionsPanel.add(sIter);
    sShrinkPower = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    sShrinkPower.addChangeListener(this);
    optionsPanel.add(new JLabel("Shrink power"));
    optionsPanel.add(sShrinkPower);
    sExpandPower = new JSpinner(new SpinnerNumberModel(15, 0, 1000, 1));
    sExpandPower.addChangeListener(this);
    optionsPanel.add(new JLabel("Expand power"));
    optionsPanel.add(sExpandPower);
    optionsPanel.add(new JLabel("Shrink method"));
    optionsPanel.add(cShrinkMethod);
    optionsPanel.add(cShowSeed);
    optionsPanel.add(cShowPreview);

    // integrate middle panels into one
    JPanel seedoptionsPanel = new JPanel();
    seedoptionsPanel.setLayout(new GridBagLayout()); // prevent equal sizes
    GridBagConstraints c = new GridBagConstraints();
    // c.gridheight = 1;
    // c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 50;
    c.fill = GridBagConstraints.HORIZONTAL;
    seedoptionsPanel.add(seedBuildPanels, c);
    // c.gridheight = 5;
    // c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    seedoptionsPanel.add(optionsPanel, c);

    // cancel apply row
    JPanel caButtons = new JPanel();
    caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    bApply = new JButton("Apply");
    bApply.addActionListener(this);
    bCancel = new JButton("Cancel");
    bCancel.addActionListener(this);
    bHelp = new JButton("Help");
    bHelp.addActionListener(this);
    caButtons.add(bApply);
    caButtons.add(bCancel);
    caButtons.add(bHelp);

    // build window
    panel.add(comboPanel, BorderLayout.NORTH);
    panel.add(seedoptionsPanel, BorderLayout.CENTER);
    // panel.add(optionsPanel,BorderLayout.SOUTH);
    panel.add(caButtons, BorderLayout.SOUTH);
    wnd.add(panel);

    // reaction on focus = all choices are rebuilt
    wnd.addWindowFocusListener(new WindowFocusListener() {

      @Override
      public void windowLostFocus(WindowEvent e) {
      }

      /**
       * Updates selector if user deleted the window
       */
      @Override
      public void windowGainedFocus(WindowEvent e) {
        if (isRun == true)
          return;
        Object sel = cSeed.getSelectedItem();
        cSeed.removeAllItems();
        for (String s : WindowManager.getImageTitles())
          cSeed.addItem(s);
        cSeed.setSelectedItem(sel);
        sel = cImage.getSelectedItem();
        cImage.removeAllItems();
        for (String s : WindowManager.getImageTitles())
          cImage.addItem(s);
        cImage.setSelectedItem(sel);
        uiLogic();
      }
    });
    wnd.pack();

    wnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    wnd.setVisible(val);
    return 0;
  }

  /**
   * Control status of FG, BG and Clone buttons.
   */
  private void uiLogic() {
    // disable on start only if there is no image
    if (cImage.getSelectedItem() == null) // if not null it must be string
      bClone.setEnabled(false);
    else
      bClone.setEnabled(true);
    if (cSeed.getSelectedItem() == null) {
      bFore.setEnabled(false);
      bBack.setEnabled(false);
    } else {
      bFore.setEnabled(true);
      bBack.setEnabled(true);
    }
  }

  /**
   * Set the same status for all UI elements except Cancel button.
   * 
   * @param status
   */
  private void enableUI(boolean status) {
    cImage.setEnabled(status);
    cSeed.setEnabled(status);
    cShrinkMethod.setEnabled(status);
    bClone.setEnabled(status);
    bBack.setEnabled(status);
    bFore.setEnabled(status);
    sAlpha.setEnabled(status);
    sBeta.setEnabled(status);
    sGamma.setEnabled(status);
    sIter.setEnabled(status);
    sShrinkPower.setEnabled(status);
    sExpandPower.setEnabled(status);
    bApply.setEnabled(status);
    bHelp.setEnabled(status);
    cShowSeed.setEnabled(status);
    cShowPreview.setEnabled(status);
  }

  /**
   * Plugin runner.
   * 
   * Shows UI and perform segmentation after validating UI
   */
  @Override
  public void run(String arg) {
    // validate registered user
    new Registration(IJ.getInstance(), "QuimP Registration");
    showUI(true);
  }

  /**
   * Run segmentation - fired from UI.
   */
  private void runPlugin() {
    ImageStack ret; // all images treated as stacks
    Map<Integer, List<Point>> seeds;
    PropagateSeeds propagateSeeds;
    ImagePlus prev = null; // preview window, null if not opened
    // create seeding object with or without storing the history of configured type
    switch ((String) cShrinkMethod.getSelectedItem()) {
      case "OUTLINE":
        propagateSeeds = new PropagateSeeds.Contour(cShowSeed.isSelected());
        break;
      case "MORPHO":
        propagateSeeds = new PropagateSeeds.Morphological(cShowSeed.isSelected());
        break;
      default:
        throw new IllegalArgumentException("Unsupported shrinking algorithm");
    }
    isRun = true; // segmentation started
    ImageStack is = image.getStack(); // get current stack (size 1 for one image)
    // if preview selected - prepare image
    if (cShowPreview.isSelected())
      prev = new ImagePlus();
    try {
      ret = new ImageStack(image.getWidth(), image.getHeight()); // output stack
      // segment first slice (or image if it is not stack)
      RandomWalkSegmentation obj = new RandomWalkSegmentation(is.getProcessor(1), params);
      seeds = obj.decodeSeeds(seedImage.getStack().getProcessor(1), Color.RED, Color.GREEN); // generate
                                                                                             // seeds
      ImageProcessor retIp = obj.run(seeds); // segmentation
      ret.addSlice(retIp.convertToByte(true)); // store output in new stack
      if (cShowPreview.isSelected()) { // display first slice
        prev.setProcessor(retIp);
        prev.setTitle("Previev - frame: " + 1);
        prev.show();
        prev.updateAndDraw();
      }
      // iterate over all slices after first (may not run for one image)
      for (int s = 2; s <= is.getSize() && isCanceled == false; s++) {
        Map<Integer, List<Point>> nextseed;
        obj = new RandomWalkSegmentation(is.getProcessor(s), params);
        // get seeds from previous result
        if (useSeedStack) { // true - use slices
          nextseed = obj.decodeSeeds(seedImage.getStack().getProcessor(s), Color.RED, Color.GREEN);
          retIp = obj.run(nextseed); // segmentation and results stored for next seeding
        } else {// false - use previous frame
          // convert unmodified masks to List
          ImageProcessor retIPinverted = retIp.duplicate();
          retIPinverted.invert();
          double[] meanSeed = obj.getMeanSeed(propagateSeeds.convertToList(retIp, retIPinverted));
          // modify masks and convert to lists
          nextseed = propagateSeeds.propagateSeed(retIp, shrinkPower, expandPower);
          retIp = obj.run(nextseed, meanSeed); // segmentation and results
                                               // stored for next seeding
        }
        ret.addSlice(retIp); // add next slice
        if (cShowPreview.isSelected()) { // show preview remaining slices
          prev.setProcessor(retIp);
          prev.setTitle("Previev - frame: " + s);
          prev.setActivated();
          prev.updateAndDraw();
        }
        IJ.showProgress(s - 1, is.getSize());
      }
      // convert to ImagePlus and show
      ImagePlus segmented = new ImagePlus("Segmented_" + image.getTitle(), ret);
      segmented.show();
      segmented.updateAndDraw();
      // show seeds if selected and not stack seeds
      if (cShowSeed.isSelected()) {
        if (useSeedStack == true)
          LOGGER.warn(
                  "Effective seeds are not displayed if" + " initial seeds are provided as stack");
        else
          propagateSeeds.getCompositeSeed(image.duplicate()).show();
      }
    } catch (RandomWalkException rwe) {
      rwe.handleException(wnd, "Segmentation problem:");
    } catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);
      LOGGER.error("Random Walk Segmentation error: " + e.getMessage(), e);
    } finally {
      isRun = false; // segmentation stopped
      IJ.showProgress(is.getSize() + 1, is.getSize()); // erase progress bar
      if (prev != null)
        prev.close();
    }
  }

  /**
   * Verify all entries in window and set them as final assigning to object variables. All
   * operations are here. Performs also some window logic
   * 
   * @param e Component
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object b = e.getSource();
    // enable disable controls depending on selectors (see diagram)
    if (b == cImage || b == cSeed) {
      uiLogic();
    }
    // Start data verification, show message on problem and exit method setting FGBG unselected
    // 0. check if we can paint on selected image if user try
    if (b == bFore || b == bBack) {
      ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem());
      if (tmpSeed == null)
        return;
      if (tmpSeed.getBitDepth() != 24) {
        JOptionPane.showMessageDialog(wnd, "Seed image must be 24 bit RGB type", "Error",
                JOptionPane.ERROR_MESSAGE);
        bFore.setSelected(false);
        bBack.setSelected(false);
        return; // we cant - return
      }
    }
    if (b == bFore) { // foreground pressed
      if (((JToggleButton) b).isSelected()) { // if selected
        bBack.setSelected(false); // unselect background
        IJ.setForegroundColor(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue()); // set
                                                                                              // pen
                                                                                              // color
        BrushTool.setBrushWidth(10); // set brush width
        br.run(""); // tun macro
      } else {
        IJ.setTool(lastTool); // if unselected just switch off BrushTool selecting other
                              // tool
      }
    }
    if (b == bBack) { // see bFore comments
      if (((JToggleButton) b).isSelected()) {
        bFore.setSelected(false);
        IJ.setForegroundColor(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue());
        BrushTool.setBrushWidth(10);
        br.run("");
      } else {
        IJ.setTool(lastTool);
      }
    }
    if (b == bApply) {
      isCanceled = false; // run
      // verify data before - store data in object after verification
      ImagePlus tmpSeed = WindowManager.getImage((String) cSeed.getSelectedItem()); // tmp var
      ImagePlus tmpImage = WindowManager.getImage((String) cImage.getSelectedItem());
      if (tmpSeed == null || tmpImage == null) {
        JOptionPane.showMessageDialog(wnd, "No image selected", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      // 1. Verify sizes - must be the same
      if (tmpSeed.getWidth() != tmpImage.getWidth()
              && tmpSeed.getHeight() != tmpImage.getHeight()) {
        JOptionPane.showMessageDialog(wnd, "Images have incompatibile sizes", "Error",
                JOptionPane.ERROR_MESSAGE);
        return; // when wrong sizes
      }
      // 2. Check seed bitDepth
      if (tmpSeed.getBitDepth() != 24) {
        JOptionPane.showMessageDialog(wnd, "Seed image must be 24 bit RGB type", "Error",
                JOptionPane.ERROR_MESSAGE);
        return; // when no 24 bit depth
      }
      // 3. Check stack size compatibility
      if (tmpSeed.getStackSize() == 1)
        useSeedStack = false; // use propagateSeed for generating next frame seed from prev
      else if (tmpSeed.getStackSize() == tmpImage.getStackSize())
        useSeedStack = true; // use slices as seeds
      else {
        JOptionPane.showMessageDialog(wnd, "Seed must be image or stack of the same size as image",
                "Error", JOptionPane.ERROR_MESSAGE);
        return; // wrong seed size
      }
      // 4. Read numeric data and other params
      //!>
            params = new Params((Integer) sAlpha.getValue(), // alpha
                    (Integer) sBeta.getValue(), // beta
                    (Integer) sGamma.getValue(), // gamma1
                    0, // not used gamma 2
                    (Integer) sIter.getValue(), // iterations
                    0.1, // dt
                    8e-3 // error
            );
            //!<
      shrinkPower = ((Integer) sShrinkPower.getValue()).doubleValue(); // shrinking object
      expandPower = ((Integer) sExpandPower.getValue()).doubleValue(); // expanding to get
                                                                       // background
      // all ok - store images to later use
      image = tmpImage;
      seedImage = tmpSeed;
      RWWorker rww = new RWWorker();
      rww.execute();
      // decelect seeds buttons
      bBack.setSelected(false);
      bFore.setSelected(false);
    } // end Apply
    if (b == bClone) {
      // clone seed image, convert it to RGB, add to list and select it on it
      ImagePlus tmpImage = WindowManager.getImage((String) cImage.getSelectedItem());
      ImagePlus duplicatedImage = tmpImage.duplicate();
      duplicatedImage.show();
      new Converter().run("RGB Color");
      duplicatedImage.setTitle("SEED_" + tmpImage.getTitle());

      cSeed.addItem(duplicatedImage.getTitle());
      cSeed.setSelectedItem(duplicatedImage.getTitle());
    }
    if (b == bHelp) {
      String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
      try {
        java.awt.Desktop.getDesktop().browse(new URI(url));
      } catch (Exception e1) {
        LOGGER.debug(e1.getMessage(), e1);
        LOGGER.error("Could not open help: " + e1.getMessage(), e1);
      }
    }
    if (b == bCancel) {
      isCanceled = true;
      if (isRun == false)
        wnd.dispose(); // Close window but only when segmentation is not run. Otherwise only
                       // set isCanceled to false
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    LOGGER.debug("State changed");
  }

  @Override
  public int setup() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setPluginConfig(ParamList par) throws QuimpPluginException {
    // TODO Auto-generated method stub

  }

  @Override
  public ParamList getPluginConfig() {
    // TODO Auto-generated method stub
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
   * Run segmentation and take care about renaming/blocking UI elements.
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
      bCancel.setText("STOP"); // use cancel to stopping
      enableUI(false);
      runPlugin(); // will update IJ progress bar
      enableUI(true);
      bCancel.setText("Cancel");
      return null;
    }
  }

}
