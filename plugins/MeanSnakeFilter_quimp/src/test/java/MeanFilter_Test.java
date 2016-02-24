
/**
 * @file MeanFilter_Test.java
 */

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * Test runner for Interpolate class.
 * 
 * Contain only simple non-parameterized tests
 * 
 * @author baniuk
 *
 */
public class MeanFilter_Test {

    private List<Vector2d> testcase;
    private static final Logger LOGGER =
            LogManager.getLogger(MeanFilter_Test.class.getName());

    /**
     * Called after construction but before tests
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        testcase = new ArrayList<Vector2d>();
        for (int i = 1; i <= 10; i++)
            testcase.add(new Vector2d(i, i));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        testcase = null;
    }

    /**
     * @test Test of getInterpolationMean method
     * @throws QuimpPluginException
     * @pre Vector of 1-10 elements
     * @post Running mean for window 3: [4.3333 2.0000 3.0000 4.0000 5.0000
     * 6.0000 7.0000 8.0000 9.0000 6.6667]
     */
    @SuppressWarnings("serial")
    @Test
    public void test_getInterpolationMean() throws QuimpPluginException {
        MeanSnakeFilter_ in = new MeanSnakeFilter_();
        in.attachData(testcase);
        Integer window = 3;
        in.setPluginConfig(new ParamList() {
            {
                put("Window", String.valueOf(window));
            }
        });
        double[] expected = { 4.3333, 2.0000, 3.0000, 4.0000, 5.0000, 6.0000,
                7.0000, 8.0000, 9.0000, 6.6667 };

        List<Vector2d> out;
        out = (List<Vector2d>) in.runPlugin();
        LOGGER.debug("org     : " + testcase.toString());
        LOGGER.debug("Window 3: " + out.toString());

        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i], out.get(i).getX(), 1e-4);
            assertEquals(out.get(i).getX(), out.get(i).getY(), 1e-6);
        }

    }

}
