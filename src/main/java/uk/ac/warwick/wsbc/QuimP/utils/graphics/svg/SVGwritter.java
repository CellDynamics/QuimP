package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Plot shapes on SVG image.
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class SVGwritter {

    /**
     * Generate typical header.
     * 
     * @param osw
     * @throws IOException
     */
    public static void writeHeader(OutputStreamWriter osw) throws IOException {
        osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + (-10) + " " + (-10) + " " + 20
                + " " + 20 + "\" "
                + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        osw.write("\n");

        osw.write("<rect x=\"" + -10 + "\" y=\"" + -10 + "\" width=\"" + 20 + "\" height=\"" + 20
                + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;" + "stroke:rgb(0,0,0)\"/>\n\n");
    }

    /**
     * Represent circle on SVG image.
     * 
     * @author p.baniukiewicz
     *
     */
    public static class Qcircle {
        public double x1, y1;
        public double radius;
        public QColor colour;

        public Qcircle(double x1, double y1, double radius) {
            this.x1 = x1;
            this.y1 = y1;
            this.radius = radius;
            colour = new QColor(1, 1, 1);
        }

        public void draw(OutputStreamWriter osw) throws IOException {
            //!<
            osw.write(
                      "<circle cx=\""+x1+"\""
                           + " cy=\""+y1+"\""
                           + " r=\""+radius+"\""
                           + " fill=\""+colour.getColorSVG()+"\""
                           + " stroke=\"blue\""
                           + " stroke-width=\"0\"/>\n");
            /**/
        }
    }

    /**
     * Represent line on SVG image.
     * 
     * @author p.baniukiewicz
     *
     */
    public static class Qline {
        public double x1, y1, x2, y2;
        public double thickness;
        public QColor colour;

        public Qline(double xx1, double yy1, double xx2, double yy2) {
            x1 = xx1;
            x2 = xx2;
            y1 = yy1;
            y2 = yy2;

            thickness = 1;
            colour = new QColor(1, 1, 1);
        }

        public double length() {
            return Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        }

        public void draw(OutputStreamWriter osw) throws IOException {
            osw.write("\n<line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2
                    + "\" ");
            osw.write("style=\"stroke:" + colour.getColorSVG() + ";stroke-width:" + thickness
                    + "\"/>");
        }
    }

    /**
     * Represent text on SVG image.
     * 
     * @author p.baniukiewicz
     *
     */
    public static class Qtext {

        public String text;
        public double size;
        public QColor colour;
        public String font;

        public Qtext(String t, double s, String f) {
            text = t;
            size = s;
            font = f;
            colour = new QColor(1, 1, 1);
        }

        public int length() {
            return text.length();
        }

        public void draw(OutputStreamWriter osw, ExtendedVector2d l) throws IOException {
            osw.write("\n<text x=\"" + l.getX() + "\" y=\"" + l.getY() + "\" "
                    + "style=\"font-family: " + font + ";font-size: " + size + ";fill: "
                    + colour.getColorSVG() + "\">" + text + "</text>");
        }
    }
}
