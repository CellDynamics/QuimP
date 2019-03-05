package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Represent collection of tracks.
 * 
 * @author p.baniukiewicz
 * @see Track
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
    bf.add(new ImmutablePair<Track, Track>(tmpB, tmpF));
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
    bf.add(new ImmutablePair<Track, Track>(b, f));

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
      ret.add(p.getLeft());
      ret.add(p.getRight());
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

  /**
   * Save tracks to csv file.
   * 
   * <p>Format of the file is:
   * 
   * <pre>
   * Point 000 backward;[frame],10.0,11.0
   * Point 000 backward;[index],110.0,111.0
   * Point 000 forward;[frame],0.0,1.0,2.0
   * Point 000 forward;[index],100.0,101.0,102.0
   * Point 001 backward;[frame],30.0,31.0,32.0,33.0,34.0
   * Point 001 backward;[index],130.0,131.0,132.0,133.0,134.0
   * Point 001 forward;[frame],20.0,21.0,22.0,23.0
   * Point 001 forward;[index],120.0,121.0,122.0,123.0
   * </pre>
   * 
   * <p>First columns is legend, next columns are indexes of rows and columns in maps returned from
   * Q-Analysis. To obtain screen coordinates one can use xCoord and yCorrd maps. Tracks for each
   * point are saved alternately in the order Backward - Forward. Each track occupies two rows for
   * frame and outline position coordinates.
   * 
   * @param writer where to save
   * @throws IOException on error
   */
  public void saveTracks(Writer writer) throws IOException {
    Iterator<Pair<Track, Track>> iter = iterator();
    int pointno = 0;
    while (iter.hasNext()) {
      Pair<Track, Track> track = iter.next();
      StringBuilder sb = new StringBuilder();

      sb.append(String.format("Point %03d backward;[frame]", pointno));
      sb.append(',');
      for (Point p : track.getLeft()) {
        sb.append(p.x);
        sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append('\n');

      sb.append(String.format("Point %03d backward;[index]", pointno));
      sb.append(',');
      for (Point p : track.getLeft()) {
        sb.append(p.y);
        sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append('\n');

      sb.append(String.format("Point %03d forward;[frame]", pointno));
      sb.append(',');
      for (Point p : track.getRight()) {
        sb.append(p.x);
        sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append('\n');

      sb.append(String.format("Point %03d forward;[index]", pointno));
      sb.append(',');
      for (Point p : track.getRight()) {
        sb.append(p.y);
        sb.append(',');
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append('\n');

      writer.write(sb.toString());
      pointno++;
    }
  }

}
