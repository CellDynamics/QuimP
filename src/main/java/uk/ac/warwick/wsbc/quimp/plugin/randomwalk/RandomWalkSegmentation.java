package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
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

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.QuimP;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;

/**
 * This is implementation of Matlab version of Random Walk segmentation algorithm.
 * 
 * <p>See: src/test/Resources-static/Matlab/rw_laplace4.m
 * 
 * @author p.baniukiewicz
 */
public class RandomWalkSegmentation {

  /**
   * How often we will compute relative error.
   */
  final int relErrStep = 20;

  /**
   * Reasons of stopping diffusion process.
   * 
   * @author p.baniukiewicz
   *
   */
  private enum StoppedBy {
    /**
     * Maximum number of iterations reached.
     */
    ITERATIONS,
    /**
     * Found NaN in solution.
     */
    NANS,
    /**
     * Found Inf in solution.
     */
    INFS,
    /**
     * Relative error smaller than limit.
     */
    RELERR
  }

  /**
   * Define foreground and background indexes.
   * 
   * @author p.baniukiewicz
   *
   */
  public enum Seeds {
    /**
     * Denote foreground related data. Usually on index 0.
     */
    FOREGROUND(0),
    /**
     * Denote background related data. Usually on index 1.
     */
    BACKGROUND(1),
    /**
     * Rough mask used for computing local mean. Used only if {@link Params#useLocalMean} is true.
     * 
     * @see RandomWalkSegmentation#solver(Map, RealMatrix[])
     * @see RandomWalkSegmentation#getMeanSeedLocal(ImageProcessor, int)
     */
    ROUGHMASK(2);

    private final int index;

    private Seeds(int index) {
      this.index = index;
    }

    /**
     * Convert enum to int. Used when addressing arrays.
     * 
     * @return index assigned to enum value.
     */
    public int getIndex() {
      return index;
    }
  }

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
   * Image to process in 8bit greyscale. Converted to RealMatrix
   * 
   * @see #ip
   */
  private RealMatrix image;
  /**
   * Original image to process.
   * 
   * @see #image
   */
  private ImageProcessor ip;
  /**
   * User provided parameters.
   */
  private Params params;

  /**
   * Construct segmentation object from ImageProcessor.
   * 
   * @param ip image to segment
   * @param params parameters
   * @throws RandomWalkException on wrong image format
   */
  public RandomWalkSegmentation(ImageProcessor ip, Params params) throws RandomWalkException {
    if (ip.getBitDepth() != 8 && ip.getBitDepth() != 16) {
      throw new RandomWalkException("Only 8-bit or 16-bit images are supported");
    }
    this.ip = ip;
    this.image = RandomWalkSegmentation.imageProcessor2RealMatrix(ip);
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
    this.ip = realMatrix2ImageProcessor(image);
    this.params = params;
  }

  /**
   * Main runner, does segmentation.
   * 
   * @param seeds Seed arrays from decodeSeeds(ImagePlus, Color, Color)
   * @return Segmented image as ByteProcessor
   * @throws RandomWalkException On wrong seeds
   */
  public ImageProcessor run(Map<Seeds, ImageProcessor> seeds) throws RandomWalkException {
    Map<Seeds, RealMatrix> solved;
    RealMatrix[] precomputed = precomputeGradients(); // precompute gradients
    solved = solver(seeds, precomputed);
    if (params.intermediateFilter != null && params.gamma[1] != 0) { // do second sweep
      LOGGER.debug("Running next sweep: " + params.intermediateFilter.getClass().getName());
      Map<Seeds, ImageProcessor> seedsNext = rollNextSweep(solved);
      if (seeds.get(Seeds.ROUGHMASK) != null) {
        seedsNext.put(Seeds.ROUGHMASK, seeds.get(Seeds.ROUGHMASK));
      }
      solved = solver(seedsNext, precomputed);
      params.swapGamma();
    }
    RealMatrix result = compare(solved); // result as matrix
    // do final filtering
    if (params.finalFilter != null) {
      return params.finalFilter
              .filter(realMatrix2ImageProcessor(result).convertToByteProcessor(true));
    } else {
      return realMatrix2ImageProcessor(result).convertToByteProcessor(true);
    }
  }

