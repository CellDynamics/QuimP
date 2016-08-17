/**
 * @file TrackMap.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * Compute forward and backward tracking maps from origin and coordinates maps. 
 * 
 * Origin and coordinates maps are stored in both <i>QCONF</i> and <i>paQP</i> data files.
 * Allow to track given outline point forward in backward using these maps.
 * 
 * @author p.baniukiewicz
 * @see {@link uk.ac.warwick.wsbc.QuimP.geom.TrackMapTest}
 *
 */
public class TrackMap {

    /**
     * Decides whether include starting point in tracking. By default Matlab procedures do
     * not include it. Therefore, trackXX(int, int, int) returns first tracked point AFTER initial
     * one.
     */
    public boolean includeFirst = false;

    int[][] forwardMap;
    int[][] backwardMap;

    /**
     * Number of rows in <i>Map</i> - equals to number of frames.
     */
    int rowsFrames;
    /**
     * Number of columns in <i>Map</i> - equals to number of outline points set by resolution in
     * Q Analysis.
     */
    int colsIndexes;

    /**
     * Construct tracking maps.
     * 
     * Prepare tracking maps that are not stored by default in 
     * {@link uk.ac.warwick.wsbc.QuimP.DataContainer DataContainer}. This code is based on 
     * Matlab routine buildTrackMaps.m. 
     * 
     * <p><b>Note</b><p>
     * All frames are numbered from 0 as well as outline indexes.
     * Nonexisting indexes are marked as -1.
     *  
     * @param originMap originMap stored in {@link uk.ac.warwick.wsbc.QuimP.DataContainer.QState QState}
     * @param coordMap coordMap stored in {@link uk.ac.warwick.wsbc.QuimP.DataContainer.QState QState}
     * @see uk.ac.warwick.wsbc.QuimP.DataContainer
     * @see uk.ac.warwick.wsbc.QuimP.STmap
     */
    public TrackMap(double[][] originMap, double[][] coordMap) {

        forwardMap = QuimPArrayUtils.initIntegerArray(originMap.length, originMap[0].length);
        backwardMap = QuimPArrayUtils.initIntegerArray(forwardMap.length, forwardMap[0].length);

        QuimPArrayUtils.fill2Darray(forwardMap, -1);
        QuimPArrayUtils.fill2Darray(backwardMap, -1);

        rowsFrames = forwardMap.length;
        colsIndexes = forwardMap[0].length;
        double[] minV = new double[3];
        double[] minI = new double[3];
        // backward map
        for (int i = 1; i < rowsFrames; i++)
            for (int j = 0; j < colsIndexes; j++) {
                double p = originMap[i][j];
                double[] diffA = rowDiff(p, coordMap[i - 1]);
                double[] diffB = rowDiff(p, rowAdd(-1, coordMap[i - 1]));
                double[] diffC = rowDiff(p, rowAdd(+1, coordMap[i - 1]));

                double[] minDiffA = QuimPArrayUtils.minArrayIndexElement(diffA);
                double[] minDiffB = QuimPArrayUtils.minArrayIndexElement(diffB);
                double[] minDiffC = QuimPArrayUtils.minArrayIndexElement(diffC);

                // copy min values to array
                minV[0] = minDiffA[0];
                minV[1] = minDiffB[0];
                minV[2] = minDiffC[0];
                minI[0] = minDiffA[1];
                minI[1] = minDiffB[1];
                minI[2] = minDiffC[1];
                double[] minMinV = QuimPArrayUtils.minArrayIndexElement(minV);
                backwardMap[i][j] = (int) minI[(int) minMinV[1]]; // copy index of smallest among
                                                                  // A,B,C
            }

        // forward map
        for (int i = 0; i < rowsFrames - 1; i++)
            for (int j = 0; j < colsIndexes; j++) {
                double p = coordMap[i][j];
                double[] diffA = rowDiff(p, originMap[i + 1]);
                double[] diffB = rowDiff(p, rowAdd(-1, originMap[i + 1]));
                double[] diffC = rowDiff(p, rowAdd(+1, originMap[i + 1]));

                double[] minDiffA = QuimPArrayUtils.minArrayIndexElement(diffA);
                double[] minDiffB = QuimPArrayUtils.minArrayIndexElement(diffB);
                double[] minDiffC = QuimPArrayUtils.minArrayIndexElement(diffC);

                // copy min values to array
                minV[0] = minDiffA[0];
                minV[1] = minDiffB[0];
                minV[2] = minDiffC[0];
                minI[0] = minDiffA[1];
                minI[1] = minDiffB[1];
                minI[2] = minDiffC[1];
                double[] minMinV = QuimPArrayUtils.minArrayIndexElement(minV);
                forwardMap[i][j] = (int) minI[(int) minMinV[1]]; // copy index of smallest among
                                                                 // A,B,C
            }
    }

