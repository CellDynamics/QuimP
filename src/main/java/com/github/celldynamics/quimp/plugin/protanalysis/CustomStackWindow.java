package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;

/**
 * Implement Prot Analysis UI based on IJ StackWindow.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
class CustomStackWindow extends StackWindow {
  private Prot_Analysis model; // main model with method to run on ui action
  private Color overlayColor = Color.GREEN; // outline color

  private ArrayList<OutlineHandler> handlers; // extracted from loaded QCONF

  private Component cmp;
  private Overlay overlay;
  private ImagePlus imp;
  JLabel pointsSelected = new JLabel("");
  JLabel pointsSelectedPolar = new JLabel("");

  /**
   * Construct the window.
   * 
   * @param model application logic module (with options)
   * @param imp ImagePlus image to be displayed.
   */
  public CustomStackWindow(Prot_Analysis model, final ImagePlus imp) {
    super(imp, new CustomCanvas(imp, model));
    this.model = model;
    try {
      handlers = model.getQconfLoader().getEcmm().oHs;
    } catch (QuimpException e) {
      // we should never be here as ecmm is validated on load
      throw new RuntimeException("ECMM can not be obtained");
    }
    cmp = this.getComponent(1);
    remove(cmp); // FIXME Protect against single image
    this.imp = imp;
    buildWindow();
  }

  void updateStaticFields() {
    pointsSelected.setText(Integer.toString(model.selected.size()));
  }

  private void addListeners() {
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        Prot_Analysis.LOGGER.trace("Closing Custom Stack");
        model.gui.setVisible(false);
        model.gui.dispose();
        super.windowClosing(e);
        dispose();
      }

    });

  }

  /**
   * Build the window.
   */
  public void buildWindow() {
    ProtAnalysisOptions opt = (ProtAnalysisOptions) model.getOptions();
    setLayout(new BorderLayout(10, 10));
    addListeners();
    add(ic, BorderLayout.CENTER); // IJ image
    // panel for slidebar to make it more separated from window edges
    JPanel cmpP = new JPanel();
    cmpP.setLayout(new GridLayout());
    cmpP.add(cmp);
    cmpP.setBorder(new EmptyBorder(5, 5, 10, 10));
    add(cmpP, BorderLayout.SOUTH); // slidebar
    // right panel
    Panel right = new Panel();
    // right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
    final int rightWidth = 160; // width of the right panel
    right.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    { // point selection panel
      JPanel visualTrackingPanel = new JPanel();
      // final int selectPointsPanelHeight = 160;
      visualTrackingPanel.setLayout(new BoxLayout(visualTrackingPanel, BoxLayout.PAGE_AXIS));
      visualTrackingPanel.setBorder(BorderFactory.createTitledBorder("Visual tracking"));
      { // two text lines: help and selected points
        JTextArea help = new JTextArea("Select points with CTRL key");
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        help.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // selected points: VAL
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(1, 2));
        JLabel selected = new JLabel("Selected: ");
        textPanel.add(selected);
        pointsSelected.setHorizontalAlignment(JLabel.RIGHT);
        textPanel.add(pointsSelected);
        visualTrackingPanel.add(buildSubPanel(2, 1, help, textPanel));
      }
      { // line with two buttons ROI
        visualTrackingPanel.add(
                buildSubPanel(1, 2, getButton(new ActionNotSupported("-> ROI", "-> ROI", this)),
                        getButton(new ActionNotSupported("<- ROI", "-> ROI", this))));
      }
      { // line with one button clear
        visualTrackingPanel.add(buildSubPanel(1, 1, getButton(
                new ActionClearPoints("Remove all", "Remove all selected points", this))));
      }
      { // line with 2 radio buttons
        JRadioButton rbnStatic = new JRadioButton();
        rbnStatic.setAction(new ActionNotSupported("Static", "Static", this));
        rbnStatic.setSelected(opt.plotStatic);
        JRadioButton rbnDynamic = new JRadioButton();
        rbnDynamic.setAction(new ActionNotSupported("Dynamic", "Dynamic", this));
        rbnDynamic.setSelected(!opt.plotStatic);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rbnStatic);
        buttonGroup.add(rbnDynamic);
        visualTrackingPanel.add(buildSubPanel(1, 2, rbnStatic, rbnDynamic));
      }
      { // line with 2 checkbox
        visualTrackingPanel.add(buildSubPanel(2, 1,
                getCheckbox(new ActionNotSupported("Show point", "Show tracked point", this),
                        opt.guiShowPoint),
                getCheckbox(
                        new ActionNotSupported("Smooth tracks",
                                "Apply track smoothing in static and dynamic view", this),
                        opt.guiSmoothTracks)));
      }
      { // line with outline selector
        JComboBox<String> cbOutlineColor = new JComboBox<String>();
        setJComboBox(cbOutlineColor, ProtAnalysisOptions.outlineColoring,
                opt.selOutlineColoring);
        cbOutlineColor
                .setAction(new ActionNotSupported("Outline color", "Set outline color", this));
        visualTrackingPanel.add(buildSubPanel(1, 1, cbOutlineColor));
      }
      { // line with open new image check box
        visualTrackingPanel.add(buildSubPanel(1, 1,
                getCheckbox(
                        new ActionNewImage("New image", "Always open new image with tracks", this),
                        opt.guiNewImage)));

      }
      { // two lines with buttons
        visualTrackingPanel.add(buildSubPanel(2, 1,
                getButton(new ActionClearOverlay("Clear", "Clear Overlay", this)),
                getButton(new ActionStaticTrackPoints("Track", "Track points", this))));
      }
      c.anchor = GridBagConstraints.NORTH;
      c.gridx = 0;
      c.gridy = 0;
      c.weighty = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      right.add(visualTrackingPanel, c);
    }
    { // Maps Panel
      JPanel mapsPanel = new JPanel();
      mapsPanel.setLayout(new BoxLayout(mapsPanel, BoxLayout.Y_AXIS));
      mapsPanel.setBorder(BorderFactory.createTitledBorder("Maps"));
      { // line with cell selector
        JComboBox<Integer> cbMapCellNumber = new JComboBox<Integer>();
        // get stmap but without checking, assume that there is q analysis
        STmap[] gs = ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer()
                .getQState();
        setComboBox(cbMapCellNumber, 0, gs.length - 1, opt.activeCellMap);
        cbMapCellNumber.setAction(
                new ActionNotSupported("Cell number", "Which cell to generate map for.", this));
        mapsPanel.add(buildSubPanel(1, 1, cbMapCellNumber));
      }
      { // map type line, 3 buttons
        mapsPanel.add(buildSubPanel(1, 3,
                getButton(new ActionNotSupported("Mot", "Plot motility map for selected cell.",
                        this)),
                getButton(new ActionNotSupported("Con", "Plot convexity map for selected cell.",
                        this)),
                getButton(new ActionNotSupported("Flu", "Plot fluoresence maps for selected cell.",
                        this))));
      }
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy = 1;
      c.weighty = 0;
      right.add(mapsPanel, c);
    }
    { // tables panel
      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
      tablePanel.setBorder(BorderFactory.createTitledBorder("Tables and plots"));
      { // selelct cell
        JComboBox<Integer> cbPlotCellNumber = new JComboBox<Integer>();
        // get stmap but without checking, assume that there is q analysis
        STmap[] gs = ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer()
                .getQState();
        setComboBox(cbPlotCellNumber, 0, gs.length - 1, opt.activeCellPlot);
        cbPlotCellNumber.setAction(
                new ActionNotSupported("Cell number", "Which cell to generate map for.", this));
        tablePanel.add(buildSubPanel(1, 1, cbPlotCellNumber));
      }
      { // line with channel selection (3 radios)
        JRadioButton rbnCh1 = new JRadioButton();
        rbnCh1.setAction(new ActionNotSupported("Ch1", "Set channel 1 active", this));
        JRadioButton rbnCh2 = new JRadioButton();
        rbnCh2.setAction(new ActionNotSupported("Ch2", "Set channel 2 active", this));
        JRadioButton rbnCh3 = new JRadioButton();
        rbnCh3.setAction(new ActionNotSupported("Ch3", "Set channel 3 active", this));
        switch (opt.activeChannel) {
          case 2:
            rbnCh2.setSelected(true);
            break;
          case 3:
            rbnCh3.setSelected(true);
            break;
          default:
            rbnCh1.setSelected(true);
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rbnCh1);
        buttonGroup.add(rbnCh2);
        buttonGroup.add(rbnCh3);
        tablePanel.add(buildSubPanel(1, 3, rbnCh1, rbnCh2, rbnCh3));
      }
      { // separator
        tablePanel.add(buildSubPanel(1, 1, new JSeparator(SwingConstants.HORIZONTAL)));
      }
      { // suff to plot checkboxes
        tablePanel.add(buildSubPanel(5, 2,
                getCheckbox(new ActionNotSupported("X-Centr", "Centroid x-coordinate", this),
                        opt.chbXcentrPlot),
                getCheckbox(new ActionNotSupported("Y-Centr", "Centroid y-coordinate", this),
                        opt.chbYcentrPlot),
                getCheckbox(new ActionNotSupported("Displ", "Displacement", this),
                        opt.chbDisplPlot),
                getCheckbox(new ActionNotSupported("Dist", "Distance", this), opt.chbDistPlot),
                getCheckbox(new ActionNotSupported("Direct", "Direction", this),
                        opt.chbDirectPlot),
                getCheckbox(new ActionNotSupported("Speed", "Speed", this), opt.chbSpeedPlot),
                getCheckbox(new ActionNotSupported("Perim", "Perimeter", this),
                        opt.chbPerimPlot),
                getCheckbox(new ActionNotSupported("Elong", "Elongation", this),
                        opt.chbElongPlot),
                getCheckbox(new ActionNotSupported("Circ", "Circularity", this),
                        opt.chbCircPlot),
                getCheckbox(new ActionNotSupported("Area", "Area", this), opt.chbAreaPlot)));
      }
      { // separator
        tablePanel.add(buildSubPanel(1, 1, new JSeparator(SwingConstants.HORIZONTAL)));
      }
      { // suff to plot checkboxes
        tablePanel.add(buildSubPanel(5, 2,
                getCheckbox(new ActionNotSupported("Total fl", "Total fluoresence", this),
                        opt.chbTotFluPlot),
                getCheckbox(new ActionNotSupported("Mean fl", "Mean fluoresence", this),
                        opt.chbMeanFluPlot),
                getCheckbox(new ActionNotSupported("Cortex", "Cortex width", this),
                        opt.chbCortexWidthPlot),
                getCheckbox(new ActionNotSupported("Cyto", "Cyto area", this),
                        opt.chbCytoAreaPlot),
                getCheckbox(new ActionNotSupported("Total ctf", "Total", this),
                        opt.chbTotalCtfPlot),
                getCheckbox(new ActionNotSupported("Mean ctf", "Mean", this),
                        opt.chbMeanCtfPlot),
                getCheckbox(new ActionNotSupported("Cortex ar", "Cortex area", this),
                        opt.chbCortexAreaPlot),
                getCheckbox(new ActionNotSupported("Total ctf", "Total ctf", this),
                        opt.chbTotalCtf2Plot),
                getCheckbox(new ActionNotSupported("Mean ctf", "Mean ctf", this),
                        opt.chbManCtfPlot)));
      }
      { // button
        tablePanel.add(buildSubPanel(1, 1,
                getButton(new ActionNotSupported("Generate", "Generate polar plot", this))));
      }
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy = 2;
      c.weighty = 0;
      right.add(tablePanel, c);
    }
    { // polarplot panel
      JPanel polarPanel = new JPanel();
      polarPanel.setLayout(new BoxLayout(polarPanel, BoxLayout.Y_AXIS));
      polarPanel.setBorder(BorderFactory.createTitledBorder("Polar plots"));
      { // row with buttons
        polarPanel.add(buildSubPanel(1, 2,
                getButton(new ActionNotSupported("Click", "Select reference point", this)),
                getButton(new ActionNotSupported("ROI", "Select reference point from ROI", this))));
      }
      { // info
        JLabel selected = new JLabel("Selected: ");
        pointsSelectedPolar.setHorizontalAlignment(JLabel.RIGHT);
        polarPanel.add(buildSubPanel(1, 2, selected, pointsSelectedPolar));
      }
      { // relative to line
        JComboBox<String> cbRelativePolar = new JComboBox<String>();
        setJComboBox(cbRelativePolar, ProtAnalysisOptions.relativePolar, opt.selrelativePolar);
        cbRelativePolar.setAction(new ActionNotSupported("Relative to", "Point relative to", this));
        polarPanel.add(buildSubPanel(1, 1, cbRelativePolar));
      }
      { // generate
        polarPanel.add(buildSubPanel(1, 1,
                getButton(new ActionNotSupported("Generate", "Generate polar plot", this))));
      }
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy = 3;
      c.weighty = 1;
      right.add(polarPanel, c);
    }

    add(right, BorderLayout.EAST);
    pack();
    updateStaticFields();
    // this.setSize(600, 600);
    imp.setSlice(1);
    updateOverlay(1);
    setVisible(false); // to allow use showUI
  }

  /**
   * Helper to produce checkboxes.
   * 
   * @param act action
   * @param opt related option in {@link ProtAnalysisOptions}
   * @return checkbox
   */
  private JCheckBox getCheckbox(ProtAnalysisAbstractAction act, boolean opt) {
    JCheckBox chb = new JCheckBox();
    chb.setAction(act);
    chb.setSelected(opt);
    return chb;
  }

  /**
   * Helper to produce checkboxes.
   * 
   * @param act action
   * @param opt related option in {@link ProtAnalysisOptions}
   * @return checkbox
   */
  private JButton getButton(ProtAnalysisAbstractAction act) {
    JButton chb = new JButton();
    chb.setAction(act);
    return chb;
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
  }

  /**
   * Populate component with range of integers.
   * 
   * @param component component
   * @param min first value
   * @param max last value
   * @param sel selected value
   */
  private void setComboBox(JComboBox<Integer> component, Integer min, Integer max, Integer sel) {
    for (Integer i = min; i <= max; i++) {
      component.addItem(i);
    }
    component.setSelectedItem(sel);
  }

  /**
   * Add components to GridLayout panel.
   * 
   * @param w number of rows
   * @param h number of columns
   * @param cmp components to add (vararg)
   * @return Constructed panel
   */
  private JPanel buildSubPanel(int w, int h, Component... cmp) {
    JPanel localPanel = new JPanel(new GridLayout(w, h));
    for (Component c : cmp) {
      localPanel.add(c);
    }
    return localPanel;

  }

  /*
   * On each slice change.
   * 
   * Actions performed:
   * - Clear outlines array (keep outlines only for current frame)
   * - Clear selected points
   * - Update overlay for new frame
   * TODO Comply with new image checkbox
   */
  @Override
  public void updateSliceSelector() {
    super.updateSliceSelector();
    model.currentFrame = imp.getCurrentSlice() - 1;
    new ActionClearPoints(this).clear();
    model.outlines.clear(); // remove old outlines for old frame
    updateOverlay(model.currentFrame + 1);

  }

  /**
   * Plot overlay (outline) at frame.
   * 
   * @param frame to plot in (1-based)
   */
  public void updateOverlay(int frame) {
    overlay = new Overlay();
    for (OutlineHandler oh : handlers) {
      if (oh.isOutlineAt(frame)) {
        Outline outline = oh.getStoredOutline(frame);
        model.outlines.add(outline); // remember outline for proximity calculations
        Roi r = outline.asFloatRoi();
        r.setStrokeColor(overlayColor);
        overlay.add(r);
      }
    }
    imp.setOverlay(overlay);
  }

  /**
   * Show UI.
   * 
   * @param val true or false to show or hide UI
   */
  public void showUI(boolean val) {
    setVisible(val);
  }

  /**
   * Get model.
   * 
   * @return Reference to application model class
   */
  Prot_Analysis getModel() {
    return model;
  }
}

