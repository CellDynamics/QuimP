package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.awt.Polygon;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.QParamsQconf;
import uk.ac.warwick.wsbc.quimp.filesystem.QconfLoader;
import uk.ac.warwick.wsbc.quimp.plugin.protanalysis.MaximaFinder;
import uk.ac.warwick.wsbc.quimp.plugin.qanalysis.STmap;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;

// TODO: Auto-generated Javadoc
/**
 * Test class for {@link uk.ac.warwick.wsbc.quimp.plugin.protanalysis.MaximaFinder}
 * 
 * @author p.baniukiewicz
 *
 */
public class MaximaFinderTest {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(MaximaFinderTest.class.getName());
    
    /**
     * The q L 1.
     */
    static QconfLoader qL1;
    private STmap[] stMap;
    private ImageProcessor imp;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        qL1 = new QconfLoader(Paths
                .get("src/test/resources/TrackMapTests/fluoreszenz-test_eq_smooth.QCONF").toFile());
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
        stMap = ((QParamsQconf) qL1.getQp()).getLoadedDataContainer().QState;
        float[][] motMap = QuimPArrayUtils.double2float(stMap[0].motMap);
        // rotate and flip to match orientation of ColorProcessor (QuimP default)
        imp = new FloatProcessor(motMap).rotateRight();
        imp.flipHorizontal();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.plugin.protanalysis.MaximaFinder#MaximaFinder(ij.process.ImageProcessor)}.
     * 
     * Results compared with those generated in IJ from
     * src/test/resources/ProtAnalysisTest/fluoreszenz-test_eq_smooth_0_motilityMap.maQP when
     * imported as text image.
     * 
     * @throws Exception
     */
    @Test
    public void testMaximaFinder() throws Exception {
        int expectedX[] = { 160, 110, 134, 266, 40, 359, 79, 236, 288, 273, 212, 127, 73, 8, 331,
                70, 270, 147, 368, 13 };
        int expectedY[] = { 20, 89, 129, 62, 63, 97, 50, 77, 31, 126, 80, 132, 57, 42, 58, 15, 102,
                31, 103, 40 };
        // these values have been read from matlab using above points
        double[] expectedVal = { 21.00, 15.15, 15.06, 15.05, 14.58, 14.52, 14.48, 14.34, 14.14,
                13.28, 13.03, 12.44, 11.83, 11.75, 11.55, 11.08, 11.07, 10.90, 10.64, 10.43 };
        MaximaFinder mF = new MaximaFinder(imp);
        mF.computeMaximaIJ(10);
        Polygon ret = mF.getMaxima();
        double[] val = mF.getMaxValues();
        LOGGER.debug(Arrays.toString(val));
        assertThat(ret.xpoints, is(expectedX));
        assertThat(ret.ypoints, is(expectedY));
        assertArrayEquals(expectedVal, val, 1e-2);
    }

}
