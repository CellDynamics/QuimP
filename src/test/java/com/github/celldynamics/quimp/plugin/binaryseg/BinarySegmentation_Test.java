package com.github.celldynamics.quimp.plugin.binaryseg;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.celldynamics.quimp.plugin.ParamList;

import ij.ImageJ;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BinarySegmentation_Test {

  @Mock
  private BinarySegmentationView bsp;

  @InjectMocks
  private BinarySegmentation_ bs;

  @Spy
  private BinarySegmentation_ bspy;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    new ImageJ();
  }

  /**
   * Test method for {@link BinarySegmentation_#BinarySegmentation_()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentation_() throws Exception {
    bs.run("");
    verify(bsp).showWindow(true);
    verify(bsp, never()).setValues(any(ParamList.class));
    bspy.run("");
    verify(bspy).showUi(true);
  }

  /**
   * Test method for {@link BinarySegmentation_#BinarySegmentation_()}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentation_1() throws Exception {
    bspy.run("");
    verify(bsp, times(1)).setValues(any(ParamList.class));
  }

}
