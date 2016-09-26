/**
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader;

/**
 * Test class for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker}. 
 * @author p.baniukiewicz
 *
 */
public class TrackMapTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(TrackMapTest.class.getName());
    static QconfLoader qL1;
    double[][] originMap1;
    double[][] coordMap1;

    static QconfLoader qL2;
    double[][] originMap2;
    double[][] coordMap2;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        qL1 = new QconfLoader(Paths
                .get("src/test/resources/TrackMapTests/Stack_cut_10frames_trackMapTest.QCONF"));
        qL2 = new QconfLoader(
                Paths.get("src/test/resources/TrackMapTests/fluoreszenz-test_eq_smooth.QCONF"));
    }// throw new UnsupportedOperationException("Not implemented here");

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
        coordMap1 = qL1.getQp().getLoadedDataContainer().QState[0].coordMap;
        originMap1 = qL1.getQp().getLoadedDataContainer().QState[0].originMap;

        coordMap2 = qL2.getQp().getLoadedDataContainer().QState[0].coordMap;
        originMap2 = qL2.getQp().getLoadedDataContainer().QState[0].originMap;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#TrackMap(double[][], double[][])}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @Test
    public void testTrackMap() throws Exception {
        //!<
        int[][] forwardExpected = {     {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1}};
        int[][] backwardExpected = {    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9},
                                        {0,1,2,3,4,5,6,7,8,9}};
        /**/
        MapTracker tM = new MapTracker(originMap1, coordMap1);
        assertThat(tM.forwardMap, is(forwardExpected));
        assertThat(tM.backwardMap, is(backwardExpected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackForward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTrackForward_1() throws Exception {
        int[] expected = { 4, 4, 4, 4, 4, 4, 4, 4, 4, -1 };
        MapTracker tM = new MapTracker(originMap1, coordMap1);
        int[] ret = tM.trackForward(0, 4, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackForwardValid(int, int, int)}.
     */
    @SuppressWarnings("serial")
    @Test
    public void testTrackForwardValid_1() throws Exception {
        ArrayList<Point> e = new ArrayList<Point>() {
            {
                add(new Point(1, 4));
                add(new Point(2, 4));
                add(new Point(3, 4));
                add(new Point(4, 4));
                add(new Point(5, 4));
                add(new Point(6, 4));
                add(new Point(7, 4));
                add(new Point(8, 4));
                add(new Point(9, 4));

            }
        };
        MapTracker tM = new MapTracker(originMap1, coordMap1);
        ArrayList<Point> ret = (ArrayList<Point>) tM.trackForwardValid(0, 4, 10);
        assertThat(ret, is(e));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackForward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTrackForward_2() throws Exception {
        //!<
        int[] expected = {  262-1,
                            259-1,
                            263-1,
                            269-1,
                            265-1,
                            274-1,
                            276-1,
                            265-1,
                            277-1,
                            276-1 };
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        int[] ret = tM.trackForward(90 - 1, 272 - 1, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackForwardValid(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("serial")
    @Test
    public void testTrackForwardValid_2() throws Exception {
        //!<
        ArrayList<Point> e = new ArrayList<Point>() {{
            add(new Point(90,262-1));
            add(new Point(91,259-1));
            add(new Point(92,263-1));
            add(new Point(93,269-1));
            add(new Point(94,265-1));
            add(new Point(95,274-1));
            add(new Point(96,276-1));
            add(new Point(97,265-1));
            add(new Point(98,277-1));
            add(new Point(99,276-1));
        }};
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        ArrayList<Point> ret = (ArrayList<Point>) tM.trackForwardValid(90 - 1, 272 - 1, 10);
        assertThat(ret, is(e));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#getForwardFrames(int, int)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetForwardFrames() throws Exception {
      //!<
        int[] expected = {  91-1,
                            92-1,
                            93-1,
                            94-1,
                            95-1,
                            96-1,
                            97-1,
                            98-1,
                            99-1,
                            100-1 };
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        int[] ret = tM.getForwardFrames(90 - 1, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackBackward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTrackBackward_1() throws Exception {
        int[] expected = { -1, 4, 4, 4, 4, 4 };
        MapTracker tM = new MapTracker(originMap1, coordMap1);
        int[] ret = tM.trackBackward(5, 4, 6);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackBackwardValid(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("serial")
    @Test
    public void testTrackBackwardValid_1() throws Exception {
        ArrayList<Point> e = new ArrayList<Point>() {
            {
                add(new Point(0, 4));
                add(new Point(1, 4));
                add(new Point(2, 4));
                add(new Point(3, 4));
                add(new Point(4, 4));
            }
        };
        MapTracker tM = new MapTracker(originMap1, coordMap1);
        ArrayList<Point> ret = (ArrayList<Point>) tM.trackBackwardValid(5, 4, 6);
        assertThat(ret, is(e));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackBackward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testTrackBackward_2() throws Exception {
      //!<
        int[] expected = {  303-1,
                            301-1,
                            300-1,
                            297-1,
                            291-1,
                            287-1,
                            278-1,
                            282-1,
                            278-1,
                            284-1,
                            281-1,
                            292-1,
                            294-1,
                            283-1,
                            297-1};
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        int[] ret = tM.trackBackward(100 - 1, 300 - 1, 15);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#trackBackwardValid(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @SuppressWarnings("serial")
    @Test
    public void testTrackBackwardValid_2() throws Exception {
      //!<
        ArrayList<Point> e = new ArrayList<Point>() {
            {
                add(new Point(84, 303-1));
                add(new Point(85, 301-1));
                add(new Point(86, 300-1));
                add(new Point(87, 297-1));
                add(new Point(88, 291-1));
                add(new Point(89, 287-1));
                add(new Point(90, 278-1));
                add(new Point(91, 282-1));
                add(new Point(92, 278-1));
                add(new Point(93, 284-1));
                add(new Point(94, 281-1));
                add(new Point(95, 292-1));
                add(new Point(96, 294-1));
                add(new Point(97, 283-1));
                add(new Point(98, 297-1));

            }
        };
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        ArrayList<Point> ret = (ArrayList<Point>) tM.trackBackwardValid(100 - 1, 300 - 1, 15);
        assertThat(ret, is(e));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.MapTracker#getBackwardFrames(int, int)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetBackwardFrames() throws Exception {
      //!<
        int[] expected = {  85-1,
                            86-1,
                            87-1,
                            88-1,
                            89-1,
                            90-1,
                            91-1,
                            92-1,
                            93-1,
                            94-1,
                            95-1,
                            96-1,
                            97-1,
                            98-1,
                            99-1};
        /**/
        MapTracker tM = new MapTracker(originMap2, coordMap2);
        int[] ret = tM.getBackwardFrames(100 - 1, 15);
        assertThat(ret, is(expected));
    }
}
