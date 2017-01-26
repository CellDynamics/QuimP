package uk.ac.warwick.wsbc.QuimP.utils;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;

/**
 * @author p.baniukiewicz
 *
 */
public class IJToolsTest {

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetComposite() throws Exception {
        ImagePlus org = IJ.openImage("src/test/resources/G.tif");
        ImagePlus small = IJ.openImage("src/test/resources/R.tif");
        ImagePlus big = IJ.openImage("src/test/resources/B.tif");

        ImagePlus ret = IJTools.getComposite(org, small, big);
        IJ.saveAsTiff(ret, "/tmp/composite.tif");
        // ret.show();
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetComposite_stack() throws Exception {
        ImagePlus org = IJ.openImage("src/test/resources/G1.tif");
        ImagePlus small = IJ.openImage("src/test/resources/R1.tif");
        ImagePlus big = IJ.openImage("src/test/resources/B1.tif");

        ImagePlus ret = IJTools.getComposite(org, small, big);
        IJ.saveAsTiff(ret, "/tmp/compositestack.tif");
    }

}
