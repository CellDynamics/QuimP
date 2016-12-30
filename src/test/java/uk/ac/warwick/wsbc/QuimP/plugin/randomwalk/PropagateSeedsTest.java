/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.Outline;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.PropagateSeeds.Morphological;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver;

/**
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("unused")
public class PropagateSeedsTest {

    static Object accessPrivate(String name, PropagateSeeds.Contour obj, Object[] param,
            Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
        prv.setAccessible(true);
        return prv.invoke(obj, param);
    }

    static ImagePlus testImage2;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testImage2 = IJ.openImage("src/test/resources/binary_1.tif");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        testImage2.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPropagateSeed() throws Exception {
        ImagePlus ip = testImage2.duplicate();
        BinaryProcessor ret = new BinaryProcessor(ip.getProcessor().convertToByteProcessor());
        Map<Integer, List<Point>> seed = Morphological.propagateSeed(ret, 20);
        // IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testPropagateSeed_20.tif");
    }

    /**
     * @test of eroding
     * @post eroded image on disk
     * @throws Exception
     */
    @Test
    public void testIterateMorphological() throws Exception {
        ImagePlus ip = testImage2.duplicate();
        BinaryProcessor rete =
                new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
        BinaryProcessor retd =
                new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
        Morphological.iterateMorphological(rete, PropagateSeeds.ERODE, 3);
        IJ.saveAsTiff(new ImagePlus("", rete), "/tmp/testIterateMorphological_erode3.tif");

        Morphological.iterateMorphological(retd, PropagateSeeds.DILATE, 5);
        IJ.saveAsTiff(new ImagePlus("", retd), "/tmp/testIterateMorphological_dilate5.tif");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOutline() throws Exception {
        PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
        List<Outline> ret = (List<Outline>) accessPrivate("getOutline", cc,
                new Object[] { testImage2.getProcessor() },
                new Class<?>[] { ImageProcessor.class });

        RoiSaver.saveROI("c:/Users/baniu/Downloads/test0.tif", ret.get(0).asList());
        RoiSaver.saveROI("c:/Users/baniu/Downloads/test1.tif", ret.get(1).asList());
        RoiSaver.saveROI("c:/Users/baniu/Downloads/test2.tif", ret.get(2).asList());
    }

}
