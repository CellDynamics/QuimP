package com.github.celldynamics.quimp.filesystem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.QParamsQconf;
import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.ANAParamCollection;
import com.github.celldynamics.quimp.filesystem.DataContainer;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class QconfLoaderTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(QconfLoaderTest.class.getName());

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Prepare fake QCONF. No data just empty container.
   * 
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DataContainer dt = new DataContainer();
    Serializer<DataContainer> serializer;
    serializer = new Serializer<>(dt, new QuimpVersion("17.10.11", "p.baniukiewicz", "QuimP"));
    serializer.setPretty();
    serializer.save(tmpdir + "qconftestloader.QCONF");

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
   * {@link com.github.celldynamics.quimp.filesystem.QconfLoader#QconfLoader(java.io.File)}.
   * 
   * @throws Exception
   */
  @Test
  public void testQconfLoaderPath() throws Exception {
    new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile());
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#getImage()}.
   * 
   * @throws Exception
   */
  @SuppressWarnings("unused")
  @Test
  @Ignore("Use GUI for testing. Not for automated run")
  public void testGetImage() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus i1 =
            IJ.openImage("src/test/Resources-static/fluoreszenz-test_eq_smooth_frames_1-5.tif");
    i1.show();
    ImagePlus i2 = IJ.openImage("src/test/Resources-static/Stack_cut.tif");
    i2.show();
    Files.copy(
            Paths.get("src/test/Resources-static/ProtAnalysisTest/"
                    + "KZ4-220214-cAR1-GFP-devel5noimage.QCONF"),
            Paths.get(tmpdir + "KZ4-220214-cAR1-GFP-devel5noimage_cp.QCONF"),
            StandardCopyOption.REPLACE_EXISTING);
    QconfLoader o = new QconfLoader(
            Paths.get(tmpdir + "KZ4-220214-cAR1-GFP-devel5noimage_cp.QCONF").toFile());
    o.getImage();
    o.getQp().writeParams();
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#validateQconf()}.
   * 
   * @throws Exception
   */
  @Test
  public void testValidateQconf() throws Exception {
    LOGGER.trace("testValidateQconf");
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));

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
    assertThat(q.getEcmm(), is(dC.ECMMState));
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
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#isBOAPresent()}.
   * 
   * @throws Exception
   */
  @Test
  public void testIsBOAPresent() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));
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
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#isECMMPresent()}.
   * 
   * @throws Exception
   */
  @Test
  public void testIsECMMPresent() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));
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
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#isANAPresent()}.
   * 
   * @throws Exception
   */
  @Test
  public void testIsANAPresent() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));
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
   * Test method for {@link com.github.celldynamics.quimp.filesystem.QconfLoader#isQPresent()}.
   * 
   * @throws Exception
   */
  @Test
  public void testIsQPresent() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));
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

  /**
   * @throws Exception
   */
  @Test(expected = QuimpException.class)
  public void testGetBOA() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));

    QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
    ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
    DataContainer dC = new DataContainer();

    Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
    Mockito.when(q.getQp()).thenReturn(qPc);
    q.getBOA();
  }

  /**
   * Test get Q.
   *
   * @throws Exception the exception
   */
  @Test(expected = QuimpException.class)
  public void testGetQ() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));

    QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
    ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
    DataContainer dC = new DataContainer();

    Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
    Mockito.when(q.getQp()).thenReturn(qPc);
    q.getQ();
  }

  /**
   * @throws Exception
   */
  @Test(expected = QuimpException.class)
  public void testGetANA() throws Exception {
    QconfLoader q =
            Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));

    QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
    ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
    DataContainer dC = new DataContainer();

    Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
    Mockito.when(q.getQp()).thenReturn(qPc);
    q.getANA();
  }

  /**
     * @throws Exception
     */
    @Test(expected = QuimpException.class)
    public void testGetEcmm() throws Exception {
      QconfLoader q =
              Mockito.spy(new QconfLoader(Paths.get(tmpdir + "qconftestloader.QCONF").toFile()));
  
      QParamsQconf qPc = Mockito.mock(QParamsQconf.class);
      ((QParamsQconf) qPc).paramFormat = QParams.NEW_QUIMP;
      DataContainer dC = new DataContainer();
  
      Mockito.when(qPc.getLoadedDataContainer()).thenReturn(dC);
      Mockito.when(q.getQp()).thenReturn(qPc);
      q.getEcmm();
    }

}