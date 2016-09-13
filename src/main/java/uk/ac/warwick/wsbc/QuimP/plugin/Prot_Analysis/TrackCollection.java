package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.tools.javac.util.Pair;

/**
 * @author baniuk
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
        bf.add(new Pair<Track, Track>(new Track(backward, nextId++, null),
                new Track(forward, nextId++, null)));

    }

    public Iterator<Pair<Track, Track>> iterator() {
        return bf.iterator();
    }

}

class Track extends ArrayList<Point> {
    private static final long serialVersionUID = 8928704797702167155L;
    private static final Logger LOGGER = LogManager.getLogger(Track.class.getName());
    int id;
    Point parents;

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
            ret.addPoint(p.y, p.x);
        }
        return ret;
    }

}
