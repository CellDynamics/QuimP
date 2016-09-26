package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.tools.javac.util.Pair;

/**
 * Represent collection of tracks.
 * 
 * @author p.baniukiewicz
 *
 */
public class TrackCollection {
    /**
     * Collection of pairs of tracks. Every pair originates from the same starting point. 
     * First element is backward tracks, second forward. Tracks can be empty but never null.
     * Every track has different id.
     */
    private ArrayList<Pair<Track, Track>> bf;

    int nextId = 0;

    public TrackCollection() {
        bf = new ArrayList<>();
    }

    /**
     * Add pair of tracks to collection <backwardTrack,forwardTrack>
     * 
     * @param backward
     * @param forward
     */
    public void addPair(Polygon backward, Polygon forward) {
        Track tmpB = new Track(nextId++, null);
        for (int i = 0; i < backward.npoints; i++)
            tmpB.add(new Point(backward.xpoints[i], backward.ypoints[i]));
        Track tmpF = new Track(nextId++, null);
        for (int i = 0; i < forward.npoints; i++)
            tmpF.add(new Point(forward.xpoints[i], forward.ypoints[i]));
        bf.add(new Pair<Track, Track>(tmpB, tmpF));
    }

    /**
     * Add pair of tracks to collection
     * 
     * @param backward
     * @param forward
     */
    public void addPair(List<Point> backward, List<Point> forward) {
        Track b = new Track(backward, nextId++, null);
        b.type = Track.Type.BACKWARD;
        Track f = new Track(forward, nextId++, null);
        f.type = Track.Type.FORWARD;
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
            ret.add(p.fst);
            ret.add(p.snd);
        }
        return ret.iterator();
    }

    /**
     * @return the bf
     */
    public List<Pair<Track, Track>> getBf() {
        return Collections.unmodifiableList(bf);
    }

}

/**
 * Hold one track line with additional parameters.
 * <p>
 * In general x coordinate stands for frame and y for index.
 * @author p.baniukiewicz
 *
 */
class Track extends ArrayList<Point> {
    private static final long serialVersionUID = 8928704797702167155L;
    private static final Logger LOGGER = LogManager.getLogger(Track.class.getName());

    /**
     * Types of tracking lines.
     * 
     * @author p.baniukiewicz
     *
     */
    public static enum Type {
        BACKWARD, FORWARD, OTHER
    };

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
    Type type;

    public Track() {
        super();
        id = -1;
        parents = new Point(-1, -1);
    }

    public Track(int id, Point parents) {
        this();
        this.id = id;
        if (parents != null)
            this.parents = parents;
    }

    public Track(Collection<? extends Point> c) {
        super(c);
        id = -1;
        parents = new Point(-1, -1);
    }

    public Track(Collection<? extends Point> c, int id, Point parents) {
        this(c);
        this.id = id;
        if (parents != null)
            parents = new Point(-1, -1);
    }

    /**
     * Not use due to similar structure as Track(int, Point)
     * @param initialCapacity
     */
    @SuppressWarnings("unused")
    private Track(int initialCapacity) {
        super(initialCapacity);
        id = -1;
        parents = new Point(-1, -1);
    }

    public Polygon asPolygon() {
        Iterator<Point> it = iterator();
        Polygon ret = new Polygon();
        while (it.hasNext()) {
            Point p = it.next();
            ret.addPoint(p.x, p.y);
        }
        return ret;
    }

}
