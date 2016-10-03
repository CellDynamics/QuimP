package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.QuimP.QColor;

public class Qline {
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
        osw.write(
                "\n<line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" ");
        osw.write("style=\"stroke:" + colour.getColorSVG() + ";stroke-width:" + thickness + "\"/>");
    }
}