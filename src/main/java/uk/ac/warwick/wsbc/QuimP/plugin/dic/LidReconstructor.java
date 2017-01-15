package uk.ac.warwick.wsbc.QuimP.plugin.dic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.ImageProcessorPlus;

/**
 * Implementation of Line Integration and Deconvolution algorithm proposed by Kam.
 * 
 * This algorithm uses corrected line integration that runs in both directions from current point of
 * image \f$(x_n,y_n)\f$ to \f$r_1\f$ and end \f$r_2\f$. Vector \f$dr\f$ is set to be parallel to 0X
 * axis. Final formula for reconstruction of pixel \f$S(x_n,y_n)\f$ is given by:
 * 
 * \f[ S(x_n,y_n)=\int_{r_1}^{(x_n,y_n)}\left ( I(r)-I_{i,0}) \right )e^{-\delta\left | r_n-r \right
 * | }dr-\int_{r_2}^{(x_n,y_n)}\left ( I(r)-I_{i,0}) \right )e^{-\delta\left | r-r_n \right | }dr
 * \f]
 * 
 * The algorithm perform the following steps to reconstruct whole image: \li Input is converted to
 * 16bit and stored in private field ExtraImageProcessor srcImageCopyProcessor \li The value of \c
 * shift is added to all pixels of input image. All operations are performed on copy of input \c
 * ImageProcessor or \c ImagePlus. Rotate image by angle to bring bas-reliefs perpendicular to OX
 * axis. Rotated image has different dimensions and it is padded by 0. After this operation 0 pixels
 * are those that belong to background. For every line in rotated image position of \b true pixels
 * is calculated. \b True pixels are those that belong to original image excluding background added
 * during rotation. For every line position of first and last \b true pixel is noted in table \c
 * ranges \li Decay factors are pre-calculated and stored in \c decays table. \li Final
 * reconstruction is performed.
 * 
 * Image for reconstruction is passed during construction of DICReconstruction object. For this
 * object ranges and decays are evaluated and then user can call reconstructionDicLid() method to
 * get reconstruction. getRanges() method also rotates private object thus rotation in
 * reconstructionDicLid() is not necessary (flagged by DICReconstruction::isRotated). Different
 * situation happens when the whole stack is reconstructed. To prevent creating new instance of
 * DICReconstruction for every slice the setIp(ImageProcessor) method is used for connecting new
 * slice. In this case it is assumed that ImageProcessor objects are similar and they have the same
 * geometry. ranges are filled only once on DICReconstruction constructing thus images connected by
 * setIp(ImageProcessor) are not rotated. This situation is detected in reconstructionDicLid() by
 * isRotated flag.
 * 
 * Privates:
 * <ul>
 * <li>\c ranges - \b true pixels begin and end on \a x axis. Set by getRanges(). [r][0] - x of
 * first pixel of line r of image, [r][1] - x of last pixel of image of line r
 * <li>\c maxWidth - Set by getRanges()
 * <li>\c decays - set by generateDeacy(double, int)
 * <li>\c srcImageCopyProcessor - local \b copy of input ImageProcessor passed to object. Set by
 * constructors and setIp(ImageProcessor)
 * <li>\c isRotated - It is set by getRanges() that rotates object to get true pixels position and
 * cancelled by setIP
 * </ul>
 * 
 * <p>
 * <b>Warning</b>
 * <p>
 * Currently this class supports only 8bit images. It can support also 16bit input but in this case
 * algorithm used for detection of \b true pixels may not work correctly for certain cases - when
 * maximum intensity will be \f$\mathrm{max}(\mathrm{int})-shift\f$
 * <p>
 * The input image can be prefitlered before processing. This is running mean filter of given mask
 * size applied at angle perpendicular to the shear (this angle is given by caller).
 * <p>
 * <ul>
 * <li>Z. Kam, “Microscopic differential interference contrast image processing by line integration
 * (LID) and deconvolution,” Bioimaging, vol. 6, no. 4, pp. 166–176, 1998.
 * <li>B. Heise, A. Sonnleitner, and E. P. Klement, “DIC image reconstruction on large cell scans.,”
 * Microsc. Res. Tech., vol. 66, no. 6, pp. 312–320, 2005.
 * </ul>
 * 
 * @author p.baniukiewicz
 *
 */
public class LidReconstructor {

    static final Logger LOGGER = LoggerFactory.getLogger(LidReconstructor.class.getName());
    private final int shift = 1; // shift added to original image eliminate 0s

    private ImageProcessor srcIp; // local reference of ImageProcessor (const)
    private double decay;
    private double angle;
    private double[] decays; // reference to preallocated decay data created
    private int maxWidth; // Width of image after rotation. Set by getRanges()
    private int[][] ranges; // true pixels begin and end on x axis.
    private ImageProcessor srcImageCopyProcessor; // local copy of input
    private boolean isRotated; // true if srcImageCopyProcessor is rotated
    private ImageStatistics is;
    private ImageProcessorPlus ipp; // helper class for rotating images
    private String prefilterangle;
    private int masksize;

