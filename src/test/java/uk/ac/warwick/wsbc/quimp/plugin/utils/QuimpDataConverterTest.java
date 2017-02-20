/**
 */
package uk.ac.warwick.wsbc.quimp.plugin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.quimp.BOAState;
import uk.ac.warwick.wsbc.quimp.BOA_;
import uk.ac.warwick.wsbc.quimp.Node;
import uk.ac.warwick.wsbc.quimp.Snake;
import uk.ac.warwick.wsbc.quimp.plugin.utils.QuimpDataConverter;

/**
 * @author p.baniukiewicz
 *
 */
public class QuimpDataConverterTest {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(QuimpDataConverterTest.class.getName());

    private double X[], Y[];
    private ArrayList<Point2d> list;
    private Snake snake;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        BOA_.qState = new BOAState(null);
        X = new double[] { 3.0, 6.0, 6.0, 3.0 };
        Y = new double[] { 2.0, 2.0, 6.0, 6.0 };
        list = new ArrayList<Point2d>(4);
        list.add(new Point2d(3, 2));
        list.add(new Point2d(6, 2));
        list.add(new Point2d(6, 6));
        list.add(new Point2d(3, 6));
        snake = new Snake(list, 1);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * testConversion_1
     * 
     * pre: list on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_1() throws Exception {
        QuimpDataConverter dc = new QuimpDataConverter(list);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        Node n = s.getHead();
        // dirty hack - move head if it was set to last node due to removing fake head in Snake
        // constructor
        if (n.getY() == 6) {
            s.setNewHead(n.getNext().getTrackNum());
            n = s.getHead();
        }
        int i = 0;
        do {
            assertEquals(X[i], n.getX(), 1e-5);
            assertEquals(Y[i], n.getY(), 1e-5);
            i++;
            n = n.getNext();
        } while (!n.isHead());

    }

    /**
     * testConversion_2
     * 
     * pre: XY on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_2() throws Exception {
        QuimpDataConverter dc = new QuimpDataConverter(X, Y);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        Node n = s.getHead();
        // dirty hack - move head if it was set to last node due to removing fake head in Snake
        // constructor
        if (n.getY() == 6) {
            s.setNewHead(n.getNext().getTrackNum());
            n = s.getHead();
        }
        int i = 0;
        do {
            assertEquals(X[i], n.getX(), 1e-5);
            assertEquals(Y[i], n.getY(), 1e-5);
            i++;
            n = n.getNext();
        } while (!n.isHead());

    }

    /**
     * testConversion_3
     * 
     * pre: snake on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_3() throws Exception {
        Node n = snake.getHead();
        if (n.getY() == 6) {
            snake.setNewHead(n.getNext().getTrackNum());
        }
        QuimpDataConverter dc = new QuimpDataConverter(snake);
        assertArrayEquals(X, dc.getX(), 1e-5);
        assertArrayEquals(Y, dc.getY(), 1e-5);
        assertEquals(list, dc.getList());
        Snake s = dc.getSnake(snake.getSnakeID());
        n = s.getHead();
        // dirty hack - move head if it was set to last node due to removing fake head in Snake
        // constructor
        if (n.getY() == 6) {
            s.setNewHead(n.getNext().getTrackNum());
            n = s.getHead();
        }
        int i = 0;
        if (n.getY() == 2) {
            do {
                assertEquals(X[i], n.getX(), 1e-5);
                assertEquals(Y[i], n.getY(), 1e-5);
                i++;
                n = n.getNext();
            } while (!n.isHead());
        }

    }

    /**
     * testConversion_4
     * 
     * pre: snake null on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_4() throws Exception {
        Snake input = null;
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

    /**
     * testConversion_5
     * 
     * pre: list null on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_5() throws Exception {
        ArrayList<Point2d> input = null;
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

    /**
     * testConversion_6
     * 
     * pre: list o len on input
     * 
     * @throws Exception
     */
    @Test
    public void testConversion_6() throws Exception {
        ArrayList<Point2d> input = new ArrayList<>();
        QuimpDataConverter dc = new QuimpDataConverter(input);
        assertEquals(0, dc.getX().length);
        assertEquals(0, dc.getY().length);
        assertEquals(true, dc.getList().isEmpty());
        Snake s = dc.getSnake(snake.getSnakeID());
        assertEquals(null, s);
    }

}
