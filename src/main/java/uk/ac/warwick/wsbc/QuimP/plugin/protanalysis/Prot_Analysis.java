package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.ZProjector;
import uk.ac.warwick.wsbc.QuimP.PropertyReader;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QuimpConfigFilefilter;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.filesystem.OutlinesCollection;
import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.ProtAnalysisConfig.outlinePlotTypes;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection;

/**
 * @author p.baniukiewicz
 * TODO This class support IQuimpPlugin for future.
 */
public class Prot_Analysis implements IQuimpPlugin {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Prot_Analysis.class.getName());

    private QconfLoader qconfLoader = null; // main object representing loaded configuration file
    private File paramFile;
    private boolean uiCancelled = false;
    public Prot_AnalysisUI gui;
    @SuppressWarnings("serial")
    // default configuration parameters, for future using
    ParamList paramList = new ParamList();
    /**
     * Keep overall configuration. This object is filled in GUI and passed to runPlugin, where
     * it is read out.
     */
    private ProtAnalysisConfig config;

    /**
     * Default constructor. 
     * <p>
     * Run parameterized constructor with <tt>null</tt> showing file selector.
     */
    public Prot_Analysis() {
        this(null);
    }

    /**
     * Constructor that allows to provide own file - used for tests.
     * <p>
     * @param paramFile File to process.
     */
    public Prot_Analysis(File paramFile) {
        IJ.log(new QuimpToolsCollection().getQuimPversion());
        config = new ProtAnalysisConfig();
        gui = new Prot_AnalysisUI(config, this);
        // check whether config file name is provided or ask user for it
        try {
            IJ.showStatus("Protrusion Analysis");
            if (paramFile == null) { // open UI if no file provided
                QuimpConfigFilefilter fileFilter = new QuimpConfigFilefilter(".QCONF"); // use
                                                                                        // default
                FileDialog od = new FileDialog(IJ.getInstance(),
                        "Open paramater file " + fileFilter.toString());
                od.setFilenameFilter(fileFilter);
                od.setDirectory(OpenDialog.getLastDirectory());
                od.setMultipleMode(false);
                od.setMode(FileDialog.LOAD);
                od.setVisible(true);
                if (od.getFile() == null) {
                    IJ.log("Cancelled - exiting...");
                    return;
                }
                // load config file but check if it is new format or old
                this.paramFile = new File(od.getDirectory(), od.getFile());
            } else // use provided file
                this.paramFile = paramFile;
            loadFile(); // load configuration file given by this.paramFile
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
     * Helper method to keep logic of ECMM, ANA, Q plugins.
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
                // visSingle.addStaticCirclesToImage(pT.getCommonPoints(), Color.ORANGE, 7);
                visSingle.getOriginalImage().show();
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

            // Maps are correlated in order with Outlines in DataContainer.
            // mapCell.map2ColorImagePlus("motility_map", mapCell.motMap,
            // oHs.oHs.get(h).migLimits[0],
            // oHs.oHs.get(h).migLimits[1]).show();
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
            new ProtStat(mF, trackCollection,
                    qconfLoader.getQp().getLoadedDataContainer().getStats().sHs.get(h), mapCell)
                            .writeCell(cellStatFile, h);
            protStatFile.close();
            cellStatFile.close();
            h++;
        }

        if (config.plotStaticmax)
            visStackStatic.getOriginalImage().show();
        if (config.plotDynamicmax)
            visStackDynamic.getOriginalImage().show();
        if (config.plotOutline)
            visStackOutline.getOriginalImage().show();
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
        // TODO Auto-generated method stub
        return null;
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
     * Load configuration file given by this.paramFile field. (only if not loaded before).
     * 
     * Set <tt>qconfLoader</tt> field on success or set it to <tt>null</tt>.
     * @throws QuimpPluginException
     */
    private void loadFile() throws QuimpPluginException {
        try {
            if (qconfLoader == null) {
                qconfLoader = new QconfLoader(paramFile.toPath()); // load file
                if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
                    // validate in case new format
                    qconfLoader.getBOA(); // will throw exception if not present
                    qconfLoader.getECMM();
                    qconfLoader.getQ();
                } else {
                    qconfLoader = null; // failed load or checking
                    throw new IllegalStateException(
                            "QconfLoader returned unsupported version of QuimP");
                }
            }
        } catch (Exception e) { // catch all here and convert to expected type
            throw new QuimpPluginException(e);
        }
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
    private JButton bCancel, bApply, bHelp;
    private JFormattedTextField f_noiseTolerance, f_dropValue, f_motThreshold, f_convThreshold;
    private JComboBox<ProtAnalysisConfig.outlinePlotTypes> o_plotType;
    private JCheckBox c_plotMotmap, c_plotMotmapmax, c_plotConmap, c_plotOutline, c_plotStaticmax,
            c_plotDynamicmax;

    JLabel labelMaxnum, labelMaxval, labelMinval;

    private JCheckBox c_staticPlotmax, c_staticPlottrack, c_staticAverimage;
    private JCheckBox c_dynamicPlotmax, c_dynamicPlottrack;

    private ProtAnalysisConfig config;
    private Prot_Analysis model; // main model with method to run on ui action

    public Prot_AnalysisUI(ProtAnalysisConfig config, Prot_Analysis model) {
        this.config = config;
        this.model = model;
        buildUI();
    }

    /**
     * Copy UI settings to {@link ProtAnalysisConfig} object.
     */
    public void readUI() {
        config.noiseTolerance = ((Number) f_noiseTolerance.getValue()).doubleValue();
        config.dropValue = ((Number) f_dropValue.getValue()).doubleValue();

        config.plotOutline = c_plotOutline.isSelected();
        config.outlinesToImage.motThreshold = ((Number) f_motThreshold.getValue()).doubleValue();
        config.outlinesToImage.convThreshold = ((Number) f_convThreshold.getValue()).doubleValue();
        config.outlinesToImage.plotType = (outlinePlotTypes) o_plotType.getSelectedItem();

        config.plotMotmap = c_plotMotmap.isSelected();
        config.plotMotmapmax = c_plotMotmapmax.isSelected();
        config.plotConmap = c_plotConmap.isSelected();

        config.plotStaticmax = c_plotStaticmax.isSelected();
        config.staticPlot.plotmax = c_staticPlotmax.isSelected();
        config.staticPlot.plottrack = c_staticPlottrack.isSelected();
        config.staticPlot.averimage = c_staticAverimage.isSelected();

        config.plotDynamicmax = c_plotDynamicmax.isSelected();
        config.dynamicPlot.plotmax = c_dynamicPlotmax.isSelected();
        config.dynamicPlot.plottrack = c_dynamicPlottrack.isSelected();

    }

    /**
     * Copy {@link ProtAnalysisConfig} settings to UI.
     */
    public void writeUI() {
        f_noiseTolerance.setValue(new Double(config.noiseTolerance));
        f_dropValue.setValue(new Double(config.dropValue));

        c_plotOutline.setSelected(config.plotOutline);
        f_motThreshold.setValue(new Double(config.outlinesToImage.motThreshold));
        f_convThreshold.setValue(new Double(config.outlinesToImage.convThreshold));
        o_plotType.setSelectedItem(config.outlinesToImage.plotType);

        c_plotMotmap.setSelected(config.plotMotmap);
        c_plotMotmapmax.setSelected(config.plotMotmapmax);
        c_plotConmap.setSelected(config.plotConmap);

        c_plotStaticmax.setSelected(config.plotStaticmax);
        c_staticPlotmax.setSelected(config.staticPlot.plotmax);
        c_staticPlottrack.setSelected(config.staticPlot.plottrack);
        c_staticAverimage.setSelected(config.staticPlot.averimage);

        c_plotDynamicmax.setSelected(config.plotDynamicmax);
        c_dynamicPlotmax.setSelected(config.dynamicPlot.plotmax);
        c_dynamicPlottrack.setSelected(config.dynamicPlot.plottrack);
    }

    /**
     * Build and show UI.
     */
    public void showUI(boolean val) {
        wnd.setVisible(val);
    }

    /**
     * 
     */
    private void buildUI() {
        wnd = new JFrame("Protrusion analysis plugin");
        wnd.setResizable(false);
        JPanel wndpanel = new JPanel(new BorderLayout());

        // middle main panel - integrates fields
        JPanel middle = new JPanel();
        middle.setLayout(new GridLayout(2, 2));
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
            // f_dropValue.setValue(new Double(config.dropValue));
            f_dropValue.setColumns(4);
            f_noiseTolerance = new JFormattedTextField(NumberFormat.getInstance());
            // f_noiseTolerance.setValue(new Double(config.noiseTolerance));
            f_noiseTolerance.setColumns(4);
            params.add(f_dropValue);
            params.add(new JLabel("Drop value"));
            params.add(f_noiseTolerance);
            params.add(new JLabel("Sensitivity"));
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
            info.add(new JLabel("Maxima number:"));
            labelMaxnum = new JLabel(" ");
            labelMaxnum.setBackground(Color.GREEN);
            info.add(labelMaxnum);
            info.add(new JLabel("Max value:"));
            labelMaxval = new JLabel(" ");
            labelMaxval.setBackground(Color.GREEN);
            info.add(labelMaxval);
            info.add(new JLabel("Min value:"));
            labelMinval = new JLabel(" ");
            labelMinval.setBackground(Color.GREEN);
            info.add(labelMinval);
            info.add(new JLabel(" "));
            info.add(new JLabel(" "));
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
            c_plotMotmap = new JCheckBox("Motility map");
            c_plotConmap = new JCheckBox("Convexity map");
            c_plotMotmapmax = new JCheckBox("Maxima");
            mapplots.add(c_plotMotmap);
            mapplots.add(c_plotConmap);
            mapplots.add(c_plotMotmapmax);
            mapplots.add(new JLabel(" "));
            mapplots.add(new JLabel(" "));
            middle.add(mapplots);
        }
        {
            // outline plot
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Outline plots"));
            outlines.setLayout(new BorderLayout());
            c_plotOutline = new JCheckBox("Outline plot");
            c_plotOutline.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotOutline, BorderLayout.NORTH);
            middle.add(outlines);
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
            outlinesp.add(o_plotType);
            outlinesp.add(new JLabel("Mot Thres"));
            f_motThreshold = new JFormattedTextField(NumberFormat.getInstance());
            f_motThreshold.setColumns(4);
            outlinesp.add(f_motThreshold);
            outlinesp.add(new JLabel("Conv Thres"));
            f_convThreshold = new JFormattedTextField(NumberFormat.getInstance());
            f_convThreshold.setColumns(4);
            outlinesp.add(f_convThreshold);

        }
        {
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Maxima plot"));
            outlines.setLayout(new BorderLayout());
            c_plotStaticmax = new JCheckBox("Maxima plot");
            c_plotStaticmax.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotStaticmax, BorderLayout.NORTH);
            middle.add(outlines);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            c_staticAverimage = new JCheckBox("Averaged plot");
            outlinesp.add(c_staticAverimage);
            c_staticPlotmax = new JCheckBox("Plot maxima");
            outlinesp.add(c_staticPlotmax);
            c_staticPlottrack = new JCheckBox("Plot tracks");
            outlinesp.add(c_staticPlottrack);
            outlinesp.add(new JLabel(" "));

        }
        {
            JPanel outlines = new JPanel();
            outlines.setBorder(BorderFactory.createTitledBorder("Dynamic plot"));
            outlines.setLayout(new BorderLayout());
            c_plotDynamicmax = new JCheckBox("Dynamic plot");
            c_plotDynamicmax.setBackground(new Color(255, 255, 102));
            outlines.add(c_plotDynamicmax, BorderLayout.NORTH);
            middle.add(outlines);
            JPanel outlinesp = new JPanel();
            GridLayout g = new GridLayout(3, 2);
            g.setHgap(2);
            g.setVgap(2);
            outlinesp.setLayout(g);
            outlines.add(outlinesp, BorderLayout.CENTER);
            c_dynamicPlotmax = new JCheckBox("Plot maxima");
            outlinesp.add(c_dynamicPlotmax);
            c_dynamicPlottrack = new JCheckBox("Plot tracks");
            outlinesp.add(c_dynamicPlottrack);
            outlinesp.add(new JLabel(" "));
            outlinesp.add(new JLabel(" "));

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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bApply) {
            readUI(); // get ui values to config class
            try {
                model.runPlugin();
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
                LOGGER.error("Could not open help: " + e1.getMessage(), e1);
            }
        }

    }
}
