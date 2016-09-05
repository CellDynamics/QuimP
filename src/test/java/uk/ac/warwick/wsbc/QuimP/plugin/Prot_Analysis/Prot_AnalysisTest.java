/**
 * @file Prot_AnalysisTest.java
 * @date 13 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

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

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.geom.TrackMap;
import uk.ac.warwick.wsbc.QuimP.plugin.QconfSupporter;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 * @date 13 Aug 2016
 *
 */
public class Prot_AnalysisTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(Prot_AnalysisTest.class.getName());
    static QconfLoaderLocal qL1;
    private STmap[] stMap;
    private ImageProcessor imp;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        qL1 = new QconfLoaderLocal(
                Paths.get("src/test/resources/TrackMapTests/fluoreszenz-test_eq_smooth.QCONF"));
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
        stMap = qL1.getQp().getLoadedDataContainer().QState;
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
     * Check tracking for found maxima. This duplicate tests from {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMapTest}
     * but for local data.
     * 
     * @throws Exception
     * @see src/test/resources/ProtAnalysisTest/main.m
     */
    @Test
    public void testTracking() throws Exception {
        int[] expectedI = { 167 - 1, 150 - 1, 139 - 1, 147 - 1, 138 - 1, 136 - 1, 134 - 1, 135 - 1,
                141 - 1, 139 - 1 };
        int[] expectedF =
                { 22 - 1, 23 - 1, 24 - 1, 25 - 1, 26 - 1, 27 - 1, 28 - 1, 29 - 1, 30 - 1, 31 - 1 };
        TrackMap tM = new TrackMap(stMap[0].originMap, stMap[0].coordMap);
        int frame = 20; // frame from found maxima [0]
        int index = 160; // index from found maxima [0]
        int[] ret = tM.trackForward(frame, index, 10);
        int[] retF = tM.getForwardFrames(frame, 10);
        assertThat(ret, is(expectedI));
        assertThat(retF, is(expectedF));
    }

}

/**
 * Simple loader of QCONF file.
 * 
 * @author p.baniukiewicz
 *
 */
class QconfLoaderLocal extends QconfSupporter {

    /**
     * 
     */
    public QconfLoaderLocal() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param path
     */
    public QconfLoaderLocal(Path path) {
        super(path);
        // TODO Auto-generated constructor stub
    }

}
