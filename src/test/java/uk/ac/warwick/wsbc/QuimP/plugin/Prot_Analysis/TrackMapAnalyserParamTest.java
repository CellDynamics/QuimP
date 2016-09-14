package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.tools.javac.util.Pair;

/**
 * @author baniuk
 *
 */
@RunWith(Parameterized.class)
public class TrackMapAnalyserParamTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER =
            LogManager.getLogger(TrackMapAnalyserParamTest.class.getName());

    enum Type {
        INTERSECTION, REPEATING, ENUMERATE
    };

    private TrackMapAnalyser trackMapAnalyser;
    private ArrayList<Polygon> track;
    private Polygon expIntersectionPoints;
    private List<Pair<Point, Point>> expIntersectionPairs;
    private Type type;
    private int selfCrossing;

    //!<
    @Parameters(name = "{index}: ({0})")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> ret = new ArrayList<>();
        // One common point
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2});
            Polygon expIntersectionPoints = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);
            Pair<Point, Point> p = new Pair<Point, Point>(new Point(0, 1), new Point(5, 5));
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>(Arrays.asList(p));
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // no intersect
        // return empty polygon and empty list of pairs
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2});
            Polygon expIntersectionPoints = new Polygon(new int[] {}, new int[] {}, 0);
            List<Pair<Point, Point>> expIntersectionPairs =  new ArrayList<Pair<Point, Point>>();
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // 2 intersections
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 9, 1 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2});
            Polygon expIntersectionPoints = new Polygon(new int[] { 5, 9 }, new int[] { 5, 9 }, 2);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(5, 5)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(9, 9)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // all points the same
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2});
            Polygon expIntersectionPoints = new Polygon(new int[] { 1, 2, 3, 4, 6, 8, 5, 7, 10, 9 },
                    new int[] { 1, 2, 3, 4, 6, 8, 5, 7, 10, 9 }, 10);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(1, 1)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(2, 2)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(3, 3)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(4, 4)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(5, 5)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(6, 6)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(7, 7)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(8, 8)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(9, 9)));
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(10, 10)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // three tracks - each cross each
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

            int[] x3 = { 100, 6, 9, 40 };
            int[] y3 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3});
            Polygon expIntersectionPoints = new Polygon(new int[] { 6, 5, 9 }, new int[] { 5, 5, 9 }, 3);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(5, 5)));
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(9, 9)));
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(6, 5)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
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
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3});
            Polygon expIntersectionPoints =  new Polygon(new int[] { 17 }, new int[] { 70 }, 1);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(17, 70)));
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(17, 70)));
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(17, 70)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // only one track on input
        // returns empty polygon or list
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1});
            Polygon expIntersectionPoints =  new Polygon(new int[] {  }, new int[] {  }, 0);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {

                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // forward track is empty
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };
            
            int[] x2 = {};
            int[] y2 = {};
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2});
            Polygon expIntersectionPoints =  new Polygon(new int[] {  }, new int[] {  }, 0);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {

                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // backward track is empty
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };
            
            int[] x2 = {};
            int[] y2 = {};
            ArrayList<Polygon> track = test(new Object[]{x2,y2},new Object[]{x1,y1});
            Polygon expIntersectionPoints =  new Polygon(new int[] {  }, new int[] {  }, 0);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {

                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // intersection without selfcrossing
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 400, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 7, 10 };

            int[] x2 = { 1,  2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

            int[] x3 = { 100, 200, 300, 400 };
            int[] y3 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3});
            Polygon expIntersectionPoints =  new Polygon(new int[] {5,400  }, new int[] {5,7  }, 2);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(400, 7)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITHOUT_SELFCROSSING});
        }
        // without selfcrossing, no backward track for one track
        {
            int[] x1 = { };
            int[] y1 = { };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 400, 8, 200, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 7, 3,  5, 1 };

            int[] x3 = { 100, 200, 300, 400 };
            int[] y3 = { 100, 5,   9,   7 };

            int[] x4 = { 100, 11, 12, 13 };
            int[] y4 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3},new Object[]{x4,y4});
            Polygon expIntersectionPoints =  new Polygon(new int[] {200,400,100  }, new int[] {5,7,100  }, 3);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(400, 7)));
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(200, 5)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITHOUT_SELFCROSSING});
        }
        // with selfcrossing, no backward track for one track
        {
            int[] x1 = { };
            int[] y1 = { };

            int[] x2 = { 1, 2, 3, 4, 5, 6, 400, 8, 200, 10 };
            int[] y2 = { 10, 9, 8, 7, 5, 5, 7, 3,  5, 1 };

            int[] x3 = { 100, 200, 300, 400 };
            int[] y3 = { 100, 5,   9,   7 };

            int[] x4 = { 100, 11, 12, 13 };
            int[] y4 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3},new Object[]{x4,y4});
            Polygon expIntersectionPoints =  new Polygon(new int[] {200,400,100  }, new int[] {5,7,100  }, 3);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(400, 7)));
                    add(new Pair<Point, Point>(new Point(1, 2), new Point(200, 5)));
                    add(new Pair<Point, Point>(new Point(2, 3), new Point(100, 100)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
        }
        // without selfcrossing, no forward track for one track
        {
            int[] x1 = { 1, 200, 3, 4, 5, 6, 7, 8, 400, 10 };
            int[] y1 = { 1, 5, 3, 4, 5, 6, 7, 8, 7, 10 };

            int[] x2 = { };
            int[] y2 = { };

            int[] x3 = { 100, 200, 300, 400 };
            int[] y3 = { 100, 5, 9, 7 };

            int[] x4 = { 100, 11, 12, 13 };
            int[] y4 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3},new Object[]{x4,y4});
            Polygon expIntersectionPoints =  new Polygon(new int[] {200,400,100  }, new int[] {5,7,100  }, 3);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(200, 5)));
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(400, 7)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITHOUT_SELFCROSSING});
        }
        // with selfcrossing, no forward track for one track
        {
            int[] x1 = { 1, 200, 3, 4, 5, 6, 7, 8, 400, 10 };
            int[] y1 = { 1, 5, 3, 4, 5, 6, 7, 8, 7, 10 };

            int[] x2 = { };
            int[] y2 = { };

            int[] x3 = { 100, 200, 300, 400 };
            int[] y3 = { 100, 5, 9, 7 };

            int[] x4 = { 100, 11, 12, 13 };
            int[] y4 = { 100, 5, 9, 7 };
            ArrayList<Polygon> track = test(new Object[]{x1,y1},new Object[]{x2,y2},new Object[]{x3,y3},new Object[]{x4,y4});
            Polygon expIntersectionPoints =  new Polygon(new int[] {200,400,100  }, new int[] {5,7,100  }, 3);
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(200, 5)));
                    add(new Pair<Point, Point>(new Point(0, 2), new Point(400, 7)));
                    add(new Pair<Point, Point>(new Point(2, 3), new Point(100, 100)));
                }
            };
            ret.add(new Object[] {Type.INTERSECTION,track,expIntersectionPoints,expIntersectionPairs,TrackMapAnalyser.WITH_SELFCROSSING});
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
                    add(new Pair<Point, Point>(new Point(0, 1), new Point(6, 6)));
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {}, new int[] { }, 0);
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {2}, new int[] {2 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(3, 3), new Point())); // tested point
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {10}, new int[] {10 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(11, 11), new Point())); // tested point
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {9}, new int[] {9 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(10, 10), new Point())); // tested point
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {11}, new int[] {11 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(11, 11), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    70000 });
        }
        // last point from backtrack but no forward track is given
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = {  };
            int[] y2 = {  };

            ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {9}, new int[] {9 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(10, 10), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    0 });
        }
        // before last point from backtrack but no forward track is given
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = {  };
            int[] y2 = {  };

            ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {8}, new int[] {8 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(9, 9), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    0 });
        }
        // point from forwardtrack but no backward track is given
        {
            int[] x1 = { };
            int[] y1 = { };

            int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
            int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

            ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {1}, new int[] {1 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(11, 11), new Point())); // tested point
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
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {-1}, new int[] {-1}, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(110, 110), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    0 });
        }
        // point from backtrack but no forward track is given + include maximum
        {
            int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
            int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

            int[] x2 = {  };
            int[] y2 = {  };

            ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {9}, new int[] {9 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(10, 10), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    70000 });
        }
        // point from forwardtrack but no backward track is given + include maximum
        {
            int[] x1 = { };
            int[] y1 = { };

            int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
            int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

            ArrayList<Polygon> track = test(new Object[] { x1, y1 }, new Object[] { x2, y2 });
            Polygon expIntersectionPoints =
                    new Polygon(new int[] {1}, new int[] {1 }, 1); // expected index at x[0]
            @SuppressWarnings("serial")
            List<Pair<Point, Point>> expIntersectionPairs = new ArrayList<Pair<Point, Point>>() {
                {
                    add(new Pair<Point, Point>(new Point(11, 11), new Point())); // tested point
                }
            };
            ret.add(new Object[] { Type.ENUMERATE, track, expIntersectionPoints, expIntersectionPairs,
                    70000 });
        }
        
        
        return ret;
    }

    /**/
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        trackMapAnalyser = new TrackMapAnalyser();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.TrackMapAnalyser#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPointsParam() throws Exception {

        Assume.assumeTrue(type == Type.INTERSECTION);
        Polygon ret = trackMapAnalyser.getIntersectionPoints(track);
        assertThat(ret.xpoints, is(expIntersectionPoints.xpoints));
        assertThat(ret.ypoints, is(expIntersectionPoints.ypoints));

        List<Pair<Point, Point>> ret1 =
                trackMapAnalyser.getIntersectionParents(track, selfCrossing);
        assertThat(ret1, is(expIntersectionPairs));
    }

    /**
     * {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.TrackMapAnalyser#removeSelfRepeatings(java.util.List)}.
     * @throws Exception
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.TrackMapAnalyser#enumeratePoint(java.awt.Polygon, java.awt.Polygon, java.awt.Point)}.
     * Assume that tested point is first pair left in expIntersectionPairs
     * Result is in expIntersectionPoints as x[0] coord.
     * Use selfCrossing to decide whether use INCLUDE_INITIAL
     */
    @Test
    public void testEnumeratePoint() throws Exception {

        Assume.assumeTrue(type == Type.ENUMERATE);
        if (selfCrossing > 65535) {
            TrackMapAnalyser.INCLUDE_INITIAL_ONCE = false; // count all
        } else
            TrackMapAnalyser.INCLUDE_INITIAL_ONCE = true; // count maximum one
        int ret1 = TrackMapAnalyser.enumeratePoint(track.get(0), track.get(1),
                expIntersectionPairs.get(0).fst);
        assertThat(ret1, is(expIntersectionPoints.xpoints[0]));
    }

    /**
     * Helper method for build list of polygons using {x[],y[]} pairs.
     * 
     * @param obj
     * @return
     */
    static ArrayList<Polygon> test(Object[]... obj) {
        ArrayList<Polygon> track = new ArrayList<>();
        for (Object[] o : obj) {
            track.add(new Polygon((int[]) o[0], (int[]) o[1], ((int[]) o[0]).length));
        }
        return track;
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.TrackMapAnalyser#removeSelfRepeatings(java.util.List)}.
     * No selfcrossing, B1 B2 intersection
     */
    @Test
    @Ignore
    public void testRemoveSelfRepeatings_2() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
