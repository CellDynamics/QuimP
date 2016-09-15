package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.plugin.QconfLoader;

/**
 * @author p.baniukiewicz
 *
 */
public class FormatConverterTest {
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
        Files.deleteIfExists(
                Paths.get("src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0.snQP"));
        Files.deleteIfExists(
                Paths.get("src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP"));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.FormatConverter#generatepaQP()}.
     */
    @Test
    public void testGeneratepaQP() throws Exception {
        FormatConverter fC = new FormatConverter(
                new File("src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
        fC.generatepaQP();

        // compare paQP
        // manualy generated one
        FileInputStream readerexpected = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0_expected.paQP");
        // expected
        FileInputStream readertest = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP");

        readerexpected.skip(76); // skip header with random controlsum
        readertest.skip(76);
        byte[] expected = new byte[512];
        byte[] test = new byte[512];
        readerexpected.read(expected);
        readerexpected.close();
        readertest.read(test);
        readertest.close();

        assertThat(test, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.FormatConverter#generatesnQP()}.
     */
    @Test
    public void testGeneratesnQP() throws Exception {
        FormatConverter fC = new FormatConverter(
                new File("src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
        fC.generatesnQP();
        Thread.sleep(1000);
        // compare paQP
        // manualy generated one
        FileInputStream readerexpected = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0_expected.paQP");
        // expected
        FileInputStream readertest = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP");

        readerexpected.skip(76); // skip header with random controlsum
        readertest.skip(76);
        byte[] expected = new byte[512];
        byte[] test = new byte[512];
        readerexpected.read(expected);
        readerexpected.close();
        readertest.read(test);
        readertest.close();

        assertThat(test, is(expected));
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.FormatConverter#FormatConverter(uk.ac.warwick.wsbc.QuimP.QParamsQconf, java.nio.file.Path)}.
     */
    @Test
    public void testFormatConverterQParamsQconfPath() throws Exception {
        QconfLoader qC = new QconfLoader(
                Paths.get("src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth.QCONF"));
        FormatConverter fC = new FormatConverter((QParamsQconf) qC.getQp(),
                Paths.get("src/test/resources/FormatConverter"));
        fC.generateOldDataFiles();

        // compare paQP
        // manualy generated one
        FileInputStream readerexpected = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0_expected.paQP");
        // expected
        FileInputStream readertest = new FileInputStream(
                "src/test/resources/FormatConverter/fluoreszenz-test_eq_smooth_0.paQP");

        readerexpected.skip(76); // skip header with random controlsum
        readertest.skip(76);
        byte[] expected = new byte[512];
        byte[] test = new byte[512];
        readerexpected.read(expected);
        readerexpected.close();
        readertest.read(test);
        readertest.close();

        assertThat(test, is(expected));
    }

}
