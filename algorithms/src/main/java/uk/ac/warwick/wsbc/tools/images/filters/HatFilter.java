package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.tools.images.FilterException;

/**
 * Implementation of HatFilter for removing convexities from polygon
 * 
 * This filter run mask of size \b M along path defined by vertexes on 2D plane.
 * The mask \b M contains smaller inner part called crown \b C. There is always
 * relation that \b C < \b M and \b M and \b C are uneven. For example for \b M=9 
 * and \b C=5 the mask is: \c MMCCCCCMM.
 * For every position \a i of mask \b M on path two distances are calculated:
 *  -# the distance \a dM that is total length of path covered by \b M (sum of lengths
 *  of vectors between vertexes V(i+1) and V(i) for all i included in \b M
 *  -# the distance \a dC that is total length of curve with \b removed points from
 *  crown \b C.
 *  
 * For straight line \a dM and \a dC will be equal just because removing some inner
 * points does not change length of path. For strong curvature, if this curvature
 * is matched in position to crown window \b C, those length will differ. The distance
 * calculated without \b C points will be significantly shorter.
 * The ratio defined as:
 * \f[
 * ratio=1-\frac{\left \|dC\right \|}{\left \|dM\right \|}
 * \f]
 * All points inside window \b M for given \a i that belong to crown \b C are removed if 
 * \a ratio for current \a i is bigger than \f$\sigma\f$ 
 *    
 * @author p.baniukiewicz
 * @date 25 Jan 2016
 *
 */
public class HatFilter extends Vector2dFilter implements IPadArray {

	private int window; ///< filter's window size 
	private int crown; ///< filter's crown size (in middle of \a window)
	private double sig; ///< acceptance criterion
	
	private static final Logger logger = LogManager.getLogger(HatFilter.class.getName());
	
	/**
	 * Construct HatFilter
	 * Input array with data is virtually circularly padded 
	 * 
	 * @param input Input array with vertexes of polygon.
	 * @param window Size of main processing window (uneven, positive, longer than 2)
	 * @param crown Size of crown - smaller than \a window
	 * @param sig Acceptance criterion, all ratios larger than \a sig will be removed from \a input list
	 */
	public HatFilter(List<Vector2d> input, int window, int crown, double sig) {
		super(input);
		this.window = window;
		this.crown = crown;
		this.sig = sig;
	}

	/**
	 * Main filter runner
	 * 
	 * @return Processed \a input list, size of output list may be different than input. Empty output is also allowed.
	 */
	@Override
	public Collection<Vector2d> RunFilter() throws FilterException {
		int cp = window/2; // left and right range of window
		int cr = crown/2; // left and right range of crown
		int indexTmp; // temporary index after padding
		int countW = 0; // window indexer
		int countC = 0; // crown indexer
		double lenAll; // length of curve in window
		double lenBrim; // length of curve in window without crown
		Set<Integer> indToRemove = new HashSet<Integer>();
		List<Vector2d> out = new ArrayList<Vector2d>(); // output table
		
		// check input conditions
		if(window%2==0 || crown%2==0)
			throw new FilterException("Input arguments must be uneven, positive and larger than 0");
		if(window>=points.size() || crown>=points.size())
			throw new FilterException("Processing window or crown to long");
		if(crown>=window)
			throw new FilterException("Crown can not be larger or equal to window");
		if(window<3)
			throw new FilterException("Window should be larger than 2");
		
		Vector2d V[] = new Vector2d[window]; // temporary array for holding content of window [v1 v2 v3 v4 v5 v6 v7]
		Vector2d B[] = new Vector2d[window-crown]; //array for holding brim only points  [v1 v2 v6 v7]
		
		for(int c=0;c<points.size();c++)	{	// for every point in data, c is current window position - middle point
			countW = 0;
			countC = 0;
			lenAll = 0;
			lenBrim = 0;
			for(int cc=c-cp;cc<=c+cp;cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2 (for window=5)
				indexTmp = IPadArray.getIndex(points.size(), cc, IPadArray.CIRCULARPAD); // get padded indexes
				V[countW] = (Vector2d) points.get(indexTmp).clone(); // store window content, copy as V and B will be changed and can't be referenced each other
				if(cc<c-cr || cc>c+cr) //FIXME Avoid clone() method
					B[countC++] = (Vector2d) points.get(indexTmp).clone(); // store only brim, copy as V and B will be changed and can't be referenced each other
				countW++;
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
			// decide whether to remove crown
			double ratio = 1 - lenBrim/lenAll;
			logger.debug("c: "+c+" lenAll="+lenAll+" lenBrim="+lenBrim+" ratio: "+ratio);
			if(ratio>sig) // add crown for current window position c to remove list. Added are real indexes in points array (not local window indexes)
				for(int i=c-cr;i<=c+cr;i++) 
					indToRemove.add(i); // add only if not present in set
		}
		logger.debug("Points to remove: "+indToRemove.toString());
		// copy old array to new skipping points marked to remove
		for(int i=0;i<points.size();i++)
			if( !indToRemove.contains(i) )
				out.add(points.get(i));
		return out;
	}

}
