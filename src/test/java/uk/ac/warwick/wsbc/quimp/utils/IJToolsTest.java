package uk.ac.warwick.wsbc.quimp.utils;

import java.io.File;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;

/**
 * @author p.baniukiewicz
 *
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
    ImagePlus org = IJ.openImage("src/test/resources/G.tif");
    ImagePlus small = IJ.openImage("src/test/resources/R.tif");
    ImagePlus big = IJ.openImage("src/test/resources/B.tif");

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
    ImagePlus org = IJ.openImage("src/test/resources/G.tif");
    ImagePlus small = IJ.openImage("src/test/resources/R.tif");
    ImagePlus big = IJ.openImage("src/test/resources/B.tif");

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
    ImagePlus org = IJ.openImage("src/test/resources/G1.tif");
    ImagePlus small = IJ.openImage("src/test/resources/R1.tif");
    ImagePlus big = IJ.openImage("src/test/resources/B1.tif");

    ImagePlus ret = IJTools.getComposite(org, small, big);
    IJ.saveAsTiff(ret, tmpdir + "compositestack_QuimP.tif");
  }

}
