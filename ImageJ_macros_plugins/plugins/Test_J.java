
import ij.*;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Polygon;
import QuimP10.Vect2d;
import QuimP10.Vert;


/** Inserts an image or stack into a stack.
 */
public class Test_J implements PlugIn {

    private static final int m = Measurements.AREA +
            Measurements.INTEGRATED_DENSITY +
            Measurements.MEAN;


    public void run(String arg) {
        testAngle();
    }

    void testArea(){
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        imp.setSlice(1);
        ImageProcessor impProc= imp.getProcessor();

        Polygon outerPoly = new Polygon();
        outerPoly.addPoint(11, 11);
        outerPoly.addPoint(11, 110);
        outerPoly.addPoint(110,  11);


        Polygon innerPoly = new Polygon();
        innerPoly.addPoint(110, 110);
        innerPoly.addPoint(140, 120);
        innerPoly.addPoint(60,  130);

        PolygonRoi outerRoi = new PolygonRoi(outerPoly, Roi.POLYGON);
        PolygonRoi innerRoi = new PolygonRoi(innerPoly, Roi.POLYGON);

        impProc.setRoi(outerRoi);

        ImageStatistics is = ImageStatistics.getStatistics(impProc, m, null);


        System.out.println("Area="+is.area + ", mean=" + is.mean);
        /*
        //ImageProcessor orgIp = orgStack.getProcessor(i + 1);

        PolygonFiller pf = new PolygonFiller();
        pf.setPolygon(roi.getXCoordinates(), roi.getYCoordinates(), roi.getNCoordinates());
        Rectangle b = roi.getBounds();
        orgIp.setRoi(poly);
        orgIp.setMask(pf.getMask(b.width, b.height));
        ImageStatistics is = ImageStatistics.getStatistics(orgIp, m, null);
        */

    }


    void testAngle(){

        Vert a,b,c;

        a = new Vert(10,10,1);
        b = new Vert(15,15,2);
        c = new Vert(10,15,3);




        Vect2d edge1 =  Vect2d.vecP2P(b.getPoint(), a.getPoint());

        Vect2d edge2 =  Vect2d.vecP2P(b.getPoint(), c.getPoint());

        double curvature = Vect2d.angle(edge1, edge2) * (180/Math.PI);

        System.out.println("curv = " + curvature);
    }
}
