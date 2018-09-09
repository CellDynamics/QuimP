package com.github.celldynamics.quimp.filesystem.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.BOAState;
import com.github.celldynamics.quimp.Nest;
import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.OutlineTest;
import com.github.celldynamics.quimp.QParams;
import com.github.celldynamics.quimp.Shape;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.SnakeHandler;
import com.github.celldynamics.quimp.filesystem.OutlinesCollection;
import com.github.celldynamics.quimp.filesystem.QconfLoader;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

/**
 * FormatConverter test using mocked QconfLoader.
 *
 * @author p.baniukiewicz
 */
@RunWith(MockitoJUnitRunner.class)
public class FormatConverterMockTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(FormatConverterMockTest.class.getName());

  /** The qc L. */
  @Mock
  private QconfLoader qcL; // mock this private field in FormatConverter

  /** The fcmock. */
  @InjectMocks
  @Spy
  private FormatConverter fcmock = new FormatConverter();

  /**
   * Temporary folder.
   * 
   * @see TemporaryFolder
   * @see MyTemporaryFolder
   */
  @Rule
  public MyTemporaryFolder folder = new MyTemporaryFolder();

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    Files.deleteIfExists(Paths.get(Paths.get(".").toAbsolutePath().normalize().toString()
            + "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.snQP"));
    Files.deleteIfExists(Paths.get(Paths.get(".").toAbsolutePath().normalize().toString()
            + "src/test/Resources-static/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));
    Field f = Shape.class.getDeclaredField("threshold");
    f.setAccessible(true);
    f.setDouble(Shape.class, 1.0);
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveBoaCentroids() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveBoaCentroids();
  }

  /**
   * Verify saving xls.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSaveBoaCentroids_1() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.NEW_QUIMP);

    Snake s = Mockito.mock(Snake.class); // mock snake
    Mockito.when(s.getCentroid()).thenReturn(new ExtendedVector2d(1, 2)); // only this param

    // mock snakehandler for 2 frames and 2 snakes
    SnakeHandler sh = Mockito.mock(SnakeHandler.class);
    Mockito.when(sh.getStartFrame()).thenReturn(1); // start frame = 1
    Mockito.when(sh.getEndFrame()).thenReturn(2); // end = 2
    Mockito.when(sh.getStoredSnake(1)).thenReturn(s); // snake at frame 1
    Mockito.when(sh.getStoredSnake(2)).thenReturn(s); // snake at frmae 2 ( the same)

    List<SnakeHandler> shL = Arrays.asList(sh, sh); // say we have two such snakes

    Nest nest = Mockito.mock(Nest.class);
    Mockito.when(nest.getHandlers()).thenReturn(shL); // out snakehandler
    BOAState bs = new BOAState();
    bs.nest = nest;
    Mockito.when(qcL.getBOA()).thenReturn(bs);

    Path tmp = folder.getRoot().toPath();
    Path target0 = tmp.resolve("boacent_0.xls"); // we mock getFeatureFilename, 1st handler
    Path target1 = tmp.resolve("boacent_1.xls"); // 2nd handler
    LOGGER.debug(tmp.toString());
    // do getFeatureFilename mocks for two handlers
    Mockito.doReturn(target0).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(0), Mockito.any(String.class));
    Mockito.doReturn(target1).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(1), Mockito.any(String.class));
    fcmock.saveBoaCentroids();

    // verify if we have two snakes in each file
    Mockito.verify(sh, Mockito.times(2)).getStoredSnake(1);
    Mockito.verify(sh, Mockito.times(2)).getStoredSnake(2);
    // and files itself
    assertThat(target0.toFile().exists(), is(true));
    assertThat(target1.toFile().exists(), is(true));
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveEcmmOutlines() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveEcmmOutlines(false);
  }

  /**
   * Verify saving xls.
   * 
   * @throws Exception Exception
   */
  @Test()
  public void testSaveEcmmOutlines_1() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.NEW_QUIMP);

    Outline o = OutlineTest.getRandomOutline(); // build outline

    OutlineHandler oh = Mockito.mock(OutlineHandler.class);
    Mockito.when(oh.getStartFrame()).thenReturn(1); // start frame = 1
    Mockito.when(oh.getEndFrame()).thenReturn(2); // end = 2
    Mockito.when(oh.getStoredOutline(1)).thenReturn(o); // outline at frame 1
    Mockito.when(oh.getStoredOutline(2)).thenReturn(o); // outline at frmae 2 ( the same)

    ArrayList<OutlineHandler> ohL = new ArrayList<>(Arrays.asList(oh, oh)); // say we have two outli

    OutlinesCollection occ = new OutlinesCollection();
    occ.oHs = ohL;
    Mockito.when(qcL.getEcmm()).thenReturn(occ);

    Path tmp = folder.getRoot().toPath();
    Path target0 = tmp.resolve("ecmmcentroid_0.xls"); // we mock getFeatureFilename, 1st handler
    Path target1 = tmp.resolve("ecmmcentroid_1.xls"); // 2nd handler
    LOGGER.debug("testSaveEcmmOutlines_1 " + tmp.toString());
    // do getFeatureFilename mocks for two handlers
    Mockito.doReturn(target0).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(0), Mockito.any(String.class));
    Mockito.doReturn(target1).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(1), Mockito.any(String.class));
    fcmock.saveEcmmOutlines(true);

    // verify if we have two snakes in each file
    Mockito.verify(oh, Mockito.times(2)).getStoredOutline(1);
    Mockito.verify(oh, Mockito.times(2)).getStoredOutline(2);
    // and files itself
    assertThat(target0.toFile().exists(), is(true));
    assertThat(target1.toFile().exists(), is(true));

    // for false (use contains to generate paths - 4 different)
    // // remove files
    // target0.toFile().delete();
    // target1.toFile().delete();
    // fcmock.saveEcmmOutlines(false); // one file
    // // verify if we have two snakes in each file
    // Mockito.verify(oh, Mockito.times(2)).getStoredOutline(1);
    // Mockito.verify(oh, Mockito.times(2)).getStoredOutline(2);
    // // and files itself
    // assertThat(target0.toFile().exists(), is(true));
    // assertThat(target1.toFile().exists(), is(false));
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveEcmmCentroids() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveEcmmCentroids();
  }

  /**
   * Verify saving xls.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSaveEcmmCentroids_1() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.NEW_QUIMP);

    Outline o = Mockito.mock(Outline.class); // mock outline
    Mockito.when(o.getCentroid()).thenReturn(new ExtendedVector2d(1, 2)); // only this param

    // mock snakehandler for 2 frames and 2 outlines
    OutlineHandler oh = Mockito.mock(OutlineHandler.class);
    Mockito.when(oh.getStartFrame()).thenReturn(1); // start frame = 1
    Mockito.when(oh.getEndFrame()).thenReturn(2); // end = 2
    Mockito.when(oh.getStoredOutline(1)).thenReturn(o); // outline at frame 1
    Mockito.when(oh.getStoredOutline(2)).thenReturn(o); // outline at frmae 2 ( the same)

    ArrayList<OutlineHandler> ohL = new ArrayList<>(Arrays.asList(oh, oh)); // say we have two outli

    OutlinesCollection occ = new OutlinesCollection();
    occ.oHs = ohL;
    Mockito.when(qcL.getEcmm()).thenReturn(occ);

    Path tmp = folder.getRoot().toPath();
    Path target0 = tmp.resolve("ecmmcentroid_0.xls"); // we mock getFeatureFilename, 1st handler
    Path target1 = tmp.resolve("ecmmcentroid_1.xls"); // 2nd handler
    LOGGER.debug(tmp.toString());
    // do getFeatureFilename mocks for two handlers
    Mockito.doReturn(target0).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(0), Mockito.any(String.class));
    Mockito.doReturn(target1).when(fcmock).getFeatureFileName(Mockito.any(String.class),
            Mockito.eq(1), Mockito.any(String.class));
    fcmock.saveEcmmCentroids();

    // verify if we have two snakes in each file
    Mockito.verify(oh, Mockito.times(2)).getStoredOutline(1);
    Mockito.verify(oh, Mockito.times(2)).getStoredOutline(2);
    // and files itself
    assertThat(target0.toFile().exists(), is(true));
    assertThat(target1.toFile().exists(), is(true));
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveBoaSnakes() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveBoaSnakes(false);
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveMaps() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveMaps(0);
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveStatFluores() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveStatFluores();
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveStatGeom() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveStatGeom();
  }

  /**
   * Throw exception if used on old format.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSaveStats() throws Exception {
    Mockito.when(qcL.isFileLoaded()).thenReturn(QParams.QUIMP_11);
    fcmock.saveStats();
  }

  /**
   * TemporaryFolder from junit with blocked deletion of tmp folder.
   * 
   * @author p.baniukiewicz
   *
   */
  class MyTemporaryFolder extends TemporaryFolder {

    /*
     * (non-Javadoc)
     * 
     * @see org.junit.rules.TemporaryFolder#after()
     */
    @Override
    protected void after() {
      // comment to block deleting - for testing
      // super.after();
    }

  }
}
