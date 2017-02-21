package uk.ac.warwick.wsbc.quimp.utils.graphics.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

// TODO: Auto-generated Javadoc
/**
 * Previous implementation of plotter.
 * 
 * @author tyson
 * 
 */
@Deprecated
public class SVGdraw {

    /**
     * Instantiates a new SV gdraw.
     */
    public SVGdraw() {
    }

    /**
     * Line.
     *
     * @param osw the osw
     * @param l the l
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static public void line(OutputStreamWriter osw, SVGwritter.Qline l) throws IOException {
        osw.write("\n<line x1=\"" + l.x1 + "\" y1=\"" + l.y1 + "\" x2=\"" + l.x2 + "\" y2=\"" + l.y2
                + "\" ");
        osw.write("style=\"stroke:" + l.colour.getColorSVG() + ";stroke-width:" + l.thickness
                + "\"/>");
    }

    /**
     * Text.
     *
     * @param osw the osw
     * @param t the t
     * @param l the l
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static public void text(OutputStreamWriter osw, SVGwritter.Qtext t, ExtendedVector2d l)
            throws IOException {
        osw.write("\n<text x=\"" + l.getX() + "\" y=\"" + l.getY() + "\" " + "style=\"font-family: "
                + t.font + ";font-size: " + t.size + ";fill: " + t.colour.getColorSVG() + "\">"
                + t.text + "</text>");
    }

}
