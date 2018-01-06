package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * @author p.baniukiewicz
 *
 */
public class SeedProcessorTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(SeedProcessorTest.class.getName());

  /**
   * The test image 1 rgb.
   */
  static ImagePlus testImage1rgb; // contains rgb image with test seed points

  /**
   * The test image 1.
   */
  static ImagePlus testImage1;

  /**
   * @throws java.lang.Exception on error
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testImage1rgb = IJ.openImage("src/test/Resources-static/segtest_small_rgb_test.tif");
    testImage1 = IJ.openImage("src/test/Resources-static/segtest_small.tif");
  }

  /**
   * @throws java.lang.Exception on error
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    testImage1rgb.close();
    testImage1rgb = null;
    testImage1.close();
    testImage1 = null;
  }

  /**
   * Test of decodeSeeds(ImagePlus, Color, Color).
   * 
   * <p>Pre: Image with green/red/yellow seed with known positions (segtest_small.rgb.tif)
   * 
   * <p>Post: Two lists with positions of seeds
   * 
   * @throws Exception on error
   */
  @Test
  public void testDecodeSeedsfromRgb() throws Exception {
    Set<Point> expectedForeground = new HashSet<Point>();
    expectedForeground.add(new Point(70, 70));
    expectedForeground.add(new Point(71, 70));
    expectedForeground.add(new Point(72, 70));
    expectedForeground.add(new Point(100, 20));
    expectedForeground.add(new Point(172, 97));

    Set<Point> expectedForeground1 = new HashSet<Point>();
    expectedForeground1.add(new Point(83, 41));
    expectedForeground1.add(new Point(158, 79));

    Set<Point> expectedBackground = new HashSet<Point>();
    expectedBackground.add(new Point(20, 20));
    expectedBackground.add(new Point(40, 40));
    expectedBackground.add(new Point(60, 60));

    Seeds ret = SeedProcessor.decodeSeedsfromRgb(testImage1rgb,
            Arrays.asList(Color.RED, new Color(255, 254, 128)), Color.GREEN);

    List<List<Point>> list = ret.convertToList(SeedTypes.FOREGROUNDS);
    assertThat(list.size(), is(2));
    Set<Point> fseeds = new HashSet<>(list.get(0));
    assertThat(fseeds, is(expectedForeground));
    Set<Point> fseeds1 = new HashSet<>(list.get(1));
    assertThat(fseeds1, is(expectedForeground1));

    list = ret.convertToList(SeedTypes.BACKGROUND);
    assertThat(list.size(), is(1));
    Set<Point> bseeds = new HashSet<>(list.get(0));
    assertThat(bseeds, is(expectedBackground));
  }

  /**
   * Test of decodeSeeds(ImagePlus, Color, Color).
   * 
   * <p>Pre: Mask image. Test approach if input is binary mask converted to rgb
   * 
   * <p>Post: Two lists with positions of seeds
   * 
   * @throws Exception on error
   */
  @Test
  public void testDecodeSeedsfromRgbBW() throws Exception {
    Set<Point> expectedForeground = new HashSet<Point>();
    expectedForeground.add(new Point(218, 120));
    expectedForeground.add(new Point(233, 118));
    expectedForeground.add(new Point(239, 132));
    expectedForeground.add(new Point(249, 131));
    expectedForeground.add(new Point(322, 225));

    Set<Point> expectedBackground = new HashSet<Point>();
    expectedBackground.add(new Point(334, 321));
    expectedBackground.add(new Point(238, 81));
    expectedBackground.add(new Point(319, 246));

    ImagePlus testImage = IJ.openImage("src/test/Resources-static/GMask.tif");
    new ImageConverter(testImage).convertToRGB(); // convert to rgb

    Seeds ret =
            SeedProcessor.decodeSeedsfromRgb(testImage, Arrays.asList(Color.WHITE), Color.BLACK);

    List<List<Point>> list = ret.convertToList(SeedTypes.FOREGROUNDS);
    assertThat(list.size(), is(1));
    Set<Point> fseeds = new HashSet<>(list.get(0));
    for (Point p : expectedForeground) {
      assertTrue(fseeds.contains(p));
    }

    list = ret.convertToList(SeedTypes.BACKGROUND);
    assertThat(list.size(), is(1));
    Set<Point> bseeds = new HashSet<>(list.get(0));
    for (Point p : expectedBackground) {
      assertTrue(bseeds.contains(p));
    }
  }

  /**
   * Test of {@link SeedProcessor#decodeSeedsRoi(List, String, String, int, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testDecodeSeedsRoi() throws Exception {
    String fgcore = "fg";
    String bgcore = "bg";
    Roi l1 = new Line(0, 0, 10, 0); // 11 pixels
    l1.setName(fgcore + "0_0");
    Roi l2 = new Line(5, 5, 5, 10); // 6 pixels;
    l2.setName(fgcore + "0_2");

    Roi l3 = new Line(10, 10, 20, 10); // 11
    l3.setName(fgcore + "2_0"); // skip one

    Roi l4 = new Line(7, 7, 7, 7);
    l4.setName(bgcore + "0_0");
    Roi l5 = new Line(32, 32, 32, 32);
    l5.setName(bgcore + "0_1");
    List<Roi> rois = new ArrayList<>();
    rois.add(l1);
    rois.add(l2);
    rois.add(l3);
    rois.add(l4);
    rois.add(l5);

    Seeds ret = SeedProcessor.decodeSeedsRoi(rois, fgcore, bgcore, 128, 128);

    assertThat(ret.size(), is(2));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).size(), is(2));
    assertThat(ret.get(SeedTypes.BACKGROUND).size(), is(1));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(0).getWidth(), is(128));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(0).getHeight(), is(128));
    // first FG
    ImageProcessor ip = ret.get(SeedTypes.FOREGROUNDS).get(0);
    int[] h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 17)); // we have 17 pixels white
    assertThat(h[255], is(17));

    // second FG
    ip = ret.get(SeedTypes.FOREGROUNDS).get(1);
    h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 11)); // we have 21 pixels white
    assertThat(h[255], is(11));

    // BG
    ip = ret.get(SeedTypes.BACKGROUND).get(0);
    h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 2)); // we have 21 pixels white
    assertThat(h[255], is(2));
  }

  /**
   * Test of {@link SeedProcessor#getSeedsAsGrayscale(Seeds)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetSeedsAsGrayscale() throws Exception {
    String fgcore = "fg";
    String bgcore = "bg";
    Roi l1 = new Line(0, 0, 10, 0); // 11 pixels
    l1.setName(fgcore + "0_0");
    Roi l2 = new Line(5, 5, 5, 10); // 6 pixels;
    l2.setName(fgcore + "0_2");

    Roi l3 = new Line(10, 10, 20, 10); // 11
    l3.setName(fgcore + "2_0"); // skip one

    Roi l4 = new Line(7, 7, 7, 7);
    l4.setName(bgcore + "0_0");
    Roi l5 = new Line(32, 32, 32, 32);
    l5.setName(bgcore + "0_1");
    List<Roi> rois = new ArrayList<>();
    rois.add(l1);
    rois.add(l2);
    rois.add(l3);
    rois.add(l4);
    rois.add(l5);

    Seeds seeds = SeedProcessor.decodeSeedsRoi(rois, fgcore, bgcore, 128, 128);

    ImageProcessor ret = SeedProcessor.getSeedsAsGrayscale(seeds);
    int[] h = ret.getHistogram();
    assertThat(h[1], is(17));
    assertThat(h[2], is(11));
    assertThat(h[3], is(2));
  }

  /**
   * Test of {@link SeedProcessor#getGrayscaleAsSeeds(ImageProcessor)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetGrayscaleAsSeeds() throws Exception {
    ImageProcessor test = new ByteProcessor(128, 256);
    test.putPixel(5, 10, 1);
    test.putPixel(10, 20, 2);
    test.putPixel(20, 30, 4);

    Seeds ret = SeedProcessor.getGrayscaleAsSeeds(test);
    assertThat(ret.size(), is(1));
    assertThat(ret.get(SeedTypes.FOREGROUNDS), is(not(nullValue())));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).isEmpty(), is(false));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).size(), is(3));

    for (int i = 0; i < 3; i++) {
      ImageProcessor t = ret.get(SeedTypes.FOREGROUNDS).get(i);
      int[] h = t.getHistogram();
      assertThat(h[0], is(128 * 256 - 1));
      assertThat(h[255], is(1));
    }

    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(0).getPixel(5, 10), is(255));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(1).getPixel(10, 20), is(255));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(2).getPixel(20, 30), is(255));

  }

  /**
   * Test method for
   * {@link SeedProcessor#decodeSeedsRoiStack(List, String, String, int, int, int)}
   * 
   * <p>Similar to
   * {@link com.github.celldynamics.quimp.plugin.randomwalk.SeedProcessorTest#testDecodeSeedsRoi()}
   * 
   * @throws Exception Exception
   * 
   */
  @Test
  public void testDecodeSeedsRoiStack() throws Exception {
    String fgcore = "fg";
    String bgcore = "bg";
    Roi l1 = new Line(0, 0, 10, 0); // 11 pixels
    l1.setName(fgcore + "0_0");
    Roi l2 = new Line(5, 5, 5, 10); // 6 pixels;
    l2.setName(fgcore + "0_2");

    Roi l3 = new Line(10, 10, 20, 10); // 11
    l3.setName(fgcore + "2_0"); // skip one

    Roi l4 = new Line(7, 7, 7, 7);
    l4.setName(bgcore + "0_0");
    Roi l5 = new Line(32, 32, 32, 32);
    l5.setName(bgcore + "0_1");
    List<Roi> rois = new ArrayList<>();
    rois.add(l1);
    rois.add(l2);
    rois.add(l3);
    rois.add(l4);
    rois.add(l5);

    // check for one slice
    List<Seeds> r = SeedProcessor.decodeSeedsRoiStack(rois, fgcore, bgcore, 128, 128, 1);
    Seeds ret = r.get(0);

    assertThat(ret.size(), is(2));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).size(), is(2));
    assertThat(ret.get(SeedTypes.BACKGROUND).size(), is(1));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(0).getWidth(), is(128));
    assertThat(ret.get(SeedTypes.FOREGROUNDS).get(0).getHeight(), is(128));
    // first FG
    ImageProcessor ip = ret.get(SeedTypes.FOREGROUNDS).get(0);
    int[] h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 17)); // we have 17 pixels white
    assertThat(h[255], is(17));

    // second FG
    ip = ret.get(SeedTypes.FOREGROUNDS).get(1);
    h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 11)); // we have 21 pixels white
    assertThat(h[255], is(11));

    // BG
    ip = ret.get(SeedTypes.BACKGROUND).get(0);
    h = ip.getHistogram();
    assertThat(h[0], is(128 * 128 - 2)); // we have 21 pixels white
    assertThat(h[255], is(2));
  }

}
