package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.util.Arrays;

/**
 * Hold algorithm parameters
 * 
 * @author p.baniukiewicz
 *
 */
public class Params {
    double alpha;
    double beta;
    double[] gamma;
    int Iter;
    double dt;
    double relim;

    /**
     * Set default values
     */
    public Params() {
        this.gamma = new double[2];
        alpha = 4e2;
        beta = 2 * 25;
        gamma[0] = 100;
        gamma[1] = 300;
        Iter = 10000;
        dt = 0.1;
        relim = 8e-3;
    }

    /**
     * Set user values
     * 
     * @param alpha
     * @param beta
     * @param gamma1
     * @param gamma2
     * @param iter
     * @param dt
     * @param relim
     */
    public Params(double alpha, double beta, double gamma1, double gamma2, int iter, double dt,
            double relim) {
        this();
        this.alpha = alpha;
        this.beta = beta;
        this.gamma[0] = gamma1;
        this.gamma[1] = gamma2;
        Iter = iter;
        this.dt = dt;
        this.relim = relim;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Params [alpha=" + alpha + ", beta=" + beta + ", gamma=" + Arrays.toString(gamma)
                + ", Iter=" + Iter + ", dt=" + dt + ", relim=" + relim + "]";
    }

}