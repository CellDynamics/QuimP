package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkModel.SeedSource;
import com.github.celldynamics.quimp.utils.IJTools;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;

// TODO: Auto-generated Javadoc
/**
 * Example of high level API and tests of RandomWalkSegmentationPlugin.
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkSegmentationPluginTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER =
          LoggerFactory.getLogger(RandomWalkSegmentationPluginTest.class.getName());

  /** The original image. */
  private ImagePlus originalImage;

  /** The seed image. */
  private ImagePlus seedImage;

  private static ImageJ ij;

  /**
   * SetUp ImageJ.
   * 
   * @throws Exception Exception
   */
  @BeforeClass
  public static void before() throws Exception {
    ij = new ImageJ();
  }

  /**
   * Exit ImageJ.
   * 
   * @throws Exception Exception
   */
  @AfterClass
  public static void after() throws Exception {
    IJTools.exitIj(ij);
    ij = null;
  }

  /**
   * setUp.
   */
  @Before
  public void setUp() {
    originalImage = Mockito.mock(ImagePlus.class);
    seedImage = Mockito.mock(ImagePlus.class);
    Mockito.when(originalImage.getTitle()).thenReturn("ORGINAL IMAGE.tiff");
    Mockito.when(seedImage.getTitle()).thenReturn("SEED IMAGE.tiff");

  }

  /**
   * tearDown.
   */
  @After
  public void tearDown() {
    originalImage = null;
    seedImage = null;
    IJTools.closeAllImages();
  }

  /**
   * Example of high level API call.
   * 
   * @throws Exception on error
   */
  @Test
  public void segmentTest() throws Exception {
    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_();
    obj.apiCall = true; // override this as by default it is false
    //!> 
    // image can be opened in IJ and displayed or the full path can be provided to configuration
    // file
    IJ.openImage(
            "src/test/Resources-static/RW/"
            + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif")
            .show();
    StopWatch timer = StopWatch.createStarted();
    obj.run("opts={algOptions:{alpha:900.0,beta:100.0,gamma:[100.0,0.0],iter:10000,"
            + "dt:0.1,relim:[0.002,0.02],useLocalMean:true,localMeanMaskSize:23,"
            + "maskLimit:false},"
            + "originalImageName:(src/test/Resources-static/RW/" // full path here e.g.
            + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif),"
            + "selectedSeedSource:MaskImage,"
            + "seedImageName:(C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif),"
            + "qconfFile:(null),"
            + "selectedShrinkMethod:CONTOUR,shrinkPower:17.0,expandPower:15.0,"
            + "scaleSigma:0.3,scaleMagn:4.0,"
            + "scaleEqNormalsDist:12.0,"
            + "scaleCurvDistDist:12.0,"
            + "estimateBackground:false,"
            + "selectedFilteringMethod:NONE,"
            + "hatFilter:false,alev:0.9,num:1,window:15,"
            + "selectedFilteringPostMethod:NONE,"
            + "showSeeds:false,showPreview:false,"
            + "showProbMaps:false,"
            + "paramFile:(null)}");
    //!<
    timer.stop();
    LOGGER.warn("Time elapsed: " + timer.toString());
    ImagePlus res = obj.getResult();
    assertThat(res.getShortTitle(), is("Segmented_C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18"));
    assertThat(res.getWidth(), is(512));
    assertThat(res.getHeight(), is(512));
  }

  /**
   * Example of high level API call.
   * 
   * <p>Need to take care about proper combination of parameters.
   * 
   * @throws Exception on error
   */
  @Test
  public void segment1Test() throws Exception {
    RandomWalkModel opts = new RandomWalkModel();
    Opener op = new Opener();
    ImagePlus si = op.openImage("src/test/Resources-static/RW/"
            + "C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18_rough_snakemask.tif");
    ImagePlus mi = op
            .openImage("src/test/Resources-static/RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18.tif");
    opts.setSeedImage(si);
    opts.setOriginalImage(mi);
    opts.setSelectedSeedSource(SeedSource.MaskImage);

    RandomWalkSegmentationPlugin_ obj = new RandomWalkSegmentationPlugin_(opts);
    obj.apiCall = true;
    obj.runPlugin();
    ImagePlus res = obj.getResult();
    assertThat(res.getShortTitle(), is("Segmented_C1-talA_mNeon_bleb_0pt7%agar_FLU_frame18"));
    assertThat(res.getWidth(), is(512));
    assertThat(res.getHeight(), is(512));
  }

  /**
   * Test method for
   * {@link RandomWalkSegmentationPlugin_#writeUI()}.
   * 
   * <p>PRE: Set UI from model and then read it.
   * 
   * <p>POST: read model should be the same as set up.
   *
   * @throws NoSuchFieldException NoSuchFieldException
   * @throws SecurityException SecurityException
   * @throws IllegalArgumentException IllegalArgumentException
   * @throws IllegalAccessException IllegalAccessException
   */
  @Test
  public void testWriteReadUI() throws NoSuchFieldException, SecurityException,
          IllegalArgumentException, IllegalAccessException {
    RandomWalkSegmentationPlugin_ plugin = new RandomWalkSegmentationPlugin_();
    RandomWalkModel model = new RandomWalkModel();
    model.setSelectedSeedSource(SeedSource.MaskImage);
    Field f = plugin.getClass().getSuperclass().getSuperclass().getSuperclass()
            .getDeclaredField("options");
    f.setAccessible(true);
    f.set(plugin, model);
    // plugin.model = model;
    int hash = model.hashCode(); // remember hash
    LOGGER.debug("before: " + model.toString());
    plugin.writeUI();

    RandomWalkModel ret = plugin.readUI(); // restore from ui, hash should be the same

    LOGGER.debug("after:  " + ret.toString());
    assertThat(ret.hashCode(), is(hash));

  }

}
