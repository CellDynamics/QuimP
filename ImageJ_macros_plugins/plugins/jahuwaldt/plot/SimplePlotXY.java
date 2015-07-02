/*
*   SimplePlotXY  -- Represents a simple X-Y scatter graph type data plot.
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
import javax.swing.*;
import java.util.*;
import java.text.NumberFormat;


/**
*  <p> An object that represents a simple XY scatter
*      graph type plot.  X is the horizontal axis
*      and Y is the vertical axis.
*  </p>
*  <p> Grid styles, axis formats, and axis labels can
*      be changed by accessing this object's axes and
*      making the changes there.  Then call repaint()
*      on the component containing this plot.
*  </p>
*  <p> The data being plotted can be
*      changed by accessing this object's run list
*      and making the changes to the runs and
*      data points there.  Then call repaint()
*      on the component containing this plot.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 15, 2000
*  @version December 2, 2000
**/
public class SimplePlotXY implements Plot2D {

	//	Debug flag.
	private static final boolean DEBUG = false;
	
	/**
	*  The title to be displayed across the top of the plot.
	**/
	private String plotTitle;

	/**
	*  The list of runs plotted in this panel.
	**/
	private PlotRunList runs = new PlotRunList();
	
	/**
	*  The X and Y axes for this plot.
	**/
	private PlotAxis xAxis, yAxis;
	
	
	//-------------------------------------------------------------------------
	/**
	*  Only subclasses can access the default constructor.
	**/
	protected SimplePlotXY() { }
	
	/**
	*  Creates an XY plot (scatter graph) using the data in
	*  the supplied run list.
	*
	*  @param  runList An list of runs of data points to be plotted.
	*  @param  title   The title to be displayed across the top
	*                  of the plot.
	*  @param  xLabel  The label to be displayed along the X-axis.
	*  @param  yLabel  The label to be displayed along the Y-axis.
	*  @param  xFormat The number format to be used for the X axis
	*                  tick mark labels.
	*  @param  yFormat The number format to be used for the Y axis
	*                  tick mark labels.
	**/
	public SimplePlotXY( PlotRunList runList, String title,
						String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat ) {

		plotTitle = title;
		runs = runList;
		
		//	Create our plot axes.
		xAxis = new PlotXAxis(xLabel, null, xFormat, PlotAxis.kMajorGrid);
		yAxis = new PlotYAxis(yLabel, null, yFormat, PlotAxis.kMajorGrid);
		
	}

	/**
	*  Creates an XY plot (scatter graph) using the supplied
	*  X and Y data values, titles, labels, and formats.
	*
	*  @param  xArr    An array of X coordinate values.
	*  @param  yArr    An array of Y coordinate values.
	*  @param  title   The title to be displayed across the top
	*                  of the plot.
	*  @param  xLabel  The label to be displayed along the X-axis.
	*  @param  yLabel  The label to be displayed along the Y-axis.
	*  @param  xFormat The number format to be used for the X axis
	*                  tick mark labels.
	*  @param  yFormat The number format to be used for the Y axis
	*                  tick mark labels.
	**/
	public SimplePlotXY( double[] xArr, double[] yArr, String title,
						String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat ) {

		plotTitle = title;
		
		createPlot(xArr, yArr, xLabel, yLabel, xFormat, yFormat, null);
		
	}

	/**
	*  Creates an XY plot (scatter graph) using the supplied
	*  X and Y data values, titles, labels, formats and plot symbols.
	*
	*  @param  xArr    An array of X coordinate values.
	*  @param  yArr    An array of Y coordinate values.
	*  @param  title   The title to be displayed across the top
	*                  of the plot.
	*  @param  xLabel  The label to be displayed along the X-axis.
	*  @param  yLabel  The label to be displayed along the Y-axis.
	*  @param  xFormat The number format to be used for the X axis
	*                  tick mark labels.
	*  @param  yFormat The number format to be used for the Y axis
	*                  tick mark labels.
	*  @param  symbol  The plot symbol to use for the data points in this plot.
	*                  If null is passed, no symbol will be drawn.
	**/
	public SimplePlotXY( double[] xArr, double[] yArr, String title,
						String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat, PlotSymbol symbol ) {

		plotTitle = title;
		
		createPlot(xArr, yArr, xLabel, yLabel, xFormat, yFormat, symbol);
		
	}

