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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.Blitter;
import ij.process.FloatPolygon;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import uk.ac.warwick.wsbc.QuimP.BOA_.BOAState;
import uk.ac.warwick.wsbc.QuimP.BOAp.SegParam;
import uk.ac.warwick.wsbc.QuimP.SnakePluginList.Plugin;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpSnakeFilter;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter;

/**
 * Main class implementing BOA plugin.
 * 
 * @author Richard Tyson
 * @author Till Bretschneider
 * @author Piotr Baniukiewicz
 * @date 16-04-09
 * @date 4 Feb 2016
 */
public class BOA_ implements PlugIn {
    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(BOA_.class.getName());
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
    private final static String NONE = "NONE";
    /**
     * Reserved word that states full view zoom in zoom choice. Also default text that
     * appears there
     */
    private final static String fullZoom = "Frame zoom";
    /**
     * Hold current BOA object and provide access to only selected methods from plugin. Reference to
     * this field is passed to plugins and give them possibility to call selected methods from BOA
     * class
     */
    public static ViewUpdater viewUpdater;
    /**
     * Keep data from getQuimPBuildInfo() These information are used in About dialog, window title
     * bar, logging, etc.
     */
    public static String[] quimpInfo;
    private static int logCount; // add counter to logged messages
    static final private int NUM_SNAKE_PLUGINS = 3; /*!< number of Snake plugins  */
    private HistoryLogger historyLogger; // logger
    /**
     * Configuration object, available from all modules. Must be initialized here \b AND in 
     * constructor (to reset settings on next BOA call without quitting Fiij)
     */
    static public BOAp boap = new BOAp();
    private BOAState boaState; // current state of BOA module

    /**
     * Hold current BOA state that can be serialized
     * 
     * @author p.baniukiewicz
     * @date 30 Mar 2016
     * @see Serializer
     * @remarks Currently the SegParam object is connected to this class only (in
     * run(final String)) and it is created in BOAp class constructor. In future SegParam will
     * be part of this class. This class in supposed to be main configuration holder for BOA_
     */
    class BOAState implements IQuimpSerialize {
        /**
         * Current frame, CustomStackWindow.updateSliceSelector()
         * Not stored due to archiving all parameters for every frame separately
         */
        public transient int frame;
        /**
         * Snake selected in zoom selector, negative value if 100% view
         */
        public transient int snakeToZoom = -1;
        /**
         * Reference to segmentation parameters. Holds current parameters (as reference to
         * boap.segParam)
         * 
         * On every change of BOA state it is stored as copy in segParamSnapshots for current
         * frame. This is why that field is \c transient
         * 
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
         * @todo TODO This should exist in BOA or BOAState space not in BOAp
         * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/ConfigurationHandling
         */
        public transient SegParam segParam;
        public String fileName; //!< Current data file name
        /**
         * Keep snapshots of SegParam objects for every frame separately
         */
        private ArrayList<SegParam> segParamSnapshots;
        /**
         * Keep snapshots of SnakePluginList objects for every frame separately. Plugin
         * configurations are stored as well (but without plugin references)
         */
        private ArrayList<SnakePluginList> snakePluginListSnapshots;
        /**
         * List of plugins selected in plugin stack and information if they are active or not
         * This field is serializable.
         * 
         * Holds current parameters as the main object not referenced in BOAp
         * On every change of BOA state it is stored as copy in snakePluginListSnapshots for current
         * frame. This is why that field is \c transient
         * 
         * @see SnakePluginList
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
         */
        public transient SnakePluginList snakePluginList;
        /**
         * Reference to Nest, which is serializable as well
         * 
         * This is main object not referenced in other parts of QuimP
         */
        public Nest nest;
        /**
         * Store information whether for current frame button \b Edit was used. 
         * 
         * Do not indicate that any of Snakes was edited.
         */
        public ArrayList<Boolean> isFrameEdited;

        /**
         * Construct BOAState object for given stack size
         * 
         * @param numofframes number of frames in loaded stack
         */
        public BOAState(int numofframes) {
            segParamSnapshots = new ArrayList<SegParam>(Collections.nCopies(numofframes, null));
            snakePluginListSnapshots =
                    new ArrayList<SnakePluginList>(Collections.nCopies(numofframes, null));
            isFrameEdited = new ArrayList<Boolean>(Collections.nCopies(numofframes, false));
            LOGGER.debug("Initialize storage of size: " + numofframes + " size of segParams: "
                    + segParamSnapshots.size());
        }

        /**
         * Make snapshot of current objects state
         * 
         * @param frame actual frame numbered from 1
         */
        public void store(int frame) {
            LOGGER.debug("Data stored at frame:" + frame + " size of segParams is "
                    + segParamSnapshots.size());
            segParamSnapshots.set(frame - 1, boap.new SegParam(segParam));
            snakePluginListSnapshots.set(frame - 1, snakePluginList.getShallowCopy()); // download
                                                                                       // Plugin
                                                                                       // config
                                                                                       // as well
        }

        /**
         * Store information whether frame was edited only
         * 
         * Can be called when global state does not change, e.g. user clicked \b Edit button so
         * parameters and plugins have not been modified
         * 
         * @param frame current frame numbered from 1
         */
        public void storeOnlyEdited(int frame) {
            isFrameEdited.set(frame - 1, true);
        }

        /**
         * Should be called before serialization. Fills extra fields from BOAp
         */
        @Override
        public void beforeSerialize() {
            fileName = boap.fileName; // copy filename from system wide boap
            snakePluginList.beforeSerialize(); // download plugins configurations
            nest.beforeSerialize(); // prepare snakes
            // snakePluginListSnapshots and segParamSnapshots do not need beforeSerialize()
        }

