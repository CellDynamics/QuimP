
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import uk.ac.warwick.wsbc.QuimP.plugin.ParamList;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.DataLoader;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * Test class for HatFilter
 * 
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
public class HatFilter_Test {

    private static final Logger LOGGER = LogManager.getLogger(HatFilter_Test.class.getName());
    private List<Point2d> input;
    private List<Point2d> lininput; //!< line at 45 deg
    private List<Point2d> circ; //!< circular object <EM>../src/test/resources/HatFilter.m</EM>
    /**
     * simulated protrusions
     * %% protrusions - generate test data from <EM>../src/test/resources/HatFilter.m</EM>
     */
    private List<Point2d> prot;

    @Rule
    public TestName name = new TestName(); //!< Allow to get tested method name (called at setUp())

    /**
     * Load all data
     * 
     * @throws Exception
     * @see ../src/test/resources/HatFilter.m
     */
    @Before
    public void setUp() throws Exception {
        input = new ArrayList<>();
        for (int i = 0; i < 40; i++)
            input.add(new Point2d(i, 0));
        input.set(18, new Point2d(18, 1));
        input.set(19, new Point2d(19, 1));
        input.set(20, new Point2d(20, 1));
        LOGGER.info("Entering " + name.getMethodName());

        lininput = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            lininput.add(new Point2d(i, i));

        circ = new DataLoader("../src/test/resources/testData_circle.dat").getData();

        prot = new DataLoader("../src/test/resources/testData_prot.dat").getData();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * @test Test of HatSnakeFilter_.runPlugin()
     * @pre Ideally circular object
     * @post In logs:
     * -# Weighting the same
     * -# circularity the same 
     * @throws QuimpPluginException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_run() throws QuimpPluginException {
        LOGGER.debug("input: " + circ.toString());
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(circ);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", "5");
                put("pnum", "1");
                put("alev", "0");
            }
        });
        hf.runPlugin();
    }

    /**
     * @test Test of HatSnakeFilter_.runPlugin()
     * @pre Simulated protrusions
     * @post Logs are comparable with script <EM>../src/test/resources/HatFilter.m</EM>
     * After run go to folder mentioned above and run 
     * <EM>%% protrusions - load java results and compare with matlab</EM> to verify results
     * @throws QuimpPluginException
     * @warning This matlab code is not fully compatible with java. Some results differ
     * Matlab dont accept windows lying on beginning because they have indexes 1-max.
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_run_2() throws QuimpPluginException {
        LOGGER.debug("input: " + prot.toString());
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(prot);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", "9");
                put("pnum", "3");
                put("alev", "0");
            }
        });
        List<Point2d> out = hf.runPlugin();
        RoiSaver.saveROI("/tmp/test_HatFilter_run_2.tif", out);
    }

    /**
     * @test Test of HatSnakeFilter_.runPlugin()
     * @pre Linear object
     * @post In logs:
     * -# Weighting differ at end
     * -# circularity differ at end
     * -# Window is moving and has circular padding
     * @throws QuimpPluginException
     */
    @SuppressWarnings("serial")
    @Test
    public void test_HatFilter_run_1() throws QuimpPluginException {
        LOGGER.debug("input: " + lininput.toString());
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(lininput);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", "5");
                put("pnum", "1");
                put("alev", "0");
            }
        });
        hf.runPlugin();
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
        HatSnakeFilter_ hf = new HatSnakeFilter_();
        hf.attachData(input);
        hf.setPluginConfig(new ParamList() {
            {
                put("window", "5");
                put("pnum", "1");
                put("alev", "0.23");
            }
        });
        ParamList ret = hf.getPluginConfig();
        assertEquals(5, ret.getIntValue("Window"));
        assertEquals(1, ret.getIntValue("pnum"));
        assertEquals(0.23, ret.getDoubleValue("alev"), 1e-4);
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
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // even window
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "6");
                    put("pnum", "3");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // neg window
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "-5");
                    put("pnum", "3");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // too long win
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "600");
                    put("pnum", "3");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // to small window
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "1");
                    put("pnum", "3");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // bad protrusions
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "5");
                    put("pnum", "0");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // bad acceptance
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "5");
                    put("pnum", "3");
                    put("alev", "-1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
        try {
            HatSnakeFilter_ hf = new HatSnakeFilter_(); // bad crown
            hf.attachData(input);
            hf.setPluginConfig(new ParamList() {
                {
                    put("window", "6");
                    put("pnum", "-4");
                    put("alev", "1.0");
                }
            });
            hf.runPlugin();
            fail("Exception not thrown");
        } catch (QuimpPluginException e) {
            assertTrue(e != null);
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * @test Test of WindowIndRange class
     * @pre Separated ranges of indexes
     * @post All ranges are added to list
     */
    @Test
    public void testWindowIndRange_1() {
        TreeSet<WindowIndRange> p = new TreeSet<>();
        assertTrue(p.add(new WindowIndRange(1, 5)));
        assertTrue(p.add(new WindowIndRange(6, 10)));
        assertTrue(p.add(new WindowIndRange(-5, 0)));
        LOGGER.debug(p.toString());
    }

    /**
     * @test Test of WindowIndRange class
     * @pre Overlap ranges of indexes
     * @post Overlap ranges are not added to list
     */
    @Test
    public void testWindowIndRange_2() {
        TreeSet<WindowIndRange> p = new TreeSet<>();
        assertTrue(p.add(new WindowIndRange(1, 5)));
        assertTrue(p.add(new WindowIndRange(7, 10)));
        assertTrue(p.add(new WindowIndRange(-5, 0)));

        assertFalse(p.add(new WindowIndRange(7, 8)));
        assertFalse(p.add(new WindowIndRange(10, 12)));
        assertFalse(p.add(new WindowIndRange(9, 12)));
        assertFalse(p.add(new WindowIndRange(4, 7)));
        assertFalse(p.add(new WindowIndRange(4, 6)));
        assertFalse(p.add(new WindowIndRange(-5, 0)));
        LOGGER.debug(p.toString());
    }

    /**
     * @test Test of WindowIndRange class
     * Test if particular point is included in any range stored in TreeSet
     */
    @Test
    public void testWindowIndRange_3() {
        TreeSet<WindowIndRange> p = new TreeSet<>();
        assertTrue(p.add(new WindowIndRange(1, 5)));
        assertTrue(p.add(new WindowIndRange(6, 10)));
        assertTrue(p.add(new WindowIndRange(-5, 0)));

        assertTrue(p.contains(new WindowIndRange(2, 2)));
        assertTrue(p.contains(new WindowIndRange(6, 6)));
        assertFalse(p.contains(new WindowIndRange(11, 11)));

        LOGGER.debug(p.toString());
    }

}
