package uk.ac.warwick.wsbc.QuimP.plugin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.ANAStates;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.DataContainer;
import uk.ac.warwick.wsbc.QuimP.OutlineHandlers;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.STmap;
import uk.ac.warwick.wsbc.QuimP.Serializer;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class QconfLoaderTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "qlog4j2.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(QconfLoaderTest.class.getName());

    /**
     * Prepare fake QCONF. No data just empty container.
     * 
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DataContainer dt = new DataContainer();
        Serializer<DataContainer> serializer;
        serializer = new Serializer<>(dt, new uk.ac.warwick.wsbc.QuimP.Tool().getQuimPBuildInfo());
        serializer.setPretty();
        serializer.save("/tmp/qconftestloader.QCONF");

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#QconfLoader(java.nio.file.Path)}.
     */
    @Test
    public void testQconfLoaderPath() throws Exception {
        new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF"));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#getImage()}.
     */
    @SuppressWarnings("unused")
    @Test
    @Ignore("Use GUI for testing. Not for automated run")
    public void testGetImage() throws Exception {
        ImageJ ij = new ImageJ();
        ImagePlus i1 = IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif");
        i1.show();
        ImagePlus i2 = IJ.openImage("src/test/resources/Stack_cut.tif");
        i2.show();
        QconfLoader o = new QconfLoader(Paths.get(
                "src/test/resources/ProtAnalysisTest/KZ4-220214-cAR1-GFP-devel5noimage.QCONF"));
        o.getImage();
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#validateQconf()}.
     */
    @Test
    public void testValidateQconf() throws Exception {
        LOGGER.trace("testValidateQconf");
        QconfLoader q = Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF")));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);

        assertThat("DataContainer is empty", q.validateQconf(), is(QconfLoader.QCONF_INVALID));

        dC.BOAState = new BOAState();
        assertThat(q.validateQconf(), is(DataContainer.BOA_RUN));
        dC.ECMMState = new OutlineHandlers();
        assertThat(q.validateQconf(), is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN));
        dC.ANAState = new ANAStates();
        assertThat(q.validateQconf(),
                is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN + DataContainer.ANA_RUN));
        dC.QState = new STmap[1];
        assertThat(q.validateQconf(), is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN
                + DataContainer.ANA_RUN + DataContainer.Q_RUN));
        dC.ANAState = null;
        assertThat(q.validateQconf(),
                is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN + DataContainer.Q_RUN));

        Mockito.when(q.getQp()).thenReturn(null);
        assertThat("DataContainer is empty", q.validateQconf(), is(QconfLoader.QCONF_INVALID));

        Mockito.when(q.getQp()).thenReturn(qPc);
        ((QParamsQconf) qPc).paramFormat = QParams.QUIMP_11;
        assertThat(q.validateQconf(), is(QParams.QUIMP_11));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#isBOAPresent()}.
     */
    @Test
    public void testIsBOAPresent() throws Exception {
        QconfLoader q = Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF")));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN);
        assertThat(q.isBOAPresent(), is(true));
        Mockito.when(q.validateQconf())
                .thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN + DataContainer.Q_RUN);
        assertThat(q.isBOAPresent(), is(true));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.ECMM_RUN + DataContainer.Q_RUN);
        assertThat(q.isBOAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QconfLoader.QCONF_INVALID);
        assertThat(q.isBOAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.QUIMP_11);
        assertThat(q.isBOAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.OLD_QUIMP);
        assertThat(q.isBOAPresent(), is(false));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#isECMMPresent()}.
     */
    @Test
    public void testIsECMMPresent() throws Exception {
        QconfLoader q = Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF")));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN);
        assertThat(q.isECMMPresent(), is(true));
        Mockito.when(q.validateQconf())
                .thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN + DataContainer.Q_RUN);
        assertThat(q.isECMMPresent(), is(true));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.Q_RUN);
        assertThat(q.isECMMPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QconfLoader.QCONF_INVALID);
        assertThat(q.isECMMPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.QUIMP_11);
        assertThat(q.isECMMPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.OLD_QUIMP);
        assertThat(q.isECMMPresent(), is(false));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#isANAPresent()}.
     */
    @Test
    public void testIsANAPresent() throws Exception {
        QconfLoader q = Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF")));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.ANA_RUN);
        assertThat(q.isANAPresent(), is(true));
        Mockito.when(q.validateQconf())
                .thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN + DataContainer.ANA_RUN);
        assertThat(q.isANAPresent(), is(true));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.Q_RUN);
        assertThat(q.isANAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QconfLoader.QCONF_INVALID);
        assertThat(q.isANAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.QUIMP_11);
        assertThat(q.isANAPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.OLD_QUIMP);
        assertThat(q.isANAPresent(), is(false));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader#isQPresent()}.
     */
    @Test
    public void testIsQPresent() throws Exception {
        QconfLoader q = Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF")));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.Q_RUN);
        assertThat(q.isQPresent(), is(true));
        Mockito.when(q.validateQconf())
                .thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN + DataContainer.Q_RUN);
        assertThat(q.isQPresent(), is(true));
        Mockito.when(q.validateQconf()).thenReturn(DataContainer.BOA_RUN + DataContainer.ECMM_RUN);
        assertThat(q.isQPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QconfLoader.QCONF_INVALID);
        assertThat(q.isQPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.QUIMP_11);
        assertThat(q.isQPresent(), is(false));
        Mockito.when(q.validateQconf()).thenReturn(QParams.OLD_QUIMP);
        assertThat(q.isQPresent(), is(false));
    }

}