/**
 * Handle mouse events.
 * 
 * @author baniu
 *
 */
@SuppressWarnings("serial")
class CustomCanvas extends ImageCanvas {
  static final Logger LOGGER = LoggerFactory.getLogger(CustomCanvas.class.getName());
  // closest point on outline to mouse position (image coordinates) + index of outline
  PointCoords pc = null;
  private Prot_Analysis model; // main model with method to run on ui action
  private int sensitivity = 10; // square of distance
  private Color pointColor = Color.CYAN; // box color
  private Color staticPointColor = Color.YELLOW; // box color
  private int pointSize = 10; // box size

  public CustomCanvas(ImagePlus imp, Prot_Analysis model) {
    super(imp);
    this.model = model;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    // action - select outline point if CTRL is pressed and LMB. In this mode IJ handlers are
    // suppressed. Second click on the point will remove it
    if (SwingUtilities.isLeftMouseButton(e)
            && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
      if (pc != null) {
        if (model.selected.add(pc) == false) { // already exists
          model.selected.remove(pc); // so remove
        }
        model.getGui().updateStaticFields();
      }
    } else {
      super.mousePressed(e);
    }
  }

  /**
   * Find closes point between outlines for current frame (outlines) and mouse position.
   * 
   * @param current current mouse position in the image coordinates
   * @param dist max distance
   * @return found point that belongs to outline (image coordinates, frame) and outline index in
   *         {@link Prot_Analysis#outlines}
   */
  private PointCoords checkProximity(Point current, double dist) {
    // Point current = new Point(screenXD(currentt.getX()), screenYD(currentt.getY()));
    ListIterator<Outline> it = model.outlines.listIterator();
    while (it.hasNext()) {
      Integer io = it.nextIndex(); // order!
      Outline o = it.next();
      Rectangle2D.Double bounds = o.getDoubleBounds(); // FIXME cache
      if (bounds.contains(current)) { // investigate deeper
        for (Vert v : o) { // over vertices
          if (current.distanceSq(v.getX(), v.getY()) < dist) {
            return new PointCoords(
                    new Point((int) Math.round(v.getX()), (int) Math.round(v.getY())), io);
          }
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.gui.ImageCanvas#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    // offscreen - coordinates of the image, regardless zoom. e - absolute coordinates of the window
    Point p = new Point(offScreenX(e.getX()), offScreenY(e.getY()));
    // LOGGER.trace("e: [" + e.getX() + "," + e.getY() + "] offScreenX: " + p.toString());
    PointCoords ptmp = checkProximity(p, sensitivity);
    if (ptmp != null) { // if there is point close
      pc = ptmp; // set it to current under mouse
      repaint(); // refresh
    } else {
      if (pc != null) {
        pc = null; // otherwise clear current under mouse
        repaint(); // and repaint
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ij.gui.ImageCanvas#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2 = (Graphics2D) g;
    double half = pointSize / 2;
    if (pc != null) {
      Rectangle2D e = new Rectangle2D.Double(screenXD(pc.point.getX()) - half,
              screenYD(pc.point.getY()) - half, pointSize, pointSize);
      g2.setPaint(pointColor);
      g2.draw(e);
    }
    g2.setPaint(staticPointColor);
    for (PointCoords p : model.selected) {
      Ellipse2D e = new Ellipse2D.Double(screenXD(p.point.getX()) - half,
              screenYD(p.point.getY()) - half, pointSize, pointSize);
      g2.fill(e);
    }
  }

}