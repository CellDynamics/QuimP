package com.github.celldynamics.quimp.geom.filters;

import static com.github.celldynamics.quimp.utils.test.matchers.arrays.ArrayMatchers.arrayCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.vecmath.Point2d;

import com.github.celldynamics.quimp.Node;
import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.Snake;
import com.github.celldynamics.quimp.geom.SegmentedShapeRoi;
import com.github.celldynamics.quimp.geom.filters.OutlineProcessor;
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
   * {@link Outline#scale(double, double, double, double)}. - linear shrinking
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
    outline.scale(3, 0.3, 0.1, 0.01); // modified outline differs in number
    // of points. Not related to conversion.
    outline.unfreezeAll();
    RoiSaver.saveRoi("/tmp/conv", outline.asFloatRoi()); // every time slightly different
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.geom.filters.OutlineProcessor#runningMean(double[], int)}.
   * 
   * <pre>
   * <code>
   * x=1:10
   * xx=padarray(x',1,'circular')'
   * ret=movmean(xx,3)
   * ret(2:end-1)
   * </code>
   * </pre>
   * 
   * @throws Exception Exception
   */
  @Test
  public void testRunningMean() throws Exception {
    double[] data = new double[10];
    IntStream.range(0, 10).forEach(i -> data[i] = i + 1);
    OutlineProcessor.runningMean(data, 3);
    double[] expected =
            { 4.3333, 2.0000, 3.0000, 4.0000, 5.0000, 6.0000, 7.0000, 8.0000, 9.0000, 6.6667 };
    assertThat(ArrayUtils.toObject(data), arrayCloseTo(expected, 1e-4));

    // table size == window
    double[] data1 = new double[3];
    IntStream.range(0, 3).forEach(i -> data1[i] = i + 1);
    OutlineProcessor.runningMean(data1, 3);
    double[] expected1 = { 2, 2, 2 };
    assertThat(ArrayUtils.toObject(data1), arrayCloseTo(expected1, 1e-4));

    // table size < window
    double[] data2 = new double[1];
    IntStream.range(0, 1).forEach(i -> data2[i] = i + 1);
    OutlineProcessor.runningMean(data2, 3);
    double[] expected2 = { 1 };
    assertThat(ArrayUtils.toObject(data2), arrayCloseTo(expected2, 1e-4));

    // empty input
    double[] data3 = new double[0];
    OutlineProcessor.runningMean(data3, 3);
    assertThat(data3.length, is(0));
  }

  /**
   * Test method for {@link com.github.celldynamics.quimp.geom.filters.OutlineProcessor#smooth(int)}.
   * 
   * @throws Exception Exception
   */
  @Test
  public void testSmooth() throws Exception {
    double[] x = { 0, 5, 10, 10, 10, 5, 0, 0 };
    double[] y = { 0, 0, 0, 5, 10, 10, 10, 5 };
    Outline o = new QuimpDataConverter(x, y).getOutline();

    double[] xe = { 1.6667, 5.0000, 8.3333, 10.0000, 8.3333, 5.0000, 1.6667, 0 };
    double[] ye = { 1.6667, 0, 1.6667, 5.0000, 8.3333, 10.0000, 8.3333, 5.0000 };

    new OutlineProcessor<Outline>(o).smooth(3);
    assertThat(ArrayUtils.toObject(o.xtoArr()), arrayCloseTo(xe, 1e-4));
    assertThat(ArrayUtils.toObject(o.ytoArr()), arrayCloseTo(ye, 1e-4));

    // one vertex
    Node v = new Node(5, 3, 0);
    v.setNext(v);
    v.setPrev(v);
    Snake o1 = new Snake(v, 1, 0);

    double[] xe1 = { 5 };
    double[] ye1 = { 3 };

    new OutlineProcessor<Snake>(o1).smooth(3);
    assertThat(ArrayUtils.toObject(o1.xtoArr()), arrayCloseTo(xe1, 1e-4));
    assertThat(ArrayUtils.toObject(o1.ytoArr()), arrayCloseTo(ye1, 1e-4));

  }

}
