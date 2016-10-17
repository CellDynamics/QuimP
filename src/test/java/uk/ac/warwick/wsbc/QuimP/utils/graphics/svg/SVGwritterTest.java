package uk.ac.warwick.wsbc.QuimP.utils.graphics.svg;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 *
 */
public class SVGwritterTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.graphics.svg.SVGwritter#writeHeader(java.io.OutputStreamWriter)}.
     */
    @Test
    public void testWriteHeader() throws Exception {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("/tmp/t.svg"));
        OutputStreamWriter osw = new OutputStreamWriter(out);
        SVGwritter.writeHeader(osw, new Rectangle(-10, -10, 10, 10));
        SVGwritter.QPolarAxes qc = new SVGwritter.QPolarAxes(new Rectangle(-10, -10, 10, 10));
        qc.draw(osw);
        osw.write("</svg>\n");
        osw.close();
    }

}