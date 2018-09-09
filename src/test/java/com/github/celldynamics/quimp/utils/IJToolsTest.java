package com.github.celldynamics.quimp.utils;

import java.io.File;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;

/**
 * The Class IJToolsTest.
 *
 * @author p.baniukiewicz
 */
public class IJToolsTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * testGetComposite.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetComposite() throws Exception {
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus small = IJ.openImage("src/test/Resources-static/R.tif");
    ImagePlus big = IJ.openImage("src/test/Resources-static/B.tif");

    ImagePlus ret = IJTools.getComposite(org, small, big);
    IJ.saveAsTiff(ret, tmpdir + "composite_QuimP.tif");
    // ret.show();
  }

  /**
   * trstgetRGB.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetRGB() throws Exception {
    ImagePlus org = IJ.openImage("src/test/Resources-static/G.tif");
    ImagePlus small = IJ.openImage("src/test/Resources-static/R.tif");
    ImagePlus big = IJ.openImage("src/test/Resources-static/B.tif");

    ColorProcessor ret = IJTools.getRGB(org, small, big);
    IJ.saveAsTiff(new ImagePlus("", ret), tmpdir + "testGetRGB_QuimP.tif");
    // ret.show();
  }

  /**
   * testGetComposite_stack.
   * 
   * @throws Exception on error
   */
  @Test
  public void testGetComposite_stack() throws Exception {
    ImagePlus org = IJ.openImage("src/test/Resources-static/G1.tif");
    ImagePlus small = IJ.openImage("src/test/Resources-static/R1.tif");
    ImagePlus big = IJ.openImage("src/test/Resources-static/B1.tif");

    ImagePlus ret = IJTools.getComposite(org, small, big);
    IJ.saveAsTiff(ret, tmpdir + "compositestack_QuimP.tif");
  }

}
