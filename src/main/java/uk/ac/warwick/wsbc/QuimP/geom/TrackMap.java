/**
 * @file TrackMap.java
 * @date 15 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils;

/**
 * @author p.baniukiewicz
 *
 */
public class TrackMap {

    double[][] forwardMap;
    double[][] backwardMap;

    /**
     * Construct object.
     * 
     * Prepare tracking maps that are not stored in 
     * {@link uk.ac.warwick.wsbc.QuimP.DataContainer DataContainer}. This code is based on 
     * Matlab routine buildTrackMaps.m
     * 
     * @param originMap originMap stored in {@link uk.ac.warwick.wsbc.QuimP.DataContainer.QState QState}
     * @param coordMap coordMap stored in {@link uk.ac.warwick.wsbc.QuimP.DataContainer.QState QState}
     * @see uk.ac.warwick.wsbc.QuimP.DataContainer
     * @see uk.ac.warwick.wsbc.QuimP.STmap
     */
    public TrackMap(double[][] originMap, double[][] coordMap) {

        forwardMap = QuimPArrayUtils.initDoubleArray(originMap.length, originMap[0].length);
        backwardMap = QuimPArrayUtils.initDoubleArray(forwardMap.length, forwardMap[0].length);

        int rows = forwardMap.length;
        int cols = forwardMap[0].length;
        double[] minV = new double[3];
        double[] minI = new double[3];
        // backward map
        for (int i = 1; i < rows; i++)
            for (int j = 0; j < cols; j++) {
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
                backwardMap[i][j] = minI[(int) minMinV[1]]; // copy index of smallest among A,B,C
            }

        // forward map
        for (int i = 0; i < rows - 1; i++)
            for (int j = 0; j < cols; j++) {
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
                forwardMap[i][j] = minI[(int) minMinV[1]]; // copy index of smallest among A,B,C
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

}
