/**
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
@Deprecated
public class QconfSupporterTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.QconfSupporter#getImage()}.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unused")
    @Test
    @Ignore("Use GUI for testing. Not for automated run")
    public void testGetImage() throws Exception {
        ImageJ ij = new ImageJ();
        ImagePlus i1 = IJ.openImage("src/test/resources/fluoreszenz-test_eq_smooth_frames_1-5.tif");
        i1.show();
        ImagePlus i2 = IJ.openImage("src/test/resources/Stack_cut.tif");
        i2.show();
        QuimpPluginCoreRef o = new QuimpPluginCoreRef(Paths.get(
                "src/test/resources/ProtAnalysisTest/KZ4-220214-cAR1-GFP-devel5noimage.QCONF"));

        o.getImage();
    }

}

@SuppressWarnings("deprecation")
class QuimpPluginCoreRef extends QconfSupporter {

    /**
     * 
     */
    public QuimpPluginCoreRef() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param path
     */
    public QuimpPluginCoreRef(Path path) {
        super(path);
        // TODO Auto-generated constructor stub
    }

}