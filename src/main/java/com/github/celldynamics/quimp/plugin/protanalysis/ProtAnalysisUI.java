// package com.github.celldynamics.quimp.plugin.protanalysis;
//
// import java.awt.Color;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.net.URI;
// import java.util.ArrayList;
//
// import javax.swing.JButton;
// import javax.swing.JCheckBox;
// import javax.swing.JComboBox;
// import javax.swing.JFormattedTextField;
// import javax.swing.JLabel;
//
// import org.apache.commons.lang3.NotImplementedException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import com.github.celldynamics.quimp.Outline;
// import com.github.celldynamics.quimp.OutlineHandler;
// import com.github.celldynamics.quimp.PropertyReader;
// import com.github.celldynamics.quimp.QuimpException;
// import com.github.celldynamics.quimp.plugin.protanalysis.ProtAnalysisOptions.OutlinePlotTypes;
//
// import ij.ImagePlus;
//
/// **
// * Build GUI for plugin.
// *
// * @author p.baniukiewicz
// *
// */
// class ProtAnalysisUI implements ActionListener {
// static final Logger LOGGER = LoggerFactory.getLogger(ProtAnalysisUI.class.getName());
// // UI elements
// CustomStackWindow wnd;
// private JButton bnCancel;
// private JButton bnApply;
// private JButton bnHelp;
// private JButton bnGradient;
// private JFormattedTextField tfNoiseTolerance;
// private JFormattedTextField tfDropValue;
// private JFormattedTextField tfMotThreshold;
// private JFormattedTextField tfConvThreshold;
// private JComboBox<ProtAnalysisOptions.OutlinePlotTypes> cbPlotType;
// private JCheckBox chPlotMotmap;
// private JCheckBox chPlotMotmapmax;
// private JCheckBox chPlotConmap;
// private JCheckBox chPlotOutline;
// private JCheckBox chPlotStaticmax;
// private JCheckBox chPlotDynamicmax;
//
// JLabel lbMaxnum;
// JLabel lbMaxval;
// JLabel lbMinval;
// JLabel lbGradinet;
//
// private JCheckBox chStaticPlotmax;
// private JCheckBox chStaticPlottrack;
// private JCheckBox chStaticAverimage;
// private JCheckBox chPlotPolarplot;
//
// private JCheckBox chDynamicPlotmax;
// private JCheckBox chDynamicPlottrack;
// private JCheckBox chUseGradient;
//
// private Prot_Analysis model; // main model with method to run on ui action
// private Color overlayColor = Color.GREEN; // outline color
// private Color pointColor = Color.CYAN; // box color
// private Color staticPointColor = Color.RED; // box color
// private int pointSize = 10; // box size
// private int sensitivity = 10; // square of distance
// private ArrayList<OutlineHandler> handlers; // extracted from loaded QCONF
// // updated on each slice, outlines for current frame
// private ArrayList<Outline> outlines = new ArrayList<>();
//
// // /**
// // * Implement Prot Analysis UI based on IJ StackWindow.
// // *
// // * @author baniu
// // *
// // */
// // @SuppressWarnings("serial")
// // class CustomStackWindow extends StackWindow {
// // private Component cmp;
// // private Overlay overlay;
// // private ImagePlus imp;
// // JLabel pointsSelected = new JLabel("0");
// //
// // /**
// // * Construct the window.
// // *
// // * @param imp ImagePlus image displayed.
// // */
// // public CustomStackWindow(final ImagePlus imp) {
// // super(imp, new CustomCanvas(imp));
// // cmp = this.getComponent(1);
// // remove(cmp); // FIXME Protect against single image
// // this.imp = imp;
// // }
// //
// // /**
// // * Build the window.
// // */
// // public void buildWindow() {
// // setLayout(new BorderLayout(10, 10));
// // add(ic, BorderLayout.CENTER); // IJ image
// // // panel for slidebar to make it more separated from window edges
// // JPanel cmpP = new JPanel();
// // cmpP.setLayout(new GridLayout());
// // cmpP.add(cmp);
// // cmpP.setBorder(new EmptyBorder(5, 5, 10, 10));
// // add(cmpP, BorderLayout.SOUTH); // slidebar
// // // right panel
// // Panel right = new Panel();
// // // right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
// // JPanel selectPointsPanel = new JPanel();
// // selectPointsPanel.setLayout(new BoxLayout(selectPointsPanel, BoxLayout.PAGE_AXIS));
// // selectPointsPanel.setBorder(BorderFactory.createTitledBorder("Select points"));
// // selectPointsPanel.setAlignmentX(LEFT_ALIGNMENT);
// // {
// // JPanel textPanel = new JPanel();
// // textPanel.setLayout(new FlowLayout());
// // JLabel selected = new JLabel("Selected: ");
// // textPanel.add(selected);
// // textPanel.add(pointsSelected);
// // textPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
// // selectPointsPanel.add(textPanel);
// // }
// // {
// // JButton bnClear = new JButton();
// // bnClear.setAction(new ActionClearPoints("Clear", "Clear points", ProtAnalysisUI.this));
// // bnClear.setAlignmentX(JComponent.LEFT_ALIGNMENT);
// // selectPointsPanel.add(bnClear);
// // }
// // right.add(selectPointsPanel);
// // add(right, BorderLayout.EAST);
// //
// // pack();
// // // this.setSize(600, 600);
// // }
// //
// // /*
// // * On each slice change.
// // *
// // * Actions performed:
// // * - Clear outlines array (keep outlines only for current frame)
// // * - Clear selected points
// // * - Update overlay for new frame
// // */
// // @Override
// // public void updateSliceSelector() {
// // super.updateSliceSelector();
// // int frame = imp.getCurrentSlice();
// // outlines.clear(); // remove old outlines for old frame
// // model.selected.clear();
// // updateOverlay(frame);
// //
// // }
// //
// // /**
// // * Plot overlay (outline) at frame.
// // *
// // * @param frame to plot in
// // */
// // public void updateOverlay(int frame) {
// // overlay = new Overlay();
// // for (OutlineHandler oh : handlers) {
// // if (oh.isOutlineAt(frame)) {
// // Outline outline = oh.getStoredOutline(frame);
// // outlines.add(outline); // remember outline for proximity calculations
// // Roi r = outline.asFloatRoi();
// // r.setStrokeColor(overlayColor);
// // overlay.add(r);
// // }
// // }
// // imp.setOverlay(overlay);
// // }
// //
// // }
// //
// // /**
// // * Handle mouse events.
// // *
// // * @author baniu
// // *
// // */
// // @SuppressWarnings("serial")
// // class CustomCanvas extends ImageCanvas {
// // Point pc = null; // closes point on outline to mouse position
// //
// // public CustomCanvas(ImagePlus imp) {
// // super(imp);
// // }
// //
// // /*
// // * (non-Javadoc)
// // *
// // * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
// // */
// // @Override
// // public void mousePressed(MouseEvent e) {
// // // action - select outline point if CTRL is pressed and LMB. In this mode IJ handlers are
// // // suppressed. Second click on the point will remove it
// // if (SwingUtilities.isLeftMouseButton(e)
// // && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
// // if (pc != null) {
// // if (model.selected.add(pc) == false) { // already exists
// // model.selected.remove(pc); // so remove
// // }
// // }
// // } else {
// // super.mousePressed(e);
// // }
// // }
// //
// // /**
// // * Find closes point between outlines for current frame (outlines) and mouse position.
// // *
// // * @param current current mouse position
// // * @param dist max distance
// // * @return found point
// // */
// // private Point checkProximity(Point current, double dist) {
// // for (Outline o : outlines) {
// // Rectangle2D.Double bounds = o.getDoubleBounds(); // FIXME cache
// // if (bounds.contains(current)) { // investigate deeper
// // for (Vert v : o) { // over vertices
// // if (current.distanceSq(v.getX(), v.getY()) < dist) {
// // return new Point((int) Math.round(v.getX()), (int) Math.round(v.getY()));
// // }
// // }
// // }
// // }
// // return null;
// // }
// //
// // /*
// // * (non-Javadoc)
// // *
// // * @see ij.gui.ImageCanvas#mouseMoved(java.awt.event.MouseEvent)
// // */
// // @Override
// // public void mouseMoved(MouseEvent e) {
// // super.mouseMoved(e);
// // Point p = new Point(offScreenX(e.getX()), offScreenY(e.getY()));
// // Point ptmp = checkProximity(p, sensitivity);
// // if (ptmp != null) { // if there is point close
// // pc = ptmp; // set it to current under mouse
// // repaint(); // refresh
// // } else {
// // if (pc != null) {
// // pc = null; // otherwise clear current under mouse
// // repaint(); // and repaint
// // }
// // }
// // }
// //
// // /*
// // * (non-Javadoc)
// // *
// // * @see ij.gui.ImageCanvas#paint(java.awt.Graphics)
// // */
// // @Override
// // public void paint(Graphics g) {
// // super.paint(g);
// // Graphics2D g2 = (Graphics2D) g;
// // if (pc != null) {
// // Rectangle2D e = new Rectangle2D.Double(screenXD(pc.getX()), screenYD(pc.getY()), pointSize,
// // pointSize);
// // g2.setPaint(pointColor);
// // g2.draw(e);
// // }
// // g2.setPaint(staticPointColor);
// // for (Point p : model.selected) {
// // Rectangle2D e = new Rectangle2D.Double(screenXD(p.getX()), screenYD(p.getY()), pointSize,
// // pointSize);
// // g2.fill(e);
// // }
// // }
// //
// // }
//
// ProtAnalysisUI(Prot_Analysis model) {
// this.model = model;
// try {
// handlers = model.getQconfLoader().getEcmm().oHs;
// } catch (QuimpException e) {
// // we should never be here as ecmm is validated on load
// throw new RuntimeException("ECMM can not be obtained");
// }
// buildUI();
// }
//
// /**
// * Default constructor, build empty UI.
// */
// public ProtAnalysisUI() {
// buildUI();
// }
//
// /**
// * Copy UI settings to {@link ProtAnalysisOptions}
// * object.
// */
// public void readUI() {
// ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
// config.noiseTolerance = ((Number) tfNoiseTolerance.getValue()).doubleValue();
// config.dropValue = ((Number) tfDropValue.getValue()).doubleValue();
//
// config.plotOutline = chPlotOutline.isSelected();
// config.outlinesToImage.motThreshold = ((Number) tfMotThreshold.getValue()).doubleValue();
// config.outlinesToImage.convThreshold = ((Number) tfConvThreshold.getValue()).doubleValue();
// config.outlinesToImage.plotType = (OutlinePlotTypes) cbPlotType.getSelectedItem();
//
// config.plotMotmap = chPlotMotmap.isSelected();
// config.plotMotmapmax = chPlotMotmapmax.isSelected();
// config.plotConmap = chPlotConmap.isSelected();
//
// config.plotStaticmax = chPlotStaticmax.isSelected();
// config.staticPlot.plotmax = chStaticPlotmax.isSelected();
// config.staticPlot.plottrack = chStaticPlottrack.isSelected();
// config.staticPlot.averimage = chStaticAverimage.isSelected();
//
// config.plotDynamicmax = chPlotDynamicmax.isSelected();
// config.dynamicPlot.plotmax = chDynamicPlotmax.isSelected();
// config.dynamicPlot.plottrack = chDynamicPlottrack.isSelected();
//
// config.polarPlot.plotpolar = chPlotPolarplot.isSelected();
// config.polarPlot.useGradient = chUseGradient.isSelected();
//
// }
//
// /**
// * Copy {@link ProtAnalysisOptions} settings to UI.
// */
// public void writeUI() {
// ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
// tfNoiseTolerance.setValue(Double.valueOf(config.noiseTolerance));
// tfDropValue.setValue(Double.valueOf(config.dropValue));
//
// chPlotOutline.setSelected(config.plotOutline);
// tfMotThreshold.setValue(Double.valueOf(config.outlinesToImage.motThreshold));
// tfConvThreshold.setValue(Double.valueOf(config.outlinesToImage.convThreshold));
// cbPlotType.setSelectedItem(config.outlinesToImage.plotType);
//
// chPlotMotmap.setSelected(config.plotMotmap);
// chPlotMotmapmax.setSelected(config.plotMotmapmax);
// chPlotConmap.setSelected(config.plotConmap);
//
// chPlotStaticmax.setSelected(config.plotStaticmax);
// chStaticPlotmax.setSelected(config.staticPlot.plotmax);
// chStaticPlottrack.setSelected(config.staticPlot.plottrack);
// chStaticAverimage.setSelected(config.staticPlot.averimage);
//
// chPlotDynamicmax.setSelected(config.plotDynamicmax);
// chDynamicPlotmax.setSelected(config.dynamicPlot.plotmax);
// chDynamicPlottrack.setSelected(config.dynamicPlot.plottrack);
//
// chPlotPolarplot.setSelected(config.polarPlot.plotpolar);
// chUseGradient.setSelected(config.polarPlot.useGradient);
// String g;
// switch (config.polarPlot.type) {
// case OUTLINEPOINT:
// g = "Not implemented";
// chUseGradient.setSelected(true);
// break;
// case SCREENPOINT:
// g = "x=" + config.polarPlot.gradientPoint.getX() + " y="
// + config.polarPlot.gradientPoint.getY();
// chUseGradient.setSelected(true);
// break;
// default:
// g = "";
// chUseGradient.setSelected(false);
// }
// lbGradinet.setText(g);
// }
//
// /**
// * Show UI.
// *
// * @param val true or false to show or hide UI
// */
// public void showUI(boolean val) {
// if (wnd != null) {
// wnd.setVisible(val);
// }
// }
//
// /**
// * Construct main UI.
// */
// private void buildUI() {
// ImagePlus image = model.getImage();
// LOGGER.trace("Attached image " + image.toString());
// // wnd = new CustomStackWindow(image);
// wnd.buildWindow();
// image.setSlice(1);
// wnd.updateOverlay(1);
// wnd.setVisible(false); // to allow use showUI
// }
//
// // /**
// // * Construct main UI.
// // */
// // private void buildUI() {
// // wnd = new JFrame("Protrusion analysis plugin");
// // wnd.setResizable(false);
// // JPanel wndpanel = new JPanel(new BorderLayout());
// //
// // // middle main panel - integrates fields
// // JPanel middle = new JPanel();
// // middle.setLayout(new GridLayout(2, 4));
// // wndpanel.add(middle, BorderLayout.CENTER);
// // // tiles in UI
// // {
// // // options
// // JPanel params = new JPanel();
// // params.setBorder(BorderFactory.createTitledBorder("Options"));
// // GridLayout g = new GridLayout(4, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // params.setLayout(g);
// // tfDropValue = new JFormattedTextField(NumberFormat.getInstance());
// // tfDropValue.setColumns(0);
// // tfDropValue.setPreferredSize(new Dimension(80, 26));
// // tfNoiseTolerance = new JFormattedTextField(NumberFormat.getInstance());
// // tfNoiseTolerance.setColumns(0);
// // tfNoiseTolerance.setPreferredSize(new Dimension(80, 26));
// // params.add(tfDropValue);
// // params.add(new JLabel("Drop"));
// // params.add(tfNoiseTolerance);
// // params.add(new JLabel("Sens"));
// // params.add(new JLabel(" "));
// // params.add(new JLabel(" "));
// // params.add(new JLabel(" "));
// // params.add(new JLabel(" "));
// // middle.add(params);
// // }
// // {
// // // info
// // JPanel info = new JPanel();
// // info.setBorder(BorderFactory.createTitledBorder("Info"));
// // GridLayout g = new GridLayout(4, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // info.setLayout(g);
// // info.add(new JLabel("Maxima no:"));
// // lbMaxnum = new JLabel(" ");
// // lbMaxnum.setBackground(Color.GREEN);
// // info.add(lbMaxnum);
// // info.add(new JLabel("Max val:"));
// // lbMaxval = new JLabel(" ");
// // lbMaxval.setBackground(Color.GREEN);
// // info.add(lbMaxval);
// // info.add(new JLabel("Min val:"));
// // lbMinval = new JLabel(" ");
// // lbMinval.setBackground(Color.GREEN);
// // info.add(lbMinval);
// // info.add(new JLabel("Gradient:"));
// // lbGradinet = new JLabel(" ");
// // info.add(lbGradinet);
// // middle.add(info);
// // }
// // {
// // // simple plot
// // JPanel mapplots = new JPanel();
// // mapplots.setBorder(BorderFactory.createTitledBorder("Map plots"));
// // GridLayout g = new GridLayout(4, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // mapplots.setLayout(g);
// // chPlotMotmap = new JCheckBox("Mot map");
// // chPlotConmap = new JCheckBox("Conv map");
// // chPlotMotmapmax = new JCheckBox("Maxima");
// // mapplots.add(chPlotMotmap);
// // mapplots.add(new JLabel(" "));
// // mapplots.add(chPlotConmap);
// // mapplots.add(new JLabel(" "));
// // mapplots.add(chPlotMotmapmax);
// // middle.add(mapplots);
// // }
// // {
// // // outline plot
// // JPanel outlines = new JPanel();
// // outlines.setBorder(BorderFactory.createTitledBorder("Outline plots"));
// // outlines.setLayout(new BorderLayout());
// // chPlotOutline = new JCheckBox("Show");
// // chPlotOutline.setBackground(new Color(255, 255, 102));
// // outlines.add(chPlotOutline, BorderLayout.NORTH);
// // JPanel outlinesp = new JPanel();
// // GridLayout g = new GridLayout(3, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // outlinesp.setLayout(g);
// // outlines.add(outlinesp, BorderLayout.CENTER);
// // outlinesp.add(new JLabel("Plot type"));
// // OutlinePlotTypes[] types = { OutlinePlotTypes.MOTILITY, OutlinePlotTypes.CONVEXITY,
// // OutlinePlotTypes.CONVANDEXP, OutlinePlotTypes.CONCANDRETR, OutlinePlotTypes.BOTH };
// // cbPlotType = new JComboBox<>(types);
// // cbPlotType.setPreferredSize(new Dimension(80, 26));
// // outlinesp.add(cbPlotType);
// // outlinesp.add(new JLabel("Mot Thr"));
// // tfMotThreshold = new JFormattedTextField(NumberFormat.getInstance());
// // tfMotThreshold.setColumns(0);
// // tfMotThreshold.setPreferredSize(new Dimension(80, 26));
// // outlinesp.add(tfMotThreshold);
// // outlinesp.add(new JLabel("Conv Thr"));
// // tfConvThreshold = new JFormattedTextField(NumberFormat.getInstance());
// // tfConvThreshold.setColumns(0);
// // tfConvThreshold.setPreferredSize(new Dimension(80, 26));
// // outlinesp.add(tfConvThreshold);
// // middle.add(outlines);
// // }
// // {
// // JPanel outlines = new JPanel();
// // outlines.setBorder(BorderFactory.createTitledBorder("Maxima plot"));
// // outlines.setLayout(new BorderLayout());
// // chPlotStaticmax = new JCheckBox("Show");
// // chPlotStaticmax.setBackground(new Color(255, 255, 102));
// // outlines.add(chPlotStaticmax, BorderLayout.NORTH);
// // JPanel outlinesp = new JPanel();
// // GridLayout g = new GridLayout(3, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // outlinesp.setLayout(g);
// // outlines.add(outlinesp, BorderLayout.CENTER);
// // chStaticAverimage = new JCheckBox("Aver. plot");
// // outlinesp.add(chStaticAverimage);
// // outlinesp.add(new JLabel(" "));
// // chStaticPlotmax = new JCheckBox("Plot maxi");
// // outlinesp.add(chStaticPlotmax);
// // outlinesp.add(new JLabel(" "));
// // chStaticPlottrack = new JCheckBox("Plot tracks");
// // outlinesp.add(chStaticPlottrack);
// // middle.add(outlines);
// // }
// // {
// // JPanel outlines = new JPanel();
// // outlines.setBorder(BorderFactory.createTitledBorder("Dynamic plot"));
// // outlines.setLayout(new BorderLayout());
// // chPlotDynamicmax = new JCheckBox("Show");
// // chPlotDynamicmax.setBackground(new Color(255, 255, 102));
// // outlines.add(chPlotDynamicmax, BorderLayout.NORTH);
// // JPanel outlinesp = new JPanel();
// // GridLayout g = new GridLayout(3, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // outlinesp.setLayout(g);
// // outlines.add(outlinesp, BorderLayout.CENTER);
// // chDynamicPlotmax = new JCheckBox("Plot maxi");
// // outlinesp.add(chDynamicPlotmax);
// // outlinesp.add(new JLabel(" "));
// // chDynamicPlottrack = new JCheckBox("Plot tracks");
// // outlinesp.add(chDynamicPlottrack);
// // outlinesp.add(new JLabel(" "));
// // middle.add(outlines);
// // }
// // {
// // // Polar plots
// // JPanel outlines = new JPanel();
// // outlines.setBorder(BorderFactory.createTitledBorder("Polar plot"));
// // outlines.setLayout(new BorderLayout());
// // chPlotPolarplot = new JCheckBox("Save");
// // chPlotPolarplot.setBackground(new Color(255, 255, 102));
// // outlines.add(chPlotPolarplot, BorderLayout.NORTH);
// // JPanel outlinesp = new JPanel();
// // GridLayout g = new GridLayout(3, 2);
// // g.setHgap(2);
// // g.setVgap(2);
// // outlinesp.setLayout(g);
// // outlines.add(outlinesp, BorderLayout.CENTER);
// // bnGradient = new JButton("Pick grad");
// // bnGradient.addActionListener(this);
// // outlinesp.add(bnGradient);
// // chUseGradient = new JCheckBox("Use grad");
// // chUseGradient.setEnabled(false);
// // chUseGradient.setToolTipText("Not implemented");
// // outlinesp.add(chUseGradient);
// // outlinesp.add(new JLabel(" "));
// // outlinesp.add(new JLabel(" "));
// // middle.add(outlines);
// // }
// //
// // // cancel apply row
// // JPanel caButtons = new JPanel();
// // caButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
// // bnApply = new JButton("Apply");
// // bnApply.addActionListener(this);
// // bnCancel = new JButton("Cancel");
// // bnCancel.addActionListener(this);
// // bnHelp = new JButton("Help");
// // bnHelp.addActionListener(this);
// // caButtons.add(bnApply);
// // caButtons.add(bnCancel);
// // caButtons.add(bnHelp);
// // wndpanel.add(caButtons, BorderLayout.SOUTH);
// //
// // wnd.add(wndpanel);
// // wnd.pack();
// // wnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
// // }
//
// /**
// * Get model object.
// *
// * @return the model
// */
// protected Prot_Analysis getModel() {
// return model;
// }
//
// // /**
// // * Open window with custom Canvas that allows user to click point.
// // *
// // * @param img Image to show, can be stack.
// // * @see CustomCanvas
// // */
// // public void getGradient(ImagePlus img) {
// // if (img == null) {
// // return;
// // }
// // // cut one slice from stack
// // ImagePlus copy = img.duplicate();
// // ImageStack is = copy.getImageStack();
// // ImagePlus single = new ImagePlus("", is.getProcessor(1));
// // // open the window
// // new ImageWindow(single, new CustomCanvas(single)).setVisible(true);
// //
// // }
//
// @Override
// public void actionPerformed(ActionEvent e) {
// if (e.getSource() == bnApply) {
// readUI(); // get ui values to config class
// try {
// model.runPlugin();
// } catch (Exception ex) { // catch all exceptions here
// LOGGER.debug(ex.getMessage(), ex);
// LOGGER.error("Problem with running of Protrusion Analysis mapping: " + ex.getMessage());
// }
// }
// if (e.getSource() == bnCancel) {
// throw new NotImplementedException("Removed");
// }
// if (e.getSource() == bnHelp) {
// String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
// try {
// java.awt.Desktop.getDesktop().browse(new URI(url));
// } catch (Exception e1) {
// LOGGER.debug(e1.getMessage(), e1);
// LOGGER.error("Could not open help: " + e1.getMessage(), e1);
// }
// }
// if (e.getSource() == bnGradient) {
// // getGradient(model.getQconfLoader().getImage());
// }
// }
//
// /**
// * Update ProtAnalysisConfig.gradientPosition to actual clicked point on image.
// *
// * <p>Used during displaying frame to allow user to pick desired gradient point.
// *
// * @author p.baniukiewicz
// *
// */
// // class CustomCanvas extends ImageCanvas {
// // private static final long serialVersionUID = 1L;
// //
// // public CustomCanvas(ImagePlus imp) {
// // super(imp);
// // }
// //
// // /*
// // * (non-Javadoc)
// // *
// // * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
// // */
// // @Override
// // public void mousePressed(MouseEvent e) {
// // ProtAnalysisOptions config = (ProtAnalysisOptions) model.getOptions();
// // super.mousePressed(e);
// // LOGGER.debug("Image coords: " + offScreenX(e.getX()) + " " + offScreenY(e.getY()));
// // config.polarPlot.type = GradientType.SCREENPOINT;
// // config.polarPlot.gradientPoint = new Point2d(offScreenX(e.getX()), offScreenY(e.getY()));
// // writeUI(); // update UI
// // }
// //
// // }
// }