package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Point;
import java.awt.Polygon;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link TrackCollection}.
 * 
 * @author p.baniukiewicz
 *
 */
public class TrackCollectionTest {

  // two tracks
  private List<Point> f1; // [(0,100),(1,101),(2,102)]
  private List<Point> b1; // [(10,110),(11,111)]
  private List<Point> f2; // [(20,120),(21,121),(22,122),(23,123)]
  private List<Point> b2; // [(30,130),(31,131),(32,132),(33,133),(34,134)]

  private List<Point> ef1;
  private List<Point> eb1;
  private List<Point> ef2;
  private List<Point> eb2;
  private int ef1len = 3;
  private int eb1len = 2;
  private int ef2len = 4;
  private int eb2len = 5;

  /**
   * Set up.
   * 
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
    f1 = new ArrayList<>();
    ef1 = new ArrayList<>();
    for (int i = 0; i < ef1len; i++) {
      f1.add(new Point(i, i + 100));
      ef1.add(new Point(i, i + 100));
    }

    b1 = new ArrayList<>();
    eb1 = new ArrayList<>();
    for (int i = 0; i < eb1len; i++) {
      b1.add(new Point(i + 10, i + 100 + 10));
      eb1.add(new Point(i + 10, i + 100 + 10));
    }

    f2 = new ArrayList<>();
    ef2 = new ArrayList<>();
    for (int i = 0; i < ef2len; i++) {
      f2.add(new Point(i + 20, i + 100 + 20));
      ef2.add(new Point(i + 20, i + 100 + 20));
    }

    b2 = new ArrayList<>();
    eb2 = new ArrayList<>();
    for (int i = 0; i < eb2len; i++) {
      b2.add(new Point(i + 30, i + 100 + 30));
      eb2.add(new Point(i + 30, i + 100 + 30));
    }

  }

  /**
   * Test method for
   * {@link TrackCollection#isInitialPointIncluded()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIsInitialPointIncluded() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    assertThat(tc.isInitialPointIncluded(), is(true));
    tc = new TrackCollection(false);
    assertThat(tc.isInitialPointIncluded(), is(false));
  }

  /**
   * Test method for
   * {@link TrackCollection#addPair(java.awt.Polygon, java.awt.Polygon)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testAddPairPolygonPolygon() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    tc.addPair(toPolygon(b1), toPolygon(f1));
    tc.addPair(toPolygon(b2), toPolygon(f2));
    Iterator<Pair<Track, Track>> it = tc.iterator();
    List<List<Point>> eb = Arrays.asList(eb1, eb2);
    List<List<Point>> ef = Arrays.asList(ef1, ef2);
    int ind = 0;
    while (it.hasNext()) {
      Pair<Track, Track> pair = it.next();
      assertThat(pair.getLeft(), Matchers.contains(eb.get(ind).toArray()));
      assertThat(pair.getRight(), Matchers.contains(ef.get(ind).toArray()));
      ind++;
    }
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.TrackCollection#iterator()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIterator() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    assertThat(tc.iterator().hasNext(), is(false));

    tc.addPair(b1, f1);
    tc.addPair(b2, f2);
    Iterator<Pair<Track, Track>> it = tc.iterator();
    assertThat(tc.iterator().hasNext(), is(true));
    List<List<Point>> eb = Arrays.asList(eb1, eb2);
    List<List<Point>> ef = Arrays.asList(ef1, ef2);
    int ind = 0;
    while (it.hasNext()) {
      Pair<Track, Track> pair = it.next();
      assertThat(pair.getLeft(), Matchers.contains(eb.get(ind).toArray()));
      assertThat(pair.getRight(), Matchers.contains(ef.get(ind).toArray()));
      ind++;
    }

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.TrackCollection#iteratorTrack()}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIteratorTrack() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    tc.addPair(b1, f1);
    tc.addPair(b2, f2);
    Iterator<Track> it = tc.iteratorTrack();
    int ind = 0;
    List<List<Point>> e = Arrays.asList(eb1, ef1, eb2, ef2);
    while (it.hasNext()) {
      Track track = it.next();
      assertThat(track, Matchers.contains(e.get(ind).toArray()));
      ind++;
    }
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.TrackCollection#getBf()}.
   * 
   * @throws Exception on error
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testGetBf() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    tc.addPair(b1, f1);
    tc.addPair(b2, f2);

    List<Pair<Track, Track>> bf = tc.getBf();
    bf.add(new ImmutablePair<Track, Track>(new Track(), new Track()));
  }

  /**
   * Test method for
   * {@link TrackCollection#saveTracks(java.io.Writer)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testSaveTracks() throws Exception {
    TrackCollection tc = new TrackCollection(true);
    tc.addPair(b1, f1);
    tc.addPair(b2, f2);

    PrintWriter writer = new PrintWriter(System.out);
    tc.saveTracks(writer);

    writer.flush();
    writer.close();

  }

  private Polygon toPolygon(List<Point> list) {
    Polygon p = new Polygon();
    for (Point point : list) {
      p.addPoint((int) point.getX(), (int) point.getY());
    }
    return p;
  }

}
