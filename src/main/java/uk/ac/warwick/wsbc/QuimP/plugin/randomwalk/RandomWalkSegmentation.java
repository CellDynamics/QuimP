/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

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
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Perform element-wise multiplication by value (.*val in Matlab)
 * 
 * @author p.baniukiewicz
 * @remarks Done in-place
 */
class MatrixElementMultiply implements RealMatrixChangingVisitor {

    private double d; // multiplier

    /**
     * Assign multiplier
     * 
     * @param d multiplier
     */
    MatrixElementMultiply(double d) {
        this.d = d;
    }

    @Override
    public double end() {
        return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
    }

    /**
     * Multiply entry by value
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
        return arg2 * d;
    }

}

/**
 * Perform element-wise exp
 * 
 * @author p.baniukiewicz
 * @remarks Done in-place
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
 * Perform element-wise power (.^2 in Matlab) and then divide by val
 * 
 * @author p.baniukiewicz
 * @remarks Done in-place
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
     * Multiply entry by itself
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
        return (arg2 * arg2) / val;
    }
}

/**
 * Perform element-wise power (.^2 in Matlab)
 * 
 * @author p.baniukiewicz
 * @remarks Done in-place
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
     * Multiply entry by itself
     */
    @Override
    public double visit(int arg0, int arg1, double arg2) {
        return arg2 * arg2;
    }
}

/**
 * Multiply in-place this matrix by another
 * 
 * @author p.baniukiewicz
 *
 */
class MatrixDotProduct implements RealMatrixChangingVisitor {

    RealMatrix m;

    public MatrixDotProduct(RealMatrix m) {
        this.m = m;
    }

    @Override
    public double end() {
        return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        if (m.getColumnDimension() != arg1 || m.getRowDimension() != arg0)
            throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(),
                    arg0, arg1);

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

        return arg2 * m.getEntry(arg0, arg1);
    }

}

/**
 * Divide in-place this matrix by another
 * 
 * @author p.baniukiewicz
 *
 */
class MatrixDotDiv implements RealMatrixChangingVisitor {

    RealMatrix m;

    public MatrixDotDiv(RealMatrix m) {
        this.m = m;
    }

    @Override
    public double end() {
        return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        if (m.getColumnDimension() != arg1 || m.getRowDimension() != arg0)
            throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(),
                    arg0, arg1);

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

        return arg2 / m.getEntry(arg0, arg1);
    }

}

/**
 * Add in-place this matrix to another
 * 
 * @author p.baniukiewicz
 *
 */
class MatrixDotAdd implements RealMatrixChangingVisitor {

    RealMatrix m;

    public MatrixDotAdd(RealMatrix m) {
        this.m = m;
    }

    @Override
    public double end() {
        return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        if (m.getColumnDimension() != arg1 || m.getRowDimension() != arg0)
            throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(),
                    arg0, arg1);

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

        return arg2 + m.getEntry(arg0, arg1);
    }

}

/**
 * Sub in-place this matrix to another
 * 
 * @author p.baniukiewicz
 *
 */
class MatrixDotSub implements RealMatrixChangingVisitor {

    RealMatrix m;

    public MatrixDotSub(RealMatrix m) {
        this.m = m;
    }

    @Override
    public double end() {
        return 0;
    }

    @Override
    public void start(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        if (m.getColumnDimension() != arg1 || m.getRowDimension() != arg0)
            throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(),
                    arg0, arg1);

    }

    @Override
    public double visit(int arg0, int arg1, double arg2) {

        return arg2 - m.getEntry(arg0, arg1);
    }

}

/**
 * Sub and then div in-place this matrix and another
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
 * This is implementation of Matlab version of Random Walk segmentation algorithm
 * 
 * @author p.baniukiewicz
 * @see src/test/resources/Matlab/rw_laplace4_java_base.m at <a href="./examples.html">Examples</a>
 */
public class RandomWalkSegmentation {

