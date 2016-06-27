/**
 * @file FakeSegmentationTest.java
 * @date 27 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Color;
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
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.QuimP.geom.TrackOutline;

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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        test1.close();
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

        assertThat(r1.id, is(0)); // first outline
        assertThat(r3.id, is(0)); // second outline has ID of first if they overlap
        assertThat(r2.id, is(SegmentedShapeRoi.NOT_COUNTED)); // this not overlap and has not id yet

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

        assertThat(r1.id, is(SegmentedShapeRoi.NOT_COUNTED));
        assertThat(r2.id, is(SegmentedShapeRoi.NOT_COUNTED));
        assertThat(r3.id, is(SegmentedShapeRoi.NOT_COUNTED));

        assertThat(r4.id, is(0)); // first outline always has id assigned
    }

    /**
     * @test Check generated chains
     * @pre Three objects on 5 frames 
     * @throws Exception
     */
    @Test
    public void testGetChains() throws Exception {
        FakeSegmentation obj = new FakeSegmentation(test1);
        obj.trackObjects();
        ArrayList<ArrayList<ShapeRoi>> ret = obj.getChains();
        assertThat(ret.size(), is(3));
        for (ArrayList<ShapeRoi> aL : ret)
            assertThat(aL.size(), is(5));

        ImagePlus cp = test1.duplicate();
        new ImageConverter(cp).convertToRGB();
        for (ArrayList<ShapeRoi> aL : ret) {
            QColor qcolor = QColor.lightColor();
            Color color = new Color(qcolor.getColorInt());
            for (int i = 0; i < aL.size(); i++) {
                ImageProcessor currentP = cp.getImageStack().getProcessor(i + 1);
                currentP.setColor(color);
                currentP.setLineWidth(2);
                aL.get(i).drawPixels(currentP);
            }
        }
        IJ.saveAsTiff(cp, "/tmp/testGetChains.tif"); // save image

    }

}
