/**
 * @file QuimPArrayUtils.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
     * Create 2D array of doubles.
     * @param rows Number of rows
     * @param cols Number of columns
     * @return rows x cols array
     */
    public static double[][] initDoubleArray(int rows, int cols) {
        double[][] ret;
        ret = new double[rows][];
        for (int r = 0; r < rows; r++)
            ret[r] = new double[cols];
        return ret;
    }

    /**
     * Create 2D array of integers.
     * @param rows Number of rows
     * @param cols Number of columns
     * @return rows x cols array
     */
    public static int[][] initIntegerArray(int rows, int cols) {
        int[][] ret;
        ret = new int[rows][];
        for (int r = 0; r < rows; r++)
            ret[r] = new int[cols];
        return ret;
    }

    /**
     * Make deep copy of 2D array
     * <p>
     * If destination matrix i already initialized it must have correct size.
     * 
     * @param source source matrix
     * @param dest destination matrix, if <tt>dest</tt> is <tt>null</tt> the matrix is initialized 
     * in place.
     * @return copy of source matrix
     */
    public static double[][] copy2darray(double[][] source, double[][] dest) {
        double[][] ret;
        if (dest == null) {
            ret = new double[source.length][];
            for (int i = 0; i < source.length; i++)
                ret[i] = new double[source[i].length];
        } else
            ret = dest;
        for (int r = 0; r < source.length; r++)
            System.arraycopy(source[r], 0, ret[r], 0, source[r].length);
        return ret;
    }

    public static void arrayToFile(double[][] a, String delim, File outFile) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto flush
        for (int i = 0; i < a.length; i++) {
            if (i != 0)
                pw.write("\n");
            pw.write(a[i][0] + "");
            for (int j = 1; j < a[0].length; j++) {
                pw.write("," + a[i][j]);
            }
        }
        pw.close();
    }

    public static int findNumPeaks(double[] data, int peakWidth) {

        int[] peaks = new int[data.length];
        int flag;

        // find local peak points
        for (int i = peakWidth; i < data.length - peakWidth; i++) {
            flag = 0;
            peaks[i] = 0;
            for (int j = -peakWidth; j <= peakWidth; j++) {
                if (data[i + j] > data[i]) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                peaks[i] = 1;
                // System.out.println("peak at " + i);
            }
        }

        // remove consecutive points (i.e. in flat areas)
        int realPeaks = 0;
        for (int i = 0; i < peaks.length; i++) {
            if (peaks[i] == 1) {
                realPeaks++;
                if (peaks[i + 1] == 1) {
                    realPeaks--;
                }
            }
        }

        return realPeaks;
    }

    public static int arrayMax(int[] a) {
        int max = a[0];
        if (a.length == 1) {
            return max;
        }

        for (int i = 1; i < a.length; i++) {
            if (max < a[i]) {
                max = a[i];
            }
        }
        return max;
    }

    public static double arrayMax(double[] a) {
        double max = a[0];
        if (a.length == 1) {
            return max;
        }

        for (int i = 1; i < a.length; i++) {
            if (max < a[i]) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Find index of minimal element
     * 
     * @param a array to search in
     * @return index of min(a)
     */
    public static int minArrayIndex(double[] a) {
        // find the index of the min
        double min = a[0];
        int iMin = 0;
        if (a.length == 1) {
            return iMin;
        }

        for (int i = 1; i < a.length; i++) {
            if (min > a[i]) {
                min = a[i];
                iMin = i;
            }
        }
        return iMin;
    }

    /**
     * Find index of minimal element and the element itself.
     * 
     * @param a array to search in
     * @return [min,index]
     */
    public static double[] minArrayIndexElement(double[] a) {
        // find the index of the min
        double[] ret = new double[2];
        double min = a[0];
        int iMin = 0;
        if (a.length == 1) {
            ret[0] = min;
            ret[1] = iMin;
            return ret;
        }

        for (int i = 1; i < a.length; i++) {
            if (min > a[i]) {
                min = a[i];
                iMin = i;
            }
        }
        ret[0] = min;
        ret[1] = iMin;
        return ret;
    }

    public static double arrayMin(double[] a) {
        double min = a[0];
        if (a.length == 1) {
            return min;
        }

        for (int i = 1; i < a.length; i++) {
            if (min > a[i]) {
                min = a[i];
            }
        }
        return min;
    }

    public static int sumArray(int[] a) {
        int sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i];

        return sum;
    }

    public static void print(double[] a) {

        for (int i = 0; i < a.length; i++) {
            System.out.print("" + a[i] + "\n");
        }
        System.out.println("");
    }

    public static void print(double[][] a) {

        for (int i = 0; i < a.length; i++) {
            System.out.print("" + a[i][0] + " " + a[i][1] + "\n");
        }
    }

    /**
     * 
     * @param object Array
     * @return Size of array <tt>a</tt>
     */
    public static int getArraySize(Object object) {
        if (!object.getClass().isArray()) {
            return 1;
        }
        int size = 0;
        for (int i = 0; i < Array.getLength(object); i++) {
            size += getArraySize(Array.get(object, i));
        }
        return size;
    }

    /**
     * Fill 2D array with given value.
     * <p>
     * Assumes regular array.
     * 
     * @param forwardMap
     * @param d
     */
    public static <T> void fill2Darray(double[][] array, double d) {
        for (double[] row : array)
            Arrays.fill(row, d);
    }

    /**
     * Fill 2D array with given value.
     * <p>
     * Assumes regular array.
     * 
     * @param forwardMap
     * @param d
     */
    public static <T> void fill2Darray(int[][] array, int d) {
        for (int[] row : array)
            Arrays.fill(row, d);
    }

    /**
     * Find index of minimal element in Collection
     * 
     * @param a collection to search in
     * @return index of minimal element
     */
    public static <T extends Comparable<T>> int minListIndex(List<T> a) {
        return a.indexOf(Collections.min(a));
    }
}
