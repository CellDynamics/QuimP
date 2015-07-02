/*
*   PlotPanel  -- A Swing panel that displays the specified data plot.
*
*   Copyright (C) 2000-2002 by Joseph A. Huwaldt <jhuwaldt@knology.net>.
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Library General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Library General Public License for more details.
**/
package jahuwaldt.plot;

import java.awt.*;
import javax.swing.*;


/**
*  <p> A panel component that allows the supplied data
*      plot to be included and drawn in a Swing container.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  June 1, 2000
*  @version November 18, 2000
**/
public class PlotPanel extends JPanel {

	/**
	*  The plot this panel is displaying
	**/
	private Plot thePlot;
	
	
	//-------------------------------------------------------------------------
	/**
	*  Creates a plot panel that displays the given plot.
	*
	*  @param  plot   The plot to be displayed in this panel.
	**/
	public PlotPanel( Plot plot ) {
		super();

		thePlot = plot;
	}

	//-------------------------------------------------------------------------
	/**
	*  Returns a reference to the plot displayed in this
	*  panel.
	**/
	public Plot getPlot() {
		return thePlot;
	}
	
	/**
	*  Called automatically by system to paint this component.
	**/
	public void paintComponent( Graphics gc ) {

		// Paint the background
		super.paintComponent( gc );
		
		if (thePlot != null) {
			// Deal with a possible border.
			Insets insets = getInsets();
			int currentWidth = getWidth() - insets.left - insets.right;
			int currentHeight = getHeight() - insets.top - insets.bottom;
			Rectangle bounds = new Rectangle(insets.left, insets.top,
												currentWidth, currentHeight);
			
			//	Render the plot image.
			thePlot.draw(gc, this, bounds);
		}
	}


}


