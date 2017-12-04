package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkModel.SeedSource;

import ij.ImagePlus;

/**
 * Test class for RandomWalkSegmentationPlugin
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

  private ImagePlus originalImage;
  private ImagePlus seedImage;

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

  }

  /**
   * Test method for
   * {@link RandomWalkSegmentationPlugin_#writeUI()}.
   * 
   * <p>PRE: Set UI from model and then read it.
   * 
   * <p>POST: read model should be the same as set up.
   * 
   * @throws SecurityException SecurityException
   * @throws NoSuchFieldException NoSuchFieldException
   * @throws IllegalAccessException IllegalAccessException
   * @throws IllegalArgumentException IllegalArgumentException
   */
  @Test
  public void testWriteReadUI() throws NoSuchFieldException, SecurityException,
          IllegalArgumentException, IllegalAccessException {
    RandomWalkSegmentationPlugin_ plugin = new RandomWalkSegmentationPlugin_();
    RandomWalkModel model = new RandomWalkModel();
    model.seedSource = SeedSource.MaskImage;
    Field f = plugin.getClass().getSuperclass().getDeclaredField("options");
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
