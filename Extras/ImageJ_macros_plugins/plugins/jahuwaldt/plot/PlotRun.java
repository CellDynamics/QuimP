/*
*   PlotRun  -- Container for a list of plot data points.
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
import java.util.*;


/**
*  <p> This class represents a single run of data in a plot.
*      A run is an array or list of PlotDatum objects.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version December 12, 2000
**/
public class PlotRun extends AbstractList implements Cloneable, java.io.Serializable {

	/**
	*  The plot datum objects are stored in an ArrayList.
	**/
	private List data = new ArrayList();
	
	
	//-------------------------------------------------------------------------
	/**
	*  Create an empty run that contains no data.
	**/
	public PlotRun() { }
	
	/**
	*  Create a run that contains the specified array of PlotDatum objects.
	*
	*  @param run  An array of PlotDatum objects that make up a run of data
	*              to be plotted.
	**/
	public PlotRun( PlotDatum[] run) {
		int length = run.length;
		for (int i=0; i < length; ++i)
			data.add(run[i]);
		
	}

	/**
	*  Create a run from a set of Java arrays for the X & Y data.
	*
	*  @param  xArr  An array containing the X coordinates of the data points
	*                to be plotted.
	*  @param  yArr  An array containing the Y coordinates of the data points
	*                to be plotted.
	*  @param  connectFlg  Set to true to have the points in the X & Y arrays
	*                      connected by a line, false for them to not be connected.
	*  @param  symbol      The plot symbol to use for the plotted points.
	*
	*  @throws NullPointerException if either array is null.
	*  @throws ArrayIndexOutOfBoundsException if the X and Y arrays are not the
	*          same length.
	**/
	public PlotRun(double[] xArr, double[] yArr, boolean connectFlg, PlotSymbol symbol) {
	
		if (xArr == null || yArr == null)
			throw new NullPointerException();
			
		int length = xArr.length;
		if (yArr.length != length)
			throw new ArrayIndexOutOfBoundsException();
		
		for (int i=0; i < length; ++i) {
			data.add(new PlotDatum(xArr[i], yArr[i], connectFlg, symbol));
		}
		
	}
	
	/**
	*  Create a run that contains the PlotDatum objects in the specified
	*  Collection.
	*
	*  @param data  An Collection containing PlotDatum objects.
	**/
	public PlotRun( Collection run ) {
	
		if (run instanceof PlotRun)
			data.addAll(run);
		else {
			for (Iterator i=run.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof PlotDatum)
					data.add(obj);
			}
		}
		
	}
	
	//-------------------------------------------------------------------------
	/**
	*  Return the minimum X value of the data contained in this run.
	**/
	public double getMinX() {
		double min = Double.MAX_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			min = Math.min(min, datum.x);
		}
		
		return min;
	}
	
	/**
	*  Return the maximum X value of the data contained in this run.
	**/
	public double getMaxX() {
		double max = Double.MIN_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			max = Math.max(max, datum.x);
		}
		
		return max;
	}
	
	/**
	*  Return the minimum Y value of the data contained in this run.
	**/
	public double getMinY() {
		double min = Double.MAX_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			min = Math.min(min, datum.y);
		}
		
		return min;
	}
	
	/**
	*  Return the maximum Y value of the data contained in this run.
	**/
	public double getMaxY() {
		double max = Double.MIN_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			max = Math.max(max, datum.y);
		}
		
		return max;
	}
	
	/**
	*  Use this method to change the plot symbol used by all
	*  the plot data points in this run.
	**/
	public void setPlotSymbol(PlotSymbol symbol) {
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			datum.setPlotSymbol(symbol);
		}
		
	}
	
	/**
	*  Use this method to change the line color used by all
	*  the plot data points in this run.
	**/
	public void setLineColor(Color color) {
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotDatum datum = (PlotDatum)i.next();
			datum.setLineColor(color);
		}
		
	}
	
	
	//**** Methods required to implement AbstractList  ******
	
	/**
	*  Returns the number of PlotDatum objects in this run.
	*
	*  @return The number of plot data objects in this run.
	**/
	public int size() {
		return data.size();
	}
	
	/**
	*  Returns the PlotDatum object at the specified position
	*  in this run.
	*
	*  @param   index  The index of the plot data object to return.
	*  @return  The PlotDatum object at the specified position
	*           in this run.
	**/
	public Object get(int index) {
		return data.get(index);
	}
	
	/**
	*  Replaces the plot data element at the specified position
	*  in this run with the specified datum.
	*
	*  @param   index   The index of the data element to replace.
	*  @param   element The datum to be stored a the specified position.
	*  @return  The datum previously at the specified position in this run.
	*  @throws  ClassCastException - if the specified element is not a
	*                                PlotDatum type object.
	**/
	public Object set(int index, Object element) {
		PlotDatum obj = (PlotDatum) element;
		return data.set(index, element);
	}
	
	/**
	*  Inserts the specified plot data element at the specified
	*  position in this run.  Shifts the plot data element
	*  currently at that position (if any) and any subsequent
	*  data elements to the right (adds one to their indices).
	*
	*  @param  index   Index at which the specified datum is to be
	*                  inserted.
	*  @param  element PlotDatum object to be inserted.
	*  @throws ClassCastException - if the specified element is not a
	*                               PlotDatum type object.
	**/
	public void add(int index, Object element) {
		PlotDatum obj = (PlotDatum) element;
		data.add(index, element);
	}
	
	/**
	*  Remove the plot data object at the specified position in
	*  this run.  Shifts any subsequent data elements 
	*  to the left (subtracts one from their indices).  Returns the
	*  data element that was removed from this run.
	*
	*  @param   index  The index of the plot data element to remove.
	*  @return  The PlotDatum object previously at the specified position.
	**/
	public Object remove(int index) {
		return data.remove(index);
	}
	
	/**
	*  Removes all the plot data elements from this run.
	*  The run will be empty after this call returns
	*  (unless it throws an exception).
	**/
	public void clear() {
		data.clear();
	}
	
	/**
	*  Return an enumeration of all the plot data elements in
	*  this run.
	*
	*  @return An interation of all the PlotDatum objects in this run.
	**/
	public Iterator iterator() {
		return data.iterator();
	}
	
	/**
	*  Make a copy of this PlotRun object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		PlotRun newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (PlotRun) super.clone();

			// Now clone the data points attached to this run.
			newObject.data = new ArrayList();
			int size = this.data.size();
			for (int i=0; i < size; ++i) {
				Object datum = ((PlotDatum)this.data.get(i)).clone();
				newObject.data.add(datum);
			}

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

}


