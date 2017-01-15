/**
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * @author p.baniukiewicz
 *
 */
public class NodeTest {
    static final Logger LOGGER = LoggerFactory.getLogger(NodeTest.class.getName());
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
     * Test method for uk.ac.warwick.wsbc.QuimP.Node.Node(final Node). Test copy constructor and
     * equals methods.
     * 
     * @throws Exception
     */
    @Test
    public void testNodeNode() throws Exception {
        Node copy = new Node(n);
        Node c = n;
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertEquals(n, copy);
        LOGGER.debug(Integer.toString(n.hashCode()));
        LOGGER.debug(Integer.toString(copy.hashCode()));
        assertEquals(n.hashCode(), copy.hashCode());
        assertThat(c, is(n));
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Node.Node(final Node). Test copy constructor and
     * equals methods.
     * 
     * Pre: Copied method is modified
     * 
     * @throws Exception
     */
    @Test
    public void testNodeNode_1() throws Exception {
        Node copy = new Node(n);
        copy.addF_total(new ExtendedVector2d(2, 3));
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertThat(copy, is(not(n)));
        LOGGER.debug(Integer.toString(n.hashCode()));
        LOGGER.debug(Integer.toString(copy.hashCode()));
        assertThat(copy.hashCode(), is(not(n.hashCode())));
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Node.Node(final Node). Test copy constructor and
     * equals methods.
     * 
     * Pre: Copied method is modified - underlying class method
     * 
     * @throws Exception
     */
    @Test
    public void testNodeNode_2() throws Exception {
        Node copy = new Node(n);
        copy.setNormal(1, 1);
        LOGGER.debug(n.toString());
        LOGGER.debug(copy.toString());
        assertThat(copy, is(not(n)));
        LOGGER.debug(Integer.toString(n.hashCode()));
        LOGGER.debug(Integer.toString(copy.hashCode()));
        assertThat(copy.hashCode(), is(not(n.hashCode())));
    }

    @Test
    public void testNode2Vert() {
        Vert v = new Vert(n);
        Vert cmp = new Vert(5, 10, 15); // the same props as in Node
        cmp.setNormal(25, 17);
        LOGGER.debug(v.toString());
        LOGGER.debug(cmp.toString());
        assertThat(v.equals(cmp), is(true));
    }

}
