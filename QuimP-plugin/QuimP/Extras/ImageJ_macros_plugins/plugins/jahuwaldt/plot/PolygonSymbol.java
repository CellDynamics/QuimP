/*
*   PolygonSymbol  -- Represents a generic plot symbol drawn by a polygon.
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

/**
*  <p> This class represents a generic plot symbol
*      drawn by a polygon.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  January 2, 2001
*  @version January 2, 2001
**/
public abstract class PolygonSymbol extends PlotSymbol {

	/**
	*  X-coordinates of the corners of the polygon
	*  that makes up this symbol.
	**/
	protected int[] xPoints;
	
	/**
	*  Y-coordinates of the corners of the polygon
	*  that makes up this symbol.
	**/
	protected int[] yPoints;
	
	
	//-------------------------------------------------------------------------
	/**
	*  Creates a generic plot symbol made up of a polygon
	*  that has a width of 8 pixels, is transparent and
	*  has a border color of black.
	**/
	protected PolygonSymbol() {}
	
	//-------------------------------------------------------------------------

	/**
	*  Draws a plot symbol consisting of a polygon to the
	*  specified graphics context at the specified
	*  coordinates.
	*
	*  @param  gc  The graphics context where the symbol will be drawn.
	*  @param  x   The horizontal position of the center of the symbol.
	*  @param  y   The vertical position of the center of the symbol.
	**/
	public void draw(Graphics gc, int x, int y) {
		Color saveColor = gc.getColor();
		
		//	Generate the coordinate points for the symbol polygon.
		generatePoints(x, y);
		
		//	Draw in a filled symbol if color specified.
		Color fillColor = getFillColor();
		if (fillColor != null) {
			gc.setColor(fillColor);
			gc.drawPolygon(xPoints, yPoints, xPoints.length);
			gc.fillPolygon(xPoints, yPoints, xPoints.length);
			
		}
		
		//	Draw the border of the symbol if color specified.
		Color borderColor = getBorderColor();
		if (borderColor != null) {
			gc.setColor(borderColor);
			gc.drawPolygon(xPoints, yPoints, xPoints.length);
		}
		
		//	Be kind, rewind.
		gc.setColor(saveColor);
	}
	
	
	/**
	*  Method that determines the corner points of the
	*  polygon used to draw this plot symbol.  The corner
	*  points are stored in 2 arrays, xPoints and yPoints.
	*  If the arrays don't already exist for this symbol instance,
	*  this method must allocate space for them.
	*
	*  @param  x  The horizontal position of the center of the symbol.
	*  @param  y  The vertical position of the center of the symbol.
	**/
	protected abstract void generatePoints(int x, int y);
	
}


