/**
 * @file ToolTest.java
 * @date 10 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

// https://objectpartners.com/2013/09/18/the-benefits-of-using-assertthat-over-other-assert-methods-in-unit-tests/
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
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
    public void testGetQuimPversion() throws Exception {
        LOGGER.debug(new Tool().getQuimPversion());
    }

    @Test
    public void testMinListIndex() throws Exception {
        ArrayList<Double> ar = new ArrayList<>();
        ar.add(34.0);
        ar.add(5.0);
        ar.add(-5.0);

        assertThat(Tool.minListIndex(ar), is(2));
    }

}
