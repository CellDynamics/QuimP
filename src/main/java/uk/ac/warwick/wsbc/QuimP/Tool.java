/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.warwick.wsbc.QuimP;

// import jahuwaldt.plot.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// import jahuwaldt.tools.math.*;
// import javax.swing.*;
// import java.awt.*;

/**
 *
 * @author Richard
 */
public class Tool {

    /**
     * Gets QuimP version
     * 
     * @return Formatted string with QuimP version
     * @deprecated This method should not be used due to changes in QuimP versioning scheme
     */
    @Deprecated
    public static String getQuimPversion() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        return "QuimP11b-(25/05/14)";
    }

    public static void print(double[][] a) {

        for (int i = 0; i < a.length; i++) {
            System.out.print("" + a[i][0] + " " + a[i][1] + "\n");
        }
    }

    public static void print(double[] a) {

        for (int i = 0; i < a.length; i++) {
            System.out.print("" + a[i] + "\n");
        }
        System.out.println("");
    }

    public static double s2d(String s) {
        Double d;
        try {
            d = new Double(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        if (d != null) {
            return (d.doubleValue());
        } else {
            return (0.0);
        }
    }

    public static int sumArray(int[] a) {
        int sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i];

        return sum;
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

    public static String removeExtension(String filename) {
        // extract fileName without extension

        int dotI = filename.lastIndexOf(".");
        if (dotI > 0) {
            filename = filename.substring(0, dotI);
        }
        return filename;
    }

    public static String getFileExtension(String filename) {
        // extract fileName without extension

        int dotI = filename.lastIndexOf(".");
        if (dotI > 0) {
            filename = filename.substring(dotI + 1, filename.length());
        }
        return filename;
    }

    public static Vert closestFloor(Outline o, double target) {
        // find the vert with coor closest (floored) to target coordinate

        Vert v = o.getHead();
        double coordA, coordB;

        do {
            // coordA = v.fCoord;
            // coordB = v.getNext().fCoord;
            coordA = v.coord;
            coordB = v.getNext().coord;
            // System.out.println("A: " + coordA + ", B: "+ coordB);

            if ((coordA > coordB)) {
                if (coordA <= target && coordB + 1 > target) {
                    break;
                }

                if (coordA - 1 <= target && coordB > target) {
                    break;
                }

            } else {
                if (coordA <= target && coordB > target) {
                    break;
                }
            }
            v = v.getNext();
        } while (!v.isHead());

        return v;
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

    public static double distanceToScale(double value, double scale) {
        // assums pixelwidth is in micro meters
        return value * scale;
    }

    public static double areaToScale(double value, double scale) {
        // assums pixelwidth is in micro meters
        return value * (scale * scale);
    }

    public static double speedToScale(double value, double scale, double frameInterval) {
        return (value * scale) / frameInterval;
    }

    public static double distanceToScale(int value, double scale) {
        return value * scale;
    }

    public static double distanceFromScale(double value, double scale) {
        return value / scale;
    }

    public static double speedToScale(int value, double scale, double frameInterval) {
        return (value * scale) / frameInterval;
    }

    public static String dateAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public static double[] setLimitsEqual(double[] migLimits) { // min and max
        if (migLimits.length < 2) {
            System.out.println("Tool.237-Array to short. Needs a min and max");
            return migLimits;
        }
        // Set limits to equal positive and negative
        if (migLimits[1] < 0)
            migLimits[1] = -migLimits[0];
        if (migLimits[0] > 0)
            migLimits[0] = -migLimits[1];

        // Make min and max equal for mig and conv
        if (migLimits[0] < -migLimits[1]) {
            migLimits[1] = -migLimits[0];
        } else {
            migLimits[0] = -migLimits[1];
        }

        return migLimits;
    }

}
