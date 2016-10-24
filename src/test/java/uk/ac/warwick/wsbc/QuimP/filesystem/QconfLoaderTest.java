package uk.ac.warwick.wsbc.QuimP.filesystem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.BOAState;
import uk.ac.warwick.wsbc.QuimP.QParams;
import uk.ac.warwick.wsbc.QuimP.QParamsQconf;
import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.Serializer;
import uk.ac.warwick.wsbc.QuimP.plugin.qanalysis.STmap;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class QconfLoaderTest {
    static final Logger LOGGER = LoggerFactory.getLogger(QconfLoaderTest.class.getName());

    /**
     * Prepare fake QCONF. No data just empty container.
     * 
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DataContainer dt = new DataContainer();
        Serializer<DataContainer> serializer;
        serializer = new Serializer<>(dt,
                new uk.ac.warwick.wsbc.QuimP.utils.QuimpToolsCollection().getQuimPBuildInfo());
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
     * Test method for
     * {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#QconfLoader(java.nio.file.Path)}.
     */
    @Test
    public void testQconfLoaderPath() throws Exception {
        new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#getImage()}.
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
        QconfLoader o = new QconfLoader(Paths
                .get("src/test/resources/ProtAnalysisTest/KZ4-220214-cAR1-GFP-devel5noimage.QCONF")
                .toFile());
        o.getImage();
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#validateQconf()}.
     */
    @Test
    public void testValidateQconf() throws Exception {
        LOGGER.trace("testValidateQconf");
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);

        assertThat("DataContainer is empty", q.validateQconf(), is(QconfLoader.QCONF_INVALID));

        dC.BOAState = new BOAState();
        assertThat(q.validateQconf(), is(DataContainer.BOA_RUN));
        assertThat(q.getBOA(), is(dC.BOAState));
        dC.ECMMState = new OutlinesCollection();
        assertThat(q.validateQconf(), is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN));
        assertThat(q.getECMM(), is(dC.ECMMState));
        dC.ANAState = new ANAParamCollection();
        assertThat(q.validateQconf(),
                is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN + DataContainer.ANA_RUN));
        assertThat(q.getANA(), is(dC.ANAState));
        dC.QState = new STmap[1];
        assertThat(q.validateQconf(), is(DataContainer.ECMM_RUN + DataContainer.BOA_RUN
                + DataContainer.ANA_RUN + DataContainer.Q_RUN));
        assertThat(q.getQ(), is(dC.QState));
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#isBOAPresent()}.
     */
    @Test
    public void testIsBOAPresent() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#isECMMPresent()}.
     */
    @Test
    public void testIsECMMPresent() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#isANAPresent()}.
     */
    @Test
    public void testIsANAPresent() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));
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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.filesystem.QconfLoader#isQPresent()}.
     */
    @Test
    public void testIsQPresent() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));
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

    @Test(expected = QuimpException.class)
    public void testGetBOA() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);
        q.getBOA();
    }

    @Test(expected = QuimpException.class)
    public void testGetQ() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);
        q.getQ();
    }

    @Test(expected = QuimpException.class)
    public void testGetANA() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);
        q.getANA();
    }

    @Test(expected = QuimpException.class)
    public void testGetECMM() throws Exception {
        QconfLoader q =
                Mockito.spy(new QconfLoader(Paths.get("/tmp/qconftestloader.QCONF").toFile()));

        QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
        ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
        DataContainer dC = new DataContainer();

        Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
        Mockito.when(q.getQp()).thenReturn(qPc);
        q.getECMM();
    }

}
