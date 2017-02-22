package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.stat.StatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;

/**
 * This is implementation of Matlab version of Random Walk segmentation algorithm.
 * 
 * <p>See: src/test/resources/Matlab/rw_laplace4_java_base.m
 * 
 * @author p.baniukiewicz
 * @see <a href="./examples.html">Examples</a>
 */
public class RandomWalkSegmentation {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkSegmentation.class.getName());

  /**
   * Direction of circshift coded as in Matlab.
   */
  public static final int RIGHT = -10;
  /**
   * Direction of circshift coded as in Matlab.
   */
  public static final int LEFT = 10;
  /**
   * Direction of circshift coded as in Matlab.
   */
  public static final int TOP = -01;
  /**
   * Direction of circshift coded as in Matlab.
   */
  public static final int BOTTOM = 01;
  /**
   * Definition of foreground pixels.
   */
  public static final int FOREGROUND = 0;
  /**
   * Definition of background pixels.
   */
  public static final int BACKGROUND = 1;

  /**
   * Image to process in 8bit greyscale.
   */
  private RealMatrix image;
  /**
   * User provided parameters.
   */
  private Params params;

  /**
   * Construct segmentation object from ImageProcessor.
   * 
   * @param ip image to segment
   * @param params parameters
   */
  public RandomWalkSegmentation(ImageProcessor ip, Params params) {
    if (ip.getBitDepth() != 8 && ip.getBitDepth() != 16) {
      throw new IllegalArgumentException("Only 8-bit or 16-bit images are supported");
    }
    image = RandomWalkSegmentation.imageProcessor2RealMatrix(ip);
    this.params = params;
  }

  /**
   * Construct segmentation object from 2D RealMatrix representing image.
   * 
   * @param image image to segment
   * @param params parameters
   */
  public RandomWalkSegmentation(RealMatrix image, Params params) {
    this.image = image;
    this.params = params;
  }

  /**
   * Main runner, does segmentation.
   * 
   * @param seeds Seed arrays from decodeSeeds(ImagePlus, Color, Color)
   * @return Segmented image as ByteProcessor
   * @throws RandomWalkException On wrong seeds
   */
  public ImageProcessor run(Map<Integer, List<Point>> seeds) throws RandomWalkException {
    return run(seeds, null);
  }

  /**
   * Runner that allows to provide own mean intensity values for FG and BG pixels.
   * 
   * @param seeds seeds
   * @param meanseeds array of [FG] [BG] mean values of pixels. Set to null to compute from seeds.
   * @return Segmented image
   * @throws RandomWalkException on empty seed image
   * @see #getMeanSeed(Map)
   */
  public ImageProcessor run(Map<Integer, List<Point>> seeds, double[] meanseeds)
          throws RandomWalkException {
    RealMatrix[] solved;
    if (seeds.get(FOREGROUND).isEmpty() || seeds.get(BACKGROUND).isEmpty()) {
      throw new RandomWalkException(
              "Seed pixels are empty, check if:\n- correct colors were used\n- all slices have"
                      + " been seeded (if stacked seed is used)\n"
                      + "- Shrink/expand parameters are not too big.");
    }
    RealMatrix[] precomputed = precompute(); // precompute gradients
    if (meanseeds == null) {
      solved = solver(image, seeds, precomputed, getMeanSeed(seeds), params);
    } else {
      solved = solver(image, seeds, precomputed, meanseeds, params); // run solver
    }
    RealMatrix result = compare(solved[FOREGROUND], solved[BACKGROUND]); // result as matrix
    return realMatrix2ImageProcessor(result).convertToByteProcessor(true);
  }

  /**
   * Find maximum in 2D RealMatrix.
   * 
   * @param input Matrix to process
   * @return maximal value in \a input
   */
  public static double getMax(RealMatrix input) {
    double[][] data;
    if (input instanceof Array2DRowRealMatrix) {
      data = ((Array2DRowRealMatrix) input).getDataRef();
    } else {
      data = input.getData(); // TODO optimize using visitors because this is copy
    }
    double[] maxs = new double[input.getRowDimension()];
    for (int r = 0; r < input.getRowDimension(); r++) {
      maxs[r] = StatUtils.max(data[r]);
    }
    return StatUtils.max(maxs);
  }

  /**
   * Find minimum in 2D RealMatrix.
   * 
   * @param input Matrix to process
   * @return minimal value in input
   */
  public static double getMin(RealMatrix input) {
    double[][] data;
    if (input instanceof Array2DRowRealMatrix) {
      data = ((Array2DRowRealMatrix) input).getDataRef(); // only available for non cache-friendly
    } else {
      data = input.getData(); // TODO optimize using visitors because this is copy
    }
    double[] maxs = new double[input.getRowDimension()];
    for (int r = 0; r < input.getRowDimension(); r++) {
      maxs[r] = StatUtils.min(data[r]);
    }
    return StatUtils.min(maxs);
  }

  /**
   * Create RealMatrix 2D from image. Image is converted to Double
   * 
   * @param ip input image
   * @return 2D matrix converted to Double
   */
  public static RealMatrix imageProcessor2RealMatrix(ImageProcessor ip) {
    if (ip == null) {
      return null;
    }
    RealMatrix out;
    float[][] image = ip.getFloatArray();
    // no copy (it is done in float2double)
    out = new Array2DRowRealMatrix(QuimPArrayUtils.float2ddouble(image), false);
    return out;
  }

  /**
   * Create FloatProcessor 2D from RealMatrix.
   * 
   * @param rm input matrix
   * @return FloatProcessor
   */
  public static FloatProcessor realMatrix2ImageProcessor(RealMatrix rm) {
    double[][] rawData = rm.getData();
    return new FloatProcessor(QuimPArrayUtils.double2float(rawData));
  }

  /**
   * Find <b>BACKGROUND</b> and <b>FOREGROUND</b> labelled pixels on seed image and return their
   * positions.
   * 
   * @param rgb RGB seed image
   * @param fseed color of marker for foreground pixels
   * @param bseed color of marker for background pixels
   * @return Map containing list of coordinates that belong to foreground and background. Map is
   *         addressed by two enums: <i>FOREGROUND</i> and <i>BACKGROUND</i>
   * @throws RandomWalkException When image other that RGB provided
   */
  public Map<Integer, List<Point>> decodeSeeds(final ImagePlus rgb, final Color fseed,
          final Color bseed) throws RandomWalkException {
    if (rgb.getType() != ImagePlus.COLOR_RGB) {
      throw new RandomWalkException("Unsupported image type");
    }
    return decodeSeeds(rgb.getProcessor(), fseed, bseed);
  }

  /**
   * Decode image seed to lists of points.
   * 
   * @param rgb original image
   * @param fseed foreground seed image
   * @param bseed background seed image
   * @return {@link #decodeSeeds(ImagePlus, Color, Color)}
   * @throws RandomWalkException on problems with decoding, unsupported image
   * @see #decodeSeeds(ImagePlus, Color, Color)
   */
  public Map<Integer, List<Point>> decodeSeeds(final ImageProcessor rgb, final Color fseed,
          final Color bseed) throws RandomWalkException {
    // output map integrating two lists of points
    HashMap<Integer, List<Point>> out = new HashMap<Integer, List<Point>>();
    // output lists of points. Can be null if points not found
    List<Point> foreground = new ArrayList<>();
    List<Point> background = new ArrayList<>();
    // verify input condition
    if (rgb.getBitDepth() != 24) {
      throw new RandomWalkException("Unsupported seed image type");
    }
    // find marked pixels
    ColorProcessor cp = (ColorProcessor) rgb; // can cast here because of type checking
    for (int x = 0; x < cp.getWidth(); x++) {
      for (int y = 0; y < cp.getHeight(); y++) {
        Color c = cp.getColor(x, y); // get color for pixel
        if (c.equals(fseed)) {
          foreground.add(new Point(y, x)); // remember foreground coords
        } else if (c.equals(bseed)) {
          background.add(new Point(y, x)); // remember background coords
        }
      }
    }
    // pack outputs into map
    out.put(FOREGROUND, foreground);
    out.put(BACKGROUND, background);
    return out;
  }

  /**
   * Compare probabilities from two matrices and create third depending on winner.
   * 
   * @param fg Foreground probabilities for all points
   * @param bg Background probabilities for all points
   * @return OUT=FG>BG, 1 for every pixel that wins for FG, o otherwise
   */
  public RealMatrix compare(RealMatrix fg, RealMatrix bg) {
    RealMatrix ret = MatrixUtils.createRealMatrix(fg.getRowDimension(), fg.getColumnDimension());
    for (int r = 0; r < fg.getRowDimension(); r++) {
      for (int c = 0; c < fg.getColumnDimension(); c++) {
        if (fg.getEntry(r, c) > bg.getEntry(r, c)) {
          ret.setEntry(r, c, 1);
        } else {
          ret.setEntry(r, c, 0);
        }
      }
    }
    return ret;

  }

  /**
   * Compute image circularly shifted by one pixel towards selected direction.
   *
   * @param input Image to be shifted
   * @param direction Shift direction. This method is adjusted to work as MAtlab code and to keep
   *        Matlab naming (rw_laplace4.m) thus the shift direction names are not adequate to shift
   *        direction.
   * @return Copy of \a input shifted by one pixel in \a direction.
   */
  protected RealMatrix circshift(RealMatrix input, int direction) {
    double[][] sub; // part of matrix that does no change put is shifted
    int rows = input.getRowDimension(); // cache sizes
    int cols = input.getColumnDimension();
    Array2DRowRealMatrix out = new Array2DRowRealMatrix(rows, cols); // output matrix, shifted
    switch (direction) {
      case LEFT: // b
        // rotated right - last column become first
        // cut submatrix from first column to before last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 0, cols - 2, sub); // cols-2 because last is not
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 1);
        // copy last column to first
        out.setColumnVector(0, input.getColumnVector(cols - 1));
        break;
      case RIGHT: // top
        // rotated left - first column become last
        // cut submatrix from second column to last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 1, cols - 1, sub);
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 0);
        // copy first column to last
        out.setColumnVector(cols - 1, input.getColumnVector(0));
        break;
      case TOP: // right
        // rotated top - first row become last
        // cut submatrix from second row to last
        sub = new double[rows - 1][cols];
        input.copySubMatrix(1, rows - 1, 0, cols - 1, sub);
        // create new matrix - paste submatrix but shifted up
        out.setSubMatrix(sub, 0, 0);
        // copy first row to last
        out.setRowVector(rows - 1, input.getRowVector(0));
        break;
      case BOTTOM: // left
        // rotated bottom - last row become first
        // cut submatrix from first row to before last
        sub = new double[rows - 1][cols];
        input.copySubMatrix(0, rows - 2, 0, cols - 1, sub);
        // create new matrix - paste submatrix but shifted up
        out.setSubMatrix(sub, 1, 0);
        // copy last row to first
        out.setRowVector(0, input.getRowVector(rows - 1));
        break;
      default:
        throw new IllegalArgumentException("circshift: Unknown direction");
    }
    return out;
  }

  /**
   * 
   * @param a left operand
   * @param b right operand
   * @return Image (a-b).^2
   */
  protected RealMatrix getSqrdDiffIntensity(RealMatrix a, RealMatrix b) {
    RealMatrix s = a.subtract(b);
    s.walkInOptimizedOrder(new MatrixElementPower());
    return s;
  }

  /**
   * Pre-compute gradient matrices.
   * 
   * @return Array of precomputed data in the following order: -# [0] - gRight2 -# [1] - gTop2 -#
   *         [2] - gLeft2 -# [3] - gBottom2
   */
  private RealMatrix[] precompute() {
    // setup shifted images
    RealMatrix right = circshift(image, RIGHT);
    RealMatrix top = circshift(image, TOP);
    // compute squared intensity differences
    RealMatrix gradRight2 = getSqrdDiffIntensity(image, right);
    RealMatrix gradTop2 = getSqrdDiffIntensity(image, top);
    // compute maximum of horizontal and vertical intensity gradients
    double maxGright2 = RandomWalkSegmentation.getMax(gradRight2);
    LOGGER.debug("maxGright2 " + maxGright2);
    double maxGtop2 = RandomWalkSegmentation.getMax(gradTop2);
    LOGGER.debug("maxGtop2 " + maxGtop2);
    double maxGrad2 = maxGright2 > maxGtop2 ? maxGright2 : maxGtop2;
    LOGGER.debug("maxGrad2max " + maxGrad2);
    // Normalize squared gradients to maxGrad
    gradRight2.walkInOptimizedOrder(new MatrixElementMultiply(1 / maxGrad2));
    gradTop2.walkInOptimizedOrder(new MatrixElementMultiply(1 / maxGrad2));
    // assign outputs
    RealMatrix[] out = new RealMatrix[4];
    RealMatrix gradLeft2 = circshift(gradRight2, LEFT);
    out[2] = gradLeft2;
    RealMatrix gradBottom2 = circshift(gradTop2, BOTTOM);
    out[3] = gradBottom2;
    out[0] = gradRight2;
    out[1] = gradTop2;
    return out;
  }

  /**
   * Calculate mean intensity for FG and BG seeds for stored image.
   * 
   * @param seeds FG and BG seeds
   * @return [0] - foreground mean value, [1] - background mean value for seeded pixels.
   */
  public double[] getMeanSeed(Map<Integer, List<Point>> seeds) {
    double[] meanseed = new double[2];
    // fg
    meanseed[0] = StatUtils.mean(getValues(image, seeds.get(FOREGROUND)).getDataRef());
    // bg
    meanseed[1] = StatUtils.mean(getValues(image, seeds.get(BACKGROUND)).getDataRef());
    return meanseed;
  }

  /**
   * Do main computations
   * 
   * @param image original image
   * @param seeds seed array returned from decodeSeeds(ImagePlus, Color, Color)
   * @param gradients precomputed gradients returned from precompute()
   * @param meanseed mean values of image intensities for FG and BG. Calculated by
   *        {@link #getMeanSeed(Map)}. Can be provided separately.
   * @param params Parameters
   * @return Computed probabilities for background and foreground, RealMatrix[2],
   *         RealMatrix[FOREGROUND] and RealMatrix[BACKGROUND]
   */
  protected RealMatrix[] solver(RealMatrix image, Map<Integer, List<Point>> seeds,
          RealMatrix[] gradients, double[] meanseed, Params params) {
    LOGGER.debug("meanseed_fg=" + meanseed[0] + " meanseed_bg=" + meanseed[1]); // correct
    LOGGER.trace("fseeds: " + seeds.get(FOREGROUND)); // correct
    LOGGER.trace("getValfseed: " + getValues(image, seeds.get(FOREGROUND))); // correct
    // compute normalised squared differences to mean seed intensities
    RealMatrix diffIfg = image.scalarAdd(-meanseed[0]);
    diffIfg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
    RealMatrix diffIbg = image.scalarAdd(-meanseed[1]);
    diffIbg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
    // compute weights for diffusion in all four directions, dependent on local gradients and
    // differences to mean intensities of seeds
    Array2DRowRealMatrix wrfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[0]);
    Array2DRowRealMatrix wlfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[2]);
    Array2DRowRealMatrix wtfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[1]);
    Array2DRowRealMatrix wbfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[3]);

    Array2DRowRealMatrix wrbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[0]);
    Array2DRowRealMatrix wlbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[2]);
    Array2DRowRealMatrix wtbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[1]);
    Array2DRowRealMatrix wbbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[3]);

    // compute averaged weights, left/right and top/bottom
    // used when computing second spatial derivate from first one
    RealMatrix avgwxfg = wlfg.add(wrfg);
    avgwxfg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // correct
    RealMatrix avgwyfg = wtfg.add(wbfg);
    avgwyfg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
    RealMatrix avgwxbg = wlbg.add(wrbg);
    avgwxbg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
    RealMatrix avgwybg = wtbg.add(wbbg);
    avgwybg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
    // d is the diffusion constant times the timestep of the Euler scheme obey CFL stability
    // criterion. D*dt/(dx^2) should be <<1/4
    // WARN See differences between matlab and java in definition up,down,etx. In matlab it seems to
    // be wrong according to names
    double tmp1 = getStabilityCriterion(wrfg, avgwxfg);
    double tmp2 = getStabilityCriterion(wlfg, avgwxfg);
    double drl2 = tmp1 < tmp2 ? tmp1 : tmp2;
    tmp1 = getStabilityCriterion(wtfg, avgwyfg);
    tmp2 = getStabilityCriterion(wbfg, avgwyfg);
    double dtb2 = tmp1 < tmp2 ? tmp1 : tmp2;
    double diffusion = drl2 < dtb2 ? drl2 : dtb2; // D=0.25*min(drl2,dtb2)
    diffusion *= 0.25; // correct
    LOGGER.debug("drl2=" + drl2 + " dtb2=" + dtb2); // ok
    LOGGER.debug("D=" + diffusion); // ok

    Array2DRowRealMatrix fg =
            new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());
    double[][] fglast2d = new double[image.getRowDimension()][image.getColumnDimension()];
    Array2DRowRealMatrix bg =
            new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());

    // precompute terms for loop
    wrfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
    wlfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
    wtfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));
    wbfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));

    wrbg.walkInOptimizedOrder(new MatrixDotProduct(avgwxbg));
    wlbg.walkInOptimizedOrder(new MatrixDotProduct(avgwxbg));
    wtbg.walkInOptimizedOrder(new MatrixDotProduct(avgwybg));
    wbbg.walkInOptimizedOrder(new MatrixDotProduct(avgwybg));

    double[][] wrfg2d = wrfg.getDataRef();
    double[][] wlfg2d = wlfg.getDataRef();
    double[][] wtfg2d = wtfg.getDataRef();
    double[][] wbfg2d = wbfg.getDataRef();

    double[][] wrbg2d = wrbg.getDataRef();
    double[][] wlbg2d = wlbg.getDataRef();
    double[][] wtbg2d = wtbg.getDataRef();
    double[][] wbbg2d = wbbg.getDataRef();

    // main loop
    for (int i = 0; i < params.Iter; i++) {
      LOGGER.trace("Iter: " + i);
      // TODO create separate version for values
      setValues(fg, seeds.get(FOREGROUND), new ArrayRealVector(new double[] { 1 }));
      setValues(fg, seeds.get(BACKGROUND), new ArrayRealVector(new double[] { 0 }));
      setValues(bg, seeds.get(FOREGROUND), new ArrayRealVector(new double[] { 0 }));
      setValues(bg, seeds.get(BACKGROUND), new ArrayRealVector(new double[] { 1 }));

      // groups for long term for FG
      Array2DRowRealMatrix fgcircright = (Array2DRowRealMatrix) circshift(fg, RIGHT);
      Array2DRowRealMatrix fgcircleft = (Array2DRowRealMatrix) circshift(fg, LEFT);
      Array2DRowRealMatrix fgcirctop = (Array2DRowRealMatrix) circshift(fg, TOP);
      Array2DRowRealMatrix fgcircbottom = (Array2DRowRealMatrix) circshift(fg, BOTTOM);

      double[][] fgcircright2d = fgcircright.getDataRef();
      double[][] fg2d = fg.getDataRef();
      double[][] bg2d = bg.getDataRef();
      double[][] fgcircleft2d = fgcircleft.getDataRef();
      double[][] fgcirctop2d = fgcirctop.getDataRef();
      double[][] fgcircbottom2d = fgcircbottom.getDataRef();

      for (int r = 0; r < fg.getRowDimension(); r++) {
        for (int c = 0; c < fg.getColumnDimension(); c++) {
          fg2d[r][c] += params.dt * (diffusion
                  * (((fgcircright2d[r][c] - fg2d[r][c]) / wrfg2d[r][c]
                          - (fg2d[r][c] - fgcircleft2d[r][c]) / wlfg2d[r][c])
                          + ((fgcirctop2d[r][c] - fg2d[r][c]) / wtfg2d[r][c]
                                  - (fg2d[r][c] - fgcircbottom2d[r][c]) / wbfg2d[r][c]))
                  - params.gamma[0] * fg2d[r][c] * bg2d[r][c]);
        }
      }

      // groups for long term for BG
      Array2DRowRealMatrix bgcircright = (Array2DRowRealMatrix) circshift(bg, RIGHT);
      Array2DRowRealMatrix bgcircleft = (Array2DRowRealMatrix) circshift(bg, LEFT);
      Array2DRowRealMatrix bgcirctop = (Array2DRowRealMatrix) circshift(bg, TOP);
      Array2DRowRealMatrix bgcircbottom = (Array2DRowRealMatrix) circshift(bg, BOTTOM);

      double[][] bgcircright2d = bgcircright.getDataRef();
      double[][] bgcircleft2d = bgcircleft.getDataRef();
      double[][] bgcirctop2d = bgcirctop.getDataRef();
      double[][] bgcircbottom2d = bgcircbottom.getDataRef();

      for (int r = 0; r < bg.getRowDimension(); r++) {
        for (int c = 0; c < bg.getColumnDimension(); c++) {
          bg2d[r][c] += params.dt * (diffusion
                  * (((bgcircright2d[r][c] - bg2d[r][c]) / wrbg2d[r][c]
                          - (bg2d[r][c] - bgcircleft2d[r][c]) / wlbg2d[r][c])
                          + ((bgcirctop2d[r][c] - bg2d[r][c]) / wtbg2d[r][c]
                                  - (bg2d[r][c] - bgcircbottom2d[r][c]) / wbbg2d[r][c]))
                  - params.gamma[0] * fglast2d[r][c] * bg2d[r][c]);
        }
      }

      QuimPArrayUtils.copy2darray(fg2d, fglast2d);
      fg = new Array2DRowRealMatrix(fg2d, false); // not copy of FG2d, just replace old FG
      bg = new Array2DRowRealMatrix(bg2d, false);
    }
    RealMatrix[] ret = new RealMatrix[2];
    ret[FOREGROUND] = fg;
    ret[BACKGROUND] = bg;

    return ret;
  }

  /**
   * 
   * @param diffI normalized squared differences to mean seed intensities
   * @param grad2 normalized the squared gradients by the maximum gradient
   * @return wr_fg = exp(P.alpha*diffI_fg+P.beta*G.gradright2);
   */
  private RealMatrix computeweights(RealMatrix diffI, RealMatrix grad2) {
    // TODO optimize this part, see matlab code, products are the same
    double alpha = params.alpha;
    double beta = params.beta;
    double[][] diffI2d;
    double[][] grad22d;
    if (diffI instanceof Array2DRowRealMatrix) {
      diffI2d = ((Array2DRowRealMatrix) diffI).getDataRef();
    } else {
      diffI2d = diffI.getData();
    }
    if (grad2 instanceof Array2DRowRealMatrix) {
      grad22d = ((Array2DRowRealMatrix) grad2).getDataRef();
    } else {
      grad22d = grad2.getData();
    }
    Array2DRowRealMatrix w =
            new Array2DRowRealMatrix(diffI.getRowDimension(), diffI.getColumnDimension());
    double[][] w2d = w.getDataRef(); // reference of w
    for (int r = 0; r < diffI.getRowDimension(); r++) {
      for (int c = 0; c < diffI.getColumnDimension(); c++) {
        w2d[r][c] = Math.exp(diffI2d[r][c] * alpha + grad22d[r][c] * beta);
      }
    }
    return w;
  }

  /**
   * Compute part of stability criterion for stopping iterations.
   * 
   * <p>Only in-place multiplication and minimu of result is done here
   * 
   * <pre>
   * <code>
   * drl2 = min ( min(min (wr_fg.*avgwx_fg)) , min(min (wl_fg.*avgwx_fg)) ); dtb2 = min (
   *       min(min (wt_fg.*avgwy_fg)) , min(min (wb_fg.*avgwy_fg)) ); D=0.25*min(drl2,dtb2);
   * </code>
   * </pre>
   * 
   * @param wfg wfg
   * @param avgwfg avgwfg
   * @return min(min (wfg.*avgwfg))
   */
  private double getStabilityCriterion(RealMatrix wfg, RealMatrix avgwfg) {
    RealMatrix cp = wfg.copy();
    cp.walkInOptimizedOrder(new MatrixDotProduct(avgwfg));
    return RandomWalkSegmentation.getMin(cp);
  }

  /**
   * Return values from in matrix that are on indexes ind.
   * 
   * @param in Input matrix 2D
   * @param ind List of indexes
   * @return Values that are on indexes \a ind
   */
  protected ArrayRealVector getValues(RealMatrix in, List<Point> ind) {
    ArrayRealVector out = new ArrayRealVector(ind.size());
    int l = 0;
    for (Point p : ind) {
      out.setEntry(l++, in.getEntry(p.row, p.col));
    }
    return out;
  }

  /**
   * Set values from val on indexes ind in array in
   * 
   * @param in Input matrix. Will be modified
   * @param ind List of indexes
   * @param val List of values, length must be the same as \a ind or 1
   */
  protected void setValues(RealMatrix in, List<Point> ind, ArrayRealVector val) {
    if (ind.size() != val.getDimension() && val.getDimension() != 1) {
      throw new InvalidParameterException(
              "Vector with data must contain 1 element or the same as indexes");
    }
    int delta;
    int l = 0;
    if (val.getDimension() == 1) {
      delta = 0;
    } else {
      delta = 1;
    }
    for (Point p : ind) {
      in.setEntry(p.row, p.col, val.getDataRef()[l]);
      l += delta;
    }
  }

  /**
   * Sub and then div in-place this matrix and another.
   * 
   * @author p.baniukiewicz
   *
   */
  class MatrixDotSubDiv implements RealMatrixChangingVisitor {

    RealMatrix sub;
    RealMatrix div;

    public MatrixDotSubDiv(RealMatrix sub, RealMatrix div) {
      this.sub = sub;
      this.div = div;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

      return (arg2 - sub.getEntry(arg0, arg1)) / div.getEntry(arg0, arg1);
    }

  }

  /**
   * Perform element-wise multiplication by value (.*val in Matlab). Done in-place.
   * 
   * @author p.baniukiewicz
   */
  class MatrixElementMultiply implements RealMatrixChangingVisitor {

    private double multiplier;

    /**
     * Assign multiplier.
     * 
     * @param d multiplier
     */
    MatrixElementMultiply(double d) {
      this.multiplier = d;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
    }

    /**
     * Multiply entry by value.
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return arg2 * multiplier;
    }

  }

  /**
   * Perform element-wise exp. Done in-place.
   * 
   * @author p.baniukiewicz
   */
  class MatrixElementExp implements RealMatrixChangingVisitor {

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return Math.exp(arg2);
    }

  }

  /**
   * Perform element-wise power (.^2 in Matlab). Done in-place.
   * 
   * @author p.baniukiewicz
   */
  class MatrixElementPower implements RealMatrixChangingVisitor {

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
    }

    /**
     * Multiply entry by itself.
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return arg2 * arg2;
    }
  }

  /**
   * Multiply in-place this matrix by another.
   * 
   * @author p.baniukiewicz
   *
   */
  class MatrixDotProduct implements RealMatrixChangingVisitor {

    RealMatrix matrix;

    public MatrixDotProduct(RealMatrix m) {
      this.matrix = m;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
      if (matrix.getColumnDimension() != arg1 || matrix.getRowDimension() != arg0) {
        throw new MatrixDimensionMismatchException(matrix.getRowDimension(),
                matrix.getColumnDimension(), arg0, arg1);
      }

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

      return arg2 * matrix.getEntry(arg0, arg1);
    }

  }

  /**
   * Divide in-place this matrix by another.
   * 
   * @author p.baniukiewicz
   *
   */
  class MatrixDotDiv implements RealMatrixChangingVisitor {

    RealMatrix matrix;

    public MatrixDotDiv(RealMatrix m) {
      this.matrix = m;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
      if (matrix.getColumnDimension() != arg1 || matrix.getRowDimension() != arg0) {
        throw new MatrixDimensionMismatchException(matrix.getRowDimension(),
                matrix.getColumnDimension(), arg0, arg1);
      }

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return arg2 / matrix.getEntry(arg0, arg1);
    }

  }

  /**
   * Add in-place this matrix to another.
   * 
   * @author p.baniukiewicz
   *
   */
  class MatrixDotAdd implements RealMatrixChangingVisitor {

    RealMatrix matrix;

    public MatrixDotAdd(RealMatrix m) {
      this.matrix = m;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
      if (matrix.getColumnDimension() != arg1 || matrix.getRowDimension() != arg0) {
        throw new MatrixDimensionMismatchException(matrix.getRowDimension(),
                matrix.getColumnDimension(), arg0, arg1);
      }

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return arg2 + matrix.getEntry(arg0, arg1);
    }

  }

  /**
   * Sub in-place this matrix to another.
   * 
   * @author p.baniukiewicz
   *
   */
  class MatrixDotSub implements RealMatrixChangingVisitor {

    RealMatrix matrix;

    public MatrixDotSub(RealMatrix m) {
      this.matrix = m;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
      if (matrix.getColumnDimension() != arg1 || matrix.getRowDimension() != arg0) {
        throw new MatrixDimensionMismatchException(matrix.getRowDimension(),
                matrix.getColumnDimension(), arg0, arg1);
      }

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return arg2 - matrix.getEntry(arg0, arg1);
    }

  }

  /**
   * Perform element-wise power (.^2 in Matlab) and then divide by val. Done in-place.
   * 
   * @author p.baniukiewicz
   */
  class MatrixElementPowerDiv implements RealMatrixChangingVisitor {

    double val;

    public MatrixElementPowerDiv(double val) {
      this.val = val;
    }

    @Override
    public double end() {
      return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
    }

    /**
     * Multiply entry by itself.
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
      return (arg2 * arg2) / val;
    }
  }
}

/**
 * @example src/test/resources/Matlab/rw_laplace4_java_base.m This is source file of segmentation in
 *          Matlab that was a base for RandomWalkSegmentation Implementation
 */
