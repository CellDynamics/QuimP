/**
 * @file ProtrusionVisTest.java
 * @date 19 Aug 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis;

import java.awt.Polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import ij.IJ;
import ij.ImagePlus;
import uk.ac.warwick.wsbc.QuimP.STmap;

/**
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtrusionVisTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(ProtrusionVisTest.class.getName());
    private static ImagePlus originalImage;
    // http://stackoverflow.com/questions/16467685/difference-between-mock-and-injectmocks
    @Mock
    private MaximaFinder mF;
    @Mock
    private STmap mapCell;
    @InjectMocks
    private ProtrusionVis protrusionVis;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        originalImage = IJ.openImage(
                "/home/baniuk/Documents/Kay-copy/KZ4/KZ4-220214-cAR1-GFP-devel5.5h-agar07-14.tif");
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
        MockitoAnnotations.initMocks(this); // must be as we create mocked classes in mocked already
                                            // QParams
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.ProtrusionVis#ProtrusionVis(uk.ac.warwick.wsbc.QuimP.QParams, uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.MaximaFinder)}.
     */
    @Test
    public void testProtrusionVisQParamsMaximaFinder() throws Exception {

    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.plugin.Prot_Analysis.ProtrusionVis#addPointsToImage()}.
     */
    @Test
    public void testAddPointsToImage() throws Exception {
        protrusionVis.originalImage = originalImage;
        int[] frames = { 35, 28, 72, 72, 19, 140, 27 };
        int[] indexes = { 0, 0, 0, 0, 0, 0, 0 };
        double[][] xs = { { 10 }, { 50 }, { 100 }, { 150 }, { 200 }, { 300 }, { 400 } };
        double[][] ys = { { 50 }, { 60 }, { 160 }, { 210 }, { 360 }, { 460 }, { 510 } };
        Mockito.when(mapCell.getxMap()).thenReturn(xs);
        Mockito.when(mapCell.getyMap()).thenReturn(ys);

        Mockito.when(mF.getMaxima()).thenReturn(new Polygon(indexes, frames, 7));

        protrusionVis.addPointsToImage();

        protrusionVis.originalImage.show();
        while (protrusionVis.originalImage.isVisible()) {
            Thread.sleep(1500);
        }
    }

}
