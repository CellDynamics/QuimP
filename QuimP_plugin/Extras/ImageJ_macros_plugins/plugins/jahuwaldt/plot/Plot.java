/*
*   Plot  -- A common interface for all data plot objects.
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
*  <p> An interface for all data plot objects.  This
*      provides the common interface to all types
*      of data plots.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 14, 2000
*  @version December 2, 2000
**/
public interface Plot extends Cloneable, java.io.Serializable {

	/**
	*  Returns the title assigned to this plot.
	**/
	public String getTitle();
	
	/**
	*  Set's the title assigned to this plot.
	**/
	public void setTitle(String title);
	
	/**
	*  This method is called to render this plot to the
	*  specified graphics context within the specified bounding
	*  rectangle.
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  comp    The component that we are drawing into.
	*  @param  bounds  The overall bounding rectangle we are drawing into
	*                  including the title and axis labels.  This is typically
	*                  the component bounding rectangle minus insets.
	**/
	public void draw( Graphics gc, Component comp, Rectangle bounds );
	
	/**
	*  Make a copy of this Plot object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone();
	
}
