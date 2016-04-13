package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

public class Vertex {
    private ExtendedVector2d point; // x,y co-ordinates of the node
    private ExtendedVector2d normal; // normals
    private ExtendedVector2d tan;
    private boolean head;
    private static boolean clockwise = true; // access clockwise if true

    public Vertex(int t) {
        // t = tracking number
        point = new ExtendedVector2d();
        normal = new ExtendedVector2d();
        tan = new ExtendedVector2d();
    }

}
