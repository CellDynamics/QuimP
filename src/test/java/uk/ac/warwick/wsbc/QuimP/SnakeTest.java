/**
 * @file SnakeTest.java
 * @date 10 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

/**
 * @author p.baniukiewicz
 * @date 10 Apr 2016
 *
 */
public class SnakeTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(SnakeTest.class.getName());

    private String[] info = { "QuimP", "verr", "ddd" };
    private Snake snake1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        snake1 = null;
    }

    /**
     * @test Test method for {@link uk.ac.warwick.wsbc.QuimP.Snake#setNewHead(int)}. Set head
     * for non existing node.
     * @pre valid snake with 4 nodes
     * @post the same snake with head in the same position
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
     * @test Test method for {@link uk.ac.warwick.wsbc.QuimP.Snake#setNewHead(int)}. Set head
     * for second node.
     * @pre valid snake with 4 nodes
     * @post the same snake with head in on second position
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
    }
}
