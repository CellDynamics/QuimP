/*
*   ContourAttrib  -- Represents attributes that may be assigned to contour paths.
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
*  <p> This object represents the attributes assigned to a
*      contour path.  Typically, the same attributes are
*      assigned to all the contour paths of a given contour
*      level.
*  </p>
*
*  <p> Right now, the only attribute used is "level", but
*      in the future I may add more.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  November 11, 2000
*  @version November 17, 2000
**/
public class ContourAttrib implements Cloneable, java.io.Serializable {

	//	The level (altitude) of a contour path.
	private double level;	
	
	
	/**
	*  Create a contour attribute object where only
	*  the contour level is specified.
	**/
	public ContourAttrib(double level) {
		this.level = level;
	}
	
	/**
	*  Return the level stored in this contour attribute.
	**/
	public double getLevel() {
		return level;
	}
	
	/**
	*  Set or change the level stored in this contour attribute.
	**/
	public void setLevel(double level) {
		this.level = level;
	}
	
	/**
	*  Make a copy of this ContourAttrib object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		ContourAttrib newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (ContourAttrib) super.clone();

			// There is no "deep" data to be cloned.

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

}
