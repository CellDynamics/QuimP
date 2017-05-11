package uk.ac.warwick.wsbc.quimp.geom.filters;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.vecmath.Point2d;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi;
import uk.ac.warwick.wsbc.quimp.plugin.binaryseg.BinarySegmentation;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;
import uk.ac.warwick.wsbc.quimp.utils.test.RoiSaver;

/**
 * @author p.baniukiewicz
 *
 */
public class OutlineProcessorTest {

  private ImagePlus mask;
  private ArrayList<ArrayList<SegmentedShapeRoi>> ret;

  /**
   * @throws java.lang.Exception Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // ij = new ImageJ();
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {

  }

  /**
   * @throws java.lang.Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    mask = IJ.openImage(
            "src/test/Resources-static/uk.ac.warwick.wsbc.quimp.geom.filters.OutlineProcessorTest/"
                    + "C1-talA_GFP_rnd_motility_FLU_frame59.tif");
    BinarySegmentation obj = new BinarySegmentation(mask);
    obj.trackObjects(); // run tracking
    ret = obj.getChains();
    for (ArrayList<SegmentedShapeRoi> asS : ret) {
      for (SegmentedShapeRoi ss : asS) {
        ss.setInterpolationParameters(1, false);
      }
    }
  }

  /**
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    ret = null;
    mask = null;
  }

  /**
   * Test method for
   * {@link OutlineProcessor#shrink(double, double, double, double)}.
   * 
   * <p>Saves processed outline for comparison
   * 
   * @throws Exception Exception
   */
  @Test
  public void testShrink() throws Exception {
    // nc - not changing on run
    SegmentedShapeRoi ssR = ret.get(0).get(0);// nc
    RoiSaver.saveRoi("/tmp/fgf", ssR);
    List<Point2d> points = ssR.getOutlineasRawPoints();
    RoiSaver.saveRoi("/tmp/fgfs", points); // nc
    Outline outline = new QuimpDataConverter(points).getOutline(0);
    RoiSaver.saveRoi("/tmp/outline", outline.asList()); // nc
    new OutlineProcessor(outline).shrink(10, 0.3, 0.1, 0.01); // modified outline differs in number
    // of points. Not related to conversion.
    outline.unfreezeAll();
    RoiSaver.saveRoi("/tmp/conv", outline.asFloatRoi()); // every time slightly different
  }

}
