/*
*   PlotRunList  -- Container for a list of runs to be plotted.
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
*  <p> This class represents a list of runs of data in a plot.
*      A run is an array or list of PlotDatum objects.  A run
*      list is an array or list of runs.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version November 20, 2000
**/
public class PlotRunList extends AbstractList implements Cloneable, java.io.Serializable {

	/**
	*  The run objects are stored in an ArrayList.
	**/
	private List data = new ArrayList();
	
	
	//-------------------------------------------------------------------------
	/**
	*  Create an empty run list that contains no runs.
	**/
	public PlotRunList() { }
	
	/**
	*  Create a run list that contains the specified
	*  array of PlotRun objects.
	*
	*  @param run  An array of PlotRun objects that make up a list
	*              of runs to be plotted.
	**/
	public PlotRunList( PlotRun[] runArr) {
		int length = runArr.length;
		for (int i=0; i < length; ++i)
			data.add(runArr[i]);
		
	}

	/**
	*  Create a runlist  that contains the PlotRun objects in the specified
	*  Collection.
	*
	*  @param data  An Collection containing PlotRun objects.
	**/
	public PlotRunList( Collection runs ) {
	
		if (runs instanceof PlotRunList)
			data.addAll(runs);
		else {
			for (Iterator i=runs.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof PlotRun)
					data.add(obj);
			}
		}
		
	}
	
	//-------------------------------------------------------------------------
	/**
	*  Return the minimum X value of the data contained in this
	*  run list.
	**/
	public double getMinX() {
		double min = Double.MAX_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			min = Math.min(min, run.getMinX());
		}
		
		return min;
	}
	
	/**
	*  Return the maximum X value of the data contained in this
	*  run list.
	**/
	public double getMaxX() {
		double max = Double.MIN_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			max = Math.max(max, run.getMaxX());
		}
		
		return max;
	}
	
	/**
	*  Return the minimum Y value of the data contained in this
	*  run list.
	**/
	public double getMinY() {
		double min = Double.MAX_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			min = Math.min(min, run.getMinY());
		}
		
		return min;
	}
	
	/**
	*  Return the maximum Y value of the data contained in this
	*  run list.
	**/
	public double getMaxY() {
		double max = Double.MIN_VALUE;
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			max = Math.max(max, run.getMaxY());
		}
		
		return max;
	}
	
	/**
	*  Use this method to change the plot symbol used by all
	*  the plot data points in all the runs in this run list.
	**/
	public void setPlotSymbol(PlotSymbol symbol) {
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			run.setPlotSymbol(symbol);
		}
		
	}
	
	/**
	*  Use this method to change the line color used by all
	*  the plot data points in all the runs in this run list.
	**/
	public void setLineColor(Color color) {
		
		for (Iterator i=data.iterator(); i.hasNext();) {
			PlotRun run = (PlotRun)i.next();
			run.setLineColor(color);
		}
		
	}
	
	
	//**** Methods required to implement AbstractList  ******
	
	/**
	*  Returns the number of PlotRun objects in this run list.
	*
	*  @return The number of plot run objects in this run list.
	**/
	public int size() {
		return data.size();
	}
	
	/**
	*  Returns the PlotRun object at the specified position
	*  in this run list.
	*
	*  @param   index  The index of the plot run object to return.
	*  @return  The PlotRun object at the specified position
	*           in this run.
	**/
	public Object get(int index) {
		return data.get(index);
	}
	
	/**
	*  Replaces the plot run element at the specified position
	*  in this run list with the specified run.
	*
	*  @param   index   The index of the data run to replace.
	*  @param   element The run to be stored a the specified position.
	*  @return  The run previously at the specified position in this list.
	*  @throws  ClassCastException - if the specified element is not a
	*                                PlotRun type object.
	**/
	public Object set(int index, Object element) {
		PlotRun obj = (PlotRun) element;
		return data.set(index, element);
	}
	
	/**
	*  Inserts the specified plot run element at the specified
	*  position in this run list.  Shifts the plot run element
	*  currently at that position (if any) and any subsequent
	*  runs to the right (adds one to their indices).
	*
	*  @param  index   Index at which the specified run is to be
	*                  inserted.
	*  @param  element PlotRun object to be inserted.
	*  @throws ClassCastException - if the specified element is not a
	*                               PlotRun type object.
	**/
	public void add(int index, Object element) {
		PlotRun obj = (PlotRun) element;
		data.add(index, element);
	}
	
	/**
	*  Remove the plot run object at the specified position in
	*  this run list.  Shifts any subsequent run elements 
	*  to the left (subtracts one from their indices).  Returns the
	*  run element that was removed from this run list.
	*
	*  @param   index  The index of the plot run element to remove.
	*  @return  The PlotRun object previously at the specified position.
	**/
	public Object remove(int index) {
		return data.remove(index);
	}
	
	/**
	*  Removes all the plot run elements from this run list.
	*  The run list will be empty after this call returns
	*  (unless it throws an exception).
	**/
	public void clear() {
		data.clear();
	}
	
	/**
	*  Return an enumeration of all the plot run elements in
	*  this run list.
	*
	*  @return An interation of all the PlotRun objects in this list.
	**/
	public Iterator iterator() {
		return data.iterator();
	}
	
	/**
	*  Make a copy of this PlotRunList object.
	*
	*  @return  Returns a clone of this object.
	**/
	public Object clone() {
		PlotRunList newObject = null;
		
		try {
			// Make a shallow copy of this object.
			newObject = (PlotRunList) super.clone();

			// Now clone the runs attached to this list.
			newObject.data = new ArrayList();
			int size = this.data.size();
			for (int i=0; i < size; ++i) {
				Object run = ((PlotRun)this.data.get(i)).clone();
				newObject.data.add(run);
			}

		} catch (CloneNotSupportedException e) {
			// Can't happen.
			e.printStackTrace();
		}
		
		// Output the newly cloned object.
		return newObject;
	}

}


