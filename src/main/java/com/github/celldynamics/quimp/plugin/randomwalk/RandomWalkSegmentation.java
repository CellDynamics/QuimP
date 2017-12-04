package com.github.celldynamics.quimp.plugin.randomwalk;

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

import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.utils.QuimPArrayUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/*
 * //!>
 * @startuml doc-files/RandomWalkSegmentation_1_UML.png
 * usecase IN as "Initialisation
 * --
 * Apply when main object is initialised.
 * Object is always connected to the image
 * that will be segmented."
 * 
 * usecase RUN as "Run
 * --
 * Related to running segmentation on
 * the object. The result is segmented
 * image"
 * 
 * usecase CON as "Convert
 * --
 * Related to various conversions
 * that are available as static
 * methods. They allow to change
 * representation of objects among
 * dataformat used by this object.
 * "
 * 
 * usecase PAR as "Parameters
 * --
 * Related to creating object
 * representing parameters
 * of the segmentation process.
 * "
 * Dev -> IN
 * Dev -> RUN
 * Dev --> CON
 * Dev --> PAR
 * 
 * RUN ..> IN : <<include>>
 * PAR -- IN
 * CON -- RUN
 * @enduml
 * //!<
 */
/*
 * //!>
 * @startuml doc-files/RandomWalkSegmentation_2_UML.png
 * start
 * :Assign inputs
 * to internal fields;
 * note right
 * Inputs are **referenced**
 * end note
 * :Validate input
 * image;
 * :Convert image to
 * ""Matrix"";
 * note right
 * Or Matrix to image
 * depending on constructor
 * end note
 * stop
 * @enduml
 * //!<
 */
/*
 * //!>
 * @startuml doc-files/RandomWalkSegmentation_3_UML.png
 * start
 * :Compute gradients;
 * note left
 * Gradients depend
 * on image structure
 * and they are tabelarised
 * for speed
 * end note
 * :solve problem;
 * -> //Result of segmentation, FG and BG probability maps//;
 * note right
 * Main procedure for
 * RW segmentation.
 * Return segmented image.
 * end note
 * if (next sweep?) then (yes)
 * :Run
 * intermediate
 * filter;
 * note left
 * Optional, run on
 * previous results
 * end note
 * :Prepare to
 * next sweep;
 * note left
 * Convert previous result
 * into seed, increment
 * ""currentSweep""
 * end note
 * :solve problem;
 * endif
 * :Compare FG and BG;
 * note left
 * Foreground and 
 * background probabilities
 * are compared to get 
 * unique result
 * end note
 * -> //Binary image//;
 * :Run
 * post-filtering;
 * note right 
 * Optional
 * end note
 * stop
 * @enduml
 * //!<
 */
/*
 * //!>
 * @startuml doc-files/RandomWalkSegmentation_4_UML.png
 * actor User as user
 * participant RandomWalkSegmentation as rw
 * user -> rw : run(..)
 * activate rw
 * rw -> rw : precomputeGradients()
 *  activate rw
 *  rw --> rw : RealMatrix[]
 *  deactivate rw
 * rw -> rw : solver(..) 
 *  note right: Solver always return\nprobability maps for\nforeground AND background
 *  activate rw
 *  rw --> rw : Map<Seeds, RealMatrix> solved
 *  deactivate rw
 * alt next sweep?
 *  rw -> rw :  rollNextSweep(solved)
 *   activate rw
 *   rw --> rw : Map<Seeds, ImageProcessor> seedsNext
 *   deactivate rw
 *  rw -> rw : solver(seedsNext) 
 *   activate rw
 *   rw --> rw : Map<Seeds, RealMatrix> solved
 *   deactivate rw
 * end 
 * rw -> rw : compare(solved)
 *  note right: Compare FG and BG maps\nto get segmentation result
 *  activate rw
 *  rw --> rw : RealMatrix result
 *  deactivate rw
 * alt final filtering == yes
 *  rw -> Converter : RealMatrix
 *   activate Converter
 *   Converter --> rw : ImageProcessor
 *   deactivate Converter
 *  rw -> BinaryFilters : filter(ImageProcessor) 
 *   activate BinaryFilters
 *   BinaryFilters --> rw : filtered ImageProcessor
 *   deactivate BinaryFilters
 * else No final filtering
 *  rw -> Converter : RealMatrix
 *   activate Converter
 *   Converter --> rw : ImageProcessor
 *   deactivate Converter  
 * end 
 * note right: Except filtering, output can be\ncut by raw mask here, See run method.
 * rw --> user : ImageProcessor
 * deactivate rw
 * @enduml
 * //!<
 */
