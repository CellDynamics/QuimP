package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.GradientType;
import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.OutlinePlotTypes;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;

/**
 * Build GUI for plugin.
 * 
 * @author p.baniukiewicz
 *
 */
class ProtAnalysisUI implements ActionListener {
  static final Logger LOGGER = LoggerFactory.getLogger(ProtAnalysisUI.class.getName());
  // UI elements
  private JFrame wnd;
  private JButton bnCancel;
  private JButton bnApply;
  private JButton bnHelp;
  private JButton bnGradient;
  private JFormattedTextField tfNoiseTolerance;
  private JFormattedTextField tfDropValue;
  private JFormattedTextField tfMotThreshold;
  private JFormattedTextField tfConvThreshold;
  private JComboBox<ProtAnalysisOptions.OutlinePlotTypes> cbPlotType;
  private JCheckBox chPlotMotmap;
  private JCheckBox chPlotMotmapmax;
  private JCheckBox chPlotConmap;
  private JCheckBox chPlotOutline;
  private JCheckBox chPlotStaticmax;
  private JCheckBox chPlotDynamicmax;

  JLabel lbMaxnum;
  JLabel lbMaxval;
  JLabel lbMinval;
  JLabel lbGradinet;

  private JCheckBox chStaticPlotmax;
  private JCheckBox chStaticPlottrack;
  private JCheckBox chStaticAverimage;
  private JCheckBox chPlotPolarplot;

  private JCheckBox chDynamicPlotmax;
  private JCheckBox chDynamicPlottrack;
  private JCheckBox chUseGradient;

  private Prot_Analysis model; // main model with method to run on ui action

  public ProtAnalysisUI(Prot_Analysis model) {
    this.model = model;
    buildUI();
  }

  /**
   * Copy UI settings to {@link ProtAnalysisOptions}
   * object.
   */
  public void readUI() {
    ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
    config.noiseTolerance = ((Number) tfNoiseTolerance.getValue()).doubleValue();
    config.dropValue = ((Number) tfDropValue.getValue()).doubleValue();

    config.plotOutline = chPlotOutline.isSelected();
    config.outlinesToImage.motThreshold = ((Number) tfMotThreshold.getValue()).doubleValue();
    config.outlinesToImage.convThreshold = ((Number) tfConvThreshold.getValue()).doubleValue();
    config.outlinesToImage.plotType = (OutlinePlotTypes) cbPlotType.getSelectedItem();

    config.plotMotmap = chPlotMotmap.isSelected();
    config.plotMotmapmax = chPlotMotmapmax.isSelected();
    config.plotConmap = chPlotConmap.isSelected();

    config.plotStaticmax = chPlotStaticmax.isSelected();
    config.staticPlot.plotmax = chStaticPlotmax.isSelected();
    config.staticPlot.plottrack = chStaticPlottrack.isSelected();
    config.staticPlot.averimage = chStaticAverimage.isSelected();

    config.plotDynamicmax = chPlotDynamicmax.isSelected();
    config.dynamicPlot.plotmax = chDynamicPlotmax.isSelected();
    config.dynamicPlot.plottrack = chDynamicPlottrack.isSelected();

    config.polarPlot.plotpolar = chPlotPolarplot.isSelected();
    config.polarPlot.useGradient = chUseGradient.isSelected();

  }

