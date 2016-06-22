/**
 * @file QuimPArrayUtilsTest.java
 * @date 22 Jun 2016
 */
package uk.ac.warwick.wsbc.QuimP.utils;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 * @date 22 Jun 2016
 *
 */
public class QuimPArrayUtilsTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(QuimPArrayUtilsTest.class.getName());

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
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.utils.QuimPArrayUtils#float2Ddouble(float[][])}.
     */
    @Test
    public void testFloat2Ddouble() throws Exception {
        float[][] in = { { 1.0f, 2.0f, 3.0f }, { 1.11f, 2.11f, 3.11f } };
        double[][] out = QuimPArrayUtils.float2Ddouble(in);
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 3; c++)
                assertEquals(in[r][c], out[r][c], 1e-3);
    }

}
