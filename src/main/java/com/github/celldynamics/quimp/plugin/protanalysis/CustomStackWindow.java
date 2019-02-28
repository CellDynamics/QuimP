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
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.GradientType;
import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.OutlinePlotTypes;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;
import com.github.celldynamics.quimp.utils.graphics.GraphicsElements;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
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
  JToggleButton bnPickPoint; // outside because it is set by CustomCanvas#mousePressed

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
    Point2d point = ((ProtAnalysisOptions) model.getOptions()).gradientPoint;
    pointsSelectedPolar.setText(point.toString());
  }

  /**
   * Build the window.
   */
  public void buildWindow() {
    ProtAnalysisOptions opt = (ProtAnalysisOptions) model.getOptions();
    setLayout(new BorderLayout(10, 10));
    add(ic, BorderLayout.CENTER); // IJ image
    // panel for slidebar to make it more separated from window edges
    JPanel cmpP = new JPanel();
    cmpP.setLayout(new GridLayout());
    cmpP.add(cmp);
    cmpP.setBorder(new EmptyBorder(5, 5, 10, 10));
    add(cmpP, BorderLayout.SOUTH); // slidebar
    // right panel
    Panel right = new Panel();
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
        visualTrackingPanel.add(buildSubPanel(1, 2, getButton(new ActionToRoi("-> ROI",
                "Transfer selected points to ROI manager. ROI manager will be cleared", this)),
                getButton(new ActionFromRoi("<- ROI",
                        "Import ROIs from Roi Manager. ROI name must have format: "
                                + ProtAnalysisOptions.roiPrefix
                                + "CELLNO, where CELLNO is cell index. Hold CTRL key if all points "
                                + "refer to the same cell (no naming pattern needed).",
                        this))));
      }
      { // line with one button clear
        visualTrackingPanel.add(buildSubPanel(1, 1, getButton(
                new ActionClearPoints("Remove all", "Remove all selected points", this))));
      }
      { // line with 2 radio buttons
        JRadioButton rbnStatic = new JRadioButton();
        rbnStatic.setAction(new ActionUpdateOptionsRadio("Static", "Static", this,
                opt.plotStaticDynamic, ProtAnalysisOptions.PLOT_STATIC));
        JRadioButton rbnDynamic = new JRadioButton();
        rbnDynamic.setAction(new ActionUpdateOptionsRadio("Dynamic", "Dynamic", this,
                opt.plotStaticDynamic, ProtAnalysisOptions.PLOT_DYNAMIC));
        switch (opt.plotStaticDynamic.getValue()) {
          case ProtAnalysisOptions.PLOT_STATIC:
            rbnStatic.setSelected(true);
            break;
          case ProtAnalysisOptions.PLOT_DYNAMIC:
            rbnDynamic.setSelected(true);
            break;
          default:
            rbnStatic.setSelected(true);
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rbnStatic);
        buttonGroup.add(rbnDynamic);
        visualTrackingPanel.add(buildSubPanel(1, 2, rbnStatic, rbnDynamic));
      }
      { // line with 2 checkbox
        visualTrackingPanel.add(buildSubPanel(2, 2,
                getCheckbox("Show point", "Show tracked point", opt.chbShowPoint),
                getCheckbox("Show track", "Show tracks", opt.chbShowTrack),
                getCheckbox("Smooth", "!Apply track smoothing in static and dynamic view",
                        opt.chbSmoothTracks),
                getCheckbox("Show map", "Show tracks on motility map", opt.chbShowTrackMotility)));
      }
      { // line with outline selector
        JComboBox<OutlinePlotTypes> cbOutlineColor =
                new JComboBox<OutlinePlotTypes>(OutlinePlotTypes.values());
        cbOutlineColor.setSelectedItem(opt.selOutlineColoring.plotType);
        cbOutlineColor.setAction(new ActionUpdateOptionsEnum("Outline color", "Set outline color",
                this, opt.selOutlineColoring));
        visualTrackingPanel.add(buildSubPanel(1, 1, cbOutlineColor));
      }
      { // line with open new image check box
        JCheckBox cb =
                getCheckbox("New image", "Always open new image with tracks", opt.chbNewImage);
        visualTrackingPanel.add(buildSubPanel(1, 1, cb, getCheckbox("Flatten",
                "Flatten stack used for showing static tracks", opt.chbFlattenStaticTrackImage)));
        // TODO decide if we need this. Refreshing overlay on org image will cause problems
        cb.setEnabled(false);
      }
      { // two lines with buttons
        visualTrackingPanel.add(
                buildSubPanel(2, 1, getButton(new ActionTrackPoints("Track", "Track points", this)),
                        getButton(new ActionClearOverlay("Clear", "Clear Overlay", this))));
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
        setComboBox(cbMapCellNumber, 0, gs.length - 1, opt.selActiveCellMap.getValue());
        cbMapCellNumber.setAction(new ActionUpdateOptionsNumber("Cell number",
                "Which cell to generate map for.", this, opt.selActiveCellMap));
        mapsPanel.add(buildSubPanel(1, 1, cbMapCellNumber));
      }
      { // map type line, 3 buttons
        mapsPanel.add(buildSubPanel(1, 3,
                getButton(new ActionPlotMap("Mot", "Plot motility map for selected cell.", this,
                        "MOT")),
                getButton(new ActionPlotMap("Con", "Plot convexity map for selected cell.", this,
                        "CONV")),
                getButton(new ActionPlotMap("Flu", "Plot fluoresence maps for selected cell.", this,
                        "FLU"))));
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
      { // select cell
        JComboBox<Integer> cbPlotCellNumber = new JComboBox<Integer>();
        // get stmap but without checking, assume that there is q analysis
        STmap[] gs = ((QParamsQconf) model.getQconfLoader().getQp()).getLoadedDataContainer()
                .getQState();
        setComboBox(cbPlotCellNumber, 0, gs.length - 1, opt.selActiveCellPlot.getValue());
        cbPlotCellNumber.setAction(new ActionUpdateOptionsNumber("Cell number",
                "Which cell to generate map for.", this, opt.selActiveCellPlot));
        tablePanel.add(buildSubPanel(1, 1, cbPlotCellNumber));
      }
      { // line with channel selection (3 radios)
        JRadioButton rbnCh1 = new JRadioButton();
        rbnCh1.setAction(new ActionUpdateOptionsRadio("Ch1", "Set channel 1 active", this,
                opt.selActiveChannel, ProtAnalysisOptions.CH1));
        JRadioButton rbnCh2 = new JRadioButton();
        rbnCh2.setAction(new ActionUpdateOptionsRadio("Ch2", "Set channel 2 active", this,
                opt.selActiveChannel, ProtAnalysisOptions.CH2));
        JRadioButton rbnCh3 = new JRadioButton();
        rbnCh3.setAction(new ActionUpdateOptionsRadio("Ch3", "Set channel 3 active", this,
                opt.selActiveChannel, ProtAnalysisOptions.CH3));
        switch (opt.selActiveChannel.getValue()) {
          case ProtAnalysisOptions.CH2:
            rbnCh2.setSelected(true);
            break;
          case ProtAnalysisOptions.CH3:
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
      { // tables
        tablePanel.add(buildSubPanel(1, 1, new JSeparator(SwingConstants.HORIZONTAL)));
        tablePanel.add(buildSubPanel(1, 2,
                getButton(new ActionTableGeom("Geom", "Show geometric features in table.", this)),
                getButton(
                        new ActionTableFluo("Fluo", "Show fluoresecne features in table.", this))));
      }
      { // separator
        tablePanel.add(buildSubPanel(1, 1, new JSeparator(SwingConstants.HORIZONTAL)));
      }
      { // suff to plot checkboxes
        tablePanel.add(buildSubPanel(5, 2,
                getCheckbox("X-Centr", "Centroid x-coordinate", opt.chbXcentrPlot),
                getCheckbox("Y-Centr", "Centroid y-coordinate", opt.chbYcentrPlot),
                getCheckbox("Displ", "Displacement", opt.chbDisplPlot),
                getCheckbox("Dist", "Distance", opt.chbDistPlot),
                getCheckbox("Persist", "Persistence", opt.chbPersistencePlot),
                getCheckbox("Speed", "Speed", opt.chbSpeedPlot),
                getCheckbox("Perim", "Perimeter", opt.chbPerimPlot),
                getCheckbox("Elong", "Elongation", opt.chbElongPlot),
                getCheckbox("Circ", "Circularity", opt.chbCircPlot),
                getCheckbox("Area", "Area", opt.chbAreaPlot)));
      }
      { // separator
        tablePanel.add(buildSubPanel(1, 1, new JSeparator(SwingConstants.HORIZONTAL)));
      }
      { // suff to plot checkboxes
        tablePanel.add(buildSubPanel(5, 2,
                // totalFluor
                getCheckbox("Total fl",
                        "Total fluorescence. Sum of all pixel intensities within the cell outline.",
                        opt.chbTotFluPlot),
                // meanFluor
                getCheckbox("Mean fl",
                        "Mean fluorescence. Average intensity of pixels within the cell outline.",
                        opt.chbMeanFluPlot),
                // cortexWidth
                getCheckbox("Cortex", "Width of the cortex, as specified by the user.",
                        opt.chbCortexWidthPlot),
                // innerArea
                getCheckbox("Cyto",
                        "Area of the cytoplasm (area of the whole cell minus the cortex area).",
                        opt.chbCytoAreaPlot),
                // totalInnerFluor
                getCheckbox("Total ctf", "Sum of all pixel intensities within the cytoplasm.",
                        opt.chbTotalCytoPlot),
                // meanInnerFluor
                getCheckbox("Mean ctf", "Average pixel intensity within the cytoplasm.",
                        opt.chbMeanCytoPlot),
                // cortexArea
                getCheckbox("Cortex ar", "Area of the cortex.", opt.chbCortexAreaPlot),
                // totalCorFluo
                getCheckbox("Total ctf", "Sum of all pixel intensities within the cortex.",
                        opt.chbTotalCtf2Plot),
                // meanCorFluo
                getCheckbox("Mean ctf", "Average pixel intensity within the cortex.",
                        opt.chbManCtfPlot)));
      }
      { // button
        tablePanel.add(buildSubPanel(1, 1,
                getButton(new ActionPlot2d("Plot", "Plot selected parameters.", this))));
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
        bnPickPoint = getToggleButton(
                new ActionClickPredefinedPoint("Click", "Select reference point.", this));
        polarPanel.add(buildSubPanel(1, 2, bnPickPoint, getButton(
                new ActionRoiPredefinedPoint("ROI", "Select reference point from ROI.", this))));
      }
      { // info
        JLabel selected = new JLabel("Selected: ");
        pointsSelectedPolar.setHorizontalAlignment(JLabel.RIGHT);
        polarPanel.add(buildSubPanel(1, 2, selected, pointsSelectedPolar));
      }
      { // relative to line
        JComboBox<GradientType> cbRelativePolar =
                new JComboBox<GradientType>(GradientType.values());
        cbRelativePolar.setSelectedItem(GradientType.LB_CORNER);
        cbRelativePolar
                .setAction(new ActionGetPredefinedPoint("Relative to", "Point relative to", this));
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
   * @param name checkbox name
   * @param desc tooltip, if starts with !, option is not implemented. For tests rather
   * @param option option related
   * @return checkbox
   */
  private JCheckBox getCheckbox(String name, String desc, MutableBoolean option) {
    JCheckBox chb = new JCheckBox();
    chb.setAction(new ActionUpdateOptionsBoolean(name, desc, this, option));
    chb.setSelected(option.getValue());
    if (desc.startsWith("!")) {
      chb.setAction(new ActionNotSupported(name, desc, this));
    }
    return chb;
  }

  /**
   * Helper to produce buttons.
   * 
   * @param act action
   * @return button
   */
  private JButton getButton(ProtAnalysisAbstractAction act) {
    JButton chb = new JButton();
    chb.setAction(act);
    return chb;
  }

  /**
   * Helper to produce buttons.
   * 
   * @param act action
   * @return toggle button
   */
  private JToggleButton getToggleButton(ProtAnalysisAbstractAction act) {
    JToggleButton chb = new JToggleButton();
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
    model.outlines.clear(); // remove old outlines for old frame
    updateOverlay(model.currentFrame + 1);

  }

  /**
   * Plot overlay (outline) at frame.
   * 
   * <p>Called on each slice selector action.
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
    updateOverlayPoints(frame);
  }

  /**
   * Add only {@link PointCoords} from {@link Prot_Analysis#selected} to overlay.
   * 
   * <p>Overlay must exist already in the image.
   * 
   * @param frame frame to update
   */
  void updateOverlayPoints(int frame) {
    overlay = imp.getOverlay();
    if (overlay == null) {
      return;
    }
    // find points
    for (PointCoords p : model.selected) {
      if (p.frame == imp.getCurrentSlice() - 1) {
        PolygonRoi or = GraphicsElements.getCircle(p.point.getX(), p.point.getY(),
                ProtAnalysisOptions.staticPointColor, ProtAnalysisOptions.staticPointSize);
        overlay.add(or);
      }
    }
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
  // updated on mouse move and copied to model on LMB
  PointCoords pc = null;
  private Prot_Analysis model; // main model with method to run on ui action
  private ProtAnalysisOptions options; // helper
  private int sensitivity = 10; // square of distance

  public CustomCanvas(ImagePlus imp, Prot_Analysis model) {
    super(imp);
    this.model = model;
    this.options = ((ProtAnalysisOptions) model.getOptions());
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
        // store in model after appending frame number
        if (model.selected.add(pc) == false) { // already exists
          model.selected.remove(pc); // so remove
        }
        model.getGui().updateStaticFields();
        model.getGui().updateOverlayPoints(model.currentFrame);
      }
    } else {
      if (SwingUtilities.isLeftMouseButton(e) && options.bnGradientPickActive.booleanValue()) {
        options.gradientPoint = new Point2d(e.getX(), e.getY());
        model.getGui().bnPickPoint.doClick();
        model.getGui().updateStaticFields();
      } else {
        super.mousePressed(e);
      }
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
    double half = ProtAnalysisOptions.pointSize / 2;
    if (pc != null) {
      Rectangle2D e = new Rectangle2D.Double(screenXD(pc.point.getX()) - half,
              screenYD(pc.point.getY()) - half, ProtAnalysisOptions.pointSize,
              ProtAnalysisOptions.pointSize);
      g2.setPaint(ProtAnalysisOptions.pointColor);
      g2.draw(e);
    }
  }

}