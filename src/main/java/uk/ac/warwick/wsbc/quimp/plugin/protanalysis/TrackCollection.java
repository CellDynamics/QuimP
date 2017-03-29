package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import uk.ac.warwick.wsbc.quimp.utils.Pair;

/**
 * Represent collection of tracks.
 * 
 * @author p.baniukiewicz
 *
 */
public class TrackCollection {
  /**
   * Collection of pairs of tracks. Every pair originates from the same starting point. First
   * element is backward tracks, second forward. Tracks can be empty but never null. Every track
   * has different id.
   */
  private ArrayList<Pair<Track, Track>> bf;

  /**
   * Indicates whether Track stored in this collection have initial point on first entry.
   */
  private boolean isInitialPointIncluded;

  /**
   * Check if initial point is included in tracking.
   * 
   * @return the isInitialPointIncluded
   */
  public boolean isInitialPointIncluded() {
    return isInitialPointIncluded;
  }

  private int nextId = 0;

  /**
   * Instantiates a new track collection.
   *
   * @param isInitialPointIncluded the is initial point included
   */
  public TrackCollection(boolean isInitialPointIncluded) {
    bf = new ArrayList<>();
    this.isInitialPointIncluded = isInitialPointIncluded;
  }

  /**
   * Add pair of tracks to collection backwardTrack,forwardTrack.
   * 
   * @param backward backward track
   * @param forward forward track
   * @see #addPair(List, List)
   */
  public void addPair(Polygon backward, Polygon forward) {
    Track tmpB = new Track(nextId++, null);
    for (int i = 0; i < backward.npoints; i++) {
      tmpB.add(new Point(backward.xpoints[i], backward.ypoints[i]));
    }
    Track tmpF = new Track(nextId++, null);
    for (int i = 0; i < forward.npoints; i++) {
      tmpF.add(new Point(forward.xpoints[i], forward.ypoints[i]));
    }
    bf.add(new Pair<Track, Track>(tmpB, tmpF));
  }

  /**
   * Add pair of tracks to collection.
   * 
   * @param backward backward track points
   * @param forward forward track points
   * @see #addPair(Polygon, Polygon)
   */
  public void addPair(List<Point> backward, List<Point> forward) {
    Track b = new Track(backward, nextId++, null);
    b.type = Track.TrackType.BACKWARD;
    Track f = new Track(forward, nextId++, null);
    f.type = Track.TrackType.FORWARD;
    bf.add(new Pair<Track, Track>(b, f));

  }

  /**
   * Get iterator over pairs of tracks (related to one starting point).
   * 
   * @return iterator
   */
  public Iterator<Pair<Track, Track>> iterator() {
    return bf.iterator();
  }

  /**
   * Get iterator over all tracks in collection.
   * 
   * @return iterator
   */
  public Iterator<Track> iteratorTrack() {
    List<Track> ret = new ArrayList<>();
    for (Pair<Track, Track> p : bf) {
      ret.add(p.first);
      ret.add(p.second);
    }
    return ret.iterator();
  }

  /**
   * Get unmodifiable list of all tracks.
   * 
   * @return the bf list of all tracks (forward,backward)
   */
  public List<Pair<Track, Track>> getBf() {
    return Collections.unmodifiableList(bf);
  }

}
