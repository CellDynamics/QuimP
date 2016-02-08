package uk.ac.warwick.wsbc.tools.images.filters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.plugin.QuimpPluginException;
import uk.ac.warwick.wsbc.plugin.snakes.IQuimpPoint2dFilter;
import uk.ac.warwick.wsbc.plugin.utils.IPadArray;
import uk.ac.warwick.wsbc.plugin.utils.QWindowBuilder;

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
 */
public class HatFilter extends QWindowBuilder implements IQuimpPoint2dFilter<Vector2d>,IPadArray, ChangeListener, ActionListener {

	private static final Logger logger = LogManager.getLogger(HatFilter.class.getName());
	
	private int window; ///< filter's window size 
	private int crown; ///< filter's crown size (in middle of \a window)
	private double sig; ///< acceptance criterion
	private List<Vector2d> points;
	private HashMap<String,String[]> uiDefinition; ///< Definition of UI for this plugin
	private DrawPanel dp; ///< Here we will draw. This panel is plot in place of help field
	/**
	 * Construct HatFilter
	 * Input array with data is virtually circularly padded 
	 */
	public HatFilter() {
		logger.trace("Entering constructor");
		this.window = 23;
		this.crown = 13;
		this.sig = 0.3;
		logger.debug("Set default parameter: window="+window+" crown="+crown+" sig="+sig);
		uiDefinition = new HashMap<String, String[]>(); // will hold ui definitions 
		uiDefinition.put("name", new String[] {"HatFilter"}); // name of window
		uiDefinition.put("window", new String[] {"spinner", "3","51","2"}); // the name of this ui control is "system-wide", now it will define ui and name of numerical data related to this ui and parameter
		uiDefinition.put("crown", new String[] {"spinner", "1","51","2"});
		uiDefinition.put("sigma", new String[] {"spinner", "0.01","0.9","0.01"});
//		uiDefinition.put("help", new String[] {""}); // help string
		BuildWindow(uiDefinition); // construct ui (not shown yet)
	}

	/**
	 * Attach data to process.
	 * 
	 * Data are as list of vectors defining points of polygon.
	 * Passed points should be sorted according to a clockwise
	 * or anti-clockwise direction
	 * 
	 * @param data Polygon points
	 * @see uk.ac.warwick.wsbc.plugin.snakes.IQuimpPoint2dFilter.attachData(List<E>)
	 */
	@Override
	public void attachData(List<Vector2d> data) {
		logger.trace("Entering attachData");
		points = data;		
	}
	
