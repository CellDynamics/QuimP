/**
 * @file TrackMapTest.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginCore;

/**
 * Test class for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap}. 
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#TrackMap(double[][], double[][])}.
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
        TrackMap tM = new TrackMap(originMap1, coordMap1);
        assertThat(tM.forwardMap, is(forwardExpected));
        assertThat(tM.backwardMap, is(backwardExpected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#trackForward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @Test
    public void testTrackForward_1() throws Exception {
        int[] expected = { 4, 4, 4, 4, 4, 4, 4, 4, 4, -1 };
        TrackMap tM = new TrackMap(originMap1, coordMap1);
        int[] ret = tM.trackForward(0, 4, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#trackForward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
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
        TrackMap tM = new TrackMap(originMap2, coordMap2);
        int[] ret = tM.trackForward(90 - 1, 272 - 1, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#getForwardFrames(int, int)}.
     */
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
        TrackMap tM = new TrackMap(originMap2, coordMap2);
        int[] ret = tM.getForwardFrames(90 - 1, 10);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#trackBackward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
    @Test
    public void testTrackBackward_1() throws Exception {
        int[] expected = { -1, 4, 4, 4, 4, 4 };
        TrackMap tM = new TrackMap(originMap1, coordMap1);
        int[] ret = tM.trackBackward(5, 4, 6);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#trackBackward(int, int, int)}.
     * <p>
     * Output results generated in Matlab by TrackMapTests/main.m
     */
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
        TrackMap tM = new TrackMap(originMap2, coordMap2);
        int[] ret = tM.trackBackward(100 - 1, 300 - 1, 15);
        assertThat(ret, is(expected));
    }

}

/**
 * Simple loader of QCONF file.
 * 
 * @author p.baniukiewicz
 *
 */
class QconfLoader extends QuimpPluginCore {

    /**
     * 
     */
    public QconfLoader() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param path
     */
    public QconfLoader(Path path) {
        super(path);
        // TODO Auto-generated constructor stub
    }

}