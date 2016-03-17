/*
*   Log10AxisScale  -- Provides base 10 logarithmic scaling for plot axes.
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

import jahuwaldt.tools.math.MathTools;


/**
*  <p> This object provides log10 scaling for plot axes.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version January 10, 2001
**/
public class Log10AxisScale implements PlotAxisScale {

	//	Debug flag.
	private static final boolean DEBUG = false;
	
	/**
	*  The transformation function used to scale the data
	*  plotted against this axis.
	*  This axis uses a log10 scaling function:  f(a) = log10(a).
	**/
	public final double func(double a) {
		return MathTools.log10(a);
	}
	
	/**
	*  Method that returns the default lower bounds for
	*  this axis scale.  Returns 1.0.
	**/
	public double lowerBounds() {
		return 1.0;
	}
	
	/**
	*  Method that returns the default upper bounds for
	*  this axis scale.  Returns 10.0.
	**/
	public double upperBounds() {
		return 10.0;
	}

	/**
	*  Method that returns an AxisLimitData object that contains
	*  the preferred axis limits and tick mark spacing for the
	*  specified range of data values for this log10 axis scale.
	*
	*  @param  lb   The lower bounds of the data plotted on this axis.
	*  @param  ub   The upper bounds of the data plotted on this axis.
	**/
	public AxisLimitData findGoodLimits(double lb, double ub) {
	
		//	Make sure we don't have a degenerate case.
		if (Math.abs(ub - lb) <= 0.000001) {
			if ( ub > 0. ) {
				ub = 2.*ub;
				lb = 1.;
			}
			if (Math.abs(ub - lb) <= 0.000001) {
				lb = lowerBounds();
				ub = upperBounds();
			}
		}
		
		if (DEBUG) {
			System.out.println("In Log10AxisScale.findGoodLimits()...");
			System.out.println("   lb = " + lb + ", ub = " + ub);
		}
		
		//	The lower limit, upper limit and tick mark spacing
		//	being calculated.
		double s = 1.0, t = 0., r = 0.;
		
		for (; lb*s < 1.; s *= 10.);
		for (r=1./s; 10.*r <= lb; r *= 10.);
		for (t=1./s; t < ub; t *= 10.);
		
		AxisLimitData limData = new AxisLimitData();
		limData.lb = r;
		limData.ub = t;
		
		if (ub/lb < 100.) {
			if (lb >= 5.*limData.lb)
				limData.lb *= 5.;
			else if (lb >= 2.*limData.lb)
				limData.lb *= 2.;
			
			if (ub*5. <= limData.ub)
				limData.ub /= 5.;
			else if (ub*2. < limData.ub)
				limData.ub /= 2.;
			
		}
		
		//	Save off the tick mark spacing.
		limData.quantum = r;
		
		if (DEBUG) {
			System.out.println("   limData.lb = " + limData.lb + ", limData.ub = " +
									limData.ub);
			System.out.println("   limData.quantum = " + limData.quantum);
		}
		return limData;
	}
	
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
										double xA, double xB) {
		//	Number of decades on axis.
		int decades = (int)MathTools.log10(Math.abs(aUB/quantum));
		
		if (DEBUG) {
			System.out.println("In Log10AxisScale.calcTickMarks()...");
			System.out.println("   quantum = " + quantum + ", aLB = " + aLB +
											", aUB = " + aUB);
			System.out.println("   xA = " + xA + ", xB = " + xB);
			System.out.println("   decades = " + decades);
		}
		
		//	Determine the number of tick marks.
		int xn = 0;
		for (double x = quantum; x < aUB; x *= 10.) {

			//	Count major tick marks.
			if (1.001*aLB < x && 0.999*aUB > x)
				++xn;
			if (decades < 12) {
				for (int i=2; i < 10; ++i) {
					double q = i*x;
					//	Count minor tick marks.
					if (1.001*aLB < q && 0.999*aUB > q)
						++xn;
				}
			}
			
		}
		
		if (DEBUG)
			System.out.println("   xn = " + xn);
		
		//	Allocate memory for tick mark arrays.
		int[] mark = new int[xn];
		int[] lmark = new int[xn];
		float[] markValue = new float[xn];
		
		//	Step through the tick marks.
		int j=0;
		for (double x = quantum; x < aUB; x *= 10.) {

			//	Major tick mark.
			lmark[j] = 3*PlotAxis.kTick;
			markValue[j] = (float)x;
			if (1.001*aLB < x && 0.999*aUB > x)
				//	Store tick mark in buffer.
				mark[j++] = (int)(func(x)*xA + xB);
			
			if (decades < 12) {
				for (int i=2; i < 10; ++i) {
					double q = i*x;
					
					//	Minor tick mark.
					lmark[j] = PlotAxis.kTick;
					markValue[j] = (float)q;
					if (1.001*aLB < q && 0.999*aUB > q)
						//	Store tick mark in buffer.
						mark[j++] = (int)(func(q)*xA + xB);
					
				}
			}
			
		}
		
		if (DEBUG) {
			System.out.println("Tick mark values:");
			int length = markValue.length;
			for (int i=0; i < length; ++i)
				System.out.print("   " + markValue[i]);
			System.out.println();
		}
		
		//	Create a tick mark object and send it out.
		TickMarkData data = new TickMarkData();
		data.mark = mark;
		data.lmark = lmark;
		data.markValue = markValue;
		
		return data;
	}
	
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
									AxisLimitData output) {
	
		double y = datum.y;
		double yErr = datum.getYError();
		double temp = (y > yErr ? y - yErr : y);
		aLB = Math.min(aLB, temp);
		aUB = Math.max(aUB, datum.y + datum.getYError());
		
		output.lb = aLB;
		output.ub = aUB;
		
	}
	
	
	private double modfloor(double f, double t) {
		t = Math.abs(t);
		return (Math.floor(f/t)*t);
	}
	
}


