/**
 * <h1>About</h1>
 * 
 * This plugin performs ANA analysis.
 * 
 * <h2>Prerequisites</h2>
 * 
 * <ol>
 * <li>Image to analyse - opened in IJ
 * <li>QCONF file or paQP file
 * </ol>
 * 
 * <h2>Macro support</h2>
 * 
 * Macro not supported.
 * 
 * <h3>Parameters</h3>
 * 
 * <h2>API support</h2>
 * 
 * API not supported or not tested.
 * 
 * <h1>Remarks</h1>
 * Fluorescence statistics are computed by
 * {@link uk.ac.warwick.wsbc.quimp.plugin.ana.ANA_#setFluoStats} method using inner and outer
 * contours. Outer contour is the result of the segmentation. Inner contour is the outer one shrank
 * by given distance. ImageJ method
 * {@link ij.process.ImageStatistics#getStatistics(ij.process.ImageProcessor, int, ij.measure.Calibration)}
 * is used for computing intensity statistics, saved later in <tt>stQP.csv</tt> output file. Inner
 * and outer contours are used to set ROIs within statistics are computed.
 * <ol>
 * <li><i>totalFluor</i> - mean intensity in outer contour * outer contour area. This is <b>not
 * scaled</b> to any unit.
 * <li><i>meanFluor</i> - mean intensity within outer contour
 * <li><i>innerArea</i> - area of inner contour - <b>scaled</b>
 * <li><i>totalInnerFluor</i> - mean intensity within inner contour * <b>unscaled</b> area of inner
 * contour
 * <li><i>meanInnerFluor</i> - mean intensity of inner contour.
 * <li><i>cortexArea</i> - scaled outer area - scaled inner area. Outer area is read from
 * <tt>stQP</tt>
 * file and it is computed by BOA on exit.
 * ({@link uk.ac.warwick.wsbc.quimp.Nest#analyse(ij.ImagePlus, boolean)} and then
 * {@link uk.ac.warwick.wsbc.quimp.CellStatsEval})
 * <li><i>totalCorFluo</i> - mean intensity in outer contour - mean intensity within inner contour
 * <li><i>meanCorFluo</i> - totalCorFluo / (outer contour area (not scaled) - inner contour area
 * (not scaled))
 * <li><i>percCortexFluo</i> - totalCorFluo / totalFluor * 100
 * <li><i>cortexWidth</i> - scaled width of the cortex.
 * </ol>
 * 
 * {@link uk.ac.warwick.wsbc.quimp.Outline} is supplemented with intensity data stored at
 * {@link uk.ac.warwick.wsbc.quimp.Vert#fluores}. Depending on UI settings
 * <tt>anap.sampleAtSame</tt>, intensity is sampled for coordinates from other channel or for inner
 * contour nodes resulting from ECMM mapping between outer contour and inner contour.
 * 
 * <p>For the first
 * case fluorescence is computed as mean of 9-point stencil located at {x,y} position (taken from
 * other channel).
 * 
 * <p>For the second case fluorescence data are computed by ECMM
 * {@link uk.ac.warwick.wsbc.quimp.plugin.ecmm.ECMM_Mapping#runByANA(uk.ac.warwick.wsbc.quimp.OutlineHandler, ij.process.ImageProcessor, double)}
 * by <tt>ODEsolver#euler</tt> from {@link uk.ac.warwick.wsbc.quimp.plugin.ecmm} package. As
 * previously 9-point stencil is used.
 * 
 * @author r.tyson
 * @author p.baniukiewicz
 *
 */
package uk.ac.warwick.wsbc.quimp.plugin.ana;
