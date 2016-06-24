/**
 * @file TrackOutlineTest.java
 * @date 24 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import java.lang.reflect.Field;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * @author p.baniukiewicz
 * @date 24 Jun 2016
 *
 */
public class TrackOutlineTest {

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
    static Object accessPrivateField(String name, TrackOutline obj) throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        Field prv = obj.getClass().getDeclaredField(name);
        prv.setAccessible(true);
        return prv.get(obj);
    }

    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(TrackOutlineTest.class.getName());
    private ImagePlus image;
    private TrackOutline obj;

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
        image = IJ.openImage("src/test/resources/outline_track_1.tif");
        obj = new TrackOutline(image, 0);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (image.changes) { // check if source was modified
            image.changes = false; // set flag to false to prevent save dialog
            image.close(); // close image
            throw new Exception("Image has been modified"); // throw exception
                                                            // if source image
                                                            // was modified
        }
        image.close();
        obj = null;
    }

    /**
     * @test testPrepare
     * @post Generate filtered image
     * @throws Exception
     */
    @Test
    public void testPrepare() throws Exception {
        ImagePlus ret = obj.prepare();
        IJ.saveAsTiff(ret, "/tmp/testPrepare.tif");
    }

    /**
     * @test testGetOutline
     * @post Find one outline for given coords
     * @throws Exception
     */
    @Test
    public void testGetOutline() throws Exception {
        List<Point2d> ret = obj.getOutline(270, 227, 255);
        LOGGER.debug(ret);
        IJ.saveAsTiff((ImagePlus) accessPrivateField("prepared", obj), "/tmp/testGetOutline.tif");
    }

    /**
     * @test testGetOutlines
     * @post Finds all outlines in image
     * @throws Exception
     */
    @Test
    public void testGetOutlines() throws Exception {
        List<List<Point2d>> ret = obj.getOutlines();
        LOGGER.debug("Found " + ret.size());
        IJ.saveAsTiff((ImagePlus) accessPrivateField("prepared", obj), "/tmp/testGetOutlines.tif");
        RoiSaver.saveROI("/tmp/testGetOutlines_roi.tif", ret.get(0));
    }

    /**
     * @test testGetOutlines_1
     * @post Finds all outlines in image with smoothing
     * @throws Exception
     */
    @Test
    public void testGetOutlines_1() throws Exception {
        obj.setConfig(-1, 1, true);
        List<List<Point2d>> ret = obj.getOutlines();
        LOGGER.debug("Found " + ret.size());
        RoiSaver.saveROI("/tmp/testGetOutlines_roi_s.tif", ret.get(0));
    }

    /**
     * @test testGetOutlines_2
     * @post Finds all outlines in image with smoothing and step 6
     * @throws Exception
     */
    @Test
    public void testGetOutlines_6() throws Exception {
        obj.setConfig(-1, 6, true);
        List<List<Point2d>> ret = obj.getOutlines();
        LOGGER.debug("Found " + ret.size());
        RoiSaver.saveROI("/tmp/testGetOutlines_roi_s6.tif", ret.get(0));
    }

}