    /**
     * Compute <tt>p</tt>-<tt>row</tt>.
     * <p>
     * Based on Matlab routine buildTrackMaps.m
     * 
     * @param p
     * @param row
     * @return Vector of <tt>p</tt>-<tt>row</tt> as a copy.
     */
    private double[] rowDiff(double p, double[] row) {
        double[] cpy = new double[row.length];
        for (int i = 0; i < cpy.length; i++)
            cpy[i] = Math.abs(p - row[i]);
        return cpy;
    }

    /**
     * Add value <tt>val</tt> to vector <tt>row</tt>
     * <p>
     * Based on Matlab routine buildTrackMaps.m
     * 
     * @param val Value to add
     * @param row Input matrix
     * @return <tt>row</tt>+<tt>val</tt> as a copy.
     */
    private double[] rowAdd(double val, double[] row) {
        double[] cpy = new double[row.length];
        System.arraycopy(row, 0, cpy, 0, row.length);
        for (int i = 0; i < cpy.length; i++)
            cpy[i] += val;
        return cpy;
    }

    /**
     * Get position of <tt>membraneIndex</tt> on frame <tt>currentFrame+1</tt>.
     * 
     * @param currentFrame frame number, counted from 0
     * @param membraneIndex index of point on membrane on frame <tt>currentFrame</tt>
     * @return corresponding index on next frame. Returns -1 when there is neither next frame nor
     * index.
     */
    public int getNext(int currentFrame, int membraneIndex) {
        if (currentFrame + 1 >= rowsFrames || membraneIndex >= colsIndexes)
            return -1;
        return forwardMap[currentFrame][membraneIndex];
    }

    /**
     * Get position of <tt>membraneIndex</tt> on frame <tt>currentFrame-1</tt>.
     * 
     * @param currentFrame frame number, counted from 0
     * @param membraneIndex index of point on membrane on frame <tt>currentFrame</tt>
     * @return corresponding index on previous frame. Returns -1 when there is neither next frame 
     * nor index.
     */
    public int getPrev(int currentFrame, int membraneIndex) {
        if (currentFrame - 1 < 0 || membraneIndex >= colsIndexes)
            return -1;
        return backwardMap[currentFrame][membraneIndex];
    }

    /**
     * Track given point forward.
     * 
     * @param currentFrame Starting frame (not included in results - depends on <tt>includeFirst</tt>
     * flag)
     * @param membraneIndex Tracked membrane index
     * @param timeSpan Number of frames to track
     * @return Indexes of point <tt>membraneIndex</tt> in frames <tt>currentFrame+1</tt> to 
     * <tt>currentFrame+timeSpan</tt>
     */
    public int[] trackForward(int currentFrame, int membraneIndex, int timeSpan) {
        if (includeFirst)
            timeSpan++;
        int[] ret = new int[timeSpan];
        if (includeFirst)
            ret[0] = membraneIndex;
        else
            ret[0] = getNext(currentFrame, membraneIndex);
        for (int t = 1; t < timeSpan; t++)
            ret[t] = getNext(currentFrame + t, (int) ret[t - 1]);
        return ret;
    }

    /**
     * Track given point backward.
     * 
     * @param currentFrame Starting frame (not included in results - depends on <tt>includeFirst</tt>
     * flag)
     * @param membraneIndex Tracked membrane index
     * @param timeSpan Number of frames to track
     * @return Indexes of point <tt>membraneIndex</tt> in frames <tt>currentFrame-1</tt> to 
     * <tt>currentFrame-timeSpan</tt>
     */
    public int[] trackBackward(int currentFrame, int membraneIndex, int timeSpan) {
        if (includeFirst)
            timeSpan++;
        int[] ret = new int[timeSpan];
        if (includeFirst)
            ret[timeSpan - 1] = membraneIndex;
        else
            ret[timeSpan - 1] = getPrev(currentFrame, membraneIndex);
        for (int t = timeSpan - 2; t >= 0; t--)
            ret[t] = getPrev(currentFrame - (timeSpan - t - 1), (int) ret[t + 1]);
        return ret;
    }

    /**
     * Helper that generates range of frames for given input parameters.
     * 
     * These are frames that {@link trackForward(int, int, int)} returns indexes for.
     * Input parameters must be the same as for {@link trackForward(int, int, int)}.
     * 
     * @param currentFrame Starting frame (not included in results)
     * @param timeSpan timeSpan Number of frames to track
     * @return Array of frame numbers
     */
    public int[] getForwardFrames(int currentFrame, int timeSpan) {
        int f;
        if (includeFirst)
            timeSpan++;
        int[] ret = new int[timeSpan];
        if (includeFirst)
            f = currentFrame;
        else
            f = currentFrame + 1;
        int l = 0;
        do {
            ret[l++] = f++;
        } while (l < timeSpan);
        return ret;
    }

}
