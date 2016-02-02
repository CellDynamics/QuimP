package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.plugin.snakefilter.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.plugin.utils.Vector2dFilter;

/**
 * Interpolation of points (X,Y) by means of Loess method
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 * @see William S. Cleveland - Robust Locally Weighted Regression and Smoothing Scatterplots
 *
 */
public class LoessFilter implements IQuimpPoint2dFilter<Vector2d> {
	private Vector2dFilter xyData; ///< input List converted to separate X and Y arrays
	private double smoothing; ///< smoothing value (f according to references)
	
	/**
	 * Create Loess filter.
	 * 
	 * @param input List of points to be filtered
	 * @param smoothing Smoothing parameter, usually in range 0.15-0.1. Smaller values 
	 * give less filtered shape.
	 */
	public LoessFilter(List<Vector2d> input, double smoothing) {
		xyData = new Vector2dFilter(input);
		this.smoothing = smoothing;
	}

	/**
	 * Run interpolation on X,Y vectors using LoessInterpolator
	 * 
	 * @return Filtered points as list of Vector2d objects
	 * @throws QuimpPluginException when:
	 *  - smoothing value is too small (usually below 0.15)
	 */
	@Override
	public List<Vector2d> runPlugin() throws QuimpPluginException {
		float density = 1.0f;	// If smaller than 1 output points will be refined. For 1 numbers of output points and input points are equal.  
		LoessInterpolator sI;
		double[] i = new double[xyData.size()]; // table of linear indexes that stand for x values for X,Y vectors (treated separately now)
		List<Vector2d> out = new ArrayList<Vector2d>(); // output interpolated data
		PolynomialSplineFunction psfX; // result of LoessInterpolator.interpolate for X and Y data
		PolynomialSplineFunction psfY;
		for(int ii=0;ii<xyData.size();ii++)
			i[ii] = ii;	// create linear indexes for X and Y
		try {
			sI = new LoessInterpolator(
				smoothing,// f 0.03-0.1
				1, // W
				1.0E-15);
			psfX = sI.interpolate(i, xyData.getX());	// interpolation of X
			psfY = sI.interpolate(i, xyData.getY());	// interpolation of Y
		} 
		catch (NumberIsTooSmallException e) {
				throw new QuimpPluginException("Smoothing value is too small",e); // change for checked exception and add cause
		}
		// copy to Vector2d List
		for(float ii=0;ii<=xyData.size()-1;ii+=density) {
			out.add(new Vector2d(
					psfX.value(ii),
					psfY.value(ii)));
		}
		return out;		
	}

	@Override
	public int setup() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPluginConfig(HashMap<String, Object> par) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getPluginConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attachData(Object data) {
		// TODO Auto-generated method stub
		
	}
}