	private void createPlot( double[] xArr, double[] yArr,
						String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat, PlotSymbol symbol) {
		
		//	Create a single run of data.
		int numPoints = xArr.length;
		if (yArr.length != numPoints)
			throw new IllegalArgumentException("Inconsistant array sizes.");
		
		//	Create a data run.
		PlotRun newRun = new PlotRun();
		
		//	Add each data point to the run.
		for (int i=0; i < numPoints; ++i) {
			newRun.add( new PlotDatum(xArr[i], yArr[i], true, symbol) );
		}
		
		//	Add this new run to this graph's run list.
		runs.add(newRun);
		
		//	Create our plot axes.
		xAxis = new PlotXAxis(xLabel, null, xFormat, PlotAxis.kMajorGrid);
		yAxis = new PlotYAxis(yLabel, null, yFormat, PlotAxis.kMajorGrid);
		
	}
	
	//-------------------------------------------------------------------------
	/**
	*  Returns the title assigned to this plot.
	**/
	public String getTitle() {
		return plotTitle;
	}
	
	/**
	*  Set's the title assigned to this plot.
	**/
	public void setTitle(String title) {
		plotTitle = title;
	}

	/**
	*  Return a reference to the plot's vertical or Y axis.
	**/
	public PlotAxis getVerticalAxis() {
		return yAxis;
	}
	
	/**
	*  Set the plot's vertical or Y axis.
	**/
	public void setVerticalAxis(PlotAxis axis) {
		yAxis = axis;
	}
	
	/**
	*  Return a reference to the plot's horizontal or X axis.
	**/
	public PlotAxis getHorizontalAxis() {
		return xAxis;
	}
	
	/**
	*  Set the plot's horizontal or X axis.
	**/
	public void setHorizontalAxis(PlotAxis axis) {
		xAxis = axis;
	}
	
	/**
	*  Return a reference to the list of data runs 
	*  assigned to this plot.
	**/
	public PlotRunList getRuns() {
		return runs;
	}
	
	/**
	*  Set the list of runs to be plotted in this plot.
	**/
	public void setRuns(PlotRunList runList) {
		runs = runList;
	}
	
