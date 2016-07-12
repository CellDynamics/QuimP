/**
 * @file FakeSegmentationTest.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.geom.TrackOutline;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * @author p.baniukiewicz
 * @date 27 Jun 2016
 *
 */
public class FakeSegmentationTest {

    /**
     * Accessor to private field
     * 
     * @param name Name of private field
     * @param obj Reference to object 
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException         
     */
    static Object accessPrivateField(String name, FakeSegmentation obj) throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        Field prv = obj.getClass().getDeclaredField(name);
        prv.setAccessible(true);
        return prv.get(obj);
    }

    static Object accessPrivate(String name, FakeSegmentation obj, Object[] param,
            Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
        prv.setAccessible(true);
        return prv.invoke(obj, param);
    }

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(FakeSegmentationTest.class.getName());

    private ImagePlus test1;
    private ImagePlus test2;
    private ImagePlus test3;
    private ImagePlus test4;
    private ImagePlus test5;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        test1 = IJ.openImage("src/test/resources/BW_seg_5_slices.tif");
        test2 = IJ.openImage("src/test/resources/BW_seg_5_slices_no_last.tif");
        test3 = IJ.openImage("src/test/resources/BW_seg_5_slices_no_middle_last.tif");
        test4 = IJ.openImage("src/test/resources/BW_seg_5_slices_no_first.tif");
        test5 = IJ.openImage("src/test/resources/BW_seg_1_slice.tif");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        test1.close();
        test2.close();
        test3.close();
        test4.close();
        test5.close();
    }

    /**
     * @test Check outline generation
     * @throws Exception
     */
    @Test
    public void testFakeSegmentation() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
        TrackOutline[] trackers = (TrackOutline[]) accessPrivateField("trackers", obj);
        LOGGER.debug(Arrays.asList(trackers));
    }

    /**
     * @test Check intersection
     * @pre Slices do not overlap
     * @throws Exception
     */
    @Test
    public void testTestIntersect_1() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
        ShapeRoi r1 = new ShapeRoi(new Roi(0, 0, 100, 100));
        ShapeRoi r2 = new ShapeRoi(new Roi(101, 101, 100, 100));

        boolean ret = (boolean) accessPrivate("testIntersect", obj, new Object[] { r1, r2 },
                new Class<?>[] { ShapeRoi.class, ShapeRoi.class });

        assertThat(ret, is(false));
    }

    /**
     * @test Check intersection
     * @pre Slices do overlap
     * @throws Exception
     */
    @Test
    public void testTestIntersect_2() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
        ShapeRoi r1 = new ShapeRoi(new Roi(0, 0, 100, 100));
        ShapeRoi r3 = new ShapeRoi(new Roi(50, 50, 100, 100));

        boolean ret = (boolean) accessPrivate("testIntersect", obj, new Object[] { r1, r3 },
                new Class<?>[] { ShapeRoi.class, ShapeRoi.class });

        assertThat(ret, is(true));
    }

    /**
     * @test Check intersection for one Roi and Array of ROIs
     * @pre One slice does overlap
     * @throws Exception
     */
    @Test
    public void testTestIntersect_3() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
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
        assertThat(r2.getId(), is(SegmentedShapeRoi.NOT_COUNTED)); // this not overlap and has not
                                                                   // id yet

    }

    /**
     * @test Check intersection for one Roi and Array of ROIs
     * @pre The same slice as on input (referenced)
     * @post this slice correctly labeled
     * @throws Exception
     */
    @Test
    public void testTestIntersect_Same() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
        SegmentedShapeRoi r1 = new SegmentedShapeRoi(new Roi(0, 0, 100, 100));
        ArrayList<SegmentedShapeRoi> test = new ArrayList<>();
        test.add(r1);

        accessPrivate("testIntersect", obj, new Object[] { r1, test },
                new Class<?>[] { r1.getClass(), test.getClass() });

        assertThat(r1.getId(), is(0)); // first outline
    }

    /**
     * @test Check intersection for one Roi and Array of ROIs
     * @pre Slices do not overlap
     * @throws Exception
     */
    @Test
    public void testTestIntersect_4() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
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
     * @test Check generated chains
     * @pre Three objects on 5 frames 
     * @throws Exception
     */
    @Test
    public void testGetChains() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1); // create object with stack
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        assertThat(ret.size(), is(3)); // check number of objects
        for (ArrayList<SegmentedShapeRoi> aL : ret) { // iterate over objects and get subsequent
            // segmentations
            assertThat(aL.size(), is(5)); // all objects are on all frames
            for (int i = 0; i < 5; i++) {
                SegmentedShapeRoi oF = (SegmentedShapeRoi) aL.get(i); // cast to SegmentedShapeRoi
                assertThat(oF.getFrame(), is(i + 1)); // and check if every next outline for given
                                                      // cell has increasing frame number
            }
        }

        RoiSaver.saveROIs(test1, "/tmp/testGetChains.tif", ret);
    }

    /**
     * @test Check generated chains
     * @pre Three objects on 1-4 frames, 2 objects on 5th 
     * @post Three objects detected but one has shorter chain (frames 1-4)
     * @throws Exception
     */
    @Test
    public void testGetChains_no_last() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test2); // create object with stack
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        assertThat(ret.size(), is(3)); // check number of objects

        assertThat(ret.get(0).size(), is(4)); // missing on 5th frame
        assertThat(ret.get(1).size(), is(5));
        assertThat(ret.get(2).size(), is(5));

        for (int i = 0; i < 4; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(0).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 5; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(1).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 5; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(2).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        RoiSaver.saveROIs(test2, "/tmp/testGetChains_no_last.tif", ret);
    }

    /**
     * @test Check generated chains
     * @pre Three objects on 2-5 frames, 2 objects on 1st 
     * @post Three objects detected but one has shorter chain (frames 2-5)
     * @throws Exception
     */
    @Test
    public void testGetChains_no_first() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test4); // create object with stack
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        assertThat(ret.size(), is(3)); // check number of objects

        assertThat(ret.get(0).size(), is(5));
        assertThat(ret.get(1).size(), is(5));
        assertThat(ret.get(2).size(), is(4)); // missing on 1st frame

        for (int i = 0; i < 4; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(0).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 5; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(1).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 4; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(2).get(i);
            assertThat(oF.getFrame(), is(i + 2)); // starts from 2nd frame
        }

        RoiSaver.saveROIs(test4, "/tmp/testGetChains_no_first.tif", ret); // wrong image because of
                                                                          // saveROIs
    }

    /**
     * @test Check generated chains
     * @pre Three objects on frames 1,2,4 and 2 objects on 3,5 
     * @post 4 chains detected. Missing object breaks the chain
     * @throws Exception
     */
    @Test
    public void testGetChains_no_middle_last() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test3); // create object with stack
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results
        assertThat(ret.size(), is(4)); // check number of objects

        assertThat(ret.get(0).size(), is(4)); // missing on 5th frame
        assertThat(ret.get(1).size(), is(2));
        assertThat(ret.get(2).size(), is(5));
        assertThat(ret.get(3).size(), is(2)); // new chain after break

        for (int i = 0; i < 4; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(0).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 2; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(1).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 5; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(2).get(i);
            assertThat(oF.getFrame(), is(i + 1));
        }

        for (int i = 0; i < 2; i++) {
            SegmentedShapeRoi oF = (SegmentedShapeRoi) ret.get(3).get(i);
            assertThat(oF.getFrame(), is(i + 4)); // new chain from 4th frame
        }

        RoiSaver.saveROIs(test3, "/tmp/testGetChains_no_middle_last.tif", ret); // wrong image
                                                                                // because of
                                                                                // saveROIs
    }

    /**
     * @test Check generated chains
     * @pre only one slice on input
     * @post Correct segmentation
     * @throws Exception
     */
    @Test
    public void testGetChains_oneSlice() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test5); // create object with stack
        obj.trackObjects(); // run tracking
        ArrayList<ArrayList<SegmentedShapeRoi>> ret = obj.getChains(); // get results

        RoiSaver.saveROIs(test5, "/tmp/testGetChains_oneSlice.tif", ret); // wrong image
                                                                          // because of
                                                                          // saveROIs
    }

}
