package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ij.ImagePlus;

/**
 * RandomWalkModel.
 * 
 * @author p.baniukiewicz
 *
 */
public class RandomWalkModelTest {

  /** The original image. */
  private ImagePlus originalImage;

  /** The original imagesmall. */
  private ImagePlus originalImagesmall;

  /** The seed image. */
  private ImagePlus seedImage;

  /**
   * setUp.
   */
  @Before
  public void setUp() {
    originalImage = Mockito.mock(ImagePlus.class);
    originalImagesmall = Mockito.mock(ImagePlus.class);
    seedImage = Mockito.mock(ImagePlus.class);
    Mockito.when(originalImage.getTitle()).thenReturn("ORGINAL IMAGE.tiff");
    Mockito.when(seedImage.getTitle()).thenReturn("SEED IMAGE.tiff");
    Mockito.when(originalImagesmall.getTitle()).thenReturn("ORGINALE.tiff");

  }

  /**
   * Test of hashes.
   */
  @Test
  public void testEquals() {
    // different objects the same vals
    RandomWalkModel model = new RandomWalkModel();
    RandomWalkModel model1 = new RandomWalkModel();

    assertThat(model1.hashCode(), is(model.hashCode()));

    // different value
    model1.alev = 567;
    assertThat(model1.hashCode(), not(model.hashCode()));

  }

  /**
   * Test of hashes and comparing ImagePlus by name.
   */
  @Test
  public void testEquals1() {
    // different objects the same vals
    RandomWalkModel model = new RandomWalkModel();
    model.setOriginalImage(originalImage);
    model.setSeedImage(seedImage);
    RandomWalkModel model1 = new RandomWalkModel();
    model1.setOriginalImage(originalImage);
    model1.setSeedImage(seedImage);

    assertThat(model1.hashCode(), is(model.hashCode()));

    model.setOriginalImage(originalImagesmall);
    assertThat(model1.hashCode(), not(model.hashCode()));

  }

}
