/*
*   BoxLRSymbol  -- Represents a square plot symbol with the lower right corner cut out.
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
*  <p> This class represents a square plot symbol with
*      the lower right corner removed.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  January 2, 2001
*  @version January 2, 2001
**/
public class BoxLRSymbol extends PolygonSymbol {

	//	Number of points in the coordinate arrays.
	private static final int kNumPoints = 5;
	
	//-------------------------------------------------------------------------
	/**
	*  Creates a square with LR corner removed plot
	*  symbol that has a width of 8 pixels, is transparent and
	*  has a border color of black.
	**/
	public BoxLRSymbol() {}
	
	//-------------------------------------------------------------------------

	/**
	*  Method that determines the corner points of the
	*  polygon used to draw this plot symbol.  The corner
	*  points are stored in 2 arrays, xPoints and yPoints.
	*  If the arrays don't already exist for this symbol instance,
	*  this method must allocate them.
	*
	*  @param  x  The horizontal position of the center of the symbol.
	*  @param  y  The vertical position of the center of the symbol.
	**/
	protected void generatePoints(int x, int y) {

		//	Allocate memory for point arrays if they don't already exist.
		if (xPoints == null) {
			xPoints = new int[kNumPoints];
			yPoints = new int[kNumPoints];
		}
		
		int width2 = getSize()/2;
		int xmw2 = x - width2;
		int xpw2 = x + width2;
		int ymw2 = y - width2;
		int ypw2 = y + width2;
		
		//	Determine points.
		xPoints[0] = xmw2;		yPoints[0] = ymw2;
		xPoints[1] = xmw2;		yPoints[1] = ypw2;
		xPoints[2] = x;			yPoints[2] = ypw2;
		xPoints[3] = xpw2;		yPoints[3] = y;
		xPoints[4] = xpw2;		yPoints[4] = ymw2;
		
	}
}


