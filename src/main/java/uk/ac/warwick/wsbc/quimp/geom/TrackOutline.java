package uk.ac.warwick.wsbc.quimp.geom;

import java.util.ArrayList;
import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.quimp.Outline;
import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

/*
 * //!>
 * @startuml doc-files/TrackOutline_1_UML.png
 * User->(Create object)
 * User->(Convert Outlines to Point2d)
 * User->(get deep copy of Outlines)
 * @enduml
 * 
 * @startuml doc-files/TrackOutline_2_UML.png
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
 * //!<
 */
/**
 * Convert BW masks into list of vertices in correct order. Stand as ROI holder.
 * 
 * <p>The algorithm uses IJ tools for tracking and filling (deleting) objects It goes through all
 * points of the image and for every visited point it checks whether the value is different than
 * defined background. If it is, the Wand tool is used to select object given by the pixel value
 * inside it. The ROI (outline) is then stored in this object and served as reference The ROI is
 * then used to delete selected object from image (using background value). Next, the algorithm
 * moves to next pixel (of the same image the object has been deleted from, so it is not possible to
 * detect the same object twice).
 * 
 * <p>It assigns also frame number to outline<br>
 * <img src="doc-files/TrackOutline_1_UML.png"/><br>
 * Creating object runs also outline detection and tracking. Detected outlines are stored in object
 * and can be accessed by reference directly from \a outlines array or as copies from
 * getCopyofShapes().<br>
 * <img src="doc-files/TrackOutline_2_UML.png"/><br>
 * 
 * @author p.baniukiewicz
 * @see uk.ac.warwick.wsbc.quimp.geom.SegmentedShapeRoi
 *
 */
public class TrackOutline {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(TrackOutline.class.getName());

  /**
   * Original image. It is not modified.
   */
  protected ImageProcessor imp;
  /**
   * Image under process. It is modified by Outline methods.
   */
  private ImageProcessor prepared;

  /**
   * List of found outlines as ROIs.
   */
  public ArrayList<SegmentedShapeRoi> outlines;

  /**
   * The background color.
   */
  protected int background;
  /**
   * Maximal number of searched objects, all objects if negative.
   */
  private int maxNumObj = -1;
  /**
   * Frame for which imp has been got.
   */
  private int frame;

  /**
   * Constructor from ImageProcessor.
   * 
   * @param imp Image to process (not modified)
   * @param background Color value for background
   * @param frame Frame of stack that \a imp belongs to
   * @throws IllegalArgumentException when wrong image format is provided
   */
  public TrackOutline(ImageProcessor imp, int background, int frame) {
    if (imp.getBitDepth() != 8 && imp.getBitDepth() != 16) {
      throw new IllegalArgumentException("Only 8-bit or 16-bit images are supported");
    }
    outlines = new ArrayList<>();
    this.imp = imp;
    this.background = background;
    this.prepared = prepare();
    this.frame = frame;
    getOutlines();
  }

  /**
   * Constructor from ImageProcessor for single images.
   * 
   * @param imp Image to process (not modified)
   * @param background Color value for background
   * @throws IllegalArgumentException when wrong image format is provided
   */
  public TrackOutline(ImageProcessor imp, int background) {
    this(imp, background, 1);
  }

  /**
   * Default constructor.
   * 
   * @param im Image to process (not modified), 8-bit, one slice
   * @param background Background color
   */
  public TrackOutline(ImagePlus im, int background) {
    this(im.getProcessor(), background, 1);
  }

  /**
   * Filter input image to remove single pixels.
   * 
   * <p>Implement closing followed by opening.
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
   * Get outline using Wand tool.
   * 
   * @param col Any point inside region
   * @param row Any point inside region
   * @param color Color of object
   * @return ShapeRoi that contains ROI for given object with assigned frame to it
   * @throws IllegalArgumentException when wand was not able to find point
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
   * Try to find all outlines on image.
   * 
   * <p>It is possible to limit number of searched outlines setting maxNumObj > 0 The algorithm goes
   * through every pixel on image and if this pixel is different than background (defined in
   * constructor) it uses it as source of Wand. Wand should outline found object, which is then
   * erased from image. then next pixel is analyzed.
   * 
   * <p>Fills outlines field that contains list of all ROIs obtained for this image together with
   * frame number assigned to TrackOutline
   * 
   */
  private void getOutlines() {
    // go through the image and look for non \a background pixels
    outer: for (int r = 0; r < prepared.getHeight(); r++) {
      for (int c = 0; c < prepared.getWidth(); c++) {
        if (prepared.getPixel(c, r) != background) { // non background pixel
          // remember outline and delete it from input image
          outlines.add(getOutline(c, r, prepared.getPixel(c, r)));
          if (maxNumObj > -1) {
            if (outlines.size() >= maxNumObj) {
              LOGGER.warn("Reached maximal number of outlines");
              break outer;
            }
          }
        }
      }
    }
  }

  /**
   * Convert found Oulines to Outline.
   * 
   * @param step resolution step
   * @param smooth true to use IJ polygon smoothing (running average).
   * @return List of Outline object that represents all
   * @see SegmentedShapeRoi#getOutlineasPoints()
   * @see #getOutlinesasPoints(double, boolean)
   */
  public List<Outline> getOutlines(double step, boolean smooth) {
    List<SegmentedShapeRoi> rois = getCopyofShapes();
    // convert to Outlines from ROIs
    ArrayList<Outline> outlines = new ArrayList<>();
    for (SegmentedShapeRoi sr : rois) {
      // interpolate and reduce number of points
      sr.setInterpolationParameters(step, false, smooth);
      Outline o;
      try {
        o = new QuimpDataConverter(sr.getOutlineasPoints()).getOutline();
        outlines.add(o);
      } catch (QuimpException e) {
        // TODO Remove this after introducing outline constructor
        e.printStackTrace();
      }

    }
    return outlines;
  }

  /**
   * Erase roi on image stored in object with color bckColor.
   * 
   * @param roi roi on this image
   * @param bckColor color for erasing
   */
  private void clearRoi(Roi roi, int bckColor) {
    prepared.setColor(bckColor);
    prepared.fill(roi);
  }

  /**
   * Convert found outlines to List.
   * 
   * @param step step - step during conversion outline to points. For 1 every point from outline
   *        is included in output list
   * @param smooth true for using running average during interpolation
   * @return List of List of ROIs
   * @see SegmentedShapeRoi#getOutlineasPoints()
   */
  public List<List<Point2d>> getOutlinesasPoints(double step, boolean smooth) {
    List<List<Point2d>> ret = new ArrayList<>();
    for (SegmentedShapeRoi sr : outlines) {
      sr.setInterpolationParameters(step, false, smooth);
      ret.add(sr.getOutlineasPoints());
    }
    return ret;
  }

  /**
   * Convert found outlines to List without any interpolation.
   * 
   * @return List of List of ROIs
   * 
   * @see SegmentedShapeRoi#getOutlineasPoints()
   */
  public List<List<Point2d>> getOutlineasRawPoints() {
    List<List<Point2d>> ret = new ArrayList<>();
    for (SegmentedShapeRoi sr : outlines) {
      ret.add(sr.getOutlineasRawPoints());
    }
    return ret;
  }

  /**
   * 
   * @return deep copy of Rois.
   */
  public List<SegmentedShapeRoi> getCopyofShapes() {
    ArrayList<SegmentedShapeRoi> clon = new ArrayList<>();
    for (SegmentedShapeRoi sr : outlines) {
      clon.add((SegmentedShapeRoi) sr.clone());
    }
    return clon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "\nTrackOutline [outlines=" + outlines + "]";
  }

}
