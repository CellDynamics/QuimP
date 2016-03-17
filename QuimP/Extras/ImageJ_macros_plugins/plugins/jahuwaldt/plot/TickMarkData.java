/*
*   TickMarkData  -- Temporary container for tick mark data.
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


/**
*  <p> This class is used to pass data on tick mark
*      spacing and size, and data value at each
*      tick mark between objects in the plot package.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 25, 2000
*  @version October 14, 2000
**/
public class TickMarkData extends Object implements java.io.Serializable {

	/**
	*  Buffer that holds the position of each tick mark along
	*  the axis in pixels.
	**/
	int[] mark;
	
	/**
	*  Buffer that holds the length of each tick mark in pixels.
	**/
	int[] lmark;
	
	/**
	*  Buffer that holds the data value at each tick mark.
	**/
	float[] markValue;
	
}

