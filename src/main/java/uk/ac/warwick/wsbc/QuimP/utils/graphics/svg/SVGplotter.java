package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import ij.IJ;
import uk.ac.warwick.wsbc.QuimP.Outline;
import uk.ac.warwick.wsbc.QuimP.OutlineHandler;
import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.Vert;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * SVG plotter used in QuimP. Left in this form for compatibility reason.
 * 
 * @author rtyson
 *
 */
public class SVGplotter {
    OutlineHandler oH;
    File outFile;
    double scale;
    double deltaT;
    int channel;

    String colorWith;

    QColor[] colorMap;

    public SVGplotter(OutlineHandler o, double t, double s, int c, File out) {
        oH = o;
        deltaT = t;
        scale = s;
        outFile = out;
        channel = c;
    }

    public void plotTrackER(String c) {
        colorWith = c;
        // System.out.println("max min: " + oH.maxM + ", " + oH.minM);

        int miny = (int) Math.floor(oH.minCoor.getY()) - 10;
        int minx = (int) Math.floor(oH.minCoor.getX()) - 10;
        int maxy = (int) Math.ceil(oH.maxCoor.getY()) + 10;
        int maxx = (int) Math.ceil(oH.maxCoor.getX()) + 10;

        int width = maxx - minx;
        int height = maxy - miny;

        try {

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outFile.getAbsolutePath() + "_motility.svg"));
            OutputStreamWriter osw = new OutputStreamWriter(out);

            osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
            osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " "
                    + width + " " + height + "\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            osw.write("\n");

            osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width
                    + "\" height=\"" + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
                    + "stroke:rgb(0,0,0)\"/>\n\n");

            double t = 0;
            double dur = 0.2;
            Outline o;

            // colorMap = QColor.colourMap("Summer", oH.getSize());
            for (int i = 0; i < oH.getSize(); i++) {
                o = oH.indexGetOutline(i);
                t = (double) i * dur;
                plotVerts(osw, o, t, dur);
            }

            osw.write("</svg>\n");
            osw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void plotTrackAnim() {

        // oH.minCoor.print("minCoor:");
        // oH.maxCoor.print("maxCoor:");
        int miny = (int) Math.floor(oH.minCoor.getY()) - 10;
        int minx = (int) Math.floor(oH.minCoor.getX()) - 10;
        int maxy = (int) Math.ceil(oH.maxCoor.getY()) + 10;
        int maxx = (int) Math.ceil(oH.maxCoor.getX()) + 10;

        int width = maxx - minx;
        int height = maxy - miny;

        try {

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outFile.getAbsolutePath() + "_trackAnim.svg"));
            OutputStreamWriter osw = new OutputStreamWriter(out);

            osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
            osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " "
                    + width + " " + height + "\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            osw.write("\n");

            osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width
                    + "\" height=\"" + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
                    + "stroke:rgb(0,0,0)\"/>\n\n");

            Outline o;
            double t;
            double dur = 0.1;

            colorMap = QColor.colourMap("Summer", oH.getSize());
            for (int i = 0; i < oH.getSize(); i++) {
                o = oH.getOutline(i);
                t = (double) i * dur;
                plotOutline(osw, o, colorMap[i].getColorSVG(), true, t, dur);
            }

            osw.write("</svg>\n");
            osw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void plotTrack(String trackColor, int increment) {

        // oH.minCoor.print("minCoor:");
        // oH.maxCoor.print("maxCoor:");
        int miny = (int) Math.floor(oH.minCoor.getY()) - 15;
        int minx = (int) Math.floor(oH.minCoor.getX()) - 10;
        int maxy = (int) Math.ceil(oH.maxCoor.getY()) + 10;
        int maxx = (int) Math.ceil(oH.maxCoor.getX()) + 10;

        int width = maxx - minx;
        int height = maxy - miny;

        try {

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outFile.getAbsolutePath() + "_track.svg"));
            OutputStreamWriter osw = new OutputStreamWriter(out);

            osw.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
            osw.write("<svg width=\"15cm\" height=\"15cm\" viewBox=\"" + minx + " " + miny + " "
                    + width + " " + height + "\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            osw.write("\n");

            osw.write("<rect x=\"" + minx + "\" y=\"" + miny + "\" width=\"" + width
                    + "\" height=\"" + height + "\" " + "style=\"fill:rgb(0,0,0);stroke-width:0;"
                    + "stroke:rgb(0,0,0)\"/>\n\n");

            Outline o;
            int colSize = (int) Math.ceil(oH.getSize() / (double) increment);

            colorMap = QColor.colourMap(trackColor, colSize);
            int count = 0;
            for (int i = 0; i < oH.getSize(); i += increment) {
                o = oH.indexGetOutline(i);
                plotOutline(osw, o, colorMap[count].getColorSVG(), false, 0, 0);
                count++;
            }

            int barValue = (int) Math.round((width / 5d) * scale);
            ScaleBar scaleBar = new ScaleBar(new ExtendedVector2d(minx + 5, miny + 8), "&#x3BC;m",
                    barValue, scale);
            scaleBar.thickness = 1;
            scaleBar.colour.setRGB(1, 1, 1);
            scaleBar.draw(osw);

            osw.write("\n</svg>\n");
            osw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plotOutline(OutputStreamWriter osw, Outline o, String colour, boolean anim,
            double t, double dur) throws Exception {
        Vert v = o.getHead();
        osw.write("<polyline ");
        if (anim)
            osw.write("display=\"none\" ");
        osw.write("fill=\"none\" style=\"stroke:" + colour + ";stroke-width:" + 0.5
                + "\" points=\"\n");
        do {
            osw.write(IJ.d2s(v.getX(), 6) + "," + IJ.d2s(v.getY(), 6) + "\n");
            v = v.getNext();
        } while (!v.isHead());
        osw.write(IJ.d2s(v.getX(), 6) + "," + IJ.d2s(v.getY(), 6));

        if (anim) {
            osw.write("\" >\n");
            osw.write("<animate id='frame_" + IJ.d2s(t)
                    + "' attributeName='display' values='inline;none'" + " dur='" + dur
                    + "s' fill='freeze' begin=\"" + IJ.d2s(t) + "s\" repeatCount=\"1\"/>\n");
            osw.write("</polyline>");
        } else {
            osw.write("\"/>\n");
        }
        osw.write("\n");
    }

    private void plotVerts(OutputStreamWriter osw, Outline o, double t, double dur)
            throws Exception {
        osw.write("<g id=\"verts_1\" display=\"none\">\n");
        Vert v = o.getHead();
        QColor ERcolour;
        do {
            ERcolour = getERcolor(v);
            osw.write("<circle cx=\"" + IJ.d2s(v.getX(), 6) + "\" cy=\"" + IJ.d2s(v.getY(), 6)
                    + "\" " + "r=\"0.7\" stroke-width=\"0\" fill=\"" + ERcolour.getColorSVG()
                    + "\"/>");
            v = v.getNext();
        } while (!v.isHead());

        osw.write("\n");
        osw.write("<animate id='frame_" + IJ.d2s(t)
                + "' attributeName='display' values='inline;none'" + " dur='" + dur
                + "s' fill='freeze' begin=\"" + IJ.d2s(t) + "s\" repeatCount=\"1\"/>");
        osw.write("\n</g>\n\n");
    }

    private QColor getERcolor(Vert v) {
        if (colorWith.matches("Speed")) {
            return QColor.ERColorMap2("rwb", v.distance, oH.migLimits[0], oH.migLimits[1]);
        } else if (colorWith.matches("Fluorescence")) {
            return QColor.RWBmap(v.fluores[channel].intensity, 255, 0);
        } else if (colorWith.matches("Convexity")) {
            return QColor.ERColorMap2("rwb", v.curvatureSum, oH.curvLimits[0], oH.curvLimits[1]);
        } else {
            System.out.println("unknown color map: SVGplotter l:221");
            return new QColor(1, 1, 1);
        }

    }
}

