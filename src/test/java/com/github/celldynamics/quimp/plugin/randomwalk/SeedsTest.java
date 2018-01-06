package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class SeedsTest {

  /**
   * Test of {@link Seeds#put(SeedTypes, ImageProcessor)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testPutSeedTypesImageProcessor() throws Exception {
    Seeds obj = new Seeds();
    ImageProcessor imp = new ByteProcessor(150, 150);
    obj.put(SeedTypes.FOREGROUNDS, imp);
    obj.put(SeedTypes.FOREGROUNDS, imp);

    List<ImageProcessor> ret = obj.get(SeedTypes.FOREGROUNDS);
    assertThat(ret.size(), is(2));
  }

  /**
   * Test of {@link Seeds#get(Object, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetObjectInt() throws Exception {
    Seeds obj = new Seeds();
    ImageProcessor imp = new ByteProcessor(150, 150);
    imp.putPixel(100, 101, 63); // equals des not work for IPs, recognize by this pixels
    ImageProcessor imp1 = new ByteProcessor(150, 150);
    imp1.putPixel(63, 63, 52);
    obj.put(SeedTypes.FOREGROUNDS, imp);
    obj.put(SeedTypes.FOREGROUNDS, imp1);

    assertThat(obj.get(SeedTypes.FOREGROUNDS, 1).getPixel(100, 101), is(63));
    assertThat(obj.get(SeedTypes.FOREGROUNDS, 2).getPixel(63, 63), is(52));
    assertThat(obj.get(SeedTypes.FOREGROUNDS, 0), is(nullValue()));
    assertThat(obj.get(SeedTypes.FOREGROUNDS, 3), is(nullValue()));
  }

  /**
   * Test of {@link Seeds#convertToList(Object)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConvertToList() throws Exception {
    // prepare fake image from these pixels
    Set<Point> expectedForeground = new HashSet<Point>();
    expectedForeground.add(new Point(70, 70));
    expectedForeground.add(new Point(71, 70));
    expectedForeground.add(new Point(72, 70));
    expectedForeground.add(new Point(100, 20));
    expectedForeground.add(new Point(172, 97));

    Set<Point> expectedBackground = new HashSet<Point>();
    expectedBackground.add(new Point(20, 20));
    expectedBackground.add(new Point(40, 40));
    expectedBackground.add(new Point(60, 60));

    // put pixels to IP
    ImageProcessor fg = new ByteProcessor(256, 128);
    expectedForeground.stream().forEach(p -> fg.putPixel(p.col, p.row, 255));

    ImageProcessor bg = new ByteProcessor(256, 128);
    expectedBackground.stream().forEach(p -> bg.putPixel(p.col, p.row, 255));

    // try to retrieve the same pixels as in those lists from images
    Seeds ret = new Seeds();
    ret.put(SeedTypes.FOREGROUNDS, fg); // store our images in seeds
    ret.put(SeedTypes.BACKGROUND, bg);
    // test foreground
    List<List<Point>> list = ret.convertToList(SeedTypes.FOREGROUNDS);
    assertThat(list.size(), is(1)); // one slice
    Set<Point> pseeds = new HashSet<>(list.get(0));
    assertThat(pseeds, is(expectedForeground));

    // test background
    list = ret.convertToList(SeedTypes.BACKGROUND);
    assertThat(list.size(), is(1)); // one slice
    pseeds = new HashSet<>(list.get(0));
    assertThat(pseeds, is(expectedBackground));
  }

  /**
   * Test of {@link Seeds#convertToList(Object)}.
   * 
   * <p>Expect null is key not found or empty stack
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConvertToList_1() throws Exception {
    Seeds ret = new Seeds();

    List<List<Point>> list = ret.convertToList(SeedTypes.FOREGROUNDS);
    assertThat(list.isEmpty(), is(true));

    // empty stack
    ret.put(SeedTypes.FOREGROUNDS, new ArrayList<>());
    list = ret.convertToList(SeedTypes.FOREGROUNDS);
    assertThat(list.isEmpty(), is(true));
  }

  /**
   * Test of {@link Seeds#convertToStack(Object)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testConvertToStack() throws Exception {
    Seeds obj = new Seeds();
    ImageProcessor imp = new ByteProcessor(150, 150);
    imp.putPixel(100, 101, 63); // equals des not work for IPs, recognize by this pixels
    ImageProcessor imp1 = new ByteProcessor(150, 150);
    imp1.putPixel(63, 63, 52);
    obj.put(SeedTypes.FOREGROUNDS, imp);
    obj.put(SeedTypes.FOREGROUNDS, imp1);

    // no key
    ImageStack ret = obj.convertToStack(SeedTypes.BACKGROUND);
    assertThat(ret, is(nullValue()));

    ret = obj.convertToStack(SeedTypes.FOREGROUNDS);
    assertThat(ret.getSize(), is(2));
    assertThat(ret.getVoxel(100, 101, 0), is(63.0));
    assertThat(ret.getVoxel(63, 63, 1), is(52.0));

    // empty
    obj.put(SeedTypes.BACKGROUND, new ArrayList<>());
    ret = obj.convertToStack(SeedTypes.BACKGROUND);
    assertThat(ret, is(nullValue()));

  }

}
