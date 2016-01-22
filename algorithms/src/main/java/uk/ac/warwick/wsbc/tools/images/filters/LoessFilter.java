package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Vector2d;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

import uk.ac.warwick.wsbc.tools.images.FilterException;

/**
 * Interpolation of points (X,Y) by means of Loess method
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 * @see William S. Cleveland - Robust Locally Weighted Regression and Smoothing Scatterplots
 *
 */
public class LoessFilter extends Vector2dFilter {

	private double smoothing; ///< smoothing value (f according to references)
	
	/**
	 * Create Loess filter.
	 * 
	 * @param input List of points to be filtered
	 * @param smoothing Smoothing parameter, usually in range 0.15-0.1. Smaller values 
	 * give less filtered shape.
	 */
	public LoessFilter(List<Vector2d> input, double smoothing) {
		super(input);
		toArrays(); // this algorithm needs access to coordinates separately
		this.smoothing = smoothing;
	}

	/**
	 * Run interpolation on X,Y vectors using LoessInterpolator
	 * 
	 * @return Filtered points as list of Vector2d objects
	 * @throws FilterException when:
	 *  - smoothing value is too small (usually below 0.15)
	 */
	@Override
	public Collection<Vector2d> RunFilter() throws FilterException {
		float density = 1.0f;	// If smaller than 1 output points will be refined. For 1 numbers of output points and input points are equal.  
		LoessInterpolator sI;
		double[] i = new double[points.size()]; // table of linear indexes that stand for x values for X,Y vectors (treated separately now)
		List<Vector2d> out = new ArrayList<Vector2d>(); // output interpolated data
		PolynomialSplineFunction psfX; // result of LoessInterpolator.interpolate for X and Y data
		PolynomialSplineFunction psfY;
		for(int ii=0;ii<points.size();ii++)
			i[ii] = ii;	// create linear indexes for X and Y
		try {
			sI = new LoessInterpolator(
				smoothing,// f 0.03-0.1
				1, // W
				1.0E-15);
			psfX = sI.interpolate(i, X);	// interpolation of X
			psfY = sI.interpolate(i, Y);	// interpolation of Y
		} 
		catch (NumberIsTooSmallException e) {
				throw new FilterException("Smoothing value is too small",e); // change for checked exception and add cause
		}
		// copy to Vector2d List
		for(float ii=0;ii<=points.size()-1;ii+=density) {
			out.add(new Vector2d(
					psfX.value(ii),
					psfY.value(ii)));
		}
		return out;		
	}

}