    /**
     * Default constructor that accepts ImagePlus. It does not support stacks.
     * 
     * Input srcImage is not modified
     * 
     * @param srcImage Image to process
     * @param decay
     * @param angle DIC angle
     * 
     * @throws DicException Throws exception after generateRanges()
     */
    public LidReconstructor(final ImagePlus srcImage, double decay, double angle)
            throws DicException {
        this(srcImage.getProcessor(), decay, angle);
    }

    /**
     * Default constructor that accepts ImageProcessor
     * 
     * Input ip is not modified
     * 
     * @param ip ImageProcessor to process
     * @param decay
     * @param angle DIC angle
     * 
     * @throws DicException Throws exception after generateRanges()
     */
    public LidReconstructor(final ImageProcessor ip, double decay, double angle)
            throws DicException {
        this(ip, decay, angle, "0", 0);
    }

    /**
     * Default constructor.
     * 
     * @param ip
     * @param decay
     * @param angle
     * @param prefilterangle Supported angle of prefiltering
     * @param masksize uneven mask size, 0 switches off filtering
     * @throws DicException
     */
    public LidReconstructor(final ImageProcessor ip, double decay, double angle,
            String prefilterangle, int masksize) throws DicException {
        LOGGER.trace("Use params: ip:" + ip + " decay:" + decay + " angle:" + angle
                + " filterangle:" + prefilterangle + " masksize:" + masksize);
        this.angle = angle;
        this.decay = decay;
        this.isRotated = false;
        this.prefilterangle = prefilterangle;
        this.masksize = masksize;
        ipp = new ImageProcessorPlus();
        setIp(ip);
        recalculate();
    }

    /**
     * Sets new reconstruction parameters for current object
     * 
     * @param decay
     * @param angle
     * @throws DicException Throws exception after generateRanges()
     */
    public void setParams(double decay, double angle) throws DicException {
        this.angle = angle;
        this.decay = decay;
        recalculate();
    }

    /**
     * Assigns ImageProcessor for reconstruction to current object. Releases previous one.
     * 
     * This method can be used for changing image connected to DICReconstruction object. New image
     * should have the same architecture as image passed in constructor. Typically this method is
     * used for passing next slice from stack.
     * 
     * Input ip is not modified
     * 
     * @param ip New ImageProcessor containing image for reconstruction.
     */
    public void setIp(final ImageProcessor ip) {
        this.srcIp = ip;
        // make copy of original image to not modify it - converting to 16bit
        this.srcImageCopyProcessor = srcIp.convertToShort(true);
        new ImageProcessorPlus().runningMean(srcImageCopyProcessor, prefilterangle, masksize);
        srcImageCopyProcessor.resetMinAndMax(); // ensure that minmax will be recalculated (usually
                                                // they are stored in class field) set interpolation
        srcImageCopyProcessor.setInterpolationMethod(ImageProcessor.BICUBIC);
        // Rotating image - set 0 background
        srcImageCopyProcessor.setBackgroundValue(0.0);
        // getting mean value
        is = srcImageCopyProcessor.getStatistics();
        this.isRotated = false; // new Processor not rotated yet
    }

    /**
     * Recalculates true pixels range and new size of image after rotation. Setup private class
     * fields.
     * 
     * Modifies private class fields: <tt>maxWidth</tt>, <tt>ranges</tt>,
     * <tt>srcImageCopyProcessor</tt>
     * 
     * <tt>maxWidth</tt> holds width of image after rotation
     * 
     * <tt>ranges</tt> table holds first and last x position of image line (first and last pixel of
     * image on background after rotation)
     * 
     * <tt>srcImageCopyProcessor</tt> is rotated and shifted.
     * 
     * @throws DicException when input image is close to saturation e.g. has values of 65536-shift.
     *         This is due to applied algorithm of detection image pixels after rotation.
     * @see #reconstructionDicLid()
     */
    private void getRanges() throws DicException {
        double maxpixel; // minimal pixel value
        int r; // loop indexes
        int firstpixel, lastpixel; // first and last pixel of image in line

        // check condition for removing 0 value from image
        maxpixel = srcImageCopyProcessor.getMax();
        if (maxpixel > 65535 - shift) {
            LOGGER.error("Possible image clipping - check if image is saturated");
            throw new DicException(
                    String.format("Possible image clipping - input image has at leas one"
                            + " pixel with value %d", 65535 - shift));
        }
        // scale pixels by adding 1 - we remove any 0 value from source image
        srcImageCopyProcessor.add(shift);
        srcImageCopyProcessor.resetMinAndMax();
        // rotate image with extending it. borders have the same value as
        // background
        srcImageCopyProcessor = ipp.rotate(srcImageCopyProcessor, angle, true);
        isRotated = true; // current object was rotated
        // get references of covered object for optimisation purposes
        int newWidth = srcImageCopyProcessor.getWidth();
        int newHeight = srcImageCopyProcessor.getHeight();
        // set private fields - size of image after rotation
        maxWidth = newWidth;
        ranges = new int[newHeight][2];
        // get true pixels positions for every row
        for (r = 0; r < newHeight; r++) {
            // to not process whole line, detect where starts and ends pixels of
            // image (reject background added during rotation)
            for (firstpixel = 0; firstpixel < newWidth
                    && srcImageCopyProcessor.get(firstpixel, r) == 0; firstpixel++)
                ;
            for (lastpixel = newWidth - 1; lastpixel >= 0
                    && srcImageCopyProcessor.get(lastpixel, r) == 0; lastpixel--)
                ;
            // if empty row, reject it all. reconstructionDicLid process pixels in ranges. such
            // borders reject processing at all
            if (firstpixel >= newWidth) {
                firstpixel = 1;
                lastpixel = 0;
            }
            ranges[r][0] = firstpixel;
            ranges[r][1] = lastpixel;
        }
    }

