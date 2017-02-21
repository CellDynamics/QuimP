/**
 * 
 */
package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import uk.ac.warwick.wsbc.quimp.plugin.protanalysis.Track;
import uk.ac.warwick.wsbc.quimp.plugin.protanalysis.TrackCollection;
import uk.ac.warwick.wsbc.quimp.plugin.protanalysis.TrackMapAnalyser;
import uk.ac.warwick.wsbc.quimp.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
@RunWith(JUnitParamsRunner.class)
public class TrackMapAnalyserTest {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(TrackMapAnalyserTest.class.getName());

    // https://lkrnac.net/blog/2014/01/mock-autowired-fields/
    @Mock
    private TrackCollection trackCollection;
    @InjectMocks
    private TrackMapAnalyser trackMapAnalyser;

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
        MockitoAnnotations.initMocks(this);
        // trackMapAnalyser = Mockito.spy(new TrackMapAnalyser()); // injectmock does the job
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.protanalysis.TrackMapAnalyser#Polygon2Point2i(List)}.
     * 
     * @throws Exception
     */
    @Test
    public void testPolygon2Map() throws Exception {
        List<Point> expected = new ArrayList<>();
        expected.add(new Point(1, 11));
        expected.add(new Point(1, 44));
        int[] x1 = { 1, 2, 3, 1 };
        int[] y1 = { 11, 22, 33, 44 };
        int[] x2 = { 101, 102, 103 };
        int[] y2 = { 111, 112, 113 };

        ArrayList<Polygon> p = new ArrayList<>();
        p.add(new Polygon(x1, y1, x1.length));
        p.add(new Polygon(x2, y2, x2.length));

        TrackMapAnalyser.INCLUDE_INITIAL = true;
        List<Point> ret = TrackMapAnalyser.Polygon2Point2i(p);
        List<Point> result = ret.stream().filter(e -> e.getX() == 1).collect(Collectors.toList());
        assertThat(result, is(expected));
    }

    /**
     * Sub list.
     */
    @SuppressWarnings("serial")
    @Test
    public void subList() {
        ArrayList<Integer> a = new ArrayList<Integer>() {
            {
                add(1);
                add(2);
                add(3);
            }
        };
        List<Integer> ret = a.subList(0, 0);
        assertThat(ret.size(), is(0));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.protanalysis.TrackMapAnalyser#enumeratePoint(java.awt.Polygon, java.awt.Polygon, java.awt.Point)}.
     * 
     * @throws Exception
     */
    @Test
    public void testEnumeratePoint() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
        int[] y2 = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        TrackMapAnalyser.INCLUDE_INITIAL = true;
        Point testPoint1 = new Point(3, 3);
        int ret1 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint1);
        assertThat(ret1, is(2));

