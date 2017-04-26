package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.plugin.utils.RoiSaver;

/**
 * @author p.baniukiewicz
 *
 */
@SuppressWarnings("unused")
public class PropagateSeedsTest {

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
   * @throws java.lang.Exception on error
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    testImage2 = IJ.openImage("src/test/Resources-static/binary_1.tif");
  }

  /**
   * @throws java.lang.Exception on error
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    testImage2.close();
  }

  /**
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception on error
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test getOutline.
   * 
   * @throws Exception on error
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testGetOutline() throws Exception {
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
    List<Outline> ret = (List<Outline>) accessPrivate("getOutline", cc,
            new Object[] { testImage2.getProcessor() }, new Class<?>[] { ImageProcessor.class });

    RoiSaver.saveROI(tmpdir + "testGetOutline0_QuimP.tif", ret.get(0).asList());
    RoiSaver.saveROI(tmpdir + "testGetOutline1_QuimP.tif", ret.get(1).asList());
    RoiSaver.saveROI(tmpdir + "testGetOutline2_QuimP.tif", ret.get(2).asList());
  }

  /**
   * Test propagate.
   * 
   * @throws Exception on error
   */
  @Test
  public void testPropagateSeedOutline() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
    cc.propagateSeed(ip.getProcessor(), 5, 10);

  }

  /**
   * testGetCompositeSeed_Contour.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetCompositeSeed_Contour() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true);
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus mask = IJ.openImage("src/test/Resources-static/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), 5, 10);
    ImagePlus ret = cc.getCompositeSeed(org, 0);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeed_QuimP.tif");
  }

  /**
   * testGetCompositeSeed_Morphological.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetCompositeSeed_Morphological() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = testImage2.duplicate();
    PropagateSeeds.Morphological cc = new PropagateSeeds.Morphological(true);
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus mask = IJ.openImage("src/test/Resources-static/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), 20, 40);
    ImagePlus ret = cc.getCompositeSeed(org, 0);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeedM_QuimP.tif");
  }

}
