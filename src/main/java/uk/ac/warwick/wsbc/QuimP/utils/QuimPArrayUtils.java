/**
 * @file QuimPArrayUtils.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.utils;

/**
 * Deliver simple methods operating on arrays
 * @author p.baniukiewicz
 * @date 22 Jun 2016
 *
 */
public class QuimPArrayUtils {

    /**
     * Convert 2D float array to double
     * 
     * @param input Array to convert
     * @return converted one
     */
    public static double[][] float2ddouble(float[][] input) {
        if (input == null)
            return null;
        int rows = input.length;
        double[][] out = new double[rows][];
        // iterate over rows with conversion
        for (int r = 0; r < rows; r++) {
            float[] row = input[r];
            int cols = row.length;
            out[r] = new double[cols];
            // iterate over columns
            for (int c = 0; c < cols; c++) {
                out[r][c] = input[r][c];
            }
        }
        return out;
    }

    /**
     * Convert 2D double array to float
     * 
     * @param input Array to convert
     * @return converted one
     */
    public static float[][] double2float(double[][] input) {
        if (input == null)
            return null;
        int rows = input.length;
        float[][] out = new float[rows][];
        // iterate over rows with conversion
        for (int r = 0; r < rows; r++) {
            double[] row = input[r];
            int cols = row.length;
            out[r] = new float[cols];
            // iterate over columns
            for (int c = 0; c < cols; c++) {
                out[r][c] = Double.valueOf((input[r][c])).floatValue();
            }
        }
        return out;
    }
    
    /**
     * Make deep copy of 3D array
     * 
     * @param source source matrix
     * @param dest destination matrix
     * @warning destination matrix must be initialized and has correct size
     */
    public static void copy2darray(double[][] source, double[][] dest) {
    	for(int r=0;r<source.length;r++)
    		System.arraycopy(source[r], 0, dest[r], 0, source[r].length);
    }
}
