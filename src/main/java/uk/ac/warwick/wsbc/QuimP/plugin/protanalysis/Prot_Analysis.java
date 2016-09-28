package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.ZProjector;
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
public class Prot_Analysis implements IQuimpPlugin, ActionListener {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(Prot_Analysis.class.getName());

    private QconfLoader qconfLoader; // main object representing loaded configuration file
    private File paramFile;
    /**
     * Keep overall configuration. This object is filled in GUI and passed to runPlugin, where
     * it is read out.
     */
    private ProtAnalysisConfig config;

    // UI elements
    private JFrame wnd;
    private JButton bCancel, bApply, bHelp;
    private JFormattedTextField fieldnoiseTolerance, fielddropValue;
    private JCheckBox checkPlotMotmap, checkPlotStaticmaxima;
    private JComboBox<ProtAnalysisConfig.outlinePlotTypes> comboPlotOutlineParam;

    private boolean uiCancelled = false;
    @SuppressWarnings("serial")
    // default configuration parameters, for future using
    ParamList paramList = new ParamList();

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
            // showUI(true);
            if (uiCancelled)
                return;
            // runPlugin();
            IJ.log("Protrusion Analysis complete");
            IJ.showStatus("Finished");
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
        int h = 0;
        ImagePlus im1static = qconfLoader.getImage();
        if (im1static == null)
            return; // stop if no image

        TrackVisualisation.Image visStackStatic =
                new TrackVisualisation.Image(im1static.duplicate());
        visStackStatic.getOriginalImage().setTitle("Static points");
        // Example of plotting on averaged image
        visStackStatic.flatten(ZProjector.AVG_METHOD, false);

        TrackVisualisation.Image visCommonPoints =
                new TrackVisualisation.Image(im1static.duplicate());
        visCommonPoints.getOriginalImage().setTitle("Common points");

        TrackVisualisation.Stack visStackDynamic =
                new TrackVisualisation.Stack(im1static.duplicate());
        visStackDynamic.getOriginalImage().setTitle("Dynamic tracking");

        TrackVisualisation.Stack visStackOutline =
                new TrackVisualisation.Stack(im1static.duplicate());
        visStackOutline.getOriginalImage().setTitle("Outlines");

        TrackMapAnalyser pT = new TrackMapAnalyser();
        LOGGER.trace("Cells in database: " + stMap.length);
        for (STmap mapCell : stMap) { // iterate through cells
            // convert binary 2D array to ImageJ
            TrackVisualisation.Map visSingle = new TrackVisualisation.Map("motility_map",
                    QuimPArrayUtils.double2float(mapCell.motMap));
            // compute maxima
            MaximaFinder mF = new MaximaFinder(visSingle.getOriginalImage().getProcessor());
            mF.computeMaximaIJ(config.noiseTolerance); // 1.5
            // track maxima across motility map
            pT.trackMaxima(mapCell, config.dropValue, mF);
            TrackCollection trackCollection = pT.getTrackCollection();

            visSingle.addMaximaToImage(mF);
            visSingle.addTrackingLinesToImage(trackCollection);
            // visSingle.addStaticCirclesToImage(pT.getCommonPoints(), Color.ORANGE, 7);
            visSingle.getOriginalImage().show();

            // visStackStatic.addElementsToImage(mapCell, trackCollection, mF);

            // visCommonPoints.addCirclesToImage(mapCell, pT.getCommonPoints(), Color.ORANGE, 7);

            visStackDynamic.addMaximaToImage(mapCell, mF);
            visStackDynamic.addTrackingLinesToImage(mapCell, trackCollection);

            // visStackOutline.addOutlinesToImage(mapCell, config);

            // visStackDynamic.addCirclesToImage(mapCell, pT.getCommonPoints(), Color.ORANGE, 9);

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

        // visStackStatic.getOriginalImage().show();
        visStackDynamic.getOriginalImage().show();
        // visCommonPoints.getOriginalImage().show();
        // visStackOutline.getOriginalImage().show();
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

    /**
     * test method to replace showUI
     */
    public void showUI2() {
        wnd = new JFrame("Protrusion analysis plugin");
        wnd.setResizable(false);
        JPanel wndpanel = new JPanel(new BorderLayout());

        // middle main panel - integrates fields
        JPanel middle = new JPanel();
        middle.setLayout(new FlowLayout());
        wndpanel.add(middle, BorderLayout.CENTER);

        // options
        JPanel params = new JPanel();
        params.setBorder(BorderFactory.createTitledBorder("Options"));
        params.setLayout(new GridLayout(2, 2));
        fielddropValue = new JFormattedTextField(NumberFormat.getInstance());
        fielddropValue.setValue(new Double(config.dropValue));
        fielddropValue.setColumns(4);
        fieldnoiseTolerance = new JFormattedTextField(NumberFormat.getInstance());
        fieldnoiseTolerance.setValue(new Double(config.noiseTolerance));
        fieldnoiseTolerance.setColumns(4);
        params.add(fielddropValue);
        params.add(new JLabel("Drop value"));
        params.add(fieldnoiseTolerance);
        params.add(new JLabel("Sensitivity"));
        middle.add(params);

        // vis options
        JPanel visparams = new JPanel();
        visparams.setBorder(BorderFactory.createTitledBorder("Simple plots"));
        visparams.setLayout(new GridLayout(2, 2));
        checkPlotMotmap = new JCheckBox("Motility map");
        checkPlotStaticmaxima = new JCheckBox("Outlines");
        comboPlotOutlineParam = new JComboBox<>();
        visparams.add(checkPlotMotmap);
        visparams.add(new JLabel(""));
        visparams.add(checkPlotStaticmaxima);
        visparams.add(comboPlotOutlineParam);
        middle.add(visparams);

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
        wnd.setVisible(true);

    }

    @Override
    public void showUI(boolean val) {
        GenericDialog pd = new GenericDialog("Protrusion detection dialog", IJ.getInstance());
        pd.addNumericField("Noise tolerance", config.noiseTolerance, 3);
        pd.addNumericField("Drop value", config.dropValue, 2);
        pd.addMessage("Noise tolerance - Maxima in motility map are ignored if\n"
                + " they do not stand out from the surroundings by more\n" + " than this value\n"
                + " \n" + "Drop value - Tracking of maximum point of motility map\n"
                + " stops if current point value is smaller than max-drop*max");

        pd.showDialog();
        config.outlinesToImage.plotType = outlinePlotTypes.CONVANDEXP;
        if (pd.wasCanceled()) {
            uiCancelled = true;
            return;
        }
        config.noiseTolerance = pd.getNextNumber();
        config.dropValue = pd.getNextNumber();
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
            qconfLoader = new QconfLoader(paramFile.toPath()); // load file
            if (qconfLoader.getConfVersion() == QParams.NEW_QUIMP) { // new path
                // validate in case new format
                qconfLoader.getBOA(); // will throw exception if not present
                qconfLoader.getECMM();
                qconfLoader.getQ();
                runFromQCONF();
            } else {
                throw new IllegalStateException(
                        "QconfLoader returned unsupported version of QuimP");
            }
        } catch (Exception e) { // catch all here and convert to expected type
            throw new QuimpPluginException(e);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

}
