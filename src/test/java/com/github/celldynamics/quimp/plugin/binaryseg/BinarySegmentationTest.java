package com.github.celldynamics.quimp.plugin.binaryseg;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.geom.TrackOutline;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;

/**
 * Test of low level API.
 * 
 * @author p.baniukiewicz
 * @see BinarySegmentation_Test
 */
public class BinarySegmentationTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Accessor to private field.
   * 
   * @param name Name of private field
   * @param obj Reference to object
   * @return of private method
   * @throws NoSuchFieldException NoSuchFieldException
   * @throws SecurityException SecurityException
   * @throws IllegalArgumentException IllegalArgumentException
   * @throws IllegalAccessException IllegalAccessException
   */
  static Object accessPrivateField(String name, BinarySegmentation obj) throws NoSuchFieldException,
          SecurityException, IllegalArgumentException, IllegalAccessException {
    Field prv = obj.getClass().getDeclaredField(name);
    prv.setAccessible(true);
    return prv.get(obj);
  }

  /**
   * Access private.
   *
   * @param name the name
   * @param obj the obj
   * @param param the param
   * @param paramtype the paramtype
   * @return the object
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  static Object accessPrivate(String name, BinarySegmentation obj, Object[] param,
          Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(BinarySegmentationTest.class.getName());

  private ImagePlus test1;
  private ImagePlus test2;
  private ImagePlus test3;
  private ImagePlus test4;
  private ImagePlus test5;
  private ImagePlus test6;

  /**
   * setUp.
   *
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    test1 = IJ.openImage("src/test/Resources-static/BW_seg_5_slices.tif");
    test2 = IJ.openImage("src/test/Resources-static/BW_seg_5_slices_no_last.tif");
    test3 = IJ.openImage("src/test/Resources-static/BW_seg_5_slices_no_middle_last.tif");
    test4 = IJ.openImage("src/test/Resources-static/BW_seg_5_slices_no_first.tif");
    test5 = IJ.openImage("src/test/Resources-static/BW_seg_1_slice.tif");
    test6 = IJ.openImage("src/test/Resources-static/GR_seg_6_slices.tif");
  }

  /**
   * tearDown.
   *
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    test1.close();
    test2.close();
    test3.close();
    test4.close();
    test5.close();
    test6.close();
  }

  /**
   * Check outline generation.
   *
   * @throws Exception Exception
   */
  @Test
  public void testBinarySegmentation() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    TrackOutline[] trackers = (TrackOutline[]) accessPrivateField("trackers", obj);
    LOGGER.debug(Arrays.asList(trackers).toString());
  }

  /**
   * Check intersection.
   *
   * <p>pre: Slices do not overlap
   *
   * @throws Exception Exception
   */
  @Test
  public void testTestIntersect_1() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    ShapeRoi r1 = new ShapeRoi(new Roi(0, 0, 100, 100));
    ShapeRoi r2 = new ShapeRoi(new Roi(101, 101, 100, 100));

    boolean ret = (boolean) accessPrivate("testIntersect", obj, new Object[] { r1, r2 },
            new Class<?>[] { ShapeRoi.class, ShapeRoi.class });

    assertThat(ret, is(false));
  }

  /**
   * Check intersection.
   *
   * <p>pre: Slices do overlap
   *
   * @throws Exception Exception
   */
  @Test
  public void testTestIntersect_2() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    ShapeRoi r1 = new ShapeRoi(new Roi(0, 0, 100, 100));
    ShapeRoi r3 = new ShapeRoi(new Roi(50, 50, 100, 100));

    boolean ret = (boolean) accessPrivate("testIntersect", obj, new Object[] { r1, r3 },
            new Class<?>[] { ShapeRoi.class, ShapeRoi.class });

    assertThat(ret, is(true));
  }

  /**
   * Check intersection for one Roi and Array of ROIs.
   *
   * <p>pre: One slice does overlap
   *
   * @throws Exception Exception
   */
  @Test
  public void testTestIntersect_3() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    SegmentedShapeRoi r1 = new SegmentedShapeRoi(new Roi(0, 0, 100, 100));
    SegmentedShapeRoi r2 = new SegmentedShapeRoi(new Roi(101, 101, 100, 100));
    SegmentedShapeRoi r3 = new SegmentedShapeRoi(new Roi(50, 50, 100, 100));
    ArrayList<SegmentedShapeRoi> test = new ArrayList<>();
    test.add(r2);
    test.add(r3);

    accessPrivate("testIntersect", obj, new Object[] { r1, test },
            new Class<?>[] { r1.getClass(), test.getClass() });

    assertThat(r1.getId(), is(0)); // first outline
    assertThat(r3.getId(), is(0)); // second outline has ID of first if they overlap
    assertThat(r2.getId(), is(SegmentedShapeRoi.NOT_COUNTED)); // this not overlap and has no id

  }

  /**
   * Check intersection for one Roi and Array of ROIs.
   *
   * <p>pre: The same slice as on input (referenced)
   *
   * <p>post: this slice correctly labelled
   *
   * @throws Exception Exception
   */
  @Test
  public void testTestIntersect_Same() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    SegmentedShapeRoi r1 = new SegmentedShapeRoi(new Roi(0, 0, 100, 100));
    ArrayList<SegmentedShapeRoi> test = new ArrayList<>();
    test.add(r1);

    accessPrivate("testIntersect", obj, new Object[] { r1, test },
            new Class<?>[] { r1.getClass(), test.getClass() });

    assertThat(r1.getId(), is(0)); // first outline
  }

  /**
   * Check intersection for one Roi and Array of ROIs.
   *
   * <p>pre: Slices do not overlap
   *
   * @throws Exception Exception
   */
  @Test
  public void testTestIntersect_4() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1);
    SegmentedShapeRoi r1 = new SegmentedShapeRoi(new Roi(0, 0, 100, 100));
    SegmentedShapeRoi r2 = new SegmentedShapeRoi(new Roi(101, 101, 100, 100));
    SegmentedShapeRoi r3 = new SegmentedShapeRoi(new Roi(50, 50, 100, 100));
    SegmentedShapeRoi r4 = new SegmentedShapeRoi(new Roi(300, 300, 100, 100));
    ArrayList<SegmentedShapeRoi> test = new ArrayList<>();
    test.add(r1);
    test.add(r2);
    test.add(r3);

    accessPrivate("testIntersect", obj, new Object[] { r4, test },
            new Class<?>[] { r1.getClass(), test.getClass() });

    assertThat(r1.getId(), is(SegmentedShapeRoi.NOT_COUNTED));
    assertThat(r2.getId(), is(SegmentedShapeRoi.NOT_COUNTED));
    assertThat(r3.getId(), is(SegmentedShapeRoi.NOT_COUNTED));

    assertThat(r4.getId(), is(0)); // first outline always has id assigned
  }

  /**
   * Check generated chains.
   *
   * <p>pre: Three objects on 5 frames
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test1); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(3)); // check number of objects
    for (ArrayList<SegmentedShapeRoi> al : ret) { // iterate over objects and get subsequent
      // segmentations
      assertThat(al.size(), is(5)); // all objects are on all frames
      for (int i = 0; i < 5; i++) {
        SegmentedShapeRoi of = (SegmentedShapeRoi) al.get(i); // cast to SegmentedShapeRoi
        assertThat(of.getFrame(), is(i + 1)); // and check if every next outline for given
        // cell has increasing frame number
      }
    }

    RoiSaver.saveRois(test1, tmpdir + "testGetChains.tif", ret);
  }

  /**
   * Check generated chains for grayscale image.
   * 
   * <p>pre: Two objects on 30 frames
   * 
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_xxx() throws Exception {
    ImagePlus testImage = IJ.openImage("src/test/Resources-static//Segmented_Stack-30.tif");
    BinarySegmentation obj = new BinarySegmentation(testImage);
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(2)); // check number of objects
    for (ArrayList<SegmentedShapeRoi> al : ret) { // iterate over objects and get subsequent
      // segmentations
      assertThat(al.size(), is(30)); // all objects are on all frames
      for (int i = 0; i < 30; i++) {
        SegmentedShapeRoi of = (SegmentedShapeRoi) al.get(i); // cast to SegmentedShapeRoi
        assertThat(of.getFrame(), is(i + 1)); // and check if every next outline for given
        // cell has increasing frame number
      }
    }

    RoiSaver.saveRois(testImage, tmpdir + "testGetChains_xxx.tif", ret);
  }

  /**
   * Check generated chains.
   *
   * <p>pre: Three objects on 1-4 frames, 2 objects on 5th
   *
   * <p>post: Three objects detected but one has shorter chain (frames 1-4)
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_no_last() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test2); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(3)); // check number of objects

    assertThat(ret.get(0).size(), is(4)); // missing on 5th frame
    assertThat(ret.get(1).size(), is(5));
    assertThat(ret.get(2).size(), is(5));

    for (int i = 0; i < 4; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(0).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 5; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(1).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 5; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(2).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    RoiSaver.saveRois(test2, tmpdir + "testGetChains_no_last.tif", ret);
  }

  /**
   * Check generated chains.
   *
   * <p>pre: Three objects on 2-5 frames, 2 objects on 1st
   *
   * <p>post: Three objects detected but one has shorter chain (frames 2-5)
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_no_first() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test4); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(3)); // check number of objects

    assertThat(ret.get(0).size(), is(5));
    assertThat(ret.get(1).size(), is(5));
    assertThat(ret.get(2).size(), is(4)); // missing on 1st frame

    for (int i = 0; i < 4; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(0).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 5; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(1).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 4; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(2).get(i);
      assertThat(of.getFrame(), is(i + 2)); // starts from 2nd frame
    }

    RoiSaver.saveRois(test4, tmpdir + "testGetChains_no_first.tif", ret); // wrong image because
    // of saveROIs
  }

  /**
   * Check generated chains.
   *
   * <p>pre: Three objects on frames 1,2,4 and 2 objects on 3,5
   *
   * <p>post: 4 chains detected. Missing object breaks the chain
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_no_middle_last() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test3); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(4)); // check number of objects

    assertThat(ret.get(0).size(), is(4)); // missing on 5th frame
    assertThat(ret.get(1).size(), is(2));
    assertThat(ret.get(2).size(), is(5));
    assertThat(ret.get(3).size(), is(2)); // new chain after break

    for (int i = 0; i < 4; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(0).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 2; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(1).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 5; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(2).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }

    for (int i = 0; i < 2; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(3).get(i);
      assertThat(of.getFrame(), is(i + 4)); // new chain from 4th frame
    }

    RoiSaver.saveRois(test3, tmpdir + "testGetChains_no_middle_last.tif", ret); // wrong image
    // because of saveROIs
  }

  /**
   * Check generated chains.
   *
   * <p>pre: Four tracks same color used on first and last frame but objects are not merged because
   * it looks only one frame forward to find same colors
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_gray() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test6); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
    assertThat(ret.size(), is(4)); // check number of objects

    assertThat(ret.get(0).size(), is(6)); // all frames
    assertThat(ret.get(1).size(), is(1));
    assertThat(ret.get(2).size(), is(3));
    assertThat(ret.get(3).size(), is(1));

    for (int i = 0; i < 6; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(0).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }
    for (int i = 0; i < 1; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(1).get(i);
      assertThat(of.getFrame(), is(i + 1));
    }
    for (int i = 0; i < 3; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(2).get(i);
      assertThat(of.getFrame(), is(i + 3));
    }
    for (int i = 0; i < 1; i++) {
      SegmentedShapeRoi of = (SegmentedShapeRoi) ret.get(3).get(i);
      assertThat(of.getFrame(), is(i + 6));
    }

    RoiSaver.saveRois(test6, tmpdir + "testGetChains_gray.tif", ret); // wrong image
    // because of saveROIs
  }

  /**
   * Check generated chains.
   *
   * <p>pre: only one slice on input
   *
   * <p>post: Correct segmentation
   *
   * @throws Exception Exception
   */
  @Test
  public void testGetChains_oneSlice() throws Exception {
    BinarySegmentation obj = new BinarySegmentation(test5); // create object with stack
    obj.trackObjects(); // run tracking
    ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results

    RoiSaver.saveRois(test5, tmpdir + "testGetChains_oneSlice.tif", ret); // wrong image
    // because of saveROIs
  }

}
