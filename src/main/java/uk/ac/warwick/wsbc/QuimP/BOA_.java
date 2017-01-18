package uk.ac.warwick.wsbc.QuimP;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
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
import uk.ac.warwick.wsbc.QuimP.BOAState.BOAp;
import uk.ac.warwick.wsbc.QuimP.SnakePluginList.Plugin;
import uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer;
import uk.ac.warwick.wsbc.QuimP.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.QuimP.filesystem.StatsCollection;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.binaryseg.BinarySegmentationPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOAPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOASnakeFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter;
import uk.ac.warwick.wsbc.QuimP.registration.Registration;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;
import uk.ac.warwick.wsbc.QuimP.utils.graphics.GraphicsElements;

/**
 * Main class implementing BOA plugin.
 * 
 * @author Richard Tyson
 * @author Till Bretschneider
 * @author Piotr Baniukiewicz
 */
public class BOA_ implements PlugIn {
    private static final Logger LOGGER = LoggerFactory.getLogger(BOA_.class.getName());
    CustomCanvas canvas;
    CustomStackWindow window;
    static TextArea logArea;
    static boolean running = false;
    ImageGroup imageGroup;
    private Constrictor constrictor;
    private PluginFactory pluginFactory; // load and maintain plugins
    private String lastTool; // last selection tool selected in IJ remember last tool to reselect
                             // it after truncating or deleting operation
    /**
     * Reserved word that stands for plugin that is not selected
     */
    public final static String NONE = "NONE";
    /**
     * Reserved word that states full view zoom in zoom choice. Also default text that appears there
     */
    private final static String fullZoom = "Frame zoom";
    /**
     * Hold current BOA object and provide access to only selected methods from plugin.
     * 
     * Reference to this field is passed to plugins and give them possibility to call selected
     * methods from BOA class
     * 
     * TODO Should not be static rather
     */
    public static ViewUpdater viewUpdater;
    /**
     * Keep data from getQuimPBuildInfo().
     * 
     * These information are used in About dialog, window title bar, logging, etc. Static because
     * window related staff is in another classes.
     */
    public static String[] quimpInfo;
    private static int logCount; // add counter to logged messages
    public static final int NUM_SNAKE_PLUGINS = 3; /*!< number of Snake plugins  */
    private HistoryLogger historyLogger; // logger
    /**
     * Configuration object, available from all modules.
     * 
     * Must be initialised here <b>AND</b> in constructor (to reset settings on next BOA call
     * without quitting Fiji) Keep data that will be serialized.
     */
    static public BOAState qState; // current state of BOA module

