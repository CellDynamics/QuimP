package com.github.celldynamics.quimp.utils.graphics.svg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.OutlineHandler;
import com.github.celldynamics.quimp.QColor;
import com.github.celldynamics.quimp.Vert;
import com.github.celldynamics.quimp.filesystem.FileExtensions;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.IJ;

/**
 * SVG plotter used in QuimP. Left in this form for compatibility reason.
 * 
 * <P>TODO This whole class should end up at SVGwritter
 * 
 * @author rtyson
 *
 */
public class SVGplotter {

  /**
   * The OutlineHandler to plot.
   */
  OutlineHandler oh;

  /**
   * The out file.
   */
  File outFile;

  /**
   * The scale.
   */
  double scale;

  /**
   * The delta T.
   */
  double deltaT;

  /**
   * The channel.
   */
  int channel;

  /**
   * The color with.
   */
  String colorWith;

  /**
   * The color map.
   */
  QColor[] colorMap;

  /**
   * Instantiates a new SV gplotter.
   *
   * @param o the o
   * @param t the t
   * @param s the s
   * @param c the c
   * @param out the out
   */
  public SVGplotter(OutlineHandler o, double t, double s, int c, File out) {
    oh = o;
    deltaT = t;
    scale = s;
    outFile = out;
    channel = c;
  }

