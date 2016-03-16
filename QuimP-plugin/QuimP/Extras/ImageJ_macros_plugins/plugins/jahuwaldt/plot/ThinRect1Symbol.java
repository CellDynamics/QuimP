/*
*   ThinRect1Symbol  -- Represents a vertical thin rectangle plot symbol.
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
*  <p> This class represents a vertical thin rectangle
*      plot symbol.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  January 2, 2001
*  @version January 2, 2001
**/
public class ThinRect1Symbol extends PolygonSymbol {

	//	Number of points in the coordinate arrays.
	private static final int kNumPoints = 4;
	
	//-------------------------------------------------------------------------
	/**
	*  Creates a vertical thin rectangle plot
	*  symbol that has a height of 8 pixels, is transparent and
	*  has a border color of black.
	**/
	public ThinRect1Symbol() {}
	
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
		int width4 = getSize()/4;
		int xmw4 = x - width4;
		int xpw4 = x + width4;
		int ymw2 = y - width2;
		int ypw2 = y + width2;
		
		//	Determine points.
		xPoints[0] = xpw4;			yPoints[0] = ymw2;
		xPoints[1] = xmw4;			yPoints[1] = ymw2;
		xPoints[2] = xmw4;			yPoints[2] = ypw2;
		xPoints[3] = xpw4;			yPoints[3] = ypw2;
		
	}
}


