/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.warwick.wsbc.QuimP;

/**
 *
 * @author rtyson
 */

public class FluoMap {

    int T; // num of frames
    int res; // horizontal res
    int channel;
    double[][] map;
    byte[] fluColor;

    boolean enabled; // if no data switch false

    FluoMap(int t, int r, int i) {
        T = t;
        res = r;
        channel = i;
        enabled = true;
        map = new double[T][res];
        fluColor = new byte[T * res];
    }

    void setEnabled(boolean b) {
        enabled = b;
    }

    boolean isEnabled() {
        return enabled;
    }

    void fill(int t, int p, int pN, double intensity, double max) {
        // t - time, p- membrane pixel, pN-pixel index
        // System.out.println("fill with " + intensity);
        // if(intensity > 255 || intensity < 0){
        // IJ.log("Warning, Map value "+Math.round(intensity)+" out of 8-bit
        // range frame " + t);
        // intensity = 0d;
        // }

        map[t][p] = intensity;
        fluColor[pN] = (byte) QColor.bwScale(intensity, 256, max, 0); // don't
                                                                      // bother
                                                                      // scaling
        // fluColor[pN] = (byte) intensity;
    }

    byte[] getColours() {
        return fluColor;
    }

    double[][] getMap() {
        return map;
    }
}