  /**
   * Prepare seeds from results of previous solver. It swaps gammas.
   * 
   * @param solved results from {@link #solver(Map, RealMatrix[])}
   * @return new seed taken from previous solution.
   * @throws RandomWalkException o unsupported image or empty seed list after decoding
   */
  private Map<Seeds, ImageProcessor> rollNextSweep(Map<Seeds, RealMatrix> solved)
          throws RandomWalkException {
    final double weight = 1e20;
    // make copy of input results to weight them
    RealMatrix[] solvedWeighted = new RealMatrix[2];
    solvedWeighted[Seeds.FOREGROUND.getIndex()] = solved.get(Seeds.FOREGROUND).copy();
    solvedWeighted[Seeds.BACKGROUND.getIndex()] = solved.get(Seeds.BACKGROUND).copy();

    // seed_fg = FGl>1e20*BGl
    solvedWeighted[Seeds.FOREGROUND.getIndex()]
            .walkInOptimizedOrder(new MatrixCompareWeighted(solved.get(Seeds.BACKGROUND), weight));
    // seed_bg = BGl>1e20*FGl;
    solvedWeighted[Seeds.BACKGROUND.getIndex()]
            .walkInOptimizedOrder(new MatrixCompareWeighted(solved.get(Seeds.FOREGROUND), weight));
    // convert weighted results to images
    ImageProcessor fg1 = realMatrix2ImageProcessor(solvedWeighted[Seeds.FOREGROUND.getIndex()])
            .convertToByte(true);
    ImageProcessor bg1 = realMatrix2ImageProcessor(solvedWeighted[Seeds.BACKGROUND.getIndex()])
            .convertToByte(true);

    if (QuimP.SUPER_DEBUG) { // save intermediate results
      LOGGER.debug("Saving intermediate results");
      String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
      IJ.saveAsTiff(new ImagePlus("", fg1), tmpdir + "fg1_QuimP.tif");
      IJ.saveAsTiff(new ImagePlus("", bg1), tmpdir + "bg1_QuimP.tif");
    }
    // filter them (if weare here intermediate filter cant be null)
    fg1 = params.intermediateFilter.filter(fg1);
    bg1.invert();
    bg1 = params.intermediateFilter.filter(bg1);
    bg1.invert();
    // prepare second sweep seeds from previous
    params.swapGamma(); // to have second sweep gamma in [0]

    Map<Seeds, ImageProcessor> ret = new HashMap<Seeds, ImageProcessor>(2);
    ret.put(Seeds.FOREGROUND, fg1);
    ret.put(Seeds.BACKGROUND, bg1);
    return ret;
  }