class ScaleBar {

    private double length;
    private String units;
    private int value;
    public double thickness;
    public QColor colour;
    private ExtendedVector2d location;
    private SVGwritter.Qtext text;

    public ScaleBar(ExtendedVector2d l, String u, int v, double s) {
        location = l;
        units = u;
        value = v;
        thickness = 1;
        colour = new QColor(1, 1, 1);
        this.setScale(s);
        text = new SVGwritter.Qtext(IJ.d2s(value, 0) + units, 6, "Courier");
        text.colour = colour;
    }

    public void setScale(double s) {
        length = value / s;
    }

    public void draw(OutputStreamWriter osw) throws IOException {
        SVGwritter.Qline body, lTick, rTick;
        double tickSize = 2 * thickness;

        ExtendedVector2d end = new ExtendedVector2d(location.getX(), location.getY());
        end.addVec(new ExtendedVector2d(length, 0));
        body = new SVGwritter.Qline(location.getX(), location.getY(), end.getX(), end.getY());
        body.thickness = thickness;
        body.colour = colour;

        lTick = new SVGwritter.Qline(location.getX(), location.getY() + tickSize, location.getX(),
                location.getY() - tickSize);
        rTick = new SVGwritter.Qline(end.getX(), end.getY() + tickSize, end.getX(),
                end.getY() - tickSize);
        lTick.thickness = thickness;
        lTick.colour = colour;
        rTick.thickness = thickness;
        rTick.colour = colour;

        SVGdraw.line(osw, lTick); // TODO replace with own Qline.draw method
        SVGdraw.line(osw, rTick);
        SVGdraw.line(osw, body);

        // centre the text
        int textLength = 2 + Integer.toString(value).length();
        textLength = textLength * 4;
        double textDis = (body.length() - textLength) / 2;
        SVGdraw.text(osw, text,
                new ExtendedVector2d(location.getX() + textDis, location.getY() - 2));
    }

}
