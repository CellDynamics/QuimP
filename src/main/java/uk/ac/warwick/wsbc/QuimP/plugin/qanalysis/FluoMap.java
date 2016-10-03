/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.warwick.wsbc.QuimP.plugin.qanalysis;

import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

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

    /**
     * Copy constructor
     * 
     * @param src source object
     */
    public FluoMap(final FluoMap src) {
        this.T = src.T;
        this.res = src.res;
        this.channel = src.channel;
        this.map = QuimPArrayUtils.copy2darray(src.map, null);
        this.fluColor = new byte[src.fluColor.length];
        System.arraycopy(src.fluColor, 0, this.fluColor, 0, src.fluColor.length);
        this.enabled = src.enabled;
    }

    public FluoMap(int t, int r, int i) {
        T = t;
        res = r;
        channel = i;
        enabled = true;
        map = new double[T][res];
        fluColor = new byte[T * res];
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void fill(int t, int p, int pN, double intensity, double max) {
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

    public byte[] getColours() {
        return fluColor;
    }

    public double[][] getMap() {
        return map;
    }
}