    /**
     * Recalculates tables on demand. Calculates new ranges for true pixels and new decay table.
     * 
     * @throws DicException Throws exception after generateRanges()
     */
    private void recalculate() throws DicException {
        // calculate preallocated decay data
        // generateRanges() must be called first as it initializes fields used
        // by generateDecay()
        getRanges();
        generateDeacy(decay, maxWidth);
    }

    /**
     * Reconstruct DIC image by LID method. This is main method used to reconstruct passed ip
     * object.
     * 
     * The reconstruction algorithm assumes that input image bas-reliefs are oriented horizontally,
     * thus correct angle should be provided.
     * <p>
     * <b>Warning</b>
     * <p>
     * Used optimization with detecting of image pixels based on their value may not be accurate
     * when input image will be 16-bit and it will contain saturated pixels
     * 
     * @return Return reconstruction of srcImage as 16-bit image
     */
    public ImageProcessor reconstructionDicLid() {
        double cumsumup, cumsumdown;
        int c, u, d, r; // loop indexes
        int linindex = 0; // output table linear index
        if (!isRotated) { // rotate if not rotated in getRanges - e.g. we have
                          // new Processor added by setIp
            srcImageCopyProcessor.add(shift); // we use different IP so shift
                                              // must be added
            srcImageCopyProcessor = ipp.rotate(srcImageCopyProcessor, angle, true);
        }
        // dereferencing for optimization purposes
        int newWidth = srcImageCopyProcessor.getWidth();
        int newHeight = srcImageCopyProcessor.getHeight();
        // create array for storing results - 32bit float as imageprocessor
        ImageProcessor outputArrayProcessor = new FloatProcessor(newWidth, newHeight);
        float[] outputPixelArray = (float[]) outputArrayProcessor.getPixels();

        // do for every row - bas-relief is oriented horizontally
        for (r = 0; r < newHeight; r++) {
            // ranges[r][0] - first image pixel in line r
            // ranges[r][1] - last image pixel in line r
            linindex = linindex + ranges[r][0];
            // for every point apply KAM formula
            for (c = ranges[r][0]; c <= ranges[r][1]; c++) {
                // up
                cumsumup = 0;
                for (u = c; u >= ranges[r][0]; u--) {
                    cumsumup += (srcImageCopyProcessor.get(u, r) - shift - is.mean)
                            * decays[Math.abs(u - c)];
                }
                // down
                cumsumdown = 0; // cumulative sum from point r to the end of
                                // column
                for (d = c; d <= ranges[r][1]; d++) {
                    cumsumdown += (srcImageCopyProcessor.get(d, r) - shift - is.mean)
                            * decays[Math.abs(d - c)];
                }
                // integral
                outputPixelArray[linindex] = (float) (cumsumup - cumsumdown);
                linindex++;
            }
            linindex = linindex + newWidth - ranges[r][1] - 1;
        }
        // rotate back output processor
        outputArrayProcessor.setBackgroundValue(0.0);
        outputArrayProcessor.rotate(-angle);
        // crop it back to original size
        outputArrayProcessor = ipp.cropImageAfterRotation(outputArrayProcessor, srcIp.getWidth(),
                srcIp.getHeight());

        return outputArrayProcessor.convertToShort(true); // return reconstruction
    }

    /**
     * Generates decay table with exponential distances between pixels multiplied by decay
     * coefficient
     * 
     * @param decay The value of decay coefficient
     * @param length Length of table, usually equals to longest processed line on image
     */
    private void generateDeacy(double decay, int length) {
        decays = new double[length];
        for (int i = 0; i < length; i++)
            decays[i] = Math.exp(-decay * i);
    }

}