  /**
   * Plot track ER.
   *
   * @param c the type of plot
   */
  public void plotTrackER(String c) {
    // default
    if (c == null || c.isEmpty()) {
      colorWith = "Speed";
    } else {
      colorWith = c;
    }
    // System.out.println("max min: " + oH.maxM + ", " + oH.minM);

    int miny = (int) Math.floor(oh.minCoor.getY()) - 10;
    int minx = (int) Math.floor(oh.minCoor.getX()) - 10;
    int maxy = (int) Math.ceil(oh.maxCoor.getY()) + 10;
    int maxx = (int) Math.ceil(oh.maxCoor.getX()) + 10;

    int width = maxx - minx;
    int height = maxy - miny;

    try {

      BufferedOutputStream out = new BufferedOutputStream(
              new FileOutputStream(outFile.getAbsolutePath() + FileExtensions.motvecimageFileExt));
      PrintWriter osw = new PrintWriter(out);

      osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
      osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " " + width
              + " " + height + "\" "
              + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
      osw.write("\n");

      osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width + "\" height=\""
              + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
              + "stroke:rgb(0,0,0)\"/>\n\n");

      double t = 0;
      double dur = 0.2;
      Outline o;

      // colorMap = QColor.colourMap("Summer", oH.getSize());
      for (int i = 0; i < oh.getSize(); i++) {
        o = oh.indexGetOutline(i);
        t = (double) i * dur;
        plotVerts(osw, o, t, dur);
      }

      osw.write("</svg>\n");
      osw.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Plot track anim.
   */
  @Deprecated
  public void plotTrackAnim() {

    // oH.minCoor.print("minCoor:");
    // oH.maxCoor.print("maxCoor:");
    int miny = (int) Math.floor(oh.minCoor.getY()) - 10;
    int minx = (int) Math.floor(oh.minCoor.getX()) - 10;
    int maxy = (int) Math.ceil(oh.maxCoor.getY()) + 10;
    int maxx = (int) Math.ceil(oh.maxCoor.getX()) + 10;

    int width = maxx - minx;
    int height = maxy - miny;

    try {

      BufferedOutputStream out = new BufferedOutputStream(
              new FileOutputStream(outFile.getAbsolutePath() + "_trackAnim.svg"));
      PrintWriter osw = new PrintWriter(out);

      osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
      osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " " + width
              + " " + height + "\" "
              + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
      osw.write("\n");

      osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width + "\" height=\""
              + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
              + "stroke:rgb(0,0,0)\"/>\n\n");

      Outline o;
      double t;
      double dur = 0.1;

      colorMap = QColor.colourMap("Summer", oh.getSize());
      for (int i = 0; i < oh.getSize(); i++) {
        o = oh.getStoredOutline(i);
        t = (double) i * dur;
        plotOutline(osw, o, colorMap[i].getColorSVG(), true, t, dur);
      }

      osw.write("</svg>\n");
      osw.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Plot track.
   *
   * @param trackColor the track color
   * @param increment the increment
   */
  public void plotTrack(String trackColor, int increment) {

    // oH.minCoor.print("minCoor:");
    // oH.maxCoor.print("maxCoor:");
    int miny = (int) Math.floor(oh.minCoor.getY()) - 15;
    int minx = (int) Math.floor(oh.minCoor.getX()) - 10;
    int maxy = (int) Math.ceil(oh.maxCoor.getY()) + 10;
    int maxx = (int) Math.ceil(oh.maxCoor.getX()) + 10;

    int width = maxx - minx;
    int height = maxy - miny;

    try {

      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
              outFile.getAbsolutePath() + FileExtensions.trackvecimageFileExt));
      PrintWriter osw = new PrintWriter(out);

      osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
      osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " " + width
              + " " + height + "\" "
              + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
      osw.write("\n");

      osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width + "\" height=\""
              + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
              + "stroke:rgb(0,0,0)\"/>\n\n");

      Outline o;
      int colSize = (int) Math.ceil(oh.getSize() / (double) increment);

      colorMap = QColor.colourMap(trackColor, colSize);
      int count = 0;
      for (int i = 0; i < oh.getSize(); i += increment) {
        o = oh.indexGetOutline(i);
        plotOutline(osw, o, colorMap[count].getColorSVG(), false, 0, 0);
        count++;
      }

      int barValue = (int) Math.round((width / 5d) * scale);
      SVGwritter.QScaleBar scaleBar = new SVGwritter.QScaleBar(
              new ExtendedVector2d(minx + 5, miny + 8), "&#x3BC;m", barValue, scale);
      scaleBar.thickness = 1;
      scaleBar.colour.setRGB(1, 1, 1);
      scaleBar.draw(osw);

      osw.write("\n</svg>\n");
      osw.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void plotOutline(PrintWriter osw, Outline o, String colour, boolean anim, double t,
          double dur) throws Exception {
    Vert v = o.getHead();
    osw.write("<polyline ");
    if (anim) {
      osw.write("display=\"none\" ");
    }
    osw.write("fill=\"none\" style=\"stroke:" + colour + ";stroke-width:" + 0.5 + "\" points=\"\n");
    do {
      osw.write(IJ.d2s(v.getX(), 6) + "," + IJ.d2s(v.getY(), 6) + "\n");
      v = v.getNext();
    } while (!v.isHead());
    osw.write(IJ.d2s(v.getX(), 6) + "," + IJ.d2s(v.getY(), 6));

    if (anim) {
      osw.write("\" >\n");
      osw.write("<animate id='frame_" + IJ.d2s(t) + "' attributeName='display' values='inline;none'"
              + " dur='" + dur + "s' fill='freeze' begin=\"" + IJ.d2s(t)
              + "s\" repeatCount=\"1\"/>\n");
      osw.write("</polyline>");
    } else {
      osw.write("\"/>\n");
    }
    osw.write("\n");
  }

  private void plotVerts(PrintWriter osw, Outline o, double t, double dur) throws Exception {
    osw.write("<g id=\"verts_1\" display=\"none\">\n");
    Vert v = o.getHead();
    QColor erColour;
    do {
      erColour = getERcolor(v);
      osw.write("<circle cx=\"" + IJ.d2s(v.getX(), 6) + "\" cy=\"" + IJ.d2s(v.getY(), 6) + "\" "
              + "r=\"0.7\" stroke-width=\"0\" fill=\"" + erColour.getColorSVG() + "\"/>");
      v = v.getNext();
    } while (!v.isHead());

    osw.write("\n");
    osw.write("<animate id='frame_" + IJ.d2s(t) + "' attributeName='display' values='inline;none'"
            + " dur='" + dur + "s' fill='freeze' begin=\"" + IJ.d2s(t) + "s\" repeatCount=\"1\"/>");
    osw.write("\n</g>\n\n");
  }

  private QColor getERcolor(Vert v) {
    if (colorWith.matches("Speed")) {
      return QColor.erColorMap2("rwb", v.distance, oh.migLimits[0], oh.migLimits[1]);
    } else if (colorWith.matches("Fluorescence")) {
      return QColor.rwbMap(v.fluores[channel].intensity, 255, 0);
    } else if (colorWith.matches("Convexity")) {
      return QColor.erColorMap2("rwb", v.curvatureSum, oh.curvLimits[0], oh.curvLimits[1]);
    } else {
      System.out.println("unknown color map: SVGplotter l:221");
      return new QColor(1, 1, 1);
    }

  }
}
