/*
*   Plot2D  -- A common interface for all 2D data plot objects.
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
*  <p> An interface for all 2D data plot objects.  This
*      provides the common interface to all types
*      of 2D data plots.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 15, 2000
*  @version October 14, 2000
**/
public interface Plot2D extends Plot {

	/**
	*  Return a reference to the plot's vertical axis.
	**/
	public PlotAxis getVerticalAxis();
	
	/**
	*  Set the plot's vertical axis.
	**/
	public void setVerticalAxis(PlotAxis axis);
	
	/**
	*  Return a reference to the plot's horizontal axis.
	**/
	public PlotAxis getHorizontalAxis();
	
	/**
	*  Set the plot's horizontal axis.
	**/
	public void setHorizontalAxis(PlotAxis axis);
	
	/**
	*  Return a reference to the list of data runs 
	*  assigned to this plot.
	**/
	public PlotRunList getRuns();
	
}
