package com.github.celldynamics.quimp.geom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author baniu
 *
 */
public class MapCoordConverterTest {

  @Test
  public void testFindIndex() throws Exception {
    double[] arr = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int ret = MapCoordConverter.findIndex(arr, 5.01, 1e-1);
    assertThat(ret, is(4));
    ret = MapCoordConverter.findIndex(arr, 5.01, 1e-5); // too big tolerance, not found
    assertThat(ret, is(-1));
  }

  @Test
  public void testToCartesian() throws Exception {
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testToMap() throws Exception {
    throw new RuntimeException("not yet implemented");
  }

}
