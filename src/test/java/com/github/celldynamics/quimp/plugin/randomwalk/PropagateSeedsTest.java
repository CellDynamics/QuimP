package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.geom.filters.OutlineProcessor;
import com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds.Contour;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;
import com.github.celldynamics.quimp.utils.CsvWritter;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

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

    RoiSaver.saveRoi(tmpdir + "testGetOutline0_QuimP.tif", ret.get(0).asList());
    RoiSaver.saveRoi(tmpdir + "testGetOutline1_QuimP.tif", ret.get(1).asList());
    RoiSaver.saveRoi(tmpdir + "testGetOutline2_QuimP.tif", ret.get(2).asList());
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
    cc.propagateSeed(ip.getProcessor(), ip.getProcessor(), 5, 10);

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
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(true, null);
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus mask = IJ.openImage("src/test/Resources-static/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), mask.getStack().getProcessor(1), 5, 10);
    ImagePlus ret = cc.getCompositeSeed(org, 0);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeed_QuimP.tif");
  }

  /**
   * testGetCompositeSeed_Contour.
   * 
   * <p>Check if high scaling factor will remove thin objects.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetCompositeSeed_Contour1() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus ip = IJ.openImage("src/test/Resources-static/scaletest.tif");
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(false, null);

    Map<Seeds, ImageProcessor> ret = cc.propagateSeed(ip.getProcessor(), ip.getProcessor(), 2, 10);
    IJ.saveAsTiff(new ImagePlus("", ret.get(Seeds.BACKGROUND)),
            tmpdir + "testGetCompositeSeed_Contour1_B_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", ret.get(Seeds.FOREGROUND)),
            tmpdir + "testGetCompositeSeed_Contour1_F_QuimP.tif");
  }

  /**
   * testGetCompositeSeed_Contour.
   * 
   * <p>Produce outline plot with scaled and original outline.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetCompositeSeed_Contour2() throws Exception {
    ImagePlus ip = IJ.openImage("src/test/Resources-static/239/shape1.tif");
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(false, null, 0.35, 5, 0);

    Outline outlineOrg = Contour.getOutline(ip.getProcessor()).get(0);
    new OutlineProcessor<Outline>(outlineOrg).averageCurvature(1).sumCurvature(1);
    Map<Seeds, ImageProcessor> ret = cc.propagateSeed(ip.getProcessor(), ip.getProcessor(), 5, 10);
    Outline outlineSh = Contour.getOutline(ret.get(Seeds.FOREGROUND)).get(0);
    ImageProcessor bck = ret.get(Seeds.BACKGROUND);
    bck.invert();
    Outline outlineEx = Contour.getOutline(bck).get(0);

    RoiSaver.saveRois(tmpdir + "reo.tif", 512, 512, outlineOrg.asList(), Color.GREEN,
            outlineSh.asList(), Color.RED, outlineEx.asList(), Color.BLUE);
    CsvWritter csv =
            new CsvWritter(Paths.get(tmpdir, "outlineOrg.csv"), FormatConverter.headerEcmmOutline);
    FormatConverter.saveOutline(outlineOrg, csv);
    csv.close();
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
    PropagateSeeds.Morphological cc = new PropagateSeeds.Morphological(true, null);
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus mask = IJ.openImage("src/test/Resources-static/GMask.tif");

    cc.propagateSeed(mask.getStack().getProcessor(1), mask.getStack().getProcessor(1), 20, 40);
    ImagePlus ret = cc.getCompositeSeed(org, 0);
    IJ.saveAsTiff(ret, tmpdir + "testGetCompositeSeedM_QuimP.tif");
  }

  /**
   * Test of getTrueBackground().
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetTrueBackground() throws Exception {
    ImageJ ij = new ImageJ();
    ImagePlus testImage = IJ.openImage("src/test/Resources-static/PropagateSeeds/stack.tif");
    ImagePlus testImagemask =
            IJ.openImage("src/test/Resources-static/PropagateSeeds/stack-mask.tif");

    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(false, AutoThresholder.Method.Otsu);
    Map<Seeds, ImageProcessor> ret =
            cc.propagateSeed(testImagemask.getProcessor(), testImagemask.getProcessor(), 5, 10);
    ImageProcessor bck = cc.getTrueBackground(ret.get(Seeds.BACKGROUND), testImage.getProcessor());
    IJ.saveAsTiff(new ImagePlus("", ret.get(Seeds.BACKGROUND)),
            tmpdir + "testPropagateSeedBackground_B_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", ret.get(Seeds.FOREGROUND)),
            tmpdir + "testPropagateSeedBackground_F_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", bck), tmpdir + "testPropagateSeedBackground_MOD_QuimP.tif");
    // output is expected to not contain expanded cell and has removed other cells from background
    // area
  }

}
