package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * Previous implementation of plotter.
 * @author 
 * 
 */
@Deprecated
public class SVGdraw {

    public SVGdraw() {
    }

    static public void line(OutputStreamWriter osw, SVGwritter.Qline l) throws IOException {
        osw.write("\n<line x1=\"" + l.x1 + "\" y1=\"" + l.y1 + "\" x2=\"" + l.x2 + "\" y2=\"" + l.y2
                + "\" ");
        osw.write("style=\"stroke:" + l.colour.getColorSVG() + ";stroke-width:" + l.thickness
                + "\"/>");
    }

    static public void text(OutputStreamWriter osw, SVGwritter.Qtext t, ExtendedVector2d l)
            throws IOException {
        osw.write("\n<text x=\"" + l.getX() + "\" y=\"" + l.getY() + "\" " + "style=\"font-family: "
                + t.font + ";font-size: " + t.size + ";fill: " + t.colour.getColorSVG() + "\">"
                + t.text + "</text>");
    }

}