    static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkSegmentation.class.getName());

    public static final int RIGHT = -10; //!< Direction of circshift coded as in Matlab */
    public static final int LEFT = 10; //!< Direction of circshift coded as in Matlab */
    public static final int TOP = -01; //!< Direction of circshift coded as in Matlab */
    public static final int BOTTOM = 01; //!< Direction of circshift coded as in Matlab */
    public static final int FOREGROUND = 0; //!< Definition of foreground pixels */
    public static final int BACKGROUND = 1; //!< Definition of background pixels */

    private RealMatrix image; //!< Image to process in 8bit greyscale
    private Params params; //!< User provided parameters

    /**
     * Construct segmentation object from ImageProcessor
     * 
     * @param ip image to segment
     * @param params parameters
     */
    public RandomWalkSegmentation(ImageProcessor ip, Params params) {
        if (ip.getBitDepth() != 8 && ip.getBitDepth() != 16)
            throw new IllegalArgumentException("Only 8-bit or 16-bit images are supported");
        image = RandomWalkSegmentation.ImageProcessor2RealMatrix(ip);
        this.params = params;
    }

    /**
     * Construct segmentation object from 2D RealMatrix representing image
     * 
     * @param image image to segment
     * @param params parameters
     */
    public RandomWalkSegmentation(RealMatrix image, Params params) {
        this.image = image;
        this.params = params;
    }

    /**
     * Main runner, does segmentation
     * 
     * @param seeds Seed arrays from decodeSeeds(ImagePlus, Color, Color)
     * @return Segmented image as ByteProcessor
     * @throws RandomWalkException On wrong seeds
     */
    public ImageProcessor run(Map<Integer, List<Point>> seeds) throws RandomWalkException {
        if (seeds.get(FOREGROUND).isEmpty() || seeds.get(BACKGROUND).isEmpty())
            throw new RandomWalkException(
                    "Seed pixels are empty, check whether correct colors were used");
        RealMatrix[] precomputed = precompute(); // precompute gradients
        RealMatrix[] solved = solver(image, seeds, precomputed, params); // run solver
        RealMatrix result = compare(solved[FOREGROUND], solved[BACKGROUND]); // result as matrix
        return RealMatrix2ImageProcessor(result).convertToByteProcessor(true);
    }

    /**
     * Find maximum in 2D RealMatrix
     * 
     * @param input Matrix to process
     * @return maximal value in \a input
     */
    static public double getMax(RealMatrix input) {
        double[][] data;
        if (input instanceof Array2DRowRealMatrix)
            data = ((Array2DRowRealMatrix) input).getDataRef();
        else
            data = input.getData(); // TODO optimize using visitors because this is copy
        double[] maxs = new double[input.getRowDimension()];
        for (int r = 0; r < input.getRowDimension(); r++)
            maxs[r] = StatUtils.max(data[r]);
        return StatUtils.max(maxs);
    }

    /**
     * Find minimum in 2D RealMatrix
     * 
     * @param input Matrix to process
     * @return minimal value in \a input
     */
    static public double getMin(RealMatrix input) {
        double[][] data;
        if (input instanceof Array2DRowRealMatrix)
            data = ((Array2DRowRealMatrix) input).getDataRef(); // only avaiable for non
                                                                // cache-friendly matrix
        else
            data = input.getData(); // TODO optimize using visitors because this is copy
        double[] maxs = new double[input.getRowDimension()];
        for (int r = 0; r < input.getRowDimension(); r++)
            maxs[r] = StatUtils.min(data[r]);
        return StatUtils.min(maxs);
    }

    /**
     * Create RealMatrix 2D from image. Image is converted to Double
     * 
     * @param ip input image
     * @return 2D matrix converted to Double
     */
    static public RealMatrix ImageProcessor2RealMatrix(ImageProcessor ip) {
        if (ip == null)
            return null;
        RealMatrix out;
        float[][] image = ip.getFloatArray();
        out = new Array2DRowRealMatrix(QuimPArrayUtils.float2ddouble(image), false); // no copy (it
                                                                                     // is done in
                                                                                     // float2double)
        return out;
    }

    /**
     * Create FloatProcessor 2D from RealMatrix.
     * 
     * @param rm input matrix
     * @return FloatProcessor
     */
    static public FloatProcessor RealMatrix2ImageProcessor(RealMatrix rm) {
        double[][] rawData = rm.getData();
        return new FloatProcessor(QuimPArrayUtils.double2float(rawData));
    }

    /**
     * Find \b BACKGROUND and \b FOREGROUND labeled pixels on seed image and return their positions
     * 
     * @param rgb RGB seed image
     * @param fseed color of marker for foreground pixels
     * @param bseed color of marker for background pixels
     * @return Map containing list of coordinates that belong to foreground and background. Map is
     *         addressed by two enums: \a FOREGROUND and \a BACKGROUND
     * @throws RandomWalkException When image other that RGB provided
     */
    public Map<Integer, List<Point>> decodeSeeds(final ImagePlus rgb, final Color fseed,
            final Color bseed) throws RandomWalkException {
        if (rgb.getType() != ImagePlus.COLOR_RGB)
            throw new RandomWalkException("Unsupported image type");
        return decodeSeeds(rgb.getProcessor(), fseed, bseed);
    }

    /**
     * @copydoc decodeSeeds(ImagePlus, Color, Color)
     */
    public Map<Integer, List<Point>> decodeSeeds(final ImageProcessor rgb, final Color fseed,
            final Color bseed) throws RandomWalkException {
        // output map integrating two lists of points
        HashMap<Integer, List<Point>> out = new HashMap<Integer, List<Point>>();
        // output lists of points. Can be null if points not found
        List<Point> foreground = new ArrayList<>();
        List<Point> background = new ArrayList<>();
        // verify input condition
        if (rgb.getBitDepth() != 24)
            throw new RandomWalkException("Unsupported seed image type");
        // find marked pixels
        ColorProcessor cp = (ColorProcessor) rgb; // can cast here because of type
                                                  // checking
        for (int x = 0; x < cp.getWidth(); x++)
            for (int y = 0; y < cp.getHeight(); y++) {
                Color c = cp.getColor(x, y); // get color for pixel
                if (c.equals(fseed)) // WARN Why must be y,x??
                    foreground.add(new Point(y, x)); // remember foreground coords
                else if (c.equals(bseed))
                    background.add(new Point(y, x)); // remember background coords
            }
        // pack outputs into map
        out.put(FOREGROUND, foreground);
        out.put(BACKGROUND, background);
        return out;
    }

    /**
     * Compare probabilities from two matrices and create third depending on winner
     * 
     * @param fg Foreground probabilities for all points
     * @param bg Background probabilities for all points
     * @return OUT=FG>BG, 1 for every pixel that wins for FG, o otherwise
     */
    public RealMatrix compare(RealMatrix fg, RealMatrix bg) {
        RealMatrix ret =
                MatrixUtils.createRealMatrix(fg.getRowDimension(), fg.getColumnDimension());
        for (int r = 0; r < fg.getRowDimension(); r++)
            for (int c = 0; c < fg.getColumnDimension(); c++)
                if (fg.getEntry(r, c) > bg.getEntry(r, c))
                    ret.setEntry(r, c, 1);
                else
                    ret.setEntry(r, c, 0);
        return ret;

    }

    /**
     * Compute image circularly shifted by one pixel towards selected direction
     * 
     * @param input Image to be shifted
     * @param direction Shift direction
     * @return Copy of \a input shifted by one pixel in \a direction.
     * @warning This method is adjusted to work as MAtlab code and to keep Matlab naming (
     *          rw_laplace4.m) thus the shift direction names are not adequate to shift direction
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
     * @param a
     * @param b
     * @return Image (a-b).^2
     */
    protected RealMatrix getSqrdDiffIntensity(RealMatrix a, RealMatrix b) {
        RealMatrix s = a.subtract(b);
        s.walkInOptimizedOrder(new MatrixElementPower());
        return s;
    }

    /**
     * Pre-compute gradient matrices
     * 
     * @return Array of precomputed data in the following order: -# [0] - gRight2 -# [1] - gTop2 -#
     *         [2] - gLeft2 -# [3] - gBottom2
     */
    private RealMatrix[] precompute() {
        // setup shifted images
        RealMatrix right = circshift(image, RIGHT);
        RealMatrix top = circshift(image, TOP);
        // compute squared intensity differences
        RealMatrix gRight2 = getSqrdDiffIntensity(image, right);
        RealMatrix gTop2 = getSqrdDiffIntensity(image, top);
        // compute maximum of horizontal and vertical intensity gradients
        double maxGright2 = RandomWalkSegmentation.getMax(gRight2);
        LOGGER.debug("maxGright2 " + maxGright2);
        double maxGtop2 = RandomWalkSegmentation.getMax(gTop2);
        LOGGER.debug("maxGtop2 " + maxGtop2);
        double maxGrad2 = maxGright2 > maxGtop2 ? maxGright2 : maxGtop2;
        LOGGER.debug("maxGrad2max " + maxGrad2);
        // Normalize squared gradients to maxGrad
        gRight2.walkInOptimizedOrder(new MatrixElementMultiply(1 / maxGrad2));
        gTop2.walkInOptimizedOrder(new MatrixElementMultiply(1 / maxGrad2));
        RealMatrix gLeft2 = circshift(gRight2, LEFT);
        RealMatrix gBottom2 = circshift(gTop2, BOTTOM);
        // assign outputs
        RealMatrix[] out = new RealMatrix[4];
        out[0] = gRight2;
        out[1] = gTop2;
        out[2] = gLeft2;
        out[3] = gBottom2;
        return out;
    }

    /**
     * Do main computations
     * 
     * @param image original image
     * @param seeds seed array returned from decodeSeeds(ImagePlus, Color, Color)
     * @param gradients precomputed gradients returned from precompute()
     * @param params Parameters
     * @return Computed probabilities for background and foreground
     * @retval RealMatrix[2], RealMatrix[FOREGROUND] and RealMatrix[BACKGROUND]
     */
    protected RealMatrix[] solver(RealMatrix image, Map<Integer, List<Point>> seeds,
            RealMatrix[] gradients, Params params) {
        // compute mean intensity of foreground and background pixels
        double meanseed_fg = StatUtils.mean(getValues(image, seeds.get(FOREGROUND)).getDataRef());
        double meanseed_bg = StatUtils.mean(getValues(image, seeds.get(BACKGROUND)).getDataRef());
        LOGGER.debug("meanseed_fg=" + meanseed_fg + " meanseed_bg=" + meanseed_bg); // correct
        LOGGER.trace("fseeds: " + seeds.get(FOREGROUND)); // correct
        LOGGER.trace("getValfseed: " + getValues(image, seeds.get(FOREGROUND))); // correct
        // compute normalised squared differences to mean seed intensities
        RealMatrix diffI_fg = image.scalarAdd(-meanseed_fg);
        diffI_fg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
        RealMatrix diffI_bg = image.scalarAdd(-meanseed_bg);
        diffI_bg.walkInOptimizedOrder(new MatrixElementPowerDiv(65025));
        // compute weights for diffusion in all four directions, dependent on local gradients and
        // differences to mean intensities of seeds
        Array2DRowRealMatrix wr_fg = (Array2DRowRealMatrix) computeweights(diffI_fg, gradients[0]);
        Array2DRowRealMatrix wl_fg = (Array2DRowRealMatrix) computeweights(diffI_fg, gradients[2]);
        Array2DRowRealMatrix wt_fg = (Array2DRowRealMatrix) computeweights(diffI_fg, gradients[1]);
        Array2DRowRealMatrix wb_fg = (Array2DRowRealMatrix) computeweights(diffI_fg, gradients[3]);

        Array2DRowRealMatrix wr_bg = (Array2DRowRealMatrix) computeweights(diffI_bg, gradients[0]);
        Array2DRowRealMatrix wl_bg = (Array2DRowRealMatrix) computeweights(diffI_bg, gradients[2]);
        Array2DRowRealMatrix wt_bg = (Array2DRowRealMatrix) computeweights(diffI_bg, gradients[1]);
        Array2DRowRealMatrix wb_bg = (Array2DRowRealMatrix) computeweights(diffI_bg, gradients[3]);

        // compute averaged weights, left/right and top/bottom
        // used when computing second spatial derivate from first one
        RealMatrix avgwx_fg = wl_fg.add(wr_fg);
        avgwx_fg.walkInOptimizedOrder(new MatrixElementMultiply(0.5)); // correct
        RealMatrix avgwy_fg = wt_fg.add(wb_fg);
        avgwy_fg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
        RealMatrix avgwx_bg = wl_bg.add(wr_bg);
        avgwx_bg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
        RealMatrix avgwy_bg = wt_bg.add(wb_bg);
        avgwy_bg.walkInOptimizedOrder(new MatrixElementMultiply(0.5));
        // d is the diffusion constant times the timestep of the Euler scheme obey CFL stability
        // criterion. D*dt/(dx^2) should be <<1/4
        double drl2, dtb2;
        double tmp1, tmp2;
        tmp1 = getStabilityCriterion(wr_fg, avgwx_fg);
        tmp2 = getStabilityCriterion(wl_fg, avgwx_fg);
        drl2 = tmp1 < tmp2 ? tmp1 : tmp2;
        tmp1 = getStabilityCriterion(wt_fg, avgwy_fg);
        tmp2 = getStabilityCriterion(wb_fg, avgwy_fg); // WARN See differences between matlab and
                                                       // java in definition up,down,etx. In matlab
                                                       // it seems to be wrong according to names
        dtb2 = tmp1 < tmp2 ? tmp1 : tmp2;
        double D = drl2 < dtb2 ? drl2 : dtb2; // D=0.25*min(drl2,dtb2)
        D *= 0.25; // correct
        LOGGER.debug("drl2=" + drl2 + " dtb2=" + dtb2); // ok
        LOGGER.debug("D=" + D); // ok

        Array2DRowRealMatrix FG =
                new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());
        double[][] FGlast2d = new double[image.getRowDimension()][image.getColumnDimension()];
        Array2DRowRealMatrix BG =
                new Array2DRowRealMatrix(image.getRowDimension(), image.getColumnDimension());

        // precompute terms for loop
        wr_fg.walkInOptimizedOrder(new MatrixDotProduct(avgwx_fg));
        wl_fg.walkInOptimizedOrder(new MatrixDotProduct(avgwx_fg));
        wt_fg.walkInOptimizedOrder(new MatrixDotProduct(avgwy_fg));
        wb_fg.walkInOptimizedOrder(new MatrixDotProduct(avgwy_fg));

        wr_bg.walkInOptimizedOrder(new MatrixDotProduct(avgwx_bg));
        wl_bg.walkInOptimizedOrder(new MatrixDotProduct(avgwx_bg));
        wt_bg.walkInOptimizedOrder(new MatrixDotProduct(avgwy_bg));
        wb_bg.walkInOptimizedOrder(new MatrixDotProduct(avgwy_bg));

        double[][] wr_fg2d = wr_fg.getDataRef();
        double[][] wl_fg2d = wl_fg.getDataRef();
        double[][] wt_fg2d = wt_fg.getDataRef();
        double[][] wb_fg2d = wb_fg.getDataRef();

        double[][] wr_bg2d = wr_bg.getDataRef();
        double[][] wl_bg2d = wl_bg.getDataRef();
        double[][] wt_bg2d = wt_bg.getDataRef();
        double[][] wb_bg2d = wb_bg.getDataRef();

        // main loop
        for (int i = 0; i < params.Iter; i++) {
            LOGGER.trace("Iter: " + i);
            // TODO create separate version for values
            setValues(FG, seeds.get(FOREGROUND), new ArrayRealVector(new double[] { 1 }));
            setValues(FG, seeds.get(BACKGROUND), new ArrayRealVector(new double[] { 0 }));
            setValues(BG, seeds.get(FOREGROUND), new ArrayRealVector(new double[] { 0 }));
            setValues(BG, seeds.get(BACKGROUND), new ArrayRealVector(new double[] { 1 }));

            // groups for long term for FG
            Array2DRowRealMatrix fgcirc_right = (Array2DRowRealMatrix) circshift(FG, RIGHT);
            Array2DRowRealMatrix fgcirc_left = (Array2DRowRealMatrix) circshift(FG, LEFT);
            Array2DRowRealMatrix fgcirc_top = (Array2DRowRealMatrix) circshift(FG, TOP);
            Array2DRowRealMatrix fgcirc_bottom = (Array2DRowRealMatrix) circshift(FG, BOTTOM);

            double[][] fgcirc_right2d = fgcirc_right.getDataRef();
            double[][] FG2d = FG.getDataRef();
            double[][] BG2d = BG.getDataRef();
            double[][] fgcirc_left2d = fgcirc_left.getDataRef();
            double[][] fgcirc_top2d = fgcirc_top.getDataRef();
            double[][] fgcirc_bottom2d = fgcirc_bottom.getDataRef();

            for (int r = 0; r < FG.getRowDimension(); r++)
                for (int c = 0; c < FG.getColumnDimension(); c++) {
                    FG2d[r][c] += params.dt * (D
                            * (((fgcirc_right2d[r][c] - FG2d[r][c]) / wr_fg2d[r][c]
                                    - (FG2d[r][c] - fgcirc_left2d[r][c]) / wl_fg2d[r][c])
                                    + ((fgcirc_top2d[r][c] - FG2d[r][c]) / wt_fg2d[r][c]
                                            - (FG2d[r][c] - fgcirc_bottom2d[r][c]) / wb_fg2d[r][c]))
                            - params.gamma[0] * FG2d[r][c] * BG2d[r][c]);
                }

            // groups for long term for BG
            Array2DRowRealMatrix bgcirc_right = (Array2DRowRealMatrix) circshift(BG, RIGHT);
            Array2DRowRealMatrix bgcirc_left = (Array2DRowRealMatrix) circshift(BG, LEFT);
            Array2DRowRealMatrix bgcirc_top = (Array2DRowRealMatrix) circshift(BG, TOP);
            Array2DRowRealMatrix bgcirc_bottom = (Array2DRowRealMatrix) circshift(BG, BOTTOM);

            double[][] bgcirc_right2d = bgcirc_right.getDataRef();
            double[][] bgcirc_left2d = bgcirc_left.getDataRef();
            double[][] bgcirc_top2d = bgcirc_top.getDataRef();
            double[][] bgcirc_bottom2d = bgcirc_bottom.getDataRef();

            for (int r = 0; r < BG.getRowDimension(); r++)
                for (int c = 0; c < BG.getColumnDimension(); c++) {
                    BG2d[r][c] += params.dt * (D
                            * (((bgcirc_right2d[r][c] - BG2d[r][c]) / wr_bg2d[r][c]
                                    - (BG2d[r][c] - bgcirc_left2d[r][c]) / wl_bg2d[r][c])
                                    + ((bgcirc_top2d[r][c] - BG2d[r][c]) / wt_bg2d[r][c]
                                            - (BG2d[r][c] - bgcirc_bottom2d[r][c]) / wb_bg2d[r][c]))
                            - params.gamma[0] * FGlast2d[r][c] * BG2d[r][c]);
                }

            QuimPArrayUtils.copy2darray(FG2d, FGlast2d);
            FG = new Array2DRowRealMatrix(FG2d, false); // not copy of FG2d, just replace old FG
            BG = new Array2DRowRealMatrix(BG2d, false);
        }
        RealMatrix[] ret = new RealMatrix[2];
        ret[FOREGROUND] = FG;
        ret[BACKGROUND] = BG;

        return ret;
    }

    /**
     * 
     * @param diffI normalized squared differences to mean seed intensities
     * @param grad2 normalized the squared gradients by the maximum gradient
     * @return wr_fg = exp(P.alpha*diffI_fg+P.beta*G.gradright2);
     * @todo optimize this part, see matlab code, products are the same
     */
    private RealMatrix computeweights(RealMatrix diffI, RealMatrix grad2) {
        double alpha = params.alpha;
        double beta = params.beta;
        double[][] diffI2d;
        double[][] grad22d;
        if (diffI instanceof Array2DRowRealMatrix)
            diffI2d = ((Array2DRowRealMatrix) diffI).getDataRef();
        else
            diffI2d = diffI.getData();
        if (grad2 instanceof Array2DRowRealMatrix)
            grad22d = ((Array2DRowRealMatrix) grad2).getDataRef();
        else
            grad22d = grad2.getData();
        Array2DRowRealMatrix w =
                new Array2DRowRealMatrix(diffI.getRowDimension(), diffI.getColumnDimension());
        double[][] w2d = w.getDataRef(); // reference of w
        for (int r = 0; r < diffI.getRowDimension(); r++)
            for (int c = 0; c < diffI.getColumnDimension(); c++) {
                w2d[r][c] = Math.exp(diffI2d[r][c] * alpha + grad22d[r][c] * beta);
            }
        return w;
    }

    /**
     * Compute part of stability criterion for stopping iterations
     * 
     * Only in-place multiplication and minimu of result is done here
     * 
     * @code drl2 = min ( min(min (wr_fg.*avgwx_fg)) , min(min (wl_fg.*avgwx_fg)) ); dtb2 = min (
     *       min(min (wt_fg.*avgwy_fg)) , min(min (wb_fg.*avgwy_fg)) ); D=0.25*min(drl2,dtb2);
     * @endcode
     * @param w__fg
     * @param avgw__fg
     * @return min(min (w__fg.*avgw__fg))
     */
    private double getStabilityCriterion(RealMatrix w__fg, RealMatrix avgw__fg) {
        RealMatrix cp = w__fg.copy();
        cp.walkInOptimizedOrder(new MatrixDotProduct(avgw__fg));
        return RandomWalkSegmentation.getMin(cp);
    }

    /**
     * Return values from \a in matrix that are on indexes \a ind
     * 
     * @param in Input matrix 2D
     * @param ind List of indexes
     * @return Values that are on indexes \a ind
     */
    protected ArrayRealVector getValues(RealMatrix in, List<Point> ind) {
        ArrayRealVector out = new ArrayRealVector(ind.size());
        int l = 0;
        for (Point p : ind)
            out.setEntry(l++, in.getEntry(p.row, p.col));
        return out;
    }

    /**
     * Set values from \a val on indexes \a ind in array \a in
     * 
     * @param in Input matrix. Will be modified
     * @param ind List of indexes
     * @param val List of values, length must be the same as \a ind or 1
     * @warning modify \a in
     */
    protected void setValues(RealMatrix in, List<Point> ind, ArrayRealVector val) {
        if (ind.size() != val.getDimension() & val.getDimension() != 1)
            throw new InvalidParameterException(
                    "Vector with data must contain 1 element or the same as indexes");
        int delta, l = 0;
        if (val.getDimension() == 1)
            delta = 0;
        else
            delta = 1;
        for (Point p : ind) {
            in.setEntry(p.row, p.col, val.getDataRef()[l]);
            l += delta;
        }
    }
}

/**
 * @example src/test/resources/Matlab/rw_laplace4_java_base.m This is source file of segmentation in
 *          Matlab that was a base for RandomWalkSegmentation Implementation
 */
