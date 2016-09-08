package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point2i;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.tools.javac.util.Pair;

/**
 * @author baniuk
 *
 */
public class PointTrackerTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }

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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        Pair<Point2i, Point2i> p = new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(5, 5));

        List<Pair<Point2i, Point2i>> expected1 =
                new ArrayList<Pair<Point2i, Point2i>>(Arrays.asList(p));
        assertThat(ret1, is(expected1));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_nointersect() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] {}, new int[] {}, 0);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        assertThat(ret1.size(), is(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_2intersects() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 9, 1 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] { 5, 9 }, new int[] { 5, 9 }, 2);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        Pair<Point2i, Point2i> p = new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(5, 5));
        Pair<Point2i, Point2i> p1 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(9, 9));

        @SuppressWarnings("serial")
        List<Pair<Point2i, Point2i>> expected1 = new ArrayList<Pair<Point2i, Point2i>>() {
            {
                add(p);
                add(p1);
            }
        };

        assertThat(ret1, is(expected1));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_allthesame() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] { 2, 4, 6, 8, 10, 1, 3, 5, 7, 9 },
                new int[] { 2, 4, 6, 8, 10, 1, 3, 5, 7, 9 }, 10);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        Pair<Point2i, Point2i> p = new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(1, 1));
        Pair<Point2i, Point2i> p1 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(2, 2));
        Pair<Point2i, Point2i> p2 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(3, 3));
        Pair<Point2i, Point2i> p3 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(4, 4));
        Pair<Point2i, Point2i> p4 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(5, 5));
        Pair<Point2i, Point2i> p5 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(6, 6));
        Pair<Point2i, Point2i> p6 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(7, 7));
        Pair<Point2i, Point2i> p7 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(8, 8));
        Pair<Point2i, Point2i> p8 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(9, 9));
        Pair<Point2i, Point2i> p9 =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(10, 10));

        @SuppressWarnings("serial")
        List<Pair<Point2i, Point2i>> expected1 = new ArrayList<Pair<Point2i, Point2i>>() {
            {
                add(p);
                add(p1);
                add(p2);
                add(p3);
                add(p4);
                add(p5);
                add(p6);
                add(p7);
                add(p8);
                add(p9);
            }
        };

        assertThat(ret1, is(expected1));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_3() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

        int[] x3 = { 100, 6, 9, 40 };
        int[] y3 = { 100, 5, 9, 7 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));
        test.add(new Polygon(x3, y3, 4));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] { 6, 5, 9 }, new int[] { 5, 5, 9 }, 3);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        Pair<Point2i, Point2i> p = new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(5, 5));
        Pair<Point2i, Point2i> p1 =
                new Pair<Point2i, Point2i>(new Point2i(1, 2), new Point2i(6, 5));
        Pair<Point2i, Point2i> p2 =
                new Pair<Point2i, Point2i>(new Point2i(0, 2), new Point2i(9, 9));

        @SuppressWarnings("serial")
        List<Pair<Point2i, Point2i>> expected1 = new ArrayList<Pair<Point2i, Point2i>>() {
            {
                add(p);
                add(p2);
                add(p1);
            }
        };
        assertThat(ret1, is(expected1));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     * The same point many times. Will be returned once.
     */
    @Test
    public void testGetIntersectionPoints_multiple() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 6, 5, 70, 3, 2, 1 };

        int[] x3 = { 100, 61, 17, 40 };
        int[] y3 = { 100, 5, 70, 7 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));
        test.add(new Polygon(x3, y3, 4));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] { 17 }, new int[] { 70 }, 1);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);
        Pair<Point2i, Point2i> p =
                new Pair<Point2i, Point2i>(new Point2i(0, 1), new Point2i(17, 70));
        Pair<Point2i, Point2i> p1 =
                new Pair<Point2i, Point2i>(new Point2i(0, 2), new Point2i(17, 70));
        Pair<Point2i, Point2i> p2 =
                new Pair<Point2i, Point2i>(new Point2i(1, 2), new Point2i(17, 70));

        @SuppressWarnings("serial")
        List<Pair<Point2i, Point2i>> expected1 = new ArrayList<Pair<Point2i, Point2i>>() {
            {
                add(p);
                add(p1);
                add(p2);
            }
        };
        assertThat(ret1, is(expected1));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_oneelementinlist() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 17, 8, 9, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 70, 8, 9, 10 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);
        Polygon expected = new Polygon(new int[] {}, new int[] {}, 0);

        assertThat(ret.xpoints, is(expected.xpoints));
        assertThat(ret.ypoints, is(expected.ypoints));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITH_SELFCROSSING);

        assertThat(ret1.size(), is(0));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.PointTracker#getIntersectionPoints(java.util.List)}.
     */
    @Test
    public void testGetIntersectionPoints_withoutselfcrossing() throws Exception {
        int[] x1 = { 1, 2, 3, 4, 5, 6, 7, 8, 400, 10 };
        int[] y1 = { 1, 2, 3, 4, 5, 6, 7, 8, 7, 10 };

        int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        int[] y2 = { 10, 9, 8, 7, 5, 5, 4, 3, 2, 1 };

        int[] x3 = { 100, 200, 300, 400 };
        int[] y3 = { 100, 5, 9, 7 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));
        test.add(new Polygon(x3, y3, 4));

        List<Pair<Point2i, Point2i>> ret1 =
                new PointTracker().getIntersectionParents(test, PointTracker.WITHOUT_SELFCROSSING);
        Pair<Point2i, Point2i> p =
                new Pair<Point2i, Point2i>(new Point2i(0, 2), new Point2i(400, 7));

        @SuppressWarnings("serial")
        List<Pair<Point2i, Point2i>> expected1 = new ArrayList<Pair<Point2i, Point2i>>() {
            {
                add(p);
            }
        };
        assertThat(ret1, is(expected1));
        assertThat(test.size(), is(3));
    }

}
