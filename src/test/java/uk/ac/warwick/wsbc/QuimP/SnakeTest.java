/**
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import uk.ac.warwick.wsbc.QuimP.geom.ExtendedVector2d;

/**
 * @author p.baniukiewicz
 *
 */
public class SnakeTest {
    static final Logger LOGGER = LoggerFactory.getLogger(SnakeTest.class.getName());

    private String[] info = { "QuimP", "verr", "ddd" };
    private Snake snake1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        BOA_.qState = new BOAState(null);
        float x[] = new float[4];
        float y[] = new float[4];
        x[0] = 0;
        y[0] = 0;
        x[1] = 10;
        y[1] = 0;
        x[2] = 10;
        y[2] = 10;
        x[3] = 0;
        y[3] = 10;

        PolygonRoi pr = new PolygonRoi(new FloatPolygon(x, y), Roi.POLYGON);
        snake1 = new Snake(pr, 1);
        snake1.getHead().addF_total(new ExtendedVector2d(2, 1));
        snake1.getHead().setNormal(15, 10);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        snake1 = null;
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Snake#setNewHead(int)}. Set head for non
     * existing node.
     * 
     * Pre: valid snake with 4 nodes
     * 
     * Post: the same snake with head in the same position
     * 
     * @throws Exception
     */
    @Test
    public void testSetNewHead() throws Exception {
        float x[] = new float[4];
        float y[] = new float[4];
        x[0] = 0;
        y[0] = 0;
        x[1] = 10;
        y[1] = 0;
        x[2] = 10;
        y[2] = 10;
        x[3] = 0;
        y[3] = 10;

        PolygonRoi pr = new PolygonRoi(new FloatPolygon(x, y), Roi.POLYGON);
        Snake s = new Snake(pr, 1);
        Node head = s.getHead();
        s.setNewHead(10);
        assertEquals(head, s.getHead());
        assertEquals(head.getTrackNum(), s.getHead().getTrackNum());
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Snake#setNewHead(int)}. Set head for second
     * node.
     * 
     * Pre: valid snake with 4 nodes
     * 
     * Post: the same snake with head in on second position
     * 
     * @throws Exception
     */
    @Test
    public void testSetNewHead_1() throws Exception {
        float x[] = new float[4];
        float y[] = new float[4];
        x[0] = 0;
        y[0] = 0;
        x[1] = 10;
        y[1] = 0;
        x[2] = 10;
        y[2] = 10;
        x[3] = 0;
        y[3] = 10;

        PolygonRoi pr = new PolygonRoi(new FloatPolygon(x, y), Roi.POLYGON);
        Snake s = new Snake(pr, 1);
        s.setNewHead(2);
        assertEquals(2, s.getHead().getTrackNum());
    }

    /**
     * Test of Snake Serializer
     * 
     * Pre: Snake is saved to disk
     * 
     * Post: Snake is loaded and restored
     * 
     * @throws IOException
     * @throws Exception
     */
    @Test
    public void testSerializeSnake_1() throws IOException, Exception {
        snake1.setNewHead(2);
        Serializer<Snake> serializer;
        serializer = new Serializer<>(snake1, info);
        serializer.setPretty();
        serializer.save("/tmp/snake1.tmp");

        // load it
        Snake loaded;
        Serializer<Snake> loader = new Serializer<>(Snake.class);
        loaded = loader.load("/tmp/snake1.tmp").obj;
        LOGGER.debug(loaded.toString());
        assertEquals(snake1.getNumNodes(), loaded.getNumNodes());
        for (int i = 0; i < snake1.getNumNodes(); i++) {
            Node s1 = snake1.getHead();
            Node s2 = loaded.getHead();
            assertEquals(s1.frozen, s2.frozen);
            assertEquals(s1.getF_total(), s2.getF_total());
            assertEquals(s1.getVel(), s2.getVel());
            assertEquals(s1.point, s2.point);
            assertEquals(s1.normal, s2.normal);
            assertEquals(s1.tan, s2.tan);
            assertEquals(s1.tracknumber, s2.tracknumber);
            assertEquals(s1.position, s2.position, 1e-6);

            s1 = s1.getNext();
            s2 = s2.getNext();
        }
        assertEquals(snake1.alive, loaded.alive);
        assertEquals(snake1.getSnakeID(), loaded.getSnakeID(), 1e-6);
        assertEquals(snake1.isFrozen(), loaded.isFrozen());

        // copmapre using equals
        assertThat(loaded, is(snake1));
    }

    /**
     * Test of copy constructor
     * 
     * @throws Exception
     */
    @Test
    public void testSnakeSnakeInt() throws Exception {
        Snake copy = new Snake(snake1, snake1.getSnakeID());
        Snake c = snake1;
        LOGGER.debug(snake1.toString());
        LOGGER.debug(copy.toString());
        assertEquals(copy, snake1);
        LOGGER.debug(Integer.toString(snake1.hashCode()));
        LOGGER.debug(Integer.toString(copy.hashCode()));
        assertEquals(copy.hashCode(), snake1.hashCode());
        assertThat(c, is(snake1));
        LOGGER.debug(copy.toString());
        LOGGER.debug(snake1.toString());
    }

    /**
     * Test of copy constructor
     * 
     * Pre: One node differs
     * 
     * @throws Exception
     */
    @Test
    public void testSnakeSnakeInt_2() throws Exception {
        Snake copy = new Snake(snake1, snake1.getSnakeID());
        Node n = copy.getHead().getNext();
        n.addVel(new ExtendedVector2d(3, 3));
        assertThat(copy, is(not(snake1)));
        LOGGER.debug(Integer.toString(snake1.hashCode()));
        LOGGER.debug(Integer.toString(copy.hashCode()));
        assertThat(copy.hashCode(), is(not(snake1.hashCode())));
    }

    @Test
    public void testSnakeToOutline() {
        Outline o = new Outline(snake1);
        LOGGER.debug(snake1.toString());
        LOGGER.debug(o.toString());
        assertThat(o.head, instanceOf(Vert.class));
        assertThat(o.POINTS, is(snake1.POINTS));
        assertThat(o.getCentroid(), is(snake1.getCentroid()));
        assertThat(o.nextTrackNumber, is(snake1.nextTrackNumber));
    }

}
