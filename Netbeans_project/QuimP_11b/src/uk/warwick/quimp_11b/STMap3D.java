/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.warwick.quimp_11b;

import java.io.File;
//import javax.media.j3d.BranchGroup;
//import javax.media.j3d.IndexedQuadArray;
//import javax.media.j3d.QuadArray;
//import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
//import org.cybergarage.x3d.j3d.VRML97Saver;

/**
 *
 * @author rtyson
 */
public class STMap3D {

   double[][] xMap, yMap;
   int[] colors;
   int VERTS;
   int RES;
   int TIME;
   VRMLobject cell3d;

   public STMap3D(double[][] xm, double[][] ym, int[] col) {
      xMap = xm;
      yMap = ym;
      colors = col;

      RES = xm[0].length;
      TIME = xm.length;
      VERTS = col.length;
   }

   void build() {
      Point3f[] coords = new Point3f[VERTS];
      Color3f[] colorsF = new Color3f[VERTS];
      int coCount = 0;

      //int[] colIndices = new int[VERTS];

      for (int i = 0; i < xMap.length; i++) {
         for (int j = xMap[0].length -1; j >=0; j--) {
            coords[coCount] = new Point3f((float) xMap[i][j], (i * 1.0f), (float) yMap[i][j]);
            colorsF[coCount] = QColor.colorInt23f(colors[(i*RES)+j]);
            //if(i<30){
               //colorsF[coCount] = new Color3f(0.5168f,0.0f,0.0f);
            //}else{
               //colorsF[coCount] = new Color3f(0.0f,0.0f,0.5532f);
            //}
            //colIndices[coCount] = coCount;
            coCount++;
         }

      }

      //int[] indices= new int[(RES*4)* (oH.getSize()-1)];
      int[] indices = new int[(RES * 4) * (TIME - 1)];
      System.out.println("indeices length: " + indices.length);

      int base;
      for (int i = 0; i < indices.length; i += 4) {

         base = i / 4;

         indices[i] = base;
         indices[i + 1] = base + 1;
         indices[i + 2] = base + 1 + RES;
         indices[i + 3] = base + RES;

         if (((i - ((RES - 1) * 4))) % (4 * RES) == 0) {
            indices[i + 1] = base + 1 - RES;
            indices[i + 2] = base + 1;
            //System.out.print("\twraped: ");
         }

         //System.out.println(""+indices[i] +"," +indices[i+1]+"," +indices[i+2]+"," +indices[i+3]);
      }


      System.out.println("coord length" + coords.length);
      System.out.println("max index = " + Tool.arrayMax(indices));

      cell3d = new VRMLobject(coords, colorsF, indices);

      
   }

   void toOrigin(Vect2d centre){
      cell3d.transform((float)-centre.getX(), 0, (float)-centre.getY());
   }

   void scale(float f){
      cell3d.scale(f);
   }

   void write(File CELL){
      if(CELL.exists()){
         CELL.delete();
      }
      cell3d.write(CELL);
   }
}
