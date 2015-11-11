/*
*   LinearAxisScale  -- Provides linear axis scaling for plot axes.
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
*  <p> This object provides linear scaling for plot axes.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version January 10, 2001
**/
public class LinearAxisScale extends Object implements PlotAxisScale {
	
	//	Debug flag.
	private static final boolean DEBUG = false;
	
	
	/**
	*  The transformation function used to scale the data
	*  plotted against this axis.
	*  This axis uses a linear scaling function:  f(a) = a.
	**/
	public final double func(double a) {
		return a;
	}
	
	/**
	*  Method that returns the default lower bounds for
	*  this axis scale.  Returns -1.0.
	**/
	public double lowerBounds() {
		return -1.0;
	}
	
	/**
	*  Method that returns the default upper bounds for
	*  this axis scale.  Returns 1.0.
	**/
	public double upperBounds() {
		return 1.0;
	}
	
	/**
	*  Method that returns an AxisLimitData object that contains
	*  the preferred axis limits and tick mark spacing for the
	*  specified range of data values for this linear axis scale.
	*
	*  @param  aLB  The lower bounds of the data plotted on this axis.
	*  @param  aUB  The upper bounds of the data plotted on this axis.
	**/
	public AxisLimitData findGoodLimits(double aLB, double aUB) {
		//	The lower limit and tick mark spacing being calculated.
		double s = 0., r = 0.;
		
		//	Make sure we don't have a degenerate case.
		if (Math.abs(aUB - aLB) <= 0.000001) {
			if ( aUB > 0. ) {
				aUB = 2.*aUB;
				aLB = 0.;
			} else if ( aLB < 0 ) {
				aLB = 2.*aLB;
				aUB = 0.;
			}
			if (Math.abs(aUB - aLB) <= 0.000001) {
				aLB = lowerBounds();
				aUB = upperBounds();
			}
		}
		
		if (DEBUG) {
			System.out.println("In findGoodLimits()...");
			System.out.println("   aLB = " + aLB + ", aUB = " + aUB);
		}
		
		//	Object used to return results.
		AxisLimitData limData = new AxisLimitData();
		
		boolean done = false;
		while (!done) {
			done = true;
			
			double ub = aUB;
			double lb = aLB;
			double delta = ub - lb;
			
			//	Scale up by s, a power of 10, so range (delta) exceeds 1.
			s = 1.;
			while (delta*s < 10.)	s *= 10.;
			
			//	Find power of 10 quantum, r, such that delta/10 <= r < delta.
			r = 1./s;
			while (10.*r < delta)	r *= 10.;
			
			//	Set r=(1,2,5)*10**n so that 3-5 quanta cover range.
			if ( r >= delta/2. )	r /= 2.;
			else if (r < delta/5.)	r *= 2.;
			
			limData.ub = modceil(ub, r);
			limData.lb = modfloor(lb,r);
			
			//	If lower bound is <= r and > 0, then repeat.
			if (limData.lb <= r && limData.lb > 0.) {
				aLB = 0.;
				done = false;
			
			//	If upper bound >= -r and < 0, then repeat.
			} else if (limData.ub >= -r && limData.ub < 0.) {
				aUB = 0.;
				done = false;
			}
		}
		
		//	Save off tick mark spacing.
		limData.quantum = r;
		
		if (DEBUG) {
			System.out.println("    limData.lb = " + limData.lb + ", limData.ub = " +
									limData.ub + ", limData.quantum = " + limData.quantum);
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
	*  @return An TickMarkData object containing the tick mark positions, lengths,
	*          and data values at each tick mark.
	**/
	public TickMarkData calcTickMarks(double quantum, double aLB, double aUB,
										double xA, double xB) {
		double xl = 0;
		double xu = 0;
		double q = quantum;
		if (q > 0.) {
			xl = modfloor(aLB, q);
			xu = modfloor(aUB - q/10., q/5.) + q/10.;
			
		} else {
			xl = modfloor(aUB, q);
			xu = modfloor(aLB + q/10., q/5.) - q/10.;
		}
		
		//	Determine the number of tick marks.
		int xn = (int)( (xu - xl)/(Math.abs(quantum)/5.) );
		
		if (DEBUG) {
			System.out.println("In calcTickMarks()...");
			System.out.println("    quantum = " + quantum + ", aLB = " + aLB + ", aUB = " + aUB);
			System.out.println("    xA = " + xA + ", xB = " + xB);
			System.out.println("    xl = " + xl + ", xu = " + xu);
			System.out.println("    xn = " + xn);
		}
		
		//	Allocate memory for tick mark arrays.
		int[] mark = new int[xn];
		int[] lmark = new int[xn];
		float[] markValue = new float[xn];
		
		xn = 0;
		int i=0;
		for (double x=xl; x <= xu; x += Math.abs(quantum)/5., ++i) {
			
			//	Bounds check.
			if (q > 0.)
				if (x <= aLB || x >= aUB) continue;
			if (q < 0.)
				if (x >= aLB || x <= aUB) continue;
			
			//	Store tick mark.
			mark[xn] = (int)(func(x)*xA + xB);
			markValue[xn] = (float)x;
			if ( i%5 != 0)
				//	Minor tick mark.
				lmark[xn++] = PlotAxis.kTick;
			else
				//	Major tick mark.
				lmark[xn++] = 3*PlotAxis.kTick;
		}
		
		if (DEBUG) {
			System.out.println("Tick mark values:");
			int length = markValue.length;
			for (i=0; i < length; ++i)
				System.out.print("   " + markValue[i]);
			System.out.println();
		}
		
		//	Create a tick mark data object and pass it out.
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
	
		double temp = datum.getYError();
		aLB = Math.min(aLB, datum.y - temp);
		aUB = Math.max(aUB, datum.y + temp);
		
		output.lb = aLB;
		output.ub = aUB;
		
	}
	
	private double modceil(double f, double t) {
		t = Math.abs(t);
		return (Math.ceil(f/t)*t);
	}
	
	private double modfloor(double f, double t) {
		t = Math.abs(t);
		return (Math.floor(f/t)*t);
	}
	
}


