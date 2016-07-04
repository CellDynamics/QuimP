/**
 * @file TrackOutline.java
 * @date 24 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.geom;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter;

/**
 * Convert BW masks into list of vertices in correct order. Stand as ROI holder.
 * 
 * The algorithm uses IJ tools for tracking and filling (deleting) objects
 * It goes through all points of the image and for every visited point
 * it checks whether the value is different than defined background.
 * If it is, the Wand tool is used to select object given by
 * the pixel value inside it. 
 * The ROI (outline) is then stored in this object and served as reference
 * The ROI is then used to delete selected object from image (using background value).
 * Next, the algorithm moves to next pixel (of the same image the object has been deleted from, so
 * it is not possible to detect the same object twice).
 * 
 * It assigns also frame number to outline
 * 
 * @startuml
 * User->(Create object)
 * User->(Convert Outlines to Point2d)
 * User->(get deep copy of Outlines)
 * @enduml
 * 
 * Creating object runs also outline detection and tracking. Detected outlines are stored in object
 * and can be accessed by reference directly from \a outlines array or as copies from 
 * getCopyofShapes().
 * 
 * @startuml
 * actor User
 * User-->TrackOutline : <<create>>\n""image"",""frame""
 * TrackOutline->prepare : ""image""
 * note left 
 * Filtering and BW
 * operations
 * endnote
 * prepare -> prepare : //open//
 * prepare->prepare : //close//
 * prepare->TrackOutline : ""prepared""
 * TrackOutline -> getOutlines
 * loop every pixel
 * getOutlines->getOutlines : check condition
 * getOutlines->getOutline : not background pixel [x,y]
 * getOutline->Wand : [x,y]
 * Wand->getOutline : ""xpoints"",""ypoints""
 * getOutline->SegmentedShapeRoi : <<create>>
 * SegmentedShapeRoi-->getOutline
 * getOutline->getOutline : clear ROI on image
 * getOutline->SegmentedShapeRoi : set ""frame""
 * SegmentedShapeRoi-->getOutline
 * getOutline->getOutlines : ""SegmentedShapeRoi""
 * getOutlines->getOutlines : store ""SegmentedShapeRoi""
 * end
 * getOutlines->TrackOutline
 * @enduml
 *  
 * @author p.baniukiewicz
 * @date 24 Jun 2016
 * @see uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi
 *
 */
public class TrackOutline {

    private static final Logger LOGGER = LogManager.getLogger(TrackOutline.class.getName());

    protected ImageProcessor imp; //!< Original image. It is not modified
    private ImageProcessor prepared; //!< Image under process. It is modified by Outline methods
    public ArrayList<SegmentedShapeRoi> outlines; //!< List of found outlines as ROIs
    protected int background; //!< Background color
    private int MAX = -1; //!< Maximal number of searched objects,  all objects if negative
    private int frame; //!< Frame for which imp has been got

    /**
     * Constructor from ImageProcessor
     * 
     * @param imp Image to process (not modified)
     * @param background Color value for background
     * @param frame Frame of stack that \a imp belongs to
     */
    public TrackOutline(ImageProcessor imp, int background, int frame) {
        if (imp.getBitDepth() != 8 && imp.getBitDepth() != 16)
            throw new IllegalArgumentException("Only 8-bit or 16-bit images are supported");
        outlines = new ArrayList<>();
        this.imp = imp;
        this.background = background;
        this.prepared = prepare();
        this.frame = frame;
        getOutlines();
    }

    /**
     * Default constructor
     * @param im Image to process (not modified), 8-bit, one slice
     * @param background Background color
     */
    public TrackOutline(ImagePlus im, int background) {
        this(im.getProcessor(), background, 1);
    }

    /**
     * Filter input image to remove single pixels
     * 
     * Implement closing followed by opening
     * 
     * @return Filtered processor
     */
    ImageProcessor prepare() {
        ImageProcessor filtered = imp.duplicate();
        // closing
        filtered.dilate();
        filtered.erode();
        // opening
        filtered.erode();
        filtered.dilate();

        return filtered;
    }

    /**
     * Get outline using Wand tool
     * 
     * @param col Any point inside region
     * @param row Any point inside region
     * @param color Color of object
     * @return ShapeRoi that contains ROI for given object with assigned frame to it
     */
    SegmentedShapeRoi getOutline(int col, int row, int color) {
        Wand wand = new Wand(prepared);
        wand.autoOutline(col, row, color, color, Wand.EIGHT_CONNECTED);
        if (wand.npoints == 0) {
            throw new IllegalArgumentException("Wand: Points not found");
        }
        Roi roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.FREEROI);
        clearRoi(roi, background);
        SegmentedShapeRoi ret = new SegmentedShapeRoi(roi); // create segmentation object
        ret.setFrame(frame); // set current frame to this object
        return ret;
    }

    /**
     * Try to find all outlines on image
     * 
     * It is possible to limit number of searched outlines setting \a MAX > 0
     * The algorithm goes through every pixel on image and if this pixel is different than 
     * background (defined in constructor) it uses it as source of Wand. Wand should outline found
     * object, which is then erased from image. then next pixel is analyzed.
     * 
     * Fills \a outlines field that contains list of all ROIs obtained for this image together with 
     * frame number assigned to TrackOutline
     *  
     */
    private void getOutlines() {
        // go through the image and look for non 0 pixels
        outer: for (int r = 0; r < prepared.getHeight(); r++)
            for (int c = 0; c < prepared.getWidth(); c++) {
                if (prepared.getPixel(c, r) != background) { // non background pixel
                    outlines.add(getOutline(c, r, prepared.getPixel(c, r))); // remember outline and
                                                                             // delete it from input
                                                                             // image
                    if (MAX > -1) // not all
                        if (outlines.size() >= MAX) {
                            LOGGER.warn("Reached maximal number of outlines");
                            break outer;
                        }
                }
            }
    }

    /**
     * Erase \a roi on image stored in object with color \a bckColor
     * 
     * @param roi roi on this image
     * @param bckColor color for erasing
     */
    private void clearRoi(Roi roi, int bckColor) {
        prepared.setColor(bckColor);
        prepared.fill(roi);
    }

    /**
     * Convert found outlines to List
     * @param step step - step during conversion outline to points. For 1 every point from outline
     * is included in output list
     * @param smooth \a true for using smoothing during interpolation 
     * @return List of List of ROIs
     * @see SegmentedShapeRoi.getOutlineasPoints(double, boolean)
     */
    public List<List<Point2d>> getOutlinesasPoints(double step, boolean smooth) {
        List<List<Point2d>> ret = new ArrayList<>();
        FloatPolygon fp;
        for (SegmentedShapeRoi sR : outlines) {
            fp = sR.getInterpolatedPolygon(step, smooth);
            ret.add(new QuimpDataConverter(fp.xpoints, fp.ypoints).getList());
        }
        return ret;
    }

    /**
     * Return deep copy of Rois
     * @return
     */
    public List<SegmentedShapeRoi> getCopyofShapes() {
        ArrayList<SegmentedShapeRoi> clon = new ArrayList<>();
        for (SegmentedShapeRoi sR : outlines)
            clon.add((SegmentedShapeRoi) sR.clone());
        return clon;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "\nTrackOutline [outlines=" + outlines + "]";
    }

}