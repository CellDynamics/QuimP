/**
 * @file ToolTest.java
 * @date 10 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author p.baniukiewicz
 * @date 10 May 2016
 *
 */
public class ToolTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(ToolTest.class.getName());

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
         * Test method for {@link uk.ac.warwick.wsbc.QuimP.Tool#getQuimPversion()}.
         */
    @Test
    public void testGetFormattedQuimPversion() throws Exception {
        LOGGER.debug(new Tool().getQuimPversion());
    }

}
