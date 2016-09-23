package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.QuimP.QColor;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

public class Qtext {

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
        osw.write("\n<text x=\"" + l.getX() + "\" y=\"" + l.getY() + "\" " + "style=\"font-family: "
                + font + ";font-size: " + size + ";fill: " + colour.getColorSVG() + "\">" + text
                + "</text>");
    }
}