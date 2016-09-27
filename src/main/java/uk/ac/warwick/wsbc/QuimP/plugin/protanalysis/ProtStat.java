package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Polygon;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import uk.ac.warwick.wsbc.QuimP.CellStats;
import uk.ac.warwick.wsbc.QuimP.FrameStatistics;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Compute statistics for one cell.
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtStat implements IQuimpSerialize {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(ProtStat.class.getName());
    /**
     * Frame window used for computation.
     * Should be uneven.
     */
    private final int framewindow = 3;

    private CellStats cs;
    private TrackCollection tc;
    private MaximaFinder mf;
    private STmap maps;
    /**
     * Number of frames.
     */
    private int frames;
    /**
     * Map resolution.
     */
    private int mapRes;

    // parameter vs. frames
    ArrayList<Double> displacement;
    ArrayList<Double> distance;
    ArrayList<Double> area;
    ArrayList<Double> circularity;
    /**
     * Mean of motility.
     */
    ArrayList<Double> motMean;
    /**
     * Variance of motility.
     */
    ArrayList<Double> motVar;
    /**
     * Mean of convexity.
     */
    ArrayList<Double> conMean;
    /**
     * Variance of convexity.
     */
    ArrayList<Double> conVar;
    /**
     * Number of protrusions in every frame.
     */
    ArrayList<Double> protcount;

    public ProtStat(MaximaFinder mf, TrackCollection tc, CellStats cs, STmap maps) {
        this.mf = mf;
        this.tc = tc;
        this.cs = cs;
        this.maps = maps;
        frames = maps.getT();
        mapRes = maps.getRes();

        getFromCellStats(); // copy some stats from CellStats
        // calculate basic stats for motility and convexity
        motMean = geMean(maps.motMap);
        conMean = geMean(maps.convMap);
        motVar = getVar(maps.motMap);
        conVar = getVar(maps.convMap);
        protcount = countProtrusions();
    }

    /**
     * Extract already calculated stats from CellStats.
     */
    private void getFromCellStats() {
        for (FrameStatistics frameStat : cs.framestat) {
            displacement.add(frameStat.displacement);
            distance.add(frameStat.dist);
            area.add(frameStat.area);
            circularity.add(frameStat.circularity);

        }
    }

    /**
     * Count protrusions for every frame.
     * 
     * @return Number of protrusions found for every frame.
     * TODO use framewindow
     */
    private ArrayList<Double> countProtrusions() {
        ArrayList<Double> ret = new ArrayList<>();
        Polygon maxima = mf.getMaxima();
        int[] frames = maxima.xpoints;
        ImageProcessor ip =
                new ShortProcessor(frames.length, 1, QuimPArrayUtils.int2short(frames), null);
        ImageStatistics is = ip.getStatistics();

        return ret;
    }

    /**
     * Calculate mean of map for every frame (row).
     * @param map
     * @return Mean of map for every row as list.
     * TODO use framewindow 
     */
    private ArrayList<Double> geMean(double[][] map) {
        ArrayList<Double> ret = new ArrayList<>();
        for (int f = 0; f < frames; f++) { // for every frame
            double mean = 0;
            for (int r = 0; r < mapRes; r++)
                mean += map[f][r];
            ret.add(mean / mapRes);
        }
        return ret;
    }

    /**
     * Calculate variance of map for every frame (row).
     * @param map
     * @return Variance of map for every row as list.
     * TODO use framewindow
     */
    private ArrayList<Double> getVar(double[][] map) {
        ArrayList<Double> ret = new ArrayList<>();
        ArrayList<Double> means = geMean(map); // FIXME Efficiency issue. Mean calculated twice.
        for (int f = 0; f < frames; f++) { // for every frame
            double var = 0;
            for (int r = 0; r < mapRes; r++)
                var += Math.pow(means.get(f) - map[f][r], 2.0);
            ret.add(var / mapRes);
        }
        return ret;
    }

    /**
     * Add stats for this cell to output.
     * @param bf
     */
    public void writeCell(PrintWriter bf, int cellno) {
        writeHeader(bf, cellno);
        for (int f = 0; f < frames; f++)
            writeRecord(bf, f);

    }

    /**
     * Add header to common file before next cell.
     * 
     * @param cellno Number of cell.
     */
    private void writeHeader(PrintWriter bf, int cellno) {

    }

    /**
     * Write one line for given frame for current cell.
     * 
     * @param bf
     * @param frameno
     */
    private void writeRecord(PrintWriter bf, Integer frameno) {
        String ret = frameno.toString() + ',';
        ret.concat(protcount.get(frameno).toString());

        LOGGER.debug(ret);

    }

    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }
}