	/**
	*  Make a copy of this SimplePlotXY object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		SimplePlotXY newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (SimplePlotXY) super.clone();

			// Make a copy of this object's data structures.
			newObject.runs = (PlotRunList)this.runs.clone();
			newObject.xAxis = (PlotAxis)this.xAxis.clone();
			newObject.yAxis = (PlotAxis)this.yAxis.clone();

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

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
	public void draw( Graphics gc, Component comp, Rectangle bounds ) {

		//	Determine the bounds of the data to be plotted.
		xAxis.setAxisBounds(runs);
		yAxis.setAxisBounds(runs);
		
		// Find the bounds of the plot itself (minus labels).
		Rectangle plotBounds = findPlotBounds(bounds, xAxis, yAxis, plotTitle, gc);
		
		// Scale axes to the new plot data bounds.
		xAxis.setSize(plotBounds);
		yAxis.setSize(plotBounds);
		
		// Render this plot image.
		drawTitle(gc, bounds);
		drawAxesAndGrid(gc, comp, plotBounds);
		drawPlotData(gc, plotBounds);
		
	}


	/**
	*  Determines how much space is required for the plot title, axes,
	*  and labels and reduces the plot bounds accordingly.
	*
	*  @param  bounds     The overall bounding rectangle we are drawing into
	*                     including the title and axis labels.
	*  @param  xAxis      Reference to the plot's X axis.
	*  @param  yAxis      Reference to the plot's Y axis.
	*  @param  plotTitle  The title of the plot.
	*  @param  gc         The graphics context being used for rendering.
	*  @return A rectangle that defines the boundaries of the plot itself
	*          without the title, axes labels, etc.
	**/
	private static Rectangle findPlotBounds(Rectangle bounds, PlotAxis xAxis,
							PlotAxis yAxis, String plotTitle, Graphics gc) {

		if (DEBUG)
			System.out.println("In SimplePlotXY.findPlotBounds()...");
			
		FontMetrics fm = null;
		int height = 0;
		int leading = 0;
		int width = 0;
		int offset = 0;
		
		//	Copy over overall bounds rectangle data.
		Rectangle plotBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
		
		//	Make space for the plot title.
		if (plotTitle != null) {
		
			//	Retrieve information on the current font.
			Font currFont = gc.getFont();
			String fontName = currFont.getName();
			int fontSize = currFont.getSize();
		
			//	Enlarge the title text and make it bold.
			Font newFont = new Font(fontName, Font.BOLD, fontSize*2);
			gc.setFont(newFont);
			
			//	Get info about the title font we are using
			fm = gc.getFontMetrics();
			height = fm.getHeight();
			leading = fm.getLeading();
			
			//	Lower top of plot enough to leave room for text.
			offset = height + leading + 1;
			plotBounds.y += offset;
			plotBounds.height -= offset;
			
			//	Restore the previous font.
			gc.setFont(currFont);
		}
		
		//	Make space for the X axis numbers and labels.
		
		//	Get info about the font we are using.
		fm = gc.getFontMetrics();
		height = fm.getHeight();
		leading = fm.getLeading();
		
		//	Raise bottom of plot enough to leave room for axis numbers.
		plotBounds.height -= height + leading + 1;
		
		//	Make room for X axis label.
		if (xAxis.getLabel() != null)
			plotBounds.height -= 1.5*height;
		
		//	Move right bounds to allow room for the last X axis number
		//	to hang over.
		width = fm.stringWidth(xAxis.upperBoundsAsString());
		plotBounds.width -= width/2 + 6;
		
		//	Make space for Y axis numbers and labels.
		
		//	Make room for the axis numbers.
		width = fm.stringWidth(yAxis.upperBoundsAsString());
		int width2 = fm.stringWidth(yAxis.lowerBoundsAsString());
		width = Math.max(width, width2);
		plotBounds.x += width + 4;
		plotBounds.width -= width + 4;
		plotBounds.y += height/2;
		plotBounds.height -= height/2;
		
		//	Make room for the axis label (if there is one).
		if (yAxis.getLabel() != null) {
			plotBounds.x += 1.5*height;
			plotBounds.width -= 1.5*height;
		}
		
		if (DEBUG)
			System.out.println("    plotBounds = " + plotBounds);
		
		return plotBounds;
	}
	
	/**
	*  Draw the plot title onto the plot.
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  bounds  The overall bounding rectangle we are drawing into
	*                  including the title and axis labels.
	**/
	private void drawTitle(Graphics gc, Rectangle bounds) {
		if ( plotTitle != null && !plotTitle.equals("") ) {
			
			//	Retrieve information on the current font.
			Font currFont = gc.getFont();
			String fontName = currFont.getName();
			int fontSize = currFont.getSize();
		
			//	Enlarge the title text and make it bold.
			Font newFont = new Font(fontName, Font.BOLD, fontSize*2);
			gc.setFont(newFont);
			
			//	Get info about the font we are using
			FontMetrics fm = gc.getFontMetrics();
			int textWidth = fm.stringWidth(plotTitle);
			
			//	Center the title in the frame.
			int xPos = bounds.width/2 - textWidth/2;
			int yPos = bounds.y + fm.getAscent() + fm.getLeading() + 1;
			
			//	Render the title.
			gc.drawString(plotTitle, xPos, yPos);
			
			//	Reset the font back to it's previous value.
			gc.setFont(currFont);
		}
	}
	
	/**
	*  Draw the plot's horizontal and vertical axes and
	*  grid lines (depending on grid style set for each
	*  axis).
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  comp    The component that we are drawing into.
	*  @param  bounds  The bounding rectangle for the data area
	*                  of the plot (overall bounds minus area for titles,
	*                  labels, etc).
	**/
	private void drawAxesAndGrid(Graphics gc, Component comp, Rectangle bounds) {
		
		//	Draw a box around the boundary of the plot.
		gc.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		gc.drawRect(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2);
		
		//	Draw the horizontal axis and grid.
		xAxis.draw(gc, comp, bounds);
		
		//	Draw the vertical axis and grid.
		yAxis.draw(gc, comp, bounds);
		
	}
	
