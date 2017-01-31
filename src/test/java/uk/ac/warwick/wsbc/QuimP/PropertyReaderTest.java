/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Test class for PropertyReader
 * 
 * @author baniu
 *
 */
public class PropertyReaderTest {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PropertyReaderTest.class.getName());

    /**
     * read property and display it
     * 
     * Post: value of key displayed
     * 
     * @throws Exception
     */
    @Test
    public void testReadProperty() throws Exception {
        LOGGER.debug(new PropertyReader().readProperty("quimpconfig.properties", "manualURL"));
    }

}
