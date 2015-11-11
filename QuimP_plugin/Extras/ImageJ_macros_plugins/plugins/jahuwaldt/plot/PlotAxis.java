/*
*   PlotAxis  -- Represents a generic plot axis.
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

import java.text.*;
import java.util.*;
import java.awt.*;


/**
*  <p> This class represents an generic plot axis.
*      Subtypes should specify if they are X or Y
*      axis types.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version January 10, 2001
**/
public abstract class PlotAxis extends Object implements Cloneable, java.io.Serializable {

	//	Debug flag.
	private static final boolean DEBUG = false;
	
	/**
	*  Size of minor tick marks in pixels.  Major tick
	*  marks are 3 times bigger.
	**/
	public static final int kTick = 5;
	
	/**
	*  Grid style indicating that no grid should be drawn.
	**/
	public static final int kNoGrid = 0;
	
	/**
	*  Grid style indicating that only the tick marks should be drawn.
	**/
	public static final int kTickMarksOnly = 1;
	
	/**
	*  Grid style indicating that the grid should be drawn on the
	*  major tick marks.
	**/
	public static final int kMajorGrid = 2;
	
	
	/**
	*  Indicates if the upper and lower bounds are manually
	*  input or automatically calculated.
	**/
	protected boolean manualLB, manualUB;
	
	/**
	*  Indicates if the tick mark step size is manually input
	*  or automatically calculated.
	**/
	protected boolean manualQuant;
	
	/**
	*  Scaling coefficients between data and screen coordinates.
	**/
	protected double xA = 1., xB = 1.;

	/**
	*  Lower and upper bounds on this axis.
	**/
	protected double aLB = Double.MAX_VALUE, aUB = -Double.MAX_VALUE;
	
	/**
	*  The tick mark step size in data coordinates.
	**/
	protected double quantum;
	
	/**
	*  The grid style (0=no grid, 1=tick marks only, 2=grid on major tickmarks).
	**/
	protected int gridStyle = kTickMarksOnly;
	
	/**
	*  The axis scale transformation function is in this object.
	**/
	protected PlotAxisScale scale = new LinearAxisScale();
	
	/**
	*  The number format used for the axis number labels.  The default
	*  decimal format is used if nothing is specified.
	**/
	protected NumberFormat nf = (DecimalFormat)NumberFormat.getInstance();
	
	/**
	*  The label for this plot axis.
	**/
	protected String label;
	
	
	//-------------------------------------------------------------------------
	/**
	*  Create a new plot axis using the default settings.  Can only
	*  be accessed from sub-classes.
	**/
	protected PlotAxis() { }
	
	/**
	*  Create a plot axis with the label, axis scale transformation,
	*  number format and grid style specified.
	*
	*  @param label      The label to place on this axis.  If null is passed
	*                    no label is drawn.
	*  @param scale      Object indicating if the axis scale type.
	*  @param nf         The object used to format the axis numbers.
	*                    If null is passed, the default decimal format is used.
	*  @param gridStyle  The grid style to use.  Should be one of the constants
	*                    provided by this class.
	**/
	protected PlotAxis( String label, PlotAxisScale scale, NumberFormat nf, int gridStyle ) {
		this.label = label;
		
		if (scale != null)
			this.scale = scale;
			
		if (nf != null)
			this.nf = nf;
		
		this.gridStyle = gridStyle;
		
	}

	//-------------------------------------------------------------------------
	/**
	*  Set the label used by this plot axis.  If null is passed,
	*  no label is drawn.
	**/
	public void setLabel(String newLabel) {
		label = newLabel;
	}

	/**
	*  Return the label used by this plot axis.
	**/
	public String getLabel() {
		return label;
	}

	/**
	*  Set the axis scale type used by this axis.  If null is
	*  passed, the scale will default to a linear scale.
	**/
	public void setScale(PlotAxisScale scale) {
		if (scale == null)
			this.scale = new LinearAxisScale();
		else
			this.scale = scale;
	}
	
	/**
	*  Return the axis scale used by this plot axis.
	**/
	public PlotAxisScale getScale() {
		return scale;
	}
	
	/**
	*  Set the number format used to format the axis numbers for this axis.
	*  If null is passed, the default decimal format is used.
	**/
	public void setNumberFormat( NumberFormat format ) {
		if (format == null)
			nf = (DecimalFormat)NumberFormat.getInstance();
		else
			nf = format;
	}

	/**
	*  Return the number format used by this plot axis.
	**/
	public NumberFormat getNumberFormat() {
		return nf;
	}

	/**
	*  Set the grid style used by this plot axis.  The grid style
	*  should be one of the constants supplied by this class.
	**/
	public void setGridStyle( int style ) {
		gridStyle = style;
	}

	/**
	*  Return the grid style used by this plot axis.
	**/
	public int getGridStyle() {
		return gridStyle;
	}

	/**
	*  Sets whether or not this scale has a manually
	*  or automatically set lower bounds.
	**/
	public void manualLowerBounds(boolean flag) {
		manualLB = flag;
	}
	
	/**
	*  Set the lower (most negative) limit of the axis.
	**/
	public void setLowerBounds(double value) {
		aLB = value;
	}
	
