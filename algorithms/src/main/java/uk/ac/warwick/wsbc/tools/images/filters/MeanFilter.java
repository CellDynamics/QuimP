package uk.ac.warwick.wsbc.tools.images.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.plugin.snakefilter.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.plugin.utils.QuimpDataConverter;

/**
 * Interpolation of points (X,Y) by means of running mean method
 * 
 * @author p.baniukiewicz
 * @date 20 Jan 2016
 *
 */
public class MeanFilter implements IQuimpPoint2dFilter<Vector2d>,IPadArray {
	
	private static final Logger logger = LogManager.getLogger(MeanFilter.class.getName());
	private QuimpDataConverter xyData; ///< input List converted to separate X and Y arrays
	private int window; ///< size of processing window
	
	/**
	 * Create running mean filter.
	 * 
	 * All default parameters should be declared here. Non-default are passed by 
	 * setPluginConfig(HashMap<String, Object>)
	 */
	public MeanFilter() {
		this.window = 7; // default value
	}

	/**
	 * Attach data to process.
	 * 
	 * Data are as list of vectors defining points of polygon.
	 * Passed points should be sorted according to a clockwise
	 * or anti-clockwise direction
	 * 
	 * @param data Polygon points
	 * @see uk.ac.warwick.wsbc.plugin.snakefilter.IQuimpPoint2dFilter.attachData(List<E>)
	 */
	@Override
	public void attachData(List<Vector2d> data) {
		xyData = new QuimpDataConverter(data);
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
		logger.debug(String.format("Run plugin with params: window %d",window));
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

	/**
	 * This method should return a flag word that specifies the filters capabilities.
	 * 
	 * @return Configuration codes
	 * @see uk.ac.warwick.wsbc.plugin.IQuimpPlugin
	 * @see uk.ac.warwick.wsbc.plugin.IQuimpPlugin.setup()
	 */
	@Override
	public int setup() {
		return DOES_SNAKES;
	}

	/**
	 * Configure plugin and overrides default values
	 * 
	 * @param par configuration as pairs <key,val>. Keys are defined
	 * by plugin creator and plugin caller do not modify them.
	 * @see uk.ac.warwick.wsbc.plugin.IQuimpPlugin.setPluginConfig(HashMap<String, Object>)
	 */
	@Override
	public void setPluginConfig(HashMap<String, Object> par) throws QuimpPluginException {
		try
		{
			window = ((Double)par.get("window")).intValue(); // by default all numeric values are passed as double
		}
		catch(Exception e)
		{
			// we should never hit this exception as parameters are not touched by caller
			// they are only passed to configuration saver and restored from it
			throw new QuimpPluginException("Wrong input argument->"+e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Object> getPluginConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showUI(boolean val) {
		logger.debug("Got message to show UI");
		
	}
}
