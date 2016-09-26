/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin.randomwalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;

/**
 * Generate new seeds for n+1 frame in stack using previous results of segmentation
 * 
 * @author p.baniukiewicz
 *
 */
public class PropagateSeeds {
    final static int ERODE = 0;
    final static int DILATE = 1;

    /**
     * Generate new seeds using segmented image
     * 
     * @param previous segmented image, background on \b zero
     * @param iter number of erode/dilate iterations
     *  
     * @return Map containing list of coordinates that belong to foreground and background. Map is
     * addressed by two enums: \a FOREGROUND and \a BACKGROUND
     * @see RandomWalkSegmentation.decodeSeeds(ImagePlus, Color, Color)
     */
    public static Map<Integer, List<Point>> propagateSeed(ImageProcessor previous, int iter) {
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

        // apply big to old background making object bigger and prevent covering objects on frame
        // n+1
        // by previous background (make "empty" not seeded space around objects)
        // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_bigbef.tif");
        // IJ.saveAsTiff(new ImagePlus("", cp), "/tmp/testIterateMorphological_cp.tif");
        for (int x = 0; x < cp.getWidth(); x++)
            for (int y = 0; y < cp.getHeight(); y++) {
                big.putPixel(x, y, big.getPixel(x, y) | cp.getPixel(x, y));
            }

        // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_big.tif");

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

    public static void iterateMorphological(BinaryProcessor ip, int oper, int iter) {
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
