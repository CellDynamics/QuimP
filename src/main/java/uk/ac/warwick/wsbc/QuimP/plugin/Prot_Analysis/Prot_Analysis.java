/**
 * @file Prot_Analysis.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Polygon;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
        ImagePlus im1 =
                IJ.openImage(qp.getLoadedDataContainer().BOAState.boap.getOrgFile().getPath());
        ProtrusionVis pV = new ProtrusionVis(im1);
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
            List<PolygonRoi> pL = trackMaxima(mapCell, 1, mF); // track maxima across motility map

            // plotting
            Polygon maxi = mF.getMaxima();
            // build overlay with points
            Overlay overlay = new Overlay();
            PointRoi pR = new PointRoi(maxi.xpoints, maxi.ypoints, maxi.xpoints.length);
            ImagePlus im = new ImagePlus("motility_map", imp);
            im.setOverlay(overlay);
            overlay.add(pR);
            for (PolygonRoi p : pL) {
                overlay.add(p);
            }
            im.show();

            pV.addMaximaToImage(mapCell, mF);
            pV.addTrackingLinesToImage(mapCell, pL);

            // Maps are correlated in order with Outlines in DataContainer.
            mapCell.map2ColorImagePlus("motility_map", mapCell.motMap, oHs.oHs.get(h).migLimits[0],
                    oHs.oHs.get(h).migLimits[1]).show();

            h++;
        }

        pV.getOriginalImage().show();
    }

    /* (non-Javadoc)
     * @see uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore#runFromPAQP()
     */
    @Override
    public void runFromPAQP() {
        // TODO Auto-generated method stub
        super.runFromPAQP();
    }

    /**
     * Track maxima across motility map as long as they fulfill criterion of amplitude.
     * 
     * @param mapCell holds all maps generated and saved by QuimP
     * @param drop the value (in x/100) while velocity remains above of the peak speed. E.g for
     * drop=1 all tracked points are considered (along positive motility), drop=0.5 stands for 
     * points that are above 0.5*peakval, where peakval is the value of found maximum.  
     * @param maximaFinder properly initialized object that holds maxima of motility map. 
     * All maxima are tracked
     * @return List of points tracked from every maximum point as long as they meet criterion.
     * Maximum point can be included in this list depending on setting of 
     * {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap.includeFirst} flag. Points for one tracking 
     * line are packed into PolygonRoi object. Those objects alternate -
     * backwardM1,forwardM1,backwardM2,forwardM2,... where Mx is maximum point. The size of list 
     * is 2*number of maxima.
     */
    private List<PolygonRoi> trackMaxima(STmap mapCell, double drop,
            final MaximaFinder maximaFinder) {
        numFrames = mapCell.motMap.length;
        numIndexes = mapCell.motMap[0].length;
        ArrayList<PolygonRoi> ret = new ArrayList<>();
        // int[] indexes = new int[numFrames];
        int[] framesF = null;
        int[] framesB = null;
        Polygon maxi = maximaFinder.getMaxima(); // restore computed maxima
        double[] maxValues = maximaFinder.getMaxValues(); // max values in order of maxi
        TrackMap trackMap = new TrackMap(mapCell.originMap, mapCell.coordMap); // build tracking map
        trackMap.includeFirst = true;
        int[] tForward = null;
        int[] tBackward = null;
        int N = 0;
        // iterate through all maxima - take only indexes (x)
        for (int i = 0; i < maxi.npoints; i++) {
            int index = maxi.xpoints[i]; // considered index
            int frame = maxi.ypoints[i]; // considered frame
            LOGGER.trace("Max = [" + frame + "," + index + "]");
            // trace forward every index until end of time
            tForward = trackMap.trackForward(frame, index, numFrames - frame);
            framesF = trackMap.getForwardFrames(frame, numFrames - frame);
            // trace backward every index until end of time
            tBackward = trackMap.trackBackward(frame, index, frame);
            framesB = trackMap.getBackwardFrames(frame, frame);
            QuimPArrayUtils.reverseIntArray(framesB); // reverse have last frame on 0 index
                                                      // (important for Polygon creation)
            QuimPArrayUtils.reverseIntArray(tBackward);
            // check where is drop off - index that has velocity below drop
            double dropValue = maxValues[i] - maxValues[i] * drop;

            for (N = 0; N < tBackward.length; N++) {
                // frames[N] = frame + N + 1; // store number of current frame for tracked point +1
                // because max point is not included in tForward (tForward[0] is for frame+1)
                if (tBackward[N] >= 0) {
                    double val = (mapCell.motMap[framesB[N]][tBackward[N]]);
                    if (val < dropValue)
                        break;
                }
            }
            N = (--N < 0) ? 0 : N; // now end is the last index that fulfill criterion
            LOGGER.trace("tBackward frames:" + Arrays.toString(framesB));
            ret.add(new PolygonRoi(tBackward, framesB, N, Roi.FREELINE));

            for (N = 0; N < tForward.length; N++) {
                // frames[N] = frame + N + 1; // store number of current frame for tracked point +1
                // because max point is not included in tForward (tForward[0] is for frame+1)
                if (tForward[N] >= 0) {
                    double val = (mapCell.motMap[framesF[N]][tForward[N]]);
                    if (val < dropValue)
                        break;
                }
            }
            N = (--N < 0) ? 0 : N; // now end is the last index that fulfill criterion
            LOGGER.trace("tForward frames:" + Arrays.toString(framesF));
            ret.add(new PolygonRoi(tForward, framesF, N, Roi.FREELINE));
        }
        return ret;
    }

}
