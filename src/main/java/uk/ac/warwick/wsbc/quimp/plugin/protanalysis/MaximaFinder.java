package uk.ac.warwick.wsbc.quimp.plugin.protanalysis;

import java.awt.Polygon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * Calculate maxima for image.
 * 
 * Support various methods of finding maxima in ImageJ image.
 * 
 * @author p.baniukiewicz
 *
 */
public class MaximaFinder {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(MaximaFinder.class.getName());
  private ImageProcessor iP;
  private Polygon maxima; // found maxima as polygon

  // /**
  // * Indicate that image processor has been rotated. By default x coordinate should be frame, y
  // * index. But For visualisation is better to rotate image to have longer axis on bottom.
  // * By default TrackVisualisation.Map.Map(String, float[][]) rotates image.
  // */
  // public boolean ROTATED = true;

  /**
   * Construct MaximaFinder object.
   * 
   * @param iP Image processor with image to analyse.
   */
  public MaximaFinder(ImageProcessor iP) {
    this.iP = iP;
    maxima = null;
  }

  /**
   * Compute maxima using ImageJ procedure.
   * 
   * @param tolerance
   * @see <a href=
   *      "link">https://rsb.info.nih.gov/ij/developer/api/ij/plugin/filter/MaximumFinder.html</a>
   */
  public void computeMaximaIJ(double tolerance) {
    MaximumFinder mF = new MaximumFinder();
    maxima = mF.getMaxima(iP, tolerance, false);
    LOGGER.debug("Found maxima: " + maxima.npoints);
  }

  /**
   * Compute maxima from image where points different from background stand for location of maxima
   * in <tt>iP</tt>.
   * <p>
   * This method can be used for restoring maxima in compatible format supported by this class
   * from other image created outside.
   * 
   * @param mximaMap map of maxima in image used for constructing this object
   */
  public void computeMaximaImage(ImageProcessor mximaMap) {
    // TODO finish computeMaximaImage method
  }

  /**
   * Return values corresponding to indexes returned by getMaxima.
   * <p>
   * Must be called after getMaxima.
   * 
   * @return Maxima in order of indexes returned by getMaxima.
   */
  public double[] getMaxValues() {
    if (maxima == null)
      return new double[0];
    double[] ret = new double[maxima.xpoints.length];
    for (int i = 0; i < maxima.xpoints.length; i++)
      ret[i] = iP.getf(maxima.xpoints[i], maxima.ypoints[i]);
    return ret;
  }

  /**
   * 
   * @return Return maxima found by {@link #computeMaximaIJ(double)}. The coordinates depend on
   *         orientation of input image. For typical application like analysis of motility map, x
   *         axis stands for frames and y-axis for outline indexes.
   */
  public Polygon getMaxima() {
    if (maxima == null)
      return new Polygon();
    return maxima;
  }

  /**
   * 
   * @return Number of points found.
   */
  public int getMaximaNumber() {
    if (maxima == null)
      return 0;
    return maxima.npoints;
  }
}