	/**
	*  Return the lower bounds of the axis.
	**/
	public double getLowerBounds() {
		return aLB;
	}
	
	/**
	*  Return the axis lower bound as a formatted
	*  string using the current format.
	**/
	public String lowerBoundsAsString() {
		return nf.format((float)aLB);
	}
	
	/**
	*  Sets whether or not this scale has a manually
	*  or automatically set upper bounds.
	**/
	public void manualUpperBounds(boolean flag) {
		manualUB = flag;
	}
	
	/**
	*  Set the upper (most positive) limit of the axis.
	**/
	public void setUpperBounds(double value) {
		aUB = value;
	}
	
	/**
	*  Return the upper bounds of the axis.
	**/
	public double getUpperBounds() {
		return aUB;
	}
	
	/**
	*  Return the axis upper bound as a formatted
	*  string using the current format.
	**/
	public String upperBoundsAsString() {
		return nf.format((float)aUB);
	}
	
	/**
	*  Sets whether or not this scale has a manually
	*  or automatically set tick mark step size.
	**/
	public void manualTickStepSize(boolean flag) {
		manualQuant = flag;
	}
	
	/**
	*  Set the tick mark step size for this axis.
	**/
	public void setTickStepSize(double value) {
		quantum = value;
	}
	
	/**
	*  Return the tick mark step size for this axis.
	**/
	public double getTickStepSize() {
		return quantum;
	}
	
	/**
	*  Set the upper and lower bounds for this
	*  axis based on the data in the runs input.
	*  If the axis bounds have been set to be manually
	*  input, this method will do nothing.
	*
	*  @param runs  A list of runs to be plotted.
	**/
	public abstract void setAxisBounds(PlotRunList runs);
	
	/**
	*  Scale the axis data so that it fits within the plot
	*  area and pick a tick mark spacing.  If the tick
	*  mark spacing has been set to manually input,
	*  it will not be modified.
	*
	*  @param  plotFrame  The bounding rectangle for the data area
	*                     of the plot (overall bounds minus area for
	*                     titles, labels, etc).
	**/
	public void setSize(Rectangle plotFrame) {
	
		if (DEBUG)
			System.out.println("In PlotAxis.setSize()...");
		
		//	Find good axis limits and tick mark step sizes.
		setAxisTickLimits();
		
		//	Scale the axes to fit in the plot frame.
		resizeAxis(plotFrame);
		
	}
	
	/**
	*  Sets the axis tick mark step size and tick mark limits.
	*  If the tick mark spacing has been set to be manually
	*  input, this method will do nothing.
	**/
	protected void setAxisTickLimits() {
		if (manualQuant)	return;
		
		if (DEBUG)
			System.out.println("In PlotAxis.setAxisTickLimits()...");
		
		boolean lbf = manualLB;
		boolean ubf = manualUB;
		
		double sign = 1.;			//	Start by assuming that positive is to the right.
		double lb = aLB;
		double ub = aUB;
		double delta = aUB - aLB;
		
		if (delta < 0.) {
		
			//	Scale is reversed (positive to the left).
			sign = -1;
			
			//	Swap upper and lower bounds.
			double temp = lb;
			lb = ub;
			ub = temp;
			
			//	Swap upper and lower manually input flags too.
			boolean btemp = lbf;
			lbf = ubf;
			ubf = btemp;
		
		}
		
		//	Find good axis limits for this axis scale.
		AxisLimitData limData = scale.findGoodLimits(lb, ub);
		
		//	Save off axis limits.
		if (!manualLB)
			if (sign > 0)
				aLB = limData.lb;
			else
				aLB = limData.ub;
		
		if (!manualUB)
			if (sign > 0)
				aUB = limData.ub;
			else
				aUB = limData.lb;
		
		//	Save off tick mark spacing.
		quantum = sign*limData.quantum;
	}
	
	/**
	*  Scale this axis so that it fits inside of the specified
	*  plot frame rectangle.  Horizontal axes must fit inside
	*  the width of the frame and vertical axes must fit inside
	*  the height of the frame.
	*
	*  @param  plotFrame  The bounding rectangle for the data area
	*                     of the plot (overall bounds minus area for
	*                     titles, labels, etc).
	**/
	protected abstract void resizeAxis(Rectangle plotFrame);
	
	/**
	*  Renders this plot axis and grid (based on grid
	*  style) into the specified graphics context.
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  comp    The component that we are drawing into.
	*  @param  bounds  The bounding rectangle for the data area
	*                  of the plot (overall bounds minus area for titles,
	*                  labels, etc).
	**/
	public abstract void draw(Graphics gc, Component comp, Rectangle bounds);
	
	
	/**
	*  Scales a given data point to the plot coordinates.
	*  The screen coordinate along this axis for the input
	*  data point is returned.
	**/
	public int scaleCoord(double x) {
		return( (int)(xA*scale.func(x) + xB) );
	}


	/**
	*  Make a copy of this PlotAxis object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		PlotAxis newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (PlotAxis) super.clone();

			// There is no "deep" data to be cloned.

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

}