        @Override
        public void afterSerialize() throws Exception {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Main constructor.
     * 
     * All static resources should be re-initialized here, otherwise they persist in memory between 
     * subsequent BOA calls from Fiji.
     */
    public BOA_() {
        LOGGER.trace("Constructor called");
        boap = new BOAp(); // set default parameters for static boap
        logCount = 1; // reset log count (it is also static)
    }

    /**
     * Main method called from Fiji. Initializes internal BOA structures.
     * 
     * @param arg Currently it can be string pointing to plugins directory
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.setup(final ImagePlus)
     */
    @Override
    public void run(final String arg) {
        if (IJ.versionLessThan("1.45")) {
            return;
        }

        if (IJ.getVersion().compareTo("1.46") < 0) {
            boap.useSubPixel = false;
        } else {
            boap.useSubPixel = true;
        }
        if (BOA_.running) {
            BOA_.running = false;
            IJ.error("Warning: Only have one instance of BOA running at a time");
            return;
        }
        // assign current object to ViewUpdater
        viewUpdater = new ViewUpdater(this);
        // collect information about quimp version read from jar
        quimpInfo = new Tool().getQuimPBuildInfo();
        // create history logger
        historyLogger = new HistoryLogger();

        ImagePlus ip = WindowManager.getCurrentImage();
        lastTool = IJ.getToolName();
        // stack or single image?
        if (ip == null) {
            IJ.error("Image required");
            return;
        } else if (ip.getStackSize() == 1) {
            boap.singleImage = true;
        } else {
            boap.singleImage = false;
        }
        // check if 8-bit image
        if (ip.getType() != ImagePlus.GRAY8) {
            YesNoCancelDialog ync = new YesNoCancelDialog(window, "Image bit depth",
                    "8-bit Image required. Convert?");
            if (ync.yesPressed()) {
                if (boap.singleImage) {
                    new ImageConverter(ip).convertToGray8();
                } else {
                    new StackConverter(ip).convertToGray8();
                }
            } else {
                return;
            }
        }

        boaState = new BOAState(ip.getStackSize()); // create BOA state machine
        boaState.segParam = boap.segParam; // assign reference of segmentation parameters to state
                                           // machine
        // Build plugin engine
        try {
            String path = IJ.getDirectory("plugins");
            if (path == null) {
                IJ.log("BOA: Plugin directory not found");
                LOGGER.warn("BOA: Plugin directory not found, use provided with arg: " + arg);
                path = arg;
            }
            // initialize plugin factory (jar scanning and registering)
            pluginFactory = new PluginFactory(Paths.get(path));
            // initialize arrays for plugins instances and give them initial values (GUI)
            boaState.snakePluginList =
                    new SnakePluginList(NUM_SNAKE_PLUGINS, pluginFactory, viewUpdater);
        } catch (Exception e) {
            // temporary catching may in future be removed
            LOGGER.error("run " + e);
        }

        BOA_.running = true;
        setup(ip); // create main objects in BOA and BOAState, build window

        if (boap.useSubPixel == false) {
            BOA_.log("Upgrade to ImageJ 1.46, or higher," + "\nto get sub-pixel editing.");
        }
        if (IJ.getVersion().compareTo("1.49a") > 0) {
            BOA_.log("(ImageJ " + IJ.getVersion() + " untested)");
        }

        try {
            if (!boaState.nest.isVacant()) {
                runBoa(1, 1);
            }
        } catch (BoaException be) {
            BOA_.log("RUNNING BOA...inital preview failed");
            BOA_.log(be.getMessage());
            be.printStackTrace();
        }
    }

    /**
     * Build all BOA windows and setup initial parameters for segmentation
     * Define also windowListener for cleaning after closing the main window by
     * user.
     * 
     * @param ip Reference to image to be processed by BOA
     * @see BOAp
     */
    void setup(final ImagePlus ip) {
        if (boap.paramsExist == null) {
            boap.segParam.setDefaults();
        }
        boap.setup(ip);

        boaState.nest = new Nest();
        imageGroup = new ImageGroup(ip, boaState.nest);
        boaState.frame = 1;
        // build window and set its title
        canvas = new CustomCanvas(imageGroup.getOrgIpl());
        window = new CustomStackWindow(imageGroup.getOrgIpl(), canvas);
        window.buildWindow();
        window.setTitle(window.getTitle() + " :QuimP: " + quimpInfo[0]);
        // warn about scale
        if (boap.scaleAdjusted) {
            BOA_.log("WARNING Scale was zero - set to 1");
        }
        if (boap.fIAdjusted) {
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
            boaState.nest.addHandlers(rm.getRoisAsArray(), 1);
        } else {
            BOA_.log("No cells from ROI manager");
            if (ip.getRoi() != null) {
                boaState.nest.addHandler(ip.getRoi(), 1);
            } else {
                BOA_.log("No cells from selection");
            }
        }
        rm.close();
        ip.killRoi();

        constrictor = new Constrictor(); // does computations on snakes
    }

    /**
     * Called on every change of GUI that requires action 
     * @warning If screen must be updated but this is not related to updating data (like finishing
     * editing or using zselector) imageGroup.updateOverlay(frame) should be called directly
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.stopEdit()
     * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateSliceSelector()
     */
    private void updateBOA(int frame) {
        imageGroup.updateOverlay(frame);
        boaState.store(frame);
    }

    /**
     * Display about information in BOA window. 
     * 
     * Called from menu bar. Reads also information from all found plugins.
     */
    void about() {
        AboutDialog ad = new AboutDialog(window); // create about dialog with parent 'window'
        ad.appendLine(Tool.getQuimPversion(quimpInfo)); // dispaly template filled by quimpInfo
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
            IQuimpPlugin tmpinst = pluginFactory.getInstance(entry.getKey());
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
        logArea.append("[" + logCount++ + "] " + s + '\n');
    }

    /**
     * Redraw current view. Process outlines by all active plugins. Do not run segmentation again
     * Updates \c liveSnake.
     */
    public void recalculatePlugins() {
        LOGGER.trace("BOA: recalculatePlugins called");
        SnakeHandler sH;
        if (boaState.nest.isVacant())
            return;
        imageGroup.clearPaths(boaState.frame);
        imageGroup.setProcessor(boaState.frame);
        imageGroup.setIpSliceAll(boaState.frame);
        try {
            for (int s = 0; s < boaState.nest.size(); s++) { // for each snake
                sH = boaState.nest.getHandler(s);
                if (boaState.frame < sH.getStartframe()) // if snake does not exist on current frame
                    continue;
                // but if one is on frame f+n and strtFrame is e.g. 1 it may happen that there is
                // no continuity of this snake between frames. In this case getBackupSnake
                // returns null. In general QuimP assumes that if there is a cell on frame f, it
                // will exist on all consecutive frames.
                Snake snake = sH.getBackupSnake(boaState.frame); // if exist get its backup copy
                                                                 // (segm)
                if (snake == null || !snake.alive) // if not alive
                    continue;
                try {
                    Snake out = iterateOverSnakePlugins(snake); // apply all plugins to snake
                    sH.storeThisSnake(out, boaState.frame); // set processed snake as final
                } catch (QuimpPluginException qpe) {
                    // must be rewritten with whole runBOA #65 #67
                    BOA_.log("Error in filter module: " + qpe.getMessage());
                    LOGGER.error(qpe);
                    sH.storeLiveSnake(boaState.frame); // so store only segmented snake as final
                }
            }
        } catch (Exception e) {
            LOGGER.error("Can not update view. Output snake may be defective: " + e.getMessage());
            LOGGER.error(e);
        } finally {
            historyLogger.addEntry("Plugin settings", boaState);
        }
        updateBOA(boaState.frame);
    }

    /**
     * Override action performed on window closing. Clear BOA._running static
     * variable and prevent to notify user that QuimP is running when it has
     * been closed and called again.
     * 
     * @bug
     * When user closes window by system button QuimP does not ask for
     * saving current work. This is because by default QuimP window is
     * managed by ImageJ and it \a probably only hides it on closing
     * 
     * @remarks
     * This class could be located directly in CustomStackWindow which is 
     * included in BOA_. But it need to have access to BOA field \c running.
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
            boaState.snakePluginList.clear(); // close all opened plugin windows
            canvas = null; // clear window data
            imageGroup = null;
            window = null;
            // clear static
            boap = null;
            viewUpdater = null;
        }

        @Override
        public void windowClosing(final WindowEvent arg0) {
            LOGGER.trace("CLOSING");
        }

        @Override
        public void windowActivated(final WindowEvent e) {
            LOGGER.trace("ACTIVATED");
            // rebuild manu for this local window
            // workaround for Mac and theirs menus on top screen bar
            // IJ is doing the same for activation of its window so every time one has correct menu
            // on top
            window.setMenuBar(window.quimpMenuBar);
        }
    }

    /**
     * Supports mouse actions on image at QuimP window according to selected
     * option
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
         * Implement mouse action on image loaded to BOA Used for manual
         * editions of segmented shape. Define reactions of mouse buttons
         * according to GUI state, set by \b Delete and \b Edit buttons.
         * 
         * @see BOAp
         * @see CustomStackWindow
         */
        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);
            if (boap.doDelete) {
                // BOA_.log("Delete at:
                // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
                deleteCell(offScreenX(e.getX()), offScreenY(e.getY()), boaState.frame);
                IJ.setTool(lastTool);
            }
            if (boap.doDeleteSeg) {
                // BOA_.log("Delete at:
                // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
                deleteSegmentation(offScreenX(e.getX()), offScreenY(e.getY()), boaState.frame);
            }
            if (boap.editMode && boap.editingID == -1) {
                // BOA_.log("Delete at:
                // ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
                editSeg(offScreenX(e.getX()), offScreenY(e.getY()), boaState.frame);
            }
        }
    } // end of CustomCanvas

    /**
     * Extends standard ImageJ StackWindow adding own GUI elements.
     * 
     * This class stands for definition of main BOA plugin GUI window. Current
     * state of BOA plugin is stored at {@link BOAp} class.
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
        private MenuItem menuVersion, menuSaveConfig, menuLoadConfig, menuShowHistory; // items
        private CheckboxMenuItem cbMenuPlotOriginalSnakes, cbMenuPlotHead;

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
         * This method is called as first. The interface is built in three steps:
         * Left side of window (configuration zone) and right side of main
         * window (logs and other info and buttons) and finally upper menubar
         * 
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateWindowState()
         */
        private void buildWindow() {

            setLayout(new BorderLayout(10, 3));

            if (!boap.singleImage) {
                remove(sliceSelector);
            }
            if (!boap.singleImage) {
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

        }

        /**
         * Build window menu.
         * 
         * Menu is local for this window of QuimP and it is stored in \c quimpMenuBar variable.
         * On every time when QuimP is active, this menu is restored in 
         * uk.ac.warwick.wsbc.QuimP.BOA_.CustomWindowAdapter.windowActivated(WindowEvent) method
         * This is due to overwriting menu by IJ on Mac (all menus are on top screen bar)
         * @return Reference to menu bar
         */
        final MenuBar buildMenu() {
            MenuBar menuBar; // main menu bar
            Menu menuAbout; // menu About in menubar
            Menu menuConfig; // menu Config in menubar

            menuBar = new MenuBar();

            menuConfig = new Menu("Preferences");

            menuAbout = new Menu("About");

            // build main line
            menuBar.add(menuConfig);
            menuBar.add(menuAbout);

            // add entries
            menuVersion = new MenuItem("Version");
            menuVersion.addActionListener(this);
            menuAbout.add(menuVersion);

            cbMenuPlotOriginalSnakes = new CheckboxMenuItem("Plot original");
            cbMenuPlotOriginalSnakes.setState(boap.isProcessedSnakePlotted);
            cbMenuPlotOriginalSnakes.addItemListener(this);
            menuConfig.add(cbMenuPlotOriginalSnakes);
            cbMenuPlotHead = new CheckboxMenuItem("Plot head");
            cbMenuPlotHead.setState(boap.isHeadPlotted);
            cbMenuPlotHead.addItemListener(this);
            menuConfig.add(cbMenuPlotHead);

            menuSaveConfig = new MenuItem("Save preferences");
            menuSaveConfig.addActionListener(this);
            menuConfig.add(menuSaveConfig);

            menuLoadConfig = new MenuItem("Load preferences");
            menuLoadConfig.addActionListener(this);
            menuConfig.add(menuLoadConfig);

            menuShowHistory = new MenuItem("Show history");
            menuShowHistory.addActionListener(this);
            menuConfig.add(menuShowHistory);

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

            fpsLabel = new Label("F Interval: " + IJ.d2s(boap.imageFrameInterval, 3) + " s");
            northPanel.add(fpsLabel);
            pixelLabel = new Label("Scale: " + IJ.d2s(boap.imageScale, 6) + " \u00B5m");
            northPanel.add(pixelLabel);

            bScale = addButton("Set Scale", northPanel);
            bDelSeg = addButton("Truncate Seg", northPanel);
            bAdd = addButton("Add cell", northPanel);
            bDel = addButton("Delete cell", northPanel);

            // build subpanel with plugins
            // get plugins names collected by PluginFactory
            ArrayList<String> pluginList =
                    boaState.snakePluginList.getPluginNames(IQuimpPlugin.DOES_SNAKES);
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

            cFirstPluginActiv = addCheckbox("A", pluginPanel, boaState.snakePluginList.isActive(0));
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

            cSecondPluginActiv =
                    addCheckbox("A", pluginPanel, boaState.snakePluginList.isActive(1));
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

            cThirdPluginActiv = addCheckbox("A", pluginPanel, boaState.snakePluginList.isActive(2));
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
            dsNodeRes = addDoubleSpinner("Node Spacing:", paramPanel, boap.segParam.getNodeRes(),
                    1., 20., 0.2, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isMaxIterations =
                    addIntSpinner("Max Iterations:", paramPanel, boap.segParam.max_iterations, 100,
                            10000, 100, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isBlowup = addIntSpinner("Blowup:", paramPanel, boap.segParam.blowup, 0, 200, 2,
                    CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsVel_crit = addDoubleSpinner("Crit velocity:", paramPanel, boap.segParam.vel_crit,
                    0.0001, 2., 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_image = addDoubleSpinner("Image F:", paramPanel, boap.segParam.f_image, 0.01, 10.,
                    0.01, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_central = addDoubleSpinner("Central F:", paramPanel, boap.segParam.f_central,
                    0.0005, 1, 0.002, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsF_contract = addDoubleSpinner("Contract F:", paramPanel, boap.segParam.f_contract,
                    0.001, 1, 0.001, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            dsFinalShrink = addDoubleSpinner("Final Shrink:", paramPanel, boap.segParam.finalShrink,
                    -100, 100, 0.5, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isSample_tan = addIntSpinner("Sample tan:", paramPanel, boap.segParam.sample_tan, 1, 30,
                    1, CustomStackWindow.DEFAULT_SPINNER_SIZE);
            isSample_norm = addIntSpinner("Sample norm:", paramPanel, boap.segParam.sample_norm, 1,
                    60, 1, CustomStackWindow.DEFAULT_SPINNER_SIZE);

            cPrevSnake = addCheckbox("Use Previouse Snake", paramPanel,
                    boap.segParam.use_previous_snake);
            cExpSnake = addCheckbox("Expanding Snake", paramPanel, boap.segParam.expandSnake);

            Panel segEditPanel = new Panel();
            segEditPanel.setLayout(new GridLayout(1, 2));
            bSeg = addButton("SEGMENT", segEditPanel);
            bEdit = addButton("Edit", segEditPanel);
            paramPanel.add(segEditPanel);

            // mini panel comprised from slice selector and frame number (if not single image)
            Panel sliderPanel = new Panel();
            sliderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            if (!boap.singleImage) {
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
            cPath = addCheckbox("Show paths", bottomPanel, boap.segParam.showPaths);
            sZoom = addComboBox(new String[] { fullZoom }, bottomPanel);
            // add mouse listener to create menu dynamically on click
            sZoom.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    sZoom.removeAll();
                    sZoom.add(fullZoom); // default word for full zoom (100% of view)
                    List<Integer> frames = boaState.nest.getSnakesforFrame(boaState.frame);
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
         * Set default values defined in model class
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp} and update UI
         * 
         * @see BOAp
         */
        private void setDefualts() {
            boap.segParam.setDefaults();
            updateSpinnerValues();
        }

        /**
         * Update spinners in BOA UI Update spinners according to values stored
         * in machine state {@link uk.ac.warwick.wsbc.QuimP.BOAp}
         * 
         * @see BOAp
         */
        private void updateSpinnerValues() {
            boap.supressStateChangeBOArun = true;
            dsNodeRes.setValue(boap.segParam.getNodeRes());
            dsVel_crit.setValue(boap.segParam.vel_crit);
            dsF_image.setValue(boap.segParam.f_image);
            dsF_central.setValue(boap.segParam.f_central);
            dsF_contract.setValue(boap.segParam.f_contract);
            dsFinalShrink.setValue(boap.segParam.finalShrink);
            isMaxIterations.setValue(boap.segParam.max_iterations);
            isBlowup.setValue(boap.segParam.blowup);
            isSample_tan.setValue(boap.segParam.sample_tan);
            isSample_norm.setValue(boap.segParam.sample_norm);
            boap.supressStateChangeBOArun = false;
        }

        /**
         * Update checkboxes
         * 
         * @see SnakePluginList
         * @see itemStateChanged(ItemEvent)
         */
        private void updateCheckBoxes() {
            // first plugin activity
            cFirstPluginActiv.setState(boaState.snakePluginList.isActive(0));
            // second plugin activity
            cSecondPluginActiv.setState(boaState.snakePluginList.isActive(1));
            // third plugin activity
            cThirdPluginActiv.setState(boaState.snakePluginList.isActive(2));
        }

        /**
         * Update Choices
         * 
         * @see SnakePluginList
         * @see itemStateChanged(ItemEvent)
         */
        private void updateChoices() {
            // first slot snake plugin
            if (boaState.snakePluginList.getInstance(0) == null)
                sFirstPluginName.select(NONE);
            else
                sFirstPluginName.select(boaState.snakePluginList.getName(0));
            // second slot snake plugin
            if (boaState.snakePluginList.getInstance(1) == null)
                sSecondPluginName.select(NONE);
            else
                sSecondPluginName.select(boaState.snakePluginList.getName(1));
            // third slot snake plugin
            if (boaState.snakePluginList.getInstance(2) == null)
                sThirdPluginName.select(NONE);
            else
                sThirdPluginName.select(boaState.snakePluginList.getName(2));

        }

        /**
         * Implement user interface logic. Do not refresh values, rather disable/enable controls.
         */
        private void updateWindowState() {
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
         * Do not support mouse events, only UI elements like buttons, spinners and menus. 
         * Runs also main algorithm on specified input state and update screen on plugins
         * operations.
         * 
         * @param e Type of event
         * @see BOAp
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateWindowState()
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            boolean run = false; // some actions require to re-run segmentation.
                                 // They set run to true
            Object b = e.getSource();
            if (b == bDel && !boap.editMode && !boap.doDeleteSeg) {
                if (boap.doDelete == false) {
                    bDel.setLabel("*STOP DEL*");
                    boap.doDelete = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                } else {
                    boap.doDelete = false;
                    bDel.setLabel("Delete cell");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (boap.doDelete) { // stop if delete is on
                BOA_.log("**DELETE IS ON**");
                return;
            }
            if (b == bDelSeg && !boap.editMode) {
                if (!boap.doDeleteSeg) {
                    bDelSeg.setLabel("*STOP TRUNCATE*");
                    boap.doDeleteSeg = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                } else {
                    boap.doDeleteSeg = false;
                    bDelSeg.setLabel("Truncate Seg");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (boap.doDeleteSeg) { // stop if delete is on
                BOA_.log("**TRUNCATE SEG IS ON**");
                return;
            }
            if (b == bEdit) {
                if (boap.editMode == false) {
                    bEdit.setLabel("*STOP EDIT*");
                    BOA_.log("**EDIT IS ON**");
                    boap.editMode = true;
                    lastTool = IJ.getToolName();
                    IJ.setTool(Toolbar.LINE);
                    if (boaState.nest.size() == 1)
                        editSeg(0, 0, boaState.frame); // if only 1 snake go straight to edit, if
                                                       // more user must pick one
                    // remember that this frame is edited
                    boaState.storeOnlyEdited(boaState.frame);
                } else {
                    boap.editMode = false;
                    if (boap.editingID != -1) {
                        stopEdit();
                    }
                    bEdit.setLabel("Edit");
                    IJ.setTool(lastTool);
                }
                return;
            }
            if (boap.editMode) { // stop if edit on
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
                    runBoa(boaState.frame, boap.FRAMES);
                    framesCompleted = boap.FRAMES;
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
                    if (boap.readParams()) {
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
                pixelLabel.setText("Scale: " + IJ.d2s(boap.imageScale, 6) + " \u00B5m");
                fpsLabel.setText("F Interval: " + IJ.d2s(boap.imageFrameInterval, 3) + " s");
            } else if (b == bAdd) {
                addCell(canvas.getImage().getRoi(), boaState.frame);
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
                        + boaState.snakePluginList.getInstance(0));
                if (boaState.snakePluginList.getInstance(0) != null) // call 0 instance
                    boaState.snakePluginList.getInstance(0).showUI(true);
            }
            if (b == bSecondPluginGUI) {
                LOGGER.debug("Second plugin GUI, state of BOAp is "
                        + boaState.snakePluginList.getInstance(1));
                if (boaState.snakePluginList.getInstance(1) != null) // call 1 instance
                    boaState.snakePluginList.getInstance(1).showUI(true);
            }
            if (b == bThirdPluginGUI) {
                LOGGER.debug("Third plugin GUI, state of BOAp is "
                        + boaState.snakePluginList.getInstance(2));
                if (boaState.snakePluginList.getInstance(2) != null) // call 2 instance
                    boaState.snakePluginList.getInstance(2).showUI(true);
            }

            // menu listeners
            if (b == menuVersion) {
                about();
            }
            if (b == menuSaveConfig) {
                String saveIn = boap.orgFile.getParent();
                SaveDialog sd = new SaveDialog("Save plugin config data...", saveIn, boap.fileName,
                        ".pgQP");
                if (sd.getFileName() != null) {
                    try {
                        // Create Serialization object with extra info layer
                        Serializer<SnakePluginList> s;
                        s = new Serializer<>(boaState.snakePluginList, quimpInfo);
                        s.setPretty(); // set pretty format
                        s.save(sd.getDirectory() + sd.getFileName()); // save it
                        s = null; // remove
                    } catch (FileNotFoundException e1) {
                        LOGGER.error("Problem with saving plugin config");
                    }
                }
            }
            // TODO Add checking loaded version using quimpInfo data sealed in Serializer.save
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
                        boaState.snakePluginList.clear(); // closes windows, etc
                        boaState.snakePluginList = loaded.obj; // replace with fresh instance
                        updateCheckBoxes(); // update checkboxes
                        updateChoices(); // and choices
                        recalculatePlugins(); // and screen
                    } catch (IOException e1) {
                        LOGGER.error("Problem with loading plugin config");
                    } catch (JsonSyntaxException e1) {
                        LOGGER.error("Problem with configuration file: " + e1.getMessage());
                    } catch (Exception e1) {
                        LOGGER.error(e1); // something serious
                    }
                }
            }

            // history
            if (b == menuShowHistory) {
                LOGGER.debug("got ShowHistory");
                if (historyLogger.isOpened())
                    historyLogger.closeHistory();
                else
                    historyLogger.openHistory();
            }

            updateWindowState(); // window logic on any change

            // run segmentation for selected cases
            if (run) {
                System.out.println("running from in stackwindow");
                // run on current frame
                try {
                    runBoa(boaState.frame, boaState.frame);
                } catch (BoaException be) {
                    BOA_.log(be.getMessage());
                }
                // imageGroup.setSlice(1);
            }
        }

        /**
         * Detect changes in checkboxes and run segmentation for current frame
         * if necessary. Transfer parameters from changed GUI element to
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp} class
         * 
         * @param e Type of event
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateWindowState()
         */
        @Override
        public void itemStateChanged(final ItemEvent e) {
            // detect check boxes
            if (boap.doDelete) {
                BOA_.log("**WARNING:DELETE IS ON**");
            }
            boolean run = false; // set to true if any of items changes require
                                 // to re-run segmentation
            Object source = e.getItemSelectable();
            if (source == cPath) {
                boap.segParam.showPaths = cPath.getState();
                if (boap.segParam.showPaths) {
                    this.setImage(imageGroup.getPathsIpl());
                } else {
                    this.setImage(imageGroup.getOrgIpl());
                }
                if (boap.zoom && !boaState.nest.isVacant()) { // set zoom
                    imageGroup.zoom(canvas, boaState.frame, boaState.snakeToZoom);
                }
            } else if (source == cPrevSnake) {
                boap.segParam.use_previous_snake = cPrevSnake.getState();
            } else if (source == cExpSnake) {
                boap.segParam.expandSnake = cExpSnake.getState();
                run = true;
            } else if (source == cFirstPluginActiv) {
                boaState.snakePluginList.setActive(0, cFirstPluginActiv.getState());
                recalculatePlugins();
            } else if (source == cSecondPluginActiv) {
                boaState.snakePluginList.setActive(1, cSecondPluginActiv.getState());
                recalculatePlugins();
            } else if (source == cThirdPluginActiv) {
                boaState.snakePluginList.setActive(2, cThirdPluginActiv.getState());
                recalculatePlugins();
            }

            // action on menus
            if (source == cbMenuPlotOriginalSnakes) {
                boap.isProcessedSnakePlotted = cbMenuPlotOriginalSnakes.getState();
                recalculatePlugins();
            }
            if (source == cbMenuPlotHead) {
                boap.isHeadPlotted = cbMenuPlotHead.getState();
                updateBOA(boaState.frame);
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
                    boaState.snakeToZoom = -1; // set negative value to indicate no zoom
                    boap.zoom = false; // important for other parts (legacy)
                    imageGroup.unzoom(canvas); // unzoom view
                } else // zoom here
                if (!boaState.nest.isVacant()) { // any snakes present
                    boaState.snakeToZoom = Integer.parseInt(sZoom.getSelectedItem()); // get int
                    boap.zoom = true; // legacy compatibility
                    imageGroup.zoom(canvas, boaState.frame, boaState.snakeToZoom);
                }
            }

            updateWindowState(); // window logic on any change

            if (run) {
                if (boap.supressStateChangeBOArun) {
                    // boap.supressStateChangeBOArun = false;
                    System.out.println("supressStateItem");
                    System.out.println(source.toString());
                    return;
                }
                // run on current frame
                try {
                    runBoa(boaState.frame, boaState.frame);
                } catch (BoaException be) {
                    BOA_.log(be.getMessage());
                }
                // imageGroup.setSlice(1);
            }
        }

        /**
         * Detect changes in spinners and run segmentation for current frame if
         * necessary. Transfer parameters from changed GUI element to
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp} class
         * 
         * @param ce Type of event
         * @see uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateWindowState()
         */
        @Override
        public void stateChanged(final ChangeEvent ce) {
            if (boap.doDelete) {
                BOA_.log("**WARNING:DELETE IS ON**");
            }
            boolean run = false; // set to true if any of items changes require to re-run
                                 // segmentation
            Object source = ce.getSource();

            if (source == dsNodeRes) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.setNodeRes((Double) spinner.getValue());
                run = true;
            } else if (source == dsVel_crit) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.vel_crit = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_image) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.f_image = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_central) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.f_central = (Double) spinner.getValue();
                run = true;
            } else if (source == dsF_contract) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.f_contract = (Double) spinner.getValue();
                run = true;
            } else if (source == dsFinalShrink) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.finalShrink = (Double) spinner.getValue();
                run = true;
            } else if (source == isMaxIterations) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.max_iterations = (Integer) spinner.getValue();
                run = true;
            } else if (source == isBlowup) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.blowup = (Integer) spinner.getValue();
                run = true;
            } else if (source == isSample_tan) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.sample_tan = (Integer) spinner.getValue();
                run = true;
            } else if (source == isSample_norm) {
                JSpinner spinner = (JSpinner) source;
                boap.segParam.sample_norm = (Integer) spinner.getValue();
                run = true;
            }

            updateWindowState(); // window logic on any change

            if (run) {
                if (boap.supressStateChangeBOArun) {
                    // boap.supressStateChangeBOArun = false;
                    System.out.println("supressState");
                    System.out.println(source.toString());
                    return;
                }
                // System.out.println("run from state change");
                // run on current frame
                try {
                    runBoa(boaState.frame, boaState.frame);
                } catch (BoaException be) {
                    BOA_.log(be.getMessage());
                }
                // imageGroup.setSlice(1);
            }

        }

        /**
         * Update the frame label, overlay, frame and set zoom Called when user
         * clicks on slice selector in IJ window.
         */
        @Override
        public void updateSliceSelector() {
            super.updateSliceSelector();
            zSelector.setValue(imp.getCurrentSlice()); // this is delayed in
                                                       // super.updateSliceSelector force it now

            // if in edit, save current edit and start edit of next frame if exists
            boolean wasInEdit = boap.editMode;
            if (boap.editMode) {
                // BOA_.log("next frame in edit mode");
                stopEdit();
            }

            boaState.frame = imp.getCurrentSlice();
            frameLabel.setText("" + boaState.frame);
            imageGroup.updateOverlay(boaState.frame); // draw overlay
            imageGroup.setIpSliceAll(boaState.frame);

            // zoom to snake zero
            if (boap.zoom && !boaState.nest.isVacant()) {
                SnakeHandler sH = boaState.nest.getHandler(0);
                if (sH.isStoredAt(boaState.frame)) {
                    imageGroup.zoom(canvas, boaState.frame, boaState.snakeToZoom);
                }
            }

            if (wasInEdit) {
                bEdit.setLabel("*STOP EDIT*");
                BOA_.log("**EDIT IS ON**");
                boap.editMode = true;
                lastTool = IJ.getToolName();
                IJ.setTool(Toolbar.LINE);
                editSeg(0, 0, boaState.frame);
                IJ.setTool(lastTool);
            }
            LOGGER.trace(
                    "Snakes at this frame: " + boaState.nest.getSnakesforFrame(boaState.frame));
        }

        /**
         * Turn delete mode off by setting proper value in
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp}
         */
        void switchOffDelete() {
            boap.doDelete = false;
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
         * {@link uk.ac.warwick.wsbc.QuimP.BOAp}
         */
        void switchOfftruncate() {
            boap.doDeleteSeg = false;
            bDelSeg.setLabel("Truncate Seg");
        }

        void setScalesText() {
            pixelLabel.setText("Scale: " + IJ.d2s(boap.imageScale, 6) + " \u00B5m");
            fpsLabel.setText("F Interval: " + IJ.d2s(boap.imageFrameInterval, 3) + " s");
        }
    } // end of CustomStackWindow

    /**
     * Creates instance (through SnakePluginList) of plugin of given name on given UI slot.
     * 
     * Decides if plugin will be created or destroyed basing on plugin \b name from Choice list
     * 
     * @param selectedPlugin Name of plugin returned from UI elements
     * @param slot Slot of plugin
     * @param act Indicates if plugins is activated in GUI
     * @see QuimP.SnakePluginList
     */
    private void instanceSnakePlugin(final String selectedPlugin, int slot, boolean act) {

        try {
            // get instance using plugin name (obtained from getPluginNames from PluginFactory
            if (selectedPlugin != NONE) { // do no pass NONE to pluginFact
                boaState.snakePluginList.setInstance(slot, selectedPlugin, act); // build instance
            } else {
                if (boaState.snakePluginList.getInstance(slot) != null)
                    boaState.snakePluginList.getInstance(slot).showUI(false);
                boaState.snakePluginList.deletePlugin(slot);
            }
        } catch (QuimpPluginException e) {
            LOGGER.warn("Plugin " + selectedPlugin + " cannot be loaded");
        }
    }

    /**
     * Start segmentation process on range of frames
     * 
     * This method is called for update only current view as well (\c startF ==
     * \c endF)
     * 
     * @param startF start frame
     * @param endF end frame
     * @throws BoaException
     * @todo TODO Rewrite exceptions here
     * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/65
     */
    public void runBoa(int startF, int endF) throws BoaException {
        System.out.println("run BOA");
        boap.SEGrunning = true;
        if (boaState.nest.isVacant()) {
            BOA_.log("Nothing to segment!");
            boap.SEGrunning = false;
            return;
        }
        try {
            IJ.showProgress(0, endF - startF);

            // if(boap.expandSnake) boap.NMAX = 9990; // percent hack

            boaState.nest.resetForFrame(startF);
            if (!boap.segParam.expandSnake) { // blowup snake ready for contraction (only those not
                // starting
                // at or after the startF)
                constrictor.loosen(boaState.nest, startF);
            } else {
                constrictor.implode(boaState.nest, startF);
            }
            SnakeHandler sH;

            int s = 0;
            Snake snake;
            imageGroup.clearPaths(startF);

            for (boaState.frame = startF; boaState.frame <= endF; boaState.frame++) { // per frame
                // System.out.println("\n737 Frame: " + frame);
                imageGroup.setProcessor(boaState.frame);
                imageGroup.setIpSliceAll(boaState.frame);

                try {
                    if (boaState.frame != startF) {// expand snakes for next frame
                        if (!boap.segParam.use_previous_snake) {
                            boaState.nest.resetForFrame(boaState.frame);
                        } else {
                            if (!boap.segParam.expandSnake) {
                                constrictor.loosen(boaState.nest, boaState.frame);
                            } else {
                                constrictor.implode(boaState.nest, boaState.frame);
                            }
                        }
                    }

                    for (s = 0; s < boaState.nest.size(); s++) { // for each snake
                        sH = boaState.nest.getHandler(s);
                        snake = sH.getLiveSnake();
                        try {
                            if (!snake.alive || boaState.frame < sH.getStartframe()) {
                                continue;
                            }
                            imageGroup.drawPath(snake, boaState.frame); // pre tightned snake on
                                                                        // path
                            tightenSnake(snake);
                            imageGroup.drawPath(snake, boaState.frame); // post tightned snake on
                                                                        // path
                            sH.backupLiveSnake(boaState.frame);
                            Snake out = iterateOverSnakePlugins(snake);
                            sH.storeThisSnake(out, boaState.frame); // store resulting snake as
                                                                    // final

                        } catch (QuimpPluginException qpe) {
                            // must be rewritten with whole runBOA #65 #67
                            BOA_.log("Error in filter module: " + qpe.getMessage());
                            LOGGER.error(qpe);
                            sH.storeLiveSnake(boaState.frame); // store segmented nonmodified

                        } catch (BoaException be) {
                            imageGroup.drawPath(snake, boaState.frame); // failed
                            // position
                            // sH.deleteStoreAt(frame);
                            sH.storeLiveSnake(boaState.frame);
                            sH.backupLiveSnake(boaState.frame);
                            boaState.nest.kill(sH);
                            snake.unfreezeAll();
                            BOA_.log("Snake " + snake.getSnakeID() + " died, frame "
                                    + boaState.frame);
                            boap.SEGrunning = false;
                            if (boaState.nest.allDead()) {
                                throw new BoaException("All snakes dead: " + be.getMessage(),
                                        boaState.frame, 1);
                            }
                        }

                    }
                    updateBOA(boaState.frame); // update view and store state
                    IJ.showProgress(boaState.frame, endF);
                } catch (BoaException be) {
                    boap.SEGrunning = false;
                    if (!boap.segParam.use_previous_snake) {
                        imageGroup.setIpSliceAll(boaState.frame);
                        updateBOA(boaState.frame); // update view and store state
                    } else {
                        System.out.println("\nL811. Exception");
                        throw be;
                    }
                } finally {
                    historyLogger.addEntry("Processing", boaState);
                }
            }
            boaState.frame = endF;
        } catch (Exception e) {
            // e.printStackTrace();
            /// imageGroup.drawContour(nest.getSNAKES(), frame);
            // imageGroup.updateAndDraw();
            boap.SEGrunning = false;
            e.printStackTrace();
            throw new BoaException("Frame " + boaState.frame + ": " + e.getMessage(),
                    boaState.frame, 1);
        }
        boap.SEGrunning = false;
    }

    /**
     * Process \c Snake by all active plugins. Processed \c Snake is returned as new Snake with
     * the same ID. Input snake is not modified. For empty plugin list it just return input snake
     *
     * This method supports two interfaces:
     * -# IQuimpPoint2dFilter
     * -# IQuimpSnakeFilter
     * 
     * It uses smart method to detect which interface is used for every slot to avoid unnecessary
     * conversion between data. \c previousConversion keeps what interface was used on previous
     * slot in plugin stack. Then for every plugin data are converted if current plugin differs
     * from previous one. Converted data are kept in \c snakeToProcess and \c dataToProcess
     * but only one of these variables is valid in given time. Finally after last plugin 
     * data are converted to Snake.
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
        if (!boaState.snakePluginList.isRefListEmpty()) {
            LOGGER.debug("sPluginList not empty");
            for (Plugin qP : boaState.snakePluginList.getList()) { // iterate over list
                if (!qP.isExecutable())
                    continue; // no plugin on this slot or not active
                if (qP.getRef() instanceof IQuimpPoint2dFilter) { // check interface type
                    if (previousConversion == isnake) { // previous was IQuimpSnakeFilter
                        dataToProcess = snakeToProcess.asList(); // and data needs to be converted
                    }
                    IQuimpPoint2dFilter qPcast = (IQuimpPoint2dFilter) qP.getRef();
                    qPcast.attachData(dataToProcess);
                    dataToProcess = qPcast.runPlugin(); // store result in input variable
                    previousConversion = ipoint;
                }
                if (qP.getRef() instanceof IQuimpSnakeFilter) { // check interface type
                    if (previousConversion == ipoint) { // previous was IQuimpPoint2dFilter
                        // and data must be converted to snake from dataToProcess
                        snakeToProcess =
                                new QuimpDataConverter(dataToProcess).getSnake(snake.getSnakeID());
                    }
                    IQuimpSnakeFilter qPcast = (IQuimpSnakeFilter) qP.getRef();
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

        for (i = 0; i < boap.segParam.max_iterations; i++) { // iter constrict snake
            if (i % boap.cut_every == 0) {
                snake.cutLoops(); // cut out loops every p.cut_every timesteps
            }
            if (i % 10 == 0 && i != 0) {
                snake.correctDistance(true);
            }
            if (constrictor.constrict(snake, imageGroup.getOrgIp())) { // if all nodes frozen
                break;
            }
            if (i % 4 == 0) {
                imageGroup.drawPath(snake, boaState.frame); // draw current snake
            }

            if ((snake.getNumNodes() / snake.startingNnodes) > boap.NMAX) {
                // if max nodes reached (as % starting) prompt for reset
                if (boap.segParam.use_previous_snake) {
                    // imageGroup.drawContour(snake, frame);
                    // imageGroup.updateAndDraw();
                    throw new BoaException(
                            "Frame " + boaState.frame + "-max nodes reached " + snake.getNumNodes(),
                            boaState.frame, 1);
                } else {
                    BOA_.log("Frame " + boaState.frame + "-max nodes reached..continue");
                    break;
                }
            }
            // if (i == boap.max_iterations - 1) {
            // BOA_.log("Frame " + frame + "-max iterations reached");
            // }
            // break;
        }
        snake.unfreezeAll(); // set freeze tag back to false

        if (!boap.segParam.expandSnake) { // shrink a bit to get final outline
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
        gd.addNumericField("Frame interval (seconds)", boap.imageFrameInterval, 3);
        gd.addNumericField("Pixel width (\u00B5m)", boap.imageScale, 6);
        gd.showDialog();

        double tempFI = gd.getNextNumber(); // force to check for errors
        double tempP = gd.getNextNumber();

        if (gd.invalidNumber()) {
            IJ.error("Values invalid");
            BOA_.log("Scale was not updated:\n\tinvalid input");
        } else if (gd.wasOKed()) {
            boap.imageFrameInterval = tempFI;
            boap.imageScale = tempP;
            updateImageScale();
            BOA_.log("Scale successfully updated");
        }

    }

    void updateImageScale() {
        imageGroup.getOrgIpl().getCalibration().frameInterval = boap.imageFrameInterval;
        imageGroup.getOrgIpl().getCalibration().pixelHeight = boap.imageScale;
        imageGroup.getOrgIpl().getCalibration().pixelWidth = boap.imageScale;
    }

    boolean loadSnakes() {

        YesNoCancelDialog yncd = new YesNoCancelDialog(IJ.getInstance(), "Load associated snakes?",
                "\tLoad associated snakes?\n");
        if (!yncd.yesPressed()) {
            return false;
        }

        OutlineHandler oH = new OutlineHandler(boap.readQp);
        if (!oH.readSuccess) {
            BOA_.log("Could not read in snakes");
            return false;
        }
        // convert to BOA snakes

        boaState.nest.addOutlinehandler(oH);
        imageGroup.setProcessor(oH.getStartFrame());
        updateBOA(oH.getStartFrame());
        BOA_.log("Successfully read snakes");
        return true;
    }

    /**
     * Add ROI to Nest.
     * 
     * This method is called on selection that should contain object to be
     * segmented. Initialize Snake object in Nest and it performs also initial
     * segmentation of selected cell
     * 
     * @param r ROI object (IJ)
     * @param f number of current frame
     * @see tightenSnake(Snake)
     */
    // @SuppressWarnings("unchecked")
    void addCell(final Roi r, int f) {
        boolean isPluginError = false; // any error from plugin?
        SnakeHandler sH = boaState.nest.addHandler(r, f);
        Snake snake = sH.getLiveSnake();
        imageGroup.setProcessor(f);
        try {
            imageGroup.drawPath(snake, f); // pre tightned snake on path
            tightenSnake(snake);
            imageGroup.drawPath(snake, f); // post tightned snake on path
            sH.backupLiveSnake(f);
            Snake out = iterateOverSnakePlugins(snake); // process segmented snake by plugins
            sH.storeThisSnake(out, f); // store processed snake as final
        } catch (QuimpPluginException qpe) {
            isPluginError = true; // we have error
            BOA_.log("Error in filter module: " + qpe.getMessage());
            LOGGER.error(qpe);
        } catch (BoaException be) {
            BOA_.log("New snake failed to converge");
            LOGGER.error(be);
        } catch (Exception e) {
            BOA_.log("Undefined error from plugin");
            LOGGER.fatal(e);
        }
        // if any problem with plugin or other, store snake without modification
        // because snake.asList() returns copy
        try {
            if (isPluginError)
                sH.storeLiveSnake(f); // so store original livesnake after segmentation
        } catch (BoaException be) {
            BOA_.log("Could not store new snake");
            LOGGER.error(be);
        } finally {
            updateBOA(f);
            historyLogger.addEntry("Added cell", boaState);
        }

    }

    boolean deleteCell(int x, int y, int frame) {
        if (boaState.nest.isVacant()) {
            return false;
        }

        SnakeHandler sH;
        Snake snake;
        ExtendedVector2d sV;
        ExtendedVector2d mV = new ExtendedVector2d(x, y);
        double[] distance = new double[boaState.nest.size()];

        for (int i = 0; i < boaState.nest.size(); i++) { // calc all distances
            sH = boaState.nest.getHandler(i);
            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance[i] = ExtendedVector2d.lengthP2P(mV, sV);
            }
        }
        int minIndex = Tool.minArrayIndex(distance);
        if (distance[minIndex] < 10) { // if closest < 10, delete it
            BOA_.log("Deleted cell " + boaState.nest.getHandler(minIndex).getID());
            boaState.nest.removeHandler(boaState.nest.getHandler(minIndex));
            updateBOA(frame);
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
        double[] distance = new double[boaState.nest.size()];

        for (int i = 0; i < boaState.nest.size(); i++) { // calc all distances
            sH = boaState.nest.getHandler(i);

            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance[i] = ExtendedVector2d.lengthP2P(mV, sV);
            } else {
                distance[i] = 9999;
            }
        }

        int minIndex = Tool.minArrayIndex(distance);
        // BOA_.log("Debug: closest index " + minIndex + ", id " +
        // nest.getHandler(minIndex).getID());
        if (distance[minIndex] < 10) { // if closest < 10, delete it
            BOA_.log("Deleted snake " + boaState.nest.getHandler(minIndex).getID() + " from "
                    + frame + " onwards");
            sH = boaState.nest.getHandler(minIndex);
            sH.deleteStoreFrom(frame);
            updateBOA(frame);
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
     * @see stopEdit
     * @see updateSliceSelector
     */
    void editSeg(int x, int y, int frame) {
        SnakeHandler sH;
        Snake snake;
        ExtendedVector2d sV;
        ExtendedVector2d mV = new ExtendedVector2d(x, y);
        double[] distance = new double[boaState.nest.size()];

        for (int i = 0; i < boaState.nest.size(); i++) { // calc all distances
            sH = boaState.nest.getHandler(i);
            if (sH.isStoredAt(frame)) {
                snake = sH.getStoredSnake(frame);
                sV = snake.getCentroid();
                distance[i] = ExtendedVector2d.lengthP2P(mV, sV);
            }
        }
        int minIndex = Tool.minArrayIndex(distance);
        if (distance[minIndex] < 10 || boaState.nest.size() == 1) { // if closest < 10, edit it
            sH = boaState.nest.getHandler(minIndex);
            boap.editingID = minIndex; // sH.getID();
            BOA_.log("Editing cell " + sH.getID());
            imageGroup.clearOverlay();

            Roi r;
            if (boap.useSubPixel == true) {
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
     * @see updateSliceSelector
     */
    void stopEdit() {
        Roi r = canvas.getImage().getRoi();
        Roi.setColor(Color.yellow);
        SnakeHandler sH = boaState.nest.getHandler(boap.editingID);
        sH.storeRoi((PolygonRoi) r, boaState.frame);
        canvas.getImage().killRoi();
        imageGroup.updateOverlay(boaState.frame);
        boap.editingID = -1;
    }

    void deleteSeg(int x, int y) {
    }

    /**
     * Initializing all data saving and exporting results to disk and IJ
     */
    private void finish() {
        IJ.showStatus("BOA-FINISHING");
        YesNoCancelDialog ync;

        if (boap.saveSnake) {
            try {
                if (boaState.nest.writeSnakes()) { // write snPQ file (if any snake)
                    boaState.nest.analyse(imageGroup.getOrgIpl().duplicate()); // write stQP file
                                                                               // and fill outFile
                                                                               // used later
                    // auto save plugin config (but only if there is at least one snake)
                    if (!boaState.nest.isVacant()) {
                        // Create Serialization object with extra info layer
                        Serializer<SnakePluginList> s;
                        s = new Serializer<>(boaState.snakePluginList, quimpInfo);
                        s.setPretty(); // set pretty format
                        s.save(boap.outFile.getParent() + File.separator + boap.fileName + ".pgQP");
                        s = null; // remove
                        // Dump BOAState object s new format
                        Serializer<BOAState> n;
                        n = new Serializer<>(boaState, quimpInfo);
                        n.setPretty();
                        n.save(boap.outFile.getParent() + File.separator + boap.fileName
                                + ".newsnQP");
                        n = null;
                    }
                } else {
                    ync = new YesNoCancelDialog(window, "Save Segmentation",
                            "Quit without saving?");
                    if (!ync.yesPressed()) {
                        return;
                    }
                }
            } catch (IOException e) {
                IJ.error("Exception while saving");
                LOGGER.error(e);
                return;
            }
        }
        BOA_.running = false;
        imageGroup.makeContourImage();
        boaState.nest = null; // remove from memory
        imageGroup.getOrgIpl().setOverlay(new Overlay());
        new StackWindow(imageGroup.getOrgIpl()); // clear overlay
        window.setImage(new ImagePlus());
        window.close();
    }

    /**
     * Action for Quit button Set BOA_.running static field to false and close
     * the window
     * 
     * @author rtyson
     * @author p.baniukiewicz
     */
    void quit() {
        YesNoCancelDialog ync;
        ync = new YesNoCancelDialog(window, "Quit", "Quit without saving?");
        if (!ync.yesPressed()) {
            return;
        }

        BOA_.running = false;
        boaState.nest = null; // remove from memory
        imageGroup.getOrgIpl().setOverlay(new Overlay()); // clear overlay
        new StackWindow(imageGroup.getOrgIpl());

        window.setImage(new ImagePlus());// remove link to window
        window.close();
    }
}

/**
 * Hold, manipulate and draw on images
 * 
 * @author rtyson
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
    private final Nest nest;
    int w, h, f;

    private static final Logger LOGGER = LogManager.getLogger(ImageGroup.class.getName());

    /**
     * Constructor
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
     * -# Snake after segmentation, without processing by plugins
     * -# Snake after segmentation and after processing by all active plugins
     * 
     * It assign also last created Snake to ViewUpdater. This Snake can be accessed by plugin for
     * previewing purposes. If last Snake has been deleted, \c null is assigned or before last Snake
     *   
     * @param frame Current frame
     */
    public void updateOverlay(int frame) {
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
                if (BOA_.boap.isProcessedSnakePlotted == true) {
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
                if (BOA_.boap.isHeadPlotted == true) {
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
                    FloatPolygon fp1 = GraphicsElements.plotCircle(bp, 10);
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
        // remember current state - image is updated on every change of state (action on plugins,
        // segmentation ui, sliding on frames)

        orgIpl.setOverlay(overlay);

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

    final public void setIpSliceAll(int i) {
        // set slice on all images
        pathsIpl.setSlice(i);
        orgIpl.setSlice(i);
    }

    public void clearPaths(int fromFrame) {
        for (int i = fromFrame; i <= BOA_.boap.FRAMES; i++) {
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

            if (BOA_.boap.HEIGHT > 800) {
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

        for (int i = 1; i <= BOA_.boap.FRAMES; i++) { // copy original
            orgIp = orgStack.getProcessor(i);
            contourIp = contourStack.getProcessor(i);
            contourIp.copyBits(orgIp, 0, 0, Blitter.COPY);
        }

        drawCellRois(contourStack);
        new ImagePlus(orgIpl.getTitle() + "_Segmentation", contourStack).show();

    }

    /**
     * Zoom current view to snake with \c snakeID
     * 
     * If snake is not found nothing happens
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

        sH = nest.getHandler(snakeID);

        if (sH != null && sH.isStoredAt(frame)) {
            snake = sH.getStoredSnake(frame);
        } else {
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
            for (int i = 1; i <= BOA_.boap.FRAMES; i++) {
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
 * Calculate forces that affect the snake
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
                V_temp.setX(n.getNormal().getX() * BOA_.boap.segParam.f_central);
                V_temp.setY(n.getNormal().getY() * BOA_.boap.segParam.f_central);
                n.setF_total(V_temp);

                // compute F_contract
                F_temp = contractionForce(n);
                V_temp.setX(F_temp.getX() * BOA_.boap.segParam.f_contract);
                V_temp.setY(F_temp.getY() * BOA_.boap.segParam.f_contract);
                n.addF_total(V_temp);

                // compute F_image and F_friction
                F_temp = imageForce(n, ip);
                V_temp.setX(F_temp.getX() * BOA_.boap.segParam.f_image);// - n.getVel().getX() *
                // boap.f_friction);
                V_temp.setY(F_temp.getY() * BOA_.boap.segParam.f_image);// - n.getVel().getY() *
                // boap.f_friction);
                n.addF_total(V_temp);

                // compute new velocities of the node
                V_temp.setX(BOA_.boap.delta_t * n.getF_total().getX());
                V_temp.setY(BOA_.boap.delta_t * n.getF_total().getY());
                n.addVel(V_temp);

                // store the prelimanary point to move the node to
                V_temp.setX(BOA_.boap.delta_t * n.getVel().getX());
                V_temp.setY(BOA_.boap.delta_t * n.getVel().getY());
                n.setPrelim(V_temp);

                // add some friction
                n.getVel().multiply(BOA_.boap.f_friction);

                // freeze node if vel is below vel_crit
                if (n.getVel().length() < BOA_.boap.segParam.vel_crit) {
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

        snake.updateNormales(BOA_.boap.segParam.expandSnake);

        return snake.isFrozen(); // true if all nodes frozen
    }

    /**
     * @deprecated Strictly related to absolute paths on disk. Probably for
     * testing purposes only
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
                V_temp.setX(n.getNormal().getX() * BOA_.boap.segParam.f_central);
                V_temp.setY(n.getNormal().getY() * BOA_.boap.segParam.f_central);
                pw.print("\n" + n.getTrackNum() + "," + V_temp.length() + ",");
                n.setF_total(V_temp);

                // compute F_contract
                F_temp = contractionForce(n);
                if (n.getCurvatureLocal() > 0) {
                    pw.print(F_temp.length() + ",");
                } else {
                    pw.print((F_temp.length() * -1) + ",");
                }
                V_temp.setX(F_temp.getX() * BOA_.boap.segParam.f_contract);
                V_temp.setY(F_temp.getY() * BOA_.boap.segParam.f_contract);
                n.addF_total(V_temp);

                // compute F_image and F_friction
                F_temp = imageForce(n, ip);
                pw.print((F_temp.length() * -1) + ",");
                V_temp.setX(F_temp.getX() * BOA_.boap.segParam.f_image);// - n.getVel().getX()*
                // boap.f_friction);
                V_temp.setY(F_temp.getY() * BOA_.boap.segParam.f_image);// - n.getVel().getY()*
                // boap.f_friction);
                n.addF_total(V_temp);
                pw.print(n.getF_total().length() + "");

                // compute new velocities of the node
                V_temp.setX(BOA_.boap.delta_t * n.getF_total().getX());
                V_temp.setY(BOA_.boap.delta_t * n.getF_total().getY());
                n.addVel(V_temp);

                // add some friction
                n.getVel().multiply(BOA_.boap.f_friction);

                // store the prelimanary point to move the node to
                V_temp.setX(BOA_.boap.delta_t * n.getVel().getX());
                V_temp.setY(BOA_.boap.delta_t * n.getVel().getY());
                n.setPrelim(V_temp);

                // freeze node if vel is below vel_crit
                if (n.getVel().length() < BOA_.boap.segParam.vel_crit) {
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

            snake.updateNormales(BOA_.boap.segParam.expandSnake);

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
     * @deprecated Probably old version of contractionForce(Node n)
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

        for (i = 0; i <= 1. / a * BOA_.boap.segParam.sample_tan; ++i) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.boap.segParam.sample_norm / 2; ++j) {
                x = xt + a * j * n.getNormal().getX();
                y = yt + a * j * n.getNormal().getY();

                I_inside += ip.getPixel((int) x, (int) y);
                ++I_in;

                x = xt - a * j * n.getNormal().getX();
                y = yt - a * j * n.getNormal().getY();

                // check that pixel is inside frame
                if (x > 0 && y > 0 && x <= BOA_.boap.WIDTH && y <= BOA_.boap.HEIGHT) {
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
        for (i = 0; i <= 1. / a * BOA_.boap.segParam.sample_norm; ++i) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.boap.segParam.sample_tan / 2; ++j) {
                x = xt + a * j * n.getNormal().getX();
                y = yt + a * j * n.getNormal().getY();

                I_inside += ip.getPixel((int) x, (int) y);
                ++I_in;

                x = xt - a * j * n.getNormal().getX();
                y = yt - a * j * n.getNormal().getY();
                // check that pixel is inside frame
                if (x > 0 && y > 0 && x <= BOA_.boap.WIDTH && y <= BOA_.boap.HEIGHT) {
                    I_outside += ip.getPixel((int) x, (int) y);
                    ++I_out;
                }
            }
        }

        double Delta_I_r = ((double) I_inside / I_in - (double) I_outside / I_out) / 255.;
        System.out.println("Delta_I=" + Delta_I + ", Delta_I_r =" + Delta_I_r);

        if (I_out > BOA_.boap.segParam.sample_norm / 2 * BOA_.boap.segParam.sample_tan) // check
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

        if (check > BOA_.boap.sensitivity) // Delta_I += 0.5 * check;
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
        for (i = 0; i <= 1. / a * BOA_.boap.segParam.sample_tan; i++) {
            // determine points on the tangent
            xt = n.getPoint().getX() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getX();
            yt = n.getPoint().getY() + (a * i - BOA_.boap.segParam.sample_tan / 2) * tan.getY();

            for (j = 0; j <= 1. / a * BOA_.boap.segParam.sample_norm / 2; ++j) {
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
     * Expand all snakes while preventing overlaps. Dead snakes are ignored. Count snakes on frame
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
        double steps = (double) BOA_.boap.segParam.blowup / stepSize;

        for (int i = 0; i < steps; i++) {
            // check for contacts, freeze nodes in contact.
            // Ignore snakes that begin after 'frame'
            for (int si = 0; si < N; si++) {
                snakeA = nest.getHandler(si).getLiveSnake();
                if (!snakeA.alive || frame < nest.getHandler(si).getStartframe()) {
                    continue;
                }
                for (int sj = si + 1; sj < N; sj++) {
                    snakeB = nest.getHandler(sj).getLiveSnake();
                    if (!snakeB.alive || frame < nest.getHandler(si).getStartframe()) {
                        continue;
                    }
                    if (prox[si][sj] > BOA_.boap.proximity) {
                        continue;
                    }
                    freezeProx(snakeA, snakeB);
                }

            }

            // scale up all snakes by one step (if node not frozen, or dead)
            // unless they start at this frame or after
            for (int s = 0; s < N; s++) {
                snakeA = nest.getHandler(s).getLiveSnake();
                if (snakeA.alive && frame > nest.getHandler(s).getStartframe()) {
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
                if (prox < BOA_.boap.proxFreeze) {
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
            if (snake.alive && f > sH.getStartframe()) {
                snake.implode();
            }
        }
    }
}

/**
 * Represents collection of Snakes
 * 
 * @author rtyson
 * @author p.baniukiewicz
 * @date 4 May 2016
 */
class Nest implements IQuimpSerialize {
    private ArrayList<SnakeHandler> sHs;
    private int NSNAKES; //!< Number of stored snakes in nest
    private int ALIVE;
    private int nextID; // handler ID's

    public Nest() {
        NSNAKES = 0;
        ALIVE = 0;
        nextID = 0;
        sHs = new ArrayList<SnakeHandler>();
    }

    public void addHandlers(Roi[] roiArray, int startFrame) {
        int i = 0;
        for (; i < roiArray.length; i++) {
            try {
                sHs.add(new SnakeHandler(roiArray[i], startFrame, nextID));
                nextID++;
                NSNAKES++;
                ALIVE++;
            } catch (Exception e) {
                BOA_.log("A snake failed to initilise");
            }
        }
        BOA_.log("Added " + roiArray.length + " cells at frame " + startFrame);
        BOA_.log("Cells being tracked: " + NSNAKES);
    }

    /**
     * Add ROI objects in Nest Snakes are stored in Nest object in form of
     * SnakeHandler objects kept in \c ArrayList<SnakeHandler> \c sHs field.
     * 
     * @param r ROI object that contain image object to be segmented
     * @param startFrame Current frame
     * @return SnakeHandler object that is also stored in Nest
     */
    public SnakeHandler addHandler(final Roi r, int startFrame) {
        SnakeHandler sH;
        try {
            sH = new SnakeHandler(r, startFrame, nextID);
            sHs.add(sH);
            nextID++;
            NSNAKES++;
            ALIVE++;
            BOA_.log("Added one cell, begining frame " + startFrame);
        } catch (Exception e) {
            BOA_.log("Added cell failed to initilise");
            return null;
        }
        BOA_.log("Cells being tracked: " + NSNAKES);
        return sH;
    }

    public SnakeHandler getHandler(int s) {
        return sHs.get(s);
    }

    /**
     * Write all Snakes to file.
     * 
     * File names are deducted in called functions.
     * 
     * @return \c true if write operation has been successful
     * @throws IOException when the file exists but is a directory rather than a regular file, 
     * does not exist but cannot be created, or cannot be opened for any other reason
     */
    public boolean writeSnakes() throws IOException {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.setEndFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartframe() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                removeHandler(sH);
                continue;
            }
            if (!sH.writeSnakes()) {
                return false;
            }
        }
        return true;
    }

    public void kill(final SnakeHandler sH) {
        sH.kill();
        ALIVE--;
    }

    public void reviveNest() {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            sH.revive();
        }
        ALIVE = NSNAKES;
    }

    public boolean isVacant() {
        if (NSNAKES == 0) {
            return true;
        }
        return false;
    }

    public boolean allDead() {
        if (ALIVE == 0 || NSNAKES == 0) {
            return true;
        }
        return false;
    }

    /**
     * Write \a stQP file using current Snakes
     * 
     * @param oi instance of current ImagePlus (required by CellStat that extends 
     * ij.measure.Measurements
     */
    public void analyse(final ImagePlus oi) {
        OutlineHandler outputH;
        SnakeHandler sH;
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next();

            File pFile = new File(BOA_.boap.outFile.getParent(),
                    BOA_.boap.fileName + "_" + sH.getID() + ".paQP");
            QParams newQp = new QParams(pFile);
            newQp.readParams();
            outputH = new OutlineHandler(newQp);

            File statsFile = new File(BOA_.boap.outFile.getParent() + File.separator
                    + BOA_.boap.fileName + "_" + sH.getID() + ".stQP.csv");
            new CellStat(outputH, oi, statsFile, BOA_.boap.imageScale,
                    BOA_.boap.imageFrameInterval);
        }
    }

    public void resetNest() {
        // Rset live snakes to ROI's
        reviveNest();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                sH.reset();
            } catch (Exception e) {
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removeing snake " + sH.getID());
                removeHandler(sH);
            }
        }
    }

    public void removeHandler(final SnakeHandler sH) {
        if (sH.isLive()) {
            ALIVE--;
        }
        sHs.remove(sH);
        NSNAKES--;
    }

    int size() {
        return NSNAKES;
    }

    /**
     * Prepare for segmentation from frame \c f
     * 
     * @param f current frame under segmentation
     */
    void resetForFrame(int f) {
        reviveNest();
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        // BOA_.log("Reseting for frame " + f);
        while (sHitr.hasNext()) {
            SnakeHandler sH = (SnakeHandler) sHitr.next();
            try {
                if (f <= sH.getStartframe()) {
                    // BOA_.log("Reset snake " + sH.getID() + " as Roi");
                    sH.reset();
                } else {
                    // BOA_.log("Reset snake " + sH.getID() + " as prev snake");
                    sH.resetForFrame(f);
                }
            } catch (Exception e) {
                BOA_.log("Could not reset snake " + sH.getID());
                BOA_.log("Removeing snake " + sH.getID());
                removeHandler(sH);
            }
        }
    }

    /**
     * Get list of snakes that are on frame  \c frame
     * 
     * @param frame Frame find snakes in
     * @return List of Snake id on \c frame
     */
    List<Integer> getSnakesforFrame(int frame) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        Iterator<SnakeHandler> sHiter = sHs.iterator();
        while (sHiter.hasNext()) { // over whole nest
            SnakeHandler sH = sHiter.next(); // for every SnakeHandler
            if (sH.getStartframe() > frame || sH.getEndFrame() < frame) // check its limits
                continue; // no snake in frame
            if (sH.isStoredAt(frame)) { // if limits are ok check if this particular snake exist
                // it is not deleted by user on this particular frame after successful creating as
                // series of Snakes
                Snake s = sH.getStoredSnake(frame);
                ret.add(s.getSnakeID()); // if yes get its id
            }
        }
        return ret;
    }

    /**
     * Count the snakes that exist at, or after, frame
     * 
     * @param frame
     * @return
     */
    int nbSnakesAt(int frame) {
        int n = 0;
        for (int i = 0; i < NSNAKES; i++) {
            if (sHs.get(i).getStartframe() >= frame) {
                n++;
            }
        }
        return n;
    }

    void addOutlinehandler(final OutlineHandler oH) {
        SnakeHandler sH = addHandler(oH.indexGetOutline(0).asFloatRoi(), oH.getStartFrame());

        Outline o;
        for (int i = oH.getStartFrame(); i <= oH.getEndFrame(); i++) {
            o = oH.getOutline(i);
            sH.storeRoi((PolygonRoi) o.asFloatRoi(), i);
        }
    }

    @Override
    public void beforeSerialize() {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.setEndFrame(); // find its last frame (frame with valid contour)
            if (sH.getStartframe() > sH.getEndFrame()) {
                IJ.error("Snake " + sH.getID() + " not written as its empty. Deleting it.");
                removeHandler(sH);
                continue;
            }
            sH.beforeSerialize();
        }
    }

    @Override
    public void afterSerialize() throws Exception {
        Iterator<SnakeHandler> sHitr = sHs.iterator();
        SnakeHandler sH;
        while (sHitr.hasNext()) {
            sH = (SnakeHandler) sHitr.next(); // get SnakeHandler from Nest
            sH.afterSerialize();
        }

    }
}

/**
 * Holds parameters defining snake and controlling contour matching algorithm.
 * BOAp is static class contains internal as well as external parameters used to
 * define snake and to control contour matching algorithm. There are also
 * several basic get/set methods for accessing selected parameters, setting
 * default {@link uk.ac.warwick.wsbc.QuimP.BOAp.SegParam.setDefaults() values} 
 * and writing/reading these (external) parameters to/from disk. File format used for
 * storing data in files is defined at {@link QParams} class.
 * 
 * External parameters are those related to algorithm options whereas internal
 * are those related to internal settings of algorithm, GUI and whole plugin
 * 
 * This class is shared among different QuimP components
 * 
 * @author rtyson
 * @see QParams
 * @see Tool
 * @warning This class will be redesigned in future 
 */
class BOAp {

    File orgFile; //!< handle to original file obtained from IJ (usually image opened) 
    File outFile; //!< handle to \a snPQ filled in QuimP.SnakeHandler.writeSnakes() 
    String fileName; //!< loaded image file name only, no extension (\c orgFile)
    QParams readQp; //!< read in parameter file 
    public SegParam segParam; //!< Parameters of segmentation available for user (GUI)
    // internal parameters
    int NMAX; //!< maximum number of nodes (% of starting nodes) 
    double delta_t;
    double sensitivity;
    double f_friction;
    int FRAMES; //!< Number of frames in stack 
    int WIDTH, HEIGHT;
    int cut_every; //!< cut loops in chain every X frames 
    boolean oldFormat; //!< output old QuimP format? 
    boolean saveSnake; //!< save snake data 
    private double min_dist; //!< min distance between nodes 
    private double max_dist; //!< max distance between nodes 
    double proximity; //!< distance between centroids at which contact is tested for 
    double proxFreeze; //!< proximity of nodes to freeze when blowing up 
    boolean savedOne;
    double imageScale; //!< scale of image in 
    double imageFrameInterval;
    boolean scaleAdjusted;
    boolean fIAdjusted;
    boolean singleImage;
    String paramsExist; // on startup check if defaults are needed to set
    boolean zoom;
    boolean doDelete;
    boolean doDeleteSeg;
    boolean editMode; //!< is select a cell for editing active? 
    int editingID; //!< currently editing cell iD. -1 if not editing
    boolean useSubPixel = true;
    boolean supressStateChangeBOArun = false;
    int callCount; //<! use to test how many times a method is called
    boolean SEGrunning; //!< is segmentation running 

    /**
     * Hold user parameters for segmentation algorithm
     * 
     * @author p.baniukiewicz
     * @date 30 Mar 2016
     * @see BOAState
     */
    class SegParam {
        private double nodeRes; //!< Number of nodes on ROI edge 
        int blowup; //!< distance to blow up chain 
        double vel_crit;
        double f_central;
        double f_image; //!< image force 
        int max_iterations; //!< max iterations per contraction 
        int sample_tan;
        int sample_norm;
        double f_contract;
        double finalShrink;
        // Switch Params
        boolean use_previous_snake;//!< next contraction begins with prev chain 
        boolean showPaths;
        boolean expandSnake; //!< whether to act as an expanding snake 

        /**
         * Copy constructor
         * 
         * @param src object to copy
         */
        public SegParam(final SegParam src) {
            this.nodeRes = src.nodeRes;
            this.blowup = src.blowup;
            this.vel_crit = src.vel_crit;
            this.f_central = src.f_central;
            this.f_image = src.f_image;
            this.max_iterations = src.max_iterations;
            this.sample_tan = src.sample_tan;
            this.sample_norm = src.sample_norm;
            this.f_contract = src.f_contract;
            this.finalShrink = src.finalShrink;
            this.use_previous_snake = src.use_previous_snake;
            this.showPaths = src.showPaths;
            this.expandSnake = src.expandSnake;
        }

        /**
         * Sets default values of parameters
         */
        public SegParam() {
            setDefaults();
            // defaults for GUI settings
            showPaths = false;
            use_previous_snake = true; // next contraction begins with last chain
            expandSnake = false; // set true to act as an expanding snake

        }

        /**
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + blowup;
            result = prime * result + (expandSnake ? 1231 : 1237);
            long temp;
            temp = Double.doubleToLongBits(f_central);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(f_contract);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(f_image);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(finalShrink);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + max_iterations;
            temp = Double.doubleToLongBits(nodeRes);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + sample_norm;
            result = prime * result + sample_tan;
            result = prime * result + (showPaths ? 1231 : 1237);
            result = prime * result + (use_previous_snake ? 1231 : 1237);
            temp = Double.doubleToLongBits(vel_crit);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        /**
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof SegParam))
                return false;
            SegParam other = (SegParam) obj;
            if (blowup != other.blowup)
                return false;
            if (expandSnake != other.expandSnake)
                return false;
            if (Double.doubleToLongBits(f_central) != Double.doubleToLongBits(other.f_central))
                return false;
            if (Double.doubleToLongBits(f_contract) != Double.doubleToLongBits(other.f_contract))
                return false;
            if (Double.doubleToLongBits(f_image) != Double.doubleToLongBits(other.f_image))
                return false;
            if (Double.doubleToLongBits(finalShrink) != Double.doubleToLongBits(other.finalShrink))
                return false;
            if (max_iterations != other.max_iterations)
                return false;
            if (Double.doubleToLongBits(nodeRes) != Double.doubleToLongBits(other.nodeRes))
                return false;
            if (sample_norm != other.sample_norm)
                return false;
            if (sample_tan != other.sample_tan)
                return false;
            if (showPaths != other.showPaths)
                return false;
            if (use_previous_snake != other.use_previous_snake)
                return false;
            if (Double.doubleToLongBits(vel_crit) != Double.doubleToLongBits(other.vel_crit))
                return false;
            return true;
        }

        /**
         * Return nodeRes
         * 
         * @return nodeRes field
         */
        public double getNodeRes() {
            return nodeRes;
        }

        /**
         * Set \c nodeRes field and calculate \c min_dist and \c max_dist
         * 
         * @param d
         */
        public void setNodeRes(double d) {
            nodeRes = d;
            if (nodeRes < 1) {
                min_dist = 1; // min distance between nodes
                max_dist = 2.3; // max distance between nodes
                return;
            }
            min_dist = nodeRes; // min distance between nodes
            max_dist = nodeRes * 1.9; // max distance between nodes
        }

        /**
         * Set default parameters for contour matching algorithm.
         * 
         * These parameters are external - available for user to set in GUI.
         */
        public void setDefaults() {
            setNodeRes(6.0);
            blowup = 20; // distance to blow up chain
            vel_crit = 0.005;
            f_central = 0.04;
            f_image = 0.2; // image force
            max_iterations = 4000; // max iterations per contraction
            sample_tan = 4;
            sample_norm = 12;
            f_contract = 0.04;
            finalShrink = 3d;
        }
    } // end of SegParam

    /**
     * Default constructor
     */
    public BOAp() {
        segParam = new SegParam(); // build segmentation parameters object with default values
    }

    /**
     * Plot or not snakes after processing by plugins. If \c yes both snakes, after 
     * segmentation and after filtering are plotted.
     */
    boolean isProcessedSnakePlotted = true;

    /**
     * Define if first node of Snake (head) is plotted or not
     */
    boolean isHeadPlotted = false;

    /**
     * When any plugin fails this field defines how QuimP should behave. When
     * it is \c true QuimP breaks process of segmentation and do not store
     * filtered snake in SnakeHandler
     * @warning Currently not used
     * @todo TODO Implement this feature
     * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/81
     */
    boolean stopOnPluginError = true;

    public double getMax_dist() {
        return max_dist;
    }

    public double getMin_dist() {
        return min_dist;
    }

    /**
     * Initialize internal parameters of BOA plugin
     * 
     * Most of these parameters are related to state machine of BOA. There are
     * also parameters related to internal state of Active Contour algorithm.
     * Defaults for parameters available for user are set in
     * {@link uk.ac.warwick.wsbc.QuimP.BOAp.SegParam.setDefaults()}
     * 
     * @param ip Reference to segmented image passed from IJ
     * @see setDefaults()
     */
    public void setup(final ImagePlus ip) {
        FileInfo fileinfo = ip.getOriginalFileInfo();
        if (fileinfo == null) {
            // System.out.println("1671-No file Info, use " +
            // orgIpl.getTitle());
            orgFile = new File(File.separator, ip.getTitle());
        } else {
            // System.out.println("1671-file Info, filename: " +
            // fileinfo.fileName);
            orgFile = new File(fileinfo.directory, fileinfo.fileName);
        }
        fileName = Tool.removeExtension(orgFile.getName());

        FRAMES = ip.getStackSize(); // get number of frames
        imageFrameInterval = ip.getCalibration().frameInterval;
        imageScale = ip.getCalibration().pixelWidth;
        scaleAdjusted = false;
        fIAdjusted = false;
        if (imageFrameInterval == 0) {
            imageFrameInterval = 1;
            fIAdjusted = true;
            // BOA_.log("Warning. Frame interval was 0 sec. Using 1 sec instead"
            // + "\n\t[set in 'image->Properties...']");
        }
        if (imageScale == 0) {
            imageScale = 1;
            scaleAdjusted = true;
            // BOA_.log("Warning. Scale was 1 pixel == 0 \u00B5m. Using 1
            // \u00B5m instead"
            // + "\n\t(set in 'Analyze->Set Scale...')");
        }

        savedOne = false;
        // nestSize = 0;
        WIDTH = ip.getWidth();
        HEIGHT = ip.getHeight();
        paramsExist = "YES";

        // internal parameters
        NMAX = 250; // maximum number of nodes (% of starting nodes)
        delta_t = 1.;
        sensitivity = 0.5;
        cut_every = 8; // cut loops in chain every X interations
        oldFormat = false; // output old QuimP format?
        saveSnake = true; // save snake data
        proximity = 150; // distance between centroids at
                         // which contact is tested for
        proxFreeze = 1; // proximity of nodes to freeze when blowing up
        f_friction = 0.6;
        doDelete = false;
        doDeleteSeg = false;
        zoom = false;
        editMode = false;
        editingID = -1;
        callCount = 0;
        SEGrunning = false;

    }

    /**
     * Write set of snake parameters to disk.
     * 
     * writeParams method creates \a paQP master file, referencing other
     * associated files and \a csv file with statistics.
     * 
     * @param sID ID of cell. If many cells segmented in one time, QuimP
     * produces separate parameter file for every of them
     * @param startF Start frame (typically beginning of stack)
     * @param endF End frame (typically end of stack)
     * @see QParams
     */
    public void writeParams(int sID, int startF, int endF) {
        if (saveSnake) {
            File paramFile = new File(outFile.getParent(), fileName + "_" + sID + ".paQP");
            File statsFile = new File(
                    outFile.getParent() + File.separator + fileName + "_" + sID + ".stQP.csv");

            QParams qp = new QParams(paramFile);
            qp.segImageFile = orgFile;
            qp.snakeQP = outFile;
            qp.statsQP = statsFile;
            qp.imageScale = imageScale;
            qp.frameInterval = imageFrameInterval;
            qp.startFrame = startF;
            qp.endFrame = endF;
            qp.NMAX = NMAX;
            qp.blowup = segParam.blowup;
            qp.max_iterations = segParam.max_iterations;
            qp.sample_tan = segParam.sample_tan;
            qp.sample_norm = segParam.sample_norm;
            qp.delta_t = delta_t;
            qp.nodeRes = segParam.nodeRes;
            qp.vel_crit = segParam.vel_crit;
            qp.f_central = segParam.f_central;
            qp.f_contract = segParam.f_contract;
            qp.f_image = segParam.f_image;
            qp.f_friction = f_friction;
            qp.finalShrink = segParam.finalShrink;
            qp.sensitivity = sensitivity;

            qp.writeParams();
        }
    }

    /**
     * Read set of snake parameters from disk.
     * 
     * readParams method reads \a paQP master file, referencing other associated
     * files.
     * 
     * @return Status of operation
     * @retval true when file has been loaded successfully
     * @retval false when file has not been opened correctly or
     * QParams.readParams() returned \c false
     * @see QParams
     */
    public boolean readParams() {
        OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", "");
        if (od.getFileName() == null) {
            return false;
        }
        readQp = new QParams(new File(od.getDirectory(), od.getFileName()));

        if (!readQp.readParams()) {
            BOA_.log("Failed to read parameter file");
            return false;
        }
        NMAX = readQp.NMAX;
        segParam.blowup = readQp.blowup;
        segParam.max_iterations = readQp.max_iterations;
        segParam.sample_tan = readQp.sample_tan;
        segParam.sample_norm = readQp.sample_norm;
        delta_t = readQp.delta_t;
        segParam.nodeRes = readQp.nodeRes;
        segParam.vel_crit = readQp.vel_crit;
        segParam.f_central = readQp.f_central;
        segParam.f_contract = readQp.f_contract;
        segParam.f_image = readQp.f_image;

        if (readQp.newFormat) {
            segParam.finalShrink = readQp.finalShrink;
        }
        BOA_.log("Successfully read parameters");
        return true;
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
class BoaException extends Exception {

    private static final long serialVersionUID = 1L;
    private int frame;
    private int type;

    public BoaException(String msg, int f, int t) {
        super(msg);
        frame = f;
        type = t;
    }

    public int getFrame() {
        return frame;
    }

    public int getType() {
        return type;
    }
}
