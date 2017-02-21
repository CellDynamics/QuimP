/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.io.File;
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
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.Point;
import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.PropagateSeeds;
import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("unused")
public class PropagateSeedsTest {
  static {
    System.setProperty("logback.configurationFile", "quimp-logback.xml");
  }

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

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
  static Object accessPrivate(String name, PropagateSeeds.Contour obj, Object[] param,
          Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
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
  static Object accessPrivate(String name, PropagateSeeds.Morphological obj, Object[] param,
          Class<?>[] paramtype) throws NoSuchMethodException, SecurityException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method prv = obj.getClass().getDeclaredMethod(name, paramtype);
    prv.setAccessible(true);
    return prv.invoke(obj, param);
  }

  /**
   * The test image 2.
   */
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

  /**
   * Test propagate seed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPropagateSeed() throws Exception {
    ImagePlus ip = testImage2.duplicate();
    BinaryProcessor ret = new BinaryProcessor(ip.getProcessor().convertToByteProcessor());
    Map<Integer, List<Point>> seed = new PropagateSeeds.Morphological().propagateSeed(ret, 20, 30);
    // IJ.saveAsTiff(new ImagePlus("", ret), "/tmp/testPropagateSeed_20.tif");
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIterateMorphological() throws Exception {
    ImagePlus ip = testImage2.duplicate();
    BinaryProcessor rete =
            new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
    BinaryProcessor retd =
            new BinaryProcessor(ip.getProcessor().duplicate().convertToByteProcessor());
    PropagateSeeds.Morphological obj = new PropagateSeeds.Morphological();
    accessPrivate("iterateMorphological", obj, new Object[] { rete, PropagateSeeds.ERODE, 3 },
            new Class[] { BinaryProcessor.class, int.class, double.class });
    IJ.saveAsTiff(new ImagePlus("", rete), tmpdir + "testIterateMorphological_erode3.tif");

    accessPrivate("iterateMorphological", obj, new Object[] { retd, PropagateSeeds.DILATE, 5 },
            new Class[] { BinaryProcessor.class, int.class, double.class });
    IJ.saveAsTiff(new ImagePlus("", retd), tmpdir + "testIterateMorphological_dilate5.tif");

  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetOutline() throws Exception {
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
    List<Outline> ret = (List<Outline>) accessPrivate("getOutline", cc,
            new Object[] { testImage2.getProcessor() }, new Class<?>[] { ImageProcessor.class });

    RoiSaver.saveROI(tmpdir + "test0.tif", ret.get(0).asList());
    RoiSaver.saveROI(tmpdir + "test1.tif", ret.get(1).asList());
    RoiSaver.saveROI(tmpdir + "test2.tif", ret.get(2).asList());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testPropagateSeedOutline() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
    cc.propagateSeed(ip.getProcessor(), 5, 10);

  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetCompositeSeed_Contour() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true);
    ImagePlus org = IJ.openImage("src/test/resources/G.tif");
    ImagePlus mask = IJ.openImage("src/test/resources/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), 5, 10);
    ImagePlus ret = cc.getCompositeSeed(org);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeed.tif");
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetCompositeSeed_Morphological() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Morphological cc = new PropagateSeeds.Morphological(true);
    ImagePlus org = IJ.openImage("src/test/resources/G.tif");
    ImagePlus mask = IJ.openImage("src/test/resources/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), 20, 40);
    ImagePlus ret = cc.getCompositeSeed(org);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeedM.tif");
  }

}
