package com.github.celldynamics.quimp.plugin.randomwalk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.filesystem.converter.FormatConverter;
import com.github.celldynamics.quimp.geom.filters.OutlineProcessor;
import com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds.Contour;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;
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
    List<Outline> ret = PropagateSeeds.Contour.getOutline(testImage2.getProcessor());

    int i = 0;
    for (Outline o : ret) {
      if (o.countPoints() > 10) { // more than 0 points only
        RoiSaver.saveRoi(tmpdir + "testGetOutline" + i + "_QuimP.tif", o.asList());
        i++;
      }
    }
    assertThat(i, is(3)); // but 3 objects are demanded
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

    Seeds ret = cc.propagateSeed(ip.getProcessor(), ip.getProcessor(), 2, 10);
    IJ.saveAsTiff(new ImagePlus("", ret.get(SeedTypes.BACKGROUND).get(0)),
            tmpdir + "testGetCompositeSeed_Contour1_B_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", ret.get(SeedTypes.FOREGROUNDS).get(0)),
            tmpdir + "testGetCompositeSeed_Contour1_F_QuimP.tif");
  }

  /**
   * testGetCompositeSeed_Contour.
   * 
   * <p>Produce outline plot with scaled and original outline. This is used for tests or nonlinear
   * shrinking.
   * 
   * <pre>
   * <code>
   * f = '/tmp/outlineOrg.csv'
   * M = dlmread(f,'\t',1,0);
   * C = M(:,13); % smoothed curv
   * C= M(:,14); % sum smoothed
   *
   * XY = M(:,[17,18]);
   * 
   * figure
   * plot3(XY(:,1),XY(:,2),C)
   *
   * c = jet(16);
   * ran = range(C); %finding range of data
   * min_val = min(C);%finding maximum value of data
   * max_val = max(C); %finding minimum value of data
   * 
   * ci = floor((((C-min_val)/ran).*(length(c)-1))+1);
   *
   * hold on
   * for i=1:length(XY)
   *    plot3(XY(i,1),XY(i,2),C(i),...
   *      'o',...
   *      'color',c(ci(i),:),...
   *      'MarkerFaceColor', c(ci(i),:))
   * end
   * caxis([min_val max_val])
   * grid on
   * axis square
   * view(0,90)
   * colorbar
   * </code>
   * </pre>
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetCompositeSeed_Example() throws Exception {
    ImagePlus ip = IJ.openImage("src/test/Resources-static/239/shape1.tif");
    PropagateSeeds.Contour cc = new PropagateSeeds.Contour(false, null, 0.35, 5, 1, 6);

    Outline outlineOrg = Contour.getOutline(ip.getProcessor()).get(0);
    new OutlineProcessor<Outline>(outlineOrg).averageCurvature(1).sumCurvature(1);
    Seeds ret = cc.propagateSeed(ip.getProcessor(), ip.getProcessor(), 5, 10);
    Outline outlineSh = Contour.getOutline(ret.get(SeedTypes.FOREGROUNDS, 1)).get(0);
    ImageProcessor bck = ret.get(SeedTypes.BACKGROUND, 1);
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
    Seeds ret = cc.propagateSeed(testImagemask.getProcessor(), testImagemask.getProcessor(), 5, 10);
    ImageProcessor bck =
            cc.getTrueBackground(ret.get(SeedTypes.BACKGROUND, 1), testImage.getProcessor());
    IJ.saveAsTiff(new ImagePlus("", ret.get(SeedTypes.BACKGROUND, 1)),
            tmpdir + "testPropagateSeedBackground_B_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", ret.get(SeedTypes.FOREGROUNDS, 1)),
            tmpdir + "testPropagateSeedBackground_F_QuimP.tif");
    IJ.saveAsTiff(new ImagePlus("", bck), tmpdir + "testPropagateSeedBackground_MOD_QuimP.tif");
    // output is expected to not contain expanded cell and has removed other cells from background
    // area
  }

}
