/*
*   PlotAxisScale  -- Interface for plot axis scale objects.
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
*  <p> This interface is used by objects that provide
*      axis scaling information for a plot axis.  Objects
*      implementing this interface must provide a function
*      that appropriately scales the input data to the
*      plot axis.  For example, a linear scale would
*      have a function:  f(a) = a.  A log10 scale would
*      have a function:  f(a) = log10(a), etc.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version November 19, 2000
**/
public interface PlotAxisScale extends java.io.Serializable {

	/**
	*  The transformation function used to scale the data
	*  plotted against this axis.
	*
	*  @param  a  The input to the scaling function.
	*  @return The scaled value of a.
	**/
	public double func(double a);
	
	/**
	*  Method that returns the default lower bounds for
	*  this axis scale.  For example, on a log10 scale,
	*  you might want the default lower bounds to be 1.0.
	**/
	public double lowerBounds();
	
	/**
	*  Method that returns the default upper bounds for
	*  this axis scale.  For example, on a log10 scale,
	*  you might want the default upper bounds to be 10.0.
	**/
	public double upperBounds();
	
	/**
	*  Method that returns an AxisLimitData object that contains
	*  the preferred axis limits and tick mark spacing for the
	*  specified range of data values for this axis scale.
	*
	*  @param  lb   The lower bounds of the data plotted on this axis.
	*  @param  ub   The upper bounds of the data plotted on this axis.
	**/
	public AxisLimitData findGoodLimits(double lb, double ub);
	
	/**
	*  Find the position and size (in screen coordinates) of tick
	*  marks for a given axis scale.
	*
	*  @param  quantum   Tick mark step size for the axis using this scale.
	*  @param  aLB       Lower bounds of axis using this scale.
	*  @param  aUB       Upper bounds of axis using this scale.
	*  @param  xA        Scaling coefficient for this axis.
	*  @param  xB        Scaling coefficient for this axis.
	*  @return An object containing the tick mark positions, lengths,
	*          and data values at each tick mark.
	**/
	public TickMarkData calcTickMarks(double quantum, double aLB, double aUB,
										double xA, double xB);

	/**
	*  Adjust the upper and lower axis bounds, if necissary, to allow
	*  room for error bars on the specified data point.  New bounds
	*  returned in "output" object.
	*
	*  @param datum  The data point we are bounds checking.
	*  @param aUB    The current upper bounds.
	*  @param aLB    The current lower bounds.
	*  @param output An AxisLimitData structure for passing the new upper and
	*                lower bounds to the calling routine.
	**/
	public void adjustForErrorBars(PlotDatum datum, double aUB, double aLB,
									AxisLimitData output);
}
