package uk.ac.warwick.wsbc.QuimP.plugin.protanalysis;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.sun.tools.javac.util.Pair;

import uk.ac.warwick.wsbc.QuimP.CellStats;
import uk.ac.warwick.wsbc.QuimP.FrameStatistics;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.plugin.protanalysis.Track.Type;
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

    /**
     * Total number of protrusions.
     */
    private int protCount;

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
        protCount = mf.getMaximaNumber();

        cellStatistics = new CellStatistics();
        protStatistics = new ProtrusionStatistics();

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
            motMean = QuimPArrayUtils.geMean(maps.motMap);
            conMean = QuimPArrayUtils.geMean(maps.convMap);
            motVar = QuimPArrayUtils.getVar(maps.motMap);
            conVar = QuimPArrayUtils.getVar(maps.convMap);
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
            bf.print(ret + '\n');
            bf.print(h + '\n');
            bf.flush();
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
            bf.print(ret + '\n');
            bf.flush();
        }

    }

    /**
     * Collection of methods for compute protrusion statistics.
     * 
     * @author p.baniukiewicz
     *
     */
    class ProtrusionStatistics {
        /**
         * Indicate position of the tip on cell outline. Not normalised.
         */
        int[] tipPositionIndex;
        /**
         * Screen coordinate of the tip.
         */
        Point2D.Double[] tipCoordinate;
        /**
         * Tip frame.
         */
        int[] tipFrame;
        /**
         * First frame where tip appeared, within given criterion of motility drop.
         */
        int[] tipFirstFrame;
        /**
         * Last frame where tip appeared, within given criterion of motility drop.
         */
        int[] tipLastFrame;

        /**
         * Compute protrusion stats.
         */
        public ProtrusionStatistics() {
            getFromTrackStats();
        }

        /**
         * Extract statistic data from {@link TrackCollection}.
         */
        private void getFromTrackStats() {
            if (tc.isInitialPointIncluded() == false)
                throw new IllegalArgumentException(
                        "This method assumes that initial point must be included in Track");
            tipPositionIndex = new int[protCount];
            tipCoordinate = new Point2D.Double[protCount];
            tipFrame = new int[protCount];
            tipFirstFrame = new int[protCount];
            tipLastFrame = new int[protCount];
            Iterator<Pair<Track, Track>> it = tc.iterator(); // over backward,forward pairs
            int l = 0;
            while (it.hasNext()) {
                Pair<Track, Track> p = it.next();
                Track t1 = p.fst;
                Track t2 = p.snd;
                tipPositionIndex[l] = t1.getOutline(0); // first point is maximum
                tipCoordinate[l] = t1.getXY(0, maps.getxMap(), maps.getyMap());
                tipFrame[l] = t1.getFrame(0);
                if (t1.type == Type.BACKWARD && t2.type == Type.FORWARD) {
                    tipFirstFrame[l] = t1.getFrame(t1.size() - 1);
                    tipLastFrame[l] = t2.getFrame(t2.size() - 1);
                } else if (t1.type == Type.FORWARD && t2.type == Type.BACKWARD) {
                    tipFirstFrame[l] = t2.getFrame(t2.size() - 1);
                    tipLastFrame[l] = t1.getFrame(t1.size() - 1);
                }
                l++;
            }
        }

        /**
         * Add header to common file before next cell.
         * 
         * @param cellno Number of cell.
         */
        private void writeProtHeader(PrintWriter bf, int cellno) {
            //!<
            String ret = "#Cell:"+cellno;
            LOGGER.trace(ret);
            String h =                     "#Id,"
                                          + "Position,"
                                          + "x-xoordinate,"
                                          + "y-coordinate,"
                                          + "Frame,"
                                          + "FirstFrame,"
                                          + "LastFrame";
            /**/
            LOGGER.trace(h);
            bf.print(ret + '\n');
            bf.print(h + '\n');
            bf.flush();
        }

        /**
         * Write one line for given frame for current cell.
         * 
         * @param bf
         * @param frameno
         */
        private void writeCellRecord(PrintWriter bf, int id) {
            String ret = Integer.toString(id + 1) + ',';
            ret = ret.concat(Integer.toString(tipPositionIndex[id])) + ',';
            ret = ret.concat(Double.toString(tipCoordinate[id].x)) + ',';
            ret = ret.concat(Double.toString(tipCoordinate[id].y)) + ',';
            ret = ret.concat(Integer.toString(tipFrame[id])) + ',';
            ret = ret.concat(Integer.toString(tipFirstFrame[id])) + ',';
            ret = ret.concat(Integer.toString(tipLastFrame[id]));

            LOGGER.trace(ret);
            bf.print(ret + '\n');
            bf.flush();
        }

    }

    /**
     * Add stats for this cell to output.
     * @param bf place to write
     * @param cellno Cell number
     */
    public void writeCell(PrintWriter bf, int cellno) {
        LOGGER.debug("Writing cell stats at:" + bf.toString());
        cellStatistics.writeCellHeader(bf, cellno);
        for (int f = 0; f < frames; f++)
            cellStatistics.writeCellRecord(bf, f);
    }

    /**
     * Write stats for protrusions for this cell.
     * @param bf place to write
     * @param cellno Cell number.
     */
    public void writeProtrusion(PrintWriter bf, int cellno) {
        LOGGER.debug("Writing prot stats at:" + bf.toString());
        protStatistics.writeProtHeader(bf, cellno);
        for (int t = 0; t < tc.getBf().size(); t++) {
            protStatistics.writeCellRecord(bf, t);
        }
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
