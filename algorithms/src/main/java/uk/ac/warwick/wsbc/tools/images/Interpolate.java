package uk.ac.warwick.wsbc.tools.images;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector2d;

import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * Class for interpolation of polygons defined by vertices.
 * 
 * Perform interpolation separately for X and Y coordinates of vertices 
 * listed in order given in \c input list. Polygon should be defined in 
 * clock-wise or anticlock-wise direction.
 * 
 * @author baniuk
 *
 */
public class Interpolate {
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
	 * @param f smoothing, useful range is from 0.03 - 0.1
	 * @return interpolated input data
	 */
	public List<Vector2d> getInterpolationLoess(double f)
	{
		LoessInterpolator sI = new LoessInterpolator(
				f,// f 0.03-0.1
				1, // W
				1.0E-15);
		double[] i = new double[input.size()];
		List<Vector2d> out = new ArrayList<Vector2d>();
		
		for(int ii=0;ii<input.size();ii++)
			i[ii] = ii;	// create linear indexes for X and Y
		PolynomialSplineFunction psfX = sI.interpolate(i, X);	// interpolation of X
		PolynomialSplineFunction psfY = sI.interpolate(i, Y);	// interpolation of Y
		for(float ii=0;ii<=input.size()-1;ii+=0.1f) {
			out.add(new Vector2d(
					psfX.value(ii),
					psfY.value(ii)));
		}
		return out;		
	}
	
}
