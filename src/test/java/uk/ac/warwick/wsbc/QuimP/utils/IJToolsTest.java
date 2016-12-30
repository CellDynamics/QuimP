package uk.ac.warwick.wsbc.QuimP.utils;

import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.PropagateSeeds;

/**
 * @author p.baniukiewicz
 *
 */
public class IJToolsTest {

    @Test
    public void testGetComposite() throws Exception {
        ImagePlus testImage2 = IJ.openImage("src/test/resources/binary_1.tif");
        ImageJ ij = new ImageJ();
        ImagePlus ip = testImage2.duplicate();
        PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
        ImagePlus org = IJ.openImage("src/test/resources/G.tif");
        ImagePlus small = IJ.openImage("src/test/resources/R.tif");
        ImagePlus big = IJ.openImage("src/test/resources/B.tif");

        ImagePlus ret = IJTools.getComposite(org, small, big);
        IJ.saveAsTiff(ret, "c:/Users/baniu/Downloads/composite.tif");
        // ret.show();
    }

}
