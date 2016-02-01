/**
 * @file QWindowBuilder.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * Simple window builder for QuimP plugins
 * 
 * Allow user to construct simple window for his plugins just by passing textual 
 * description of what that window should contain.
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public abstract class QWindowBuilder {

	protected Frame pluginWnd; ///< main window object
	protected boolean windowState; ///< current window state \c true if visible
	private ArrayList<Component> ui; ///<< contain list of all ui elements created on base of \c def
	final private static HashSet<String> RESERVED_KEYS = new HashSet<String>(
			Arrays.asList(
					new String[]
							{"help", "name"})); ///< list of reserved keys that are not UI elements. They are processed in different way
	
	/**
	 * Default constructor
	 */
	public QWindowBuilder() {
		ui = new ArrayList<Component>();
	}
	
	/**
	 * Main window builder
	 * 
	 * The window is constructed using configuration provided by \c def parameter
	 * which is Map of <key,value>. The key is the name of the parameter that should
	 * be related to value held in it (e.g window, smooth, step, etc.). The name must
	 * be written in small letters. Keys are strictly related to UI elements that are 
	 * created by this method window (basing on configuration passed in \b value).
	 * There are two special names that are not related to UI directly:
	 * -# help - defines textual help provided as parameter
	 * -# name - defines plugin name provided as parameter
	 * The parameter list is defined as String[] and its content is depended on key. For 
	 * \b help and \b name it is String[1] table contain single string with help text and
	 * plugin name respectively.  
	 * 
	 * The UI elements are defined for all other cases in \b value filed of Map. Known UI are
	 * as follows:
	 * -# spinner - creates Spinner control. It requires 3 parameters (in order)
	 *  -# minimal range
	 *  -# maximal range
	 *  -# step
 	 *
 	 * The type of required UI element associated with given parameter name (\a Key) is coded
 	 * in value of given Key in accordance with list above. The correct order of sub-parameters
 	 * must be preserved.	
	 * Exemplary configuration is as follows:
	 * @code{.java}
	 *  HashMap<String,String[]> def1 = new HashMap<String, String[]>();
		def1.put("name", new String[] {"test"}); // non UI element - name of window
		def1.put("window", new String[] {"spinner", "-0.5","0.5","0.1"}); // adds spinner to provide window parameter
		def1.put("smooth", new String[] {"spinner", "-1", "10", "1"});
		def1.put("help", new String[] {"help text}); // non UI element - help 
	 * @endcode
	 * 
	 * By default window is not visible yet. User must call ShowWindow or ToggleWindow
	 *  
	 * @param def Configuration \c Map<String, String[]> as described
	 * @throw IllegalArgumentException or other unchecked exceptions on wrong syntax of \c def
	 */
	public void BuildWindow(Map<String, String[]> def) {
		if(def.size()<2)
			throw new IllegalArgumentException("Window must contain title and at least one control");
		ui.clear(); // clear all ui stored on second call of third method
		
		pluginWnd = new Frame(); //create frame with title given as first position in table
		JPanel pluginPanel = new JPanel(); // main panel on whole window
		pluginPanel.setLayout(new BorderLayout()); // divide window on two zones - upper for controls, middle for help
		
		Panel north = new Panel(); // panel for controls
		// get layout size
		int siz = def.size(); // total size of Map
		// but grid layout does not contain help and name or other reserved non UI keys
		Set<String> s = def.keySet(); // get Set of keys
		for(String k : RESERVED_KEYS) // and check if any of them is in s
			if(s.contains(k))
				siz--;
		
		GridLayout gridL = new GridLayout(siz,2); // Nx2, by default in row we have control and its description
		north.setLayout(gridL);
		gridL.setVgap(5); // set bigger gaps
		gridL.setHgap(5);
		Iterator<String> keySetIterator = def.keySet().iterator();  // iterate over def entries except first one which is always title
		while(keySetIterator.hasNext()) {
			String key = keySetIterator.next();
			if(RESERVED_KEYS.contains(key))
				continue;
			String componentName = def.get(key)[0]; // get name of UI for given key
			switch(componentName.toLowerCase()) {
				case "spinner": // by default all spinners are double
					SpinnerNumberModel model = new SpinnerNumberModel(Double.parseDouble(def.get(key)[1]), // current value
							Double.parseDouble(def.get(key)[1]), // min
							Double.parseDouble(def.get(key)[2]), // max
							Double.parseDouble(def.get(key)[3]));// step
					ui.add(new JSpinner(model) );
					ui.add(new Label(key)); // add description - on even position
					break;
				default:
					throw new IllegalArgumentException("Unknown ui type provided: "+key); // wrong param syntax
			}
		}
		for(Component c : ui) // iterate over all components and add them to grid layout
			north.add(c);
		
		// add non ui elements
		if(def.containsKey("name")) {
			pluginPanel.setBorder(BorderFactory.createTitledBorder("Plugin "+def.get("name")[0])); // border on whole window
		}
		if(def.containsKey("help")) {
			JTextArea helpArea = new JTextArea(10, 10); // default size of text area
			JScrollPane helpPanel = new JScrollPane(helpArea);
			helpArea.setEditable(false);
			pluginPanel.add(helpPanel, BorderLayout.CENTER); // locate at center position
			helpArea.setText(def.get("help")[0]); // set help text
			helpArea.setLineWrap(true); // with wrapping
		}
		
		// build window
		pluginPanel.add(north, BorderLayout.NORTH);
		pluginWnd.add(pluginPanel);
		pluginWnd.pack();
		// add listener on close
		pluginWnd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			{
				pluginWnd.setVisible(false);
			}
		});
		windowState = false; // by default window is not visible. User must call ShowWindow or ToggleWindow 
	}
	
	/**
	 * Show or hide window
	 * 
	 * @param state State of the window \c true to show, \c false to hide
	 */
	public void ShowWindow(boolean state) {
		pluginWnd.setVisible(state);
		windowState = state;
	}
	
	/**
	 * Toggle window visibility
	 */
	public void ToggleWindow() {
		windowState = !windowState;
		ShowWindow(windowState);
	}
	
	public void setValues(Map<String, String> vals) {
		
	}
}