	/**
	*  Loop over all the runs and render the data in 
	*  each one in turn.
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  bounds  The bounding rectangle for the data area
	*                  of the plot (overall bounds minus area for titles,
	*                  labels, etc).
	**/
	private void drawPlotData(Graphics gc, Rectangle bounds) {
		
		if (runs != null && runs.size() > 0) {
			
			//	Loop over all the runs.
			for (Iterator i=runs.iterator(); i.hasNext();) {
		
				//	Extract the next run.
				PlotRun run = (PlotRun)i.next();
				if (run.size() > 0)
					drawSingleRun(gc, bounds, run);
			}
		}
	}
	
	/**
	*  Loop over all the data points in a single run
	*  and render them to the plot.
	*
	*  @param  gc      The graphics context we are drawing into.
	*  @param  bounds  The bounding rectangle for the data area
	*                  of the plot (overall bounds minus area for titles,
	*                  labels, etc).
	*  @param  run     A single run of data to be plotted.
	**/
	private void drawSingleRun(Graphics gc, Rectangle bounds, PlotRun run) {
		int xo = 0, yo = 0;
		boolean conFlg = false;
		Color saveColor = gc.getColor();
		
		//	Loop over all the data points to be plotted.
		for (Iterator i=run.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			
			// Check for break in the line.
			conFlg &= datum.connected();
			
			//	Scale data points to screen coordinates.
			int xi = xAxis.scaleCoord(datum.x);
			int yi = yAxis.scaleCoord(datum.y);
				
			//	If this point falls outside the plot bounds, skip it.
			if (xi < bounds.x && xi > bounds.x + bounds.width &&
				yi < bounds.y && yi > bounds.y + bounds.width) {
				conFlg = false;
				continue;
			}
			
			//	Is there an error bar to be drawn?
			if (datum.hasErrorBar()) {
				int topPos = bounds.y;
				int botPos = bounds.y + bounds.height;
				
				//	Deal with error bar.
				int yi1 = yAxis.scaleCoord(datum.y + datum.getYError());
				int yi2 = yAxis.scaleCoord(datum.y - datum.getYError());
				
				//	Start drawing the data point.
				//	Connect the center of the bar.
				if (conFlg) {
					gc.setColor(datum.getLineColor());
					gc.drawLine(xo, yo, xi, yi);
					gc.setColor(saveColor);
					xo = xi;
					yo = yi;
				} else {
					xo = xi;
					yo = yi;
				}
				
				//	If error bar is off top or bottom, adjust.
				if (!bounds.contains(xi, yi1) )
					yi1 = topPos;
				if (!bounds.contains(xi, yi2) )
					yi2 = botPos;
				
				//	Height of error bar.
				int delta = yi1 - yi2;
				delta /= 5;
				int count = run.size();
				int bar = 3*PlotAxis.kTick*(count+10)/(3*count+10);
				int dbar = (bar < delta) ? bar : delta;
				int xi1 = (botPos < xi - dbar) ? xi - dbar : botPos;
				int xi2 = (topPos > xi + dbar) ? xi + dbar : topPos;
				
				//	Draw first segment of error bar.
				gc.drawLine(xi1, yi1, xi2, yi1);
				
				//	Draw 2nd segment of error bar.
				gc.drawLine(xi1, yi2, xi2, yi2);
				gc.drawLine(xi, yi1, xi, yi2);
				
			} else {
				//	Must not have an error bar.
				//	Just draw the line and point.
				if (conFlg) {
					gc.setColor(datum.getLineColor());
					gc.drawLine(xo, yo, xi, yi);
					gc.setColor(saveColor);
					xo = xi;
					yo = yi;
				} else {
					xo = xi;
					yo = yi;
				}
			}
			
			//	Draw the plot symbol.
			PlotSymbol symbol = datum.getPlotSymbol();
			if (symbol != null)
				symbol.draw(gc, xi, yi);

			//	Prepare for next data point.
			conFlg = true;
		}
	}
	
	
}


