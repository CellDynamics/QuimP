package uk.ac.warwick.wsbc.QuimP.utils;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.plugin.randomwalk.PropagateSeeds;

/**
 * @author p.baniukiewicz
 *
 */
public class IJToolsTest {

    @Test
    public void testGetComposite() throws Exception {
        ImagePlus org = IJ.openImage("src/test/resources/G.tif");
        ImagePlus small = IJ.openImage("src/test/resources/R.tif");
        ImagePlus big = IJ.openImage("src/test/resources/B.tif");

        ImagePlus ret = IJTools.getComposite(org, small, big);
        IJ.saveAsTiff(ret, "/tmp/composite.tif");
        // ret.show();
    }

    @Test
    public void testGetComposite_stack() throws Exception {
        PropagateSeeds.Contour cc = new PropagateSeeds.Contour();
        ImagePlus org = IJ.openImage("src/test/resources/G1.tif");
        ImagePlus small = IJ.openImage("src/test/resources/R1.tif");
        ImagePlus big = IJ.openImage("src/test/resources/B1.tif");

        ImagePlus ret = IJTools.getComposite(org, small, big);
        IJ.saveAsTiff(ret, "/tmp/Downloads/compositestack.tif");
    }

}
