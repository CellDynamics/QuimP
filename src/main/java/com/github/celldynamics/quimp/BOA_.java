package com.github.celldynamics.quimp;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState.BOAp;
import com.github.celldynamics.quimp.SnakePluginList.Plugin;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.DataContainerInstanceCreator;
import com.github.celldynamics.quimp.filesystem.FileDialogEx;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.filesystem.StatsCollection;
import com.github.celldynamics.quimp.filesystem.versions.Converter170202;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.plugin.IQuimpCorePlugin;
import com.github.celldynamics.quimp.plugin.IQuimpPluginAttachImage;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.binaryseg.BinarySegmentationPlugin;
import com.github.celldynamics.quimp.plugin.engine.PluginFactory;
import com.github.celldynamics.quimp.plugin.engine.PluginProperties;
import com.github.celldynamics.quimp.plugin.snakes.IQuimpBOAPoint2dFilter;
import com.github.celldynamics.quimp.plugin.snakes.IQuimpBOASnakeFilter;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;
import com.github.celldynamics.quimp.registration.Registration;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;
import com.github.celldynamics.quimp.utils.QuimpToolsCollection;
import com.github.celldynamics.quimp.utils.graphics.GraphicsElements;
import com.google.gson.JsonSyntaxException;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.NewImage;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.gui.YesNoCancelDialog;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.Blitter;
import ij.process.FloatPolygon;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

/**
 * Main class implementing BOA plugin.
 * 
 * @author Richard Tyson
 * @author Till Bretschneider
 * @author Piotr Baniukiewicz
 */
public class BOA_ implements PlugIn {
  private static final Logger LOGGER = LoggerFactory.getLogger(BOA_.class.getName());

  /**
   * Indicate that {@link com.github.celldynamics.quimp.BOA_#runBoa(int, int)} is active.
   * 
   * <p>This method calls {@link com.github.celldynamics.quimp.ImageGroup#setIpSliceAll(int)} that
   * raises event
   * {@link com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateSliceSelector()} which then
   * fire other methods.
   */
  boolean isSegRunning = false;

  /**
   * Used for breaking segmentation if Cancel is hit. If true segmentation is stopped.
   * 
   * @see #runBoa(int, int)
   */
  private boolean isSegBreakHit = false;

  /**
   * The canvas.
   */
  CustomCanvas canvas;

  /**
   * The window.
   */
  CustomStackWindow window;

  /**
   * The log area.
   */
  static TextArea logArea;

  /**
   * Indicate if the BOA plugin is run.
   */
  static boolean isBoaRunning = false;

  /**
   * The image group.
   */
  ImageGroup imageGroup;
  private Constrictor constrictor;
  private PluginFactory pluginFactory; // load and maintain plugins
  /**
   * Last selection tool selected in IJ.
   * 
   * <p>remember last tool to reselect it after truncating or
   * deleting operation
   */
  private String lastTool;
  /**
   * Reserved word that stands for plugin that is not selected.
   */
  public static final String NONE = "NONE";
  /**
   * Reserved word that states full view zoom in zoom choice. Also default text that appears there
   */
  private static final String fullZoom = "Frame zoom";
  /**
   * Hold current BOA object and provide access to only selected methods from plugin.
   * 
   * <p>Reference to this field is passed to plugins and give them possibility to call selected
   * methods from BOA class
   */
  public static ViewUpdater viewUpdater;
  /**
   * Keep data from getQuimPBuildInfo().
   * 
   * <p>These information are used in About dialog, window title bar, logging, etc. Static because
   * window related staff is in another classes.
   */
  public static QuimpVersion quimpInfo;
  private static int logCount; // add counter to logged messages
  /**
   * Number of Snake plugins available.
   */
  public static final int NUM_SNAKE_PLUGINS = 3;
  private HistoryLogger historyLogger; // logger
  /**
   * Configuration object, available from all modules.
   * 
   * <p>Must be initialised here <b>AND</b> in constructor (to reset settings on next BOA call
   * without quitting Fiji) Keep data that will be serialized.
   */
  public static BOAState qState = new BOAState(null); // current state of BOA module

  /**
   * Main constructor.
   * 
   * <p>All static resources should be re-initialized here, otherwise they persist in memory between
   * subsequent BOA calls from Fiji.
   */
  public BOA_() {
    LOGGER.trace("Constructor called");
    qState = new BOAState(null);
    logCount = 1; // reset log count (it is also static)
    // log4j.configurationFile
  }

  /**
   * Main method called from Fiji. Initialises internal BOA structures.
   * 
   * @param arg Currently it can be string pointing to plugins directory
   * @see #setup(ImagePlus)
   */
  @Override
  public void run(final String arg) {
    if (IJ.versionLessThan("1.45")) {
      return;
    }

    if (BOA_.isBoaRunning) {
      BOA_.isBoaRunning = false;
      IJ.error("Warning: Only have one instance of BOA running at a time");
      return;
    }
    // assign current object to ViewUpdater
    viewUpdater = new ViewUpdater(this);
    // collect information about quimp version read from jar
    quimpInfo = QuimP.TOOL_VERSION;
    // create history logger
    historyLogger = new HistoryLogger();

    // Build plugin engine
    try {
      String path;
      if (QuimP.PLUGIN_DIR == null) {
        path = IJ.getDirectory("plugins");
      } else {
        path = QuimP.PLUGIN_DIR;
      }
      if (path == null) {
        IJ.log("BOA: Plugin directory not found");
        LOGGER.warn("BOA: Plugin directory not found, use provided with arg: " + arg);
        path = arg;
      }
      // initialize plugin factory (jar scanning and registering)
      pluginFactory = PluginFactoryFactory.getPluginFactory(path);
    } catch (Exception e) {
      // temporary catching may in future be removed
      LOGGER.error("run: " + e.getMessage());
      LOGGER.debug(e.getMessage(), e);
      return;
    }

    ImagePlus ip = WindowManager.getCurrentImage();
    // Initialise arrays for plugins instances and give them initial values (GUI)
    qState = new BOAState(ip, pluginFactory, viewUpdater); // create BOA state machine
    if (IJ.getVersion().compareTo("1.46") < 0) {
      qState.boap.useSubPixel = false;
    } else {
      qState.boap.useSubPixel = true;
    }

    lastTool = IJ.getToolName();
    // stack or single image?
    if (ip == null || ip.getNChannels() > 1) {
      IJ.error("Single channel image required");
      return;
    } else if (ip.getStackSize() == 1) {
      qState.boap.singleImage = true;
    } else {
      qState.boap.singleImage = false;
    }
    // check if 8-bit image
    if (ip.getType() != ImagePlus.GRAY8) {
      YesNoCancelDialog ync =
              new YesNoCancelDialog(window, "Image bit depth", "8-bit Image required. Convert?");
      if (ync.yesPressed()) {
        if (qState.boap.singleImage) {
          new ImageConverter(ip).convertToGray8();
        } else {
          new StackConverter(ip).convertToGray8();
        }
      } else {
        return;
      }
    }
    BOA_.isBoaRunning = true;
    setup(ip); // create main objects in BOA and BOAState, build window + registration window

    if (qState.boap.useSubPixel == false) {
      BOA_.log("Upgrade to ImageJ 1.46, or higher," + "\nto get sub-pixel editing.");
    }
    if (IJ.getVersion().compareTo("1.49a") > 0) {
      BOA_.log("(ImageJ " + IJ.getVersion() + " untested)");
    }

    try {
      if (!qState.nest.isVacant()) {
        runBoa(1, 1);
      }
    } catch (BoaException be) {
      BOA_.log("RUNNING BOA...inital preview failed");
      BOA_.log(be.getMessage());
      be.printStackTrace();
    }
  }

  /**
   * Build all BOA windows and setup initial parameters for segmentation Define also
   * windowListener for cleaning after closing the main window by user.
   * 
   * @param ip Reference to image to be processed by BOA
   * @see BOAp
   */
  void setup(final ImagePlus ip) {
    if (qState.boap.paramsExist == null) {
      qState.segParam.setDefaults();
    }
    qState.boap.setup(ip);

    qState.nest = new Nest();
    imageGroup = new ImageGroup(ip, qState.nest);
    qState.boap.frame = 1;
    // build window and set its title
    canvas = new CustomCanvas(imageGroup.getOrgIpl());
    window = new CustomStackWindow(imageGroup.getOrgIpl(), canvas);
    window.buildWindow();
    window.setTitle(window.getTitle() + " :QuimP: " + quimpInfo.getVersion());
    // validate registered user
    new Registration(window, "QuimP Registration");
    // warn about scale - if it was adjusted in BOAState constructor
    if (qState.boap.isScaleAdjusted()) {
      BOA_.log("WARNING Scale was zero - set to 1");
    }
    if (qState.boap.isfIAdjusted()) {
      BOA_.log("WARNING Frame interval was zero - set to 1");
    }

    // adds window listener called on plugin closing
    window.addWindowListener(new CustomWindowAdapter());

    setScales(); // ask user for scales and set them
    updateImageScale();
    window.setScalesText();

    // check for ROIs - Use as cells
    new RoiManager(); // get open ROI manager, or create a new one
    RoiManager rm = RoiManager.getInstance();
    if (rm.getRoisAsArray().length != 0) {
      qState.nest.addHandlers(rm.getRoisAsArray(), 1);
    } else {
      BOA_.log("No cells from ROI manager");
      if (ip.getRoi() != null) {
        qState.nest.addHandler(ip.getRoi(), 1);
      } else {
        BOA_.log("No cells from selection");
      }
    }
    rm.close();
    ip.killRoi();

    constrictor = new Constrictor(); // does computations on snakes
  }

  /**
   * Display about information in BOA window. Called from menu bar. Reads also information from all
   * found plugins.
   */
  void about() {
    AboutDialog ad = new AboutDialog(window); // create about dialog with parent 'window'
    ad.appendLine(QuimpToolsCollection.getFormattedQuimPversion(quimpInfo)); // display template
    ad.appendLine("List of found plugins:");
    ad.appendDistance(); // type ----
    Map<String, PluginProperties> mp = pluginFactory.getRegisterdPlugins();
    // iterate over set
    for (Map.Entry<String, PluginProperties> entry : mp.entrySet()) {
      ad.appendLine("Plugin name: " + entry.getKey());
      ad.appendLine("   Plugin type: " + entry.getValue().getType());
      ad.appendLine("   Plugin path: " + entry.getValue().getFile().toString());
      ad.appendLine("   Plugin vers: " + entry.getValue().getVersion());
      // about is not stored in PluginProperties class due to optimization of memory
      ad.appendLine("   About (returned by plugin):");
      IQuimpCorePlugin tmpinst = pluginFactory.getInstance(entry.getKey());
      if (tmpinst != null) { // can be null on problem with instance
        String about = tmpinst.about(); // may return null
        if (about != null) {
          ad.appendLine(about);
        } else {
          ad.appendLine("Plugin does not provide about note");
        }
      }
      ad.appendDistance();
    }
    ad.setVisible(true); // must be after adding content
  }

  /**
   * Append string to log window in BOA plugin.
   * 
   * @param s String to display in BOA window
   */
  static void log(final String s) {
    if (logArea == null) {
      LOGGER.debug("[" + logCount++ + "] " + s + '\n');
    } else {
      logArea.append("[" + logCount++ + "] " + s + '\n');
    }
  }

  /**
   * Redraw current view. Process outlines by all active plugins. Do not run segmentation again
   * Updates liveSnake. Also disables UI.
   * 
   * <p>Strictly related to current view {@link BOAState.BOAp#frame}.
   */
  void recalculatePlugins() {
    LOGGER.trace("BOA: recalculatePlugins called");
    SnakeHandler sh;
    if (qState.nest.isVacant()) { // only update screen
      imageGroup.updateOverlay(qState.boap.frame);
      return;
    }
    imageGroup.updateToFrame(qState.boap.frame);
    try {
      for (int s = 0; s < qState.nest.size(); s++) { // for each snake
        sh = qState.nest.getHandler(s);
        if (qState.boap.frame < sh.getStartFrame()) {
          continue;
        }
        // but if one is on frame iplStack+n and strtFrame is e.g. 1 it may happen that there is
        // no continuity of this snake between frames. In this case getBackupSnake
        // returns null. In general QuimP assumes that if there is a cell on frame iplStack, it
        // will exist on all consecutive frames.
        Snake snake = sh.getBackupSnake(qState.boap.frame); // if exist get its backup copy
        // (segm)
        if (snake == null || !snake.alive) {
          continue;
        }
        try {
          Snake out = iterateOverSnakePlugins(snake); // apply all plugins to snake
          sh.storeThisSnake(out, qState.boap.frame); // set processed snake as final
        } catch (QuimpPluginException qpe) {
          // must be rewritten with whole runBOA #65 #67
          BOA_.log("Error in filter module: " + qpe.getMessage());
          LOGGER.error("Error in filter module: " + qpe.getMessage());
          LOGGER.debug(qpe.getMessage(), qpe);
          sh.storeLiveSnake(qState.boap.frame); // so store only segmented snake as final
        }
      }
    } catch (Exception e) {
      LOGGER.error("Plugin error. Output snake may be defective. Reason: " + e.getMessage());
      LOGGER.debug(e.getMessage(), e);
    } finally {
      historyLogger.addEntry("Plugin settings", qState);
      qState.store(qState.boap.frame); // always remember state of the BOA that is
    }
    imageGroup.updateOverlay(qState.boap.frame);
  }

  /**
   * Override action performed on window closing. Clear BOA._running static variable and prevent
   * to notify user that QuimP is running when it has been closed and called again.
   * 
   * <p>When user closes window by system button QuimP does not ask for saving current work. This is
   * because by default QuimP window is managed by ImageJ and it probably only hides it on closing
   * 
   * <p>This class could be located directly in CustomStackWindow which is included in BOA_. But it
   * needs to have access to BOA field <tt>running</tt>.
   * 
   * @author p.baniukiewicz
   */
  class CustomWindowAdapter extends WindowAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    // This method will be called when BOA_ window is closed already
    // It is too late for asking user
    public void windowClosed(final WindowEvent arg0) {
      LOGGER.trace("CLOSED");
      BOA_.isBoaRunning = false; // set marker
      qState.snakePluginList.clear(); // close all opened plugin windows
      if (qState.binarySegmentationPlugin != null) {
        qState.binarySegmentationPlugin.showUi(false);
      }
      canvas = null; // clear window data
      imageGroup = null;
      window = null;
      // clear static
      qState = null;
      viewUpdater = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(final WindowEvent arg0) {
      LOGGER.trace("CLOSING");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowAdapter#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(final WindowEvent e) {
      LOGGER.trace("ACTIVATED");
      // rebuild menu for this local window
      // workaround for Mac and theirs menus on top screen bar
      // IJ is doing the same for activation of its window so every time one has correct menu
      // on top
      window.setMenuBar(window.menuBar);
    }
  }

