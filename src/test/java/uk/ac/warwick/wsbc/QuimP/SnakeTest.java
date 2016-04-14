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

}
