package uk.ac.warwick.wsbc.tools.images;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector2d;

import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

/**
 * Class for interpolation of polygons defined by vertices.
 * 
 * Perform interpolation separately for X and Y coordinates of vertices 
 * listed in order given in \c input list. Polygon should be defined in 
 * clock-wise or anticlock-wise direction.
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 *
 */
public class Interpolate {
	static final int CIRCULARPAD = 1; ///< defines circular padding at getIndex(int, int)
	
	private List<Vector2d> input; ///< reference to input list with coordinates
	private double X[]; ///< extracted x coords from Vec2d
	private double Y[]; ///< extracted y coords from Vec2d
	
	/**
	 * Default constructor creating interpolate object
	 * 
	 * @param input list of vertices
	 */
	public Interpolate(List<Vector2d> input) {
		int i = 0;
		this.input = input;
		X = new double[input.size()];
		Y = new double[input.size()];
		for(Vector2d el : input) {
			X[i] = el.getX();
			Y[i] = el.getY();
			i++;
		}
	}
	
	/**
	 * Get interpolation using Loess interpolator
	 * 
	 * For smallest smoothing (lowest \c smooth) shape is still slightly influenced. For \c density=1 does
	 * not change number of shape points. 
	 * 
	 * @param f smoothing, useful range is from 0.015 - 0.1
	 * @return interpolated input data
	 * @throws InterpolateException when \c f is too small
	 * @remarks \c density variable might be important in future
	 */
	public List<Vector2d> getInterpolationLoess(double f) throws InterpolateException
	{
		float density = 1.0f;	// If smaller than 1 output points will be refined. For 1 numbers of output points and input points are equal.  
		LoessInterpolator sI;
		double[] i = new double[input.size()]; // table of linear indexes that stand for x values for X,Y vectors (treated separately now)
		List<Vector2d> out = new ArrayList<Vector2d>(); // output interpolated data
		PolynomialSplineFunction psfX; // result of LoessInterpolator.interpolate for X and Y data
		PolynomialSplineFunction psfY;
		for(int ii=0;ii<input.size();ii++)
			i[ii] = ii;	// create linear indexes for X and Y
		try {
			sI = new LoessInterpolator(
				f,// f 0.03-0.1
				1, // W
				1.0E-15);
			psfX = sI.interpolate(i, X);	// interpolation of X
			psfY = sI.interpolate(i, Y);	// interpolation of Y
		} 
		catch (NumberIsTooSmallException e) {
				throw new InterpolateException("Smoothing value is too small",e); // change for checked exception and add cause
		}
		// copy to Vector2d List
		for(float ii=0;ii<=input.size()-1;ii+=density) {
			out.add(new Vector2d(
					psfX.value(ii),
					psfY.value(ii)));
		}
		return out;		
	}
	
	/**
	 * Perform interpolation of data by a moving average filter with given window
	 * 
	 * By default uses \b CIRCULAR padding. The window must be uneven, positive and shorter
	 * than data vector. \c X and \c Y coordinates of points are smoothed separately. 
	 * 
	 * @param window Averaging window
	 * @return smoothed input data as List of the same size
	 * @throws InterpolateException when:
	 *  - window is even
	 *  - window is longer or equal processed data
	 *  - window is negative
	 */
	public List<Vector2d> getInterpolationMean(int window) throws InterpolateException
	{
		int cp = window/2; // left and right range of window
		double meanx = 0;
		double meany = 0;	// mean of window
		int indexTmp; // temporary index after padding
		List<Vector2d> out = new ArrayList<Vector2d>();
		
		if(window%2==0)
			throw new InterpolateException("Input argument must be uneven");
		if(window>=input.size())
			throw new InterpolateException("Processing window to long");
		if(window<0)
			throw new InterpolateException("Processing window is negative");
		
		for(int c=0;c<input.size();c++)	{	// for every point in data
			meanx = 0;
			meany = 0;
			for(int cc=c-cp;cc<=c+cp;cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2 (for window=5)
				indexTmp = getIndex(cc, Interpolate.CIRCULARPAD);
				meanx += X[indexTmp];
				meany += Y[indexTmp];				
			}
			meanx = meanx/window;
			meany = meany/window;
			out.add(new Vector2d(meanx,meany));
		}
		return out;
	}
	
	/**
	 * Helper method to pick values from X, Y arrays.
	 * 
	 * It accepts negative indexes as well as larger than X.size() and does in-place padding.
	 * Returns new proper index for array that accounts padding e.g. for input = -2 it returns last+2 
	 * if padding is \b circular
	 * 
	 * @param index Index of element to get
	 * @param mode Method of padding. Available are:
	 *  - \b CIRCULAR - as in Matlab padarray
	 * @return Proper index. If \c index is negative or larger than X,Y size returned value simulates padding.
	 * @remarks Do no check relations of window (provided \c index) to whole data size. May be unstable for certain cases.
	 */
	private int getIndex(int index, int mode) {
		
		int length = input.size();
		switch(mode) {
			case CIRCULARPAD:
				if(index<0)
					return(length+index); // for -1 points last element
				if(index>=length)
					return(index-length); // for after last points to first
				break;
			default:
				throw new IllegalArgumentException("Padding mode not supported");
		}
	
		return(index); // for all remaining cases
	}
	
}
