package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Polygon;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import uk.ac.warwick.wsbc.QuimP.CellStats;
import uk.ac.warwick.wsbc.QuimP.FrameStatistics;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;

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

    /**
     * Hold cell shape based statistics.
     */
    public CellStatistics cellStatistics;
    /**
     * Hold protrusion statistics.
     */
    public ProtrusionStatistics protStatistics;

    public ProtStat(MaximaFinder mf, TrackCollection tc, CellStats cs, STmap maps) {
        this.mf = mf;
        this.tc = tc;
        this.cs = cs;
        this.maps = maps;
        frames = maps.getT();
        mapRes = maps.getRes();

        cellStatistics = new CellStatistics();

    }

    /**
     * Collection of methods for compute cell shape based statistics.
     * 
     * @author p.baniukiewicz
     *
     */
    class CellStatistics {

        // parameter vs. frames
        double[] displacement;
        double[] distance;
        double[] area;
        double[] circularity;
        /**
         * Mean of motility.
         */
        double[] motMean;
        /**
         * Variance of motility.
         */
        double[] motVar;
        /**
         * Mean of convexity.
         */
        double[] conMean;
        /**
         * Variance of convexity.
         */
        double[] conVar;
        /**
         * Number of protrusions in every frame.
         */
        double[] protcount;

        /**
         * Compute all cell statistics.
         */
        public CellStatistics() {
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
            displacement = new double[frames];
            distance = new double[frames];
            area = new double[frames];
            circularity = new double[frames];
            int l = 0;
            for (FrameStatistics frameStat : cs.framestat) {
                displacement[l] = frameStat.displacement;
                distance[l] = frameStat.dist;
                area[l] = frameStat.area;
                circularity[l] = frameStat.circularity;
                l++;
            }
        }

        /**
         * Count protrusions for every frame.
         * 
         * @return Number of protrusions found for every frame.
         * TODO use framewindow
         */
        private double[] countProtrusions() {
            Polygon maxima = mf.getMaxima();
            int[] _frames = maxima.xpoints; // frame numbers from maxima
            double[] hist = new double[frames]; // bin count
            for (int f = 0; f < _frames.length; f++)
                hist[_frames[f]]++;
            return hist;
        }

        /**
         * Add header to common file before next cell.
         * 
         * @param cellno Number of cell.
         */
        private void writeCellHeader(PrintWriter bf, int cellno) {
            //!<
            String ret = "#Cell:"+cellno;
            LOGGER.trace(ret);
            String h =                     "#Frame,"
                                          + "Displacement,"
                                          + "Distance,"
                                          + "Area,"
                                          + "Circularity,"
                                          + "meanMot,"
                                          + "varMot,"
                                          + "meanConv,"
                                          + "varConv,"
                                          + "protCount";
            /**/
            LOGGER.trace(h);

        }

        /**
         * Write one line for given frame for current cell.
         * 
         * @param bf
         * @param frameno
         */
        private void writeCellRecord(PrintWriter bf, int frameno) {
            String ret = Integer.toString(frameno + 1) + ',';
            ret = ret.concat(Double.toString(displacement[frameno])) + ',';
            ret = ret.concat(Double.toString(distance[frameno])) + ',';
            ret = ret.concat(Double.toString(area[frameno])) + ',';
            ret = ret.concat(Double.toString(circularity[frameno])) + ',';
            ret = ret.concat(Double.toString(motMean[frameno])) + ',';
            ret = ret.concat(Double.toString(motVar[frameno])) + ',';
            ret = ret.concat(Double.toString(conMean[frameno])) + ',';
            ret = ret.concat(Double.toString(conVar[frameno])) + ',';
            ret = ret.concat(Double.toString(protcount[frameno]));

            LOGGER.trace(ret);

        }

    }

    /**
     * Collection of methods for compute protrusion statistics.
     * @author p.baniukiewicz
     *
     */
    class ProtrusionStatistics {

    }

    /**
     * Calculate mean of map for every frame (row).
     * @param map
     * @return Mean of map for every row as list.
     * TODO use framewindow 
     */
    private double[] geMean(double[][] map) {
        double[] ret = new double[frames];
        for (int f = 0; f < frames; f++) { // for every frame
            double mean = 0;
            for (int r = 0; r < mapRes; r++)
                mean += map[f][r];
            ret[f] = mean / mapRes;
        }
        return ret;
    }

    /**
     * Calculate variance of map for every frame (row).
     * @param map
     * @return Variance of map for every row as list.
     * TODO use framewindow
     */
    private double[] getVar(double[][] map) {
        double[] ret = new double[frames];
        double[] means = geMean(map); // FIXME Efficiency issue. Mean calculated twice.
        for (int f = 0; f < frames; f++) { // for every frame
            double var = 0;
            for (int r = 0; r < mapRes; r++)
                var += Math.pow(means[f] - map[f][r], 2.0);
            ret[f] = var / mapRes;
        }
        return ret;
    }

    /**
     * Add stats for this cell to output.
     * @param bf
     */
    public void writeCell(PrintWriter bf, int cellno) {
        cellStatistics.writeCellHeader(bf, cellno);
        for (int f = 0; f < frames; f++)
            cellStatistics.writeCellRecord(bf, f);
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
