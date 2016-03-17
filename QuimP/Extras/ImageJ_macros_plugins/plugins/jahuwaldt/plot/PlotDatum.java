/*
*   PlotDatum  -- Represents a single data point on a 2D plot.
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

import java.awt.Color;


/**
*  <p> This class represents a single data point on a plot
*      and includes all the information required by each point.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  June 1, 2000
*  @version November 20, 2000
**/
public class PlotDatum extends Object implements Cloneable, java.io.Serializable {

	/**
	*  The X and Y coordinate points for this datum.
	**/
	public double x, y;

	/**
	*  The error on the Y value.
	**/
	private double yErr;

	/**
	*  A flag that indicates if there is an error bar.
	**/
	private boolean errBarFlg = false;

	/**
	*  Flag that indicates if this point connects to the previous point.
	**/
	private boolean conFlg;

	/**
	*  The line type for the line connecting this point to the previous point.
	*  This is not yet implemented!  Waiting until I learn Java2D.
	**/
//	private int lineMode;

	/**
	*  The line color used to connect this point to the previous one.
	**/
	private Color lineColor = Color.black;
	
	/**
	*  The plot symbol used by this datum.
	**/
	private PlotSymbol symbol = null;
	
	
	//-------------------------------------------------------------------------
	/**
	*  Create a new datum (plot coordinate point) given the specified
	*  X and Y values.  This datum will, by default, have no error bar.
	*
	*  @param xValue  The X coordinate value for this datum point.
	*  @param yValue  The Y coordinate value for this datum point.
	*  @param connected  A flag that indicates that this datum is connected
	*                    to the previous one if true, no line is drawn
	*                    to the previous datum if false.
	**/
	public PlotDatum( double xValue, double yValue, boolean connected ) {
		super();

		x = xValue;
		y = yValue;
		conFlg = connected;
	}

	/**
	*  Create a new datum (plot coordinate point) given the specified
	*  X and Y values and the given plot symbol.  This datum will, by
	*  default, have no error bar.
	*
	*  @param xValue  The X coordinate value for this datum point.
	*  @param yValue  The Y coordinate value for this datum point.
	*  @param connected  A flag that indicates that this datum is connected
	*                    to the previous one if true, no line is drawn
	*                    to the previous datum if false.
	*  @param  symbol  The plot symbol to be used for this data point.
	**/
	public PlotDatum( double xValue, double yValue, boolean connected, PlotSymbol symbol ) {
		super();

		x = xValue;
		y = yValue;
		conFlg = connected;
		this.symbol = symbol;
	}

	//-------------------------------------------------------------------------
	/**
	*  Return the X coordinate value of this point.
	**/
	public double getX() {
		return x;
	}

	/**
	*  Return the Y coordinate value of this point.
	**/
	public double getY() {
		return y;
	}

	/**
	*  Set the X coordinate value of this point.
	**/
	public void setX( double value ) {
		x = value;
	}

	/**
	*  Set the Y coordinate value of this point.
	**/
	public void setY( double value ) {
		y = value;
	}

	/**
	*  Set the error on Y value.
	**/
	public void setYError( double err ) {
		yErr = err;
		if (yErr == 0.)
			errBarFlg = false;
		else
			errBarFlg = true;
	}

	/**
	*  Get the error on Y value.
	**/
	public double getYError() {
		return yErr;
	}

	/**
	*  Returns true if this data point has an error bar.
	**/
	public boolean hasErrorBar() {
		return errBarFlg;
	}

	/**
	*  Set if this datum is connected to the previous one by a line or not.
	**/
	public void setConnected( boolean flag ) {
		conFlg = flag;
	}

	/**
	*  Return true if this datum is connected to the previous one by a line
	*  and false if it is not.
	**/
	public boolean connected() {
		return conFlg;
	}

	/**
	*  Set the plot symbol used for this datum.
	**/
	public void setPlotSymbol(PlotSymbol symbol) {
		this.symbol = symbol;
	}
	
	/**
	*  Returns a reference to the plot symbol used by
	*  this datum.
	**/
	public PlotSymbol getPlotSymbol() {
		return symbol;
	}

	/**
	*  Set the color used for the line connecting this datum
	*  to the previous one.  If null is passed, the line is
	*  drawn in black.
	**/
	public void setLineColor(Color color) {
		if (color != null)
			lineColor = color;
		else
			lineColor = Color.black;
	}
	
	/**
	*  Return the color to be used for drawing the line
	*  connecting this datum to the previous.
	**/
	public Color getLineColor() {
		return lineColor;
	}
	
	
	/**
	*  Make a copy of this PlotDatum object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		PlotDatum newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (PlotDatum) super.clone();

			// Clone this object's data structures.
			if (this.symbol != null)
				newObject.symbol = (PlotSymbol)this.symbol.clone();

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

}


