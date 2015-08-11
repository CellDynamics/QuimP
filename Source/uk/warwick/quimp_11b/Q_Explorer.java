/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.warwick.quimp_11b;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.TextReader;
import ij.process.ImageProcessor;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author rtyson
 */
public class Q_Explorer implements PlugIn {

   MapCanvas[] mapCanvs;
   MovCanvas movCanvas;
   ExplorerStackWindow explorerWindow;
   ImageManager imageManager;
   OutlineHandler oH;

   void Q_Explorer() {
   }

   @Override
   public void run(String string) {
      try {
        // do {
            OpenDialog od = new OpenDialog("Open paramater file (.paQP)...", OpenDialog.getLastDirectory(), "");
            if (od.getFileName() == null) {
               return;
            }
            File paramFile = new File(od.getDirectory(), od.getFileName());
            EXp.qp = new QParams(paramFile);
            EXp.qp.readParams();
            setup();
            return;
         //} while (true);

      } catch (Exception e) {
         //IJ.error("Unknown exception");
         e.printStackTrace();
      }
   }

   @SuppressWarnings("serial")
class MapCanvas extends ImageCanvas {

      MapCanvas(ImagePlus imp) {
         super(imp);
      }

      @Override
      public void mousePressed(MouseEvent e) {
         super.mousePressed(e);
         IJ.log("Map pressed at: ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
         mapUpdate(e.getX(), e.getY());
      }
   }

   @SuppressWarnings("serial")
class MovCanvas extends ImageCanvas {

      MovCanvas(ImagePlus imp) {
         super(imp);
      }

      @Override
      public void mousePressed(MouseEvent e) {
         IJ.log("Movie pressed at: ("+offScreenX(e.getX())+","+offScreenY(e.getY())+")");
      }
   }

   @SuppressWarnings("serial")
class ExplorerStackWindow extends StackWindow implements ActionListener, ItemListener, ChangeListener {

      ExplorerStackWindow(ImagePlus imp, ImageCanvas stackCanvas) {
         super(imp, stackCanvas);

      }

      @Override
      public void itemStateChanged(ItemEvent ie) {
      }

      @Override
      public void stateChanged(ChangeEvent ce) {
      }

      @Override
      public void updateSliceSelector() {
         super.updateSliceSelector();
         EXp.frame = sliceSelector.getValue();
         imageManager.updateOverlays(); //draw overlay
      } // update the frame label, overlay, frame and set zoom
   }

   void setup(){
      oH = new OutlineHandler(EXp.qp);
      if(!oH.readSuccess){
         IJ.error("Failed to read " + EXp.qp.snakeQP.getName());
         return;
      }
      imageManager = new ImageManager(oH);
      buildWindows();
   }

   void buildWindows(){
      mapCanvs = new MapCanvas[imageManager.nbMaps];
      mapCanvs[0] = new MapCanvas(imageManager.mapsIpl[0]);
      new ImageWindow(imageManager.mapsIpl[0], mapCanvs[0]);
      imageManager.mapsIpl[0].show();

      movCanvas = new MovCanvas(imageManager.movieIpl);
      explorerWindow = new ExplorerStackWindow(imageManager.movieIpl, movCanvas);

      imageManager.movieIpl.show();
      imageManager.movieIpl.setSlice(1);
      EXp.frame = 1;
      imageManager.updateOverlays();
   }

   void mapUpdate(int x, int y){
      imageManager.setSlice(y);
   }

   Vect2d map2Mov(){
      return new Vect2d(-1,-1);
   }
}


class ImageManager{

   ImagePlus[] mapsIpl;
   ImagePlus movieIpl;
   ImagePlus xMap, yMap;
   Overlay[] movOverlay;
   Vect2d mapCoord;
   Vect2d movCoord;
   int nbMaps;

   ImageManager( OutlineHandler oH){
      nbMaps = 1;
      mapsIpl = new ImagePlus[1];

      xMap = openMap(EXp.qp.xFile, "xMap");
      yMap = openMap(EXp.qp.xFile, "yMap");

      mapsIpl[0] = openMap(EXp.qp.motilityFile, "Motility Map");

      movieIpl = IJ.openImage(EXp.qp.segImageFile.getAbsolutePath());
      movOverlay = new Overlay[movieIpl.getStackSize()];

      //build overlays
      movOverlay = new Overlay[movieIpl.getStackSize()];
      for(int i = 1; i <= movieIpl.getStackSize(); i++){
         movOverlay[i-1] = new Overlay();
         if(oH.isOutlineAt(i)){
            movOverlay[i-1].add(oH.getOutline(i).asFloatRoi());
         }
      }

   }

   private ImagePlus openMap( File f, String n){

      if(!f.exists()){
         IJ.log("Could not open " + f.getName());
         return null;
      }

      TextReader textR = new TextReader();
      ImageProcessor ip = textR.open( f.getAbsolutePath());
      return new ImagePlus(n, ip);
   }

   void updateOverlays(){
      movieIpl.setOverlay(movOverlay[EXp.frame-1]);
   }

   void setSlice(int i){
      movieIpl.setSlice(i);
      EXp.frame = i;
      this.updateOverlays();
   }

}

class EXp{

   static QParams qp;
   static int frame;

   EXp(){

   }

}