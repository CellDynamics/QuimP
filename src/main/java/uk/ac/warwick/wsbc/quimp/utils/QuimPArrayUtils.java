package uk.ac.warwick.wsbc.quimp.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;

// TODO: Auto-generated Javadoc
/**
 * Deliver simple methods operating on arrays.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimPArrayUtils {

  /**
   * Convert 2D float array to double.
   * 
   * @param input Array to convert
   * @return converted one
   */
  public static double[][] float2ddouble(float[][] input) {
    if (input == null) {
      return null;
    }
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
   * Calculate mean of map for every row.
   * 
   * @param map map
   * @return Mean of map for every row as list.
   */
  public static double[] getMeanR(double[][] map) {
    double[] ret = new double[map.length];
    for (int f = 0; f < map.length; f++) { // for every frame
      double mean = 0;
      for (int r = 0; r < map[f].length; r++) {
        mean += map[f][r];
      }
      ret[f] = mean / map[f].length;
    }
    return ret;
  }

  /**
   * Calculate mean of map for every column.
   * 
   * @param map map
   * @return Mean of map for every row as list.
   */
  public static double[] getMeanC(double[][] map) {
    double[] ret = new double[map[0].length];
    for (int r = 0; r < map[0].length; r++) { // for every frame
      double mean = 0;
      for (int f = 0; f < map.length; f++) {
        mean += map[f][r];
      }
      ret[r] = mean / map.length;
    }
    return ret;
  }

  /**
   * Calculate variance of map for every row.
   * 
   * @param map map
   * @return Variance of map for every row as list.
   */
  public static double[] getVarR(double[][] map) {
    double[] ret = new double[map.length];
    double[] means = getMeanR(map);
    for (int f = 0; f < map.length; f++) { // for every frame
      double var = 0;
      for (int r = 0; r < map[f].length; r++) {
        var += Math.pow(means[f] - map[f][r], 2.0);
      }
      ret[f] = var / map[f].length;
    }
    return ret;
  }

  /**
   * Remove duplicated elements from input list.
   * 
   * @param in in
   * @return New list without duplicates. Ordering may be different than in input list.
   */
  public static <T> List<T> removeDuplicates(List<T> in) {
    Set<T> retNoDpl = new HashSet<>(); // no duplicates allowed
    retNoDpl.addAll(in); // add if not present already
    ArrayList<T> retP2i = new ArrayList<>();
    retP2i.addAll(retNoDpl); // convert back to list
    return retP2i;
  }

  /**
   * reverse the given array in place.
   * 
   * @param input input
   */
  public static void reverseIntArray(int[] input) {
    // handling null, empty and one element array
    if (input == null || input.length <= 1) {
      return;
    }

    for (int i = 0; i < input.length / 2; i++) {
      int temp = input[i]; // swap numbers
      input[i] = input[input.length - 1 - i];
      input[input.length - 1 - i] = temp;
    }
  }

  /**
   * Convert 2D double array to float.
   * 
   * @param input Array to convert
   * @return converted one
   */
  public static float[][] double2float(double[][] input) {
    if (input == null) {
      return null;
    }
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
   * Convert integer array to short.
   * 
   * @param input input
   * @return Input array converted to short
   */
  public static short[] int2short(int[] input) {
    short[] ret = new short[input.length];
    for (int i = 0; i < input.length; i++) {
      if (input[i] > Short.MAX_VALUE) {
        throw new BufferOverflowException();
      }
      ret[i] = (short) input[i];
    }
    return ret;
  }

  /**
   * Create 2D array of doubles.
   * 
   * @param rows Number of rows
   * @param cols Number of columns
   * @return rows x cols array
   */
  public static double[][] initDoubleArray(int rows, int cols) {
    double[][] ret;
    ret = new double[rows][];
    for (int r = 0; r < rows; r++) {
      ret[r] = new double[cols];
    }
    return ret;
  }

  /**
   * Create 2D array of integers.
   * 
   * @param rows Number of rows
   * @param cols Number of columns
   * @return rows x cols array
   */
  public static int[][] initIntegerArray(int rows, int cols) {
    int[][] ret;
    ret = new int[rows][];
    for (int r = 0; r < rows; r++) {
      ret[r] = new int[cols];
    }
    return ret;
  }

  /**
   * Convert List of Integers to primitive type.
   * 
   * @param input input
   * @return array of ints converted from List
   */
  public static int[] listToPrimitive(List<Integer> input) {
    Iterator<Integer> it = input.iterator();
    int[] ret = new int[input.size()];
    int l = 0;
    while (it.hasNext()) {
      ret[l++] = it.next().intValue();
    }
    return ret;
  }

  /**
   * Convert primitive integer to Integer.
   * 
   * @param input input
   * @return array of Integers converted from array of ints
   */
  public static Integer[] primitiveToClass(int[] input) {
    Integer[] ret = new Integer[input.length];
    for (int i = 0; i < input.length; i++) {
      ret[i] = input[i];
    }
    return ret;
  }

  /**
   * Make deep copy of 2D array.
   * 
   * <p>If destination matrix i already initialized it must have correct size.
   * 
   * @param source source matrix
   * @param dest destination matrix, if <tt>dest</tt> is <tt>null</tt> the matrix is initialized
   *        in place.
   * @return copy of source matrix
   */
  public static double[][] copy2darray(double[][] source, double[][] dest) {
    double[][] ret;
    if (dest == null) {
      ret = new double[source.length][];
      for (int i = 0; i < source.length; i++) {
        ret[i] = new double[source[i].length];
      }
    } else {
      ret = dest;
    }
    for (int r = 0; r < source.length; r++) {
      System.arraycopy(source[r], 0, ret[r], 0, source[r].length);
    }
    return ret;
  }

  /**
   * Array to file.
   * 
   * <p>Can be imported e.g. in Matlab {@code image=importdata('/tmp/image.txt');}
   *
   * @param a the a
   * @param delim the delim
   * @param outFile the out file
   * @throws IOException Signals that an I/O exception has occurred.
   * @see #file2Array(String, File)
   */
  public static void arrayToFile(double[][] a, String delim, File outFile) throws IOException {
    PrintWriter pw = new PrintWriter(new FileWriter(outFile), true); // auto flush
    for (int i = 0; i < a.length; i++) {
      if (i != 0) {
        pw.write("\n");
      }
      pw.write(a[i][0] + "");
      for (int j = 1; j < a[0].length; j++) {
        pw.write(delim + a[i][j]);
      }
    }
    pw.close();
  }

  /**
   * Save RelaMatrix 2D to file.
   * 
   * <p>Use the code in Matlab:
   * {@code data = importdata('/tmp/testRealMatrix2D2File.txt')}
   * 
   * @param matrix matrix to save
   * @param outFile output file
   * @throws IOException on file error
   */
  public static void realMatrix2D2File(RealMatrix matrix, String outFile) throws IOException {
    if (matrix instanceof Array2DRowRealMatrix) {
      double[][] ref = ((Array2DRowRealMatrix) matrix).getDataRef();
      arrayToFile(ref, ",", new File(outFile));
    } else {
      throw new IllegalArgumentException("Input matrix should be instance of Array2DRowRealMatrix");
    }
  }

  /**
   * Load map file produced by arrayToFile.
   * 
   * <p>Array must have equal number of columns in every row.
   * 
   * @param delim Delimiter, should be the same used in arrayToFile
   * @param inFile inFile
   * @return loaded file as 2D array
   * @throws IOException on file error
   * @see #arrayToFile(double[][], String, File)
   */
  public static double[][] file2Array(String delim, File inFile) throws IOException {
    LineNumberReader pw = new LineNumberReader(new FileReader(inFile));
    int lines = getNumberOfLinesinFile(inFile); // get number of rows
    double[][] ret = new double[lines][];
    String line = pw.readLine();
    while (line != null) {
      StringTokenizer tk = new StringTokenizer(line, delim);
      ret[pw.getLineNumber() - 1] = new double[tk.countTokens()];
      int colno = 0;
      while (tk.hasMoreTokens()) {
        ret[pw.getLineNumber() - 1][colno++] = Double.valueOf(tk.nextToken());
      }
      line = pw.readLine();
    }
    pw.close();
    return ret;
  }

  /**
   * Return number of lines in file.
   * 
   * @param file file
   * @return Number of lines
   * @throws IOException on file error
   */
  public static int getNumberOfLinesinFile(File file) throws IOException {
    LineNumberReader lnr = new LineNumberReader(new FileReader(file));
    lnr.skip(Long.MAX_VALUE);
    int lines = lnr.getLineNumber() + 1;
    lnr.close();
    return lines;
  }

  /**
   * Find num peaks.
   *
   * @param data the data
   * @param peakWidth the peak width
   * @return the int
   */
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

  /**
   * Array max.
   *
   * @param a the a
   * @return the int
   */
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

  /**
   * Array max.
   *
   * @param a the a
   * @return the double
   */
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
   * Array max.
   *
   * @param a the a
   * @return the double
   */
  public static double arrayMax(double[][] a) {
    double max = arrayMax(a[0]);
    if (a.length == 1) {
      return max;
    }
    for (int i = 1; i < a.length; i++) {
      double rmax = arrayMax(a[i]);
      if (max < rmax) {
        max = rmax;
      }
    }
    return max;
  }

  /**
   * Find index of minimal element.
   * 
   * @param a array to search in
   * @return index of min(a)
   */
  public static int minArrayIndex(double[] a) {
    // find the index of the min
    double min = a[0];
    int imin = 0;
    if (a.length == 1) {
      return imin;
    }

    for (int i = 1; i < a.length; i++) {
      if (min > a[i]) {
        min = a[i];
        imin = i;
      }
    }
    return imin;
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
    int imin = 0;
    if (a.length == 1) {
      ret[0] = min;
      ret[1] = imin;
      return ret;
    }

    for (int i = 1; i < a.length; i++) {
      if (min > a[i]) {
        min = a[i];
        imin = i;
      }
    }
    ret[0] = min;
    ret[1] = imin;
    return ret;
  }

  /**
   * Array min.
   *
   * @param a the a
   * @return the double
   */
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

  /**
   * Array min.
   *
   * @param a the a
   * @return the double
   */
  public static double arrayMin(double[][] a) {
    double min = arrayMin(a[0]);
    if (a.length == 1) {
      return min;
    }
    for (int i = 1; i < a.length; i++) {
      double rmin = arrayMin(a[i]);
      if (min > rmin) {
        min = rmin;
      }
    }
    return min;
  }

  /**
   * Sum array.
   *
   * @param a the a
   * @return the int
   */
  public static int sumArray(int[] a) {
    int sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];
    }

    return sum;
  }

  /**
   * Prints the.
   *
   * @param a the a
   */
  public static void print(double[] a) {

    for (int i = 0; i < a.length; i++) {
      System.out.print("" + a[i] + "\n");
    }
    System.out.println("");
  }

  /**
   * Prints the.
   *
   * @param a the a
   */
  public static void print(double[][] a) {

    for (int i = 0; i < a.length; i++) {
      System.out.print("" + a[i][0] + " " + a[i][1] + "\n");
    }
  }

  /**
   * Get size of array across all dimensions.
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
   * 
   * <p>Assumes regular array.
   * 
   * @param array array
   * @param d value to fill with
   */
  public static <T> void fill2Darray(double[][] array, double d) {
    for (double[] row : array) {
      Arrays.fill(row, d);
    }
  }

  /**
   * Fill 2D array with given value.
   * 
   * <p>Assumes regular array.
   * 
   * @param array array
   * @param d value to fill with
   */
  public static <T> void fill2Darray(int[][] array, int d) {
    for (int[] row : array) {
      Arrays.fill(row, d);
    }
  }

  /**
   * Find index of minimal element in Collection.
   * 
   * @param a collection to search in
   * @return index of minimal element
   */
  public static <T extends Comparable<T>> int minListIndex(List<T> a) {
    return a.indexOf(Collections.min(a));
  }

  /**
   * Find maximum in 2D RealMatrix.
   * 
   * @param input Matrix to process
   * @return maximal value in input
   */
  public static double getMax(RealMatrix input) {
    double[][] data;
    if (input instanceof Array2DRowRealMatrix) {
      data = ((Array2DRowRealMatrix) input).getDataRef();
    } else {
      data = input.getData(); // optimize using visitors because this is copy
    }
    double[] maxs = new double[input.getRowDimension()];
    for (int r = 0; r < input.getRowDimension(); r++) {
      maxs[r] = StatUtils.max(data[r]);
    }
    return StatUtils.max(maxs);
  }

  /**
   * Find minimum in 2D RealMatrix.
   * 
   * @param input Matrix to process
   * @return minimal value in input
   */
  public static double getMin(RealMatrix input) {
    double[][] data;
    if (input instanceof Array2DRowRealMatrix) {
      data = ((Array2DRowRealMatrix) input).getDataRef(); // only available for non cache-friendly
    } else {
      data = input.getData(); // optimize using visitors because this is copy
    }
    double[] maxs = new double[input.getRowDimension()];
    for (int r = 0; r < input.getRowDimension(); r++) {
      maxs[r] = StatUtils.min(data[r]);
    }
    return StatUtils.min(maxs);
  }
}
