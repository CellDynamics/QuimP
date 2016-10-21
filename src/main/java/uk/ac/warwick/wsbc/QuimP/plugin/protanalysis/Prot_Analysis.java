package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.plugin.filter.Analyzer;
import uk.ac.warwick.wsbc.QuimP.PropertyReader;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig.gradientType;
import uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig.outlinePlotTypes;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.registration.Registration;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;
import uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot;

/**
 * Main class for Protrusion Analysis module.
 * 
 * Contain business logic for protrusion analysis. The UI is built by
 * {@link uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.Prot_AnalysisUI}. The communication between
 * these modules is through {@link uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig}
 * 
 * !<
 * @startuml
 * Prot_Analysis *-- "1" ProtAnalysisConfig
 * Prot_Analysis *-- "1" Prot_AnalysisUI
 * Prot_AnalysisUI o-- "1" ProtAnalysisConfig
 * @enduml
 * !>
 * @author p.baniukiewicz
 */
public class Prot_Analysis implements IQuimpPlugin {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Prot_Analysis.class.getName());

    /**
     * Loaded QCONF file.
     * 
     * Initialised by {@link #loadFile(File)} through this constructor.
     */
    QconfLoader qconfLoader = null; // main object representing loaded configuration file
    private boolean uiCancelled = false;
    /**
     * Instance of module UI.
     * 
     * Initialised by this constructor.
     */
    public Prot_AnalysisUI gui;
    // default configuration parameters, for future using
    ParamList paramList = new ParamList();
    /**
     * Keep overall configuration.
     * 
     * This object is filled in GUI and passed to runPlugin, where it is read out. Initialised by
     * this constructor.
     */
    ProtAnalysisConfig config;

    /**
     * Instance of ResultTable.
     * 
     * Initialised by this constructor, filled by {@link #runFromQCONF()}, shown by
     * {@link Prot_AnalysisUI#actionPerformed(ActionEvent)}
     */
    ResultsTable rt;

    /**
     * Default constructor.
     * 
     * Run parameterised constructor with <tt>null</tt> showing file selector.
     */
    public Prot_Analysis() {
        this(null);
    }

    /**
     * Constructor that allows to provide own file.
     * 
     * @param paramFile File to process.
     */
    public Prot_Analysis(File paramFile) {
        IJ.log(new QuimpToolsCollection().getQuimPversion());
        config = new ProtAnalysisConfig();
        gui = new Prot_AnalysisUI(this);
        rt = createCellResultTable();
        // validate registered user
        new Registration(IJ.getInstance(), "QuimP Registration");
        // check whether config file name is provided or ask user for it
        try {
            IJ.showStatus("Protrusion Analysis");
            loadFile(paramFile); // load configuration file given by paramFile and verify it
            if (qconfLoader.getQp() == null)
                return; // not loaded
            gui.writeUI(); // set ui
            showUI(true); // show it and wait for user action. Plugin is run from Apply button
            if (uiCancelled)
                return;
        } catch (Exception e) { // catch all exceptions here
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with run of Protrusion Analysis mapping: " + e.getMessage());
        }
    }

    /**
     * Load configuration file. (only if not loaded before).
     * 
     * Validates also all necessary datafields in loaded QCONF file. Set <tt>qconfLoader</tt> field
     * on success or set it to <tt>null</tt>.
     * 
     * @throws QuimpException When QCONF could not be loaded or it does not meet requirements.
     */
    private void loadFile(File paramFile) throws QuimpException {
        if (qconfLoader == null || qconfLoader.getQp() == null) {
            // load new file
            qconfLoader = new QconfLoader(paramFile,
                    new QuimpConfigFilefilter(QuimpConfigFilefilter.newFileExt));
            if (qconfLoader.getQp() == null)
                return; // not loaded
            if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
                // validate in case new format
                qconfLoader.getBOA(); // will throw exception if not present
                qconfLoader.getECMM();
                qconfLoader.getQ();
            } else {
                qconfLoader = null; // failed load or checking
                throw new QuimpException("QconfLoader returned unsupported version of QuimP."
                        + " Only new format can be loaded");
            }
        }
    }

    /**
     * Main runner.
     * 
     * Keeps logic of ECMM, ANA, Q plugins.
     * 
     * This method reads entries in
     * {@link uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig} and performs selected
     * actions. Additionally it collects all results for all cells in one common table.
     * 
     * @throws QuimpException
     * @throws IOException
     */
    private void runFromQCONF() throws QuimpException, IOException {
        STmap[] stMap = qconfLoader.getQp().getLoadedDataContainer().getQState();
        OutlinesCollection oHs = qconfLoader.getQp().getLoadedDataContainer().getECMMState();
        TrackVisualisation.Stack visStackDynamic = null;
        TrackVisualisation.Image visStackStatic = null;
        TrackVisualisation.Stack visStackOutline = null;
        int h = 0;

        ImagePlus im1static = qconfLoader.getImage();
        if (im1static == null)
            return; // stop if no image

        // dynamic stack
        if (config.plotDynamicmax) {
            visStackDynamic = new TrackVisualisation.Stack(im1static.duplicate());
            visStackDynamic.getOriginalImage().setTitle("Dynamic tracking");
        }

        // static plot - all maxima on stack or flatten stack
        if (config.plotStaticmax) {
            visStackStatic = new TrackVisualisation.Image(im1static.duplicate());
            visStackStatic.getOriginalImage().setTitle("Static points");
            if (config.staticPlot.averimage)
                visStackStatic.flatten(ZProjector.AVG_METHOD, false);
        }

        // outlines plot
        if (config.plotOutline) {
            visStackOutline = new TrackVisualisation.Stack(im1static.duplicate());
            visStackOutline.getOriginalImage().setTitle("Outlines");
        }

        TrackMapAnalyser pT = new TrackMapAnalyser();
        LOGGER.trace("Cells in database: " + stMap.length);
        for (STmap mapCell : stMap) { // iterate through cells
            // convert binary 2D array to ImageJ
            TrackVisualisation.Map visSingle = new TrackVisualisation.Map("motility_map_cell_" + h,
                    QuimPArrayUtils.double2float(mapCell.motMap));
            // compute maxima
            MaximaFinder mF = new MaximaFinder(visSingle.getOriginalImage().getProcessor());
            mF.computeMaximaIJ(config.noiseTolerance); // 1.5
            // track maxima across motility map
            pT.trackMaxima(mapCell, config.dropValue, mF);
            TrackCollection trackCollection = pT.getTrackCollection();

            // plot motility map with maxima nad tracking lines
            if (config.plotMotmapmax) {
                visSingle.addMaximaToImage(mF);
                visSingle.addTrackingLinesToImage(trackCollection);
                visSingle.getOriginalImage().show();
            }
            // plot motility map only
            if (config.plotMotmap) {
                ImagePlus mm =
                        mapCell.map2ColorImagePlus("motility_map", "rwb", mapCell.getMotMap(),
                                oHs.oHs.get(h).migLimits[0], oHs.oHs.get(h).migLimits[1]);
                mm.setTitle("MotilityMap_cell_" + h);
                mm.show();
            }
            // plot convexity map only
            if (config.plotConmap) {
                ImagePlus mm =
                        mapCell.map2ColorImagePlus("convexity_map", "rbb", mapCell.getConvMap(),
                                oHs.oHs.get(h).curvLimits[0], oHs.oHs.get(h).curvLimits[1]);
                mm.setTitle("ConvexityMap_cell_" + h);
                mm.show();
            }

            // plot static lines/or maxi
            if (config.plotStaticmax && config.staticPlot.plotmax && config.staticPlot.plottrack)
                visStackStatic.addElementsToImage(mapCell, trackCollection, mF);
            else if (config.plotStaticmax && config.staticPlot.plotmax
                    && config.staticPlot.plottrack == false)
                visStackStatic.addElementsToImage(mapCell, null, mF);
            else if (config.plotStaticmax && config.staticPlot.plotmax == false
                    && config.staticPlot.plottrack)
                visStackStatic.addElementsToImage(mapCell, trackCollection, null);

            // plot dynamic stack
            if (config.plotDynamicmax) {
                if (config.dynamicPlot.plotmax)
                    visStackDynamic.addMaximaToImage(mapCell, mF);
                if (config.dynamicPlot.plottrack)
                    visStackDynamic.addTrackingLinesToImage(mapCell, trackCollection);
            }

            if (config.plotOutline) {
                visStackOutline.addOutlinesToImage(mapCell, config);
            }

            // write svg plots
            if (config.polarPlot.plotpolar && config.polarPlot.useGradient) {
                PolarPlot pp = new PolarPlot(mapCell, config.polarPlot.gradientPoint);
                pp.generatePlot(Paths.get(qconfLoader.getQp().getPath(),
                        qconfLoader.getQp().getFileName() + "_" + h + config.polarPlotSuffix)
                        .toString());
            }
            // write stats, and add to table
            writeStats(h, mapCell, mF, trackCollection).cellStatistics.addCellToCellTable(rt);

            // update static fields in gui
            gui.labelMaxnum.setText(Integer.toString(mF.getMaximaNumber()));
            gui.labelMaxval.setText(
                    String.format("%1$.3f", QuimPArrayUtils.arrayMax(mapCell.getMotMap())));
            gui.labelMinval.setText(
                    String.format("%1$.3f", QuimPArrayUtils.arrayMin(mapCell.getMotMap())));
            h++;
        }

        if (config.plotStaticmax)
            visStackStatic.getOriginalImage().show();
        if (config.plotDynamicmax)
            visStackDynamic.getOriginalImage().show();
        if (config.plotOutline)
            visStackOutline.getOriginalImage().show();
    }

    /**
     * Write cell statistic and protrusion statistics to files.
     * 
     * @param h Cell number
     * @param mapCell
     * @param mF
     * @param trackCollection
     * @return ProtStat instance of object that keeps cell statistics. Can be used to form e.g.
     *         table with results.
     * 
     * @throws FileNotFoundException
     */
    private ProtStat writeStats(int h, STmap mapCell, MaximaFinder mF,
            TrackCollection trackCollection) throws FileNotFoundException {
        // Maps are correlated in order with Outlines in DataContainer.
        // write data
        PrintWriter cellStatFile = new PrintWriter(Paths
                .get(qconfLoader.getQp().getPath(),
                        qconfLoader.getQp().getFileName() + "_" + h + config.cellStatSuffix)
                .toFile());
        PrintWriter protStatFile = new PrintWriter(Paths
                .get(qconfLoader.getQp().getPath(),
                        qconfLoader.getQp().getFileName() + "_" + h + config.protStatSuffix)
                .toFile());
        new ProtStat(mF, trackCollection,
                qconfLoader.getQp().getLoadedDataContainer().getStats().sHs.get(h), mapCell)
                        .writeProtrusion(protStatFile, h);

        ProtStat cellStat = new ProtStat(mF, trackCollection,
                qconfLoader.getQp().getLoadedDataContainer().getStats().sHs.get(h), mapCell);

        cellStat.writeCell(cellStatFile, h);
        protStatFile.close();
        cellStatFile.close();
        return cellStat;
    }

    @Override
    public int setup() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        paramList = new ParamList(par);
        // TODO restore config from json

    }

    @Override
    public ParamList getPluginConfig() {
        // TODO convert config to json one liner and add to paramlist
        // paramList.put("config", json)
        return paramList;
    }

    @Override
    public void showUI(boolean val) {
        gui.showUI(val);
    }

    @Override
    public String getVersion() {
        return "See QuimP version";
    }

    @Override
    public String about() {
        return "Protrusion Analysis Plugin.\n" + "Author: Piotr Baniukiewicz\n"
                + "mail: p.baniukiewicz@warwick.ac.uk";
    }

    @Override
    public void runPlugin() throws QuimpPluginException {
        try {
            IJ.showStatus("Protrusion Analysis");
            runFromQCONF();
            IJ.log("Protrusion Analysis complete");
            IJ.showStatus("Finished");
        } catch (Exception e) { // catch all here and convert to expected type
            throw new QuimpPluginException(e);
        }
    }

    /**
     * Build cell statistic result table.
     * 
     * It contains statistics for all cells.
     * 
     * @return Handle to ResultTable that can be displayed by show("Name"_ method.
     */
    public ResultsTable createCellResultTable() {
        ResultsTable rt = new ResultsTable();
        Analyzer.setResultsTable(rt);
        return rt;
    }
}

