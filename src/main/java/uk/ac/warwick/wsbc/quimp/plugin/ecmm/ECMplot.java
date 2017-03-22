package uk.ac.warwick.wsbc.quimp.plugin.ecmm;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.QColor;
import uk.ac.warwick.wsbc.quimp.Vert;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

/**
 * Class responsible for plotting ECMM outlines during computations.
 * 
 * @author rtyson
 *
 */
class ECMplot {

  public ImagePlus imPlus;
  public ImageStack imStack;
  public ImageProcessor imProc;
  public int drawFrame = 1;
  public ExtendedVector2d centre;
  public double scale;
  public QColor color;
  private int intersectSize = 6;
  private int textPos = 25;
  public int width;
  public int height;
  public int frame;
  // private int percentScreen = 65; //make visual output x% of screen height

  ECMplot(int ff) {

    // Dimension screen = IJ.getScreenSize();
    // ECMp.visualRes = (int) Math.round((screen.height / 100d) *
    // percentScreen);
    double fitTo = ECMp.visualRes * 0.7;
    scale = fitTo / ECMp.maxCellSize;

    width = ECMp.visualRes;
    height = ECMp.visualRes;

    frame = ff;
    centre = new ExtendedVector2d(0, 0);
    color = new QColor(1, 1, 1);
    // imPlus = NewImage.createByteImage("ECMM mappings", width, height, frame,
    // NewImage.FILL_BLACK);
    imPlus = NewImage.createRGBImage("ECMM_mappings", width, height, frame, NewImage.FILL_WHITE);
    imStack = imPlus.getStack();
    imPlus.show();
  }

  public void setDrawingFrame(int d) {
    drawFrame = d - ECMp.startFrame + 1;
    textPos = 25;
    imProc = imStack.getProcessor(drawFrame);
    this.writeText("Frame map " + d + " to " + (d + 1));
  }

  public void writeText(String text) {
    this.setColor(0, 0, 0);
    imProc.drawString(text, 10, textPos);
    textPos += 15;
  }

  public void drawOutline(Outline o) {
    Vert v = o.getHead();
    do {
      drawLine(v, v.getNext());
      v = v.getNext();
    } while (!v.isHead());
  }

  public void drawPolygon(FloatPolygon p) {
    Vert va;
    Vert vb;
    for (int i = 0; (i + 1) < p.xpoints.length; i++) {
      va = new Vert(p.xpoints[i], p.ypoints[i], i);
      vb = new Vert(p.xpoints[i + 1], p.ypoints[i + 1], i);
      drawLine(va, vb);

    }
  }

  public void drawLine(Vert v1, Vert v2) {

    ExtendedVector2d a = new ExtendedVector2d(v1.getX(), v1.getY());
    ExtendedVector2d b = new ExtendedVector2d(v2.getX(), v2.getY());
    relocate(a);
    relocate(b);

    imProc.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
  }

  public void drawLine(ExtendedVector2d aa, ExtendedVector2d bb) {

    ExtendedVector2d a = new ExtendedVector2d(aa.getX(), aa.getY());
    ExtendedVector2d b = new ExtendedVector2d(bb.getX(), bb.getY());
    relocate(a);
    relocate(b);

    imProc.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
  }

  public void setSlice(int f) {
    this.repaint();
    imPlus.setSlice(f);
  }

  public void drawPath(ExtendedVector2d[] data) {
    relocate(data[0]);
    for (int i = 0; i < data.length - 1; i++) {
      if (data[i + 1] == null) {
        break;
      }
      relocate(data[i + 1]);
      imProc.drawLine((int) data[i].getX(), (int) data[i].getY(), (int) data[i + 1].getX(),
              (int) data[i + 1].getY());
    }
  }

  private void relocate(ExtendedVector2d p) {
    // move a point to the centre
    p.addVec(new ExtendedVector2d(-centre.getX(), -centre.getY()));
    p.multiply(scale);
    p.addVec(new ExtendedVector2d(ECMp.visualRes / 2, ECMp.visualRes / 2));

  }

  public void setColor(double r, double g, double b) {
    color.setRGB(r, g, b);
    imProc.setColor(color.getColorInt());
  }

  public void drawIntersect(Vert i) {
    this.drawCircle(i.getPoint(), intersectSize);
  }

  public void drawCircle(ExtendedVector2d a, int s) {
    ExtendedVector2d v = new ExtendedVector2d(a.getX(), a.getY());
    relocate(v);
    imProc.drawOval((int) v.getX() - (s / 2), (int) v.getY() - (s / 2), s, s);
  }

  public void drawCross(ExtendedVector2d a, int s) {
    ExtendedVector2d p = new ExtendedVector2d(a.getX(), a.getY());
    relocate(p);
    imProc.drawLine((int) p.getX() - s, (int) p.getY() - s, (int) p.getX() + s, (int) p.getY() + s);
    imProc.drawLine((int) p.getX() + s, (int) p.getY() - s, (int) p.getX() - s, (int) p.getY() + s);
  }

  public void repaint() {
    imPlus.repaintWindow();
  }

  public void close() {
    try {
      imPlus.close();
    } catch (Exception e) {
      System.out.println("ECMM Plot could not be closed (prob not open)");
    }
  }
}