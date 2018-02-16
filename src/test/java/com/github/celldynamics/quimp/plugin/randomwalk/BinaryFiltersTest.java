package com.github.celldynamics.quimp.plugin.randomwalk;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters.MorphoOperations;
import com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters.SimpleMorpho;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * Preparation images in Matlab.
 * 
 * <pre>
 * {@code i=255*uint8(imread('fg_test1.tif'));imwrite(i,'fg_test1.tif')}
 * </pre>
 * 
 * @author p.baniukiewicz
 *
 */
public class BinaryFiltersTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
  
  /** The im. */
  private ImageProcessor im;
  
  /** The im 1. */
  private ImageProcessor im1;

  /**
   * Open test images.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    im = IJ.openImage("src/test/Resources-static/RW/bg_test1.tif").getProcessor();
    im1 = IJ.openImage("src/test/Resources-static/binary_1.tif").getProcessor();
  }

  /**
   * Test of filtering of real case.
   */
  @Test
  public void testSimpleMorphoFilter() {
    SimpleMorpho filter = new BinaryFilters.SimpleMorpho();
    ImageProcessor ret = filter.filter(im);
    IJ.saveAsTiff(new ImagePlus("test", ret), tmpdir + "testSimpleMorphoFilter_QuimP.tif");

  }

  /**
   * Test method for
   * {@link BinaryFilters#iterateMorphological(ImageProcessor, MorphoOperations, double)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIterateMorphologicalIjopen() throws Exception {
    im.invert();
    ImageProcessor res = BinaryFilters.iterateMorphological(BinaryFilters.getBinaryProcessor(im),
            MorphoOperations.IJOPEN, 1);
    IJ.saveAsTiff(new ImagePlus("test", res), tmpdir + "testIterateMorphologicalIJOPEN1_QuimP.tif");

    res = BinaryFilters.iterateMorphological(BinaryFilters.getBinaryProcessor(im),
            MorphoOperations.IJOPEN, 10);
    IJ.saveAsTiff(new ImagePlus("test", res),
            tmpdir + "testIterateMorphologicalIJOPEN10_QuimP.tif");
  }

  /**
   * Test {@link BinaryFilters#iterateMorphological(ImageProcessor, MorphoOperations, double)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIterateMorphologicalErode() throws Exception {
    // BinaryProcessor rete = BinaryFilters.getBinaryProcessor(im1);
    ImageProcessor rete = BinaryFilters.iterateMorphological(im1, MorphoOperations.ERODE, 3);
    IJ.saveAsTiff(new ImagePlus("", rete), tmpdir + "testIterateMorphologicalERODE3_QuimP.tif");
  }

  /**
   * Test {@link BinaryFilters#iterateMorphological(ImageProcessor, MorphoOperations, double)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testIterateMorphologicalDilate() throws Exception {
    // BinaryProcessor rete = BinaryFilters.getBinaryProcessor(im1);
    ImageProcessor rete = BinaryFilters.iterateMorphological(im1, MorphoOperations.DILATE, 5);
    IJ.saveAsTiff(new ImagePlus("", rete), tmpdir + "testIterateMorphologicalDILATE5_QuimP.tif");
  }

}