/**
 * Build GUI for plugin.
 * 
 * @author p.baniukiewicz
 *
 */
class Prot_AnalysisUI implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(Prot_AnalysisUI.class.getName());
    // UI elements
    private JFrame wnd;
    private JButton bCancel, bApply, bHelp, bGradient;
    private JFormattedTextField f_noiseTolerance, f_dropValue, f_motThreshold, f_convThreshold;
    private JComboBox<ProtAnalysisConfig.outlinePlotTypes> o_plotType;
    private JCheckBox c_plotMotmap, c_plotMotmapmax, c_plotConmap, c_plotOutline, c_plotStaticmax,
            c_plotDynamicmax;

    JLabel labelMaxnum, labelMaxval, labelMinval, labelGradinet;

    private JCheckBox c_staticPlotmax, c_staticPlottrack, c_staticAverimage, c_plotPolarplot;
    private JCheckBox c_dynamicPlotmax, c_dynamicPlottrack, c_useGradient;

    private Prot_Analysis model; // main model with method to run on ui action

    public Prot_AnalysisUI(Prot_Analysis model) {
        this.model = model;
        buildUI();
    }

    /**
     * Copy UI settings to {@link uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig}
     * object.
     */
    public void readUI() {
        model.config.noiseTolerance = ((Number) f_noiseTolerance.getValue()).doubleValue();
        model.config.dropValue = ((Number) f_dropValue.getValue()).doubleValue();

        model.config.plotOutline = c_plotOutline.isSelected();
        model.config.outlinesToImage.motThreshold =
                ((Number) f_motThreshold.getValue()).doubleValue();
        model.config.outlinesToImage.convThreshold =
                ((Number) f_convThreshold.getValue()).doubleValue();
        model.config.outlinesToImage.plotType = (outlinePlotTypes) o_plotType.getSelectedItem();

        model.config.plotMotmap = c_plotMotmap.isSelected();
        model.config.plotMotmapmax = c_plotMotmapmax.isSelected();
        model.config.plotConmap = c_plotConmap.isSelected();

        model.config.plotStaticmax = c_plotStaticmax.isSelected();
        model.config.staticPlot.plotmax = c_staticPlotmax.isSelected();
        model.config.staticPlot.plottrack = c_staticPlottrack.isSelected();
        model.config.staticPlot.averimage = c_staticAverimage.isSelected();

        model.config.plotDynamicmax = c_plotDynamicmax.isSelected();
        model.config.dynamicPlot.plotmax = c_dynamicPlotmax.isSelected();
        model.config.dynamicPlot.plottrack = c_dynamicPlottrack.isSelected();

        model.config.polarPlot.plotpolar = c_plotPolarplot.isSelected();
        model.config.polarPlot.useGradient = c_useGradient.isSelected();

    }

    /**
     * Copy {@link uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig} settings to UI.
     */
    public void writeUI() {
        f_noiseTolerance.setValue(new Double(model.config.noiseTolerance));
        f_dropValue.setValue(new Double(model.config.dropValue));

        c_plotOutline.setSelected(model.config.plotOutline);
        f_motThreshold.setValue(new Double(model.config.outlinesToImage.motThreshold));
        f_convThreshold.setValue(new Double(model.config.outlinesToImage.convThreshold));
        o_plotType.setSelectedItem(model.config.outlinesToImage.plotType);

        c_plotMotmap.setSelected(model.config.plotMotmap);
        c_plotMotmapmax.setSelected(model.config.plotMotmapmax);
        c_plotConmap.setSelected(model.config.plotConmap);

        c_plotStaticmax.setSelected(model.config.plotStaticmax);
        c_staticPlotmax.setSelected(model.config.staticPlot.plotmax);
        c_staticPlottrack.setSelected(model.config.staticPlot.plottrack);
        c_staticAverimage.setSelected(model.config.staticPlot.averimage);

        c_plotDynamicmax.setSelected(model.config.plotDynamicmax);
        c_dynamicPlotmax.setSelected(model.config.dynamicPlot.plotmax);
        c_dynamicPlottrack.setSelected(model.config.dynamicPlot.plottrack);

        c_plotPolarplot.setSelected(model.config.polarPlot.plotpolar);
        c_useGradient.setSelected(model.config.polarPlot.useGradient);
        String g;
        switch (model.config.polarPlot.type) {
            case OUTLINEPOINT:
                g = "Not implemented";
                c_useGradient.setSelected(true);
                break;
            case SCREENPOINT:
                g = "x=" + model.config.polarPlot.gradientPoint.getX() + " y="
                        + model.config.polarPlot.gradientPoint.getY();
                c_useGradient.setSelected(true);
                break;
            default:
                g = "";
                c_useGradient.setSelected(false);
        }
        labelGradinet.setText(g);
    }

    /**
     * Show UI.
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
            f_dropValue = new JFormattedTextField(NumberFormat.getInstance());
            f_dropValue.setColumns(0);
            f_dropValue.setPreferredSize(new Dimension(80, 26));
            f_noiseTolerance = new JFormattedTextField(NumberFormat.getInstance());
            f_noiseTolerance.setColumns(0);
            f_noiseTolerance.setPreferredSize(new Dimension(80, 26));
            params.add(f_dropValue);
            params.add(new JLabel("Drop"));
            params.add(f_noiseTolerance);
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
            labelMaxnum = new JLabel(" ");
            labelMaxnum.setBackground(Color.GREEN);
            info.add(labelMaxnum);
            info.add(new JLabel("Max val:"));
            labelMaxval = new JLabel(" ");
            labelMaxval.setBackground(Color.GREEN);
            info.add(labelMaxval);
            info.add(new JLabel("Min val:"));
            labelMinval = new JLabel(" ");
            labelMinval.setBackground(Color.GREEN);
            info.add(labelMinval);
            info.add(new JLabel("Gradient:"));
            labelGradinet = new JLabel(" ");
            info.add(labelGradinet);
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
            c_plotMotmap = new JCheckBox("Mot map");
            c_plotConmap = new JCheckBox("Conv map");
            c_plotMotmapmax = new JCheckBox("Maxima");
            mapplots.add(c_plotMotmap);
            mapplots.add(new JLabel(" "));
            mapplots.add(c_plotConmap);
            mapplots.add(new JLabel(" "));
            mapplots.add(c_plotMotmapmax);
            middle.add(mapplots);
        }
        {
            // outline plot
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Outline plots"));
            outlines.setLayout(new BorderLayout());
            c_plotOutline = new JCheckBox("Show");
            c_plotOutline.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotOutline, BorderLayout.NORTH);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            outlinesp.add(new JLabel("Plot type"));
            outlinePlotTypes[] types = { outlinePlotTypes.MOTILITY, outlinePlotTypes.CONVEXITY,
                    outlinePlotTypes.CONVANDEXP, outlinePlotTypes.CONCANDRETR,
                    outlinePlotTypes.BOTH };
            o_plotType = new JComboBox<>(types);
            o_plotType.setPreferredSize(new Dimension(80, 26));
            outlinesp.add(o_plotType);
            outlinesp.add(new JLabel("Mot Thr"));
            f_motThreshold = new JFormattedTextField(NumberFormat.getInstance());
            f_motThreshold.setColumns(0);
            f_motThreshold.setPreferredSize(new Dimension(80, 26));
            outlinesp.add(f_motThreshold);
            outlinesp.add(new JLabel("Conv Thr"));
            f_convThreshold = new JFormattedTextField(NumberFormat.getInstance());
            f_convThreshold.setColumns(0);
            f_convThreshold.setPreferredSize(new Dimension(80, 26));
            outlinesp.add(f_convThreshold);
            middle.add(outlines);
        }
        {
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Maxima plot"));
            outlines.setLayout(new BorderLayout());
            c_plotStaticmax = new JCheckBox("Show");
            c_plotStaticmax.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotStaticmax, BorderLayout.NORTH);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            c_staticAverimage = new JCheckBox("Aver. plot");
            outlinesp.add(c_staticAverimage);
            outlinesp.add(new JLabel(" "));
            c_staticPlotmax = new JCheckBox("Plot maxi");
            outlinesp.add(c_staticPlotmax);
            outlinesp.add(new JLabel(" "));
            c_staticPlottrack = new JCheckBox("Plot tracks");
            outlinesp.add(c_staticPlottrack);
            middle.add(outlines);
        }
        {
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Dynamic plot"));
            outlines.setLayout(new BorderLayout());
            c_plotDynamicmax = new JCheckBox("Show");
            c_plotDynamicmax.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotDynamicmax, BorderLayout.NORTH);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            c_dynamicPlotmax = new JCheckBox("Plot maxi");
            outlinesp.add(c_dynamicPlotmax);
            outlinesp.add(new JLabel(" "));
            c_dynamicPlottrack = new JCheckBox("Plot tracks");
            outlinesp.add(c_dynamicPlottrack);
            outlinesp.add(new JLabel(" "));
            middle.add(outlines);
        }
        {
            // Polar plots
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Polar plot"));
            outlines.setLayout(new BorderLayout());
            c_plotPolarplot = new JCheckBox("Save");
            c_plotPolarplot.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotPolarplot, BorderLayout.NORTH);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            bGradient = new JButton("Pick grad");
            bGradient.addActionListener(this);
            outlinesp.add(bGradient);
            c_useGradient = new JCheckBox("Use grad");
            outlinesp.add(c_useGradient);
            outlinesp.add(new JLabel(" "));
            outlinesp.add(new JLabel(" "));
            middle.add(outlines);
        }

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
        if (img == null)
            return;
        // cut one slice from stack
        ImagePlus copy = img.duplicate();
        ImageStack is = copy.getImageStack();
        ImagePlus single = new ImagePlus("", is.getProcessor(1));
        // open the window
        new ImageWindow(single, new CustomCanvas(single)).setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bApply) {
            readUI(); // get ui values to config class
            try {
                model.runPlugin();
                model.rt.show("Cumulated cell statistics");

            } catch (Exception ex) { // catch all exceptions here
                LOGGER.debug(ex.getMessage(), ex);
                LOGGER.error("Problem with run of Protrusion Analysis mapping: " + ex.getMessage());
            }
        }
        if (e.getSource() == bCancel) {
            wnd.dispose();
        }
        if (e.getSource() == bHelp) {
            String url = new PropertyReader().readProperty("quimpconfig.properties", "manualURL");
            try {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e1) {
                LOGGER.debug(e1.getMessage(), e1);
                LOGGER.error("Could not open help: " + e1.getMessage(), e1);
            }
        }
        if (e.getSource() == bGradient) {
            getGradient(model.qconfLoader.getImage());
        }
    }

    /**
     * Update ProtAnalysisConfig.gradientPosition to actual clicked point on image.
     * 
     * Used during displaying frame to allow user to pick desired gradient point.
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
            super.mousePressed(e);
            LOGGER.debug("Image coords: " + offScreenX(e.getX()) + " " + offScreenY(e.getY()));
            model.config.polarPlot.type = gradientType.SCREENPOINT;
            model.config.polarPlot.gradientPoint =
                    new Point2d(offScreenX(e.getX()), offScreenY(e.getY()));
            writeUI(); // update UI
        }

    }
}
