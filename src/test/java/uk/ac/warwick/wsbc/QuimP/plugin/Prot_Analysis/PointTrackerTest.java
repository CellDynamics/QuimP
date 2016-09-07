package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Polygon;
import java.util.ArrayList;

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
        int[] y2 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

        ArrayList<Polygon> test = new ArrayList<>();
        test.add(new Polygon(x1, y1, 10));
        test.add(new Polygon(x2, y2, 10));

        Polygon ret = new PointTracker().getIntersectionPoints(test);

        Polygon expected = new Polygon(new int[] { 5 }, new int[] { 5 }, 1);

        assertThat(ret, is(expected));
    }

}
