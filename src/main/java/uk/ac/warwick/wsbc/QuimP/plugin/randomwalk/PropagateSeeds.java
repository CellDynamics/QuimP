/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;
import uk.ac.warwick.wsbc.QuimP.Outline;
import uk.ac.warwick.wsbc.QuimP.geom.OutlineProcessor;
import uk.ac.warwick.wsbc.QuimP.geom.TrackOutline;
import uk.ac.warwick.wsbc.QuimP.utils.IJTools;
import uk.ac.warwick.wsbc.QuimP.utils.Pair;

/**
 * Generate new seeds for n+1 frame in stack using previous results of segmentation
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class PropagateSeeds {
    final static int ERODE = 0;
    final static int DILATE = 1;
    static int STEPS = 4;

    /**
     * Contain methods for propagating seeds to the next frame using contour shrinking operations.
     * 
     * @author p.baniukiewicz
     *
     */
    public static class Contour extends PropagateSeeds {
        private List<Pair<ImageProcessor, ImageProcessor>> seeds;
        private boolean storeSeeds = false;

        public Contour() {
            this(false);
        }

        public Contour(boolean storeSeeds) {
            this.storeSeeds = storeSeeds;
            if (storeSeeds)
                seeds = new ArrayList<>();
        }

        public Map<Integer, List<Point>> propagateSeed(ImageProcessor previous) {
            ImagePlus small = IJ.createImage("", previous.getWidth(), previous.getHeight(), 1, 8);
            ImagePlus big = IJ.createImage("", previous.getWidth(), previous.getHeight(), 1, 8);
            double stepsshrink = 5 / 0.04; // total shrink/step size
            double stepsexp = 10 / 0.04; // total shrink/step size
            // output map integrating two lists of points
            HashMap<Integer, List<Point>> out = new HashMap<Integer, List<Point>>();

            List<Outline> outlines = getOutline(previous);
            for (Outline o : outlines) {
                // shrink outline - copy as we want to expand it later
                Outline copy = new Outline(o);
                new OutlineProcessor(copy).shrink(stepsshrink, 0.04, 0.1, 1); // taken
                                                                              // from anap
                Roi fr = copy.asFloatRoi();
                fr.setFillColor(Color.WHITE);
                fr.setStrokeColor(Color.WHITE);
                small.getProcessor().drawRoi(fr);
            }

            for (Outline o : outlines) {
                // shrink outline - copy as we want to expand it later
                new OutlineProcessor(o).shrink(stepsexp, -0.04, 0.1, 1); // taken from anap
                Roi fr = o.asFloatRoi();
                fr.setFillColor(Color.WHITE);
                fr.setStrokeColor(Color.WHITE);
                big.getProcessor().drawRoi(fr);
            }
            big.getProcessor().invert();
            if (storeSeeds)
                seeds.add(Pair.createPair(small.getProcessor(), big.getProcessor()));

            return out;

        }

        public ImagePlus getCompositeSeed(ImagePlus org) {
            ImagePlus ret;
            if (seeds == null)
                throw new IllegalArgumentException("Seeds were not stored.");
            int f = seeds.size();
            if (f == 0)
                throw new IllegalArgumentException("Seeds were not stored.");
            ImageStack smallstack =
                    new ImageStack(seeds.get(0).first.getWidth(), seeds.get(0).first.getHeight());
            ImageStack bigstack =
                    new ImageStack(seeds.get(0).first.getWidth(), seeds.get(0).first.getHeight());
            for (Pair<ImageProcessor, ImageProcessor> p : seeds) {
                smallstack.addSlice((ImageProcessor) p.first);
                bigstack.addSlice((ImageProcessor) p.second);
            }
            // check if stack or not. getComposite requires the same type
            if (org.getStack().getSize() == 1)
                ret = IJTools.getComposite(org.duplicate(),
                        new ImagePlus("", smallstack.getProcessor(1)),
                        new ImagePlus("", bigstack.getProcessor(1)));
            else
                ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack),
                        new ImagePlus("", bigstack));
            return ret;
        }

        /**
         * Convert mask to outline.
         * 
         * @param previous
         * @return List of Outline for current frame
         */
        private List<Outline> getOutline(ImageProcessor previous) {
            TrackOutline tO = new TrackOutline(previous, 0);
            return tO.getOutlines(STEPS, false);
        }

    }

    /**
     * Contain methods for propagating seeds to next frame using morphological operations.
     * 
     * @author p.baniukiewicz
     *
     */
    public static class Morphological extends PropagateSeeds {
        /**
         * Generate new seeds using segmented image
         * 
         * @param previous segmented image, background on \b zero
         * @param iter number of erode/dilate iterations
         * 
         * @return Map containing list of coordinates that belong to foreground and background. Map
         *         is addressed by two enums: \a FOREGROUND and \a BACKGROUND
         * @see RandomWalkSegmentation.decodeSeeds(ImagePlus, Color, Color)
         */
        public Map<Integer, List<Point>> propagateSeed(ImageProcessor previous, int iter) {
            BinaryProcessor cp = new BinaryProcessor(previous.duplicate().convertToByteProcessor());

            BinaryProcessor small = new BinaryProcessor(cp.duplicate().convertToByteProcessor()); // object
                                                                                                  // smaller
            // than on frame n
            BinaryProcessor big = new BinaryProcessor(cp.duplicate().convertToByteProcessor()); // object
                                                                                                // bigger
            // than on frame n

            // make objects smaller
            iterateMorphological(small, PropagateSeeds.ERODE, iter);
            // make background bigger
            iterateMorphological(big, PropagateSeeds.DILATE, (int) (iter * 1.5));

            // apply big to old background making object bigger and prevent covering objects on
            // frame
            // n+1
            // by previous background (make "empty" not seeded space around objects)
            // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_bigbef.tif");
            // IJ.saveAsTiff(new ImagePlus("", cp), "/tmp/testIterateMorphological_cp.tif");
            for (int x = 0; x < cp.getWidth(); x++)
                for (int y = 0; y < cp.getHeight(); y++) {
                    big.putPixel(x, y, big.getPixel(x, y) | cp.getPixel(x, y));
                }

            // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_big.tif");

            return convertToList(small, big);
        }

        private void iterateMorphological(BinaryProcessor ip, int oper, int iter) {
            switch (oper) {
                case ERODE:
                    for (int i = 0; i < iter; i++)
                        ip.erode(1, 0); // first param influence precision, for large ,the shape is
                                        // preserved and changes are very small?
                    break;
                case DILATE:
                    for (int i = 0; i < iter; i++)
                        ip.dilate(1, 0);
                    break;
                default:
                    throw new IllegalArgumentException("Binary operation not supported");
            }
        }
    }

    /**
     * Convert processors obtained for object and background to format accepted by RW.
     * 
     * @param small object mask
     * @param big background mask
     * @return List of point coordinates accepted by RW algorithm.
     */
    Map<Integer, List<Point>> convertToList(BinaryProcessor small, BinaryProcessor big) {
        // output map integrating two lists of points
        HashMap<Integer, List<Point>> out = new HashMap<Integer, List<Point>>();
        // output lists of points. Can be null if points not found
        List<Point> foreground = new ArrayList<>();
        List<Point> background = new ArrayList<>();
        for (int x = 0; x < small.getWidth(); x++)
            for (int y = 0; y < small.getHeight(); y++) {
                if (small.get(x, y) > 0) // WARN Why must be y,x??
                    foreground.add(new Point(y, x)); // remember foreground coords
                if (big.get(x, y) == 0)
                    background.add(new Point(y, x)); // remember background coords
            }
        // pack outputs into map
        out.put(RandomWalkSegmentation.FOREGROUND, foreground);
        out.put(RandomWalkSegmentation.BACKGROUND, background);
        return out;
    }

}
