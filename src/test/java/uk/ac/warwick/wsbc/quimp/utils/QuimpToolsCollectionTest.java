/**
 */
package uk.ac.warwick.wsbc.quimp.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection;

// TODO: Auto-generated Javadoc
/**
 * @author p.baniukiewicz
 *
 */
public class QuimpToolsCollectionTest {
    
    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(QuimpToolsCollectionTest.class.getName());

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
     * Test method for
     * {@link uk.ac.warwick.wsbc.quimp.utils.QuimpToolsCollection#getQuimPversion()}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFormattedQuimPversion() throws Exception {
        LOGGER.debug(new QuimpToolsCollection().getQuimPversion());
    }

}
