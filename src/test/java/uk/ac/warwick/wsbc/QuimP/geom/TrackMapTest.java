/**
 * @file TrackMapTest.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

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
 * @author p.baniukiewicz
 * @date 15 Aug 2016
 *
 */
public class TrackMapTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(TrackMapTest.class.getName());
    static QconfLoader qL;
    double[][] originMap;
    double[][] coordMap;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        qL = new QconfLoader(Paths
                .get("src/test/resources/TrackMapTests/Stack_cut_10frames_trackMapTest.QCONF"));
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
        coordMap = qL.getQp().getLoadedDataContainer().QState[0].coordMap;
        originMap = qL.getQp().getLoadedDataContainer().QState[0].originMap;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMap#TrackMap(double[][], double[][])}.
     */
    @Test
    public void testTrackMap() throws Exception {
        @SuppressWarnings("unused")
        TrackMap tM = new TrackMap(originMap, coordMap);
        LOGGER.debug(tM.forwardMap.toString());
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