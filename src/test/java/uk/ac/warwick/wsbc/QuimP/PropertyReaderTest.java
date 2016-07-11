/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Test class for PropertyReader
 * @author baniu
 *
 */
public class PropertyReaderTest {
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