    /**
     * Main constructor.
     * 
     * All static resources should be re-initialized here, otherwise they persist in memory between
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

        if (BOA_.running) {
            BOA_.running = false;
            IJ.error("Warning: Only have one instance of BOA running at a time");
            return;
        }
        // assign current object to ViewUpdater
        viewUpdater = new ViewUpdater(this);
        // collect information about quimp version read from jar
        quimpInfo = new QuimpToolsCollection().getQuimPBuildInfo();
        // create history logger
        historyLogger = new HistoryLogger();

        // Build plugin engine
        try {
            String path = IJ.getDirectory("plugins");
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
        // initialize arrays for plugins instances and give them initial values (GUI)
        qState = new BOAState(ip, pluginFactory, viewUpdater); // create BOA state machine
        if (IJ.getVersion().compareTo("1.46") < 0) {
            qState.boap.useSubPixel = false;
        } else {
            qState.boap.useSubPixel = true;
        }

        lastTool = IJ.getToolName();
        // stack or single image?
        if (ip == null) {
            IJ.error("Image required");
            return;
        } else if (ip.getStackSize() == 1) {
            qState.boap.singleImage = true;
        } else {
            qState.boap.singleImage = false;
        }
        // check if 8-bit image
        if (ip.getType() != ImagePlus.GRAY8) {
            YesNoCancelDialog ync = new YesNoCancelDialog(window, "Image bit depth",
                    "8-bit Image required. Convert?");
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
        BOA_.running = true;
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
     * windowListener for cleaning after closing the main window by user. git tag -a
     * "SNAPSHOT-13-07-16" -m "Releasing snaphots to Fiji internal update site"
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
        window.setTitle(window.getTitle() + " :QuimP: " + quimpInfo[0]);
        // validate registered user
        new Registration(window, "QuimP Registration");
        // warn about scale
        if (qState.boap.isScaleAdjusted()) {
            BOA_.log("WARNING Scale was zero - set to 1");
        }
        if (qState.boap.isfIAdjusted()) {
            BOA_.log("WARNING Frame interval was zero - set to 1");
        }

        // adds window listener called on plugin closing
        window.addWindowListener(new CustomWindowAdapter());

        setScales();
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
     * Display about information in BOA window.
     * 
     * Called from menu bar. Reads also information from all found plugins.
     */
    void about() {
        AboutDialog ad = new AboutDialog(window); // create about dialog with parent 'window'
        ad.appendLine(QuimpToolsCollection.getFormattedQuimPversion(quimpInfo)); // display template
                                                                                 // filled by
        // quimpInfo
        // get list of found plugins
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
            if (tmpinst != null) // can be null on problem with instance
            {
                String about = tmpinst.about(); // may return null
                if (about != null)
                    ad.appendLine(about);
                else
                    ad.appendLine("Plugin does not provide about note");
            }
            ad.appendDistance();
        }
        ad.setVisible(true); // must be after adding content
    }

    /**
     * Append string to log window in BOA plugin
     * 
     * @param s String to display in BOA window
     */
    static void log(final String s) {
        if (logArea == null)
            LOGGER.debug("[" + logCount++ + "] " + s + '\n');
        else
            logArea.append("[" + logCount++ + "] " + s + '\n');
    }

    /**
     * Redraw current view. Process outlines by all active plugins. Do not run segmentation again
     * Updates \c liveSnake.
     */
    public void recalculatePlugins() {
        LOGGER.trace("BOA: recalculatePlugins called");
        SnakeHandler sH;
        if (qState.nest.isVacant()) { // only update screen
            imageGroup.updateOverlay(qState.boap.frame);
            return;
        }
        imageGroup.updateToFrame(qState.boap.frame);
        try {
            for (int s = 0; s < qState.nest.size(); s++) { // for each snake
                sH = qState.nest.getHandler(s);
                if (qState.boap.frame < sH.getStartFrame()) // if snake does not exist on current
                                                            // frame
                    continue;
                // but if one is on frame f+n and strtFrame is e.g. 1 it may happen that there is
                // no continuity of this snake between frames. In this case getBackupSnake
                // returns null. In general QuimP assumes that if there is a cell on frame f, it
                // will exist on all consecutive frames.
                Snake snake = sH.getBackupSnake(qState.boap.frame); // if exist get its backup copy
                // (segm)
                if (snake == null || !snake.alive) // if not alive
                    continue;
                try {
                    Snake out = iterateOverSnakePlugins(snake); // apply all plugins to snake
                    sH.storeThisSnake(out, qState.boap.frame); // set processed snake as final
                } catch (QuimpPluginException qpe) {
                    // must be rewritten with whole runBOA #65 #67
                    BOA_.log("Error in filter module: " + qpe.getMessage());
                    LOGGER.error("Error in filter module: " + qpe.getMessage());
                    LOGGER.debug(qpe.getMessage(), qpe);
                    sH.storeLiveSnake(qState.boap.frame); // so store only segmented snake as final
                }
            }
        } catch (Exception e) {
            LOGGER.error("Can not update view. Output snake may be defective: " + e.getMessage());
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
     * When user closes window by system button QuimP does not ask for saving current work. This is
     * because by default QuimP window is managed by ImageJ and it probably only hides it on closing
     * 
     * This class could be located directly in CustomStackWindow which is included in BOA_. But it
     * needs to have access to BOA field <tt>running</tt>.
     * 
     * @author p.baniukiewicz
     */
    class CustomWindowAdapter extends WindowAdapter {
        @Override
        // This method will be called when BOA_ window is closed already
        // It is too late for asking user
        public void windowClosed(final WindowEvent arg0) {
            LOGGER.trace("CLOSED");
            BOA_.running = false; // set marker
            qState.snakePluginList.clear(); // close all opened plugin windows
            if (qState.binarySegmentationPlugin != null)
                qState.binarySegmentationPlugin.showUI(false);
            canvas = null; // clear window data
            imageGroup = null;
            window = null;
            // clear static
            qState = null;
            viewUpdater = null;
        }

        @Override
        public void windowClosing(final WindowEvent arg0) {
            LOGGER.trace("CLOSING");
        }

        @Override
        public void windowActivated(final WindowEvent e) {
            LOGGER.trace("ACTIVATED");
            // rebuild menu for this local window
            // workaround for Mac and theirs menus on top screen bar
            // IJ is doing the same for activation of its window so every time one has correct menu
            // on top
            window.setMenuBar(window.quimpMenuBar);
        }
    }

    /**
     * Supports mouse actions on image at QuimP window according to selected option
     * 
     * @author rtyson
     *
     */
    @SuppressWarnings("serial")
    class CustomCanvas extends ImageCanvas {

        /**
         * Empty constructor
         * 
         * @param imp Reference to image loaded by BOA
         */
        CustomCanvas(final ImagePlus imp) {
            super(imp);
        }

        /**
         * @deprecated Actually not used in this version of QuimP
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
     * This class stands for definition of main BOA plugin GUI window. Current state of BOA plugin
     * is stored at {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp} class.
     * 
     * @author rtyson
     * @see BOAp
     */
    @SuppressWarnings("serial")
    class CustomStackWindow extends StackWindow
            implements ActionListener, ItemListener, ChangeListener {

        final static int DEFAULT_SPINNER_SIZE = 5;
        final static int SNAKE_PLUGIN_NUM = 3; /*!< number of currently supported plugins  */
        private Button bFinish, bSeg, bLoad, bEdit, bDefault, bScale;
        private Button bAdd, bDel, bDelSeg, bQuit;
        private Checkbox cPrevSnake, cExpSnake, cPath;
        private Choice sZoom;
        JScrollPane logPanel;
        Label fpsLabel, pixelLabel, frameLabel;
        JSpinner dsNodeRes, dsVel_crit, dsF_image, dsF_central, dsF_contract, dsFinalShrink;
        JSpinner isMaxIterations, isBlowup, isSample_tan, isSample_norm;
        private Choice sFirstPluginName, sSecondPluginName, sThirdPluginName;
        private Button bFirstPluginGUI, bSecondPluginGUI, bThirdPluginGUI;
        private Checkbox cFirstPluginActiv, cSecondPluginActiv, cThirdPluginActiv;

        private MenuBar quimpMenuBar;
        private MenuItem menuAbout, menuOpenHelp, menuSaveConfig, menuLoadConfig, menuShowHistory,
                menuLoad, menuDeletePlugin, menuApplyPlugin, menuSegmentationRun,
                menuSegmentationReset; // items
        private CheckboxMenuItem cbMenuPlotOriginalSnakes, cbMenuPlotHead;
        private Color defaultColor;

        /**
         * Default constructor
         * 
         * @param imp Image loaded to plugin
         * @param ic Image canvas
         */
        CustomStackWindow(final ImagePlus imp, final ImageCanvas ic) {
            super(imp, ic);

        }

        /**
         * Build user interface.
         * 
         * This method is called as first. The interface is built in three steps: Left side of
         * window (configuration zone) and right side of main window (logs and other info and
         * buttons) and finally upper menubar
         * 
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateWindowState()
         */
        private void buildWindow() {

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
            quimpMenuBar = buildMenu(); // store menu in var to reuse on window activation
            setMenuBar(quimpMenuBar);
            pack();
            updateWindowState(); // window logic on start
            defaultColor = sp.getBackground();

        }

        /**
         * Build window menu.
         * 
         * Menu is local for this window of QuimP and it is stored in \c quimpMenuBar variable. On
         * every time when QuimP is active, this menu is restored in
         * uk.ac.warwick.wsbc.QuimP.BOA_.CustomWindowAdapter.windowActivated(WindowEvent) method
         * This is due to overwriting menu by IJ on Mac (all menus are on top screen bar)
         * 
         * @return Reference to menu bar
         */
        final MenuBar buildMenu() {
            MenuBar menuBar; // main menu bar
            Menu menuHelp; // menu About in menubar
            Menu menuConfig; // menu Config in menubar
            Menu menuFile; // menu File in menubar
            Menu menuPlugin; // menu Plugin in menubar
            Menu menuSegmentation; // menu Segmentation in menubar

            menuBar = new MenuBar();

            menuConfig = new Menu("Preferences");
            menuHelp = new Menu("Help");
            menuFile = new Menu("File");
            menuPlugin = new Menu("Plugin");
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

            menuSegmentationRun = new MenuItem("Binary segmentation");
            menuSegmentationRun.addActionListener(this);
            menuSegmentationReset = new MenuItem("Clear all");
            menuSegmentationReset.addActionListener(this);
            menuSegmentation.add(menuSegmentationRun);
            menuSegmentation.add(menuSegmentationReset);

            return menuBar;
        }

        /**
         * Build right side of main BOA window
         * 
         * @return Reference to panel
         */
        final Panel buildSetupPanel() {
            Panel setupPanel = new Panel(); // Main panel comprised from North, Centre and South
                                            // subpanels
            Panel northPanel = new Panel(); // Contains static info and four buttons (Scale,
                                            // Truncate, Add, Delete)
            Panel southPanel = new Panel(); // Quit and Finish
            Panel centerPanel = new Panel();
            Panel pluginPanel = new Panel();

            setupPanel.setLayout(new BorderLayout());
            northPanel.setLayout(new GridLayout(3, 2));
            southPanel.setLayout(new GridLayout(2, 2));
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));

            // Grid bag for plugin zone
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            pluginPanel.setLayout(gridbag);

            fpsLabel = new Label(
                    "F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
            northPanel.add(fpsLabel);
            pixelLabel = new Label("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
            northPanel.add(pixelLabel);

            bScale = addButton("Set Scale", northPanel);
            bDelSeg = addButton("Truncate Seg", northPanel);
            bAdd = addButton("Add cell", northPanel);
            bDel = addButton("Delete cell", northPanel);

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
            sFirstPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
            c.gridx = 0;
            c.gridy = 0;
            pluginPanel.add(sFirstPluginName, c);

            bFirstPluginGUI = addButton("GUI", pluginPanel);
            c.gridx = 1;
            c.gridy = 0;
            pluginPanel.add(bFirstPluginGUI, c);

            cFirstPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(0));
            c.gridx = 2;
            c.gridy = 0;
            pluginPanel.add(cFirstPluginActiv, c);

            sSecondPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
            c.gridx = 0;
            c.gridy = 1;
            pluginPanel.add(sSecondPluginName, c);

            bSecondPluginGUI = addButton("GUI", pluginPanel);
            c.gridx = 1;
            c.gridy = 1;
            pluginPanel.add(bSecondPluginGUI, c);

            cSecondPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(1));
            c.gridx = 2;
            c.gridy = 1;
            pluginPanel.add(cSecondPluginActiv, c);

            sThirdPluginName = addComboBox(pluginList.toArray(new String[0]), pluginPanel);
            c.gridx = 0;
            c.gridy = 2;
            pluginPanel.add(sThirdPluginName, c);

            bThirdPluginGUI = addButton("GUI", pluginPanel);
            c.gridx = 1;
            c.gridy = 2;
            pluginPanel.add(bThirdPluginGUI, c);

            cThirdPluginActiv = addCheckbox("A", pluginPanel, qState.snakePluginList.isActive(2));
            c.gridx = 2;
            c.gridy = 2;
            pluginPanel.add(cThirdPluginActiv, c);

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
            bQuit = addButton("Quit", southPanel);
            bFinish = addButton("Save & Quit", southPanel);
            // ------------------------------

            centerPanel.add(new Label("Snake Plugins:"));
            centerPanel.add(pluginPanel);
            centerPanel.add(new Label("Logs:"));
            centerPanel.add(logPanel);
            setupPanel.add(northPanel, BorderLayout.PAGE_START);
            setupPanel.add(centerPanel, BorderLayout.CENTER);
            setupPanel.add(southPanel, BorderLayout.PAGE_END);

            if (pluginList.isEmpty())
                BOA_.log("No plugins found");
            else
                BOA_.log("Found " + (pluginList.size() - 1) + " plugins (see About)");

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
            topPanel.setLayout(new GridLayout(1, 2));
            paramPanel.setLayout(new GridLayout(14, 1));
            bottomPanel.setLayout(new GridLayout(1, 2));

            // --------build topPanel--------
            bLoad = addButton("Load", topPanel);
            bDefault = addButton("Default", topPanel);
            // -----------------------

            // --------build paramPanel--------------
            dsNodeRes = addDoubleSpinner("Node Spacing:", paramPanel, qState.segParam.getNodeRes(),
                    1., 20., 0.2, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isMaxIterations =
                    addIntSpinner("Max Iterations:", paramPanel, qState.segParam.max_iterations,
                            100, 10000, 100, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isBlowup = addIntSpinner("Blowup:", paramPanel, qState.segParam.blowup, 0, 200, 2,
                    CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsVel_crit = addDoubleSpinner("Crit velocity:", paramPanel, qState.segParam.vel_crit,
                    0.0001, 2., 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_image = addDoubleSpinner("Image F:", paramPanel, qState.segParam.f_image, 0.01, 10.,
                    0.01, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_central = addDoubleSpinner("Central F:", paramPanel, qState.segParam.f_central,
                    0.0005, 1, 0.002, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_contract = addDoubleSpinner("Contract F:", paramPanel, qState.segParam.f_contract,
                    0.001, 1, 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsFinalShrink =
                    addDoubleSpinner("Final Shrink:", paramPanel, qState.segParam.finalShrink, -100,
                            100, 0.5, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isSample_tan = addIntSpinner("Sample tan:", paramPanel, qState.segParam.sample_tan, 1,
                    30, 1, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isSample_norm = addIntSpinner("Sample norm:", paramPanel, qState.segParam.sample_norm,
                    1, 60, 1, CustomStackWindow.DEFAULT_SPINNER_SIZE);

            cPrevSnake = addCheckbox("Use Previouse Snake", paramPanel,
                    qState.segParam.use_previous_snake);
            cExpSnake = addCheckbox("Expanding Snake", paramPanel, qState.segParam.expandSnake);

            Panel segEditPanel = new Panel();
            segEditPanel.setLayout(new GridLayout(1, 2));
            bSeg = addButton("SEGMENT", segEditPanel);
            bEdit = addButton("Edit", segEditPanel);
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
            cPath = addCheckbox("Show paths", bottomPanel, qState.segParam.showPaths);
            sZoom = addComboBox(new String[] { fullZoom }, bottomPanel);
            // add mouse listener to create menu dynamically on click
            sZoom.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    sZoom.removeAll();
                    sZoom.add(fullZoom); // default word for full zoom (100% of view)
                    List<Integer> frames = qState.nest.getSnakesforFrame(qState.boap.frame);
                    for (Integer i : frames)
                        sZoom.add(i.toString());
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
         * Helper method for creating checkbox in UI
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
            for (String st : s)
                c.add(st);
            c.select(0);
            c.addItemListener(this);
            mp.add(c);
            return c;
        }

        /**
         * Helper method for creating spinner in UI with real values
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
         * Helper method for creating spinner in UI with integer values
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
         * Set default values defined in model class {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp}
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
         * {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp}
         * 
         * @see BOAp
         */
        private void updateSpinnerValues() {
            qState.boap.supressStateChangeBOArun = true; // block rerun of runBoa() that is called
                                                         // on Spinner event
            dsNodeRes.setValue(qState.segParam.getNodeRes());
            dsVel_crit.setValue(qState.segParam.vel_crit);
            dsF_image.setValue(qState.segParam.f_image);
            dsF_central.setValue(qState.segParam.f_central);
            dsF_contract.setValue(qState.segParam.f_contract);
            dsFinalShrink.setValue(qState.segParam.finalShrink);
            isMaxIterations.setValue(qState.segParam.max_iterations);
            isBlowup.setValue(qState.segParam.blowup);
            isSample_tan.setValue(qState.segParam.sample_tan);
            isSample_norm.setValue(qState.segParam.sample_norm);
            qState.boap.supressStateChangeBOArun = false;
        }

        /**
         * Update checkboxes
         * 
         * @see uk.ac.warwick.wsbc.QuimP.SnakePluginList
         * @see #itemStateChanged(ItemEvent)
         */
        private void updateCheckBoxes() {
            // first plugin activity
            cFirstPluginActiv.setState(qState.snakePluginList.isActive(0));
            // second plugin activity
            cSecondPluginActiv.setState(qState.snakePluginList.isActive(1));
            // third plugin activity
            cThirdPluginActiv.setState(qState.snakePluginList.isActive(2));
        }

        /**
         * Update Choices.
         * 
         * This method is called from CustomStackWindow.itemStateChanged(ItemEvent) to update colors
         * of Choices.
         * 
         * @see uk.ac.warwick.wsbc.QuimP.SnakePluginList
         * @see #itemStateChanged(ItemEvent)
         */
        private void updateChoices() {
            Color ok = new Color(178, 255, 102);
            Color bad = new Color(255, 153, 153);
            // first slot snake plugin
            if (qState.snakePluginList.getName(0).isEmpty()) {
                sFirstPluginName.select(NONE);
                sFirstPluginName.setBackground(defaultColor);
            } else {
                sFirstPluginName.select(qState.snakePluginList.getName(0)); // try to select name
                                                                            // from pluginList in
                                                                            // choice
                if (sFirstPluginName.getSelectedItem().equals(NONE)) {// tried selecting but still
                                                                      // on none - it means that
                                                                      // plugin name from
                                                                      // snkePluginList is not on
                                                                      // choice list. Tis may happen
                                                                      // when choice is propagated
                                                                      // from directory but
                                                                      // snakePluginList from
                                                                      // external QCONF
                    sFirstPluginName.add(qState.snakePluginList.getName(0)); // add to list
                    sFirstPluginName.setBackground(bad); // set as bad
                } else if (qState.snakePluginList.getInstance(0) == null) // WARN does not check if
                                                                          // instance(0) is the
                                                                          // instance of getName(0)
                    sFirstPluginName.setBackground(bad);
                else
                    sFirstPluginName.setBackground(ok);
            }
            // second slot snake plugin
            if (qState.snakePluginList.getName(1).isEmpty()) {
                sSecondPluginName.select(NONE);
                sSecondPluginName.setBackground(defaultColor);
            } else {
                sSecondPluginName.select(qState.snakePluginList.getName(1));
                if (sSecondPluginName.getSelectedItem().equals(NONE)) {
                    sSecondPluginName.add(qState.snakePluginList.getName(1)); // add to list
                    sSecondPluginName.setBackground(bad); // set as bad
                } else if (qState.snakePluginList.getInstance(1) == null)
                    sSecondPluginName.setBackground(bad);
                else
                    sSecondPluginName.setBackground(ok);
            }
            // third slot snake plugin
            if (qState.snakePluginList.getName(2).isEmpty()) {
                sThirdPluginName.select(NONE);
                sThirdPluginName.setBackground(defaultColor);
            } else {
                sThirdPluginName.select(qState.snakePluginList.getName(2));
                if (sThirdPluginName.getSelectedItem().equals(NONE)) {
                    sThirdPluginName.add(qState.snakePluginList.getName(2)); // add to list
                    sThirdPluginName.setBackground(bad); // set as bad
                } else if (qState.snakePluginList.getInstance(2) == null)
                    sThirdPluginName.setBackground(bad);
                else
                    sThirdPluginName.setBackground(ok);
            }

        }

        /**
         * Implement user interface logic.
         * 
         * Do not refresh values, rather disable/enable controls.
         */
        private void updateWindowState() {
            updateCheckBoxes(); // update checkboxes
            updateChoices(); // and choices

            // Rule 1 - NONE on any slot in filters disable GUI button and Active checkbox
            if (sFirstPluginName.getSelectedItem() == NONE) {
                cFirstPluginActiv.setEnabled(false);
                bFirstPluginGUI.setEnabled(false);
            } else {
                cFirstPluginActiv.setEnabled(true);
                bFirstPluginGUI.setEnabled(true);
            }
            if (sSecondPluginName.getSelectedItem() == NONE) {
                cSecondPluginActiv.setEnabled(false);
                bSecondPluginGUI.setEnabled(false);
            } else {
                cSecondPluginActiv.setEnabled(true);
                bSecondPluginGUI.setEnabled(true);
            }
            if (sThirdPluginName.getSelectedItem() == NONE) {
                cThirdPluginActiv.setEnabled(false);
                bThirdPluginGUI.setEnabled(false);
            } else {
                cThirdPluginActiv.setEnabled(true);
                bThirdPluginGUI.setEnabled(true);
            }

        }

        /**
         * Main method that handles all actions performed on UI elements.
         * 
         * Do not support mouse events, only UI elements like buttons, spinners and menus. Runs also
         * main algorithm on specified input state and update screen on plugins operations.
         * 
         * @param e Type of event
         * @see uk.ac.warwick.wsbc.QuimP.BOAState.BOAp
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateWindowState()
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            LOGGER.debug("EVENT:actionPerformed");
            boolean run = false; // some actions require to re-run segmentation. They set it to true
            Object b = e.getSource();
            if (b == bDel && !qState.boap.editMode && !qState.boap.doDeleteSeg) {
                if (qState.boap.doDelete == false) {
                    bDel.setLabel("*STOP DEL*");
                    qState.boap.doDelete = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                } else {
                    qState.boap.doDelete = false;
                    bDel.setLabel("Delete cell");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (qState.boap.doDelete) { // stop if delete is on
                BOA_.log("**DELETE IS ON**");
                return;
            }
            if (b == bDelSeg && !qState.boap.editMode) {
                if (!qState.boap.doDeleteSeg) {
                    bDelSeg.setLabel("*STOP TRUNCATE*");
                    qState.boap.doDeleteSeg = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                } else {
                    qState.boap.doDeleteSeg = false;
                    bDelSeg.setLabel("Truncate Seg");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (qState.boap.doDeleteSeg) { // stop if delete is on
                BOA_.log("**TRUNCATE SEG IS ON**");
                return;
            }
            if (b == bEdit) {
                if (qState.boap.editMode == false) {
                    bEdit.setLabel("*STOP EDIT*");
                    BOA_.log("**EDIT IS ON**");
                    qState.boap.editMode = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                    if (qState.nest.size() == 1)
                        editSeg(0, 0, qState.boap.frame); // if only 1 snake go straight to edit, if
                    // more user must pick one
                    // remember that this frame is edited
                    qState.storeOnlyEdited(qState.boap.frame);
                } else {
                    qState.boap.editMode = false;
                    if (qState.boap.editingID != -1) {
                        stopEdit();
                    }
                    bEdit.setLabel("Edit");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (qState.boap.editMode) { // stop if edit on
                BOA_.log("**EDIT IS ON**");
                return;
            }
            if (b == bDefault) {
                this.setDefualts();
                run = true;
            } else if (b == bSeg) { // main segmentation procedure starts here
                IJ.showStatus("SEGMENTING...");
                bSeg.setLabel("computing");
                int framesCompleted;
                try {
                    runBoa(qState.boap.frame, qState.boap.getFRAMES());
                    framesCompleted = qState.boap.getFRAMES();
                    IJ.showStatus("COMPLETE");
                } catch (BoaException be) {
                    BOA_.log(be.getMessage());
                    framesCompleted = be.getFrame();
                    IJ.showStatus("FAIL AT " + framesCompleted);
                    BOA_.log("FAIL AT " + framesCompleted);
                }
                bSeg.setLabel("SEGMENT");
            } else if (b == bLoad) {
                try {
                    if (qState.readParams()) {
                        updateSpinnerValues();
                        if (loadSnakes()) {
                            run = false;
                        } else {
                            run = true;
                        }

                    }
                } catch (Exception IOe) {
                    IJ.error("Exception when reading parameters from file...");
                }

            } else if (b == bScale) {
                setScales();
                pixelLabel.setText("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
                fpsLabel.setText(
                        "F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
            } else if (b == bAdd) {
                addCell(canvas.getImage().getRoi(), qState.boap.frame);
                canvas.getImage().killRoi();
            } else if (b == bFinish) {
                BOA_.log("Finish: Exiting BOA...");
                fpsLabel.setName("moo");
                finish();
            } else if (b == bQuit) {
                quit();
            }
            // process plugin GUI buttons
            if (b == bFirstPluginGUI) {
                LOGGER.debug("First plugin GUI, state of BOAp is "
                        + qState.snakePluginList.getInstance(0));
                if (qState.snakePluginList.getInstance(0) != null) // call 0 instance
                    qState.snakePluginList.getInstance(0).showUI(true);
            }
            if (b == bSecondPluginGUI) {
                LOGGER.debug("Second plugin GUI, state of BOAp is "
                        + qState.snakePluginList.getInstance(1));
                if (qState.snakePluginList.getInstance(1) != null) // call 1 instance
                    qState.snakePluginList.getInstance(1).showUI(true);
            }
            if (b == bThirdPluginGUI) {
                LOGGER.debug("Third plugin GUI, state of BOAp is "
                        + qState.snakePluginList.getInstance(2));
                if (qState.snakePluginList.getInstance(2) != null) // call 2 instance
                    qState.snakePluginList.getInstance(2).showUI(true);
            }

            // menu listeners
            if (b == menuAbout) {
                about();
            }
            if (b == menuOpenHelp) {
                String url =
                        new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
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
                // get extension from deduced output name
                Path p = Paths.get(qState.boap.deductFilterFileName()).getFileName();
                SaveDialog sd = new SaveDialog("Save plugin config data...", saveIn,
                        qState.boap.getFileName(),
                        "." + QuimpToolsCollection.getFileExtension(p.toString()));
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

            /**
             * Loads configuration of current filter stack.
             * 
             * @see <a href=
             *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/155">http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/155</a>
             */
            if (b == menuLoadConfig) {
                OpenDialog od = new OpenDialog("Load plugin config data...", "");
                if (od.getFileName() != null) {
                    try {
                        Serializer<SnakePluginList> loaded; // loaded instance
                        // create serializer
                        Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
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

            /**
             * Shows history window.
             * 
             * When showed all actions are notified there. This may slow down the program
             */
            if (b == menuShowHistory) {
                JOptionPane.showMessageDialog(window,
                        "The full history of changes is avaiable after saving your work in the"
                                + " file " + FileExtensions.newConfigFileExt);
                /*
                 * if (historyLogger.isOpened()) historyLogger.closeHistory(); else
                 * historyLogger.openHistory();
                 */
            }

            /**
             * Load global config - QCONF file.
             * 
             * Checks also whether the name of the image sealed in config file is the same as those
             * opened currently. If not user has an option to break the procedure or continue
             * loading.
             */
            if (b == menuLoad) {
                OpenDialog od = new OpenDialog(
                        "Load global config data...(*" + FileExtensions.newConfigFileExt + ")", "");
                if (od.getFileName() != null) {
                    try {
                        Serializer<DataContainer> loaded; // loaded instance
                        // create serializer
                        Serializer<DataContainer> s = new Serializer<>(DataContainer.class);
                        s.registerInstanceCreator(DataContainer.class,
                                new DataContainerInstanceCreator(pluginFactory, viewUpdater));
                        loaded = s.load(od.getDirectory() + od.getFileName());
                        // check against image names
                        if (!loaded.obj.BOAState.boap.getOrgFile().getName()
                                .equals(qState.boap.getOrgFile().getName())) {
                            LOGGER.warn("The image opened currently in BOA is different from those"
                                    + " pointed in configuration file");
                            log("Trying to apply configuration saved for other image");
                            YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(),
                                    "Warning", "Trying to load configuration that does not\nmath to"
                                            + " opened image.\nAre you sure?");
                            if (!yncd.yesPressed())
                                return;
                        }
                        // closes windows, etc
                        qState.reset(WindowManager.getCurrentImage(), pluginFactory, viewUpdater);
                        qState = loaded.obj.BOAState;
                        imageGroup.updateNest(qState.nest); // reconnect nest to external class
                        qState.restore(qState.boap.frame); // copy from snapshots to current object
                        updateSpinnerValues(); // update segmentation gui
                        // do not recalculatePlugins here because pluginList is empty and this
                        // method will update finalSnake overriding it by segSnake (because on
                        // empty list they are just copied)
                        // updateToFrame calls updateSliceSelector only if there is action of
                        // changing frame. If loaded frame is the same as current one this event is
                        // not called.
                        if (qState.boap.frame != imageGroup.getOrgIpl().getSlice())
                            imageGroup.updateToFrame(qState.boap.frame); // move to frame
                        else
                            updateSliceSelector(); // repaint window explicitly
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
            }

            /**
             * Discard all plugins.
             * 
             * In general it does: reset current snakePluginList and snakePluginListSnapshots,
             * Copies segSnakes to finalSnakes
             */
            if (b == menuDeletePlugin) {
                // clear all plugins
                qState.snakePluginList.clear();
                for (SnakePluginList sp : qState.snakePluginListSnapshots)
                    if (sp != null)
                        sp.clear();
                // copy snakes to finals
                for (int i = 0; i < qState.nest.size(); i++) {
                    SnakeHandler sH = qState.nest.getHandler(i);
                    sH.copyFromSegToFinal();
                }
                // update window
                imageGroup.updateOverlay(qState.boap.frame);
            }

            /**
             * Reload and re-apply all plugins stored in snakePluginListSnapshot.
             * 
             * qState.snakePluginList.clear(); can not be called here because
             * uk.ac.warwick.wsbc.QuimP.BOAState.restore(int) makes reference to
             * snakePluginListSnapshot in snakePluginList. Thus, cleaning snakePluginList deletes
             * one entry in snakePluginListSnapshot
             */
            if (b == menuApplyPlugin) {
                // iterate over snapshots and try to restore plugins in snapshots
                for (SnakePluginList sp : qState.snakePluginListSnapshots) {
                    sp.afterSerialize();
                }
                qState.restore(qState.boap.frame); // copy snaphots for frame to current
                                                   // snakePluginList (and segParams)
                recalculatePlugins(); // update screen
            }

            /**
             * Run segmentation from mask file.
             */
            if (b == menuSegmentationRun) {
                if (qState.binarySegmentationPlugin != null) {
                    if (!qState.binarySegmentationPlugin.isWindowVisible())
                        qState.binarySegmentationPlugin.showUI(true);
                } else {
                    qState.binarySegmentationPlugin = new BinarySegmentationPlugin(); // create
                    // instance
                    qState.binarySegmentationPlugin.attachData(qState.nest); // attach data
                    qState.binarySegmentationPlugin.attachContext(viewUpdater); // allow plugin to
                                                                                // update
                                                                                // screen
                    qState.binarySegmentationPlugin.showUI(true); // plugin is run internally
                                                                  // after
                                                                  // Apply
                    // update screen is always on Apply button of plugin
                }
                BOA_.log("Run segmentation from mask file");
            }

            /**
             * Clean all bOA state.
             */
            if (b == menuSegmentationReset) {
                qState.reset(WindowManager.getCurrentImage(), pluginFactory, viewUpdater);
                qState.nest.cleanNest();
                updateSpinnerValues();
                if (qState.boap.frame != imageGroup.getOrgIpl().getSlice())
                    imageGroup.updateToFrame(qState.boap.frame); // move to frame
                else
                    updateSliceSelector(); // repaint window explicitly
            }

            updateWindowState(); // window logic on any change and selectors

            // run segmentation for selected cases
            if (run) {
                System.out.println("running from in stackwindow");
                // run on current frame
                try {
                    runBoa(qState.boap.frame, qState.boap.frame);
                } catch (BoaException be) {
                    BOA_.log(be.getMessage());
                }
                // imageGroup.setSlice(1);
            }
        }

        /**
         * Detect changes in checkboxes and run segmentation for current frame if necessary.
         * 
         * Transfer parameters from changed GUI element to
         * {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp} class
         * 
         * @param e Type of event
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateWindowState()
         */
        @Override
        public void itemStateChanged(final ItemEvent e) {
            LOGGER.debug("EVENT:itemStateChanged");
            if (qState.boap.doDelete) {
                BOA_.log("**WARNING:DELETE IS ON**");
            }
            boolean run = false; // set to true if any of items changes require
                                 // to re-run segmentation
            Object source = e.getItemSelectable();
            if (source == cPath) {
                qState.segParam.showPaths = cPath.getState();
                if (qState.segParam.showPaths) {
                    this.setImage(imageGroup.getPathsIpl());
                } else {
                    this.setImage(imageGroup.getOrgIpl());
                }
                if (qState.boap.zoom && !qState.nest.isVacant()) { // set zoom
                    imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
                }
            } else if (source == cPrevSnake) {
                qState.segParam.use_previous_snake = cPrevSnake.getState();
            } else if (source == cExpSnake) {
                qState.segParam.expandSnake = cExpSnake.getState();
                run = true;
            } else if (source == cFirstPluginActiv) {
                qState.snakePluginList.setActive(0, cFirstPluginActiv.getState());
                recalculatePlugins();
            } else if (source == cSecondPluginActiv) {
                qState.snakePluginList.setActive(1, cSecondPluginActiv.getState());
                recalculatePlugins();
            } else if (source == cThirdPluginActiv) {
                qState.snakePluginList.setActive(2, cThirdPluginActiv.getState());
                recalculatePlugins();
            }

            // action on menus
            if (source == cbMenuPlotOriginalSnakes) {
                qState.boap.isProcessedSnakePlotted = cbMenuPlotOriginalSnakes.getState();
                recalculatePlugins();
            }
            if (source == cbMenuPlotHead) {
                qState.boap.isHeadPlotted = cbMenuPlotHead.getState();
                imageGroup.updateOverlay(qState.boap.frame);
            }

            // actions on Plugin selections
            if (source == sFirstPluginName) {
                LOGGER.debug("Used firstPluginName, val: " + sFirstPluginName.getSelectedItem());
                instanceSnakePlugin((String) sFirstPluginName.getSelectedItem(), 0,
                        cFirstPluginActiv.getState());
                recalculatePlugins();
            }
            if (source == sSecondPluginName) {
                LOGGER.debug("Used secondPluginName, val: " + sSecondPluginName.getSelectedItem());
                instanceSnakePlugin((String) sSecondPluginName.getSelectedItem(), 1,
                        cSecondPluginActiv.getState());
                recalculatePlugins();
            }
            if (source == sThirdPluginName) {
                LOGGER.debug("Used thirdPluginName, val: " + sThirdPluginName.getSelectedItem());
                instanceSnakePlugin((String) sThirdPluginName.getSelectedItem(), 2,
                        cThirdPluginActiv.getState());
                recalculatePlugins();
            }

            // Action on zoom selector
            if (source == sZoom) {
                if (sZoom.getSelectedItem().equals(fullZoom)) { // user selected default position
                                                                // (no zoom)
                    qState.boap.snakeToZoom = -1; // set negative value to indicate no zoom
                    qState.boap.zoom = false; // important for other parts (legacy)
                    imageGroup.unzoom(canvas); // unzoom view
                } else // zoom here
                if (!qState.nest.isVacant()) { // any snakes present
                    qState.boap.snakeToZoom = Integer.parseInt(sZoom.getSelectedItem()); // get int
                    qState.boap.zoom = true; // legacy compatibility
                    imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
                }
            }

            updateWindowState(); // window logic on any change
            updateChoices(); // only for updating colors after calling

            try {
                if (run) {
                    if (qState.boap.supressStateChangeBOArun) {// when spinners are changed
                        // programmatically they raise the
                        // event. this is to block running
                        // segmentation
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
         * Detect changes in spinners and run segmentation for current frame if necessary.
         * 
         * Transfer parameters from changed GUI element to
         * {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp} class
         * 
         * @param ce Type of event
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateWindowState()
         */
        @Override
        public void stateChanged(final ChangeEvent ce) {
            LOGGER.debug("EVENT:stateChanged");
            if (qState.boap.doDelete) {
                BOA_.log("**WARNING:DELETE IS ON**");
            }
            boolean run = false; // set to true if any of items changes require to re-run
                                 // segmentation
            Object source = ce.getSource();

            if (source == dsNodeRes) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.setNodeRes((Double) spinner.getValue());
                run = true;
            } else if (source == dsVel_crit) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.vel_crit = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_image) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.f_image = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_central) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.f_central = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_contract) {
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
            } else if (source == isSample_tan) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.sample_tan = (Integer) spinner.getValue();
                run = true;
            } else if (source == isSample_norm) {
                JSpinner spinner = (JSpinner) source;
                qState.segParam.sample_norm = (Integer) spinner.getValue();
                run = true;
            }

            updateWindowState(); // window logic on any change

            try {
                if (run) {
                    if (qState.boap.supressStateChangeBOArun) { // when spinners are changed
                                                                // programmatically they raise the
                                                                // event. this is to block running
                                                                // segmentation
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
            if (!qState.boap.singleImage)
                zSelector.setValue(imp.getCurrentSlice()); // this is delayed in
            // super.updateSliceSelector force it now

            // if in edit, save current edit and start edit of next frame if exists
            boolean wasInEdit = qState.boap.editMode;
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
                SnakeHandler sH = qState.nest.getHandler(0);
                if (sH.isStoredAt(qState.boap.frame)) {
                    imageGroup.zoom(canvas, qState.boap.frame, qState.boap.snakeToZoom);
                }
            }

            if (wasInEdit) {
                bEdit.setLabel("*STOP EDIT*");
                BOA_.log("**EDIT IS ON**");
                qState.boap.editMode = true;
                lastTool = IJ.getToolName();
                IJ.setTool(Toolbar.LINE);
                editSeg(0, 0, qState.boap.frame);
                IJ.setTool(lastTool);
            }
            LOGGER.trace(
                    "Snakes at this frame: " + qState.nest.getSnakesforFrame(qState.boap.frame));
            if (!qState.boap.SEGrunning) { // do not update or restore state when we hit this
                                           // event from runBoa() method
                                           // (through setIpSliceAll(int))
                qState.restore(qState.boap.frame);
                updateSpinnerValues();
                updateWindowState();
            }
        }

        /**
         * Turn delete mode off by setting proper value in
         * {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp}.
         */
        void switchOffDelete() {
            qState.boap.doDelete = false;
            bDel.setLabel("Delete cell");
        }

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
         * {@link uk.ac.warwick.wsbc.QuimP.BOAState.BOAp}.
         */
        void switchOfftruncate() {
            qState.boap.doDeleteSeg = false;
            bDelSeg.setLabel("Truncate Seg");
        }

        void setScalesText() {
            pixelLabel.setText("Scale: " + IJ.d2s(qState.boap.getImageScale(), 6) + " \u00B5m");
            fpsLabel.setText(
                    "F Interval: " + IJ.d2s(qState.boap.getImageFrameInterval(), 3) + " s");
        }

    } // end of CustomStackWindow

    /**
     * Creates instance (through SnakePluginList) of plugin of given name on given UI slot.
     * 
     * Decides if plugin will be created or destroyed basing on plugin name from Choice list
     * 
     * @param selectedPlugin Name of plugin returned from UI elements
     * @param slot Slot of plugin
     * @param act Indicates if plugins is activated in GUI
     * @see uk.ac.warwick.wsbc.QuimP.SnakePluginList
     */
    private void instanceSnakePlugin(final String selectedPlugin, int slot, boolean act) {

        try {
            // get instance using plugin name (obtained from getPluginNames from PluginFactory
            if (selectedPlugin != NONE) { // do no pass NONE to pluginFact
                qState.snakePluginList.setInstance(slot, selectedPlugin, act); // build instance
            } else {
                if (qState.snakePluginList.getInstance(slot) != null)
                    qState.snakePluginList.getInstance(slot).showUI(false);
                qState.snakePluginList.deletePlugin(slot);
            }
        } catch (QuimpPluginException e) {
            LOGGER.warn(
                    "Plugin " + selectedPlugin + " cannot be loaded. Reason: " + e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
    }

    /**
     * Start segmentation process on range of frames.
     * 
     * This method is called for update only current view as well (<tt>startF</tt> == <tt>endF</tt>)
     * 
     * @param startF start frame
     * @param endF end frame
     * @throws BoaException TODO Rewrite exceptions here
     * @see <a href=
     *      "http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/65">http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/65</a>
     */
    public void runBoa(int startF, int endF) throws BoaException {
        System.out.println("run BOA");
        qState.boap.SEGrunning = true;
        if (qState.nest.isVacant()) {
            BOA_.log("Nothing to segment!");
            qState.boap.SEGrunning = false;
            return;
        }
        try {
            IJ.showProgress(0, endF - startF);

            // if(boap.expandSnake) boap.NMAX = 9990; // percent hack

            qState.nest.resetForFrame(startF);
            if (!qState.segParam.expandSnake) { // blowup snake ready for contraction (only those
                                                // not
                // starting
                // at or after the startF)
                constrictor.loosen(qState.nest, startF);
            } else {
                constrictor.implode(qState.nest, startF);
            }
            SnakeHandler sH;

            int s = 0;
            Snake snake;
            imageGroup.clearPaths(startF);

            for (qState.boap.frame = startF; qState.boap.frame <= endF; qState.boap.frame++) {
                // per frame
                imageGroup.setProcessor(qState.boap.frame);
                imageGroup.setIpSliceAll(qState.boap.frame);

                try {
                    if (qState.boap.frame != startF) {// expand snakes for next frame
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
                        sH = qState.nest.getHandler(s);
                        snake = sH.getLiveSnake();
                        try {
                            if (!snake.alive || qState.boap.frame < sH.getStartFrame()) {
                                continue;
                            }
                            imageGroup.drawPath(snake, qState.boap.frame); // pre tightned snake on
                            // path
                            tightenSnake(snake);
                            imageGroup.drawPath(snake, qState.boap.frame); // post tightned snake on
                            // path
                            sH.backupLiveSnake(qState.boap.frame);
                            Snake out = iterateOverSnakePlugins(snake);
                            sH.storeThisSnake(out, qState.boap.frame); // store resulting snake as
                            // final

                        } catch (QuimpPluginException qpe) {
                            // must be rewritten with whole runBOA #65 #67
                            BOA_.log("Error in filter module: " + qpe.getMessage());
                            LOGGER.debug(qpe.getMessage(), qpe);
                            sH.storeLiveSnake(qState.boap.frame); // store segmented nonmodified

                        } catch (BoaException be) {
                            imageGroup.drawPath(snake, qState.boap.frame); // failed
                            // position
                            // sH.deleteStoreAt(frame);
                            sH.storeLiveSnake(qState.boap.frame);
                            sH.backupLiveSnake(qState.boap.frame);
                            qState.nest.kill(sH);
                            snake.unfreezeAll();
                            BOA_.log("Snake " + snake.getSnakeID() + " died, frame "
                                    + qState.boap.frame);
                            qState.boap.SEGrunning = false;
                            if (qState.nest.allDead()) {
                                throw new BoaException("All snakes dead: " + be.getMessage(),
                                        qState.boap.frame, 1);
                            }
                        }

                    }
                    imageGroup.updateOverlay(qState.boap.frame); // redraw display
                    IJ.showProgress(qState.boap.frame, endF);
                } catch (BoaException be) {
                    qState.boap.SEGrunning = false;
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
            qState.boap.SEGrunning = false;
            LOGGER.debug(e.getMessage(), e);
            imageGroup.updateOverlay(qState.boap.frame); // update on error
            // do no add LOGGER here #278
            throw new BoaException("Frame " + qState.boap.frame + ": " + e.getMessage(),
                    qState.boap.frame, 1);
        }
        qState.boap.SEGrunning = false;
    }

    /**
     * Process Snake by all active plugins.
     * 
     * Processed Snake is returned as new Snake with the same ID. Input snake is not modified. For
     * empty plugin list it just return input snake
     *
     * This method supports two interfaces:
     * {@link uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOAPoint2dFilter},
     * {@link uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOASnakeFilter}
     * 
     * It uses smart method to detect which interface is used for every slot to avoid unnecessary
     * conversion between data. <tt>previousConversion</tt> keeps what interface was used on
     * previous slot in plugin stack. Then for every plugin data are converted if current plugin
     * differs from previous one. Converted data are kept in <tt>snakeToProcess</tt> and
     * <tt>dataToProcess</tt> but only one of these variables is valid in given time. Finally after
     * last plugin data are converted to Snake.
     * 
     * @param snake snake to be processed
     * @return Processed snake or original input one when there is no plugin selected
     * @throws QuimpPluginException on plugin error
     * @throws Exception
     */
    private Snake iterateOverSnakePlugins(final Snake snake)
            throws QuimpPluginException, Exception {
        final int ipoint = 0; // define IQuimpPoint2dFilter interface
        final int isnake = 1; // define IQuimpPoint2dFilter interface
        // type of previous plugin. Define if data should be converted for current plugin
        int previousConversion = isnake; // IQuimpSnakeFilter is default interface
        Snake outsnake = snake; // if there is no plugin just return input snake
        Snake snakeToProcess = snake; // data to be processed, input snake on beginning
        // data to process in format of list
        List<Point2d> dataToProcess = null; // null but it will be overwritten in loop because first
                                            // "if" fires always (previousConversion is set to
                                            // isnake) on beginning, if first plugin is ipoint type
        if (!qState.snakePluginList.isRefListEmpty()) {
            LOGGER.debug("sPluginList not empty");
            for (Plugin qP : qState.snakePluginList.getList()) { // iterate over list
                if (!qP.isExecutable())
                    continue; // no plugin on this slot or not active
                if (qP.getRef() instanceof IQuimpBOAPoint2dFilter) { // check interface type
                    if (previousConversion == isnake) { // previous was IQuimpSnakeFilter
                        dataToProcess = snakeToProcess.asList(); // and data needs to be converted
                    }
                    IQuimpBOAPoint2dFilter qPcast = (IQuimpBOAPoint2dFilter) qP.getRef();
                    qPcast.attachData(dataToProcess);
                    dataToProcess = qPcast.runPlugin(); // store result in input variable
                    previousConversion = ipoint;
                }
                if (qP.getRef() instanceof IQuimpBOASnakeFilter) { // check interface type
                    if (previousConversion == ipoint) { // previous was IQuimpPoint2dFilter
                        // and data must be converted to snake from dataToProcess
                        snakeToProcess =
                                new QuimpDataConverter(dataToProcess).getSnake(snake.getSnakeID());
                    }
                    IQuimpBOASnakeFilter qPcast = (IQuimpBOASnakeFilter) qP.getRef();
                    qPcast.attachData(snakeToProcess);
                    snakeToProcess = qPcast.runPlugin(); // store result as snake for next plugin
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
            }
        } else
            LOGGER.debug("sPluginList empty");
        return outsnake;

    }

    private void tightenSnake(final Snake snake) throws BoaException {

        int i;
        // imageGroup.drawPath(snake, frame); //draw initial contour on path
        // image

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

            if ((snake.getNumNodes() / snake.startingNnodes) > qState.boap.NMAX) {
                // if max nodes reached (as % starting) prompt for reset
                if (qState.segParam.use_previous_snake) {
                    // imageGroup.drawContour(snake, frame);
                    // imageGroup.updateAndDraw();
                    throw new BoaException("Frame " + qState.boap.frame + "-max nodes reached "
                            + snake.getNumNodes(), qState.boap.frame, 1);
                } else {
                    BOA_.log("Frame " + qState.boap.frame + "-max nodes reached..continue");
                    break;
                }
            }
        }
        snake.unfreezeAll(); // set freeze tag back to false

        if (!qState.segParam.expandSnake) { // shrink a bit to get final outline
            snake.shrinkSnake();
        }
        // System.out.println("finished tighten- cut loops and intersects");
        snake.cutLoops();
        snake.cutIntersects();
        // System.out.println("finished tighten with loop cuts");

        // snake.correctDistance();
        // imageGroup.drawPath(snake, frame); //draw final contour on path image
    }

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

    void updateImageScale() {
        imageGroup.getOrgIpl().getCalibration().frameInterval = qState.boap.getImageFrameInterval();
        imageGroup.getOrgIpl().getCalibration().pixelHeight = qState.boap.getImageScale();
        imageGroup.getOrgIpl().getCalibration().pixelWidth = qState.boap.getImageScale();
    }

    boolean loadSnakes() {

        YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Load associated snakes?",
                "\tLoad associated snakes?\n");
        if (!yncd.yesPressed()) {
            return false;
        }

        OutlineHandler oH = new OutlineHandler(qState.boap.readQp);
        if (!oH.readSuccess) {
            BOA_.log("Could not read in snakes");
            return false;
        }
        // convert to BOA snakes

        qState.nest.addOutlinehandler(oH);
        imageGroup.setProcessor(oH.getStartFrame());
        imageGroup.updateOverlay(oH.getStartFrame());
        BOA_.log("Successfully read snakes");
        return true;
    }

    /**
     * Add ROI to Nest.
     * 
     * This method is called on selection that should contain object to be segmented. Initialise
     * Snake object in Nest and it performs also initial segmentation of selected cell.
     * 
     * @param r ROI object (IJ)
     * @param f number of current frame
     * @see #tightenSnake(Snake)
     */
    // @SuppressWarnings("unchecked")
    void addCell(final Roi r, int f) {
        SnakeHandler sH = qState.nest.addHandler(r, f);
        Snake snake = sH.getLiveSnake();
        imageGroup.setProcessor(f);
        try {
            imageGroup.drawPath(snake, f); // pre tightned snake on path
            tightenSnake(snake);
            imageGroup.drawPath(snake, f); // post tightned snake on path
            sH.backupLiveSnake(f);
            Snake out = iterateOverSnakePlugins(snake); // process segmented snake by plugins
            sH.storeThisSnake(out, f); // store processed snake as final

            // if any problem with plugin or other, store snake without modification
            // because snake.asList() returns copy
        } catch (QuimpPluginException qpe) {
            sH.storeLiveSnake(f);
            BOA_.log("Error in filter module: " + qpe.getMessage());
            LOGGER.debug(qpe.getMessage(), qpe);
        } catch (BoaException be) {
            sH.deleteStoreAt(f);
            sH.kill();
            sH.backupLiveSnake(f);
            sH.storeLiveSnake(f);
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
     * Method searches the snake in Nest that is on current frame and its centroid is close enough
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

        SnakeHandler sH;
        Snake snake;
        ExtendedVector2d sV;
        ExtendedVector2d mV = new ExtendedVector2d(x, y);
        List<Double> distance = new ArrayList<Double>();

        for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
            sH = qState.nest.getHandler(i);
            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance.add(ExtendedVector2d.lengthP2P(mV, sV));
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

    void deleteSegmentation(int x, int y, int frame) {
        SnakeHandler sH;
        Snake snake;
        ExtendedVector2d sV;
        ExtendedVector2d mV = new ExtendedVector2d(x, y);
        List<Double> distance = new ArrayList<Double>();

        for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
            sH = qState.nest.getHandler(i);

            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance.add(ExtendedVector2d.lengthP2P(mV, sV));
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
            sH = qState.nest.getHandler(minIndex);
            sH.deleteStoreFrom(frame);
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
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateSliceSelector()
     */
    void editSeg(int x, int y, int frame) {
        SnakeHandler sH;
        Snake snake;
        ExtendedVector2d sV;
        ExtendedVector2d mV = new ExtendedVector2d(x, y);
        double[] distance = new double[qState.nest.size()];

        for (int i = 0; i < qState.nest.size(); i++) { // calc all distances
            sH = qState.nest.getHandler(i);
            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance[i] = ExtendedVector2d.lengthP2P(mV, sV);
            }
        }
        int minIndex = QuimPArrayUtils.minArrayIndex(distance);
        if (distance[minIndex] < 10 || qState.nest.size() == 1) { // if closest < 10, edit it
            sH = qState.nest.getHandler(minIndex);
            qState.boap.editingID = minIndex; // sH.getID();
            BOA_.log("Editing cell " + sH.getID());
            imageGroup.clearOverlay();

            Roi r;
            if (qState.boap.useSubPixel == true) {
                r = sH.getStoredSnake(frame).asPolyLine();
            } else {
                r = sH.getStoredSnake(frame).asIntRoi();
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
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow#updateSliceSelector()
     */
    void stopEdit() {
        Roi r = canvas.getImage().getRoi();
        Roi.setColor(Color.yellow);
        SnakeHandler sH = qState.nest.getHandler(qState.boap.editingID);
        sH.storeRoi((PolygonRoi) r, qState.boap.frame); // store as final snake
        // copy to segSnakes array
        Snake stored = sH.getStoredSnake(qState.boap.frame);
        sH.backupThisSnake(stored, qState.boap.frame);
        canvas.getImage().killRoi();
        imageGroup.updateOverlay(qState.boap.frame);
        qState.boap.editingID = -1;
    }

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
        if (qState.boap.saveSnake) {
            try {
                String saveIn = BOA_.qState.boap.getOutputFileCore().getParent();
                SaveDialog sd = new SaveDialog("Save segmentation data...", saveIn,
                        BOA_.qState.boap.getFileName(), "");
                if (sd.getFileName() == null) {
                    BOA_.log("Save canceled");
                    return;
                }
                // This initialize various filenames that can be accessed bo other modules
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
                    if (!ync.yesPressed())
                        return;
                }
                // write operations
                // blocked by #263
                // if (qState.nest.writeSnakes()) { // write snPQ file (if any snake) and paQP
                {
                    // write stQP file and fill outFile used later
                    List<CellStatsEval> ret =
                            qState.nest.analyse(imageGroup.getOrgIpl().duplicate());
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
                        if (qState.boap.savePretty) // set pretty format if configured
                            n.setPretty();
                        n.save(qState.boap.deductNewParamFileName());
                        n = null;
                    }
                    // } else {
                    // ync = new YesNoCancelDialog(window, "Save Segmentation",
                    // "Quit without saving?");
                    // if (!ync.yesPressed()) {
                    // return;
                    // }
                }
            } catch (IOException e) {
                IJ.error("Exception while saving");
                LOGGER.error("Exception while saving: " + e.getMessage());
                LOGGER.debug(e.getMessage(), e);
                return;
            }
        }
        BOA_.running = false;
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

        BOA_.running = false;
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

    private ImagePlus orgIpl, pathsIpl;// , contourIpl;
    private ImageStack orgStack, pathsStack; // , contourStack;
    private ImageProcessor orgIp, pathsIp; // , contourIp;
    private Overlay overlay;
    private Nest nest;
    int w, h, f;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageGroup.class.getName());

    /**
     * Constructor.
     * 
     * @param oIpl current image opened in IJ
     * @param n Nest object associated with BOA
     */
    public ImageGroup(ImagePlus oIpl, Nest n) {
        nest = n;
        // create two new stacks for drawing

        // image set up
        orgIpl = oIpl;
        orgIpl.setSlice(1);
        orgIpl.getCanvas().unzoom();
        orgIpl.getCanvas().getMagnification();

        orgStack = orgIpl.getStack();
        orgIp = orgStack.getProcessor(1);

        w = orgIp.getWidth();
        h = orgIp.getHeight();
        f = orgIpl.getStackSize();

        // set up blank path image
        pathsIpl = NewImage.createByteImage("Node Paths", w, h, f, NewImage.FILL_BLACK);
        pathsStack = pathsIpl.getStack();
        pathsIpl.setSlice(1);
        pathsIp = pathsStack.getProcessor(1);

        setIpSliceAll(1);
        setProcessor(1);
    }

    /**
     * Sets new Nest object associated with displayed image.
     * 
     * Used after loading new BOAState
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
     * Depending on configuration this method can plot:
     * <ol>
     * <li>Snake after segmentation, without processing by plugins
     * <li>Snake after segmentation and after processing by all active plugins
     * </ol>
     * It assign also last created Snake to ViewUpdater. This Snake can be accessed by plugin for
     * previewing purposes. If last Snake has been deleted, null is assigned or before last Snake
     * <p>
     * Used when there is a need of redrawing screen because of new data
     * 
     * @param frame Current frame
     */
    public void updateOverlay(int frame) {
        LOGGER.trace("Update overlay for frame " + frame);
        SnakeHandler sH;
        Snake snake, back;
        int x, y;
        TextRoi text;
        Roi r;
        overlay = new Overlay();
        BOA_.viewUpdater.connectSnakeObject(null); //
        for (int i = 0; i < nest.size(); i++) {
            sH = nest.getHandler(i);
            if (sH.isStoredAt(frame)) { // is there a snake a;t f?

                // plot segmented snake
                if (BOA_.qState.boap.isProcessedSnakePlotted == true) {
                    back = sH.getBackupSnake(frame); // original unmodified snake
                    // Roi r = snake.asRoi();
                    r = back.asFloatRoi();
                    r.setStrokeColor(Color.RED);
                    overlay.add(r);
                }
                // remember instance of segmented snake for plugins (last created)
                BOA_.viewUpdater.connectSnakeObject(sH.getBackupSnake(frame));
                // plot segmented and filtered snake
                snake = sH.getStoredSnake(frame); // processed by plugins
                // Roi r = snake.asRoi();
                r = snake.asFloatRoi();
                r.setStrokeColor(Color.YELLOW);
                overlay.add(r);
                x = (int) Math.round(snake.getCentroid().getX()) - 15;
                y = (int) Math.round(snake.getCentroid().getY()) - 15;
                text = new TextRoi(x, y, "   " + snake.getSnakeID());
                overlay.add(text);

                // draw centre point
                PointRoi pR = new PointRoi((int) snake.getCentroid().getX(),
                        (int) snake.getCentroid().getY());
                overlay.add(pR);

                // draw head node
                if (BOA_.qState.boap.isHeadPlotted == true) {
                    // base point = 0 node
                    Point2d bp = new Point2d(snake.getHead().getX(), snake.getHead().getY());

                    // Plot Arrow mounted in 0 node and pointing direction of Snake
                    Vector2d dir = new Vector2d(
                            snake.getHead().getNext().getNext().getNext().getX() - bp.getX(),
                            snake.getHead().getNext().getNext().getNext().getY() - bp.getY());
                    FloatPolygon fp = GraphicsElements.plotArrow(dir, bp, 12.0f, 0.3f);
                    PolygonRoi oR = new PolygonRoi(fp, Roi.POLYGON);
                    oR.setStrokeColor(Color.MAGENTA);
                    oR.setFillColor(Color.MAGENTA);
                    overlay.add(oR);

                    // plot circle on head
                    FloatPolygon fp1 = GraphicsElements.getCircle(bp, 10);
                    PolygonRoi oR1 = new PolygonRoi(fp1, Roi.POLYGON);
                    oR1.setStrokeColor(Color.GREEN);
                    oR1.setFillColor(Color.GREEN);
                    overlay.add(oR1);
                }
                // dump String to log
                LOGGER.trace(snake.toString());
            } else
                BOA_.viewUpdater.connectSnakeObject(null);
        }
        orgIpl.setOverlay(overlay);
    }

    /**
     * Updates IJ to current frame. Causes that updateSliceSelector() is called.
     * 
     * USed when there is a need to move to other frame programmatically.
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

    final public void setProcessor(int i) {
        orgIp = orgStack.getProcessor(i);
        pathsIp = pathsStack.getProcessor(i);
        // System.out.println("\n1217 Proc set to : " + i);
    }

    /**
     * Calls updateSliceSelector callback only if i != current frame.
     * 
     * @param i
     */
    final public void setIpSliceAll(int i) {
        // set slice on all images
        pathsIpl.setSlice(i);
        orgIpl.setSlice(i);
    }

    public void clearPaths(int fromFrame) {
        for (int i = fromFrame; i <= BOA_.qState.boap.getFRAMES(); i++) {
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
        int x, y;
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

            if (BOA_.qState.boap.getHEIGHT() > 800) {
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
        ImagePlus contourIpl = NewImage.createByteImage("Contours", w, h, f, NewImage.FILL_BLACK);
        ImageStack contourStack = contourIpl.getStack();
        contourIpl.setSlice(1);
        ImageProcessor contourIp;

        for (int i = 1; i <= BOA_.qState.boap.getFRAMES(); i++) { // copy original
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
     * If snake is not found nothing happens.
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
        SnakeHandler sH;
        Snake snake;

        try {
            sH = nest.getHandlerofId(snakeID);// snakeID, not index
            if (sH != null && sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
            } else {
                return;
            }
        } catch (IndexOutOfBoundsException e) {
            LOGGER.debug(e.getMessage(), e);
            return;
        }

        Rectangle r = snake.getBounds();
        int border = 40;

        // add border (10 either way)
        r.setBounds(r.x - border, r.y - border, r.width + border * 2, r.height + border * 2);

        // correct r's aspect ratio
        double icAspect = (double) ic.getWidth() / (double) ic.getHeight();
        double rAspect = r.getWidth() / r.getHeight();
        int newDim; // new dimenesion size

        if (icAspect < rAspect) {
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
     * This is ImageJ image with flatten Snake contours.
     * 
     * @param stack Stack to plot in
     */
    void drawCellRois(final ImageStack stack) {
        Snake snake;
        SnakeHandler sH;
        ImageProcessor ip;

        int x, y;
        for (int s = 0; s < nest.size(); s++) {
            sH = nest.getHandler(s);
            for (int i = 1; i <= BOA_.qState.boap.getFRAMES(); i++) {
                if (sH.isStoredAt(i)) {
                    snake = sH.getStoredSnake(i);
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

/**
 * Calculate forces that affect the snake.
 * 
 * @author rtyson
 */
class Constrictor {
    public Constrictor() {
    }

    public boolean constrict(final Snake snake, final ImageProcessor ip) {

        ExtendedVector2d F_temp; // temp vectors for forces
        ExtendedVector2d V_temp = new ExtendedVector2d();

        Node n = snake.getHead();
        do { // compute F_total

            if (!n.isFrozen()) {

                // compute F_central
                V_temp.setX(n.getNormal().getX() * BOA_.qState.segParam.f_central);
                V_temp.setY(n.getNormal().getY() * BOA_.qState.segParam.f_central);
                n.setF_total(V_temp);

                // compute F_contract
                F_temp = contractionForce(n);
                V_temp.setX(F_temp.getX() * BOA_.qState.segParam.f_contract);
                V_temp.setY(F_temp.getY() * BOA_.qState.segParam.f_contract);
                n.addF_total(V_temp);

                // compute F_image and F_friction
                F_temp = imageForce(n, ip);
                V_temp.setX(F_temp.getX() * BOA_.qState.segParam.f_image);// - n.getVel().getX() *
                // boap.f_friction);
                V_temp.setY(F_temp.getY() * BOA_.qState.segParam.f_image);// - n.getVel().getY() *
                // boap.f_friction);
                n.addF_total(V_temp);

                // compute new velocities of the node
                V_temp.setX(BOA_.qState.boap.delta_t * n.getF_total().getX());
                V_temp.setY(BOA_.qState.boap.delta_t * n.getF_total().getY());
                n.addVel(V_temp);

                // store the prelimanary point to move the node to
                V_temp.setX(BOA_.qState.boap.delta_t * n.getVel().getX());
                V_temp.setY(BOA_.qState.boap.delta_t * n.getVel().getY());
                n.setPrelim(V_temp);

                // add some friction
                n.getVel().multiply(BOA_.qState.boap.f_friction);

                // freeze node if vel is below vel_crit
                if (n.getVel().length() < BOA_.qState.segParam.vel_crit) {
                    snake.freezeNode(n);
                }
            }
            n = n.getNext(); // move to next node
        } while (!n.isHead()); // continue while have not reached tail

        // update all nodes to new positions
        n = snake.getHead();
        do {
            n.update(); // use preliminary variables
            n = n.getNext();
        } while (!n.isHead());

        snake.updateNormales(BOA_.qState.segParam.expandSnake);

        return snake.isFrozen(); // true if all nodes frozen
    }

    /**
     * @param snake
     * @param ip
     * @return true on success
     * @deprecated Strictly related to absolute paths on disk. Probably for testing purposes only.
     */
    public boolean constrictWrite(final Snake snake, final ImageProcessor ip) {
        // for writing forces at each frame
        try {
            PrintWriter pw = new PrintWriter(
                    new FileWriter("/Users/rtyson/Documents/phd/tmp/test/forcesWrite/forces.txt"),
                    true); // auto flush
            ExtendedVector2d F_temp; // temp vectors for forces
            ExtendedVector2d V_temp = new ExtendedVector2d();

            Node n = snake.getHead();
            do { // compute F_total

                // if (!n.isFrozen()) {

                // compute F_central
                V_temp.setX(n.getNormal().getX() * BOA_.qState.segParam.f_central);
                V_temp.setY(n.getNormal().getY() * BOA_.qState.segParam.f_central);
                pw.print("\n" + n.getTrackNum() + "," + V_temp.length() + ",");
                n.setF_total(V_temp);

                // compute F_contract
                F_temp = contractionForce(n);
                if (n.getCurvatureLocal() > 0) {
                    pw.print(F_temp.length() + ",");
                } else {
                    pw.print((F_temp.length() * -1) + ",");
                }
                V_temp.setX(F_temp.getX() * BOA_.qState.segParam.f_contract);
                V_temp.setY(F_temp.getY() * BOA_.qState.segParam.f_contract);
                n.addF_total(V_temp);

                // compute F_image and F_friction
                F_temp = imageForce(n, ip);
                pw.print((F_temp.length() * -1) + ",");
                V_temp.setX(F_temp.getX() * BOA_.qState.segParam.f_image);// - n.getVel().getX()*
                // boap.f_friction);
                V_temp.setY(F_temp.getY() * BOA_.qState.segParam.f_image);// - n.getVel().getY()*
                // boap.f_friction);
                n.addF_total(V_temp);
                pw.print(n.getF_total().length() + "");

                // compute new velocities of the node
                V_temp.setX(BOA_.qState.boap.delta_t * n.getF_total().getX());
                V_temp.setY(BOA_.qState.boap.delta_t * n.getF_total().getY());
                n.addVel(V_temp);

                // add some friction
                n.getVel().multiply(BOA_.qState.boap.f_friction);

                // store the prelimanary point to move the node to
                V_temp.setX(BOA_.qState.boap.delta_t * n.getVel().getX());
                V_temp.setY(BOA_.qState.boap.delta_t * n.getVel().getY());
                n.setPrelim(V_temp);

                // freeze node if vel is below vel_crit
                if (n.getVel().length() < BOA_.qState.segParam.vel_crit) {
                    snake.freezeNode(n);
                }
                // }
                n = n.getNext(); // move to next node
            } while (!n.isHead()); // continue while have not reached tail

            // update all nodes to new positions
            n = snake.getHead();
            do {
                n.update(); // use preliminary variables
                n = n.getNext();
            } while (!n.isHead());

            snake.updateNormales(BOA_.qState.segParam.expandSnake);

            pw.close();
            return snake.isFrozen(); // true if all nodes frozen
        } catch (Exception e) {
            return false;
        }
    }

    public ExtendedVector2d contractionForce(final Node n) {

        ExtendedVector2d R_result;
        ExtendedVector2d L_result;
        ExtendedVector2d force = new ExtendedVector2d();

        // compute the unit vector pointing to the left neighbor (static
        // method)
        L_result = ExtendedVector2d.unitVector(n.getPoint(), n.getPrev().getPoint());

        // compute the unit vector pointing to the right neighbor
        R_result = ExtendedVector2d.unitVector(n.getPoint(), n.getNext().getPoint());

        force.setX((R_result.getX() + L_result.getX()) * 0.5); // combine vector to left and right
        force.setY((R_result.getY() + L_result.getY()) * 0.5);

        return (force);
    }

    /**
     * @param n
     * @param ip
     * @return image force
     * @deprecated Probably old version of contractionForce(Node n).
     */
    public ExtendedVector2d imageForceOLD(final Node n, final ImageProcessor ip) {
        ExtendedVector2d result = new ExtendedVector2d();
        ExtendedVector2d tan; // Tangent
        int i, j;

        double a = 0.75; // subsampling factor
        double Delta_I; // intensity contrast
        double x, y; // co-ordinates of the local neighborhood
        double xt, yt; // co-ordinates of the tangent
        int I_inside = 0, I_outside = 0; // Intensity of the local
        // Neighborhood of a node (inside/outside of the chain)
        int I_in = 0, I_out = 0; // number of pixels in the local
        // Neighborhood of a node

        // compute normalized tangent (unit vector between neighbors) via
        // static method
        tan = n.getTangent();

        // determine local neighborhood: a rectangle with sample_tan x
        // sample_norm
        // tangent to the chain

        for (i = 0; i <= 1. / a * BOA_.qState.segParam.sample_tan; ++i) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.qState.segParam.sample_norm / 2; ++j) {
                x = xt + a * j * n.getNormal().getX();
                y = yt + a * j * n.getNormal().getY();

                I_inside += ip.getPixel((int) x, (int) y);
                ++I_in;

                x = xt - a * j * n.getNormal().getX();
                y = yt - a * j * n.getNormal().getY();

                // check that pixel is inside frame
                if (x > 0 && y > 0 && x <= BOA_.qState.boap.getWIDTH()
                        && y <= BOA_.qState.boap.getHEIGHT()) {
                    I_outside += ip.getPixel((int) x, (int) y);
                    ++I_out;
                }
            }
        }

        // if (I_out > boap.sample_norm / 2 * boap.sample_tan) //check that all
        // I_out pixels are inside the frame
        // {
        Delta_I = ((double) I_inside / I_in - (double) I_outside / I_out) / 255.;
        // }

        I_inside = 0;
        I_outside = 0; // Intensity of the local
        // neighbourhood of a node (insde/outside of the chain)
        I_in = 0;
        I_out = 0; // number of pixels in the local

        // rotate sample window and take the maximum contrast
        for (i = 0; i <= 1. / a * BOA_.qState.segParam.sample_norm; ++i) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.qState.segParam.sample_tan / 2; ++j) {
                x = xt + a * j * n.getNormal().getX();
                y = yt + a * j * n.getNormal().getY();

                I_inside += ip.getPixel((int) x, (int) y);
                ++I_in;

                x = xt - a * j * n.getNormal().getX();
                y = yt - a * j * n.getNormal().getY();
                // check that pixel is inside frame
                if (x > 0 && y > 0 && x <= BOA_.qState.boap.getWIDTH()
                        && y <= BOA_.qState.boap.getHEIGHT()) {
                    I_outside += ip.getPixel((int) x, (int) y);
                    ++I_out;
                }
            }
        }

        double Delta_I_r = ((double) I_inside / I_in - (double) I_outside / I_out) / 255.;
        System.out.println("Delta_I=" + Delta_I + ", Delta_I_r =" + Delta_I_r);

        if (I_out > BOA_.qState.segParam.sample_norm / 2 * BOA_.qState.segParam.sample_tan) // check
        // that all
        // I_out pixels are
        // inside the frame
        {
            Delta_I = Math.max(Delta_I,
                    ((double) I_inside / I_in - (double) I_outside / I_out) / 255.);
        }

        // !!!!!!!!!!!
        // check if node erraneously got inside (pixel value > p.sensitivity)
        // if so, push node outside
        double check = (double) I_outside / I_out / 255.;

        if (check > BOA_.qState.boap.sensitivity) // Delta_I += 0.5 * check;
        {
            Delta_I += 0.125 * check;
        }
        // if contrast is positive compute image force as square root of the
        // contrast
        if (Delta_I > 0.) { // else remains at zero
            result.setX(-Math.sqrt(Delta_I) * n.getNormal().getX());
            result.setY(-Math.sqrt(Delta_I) * n.getNormal().getY());
        }

        return (result);
    }

    public ExtendedVector2d imageForce(final Node n, final ImageProcessor ip) {
        ExtendedVector2d result = new ExtendedVector2d();
        ExtendedVector2d tan = n.getTangent(); // Tangent at node
        int i, j; // loop vars

        double a = 0.75; // subsampling factor
        double Delta_I; // intensity contrast
        double x, y; // co-ordinates of the norm
        double xt, yt; // co-ordinates of the tangent
        int I_inside = 0;
        int I_outside = 0; // Intensity of neighbourhood of a node
                           // (insde/outside of the chain)
        int I_in = 0, I_out = 0; // number of pixels in the neighbourhood of a
                                 // node

        // determine num pixels and total intensity of
        // neighbourhood: a rectangle with sample_tan x sample_norm
        for (i = 0; i <= 1. / a * BOA_.qState.segParam.sample_tan; i++) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.qState.segParam.sample_norm / 2; ++j) {
                x = xt + a * j * n.getNormal().getX();
                y = yt + a * j * n.getNormal().getY();

                I_inside += ip.getPixel((int) x, (int) y);
                I_in++;

                x = xt - a * j * n.getNormal().getX();
                y = yt - a * j * n.getNormal().getY();

                I_outside += ip.getPixel((int) x, (int) y);
                I_out++;

                // if (x <= 0 || y <= 0 || x > width || y > height){
                // System.out.println("outer pixel I = " + ip.getPixel((int) x,
                // (int) y));
                // }

            }
        }

        Delta_I = ((double) I_inside / I_in - (double) I_outside / I_out) / 255.;

        // check if node erraneously got inside (pixel value > p.sensitivity)
        // if so, push node outside
        // double check = (double) I_outside / I_out / 255.;

        // if (check > boap.sensitivity) // Delta_I += 0.5 * check;
        // {
        // Delta_I += 0.125 * check;
        // }
        // if contrast is positive compute image force as square root of the
        // contrast
        if (Delta_I > 0.) { // else remains at zero
            result.setX(-Math.sqrt(Delta_I) * n.getNormal().getX());
            result.setY(-Math.sqrt(Delta_I) * n.getNormal().getY());
        }

        return (result);
    }

    /**
     * Expand all snakes while preventing overlaps.
     * 
     * Dead snakes are ignored. Count snakes on frame.
     * 
     * @param nest
     * @param frame
     * @throws Exception
     */
    public void loosen(final Nest nest, int frame) throws Exception {
        int N = nest.size();
        Snake snakeA, snakeB;

        double[][] prox = new double[N][N]; // dist beween snake centroids,
                                            // triangular
        for (int si = 0; si < N; si++) {
            snakeA = nest.getHandler(si).getLiveSnake();
            snakeA.calcCentroid();
            for (int sj = si + 1; sj < N; sj++) {
                snakeB = nest.getHandler(sj).getLiveSnake();
                snakeB.calcCentroid();
                prox[si][sj] =
                        ExtendedVector2d.lengthP2P(snakeA.getCentroid(), snakeB.getCentroid());
            }
        }

        double stepSize = 0.1;
        double steps = (double) BOA_.qState.segParam.blowup / stepSize;

        for (int i = 0; i < steps; i++) {
            // check for contacts, freeze nodes in contact.
            // Ignore snakes that begin after 'frame'
            for (int si = 0; si < N; si++) {
                snakeA = nest.getHandler(si).getLiveSnake();
                if (!snakeA.alive || frame < nest.getHandler(si).getStartFrame()) {
                    continue;
                }
                for (int sj = si + 1; sj < N; sj++) {
                    snakeB = nest.getHandler(sj).getLiveSnake();
                    if (!snakeB.alive || frame < nest.getHandler(si).getStartFrame()) {
                        continue;
                    }
                    if (prox[si][sj] > BOA_.qState.boap.proximity) {
                        continue;
                    }
                    freezeProx(snakeA, snakeB);
                }

            }

            // scale up all snakes by one step (if node not frozen, or dead)
            // unless they start at this frame or after
            for (int s = 0; s < N; s++) {
                snakeA = nest.getHandler(s).getLiveSnake();
                if (snakeA.alive && frame > nest.getHandler(s).getStartFrame()) {
                    snakeA.scale(stepSize, stepSize, true);
                }
            }

        }

        /*
         * //defreeze snakes for (int s = 0; s < snakes.length; s++) { snakes[s].defreeze(); }
         */
    }

    public void freezeProx(final Snake a, final Snake b) {

        Node bn;
        Node an = a.getHead();
        double prox;

        do {
            bn = b.getHead();
            do {
                if (an.isFrozen() && bn.isFrozen()) {
                    // an = an.getNext();
                    bn = bn.getNext();
                    continue;
                }
                // test proximity and freeze
                prox = ExtendedVector2d.distPointToSegment(an.getPoint(), bn.getPoint(),
                        bn.getNext().getPoint());
                if (prox < BOA_.qState.boap.proxFreeze) {
                    an.freeze();
                    bn.freeze();
                    bn.getNext().freeze();
                    break;
                }
                bn = bn.getNext();
            } while (!bn.isHead());

            an = an.getNext();
        } while (!an.isHead());

    }

    public void implode(final Nest nest, int f) throws Exception {
        // System.out.println("imploding snake");
        SnakeHandler sH;
        Snake snake;
        for (int s = 0; s < nest.size(); s++) {
            sH = nest.getHandler(s);
            snake = sH.getLiveSnake();
            if (snake.alive && f > sH.getStartFrame()) {
                snake.implode();
            }
        }
    }
}

/**
 * Extended exception class.
 * 
 * Contains additional information on frame and type
 * 
 * @author rtyson
 *
 */
class BoaException extends QuimpException {

    private static final long serialVersionUID = 1L;
    private int frame;
    private int type;

    public BoaException(String msg, int f, int t) {
        super(msg);
        frame = f;
        type = t;
    }

    public BoaException(String string) {
        super(string);
    }

    public int getFrame() {
        return frame;
    }

    public int getType() {
        return type;
    }

    /**
     * 
     */
    public BoaException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public BoaException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public BoaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public BoaException(Throwable cause) {
        super(cause);
    }

}

/**
 * Object builder for GSon and DataContainer class.
 * 
 * This class is used on load JSon representation of DataContainer class. Rebuilds snakePluginList
 * field that is not serialized. This field keeps current state of plugins.
 * 
 * @author p.baniukiewicz
 * @see Gson
 */
class DataContainerInstanceCreator implements InstanceCreator<DataContainer> {

    private PluginFactory pf;
    private ViewUpdater vu;

    public DataContainerInstanceCreator(final PluginFactory pf, final ViewUpdater vu) {
        this.pf = pf;
        this.vu = vu;
    }

    @Override
    public DataContainer createInstance(Type arg0) {
        DataContainer dt = new DataContainer(pf, vu);
        return dt;
    }
}
