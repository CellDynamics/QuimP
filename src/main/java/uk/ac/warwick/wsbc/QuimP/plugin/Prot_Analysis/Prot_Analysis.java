package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.sun.tools.javac.util.Pair;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.OutlineHandlers;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.Tool;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

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

    private QconfLoader qconfLoader; // main object representing loaded configuration file
    private File paramFile;

    private boolean uiCancelled = false;
    @SuppressWarnings("serial")
    // default configuration parameters
    ParamList paramList = new ParamList() {
        {
            put("noiseTolerance", "1.5");
            put("dropValue", "1");
        }
    };

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
        IJ.log(new Tool().getQuimPversion());
        showUI(true);
        if (uiCancelled)
            return;
        try {
            IJ.showStatus("Protrusion Analysis");
            if (paramFile == null) { // open UI if no file provided
                OpenDialog od =
                        new OpenDialog(
                                "Open paramater file (" + QParams.PAQP_EXT + "|"
                                        + QParamsQconf.QCONF_EXT + ")...",
                                OpenDialog.getLastDirectory(), QParams.PAQP_EXT);
                if (od.getFileName() == null) {
                    IJ.log("Cancelled - exiting...");
                    return;
                }
                // load config file but check if it is new format or old
                this.paramFile = new File(od.getDirectory(), od.getFileName());
            } else // use provided file
                this.paramFile = paramFile;
            runPlugin();
            IJ.log("Protrusion Analysis complete");
            IJ.showStatus("Finished");
        } catch (Exception e) { // catch all exceptions here
            LOGGER.debug(e.getMessage(), e);
            LOGGER.error("Problem with run of ECMM mapping: " + e.getMessage());
        }

    }

    /**
     * Helper method to keep logic of ECMM, ANA, Q plugins.
     * 
     * @throws QuimpException
     */
    private void runFromQCONF() throws QuimpException {
        STmap[] stMap = qconfLoader.getQp().getLoadedDataContainer().getQState();
        OutlineHandlers oHs = qconfLoader.getQp().getLoadedDataContainer().getECMMState();
        int h = 0;
        ImagePlus im1 = qconfLoader.getImage();
        if (im1 == null)
            return; // stop if no image
        TrackVisualisation.Stack pV = new TrackVisualisation.Stack(im1);
        PointTracker pT = new PointTracker();
        LOGGER.trace("Cells in database: " + stMap.length);
        for (STmap mapCell : stMap) { // iterate through cells
            // convert binary 2D array to ImageJ
            float[][] motMap = QuimPArrayUtils.double2float(mapCell.motMap);
            // rotate and flip to match orientation of ColorProcessor (QuimP default)
            ImageProcessor imp = new FloatProcessor(motMap).rotateRight();
            imp.flipHorizontal();
            // compute maxima
            MaximaFinder mF = new MaximaFinder(imp);
            mF.computeMaximaIJ(paramList.getDoubleValue("noiseTolerance")); // 1.5
            // track maxima across motility map
            List<Polygon> pL = pT.trackMaxima(mapCell, paramList.getDoubleValue("dropValue"), mF);
            // find crossings
            List<Pair<Point, Point>> crossingsP =
                    pT.getIntersectionParents(pL, PointTracker.WITHOUT_SELFCROSSING);
            LOGGER.trace("Crossings: " + crossingsP.size());
            // plotting on motility map
            Polygon maxi = mF.getMaxima();
            // build overlay with points
            Overlay overlay = new Overlay();
            PointRoi pR = new PointRoi(maxi.xpoints, maxi.ypoints, maxi.xpoints.length);
            ImagePlus im = new ImagePlus("motility_map", imp);
            im.setOverlay(overlay);
            overlay.add(pR);
            for (Polygon p : pL) {
                overlay.add(new PolygonRoi(p, Roi.FREELINE));
            }
            im.show();

            pV.addMaximaToImage(mapCell, mF);
            pV.addTrackingLinesToImage(mapCell, pL);
            pV.addPointsToImage(mapCell, crossingsP, Color.RED, 7);

            // Maps are correlated in order with Outlines in DataContainer.
            // mapCell.map2ColorImagePlus("motility_map", mapCell.motMap,
            // oHs.oHs.get(h).migLimits[0],
            // oHs.oHs.get(h).migLimits[1]).show();

            h++;
        }

        pV.getOriginalImage().show();
    }

    @Override
    public int setup() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setPluginConfig(ParamList par) throws QuimpPluginException {
        paramList = new ParamList(par);

    }

    @Override
    public ParamList getPluginConfig() {
        return paramList;
    }

    @Override
    public void showUI(boolean val) {
        GenericDialog pd = new GenericDialog("Protrusion detection dialog", IJ.getInstance());
        pd.addNumericField("Noise tolerance", paramList.getDoubleValue("noiseTolerance"), 3);
        pd.addNumericField("Drop value", paramList.getDoubleValue("dropValue"), 2);
        pd.addMessage("Noise tolerance - Maxima in motility map are ignored if\n"
                + " they do not stand out from the surroundings by more\n" + " than this value\n"
                + " \n" + "Drop value - Tracking of maximum point of motility map\n"
                + " stops if current point value is smaller than max-drop*max");

        pd.showDialog();
        if (pd.wasCanceled()) {
            uiCancelled = true;
            return;
        }
        paramList.put("noiseTolerance", Double.toString(pd.getNextNumber()));
        paramList.put("dropValue", Double.toString(pd.getNextNumber()));
    }

    @Override
    public String getVersion() {
        return "QuimP Package";
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

}
