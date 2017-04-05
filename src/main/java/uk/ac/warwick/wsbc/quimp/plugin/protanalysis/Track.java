package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold one track line with additional parameters.
 * 
 * <p>In general x coordinate stands for frame and y for index.
 * 
 * @author p.baniukiewicz
 * @see TrackMapAnalyser#trackMaxima(uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap, double,
 *      MaximaFinder)
 */
class Track extends ArrayList<Point> {
  private static final long serialVersionUID = 8928704797702167155L;
  static final Logger LOGGER = LoggerFactory.getLogger(Track.class.getName());

  /**
   * Types of tracking lines.
   * 
   * @author p.baniukiewicz
   *
   */
  public static enum TrackType {
    BACKWARD, FORWARD, OTHER
  }

  /**
   * ID of tracking line. Every line in TrackCollection has different id.
   */
  private int id;
  /**
   * Parents (Ids) of this track.
   */
  private Point parents;
  /**
   * Type of track. OTHER is reserved for virtual tracks created from merging points.
   */
  TrackType type;

  public Track() {
    super();
    id = -1;
    parents = null;
  }

  public Track(int id, Point parents) {
    this();
    this.id = id;
    this.parents = parents;
  }

  public Track(Collection<? extends Point> c) {
    super(c);
    id = -1;
    parents = null;
  }

  public Track(Collection<? extends Point> c, int id, Point parents) {
    this(c);
    this.id = id;
    this.parents = parents;
  }

  /**
   * Not in use due to similar structure as Track(int, Point).
   * 
   * @param initialCapacity initialCapacity
   */
  @SuppressWarnings("unused")
  private Track(int initialCapacity) {
    super(initialCapacity);
    id = -1;
    parents = null;
  }

  /**
   * 
   * @return This Track as polygon.
   */
  public Polygon asPolygon() {
    Iterator<Point> it = iterator();
    Polygon ret = new Polygon();
    while (it.hasNext()) {
      Point p = it.next();
      ret.addPoint(p.x, p.y);
    }
    return ret;
  }

  /**
   * Get xy coordinates of Track point according to xy maps.
   * 
   * @param index order of Track point
   * @param xmap x-coordinates map compatible with
   *        {@link uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap}
   * @param ymap y-coordinates map compatible with
   *        {@link uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap}
   * @return Screen coordinates of Track point.
   */
  public Point2D.Double getXY(int index, double[][] xmap, double[][] ymap) {
    Point p = get(index);
    return new Point2D.Double(xmap[p.x][p.y], ymap[p.x][p.y]);
  }

  /**
   * Return frame for given index of Track point.
   * 
   * <p>Resolves correct mapping between coordinates.
   * 
   * @param index index of point
   * @return Frame of this point
   */
  public int getFrame(int index) {
    return get(index).x;
  }

  /**
   * Return outline index for given index of Track point.
   * 
   * <p>Resolves correct mapping between coordinates.
   * 
   * @param index index of point
   * @return Outline index of this point
   */
  public int getOutline(int index) {
    return get(index).y;
  }

}