/*
*   AxisLimitData  -- Temporary container to hold axis limit data.
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
*  <p> This class is used to pass scaled axis limit data
*      between various plot objects.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 14, 2000
*  @version October 14, 2000
**/
public class AxisLimitData extends Object implements java.io.Serializable {

	/**
	*  The lower and upper bounds of the axis scale.
	**/
	double lb, ub;
	
	/**
	*  The tick mark step size for the axis scale.
	**/
	double quantum;

}