  /**
   * Copy {@link ProtAnalysisOptions} settings to UI.
   */
  public void writeUI() {
    ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
    tfNoiseTolerance.setValue(new Double(config.noiseTolerance));
    tfDropValue.setValue(new Double(config.dropValue));

    chPlotOutline.setSelected(config.plotOutline);
    tfMotThreshold.setValue(new Double(config.outlinesToImage.motThreshold));
    tfConvThreshold.setValue(new Double(config.outlinesToImage.convThreshold));
    cbPlotType.setSelectedItem(config.outlinesToImage.plotType);

    chPlotMotmap.setSelected(config.plotMotmap);
    chPlotMotmapmax.setSelected(config.plotMotmapmax);
    chPlotConmap.setSelected(config.plotConmap);

    chPlotStaticmax.setSelected(config.plotStaticmax);
    chStaticPlotmax.setSelected(config.staticPlot.plotmax);
    chStaticPlottrack.setSelected(config.staticPlot.plottrack);
    chStaticAverimage.setSelected(config.staticPlot.averimage);

    chPlotDynamicmax.setSelected(config.plotDynamicmax);
    chDynamicPlotmax.setSelected(config.dynamicPlot.plotmax);
    chDynamicPlottrack.setSelected(config.dynamicPlot.plottrack);

    chPlotPolarplot.setSelected(config.polarPlot.plotpolar);
    chUseGradient.setSelected(config.polarPlot.useGradient);
    String g;
    switch (config.polarPlot.type) {
      case OUTLINEPOINT:
        g = "Not implemented";
        chUseGradient.setSelected(true);
        break;
      case SCREENPOINT:
        g = "x=" + config.polarPlot.gradientPoint.getX() + " y="
                + config.polarPlot.gradientPoint.getY();
        chUseGradient.setSelected(true);
        break;
      default:
        g = "";
        chUseGradient.setSelected(false);
    }
    lbGradinet.setText(g);
  }

  /**
   * Show UI.
   * 
   * @param val true or false to show or hide UI
   */
  public void showUI(boolean val) {
    wnd.setVisible(val);
  }