        TrackMapAnalyser.INCLUDE_INITIAL = true;
        Point testPoint2 = new Point(11, 11);
        int ret2 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint2);
        assertThat(ret2, is(10));

        TrackMapAnalyser.INCLUDE_INITIAL = false; // count all
        Point testPoint3 = new Point(11, 11);
        int ret3 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint3);
        assertThat(ret3, is(11));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.protanalysis.TrackMapAnalyser#getCommonPoints()}.
     * 
     * @param tracks
     * @param expected
     * @throws Exception
     */
    @Test
    @Parameters(method = "valuesCommonPoints")
    public void testGetCommonPoints(List<Pair<Track, Track>> tracks, Polygon expected)
            throws Exception {
        Mockito.when(trackCollection.getBf()).thenReturn(tracks);
        Polygon ret = trackMapAnalyser.getCommonPoints();
        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));
    }

    @SuppressWarnings({ "serial", "unused" })
    private Object[] valuesCommonPoints() {
        //!<
        List<Object[]> ret = new ArrayList<>();
        {
            // empty track - use on empty object
            List<Pair<Track, Track>> tracks = new ArrayList<>();
            Polygon exp = new Polygon(new int[] {}, new int[] {}, 0);
            ret.add(new Object[] { tracks, exp });
        }
        { // two tracks from the same origin, they have common point nut it should not be detected
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(5, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                }
            };
            Polygon exp = new Polygon(new int[] {}, new int[] {}, 0);
            ret.add(new Object[] { tracks, exp });
        }
        { // three tracks - second without backtrack no common points
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(5, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            Track b2 = new Track() {
                {
                    add(new Point(0, 10));
                    add(new Point(1, 9));
                    add(new Point(2, 8));
                    add(new Point(3, 7));
                    add(new Point(4, 6));
                    add(new Point(3, 4));
                }
            };
            Track f2 = new Track() {
                {

                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                    add(new Pair<Track, Track>(b2, f2));
                }
            };
            Polygon exp = new Polygon(new int[] {}, new int[] {}, 0);
            ret.add(new Object[] { tracks, exp });
        }
        { // three tracks - second without backtrack 1 common point b1-b2
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(5, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            Track b2 = new Track() {
                {
                    add(new Point(0, 10));
                    add(new Point(1, 9));
                    add(new Point(2, 8));
                    add(new Point(3, 7));
                    add(new Point(4, 6));
                    add(new Point(5, 5));
                }
            };
            Track f2 = new Track() {
                {

                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                    add(new Pair<Track, Track>(b2, f2));
                }
            };
            Polygon exp = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);
            ret.add(new Object[] { tracks, exp });
        }
        { // four tracks 1 common point b1-b2 and b1-f2 and f1-b2(the same point twice)
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(5, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            Track b2 = new Track() {
                {
                    add(new Point(0, 10));
                    add(new Point(1, 9));
                    add(new Point(2, 8));
                    add(new Point(3, 7));
                    add(new Point(4, 6));
                    add(new Point(5, 5));
                }
            };
            Track f2 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 4));
                    add(new Point(7, 3));
                    add(new Point(8, 2));
                    add(new Point(9, 1));
                    add(new Point(10, 0));

                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                    add(new Pair<Track, Track>(b2, f2));
                }
            };
            Polygon exp = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);
            ret.add(new Object[] { tracks, exp });
        }
        { // four tracks 2 common point (b1-b2 b1-f2 f1-b2) and f1-b2
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(5, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            Track b2 = new Track() {
                {
                    add(new Point(0, 10));
                    add(new Point(1, 9));
                    add(new Point(2, 8));
                    add(new Point(7, 7));
                    add(new Point(4, 6));
                    add(new Point(5, 5));
                }
            };
            Track f2 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 4));
                    add(new Point(7, 3));
                    add(new Point(8, 2));
                    add(new Point(9, 1));
                    add(new Point(10, 0));

                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                    add(new Pair<Track, Track>(b2, f2));
                }
            };
            Polygon exp = new Polygon(new int[] { 5, 7 }, new int[] { 5, 7 }, 2);
            ret.add(new Object[] { tracks, exp });
        }
        { // four tracks 4 common point (b1-b2 b1-f2 f1-b2 f1-f2) + repeating
            Track b1 = new Track() {
                {
                    add(new Point(0, 0));
                    add(new Point(1, 1));
                    add(new Point(2, 2));
                    add(new Point(3, 3));
                    add(new Point(4, 4));
                    add(new Point(50, 5));
                }
            };
            Track f1 = new Track() {
                {
                    add(new Point(50, 5));
                    add(new Point(6, 6));
                    add(new Point(7, 7));
                    add(new Point(8, 8));
                    add(new Point(9, 9));
                    add(new Point(10, 10));
                }
            };
            Track b2 = new Track() {
                {
                    add(new Point(0, 10));
                    add(new Point(1, 1));
                    add(new Point(2, 8));
                    add(new Point(7, 7));
                    add(new Point(4, 6));
                    add(new Point(5, 5));
                }
            };
            Track f2 = new Track() {
                {
                    add(new Point(5, 5));
                    add(new Point(6, 4));
                    add(new Point(7, 3));
                    add(new Point(8, 2));
                    add(new Point(2, 2));
                    add(new Point(10, 10));

                }
            };
            List<Pair<Track, Track>> tracks = new ArrayList<Pair<Track, Track>>() {
                {
                    add(new Pair<Track, Track>(b1, f1));
                    add(new Pair<Track, Track>(b2, f2));
                }
            };
            Polygon exp = new Polygon(new int[] { 1, 2, 7, 10 }, new int[] { 1, 2, 7, 10 }, 4);
            ret.add(new Object[] { tracks, exp });
        }
        return ret.toArray();
        /**/
    }

}
