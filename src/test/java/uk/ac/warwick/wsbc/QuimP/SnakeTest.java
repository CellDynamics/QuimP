/**
 * @file SnakeTest.java
 * @date 10 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;

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

    /**
     * Test for saving/loading snakes. 
     * @pre Valid snake, Set head to second node
     * @post Loaded Snake the same as before save
     * @throws Exception
     */
    @Test
    public void testSerialize1() throws Exception {
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
        LOGGER.debug(s.toString());

        SnakeDumper sd = new SnakeDumper(s);
        Serializer<SnakeDumper> serializer;
        serializer = new Serializer<>(sd, info);
        serializer.setPretty();
        LOGGER.debug(serializer.toString());

        serializer.save("/tmp/snake.tmp");

        Serializer<SnakeDumper> loaded;
        Serializer<SnakeDumper> loader = new Serializer<>(SnakeDumper.class);
        loaded = loader.load("/tmp/snake.tmp");

        Snake snakeloaded = loaded.obj.s;
        assertEquals(s.getNumNodes(), snakeloaded.getNumNodes());
        for (int i = 0; i < s.getNumNodes(); i++) {
            Node s1 = s.getHead();
            Node s2 = snakeloaded.getHead();
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

    }

}
