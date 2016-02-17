package uk.ac.warwick.wsbc.tools.images.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;

/**
 * Test class for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
public class HatFilter_test {

    private static final Logger LOGGER = LogManager
            .getLogger(HatFilter_test.class.getName());
    private List<Vector2d> input;

    @Rule
    public TestName name = new TestName(); /// < Allow to get tested method name
                                           /// (called at setUp())

    /**
     * Create line with nodes in every 1 unit.
     * 
     * Three middle nodes are moved to y=1:
     * 
     * @code --- ----------------- -------------------- 0 39
     * @endcode
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        input = new ArrayList<>();
        for (int i = 0; i < 40; i++)
            input.add(new Vector2d(i, 0));
        input.set(18, new Vector2d(18, 1));
        input.set(19, new Vector2d(19, 1));
        input.set(20, new Vector2d(20, 1));
        LOGGER.info("Entering " + name.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * @test test of HatFilter method
     * @pre vector line defined in setUp()
     * @post all nodes accepted. input==output
     * @throws QuimpPluginException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_case1() throws QuimpPluginException {
        LOGGER.debug("input: " + input.toString());
        HatFilter hf = new HatFilter();
        hf.attachData(input);
        hf.setPluginConfig(new HashMap<String, Object>() {
            {
                put("window", 5.0);
                put("crown", 3.0);
                put("sigma", 1.0);
            }
        });
        ArrayList<Vector2d> out = (ArrayList<Vector2d>) hf.runPlugin();
        LOGGER.debug("  out: " + out.toString());
        assertEquals(input, out);
    }

    /**
     * @test test of HatFilter method
     * @pre vector line defined in setUp()
     * @post nodes 0, 1, 2, 37, 38, 39, 15, 16, 17, 18, 19, 20, 21, 22, 23
     * removed
     * @throws QuimpPluginException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_case2() throws QuimpPluginException {
        LOGGER.debug("input: " + input.toString());
        HatFilter hf = new HatFilter();
        hf.attachData(input);
        hf.setPluginConfig(new HashMap<String, Object>() {
            {
                put("window", 5.0);
                put("crown", 3.0);
                put("sigma", 0.05);
            }
        });
        ArrayList<Vector2d> out = (ArrayList<Vector2d>) hf.runPlugin();
        LOGGER.debug("  out: " + out.toString());

        // remove precalculated indexes from input array (see Matlab test code)
        int removed[] = { 0, 1, 2, 37, 38, 39, 15, 16, 17, 18, 19, 20, 21, 22,
                23 };
        Arrays.sort(removed);
        int lr = 0;
        for (int el : removed)
            input.remove(el - lr++);
        LOGGER.debug(input.toString());
        assertEquals(input, out);
    }

    /**
     * @test test set and get parameters to/from filter
     * @pre given parameters
     * @post the same parameters received from filter
     * @see HatFilter_run for veryfing diaplaying set parameters.
     * @throws QuimpPluginException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_setget() throws QuimpPluginException {
        HatFilter hf = new HatFilter();
        hf.attachData(input);
        hf.setPluginConfig(new HashMap<String, Object>() {
            {
                put("window", 5.0);
                put("crown", 3.0);
                put("sigma", 0.05);
            }
        });
        HashMap<String, Object> ret = (HashMap<String, Object>) hf
                .getPluginConfig();
        assertEquals(5.0, (Double) ret.get("window"), 1e-4);
        assertEquals(3.0, (Double) ret.get("crown"), 1e-4);
        assertEquals(0.05, (Double) ret.get("sigma"), 1e-4);
    }

    /**
     * @test Input condition for HatFilter
     * @pre Various bad combinations of inputs
     * @post Exception FilterException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_case3() {
        try {
            HatFilter hf = new HatFilter(); // even window
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 6.0);
                    put("crown", 3.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // even crown
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 5.0);
                    put("crown", 4.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // crown>window
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 5.0);
                    put("crown", 5.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // bad crown
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 5.0);
                    put("crown", 0.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // bad crown
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 0.0);
                    put("crown", 3.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // bad crown
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 0.0);
                    put("crown", -3.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatFilter hf = new HatFilter(); // bad crown
            hf.attachData(input);
            hf.setPluginConfig(new HashMap<String, Object>() {
                {
                    put("window", 1.0);
                    put("crown", 1.0);
                    put("sigma", 1.0);
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
    }

}
