/**
 * @file RandomWalkSegmentation.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 * @date 22 Jun 2016
 *
 */
public class RandomWalkSegmentation {

    static final int RIGHT = -10; //!< Direction of circshift coded as in Matlab */
    static final int LEFT = 10; //!< Direction of circshift coded as in Matlab */
    static final int TOP = -01; //!< Direction of circshift coded as in Matlab */
    static final int BOTTOM = 01; //!< Direction of circshift coded as in Matlab */

    /**
     * Create RealMatrix 2D from image. Image is converted to Double
     * 
     * @param ip input image
     * @return 2D matrix converted to Double
     */
    static public RealMatrix ImagePlus2RealMatrix(ImagePlus ip) {
        if (ip == null)
            return null;
        RealMatrix out;
        float[][] image = ip.getProcessor().getFloatArray();
        out = MatrixUtils.createRealMatrix(QuimPArrayUtils.float2Ddouble(image));
        return out;
    }

    protected RealMatrix circshift(RealMatrix input, int direction) {
        double[][] sub; // part of matrix that does no change put is shifted
        int rows = input.getRowDimension(); // cache sizes
        int cols = input.getColumnDimension();
        RealMatrix out = MatrixUtils.createRealMatrix(rows, cols); // output matrix, shifted
        switch (direction) {
            case RIGHT:
                // rotated right - last column become first
                // cut submatrix from first column to before last
                sub = new double[rows][cols - 1];
                input.copySubMatrix(0, rows - 1, 0, cols - 2, sub); // cols-2 because last is not
                // create new matrix - paste submatrix but shifted right
                out.setSubMatrix(sub, 0, 1);
                // copy last column to first
                out.setColumnVector(0, input.getColumnVector(cols - 1));
                break;
            case LEFT:
                // rotated left - first column become last
                // cut submatrix from second column to last
                sub = new double[rows][cols - 1];
                input.copySubMatrix(0, rows - 1, 1, cols - 1, sub);
                // create new matrix - paste submatrix but shifted right
                out.setSubMatrix(sub, 0, 0);
                // copy first column to last
                out.setColumnVector(cols - 1, input.getColumnVector(0));
                break;
            case TOP:
                // rotated top - first row become last
                // cut submatrix from second row to last
                sub = new double[rows - 1][cols];
                input.copySubMatrix(1, rows - 1, 0, cols - 1, sub);
                // create new matrix - paste submatrix but shifted up
                out.setSubMatrix(sub, 0, 0);
                // copy first row to last
                out.setRowVector(rows - 1, input.getRowVector(0));
                break;
            case BOTTOM:
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
}