  /**
   * Supports mouse actions on image at QuimP window according to selected option.
   * 
   * @author rtyson
   *
   */
  @SuppressWarnings("serial")
  class CustomCanvas extends ImageCanvas {

    /**
     * Empty constructor.
     * 
     * @param imp Reference to image loaded by BOA
     */
    CustomCanvas(final ImagePlus imp) {
      super(imp);
    }

    /**
     * @deprecated Actually not used in this version of QuimP.
     */
    @Override
    public void paint(final Graphics g) {
      super.paint(g);
      // int size = 80;
      // int screenSize = (int)(size*getMagnification());
      // int x = screenX(imageWidth/2 - size/2);
      // int y = screenY(imageHeight/2 - size/2);
      // g.setColor(Color.red);
      // g.drawOval(x, y, screenSize, screenSize);
    }

    /**
     * Implement mouse action on image loaded to BOA Used for manual editions of segmented
     * shape. Define reactions of mouse buttons according to GUI state, set by \b Delete and \b
     * Edit buttons.
     * 
     * @see BOAp
     * @see CustomStackWindow
     */
    @Override
    public void mousePressed(final MouseEvent e) {
      super.mousePressed(e);
      if (qState.boap.doDelete) {
        // BOA_.log("Delete at:
        // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
        deleteCell(offScreenX(e.getX()), offScreenY(e.getY()), qState.boap.frame);
        IJ.setTool(lastTool);
      }
      if (qState.boap.doDeleteSeg) {
        // BOA_.log("Delete at:
        // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
        deleteSegmentation(offScreenX(e.getX()), offScreenY(e.getY()), qState.boap.frame);
      }
      if (qState.boap.editMode && qState.boap.editingID == -1) {
        // BOA_.log("Delete at:
        // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
        editSeg(offScreenX(e.getX()), offScreenY(e.getY()), qState.boap.frame);
      }
    }
  } // end of CustomCanvas

  /**
   * Extends standard ImageJ StackWindow adding own GUI elements.
   * 
   * <p>This class stands for definition of main BOA plugin GUI window. Current state of BOA plugin
   * is stored at {@link com.github.celldynamics.quimp.BOAState.BOAp} class.
   * 
   * @author rtyson
   * @see BOAp
   */
  @SuppressWarnings("serial")
  class CustomStackWindow extends StackWindow
          implements ActionListener, ItemListener, ChangeListener {

    /**
     * The Constant DEFAULT_SPINNER_SIZE.
     */
    static final int DEFAULT_SPINNER_SIZE = 5;

    /**
     * Number of currently supported plugins.
     */
    static final int SNAKE_PLUGIN_NUM = 3;
    /**
     * Any worker that run thread for boa or plugins will be referenced here.
     * 
     * @see #runBoaThread(int, int, boolean)
     * @see #populatePlugins(List)
     */
    private SwingWorker<Boolean, Object> sww = null;
    /**
     * Block rerun of runBoa() when spinners have been changed programmatically.
     * 
     * <p>Modification of spinners from code causes that stateChanged() event is called.
     */
    private boolean supressStateChangeBOArun = false;
    private Button bnSeg; // also play role of Cancel button
    private Button bnFinish;
    private Button bnLoad;
    private Button bnEdit;
    private Button bnQuit;
    private Button bnDefault;
    private Button bnScale;
    private Button bnCopyLast;

    private Button bnAdd;
    private Button bnDel;
    private Button bnDelSeg;

    private Checkbox cbPrevSnake;
    private Checkbox cbExpSnake;
    private Checkbox cbPath;
    private Choice chZoom;

    /**
     * The log panel.
     */
    JScrollPane logPanel;

    private Label fpsLabel;
    private Label pixelLabel;
    private Label frameLabel;

    private JSpinner dsNodeRes;
    private JSpinner dsVelCrit;
    private JSpinner dsFImage;
    private JSpinner dsFCentral;
    private JSpinner dsFContract;
    private JSpinner dsFinalShrink;

    private JSpinner isMaxIterations;
    private JSpinner isBlowup;
    private JSpinner isSampletan;
    private JSpinner isSamplenorm;
    private Choice chFirstPluginName;
    private Choice chSecondPluginName;
    private Choice chThirdPluginName;
    private Button bnFirstPluginGUI;
    private Button bnSecondPluginGUI;
    private Button bnThirdPluginGUI;
    private Checkbox cbFirstPluginActiv;
    private Checkbox cbSecondPluginActiv;
    private Checkbox cbThirdPluginActiv;
    private Button bnPopulatePlugin; // same as menuPopulatePlugin
    private Button bnCopyLastPlugin;

    private MenuBar menuBar; // main menu bar
    private MenuItem menuAbout;
    private MenuItem menuOpenHelp;
    private MenuItem menuSaveConfig;
    private MenuItem menuLoadConfig;
    private MenuItem menuShowHistory;
    private MenuItem menuLoad;
    private MenuItem menuDeletePlugin;
    private MenuItem menuApplyPlugin;
    private MenuItem menuSegmentationRun;
    private MenuItem menuSegmentationReset; // items
    private CheckboxMenuItem cbMenuPlotOriginalSnakes;
    private CheckboxMenuItem cbMenuPlotHead;

    private MenuItem menuPopulatePlugin;

    /**
     * Default constructor.
     * 
     * @param imp Image loaded to plugin
     * @param ic Image canvas
     */
    CustomStackWindow(final ImagePlus imp, final ImageCanvas ic) {
      super(imp, ic);

    }

    /**
     * Enables or disables all UI controls.
     * 
     * @param state true for enabled, false for disabled.
     */
    public void enableUi(boolean state) {
      bnSeg.setEnabled(state);
      bnFinish.setEnabled(state);
      bnLoad.setEnabled(state);
      bnEdit.setEnabled(state);
      bnQuit.setEnabled(state);
      bnDefault.setEnabled(state);
      bnScale.setEnabled(state);
      bnCopyLast.setEnabled(state);

      bnAdd.setEnabled(state);
      bnDel.setEnabled(state);
      bnDelSeg.setEnabled(state);

      cbPrevSnake.setEnabled(state);
      cbExpSnake.setEnabled(state);
      cbPath.setEnabled(state);
      chZoom.setEnabled(state);

      dsNodeRes.setEnabled(state);
      dsVelCrit.setEnabled(state);
      dsFImage.setEnabled(state);
      dsFCentral.setEnabled(state);
      dsFContract.setEnabled(state);
      dsFinalShrink.setEnabled(state);

      isMaxIterations.setEnabled(state);
      isBlowup.setEnabled(state);
      isSampletan.setEnabled(state);
      isSamplenorm.setEnabled(state);
      chFirstPluginName.setEnabled(state);
      chSecondPluginName.setEnabled(state);
      chThirdPluginName.setEnabled(state);
      bnFirstPluginGUI.setEnabled(state);
      bnSecondPluginGUI.setEnabled(state);
      bnThirdPluginGUI.setEnabled(state);
      cbFirstPluginActiv.setEnabled(state);
      cbSecondPluginActiv.setEnabled(state);
      cbThirdPluginActiv.setEnabled(state);
      bnPopulatePlugin.setEnabled(state); // same as menuPopulatePlugin
      bnCopyLastPlugin.setEnabled(state);
      for (int i = 0; i < menuBar.getMenuCount(); i++) {
        menuBar.getMenu(i).setEnabled(state);
      }
    }

    /**
     * Similar to {@link #enableUi(boolean)} but always enables cancel button.
     * 
     * @param state true for enabled, false for disabled.
     */
    public void enableUiInterruptile(boolean state) {
      enableUi(state);
      bnSeg.setEnabled(true);
    }

    /**
     * Build user interface.
     * 
     * <p>This method is called as first. The interface is built in three steps: Left side of
     * window (configuration zone) and right side of main window (logs and other info and
     * buttons) and finally upper menubar
     * 
     * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateWindowState()
     */
    public void buildWindow() {

      setLayout(new BorderLayout(10, 3));

      if (!qState.boap.singleImage) {
        remove(sliceSelector);
      }
      if (!qState.boap.singleImage) {
        remove(this.getComponent(1)); // remove the play/pause button
      }
      Panel cp = buildControlPanel();
      Panel sp = buildSetupPanel();
      add(new Label(""), BorderLayout.NORTH);
      add(cp, BorderLayout.WEST); // add to the left, position 0
      add(ic, BorderLayout.CENTER);
      add(sp, BorderLayout.EAST);
      add(new Label(""), BorderLayout.SOUTH);

      LOGGER.debug("Menu: " + getMenuBar());
      menuBar = buildMenu(); // store menu in var to reuse on window activation
      setMenuBar(menuBar);
      pack();
      updateWindowState(); // window logic on start
    }

    /**
     * Build window menu.
     * 
     * <p>Menu is local for this window of QuimP and it is stored in \c quimpMenuBar variable. On
     * every time when QuimP is active, this menu is restored in
     * com.github.celldynamics.quimp.BOA_.CustomWindowAdapter.windowActivated(WindowEvent) method
     * This is due to overwriting menu by IJ on Mac (all menus are on top screen bar)
     * 
     * @return Reference to menu bar
     */
    final MenuBar buildMenu() {
      Menu menuHelp; // menu About in menubar
      Menu menuConfig; // menu Config in menubar
      Menu menuFile; // menu File in menubar
      Menu menuPlugin; // menu Plugin in menubar

      menuBar = new MenuBar();

      menuConfig = new Menu("Preferences");
      menuHelp = new Menu("Help");
      menuFile = new Menu("File");
      menuPlugin = new Menu("Plugin");
      Menu menuSegmentation; // menu Segmentation in menubar
      menuSegmentation = new Menu("Segmentation");

      // build main line
      menuBar.add(menuFile);
      menuBar.add(menuConfig);
      menuBar.add(menuPlugin);
      menuBar.add(menuSegmentation);
      menuBar.add(menuHelp);

      // add entries
      menuLoad = new MenuItem("Load global config");
      menuLoad.addActionListener(this);
      menuFile.add(menuLoad);
      menuFile.addSeparator();
      menuLoadConfig = new MenuItem("Load plugin preferences");
      menuLoadConfig.addActionListener(this);
      menuFile.add(menuLoadConfig);
      menuSaveConfig = new MenuItem("Save plugin preferences");
      menuSaveConfig.addActionListener(this);
      menuFile.add(menuSaveConfig);

      menuOpenHelp = new MenuItem("Help Contents");
      menuOpenHelp.addActionListener(this);
      menuHelp.add(menuOpenHelp);
      menuAbout = new MenuItem("About");
      menuAbout.addActionListener(this);
      menuHelp.add(menuAbout);

      cbMenuPlotOriginalSnakes = new CheckboxMenuItem("Plot original");
      cbMenuPlotOriginalSnakes.setState(qState.boap.isProcessedSnakePlotted);
      cbMenuPlotOriginalSnakes.addItemListener(this);
      menuConfig.add(cbMenuPlotOriginalSnakes);
      cbMenuPlotHead = new CheckboxMenuItem("Plot head");
      cbMenuPlotHead.setState(qState.boap.isHeadPlotted);
      cbMenuPlotHead.addItemListener(this);
      menuConfig.add(cbMenuPlotHead);

      menuShowHistory = new MenuItem("Show history");
      menuShowHistory.addActionListener(this);
      menuConfig.add(menuShowHistory);

      menuDeletePlugin = new MenuItem("Discard all");
      menuDeletePlugin.addActionListener(this);
      menuPlugin.add(menuDeletePlugin);
      menuApplyPlugin = new MenuItem("Re-apply all");
      menuApplyPlugin.addActionListener(this);
      menuPlugin.add(menuApplyPlugin);
      menuPopulatePlugin = new MenuItem("Populate to all frames");
      menuPopulatePlugin.addActionListener(this);
      menuPlugin.add(menuPopulatePlugin);

      menuSegmentationRun = new MenuItem("Binary segmentation");
      menuSegmentationRun.addActionListener(this);
      menuSegmentationReset = new MenuItem("Clear all");
      menuSegmentationReset.addActionListener(this);
      menuSegmentation.add(menuSegmentationRun);
      menuSegmentation.add(menuSegmentationReset);

      return menuBar;
    }

    /**
     * Build right side of main BOA window.
     * 
     * @return Reference to panel
     */
    final Panel buildSetupPanel() {
      Panel setupPanel = new Panel(); // Main panel comprised from North, Centre and South subpanels
      Panel northPanel = new Panel(); // Contains static info and four buttons (Scale, Truncate, etc
      Panel southPanel = new Panel(); // Quit and Finish
      Panel centerPanel = new Panel();
      Panel pluginPanelButtons = new Panel(); // buttons below plugins

      setupPanel.setLayout(new BorderLayout());
      northPanel.setLayout(new GridLayout(3, 2));
      southPanel.setLayout(new GridLayout(2, 2));
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));

      // plugins buttons
      pluginPanelButtons.setLayout(new GridLayout(1, 2)); // here is number of buttons
      bnPopulatePlugin = addButton("Populate fwd", pluginPanelButtons);
      bnCopyLastPlugin = addButton("Copy prev", pluginPanelButtons);

      // Grid bag for plugin zone
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      c.weightx = 0.5;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.LINE_START;
      Panel pluginPanel = new Panel();
      pluginPanel.setLayout(gridbag);