  /**
   * Construct main UI.
   */
  private void buildUI() {
    wnd = new JFrame("Protrusion analysis plugin");
    wnd.setResizable(false);
    JPanel wndpanel = new JPanel(new BorderLayout());

    // middle main panel - integrates fields
    JPanel middle = new JPanel();
    middle.setLayout(new GridLayout(2, 4));
    wndpanel.add(middle, BorderLayout.CENTER);
    // tiles in UI
    {
      // options
      JPanel params = new JPanel();
      params.setBorder(BorderFactory.createTitledBorder("Options"));
      GridLayout g = new GridLayout(4, 2);
      g.setHgap(2);
      g.setVgap(2);
      params.setLayout(g);
      tfDropValue = new JFormattedTextField(NumberFormat.getInstance());
      tfDropValue.setColumns(0);
      tfDropValue.setPreferredSize(new Dimension(80, 26));
      tfNoiseTolerance = new JFormattedTextField(NumberFormat.getInstance());
      tfNoiseTolerance.setColumns(0);
      tfNoiseTolerance.setPreferredSize(new Dimension(80, 26));
      params.add(tfDropValue);
      params.add(new JLabel("Drop"));
      params.add(tfNoiseTolerance);
      params.add(new JLabel("Sens"));
      params.add(new JLabel(" "));
      params.add(new JLabel(" "));
      params.add(new JLabel(" "));
      params.add(new JLabel(" "));
      middle.add(params);
    }
    {
      // info
      JPanel info = new JPanel();
      info.setBorder(BorderFactory.createTitledBorder("Info"));
      GridLayout g = new GridLayout(4, 2);
      g.setHgap(2);
      g.setVgap(2);
      info.setLayout(g);
      info.add(new JLabel("Maxima no:"));
      lbMaxnum = new JLabel(" ");
      lbMaxnum.setBackground(Color.GREEN);
      info.add(lbMaxnum);
      info.add(new JLabel("Max val:"));
      lbMaxval = new JLabel(" ");
      lbMaxval.setBackground(Color.GREEN);
      info.add(lbMaxval);
      info.add(new JLabel("Min val:"));
      lbMinval = new JLabel(" ");
      lbMinval.setBackground(Color.GREEN);
      info.add(lbMinval);
      info.add(new JLabel("Gradient:"));
      lbGradinet = new JLabel(" ");
      info.add(lbGradinet);
      middle.add(info);
    }
    {
      // simple plot
      JPanel mapplots = new JPanel();
      mapplots.setBorder(BorderFactory.createTitledBorder("Map plots"));
      GridLayout g = new GridLayout(4, 2);
      g.setHgap(2);
      g.setVgap(2);
      mapplots.setLayout(g);
      chPlotMotmap = new JCheckBox("Mot map");
      chPlotConmap = new JCheckBox("Conv map");
      chPlotMotmapmax = new JCheckBox("Maxima");
      mapplots.add(chPlotMotmap);
      mapplots.add(new JLabel(" "));
      mapplots.add(chPlotConmap);
      mapplots.add(new JLabel(" "));
      mapplots.add(chPlotMotmapmax);
      middle.add(mapplots);
    }
    {
      // outline plot
      JPanel outlines = new JPanel();
      outlines.setBorder(BorderFactory.createTitledBorder("Outline plots"));
      outlines.setLayout(new BorderLayout());
      chPlotOutline = new JCheckBox("Show");
      chPlotOutline.setBackground(new Color(255, 255, 102));
      outlines.add(chPlotOutline, BorderLayout.NORTH);
      JPanel outlinesp = new JPanel();
      GridLayout g = new GridLayout(3, 2);
      g.setHgap(2);
      g.setVgap(2);
      outlinesp.setLayout(g);
      outlines.add(outlinesp, BorderLayout.CENTER);
      outlinesp.add(new JLabel("Plot type"));
      OutlinePlotTypes[] types = { OutlinePlotTypes.MOTILITY, OutlinePlotTypes.CONVEXITY,
          OutlinePlotTypes.CONVANDEXP, OutlinePlotTypes.CONCANDRETR, OutlinePlotTypes.BOTH };
      cbPlotType = new JComboBox<>(types);
      cbPlotType.setPreferredSize(new Dimension(80, 26));
      outlinesp.add(cbPlotType);
      outlinesp.add(new JLabel("Mot Thr"));
      tfMotThreshold = new JFormattedTextField(NumberFormat.getInstance());
      tfMotThreshold.setColumns(0);
      tfMotThreshold.setPreferredSize(new Dimension(80, 26));
      outlinesp.add(tfMotThreshold);
      outlinesp.add(new JLabel("Conv Thr"));
      tfConvThreshold = new JFormattedTextField(NumberFormat.getInstance());
      tfConvThreshold.setColumns(0);
      tfConvThreshold.setPreferredSize(new Dimension(80, 26));
      outlinesp.add(tfConvThreshold);
      middle.add(outlines);
    }
    {
      JPanel outlines = new JPanel();
      outlines.setBorder(BorderFactory.createTitledBorder("Maxima plot"));
      outlines.setLayout(new BorderLayout());
      chPlotStaticmax = new JCheckBox("Show");
      chPlotStaticmax.setBackground(new Color(255, 255, 102));
      outlines.add(chPlotStaticmax, BorderLayout.NORTH);
      JPanel outlinesp = new JPanel();
      GridLayout g = new GridLayout(3, 2);
      g.setHgap(2);
      g.setVgap(2);
      outlinesp.setLayout(g);
      outlines.add(outlinesp, BorderLayout.CENTER);
      chStaticAverimage = new JCheckBox("Aver. plot");
      outlinesp.add(chStaticAverimage);
      outlinesp.add(new JLabel(" "));
      chStaticPlotmax = new JCheckBox("Plot maxi");
      outlinesp.add(chStaticPlotmax);
      outlinesp.add(new JLabel(" "));
      chStaticPlottrack = new JCheckBox("Plot tracks");
      outlinesp.add(chStaticPlottrack);
      middle.add(outlines);
    }
    {
      JPanel outlines = new JPanel();
      outlines.setBorder(BorderFactory.createTitledBorder("Dynamic plot"));
      outlines.setLayout(new BorderLayout());
      chPlotDynamicmax = new JCheckBox("Show");
      chPlotDynamicmax.setBackground(new Color(255, 255, 102));
      outlines.add(chPlotDynamicmax, BorderLayout.NORTH);
      JPanel outlinesp = new JPanel();
      GridLayout g = new GridLayout(3, 2);
      g.setHgap(2);
      g.setVgap(2);
      outlinesp.setLayout(g);
      outlines.add(outlinesp, BorderLayout.CENTER);
      chDynamicPlotmax = new JCheckBox("Plot maxi");
      outlinesp.add(chDynamicPlotmax);
      outlinesp.add(new JLabel(" "));
      chDynamicPlottrack = new JCheckBox("Plot tracks");
      outlinesp.add(chDynamicPlottrack);
      outlinesp.add(new JLabel(" "));
      middle.add(outlines);
    }
    {
      // Polar plots
      JPanel outlines = new JPanel();
      outlines.setBorder(BorderFactory.createTitledBorder("Polar plot"));
      outlines.setLayout(new BorderLayout());
      chPlotPolarplot = new JCheckBox("Save");
      chPlotPolarplot.setBackground(new Color(255, 255, 102));
      outlines.add(chPlotPolarplot, BorderLayout.NORTH);
      JPanel outlinesp = new JPanel();
      GridLayout g = new GridLayout(3, 2);
      g.setHgap(2);
      g.setVgap(2);
      outlinesp.setLayout(g);
      outlines.add(outlinesp, BorderLayout.CENTER);
      bnGradient = new JButton("Pick grad");
      bnGradient.addActionListener(this);
      outlinesp.add(bnGradient);
      chUseGradient = new JCheckBox("Use grad");
      chUseGradient.setEnabled(false);
      chUseGradient.setToolTipText("Not implemented");
      outlinesp.add(chUseGradient);
      outlinesp.add(new JLabel(" "));
      outlinesp.add(new JLabel(" "));
      middle.add(outlines);
    }

    // cancel apply row
    JPanel caButtons = new JPanel();
    caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
    bnApply = new JButton("Apply");
    bnApply.addActionListener(this);
    bnCancel = new JButton("Cancel");
    bnCancel.addActionListener(this);
    bnHelp = new JButton("Help");
    bnHelp.addActionListener(this);
    caButtons.add(bnApply);
    caButtons.add(bnCancel);
    caButtons.add(bnHelp);
    wndpanel.add(caButtons, BorderLayout.SOUTH);

    wnd.add(wndpanel);
    wnd.pack();
    wnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  /**
   * Open window with custom Canvas that allows user to click point.
   * 
   * @param img Image to show, can be stack.
   * @see CustomCanvas
   */
  public void getGradient(ImagePlus img) {
    if (img == null) {
      return;
    }
    // cut one slice from stack
    ImagePlus copy = img.duplicate();
    ImageStack is = copy.getImageStack();
    ImagePlus single = new ImagePlus("", is.getProcessor(1));
    // open the window
    new ImageWindow(single, new CustomCanvas(single)).setVisible(true);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == bnApply) {
      readUI(); // get ui values to config class
      try {
        model.runPlugin();
      } catch (Exception ex) { // catch all exceptions here
        LOGGER.debug(ex.getMessage(), ex);
        LOGGER.error("Problem with running of Protrusion Analysis mapping: " + ex.getMessage());
      }
    }
    if (e.getSource() == bnCancel) {
      wnd.dispose();
    }
    if (e.getSource() == bnHelp) {
      String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
      try {
        java.awt.Desktop.getDesktop().browse(new URI(url));
      } catch (Exception e1) {
        LOGGER.debug(e1.getMessage(), e1);
        LOGGER.error("Could not open help: " + e1.getMessage(), e1);
      }
    }
    if (e.getSource() == bnGradient) {
      getGradient(model.getQconfLoader().getImage());
    }
  }

  /**
   * Update ProtAnalysisConfig.gradientPosition to actual clicked point on image.
   * 
   * <p>Used during displaying frame to allow user to pick desired gradient point.
   * 
   * @author p.baniukiewicz
   *
   */
  class CustomCanvas extends ImageCanvas {
    private static final long serialVersionUID = 1L;

    public CustomCanvas(ImagePlus imp) {
      super(imp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
      super.mousePressed(e);
      LOGGER.debug("Image coords: " + offScreenX(e.getX()) + " " + offScreenY(e.getY()));
      config.polarPlot.type = GradientType.SCREENPOINT;
      config.polarPlot.gradientPoint = new Point2d(offScreenX(e.getX()), offScreenY(e.getY()));
      writeUI(); // update UI
    }

  }
}