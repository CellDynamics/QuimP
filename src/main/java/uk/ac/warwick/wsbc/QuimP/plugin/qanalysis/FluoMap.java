/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package uk.ac.warwick.wsbc.QuimP.plugin.qanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

// TODO: Auto-generated Javadoc
/**
 * Hold fluorescence map for given channel together with indexed colors.
 * 
 * @author rtyson
 */

public class FluoMap {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(FluoMap.class.getName());
    /**
     * Number of frames.
     */
    int T;
    /**
     * Horizontal resolution (points of outline)
     */
    int res;
    /**
     * Fluorescence channel kept in this object.
     */
    int channel;
    /**
     * Fluorescence map [time points][outline points].
     */
    double[][] map;
    /**
     * Colors for intensities stored in map.
     * 
     * 1D array is mapped to map[tt][p] as pN = (tt * res) + p;
     */
    byte[] fluColor;
    /**
     * If no data switch false.
     */
    boolean enabled;

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

    /**
     * @param t
     * @param r
     * @param i
     */
    public FluoMap(int t, int r, int i) {
        T = t;
        res = r;
        channel = i;
        enabled = true;
        map = new double[T][res];
        fluColor = new byte[T * res];
    }

    /**
     * @param b
     */
    public void setEnabled(boolean b) {
        enabled = b;
    }

    /**
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Put value to fluoromap calculating correct color.
     * 
     * @param t time
     * @param p membrane pixel
     * @param pN linear index for <tt>fluColor</tt>
     * @param intensity fluoroscence intensity to store at <tt>map</tt>
     * @param max value to scale to colors
     * @see #recalculateColorScale(double)
     */
    public void fill(int t, int p, int pN, double intensity, double max) {
        map[t][p] = intensity;
        fluColor[pN] = (byte) QColor.bwScale(intensity, 256, max, 0); // don't bother scaling
    }

    /**
     * Recalculate colors for <tt>map</tt> array.
     * 
     * @param max Value to scale to colors. Usually it is maximum of map.
     * @see #fill(int, int, int, double, double)
     */
    public void recalculateColorScale(double max) {
        LOGGER.debug("Recalculate fluColor, max=" + max + " enabled state:" + isEnabled());
        if (!isEnabled())
            return; // do not if not enabled (e.g. not initialized)
        int pN;
        double intensity;
        for (int r = 0; r < res; r++)
            for (int t = 0; t < T; t++) {
                pN = t * res + r;
                intensity = map[t][r];
                fluColor[pN] = (byte) QColor.bwScale(intensity, 256, max, 0);
            }

    }

    /**
     * 
     * @return fluColor
     */
    public byte[] getColours() {
        return fluColor;
    }

    /**
     * 
     * @return map
     */
    public double[][] getMap() {
        return map;
    }

    /**
     * Set new fluorescence map calculating also adequate colorscale. Set this map enabled.
     * 
     * @param map the map to set
     */
    public void setMap(double[][] map) {
        this.map = map;
        recalculateColorScale(QuimPArrayUtils.arrayMax(map));
        setEnabled(true);
    }
}
