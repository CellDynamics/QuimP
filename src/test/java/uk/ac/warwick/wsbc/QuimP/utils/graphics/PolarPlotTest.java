package uk.ac.warwick.wsbc.QuimP.utils.graphics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

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
import org.scijava.vecmath.Point2d;
import org.scijava.vecmath.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PolarPlotTest {
    static final Logger LOGGER = LoggerFactory.getLogger(PolarPlotTest.class.getName());
    @Mock
    private STmap mapCell;
    @InjectMocks
    private PolarPlot polarPlot = new PolarPlot(new STmap(), new Point2d(10, 10));

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
     * 
     * @throws java.lang.Exception
     * @see <a href=
     *      "link">http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/wiki/DataforPolarPlotTest</a>
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        double[][] xmap = { { 0, 1, 2, 3, 2, 1 }, { 20, 21, 22, 23, 22, 21 } };
        double[][] ymap = { { 0, 1, 1, 0, -1, -1 }, { 0, 1, 1, 0, -1, -1 } };
        double[][] motmap = { { 1, 1, 1, 1, 1, 1 }, { 20, 21, 22, 23, 24, 25 } };
        Mockito.when(mapCell.getMotMap()).thenReturn(motmap);
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
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getShift(java.awt.geom.Point2D.Double)}.
     * Mocked outline from 6 points, 2 frames. Gradient coord at {10,10}. Outline point at index 2
     * closest for first frame.
     * 
     */
    @Test
    public void testGetShift() throws Exception {

        int expectedindex[] = { 2, 0 }; // only 2 frames
        int c[] = polarPlot.getShift();
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
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getVectors(int, Point2d[], int[])}.
     */
    @Test
    public void testGetVectors() throws Exception {
        int f = 0;
        // expected in correct order
        //!<
        Vector2d[] expected = { new Vector2d(0.5, 1), new Vector2d(1.5, 0), new Vector2d(0.5, -1),
                new Vector2d(-0.5, -1), new Vector2d(-1.5, 0), new Vector2d(-0.5, 1) };
        /**/
        Vector2d ret[] = polarPlot.getVectors(f, polarPlot.getMassCentre(), polarPlot.getShift());
        assertThat(ret, is(expected));
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getAngles(Vector2d[], Vector2d)}.
     */
    @Test
    public void testGetAngles() throws Exception {

        int f = 0;
        //!<
        double[] expected = { 0, // rounded, assume that polygin is given in anticlock dir
                63, 127, 180, -117, -53 };
        /**/
        Vector2d v[] = polarPlot.getVectors(f, polarPlot.getMassCentre(), polarPlot.getShift());
        double ret[] = polarPlot.getAngles(v, v[0]);
        for (int i = 0; i < ret.length; i++)
            ret[i] = Math.round(ret[i] * 180 / Math.PI);
        assertThat(ret, is(expected));

    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#getRadius(int, int, double[][])}.
     */
    @Test
    public void testGetRadius() throws Exception {
        int f = 0;
        double[][] motmap = { { 0, 1, 2, 3, 4, 5 }, { 20, 21, 22, 23, 24, 25 } };
        double expected[] = { 2, 3, 4, 5, 0, 1 }; // shifted by 2
        int c[] = polarPlot.getShift();
        double[] ret = polarPlot.getRadius(0, c[f], motmap);
        assertThat(ret, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#generatePlot(int)}.
     */
    @Test
    public void testGeneratePlot() throws Exception {
        polarPlot.generatePlotFrame("/tmp/test.svg", 0);
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.PolarPlot#generatePlot(int)}.
     * Compared with matlab
     */
    @Test
    public void testGeneratePlot_1() throws Exception {
        QconfLoader qconfLoader = new QconfLoader(new File(
                "src/test/resources/ProtAnalysisTest/KZ4/KZ4-220214-cAR1-GFP-devel5.QCONF"));
        PolarPlot pp = new PolarPlot(qconfLoader.getQ()[0], new Point2d(512, 512));
        pp.generatePlot("/tmp/test_1.svg");
        pp.generatePlotFrame("/tmp/test_1f2.svg", 1);
    }

}
