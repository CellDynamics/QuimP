package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowFocusListener;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkModel.SeedSource;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;
import com.github.celldynamics.quimp.utils.UiTools;

/*
 * !>
 * @startuml
 * salt
 *   {+
 *   Random Walk segmentation
 *   ~~
 *   {+
 *   Image selection
 *   ^cbOriginalImage  ^ 
 *   }
 *   {+
 *   Generate seeds from
 *   (X) rbRgbImage | () rbCreateImage
 *   () rbMaskImage | () rbQcinfFile
 *   }
 *   {+
 *   Seeds from RGB image
 *   ^cbRgbSeedImage  ^
 *   }
 *   {+
 *   Seed build
 *   [bnClone] | [bnFore] | [bnBack]
 *   ^cbCreatedSeedImage^
 *   }
 *   {+
 *   Seeds from mask
 *   ^cbMaskSeedImage  ^
 *   }
 *   {+
 *   Seeds from QCONF
 *   [bnQconfSeedImage]
 *   lbQconfFile
 *   }
 *   {+
 *   Segmentation options
 *   Alpha | "srAlpha"
 *   Beta | "srBeta"
 *   Gamma 0 | "srGamma0"
 *   Gamma 1 | "srGamma1"
 *   Iterations | "srIter"
 *   Rel error | "srRelerr"
 *   }
 *   {+
 *   Inter-process
 *   Shrink method | ^cbShrinkMethod^ 
 *   Shrink power | "srShrinkPower" 
 *   Expand power | "srExpandPower" 
 *   Sigma | "srScaleSigma" | Magn | "srScaleMagn"
 *   | | Norm Dist | "srScaleEqNormalsDist"
 *   Binary filter | ^cbFilteringMethod^
 *   () chTrueBackground
 *   {+
 *   Use local mean
 *   () chLocalMean
 *   Window | "srLocalMeanWindow"
 *   }
 *   }
 *   {+
 *   Post-process
 *   {+
 *   Hat filter
 *   () chHatFilter
 *   Alev | "srAlev"
 *   Num | "srNum"
 *   Window | "srWindow"
 *   }
 *   Binary filter | ^cbFilteringPostMethod^
 *   }
 *   () chMaskCut
 *   {+
 *   Display options
 *   () chShowSeed | () chShowPreview
 *   }
 *   {
 *   [     OK     ] | [   Cancel   ] | [Help ]
 *   }
 *   }
 * @enduml
 * !<
 */
/**
 * UI for Random Walk algorithm.
 * 
 * <p>This is view only. It contains UI definition and handles events related to its internal state.
 * All other events are handled in controller.
 * 
 * @author p.baniukiewicz
 */
