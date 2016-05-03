/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.warwick.wsbc.QuimP;

import javax.vecmath.Color3f;

/**
 * Just a class for colors, that does not use stupid floats represented as RGB
 * in the range 0->1
 *
 * @author tyson
 */
public class QColor {
    public double red, green, blue;

    /**
     * Copy constructor 
     * @param src object to copy
     */
    public QColor(final QColor src) {
        red = src.red;
        green = src.green;
        blue = src.blue;
    }

    public QColor(double r, double g, double b) {
        this.setRGB(r, g, b);
    }

    public void print() {
        System.out.println("R:" + red + " G: " + green + " B: " + blue);
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(blue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(green);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(red);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof QColor))
            return false;
        QColor other = (QColor) obj;
        if (Double.doubleToLongBits(blue) != Double.doubleToLongBits(other.blue))
            return false;
        if (Double.doubleToLongBits(green) != Double.doubleToLongBits(other.green))
            return false;
        if (Double.doubleToLongBits(red) != Double.doubleToLongBits(other.red))
            return false;
        return true;
    }

    public void setRGB(double r, double g, double b) {
        red = r;
        blue = b;
        green = g;

        if (red > 1 || red < 0 || green > 1 || green < 0 || blue > 1 || blue < 0) {
            System.out.println("32-Warning! Colour out of range:");
            this.print();
            red = 0;
            blue = 0;
            green = 0;
            System.out.flush();
            Exception e = new Exception();
            e.printStackTrace();
        }

    }

    public String getColorSVG() {
        return "rgb(" + (red * 100) + "%," + (green * 100) + "%," + (blue * 100) + "%)";
    }

    public double getRed() {
        return red;
    }

    public double getGreen() {
        return green;
    }

    public double getBlue() {
        return blue;
    }

    public int getRed(int bits) {
        // returns the color with a color space of size 'bits'
        return (int) Math.round(red * bits);
    }

    public int getGreen(int bits) {
        // returns the color with a color space of size 'bits'
        return (int) Math.round(green * bits);
    }

    public int getBlue(int bits) {
        // returns the color with a color space of size 'bits'
        return (int) Math.round(blue * bits);
    }

    public int getColorInt() {
        return getRed(255) * 65536 + getGreen(255) * 256 + getBlue(255);
    }

    public static Color3f colorInt23f(int c) {
        float blueF = c % 256;
        int s = (int) (c / 256);
        float greenF = s % 256;
        s = (int) (c / 256) / 256;
        float redF = s % 256;

        redF = redF / 255;
        greenF = greenF / 255;
        blueF = blueF / 255;

        return new Color3f(redF, greenF, blueF);
    }

    public int getColorBW(int bits) {
        return (int) Math.round(((red + green + blue) / 3) * bits);
    }

    // static methods *************

    public static String[] colourMaps = { "Summer", "Cool", "Hot", "Grey" };

    public static QColor ERColorMap2(String c, double d, double min, double max) {
        if (c.equals("rwb")) {
            return RWBmap(d, max, min);
        } else if (c.equals("rbb")) {
            return RBBmap(d, max, min);
        } else {
            return RWBmap(d, max, min);
        }
    }

    public static QColor RWBmap(double d, double max, double min) {
        double r = 1;
        double g = 1;
        double b = 1;

        if (d == 0) {
            return new QColor(1.0, 1.0, 1.0);
        } else if (d > 0) { // red
            r = 1;

            double p = (1 / max) * d;
            if (p > 0.5) {
                g = (1 - p) * 2;
                b = 0;
            } else {
                g = 1;
                b = (0.5 - p) * 2;
            }

            return new QColor(r, g, b);
        } else { // blu
            b = 1;

            double p = (1 / min) * d;

            if (p > 0.5) {
                g = (1 - p) * 2;
                r = 0;
            } else {
                g = 1;
                r = (0.5 - p) * 2;
            }

            return new QColor(r, g, b);
        }

    }

    private static QColor RBBmap(double d, double max, double min) {
        double r = 0;
        double g = 0;
        double b = 0;

        if (d > max || d < min) {
            System.out.println(
                    "Qcolor 141: d(" + d + ") not in min(" + min + ") or max(" + max + ")");
            System.out.flush();
            Exception e = new Exception();
            e.printStackTrace();
        }

        if (d == 0) { // black
            return new QColor(0.0, 0.0, 0.0);
        } else if (d > 0) { // red
            double p = (1 / max) * d;

            if (p >= 0.5) {
                g = (p - 0.5) * 2;
                r = 1;
            } else {
                g = 0;
                r = p * 2;
            }

            return new QColor(r, g, b);
        } else { // blu

            double p = (1 / min) * d;

            if (p >= 0.5) {
                g = (p - 0.5) * 2;
                b = 1;
            } else {
                g = 0;
                b = p * 2;
            }
            return new QColor(r, g, b);
        }

    }

    static int RBBlut(byte[] reds, byte[] greens, byte[] blues) {
        int[] r = { 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 };
        int[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161,
                175, 190, 205, 219, 234, 248, 255, 255, 255, 255 };
        int[] b = { 0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255 };
        for (int i = 0; i < r.length; i++) {
            reds[i] = (byte) r[i];
            greens[i] = (byte) g[i];
            blues[i] = (byte) b[i];
        }
        return r.length;
    }

    public static int bwScale(double value, int bits, double max, double min) {
        if (value > max || value < min) {
            // System.out.println("Warning! bwScale value not in range:
            // "+value+" (min:"+ min +", max:" + max+")");
            Exception e = new Exception();
            e.printStackTrace();
            System.exit(1);
            return 0;
        }

        return (int) Math.round((value - min) * ((bits - 1) / (max - min)));
        // if(value==255)System.out.println("value: " + value+", min:" + min +
        // ", max:" + max+", bits:" + bits+",temp: "+temp+", answ: " + answ);

    }

    /**
     * Create a color map
     * 
     * @param c choice of color map: "Summer"
     * @param size size of the color map
     * @return QColor array of length 'size'
     */
    public static QColor[] colourMap(String c, int size) {
        QColor[] map = new QColor[size];
        double r, b, g;

        if (c.equals("Summer")) {
            r = 0.;
            g = 0.5;
            b = 0.4;
            map[0] = new QColor(r, g, b);

            double dr = 1d / (double) size;
            double dg = 0.5d / (double) size;

            for (int i = 1; i < size; i++) {
                r += dr;
                g += dg;
                map[i] = new QColor(r, g, b);
            }
        } else if (c.equals("Cool")) {
            r = 0.;
            b = 0.5;
            g = 0.4;
            map[0] = new QColor(r, g, b);

            double dr = 1d / (double) size;
            double db = 0.5d / (double) size;

            for (int i = 1; i < size; i++) {
                r += dr;
                b += db;
                map[i] = new QColor(r, g, b);
            }
        } else if (c.equals("Hot")) {
            // based on matlab code for Hot
            r = 0.2d;
            g = 0d;
            b = 0;
            map[0] = new QColor(r, g, b);

            double n = Math.floor(3d / 8d * size);

            for (int i = 0; i < size; i++) {

                r = (i + 1) / n;
                if (r > 1)
                    r = 1d;

                if (i < n)
                    g = 0d;
                else
                    g = (i - n + 1) / n;
                if (g > 1)
                    g = 1d;

                if (i < 2 * n)
                    b = 0d;
                else
                    b = (1 + i - 2 * n) / (size - (2 * n));
                if (b > 1)
                    b = 1d;
                map[i] = new QColor(r, g, b);
            }

        } else if (c.equals("Grey")) {
            r = 0.1d;
            g = 0.1d;
            b = 0.1d;
            map[0] = new QColor(r, g, b);

            double d = 0.9d / (double) size;

            for (int i = 1; i < size; i++) {
                r += d;
                g += d;
                b += d;
                map[i] = new QColor(r, g, b);
            }

        } else {
            for (int i = 0; i < size; i++) {
                map[i] = new QColor(0.5, 0.5, 0.5);
            }
            System.out.println("warning: unknown color map: " + c);
        }

        return map;
    }

    /**
     * Get a random light color
     * 
     * @return
     */
    public static QColor lightColor() {
        double r = (Math.random() / 1.3) + 0.23;
        double g = (Math.random() / 1.3) + 0.23;
        double b = (Math.random() / 1.3) + 0.23;

        return new QColor(r, g, b);
    }

}
