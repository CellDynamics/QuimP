/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

/**
 * Test class for PropertyReader
 * @author baniu
 *
 */
public class PropertyReaderTest {
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    static final Logger LOGGER = LogManager.getLogger(PropertyReaderTest.class.getName());

    /**
     * @test read property and display it
     * @post value of key displayed
     * @throws Exception
     */
    @Test
    public void testReadProperty() throws Exception {
        LOGGER.debug(new PropertyReader().readProperty("quimpconfig.properties", "manualURL"));
    }

}
