/*
*   ContourPlot  -- A simple 2D contour plot of gridded 3D data.
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
*  <p> An object that represents a simple 2D contour plot
*      of 3D data.
*  </p>
*  <p> Grid styles, axis formats, and axis labels can
*      be changed by accessing this object's axes and
*      making the changes there.  Then call repaint()
*      on the component containing this plot.
*  </p>
*  <p> This object's run list contains the contour lines
*      being plotted.  Each run represents a contour level.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  November 12, 2000
*  @version November 20, 2000
**/
public class ContourPlot extends SimplePlotXY {

	//	Debug flag.
	private static final boolean DEBUG = false;
	
	//	The contour paths displayed in this plot.
	private ContourPath[] paths = null;
	
	//-------------------------------------------------------------------------
	/**
	*  Only subclasses can access the default constructor.
	**/
	protected ContourPlot() { }
	
	/**
	*  Creates an contour plot of the specified gridded, 3D, data.
	*
	*  @param  xArr     A 2D array of X coordinate values.
	*  @param  yArr     A 2D array of Y coordinate values.
	*  @param  zArr     A 2D array of Z coordinate values.
	*  @param  nc       The number of contour levels to plot.
	*  @param  intType  Indicates if the intervals should be log (true),
	*                   or linear (false).
	*  @param  title    The title to be displayed across the top
	*                   of the plot.
	*  @param  xLabel   The label to be displayed along the X-axis.
	*  @param  yLabel   The label to be displayed along the Y-axis.
	*  @param  xFormat  The number format to be used for the X axis
	*                   tick mark labels.
	*  @param  yFormat  The number format to be used for the Y axis
	*                   tick mark labels.
	**/
	public ContourPlot( double[][] xArr, double[][] yArr, double[][] zArr, int nc, boolean intType,
						String title, String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat )  {

		this.setTitle(title);
		
		createPlot(xArr, yArr, zArr, xLabel, yLabel, xFormat, yFormat, nc, intType);
		
	}

	private void createPlot( double[][] xArr, double[][] yArr, double[][] zArr,
						String xLabel, String yLabel,
						NumberFormat xFormat, NumberFormat yFormat,
						int nc, boolean logIntervals) {
		try {
		
			//	Generate the contours.
			ContourGenerator cg = new ContourGenerator(xArr, yArr, zArr, nc, logIntervals);
			paths = cg.getContours();
			int npaths = paths.length;
		
			if (DEBUG) {
				System.out.println("Number of contours = " + nc);
				System.out.println("Number of contour paths = " + npaths);
			}
			
			//	Get this plots list of runs.
			PlotRunList runs = this.getRuns();
			runs.clear();
		
			//	Create an empty run for each contour level.
			for (int i=0; i < nc; ++i)
				runs.add( new PlotRun() );
		
			//	Loop over all the contour paths, adding them to the appropriate
			//	contour level.
			for (int j=0; j < npaths; ++j) {
				//	Retrieve the contour path data.
				double[] xData = paths[j].getAllX();
				double[] yData = paths[j].getAllY();
				int levelIndex = paths[j].getLevelIndex();
			
				if (DEBUG) {
					System.out.println();
					System.out.println("LevelIdx = " + levelIndex);
				}
				
				//	Retrieve the appropriate run.
				PlotRun run = (PlotRun)runs.get(levelIndex);
			
				//	Add this path the the retrieved run, one data point at a time.
				int numPoints = xData.length;
				for (int i=0; i < numPoints; ++i) {
					run.add( new PlotDatum(xData[i], yData[i], i != 0) );
					if (DEBUG)
						System.out.println("X = " + (float)xData[i] + ",  Y = " + (float)yData[i]);
				}
			}
			
		} catch (InterruptedException e) {
			//	Shouldn't be possible here.
			e.printStackTrace();
		}
		
		//	Create our plot axes and add them to this plot.
		PlotAxis axis = new PlotXAxis(xLabel, null, xFormat, PlotAxis.kMajorGrid);
		this.setHorizontalAxis(axis);
		axis = new PlotYAxis(yLabel, null, yFormat, PlotAxis.kMajorGrid);
		this.setVerticalAxis(axis);
		
	}

	/**
	*  Colorize the contours by linearly interpolating between
	*  the specified colors for this plot's range of contour levels.
	**/
	public void colorizeContours(Color lowColor, Color highColor) {
		
		//	Find the range of levels in the contours.
		double minLevel = Double.MAX_VALUE;
		double maxLevel = -minLevel;
		int npaths = paths.length;
		for (int i=0; i < npaths; ++i) {
			double level = paths[i].getAttributes().getLevel();
			minLevel = Math.min(minLevel, level);
			maxLevel = Math.max(maxLevel, level);
		}
		
		//	Now assign the colors.
		PlotRunList runs = getRuns();
		for (int i=0; i < npaths; ++i) {
			//	Extract contour path information.
			double level = paths[i].getAttributes().getLevel();
			int levelIndex = paths[i].getLevelIndex();
			
			//	Retrieve the appropriate run.
			PlotRun run = (PlotRun)runs.get(levelIndex);
			
			//	Colorize the run.
			run.setLineColor(interpColors(lowColor, highColor, minLevel, maxLevel, level));
		}
	}
	
	/**
	*  Interpolate the colors for the contour level.
	**/
	private Color interpColors(Color lowColor, Color highColor,
						double minLevel, double maxLevel, double level) {
		level -= minLevel;
		double range = maxLevel - minLevel;
		double temp = range - level;
		
		Color color = new Color(
			(int)(( temp*lowColor.getRed() + level*highColor.getRed() )/range),
			(int)(( temp*lowColor.getGreen() + level*highColor.getGreen() )/range),
			(int)(( temp*lowColor.getBlue() + level*highColor.getBlue() )/range) );
		
		return color;
	}
	
	/**
	*  Make a copy of this ContourPlot object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		ContourPlot newObject = null;
		
		// Make a shallow copy of this object.
		newObject = (ContourPlot) super.clone();

		// Make a copy of this object's data structures.
		int length = this.paths.length;
		newObject.paths = new ContourPath[length];
		for (int i=0; i < length; ++i)
			newObject.paths[i] = (ContourPath)this.paths[i].clone();

		// Output the newly cloned object.
		return newObject;
	}

}