      fpsLabel = new Label("F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
      northPanel.add(fpsLabel);
      pixelLabel = new Label("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
      northPanel.add(pixelLabel);

      bnScale = addButton("Set Scale", northPanel);
      bnDelSeg = addButton("Truncate Seg", northPanel);
      bnAdd = addButton("Add cell", northPanel);
      bnDel = addButton("Delete cell", northPanel);

      // build subpanel with plugins
      // get plugins names collected by PluginFactory
      ArrayList<String> pluginList =
              qState.snakePluginList.getPluginNames(IQuimpCorePlugin.DOES_SNAKES);
      // add NONE to list
      pluginList.add(0, NONE);

      // plugins are recognized by their names returned from pluginFactory.getPluginNames() so
      // if there is no names, it is not possible to call nonexisting plugins, because calls
      // are made using plugin names. see actionPerformed. If plugin of given name (NONE) is
      // not found getInstance return null which is stored in SnakePluginList and checked
      // during run
      // default values for plugin activity are stored in SnakePluginList
      chFirstPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
      c.gridx = 0;
      c.gridy = 0;
      pluginPanel.add(chFirstPluginName, c);
      bnFirstPluginGUI = addButton("GUI", pluginPanel);
      c.gridx = 1;
      c.gridy = 0;
      pluginPanel.add(bnFirstPluginGUI, c);
      cbFirstPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(0));
      c.gridx = 2;
      c.gridy = 0;
      pluginPanel.add(cbFirstPluginActiv, c);

      chSecondPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
      c.gridx = 0;
      c.gridy = 1;
      pluginPanel.add(chSecondPluginName, c);
      bnSecondPluginGUI = addButton("GUI", pluginPanel);
      c.gridx = 1;
      c.gridy = 1;
      pluginPanel.add(bnSecondPluginGUI, c);
      cbSecondPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(1));
      c.gridx = 2;
      c.gridy = 1;
      pluginPanel.add(cbSecondPluginActiv, c);

      chThirdPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
      c.gridx = 0;
      c.gridy = 2;
      pluginPanel.add(chThirdPluginName, c);
      bnThirdPluginGUI = addButton("GUI", pluginPanel);
      c.gridx = 1;
      c.gridy = 2;
      pluginPanel.add(bnThirdPluginGUI, c);
      cbThirdPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(2));
      c.gridx = 2;
      c.gridy = 2;
      pluginPanel.add(cbThirdPluginActiv, c);

      c.gridx = 0;
      c.gridy = 3;
      c.gridwidth = 3;
      c.fill = GridBagConstraints.HORIZONTAL;
      pluginPanel.add(pluginPanelButtons, c);

      // --------build log---------
      Panel tp = new Panel(); // panel with text area
      tp.setLayout(new GridLayout(1, 1));
      logArea = new TextArea(15, 15);
      logArea.setEditable(false);
      tp.add(logArea);
      logPanel = new JScrollPane(tp);

      // ------------------------------

      // --------build south--------------
      southPanel.add(new Label("")); // blankes
      southPanel.add(new Label("")); // blankes
      bnQuit = addButton("Quit", southPanel);
      bnFinish = addButton("Save & Quit", southPanel);
      // ------------------------------

      centerPanel.add(new Label("Snake Plugins:"));
      centerPanel.add(pluginPanel);
      centerPanel.add(new Label("Logs:"));
      centerPanel.add(logPanel);
      setupPanel.add(northPanel, BorderLayout.PAGE_START);
      setupPanel.add(centerPanel, BorderLayout.CENTER);
      setupPanel.add(southPanel, BorderLayout.PAGE_END);

      if (pluginList.isEmpty()) {
        BOA_.log("No plugins found");
      } else {
        BOA_.log("Found " + (pluginList.size() - 1) + " plugins (see About)");
      }

      return setupPanel;
    }

    /**
     * Build left side of main BOA window.
     * 
     * @return Reference to built panel
     */
    final Panel buildControlPanel() {
      Panel controlPanel = new Panel();
      Panel topPanel = new Panel();
      Panel paramPanel = new Panel();
      Panel bottomPanel = new Panel();

      controlPanel.setLayout(new BorderLayout());
      topPanel.setLayout(new GridLayout(2, 2));
      paramPanel.setLayout(new GridLayout(14, 1));
      bottomPanel.setLayout(new GridLayout(1, 2));

      // --------build topPanel--------
      bnLoad = addButton("Load", topPanel);
      bnDefault = addButton("Default", topPanel);
      bnCopyLast = addButton("Copy prev", topPanel);
      // -----------------------

      // --------build paramPanel--------------
      dsNodeRes = addDoubleSpinner("Node Spacing:", paramPanel, qState.segParam.getNodeRes(), 1.,
              20., 0.2, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      isMaxIterations = addIntSpinner("Max Iterations:", paramPanel, qState.segParam.max_iterations,
              100, 10000, 100, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      isBlowup = addIntSpinner("Blowup:", paramPanel, qState.segParam.blowup, 0, 200, 2,
              CustomStackWindow.DEFAULT_SPINNER_SIZE);
      dsVelCrit = addDoubleSpinner("Crit velocity:", paramPanel, qState.segParam.vel_crit, 0.0001,
              2., 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      dsFImage = addDoubleSpinner("Image F:", paramPanel, qState.segParam.f_image, 0.01, 10., 0.01,
              CustomStackWindow.DEFAULT_SPINNER_SIZE);
      dsFCentral = addDoubleSpinner("Central F:", paramPanel, qState.segParam.f_central, 0.0005, 1,
              0.002, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      dsFContract = addDoubleSpinner("Contract F:", paramPanel, qState.segParam.f_contract, 0.001,
              1, 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      dsFinalShrink = addDoubleSpinner("Final Shrink:", paramPanel, qState.segParam.finalShrink,
              -100, 100, 0.5, CustomStackWindow.DEFAULT_SPINNER_SIZE);
      isSampletan = addIntSpinner("Sample tan:", paramPanel, qState.segParam.sample_tan, 1, 30, 1,
              CustomStackWindow.DEFAULT_SPINNER_SIZE);
      isSamplenorm = addIntSpinner("Sample norm:", paramPanel, qState.segParam.sample_norm, 1, 60,
              1, CustomStackWindow.DEFAULT_SPINNER_SIZE);

      cbPrevSnake =
              addCheckbox("Use Previouse Snake", paramPanel, qState.segParam.use_previous_snake);
      cbExpSnake = addCheckbox("Expanding Snake", paramPanel, qState.segParam.expandSnake);

      Panel segEditPanel = new Panel();
      segEditPanel.setLayout(new GridLayout(1, 2));
      bnSeg = addButton("SEGMENT", segEditPanel);
      bnEdit = addButton("Edit", segEditPanel);
      paramPanel.add(segEditPanel);

      // mini panel comprised from slice selector and frame number (if not single image)
      Panel sliderPanel = new Panel();
      sliderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

      if (!qState.boap.singleImage) {
        sliceSelector.setPreferredSize(new Dimension(165, 20));
        sliceSelector.addAdjustmentListener(this);
        sliderPanel.add(sliceSelector);
        // frame number on right of slice selector
        frameLabel = new Label(imageGroup.getOrgIpl().getSlice() + "  ");
        sliderPanel.add(frameLabel);
      }
      paramPanel.add(sliderPanel);
      // ----------------------------------

      // -----build bottom panel---------
      cbPath = addCheckbox("Show paths", bottomPanel, qState.segParam.showPaths);
      chZoom = addComboBox(new String[] { fullZoom }, bottomPanel);
      // add mouse listener to create menu dynamically on click
      chZoom.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          LOGGER.trace("EVENT:mousePressed");
          fillZoomChoice();
        }
      });
      // -------------------------------
      // build control panel

      controlPanel.add(topPanel, BorderLayout.NORTH);
      controlPanel.add(paramPanel, BorderLayout.CENTER);
      controlPanel.add(bottomPanel, BorderLayout.SOUTH);

      return controlPanel;
    }

    /**
     * Rebuild Zoom choice UI according to cells on current frame {@link BOAp#frame}.
     * 
     * <p>According to #193 if there is no cell left it creates empty entry and set it selected to
     * enforce user to set explicitly default unzoom value and fire itemStateChanged.
     */
    private void fillZoomChoice() {
      String prev = chZoom.getSelectedItem();
      LOGGER.trace(prev);
      chZoom.removeAll();
      chZoom.add(fullZoom); // default word for full zoom (100% of view)
      List<Integer> frames = qState.nest.getSnakesforFrame(qState.boap.frame);
      for (Integer i : frames) {
        chZoom.add(i.toString());
      }
      if (chZoom.getItemCount() == 1) { // dirty trick to enforce triggering itemStateChanged (#193)
        chZoom.add("");
        chZoom.select("");
      } else {
        chZoom.select(prev); // select last selected (if exists)
      }
    }

    /**
     * Helper method for adding buttons to UI. Creates UI element and adds it to panel
     * 
     * @param label Label on button
     * @param p Reference to the panel where button is located
     * @return Reference to created button
     */
    private Button addButton(final String label, final Container p) {
      Button b = new Button(label);
      b.addActionListener(this);
      p.add(b);
      return b;
    }

    /**
     * Helper method for creating checkbox in UI.
     * 
     * @param label Label of checkbox
     * @param p Reference to the panel where checkbox is located
     * @param d Initial state of checkbox
     * @return Reference to created checkbox
     */
    private Checkbox addCheckbox(final String label, final Container p, boolean d) {
      Checkbox c = new Checkbox(label, d);
      c.addItemListener(this);
      p.add(c);
      return c;
    }

    /**
     * Helper method for creating ComboBox in UI. Creates UI element and adds it to panel
     *
     * @param s Strings to be included in ComboBox
     * @param mp Reference to the panel where ComboBox is located
     * @return Reference to created ComboBox
     */
    private Choice addComboBox(final String[] s, final Container mp) {
      Choice c = new Choice();
      for (String st : s) {
        c.add(st);
      }
      c.select(0);
      c.addItemListener(this);
      mp.add(c);
      return c;
    }

    /**
     * Helper method for creating spinner in UI with real values.
     * 
     * @param s Label of spinner (added on its left side)
     * @param mp Reference of panel where spinner is located
     * @param d The current vale of model
     * @param min The first number in sequence
     * @param max The last number in sequence
     * @param step The difference between numbers in sequence
     * @param columns The number of columns preferred for display
     * @return Reference to created spinner
     */
    private JSpinner addDoubleSpinner(final String s, final Container mp, double d, double min,
            double max, double step, int columns) {
      SpinnerNumberModel model = new SpinnerNumberModel(d, min, max, step);
      JSpinner spinner = new JSpinner(model);
      ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(columns);
      spinner.addChangeListener(this);

      Panel p = new Panel();
      p.setLayout(new FlowLayout(FlowLayout.RIGHT));
      Label label = new Label(s);
      p.add(label);
      p.add(spinner);
      mp.add(p);
      return spinner;
    }

    /**
     * Helper method for creating spinner in UI with integer values.
     * 
     * @param s Label of spinner (added on its left side)
     * @param mp Reference of panel where spinner is located
     * @param d The current vale of model
     * @param min The first number in sequence
     * @param max The last number in sequence
     * @param step The difference between numbers in sequence
     * @param columns The number of columns preferred for display
     * @return Reference to created spinner
     */
    private JSpinner addIntSpinner(final String s, final Container mp, int d, int min, int max,
            int step, int columns) {
      SpinnerNumberModel model = new SpinnerNumberModel(d, min, max, step);
      JSpinner spinner = new JSpinner(model);
      ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(columns);
      spinner.addChangeListener(this);

      Panel p = new Panel();
      p.setLayout(new FlowLayout(FlowLayout.RIGHT));
      Label label = new Label(s);
      p.add(label);
      p.add(spinner);
      mp.add(p);
      return spinner;
    }

    /**
     * Set default values defined in model class {@link com.github.celldynamics.quimp.BOAState.BOAp}
     * and update UI
     * 
     * @see BOAp
     */
    private void setDefualts() {
      qState.segParam.setDefaults();
      updateSpinnerValues();
    }

    /**
     * Update spinners in BOA UI Update spinners according to values stored in machine state
     * {@link com.github.celldynamics.quimp.BOAState.BOAp}.
     * 
     * @see BOAp
     */
    private void updateSpinnerValues() {
      // block rerun of runBoa() that is called on Spinner event
      supressStateChangeBOArun = true;
      dsNodeRes.setValue(qState.segParam.getNodeRes());
      dsVelCrit.setValue(qState.segParam.vel_crit);
      dsFImage.setValue(qState.segParam.f_image);
      dsFCentral.setValue(qState.segParam.f_central);
      dsFContract.setValue(qState.segParam.f_contract);
      dsFinalShrink.setValue(qState.segParam.finalShrink);
      isMaxIterations.setValue(qState.segParam.max_iterations);
      isBlowup.setValue(qState.segParam.blowup);
      isSampletan.setValue(qState.segParam.sample_tan);
      isSamplenorm.setValue(qState.segParam.sample_norm);
      supressStateChangeBOArun = false;
    }

    /**
     * Update checkboxes.
     * 
     * @see com.github.celldynamics.quimp.SnakePluginList
     * @see #itemStateChanged(ItemEvent)
     */
    private void updateCheckBoxes() {
      // first plugin activity
      cbFirstPluginActiv.setState(qState.snakePluginList.isActive(0));
      // second plugin activity
      cbSecondPluginActiv.setState(qState.snakePluginList.isActive(1));
      // third plugin activity
      cbThirdPluginActiv.setState(qState.snakePluginList.isActive(2));
    }

    /**
     * Update static fileds on window.
     */
    private void updateStatics() {
      setScalesText();
    }

    /**
     * Update Choices.
     * 
     * <p>This method is called from CustomStackWindow.itemStateChanged(ItemEvent) to update colors
     * of Choices.
     * 
     * @see com.github.celldynamics.quimp.SnakePluginList
     * @see #itemStateChanged(ItemEvent)
     */
    private void updateChoices() {
      final Color ok = new Color(178, 255, 102);
      final Color bad = new Color(255, 153, 153);
      // first slot snake plugin
      if (qState.snakePluginList.getName(0).isEmpty()) {
        chFirstPluginName.select(NONE);
        chFirstPluginName.setBackground(null);
      } else {
        // try to select name from pluginList in choice
        chFirstPluginName.select(qState.snakePluginList.getName(0));
        // tried selecting but still on none - it means that plugin name from snkePluginList is not
        // on choice list. Tis may happen when choice is propagated from directory
        // butsnakePluginList from external QCONF
        if (chFirstPluginName.getSelectedItem().equals(NONE)) {
          chFirstPluginName.add(qState.snakePluginList.getName(0)); // add to list
          chFirstPluginName.setBackground(bad); // set as bad
        } else if (qState.snakePluginList.getInstance(0) == null) {
          // WARN does not check instance(0) is the instance of getName(0)
          chFirstPluginName.setBackground(bad);
        } else {
          chFirstPluginName.setBackground(ok);
        }
      }
      // second slot snake plugin
      if (qState.snakePluginList.getName(1).isEmpty()) {
        chSecondPluginName.select(NONE);
        chSecondPluginName.setBackground(null);
      } else {
        chSecondPluginName.select(qState.snakePluginList.getName(1));
        if (chSecondPluginName.getSelectedItem().equals(NONE)) {
          chSecondPluginName.add(qState.snakePluginList.getName(1)); // add to list
          chSecondPluginName.setBackground(bad); // set as bad
        } else if (qState.snakePluginList.getInstance(1) == null) {
          chSecondPluginName.setBackground(bad);
        } else {
          chSecondPluginName.setBackground(ok);
        }
      }
      // third slot snake plugin
      if (qState.snakePluginList.getName(2).isEmpty()) {
        chThirdPluginName.select(NONE);
        chThirdPluginName.setBackground(null);
      } else {
        chThirdPluginName.select(qState.snakePluginList.getName(2));
        if (chThirdPluginName.getSelectedItem().equals(NONE)) {
          chThirdPluginName.add(qState.snakePluginList.getName(2)); // add to list
          chThirdPluginName.setBackground(bad); // set as bad
        } else if (qState.snakePluginList.getInstance(2) == null) {
          chThirdPluginName.setBackground(bad);
        } else {
          chThirdPluginName.setBackground(ok);
        }
      }
      // zoom choice
      if (qState.boap.snakeToZoom > -1) {
        chZoom.select(String.valueOf(qState.boap.snakeToZoom));
      }

    }

    /**
     * Implement user interface logic.
     * 
     * <p>Do not refresh values, rather disable/enable controls.
     */
    private void updateWindowState() {
      updateCheckBoxes(); // update checkboxes
      updateChoices(); // and choices
      updateStatics();

      // Rule 1 - NONE on any slot in filters disable GUI button and Active checkbox but only if
      // there is no worker working
      if (sww == null || sww.getState() == SwingWorker.StateValue.DONE) {
        if (chFirstPluginName.getSelectedItem() == NONE) {
          cbFirstPluginActiv.setEnabled(false);
          bnFirstPluginGUI.setEnabled(false);
        } else {
          cbFirstPluginActiv.setEnabled(true);
          bnFirstPluginGUI.setEnabled(true);
        }
        if (chSecondPluginName.getSelectedItem() == NONE) {
          cbSecondPluginActiv.setEnabled(false);
          bnSecondPluginGUI.setEnabled(false);
        } else {
          cbSecondPluginActiv.setEnabled(true);
          bnSecondPluginGUI.setEnabled(true);
        }
        if (chThirdPluginName.getSelectedItem() == NONE) {
          cbThirdPluginActiv.setEnabled(false);
          bnThirdPluginGUI.setEnabled(false);
        } else {
          cbThirdPluginActiv.setEnabled(true);
          bnThirdPluginGUI.setEnabled(true);
        }
      }

    }

    /**
     * Main method that handles all actions performed on UI elements.
     * 
     * <p>Do not support mouse events, only UI elements like buttons, spinners and menus. Runs also
     * main algorithm on specified input state and update screen on plugins operations.
     * 
     * @param e Type of event
     * @see com.github.celldynamics.quimp.BOAState.BOAp
     * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateWindowState()
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
      LOGGER.trace("EVENT:actionPerformed");
      boolean run = false; // some actions require to re-run segmentation. They set it to true
      Object b = e.getSource();
      if (b == bnDel && !qState.boap.editMode && !qState.boap.doDeleteSeg) {
        if (qState.boap.doDelete == false) {
          bnDel.setLabel("*STOP DEL*");
          qState.boap.doDelete = true;
          lastTool = IJ.getToolName();
          IJ.setTool(Toolbar.LINE);
        } else {
          qState.boap.doDelete = false;
          bnDel.setLabel("Delete cell");
          IJ.setTool(lastTool);
        }
        return;
      }
      if (qState.boap.doDelete) { // stop if delete is on
        BOA_.log("**DELETE IS ON**");
        return;
      }
      if (b == bnDelSeg && !qState.boap.editMode) {
        if (!qState.boap.doDeleteSeg) {
          bnDelSeg.setLabel("*STOP TRUNCATE*");
          qState.boap.doDeleteSeg = true;
          lastTool = IJ.getToolName();
          IJ.setTool(Toolbar.LINE);
        } else {
          qState.boap.doDeleteSeg = false;
          bnDelSeg.setLabel("Truncate Seg");
          IJ.setTool(lastTool);
        }
        return;
      }
      if (qState.boap.doDeleteSeg) { // stop if delete is on
        BOA_.log("**TRUNCATE SEG IS ON**");
        return;
      }
      if (b == bnEdit) {
        if (qState.boap.editMode == false) {
          bnEdit.setLabel("*STOP EDIT*");
          BOA_.log("**EDIT IS ON**");
          qState.boap.editMode = true;
          lastTool = IJ.getToolName();
          IJ.setTool(Toolbar.LINE);
          if (qState.nest.size() == 1) {
            editSeg(0, 0, qState.boap.frame); // if only 1 snake go straight to edit, if
          }
          // more user must pick one
          // remember that this frame is edited
          qState.storeOnlyEdited(qState.boap.frame);
        } else {
          qState.boap.editMode = false;
          if (qState.boap.editingID != -1) {
            stopEdit();
          }
          bnEdit.setLabel("Edit");
          IJ.setTool(lastTool);
        }
        return;
      }
      if (qState.boap.editMode) { // stop if edit on
        BOA_.log("**EDIT IS ON**");
        return;
      }
      if (b == bnDefault) { // run in thread
        this.setDefualts();
        run = true;
      } else if (b == bnSeg) { // main segmentation procedure starts here
        if (sww != null && sww.getState() != SwingWorker.StateValue.DONE) {
          // if any worker works
          isSegBreakHit = true;
          return;
        }
        runBoaThread(qState.boap.frame, qState.boap.getFrames(), true);
      } else if (b == bnScale) {
        setScales();
        pixelLabel.setText("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
        fpsLabel.setText("F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
      } else if (b == bnAdd) {
        addCell(canvas.getImage().getRoi(), qState.boap.frame);
        canvas.getImage().killRoi();
      } else if (b == bnFinish) {
        BOA_.log("Finish: Exiting BOA...");
        fpsLabel.setName("moo");
        finish();
      } else if (b == bnQuit) {
        quit();
      }
      // process plugin GUI buttons
      if (b == bnFirstPluginGUI) {
        LOGGER.debug("First plugin GUI, state of BOAp is " + qState.snakePluginList.getInstance(0));
        if (qState.snakePluginList.getInstance(0) != null) {
          qState.snakePluginList.getInstance(0).showUi(true);
        }
      }
      if (b == bnSecondPluginGUI) {
        LOGGER.debug(
                "Second plugin GUI, state of BOAp is " + qState.snakePluginList.getInstance(1));
        if (qState.snakePluginList.getInstance(1) != null) {
          qState.snakePluginList.getInstance(1).showUi(true);
        }
      }
      if (b == bnThirdPluginGUI) {
        LOGGER.debug("Third plugin GUI, state of BOAp is " + qState.snakePluginList.getInstance(2));
        if (qState.snakePluginList.getInstance(2) != null) {
          qState.snakePluginList.getInstance(2).showUi(true);
        }
      }
      if (b == bnCopyLastPlugin) { // run in thread
        int frameCopyFrom = qState.boap.frame - 1;
        if (frameCopyFrom < 1 || frameCopyFrom > qState.boap.getFrames()) {
          return;
        }
        LOGGER.debug(
                "Copy config from frame " + frameCopyFrom + " current frame " + qState.boap.frame);
        qState.copyPluginListFromSnapshot(frameCopyFrom);
        setBusyStatus(true, false);
        recalculatePlugins();
        setBusyStatus(false, true); // update screen
      }

      if (b == bnCopyLast) { // copy previous settings
        int frameCopyFrom = qState.boap.frame - 1;
        if (frameCopyFrom < 1 || frameCopyFrom > qState.boap.getFrames()) {
          return;
        }
        LOGGER.debug(
                "Copy config from frame " + frameCopyFrom + " current frame " + qState.boap.frame);
        qState.copySegParamFromSnapshot(frameCopyFrom);
        updateSpinnerValues(); // update segmentation gui
        run = true; // re run BOA (+plugins)
      }
      if (b == bnPopulatePlugin) { // copy plugin stack forward
        List<Integer> range = IntStream.rangeClosed(qState.boap.frame + 1, qState.boap.getFrames())
                .boxed().collect(Collectors.toList());
        populatePlugins(range);
      }
      // menu listeners
      if (b == menuAbout) {
        about();
      }
      if (b == menuOpenHelp) {
        String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
        try {
          java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e1) {
          LOGGER.error("Could not open help: " + e1.getMessage());
          LOGGER.debug(e1.getMessage(), e1);
        }
        return;
      }
      if (b == menuSaveConfig) {
        String saveIn = qState.boap.getOutputFileCore().getParent();
        saveIn = (saveIn == null) ? System.getProperty("user.dir") : saveIn;
        SaveDialog sd = new SaveDialog("Save plugin config data...", saveIn,
                qState.boap.getFileName(), FileExtensions.pluginFileExt);
        if (sd.getFileName() != null) {
          try {
            // Create Serialization object with extra info layer
            Serializer<SnakePluginList> s;
            s = new Serializer<>(qState.snakePluginList, quimpInfo);
            s.setPretty(); // set pretty format
            s.save(sd.getDirectory() + sd.getFileName()); // save it
            s = null; // remove
          } catch (FileNotFoundException e1) {
            LOGGER.error("Problem with saving plugin config: " + e1.getMessage());
            LOGGER.debug(e1.getMessage(), e1);
          }
        }
      }

      /*
       * Loads configuration of current filter stack.
       * 
       * @see <a href=
       * "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/155">http://www.trac-wsbc.linkpc.
       * net:8080/trac/QuimP/ticket/155</a>
       */
      if (b == menuLoadConfig) {
        OpenDialog od = new OpenDialog("Load plugin config data...", "");
        if (od.getFileName() != null) {
          try {
            Serializer<SnakePluginList> loaded; // loaded instance
            // create serializer
            Serializer<SnakePluginList> s =
                    new Serializer<>(SnakePluginList.class, QuimP.TOOL_VERSION);
            s.registerConverter(new Converter170202<>(QuimP.TOOL_VERSION));
            // pass data to constructor of serialized object. Those data are not
            // serialized and must be passed externally
            s.registerInstanceCreator(SnakePluginList.class,
                    new SnakePluginListInstanceCreator(3, pluginFactory, viewUpdater));
            loaded = s.load(od.getDirectory() + od.getFileName());
            // restore loaded objects
            qState.snakePluginList.clear(); // closes windows, etc
            qState.snakePluginList = loaded.obj; // replace with fresh instance
            qState.store(qState.boap.frame); // copy loaded snakePluginList to snapshots
            // commented after #155
            /*
             * YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(),
             * "Warning", "Would you like to load this configuration for all frames?");
             * if (yncd.yesPressed()) { // propagate over all frames for (int i = 0; i <
             * qState.snakePluginListSnapshots.size(); i++) { if (i == qState.boap.frame
             * - 1) continue; // do not copy itself
             * qState.snakePluginListSnapshots.set(i,
             * qState.snakePluginList.getDeepCopy()); } }
             */
            recalculatePlugins(); // update screen
          } catch (IOException e1) {
            LOGGER.error("Problem with loading plugin config: " + e1.getMessage());
            LOGGER.debug(e1.getMessage(), e1);
          } catch (JsonSyntaxException e1) {
            LOGGER.error("Problem with configuration file: " + e1.getMessage());
            LOGGER.debug(e1.getMessage(), e1);
          } catch (Exception e1) {
            LOGGER.error(e1.getMessage(), e1); // something serious
          }
        }
      }

      /*
       * Shows history window.
       * 
       * When showed all actions are notified there. This may slow down the program
       */
      if (b == menuShowHistory) {
        JOptionPane.showMessageDialog(window,
                "The full history of changes is avaiable after saving your work in the" + " file "
                        + FileExtensions.newConfigFileExt);
        /*
         * if (historyLogger.isOpened()) historyLogger.closeHistory(); else
         * historyLogger.openHistory();
         */
      }

      /**
       * Load global config - QCONF file or paQP file. It depends on QuimP.newFileFormat
       * 
       * Checks also whether the name of the image sealed in config file is the same as those
       * opened currently. If not user has an option to break the procedure or continue
       * loading.
       */
      if (b == menuLoad || b == bnLoad) {
        FileDialogEx od = new FileDialogEx(IJ.getInstance());
        od.setDirectory(OpenDialog.getLastDirectory());
        try {
          if (QuimP.newFileFormat.get() == true) { // load QCONF
            od.setExtension(FileExtensions.newConfigFileExt);
            if (od.showOpenDialog() == null) {
              return;
            }
            loadQconfConfiguration(Paths.get(od.getDirectory(), od.getFile()));
          }
          if (QuimP.newFileFormat.get() == false) { // old paQP and snQP
            od.setExtension(FileExtensions.configFileExt);
            if (od.showOpenDialog() == null) {
              return;
            }
            if (qState.readParams(od.getPath().toFile())) {
              updateSpinnerValues();
              if (loadSnakes()) {
                run = false;
              } else {
                run = true;
              }
            }
          }
        } catch (IOException e1) {
          LOGGER.error("Problem with loading plugin config. " + e1.getMessage());
          LOGGER.debug(e1.getMessage(), e1); // if debug enabled - get more info
        } catch (JsonSyntaxException e1) {
          LOGGER.error("Problem with configuration file: " + e1.getMessage());
          LOGGER.debug(e1.getMessage(), e1);
        } catch (Exception e1) {
          LOGGER.error(e1.getMessage(), e1); // something serious
        }

      }

      /*
       * Discard all plugins.
       * 
       * In general it does: reset current snakePluginList and snakePluginListSnapshots,
       * Copies segSnakes to finalSnakes
       */
      if (b == menuDeletePlugin) {
        // clear all plugins
        qState.snakePluginList.clear();
        for (SnakePluginList sp : qState.snakePluginListSnapshots) {
          if (sp != null) {
            sp.clear();
          }
        }
        // copy snakes to finals
        for (int i = 0; i < qState.nest.size(); i++) {
          SnakeHandler snakeHandler = qState.nest.getHandler(i);
          snakeHandler.copyFromSegToFinal();
        }
        // update window
        imageGroup.updateOverlay(qState.boap.frame);
      }

      /*
       * Reload and re-apply all plugins stored in snakePluginListSnapshot.
       * 
       * qState.snakePluginList.clear(); can not be called here because
       * com.github.celldynamics.quimp.BOAState.restore(int) makes reference to
       * snakePluginListSnapshot in snakePluginList. Thus, cleaning snakePluginList deletes
       * one entry in snakePluginListSnapshot
       */
      if (b == menuApplyPlugin) {
        // iterate over snapshots and try to restore plugins in snapshots
        for (SnakePluginList sp : qState.snakePluginListSnapshots) {
          sp.afterSerialize();
        }
        // copy snaphots for frame to current snakePluginList (and segParams)
        qState.restore(qState.boap.frame);
        // recalculatePlugins(); // update screen
      }

      /*
       * Copy current plugin tree to all frames and applies plugins.
       */
      if (b == menuPopulatePlugin) { // run in thread
        List<Integer> range = IntStream.rangeClosed(1, qState.boap.getFrames()).boxed()
                .collect(Collectors.toList());
        populatePlugins(range);
      }

      /*
       * Run segmentation from mask file.
       */
      if (b == menuSegmentationRun) {
        if (qState.binarySegmentationPlugin != null) {
          if (!qState.binarySegmentationPlugin.isWindowVisible()) {
            qState.binarySegmentationPlugin.showUi(true);
          }
        } else {
          qState.binarySegmentationPlugin = new BinarySegmentationPlugin(); // create instance
          qState.binarySegmentationPlugin.attachData(qState.nest); // attach data
          // allow plugin to update screen
          qState.binarySegmentationPlugin.attachContext(viewUpdater);
          // plugin is run internally after Apply update screen is always on Apply button of plugin
          qState.binarySegmentationPlugin.showUi(true);
        }
        qState.binarySegmentationPlugin.attachImage(imageGroup.getOrgIpl());
        BOA_.log("Run segmentation from mask file");
      }

      /*
       * Clean all bOA state.
       */
      if (b == menuSegmentationReset) {
        qState.reset(WindowManager.getCurrentImage(), pluginFactory, viewUpdater);
        qState.nest.cleanNest();
        updateSpinnerValues();
        if (qState.boap.frame != imageGroup.getOrgIpl().getSlice()) {
          imageGroup.updateToFrame(qState.boap.frame); // move to frame
        } else {
          updateSliceSelector(); // repaint window explicitly
        }
      }

      updateWindowState(); // window logic on any change and selectors

      // run segmentation for selected cases
      if (run) { // in thread
        runBoaThread(qState.boap.frame, qState.boap.frame, false);
      }
    }

    /**
     * Run segmentation in separate thread.
     * 
     * @param startFrame start frame
     * @param endFrame end frame
     * 
     * @param interruptible if true cancel button is active.
     */
    private void runBoaThread(int startFrame, int endFrame, boolean interruptible) {
      System.out.println("running from in stackwindow");
      // run on current frame
      sww = new SwingWorker<Boolean, Object>() {
        @Override
        protected Boolean doInBackground() throws Exception {
          setBusyStatus(true, interruptible);
          IJ.showStatus("SEGMENTING...");
          runBoa(startFrame, endFrame);
          return true;
        }

        @Override
        protected void done() {
          try {
            get();
          } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BoaException) {
              BOA_.log(cause.getMessage());
              int framesCompleted = ((BoaException) cause).getFrame();
              IJ.showStatus("FAIL AT " + framesCompleted);
              BOA_.log("FAIL AT " + framesCompleted);
            }
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } finally {
            setBusyStatus(false, true);
            IJ.showStatus("COMPLETE");
          }
        }

      };
      sww.execute();
    }

    /**
     * Copy plugin tree from current frame (current state of qState.snakePluginList) to other
     * frames.
     * 
     * @param frames List of frames to copy plugin stack to. Numbered from 1.
     * 
     * @see #actionPerformed(ActionEvent)
     */
    private void populatePlugins(List<Integer> frames) {
      if (frames.isEmpty()) {
        return;
      }
      SnakePluginList tmp = qState.snakePluginList.getDeepCopy();
      int cf = qState.boap.frame;

      sww = new SwingWorker<Boolean, Object>() {
        @Override
        protected Boolean doInBackground() throws Exception {
          setBusyStatus(true, true);

          // iterate over frames and applies plugins
          for (int f : frames) {
            // make a deep copy
            qState.snakePluginListSnapshots.set(f - 1, tmp.getDeepCopy());
            qState.snakePluginListSnapshots.set(f - 1, tmp.getDeepCopy());
            // instance separate copy of jar for this plugin (in fact PluginFactory will return here
            // reference if this jar is already opened)
            qState.snakePluginListSnapshots.get(f - 1).afterSerialize();

            qState.boap.frame = f; // assign to global frame variable
            imageGroup.updateToFrame(qState.boap.frame);
            recalculatePlugins();
            if (isSegBreakHit == true) { // if flag set, stop
              isSegBreakHit = false;
              break;
            }
          }
          qState.boap.frame = cf;
          imageGroup.updateToFrame(qState.boap.frame);
          return true;
        }

        @Override
        protected void done() {
          setBusyStatus(false, true);
        }

      };
      sww.execute();

    }

    /**
     * Loader of QCONF file in BOA. Initialise all BOA structures and updates window.
     * 
     * <p>Assign also format converter. This method partially updates UI but some other related
     * methods are called from {@link #actionPerformed(ActionEvent)}.
     * 
     * @param configPath path to QCONF file
     * @throws IOException on file problem
     * @throws Exception various other problems like e.g json syntax
     * @see #updateWindowState()
     */
    private void loadQconfConfiguration(Path configPath) throws IOException, Exception {
      Path filename = configPath.getFileName();
      if (filename == null) {
        throw new IllegalAccessException("Input path is not file");
      }
      Serializer<DataContainer> loaded; // loaded instance
      // create serializer
      Serializer<DataContainer> s = new Serializer<>(DataContainer.class, QuimP.TOOL_VERSION);
      s.registerConverter(new Converter170202<>(QuimP.TOOL_VERSION));
      s.registerInstanceCreator(DataContainer.class,
              new DataContainerInstanceCreator(pluginFactory, viewUpdater));
      loaded = s.load(configPath.toString());
      // check against image names
      if (!loaded.obj.BOAState.boap.getOrgFile().getName()
              .equals(qState.boap.getOrgFile().getName())) {
        LOGGER.warn("The image opened currently in BOA is different from those"
                + " pointed in configuration file");
        log("Trying to apply configuration saved for other image");
        YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Warning",
                "Trying to load configuration that does not\nmath to"
                        + " opened image.\nAre you sure?");
        if (!yncd.yesPressed()) {
          return;
        }
      }
      // replace orgFile with that already opened. It is possible as BOA can not
      // exist without image loaded so this field will always be true.
      loaded.obj.BOAState.boap.setOrgFile(qState.boap.getOrgFile());
      // replace outputFileCore with current one
      String parent;
      if (configPath.getParent() != null) {
        parent = configPath.getParent().toString();
      } else {
        parent = "";
      }
      loaded.obj.BOAState.boap.setOutputFileCore(parent + File.separator + filename.toString());
      // closes windows, etc
      qState.reset(WindowManager.getCurrentImage(), pluginFactory, viewUpdater);
      qState = loaded.obj.BOAState;
      imageGroup.updateNest(qState.nest); // reconnect nest to external class
      qState.restore(qState.boap.frame); // copy from snapshots to current object
      updateSpinnerValues(); // update segmentation gui
      // refill frame zoom choice to make possible selection last zoomed cell (called from
      // updateChoice)
      fillZoomChoice();
      // do not recalculatePlugins here because pluginList is empty and this
      // method will update finalSnake overriding it by segSnake (because on
      // empty list they are just copied)
      // updateToFrame calls updateSliceSelector only if there is action of
      // changing frame. If loaded frame is the same as current one this event is
      // not called.
      if (qState.boap.frame != imageGroup.getOrgIpl().getSlice()) {
        // move to frame (will call updateSliceSelector)
        imageGroup.updateToFrame(qState.boap.frame);
      } else {
        updateSliceSelector(); // repaint window explicitly
      }
      BOA_.log("Successfully read configuration");
    }

    /**
     * Detect changes in checkboxes and run segmentation for current frame if necessary.
     * 
     * <p>Transfer parameters from changed GUI element to
     * {@link com.github.celldynamics.quimp.BOAState.BOAp} class
     * 
     * @param e Type of event
     * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateWindowState()
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {
      LOGGER.trace("EVENT:itemStateChanged");
      if (qState.boap.doDelete) {
        BOA_.log("**WARNING:DELETE IS ON**");
      }
      boolean run = false; // set to true if any of items changes require to re-run segmentation
      Object source = e.getItemSelectable();
      if (source == cbPath) {
        qState.segParam.showPaths = cbPath.getState();
        if (qState.segParam.showPaths) {
          this.setImage(imageGroup.getPathsIpl());
        } else {
          this.setImage(imageGroup.getOrgIpl());
        }
        if (qState.boap.zoom && !qState.nest.isVacant()) { // set zoom
          imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
        }
      } else if (source == cbPrevSnake) {
        qState.segParam.use_previous_snake = cbPrevSnake.getState();
      } else if (source == cbExpSnake) {
        qState.segParam.expandSnake = cbExpSnake.getState();
        run = true;
      } else if (source == cbFirstPluginActiv) { // run in thread
        qState.snakePluginList.setActive(0, cbFirstPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      } else if (source == cbSecondPluginActiv) { // run in thread
        qState.snakePluginList.setActive(1, cbSecondPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      } else if (source == cbThirdPluginActiv) { // run in thread
        qState.snakePluginList.setActive(2, cbThirdPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      }

      // action on menus
      if (source == cbMenuPlotOriginalSnakes) { // run in thread
        qState.boap.isProcessedSnakePlotted = cbMenuPlotOriginalSnakes.getState();
        setBusyStatus(true, false);
        recalculatePlugins();
      }
      if (source == cbMenuPlotHead) {
        qState.boap.isHeadPlotted = cbMenuPlotHead.getState();
        imageGroup.updateOverlay(qState.boap.frame);
      }

      // actions on Plugin selections
      if (source == chFirstPluginName) { // run in thread
        LOGGER.debug("Used firstPluginName, val: " + chFirstPluginName.getSelectedItem());
        instanceSnakePlugin((String) chFirstPluginName.getSelectedItem(), 0,
                cbFirstPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      }
      if (source == chSecondPluginName) { // run in thread
        LOGGER.debug("Used secondPluginName, val: " + chSecondPluginName.getSelectedItem());
        instanceSnakePlugin((String) chSecondPluginName.getSelectedItem(), 1,
                cbSecondPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      }
      if (source == chThirdPluginName) { // run in thread
        LOGGER.debug("Used thirdPluginName, val: " + chThirdPluginName.getSelectedItem());
        instanceSnakePlugin((String) chThirdPluginName.getSelectedItem(), 2,
                cbThirdPluginActiv.getState());
        setBusyStatus(true, false);
        recalculatePlugins();
      }

      // Action on zoom selector
      if (source == chZoom) {
        LOGGER.trace("zoom val " + chZoom.getSelectedItem());
        if (chZoom.getSelectedItem().equals(fullZoom)) { // user selected default position (no zoom)
          qState.boap.snakeToZoom = -1; // set negative value to indicate no zoom
          qState.boap.zoom = false; // important for other parts (legacy)
          imageGroup.unzoom(canvas); // unzoom view
        } else { // zoom here
          if (!qState.nest.isVacant()) { // any snakes present
            qState.boap.snakeToZoom = Integer.parseInt(chZoom.getSelectedItem()); // get int
            qState.boap.zoom = true; // legacy compatibility
            imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
          }
        }
      }

      updateWindowState(); // window logic on any change
      updateChoices(); // only for updating colors after updating window state

      try {
        if (run) {
          if (supressStateChangeBOArun) { // when spinners are changed
            // programmatically they raise the event. this is to block segmentation re-run
            LOGGER.debug("supressState");
            return;
          }
          // run on current frame
          runBoa(qState.boap.frame, qState.boap.frame);
        }
      } catch (BoaException be) {
        BOA_.log(be.getMessage());
      } finally {
        setBusyStatus(false, true);
      }
    }

    /**
     * Detect changes in spinners and run segmentation for current frame if necessary.
     * 
     * <p>Transfer parameters from changed GUI element to
     * {@link com.github.celldynamics.quimp.BOAState.BOAp} class
     * 
     * @param ce Type of event
     * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateWindowState()
     */
    @Override
    public void stateChanged(final ChangeEvent ce) {
      LOGGER.trace("EVENT:stateChanged");
      if (qState.boap.doDelete) {
        BOA_.log("**WARNING:DELETE IS ON**");
      }
      boolean run = false; // set to true if any of items changes require to re-run segmentation
      Object source = ce.getSource();

      if (source == dsNodeRes) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.setNodeRes((Double) spinner.getValue());
        run = true;
      } else if (source == dsVelCrit) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.vel_crit = (Double) spinner.getValue();
        run = true;
      } else if (source == dsFImage) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.f_image = (Double) spinner.getValue();
        run = true;
      } else if (source == dsFCentral) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.f_central = (Double) spinner.getValue();
        run = true;
      } else if (source == dsFContract) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.f_contract = (Double) spinner.getValue();
        run = true;
      } else if (source == dsFinalShrink) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.finalShrink = (Double) spinner.getValue();
        run = true;
      } else if (source == isMaxIterations) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.max_iterations = (Integer) spinner.getValue();
        run = true;
      } else if (source == isBlowup) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.blowup = (Integer) spinner.getValue();
        run = true;
      } else if (source == isSampletan) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.sample_tan = (Integer) spinner.getValue();
        run = true;
      } else if (source == isSamplenorm) {
        JSpinner spinner = (JSpinner) source;
        qState.segParam.sample_norm = (Integer) spinner.getValue();
        run = true;
      }

      updateWindowState(); // window logic on any change

      try {
        if (run) {
          // when spinners are changed programmatically they raise the event. this is to block
          // segmentation re-run
          if (supressStateChangeBOArun) {
            LOGGER.debug("supressState");
            return;
          }
          // run on current frame
          runBoa(qState.boap.frame, qState.boap.frame);
        }
      } catch (BoaException be) {
        BOA_.log(be.getMessage());
      }
    }

    /**
     * Update the frame label, overlay, frame and set zoom Called when user clicks on slice
     * selector in IJ window.
     */
    @Override
    public void updateSliceSelector() {
      super.updateSliceSelector();
      LOGGER.debug("EVENT:updateSliceSelector");
      if (!qState.boap.singleImage) {
        zSelector.setValue(imp.getCurrentSlice()); // this is delayed in
        // super.updateSliceSelector force it now
      }
      if (qState.boap.editMode) {
        // BOA_.log("next frame in edit mode");
        stopEdit();
      }

      if (!qState.boap.singleImage) {
        qState.boap.frame = imp.getCurrentSlice();
        frameLabel.setText("" + qState.boap.frame);
      }
      imageGroup.updateOverlay(qState.boap.frame); // draw overlay
      imageGroup.setIpSliceAll(qState.boap.frame);

      // zoom to snake zero
      if (qState.boap.zoom && !qState.nest.isVacant()) {
        imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
      }
      // if in edit, save current edit and start edit of next frame if exists
      boolean wasInEdit = qState.boap.editMode;
      if (wasInEdit) {
        bnEdit.setLabel("*STOP EDIT*");
        BOA_.log("**EDIT IS ON**");
        qState.boap.editMode = true;
        lastTool = IJ.getToolName();
        IJ.setTool(Toolbar.LINE);
        editSeg(0, 0, qState.boap.frame);
        IJ.setTool(lastTool);
      }
      LOGGER.trace("Snakes at this frame: " + qState.nest.getSnakesforFrame(qState.boap.frame));
      if (!isSegRunning) {
        // do not update or restore state when we hit this event from runBoa() method (through
        // setIpSliceAll(int))
        qState.restore(qState.boap.frame);
        updateSpinnerValues();
        updateWindowState();
      }
    }

    /**
     * Turn delete mode off by setting proper value in
     * {@link com.github.celldynamics.quimp.BOAState.BOAp}.
     */
    void switchOffDelete() {
      qState.boap.doDelete = false;
      bnDel.setLabel("Delete cell");
    }

    /*
     * (non-Javadoc)
     * 
     * @see ij.gui.ImageWindow#setImage(ij.ImagePlus)
     */
    @Override
    public void setImage(ImagePlus imp2) {
      double m = this.ic.getMagnification();
      Dimension dem = this.ic.getSize();
      super.setImage(imp2);
      this.ic.setMagnification(m);
      this.ic.setSize(dem);
    }

    /**
     * Turn truncate mode off by setting proper value in
     * {@link com.github.celldynamics.quimp.BOAState.BOAp}.
     */
    void switchOfftruncate() {
      qState.boap.doDeleteSeg = false;
      bnDelSeg.setLabel("Truncate Seg");
    }

    /**
     * Set frame interval and scale on BOA window..
     */
    void setScalesText() {
      pixelLabel.setText("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
      fpsLabel.setText("F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
    }

  } // end of CustomStackWindow

  /**
   * Creates instance (through SnakePluginList) of plugin of given name on given UI slot.
   * 
   * <p>Decides if plugin will be created or destroyed basing on plugin name from Choice list
   * 
   * @param selectedPlugin Name of plugin returned from UI elements
   * @param slot Slot of plugin
   * @param act Indicates if plugins is activated in GUI
   * @see com.github.celldynamics.quimp.SnakePluginList
   */
  private void instanceSnakePlugin(final String selectedPlugin, int slot, boolean act) {

    try {
      // get instance using plugin name (obtained from getPluginNames from PluginFactory
      if (selectedPlugin != NONE) { // do no pass NONE to pluginFact
        qState.snakePluginList.setInstance(slot, selectedPlugin, act); // build instance
      } else {
        if (qState.snakePluginList.getInstance(slot) != null) {
          qState.snakePluginList.getInstance(slot).showUi(false);
        }
        qState.snakePluginList.deletePlugin(slot);
      }
    } catch (QuimpPluginException e) {
      LOGGER.warn("Plugin " + selectedPlugin + " cannot be loaded. Reason: " + e.getMessage());
      LOGGER.debug(e.getMessage(), e);
    }
  }

  /**
   * Set busy status for BOA window.
   * 
   * <p>Window is inactive for busy status. Setting flag <tt>interruptible</tt> enables Cancel
   * button that breaks current operation.
   * 
   * @param busy True if busy, false otherwise
   * @param interruptible true for enabling Cancel button. Ignored if busy==false
   */
  private void setBusyStatus(boolean busy, boolean interruptible) {
    if (busy == true) {
      window.bnSeg.setBackground(Color.RED);
      window.bnSeg.setLabel("Busy");
      if (interruptible) {
        window.enableUiInterruptile(false);
      } else {
        window.enableUi(false);
      }
    } else {
      window.bnSeg.setBackground(null);
      window.bnSeg.setLabel("SEGMENT");
      window.enableUi(true);
    }

  }

  /**
   * Start segmentation process on range of frames.
   * 
   * <p>This method is called for update only current view as well (<tt>startF</tt> ==
   * <tt>endF</tt>). It also go through plugin stack.
   * 
   * @param startF start frame
   * @param endF end frame
   * @throws BoaException TODO Rewrite exceptions here
   * @see <a href=
   *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/65">http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/65</a>
   */
  public void runBoa(int startF, int endF) throws BoaException {
    System.out.println("run BOA");
    isSegBreakHit = false;
    isSegRunning = true;
    if (qState.nest.isVacant()) {
      BOA_.log("Nothing to segment!");
      isSegRunning = false;
      return;
    }
    try {
      IJ.showProgress(0, endF - startF);

      // if(boap.expandSnake) boap.NMAX = 9990; // percent hack

      qState.nest.resetForFrame(startF);
      if (!qState.segParam.expandSnake) {
        // blowup snake ready for contraction (only those not starting at or after the startF)
        constrictor.loosen(qState.nest, startF);
      } else {
        constrictor.implode(qState.nest, startF);
      }
      SnakeHandler snH;

      int s = 0;
      Snake snake;
      imageGroup.clearPaths(startF);

      for (qState.boap.frame = startF; qState.boap.frame <= endF; qState.boap.frame++) {
        if (isSegBreakHit == true) {
          qState.boap.frame--;
          isSegBreakHit = false;
          break;
        }
        // per frame
        imageGroup.setProcessor(qState.boap.frame);
        imageGroup.setIpSliceAll(qState.boap.frame);

        try {
          if (qState.boap.frame != startF) { // expand snakes for next frame
            if (!qState.segParam.use_previous_snake) {
              qState.nest.resetForFrame(qState.boap.frame);
            } else {
              if (!qState.segParam.expandSnake) {
                constrictor.loosen(qState.nest, qState.boap.frame);
              } else {
                constrictor.implode(qState.nest, qState.boap.frame);
              }
            }
          }

          for (s = 0; s < qState.nest.size(); s++) { // for each snake
            snH = qState.nest.getHandler(s);
            snake = snH.getLiveSnake();
            try {
              if (!snake.alive || qState.boap.frame < snH.getStartFrame()) {
                continue;
              }
              imageGroup.drawPath(snake, qState.boap.frame); // pre tightned snake on path
              tightenSnake(snake);
              imageGroup.drawPath(snake, qState.boap.frame); // post tightned snake on path
              snH.backupLiveSnake(qState.boap.frame);
              Snake out = iterateOverSnakePlugins(snake);
              snH.storeThisSnake(out, qState.boap.frame); // store resulting snake as final

            } catch (QuimpPluginException qpe) {
              // must be rewritten with whole runBOA #65 #67
              BOA_.log("Error in filter module: " + qpe.getMessage());
              LOGGER.debug(qpe.getMessage(), qpe);
              snH.storeLiveSnake(qState.boap.frame); // store segmented nonmodified

            } catch (BoaException be) {
              imageGroup.drawPath(snake, qState.boap.frame); // failed
              // position
              // sH.deleteStoreAt(frame);
              snH.storeLiveSnake(qState.boap.frame);
              snH.backupLiveSnake(qState.boap.frame);
              qState.nest.kill(snH);
              snake.unfreezeAll();
              BOA_.log("Snake " + snake.getSnakeID() + " died, frame " + qState.boap.frame);
              isSegRunning = false;
              if (qState.nest.allDead()) {
                throw new BoaException("All snakes dead: " + be.getMessage(), qState.boap.frame, 1);
              }
            }

          }
          imageGroup.updateOverlay(qState.boap.frame); // redraw display
          IJ.showProgress(qState.boap.frame, endF);
        } catch (BoaException be) {
          isSegRunning = false;
          if (!qState.segParam.use_previous_snake) {
            imageGroup.setIpSliceAll(qState.boap.frame);
            imageGroup.updateOverlay(qState.boap.frame);
          } else {
            throw be; // do no add LOGGER here #278
          }
        } finally {
          historyLogger.addEntry("Processing", qState);
          qState.store(qState.boap.frame); // always remember state of the BOA that is
          // used for segmentation
        }
      }
      qState.boap.frame = endF;
    } catch (Exception e) {
      // e.printStackTrace();
      /// imageGroup.drawContour(nest.getSNAKES(), frame);
      // imageGroup.updateAndDraw();
      isSegRunning = false;
      LOGGER.debug(e.getMessage(), e);
      imageGroup.updateOverlay(qState.boap.frame); // update on error
      // do no add LOGGER here #278
      throw new BoaException("Frame " + qState.boap.frame + ": " + e.getMessage(),
              qState.boap.frame, 1);
    } finally {
      isSegRunning = false;
    }

  }

  /**
   * Process Snake by all active plugins.
   * 
   * <p>Processed Snake is returned as new Snake with the same ID. Input snake is not modified. For
   * empty plugin list it just return input snake
   *
   * <p>This method supports two interfaces:
   * {@link com.github.celldynamics.quimp.plugin.snakes.IQuimpBOAPoint2dFilter},
   * {@link com.github.celldynamics.quimp.plugin.snakes.IQuimpBOASnakeFilter}
   * 
   * <p>It uses smart method to detect which interface is used for every slot to avoid unnecessary
   * conversion between data. <tt>previousConversion</tt> keeps what interface was used on
   * previous slot in plugin stack. Then for every plugin data are converted if current plugin
   * differs from previous one. Converted data are kept in <tt>snakeToProcess</tt> and
   * <tt>dataToProcess</tt> but only one of these variables is valid in given time. Finally after
   * last plugin data are converted to Snake.
   * 
   * @param snake snake to be processed
   * @return Processed snake or original input one when there is no plugin selected
   * @throws QuimpPluginException on plugin error
   */
  private Snake iterateOverSnakePlugins(final Snake snake) throws QuimpPluginException {
    final int ipoint = 0; // define IQuimpPoint2dFilter interface
    final int isnake = 1; // define IQuimpPoint2dFilter interface
    // type of previous plugin. Define if data should be converted for current plugin
    int previousConversion = isnake; // IQuimpSnakeFilter is default interface
    Snake outsnake = snake; // if there is no plugin just return input snake
    Snake snakeToProcess = snake; // data to be processed, input snake on beginning
    // data to process in format of list
    // null but it will be overwritten in loop because first "if" fires always (previousConversion
    // is set to isnake) on beginning, if first plugin is ipoint type
    List<Point2d> dataToProcess = null;
    if (!qState.snakePluginList.isRefListEmpty()) {
      LOGGER.debug("sPluginList not empty");
      for (Plugin qsP : qState.snakePluginList.getList()) { // iterate over list
        if (!qsP.isExecutable()) {
          continue; // no plugin on this slot or not active
        }
        if (qsP.getRef() instanceof IQuimpPluginAttachImage) {
          ((IQuimpPluginAttachImage) qsP.getRef()).attachImage(imageGroup.getOrgIp());
        }
        if (qsP.getRef() instanceof IQuimpBOAPoint2dFilter) { // check interface type
          if (previousConversion == isnake) { // previous was IQuimpSnakeFilter
            dataToProcess = snakeToProcess.asList(); // and data needs to be converted
          }
          IQuimpBOAPoint2dFilter qsPcast = (IQuimpBOAPoint2dFilter) qsP.getRef();
          qsPcast.attachData(dataToProcess);
          dataToProcess = qsPcast.runPlugin(); // store result in input variable
          previousConversion = ipoint;
        }
        if (qsP.getRef() instanceof IQuimpBOASnakeFilter) { // check interface type
          if (previousConversion == ipoint) { // previous was IQuimpPoint2dFilter
            // and data must be converted to snake from dataToProcess
            snakeToProcess = new QuimpDataConverter(dataToProcess).getSnake(snake.getSnakeID());
          }
          IQuimpBOASnakeFilter qsPcast = (IQuimpBOASnakeFilter) qsP.getRef();
          qsPcast.attachData(snakeToProcess);
          snakeToProcess = qsPcast.runPlugin(); // store result as snake for next plugin
          previousConversion = isnake;
        }
      }
      // after loop previousConversion points what plugin was last and actual data
      // must be converted to snake
      switch (previousConversion) {
        case ipoint: // last plugin was IQuimpPoint2dFilter - convert to Snake
          outsnake = new QuimpDataConverter(dataToProcess).getSnake(snake.getSnakeID());
          break;
        case isnake: // last plugin was IQuimpSnakeFilter - do not convert
          outsnake = snakeToProcess;
          outsnake.setSnakeID(snake.getSnakeID()); // copy old id in case if user forgot
          break;
        default:
          throw new IllegalArgumentException("Unknown previousConversion");
      }
    } else {
      LOGGER.debug("sPluginList empty");
    }
    return outsnake;

  }

  private void tightenSnake(final Snake snake) throws BoaException {

    int i;

    for (i = 0; i < qState.segParam.max_iterations; i++) { // iter constrict snake
      if (i % qState.boap.cut_every == 0) {
        snake.cutLoops(); // cut out loops every p.cut_every timesteps
      }
      if (i % 10 == 0 && i != 0) {
        snake.correctDistance(true);
      }
      if (constrictor.constrict(snake, imageGroup.getOrgIp())) { // if all nodes frozen
        break;
      }
      if (i % 4 == 0) {
        imageGroup.drawPath(snake, qState.boap.frame); // draw current snake
      }

      if ((snake.getNumPoints() / snake.startingNnodes) > qState.boap.NMAX) {
        // if max nodes reached (as % starting) prompt for reset
        if (qState.segParam.use_previous_snake) {
          // imageGroup.drawContour(snake, frame);
          // imageGroup.updateAndDraw();
          throw new BoaException(
                  "Frame " + qState.boap.frame + "-max nodes reached " + snake.getNumPoints(),
                  qState.boap.frame, 1);
        } else {
          BOA_.log("Frame " + qState.boap.frame + "-max nodes reached..continue");
          break;
        }
      }
    }
    snake.unfreezeAll(); // set freeze tag back to false

    if (!qState.segParam.expandSnake) { // shrink a bit to get final outline
      snake.scale(-BOA_.qState.segParam.finalShrink, 0.5, false);
    }
    snake.cutLoops();
    snake.cutIntersects();
  }

  /**
   * Sets the scales.
   * 
   * <p>Scale and interval fields are already initialised in {@link BOAState} constructor from
   * loaded image. If image does not have proper scale or interval, defaults from
   * {@link BOAState.BOAp#setImageScale(double)} and
   * {@link BOAState.BOAp#setImageFrameInterval(double)} are taken.
   * 
   * <p>All stats are evaluated using scales stored in tiff file so those values put here by user
   * are copied to image by {@link #updateImageScale()}.
   */
  void setScales() {
    GenericDialog gd = new GenericDialog("Set image scale", window);
    gd.addNumericField("Frame interval (seconds)", qState.boap.getImageFrameInterval(), 3);
    gd.addNumericField("Pixel width (\u00B5m)", qState.boap.getImageScale(), 6);
    gd.showDialog();

    double tempFI = gd.getNextNumber(); // force to check for errors
    double tempP = gd.getNextNumber();

    if (gd.invalidNumber()) {
      IJ.error("Values invalid");
      BOA_.log("Scale was not updated:\n\tinvalid input");
    } else if (gd.wasOKed()) {
      qState.boap.setImageFrameInterval(tempFI);
      qState.boap.setImageScale(tempP);
      updateImageScale();
      BOA_.log("Scale successfully updated");
    }

  }

  /**
   * Update image scale.
   */
  void updateImageScale() {
    imageGroup.getOrgIpl().getCalibration().frameInterval = qState.boap.getImageFrameInterval();
    imageGroup.getOrgIpl().getCalibration().pixelHeight = qState.boap.getImageScale();
    imageGroup.getOrgIpl().getCalibration().pixelWidth = qState.boap.getImageScale();
  }

  /**
   * Load snakes from snQP file.
   *
   * @return true, if successful
   */
  private boolean loadSnakes() {

    YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Load associated snakes?",
            "\tLoad associated snakes?\n");
    if (!yncd.yesPressed()) {
      return false;
    }

    OutlineHandler otlineH = new OutlineHandler(qState.boap.readQp);
    if (!otlineH.readSuccess) {
      BOA_.log("Could not read in snakes");
      return false;
    }
    // convert to BOA snakes

    qState.nest.addOutlinehandler(otlineH);
    imageGroup.setProcessor(otlineH.getStartFrame());
    imageGroup.updateOverlay(otlineH.getStartFrame());
    BOA_.log("Successfully read snakes");
    return true;
  }

  /**
   * Add ROI to Nest.
   * 
   * <p>This method is called on selection that should contain object to be segmented. Initialise
   * Snake object in Nest and it performs also initial segmentation of selected cell.
   * 
   * @param r ROI object (IJ)
   * @param f number of current frame
   * @see #tightenSnake(Snake)
   */
  // @SuppressWarnings("unchecked")
  void addCell(final Roi r, int f) {
    SnakeHandler snakeH = qState.nest.addHandler(r, f);
    Snake snake = snakeH.getLiveSnake();
    imageGroup.setProcessor(f);
    try {
      imageGroup.drawPath(snake, f); // pre tightned snake on path
      tightenSnake(snake);
      imageGroup.drawPath(snake, f); // post tightned snake on path
      snakeH.backupLiveSnake(f);
      Snake out = iterateOverSnakePlugins(snake); // process segmented snake by plugins
      snakeH.storeThisSnake(out, f); // store processed snake as final

      // if any problem with plugin or other, store snake without modification
      // because snake.asList() returns copy
    } catch (QuimpPluginException qpe) {
      snakeH.storeLiveSnake(f);
      BOA_.log("Error in filter module: " + qpe.getMessage());
      LOGGER.debug(qpe.getMessage(), qpe);
    } catch (BoaException be) {
      snakeH.deleteStoreAt(f);
      snakeH.kill();
      snakeH.backupLiveSnake(f);
      snakeH.storeLiveSnake(f);
      BOA_.log("New snake failed to converge: " + be.getMessage());
      LOGGER.debug(be.getMessage(), be);
    } catch (Exception e) {
      BOA_.log("Undefined error");
      LOGGER.error(e.getMessage(), e);
      LOGGER.debug(e.getMessage(), e);
    } finally {
      imageGroup.updateOverlay(f);
      historyLogger.addEntry("Added cell", qState);
      qState.store(f); // always remember state of the BOA after modification of UI
    }

  }

  /**
   * Delete SnakeHandler using the snake clicked by user.
   * 
   * <p>Method searches the snake in Nest that is on current frame and its centroid is close enough
   * to clicked point. If found, the whole SnakeHandler (all Snakes of the same ID across frames)
   * is deleted.
   * 
   * @param x clicked coordinate
   * @param y clicked coordinate
   * @param frame current frame
   * @return true if handler deleted, false if not (because user does not click it)
   */
  boolean deleteCell(int x, int y, int frame) {
    if (qState.nest.isVacant()) {
      return false;
    }

    SnakeHandler snakeH;
    Snake snake;
    ExtendedVector2d snakeV;
    ExtendedVector2d mdV = new ExtendedVector2d(x, y);
    List<Double> distance = new ArrayList<Double>();

    for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
      snakeH = qState.nest.getHandler(i);
      if (snakeH.isStoredAt(frame)) {
        snake = snakeH.getStoredSnake(frame);
        snakeV = snake.getCentroid();
        distance.add(ExtendedVector2d.lengthP2P(mdV, snakeV));
      }
    }
    int minIndex = QuimPArrayUtils.minListIndex(distance);
    if (distance.get(minIndex) < 10) { // if closest < 10, delete it
      BOA_.log("Deleted cell " + qState.nest.getHandler(minIndex).getID());
      qState.nest.removeHandler(qState.nest.getHandler(minIndex));
      imageGroup.updateOverlay(frame);
      window.switchOffDelete();
      return true;
    } else {
      BOA_.log("Click the cell centre to delete");
    }
    return false;
  }

  /**
   * Delete segmentation.
   *
   * @param x the x
   * @param y the y
   * @param frame the frame
   */
  void deleteSegmentation(int x, int y, int frame) {
    SnakeHandler snakeH;
    Snake snake;
    ExtendedVector2d snakeV;
    ExtendedVector2d mmV = new ExtendedVector2d(x, y);
    List<Double> distance = new ArrayList<Double>();

    for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
      snakeH = qState.nest.getHandler(i);

      if (snakeH.isStoredAt(frame)) {
        snake = snakeH.getStoredSnake(frame);
        snakeV = snake.getCentroid();
        distance.add(ExtendedVector2d.lengthP2P(mmV, snakeV));
      } else {
        distance.add(9999.0);
      }
    }

    int minIndex = QuimPArrayUtils.minListIndex(distance);
    // BOA_.log("Debug: closest index " + minIndex + ", id " +
    // nest.getHandler(minIndex).getID());
    if (distance.get(minIndex) < 10) { // if closest < 10, delete it
      BOA_.log("Deleted snake " + qState.nest.getHandler(minIndex).getID() + " from " + frame
              + " onwards");
      snakeH = qState.nest.getHandler(minIndex);
      snakeH.deleteStoreFrom(frame);
      imageGroup.updateOverlay(frame);
      window.switchOfftruncate();
    } else {
      BOA_.log("Click the cell centre to delete");
    }
  }

  /**
   * Called when user click Edit button.
   * 
   * @param x Coordinate of clicked point
   * @param y Coordinate of clicked point
   * @param frame current frame in stack
   * @see #stopEdit()
   * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateSliceSelector()
   */
  void editSeg(int x, int y, int frame) {
    SnakeHandler snakeH;
    Snake snake;
    ExtendedVector2d snakeV;
    ExtendedVector2d mmV = new ExtendedVector2d(x, y);
    double[] distance = new double[qState.nest.size()];

    for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
      snakeH = qState.nest.getHandler(i);
      if (snakeH.isStoredAt(frame)) {
        snake = snakeH.getStoredSnake(frame);
        snakeV = snake.getCentroid();
        distance[i] = ExtendedVector2d.lengthP2P(mmV, snakeV);
      }
    }
    int minIndex = QuimPArrayUtils.minArrayIndex(distance);
    if (distance[minIndex] < 10 || qState.nest.size() == 1) { // if closest < 10, edit it
      snakeH = qState.nest.getHandler(minIndex);
      qState.boap.editingID = minIndex; // sH.getID();
      BOA_.log("Editing cell " + snakeH.getID());
      imageGroup.clearOverlay();

      Roi r;
      if (qState.boap.useSubPixel == true) {
        r = snakeH.getStoredSnake(frame).asPolyLine();
      } else {
        r = snakeH.getStoredSnake(frame).asIntRoi();
      }
      // Roi r = sH.getStoredSnake(frame).asFloatRoi();
      Roi.setColor(Color.cyan);
      canvas.getImage().setRoi(r);
    } else {
      BOA_.log("Click a cell centre to edit");
    }
  }

  /**
   * Called when user ends editing.
   * 
   * @see com.github.celldynamics.quimp.BOA_.CustomStackWindow#updateSliceSelector()
   */
  void stopEdit() {
    Roi r = canvas.getImage().getRoi();
    Roi.setColor(Color.yellow);
    SnakeHandler snakeH = qState.nest.getHandler(qState.boap.editingID);
    snakeH.storeRoi((PolygonRoi) r, qState.boap.frame); // store as final snake
    // copy to segSnakes array
    Snake stored = snakeH.getStoredSnake(qState.boap.frame);
    snakeH.backupThisSnake(stored, qState.boap.frame);
    canvas.getImage().killRoi();
    imageGroup.updateOverlay(qState.boap.frame);
    qState.boap.editingID = -1;
  }

  /**
   * Delete seg.
   *
   * @param x the x
   * @param y the y
   */
  void deleteSeg(int x, int y) {
  }

  /**
   * Initialising all data saving and exporting results to disk and IJ.
   */
  private void finish() {
    IJ.showStatus("BOA-FINISHING");
    YesNoCancelDialog ync;
    File testF;
    LOGGER.debug(qState.segParam.toString());
    for (SnakeHandler sh : qState.nest.getHandlers()) {
      sh.findLastFrame(); // make sure that endFrame points good frame
    }
    if (qState.boap.saveSnake) {
      try {
        // this field is set on loading of QCONF thus BOA will ask to save in the same
        // folder
        String saveIn = BOA_.qState.boap.getOutputFileCore().getParent();
        SaveDialog sd = new SaveDialog("Save segmentation data...", saveIn,
                BOA_.qState.boap.getFileName() + ".QCONF", "");
        if (sd.getFileName() == null) {
          BOA_.log("Save canceled");
          return;
        }
        // This initialize various filenames that can be accessed by other modules (also qconf)
        BOA_.qState.boap.setOutputFileCore(sd.getDirectory() + sd.getFileName());

        // check whether there is case saved and warn user
        // there is no option to solve this problem here. User can only agree or cancel
        // test for QCONF that is created always
        testF = new File(qState.boap.deductNewParamFileName());
        LOGGER.trace("Test for QCONF: " + testF.toString());
        if (testF.exists() && !testF.isDirectory()) {
          ync = new YesNoCancelDialog(window, "Save Segmentation",
                  "You are about to override previous results. Is it ok?\nIf not,"
                          + " previous data must be moved to another directory");
          if (!ync.yesPressed()) {
            return;
          }
        }
        // write operations
        // blocked by #263, enabled by 228
        if (QuimP.newFileFormat.get() == false) {
          qState.nest.writeSnakes(); // write snPQ file (if any snake) and paQP
        }
        // if (qState.nest.writeSnakes()) { // write snPQ file (if any snake) and paQP
        // write stQP file and fill outFile used later
        List<CellStatsEval> ret = qState.nest.analyse(imageGroup.getOrgIpl().duplicate(), true);
        // auto save plugin config (but only if there is at least one snake)
        if (!qState.nest.isVacant()) {
          // Create Serialization object with extra info layer
          Serializer<SnakePluginList> s;
          s = new Serializer<>(qState.snakePluginList, quimpInfo);
          s.setPretty(); // set pretty format
          s.save(qState.boap.deductFilterFileName());
          s = null; // remove
          // Dump BOAState object in new format
          Serializer<DataContainer> n;
          DataContainer dt = new DataContainer(); // create container
          dt.BOAState = qState; // assign boa state to correct field
          // extract relevant data from CellStat
          dt.Stats = new StatsCollection();
          dt.Stats.copyFromCellStat(ret); // StatsHandler is initialized here.
          n = new Serializer<>(dt, quimpInfo);
          if (qState.boap.savePretty) {
            n.setPretty();
          }
          n.save(qState.boap.deductNewParamFileName());
          n = null;
        }
      } catch (IOException e) {
        IJ.error("Exception while saving");
        LOGGER.error("Exception while saving: " + e.getMessage());
        LOGGER.debug(e.getMessage(), e);
        return;
      }
    }
    BOA_.isBoaRunning = false;
    imageGroup.makeContourImage();
    qState.nest = null; // remove from memory
    imageGroup.getOrgIpl().setOverlay(new Overlay());
    new StackWindow(imageGroup.getOrgIpl()); // clear overlay
    window.setImage(new ImagePlus());
    window.close();
  }

  /**
   * Action for Quit button Set BOA_.running static field to false and close the window.
   * 
   */
  void quit() {
    YesNoCancelDialog ync;
    ync = new YesNoCancelDialog(window, "Quit", "Quit without saving?");
    if (!ync.yesPressed()) {
      return;
    }

    BOA_.isBoaRunning = false;
    qState.nest = null; // remove from memory
    imageGroup.getOrgIpl().setOverlay(new Overlay()); // clear overlay
    new StackWindow(imageGroup.getOrgIpl());

    window.setImage(new ImagePlus());// remove link to window
    window.close();
  }

}

