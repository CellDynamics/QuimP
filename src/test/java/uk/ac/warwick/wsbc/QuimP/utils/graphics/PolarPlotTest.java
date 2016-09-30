package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PolarPlotTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(PolarPlotTest.class.getName());
    @Mock
    private STmap mapCell;
    @InjectMocks
    private PolarPlot polarPlot;

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
     * Define two frames composed from 5-points outline.
     * @throws java.lang.Exception
     * @see http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/DataforPolarPlotTest
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        double[][] xmap = { { 0, 1, 2, 3, 2, 1 }, { 20, 21, 22, 23, 22, 21 } };
        double[][] ymap = { { 0, 1, 1, 0, -1, -1 }, { 0, 1, 1, 0, -1, -1 } };
        Mockito.when(mapCell.getxMap()).thenReturn(xmap);
        Mockito.when(mapCell.getyMap()).thenReturn(ymap);
        Mockito.when(mapCell.getT()).thenReturn(xmap.length);
        Mockito.when(mapCell.getRes()).thenReturn(xmap[0].length);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getShift(java.awt.geom.Point2D.Double)}.
     * Mocked outline from 6 points, 2 frames. Gradient coord at {10,10}. Outline point
     * at index 2 closest for first frame.
     * 
     */
    @Test
    public void testGetShift() throws Exception {

        int expectedindex[] = { 2, 0 }; // only 2 frames
        int c[] = polarPlot.getShift(new Point2d(10, 10));
        assertThat(c, is(expectedindex));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getMassCentre()}.
     */
    @Test
    public void testGetMassCentre() throws Exception {
        Point2d expected[] = { new Point2d(1.5, 0), new Point2d(21.5, 0) };
        Point2d[] ret = polarPlot.getMassCentre();
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getVectors(int, javax.vecmath.Point2d[], int[])}.
     */
    @Test
    public void testGetVectors() throws Exception {
        int f = 0;
        // expected in correct order
        //!<
        Vector2d[] expected = {new Vector2d(0.5,1),
                               new Vector2d(1.5,0),
                               new Vector2d(0.5,-1),
                               new Vector2d(-0.5,-1),
                               new Vector2d(-1.5,0),
                               new Vector2d(-0.5,1)};
        /**/
        Vector2d ret[] = polarPlot.getVectors(f, polarPlot.getMassCentre(),
                polarPlot.getShift(new Point2d(10, 10)));
        assertThat(ret, is(expected));
    }

}
