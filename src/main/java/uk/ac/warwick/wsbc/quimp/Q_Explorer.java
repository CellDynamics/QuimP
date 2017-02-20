/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.warwick.wsbc.quimp;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.TextReader;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.filesystem.FileExtensions;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;

// TODO: Auto-generated Javadoc
/**
 *
 * @author rtyson
 */
public class Q_Explorer implements PlugIn {

    /**
     * The map canvs.
     */
    MapCanvas[] mapCanvs;
    
    /**
     * The mov canvas.
     */
    MovCanvas movCanvas;
    
    /**
     * The explorer window.
     */
    ExplorerStackWindow explorerWindow;
    
    /**
     * The image manager.
     */
    ImageManager imageManager;
    
    /**
     * The o H.
     */
    OutlineHandler oH;

    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(String string) {
        try {
            // do {
            OpenDialog od = new OpenDialog(
                    "Open paramater file (" + FileExtensions.configFileExt + ")...",
                    OpenDialog.getLastDirectory(), "");
            if (od.getFileName() == null) {
                return;
            }
            File paramFile = new File(od.getDirectory(), od.getFileName());
            EXp.qp = new QParams(paramFile);
            EXp.qp.readParams();
            setup();
            return;
            // } while (true);

        } catch (Exception e) {
            // IJ.error("Unknown exception");
            e.printStackTrace();
        }
    }

    /**
     * The Class MapCanvas.
     */
    @SuppressWarnings("serial")
    class MapCanvas extends ImageCanvas {

        /**
         * Instantiates a new map canvas.
         *
         * @param imp the imp
         */
        MapCanvas(ImagePlus imp) {
            super(imp);
        }

        /* (non-Javadoc)
         * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            IJ.log("Map pressed at: (" + offScreenX(e.getX()) + "," + offScreenY(e.getY()) + ")");
            mapUpdate(e.getX(), e.getY());
        }
    }

    /**
     * The Class MovCanvas.
     */
    @SuppressWarnings("serial")
    class MovCanvas extends ImageCanvas {

        /**
         * Instantiates a new mov canvas.
         *
         * @param imp the imp
         */
        MovCanvas(ImagePlus imp) {
            super(imp);
        }

        /* (non-Javadoc)
         * @see ij.gui.ImageCanvas#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) {
            IJ.log("Movie pressed at: (" + offScreenX(e.getX()) + "," + offScreenY(e.getY()) + ")");
        }
    }

    /**
     * The Class ExplorerStackWindow.
     */
    @SuppressWarnings("serial")
    class ExplorerStackWindow extends StackWindow
            implements ActionListener, ItemListener, ChangeListener {

        /**
         * Instantiates a new explorer stack window.
         *
         * @param imp the imp
         * @param stackCanvas the stack canvas
         */
        ExplorerStackWindow(ImagePlus imp, ImageCanvas stackCanvas) {
            super(imp, stackCanvas);

        }

        /* (non-Javadoc)
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        @Override
        public void itemStateChanged(ItemEvent ie) {
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent ce) {
        }

        /* (non-Javadoc)
         * @see ij.gui.StackWindow#updateSliceSelector()
         */
        @Override
        public void updateSliceSelector() {
            super.updateSliceSelector();
            EXp.frame = sliceSelector.getValue();
            imageManager.updateOverlays(); // draw overlay
        } // update the frame label, overlay, frame and set zoom
    }

    /**
     * Setup.
     */
    void setup() {
        oH = new OutlineHandler(EXp.qp);
        if (!oH.readSuccess) {
            IJ.error("Failed to read " + EXp.qp.getSnakeQP().getName());
            return;
        }
        imageManager = new ImageManager(oH);
        buildWindows();
    }

    /**
     * Builds the windows.
     */
    void buildWindows() {
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

    /**
     * Map update.
     *
     * @param x the x
     * @param y the y
     */
    void mapUpdate(int x, int y) {
        imageManager.setSlice(y);
    }

    /**
     * Map 2 mov.
     *
     * @return the extended vector 2 d
     */
    ExtendedVector2d map2Mov() {
        return new ExtendedVector2d(-1, -1);
    }
}

class ImageManager {

    ImagePlus[] mapsIpl;
    ImagePlus movieIpl;
    ImagePlus xMap, yMap;
    Overlay[] movOverlay;
    ExtendedVector2d mapCoord;
    ExtendedVector2d movCoord;
    int nbMaps;

    ImageManager(OutlineHandler oH) {
        nbMaps = 1;
        mapsIpl = new ImagePlus[1];

        xMap = openMap(EXp.qp.getxFile(), "xMap");
        yMap = openMap(EXp.qp.getxFile(), "yMap");

        mapsIpl[0] = openMap(EXp.qp.getMotilityFile(), "Motility Map");

        movieIpl = IJ.openImage(EXp.qp.getSegImageFile().getAbsolutePath());
        movOverlay = new Overlay[movieIpl.getStackSize()];

        // build overlays
        movOverlay = new Overlay[movieIpl.getStackSize()];
        for (int i = 1; i <= movieIpl.getStackSize(); i++) {
            movOverlay[i - 1] = new Overlay();
            if (oH.isOutlineAt(i)) {
                movOverlay[i - 1].add(oH.getOutline(i).asFloatRoi());
            }
        }

    }

    private ImagePlus openMap(File f, String n) {

        if (!f.exists()) {
            IJ.log("Could not open " + f.getName());
            return null;
        }

        TextReader textR = new TextReader();
        ImageProcessor ip = textR.open(f.getAbsolutePath());
        return new ImagePlus(n, ip);
    }

    void updateOverlays() {
        movieIpl.setOverlay(movOverlay[EXp.frame - 1]);
    }

    void setSlice(int i) {
        movieIpl.setSlice(i);
        EXp.frame = i;
        this.updateOverlays();
    }

}

class EXp {

    static QParams qp;
    static int frame;

    EXp() {

    }

}