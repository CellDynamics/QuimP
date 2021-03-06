package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.awt.Polygon;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TrackMapAnalyserParamTest.
 *
 * @author p.baniukiewicz
 */
@RunWith(Parameterized.class)
public class TrackMapAnalyserParamTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(TrackMapAnalyserParamTest.class.getName());

  /**
   * The Enum Type.
   */
  enum Type {

    /**
     * The intersection.
     */
    INTERSECTION,
    /**
     * The repeating.
     */
    REPEATING,
    /**
     * The enumerate.
     */
    ENUMERATE
  }

  /** The track map analyser. */
  private TrackMapAnalyser trackMapAnalyser;

  /** The track. */
  private ArrayList<Polygon> track;

  /** The exp intersection points. */
  private Polygon expIntersectionPoints;

  /** The exp intersection pairs. */
  private List<Pair<Point, Point>> expIntersectionPairs;

  /** The type. */
  private Type type;

  /** The self crossing. */
  private int selfCrossing;

  /**
   * Test cases defined.
   * 
   * @return test set
   */
  @Parameters(name = "{index}: ({0})")
  public static Collection<Object[]> data() {
    ArrayList<Object[]> ret = new ArrayList<>();
    // One common point
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);
      Pair<Point, Point> p = new ImmutablePair<Point, Point>(new Point(0, 1), new Point(5, 5));
      List<Pair<Point, Point>> expIntersectionPairs =
              new ArrayList<Pair<Point, Point>>(Arrays.asList(p));
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // no intersect
    // return empty polygon and empty list of pairs
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>();
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // 2 intersections
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 9, 1 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 5, 9 }, new int[] { 5, 9 }, 2);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(5, 5)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(9, 9)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // all points the same
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 1, 2, 3, 4, 6, 8, 5, 7, 10, 9 },
              new int[] { 1, 2, 3, 4, 6, 8, 5, 7, 10, 9 }, 10);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(1, 1)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(2, 2)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(3, 3)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(4, 4)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(5, 5)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(6, 6)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(7, 7)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(8, 8)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(9, 9)));
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(10, 10)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // three tracks - each cross each
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

      int[] x3 = { 100, 6, 9, 40 };
      int[] y3 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track =
              test(new Object[] { x1, y1 }, new Object[] { x2, y2 }, new Object[] { x3, y3 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 6, 5, 9 }, new int[] { 5, 5, 9 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(5, 5)));
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(9, 9)));
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(6, 5)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // one common point for three tracks
    // reduced for one common point
    // or returned as list taking under account all parents
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 6, 5, 70, 3, 2, 1 };

      int[] x3 = { 100, 61, 17, 40 };
      int[] y3 = { 100, 5, 70, 7 };
      ArrayList<Polygon> track =
              test(new Object[] { x1, y1 }, new Object[] { x2, y2 }, new Object[] { x3, y3 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 17 }, new int[] { 70 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(17, 70)));
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(17, 70)));
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(17, 70)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // only one track on input
    // returns empty polygon or list
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 });
      Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {

        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // forward track is empty
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };

      int[] x2 = {};
      int[] y2 = {};
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {

        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // backward track is empty
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };

      int[] x2 = {};
      int[] y2 = {};
      ArrayList<Polygon> track = test(new Object[] { x2, y2 }, new Object[] { x1, y1 });
      Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {

        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // intersection without selfcrossing
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 400, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 7, 10 };

      int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

      int[] x3 = { 100, 200, 300, 400 };
      int[] y3 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track =
              test(new Object[] { x1, y1 }, new Object[] { x2, y2 }, new Object[] { x3, y3 });
      Polygon expIntersectionPoints = new Polygon(new int[] { 5, 400 }, new int[] { 5, 7 }, 2);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(400, 7)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITHOUT_SELFCROSSING });
    }
    // without selfcrossing, no backward track for one track
    {
      int[] x1 = {};
      int[] y1 = {};

      int[] x2 = { 1, 2, 3, 4, 5, 6, 400, 8, 200, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 7, 3, 5, 1 };

      int[] x3 = { 100, 200, 300, 400 };
      int[] y3 = { 100, 5, 9, 7 };

      int[] x4 = { 100, 11, 12, 13 };
      int[] y4 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 },
              new Object[] { x3, y3 }, new Object[] { x4, y4 });
      Polygon expIntersectionPoints =
              new Polygon(new int[] { 200, 400, 100 }, new int[] { 5, 7, 100 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(400, 7)));
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(200, 5)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITHOUT_SELFCROSSING });
    }
    // with selfcrossing, no backward track for one track
    {
      int[] x1 = {};
      int[] y1 = {};

      int[] x2 = { 1, 2, 3, 4, 5, 6, 400, 8, 200, 10 };
      int[] y2 = { 10, 9, 8, 7, 5, 5, 7, 3, 5, 1 };

      int[] x3 = { 100, 200, 300, 400 };
      int[] y3 = { 100, 5, 9, 7 };

      int[] x4 = { 100, 11, 12, 13 };
      int[] y4 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 },
              new Object[] { x3, y3 }, new Object[] { x4, y4 });
      Polygon expIntersectionPoints =
              new Polygon(new int[] { 200, 400, 100 }, new int[] { 5, 7, 100 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(400, 7)));
          add(new ImmutablePair<Point, Point>(new Point(1, 2), new Point(200, 5)));
          add(new ImmutablePair<Point, Point>(new Point(2, 3), new Point(100, 100)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // without selfcrossing, no forward track for one track
    {
      int[] x1 = { 1, 200, 3, 4, 5, 6, 7, 8, 400, 10 };
      int[] y1 = { 1, 5, 3, 4, 5, 6, 7, 8, 7, 10 };

      int[] x2 = {};
      int[] y2 = {};

      int[] x3 = { 100, 200, 300, 400 };
      int[] y3 = { 100, 5, 9, 7 };

      int[] x4 = { 100, 11, 12, 13 };
      int[] y4 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 },
              new Object[] { x3, y3 }, new Object[] { x4, y4 });
      Polygon expIntersectionPoints =
              new Polygon(new int[] { 200, 400, 100 }, new int[] { 5, 7, 100 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(200, 5)));
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(400, 7)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITHOUT_SELFCROSSING });
    }
    // with selfcrossing, no forward track for one track
    {
      int[] x1 = { 1, 200, 3, 4, 5, 6, 7, 8, 400, 10 };
      int[] y1 = { 1, 5, 3, 4, 5, 6, 7, 8, 7, 10 };

      int[] x2 = {};
      int[] y2 = {};

      int[] x3 = { 100, 200, 300, 400 };
      int[] y3 = { 100, 5, 9, 7 };

      int[] x4 = { 100, 11, 12, 13 };
      int[] y4 = { 100, 5, 9, 7 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 },
              new Object[] { x3, y3 }, new Object[] { x4, y4 });
      Polygon expIntersectionPoints =
              new Polygon(new int[] { 200, 400, 100 }, new int[] { 5, 7, 100 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(200, 5)));
          add(new ImmutablePair<Point, Point>(new Point(0, 2), new Point(400, 7)));
          add(new ImmutablePair<Point, Point>(new Point(2, 3), new Point(100, 100)));
        }
      };
      ret.add(new Object[] { Type.INTERSECTION, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    //
    // REPEATINGS******************************************************************************
    //
    // Repeatings One self intersection
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 4, 4, 5, 6, 7, 8, 9, 11, 12, 13 };
      int[] y2 = { 1, 2, 3, 6, 7, 8, 9, 11, 12, 13 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints =
              new Polygon(new int[] { 200, 400, 100 }, new int[] { 5, 7, 100 }, 3);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(0, 1), new Point(6, 6)));
        }
      };
      ret.add(new Object[] { Type.REPEATING, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    // no intersection
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 4, 4, 5, 6, 7, 8, 9, 11, 12, 13 };
      int[] y2 = { 1, 2, 3, 60, 70, 80, 90, 11, 12, 13 };
      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
        }
      };
      ret.add(new Object[] { Type.REPEATING, track, expIntersectionPoints, expIntersectionPairs,
          TrackMapAnalyser.WITH_SELFCROSSING });
    }
    //
    // ENUMERATE******************************************************************************
    //
    // point from backtrack
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 2 }, new int[] { 2 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(3, 3), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // point from forwardtrack
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 10 }, new int[] { 10 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(11, 11), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // point from forwardtrack first one
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 9 }, new int[] { 9 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(10, 10), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // count all
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 11 }, new int[] { 11 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(11, 11), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          70000 });
    }
    // last point from backtrack but no forward track is given
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = {};
      int[] y2 = {};

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 9 }, new int[] { 9 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(10, 10), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // before last point from backtrack but no forward track is given
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = {};
      int[] y2 = {};

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 8 }, new int[] { 8 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(9, 9), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // point from forwardtrack but no backward track is given
    {
      int[] x1 = {};
      int[] y1 = {};

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 1 }, new int[] { 1 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(11, 11), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // point in not on the list
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { -1 }, new int[] { -1 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(110, 110), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          0 });
    }
    // point from backtrack but no forward track is given + include maximum
    {
      int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      int[] x2 = {};
      int[] y2 = {};

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 9 }, new int[] { 9 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(10, 10), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          70000 });
    }
    // point from forwardtrack but no backward track is given + include maximum
    {
      int[] x1 = {};
      int[] y1 = {};

      int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
      int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

      ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
      // expected index at x[0]
      Polygon expIntersectionPoints = new Polygon(new int[] { 1 }, new int[] { 1 }, 1);
      @SuppressWarnings("serial")
      List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
        {
          add(new ImmutablePair<Point, Point>(new Point(11, 11), new Point())); // tested point
        }
      };
      ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
          70000 });
    }

    return ret;
  }

  /**
   * setUp.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    trackMapAnalyser = new TrackMapAnalyser();
  }

  /**
   * Instantiates a new track map analyser param test.
   *
   * @param type the type
   * @param track the track
   * @param expIntersectionPoints the exp intersection points
   * @param expIntersectionPairs the exp intersection pairs
   * @param selfCrossing the self crossing
   */
  public TrackMapAnalyserParamTest(Type type, ArrayList<Polygon> track,
          Polygon expIntersectionPoints, List<Pair<Point, Point>> expIntersectionPairs,
          int selfCrossing) {
    this.type = type;
    this.track = track;
    this.expIntersectionPairs = expIntersectionPairs;
    this.expIntersectionPoints = expIntersectionPoints;
    this.selfCrossing = selfCrossing;
  }

  /**
   * Test method for
   * {@link TrackMapAnalyser#getIntersectionPoints(java.util.List)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetIntersectionPointsParam() throws Exception {

    Assume.assumeTrue(type == Type.INTERSECTION);
    Polygon ret = trackMapAnalyser.getIntersectionPoints(track);
    assertThat(ret.xpoints, is(expIntersectionPoints.xpoints));
    assertThat(ret.ypoints, is(expIntersectionPoints.ypoints));

    List<Pair<Point, Point>> ret1 = trackMapAnalyser.getIntersectionParents(track, selfCrossing);
    assertThat(ret1, is(expIntersectionPairs));
  }

  /**
   * {@link TrackMapAnalyser#removeSelfRepeatings(List, List)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRemoveSelfRepeatingsParam() throws Exception {

    Assume.assumeTrue(type == Type.REPEATING);
    List<Pair<Point, Point>> intersections =
            trackMapAnalyser.getIntersectionParents(track, selfCrossing);
    List<Pair<Point, Point>> ret1 =
            new TrackMapAnalyser().removeSelfRepeatings(intersections, track);
    assertThat(ret1, is(expIntersectionPairs));
  }

  /**
   * Test method for
   * {@link TrackMapAnalyser#enumeratePoint(java.awt.Polygon, java.awt.Polygon, java.awt.Point)}.
   * 
   * <p>Assume that tested point is first pair left in expIntersectionPairs Result is in
   * expIntersectionPoints as x[0] coord. Use selfCrossing to decide whether use INCLUDE_INITIAL
   * 
   * @throws Exception Exception
   */
  @Test
  public void testEnumeratePoint() throws Exception {
    Field includeinitial = TrackMapAnalyser.class.getDeclaredField("INCLUDE_INITIAL");
    includeinitial.setAccessible(true);
    Assume.assumeTrue(type == Type.ENUMERATE);
    if (selfCrossing > 65535) {
      includeinitial.setBoolean(null, false); // count all
    } else {
      includeinitial.setBoolean(null, true); // count maximum one
    }
    int ret1 = TrackMapAnalyser.enumeratePoint(track.get(0), track.get(1),
            expIntersectionPairs.get(0).getLeft());
    includeinitial.setBoolean(null, true); // count maximum one
    assertThat(ret1, is(expIntersectionPoints.xpoints[0]));
  }

  /**
   * Helper method for build list of polygons using {x[],y[]} pairs.
   * 
   * @param obj obj
   * @return array of polygons from input arrays
   */
  static ArrayList<Polygon> test(Object[]... obj) {
    ArrayList<Polygon> track = new ArrayList<>();
    for (Object[] o : obj) {
      track.add(new Polygon((int[]) o[0], (int[]) o[1], ((int[]) o[0]).length));
    }
    return track;
  }

  /**
   * Test method for
   * {@link TrackMapAnalyser#removeSelfRepeatings(List, List)}.
   * 
   * <p>No selfcrossing, B1 B2 intersection
   * 
   * @throws Exception Exception
   */
  @Test
  @Ignore
  public void testRemoveSelfRepeatings_2() throws Exception {
    throw new RuntimeException("not yet implemented");
  }

}
