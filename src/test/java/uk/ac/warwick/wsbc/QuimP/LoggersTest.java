package uk.ac.warwick.wsbc.QuimP;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 *
 */
public class LoggersTest {

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Loggers#logg()}.
     */
    @Test
    public void testLogg() throws Exception {
        new Loggers().logg();
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Loggers#scilog()}.
     */
    @Test
    @Ignore
    public void testScilog() throws Exception {
        new Loggers().scilog();
    }

}
