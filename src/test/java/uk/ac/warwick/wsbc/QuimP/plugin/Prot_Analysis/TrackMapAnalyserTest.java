/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author baniuk
 *
 */
public class TrackMapAnalyserTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER =
            LogManager.getLogger(TrackMapAnalyserParamTest.class.getName());

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.ProtrusionVis#PolygonRoi2Map(java.util.List)}.
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

        TrackMapAnalyser.INCLUDE_INITIAL_ONCE = true;
        List<Point> ret = TrackMapAnalyser.Polygon2Point2i(p);
        List<Point> result = ret.stream().filter(e -> e.getX() == 1).collect(Collectors.toList());
        assertThat(result, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.TrackMapAnalyser#enumeratePoint(java.awt.Polygon, java.awt.Polygon, java.awt.Point)}.
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

        TrackMapAnalyser.INCLUDE_INITIAL_ONCE = true;
        Point testPoint1 = new Point(3, 3);
        int ret1 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint1);
        assertThat(ret1, is(2));

        TrackMapAnalyser.INCLUDE_INITIAL_ONCE = true;
        Point testPoint2 = new Point(11, 11);
        int ret2 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint2);
        assertThat(ret2, is(10));

        TrackMapAnalyser.INCLUDE_INITIAL_ONCE = false; // count all
        Point testPoint3 = new Point(11, 11);
        int ret3 = TrackMapAnalyser.enumeratePoint(test.get(0), test.get(1), testPoint3);
        assertThat(ret3, is(11));
    }

}
