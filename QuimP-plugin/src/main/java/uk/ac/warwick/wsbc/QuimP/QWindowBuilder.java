/**
 * @file QWindowBuilder.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public abstract class QWindowBuilder {

	protected Frame pluginWnd;
	protected boolean windowState;
	private ArrayList<Component> ui;
	
	public QWindowBuilder() {
		ui = new ArrayList<Component>();
	}
	
	public void BuildWindow(String[][] def) {
		if(def.length<2)
			throw new IllegalArgumentException("Window must contain title and at least one control");
		ui.clear();
		
		pluginWnd = new Frame(def[0][0]); //create frame with title given as first position in table
		pluginWnd.setLayout(new GridLayout(def.length-1,2)); // set grid layout of length -1 (skip framename)
		
		for(int i=1; i<def.length;i++) {
			switch(def[i][0]) {
				case "spinner":
					SpinnerNumberModel model = new SpinnerNumberModel(0,
							Double.parseDouble(def[i][1]),
							Double.parseDouble(def[i][2]),
							Double.parseDouble(def[i][3]));
					ui.add(new JSpinner(model) );
					ui.add(new Label(def[i][4]));
					break;
				default:
					throw new IllegalArgumentException("Unknown ui type provided");
			}
		}
		for(Component c : ui)
			pluginWnd.add(c);
		pluginWnd.pack();
		
		pluginWnd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			{
				pluginWnd.setVisible(false);
			}
		});
		windowState = false;
	}
	
	public void ShowWindow(boolean state) {
		pluginWnd.setVisible(state);
		windowState = state;
	}
	
	public void ToggleWindow() {
		windowState = !windowState;
		ShowWindow(windowState);
	}
}
