/**
 * @file NodeTest.java
 * @date 3 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * @author p.baniukiewicz
 * @date 3 May 2016
 *
 */
public class NodeTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(NodeTest.class.getName());
    private Node n;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        n = new Node(5, 10, 15);
        n.setNormal(25, 17);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * @test Test method for {@link uk.ac.warwick.wsbc.QuimP.Node#Node(uk.ac.warwick.wsbc.QuimP.Node)}.
     * Test copy constructor and equals methods
     */
    @Test
    public void testNodeNode() throws Exception {
        Node copy = new Node(n);
        Node c = n;
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertEquals(n, copy);
        LOGGER.debug(n.hashCode());
        LOGGER.debug(copy.hashCode());
        assertEquals(n.hashCode(), copy.hashCode());
        assertThat(c, is(n));
    }

    /**
     * @test Test method for {@link uk.ac.warwick.wsbc.QuimP.Node#Node(uk.ac.warwick.wsbc.QuimP.Node)}.
     * Test copy constructor and equals methods
     * @pre Copied method is modified
     */
    @Test
    public void testNodeNode_1() throws Exception {
        Node copy = new Node(n);
        copy.addF_total(new ExtendedVector2d(2, 3));
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertThat(copy, is(not(n)));
        LOGGER.debug(n.hashCode());
        LOGGER.debug(copy.hashCode());
        assertThat(copy.hashCode(), is(not(n.hashCode())));
    }

    /**
     * @test Test method for {@link uk.ac.warwick.wsbc.QuimP.Node#Node(uk.ac.warwick.wsbc.QuimP.Node)}.
     * Test copy constructor and equals methods
     * @pre Copied method is modified - underlying class method
     */
    @Test
    public void testNodeNode_2() throws Exception {
        Node copy = new Node(n);
        copy.setNormal(1, 1);
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertThat(copy, is(not(n)));
        LOGGER.debug(n.hashCode());
        LOGGER.debug(copy.hashCode());
        assertThat(copy.hashCode(), is(not(n.hashCode())));
    }

}