	/**
	 * Main filter runner
	 * 
	 * @return Processed \a input list, size of output list may be different than input. Empty output is also allowed.
	 */
	@Override
	public List<Vector2d> runPlugin() throws QuimpPluginException {
		logger.debug(String.format("Run plugin with params: window %d, crown %d, sigma %f",window,crown,sig));
		
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
			throw new QuimpPluginException("Input arguments must be uneven, positive and larger than 0");
		if(window>=points.size() || crown>=points.size())
			throw new QuimpPluginException("Processing window or crown to long");
		if(crown>=window)
			throw new QuimpPluginException("Crown can not be larger or equal to window");
		if(window<3)
			throw new QuimpPluginException("Window should be larger than 2");
		
		Vector2d V[] = new Vector2d[window]; // temporary array for holding content of window [v1 v2 v3 v4 v5 v6 v7]
		Vector2d B[] = new Vector2d[window-crown]; //array for holding brim only points  [v1 v2 v6 v7]
		
		for(int c=0;c<points.size();c++)	{	// for every point in data, c is current window position - middle point
			countW = 0;
			countC = 0;
			lenAll = 0;
			lenBrim = 0;
			for(int cc=c-cp;cc<=c+cp;cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2 (for window=5)
				indexTmp = IPadArray.getIndex(points.size(), cc, IPadArray.CIRCULARPAD); // get padded indexes
				V[countW] = points.get(indexTmp); // store window content (reference)
				if(cc<c-cr || cc>c+cr)
					B[countC++] = points.get(indexTmp); // store only brim (reference)
				countW++;
			}
			
			// converting node points to vectors between them and get that vector length
			for(int i=0;i<V.length-1;i++)
				lenAll += getLen(V[i],V[i+1]);
			for(int i=0;i<B.length-1;i++)
				lenBrim += getLen(B[i],B[i+1]);
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
	
	/**
	 * Get length of vector v = v1-v2
	 * 
	 * Avoid creating new Vector2d object when using build-in Vector2d::sub method
	 * method
	 * 
	 * @param v1 Vector
	 * @param v2 Vector
	 * @return ||v1-v2||
	 */
	private double getLen(Vector2d v1, Vector2d v2) {
		double dx;
		double dy;
		dx = v1.x - v2.x;
		dy = v1.y - v2.y;
		
		return Math.sqrt(dx*dx + dy*dy);
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
		logger.trace("Entering setup");
		return DOES_SNAKES + CHANGE_SIZE;
	}

	/**
	 * Configure plugin and overrides default values.
	 * 
	 * Supported keys:
	 * -# \c window - size of main window
	 * -# \c crown - size of inner window
	 * -# \c sigma - cut-off value (see class description)
	 * 
	 * @param par configuration as pairs <key,val>. Keys are defined
	 * by plugin creator and plugin caller do not modify them.
	 * @throws QuimpPluginException on wrong parameters list or wrong parameter conversion
	 * @see uk.ac.warwick.wsbc.plugin.IQuimpPlugin.setPluginConfig(HashMap<String, Object>)
	 */
	@Override
	public void setPluginConfig(HashMap<String, Object> par) throws QuimpPluginException {
		try
		{
			window = ((Double)par.get("window")).intValue(); // by default all numeric values are passed as double
			crown = ((Double)par.get("crown")).intValue();
			sig = ((Double)par.get("sigma")).doubleValue();	
			setValues(par); // copy incoming parameters to UI
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
		ToggleWindow();
	}

	@Override
	public String getVersion() {
		return null;
	}
	
	
	/**
	 * Override of uk.ac.warwick.wsbc.plugin.utils.QWindowBuilder.BuildWindow()
	 * 
	 * The aim is to:
	 * -# attach listeners for spinners ro preventing even numbers
	 * -# attach listener for build-in apply button
	 * -# add draw field DrawPanel
	 */
	@Override
	public void BuildWindow(Map<String, String[]> def) {
		super.BuildWindow(def); // window must be built first
		
		((JSpinner)ui.get("window")).addChangeListener(this); // attach listener to selected ui
		((JSpinner)ui.get("crown")).addChangeListener(this); // attach listener to selected ui
		applyB.addActionListener(this); // attach listener to aplly button
		dp = new DrawPanel(); // create drawable JFrame
		pluginPanel.add(dp,BorderLayout.CENTER); // add in center position (in place of help zone)
		pluginWnd.pack();
	}

	/**
	 * React on spinners changes. 
	 * 
	 * Here used for preventing even values.
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {
		Object source = ce.getSource();
		JSpinner s = (JSpinner)ui.get("window"); // get ui element
		JSpinner s1 = (JSpinner)ui.get("crown"); // get ui element
		if(source == s) { // check if this event concerns it
			logger.debug("Spinner window used");
			if( ((Double)s.getValue()).intValue()%2==0 )
				s.setValue((Double)s.getValue() + 1);
		}
		if(source == s1) { // check if this event concerns it
			logger.debug("Spinner crown used");
			if( ((Double)s1.getValue()).intValue()%2==0 )
				s1.setValue((Double)s1.getValue() + 1);
		}
		
	}

	/**
	 * React on \b Apply button.
	 * 
	 * Here \b Apply button copies window content into plugin structures.
	 * This is different approach than in LoessFilter and MeanFilter where
	 * window content was copied while runPlugin() command
	 * 
	 * @see uk.ac.warwick.wsbc.tools.images.filters.LoessFilter.runPlugin()
	 * @see uk.ac.warwick.wsbc.tools.images.filters.MeanFilter.runPlugin()	 *  
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if(b==applyB) { // pressed apply, copy ui data to plugin
			window = getIntegerFromUI("window");
			crown = getIntegerFromUI("crown");
			sig = getDoubleFromUI("sigma");
			logger.debug(String.format("Apply pressed: window %d, crown %d, sigma %f",window,crown,sig));
		}
		
	}
	
	/**
	 * Class for plotting in center part of plugin derived from QWindowBuilder
	 * 
	 * @author p.baniukiewicz
	 * @date 8 Feb 2016
	 *
	 */
	class DrawPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		DrawPanel() {
			setPreferredSize(new Dimension(200, 200));
		}
		
		/**
		 * Main plotting function
		 */
		@Override
		public void paintComponent(Graphics g) {
	        super.paintComponent(g);

	        g.setColor(Color.BLACK);
	        g.fillRect(0, 0, 200, 200);
	        g.setColor(Color.WHITE);
	        g.drawRect(10, 10, 100, 100);
	    }
	}
}

