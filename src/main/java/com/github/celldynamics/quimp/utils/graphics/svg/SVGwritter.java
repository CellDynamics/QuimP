package com.github.celldynamics.quimp.utils.graphics.svg;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.celldynamics.quimp.QColor;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.IJ;

/**
 * Plot primitives on SVG image.
 * 
 * <p>Written svg object must be close by /svg tag manually.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class SVGwritter {

  /**
   * Generate typical header.
   * 
   * @param osw file to write
   * @param d bounding box
   * @throws IOException on file error
   */
  public static void writeHeader(PrintWriter osw, Rectangle d) throws IOException {
    Rectangle d1 = new Rectangle(d);
    d1.grow(1, 1);
    osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
    osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + d1.x + " " + d1.y + " " + d1.width
            + " " + d1.height + "\" "
            + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
    osw.write("\n");

    osw.write("<rect x=\"" + d.getX() + "\" y=\"" + d.getY() + "\" width=\"" + d.getWidth()
            + "\" height=\"" + d.getHeight() + "\" "
            + "style=\"fill:rgb(100.0%,100.0%,100.0%);stroke-width:0.01;"
            + "stroke:rgb(0.0%,0.0%,0.0%)\"/>\n\n");
  }

  /**
   * Write to file svg definition of frawn object.
   * 
   * @param osw file to write
   * @throws IOException on file error
   */
  public abstract void draw(PrintWriter osw) throws IOException;

  /**
   * Represent circle on SVG image.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Qcircle extends SVGwritter {

    /**
     * x coordinate of center.
     */
    public double x1;
    /**
     * y coordinate of center.
     */
    public double y1;

    /**
     * The radius.
     */
    public double radius;

    /**
     * The colour. If null object has no filling
     */
    public QColor colour;

    /**
     * The strokecolour.
     */
    public QColor strokecolour;

    /**
     * The thickness.
     */
    public double thickness;

    /**
     * Instantiates a new circle.
     *
     * @param x1 the x 1
     * @param y1 the y 1
     * @param radius the radius
     */
    public Qcircle(double x1, double y1, double radius) {
      this.x1 = x1;
      this.y1 = y1;
      this.radius = radius;
      colour = new QColor(0, 0, 0);
      strokecolour = colour;
      thickness = 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see SVGwritter#draw(java.io.OutputStreamWriter)
     */
    @Override
    public void draw(PrintWriter osw) throws IOException {
      String col; // fill colour
      if (colour == null) {
        col = "none";
      } else {
        col = colour.getColorSVG();
      }
      //!>
      osw.write("<circle cx=\"" + x1 + "\"" + " cy=\"" + y1 + "\"" + " r=\"" + radius + "\""
              + " fill=\"" + col + "\"" + " stroke=\"" + strokecolour.getColorSVG() + "\""
              + " stroke-width=\"" + thickness + "\"/>\n");
      //!<
    }
  }

  /**
   * Represent line on SVG image.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Qline extends SVGwritter {

    /**
     * x coordinate of first point of section.
     */
    public double x1;
    /**
     * y coordinate of first point of section.
     */
    public double y1;
    /**
     * x coordinate of last point of section.
     */
    public double x2;
    /**
     * y coordinate of last point of section.
     */
    public double y2;

    /**
     * The thickness.
     */
    public double thickness;

    /**
     * The colour.
     */
    public QColor colour;

    /**
     * Instantiates a new qline.
     *
     * @param xx1 the xx 1
     * @param yy1 the yy 1
     * @param xx2 the xx 2
     * @param yy2 the yy 2
     */
    public Qline(double xx1, double yy1, double xx2, double yy2) {
      x1 = xx1;
      x2 = xx2;
      y1 = yy1;
      y2 = yy2;

      thickness = 1;
      colour = new QColor(0, 0, 0);
    }

    /**
     * Length.
     *
     * @return the double
     */
    public double length() {
      return Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter#draw(java.io.OutputStreamWriter)
     */
    @Override
    public void draw(PrintWriter osw) throws IOException {
      osw.write("<line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" ");
      osw.write("style=\"stroke:" + colour.getColorSVG() + ";stroke-width:" + thickness + "\"/>\n");
    }
  }

  /**
   * Represent text on SVG image.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Qtext extends SVGwritter {

    /**
     * The text.
     */
    public String text;

    /**
     * The size.
     */
    public double size;

    /**
     * The colour.
     */
    public QColor colour;

    /**
     * The font.
     */
    public String font;

    /**
     * Position of text.
     */
    public ExtendedVector2d pos;

    /**
     * If letters overlap, set it to half of font size.
     */
    public double letterSpacing = 0;

    /**
     * Text anchor. Possible values: start, middle, end.
     */
    public String textAnchor = "start";

    /**
     * Instantiates a new qtext.
     *
     * @param t Text
     * @param s Size
     * @param f Font name
     * @param pos position
     */
    public Qtext(String t, double s, String f, ExtendedVector2d pos) {
      text = t;
      size = s;
      font = f;
      this.pos = pos;
      colour = new QColor(0, 0, 0);
    }

    /**
     * Length.
     *
     * @return the int
     */
    public int length() {
      return text.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter#draw(java.io.OutputStreamWriter)
     */
    @Override
    public void draw(PrintWriter osw) throws IOException {
      osw.write("\n<text x=\"" + pos.getX() + "\" y=\"" + pos.getY() + "\" "
              + "style=\"font-family: " + font + ";font-size: " + size + ";fill: "
              + colour.getColorSVG() + ";letter-spacing: " + letterSpacing + ";text-anchor: "
              + textAnchor + "\">" + text + "</text>");

    }
  }

  /**
   * Create axes in polar coordinates.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class QPolarAxes extends SVGwritter {
    private Rectangle rect;

    /**
     * The colour.
     */
    public QColor colour;

    /**
     * The thickness.
     */
    public double thickness;

    /**
     * Show angle labels.
     */
    public boolean angleLabel = false;

    /**
     * Font size. If one gets overlap letters, play with fontSize and {@link Qtext#letterSpacing}
     */
    public double fontSize = 0.2;

    /**
     * Labels for radius. Must contain numofIntCircles entries only.
     * 
     * @see #numofIntCircles
     */
    public double[] radiusLabels = null;

    /**
     * Number of circles in polar plot.
     * 
     * @see #radiusLabels
     */
    public int numofIntCircles = 5;

    /**
     * Create polar axes.
     * 
     * @param rect boundaries
     */
    public QPolarAxes(Rectangle rect) {
      this.rect = rect;
      colour = new QColor(0.5, 0.5, 0.5);
      thickness = 0.01;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter#draw(java.io.OutputStreamWriter)
     */
    @Override
    public void draw(PrintWriter osw) throws IOException {
      // plot parameters
      double x0 = rect.getLocation().getX() + rect.getWidth() / 2;
      double y0 = rect.getLocation().getY() + rect.getHeight() / 2;
      double radius = rect.getHeight() / 2;

      // main circle
      Qcircle qcmain = new Qcircle(x0, y0, radius);
      qcmain.colour = new QColor(1, 1, 1);
      qcmain.strokecolour = colour;
      qcmain.thickness = thickness;
      qcmain.draw(osw);

      // lines
      for (int a = 0; a < 360; a += 20) {
        double xend = radius * Math.cos(Math.toRadians(a)) + x0;
        double yend = radius * Math.sin(Math.toRadians(a)) + y0;
        Qline ql = new Qline(x0, y0, xend, yend);
        ql.thickness = thickness;
        ql.colour = colour;
        ql.draw(osw);

        // labels on angles
        if (angleLabel) {
          Qtext qt = new Qtext(Integer.toString(a), fontSize, "New Roman",
                  new ExtendedVector2d(xend, yend));
          // qt.letterSpacing = fontSize / 2; // may not be important for fontSize>0.6 (?)
          if (a < 90 || a > 270) {
            qt.textAnchor = "start";
          } else {
            qt.textAnchor = "end";
          }
          qt.draw(osw);
        }
      }
      // circles
      double d = radius / (numofIntCircles + 1);
      double gridrad = d;
      int i = 0;
      do {
        Qcircle g1 = new Qcircle(x0, y0, gridrad);
        g1.colour = null;
        g1.strokecolour = colour;
        g1.thickness = thickness;
        g1.draw(osw);

        // rlabel
        if (radiusLabels != null && i < numofIntCircles) {
          String st = IJ.d2s(radiusLabels[i], 2, 4);
          double x = gridrad * Math.cos(Math.toRadians(160)) + x0;
          double y = gridrad * Math.sin(Math.toRadians(160)) + y0;
          Qtext qt = new Qtext(st, fontSize - 0.04, "New Roman", new ExtendedVector2d(x, y));
          // qt.letterSpacing = (fontSize - 0.04) / 2;
          qt.textAnchor = "middle";
          qt.draw(osw);
        }
        gridrad += d;
      } while (++i < numofIntCircles);

      // circle red in middle
      {
        Qcircle qc = new Qcircle(rect.getLocation().getX() + rect.getWidth() / 2,
                rect.getLocation().getY() + rect.getHeight() / 2, thickness * 4);
        qc.colour = new QColor(1, 0, 0);
        qc.draw(osw);
      }

      // green at 0deg
      {
        Qcircle qc = new Qcircle(radius * Math.cos(Math.toRadians(0)) + x0,
                radius * Math.sin(Math.toRadians(0)) + y0, thickness * 4);
        qc.colour = new QColor(0, 1, 0);
        qc.draw(osw);
      }

      // green at 180deg
      {
        Qcircle qc = new Qcircle(radius * Math.cos(Math.toRadians(180)) + x0,
                radius * Math.sin(Math.toRadians(180)) + y0, thickness * 4);
        qc.colour = new QColor(0, 0, 1);
        qc.draw(osw);
      }

    }
  }

  /**
   * Plot scale bar on svg.
   * 
   * @author rtyson
   *
   */
  public static class QScaleBar extends SVGwritter {

    private double length;
    private String units;
    private int value;
    /**
     * Line thickness.
     */
    public double thickness;
    /**
     * Text colour.
     */
    public QColor colour;
    private ExtendedVector2d location;
    private SVGwritter.Qtext text;

    /**
     * Constructor.
     * 
     * @param l position of text
     * @param u units
     * @param v value
     * @param s scale
     */
    public QScaleBar(ExtendedVector2d l, String u, int v, double s) {
      location = l;
      units = u;
      value = v;
      thickness = 1;
      colour = new QColor(1, 1, 1);
      this.setScale(s);
      text = new SVGwritter.Qtext(IJ.d2s(value, 0) + units, 6, "Courier", l);
      text.colour = colour;
    }

    /**
     * Set scale.
     * 
     * @param s scale
     */
    public void setScale(double s) {
      length = value / s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter#draw(java.io.OutputStreamWriter)
     */
    @Override
    public void draw(PrintWriter osw) throws IOException {
      SVGwritter.Qline body;

      double tickSize = 2 * thickness;

      ExtendedVector2d end = new ExtendedVector2d(location.getX(), location.getY());
      end.addVec(new ExtendedVector2d(length, 0));
      body = new SVGwritter.Qline(location.getX(), location.getY(), end.getX(), end.getY());
      body.thickness = thickness;
      body.colour = colour;

      SVGwritter.Qline ltick;
      SVGwritter.Qline rtick;
      ltick = new SVGwritter.Qline(location.getX(), location.getY() + tickSize, location.getX(),
              location.getY() - tickSize);
      rtick = new SVGwritter.Qline(end.getX(), end.getY() + tickSize, end.getX(),
              end.getY() - tickSize);
      ltick.thickness = thickness;
      ltick.colour = colour;
      rtick.thickness = thickness;
      rtick.colour = colour;

      ltick.draw(osw);
      rtick.draw(osw);
      body.draw(osw);

      // centre the text
      int textLength = 2 + Integer.toString(value).length();
      textLength = textLength * 4;
      double textDis = (body.length() - textLength) / 2;
      text.pos = new ExtendedVector2d(location.getX() + textDis, location.getY() - 2);
      text.draw(osw);
    }

  }
}
