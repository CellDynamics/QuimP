package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

import java.util.Arrays;

import uk.ac.warwick.wsbc.quimp.plugin.randomwalk.RandomWalkSegmentation.Seeds;

/**
 * Hold algorithm parameters.
 * 
 * @author p.baniukiewicz
 *
 */
public class Params {
  // TODO add params meaning
  /**
   * The alpha.
   */
  public double alpha;

  /**
   * The beta.
   */
  public double beta;

  /**
   * The gamma.
   * 
   * <p>gamma[1]==0 disables second sweep.
   */
  public double[] gamma;

  /**
   * The iter.
   */
  public int iter;

  /**
   * The dt.
   */
  public double dt;

  /**
   * The relim.
   */
  public double relim;

  /**
   * true if method {@link #swapGamma()} was used. Means that order is different that given by user.
   */
  public boolean gammaSwapped;

  /**
   * true if local mean algorithm is used. false if global mean for seeds is computed.
   * 
   * <p>If localMean is used, the seeds provided to
   * {@link RandomWalkSegmentation#run(java.util.Map)} must have {@link Seeds#ROUGHMASK} entry.
   */
  public boolean useLocalMean;

  /**
   * Size of mask for local mean algorithm (odd).
   */
  public int localMeanMaskSize;

  /**
   * Reference to filter used for filtering results between sweeps.
   * 
   * <p>null value switches off second sweep as well as gamma[1]==0. To switch off filtering between
   * sweeps use
   * {@link BinaryFilters.EmptyMorpho}
   */
  public BinaryFilters intermediateFilter;
  /**
   * Reference to filter used on final processing.
   * 
   * <p>null value switches off final filtering.
   */
  public BinaryFilters finalFilter;

  /**
   * Set default values.
   */
  public Params() {
    this.gamma = new double[2];
    alpha = 4e2;
    beta = 2 * 25;
    gamma[0] = 100;
    gamma[1] = 300;
    iter = 10000;
    dt = 0.1;
    relim = 8e-3;
    intermediateFilter = null;
    finalFilter = null;
    gammaSwapped = false;
    useLocalMean = false;
    localMeanMaskSize = 25;
  }

  /**
   * Set user values. For compulsory parameters only.
   * 
   * @param alpha alpha
   * @param beta beta
   * @param gamma1 gamma1
   * @param gamma2 gamma2
   * @param iter iter
   * @param dt dt
   * @param relim relim
   * @param useLocalMean useLocalMean
   * @param localMeanMaskSize localMeanMaskSize
   */
  public Params(double alpha, double beta, double gamma1, double gamma2, int iter, double dt,
          double relim, boolean useLocalMean, int localMeanMaskSize) {
    this();
    this.alpha = alpha;
    this.beta = beta;
    this.gamma[0] = gamma1;
    this.gamma[1] = gamma2;
    this.iter = iter;
    this.dt = dt;
    this.relim = relim;
    this.useLocalMean = useLocalMean;
    this.localMeanMaskSize = localMeanMaskSize;
  }

  /**
   * Helper method that simply swap order of gammas.
   * 
   * <p><tt>Solver</tt> takes only gamma[0] so if there is second sweep those values should be
   * swapped.
   */
  void swapGamma() {
    gammaSwapped = !gammaSwapped;
    double tmp = gamma[0];
    gamma[0] = gamma[1];
    gamma[1] = tmp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Params [alpha=" + alpha + ", beta=" + beta + ", gamma=" + Arrays.toString(gamma)
            + ", iter=" + iter + ", dt=" + dt + ", relim=" + relim + ", gammaSwapped="
            + gammaSwapped + ", useLocalMean=" + useLocalMean + ", localMeanMaskSize="
            + localMeanMaskSize + ", intermediateFilter=" + intermediateFilter + ", finalFilter="
            + finalFilter + "]";
  }

  /*
   * Do not use BinaryFilters references.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(alpha);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(dt);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(gamma);
    result = prime * result + (gammaSwapped ? 1231 : 1237);
    result = prime * result + iter;
    result = prime * result + localMeanMaskSize;
    temp = Double.doubleToLongBits(relim);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (useLocalMean ? 1231 : 1237);
    return result;
  }

  /*
   * Do not use BinaryFilters references.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Params other = (Params) obj;
    if (Double.doubleToLongBits(alpha) != Double.doubleToLongBits(other.alpha)) {
      return false;
    }
    if (Double.doubleToLongBits(beta) != Double.doubleToLongBits(other.beta)) {
      return false;
    }
    if (Double.doubleToLongBits(dt) != Double.doubleToLongBits(other.dt)) {
      return false;
    }
    if (!Arrays.equals(gamma, other.gamma)) {
      return false;
    }
    if (gammaSwapped != other.gammaSwapped) {
      return false;
    }
    if (iter != other.iter) {
      return false;
    }
    if (localMeanMaskSize != other.localMeanMaskSize) {
      return false;
    }
    if (Double.doubleToLongBits(relim) != Double.doubleToLongBits(other.relim)) {
      return false;
    }
    if (useLocalMean != other.useLocalMean) {
      return false;
    }
    return true;
  }

}