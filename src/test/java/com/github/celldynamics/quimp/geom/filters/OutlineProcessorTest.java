package com.github.celldynamics.quimp.geom.filters;

import static com.github.baniuk.ImageJTestSuite.matchers.arrays.ArrayMatchers.arrayCloseTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.AbstractCircularShape;
import com.github.celldynamics.quimp.Node;
import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.plugin.binaryseg.BinarySegmentation;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

import ij.IJ;
import ij.ImagePlus;

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
            "src/test/Resources-static/com.github.celldynamics.quimp.geom.filters.OutlineProcessor/"
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
   * {@link Outline#scaleOutline(double, double, double, double)}. - linear shrinking
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
    Outline outline = new QuimpDataConverter(points).getOutline();
    RoiSaver.saveRoi("/tmp/outline", outline.asList()); // nc
    outline.scaleOutline(3, 0.3, 0.1, 0.01); // modified outline differs in number
    // of points. Not related to conversion.
    outline.unfreezeAll();
    RoiSaver.saveRoi("/tmp/conv", outline.asFloatRoi()); // every time slightly different
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.geom.filters.OutlineProcessor#runningMean(int, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRunningMean() throws Exception {
    double[] x = { 0, 5, 10, 10, 10, 5, 0, 0 };
    double[] y = { 0, 0, 0, 5, 10, 10, 10, 5 };
    Outline o = new QuimpDataConverter(x, y).getOutline();

    double[] xe = { 1.6667, 5.0000, 8.3333, 10.0000, 8.3333, 5.0000, 1.6667, 0 };
    double[] ye = { 1.6667, 0, 1.6667, 5.0000, 8.3333, 10.0000, 8.3333, 5.0000 };
    new OutlineProcessor<Outline>(o).runningMean(3, 1);
    assertThat(ArrayUtils.toObject(o.xtoArr()), arrayCloseTo(xe, 1e-4));
    assertThat(ArrayUtils.toObject(o.ytoArr()), arrayCloseTo(ye, 1e-4));

    // one vertex
    Node v = new Node(5, 3, 0);
    v.setNext(v);
    v.setPrev(v);
    Snake o1 = new Snake(v, 1, 0);

    double[] xe1 = { 5 };
    double[] ye1 = { 3 };

    new OutlineProcessor<Snake>(o1).runningMean(3, 1);
    assertThat(ArrayUtils.toObject(o1.xtoArr()), arrayCloseTo(xe1, 1e-4));
    assertThat(ArrayUtils.toObject(o1.ytoArr()), arrayCloseTo(ye1, 1e-4));

  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.geom.filters.OutlineProcessor#runningMedian(int, int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRunningMedian() throws Exception {
    double[] x = { 0, 5, 10, 10, 10, 5, 0, 0 };
    double[] y = { 0, 0, 0, 5, 10, 10, 10, 5 };
    Outline o = new QuimpDataConverter(x, y).getOutline();

    double[] xe = { 0, 5.0000, 10, 10.0000, 10, 5.0000, 0, 0 };
    double[] ye = { 0, 0, 0, 5.0000, 10, 10.0000, 10, 5.0000 };
    new OutlineProcessor<Outline>(o).runningMedian(3, 1);
    assertThat(ArrayUtils.toObject(o.xtoArr()), arrayCloseTo(xe, 1e-4));
    assertThat(ArrayUtils.toObject(o.ytoArr()), arrayCloseTo(ye, 1e-4));

    // one vertex
    Node v = new Node(5, 3, 0);
    v.setNext(v);
    v.setPrev(v);
    Snake o1 = new Snake(v, 1, 0);

    double[] xe1 = { 5 };
    double[] ye1 = { 3 };

    new OutlineProcessor<Snake>(o1).runningMedian(3, 1);
    assertThat(ArrayUtils.toObject(o1.xtoArr()), arrayCloseTo(xe1, 1e-4));
    assertThat(ArrayUtils.toObject(o1.ytoArr()), arrayCloseTo(ye1, 1e-4));

  }

  /**
   * Test of {@link OutlineProcessor#sumCurvature(double)}. Values checked manually.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSumCurvature() throws Exception {
    List<Point2d> p = AbstractCircularShape.getCircle();
    Outline o = new QuimpDataConverter(p).getOutline();
    OutlineProcessor<Outline> op = new OutlineProcessor<Outline>(o);
    op.averageCurvature(Math.ceil(AbstractCircularShape.DISTANCE)); // compute in range +-1 vertex
    // curvSmoothed set to -0.055555, required by sumCurvature()
    op.sumCurvature(Math.ceil(AbstractCircularShape.DISTANCE));

    AbstractCircularShape.validateNumOfPoints(o);
    AbstractCircularShape.validateCurvatureSum(o);
  }

  /**
   * Test of {@link OutlineProcessor#sumCurvature(double)}. Values checked manually.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSumCurvature_1() throws Exception {
    List<Point2d> p = AbstractCircularShape.getCircle();
    Snake s = new QuimpDataConverter(p).getSnake(0);
    OutlineProcessor<Snake> sp = new OutlineProcessor<Snake>(s);
    sp.sumCurvature(Math.ceil(AbstractCircularShape.DISTANCE)); // convert to outline
  }

  /**
   * Test of {@link OutlineProcessor#averageCurvature(double)}. Values checked manually.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testAverageCurvature() throws Exception {
    List<Point2d> p = AbstractCircularShape.getCircle();
    Outline o = new QuimpDataConverter(p).getOutline();
    OutlineProcessor<Outline> op = new OutlineProcessor<Outline>(o);
    op.averageCurvature(Math.ceil(AbstractCircularShape.DISTANCE)); // compute in range +-1 vertex

    AbstractCircularShape.validateCurvatureSmooth(o);
    AbstractCircularShape.validateNumOfPoints(o);
  }

  /**
   * Test of {@link OutlineProcessor#averageCurvature(double)}. Values checked manually.
   * 
   * @throws Exception Exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void testAverageCurvature_1() throws Exception {
    List<Point2d> p = AbstractCircularShape.getCircle();
    Snake s = new QuimpDataConverter(p).getSnake(0);
    OutlineProcessor<Snake> sp = new OutlineProcessor<Snake>(s);
    sp.averageCurvature(Math.ceil(AbstractCircularShape.DISTANCE)); // convert to outline
  }

}
