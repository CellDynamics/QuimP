package com.github.celldynamics.quimp.geom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.awt.geom.Point2D;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.celldynamics.quimp.plugin.qanalysis.STmap;

/**
 * The Class MapCoordConverterTest.
 *
 * @author baniu
 */
public class MapCoordConverterTest {

  /**
   * Test find index.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindIndex() throws Exception {
    double[] arr = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int ret = MapCoordConverter.findIndex(arr, 5.01, 1e-1);
    assertThat(ret, is(4));
    ret = MapCoordConverter.findIndex(arr, 5.01, 1e-5); // too big tolerance, not found
    assertThat(ret, is(-1));
  }

  /**
   * Test to Cartesian.
   *
   * @throws Exception the exception
   */
  @Test
  public void testToCartesian() throws Exception {
    double[][] x = { { 1, 2, 3 }, { 4, 5, 6 } };
    double[][] y = { { 10, 11, 12 }, { 40, 41, 42 } };
    double[][] c = { { 0, 1, 2 }, { 3, 4, 5 } };
    STmap map = Mockito.mock(STmap.class);
    Mockito.when(map.getxMap()).thenReturn(x);
    Mockito.when(map.getyMap()).thenReturn(y);
    Mockito.when(map.getCoordMap()).thenReturn(c);
    Point2D.Double ret = MapCoordConverter.toCartesian(map, 0, 1.1, 2e-1);
    assertThat(ret, is(new Point2D.Double(2, 11)));
    ret = MapCoordConverter.toCartesian(map, 0, 1.1, 2e-5);
    assertThat(ret, is(nullValue()));
  }

  /**
   * Test to map.
   *
   * @throws Exception the exception
   */
  @Test
  public void testToMap() throws Exception {
    double[][] x = { { 1, 2, 3 }, { 4, 5, 6 } };
    double[][] y = { { 10, 11, 12 }, { 40, 41, 42 } };
    double[][] c = { { 0, 1, 2 }, { 3, 4, 5 } };
    STmap map = Mockito.mock(STmap.class);
    Mockito.when(map.getxMap()).thenReturn(x);
    Mockito.when(map.getyMap()).thenReturn(y);
    Mockito.when(map.getCoordMap()).thenReturn(c);
    Point2D.Double ret = MapCoordConverter.toMap(map, 0, 2, 11.1, 1e-1);
    assertThat(ret, is(new Point2D.Double(0, 1)));
    ret = MapCoordConverter.toMap(map, 0, 2, 11.5, 1e-6);
    assertThat(ret, is(nullValue()));
  }

}
