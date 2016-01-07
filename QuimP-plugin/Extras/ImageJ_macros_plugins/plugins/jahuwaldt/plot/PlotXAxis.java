/*
*   ContourPath  -- Represents a horizontal or X plot axis.
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
import java.text.*;
import java.util.*;


/**
*  <p> This class represents a horizontal or X axis
*      on a plot.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version April 25, 2002
**/
public class PlotXAxis extends PlotAxis {

	//	Debug flag.
	private static final boolean DEBUG = false;
	
	//-------------------------------------------------------------------------
	/**
	*  Create a new X axis using the default settings.
	**/
	public PlotXAxis() { }
	
	/**
	*  Create a X axis with the label, axis scale transformation,
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
	public PlotXAxis( String label, PlotAxisScale scale, NumberFormat nf, int gridStyle ) {
		super(label, scale, nf, gridStyle);
	}

	//-------------------------------------------------------------------------
	/**
	*  Automatically set the upper and lower bounds for this
	*  X axis based on the data in the runs input.
	**/
	public void setAxisBounds(PlotRunList runs) {
		
		if (DEBUG) {
			System.out.println("In PlotXAxis.setAxisBounds()...");
		}
		
		if (manualLB && manualUB)	return;

		if (!manualLB)
			aLB = Double.MAX_VALUE;
		if (!manualUB)
			aUB = Double.MIN_VALUE;
				
		
		boolean dataFound = false;
		
		if (runs != null && runs.size() > 0) {
			//	Loop over all the runs.
			for (Iterator i=runs.iterator(); i.hasNext();) {
		
				//	Extract the current run.
				PlotRun run = (PlotRun)i.next();
				if (run.size() > 0) {
				
					//	The runs contain data.
					dataFound = true;
					
					//	Loop over all the data points.
					for (Iterator j=run.iterator(); j.hasNext();) {
						PlotDatum datum = (PlotDatum)j.next();
					
						//	Find axis bounds for X axis.
						if (!manualLB)
							aLB = Math.min(aLB, datum.x);
						
						if (!manualUB)
							aUB = Math.max(aUB, datum.x);
					}
				}
			}
		
		}
		
		if (!dataFound) {
			//	If no data found, set default scales.
			if (!manualLB)
				aLB = scale.lowerBounds();
			if (!manualUB)
				aUB = scale.upperBounds();
		}
		
	}
	
	/**
	*  Scale this axis so that it fits inside of the specified
	*  plot frame rectangle.  Horizontal axes must fit inside
	*  the width of the frame.
	*
	*  @param  plotFrame  The bounding rectangle for the data area
	*                     of the plot (overall bounds minus area for
	*                     titles, labels, etc).
	**/
	protected void resizeAxis(Rectangle plotFrame) {
		
		if (DEBUG) {
			System.out.println("In PlotXAxis.scaleAxis()...");
			System.out.println("    plotFrame = " + plotFrame);
			System.out.println("    aLB = " + aLB + ", aUB = " + aUB);
		}
		
		//	Right edge of axis in screen coordinates.
		int topx = plotFrame.x + plotFrame.width;
		
		//	Left edge of axis in screen coordinates.
		int botx = plotFrame.x;
		int edge = topx - botx;
		
		//	Set scaling factors in class variables.
		
		//	xA = (Scrn Height)/(Data Height)
		xA = (double)edge/(scale.func(aUB) - scale.func(aLB));
		xB = botx - scale.func(aLB)*xA + 0.5;
		
		if (DEBUG) {
			System.out.println("    xA = " + xA + ", xB = " + xB);
		}
		
	}
	
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
	public void draw(Graphics gc, Component comp, Rectangle bounds) {

		//	Extract information on the current font.
		FontMetrics fm = gc.getFontMetrics();
		int textHeight = fm.getHeight();
		int textWidth = 0;
		int xPos = 0, yPos = 0;
		
		if (DEBUG)
			System.out.println("In PlotXAxis.draw()...");
			
		int botPos = bounds.x;
		int topPos = botPos + bounds.width;
		int top = bounds.y;
		int bottom = top + bounds.height;

		if (gridStyle != kNoGrid) {
		
			Color foreColor = gc.getColor();
		
			//	Create tick marks on this axis.
			TickMarkData ticks = scale.calcTickMarks(quantum, aLB, aUB, xA, xB);
			int[] mark = ticks.mark;
			int[] lmark = ticks.lmark;
			float[] markValue = ticks.markValue;
			int xn = mark.length;
			
			if (DEBUG) {
				System.out.println("    xn = " + xn);
				System.out.println("    mark[0] = " + markValue[0] + ", mark[1] = " +
											markValue[1]);
			}
			
			//	Draw label on the first tick mark.
			String str = nf.format(2*markValue[0] - markValue[1]);
			textWidth = fm.stringWidth(str);
		
			xPos = botPos - textWidth/2;
			yPos = bottom + textHeight;
			gc.drawString(str, xPos, yPos);
		
			//	Draw intermediate tick marks and labels.
			for (int i=0; i < xn; ++i) {
				xPos = mark[i];
			
				//	Draw full grid at major tick mark if requested.
				if (lmark[i] == 3*kTick && gridStyle == kMajorGrid) {
					gc.setColor(Color.gray);
					gc.drawLine(xPos, top, xPos, bottom);
					gc.setColor(foreColor);
				}
			
				//	Draw tick marks.
				gc.drawLine(xPos, top, xPos, top + lmark[i]);
				gc.drawLine(xPos, bottom - lmark[i], xPos, bottom);
				
				//	Draw numeric labels next to major tick marks.
				if (lmark[i] == 3*kTick) {
					str = nf.format(markValue[i]);
					textWidth = fm.stringWidth(str);
				
					xPos -= textWidth/2;
					gc.drawString(str, xPos, yPos);
				}
			}
		
			//	Draw label on last tick mark.
			str = nf.format(2*markValue[xn-1] - markValue[xn-2]);
			textWidth = fm.stringWidth(str);
		
			xPos = topPos - textWidth/2;
			gc.drawString(str, xPos, yPos);
		}
		
		
		//	Draw the axis label.
		
		//	Center label under axis.
		if (label != null) {
		    textWidth = fm.stringWidth(label);
		    yPos = bottom + 2*textHeight;
	    	xPos = (botPos + topPos)/2 - textWidth/2;
		
		    gc.drawString(label, xPos, yPos);
		}
	}
	
	
}


