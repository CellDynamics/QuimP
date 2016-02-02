package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.plugin.snakefilter.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.plugin.utils.Vector2dFilter;

/**
 * Interpolation of points (X,Y) by means of running mean method
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 *
 */
public class MeanFilter implements IQuimpPoint2dFilter<Vector2d>,IPadArray {
	private Vector2dFilter xyData; ///< input List converted to separate X and Y arrays
	private int window; ///< size of processing window
	
	/**
	 * Create running mean filter.
	 * 
	 * @param input List of points to be filtered
	 * @param window Size of processing window. Must be uneven, positive and shorter than dataLength
	 */
	public MeanFilter(List<Vector2d> input, int window) {
		xyData = new Vector2dFilter(input);
		this.window = window;
	}

	/**
	 * Perform interpolation of data by a moving average filter with given window
	 * 
	 * By default uses \b CIRCULAR padding. The window must be uneven, positive and shorter
	 * than data vector. \c X and \c Y coordinates of points are smoothed separately. 
	 * 
	 * @return Filtered points as list of Vector2d objects
	 * @throws QuimpPluginException when:
	 *  - window is even
	 *  - window is longer or equal processed data
	 *  - window is negative
	 */
	@Override
	public List<Vector2d> runPlugin() throws QuimpPluginException {
		int cp = window/2; // left and right range of window
		double meanx = 0;
		double meany = 0;	// mean of window
		int indexTmp; // temporary index after padding
		List<Vector2d> out = new ArrayList<Vector2d>();
		
		if(window%2==0)
			throw new QuimpPluginException("Input argument must be uneven");
		if(window>=xyData.size())
			throw new QuimpPluginException("Processing window to long");
		if(window<0)
			throw new QuimpPluginException("Processing window is negative");
		
		for(int c=0;c<xyData.size();c++)	{	// for every point in data
			meanx = 0;
			meany = 0;
			for(int cc=c-cp;cc<=c+cp;cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2 (for window=5)
				indexTmp = IPadArray.getIndex(xyData.size(), cc, IPadArray.CIRCULARPAD);
				meanx += xyData.getX()[indexTmp];
				meany += xyData.getY()[indexTmp];				
			}
			meanx = meanx/window;
			meany = meany/window;
			out.add(new Vector2d(meanx,meany));
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
