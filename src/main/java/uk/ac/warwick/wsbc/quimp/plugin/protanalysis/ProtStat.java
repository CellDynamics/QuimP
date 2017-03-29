package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.measure.ResultsTable;
import uk.ac.warwick.wsbc.quimp.CellStats;
import uk.ac.warwick.wsbc.quimp.FrameStatistics;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.quimp.plugin.protanalysis.Track.TrackType;
import uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.quimp.utils.Pair;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;

/*
 * !>
 * @startuml doc-files/ProtStat_1_UML.png 
 * title Class dependency and most important methods. IQuimpSerialize <|.. ProtStat
 * ProtStat *-- "1" CellStatistics
 * ProtStat *-- "1" ProtrusionStatistics
 * class ProtStat {
 * +cellStatistics : CellStatistics
 * +protStatistics : ProtrusionStatistics
 * +void writeCell()
 * +void writeProtrusion()
 * }
 * class CellStatistics {
 * -void writeCellHeader()
 * -void writeCellRecord()
 * }
 * class ProtrusionStatistics {
 * -void writeProtHeader()
 * -void writeProtRecord()
 * }
 * @enduml
 * !<
 */
/**
 * Compute statistics for one cell.
 * 
 * <br>
 * <img src="doc-files/ProtStat_1_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 *
 */
public class ProtStat implements IQuimpSerialize {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ProtStat.class.getName());
  /**
   * Frame window used for computation. Should be uneven.
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

  /**
   * Construct the object using various data containers.
   * 
   * @param mf MaximaFinder
   * @param tc TrackCollection
   * @param cs CellStats
   * @param maps STmap
   */
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

    /**
     * The displacement.
     */
    // parameter vs. frames
    double[] displacement;

    /**
     * The distance.
     */
    double[] distance;

    /**
     * The area.
     */
    double[] area;

    /**
     * The circularity.
     */
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
      motMean = QuimPArrayUtils.getMeanR(maps.motMap);
      conMean = QuimPArrayUtils.getMeanR(maps.convMap);
      motVar = QuimPArrayUtils.getVarR(maps.motMap);
      conVar = QuimPArrayUtils.getVarR(maps.convMap);
      protcount = countProtrusions();
    }

    /**
     * Extract already calculated stats from CellStats.
     * 
     * <p>Fill internal fields of this class.
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
     * @return Number of protrusions found for every frame. TODO use framewindow
     */
    private double[] countProtrusions() {
      Polygon maxima = mf.getMaxima();
      int[] tmpframes = maxima.xpoints; // frame numbers from maxima
      double[] hist = new double[frames]; // bin count
      for (int f = 0; f < tmpframes.length; f++) {
        hist[tmpframes[f]]++;
      }
      return hist;
    }

    /**
     * Add header to common file before next cell.
     * 
     * @param bf writer
     * @param cellno Number of cell.
     */
    private void writeCellHeader(PrintWriter bf, int cellno) {
      //!>
      String ret = "#Cell:" + cellno;
      LOGGER.trace(ret);
      String h = "#Frame," + "Displacement," + "Distance," + "Area," + "Circularity," + "meanMot,"
              + "varMot," + "meanConv," + "varConv," + "protCount";
      //!<
      LOGGER.trace(h);
      bf.print(ret + '\n');
      bf.print(h + '\n');
      bf.flush();
    }

    /**
     * Write one line for given frame for current cell.
     * 
     * @param bf writer
     * @param frameno frame
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

    /**
     * Add cell statistic to given ResultsTable.
     * 
     * @param rt table
     * @see #writeCellRecord(PrintWriter, int)
     * @see #writeCellHeader(PrintWriter, int)
     */
    public void addCellToCellTable(ResultsTable rt) {
      // Those fields must be related to writeCellHeader and writeCellRecord
      for (int i = 0; i < displacement.length; i++) {
        rt.incrementCounter();
        rt.addValue("frame", i + 1);
        rt.addValue("displacement", displacement[i]);
        rt.addValue("distance", distance[i]);
        rt.addValue("area", area[i]);
        rt.addValue("circularity", circularity[i]);
        rt.addValue("motMean", motMean[i]);
        rt.addValue("motVar", motVar[i]);
        rt.addValue("conMean", conMean[i]);
        rt.addValue("conVar", conVar[i]);
        rt.addValue("protcount", protcount[i]);
      }
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
      if (tc.isInitialPointIncluded() == false) {
        throw new IllegalArgumentException(
                "This method assumes that initial point must be included in Track");
      }
      tipPositionIndex = new int[protCount];
      tipCoordinate = new Point2D.Double[protCount];
      tipFrame = new int[protCount];
      tipFirstFrame = new int[protCount];
      tipLastFrame = new int[protCount];
      Iterator<Pair<Track, Track>> it = tc.iterator(); // over backward,forward pairs
      int l = 0;
      while (it.hasNext()) {
        Pair<Track, Track> p = it.next();
        Track t1 = p.first;
        Track t2 = p.second;
        tipPositionIndex[l] = t1.getOutline(0); // first point is maximum
        tipCoordinate[l] = t1.getXY(0, maps.getxMap(), maps.getyMap());
        tipFrame[l] = t1.getFrame(0);
        if (t1.type == TrackType.BACKWARD && t2.type == TrackType.FORWARD) {
          tipFirstFrame[l] = t1.getFrame(t1.size() - 1);
          tipLastFrame[l] = t2.getFrame(t2.size() - 1);
        } else if (t1.type == TrackType.FORWARD && t2.type == TrackType.BACKWARD) {
          tipFirstFrame[l] = t2.getFrame(t2.size() - 1);
          tipLastFrame[l] = t1.getFrame(t1.size() - 1);
        }
        l++;
      }
    }

    /**
     * Add header to common file before next cell.
     * 
     * @param bf writer
     * @param cellno Number of cell.
     */
    private void writeProtHeader(PrintWriter bf, int cellno) {
      //!<
      String ret = "#Cell:" + cellno;
      LOGGER.trace(ret);
      String h = "#Id," + "Position," + "x-xoordinate," + "y-coordinate," + "Frame," + "FirstFrame,"
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
     * @param bf writer
     * @param id protrusion id
     */
    private void writeProtRecord(PrintWriter bf, int id) {
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
   * 
   * @param bf place to write
   * @param cellno Cell number
   */
  public void writeCell(PrintWriter bf, int cellno) {
    LOGGER.debug("Writing cell stats at:" + bf.toString());
    cellStatistics.writeCellHeader(bf, cellno);
    for (int f = 0; f < frames; f++) {
      cellStatistics.writeCellRecord(bf, f);
    }
  }

  /**
   * Write stats for protrusions for this cell.
   * 
   * @param bf place to write
   * @param cellno Cell number.
   */
  public void writeProtrusion(PrintWriter bf, int cellno) {
    LOGGER.debug("Writing prot stats at:" + bf.toString());
    protStatistics.writeProtHeader(bf, cellno);
    for (int t = 0; t < tc.getBf().size(); t++) {
      protStatistics.writeProtRecord(bf, t);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
    // TODO Auto-generated method stub

  }
}
