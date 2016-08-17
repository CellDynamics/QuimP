/**
 * @file Prot_Analysis.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Polygon;
import java.nio.file.Path;
import java.util.ArrayList;
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
import ij.plugin.filter.MaximumFinder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.OutlineHandlers;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.geom.TrackMap;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 * @date 15 Aug 2016
 *
 */
public class Prot_Analysis extends QuimpPluginCore {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(Prot_Analysis.class.getName());

    private int numIndexes;
    private int numFrames;

    /**
     * Default constructor. 
     * <p>
     * Run parameterized constructor with <tt>null</tt> showing file selector.
     */
    public Prot_Analysis() {
        this(null);
    }

    /**
     * @param path
     */
    public Prot_Analysis(Path path) {
        super(path);
        // LOGGER.debug("Frames=" + numFrames + " Resolution=" + numIndexes);
        IJ.showStatus("Protrusion Analysis");
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#showDialog()
     */
    @Override
    public boolean showDialog() {
        // TODO Auto-generated method stub
        return super.showDialog();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#validateQconf()
     */
    @Override
    public boolean validateQconf() throws QuimpException {
        return super.validateQconf();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#runFromQCONF()
     */
    @Override
    public void runFromQCONF() {
        STmap[] stMap = qp.getLoadedDataContainer().QState;
        OutlineHandlers oHs = qp.getLoadedDataContainer().ECMMState;
        int h = 0;
        for (STmap mapCell : stMap) { // iterate through cells
            // convert binary 2D array to ImageJ
            float[][] motMap = QuimPArrayUtils.double2float(mapCell.motMap);
            // rotate and flip to match orientation of ColorProcessor (QuimP default)
            ImageProcessor imp = new FloatProcessor(motMap).rotateRight();
            imp.flipHorizontal();
            // compute maxima
            MaximaFinder mF = new MaximaFinder(imp);
            mF.computeMaximaIJ(10);
            List<PolygonRoi> pL = trackMaxima(mapCell, 1, mF);

            // plotting
            Polygon maxi = mF.getMaxima();
            // build overlay with points
            Overlay overlay = new Overlay();
            PointRoi pR = new PointRoi(maxi.xpoints, maxi.ypoints, maxi.xpoints.length);
            ImagePlus im = new ImagePlus("motility_map", imp);
            im.setOverlay(overlay);
            overlay.add(pR);
            for (PolygonRoi p : pL)
                overlay.add(p);
            im.show();
            /*
            // Maps are correlated in order with Outlines in DataContainer.
            mapCell.map2ColorImagePlus("motility_map", mapCell.motMap, oHs.oHs.get(h).migLimits[0],
                    oHs.oHs.get(h).migLimits[1]).show();
            */
            h++;
        }
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#runFromPAQP()
     */
    @Override
    public void runFromPAQP() {
        // TODO Auto-generated method stub
        super.runFromPAQP();
    }

    private List<PolygonRoi> trackMaxima(STmap mapCell, double drop,
            final MaximaFinder maximaFiner) {
        numFrames = mapCell.motMap.length;
        numIndexes = mapCell.motMap[0].length;
        ArrayList<PolygonRoi> ret = new ArrayList<>();
        // int[] indexes = new int[numFrames];
        int[] frames = null;
        Polygon maxi = maximaFiner.getMaxima(); // restore computed maxima
        double[] maxValues = maximaFiner.getMaxValues(); // max values in order of maxi
        TrackMap trackMap = new TrackMap(mapCell.originMap, mapCell.coordMap); // build tracking map
        trackMap.includeFirst = true;
        int[] tForward = null;
        int N = 0;
        // iterate through all maxima - take only indexes (x)
        for (int i = 0; i < maxi.npoints; i++) {
            int index = maxi.xpoints[i]; // considered index
            int frame = maxi.ypoints[i]; // considered frame
            // trace forward every index until end of time
            tForward = trackMap.trackForward(frame, index, numFrames - frame);
            frames = trackMap.getForwardFrames(frame, numFrames - frame);
            // check where is drop off - index that has velocity below drop
            double dropValue = maxValues[i] - maxValues[i] * drop;
            for (N = 0; N < tForward.length; N++) {
                // frames[N] = frame + N + 1; // store number of current frame for tracked point +1
                // because max point is not included in tForward
                // (tForward[0] is for frame+1)
                if (tForward[N] >= 0) {
                    double val = Math.abs(mapCell.motMap[frames[N]][tForward[N]]);
                    if (val < dropValue)
                        break;
                }
            }
            N = (--N < 0) ? 0 : N; // now end is the last index that fulfill criterion
            LOGGER.trace("N=" + N);
            ret.add(new PolygonRoi(tForward, frames, N, Roi.FREELINE));
        }
        return ret;
    }

}

/**
 * Support various methods of finding maxima in ImageJ image.
 * 
 * @author p.baniukiewicz
 *
 */
class MaximaFinder {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(MaximaFinder.class.getName());
    private ImageProcessor iP;
    private Polygon maxima; // found maxima as polygon

    /**
     * Construct MaximaFinder object.
     * 
     * @param iP Image processor with image to analyse.
     */
    public MaximaFinder(ImageProcessor iP) {
        this.iP = iP;
        maxima = null;
    }

    /**
     * Compute maxima using ImageJ procedure.
     * 
     * @param tolerance
     * @see https://rsb.info.nih.gov/ij/developer/api/ij/plugin/filter/MaximumFinder.html
     */
    public void computeMaximaIJ(double tolerance) {
        MaximumFinder mF = new MaximumFinder();
        maxima = mF.getMaxima(iP, tolerance, false);
    }

    /**
     * Return values corresponding to indexes returned by getMaxima.
     * <p>
     * Must be called after getMaxima.
     * 
     * @return Maxima in order of indexes returned by getMaxima.
     */
    public double[] getMaxValues() {
        if (maxima == null)
            return new double[0];
        double[] ret = new double[maxima.xpoints.length];
        for (int i = 0; i < maxima.xpoints.length; i++)
            ret[i] = iP.getf(maxima.xpoints[i], maxima.ypoints[i]);
        return ret;
    }

    /**
     * 
     * @return Return maxima found by {@link computeMaximaIJ(double)}.
     */
    public Polygon getMaxima() {
        if (maxima == null)
            return new Polygon();
        return maxima;
    }
}