/**
 * Hold, manipulate and draw on images.
 * 
 */
class ImageGroup {
  // paths - snake drawn as it contracts
  // contour - final snake drawn
  // org - original image, kept clean

  private ImagePlus orgIpl;
  private ImagePlus pathsIpl; // , contourIpl;
  private ImageStack orgStack;
  private ImageStack pathsStack; // , contourStack;
  private ImageProcessor orgIp;
  private ImageProcessor pathsIp; // , contourIp;
  private Overlay overlay;
  private Nest nest;
  private int iplWidth;
  private int iplHeight;
  private int iplStack;

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageGroup.class.getName());

  /**
   * Constructor.
   * 
   * @param ipl current image opened in IJ
   * @param n Nest object associated with BOA
   */
  public ImageGroup(ImagePlus ipl, Nest n) {
    nest = n;
    // create two new stacks for drawing

    // image set up
    orgIpl = ipl;
    orgIpl.setSlice(1);
    orgIpl.getCanvas().unzoom();
    orgIpl.getCanvas().getMagnification();

    orgStack = orgIpl.getStack();
    orgIp = orgStack.getProcessor(1);

    iplWidth = orgIp.getWidth();
    iplHeight = orgIp.getHeight();
    iplStack = orgIpl.getStackSize();

    // set up blank path image
    pathsIpl = NewImage.createByteImage("Node Paths", iplWidth, iplHeight, iplStack,
            NewImage.FILL_BLACK);
    pathsStack = pathsIpl.getStack();
    pathsIpl.setSlice(1);
    pathsIp = pathsStack.getProcessor(1);

    setIpSliceAll(1);
    setProcessor(1);
  }

  /**
   * Sets new Nest object associated with displayed image.
   * 
   * <p>Used after loading new BOAState
   * 
   * @param newNest new Nest
   */
  public void updateNest(Nest newNest) {
    nest = newNest;
  }

  public ImagePlus getOrgIpl() {
    return orgIpl;
  }

  public ImagePlus getPathsIpl() {
    return pathsIpl;
  }

  public ImageProcessor getOrgIp() {
    return orgIp;
  }

  /**
   * Plots snakes on current frame.
   * 
   * <p>Depending on configuration this method can plot:
   * <ol>
   * <li>Snake after segmentation, without processing by plugins
   * <li>Snake after segmentation and after processing by all active plugins
   * </ol>
   * It assign also last created Snake to ViewUpdater. This Snake can be accessed by plugin for
   * previewing purposes. If last Snake has been deleted, null is assigned or before last Snake
   * 
   * <p>Used when there is a need of redrawing screen because of new data
   * 
   * @param frame Current frame
   */
  public void updateOverlay(int frame) {
    LOGGER.trace("Update overlay for frame " + frame);
    SnakeHandler snakeH;
    Snake snake;
    Snake back;
    int x;
    int y;
    TextRoi text;
    Roi r;
    overlay = new Overlay();
    BOA_.viewUpdater.connectSnakeObject(null); //
    for (int i = 0; i < nest.size(); i++) {
      snakeH = nest.getHandler(i);
      if (snakeH.isStoredAt(frame)) { // is there a snake a;t iplStack?

        // plot segmented snake
        if (BOA_.qState.boap.isProcessedSnakePlotted == true) {
          back = snakeH.getBackupSnake(frame); // original unmodified snake
          // Roi r = snake.asRoi();
          r = back.asFloatRoi();
          r.setStrokeColor(Color.RED);
          overlay.add(r);
        }
        // remember instance of segmented snake for plugins (last created)
        BOA_.viewUpdater.connectSnakeObject(snakeH.getBackupSnake(frame));
        // plot segmented and filtered snake
        snake = snakeH.getStoredSnake(frame); // processed by plugins
        // Roi r = snake.asRoi();
        r = snake.asFloatRoi();
        r.setStrokeColor(Color.YELLOW);
        overlay.add(r);
        x = (int) Math.round(snake.getCentroid().getX()) - 15;
        y = (int) Math.round(snake.getCentroid().getY()) - 15;
        text = new TextRoi(x, y, "   " + snake.getSnakeID());
        overlay.add(text);

        // draw centre point
        PointRoi pointR =
                new PointRoi((int) snake.getCentroid().getX(), (int) snake.getCentroid().getY());
        overlay.add(pointR);

        // draw head node
        if (BOA_.qState.boap.isHeadPlotted == true) {
          // base point = 0 node
          Point2d bp = new Point2d(snake.getHead().getX(), snake.getHead().getY());

          // Plot Arrow mounted in 0 node and pointing direction of Snake
          Vector2d dir =
                  new Vector2d(snake.getHead().getNext().getNext().getNext().getX() - bp.getX(),
                          snake.getHead().getNext().getNext().getNext().getY() - bp.getY());
          FloatPolygon fp = GraphicsElements.plotArrow(dir, bp, 12.0f, 0.3f);
          PolygonRoi polygonR = new PolygonRoi(fp, Roi.POLYGON);
          polygonR.setStrokeColor(Color.MAGENTA);
          polygonR.setFillColor(Color.MAGENTA);
          overlay.add(polygonR);

          // plot circle on head
          FloatPolygon fp1 = GraphicsElements.getCircle(bp, 10);
          PolygonRoi polyginR1 = new PolygonRoi(fp1, Roi.POLYGON);
          polyginR1.setStrokeColor(Color.GREEN);
          polyginR1.setFillColor(Color.GREEN);
          overlay.add(polyginR1);
        }
        // dump String to log
        LOGGER.trace(snake.toString());
      } else {
        BOA_.viewUpdater.connectSnakeObject(null);
      }
    }
    orgIpl.setOverlay(overlay);
  }

  /**
   * Updates IJ to current frame. Causes that updateSliceSelector() is called.
   * 
   * <p>USed when there is a need to move to other frame programmatically.
   * 
   * @param frame current frame
   * 
   */
  public void updateToFrame(int frame) {
    clearPaths(frame);
    setProcessor(frame);
    setIpSliceAll(frame);
  }

  public void clearOverlay() {
    // overlay = new Overlay();
    orgIpl.setOverlay(null);
  }

  /**
   * Set internal field to currently processed ImageProcessor.
   * 
   * <p>Those fields are used by e.g. {@link BOA_#runBoa(int, int)} during computations.
   * 
   * @param i current frame
   */
  public final void setProcessor(int i) {
    orgIp = orgStack.getProcessor(i);
    pathsIp = pathsStack.getProcessor(i);
    // System.out.println("\n1217 Proc set to : " + i);
  }

  /**
   * Set slice in stack. Call updateSliceSelector callback only if i != current frame.
   * 
   * @param i slice number
   */
  public final void setIpSliceAll(int i) {
    // set slice on all images
    pathsIpl.setSlice(i);
    orgIpl.setSlice(i);
  }

  public void clearPaths(int fromFrame) {
    for (int i = fromFrame; i <= BOA_.qState.boap.getFrames(); i++) {
      pathsIp = pathsStack.getProcessor(i);
      pathsIp.setValue(0);
      pathsIp.fill();
    }
    pathsIp = pathsStack.getProcessor(fromFrame);
  }

  public void drawPath(Snake snake, int frame) {
    pathsIp = pathsStack.getProcessor(frame);
    drawSnake(pathsIp, snake, false);
  }

  private void drawSnake(final ImageProcessor ip, final Snake snake, boolean contrast) {
    // draw snake
    int x;
    int y;
    int intensity;

    Node n = snake.getHead();
    do {
      x = (int) (n.getPoint().getX());
      y = (int) (n.getPoint().getY());

      if (!contrast) {
        intensity = 245;
      } else {
        // paint as black or white for max contrast
        if (ip.getPixel(x, y) > 127) {
          intensity = 10;
        } else {
          intensity = 245;
        }
      }
      // for colour:
      // if(boap.drawColor) intensity = n.colour.getColorInt();

      if (BOA_.qState.boap.getHeight() > 800) {
        drawPixel(x, y, intensity, true, ip);
      } else {
        drawPixel(x, y, intensity, false, ip);
      }
      n = n.getNext();
    } while (!n.isHead());
  }

  private void drawPixel(int x, int y, int intensity, boolean fat, ImageProcessor ip) {
    ip.putPixel(x, y, intensity);
    if (fat) {
      ip.putPixel(x + 1, y, intensity);
      ip.putPixel(x + 1, y + 1, intensity);
      ip.putPixel(x, y + 1, intensity);
      ip.putPixel(x - 1, y + 1, intensity);
      ip.putPixel(x - 1, y, intensity);
      ip.putPixel(x - 1, y - 1, intensity);
      ip.putPixel(x, y - 1, intensity);
      ip.putPixel(x + 1, y - 1, intensity);
    }
  }

  void makeContourImage() {
    ImagePlus contourIpl = NewImage.createByteImage("Contours", iplWidth, iplHeight, iplStack,
            NewImage.FILL_BLACK);
    ImageStack contourStack = contourIpl.getStack();
    contourIpl.setSlice(1);
    ImageProcessor contourIp;

    for (int i = 1; i <= BOA_.qState.boap.getFrames(); i++) { // copy original
      orgIp = orgStack.getProcessor(i);
      contourIp = contourStack.getProcessor(i);
      contourIp.copyBits(orgIp, 0, 0, Blitter.COPY);
    }

    drawCellRois(contourStack);
    new ImagePlus(orgIpl.getTitle() + "_Segmentation", contourStack).show();

  }

  /**
   * Zoom current view to snake with snakeID.
   * 
   * <p>If snake is not found nothing happens.
   * 
   * @param ic Current view
   * @param frame Frame the Snake is looked in
   * @param snakeID ID of Snake one looks for
   */
  void zoom(final ImageCanvas ic, int frame, int snakeID) {
    LOGGER.trace("Zoom to frame: " + frame + " ID " + snakeID);
    if (nest.isVacant() || snakeID < 0) {
      return; // negative id or empty nest
    }
    SnakeHandler snakeH;
    Snake snake;

    try {
      snakeH = nest.getHandlerofId(snakeID);// snakeID, not index
      if (snakeH != null && snakeH.isStoredAt(frame)) {
        snake = snakeH.getStoredSnake(frame);
      } else {
        return;
      }
    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
      LOGGER.debug(e.getMessage(), e);
      return;
    }

    Rectangle r = snake.getBounds();
    int border = 40;

    // add border (10 either way)
    r.setBounds(r.x - border, r.y - border, r.width + border * 2, r.height + border * 2);

    // correct r's aspect ratio
    double icAspect = (double) ic.getWidth() / (double) ic.getHeight();
    double rectAspect = r.getWidth() / r.getHeight();
    int newDim; // new dimenesion size

    if (icAspect < rectAspect) {
      // too short
      newDim = (int) (r.getWidth() / icAspect);
      r.y = r.y - ((newDim - r.height) / 2); // move snake to center
      r.height = newDim;
    } else {
      // too thin
      newDim = (int) (r.getHeight() * icAspect);
      r.x = r.x - ((newDim - r.width) / 2); // move snake to center
      r.width = newDim;
    }

    // System.out.println("ic " + icAspect + ". rA: " + r.getWidth() /
    // r.getHeight());

    double newMag;

    newMag = (double) ic.getHeight() / r.getHeight(); // mag required

    ic.setMagnification(newMag);
    Rectangle sr = ic.getSrcRect();
    sr.setBounds(r);

    ic.repaint();

  }

  void unzoom(final ImageCanvas ic) {
    // Rectangle sr = ic.getSrcRect();
    // sr.setBounds(0, 0, boap.WIDTH, boap.HEIGHT);
    ic.unzoom();
    // ic.setMagnification(orgMag);
    // ic.repaint();
  }

  /**
   * Produces final image with Snake outlines after finishing BOA.
   * 
   * <p>This is ImageJ image with flatten Snake contours.
   * 
   * @param stack Stack to plot in
   */
  void drawCellRois(final ImageStack stack) {
    Snake snake;
    SnakeHandler snakeH;
    ImageProcessor ip;

    int x;
    int y;
    for (int s = 0; s < nest.size(); s++) {
      snakeH = nest.getHandler(s);
      for (int i = 1; i <= BOA_.qState.boap.getFrames(); i++) {
        if (snakeH.isStoredAt(i)) {
          snake = snakeH.getStoredSnake(i);
          ip = stack.getProcessor(i);
          ip.setColor(255);
          ip.draw(snake.asFloatRoi());
          x = (int) Math.round(snake.getHead().getX()) - 15;
          y = (int) Math.round(snake.getHead().getY()) - 15;
          ip.moveTo(x, y);
          ip.drawString("   " + snake.getSnakeID());
          LOGGER.trace("Snake head is at: " + snake.getHead().toString());
        }
      }
    }
  }
}
