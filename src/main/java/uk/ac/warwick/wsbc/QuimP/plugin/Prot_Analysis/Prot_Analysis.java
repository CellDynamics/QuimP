package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Polygon;
import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.IJ;
import ij.ImagePlus;
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
            runPlugin(); // run
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
        ProtrusionVis pV = new ProtrusionVis(im1);
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
            mF.computeMaximaIJ(1.5);
            List<Polygon> pL = pT.trackMaxima(mapCell, 1, mF); // track maxima across motility
                                                               // map
            // plotting
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
        // TODO Auto-generated method stub

    }

    @Override
    public ParamList getPluginConfig() {
        return new ParamList();
    }

    @Override
    public void showUI(boolean val) {
        // TODO Auto-generated method stub

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
