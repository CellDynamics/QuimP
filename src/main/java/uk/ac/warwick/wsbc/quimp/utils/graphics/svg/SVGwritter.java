package uk.ac.warwick.wsbc.quimp.utils.graphics.svg;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.quimp.QColor;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

// TODO: Auto-generated Javadoc
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
     * @param d
     * @throws IOException
     */
    public static void writeHeader(OutputStreamWriter osw, Rectangle d) throws IOException {
        Rectangle d1 = new Rectangle(d);
        d1.grow(1, 1);
        osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + d1.x + " " + d1.y + " "
                + d1.width + " " + d1.height + "\" "
                + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        osw.write("\n");

        osw.write("<rect x=\"" + d.getX() + "\" y=\"" + d.getY() + "\" width=\"" + d.getWidth()
                + "\" height=\"" + d.getHeight() + "\" "
                + "style=\"fill:rgb(100.0%,100.0%,100.0%);stroke-width:0.01;"
                + "stroke:rgb(0.0%,0.0%,0.0%)\"/>\n\n");
    }

    /**
     * Represent circle on SVG image.
     * 
     * @author p.baniukiewicz
     *
     */
    static public class Qcircle extends SVGwritter {

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
         * The colour.
         * 
         * if null object has no filling
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

        /**
         * Draw.
         *
         * @param osw the osw
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void draw(OutputStreamWriter osw) throws IOException {
            String col; // fill colour
            if (colour == null)
                col = "none";
            else
                col = colour.getColorSVG();
            //!<
            osw.write("<circle cx=\"" + x1 + "\"" + " cy=\"" + y1 + "\"" + " r=\"" + radius + "\""
                    + " fill=\"" + col + "\"" + " stroke=\"" + strokecolour.getColorSVG() + "\""
                    + " stroke-width=\"" + thickness + "\"/>\n");
            /**/
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

        /**
         * Draw.
         *
         * @param osw the osw
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void draw(OutputStreamWriter osw) throws IOException {
            osw.write("<line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2
                    + "\" ");
            osw.write("style=\"stroke:" + colour.getColorSVG() + ";stroke-width:" + thickness
                    + "\"/>\n");
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
         * Instantiates a new qtext.
         *
         * @param t the t
         * @param s the s
         * @param f the f
         */
        public Qtext(String t, double s, String f) {
            text = t;
            size = s;
            font = f;
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

        /**
         * Draw.
         *
         * @param osw the osw
         * @param l the l
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void draw(OutputStreamWriter osw, ExtendedVector2d l) throws IOException {
            osw.write("\n<text x=\"" + l.getX() + "\" y=\"" + l.getY() + "\" "
                    + "style=\"font-family: " + font + ";font-size: " + size + ";fill: "
                    + colour.getColorSVG() + "\">" + text + "</text>");
        }
    }

    /**
     * 
     * @author p.baniukiewicz
     *
     */
    public static class QPolarAxes extends SVGwritter {
        private Rectangle r;

        /**
         * The colour.
         */
        public QColor colour;

        /**
         * The thickness.
         */
        public double thickness;

        /**
         * 
         * @param r
         */
        public QPolarAxes(Rectangle r) {
            this.r = r;
            colour = new QColor(0.5, 0.5, 0.5);
            thickness = 0.01;
        }

        /**
         * Draw.
         *
         * @param osw the osw
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void draw(OutputStreamWriter osw) throws IOException {
            // plot parameters
            double x0 = r.getLocation().getX() + r.getWidth() / 2;
            double y0 = r.getLocation().getY() + r.getHeight() / 2;
            double radius = r.getHeight() / 2;
            // main circle
            Qcircle qcmain = new Qcircle(x0, y0, radius);
            qcmain.colour = new QColor(1, 1, 1);
            qcmain.strokecolour = colour;
            qcmain.thickness = thickness;
            qcmain.draw(osw);

            // lines
            for (int a = 0; a < 360; a += 20) {
                Qline ql = new Qline(x0, y0, radius * Math.cos(Math.toRadians(a)) + x0,
                        radius * Math.sin(Math.toRadians(a)) + y0);
                ql.thickness = thickness;
                ql.colour = colour;
                ql.draw(osw);
            }

            // circles
            int n = 5;
            double d = radius / (n + 1);
            double gridrad = d;
            int i = 0;
            do {
                Qcircle g1 = new Qcircle(x0, y0, gridrad);
                g1.colour = null;
                g1.strokecolour = colour;
                g1.thickness = thickness;
                g1.draw(osw);
                gridrad += d;
            } while (++i < n);

            // middle
            Qcircle qcm = new Qcircle(r.getLocation().getX() + r.getWidth() / 2,
                    r.getLocation().getY() + r.getHeight() / 2, thickness * 4);
            qcm.colour = colour;
            qcm.draw(osw);

        }
    }
}