/**
 * This is implementation of Matlab version of Random Walk segmentation algorithm.
 * 
 * <p>Available use cases are: <br>
 * <img src="doc-files/RandomWalkSegmentation_1_UML.png"/><br>
 * 
 * <h1>Parameters</h1>
 * In this Use Case user create a set of segmentation parameters. They are hold in
 * {@link RandomWalkOptions}
 * class. This class also contains some pre- and post-processing settings used by
 * {@link RandomWalkSegmentation} object. Default constructor sets recommended values to all numeric
 * options skipping those related to pre- post-processing objects.
 * 
 * <h1>Initialisation</h1>
 * In this step the image that is supposed to be segmented is assigned to the object as well as
 * parameters of the process. There are two constructors available accepting <tt>ImageProcessor</tt>
 * and <tt>RelaMatrix</tt> image. These formats can be converted to each other using static
 * {@link #imageProcessor2RealMatrix(ImageProcessor)} and
 * {@link #realMatrix2ImageProcessor(RealMatrix)}.
 * <img src="doc-files/RandomWalkSegmentation_2_UML.png"/><br>
 * 
 * <h1>Convert</h1>
 * This Use Case contains preparatory static methods used for converting data into required format.
 * Those are:
 * <ol>
 * <li>ImageProcessor &lt;-&gt; RealMatrix converters
 * <li> RGB image -&gt; Seed converter
 * </ol>
 * The latter ones convert an RGB image that contain pixels labelled with different colors to binary
 * images. Each binary image will have only those pixels set which correspond to color of the
 * particular label. Seed colors should be unique across image, i.e. the object itself should be in
 * grey scale.
 * 
 * <h1>Run</h1>
 * In this USe Case the image assigned in <b>Initialisation</b> step is segmented and processed
 * according to parameters set in <b>Parameters</b>. The process is started by calling
 * {@link #run(Map)} method. The input <tt>Map</tt> contains seeds pixels converted to binary images
 * formed into {@link java.util.Map}. This structure is produced from RGB images by static
 * {@link #decodeSeeds(ImagePlus, Color, Color)} or
 * {@link #decodeSeeds(ImageProcessor, Color, Color)}.
 * 
 * <p>Here is brief look for {@link #run(Map)} method activity:
 * <img src="doc-files/RandomWalkSegmentation_3_UML.png"/><br>
 * 
 * <p>Sequence diagram of {@link #run(Map)} giving the order of called methods:
 * <img src="doc-files/RandomWalkSegmentation_4_UML.png"/><br>
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
   * Keep information about current sweep that {@link #solver(Map, RealMatrix[])} is doing.
   * 
   * <p>Affect indexing parameters arrays that keep different params for different sweeps. Used also
   * in {@link #solver(Map, RealMatrix[]) for computing actual number of iterations (second sweep
   * uses half of defined)}
   * 
   * @see RandomWalkOptions
   */
  private int currentSweep = 0;

  /**
   * Squared maximal theoretical intensity value.
   * 
   * <p>For 8-bit images it is 255^2, for 16-bit 65535^2. Set in constructor.
   */
  private int maxTheoreticalIntSqr;

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
   * Define foreground and background indexes enums with numerical indexes.
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
     * Rough mask used for computing local mean. Used only if {@link RandomWalkParams#useLocalMean}
     * is true.
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
  private RandomWalkOptions params;

  /**
   * Construct segmentation object from ImageProcessor.
   * 
   * @param ip image to segment
   * @param params parameters
   * @throws RandomWalkException on wrong image format
   */
  public RandomWalkSegmentation(ImageProcessor ip, RandomWalkOptions params)
          throws RandomWalkException {
    if (ip.getBitDepth() != 8 && ip.getBitDepth() != 16) {
      throw new RandomWalkException("Only 8-bit or 16-bit images are supported");
    }
    this.ip = ip;
    this.image = RandomWalkSegmentation.imageProcessor2RealMatrix(ip);
    this.params = params;
    setMaxTheoreticalIntSqr(ip);
  }

  /**
   * Construct segmentation object from 2D RealMatrix representing image.
   * 
   * <p>It is assumed that this input image is 8-bit. Use
   * {@link #RandomWalkSegmentation(ImageProcessor, RandomWalkOptions)} for support 8 and 16-bit
   * images.
   * Passing wrong image can have effect to results as {@link #solver(Map, RealMatrix[])} normalises
   * image intensities to maximal theoretical intensity. See
   * {@link #setMaxTheoreticalIntSqr(ImageProcessor)} and {@link #solver(Map, RealMatrix[])}
   * 
   * @param image image to segment
   * @param params parameters
   */
  public RandomWalkSegmentation(RealMatrix image, RandomWalkOptions params) {
    this.image = image;
    this.ip = realMatrix2ImageProcessor(image);
    this.params = params;
    setMaxTheoreticalIntSqr(ip);
  }

  /**
   * Main runner, does segmentation.
   * 
   * @param seeds Seed arrays from decodeSeeds(ImagePlus, Color, Color)
   * @return Segmented image as ByteProcessor
   * @throws RandomWalkException On wrong seeds
   */
  public ImageProcessor run(Map<Seeds, ImageProcessor> seeds) throws RandomWalkException {
    LOGGER.debug("Running with options: " + params.toString());
    // TODO change behaviour of gamma[1]==0. Maybe it should do second sweep but with gamma==0
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
    }
    RealMatrix result = compare(solved); // result as matrix
    ImageProcessor resultim = realMatrix2ImageProcessor(result).convertToByteProcessor(true);
    // cut mask - cut segmentation result by initial ROUGHMASK if present
    if (params.maskLimit == true && seeds.get(Seeds.ROUGHMASK) != null) {
      ImageCalculator ic = new ImageCalculator();
      ImagePlus retc =
              ic.run("and create",
                      new ImagePlus("", new BinaryProcessor(
                              (ByteProcessor) seeds.get(Seeds.ROUGHMASK).convertToByte(true))),
                      new ImagePlus("", new BinaryProcessor((ByteProcessor) resultim)));
      resultim = retc.getProcessor();
    }
    // do final filtering
    if (params.finalFilter != null) {
      return params.finalFilter.filter(resultim.convertToByteProcessor(true));
    } else {
      return resultim.convertToByteProcessor(true);
    }

  }

  /**
   * Prepare seeds from results of previous solver.
   * 
   * @param solved results from {@link #solver(Map, RealMatrix[])}
   * @return new seed taken from previous solution.
   * @throws RandomWalkException on unsupported image or empty seed list after decoding
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
    // filter them (if we are here intermediate filter can't be null)
    fg1 = params.intermediateFilter.filter(fg1);
    bg1.invert();
    bg1 = params.intermediateFilter.filter(bg1);
    bg1.invert();
    // increment sweep pointer to point correct parameters (if they are different for next sweep)
    currentSweep++;

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
    return new FloatProcessor(QuimPArrayUtils.double2dfloat(rawData));
  }

  /**
   * Find <b>BACKGROUND</b> and <b>FOREGROUND</b> labelled pixels on seed image and return their
   * positions.
   * 
   * <p>Seeded RGB image is decomposed to separated binary images that contain only seeds.
   * E.g. <b>FOREGROUND</b> image will have only pixels that were labelled as foreground.
   * Decomposition is performed with respect to provided label colours.
   * 
   * @param rgb RGB seed image
   * @param fseed color of marker for foreground pixels
   * @param bseed color of marker for background pixels
   * @return Map containing extracted seed pixels from input RGB image that belong to foreground and
   *         background. Map is addressed by two enums: <i>FOREGROUND</i> and <i>BACKGROUND</i>
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
   * Decode RGB seed images to separate binary images.
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
   * Shift image in given direction with step of one and circular boundary conditions.
   *
   * @param input Image to be shifted
   * @param direction Shift direction. This method is adjusted to be compatible with Matlab code
   *        and to keep Matlab naming (rw_laplace4.m) thus the shift direction names are not
   *        adequate to shift direction.
   * @return Copy of input shifted by one pixel in direction.
   */
  RealMatrix circshift(RealMatrix input, int direction) {
    // Routine cuts part of matrix that does not change, paste it into new matrix shifting in given
    // direction and then add missing looped row/column (last or first depending on shift
    // direction).
    double[][] sub; // part of matrix that does no change but is shifted
    int rows = input.getRowDimension(); // cache sizes
    int cols = input.getColumnDimension();
    Array2DRowRealMatrix out = new Array2DRowRealMatrix(rows, cols); // output matrix, shifted
    switch (direction) {
      case BOTTOM:
        // rotated right - last column become first
        // cut submatrix from first column to before last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 0, cols - 2, sub); // cols-2 - cols is size not last ind
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 1);
        // copy last column to first
        out.setColumnVector(0, input.getColumnVector(cols - 1));
        break;
      case TOP:
        // rotated left - first column become last
        // cut submatrix from second column to last
        sub = new double[rows][cols - 1];
        input.copySubMatrix(0, rows - 1, 1, cols - 1, sub);
        // create new matrix - paste submatrix but shifted right
        out.setSubMatrix(sub, 0, 0);
        // copy first column to last
        out.setColumnVector(cols - 1, input.getColumnVector(0));
        break;
      case RIGHT:
        // rotated top - first row become last
        // cut submatrix from second row to last
        sub = new double[rows - 1][cols];
        input.copySubMatrix(1, rows - 1, 0, cols - 1, sub);
        // create new matrix - paste submatrix but shifted up
        out.setSubMatrix(sub, 0, 0);
        // copy first row to last
        out.setRowVector(rows - 1, input.getRowVector(0));
        break;
      case LEFT:
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
   * Pre-compute normalised gradient matrices.
   * 
   * <p>This computes squared intensity differences.These correspond to horizontal and vertical
   * gradients assuming distance of one between pixels.
   * 
   * @return Array of precomputed data in the following order: [0] - gRight2 [1] - gTop2 [2] -
   *         gLeft2 [3] - gBottom2
   */
  private RealMatrix[] precomputeGradients() {
    // setup shifted images assuming shift of one pixel and periodic boundary conditions.
    RealMatrix right = circshift(image, RIGHT);
    RealMatrix top = circshift(image, TOP);
    // compute squared intensity differences (a-b).^2
    RealMatrix gradRight2 = getSqrdDiffIntensity(image, right);
    RealMatrix gradTop2 = getSqrdDiffIntensity(image, top);
    // compute maximum of horizontal and vertical intensity gradients (for normalization)
    double maxGright2 = QuimPArrayUtils.getMax(gradRight2);
    LOGGER.debug("maxGright2 " + maxGright2);
    double maxGtop2 = QuimPArrayUtils.getMax(gradTop2);
    LOGGER.debug("maxGtop2 " + maxGtop2);
    // find maximum of horizontal and vertical maxima
    double maxGrad2 = maxGright2 > maxGtop2 ? maxGright2 : maxGtop2;
    LOGGER.debug("maxGrad2max " + maxGrad2);

    // Normalize squared gradients to maxGrad
    gradRight2.walkInOptimizedOrder(new MatrixElementDivide(maxGrad2));
    gradTop2.walkInOptimizedOrder(new MatrixElementDivide(maxGrad2));
    // assign outputs
    RealMatrix[] out = new RealMatrix[4];
    // get remaining directions - use already normalized squared gradients
    RealMatrix gradLeft2 = circshift(gradRight2, LEFT);
    out[2] = gradLeft2;
    RealMatrix gradBottom2 = circshift(gradTop2, BOTTOM);
    out[3] = gradBottom2;
    out[0] = gradRight2;
    out[1] = gradTop2;

    return out;
  }

  /**
   * Compute mean value from image only seeded pixels.
   * 
   * @param seeds coordinates of points used to calculate their mean intensity
   * @return mean value for points
   * @see #convertToList(Map)
   */
  protected double getMeanSeedGlobal(List<Point> seeds) {
    return StatUtils.mean(getValues(image, seeds).getDataRef());
  }

  /**
   * Calculate local mean intensity for input image but only for areas masked by provided mask.
   * 
   * <p>The mean over segmented image intensity is evaluated within square window of configurable
   * size and only for those pixels that are masked by binary mask given to this method. Mean is
   * calculated respectfully to the number of masked pixels within window. Window is moved over the
   * whole image.
   * 
   * <p>This method works similarly to the convolution with the difference that the kernel is
   * normalised for each position of the window to the number of masked pixels (within it). If for
   * any position of the window there are no masked pixels inside, value 0.0 is set as result.
   * 
   * @param mask Binary mask of segmented image. Mask must contain only pixels with intensity 0 or
   *        255 (according to definition of binary image in IJ)
   * @param localMeanMaskSize Odd size of kernel
   * @return Averaged image. Average is computed for every location of the kernel for its center
   *         utlising only masked pixels. For not masked pixels the mean is set to 0.0
   */
  protected RealMatrix getMeanSeedLocal(ImageProcessor mask, int localMeanMaskSize) {
    // IJ convolution is utilised. Computations are done twice, first time only on mask to get the
    // number of masked pixels covered by it for each image pixel
    // second time for segmented image to get sum of intensities within the kernel.
    // in both cases kernels are normalised (default behaviour of ImageProcessor.convolve) but then
    // "denormalised" by multiplying by their size. Kernels are 1-filled.
    if (localMeanMaskSize % 2 == 0) {
      throw new IllegalArgumentException("Kernel sie must be odd");
    }
    // must be binary as we later assume that mx intensity is 255 (like in IJ binary images)
    if (!mask.isBinary()) {
      throw new IllegalArgumentException("Mask must be binary");
    }
    if (mask.getWidth() != ip.getWidth() || mask.getHeight() != ip.getHeight()) {
      throw new IllegalArgumentException("Mask must have size of processed image");
    }
    ImageProcessor maskc = mask.duplicate(); // local copy of mask to not modify it
    maskc.subtract(254); // if it is IJ binary it contains [0,255], scale to 0 - 1.0
    // make mask copy for getting number of pixels in mask for each position of kernel
    ImageProcessor numofpix = maskc.duplicate();

    // generate 1-filled kernel of given size
    float[] kernel = new float[localMeanMaskSize * localMeanMaskSize];
    Arrays.fill(kernel, 1.0f);
    // get number of mask pixesl for ach position of kernel
    numofpix = maskc.convertToFloat(); // must convert here
    numofpix.convolve(kernel, localMeanMaskSize, localMeanMaskSize);
    numofpix.multiply(kernel.length); // convolution normalises kernel - revert it 1)
    // cut to mask - mulitply by 1.0 mask - reject pixels that are not masked
    numofpix = new ImageCalculator()
            .run("mul create", new ImagePlus("", numofpix), new ImagePlus("", maskc))
            .getProcessor();

    // get sum of intensities within the kernel for segmented image
    // cut image to input mask - set all other pixels to 0. Must be done here to not count nonmasked
    // pixels
    ImageProcessor cutImage = new ImageCalculator()
            .run("mul create float", new ImagePlus("", ip), new ImagePlus("", maskc))
            .getProcessor();
    // convolve cut image
    cutImage.setCalibrationTable(null);
    cutImage.convolve(kernel, localMeanMaskSize, localMeanMaskSize);
    cutImage.multiply(kernel.length); // denormalise result
    // deal with edges of image after convolution - remove them
    cutImage = new ImageCalculator()
            .run("mul create float", new ImagePlus("", cutImage), new ImagePlus("", maskc))
            .getProcessor();
    // rounding floats to integer values - convolution works for float images only
    float[] cutImageRaw = (float[]) cutImage.getPixels();
    for (int i = 0; i < cutImage.getPixelCount(); i++) {
      cutImageRaw[i] = Math.round(cutImageRaw[i]);
    }
    float[] numofpixRaw = (float[]) numofpix.getPixels();
    for (int i = 0; i < numofpix.getPixelCount(); i++) {
      numofpixRaw[i] = Math.round(numofpixRaw[i]);
    }
    // proper mean - use only pixels inside mask. we use cutImageRaw that contains sums of
    // intensities for each location of kernel and numofpixRaw that contains number of masked pixels
    RealMatrix meanseedFg = new Array2DRowRealMatrix(cutImage.getHeight(), cutImage.getWidth());
    int count = 0;
    for (int r = 0; r < cutImage.getHeight(); r++) {
      for (int c = 0; c < cutImage.getWidth(); c++) {
        if (numofpix.getPixelValue(c, r) != 0) { // skip 0 pixels to not divide by 0
          meanseedFg.setEntry(r, c, ((double) cutImageRaw[count] / numofpixRaw[count]));
        } else {
          meanseedFg.setEntry(r, c, 0.0); // if no pixels in mask for current r,c - set mean to 0
        }
        count++;
      }
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
   * Convert binary maps representing foreground and background seeds to lists of coordinates
   * (format accepted by {@link #solver(Map, RealMatrix[])}).
   * 
   * @param seeds foreground and background seeds
   * @return List of point coordinates accepted by RW algorithm.
   * @see #decodeSeeds(ImagePlus, Color, Color)
   * @see #decodeSeeds(ImageProcessor, Color, Color)
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

  /*
   * //!>
   * @startuml doc-files/RandomWalkSegmentation_5_UML.png
   * start
   * :Convert seeds to coordinates;
   * if (local mean?) then (yes)
   *  :compute **local** mean for **FG**;
   *  note left
   *  Result is an image
   *  end note
   *  :compute **global** mean for **BG**;
   *  note left
   *  Result is a number
   *  end note
   *  :subtract FG and BG means\nfrom image;
   * else (no)
   *  :compute **global** mean for **FG**;
   *  :compute **global** mean for **BG**;
   *  :subtract FG and BG means\nfrom image;
   * endif
   * -> Here we have Image-meanseed;
   * :compute normalised\n""(Image-meanseed).^2"";
   * note left
   * Square ""Image-meanseed"" and
   * normalise to maximal theoretical
   * intensity value
   * end note
   * :compute weights;
   * note right
   * Based on intensity of
   * pixels in stencil and 
   * intensity gradient
   * end note
   * :compute average of weights;
   * note left
   * Separately for horizontal
   * and vertical differences.
   * Used later for equalising
   * grid
   * end note
   * :compute diffusion constant;
   * note left
   * To obey stability
   * criterion
   * end note
   * :compute averaged weights;
   * note right
   * Equalising differentiation
   * grid.
   * end note
   * :compute FG;
   * note left
   * Solving Laplace equation
   * by Euler method (loop, number of iter
   * depends on sweep number)
   * end note
   * :compute BG;
   * note right
   * The same loop like FG
   * end note
   * stop
   * @enduml
   * //!<
   */
  /**
   * Run Random Walk segmentation.
   * 
   * <p>The activity diagram for solver is as follows:
   * <img src="doc-files/RandomWalkSegmentation_5_UML.png"/><br>
   * 
   * @param seeds seed array returned from {@link #decodeSeeds(ImagePlus, Color, Color)}
   * @param gradients pre-computed gradients returned from {@link #precomputeGradients()}
   * @return Computed probabilities for background and foreground, RealMatrix[2]=
   *         RealMatrix[FOREGROUND] and RealMatrix[BACKGROUND]
   */
  protected Map<Seeds, RealMatrix> solver(Map<Seeds, ImageProcessor> seeds,
          RealMatrix[] gradients) {
    RealMatrix diffIfg = null; // normalised squared differences to mean seed intensities for FG
    RealMatrix diffIbg = null; // normalised squared differences to mean seed intensities for BG
    Map<Seeds, List<Point>> seedsP = convertToList(seeds); // seed images as seed points coordinates
    // decide whether to use local mean or global mean. Local mean is computed within square mask
    // of configurable size whereas the global mean is a mean intensity of all seeded pixels. Both
    // evaluated separately for BG and FG
    if (params.useLocalMean && seeds.get(Seeds.ROUGHMASK) != null) {
      RealMatrix localMeanFg =
              getMeanSeedLocal(seeds.get(Seeds.ROUGHMASK), params.localMeanMaskSize);
      diffIfg = image.subtract(localMeanFg);
      double meanseedBkg = getMeanSeedGlobal(seedsP.get(Seeds.BACKGROUND));
      LOGGER.trace("meanseedBkg: " + meanseedBkg);
      diffIbg = image.scalarAdd(-meanseedBkg);
    } else { // global for whole seeds
      double[] meanseed = new double[2]; // will keep means for FG and BG
      // compute intensity means for image points labelled by seeds
      meanseed[Seeds.FOREGROUND.getIndex()] = getMeanSeedGlobal(seedsP.get(Seeds.FOREGROUND));
      meanseed[Seeds.BACKGROUND.getIndex()] = getMeanSeedGlobal(seedsP.get(Seeds.BACKGROUND));
      LOGGER.debug("meanseed_fg=" + meanseed[0] + " meanseed_bg=" + meanseed[1]);
      // compute normalised squared differences to mean seed intensities (Image-meanseed).^2
      diffIfg = image.scalarAdd(-meanseed[Seeds.FOREGROUND.getIndex()]);
      diffIbg = image.scalarAdd(-meanseed[Seeds.BACKGROUND.getIndex()]);
    }
    // normalize (Image-meanseed).^2 to maximal (theoretical) value which is 255^2 for 8-bit images
    // have it as private field as we support 16 images as well
    diffIfg.walkInOptimizedOrder(new MatrixElementPowerDiv(maxTheoreticalIntSqr));
    diffIbg.walkInOptimizedOrder(new MatrixElementPowerDiv(maxTheoreticalIntSqr));
    LOGGER.trace("fseeds size: " + seedsP.get(Seeds.FOREGROUND).size());
    LOGGER.trace("bseeds size: " + seedsP.get(Seeds.BACKGROUND).size());
    LOGGER.trace("getValfseed: " + getValues(image, seedsP.get(Seeds.FOREGROUND)));

    // compute weights for diffusion in all four directions, dependent on local gradients and
    // differences to mean intensities of seeds, for FG and BG
    Array2DRowRealMatrix wrfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[0]);
    Array2DRowRealMatrix wlfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[2]);
    Array2DRowRealMatrix wtfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[1]);
    Array2DRowRealMatrix wbfg = (Array2DRowRealMatrix) computeweights(diffIfg, gradients[3]);

    Array2DRowRealMatrix wrbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[0]);
    Array2DRowRealMatrix wlbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[2]);
    Array2DRowRealMatrix wtbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[1]);
    Array2DRowRealMatrix wbbg = (Array2DRowRealMatrix) computeweights(diffIbg, gradients[3]);

    // compute averaged weights, left/right and top/bottom used when computing second spatial
    // derivative from first one
    // avgwx_fg = 0.5*(wl_fg+wr_fg) - for FG
    RealMatrix avgwxfg = wlfg.add(wrfg); // wl_fg+wr_fg - (left+right)
    avgwxfg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // 0.5*(wl_fg+wr_fg)
    RealMatrix avgwyfg = wtfg.add(wbfg); // wt_fg+wb_fg - (top+bottom)
    avgwyfg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // 0.5*(wt_fg+wb_fg)
    // avgwx_bg = 0.5*(wl_bg+wr_bg) - for BG
    RealMatrix avgwxbg = wlbg.add(wrbg); // wl_bg+wr_bg - (left+right)
    avgwxbg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // 0.5*(wl_bg+wr_bg)
    RealMatrix avgwybg = wtbg.add(wbbg); // wt_bg+wb_bg - (top+bottom)
    avgwybg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // 0.5*(wt_bg+wb_bg)

    // Compute diffusion coefficient that will obey stability criterion
    double diffusion = getDiffusionConst(wrfg, wlfg, wtfg, wbfg, avgwxfg, avgwyfg);
    LOGGER.debug("D=" + diffusion);

    // get average "distance" between weights multiplying w = w.*avgw, this is only for optimisation
    // purposes.
    if (params.useLocalMean == false) { // does not apply if we use local mean FIXME why?
      wrfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
      wlfg.walkInOptimizedOrder(new MatrixDotProduct(avgwxfg));
      wtfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));
      wbfg.walkInOptimizedOrder(new MatrixDotProduct(avgwyfg));
    }
    wrbg.walkInOptimizedOrder(new MatrixDotProduct(avgwxbg));
    wlbg.walkInOptimizedOrder(new MatrixDotProduct(avgwxbg));
    wtbg.walkInOptimizedOrder(new MatrixDotProduct(avgwybg));
    wbbg.walkInOptimizedOrder(new MatrixDotProduct(avgwybg));

    // initialize FG and BG probability maps, they are outputs from this routine
    Array2DRowRealMatrix fg =
            new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());
    Array2DRowRealMatrix bg =
            new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());
    // this temporary array will keep solution from n-1 iteration used for computing relative error
    double[][] tmpFglast2d = new double[image.getRowDimension()][image.getColumnDimension()];

    // dereferencing arrays, all computations are done on underlying [][] arrays but not on
    // RelaMatrix objects, optimisation again
    double[][] wrfg2d = wrfg.getDataRef(); // weight to right for FG
    double[][] wlfg2d = wlfg.getDataRef(); // weight to left for FG
    double[][] wtfg2d = wtfg.getDataRef(); // weight to top for FG
    double[][] wbfg2d = wbfg.getDataRef(); // weight to bottom for FG

    double[][] wrbg2d = wrbg.getDataRef(); // weight to right for BG
    double[][] wlbg2d = wlbg.getDataRef(); // weight to left for BG
    double[][] wtbg2d = wtbg.getDataRef(); // weight to top for BG
    double[][] wbbg2d = wbbg.getDataRef(); // weight to bottom for BG

    double[][] fg2d = fg.getDataRef(); // reference to FG probabilities
    double[][] bg2d = bg.getDataRef(); // reference to BG probabilities

    StoppedBy stoppedReason = StoppedBy.ITERATIONS; // default assumption
    int i; // iteration counter
    // compute correct number of iterations. Second sweep uses 0.5*user
    int iter = params.iter / (currentSweep + 1);
    // main loop here we simulate diffusion process in time
    outerloop: for (i = 0; i < iter; i++) {
      LOGGER.trace("Iter: " + i);
      // fill seed pixels explicitly with probability 1 for FG and BG
      ArrayRealVector tmp = new ArrayRealVector(1); // filled with 0, setValues() needs that input
      double[] tmpref = tmp.getDataRef(); // just get reference to underlying array
      // set probability to 0 of being FG for BG seeds and vice versa
      setValues(fg, seedsP.get(Seeds.BACKGROUND), tmp); // set 0
      setValues(bg, seedsP.get(Seeds.FOREGROUND), tmp);
      // set probability to 1 for of being FG for FG seeds and vice versa
      tmpref[0] = 1;
      setValues(fg, seedsP.get(Seeds.FOREGROUND), tmp); // set 1
      setValues(bg, seedsP.get(Seeds.BACKGROUND), tmp);
      tmp = null;

      // ------------------- Computation of FG map ------------------------------------------------
      // groups for long term for FG. Get all four neighbours to current pixel
      Array2DRowRealMatrix fgcircright = (Array2DRowRealMatrix) circshift(fg, RIGHT);
      Array2DRowRealMatrix fgcircleft = (Array2DRowRealMatrix) circshift(fg, LEFT);
      Array2DRowRealMatrix fgcirctop = (Array2DRowRealMatrix) circshift(fg, TOP);
      Array2DRowRealMatrix fgcircbottom = (Array2DRowRealMatrix) circshift(fg, BOTTOM);

      // extract required references from RealMatrix objects.
      double[][] fgcircright2d = fgcircright.getDataRef(); // reference to FG probab. shifted right
      double[][] fgcircleft2d = fgcircleft.getDataRef(); // reference to FG probab. shifted left
      double[][] fgcirctop2d = fgcirctop.getDataRef(); // reference to FG probab. shifted top
      double[][] fgcircbottom2d = fgcircbottom.getDataRef(); // reference to FG probab. shifted bot

      // Traverse all pixels in FG map and update them according to diffusion from 4 neighbours of
      // each pixel
      for (int r = 0; r < fg.getRowDimension(); r++) { // rows
        for (int c = 0; c < fg.getColumnDimension(); c++) { // columns
          fg2d[r][c] += params.dt * (diffusion * (((fgcircright2d[r][c] - fg2d[r][c]) / wrfg2d[r][c]
                  - (fg2d[r][c] - fgcircleft2d[r][c]) / wlfg2d[r][c])
                  + ((fgcirctop2d[r][c] - fg2d[r][c]) / wtfg2d[r][c]
                          - (fg2d[r][c] - fgcircbottom2d[r][c]) / wbfg2d[r][c])));
          // - params.gamma[currentSweep] * fg2d[r][c] * bg2d[r][c] - disabled
          // validate numerical quality. This flags will stop iterations (break outerloop) but after
          // updating all pixels in FG and BG maps
          if (Double.isNaN(fg2d[r][c])) { // if at least one NaN in solution
            stoppedReason = StoppedBy.NANS;
          }
          if (Double.isInfinite(fg2d[r][c])) { // or Inf (will overwrite previous flag of course)
            stoppedReason = StoppedBy.INFS;
          }
        }
      }

      // ------------------- Computation of BG map ------------------------------------------------
      // groups for long term for BG. Get all four neighbours to current pixel
      Array2DRowRealMatrix bgcircright = (Array2DRowRealMatrix) circshift(bg, RIGHT);
      Array2DRowRealMatrix bgcircleft = (Array2DRowRealMatrix) circshift(bg, LEFT);
      Array2DRowRealMatrix bgcirctop = (Array2DRowRealMatrix) circshift(bg, TOP);
      Array2DRowRealMatrix bgcircbottom = (Array2DRowRealMatrix) circshift(bg, BOTTOM);

      // extract required references from RealMatrix objects.
      double[][] bgcircright2d = bgcircright.getDataRef(); // reference to BG probab. shifted right
      double[][] bgcircleft2d = bgcircleft.getDataRef(); // reference to BG probab. shifted left
      double[][] bgcirctop2d = bgcirctop.getDataRef(); // reference to BG probab. shifted top
      double[][] bgcircbottom2d = bgcircbottom.getDataRef(); // reference to BG probab. shifted bot

      // Traverse all pixels in BG map and update them according to diffusion from 4 neighbours of
      // each pixel
      for (int r = 0; r < bg.getRowDimension(); r++) {
        for (int c = 0; c < bg.getColumnDimension(); c++) {
          bg2d[r][c] += params.dt * (diffusion * (((bgcircright2d[r][c] - bg2d[r][c]) / wrbg2d[r][c]
                  - (bg2d[r][c] - bgcircleft2d[r][c]) / wlbg2d[r][c])
                  + ((bgcirctop2d[r][c] - bg2d[r][c]) / wtbg2d[r][c]
                          - (bg2d[r][c] - bgcircbottom2d[r][c]) / wbbg2d[r][c])));
          // - params.gamma[currentSweep] * tmpFglast2d[r][c] * bg2d[r][c] - disabled
          // validate numerical quality. This flags will stop iterations (break outerloop) but after
          // updating all pixels in FG and BG maps. (will overwrite previous state of the flag)
          if (Double.isNaN(fg2d[r][c])) {
            stoppedReason = StoppedBy.NANS;
          }
          if (Double.isInfinite(fg2d[r][c])) {
            stoppedReason = StoppedBy.INFS;
          }
        }
      }
      // Test state of the flag. Stop iteration if there is NaN or Inf. Iterations are stopped after
      // full looping through FG and BG maps.
      if (stoppedReason == StoppedBy.NANS || stoppedReason == StoppedBy.INFS) {
        break outerloop;
      }
      // check error every relErrStep number of iterations
      if (i % relErrStep == 0 && computeRelErr(tmpFglast2d, fg2d) < params.relim[currentSweep]) {
        stoppedReason = StoppedBy.RELERR;
        break outerloop;
      }

      // remember FG map for this iteration to use it to compute relative error in next iteration
      QuimPArrayUtils.copy2darray(fg2d, tmpFglast2d);
    }

    LOGGER.info("Sweep " + currentSweep + " Stopped by " + stoppedReason + " at " + i
            + " iteration from " + iter);
    Map<Seeds, RealMatrix> ret = new HashMap<Seeds, RealMatrix>(2);
    ret.put(Seeds.FOREGROUND, fg);
    ret.put(Seeds.BACKGROUND, bg);

    return ret;
  }

  /**
   * Compute relative error between current foreground and foreground from previous iteration.
   * 
   * <p>Assumes that both matrixes have the same size and they are regular arrays.
   * 
   * @param fglast foreground matrix from previous iteration
   * @param fg current foreground
   * @return relative mean error sum[2* |fg - fglast|/(fg + fglast)]/numofel
   */
  double computeRelErr(double[][] fglast, double[][] fg) {
    int rows = fglast.length;
    int cols = fglast[0].length;
    double rel = 0; // result to sum up
    double tmp = 0; // temporary - current element
    for (int r = 0; r < rows; r++) { // iterate over every element
      for (int c = 0; c < cols; c++) {
        double denominator = fg[r][c] + fglast[r][c]; // compute denominator
        if (denominator == 0.0) { // not divide by 0
          tmp = 0.0;
        } else { // get relative error
          tmp = 2 * Math.abs(fg[r][c] - fglast[r][c]) / denominator;
        }
        rel += tmp; // sum it up to get mean at the end
      }
    }
    double rele = rel / (rows * cols);
    LOGGER.debug("Relative error = " + rele);
    return rele; // return mean error
  }

  /**
   * Compute diffusion weights using difference to mean intensity and gradient.
   * 
   * @param diffI2 normalized squared differences to mean seed intensities
   * @param grad2 normalized the squared gradients by the maximum gradient
   * @return wr_fg = exp(alpha*diffI2 + beta*grad2);
   */
  private RealMatrix computeweights(RealMatrix diffI2, RealMatrix grad2) {
    double alpha = params.alpha; // user provided segmentation parameter
    double beta = params.beta; // user provided segmentation parameter
    double[][] diffI2d; // temporary references to intensity to mean values
    double[][] grad22d; // and gradient
    // convert RealMatrix to 2D array, approach depends on the RealMatrix type (see RealMatrix doc)
    if (diffI2 instanceof Array2DRowRealMatrix) {
      diffI2d = ((Array2DRowRealMatrix) diffI2).getDataRef();
    } else {
      diffI2d = diffI2.getData();
    }
    if (grad2 instanceof Array2DRowRealMatrix) {
      grad22d = ((Array2DRowRealMatrix) grad2).getDataRef();
    } else {
      grad22d = grad2.getData();
    }
    Array2DRowRealMatrix w =
            new Array2DRowRealMatrix(diffI2.getRowDimension(), diffI2.getColumnDimension()); // out
    double[][] w2d = w.getDataRef(); // reference of w to skip get/set element from RealMatrix
    // calculate weights
    for (int r = 0; r < diffI2.getRowDimension(); r++) {
      for (int c = 0; c < diffI2.getColumnDimension(); c++) {
        w2d[r][c] = Math.exp(diffI2d[r][c] * alpha + grad22d[r][c] * beta);
      }
    }

    return w;
  }

  /**
   * Multiply two matrices (element by element) and return minimum value from result.
   * 
   * @param a left operand (matrix)
   * @param b right operand (matrix of size of left operand)
   * @return min(a.*b)
   * @see #getDiffusionConst(RealMatrix, RealMatrix, RealMatrix, RealMatrix, RealMatrix, RealMatrix)
   */
  private double getMinofDotProduct(RealMatrix a, RealMatrix b) {
    RealMatrix cp = a.copy();
    cp.walkInOptimizedOrder(new MatrixDotProduct(b));
    return QuimPArrayUtils.getMin(cp);
  }

  /**
   * Compute diffusion constant that obeys stability criterion.
   * 
   * <p>Diffusion constant that meets CFL stability criterion (when solving Euler scheme):
   * D*dt/(dx^2) should be <<1/4
   * It is computed in the following way:
   * 
   * <pre>
   * <code>
   * drl2 = min ( min(min (wr_fg.*avgwx_fg)) , min(min (wl_fg.*avgwx_fg)) );
   * dtb2 = min ( min(min (wt_fg.*avgwy_fg)) , min(min (wb_fg.*avgwy_fg)) );
   * 
   * D=0.25*min(drl2,dtb2);
   * </code>
   * </pre>
   * 
   * @param wrfg weights for diffusion in right direction
   * @param wlfg weights for diffusion in left direction
   * @param wtfg weights for diffusion in top direction
   * @param wbfg weights for diffusion in bottom direction
   * @param avgwxfg averaged left -- right weights
   * @param avgwyfg averaged bottom -- top weights
   * @return diffusion constant that obeys stability criterion
   */
  private double getDiffusionConst(RealMatrix wrfg, RealMatrix wlfg, RealMatrix wtfg,
          RealMatrix wbfg, RealMatrix avgwxfg, RealMatrix avgwyfg) {
    // diffusion constant along x
    double tmp1 = getMinofDotProduct(wrfg, avgwxfg); // min(wrfg .* avgwxfg)
    double tmp2 = getMinofDotProduct(wlfg, avgwxfg); // min(wlfg .* avgwxfg)
    double drl2 = tmp1 < tmp2 ? tmp1 : tmp2; // min(tmp1,tmp2)
    // diffusion constant along y
    tmp1 = getMinofDotProduct(wtfg, avgwyfg); // min(wtfg .* avgwyfg)
    tmp2 = getMinofDotProduct(wbfg, avgwyfg); // min(wbfg .* avgwyfg)
    double dtb2 = tmp1 < tmp2 ? tmp1 : tmp2; // min(tmp1,tmp2)
    double diffusion = drl2 < dtb2 ? drl2 : dtb2; // D = min(drl2,dtb2)
    diffusion *= 0.25; // D = 0.25*min(drl2,dtb2)
    LOGGER.debug("drl2=" + drl2 + " dtb2=" + dtb2);
    return diffusion;
  }

  /**
   * Set maximum theoretical squared intensity depending on image type.
   * 
   * @param ip segmented image.
   */
  private void setMaxTheoreticalIntSqr(ImageProcessor ip) {
    switch (ip.getBitDepth()) {
      case 16:
        maxTheoreticalIntSqr = 65535 * 65535;
        break;
      case 8:
      default:
        maxTheoreticalIntSqr = 255 * 255; // default in case of RealMatrix on input
    }
  }

  /**
   * Return values from in matrix that are on indexes ind.
   * 
   * @param in Input matrix 2D
   * @param ind List of indexes
   * @return Values that are on indexes ind
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
   * @param val List of values, length must be the same as ind or 1
   */
  void setValues(RealMatrix in, List<Point> ind, ArrayRealVector val) {
    if (ind.size() != val.getDimension() && val.getDimension() != 1) {
      throw new InvalidParameterException(
              "Vector with data must contain 1 element or the same as indexes");
    }
    int delta; // step we move in val across elements, can be 1 or 0
    int l = 0; // will point current element in val
    if (val.getDimension() == 1) {
      delta = 0; // still point to the same element (because it is only one)
    } else {
      delta = 1; // move to the next one
    }
    for (Point p : ind) { // iterate over points to fill with values val
      in.setEntry(p.row, p.col, val.getDataRef()[l]);
      l += delta; // skip to next value (points are iterated in for)
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
