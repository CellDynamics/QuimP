package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.tools.images.FilterException;

public class HatFilter extends Vector2dFilter implements IPadArray {

	private int window;
	private int crown;
	private double sig;
	private static final Logger logger = LogManager.getLogger(HatFilter.class.getName());
	/**
	 * 
	 * @param input
	 * @param window
	 * @param crown
	 * @param sig
	 */
	public HatFilter(List<Vector2d> input, int window, int crown, double sig) {
		super(input);
		this.window = window;
		this.crown = crown;
		this.sig = sig;
	}

	@Override
	public Collection<Vector2d> RunFilter() throws FilterException {
		int cp = window/2; // left and right range of window
		int indexTmp; // temporary index after padding
		int liczW = 0; // window counter
		int crownLBound = (window-crown)/2; // lower index of crown (included) - local for window array V
		int crownUBound = crownLBound + crown - 1; // upper index of crown (included) - local for window array V
		double lenAll; // length of curve in window
		double lenBrim; // length of curve in window without crown
		Set<Integer> indToRemove = new HashSet<Integer>();
		List<Vector2d> out = new ArrayList<Vector2d>(); // output table
		
		// check input conditions
		if(window%2==0 || crown%2==0)
			throw new FilterException("Input arguments must be uneven");
		if(window>=points.size() || crown>=points.size())
			throw new FilterException("Processing window or crown to long");
		if(window<0 || crown<0)
			throw new FilterException("Input arguments must be positive");
		if(crown>=window)
			throw new FilterException("Crown can not be larger or equal to window");
		if(window<3)
			throw new FilterException("Window should be larger than 2");
		
		Vector2d V[] = new Vector2d[window]; // temporary array for holding content of window [v1 v2 v3 v4 v5 v6 v7]
		Vector2d B[] = new Vector2d[window-crown]; //array for holding brim only points  [v1 v2 v6 v7]
		
		for(int c=0;c<points.size();c++)	{	// for every point in data
			liczW = 0;
			lenAll = 0;
			lenBrim = 0;
			for(int cc=c-cp;cc<=c+cp;cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2 (for window=5)
				indexTmp = IPadArray.getIndex(points.size(), cc, IPadArray.CIRCULARPAD);
				V[liczW] = points.get(indexTmp); // store window content
				if(indexTmp<crownLBound || indexTmp>crownUBound) //FIXME bug here in case of padding
					B[liczW] = points.get(indexTmp); // store only brim
				liczW++;
			}
			
			// converting node points to vectors between them
			// for temporary table V/B {n0 n1 n2 n3 ...} calculate vectors {n1-n0 n2-n1 ....}
			// results are stored in the same V/B but now V/B are valid to V/B.length-1
			for(int i=0;i<V.length-1;i++)
				V[i].sub(V[i+1]);
			for(int i=0;i<B.length-1;i++)
				B[i].sub(B[i+1]);
			// calculate lengths
			for(int i=0;i<V.length-1;i++)
				lenAll += V[i].length();
			for(int i=0;i<B.length-1;i++)
				lenBrim += B[i].length();
			// decide whether remove crown
			double ratio = 1 - lenBrim/lenAll;
			if(ratio>sig) // add crown for current window position c to remove list. Added are real indexes in points array (not local window indexes)
				for(int i=crownLBound+(c-cp);i<=crownUBound+(c-cp);i++) //FIXME check this loop
					indToRemove.add(i); // add only if not present in set
			//TODO add removing from points array
		}
		
		return null;
	}

}
