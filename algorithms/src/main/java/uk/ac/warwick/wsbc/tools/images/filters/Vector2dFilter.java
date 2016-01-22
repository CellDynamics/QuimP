package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.List;

import javax.vecmath.Vector2d;

/**
 * Abstract class performs preparing vector2d for filtering.
 * 
 * Each vector coordinate is filtered separately
 * Usually vectors define polygons. Perform interpolation separately for X
 * and Y coordinates of vertices listed in order given in \c input list. 
 * Polygon should be defined in clock-wise or anticlock-wise direction.
 * 
 * @author baniuk
 *
 */
public abstract class Vector2dFilter implements IPoint2dFilter<Vector2d> {
	protected List<Vector2d> input; ///< reference to input list with coordinates
	protected double X[]; ///< extracted x coords from Vec2d
	protected double Y[]; ///< extracted y coords from Vec2d
	
	/**
	 * Default constructor converting vector2d to arrays
	 * 
	 * @param input list of vertices
	 */
	public Vector2dFilter(List<Vector2d> input) {
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

}
