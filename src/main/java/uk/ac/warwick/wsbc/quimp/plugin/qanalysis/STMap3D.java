/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package uk.ac.warwick.wsbc.quimp.plugin.qanalysis;

import java.io.File;

// import javax.media.j3d.BranchGroup;
// import javax.media.j3d.IndexedQuadArray;
// import javax.media.j3d.QuadArray;
// import javax.media.j3d.Shape3D;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3f;
// import org.cybergarage.x3d.j3d.VRML97Saver;

import uk.ac.warwick.wsbc.quimp.QColor;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.utils.QuimPArrayUtils;
import uk.ac.warwick.wsbc.quimp.utils.graphics.vrml.VRMLobject;

// TODO: Auto-generated Javadoc
/**
 *
 * @author rtyson
 */
public class STMap3D {

  /**
   * The y map.
   */
  double[][] xMap, yMap;

  /**
   * The colors.
   */
  int[] colors;

  /**
   * The verts.
   */
  int VERTS;

  /**
   * The res.
   */
  int RES;

  /**
   * The time.
   */
  int TIME;

  /**
   * The cell 3 d.
   */
  VRMLobject cell3d;

  /**
   * Instantiates a new ST map 3 D.
   *
   * @param xm the xm
   * @param ym the ym
   * @param col the col
   */
  public STMap3D(double[][] xm, double[][] ym, int[] col) {
    xMap = xm;
    yMap = ym;
    colors = col;

    RES = xm[0].length;
    TIME = xm.length;
    VERTS = col.length;
  }

  /**
   * Builds the.
   */
  void build() {
    Point3f[] coords = new Point3f[VERTS];
    Color3f[] colorsF = new Color3f[VERTS];
    int coCount = 0;

    // int[] colIndices = new int[VERTS];

    for (int i = 0; i < xMap.length; i++) {
      for (int j = xMap[0].length - 1; j >= 0; j--) {
        coords[coCount] = new Point3f((float) xMap[i][j], (i * 1.0f), (float) yMap[i][j]);
        colorsF[coCount] = QColor.colorInt23f(colors[(i * RES) + j]);
        // if(i<30){
        // colorsF[coCount] = new Color3f(0.5168f,0.0f,0.0f);
        // }else{
        // colorsF[coCount] = new Color3f(0.0f,0.0f,0.5532f);
        // }
        // colIndices[coCount] = coCount;
        coCount++;
      }

    }

    // int[] indices= new int[(RES*4)* (oH.getSize()-1)];
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
        // System.out.print("\twraped: ");
      }

      // System.out.println(""+indices[i] +"," +indices[i+1]+","
      // +indices[i+2]+"," +indices[i+3]);
    }

    System.out.println("coord length" + coords.length);
    System.out.println("max index = " + QuimPArrayUtils.arrayMax(indices));

    cell3d = new VRMLobject(coords, colorsF, indices);

  }

  /**
   * To origin.
   *
   * @param centre the centre
   */
  void toOrigin(ExtendedVector2d centre) {
    cell3d.transform((float) -centre.getX(), 0, (float) -centre.getY());
  }

  /**
   * Scale.
   *
   * @param f the f
   */
  void scale(float f) {
    cell3d.scale(f);
  }

  /**
   * Write.
   *
   * @param CELL the cell
   */
  void write(File CELL) {
    if (CELL.exists()) {
      CELL.delete();
    }
    cell3d.write(CELL);
  }
}