public class RandomWalkView implements ActionListener, ItemListener {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkView.class.getName());

  /**
   * Reference to underlying Frame object.
   */
  private JFrame wnd;

  /**
   * Get window reference.
   * 
   * @return the wnd
   */
  public JFrame getWnd() {
    return wnd;
  }

  /**
   * Main window panel.
   */
  private JPanel panel;

  private JPanel panelMain;

  /**
   * Original image selector.
   */
  private JComboBox<String> cbOrginalImage;

  /**
   * Get name of selected original image.
   * 
   * @return the cbOrginalImage
   */
  public String getCbOrginalImage() {
    return (String) cbOrginalImage.getSelectedItem();
  }

  /**
   * Set names of original images and select one.
   * 
   * @param item list of names of images
   * @param sel index of that to select
   * 
   */
  public void setCbOrginalImage(String[] item, String sel) {
    cbOrginalImage.removeAllItems();
    setJComboBox(cbOrginalImage, item, sel);
  }

  private ButtonGroup seedSource = new ButtonGroup();

  /**
   * Get selected seed source.
   * 
   * @return selected seed source
   */
  public SeedSource getSeedSource() {
    return SeedSource.valueOf(seedSource.getSelection().getActionCommand());
  }

  /**
   * Set seed source i UI.
   * 
   * @param val source to set.
   */
  public void setSeedSource(SeedSource val) {
    Enumeration<AbstractButton> elements = seedSource.getElements();
    while (elements.hasMoreElements()) {
      AbstractButton button = (AbstractButton) elements.nextElement();
      if (button.getActionCommand().equals(val.toString())) {
        button.setSelected(true);
      }
    }
  }

  private JRadioButton rbRgbImage;
  private JRadioButton rbCreateImage;
  private JRadioButton rbMaskImage;

  private JRadioButton rbQcofFile;
  private JLabel lbQconfFile;

  /**
   * Set name of loaded qconf.
   * 
   * @param lab name to set
   */
  public void setLqconfFile(String lab) {
    lbQconfFile.setFont(new Font(lbQconfFile.getFont().getName(), Font.ITALIC,
            lbQconfFile.getFont().getSize()));
    lbQconfFile.setText(lab);
  }

  private JComboBox<String> cbRgbSeedImage;

  /**
   * Get seed image selected in dynamic rgb panel.
   * 
   * @return the cbRgbSeedImage
   */
  public String getCbRgbSeedImage() {
    return (String) cbRgbSeedImage.getSelectedItem();
  }

  /**
   * Set seed image in dynamic rgb panel.
   * 
   * @param item list of names of images
   * @param sel name of that to select
   */
  public void setCbRgbSeedImage(String[] item, String sel) {
    cbRgbSeedImage.removeAllItems();
    setJComboBox(cbRgbSeedImage, item, sel);
  }

  private JComboBox<String> cbMaskSeedImage;

  /**
   * Get seed image selected in dynamic binary mask panel.
   * 
   * @return the cbMaskSeedImage
   */
  public String getCbMaskSeedImage() {
    return (String) cbMaskSeedImage.getSelectedItem();
  }

  /**
   * Set seed image in dynamic binary mask panel.
   * 
   * @param item list of names of images
   * @param sel name of that to select
   */
  public void setCbMaskSeedImage(String[] item, String sel) {
    cbMaskSeedImage.removeAllItems();
    setJComboBox(cbMaskSeedImage, item, sel);
  }

  private JButton bnQconfSeedImage;

  private JButton bnClone;
  private JToggleButton bnFore;

  /**
   * Get FG button ref.
   * 
   * @return the bnFore
   */
  public JToggleButton getBnFore() {
    return bnFore;
  }

  private JToggleButton bnBack;

  /**
   * Get BG button ref.
   * 
   * @return the bnBack
   */
  public JToggleButton getBnBack() {
    return bnBack;
  }

  private JComboBox<String> cbCreatedSeedImage;

  /**
   * Get seed image selected in dynamic created mask panel.
   * 
   * @return the cbCreatedSeedImage
   */
  public String getCbCreatedSeedImage() {
    return (String) cbCreatedSeedImage.getSelectedItem();
  }

  /**
   * Set seed image in dynamic created mask panel.
   * 
   * @param item list of names of images
   * @param sel name of that to select
   */
  public void setCbCreatedSeedImage(String[] item, String sel) {
    cbCreatedSeedImage.removeAllItems();
    setJComboBox(cbCreatedSeedImage, item, sel);
  }

  // optionsPanel
  private JSpinner srAlpha;

  /**
   * Get RW alpha parameter.
   * 
   * @return the srAlpha
   */
  public double getSrAlpha() {
    return ((Number) srAlpha.getValue()).doubleValue();
  }

  /**
   * Set RW alpha parameter.
   * 
   * @param srAlpha the srAlpha to set
   */
  public void setSrAlpha(double srAlpha) {
    this.srAlpha.setValue(srAlpha);
  }

  private JSpinner srBeta;

  /**
   * Get RW beta parameter.
   * 
   * @return the srBeta
   */
  public double getSrBeta() {
    return ((Number) srBeta.getValue()).doubleValue();
  }

  /**
   * Set RW beta parameter.
   * 
   * @param srBeta the srBeta to set
   */
  public void setSrBeta(double srBeta) {
    this.srBeta.setValue(srBeta);
  }

  private JSpinner srGamma0;

  /**
   * Get RW gamma 0 parameter.
   * 
   * @return the srGamma0
   */
  public double getSrGamma0() {
    return ((Number) srGamma0.getValue()).doubleValue();
  }

  /**
   * Set RW gamma 0 parameter.
   * 
   * @param srGamma0 the srGamma0 to set
   */
  public void setSrGamma0(double srGamma0) {
    this.srGamma0.setValue(srGamma0);
  }

  private JSpinner srGamma1;

  /**
   * Get RW gamma 1 parameter.
   * 
   * @return the srGamma1
   */
  public double getSrGamma1() {
    return ((Number) srGamma1.getValue()).doubleValue();
  }

  /**
   * Set RW gamma 1 parameter.
   * 
   * @param srGamma1 the srGamma1 to set
   */
  public void setSrGamma1(double srGamma1) {
    this.srGamma1.setValue(srGamma1);
  }

  private JSpinner srIter;

  /**
   * Get RW number of iterations.
   * 
   * @return the srIter
   */
  public int getSrIter() {
    return ((Number) srIter.getValue()).intValue();
  }

  /**
   * Set RW number of iterations.
   * 
   * @param srIter the srIter to set
   */
  public void setSrIter(int srIter) {
    this.srIter.setValue((double) srIter);
  }

  private JSpinner srRelerr;

  /**
   * Get RW number of iterations.
   * 
   * @return the srRelerr
   */
  public double getSrRelerr() {
    return ((Number) srRelerr.getValue()).doubleValue();
  }

  /**
   * Set RW number of iterations.
   * 
   * @param srRelerr the srRelerr to set
   */
  public void setSrRelerr(double srRelerr) {
    this.srRelerr.setValue(srRelerr);
  }

  private JComboBox<String> cbShrinkMethod;

  /**
   * Initialiser of cbShrinkMethod.
   * 
   * @param item list of items
   * @param sel name of entry to select
   * @see javax.swing.JComboBox#addItem(java.lang.Object)
   */
  public void setShrinkMethod(String[] item, String sel) {
    setJComboBox(cbShrinkMethod, item, sel);
  }

  /**
   * cbShrinkMethod getter.
   * 
   * @return index of selected entry.
   */
  public int getShrinkMethod() {
    return getJComboBox(cbShrinkMethod);
  }

  private JSpinner srShrinkPower;

  /**
   * Get shrink power.
   * 
   * @return the srShrinkPower
   */
  public double getSrShrinkPower() {
    return ((Number) srShrinkPower.getValue()).doubleValue();
  }

  /**
   * Set shrink power.
   * 
   * @param srShrinkPower the srShrinkPower to set
   */
  public void setSrShrinkPower(double srShrinkPower) {
    this.srShrinkPower.setValue(srShrinkPower);
  }

  private JSpinner srExpandPower;

  /**
   * Get expand power.
   * 
   * @return the srExpandPower
   */
  public double getSrExpandPower() {
    return ((Number) srExpandPower.getValue()).doubleValue();
  }

  /**
   * Set expand power.
   * 
   * @param srExpandPower the srExpandPower to set
   */
  public void setSrExpandPower(double srExpandPower) {
    this.srExpandPower.setValue(srExpandPower);
  }

  private JSpinner srScaleSigma;

  /**
   * Get sigma.
   * 
   * @return the srScaleSigma
   */
  public double getSrScaleSigma() {
    return ((Number) srScaleSigma.getValue()).doubleValue();
  }

  /**
   * Set sigma power.
   * 
   * @param srScaleSigma the srScaleSigma to set
   */
  public void setSrScaleSigma(double srScaleSigma) {
    this.srScaleSigma.setValue(srScaleSigma);
  }

  private JSpinner srScaleMagn;

  /**
   * Get magnitude.
   * 
   * @return the srScaleMagn
   */
  public double getSrScaleMagn() {
    return ((Number) srScaleMagn.getValue()).doubleValue();
  }

  /**
   * Set magnitude power.
   * 
   * @param srScaleMagn the srScaleMagn to set
   */
  public void setSrScaleMagn(double srScaleMagn) {
    this.srScaleMagn.setValue(srScaleMagn);
  }

  private JSpinner srScaleEqNormalsDist;

  /**
   * Get distance.
   * 
   * @return the srScaleMagn
   */
  public double getSrScaleEqNormalsDist() {
    return ((Number) srScaleEqNormalsDist.getValue()).doubleValue();
  }

  /**
   * Set distance.
   * 
   * @param srScaleEqNormalsDist the srScaleEqNormalsDist to set
   */
  public void setSrScaleEqNormalsDist(double srScaleEqNormalsDist) {
    this.srScaleEqNormalsDist.setValue(srScaleEqNormalsDist);
  }

  private JComboBox<String> cbFilteringMethod;

  /**
   * Initialiser of cbFilteringMethod.
   * 
   * @param item list of items
   * @param sel name of the selection to select.
   * @see javax.swing.JComboBox#addItem(java.lang.Object)
   */
  public void setFilteringMethod(String[] item, String sel) {
    setJComboBox(cbFilteringMethod, item, sel);
  }

  /**
   * cbFilteringMethod getter.
   * 
   * @return index of selected entry.
   */
  public int getFilteringMethod() {
    return getJComboBox(cbFilteringMethod);
  }

  private JCheckBox chTrueBackground;

  /**
   * Get status of True Background.
   * 
   * @return the chTrueBackground enabled/disabled
   */
  public boolean getChTrueBackground() {
    return chTrueBackground.isSelected();
  }

  /**
   * Set status of True Background.
   * 
   * @param chTrueBackground the chTrueBackground to set (enabled/disabled)
   */
  public void setChTrueBackground(boolean chTrueBackground) {
    this.chTrueBackground.setSelected(chTrueBackground);
  }

  private JCheckBox chLocalMean;

  /**
   * Get status of Local mean.
   * 
   * @return the chLocalMean enabled/disabled
   */
  public boolean getChLocalMean() {
    return chLocalMean.isSelected();
  }

  /**
   * Set status of local mean.
   * 
   * @param chLocalMean the chLocalMean to set (enabled/disabled)
   */
  public void setChLocalMean(boolean chLocalMean) {
    this.chLocalMean.setSelected(chLocalMean);
  }

  private JSpinner srLocalMeanWindow;

  /**
   * Get value of window parameter for local mean.
   * 
   * @return the srWindow
   */
  public int getSrLocalMeanWindow() {
    return ((Number) srLocalMeanWindow.getValue()).intValue();
  }

  /**
   * Set value of local mean window parameter.
   * 
   * @param srWindow the srWindow to set
   */
  public void setSrLocalMeanWindow(int srWindow) {
    this.srLocalMeanWindow.setValue((double) srWindow);
  }

  private JCheckBox chHatFilter;

  /**
   * Get status of HatSnake filter.
   * 
   * @return the chHatFilter enabled/disabled
   */
  public boolean getChHatFilter() {
    return chHatFilter.isSelected();
  }

  /**
   * Set status of HatSnake filter.
   * 
   * @param chHatFilter the chHatFilter to set (enabled/disabled)
   */
  public void setChHatFilter(boolean chHatFilter) {
    this.chHatFilter.setSelected(chHatFilter);
  }

  private JSpinner srAlev;

  /**
   * Get value of alev parameter.
   * 
   * @return the srAlev
   */
  public double getSrAlev() {
    return ((Number) srAlev.getValue()).doubleValue();
  }

  /**
   * Set value of alev parameter.
   * 
   * @param srAlev the srAlev to set
   */
  public void setSrAlev(double srAlev) {
    this.srAlev.setValue(srAlev);
  }

  private JSpinner srNum;

  /**
   * Get value of num parameter.
   * 
   * @return the srNum
   */
  public int getSrNum() {
    return ((Number) srNum.getValue()).intValue();
  }

  /**
   * Set value of num parameter.
   * 
   * @param srNum the srNum to set
   */
  public void setSrNum(int srNum) {
    this.srNum.setValue((double) srNum);
  }

  private JSpinner srWindow;

  /**
   * Get value of window parameter.
   * 
   * @return the srWindow
   */
  public int getSrWindow() {
    return ((Number) srWindow.getValue()).intValue();
  }

  /**
   * Set value of window parameter.
   * 
   * @param srWindow the srWindow to set
   */
  public void setSrWindow(int srWindow) {
    this.srWindow.setValue((double) srWindow);
  }

  private JComboBox<String> cbFilteringPostMethod;

  /**
   * Initialiser of cbFilteringPostMethod.
   * 
   * @param item list of items
   * @param sel name of the selection to select.
   * @see javax.swing.JComboBox#addItem(java.lang.Object)
   */
  public void setFilteringPostMethod(String[] item, String sel) {
    setJComboBox(cbFilteringPostMethod, item, sel);
  }

  /**
   * cbFilteringMethod getter.
   * 
   * @return index of selected entry.
   */
  public int getFilteringPostMethod() {
    return getJComboBox(cbFilteringPostMethod);
  }

  private JCheckBox chMaskCut;

  /**
   * Get status of mask cut filter.
   * 
   * @return the chHatFilter enabled/disabled
   */
  public boolean getChMaskCut() {
    return chMaskCut.isSelected();
  }

  /**
   * Set status of mask cut filter.
   * 
   * @param chMaskCut the chHatFilter to set (enabled/disabled)
   */
  public void setChMaskCut(boolean chMaskCut) {
    this.chMaskCut.setSelected(chMaskCut);
  }

  private JCheckBox chShowSeed;

  /**
   * Get status of show seed.
   * 
   * @return the chShowSeed
   */
  public boolean getChShowSeed() {
    return chShowSeed.isSelected();
  }

  /**
   * Set status of show seed.
   * 
   * @param chShowSeed the chShowSeed to set
   */
  public void setChShowSeed(boolean chShowSeed) {
    this.chShowSeed.setSelected(chShowSeed);
  }

  private JCheckBox chShowPreview;

  /**
   * Get status of show preview.
   * 
   * @return the chShowPreview
   */
  public boolean getChShowPreview() {
    return chShowPreview.isSelected();
  }

  /**
   * Set status of show preview.
   * 
   * @param chShowPreview the chShowPreview to set
   */
  public void setChShowPreview(boolean chShowPreview) {
    this.chShowPreview.setSelected(chShowPreview);
  }

  private JButton bnRun;
  private JButton bnCancel;
  private JButton bnHelp;
  private JButton bnRunActive;

  /**
   * Build View but not show it.
   */
  public RandomWalkView() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
    ToolTipManager.sharedInstance().setDismissDelay(UiTools.TOOLTIPDELAY);
    wnd = new JFrame("Random Walker Segmentation");
    wnd.setResizable(false);

    JPanel imagePanel = new JPanel();
    imagePanel.setBorder(BorderFactory
            .createTitledBorder(BorderFactory.createLineBorder(Color.RED, 1), "Image selection"));
    imagePanel.setLayout(new GridLayout(1, 1, 2, 2));
    cbOrginalImage = new JComboBox<String>();
    imagePanel.add(getControlwithoutLabel(cbOrginalImage, "Select image to be processed"));

    JPanel seedSelPanel = new JPanel();
    seedSelPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE, 1), "Get seeds from:"));
    seedSelPanel.setLayout(new GridLayout(2, 1, 2, 2));
    rbRgbImage = new JRadioButton("RGB image");
    rbRgbImage.addActionListener(this);
    rbRgbImage.setActionCommand(SeedSource.RGBImage.toString());
    rbRgbImage.addItemListener(this);
    UiTools.setToolTip(rbRgbImage, "Load seeds as scribble image. Stack or single image");
    rbCreateImage = new JRadioButton("Create image");
    rbCreateImage.addActionListener(this);
    rbCreateImage.setActionCommand(SeedSource.CreatedImage.toString());
    rbCreateImage.addItemListener(this);
    UiTools.setToolTip(rbCreateImage,
            "Create copy of original image for scribbling. Stack or single image");
    rbMaskImage = new JRadioButton("Mask image");
    rbMaskImage.addActionListener(this);
    rbMaskImage.setActionCommand(SeedSource.MaskImage.toString());
    rbMaskImage.addItemListener(this);
    UiTools.setToolTip(rbMaskImage, "Initial seed as binary mask.");
    rbQcofFile = new JRadioButton("QCONF file");
    rbQcofFile.addActionListener(this);
    rbQcofFile.setActionCommand(SeedSource.QconfFile.toString());
    rbQcofFile.addItemListener(this);
    UiTools.setToolTip(rbQcofFile, "Get binary mask from QCONF file.");
    seedSource = new ButtonGroup();
    seedSource.add(rbRgbImage);
    seedSource.add(rbCreateImage);
    seedSource.add(rbMaskImage);
    seedSource.add(rbQcofFile);
    seedSelPanel.add(rbRgbImage);
    seedSelPanel.add(rbCreateImage);
    seedSelPanel.add(rbMaskImage);
    seedSelPanel.add(rbQcofFile);
    // create all controls with values even if not visible
    cbCreatedSeedImage = new JComboBox<String>();
    cbRgbSeedImage = new JComboBox<String>();
    cbMaskSeedImage = new JComboBox<String>();
    bnQconfSeedImage = new JButton("Open");
    bnClone = new JButton("Clone");
    UiTools.setToolTip(bnClone, "Clone selected original image and allow to seed it manually");
    bnFore = new JToggleButton("FG");
    UiTools.setToolTip(bnFore, "Select Foreground pen");
    bnFore.addActionListener(this);
    bnFore.setBackground(Color.ORANGE);
    bnBack = new JToggleButton("BG");
    UiTools.setToolTip(bnBack, "Select Background pen");
    bnBack.addActionListener(this);
    bnBack.setBackground(Color.GREEN);
    lbQconfFile = new JLabel("");

    JPanel optionsPanel = new JPanel();
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Segmentation options"));
    optionsPanel.setLayout(new GridLayout(8, 1, 2, 2));
    srAlpha = getDoubleSpinner(400, 0, 1e5, 1, 0);
    optionsPanel.add(getControlwithLabel(srAlpha, "Alpha",
            "alpha penalises pixels whose intensities are far away from the mean seed intensity"));
    srBeta = getDoubleSpinner(50, 0, 1e5, 1, 0);
    optionsPanel.add(getControlwithLabel(srBeta, "Beta",
            "beta penalises pixels located at an edge, i.e.where there is a large gradient in"
                    + " intensity. Diffusion will be reduced</html>"));
    srGamma0 = getDoubleSpinner(100, 0, 1e5, 1, 0);
    optionsPanel.add(getControlwithLabel(srGamma0, "Gamma 0",
            "gamma is the strength of competition between foreground and background."
                    + " gamma 0 is for preliminary segmentation whereas gamma 1 for fine"
                    + " segmentation. Temporally disabled"));
    srGamma0.setEnabled(false); // Temporally disabled, see enableUI as well
    srGamma1 = getDoubleSpinner(300, 0, 1e5, 1, 0);
    optionsPanel.add(getControlwithLabel(srGamma1, "Gamma 1",
            "Set to 0 to skip second sweep. Any other value is currently ignored."));
    srIter = getDoubleSpinner(300, 1, 10000, 1, 0);
    optionsPanel.add(getControlwithLabel(srIter, "Iterations",
            "Maximum number of iterations." + "Second sweep uses half of this value"));
    srRelerr = getDoubleSpinner(8.1e-3, 0, 10, 1e-5, 6);
    optionsPanel.add(getControlwithLabel(srRelerr, "Rel error", "Relative error."));
    // inter process panel
    JPanel processPanel = new JPanel();
    processPanel.setBorder(BorderFactory.createTitledBorder("Inter-process"));
    processPanel.setLayout(new GridBagLayout());
    GridBagConstraints constrProc = new GridBagConstraints();
    constrProc.gridx = 0;
    constrProc.gridy = 0;
    constrProc.fill = GridBagConstraints.HORIZONTAL;
    constrProc.weightx = 1;
    constrProc.insets = new Insets(1, 2, 1, 2);
    cbShrinkMethod = new JComboBox<String>();
    processPanel.add(
            getControlwithLabel(cbShrinkMethod, "Shrink method",
                    "Shrinking/expanding if nth frame result is used as n+1 frame seed."
                            + " Ignored for single image and if seed is stack of image size."),
            constrProc);
    cbShrinkMethod.addItemListener(this);
    srShrinkPower = getDoubleSpinner(10, 0, 10000, 1, 0);
    constrProc.gridx = 0;
    constrProc.gridy = 1;
    processPanel.add(getControlwithLabel(srShrinkPower, "Shrink power", ""), constrProc);
    srExpandPower = getDoubleSpinner(15, 0, 10000, 1, 0);
    constrProc.gridx = 0;
    constrProc.gridy = 2;
    processPanel.add(getControlwithLabel(srExpandPower, "Expand power", ""), constrProc);
    // scale panel
    JPanel scalePanel = new JPanel();
    GridLayout gr = new GridLayout(2, 2);
    gr.setHgap(2);
    scalePanel.setLayout(gr);
    srScaleSigma = getDoubleSpinner(0.3, 1e-2, 1, 1e-2, 2);
    scalePanel.add(getControlwithLabel(srScaleSigma, "Sigma",
            "Set up expotential relation between negative curvature and node translocation"));
    srScaleMagn = getDoubleSpinner(1, 1, 10, 1, 0);
    scalePanel.add(getControlwithLabel(srScaleMagn, "Magn",
            "Maximum multiplier of shrink power. Set to 1.0 to disable"));
    scalePanel.add(getControlwithLabel(new JLabel(), "", ""));
    srScaleEqNormalsDist = getDoubleSpinner(0, 0, 100, 1, 1);
    scalePanel.add(getControlwithLabel(srScaleEqNormalsDist, "Norm dist",
            "Distance (number of nodes) of normals alignment and curvature averaging "
                    + "Set to 0 to disable"
                    + " normals alignment and set averaging to approx. 3 nodes."));
    constrProc.gridx = 0;
    constrProc.gridy = 3;

    processPanel.add(scalePanel, constrProc);

    cbFilteringMethod = new JComboBox<String>();
    constrProc.gridx = 0;
    constrProc.gridy = 4;
    processPanel.add(
            getControlwithLabel(cbFilteringMethod, "Binary filter",
                    "Filtering applied for result between sweeps. Ignored if gamma[1]==0"),
            constrProc);
    chTrueBackground = new JCheckBox("Estimate Background");
    constrProc.gridx = 0;
    constrProc.gridy = 5;
    processPanel.add(
            getControlwithLabel(chTrueBackground, "",
                    "Try to estimate background level. Disable is background is homogenious"),
            constrProc);
    // Use local mean panel
    JPanel localMeanPanel = new JPanel();
    localMeanPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    localMeanPanel.setBorder(BorderFactory.createTitledBorder("Use local mean"));
    chLocalMean = new JCheckBox("Local mean");
    chLocalMean.addItemListener(this);
    localMeanPanel.add(getControlwithLabel(chLocalMean, "",
            "Enable local mean feature. LM works best if mask is greater that object"
                    + " (external masks)."));
    srLocalMeanWindow = getDoubleSpinner(23, 3, 501, 2, 0);
    localMeanPanel.add(getControlwithLabel(srLocalMeanWindow, "Window",
            "Odd mask within the local mean is evaluated"));
    constrProc.gridx = 0;
    constrProc.gridy = 6;
    processPanel.add(localMeanPanel, constrProc);

    // post process panel
    JPanel postprocessPanel = new JPanel();
    postprocessPanel.setBorder(BorderFactory.createTitledBorder("Post-process"));
    postprocessPanel.setLayout(new GridBagLayout());
    JPanel postprocesshatPanel = new JPanel();
    postprocesshatPanel.setLayout(new GridLayout(4, 1, 0, 2));
    postprocesshatPanel.setBorder(BorderFactory.createTitledBorder("Hat filter"));
    chHatFilter = new JCheckBox("Hat Filter");
    chHatFilter.addItemListener(this);
    UiTools.setToolTip(chHatFilter, "Try to remove small inclusions in contour");
    postprocesshatPanel.add(getControlwithLabel(chHatFilter, "", ""));
    srAlev = getDoubleSpinner(0.9, 0, 1, 0.01, 4);
    postprocesshatPanel.add(getControlwithLabel(srAlev, "srAlev", ""));
    srNum = getDoubleSpinner(1, 0, 500, 1, 0);
    postprocesshatPanel.add(getControlwithLabel(srNum, "srNum",
            "If set to 0 all features with rank > srAlew will be removed"));
    srWindow = getDoubleSpinner(15, 1, 500, 1, 0);
    postprocesshatPanel.add(getControlwithLabel(srWindow, "srWindow", ""));
    GridBagConstraints constrPost = new GridBagConstraints();
    constrPost.gridx = 0;
    constrPost.gridy = 0;
    constrPost.fill = GridBagConstraints.HORIZONTAL;
    constrPost.anchor = GridBagConstraints.NORTH;
    constrPost.weightx = 1;
    postprocessPanel.add(postprocesshatPanel, constrPost);
    cbFilteringPostMethod = new JComboBox<String>();
    constrPost.gridx = 0;
    constrPost.gridy = 1;
    constrPost.insets = new Insets(5, 0, 0, 0);
    postprocessPanel.add(getControlwithLabel(cbFilteringPostMethod, "Binary filter",
            "Filtering applied after segmentation"), constrPost);
    chMaskCut = new JCheckBox("Cut output");
    constrPost.gridx = 0;
    constrPost.gridy = 2;
    UiTools.setToolTip(chMaskCut, "Cut output mask by initial mask (if present)");
    postprocessPanel.add(getControlwithLabel(chMaskCut, "", ""), constrPost);

    JPanel displayPanel = new JPanel();
    displayPanel.setBorder(BorderFactory.createTitledBorder("Display options"));
    displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
    chShowSeed = new JCheckBox("Show seeds");
    UiTools.setToolTip(chShowSeed,
            "Show generated seeds. Works only if seeds are polpulated between frames"
                    + " or if seeds are given as stack of masks");
    chShowSeed.setSelected(false);
    chShowPreview = new JCheckBox("Show preview");
    chShowPreview.setSelected(false);
    displayPanel.add(chShowSeed);
    displayPanel.add(chShowPreview);

    // cancel apply row
    JPanel caButtons = new JPanel();
    caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    bnRun = new JButton("Run");
    UiTools.setToolTip(bnRun, "Run segmentation for all slices");
    bnRunActive = new JButton("Run active");
    UiTools.setToolTip(bnRunActive, "Run segmentation for selected slice only");
    bnCancel = new JButton("Cancel");
    bnHelp = new JButton("Help");
    caButtons.add(bnRun);
    caButtons.add(bnRunActive);
    caButtons.add(bnCancel);
    caButtons.add(bnHelp);

    GridBagConstraints constrains = new GridBagConstraints();
    panel = new JPanel(new BorderLayout());
    panelMain = new JPanel(new GridBagLayout());
    panel.add(panelMain, BorderLayout.CENTER);
    JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(panelButtons, BorderLayout.SOUTH);

    constrains.gridx = 0;
    constrains.gridy = 0;
    constrains.weighty = 1;
    constrains.gridheight = 1;
    constrains.anchor = GridBagConstraints.NORTH;
    constrains.fill = GridBagConstraints.HORIZONTAL;
    panelMain.add(imagePanel, constrains);

    constrains.gridx = 0;
    constrains.gridy = 1;
    constrains.weighty = 1;
    constrains.gridheight = 1;
    constrains.anchor = GridBagConstraints.NORTH;
    constrains.fill = GridBagConstraints.BOTH;
    panelMain.add(seedSelPanel, constrains);

    constrains.gridx = 0;
    constrains.gridy = 2;
    constrains.gridheight = 1;
    constrains.fill = GridBagConstraints.BOTH;
    constrains.weighty = 1;
    constrains.anchor = GridBagConstraints.NORTH;
    panelMain.add(getRgbImage(), constrains); // dynamic panel no 2

    constrains.gridx = 3;
    constrains.gridy = 0;
    constrains.weighty = 1;
    constrains.gridheight = 3;
    constrains.anchor = GridBagConstraints.NORTH;
    constrains.fill = GridBagConstraints.VERTICAL;
    panelMain.add(optionsPanel, constrains);

    constrains.gridx = 4;
    constrains.gridy = 0;
    constrains.weighty = 1;
    constrains.gridheight = 3;
    constrains.anchor = GridBagConstraints.NORTH;
    constrains.fill = GridBagConstraints.VERTICAL;
    panelMain.add(processPanel, constrains);

    constrains.gridx = 5;
    constrains.gridy = 0;
    constrains.weighty = 1;
    constrains.gridheight = 3;
    constrains.anchor = GridBagConstraints.NORTH;
    constrains.fill = GridBagConstraints.VERTICAL;
    panelMain.add(postprocessPanel, constrains);

    constrains.gridx = 7;
    constrains.gridy = 0;
    constrains.weighty = 0;
    constrains.fill = GridBagConstraints.VERTICAL;
    panelMain.add(displayPanel, constrains);

    panelButtons.add(caButtons);

    wnd.add(panel);

    wnd.pack();
    wnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setInitial();

  }

  /**
   * Enables/disables UI. Does not apply to Cancel button.
   * 
   * @param status true if enabled.
   */
  public void enableUI(boolean status) {
    cbOrginalImage.setEnabled(status);
    cbRgbSeedImage.setEnabled(status);
    rbRgbImage.setEnabled(status);
    rbCreateImage.setEnabled(status);
    rbMaskImage.setEnabled(status);
    rbQcofFile.setEnabled(status);
    cbMaskSeedImage.setEnabled(status);
    bnQconfSeedImage.setEnabled(status);
    bnClone.setEnabled(status);
    bnFore.setEnabled(status);
    bnBack.setEnabled(status);
    cbCreatedSeedImage.setEnabled(status);
    srAlpha.setEnabled(status);
    srBeta.setEnabled(status);
    srGamma0.setEnabled(status);
    srGamma1.setEnabled(status);
    srIter.setEnabled(status);
    srRelerr.setEnabled(status);
    cbShrinkMethod.setEnabled(status);
    srShrinkPower.setEnabled(status);
    srExpandPower.setEnabled(status);
    if (cbShrinkMethod.getSelectedItem().equals("CONTOUR")) {
      srScaleMagn.setEnabled(status);
      srScaleSigma.setEnabled(status);
      srScaleEqNormalsDist.setEnabled(status);
    } else {
      cbShrinkMethod.setEnabled(status);
    }
    cbFilteringMethod.setEnabled(status);
    chTrueBackground.setEnabled(status);
    chHatFilter.setEnabled(status);
    if (chHatFilter.isSelected()) {
      chHatFilter.setEnabled(status);
      srAlev.setEnabled(status);
      srNum.setEnabled(status);
      srWindow.setEnabled(status);
    } else {
      chHatFilter.setEnabled(status);
    }
    chLocalMean.setEnabled(status);
    if (chLocalMean.isSelected()) {
      chLocalMean.setEnabled(status);
      srLocalMeanWindow.setEnabled(status);
    } else {
      chLocalMean.setEnabled(status);
    }
    cbFilteringMethod.setEnabled(status);
    chMaskCut.setEnabled(status);
    chShowPreview.setEnabled(status);
    chShowSeed.setEnabled(status);
    cbFilteringPostMethod.setEnabled(status);
    bnRun.setEnabled(status);
    bnRunActive.setEnabled(status);
    bnHelp.setEnabled(status);

    // chHatFilter.setEnabled(status); // not implemented, remove after
    srGamma0.setEnabled(false); // feature currently disabled, see constructor as well
  }

  /**
   * Create two elements row [label component]. Component and label have tooltip.
   * 
   * @param c component to add.
   * @param label label to add.
   * @param toolTip tooltip.
   * @return Panel with two components.
   */
  private JPanel getControlwithLabel(JComponent c, String label, String toolTip) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(1, 2, 2, 2));
    JLabel lab = new JLabel(label);
    panel.add(lab);
    panel.add(c);
    UiTools.setToolTip(c, toolTip);
    UiTools.setToolTip(lab, toolTip);
    return panel;
  }

  /**
   * Create two elements row [label component]. Component and label have tooltip.
   * 
   * @param c component to add.
   * @param label label to add.
   * @param toolTip tooltip.
   * @return Panel with two components.
   */
  private JPanel getControlwithLabelSqueezed(JComponent c, String label, String toolTip) {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    JLabel lab = new JLabel(label);
    panel.add(lab);
    panel.add(c);
    UiTools.setToolTip(c, toolTip);
    UiTools.setToolTip(lab, toolTip);
    return panel;
  }

  /**
   * Create one elements row [component]. Component has tooltip.
   * 
   * @param c component to add.
   * @param toolTip tooltip.
   * @return Panel with two components.
   */
  private JPanel getControlwithoutLabel(JComponent c, String toolTip) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(1, 1, 2, 2));
    panel.add(c);
    if (toolTip != null && !toolTip.isEmpty()) {
      String text = "<html>" + QuimpToolsCollection.stringWrap(toolTip, 40, "<br>") + "</html>";
      c.setToolTipText(text);
    }
    return panel;
  }

  /**
   * Create double spinner.
   * 
   * @param d initial value
   * @param min minimal value
   * @param max maximal value
   * @param step step
   * @param columns digits
   * @return created spinner
   */
  private JSpinner getDoubleSpinner(double d, double min, double max, double step, int columns) {
    SpinnerNumberModel model = new SpinnerNumberModel(d, min, max, step);
    JSpinner spinner = new JSpinner(model);
    String c = "";
    if (columns == 0) {
      c = "0";
    } else {
      c = "0." + String.join("", Collections.nCopies(columns, "0"));
    }
    spinner.setEditor(new JSpinner.NumberEditor(spinner, c));
    return spinner;
  }

  /**
   * Set initial state of UI.
   */
  private void setInitial() {
    rbRgbImage.setSelected(true);
    srAlev.setEnabled(false);
    srNum.setEnabled(false);
    srWindow.setEnabled(false);
    srLocalMeanWindow.setEnabled(false);
    // chHatFilter.setEnabled(true);
  }

  /**
   * Helper creating dynamic panel for loading seeds from rgb image.
   * 
   * @return created panel.
   */
  private JPanel getRgbImage() {
    JPanel dynPanel;
    try { // protect against empty component at given index - used because this is default one
      dynPanel = (JPanel) panelMain.getComponent(2);
      dynPanel.removeAll();
    } catch (ArrayIndexOutOfBoundsException e) {
      dynPanel = new JPanel();
    }
    dynPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE), "Seeds fom RGB image"));
    dynPanel.setLayout(new GridLayout(2, 1, 2, 2));

    dynPanel.add(cbRgbSeedImage);
    dynPanel.add(new JLabel());
    return dynPanel;
  }

  /**
   * Helper creating dynamic panel for loading seeds from created image.
   * 
   * @return created panel.
   */
  private JPanel getCreateImage() {
    JPanel dynPanel = (JPanel) panelMain.getComponent(2);
    dynPanel.removeAll();

    dynPanel.setBorder(BorderFactory
            .createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE), "Seed build"));
    dynPanel.setLayout(new BorderLayout());
    ((BorderLayout) dynPanel.getLayout()).setVgap(2);
    // middle buttons
    JPanel seedBuildPanel = new JPanel();
    seedBuildPanel.setLayout(new GridLayout(2, 1, 2, 2));
    JPanel seedBuildPanelButtons = new JPanel();
    seedBuildPanelButtons.setLayout(new GridLayout(1, 3, 2, 2));

    seedBuildPanelButtons.add(bnClone);
    seedBuildPanelButtons.add(bnFore);
    seedBuildPanelButtons.add(bnBack);

    seedBuildPanel.add(seedBuildPanelButtons);
    seedBuildPanel.add(cbCreatedSeedImage);

    dynPanel.add(seedBuildPanel);

    return dynPanel;
  }

  /**
   * Helper creating dynamic panel for loading seeds from binary image.
   * 
   * @return created panel.
   */
  private JPanel getMaskImage() {
    JPanel dynPanel = (JPanel) panelMain.getComponent(2);
    dynPanel.removeAll();

    dynPanel.setBorder(BorderFactory
            .createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE), "Seed from mask"));
    dynPanel.setLayout(new GridLayout(2, 1, 2, 2));

    dynPanel.add(cbMaskSeedImage);
    dynPanel.add(new JLabel());

    return dynPanel;
  }

  /**
   * Helper creating dynamic panel for loading seeds from qconf file
   * 
   * @return created panel.
   */
  private JPanel getQconfImage() {
    JPanel dynPanel = (JPanel) panelMain.getComponent(2);
    dynPanel.removeAll();

    dynPanel.setBorder(BorderFactory
            .createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE), "Seed from QCONF"));
    dynPanel.setLayout(new GridLayout(2, 1, 2, 2));

    dynPanel.add(bnQconfSeedImage);
    dynPanel.add(lbQconfFile);

    return dynPanel;
  }

  /**
   * Show the window.
   */
  public void show() {
    wnd.setVisible(true);

  }

  /**
   * Create dynamic panel on window depending on state of radio buttons group.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == rbRgbImage) {
      getRgbImage();
      panelMain.revalidate();
      wnd.validate();
    }
    if (source == rbCreateImage) {
      getCreateImage();
      panelMain.revalidate();
      wnd.validate();
    }
    if (source == rbMaskImage) {
      getMaskImage();
      panelMain.revalidate();
      wnd.validate();
    }
    if (source == rbQcofFile) {
      getQconfImage();
      panelMain.revalidate();
      wnd.validate();
    }
    if (source == bnFore) {
      if (bnFore.isSelected()) {
        bnBack.setSelected(false);
      }
    }
    if (source == bnBack) {
      if (bnBack.isSelected()) {
        bnFore.setSelected(false);
      }
    }
  }

  /**
   * Set enable/disabled controls depending on hatsnake checkbox.
   */
  @Override
  public void itemStateChanged(ItemEvent arg0) {
    Object source = arg0.getItemSelectable();
    if (source == chHatFilter) {
      srAlev.setEnabled(chHatFilter.isSelected());
      srNum.setEnabled(chHatFilter.isSelected());
      srWindow.setEnabled(chHatFilter.isSelected());
    }
    if (source == chLocalMean) {
      srLocalMeanWindow.setEnabled(chLocalMean.isSelected());
    }
    if (source == cbShrinkMethod) {
      if (!cbShrinkMethod.getSelectedItem().equals("CONTOUR")) {
        srScaleMagn.setEnabled(false);
        srScaleSigma.setEnabled(false);
        srScaleEqNormalsDist.setEnabled(false);
      } else {
        srScaleMagn.setEnabled(true);
        srScaleSigma.setEnabled(true);
        srScaleEqNormalsDist.setEnabled(true);
      }
    }
  }

  /**
   * Helper for setting entries in JComboBox.
   * 
   * @param component component
   * @param item item
   * @param sel selection entry, should be in item list
   */
  private void setJComboBox(JComboBox<String> component, String[] item, String sel) {
    for (String i : item) {
      component.addItem(i);
    }
    if (item.length > 0) {
      component.setSelectedItem(sel);
    }
    component.revalidate();
    wnd.pack();
  }

  /**
   * Get index of selected entry.
   * 
   * @param component to read from
   * @return index of selected entry.
   */
  private int getJComboBox(JComboBox<String> component) {
    return component.getSelectedIndex();
  }

  /**
   * Assign listener to whole window. It will react on window activation or deactivation.
   * 
   * @param listener listener
   */
  public void addWindowController(WindowFocusListener listener) {
    wnd.addWindowFocusListener(listener);
  }

  /**
   * Assign listener to Run button.
   * 
   * @param list listener
   */
  public void addRunController(ActionListener list) {
    bnRun.addActionListener(list);
  }

  /**
   * Assign listener to Help button.
   * 
   * @param list listener
   */
  public void addHelpController(ActionListener list) {
    bnHelp.addActionListener(list);
  }

  /**
   * Assign listener to Run selected slice button.
   * 
   * @param list listener
   */
  public void addRunActiveController(ActionListener list) {
    bnRunActive.addActionListener(list);
  }

  /**
   * Assign listener to Cancel button.
   * 
   * @param list listener
   */
  public void addCancelController(ActionListener list) {
    bnCancel.addActionListener(list);
  }

  /**
   * Assign listener to Clone button.
   * 
   * @param list listener
   */
  public void addCloneController(ActionListener list) {
    bnClone.addActionListener(list);
  }

  /**
   * Assign listener to FG button.
   * 
   * @param list listener
   */
  public void addFgController(ActionListener list) {
    bnFore.addActionListener(list);
  }

  /**
   * Assign listener to BG button.
   * 
   * @param list listener
   */
  public void addBgController(ActionListener list) {
    bnBack.addActionListener(list);
  }

  /**
   * Assign listener to Load Qconf button.
   * 
   * @param list listener
   */
  public void addLoadQconfController(ActionListener list) {
    bnQconfSeedImage.addActionListener(list);
  }

  /**
   * Assign listener to original image selector.
   * 
   * @param list listener
   */
  public void addImageController(ActionListener list) {
    cbOrginalImage.addActionListener(list);
  }

  /**
   * Assign listener to all seed selectors.
   * 
   * @param list listener
   */
  public void addSeedController(ActionListener list) {
    cbRgbSeedImage.addActionListener(list);
    cbMaskSeedImage.addActionListener(list);
    cbCreatedSeedImage.addActionListener(list);
  }

  /**
   * Set text on cancel button.
   * 
   * @param label text to set
   */
  public void setCancelLabel(String label) {
    bnCancel.setText(label);
  }

}