  /**
   * Create RealMatrix 2D from image. Image is converted to Double.
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
    return out.transpose();
  }

  /**
   * Create FloatProcessor 2D from RealMatrix.
   * 
   * @param rm input matrix
   * @return FloatProcessor
   */
  public static FloatProcessor realMatrix2ImageProcessor(RealMatrix rm) {
    double[][] rawData = rm.transpose().getData();
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
  public static Map<Seeds, ImageProcessor> decodeSeeds(final ImagePlus rgb, final Color fseed,
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
   * @throws RandomWalkException on problems with decoding, unsupported image or empty list
   * @see #decodeSeeds(ImagePlus, Color, Color)
   */
  public static Map<Seeds, ImageProcessor> decodeSeeds(final ImageProcessor rgb, final Color fseed,
          final Color bseed) throws RandomWalkException {
    // output map integrating two lists of points
    Map<Seeds, ImageProcessor> out = new HashMap<Seeds, ImageProcessor>(2);
    // output lists of points. Can be null if points not found
    ImageProcessor foreground = new ByteProcessor(rgb.getWidth(), rgb.getHeight());
    ImageProcessor background = new ByteProcessor(rgb.getWidth(), rgb.getHeight());
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
          foreground.putPixel(x, y, 255); // remember foreground coords
        } else if (c.equals(bseed)) {
          background.putPixel(x, y, 255); // remember background coords
        }
      }
    }
    // pack outputs into map
    out.put(Seeds.FOREGROUND, foreground);
    out.put(Seeds.BACKGROUND, background);
    int[] histfg = foreground.getHistogram();
    int[] histbg = background.getHistogram();
    int pixelsNum = foreground.getPixelCount(); // the same for backgroud
    if (histfg[0] == pixelsNum || histbg[0] == pixelsNum) {
      throw new RandomWalkException(
              "Seed pixels are empty, check if:\n- correct colors were used\n- all slices have"
                      + " been seeded (if stacked seed is used)\n"
                      + "- Shrink/expand parameters are not too big.");
    }
    return out;
  }

  /**
   * Compare probabilities from two matrices and create third depending on winner.
   * 
   * @param seeds foreground and background seeds
   * @return OUT=FG>BG, 1 for every pixel that wins for FG, o otherwise
   */
  RealMatrix compare(Map<Seeds, RealMatrix> seeds) {
    RealMatrix fg = seeds.get(Seeds.FOREGROUND);
    RealMatrix bg = seeds.get(Seeds.BACKGROUND);
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
   * @return Copy of input shifted by one pixel in \a direction.
   */
  RealMatrix circshift(RealMatrix input, int direction) {
    double[][] sub; // part of matrix that does no change put is shifted
    int rows = input.getRowDimension(); // cache sizes
    int cols = input.getColumnDimension();
    Array2DRowRealMatrix out = new Array2DRowRealMatrix(rows, cols); // output matrix, shifted
    switch (direction) {
      case BOTTOM: // b
        // rotated right - last column become first
        // cut submatrix from first column to before last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 0, cols - 2, sub); // cols-2 because last is not
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 1);
        // copy last column to first
        out.setColumnVector(0, input.getColumnVector(cols - 1));
        break;
      case TOP: // top
        // rotated left - first column become last
        // cut submatrix from second column to last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 1, cols - 1, sub);
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 0);
        // copy first column to last
        out.setColumnVector(cols - 1, input.getColumnVector(0));
        break;
      case RIGHT: // right
        // rotated top - first row become last
        // cut submatrix from second row to last
        sub = new double[rows - 1][cols];
        input.copySubMatrix(1, rows - 1, 0, cols - 1, sub);
        // create new matrix - paste submatrix but shifted up
        out.setSubMatrix(sub, 0, 0);
        // copy first row to last
        out.setRowVector(rows - 1, input.getRowVector(0));
        break;
      case LEFT: // left (JBOTTM)
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
   * Compute (a-b)^2.
   * 
   * @param a left operand
   * @param b right operand
   * @return Image (a-b).^2
   */
  RealMatrix getSqrdDiffIntensity(RealMatrix a, RealMatrix b) {
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
  private RealMatrix[] precomputeGradients() {
    // setup shifted images
    RealMatrix right = circshift(image, RIGHT);
    RealMatrix top = circshift(image, TOP);
    // compute squared intensity differences
    RealMatrix gradRight2 = getSqrdDiffIntensity(image, right);
    RealMatrix gradTop2 = getSqrdDiffIntensity(image, top);
    // compute maximum of horizontal and vertical intensity gradients
    double maxGright2 = QuimPArrayUtils.getMax(gradRight2);
    LOGGER.debug("maxGright2 " + maxGright2);
    double maxGtop2 = QuimPArrayUtils.getMax(gradTop2);
    LOGGER.debug("maxGtop2 " + maxGtop2);
    double maxGrad2 = maxGright2 > maxGtop2 ? maxGright2 : maxGtop2;

    LOGGER.debug("maxGrad2max " + maxGrad2);
    // Normalize squared gradients to maxGrad
    gradRight2.walkInOptimizedOrder(new MatrixElementDivide(maxGrad2));
    gradTop2.walkInOptimizedOrder(new MatrixElementDivide(maxGrad2));
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
   * Compute mean value from image only for selected seed pixels.
   * 
   * @param seeds seeds list of points
   * @return mean value for selected seeds
   * @see #convertToList(Map)
   */
  protected double getMeanSeedGlobal(List<Point> seeds) {
    return StatUtils.mean(getValues(image, seeds).getDataRef());
  }

  /**
   * Calculate mean intensity for FG and BG seeds for stored image.
   * 
   * @param mask Binary mask of object
   * @param localMeanMaskSize Odd size of kernel
   * @return Averaged image. Average is computed for every pixel of image
   *         {@link #RandomWalkSegmentation(ImageProcessor, Params)} for <tt>localMeanMaskSize</tt>
   *         neighbours, within pixels masked by <tt>mask</tt>.
   */
  protected RealMatrix getMeanSeedLocal(ImageProcessor mask, int localMeanMaskSize) {
    if (localMeanMaskSize % 2 == 0) {
      throw new IllegalArgumentException("Kernel sie must be odd");
    }
    if (!mask.isBinary()) {
      throw new IllegalArgumentException("Mask must be binary");
    }
    if (mask.getWidth() != ip.getWidth() || mask.getHeight() != ip.getHeight()) {
      throw new IllegalArgumentException("Mask must have size of processed image");
    }
    ImageProcessor maskc = mask.duplicate(); // local copy of mask to not modify it
    maskc.subtract(254); // if it is IJ binary it contains [0,255], scale to 1.0
    ImageProcessor numofpix = maskc.duplicate(); // number of neighbouring pixels
    // fg
    // meanseed[FOREGROUND] = StatUtils.mean(getValues(image, seeds.get(FOREGROUND)).getDataRef());
    // generate kernel
    float[] kernel = new float[localMeanMaskSize * localMeanMaskSize];
    Arrays.fill(kernel, 1.0f);
    numofpix = maskc.convertToFloat(); // must convert here
    numofpix.convolve(kernel, localMeanMaskSize, localMeanMaskSize);
    numofpix.multiply(kernel.length); // convolution normalises kernel - revert it 1)
    // cut to mask - mulitply by 1.0 mask
    numofpix = new ImageCalculator()
            .run("mul create", new ImagePlus("", numofpix), new ImagePlus("", maskc))
            .getProcessor();
    // cut image to input mask
    ImageProcessor cutImage = new ImageCalculator()
            .run("mul create float", new ImagePlus("", ip), new ImagePlus("", maskc))
            .getProcessor();
    // convolve cut image
    cutImage.setCalibrationTable(null);
    cutImage.convolve(kernel, localMeanMaskSize, localMeanMaskSize);
    cutImage.multiply(kernel.length);
    // deal with edges of image after convolution - cut them
    cutImage = new ImageCalculator()
            .run("mul create float", new ImagePlus("", cutImage), new ImagePlus("", maskc))
            .getProcessor();
    // rounding floats to integer values
    float[] cutImageRaw = (float[]) cutImage.getPixels();
    for (int i = 0; i < cutImage.getPixelCount(); i++) {
      cutImageRaw[i] = Math.round(cutImageRaw[i]);
    }
    float[] numofpixRaw = (float[]) numofpix.getPixels();
    for (int i = 0; i < numofpix.getPixelCount(); i++) {
      numofpixRaw[i] = Math.round(numofpixRaw[i]);
    }
    // proper mean - use only pixels inside mask
    RealMatrix meanseedFg = new Array2DRowRealMatrix(cutImage.getHeight(), cutImage.getWidth());
    int count = 0;
    for (int r = 0; r < cutImage.getHeight(); r++) {
      for (int c = 0; c < cutImage.getWidth(); c++) {
        if (numofpix.getPixelValue(c, r) != 0) {
          // meanseedFg.setEntry(r, c,
          // ((double) cutImage.getPixelValue(c, r) / numofpix.getPixelValue(c, r)));
          meanseedFg.setEntry(r, c, ((double) cutImageRaw[count] / numofpixRaw[count]));
        } else {
          meanseedFg.setEntry(r, c, 0.0);
        }
        count++;
      }
      // ImageProcessor meanseedFg = new ImageCalculator()
      // .run("div create float", new ImagePlus("", cutImage), new ImagePlus("", numofpix))
      // .getProcessor();
      // float[] raw = (float[]) meanseedFg.getPixels();
      // for (int i = 0; i < meanseedFg.getPixelCount(); i++) {
      // if (Double.isNaN(raw[i])) {
      // raw[i] = 0;
      // }
      // }
    }

    if (QuimP.SUPER_DEBUG) { // save intermediate results
      LOGGER.debug("Saving intermediate results");
      String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
      IJ.saveAsTiff(new ImagePlus("", numofpix), tmpdir + "maskc_QuimP.tif");
      IJ.saveAsTiff(new ImagePlus("", cutImage), tmpdir + "imagecc_QuimP.tif");
      IJ.saveAsTiff(new ImagePlus("", realMatrix2ImageProcessor(meanseedFg)),
              tmpdir + "meanseedFg_QuimP.tif");
      LOGGER.trace("meanseedFg M[183;289] " + meanseedFg.getEntry(183 - 1, 289 - 1));
      LOGGER.trace("meanseedFg M[242;392] " + meanseedFg.getEntry(242 - 1, 392 - 1));
      LOGGER.trace("cutImage M[192;279] " + cutImage.getPixelValue(279 - 1, 192 - 1));
    }
    return meanseedFg;
  }

  /**
   * Convert processors obtained for object and background to format accepted by RW.
   * 
   * @param seeds foreground and background seeds
   * @return List of point coordinates accepted by RW algorithm.
   */
  public static Map<Seeds, List<Point>> convertToList(Map<Seeds, ImageProcessor> seeds) {
    ImageProcessor fgmask = seeds.get(Seeds.FOREGROUND);
    ImageProcessor bgmask = seeds.get(Seeds.BACKGROUND);
    // output map integrating two lists of points
    HashMap<Seeds, List<Point>> out = new HashMap<Seeds, List<Point>>(2);
    // output lists of points. Can be null if points not found
    List<Point> foreground = new ArrayList<>();
    List<Point> background = new ArrayList<>();
    for (int x = 0; x < fgmask.getWidth(); x++) {
      for (int y = 0; y < fgmask.getHeight(); y++) {
        if (fgmask.get(x, y) > 0) {
          foreground.add(new Point(x, y)); // remember foreground coords
        }
        if (bgmask.get(x, y) > 0) {
          background.add(new Point(x, y)); // remember background coords
        }
      }
    }
    // pack outputs into map
    out.put(Seeds.FOREGROUND, foreground);
    out.put(Seeds.BACKGROUND, background);
    return out;
  }

  /**
   * Do main computations.
   * 
   * @param seeds seed array returned from {@link #decodeSeeds(ImagePlus, Color, Color)}
   * @param gradients precomputed gradients returned from {@link #precomputeGradients()}
   * @return Computed probabilities for background and foreground, RealMatrix[2],
   *         RealMatrix[FOREGROUND] and RealMatrix[BACKGROUND]
   */
  protected Map<Seeds, RealMatrix> solver(Map<Seeds, ImageProcessor> seeds,
          RealMatrix[] gradients) {

    RealMatrix diffIfg = null;
    RealMatrix diffIbg = null;
    Map<Seeds, List<Point>> seedsP = convertToList(seeds);
    if (params.useLocalMean && seeds.get(Seeds.ROUGHMASK) != null) {
      if (seeds.get(Seeds.ROUGHMASK) == null) {
        throw new IllegalArgumentException("No rough mask provided");
      }
      RealMatrix localMeanFg =
              getMeanSeedLocal(seeds.get(Seeds.ROUGHMASK), params.localMeanMaskSize);
      diffIfg = image.subtract(localMeanFg);
      double meanseedBkg = getMeanSeedGlobal(seedsP.get(Seeds.BACKGROUND));
      LOGGER.trace("meanseedBkg: " + meanseedBkg);
      diffIbg = image.scalarAdd(meanseedBkg);
    } else { // localmean for whole seeds
      double[] meanseed = new double[2];
      meanseed[Seeds.FOREGROUND.getIndex()] = getMeanSeedGlobal(seedsP.get(Seeds.FOREGROUND));
      meanseed[Seeds.BACKGROUND.getIndex()] = getMeanSeedGlobal(seedsP.get(Seeds.BACKGROUND));
      LOGGER.debug("meanseed_fg=" + meanseed[0] + " meanseed_bg=" + meanseed[1]);
      // compute normalised squared differences to mean seed intensities
      diffIfg = image.scalarAdd(-meanseed[Seeds.FOREGROUND.getIndex()]);
      diffIbg = image.scalarAdd(-meanseed[Seeds.BACKGROUND.getIndex()]);
    }
    diffIfg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
    diffIbg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
    LOGGER.trace("fseeds size: " + seedsP.get(Seeds.FOREGROUND).size());
    LOGGER.trace("bseeds size: " + seedsP.get(Seeds.BACKGROUND).size());
    LOGGER.trace("getValfseed: " + getValues(image, seedsP.get(Seeds.FOREGROUND)));

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

    if (!params.useLocalMean) {
      // precompute terms for loop disabled rw_mod_1
      wrfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
      wlfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
      wtfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));
      wbfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));
    }

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

    StoppedBy stoppedReason = StoppedBy.ITERATIONS; // default assumption
    int i; // iteration counter
    // main loop
    outerloop: for (i = 0; i < params.iter; i++) {
      LOGGER.trace("Iter: " + i);
      // optimisation
      ArrayRealVector tmp = new ArrayRealVector(1); // filled with 0
      double[] tmpref = tmp.getDataRef();
      setValues(fg, seedsP.get(Seeds.BACKGROUND), tmp); // set 0
      setValues(bg, seedsP.get(Seeds.FOREGROUND), tmp);
      tmpref[0] = 1;
      setValues(fg, seedsP.get(Seeds.FOREGROUND), tmp); // set 1
      setValues(bg, seedsP.get(Seeds.BACKGROUND), tmp);
      tmp = null;

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
          if (Double.isNaN(fg2d[r][c])) {
            stoppedReason = StoppedBy.NANS;
          }
          if (Double.isInfinite(fg2d[r][c])) {
            stoppedReason = StoppedBy.INFS;
          }
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
          if (Double.isNaN(fg2d[r][c])) {
            stoppedReason = StoppedBy.NANS;
          }
          if (Double.isInfinite(fg2d[r][c])) {
            stoppedReason = StoppedBy.INFS;
          }
        }
      }
      // stop iteration if there is NaN or Inf. Iterations are stopped after full looping through fg
      // and bg
      if (stoppedReason == StoppedBy.NANS || stoppedReason == StoppedBy.INFS) {
        break outerloop;
      }
      QuimPArrayUtils.copy2darray(fg2d, fglast2d);
      fg = new Array2DRowRealMatrix(fg2d, false); // not copy of FG2d, just replace old FG
      bg = new Array2DRowRealMatrix(bg2d, false);
    }

    LOGGER.debug("Stopped by " + stoppedReason);
    Map<Seeds, RealMatrix> ret = new HashMap<Seeds, RealMatrix>(2);
    ret.put(Seeds.FOREGROUND, fg);
    ret.put(Seeds.BACKGROUND, bg);

    return ret;
  }

  /**
   * compute relative error between current foreground and foreground from last iteration.
   * 
   * @param fglast foreground matrix from last iteration
   * @param fg current foreground
   * @return relative error
   */
  double computeRelErr(double[][] fglast, double[][] fg) {

    return 0;
  }

  /**
   * 
   * @param diffI normalized squared differences to mean seed intensities
   * @param grad2 normalized the squared gradients by the maximum gradient
   * @return wr_fg = exp(P.alpha*diffI_fg+P.beta*G.gradright2);
   */
  private RealMatrix computeweights(RealMatrix diffI, RealMatrix grad2) {
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
    return QuimPArrayUtils.getMin(cp);
  }

  /**
   * Return values from in matrix that are on indexes ind.
   * 
   * @param in Input matrix 2D
   * @param ind List of indexes
   * @return Values that are on indexes \a ind
   */
  ArrayRealVector getValues(RealMatrix in, List<Point> ind) {
    ArrayRealVector out = new ArrayRealVector(ind.size());
    int l = 0;
    for (Point p : ind) {
      out.setEntry(l++, in.getEntry(p.row, p.col));
    }
    return out;
  }

  /**
   * Set values from val on indexes ind in array in.
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
  static class MatrixDotSubDiv implements RealMatrixChangingVisitor {

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
  static class MatrixElementMultiply implements RealMatrixChangingVisitor {

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
   * Perform element-wise division by value (./val in Matlab). Done in-place.
   * 
   * @author p.baniukiewicz
   */
  static class MatrixElementDivide implements RealMatrixChangingVisitor {

    private double div;

    /**
     * Assign multiplier.
     * 
     * @param d multiplier
     */
    MatrixElementDivide(double d) {
      this.div = d;
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
      return arg2 / div;
    }

  }

  /**
   * Perform element-wise exp. Done in-place.
   * 
   * @author p.baniukiewicz
   */
  static class MatrixElementExp implements RealMatrixChangingVisitor {

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
  static class MatrixElementPower implements RealMatrixChangingVisitor {

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
  static class MatrixDotProduct implements RealMatrixChangingVisitor {

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
  static class MatrixDotDiv implements RealMatrixChangingVisitor {

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
   * Divide matrix by matrix (element by element) setting 0 to result if divider is 0. Prevent NaNs.
   * 
   * <p>It performs the operation:
   * 
   * <pre>
   * <code>
   * A = [....];
   * B = [....];
   * R = A./B;
   * R(isnan(R)) = 0;
   * </code>
   * </pre>
   * 
   * @author p.baniukiewicz
   *
   */
  static class MatrixDotDivN extends MatrixDotDiv {

    public MatrixDotDivN(RealMatrix m) {
      super(m);
    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {
      double entry = matrix.getEntry(arg0, arg1);
      if (entry != 0.0) {
        return arg2 / matrix.getEntry(arg0, arg1);
      } else {
        return 0.0;
      }
    }
  }

  /**
   * Perform operation this = this>d*matrix?1:0.
   * 
   * @author p.baniukiewicz
   *
   */
  static class MatrixCompareWeighted implements RealMatrixChangingVisitor {

    RealMatrix matrix;
    double weight;

    public MatrixCompareWeighted(RealMatrix matrix, double weight) {
      this.matrix = matrix;
      this.weight = weight;
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
      if (arg2 > matrix.getEntry(arg0, arg1) * weight) {
        return 1.0;
      } else {
        return 0.0;
      }
    }

  }

  /**
   * Add in-place this matrix to another.
   * 
   * <p>src/test/Resources-static/Matlab/rw_laplace4_java_base.m This is source file of segmentation
   * in Matlab that was a base for RandomWalkSegmentation Implementation
   * 
   * @author p.baniukiewicz
   *
   */
  static class MatrixDotAdd implements RealMatrixChangingVisitor {

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
  static class MatrixDotSub implements RealMatrixChangingVisitor {

    /**
     * Example src/test/Resources-static/Matlab/rw_laplace4_java_base.m This is source file of
     * segmentation in Matlab that was a base for RandomWalkSegmentation Implementation.
     */
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
  static class MatrixElementPowerDiv implements RealMatrixChangingVisitor {

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
