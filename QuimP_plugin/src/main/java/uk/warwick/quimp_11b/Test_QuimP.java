package uk.warwick.quimp_11b;

import ij.*;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Polygon;
import java.io.File;

/** Inserts an image or stack into a stack.
 */
public class Test_QuimP implements PlugIn {

   private static final int m = Measurements.AREA
           + Measurements.INTEGRATED_DENSITY
           + Measurements.MEAN;

   @Override
   public void run(String arg) {
      //testAngle();
      //testOverlay();
      testLineIntersect();
      //testCutSelf();
      //this.testClosest();
      //this.testArea();

      //this.test3Dout();
      //this.colorConvert();
   }

   void testArea() {
      ImagePlus imp = WindowManager.getCurrentImage();
      if (imp == null) {
         IJ.noImage();
         return;
      }

      imp.setSlice(1);
      ImageProcessor impProc = imp.getProcessor();

      Polygon outerPoly = new Polygon();
      outerPoly.addPoint(11, 11);
      outerPoly.addPoint(11, 110);
      outerPoly.addPoint(110, 11);


      Polygon innerPoly = new Polygon();
      innerPoly.addPoint(110, 110);
      innerPoly.addPoint(140, 120);
      innerPoly.addPoint(60, 130);

      PolygonRoi outerRoi = new PolygonRoi(outerPoly, Roi.POLYGON);
      impProc.setRoi(outerRoi);

      ImageStatistics is = ImageStatistics.getStatistics(impProc, m, null);


      System.out.println("Area=" + is.area + ", mean=" + is.mean);


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

   void testAngle() {

      Vert a, b, c;

      a = new Vert(10, 10, 1);
      b = new Vert(15, 15, 2);
      c = new Vert(10, 15, 3);




      Vect2d edge1 = Vect2d.vecP2P(b.getPoint(), a.getPoint());

      Vect2d edge2 = Vect2d.vecP2P(b.getPoint(), c.getPoint());

      double curvature = Vect2d.angle(edge1, edge2) * (180 / Math.PI);

      System.out.println("curv = " + curvature);
   }

   void testQparams() {
      QParams p = new QParams(new File("/Users/rtyson/Documents/phd/tmp/smallStack/SmallStack_0.paQP"));
      boolean success = p.readParams();
      if (success) {
         System.out.println("Yeah, read baby");
      }
      System.out.println("" + p.NMAX);
      System.out.println("" + p.path);
      System.out.println("" + p.prefix);
      System.out.println("" + p.convexFile);

      //p.setParamFile(new File("/Users/rtyson/Documents/phd/tmp/smallStack/SmallStack_NEW.paQP"));
      //p.writeParams();
   }

   void testOverlay() {

      ImagePlus imp = IJ.getImage();

      PolygonRoi roiP = (PolygonRoi) imp.getRoi();

      Polygon p = roiP.getPolygon();
      Polygon np = new Polygon();

      float[] xf = new float[p.npoints];
      float[] yf = new float[p.npoints];

      for (int i = 0; i < p.npoints; i++) {
         np.addPoint(p.xpoints[i] + 10, p.ypoints[i] + 10);
         xf[i] = p.xpoints[i] + 10.4f;
         yf[i] = p.ypoints[i] + 10.4f;
      }

      Overlay ov = new Overlay();
      PolygonRoi intRoi = new PolygonRoi(np, Roi.POLYGON);
     // FloatRoi floRoi = new FloatRoi(xf, yf);
      ov.add(intRoi);
     // ov.add(floRoi);
      imp.setOverlay(ov);

     // floRoi.setIntPoints();
      //imp.getCanvas().getImage().setRoi(floRoi);
   }

   private void testLineIntersect() {
      System.out.println("Test line intersection");
      //Vect2d a = new Vect2d(238.8343223897114,180.57548291017963);
      //Vect2d b = new Vect2d(252.3128980477469,179.3057722538331);

      //Vect2d c = new Vect2d(248.21769647653375,383.1254314892506);
      //Vect2d d = new Vect2d(248.22719691475632,395.7869644627188);
      
      Vect2d a = new Vect2d(238,180);
      Vect2d b = new Vect2d(252,179);

      Vect2d c = new Vect2d(248.217,383);
      Vect2d d = new Vect2d(248.227,395);

      double[] intersect = new double[2];

      int state = Vect2d.segmentIntersection(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY(), d.getX(), d.getY(), intersect);
       System.out.println("Sate: " + state);
      if (state == -1) {
         System.out.println("\nLines parallel");
         System.out.println("close all;plot([" + a.getX() + "," + b.getX() + "],[" + a.getY() + "," + b.getY() + "],'-ob');"); // matlab output
         System.out.println("hold on; plot([" + c.getX() + "," + d.getX() + "],[" + c.getY() + "," + d.getY() + "],'-or');");

      } else if (state == -2) {
         System.out.println("\nLines parallel and overlap");
         System.out.println("close all;plot([" + a.getX() + "," + b.getX() + "],[" + a.getY() + "," + b.getY() + "],'-ob');"); // matlab output
         System.out.println("hold on; plot([" + c.getX() + "," + d.getX() + "],[" + c.getY() + "," + d.getY() + "],'-or');");
         System.out.println("plot(" + intersect[0] + "," + intersect[1] + ", 'og');");
      } else if (state == 1) {
         System.out.println("\nLines intersect at " + intersect[0] + ", " + intersect[1]);
         System.out.println("close all;plot([" + a.getX() + "," + b.getX() + "],[" + a.getY() + "," + b.getY() + "],'-ob');"); // matlab output
         System.out.println("hold on; plot([" + c.getX() + "," + d.getX() + "],[" + c.getY() + "," + d.getY() + "],'-or');");
         System.out.println("plot(" + intersect[0] + "," + intersect[1] + ", 'og');");
      }

   }

//   private void testCutSelf() {
//      QParams qp = new QParams(new File("/Users/rtyson/Documents/phd/tmp/bugs/Files_problems/Dicty-2011_04_04-exp5-RFP-5_0.paQP"));
//      qp.readParams();
//      System.out.println("" + qp.snakeQP);
//      OutlineHandler oh = new OutlineHandler(qp);
//
//      Outline o = oh.getOutline(1);
//      o.cutSelfIntersects();
//   }

//   private void testClosest() {
//
//      Vect2d p = new Vect2d(76.23858250113373,119.10033085490637);
//      Vect2d a = new Vect2d(74.16480188724655,119.79799016803751);
//      Vect2d b = new Vect2d(75.84030926810546,119.2); //118.13024961475276);
//
//      Vect2d c = Vect2d.PointToSegment(p, a, b);
//
//      System.out.println("c.x: "+ c.getX()+ ", c.y: " +c.getY());
//
//      Vect2d edge = Vect2d.unitVector(a, b);
//      Vect2d link = Vect2d.unitVector(p, c);
//
//      double angle  = Vect2d.angle(edge, link);
//
//      System.out.println("angle: " + angle);
//
//   }

   /*
   private void test3Dout(){

      QParams qp = new QParams(new File("/Users/rtyson/Documents/phd/tmp/test/QuimP11analysis/SmallStack_0.paQP"));
      qp.readParams();
      OutlineHandler oH = new OutlineHandler(qp);
      if (!oH.readSuccess) {
         return;
      }else{
         System.out.println("Contours read in. Size: " + oH.getSize());
         
      }
   
      int res = 150;
      Point3f[] coords= new Point3f[ res * oH.getSize()];
      Color3f[] colors = new Color3f[res * oH.getSize()];
      int coCount = 0;

      Vect2d centre = oH.getOutline(1).getCentroid();
      centre.setX(centre.getX()*-1);
      centre.setY(centre.getY()*-1);

      Outline o, op;
      Vect2d pHead;
      for(int i = 0; i < oH.getSize(); i++){
         o = oH.indexGetOutline(i);
         o.setResolutionN(res);
         if(i!=0){
            op = oH.indexGetOutline(i-1);
            pHead = op.getHead().getPoint();
            o.setHeadclosest(pHead);
         }
         
         Vert n = o.getHead();
         do{
            coords[coCount] = new Point3f((float)(n.getX()+centre.getX()),(i*1.0f),(float)(n.getY()+centre.getY()));
            colors[coCount] = new Color3f(0.5f,(float)Math.random(),0f);
            

            coCount++;
            n = n.getPrev(); // go tuther way round to point normals outwards
         }while(!n.isHead());
      }

      int[] indices= new int[(res*4)* (oH.getSize()-1)];
      System.out.println("indeices length: "+indices.length);

     int base;
      for (int i = 0; i < indices.length; i+=4) {
         
         base = i / 4;

         indices[i] = base;
         indices[i+1] = base+1;
         indices[i+2] = base+1+res;
         indices[i+3] = base + res;

         if(((i-((res-1)*4)))%(4*res) == 0){
            indices[i+1] = base+1-res;
            indices[i+2] = base+1;
            //System.out.print("\twraped: ");
         }

         //System.out.println(""+indices[i] +"," +indices[i+1]+"," +indices[i+2]+"," +indices[i+3]);
      }


      System.out.println("coord length" + coords.length);
      System.out.println("max index = "+ Tool.arrayMax(indices));

      /*

      //biuld a simple quad
      Point3f[] coords={
        new Point3f(-0.2f,0.2f,-0.2f),
        new Point3f(-0.2f,-0.2f,-0.2f),
        new Point3f(0.2f,-0.2f,-0.2f),
        new Point3f(0.2f,0.2f,-0.2f),
        new Point3f(-0.2f,0.2f,0.2f),
        new Point3f(-0.2f,-0.2f,0.2f),
        new Point3f(0.2f,-0.2f,0.2f),
        new Point3f(0.2f,0.2f,0.2f)
      };

      int[] indices={
        0,1,2,3,
        5,6,7,4,
        1,5,6,2,
        0,4,7,3,
        1,5,4,0,
        2,6,7,3
      };

      //QuadArray quads = new QuadArray(4,GeometryArray.COORDINATES);
      //quads.setCoordinates(0, coords);

      //IndexedLineArray lineArray=new IndexedLineArray(coords.length,
        //IndexedQuadArray.COORDINATES,indices.length);
      //lineArray.setCoordinates(0,coords);
      //lineArray.setCoordinateIndices(0,indices);

      IndexedQuadArray quadArray = new IndexedQuadArray(coords.length,
        IndexedQuadArray.COORDINATES | QuadArray.COLOR_3,indices.length);
      quadArray.setCoordinates(0,coords);
      quadArray.setCoordinateIndices(0,indices);
      quadArray.setColors(0, colors);
      quadArray.setColorIndices(0, indices);

      //Appearance a = new Appearance();
      //PolygonAttributes pAttr = new PolygonAttributes(1, 0, 0.1f);
	// a.setPolygonAttributes(pAttr);

      Shape3D myShape = new Shape3D(quadArray);

      BranchGroup bg = new BranchGroup();
      bg.addChild(myShape);
      VRML97Saver saver = new VRML97Saver();
      
      saver.setBranchGroup(bg);
      saver.save("/temp/cube_A.wrl");



   }
*/
//   private void colorConvert(){
//
//      QColor c = new QColor( 0.6d,0.2d,0.4d);
//      int ci= c.getColorInt();
//      System.out.println("col int: " + ci);
//      Color3f cf = QColor.colorInt23f(ci);
//      System.out.println("r: " + cf.x + ", g: " + cf.y + ", b: " + cf.z);
//   }
}
