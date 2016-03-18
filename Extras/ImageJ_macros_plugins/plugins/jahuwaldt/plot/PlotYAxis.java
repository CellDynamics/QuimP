/*
*   PlotYAxis  -- Represents a vertical or Y plot axis.
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
import java.awt.image.*;


/**
*  <p> This class represents a vertical or Y axis
*      on a plot.
*  </p>
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  September 13, 2000
*  @version April 25, 2002
**/
public class PlotYAxis extends PlotAxis {

	
	//-------------------------------------------------------------------------
	/**
	*  Create a new Y axis using the default settings.
	**/
	public PlotYAxis() { }
	
	/**
	*  Create a Y axis with the label, axis scale transformation,
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
	public PlotYAxis( String label, PlotAxisScale scale, NumberFormat nf, int gridStyle ) {
		super(label, scale, nf, gridStyle);
	}

	//-------------------------------------------------------------------------
	/**
	*  Automatically set the upper and lower bounds for this
	*  Y axis based on the data in the runs input.
	**/
	public void setAxisBounds(PlotRunList runs) {
		
		if (manualLB && manualUB)	return;

		if (!manualLB)
			aLB = Double.MAX_VALUE;
		if (!manualUB)
			aUB = Double.MIN_VALUE;
		
		
		AxisLimitData buffer = new AxisLimitData();		//	Buffer for passing data between objects.
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
						
						if (datum.hasErrorBar()) {
						
							//	This data point has an error bar.
							//	Adjust upper and lower bounds to allow room for error bars.
							scale.adjustForErrorBars(datum, aUB, aLB, buffer);
							if (!manualLB)
								aLB = buffer.lb;
							if (!manualUB)
								aUB = buffer.ub;
							
						} else {
							//	No error bars.
							
							//	Find axis bounds for Y axis.
							if (!manualLB)
								aLB = Math.min(aLB, datum.y);
						
							if (!manualUB)
								aUB = Math.max(aUB, datum.y);
						}
					}
				}
			}
		
		}
		
		if (!dataFound || aLB == aUB) {
			//	If no data found, set default scales.
			if (!manualLB)
				aLB = scale.lowerBounds();
			if (!manualUB)
				aUB = scale.upperBounds();
		}
		
	}
	
	/**
	*  Scale this axis so that it fits inside of the specified
	*  plot frame rectangle.  Vertical axes must fit inside
	*  the height of the frame.
	*
	*  @param  plotFrame  The bounding rectangle for the data area
	*                     of the plot (overall bounds minus area for
	*                     titles, labels, etc).
	**/
	protected void resizeAxis(Rectangle plotFrame) {
		
		//	Top edge of axis in screen coordinates.
		int topy = plotFrame.y;
		
		//	Bottom edge of axis in screen coordinates.
		int boty = plotFrame.y + plotFrame.height;
		int edge = topy - boty;
		
		//	Set scaling factors in class variables.
		
		//	xA = (Scrn Height)/(Data Height)
		xA = (double)edge/(scale.func(aUB) - scale.func(aLB));
		xB = boty - scale.func(aLB)*xA + 0.5;
		
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
		String str = null;
		
		int topPos = bounds.y;
		int botPos = topPos + bounds.height;
		int left = bounds.x;
		int right = left + bounds.width;

		if (gridStyle != kNoGrid) {
		
			Color foreColor = gc.getColor();
		
			//	Create tick marks on this axis.
			TickMarkData ticks = scale.calcTickMarks(quantum, aLB, aUB, xA, xB);
			int[] mark = ticks.mark;
			int[] lmark = ticks.lmark;
			float[] markValue = ticks.markValue;
			int yn = mark.length;
		
			//	Draw label on the first tick mark.
			str = nf.format(2*markValue[0] - markValue[1]);
			textWidth = fm.stringWidth(str);
			int ascent = fm.getAscent();
			int descent = fm.getDescent();
			
			yPos = botPos + ascent - (ascent + descent)/2;
			xPos = left - textWidth - 2;
			gc.drawString(str, xPos, yPos);
		
			//	Draw intermediate tick marks and labels.
			for (int i=0; i < yn; ++i) {
				yPos = mark[i];
			
				//	Draw full grid at major tick mark if requested.
				if (lmark[i] == 3*kTick && gridStyle == kMajorGrid) {
					gc.setColor(Color.gray);
					gc.drawLine(left, yPos, right, yPos);
					gc.setColor(foreColor);
				}
			
				//	Draw tick marks.
				gc.drawLine(left, yPos, left + lmark[i], yPos);
				gc.drawLine(right - lmark[i], yPos, right, yPos);
				
				//	Draw numeric labels next to major tick marks.
				if (lmark[i] == 3*kTick) {
					str = nf.format(markValue[i]);
					textWidth = fm.stringWidth(str);
				
					yPos += ascent - (ascent + descent)/2;
					xPos = left - textWidth - 2;
					gc.drawString(str, xPos, yPos);
				}
			}
		
			//	Draw label on last tick mark.
			str = nf.format(2*markValue[yn-1] - markValue[yn-2]);
			textWidth = fm.stringWidth(str);
		
			yPos = topPos + ascent - (ascent + descent)/2;
			xPos = left - textWidth - 2;
			gc.drawString(str, xPos, yPos);
		}
		
		
		//	Draw the axis label.

		if (label != null) {
	    	//	Center label beside axis.
		    //	Figure out where the label should go.
		    str = nf.format((float)(aUB > 0 ? -aUB : aUB));
	    	int tmp1 = fm.stringWidth(str);
	    	str = nf.format((float)(aLB > 0 ? -aLB : aLB));
	    	int tmp2 = fm.stringWidth(str);
	    	if (tmp2 > tmp1)	tmp1 = tmp2;
		
		    textWidth = fm.stringWidth(label);
		    xPos = left - tmp1 - 2 - textHeight + fm.getAscent();
		    yPos = (botPos + topPos)/2 + textWidth/2;
		
		    //	Draw the label vertically.
		    drawRotString(gc, comp, label, xPos, yPos, 90*Math.PI/180);
		}
		
	}
	
	/**
	*  Draws a string to the specified graphics context rotated by the
	*  specified angle in degrees.
	*
	*  @param  gc    The graphics context to draw into.
	*  @param  comp  The component that we are drawing into.
	*  @param  str   The string to be drawn.
	*  @param  x     The x coordinate.
	*  @param  y     The y coordinate.
	*  @param  angle The angle to rotate the string through in radians.
	**/
	private void drawRotString(Graphics gc, Component comp, String str, int x, int y, double angle) {
		
		//	Extract information on the current font.
		FontMetrics fm = gc.getFontMetrics();
		int textHeight = fm.getHeight();
		int textWidth = fm.stringWidth(str);
		int ascent = fm.getAscent();
		
		//	Create an offscreen image the width and height of the string to be rotated.
		Image img = comp.createImage(textWidth, textHeight);
		
		//	Get a graphics context for the image so we can draw to it.
		Graphics imggc = img.getGraphics();
		imggc.setFont(gc.getFont());
		imggc.setColor(comp.getBackground());
		imggc.fillRect(0,0,textWidth, textHeight);
		imggc.setColor(gc.getColor());
		
		//	Draw the string into the offscreen buffer.
		imggc.drawString(str, 0, ascent);
		
		//	Create an image filter to rotate the offscreen image.
		ImageFilter filter = new RotateFilter(angle);
		ImageProducer producer = new FilteredImageSource( img.getSource(), filter);
		Image resultImage = Toolkit.getDefaultToolkit().createImage(producer);
		
		//	Finally, draw the image to the supplied graphics context.
		x -= ascent;
		y -= textWidth;
		gc.drawImage(resultImage, x, y, null);
	}
	
	//	This is used only by RotateFilter, but can't be inside of RotateFilter
	//  since it's static.
    private static ColorModel defaultRGB = ColorModel.getRGBdefault();
    
	/**
	*  An image filter that will rotate an image by a specified angle in radians.
	*
	*  Written by Sun Microsystems.
	**/
	class RotateFilter extends ImageFilter {
	
    	private double angle;
    	private double sin;
   		private double cos;
    	private double coord[] = new double[2];

    	private int raster[];
    	private int xoffset, yoffset;
    	private int srcW, srcH;
    	private int dstW, dstH;

    	public RotateFilter(double angle) {
        	this.angle = angle;
        	sin = Math.sin(angle);
        	cos = Math.cos(angle);
    	}

    	public void transform(double x, double y, double[] retcoord) {
        	// Remember that the coordinate system is upside down so apply
        	// the transform as if the angle were negated.
        	// cos(-angle) =  cos(angle)
        	// sin(-angle) = -sin(angle)
        	retcoord[0] = cos * x + sin * y;
        	retcoord[1] = cos * y - sin * x;
    	}

   		public void itransform(double x, double y, double[] retcoord) {
        	// Remember that the coordinate system is upside down so apply
        	// the transform as if the angle were negated.  Since inverting
        	// the transform is also the same as negating the angle, itransform
        	// is calculated the way you would expect to calculate transform.
        	retcoord[0] = cos * x - sin * y;
        	retcoord[1] = cos * y + sin * x;
    	}

    	public void transformBBox(Rectangle rect) {
        	double minx = Double.POSITIVE_INFINITY;
        	double miny = Double.POSITIVE_INFINITY;
        	double maxx = Double.NEGATIVE_INFINITY;
        	double maxy = Double.NEGATIVE_INFINITY;
        	for (int y = 0; y <= 1; y++) {
            	for (int x = 0; x <= 1; x++) {
                	transform(rect.x + x * rect.width, rect.y + y * rect.height, coord);
                	minx = Math.min(minx, coord[0]);
                	miny = Math.min(miny, coord[1]);
                	maxx = Math.max(maxx, coord[0]);
                	maxy = Math.max(maxy, coord[1]);
            	}
        	}
        	rect.x = (int) Math.floor(minx);
        	rect.y = (int) Math.floor(miny);
        	rect.width = (int) Math.ceil(maxx) - rect.x + 1;
        	rect.height = (int) Math.ceil(maxy) - rect.y + 1;
    	}

    	public void setDimensions(int width, int height) {
        	Rectangle rect = new Rectangle(0, 0, width, height);
        	transformBBox(rect);
        	xoffset = -rect.x;
        	yoffset = -rect.y;
        	srcW = width;
        	srcH = height;
        	dstW = rect.width;
        	dstH = rect.height;
        	raster = new int[srcW * srcH];
        	consumer.setDimensions(dstW, dstH);
    	}

    	public void setColorModel(ColorModel model) {
        	consumer.setColorModel(defaultRGB);
    	}

    	public void setHints(int hintflags) {
        	consumer.setHints(TOPDOWNLEFTRIGHT
                          | COMPLETESCANLINES
                          | SINGLEPASS
                          | (hintflags & SINGLEFRAME));
    	}

    	public void setPixels(int x, int y, int w, int h, ColorModel model,
                          byte pixels[], int off, int scansize) {
        	int srcoff = off;
        	int dstoff = y * srcW + x;
        	for (int yc = 0; yc < h; yc++) {
            	for (int xc = 0; xc < w; xc++) {
                	raster[dstoff++] = model.getRGB(pixels[srcoff++] & 0xff);
            	}
            	srcoff += (scansize - w);
            	dstoff += (srcW - w);
        	}
    	}

    	public void setPixels(int x, int y, int w, int h, ColorModel model,
                          int pixels[], int off, int scansize) {
        	int srcoff = off;
        	int dstoff = y * srcW + x;
        	if (model == defaultRGB) {
            	for (int yc = 0; yc < h; yc++) {
                	System.arraycopy(pixels, srcoff, raster, dstoff, w);
                	srcoff += scansize;
                	dstoff += srcW;
            	}
        	} else {
            	for (int yc = 0; yc < h; yc++) {
                	for (int xc = 0; xc < w; xc++) {
                    	raster[dstoff++] = model.getRGB(pixels[srcoff++]);
                	}
                	srcoff += (scansize - w);
                	dstoff += (srcW - w);
            	}
        	}
    	}

    	public void imageComplete(int status) {
        	if (status == IMAGEERROR || status == IMAGEABORTED) {
            	consumer.imageComplete(status);
            	return;
        	}
        	int pixels[] = new int[dstW];
        	for (int dy = 0; dy < dstH; dy++) {
            	itransform(0 - xoffset, dy - yoffset, coord);
            	double x1 = coord[0];
            	double y1 = coord[1];
            	itransform(dstW - xoffset, dy - yoffset, coord);
            	double x2 = coord[0];
            	double y2 = coord[1];
            	double xinc = (x2 - x1) / dstW;
            	double yinc = (y2 - y1) / dstW;
            	for (int dx = 0; dx < dstW; dx++) {
                	int sx = (int) Math.round(x1);
                	int sy = (int) Math.round(y1);
                	if (sx < 0 || sy < 0 || sx >= srcW || sy >= srcH) {
                    	pixels[dx] = 0;
                	} else {
                    	pixels[dx] = raster[sy * srcW + sx];
                	}
                	x1 += xinc;
                	y1 += yinc;
            	}
            	consumer.setPixels(0, dy, dstW, 1, defaultRGB, pixels, 0, dstW);
        	}
        	consumer.imageComplete(status);
    	}
	}
	
}


